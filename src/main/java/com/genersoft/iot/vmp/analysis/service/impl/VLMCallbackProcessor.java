package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.AnalysisAlarm;
import com.genersoft.iot.vmp.analysis.bean.AlarmStatus;
import com.genersoft.iot.vmp.analysis.bean.VideoWindowInfo;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMAnalysisResult;
import com.genersoft.iot.vmp.analysis.service.IAnalysisTaskService;
import com.genersoft.iot.vmp.analysis.service.IAnalysisAlarmService;
import com.genersoft.iot.vmp.storager.dao.AnalysisAlarmMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

/**
 * VLM回调数据处理器
 * @author Claude
 */
@Component
@Slf4j
public class VLMCallbackProcessor {

    @Autowired
    private IAnalysisTaskService analysisTaskService;

    @Autowired
    private IAnalysisAlarmService analysisAlarmService;

    @Autowired
    private AnalysisAlarmMapper analysisAlarmMapper;

    @Value("${analysis.snapshot.storage-path:/data/analysis/snapshots}")
    private String snapshotStoragePath;

    @Value("${analysis.callback.retry.max-attempts:3}")
    private int maxRetryAttempts;

    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * 处理VLM分析结果回调
     * @param callback VLM分析结果
     */
    @Async("taskExecutor")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void processCallback(VLMAnalysisResult callback) {
        log.info("开始处理VLM回调数据，作业ID: {}, 设备ID: {}, 通道ID: {}", 
                callback.getJobId(), callback.getDeviceId(), callback.getChannelId());
        
        try {
            // 验证回调数据
            validateCallback(callback);
            
            // 查找对应的分析任务
            AnalysisTask task = analysisTaskService.getTaskByVlmJobId(callback.getJobId());
            if (task == null) {
                log.warn("未找到对应的分析任务，VLM作业ID: {}", callback.getJobId());
                return;
            }
            
            // 更新任务最后活跃时间
            task.updateLastActiveTime();
            analysisTaskService.updateTask(task);
            
            // 处理分析事件
            if (callback.getEvents() != null && !callback.getEvents().isEmpty()) {
                for (VLMAnalysisResult.Event event : callback.getEvents()) {
                    processAnalysisEvent(task, event, callback);
                }
            }
            
            log.info("VLM回调数据处理完成，作业ID: {}, 事件数量: {}", 
                    callback.getJobId(), 
                    callback.getEvents() != null ? callback.getEvents().size() : 0);
                    
        } catch (Exception e) {
            log.error("处理VLM回调数据失败，作业ID: {}", callback.getJobId(), e);
            throw e; // 重新抛出异常以触发重试机制
        }
    }

    /**
     * 处理分析事件
     * @param task 分析任务
     * @param event 分析事件
     * @param callback 回调数据
     */
    private void processAnalysisEvent(AnalysisTask task, VLMAnalysisResult.Event event, VLMAnalysisResult callback) {
        log.debug("处理分析事件，任务ID: {}, 事件描述: {}, 是否紧急: {}", 
                task.getId(), event.getEventDescription(), event.getEmergencyExist());
        
        // 只处理紧急事件或包含快照的事件
        if ((event.getEmergencyExist() != null && event.getEmergencyExist()) || 
            !StringUtils.isEmpty(event.getSnapshotBase64())) {
            
            try {
                createAlarmFromEvent(task, event, callback);
            } catch (Exception e) {
                log.error("创建告警失败，任务ID: {}, 事件描述: {}", task.getId(), event.getEventDescription(), e);
                // 不重新抛出异常，避免影响其他事件的处理
            }
        }
    }

    /**
     * 从事件创建告警
     * @param task 分析任务
     * @param event 分析事件
     * @param callback 回调数据
     */
    private void createAlarmFromEvent(AnalysisTask task, VLMAnalysisResult.Event event, VLMAnalysisResult callback) {
        log.info("创建告警记录，任务ID: {}, 事件描述: {}", task.getId(), event.getEventDescription());
        
        // 创建告警对象
        AnalysisAlarm alarm = new AnalysisAlarm();
        alarm.setId(UUID.randomUUID().toString().replace("-", ""));
        alarm.setTaskId(task.getId());
        alarm.setDeviceId(task.getDeviceId());
        alarm.setDeviceName(task.getDeviceName());
        alarm.setChannelId(task.getChannelId());
        alarm.setChannelName(task.getChannelName());
        
        // 设置告警信息
        if (task.getAnalysisCard() != null) {
            alarm.setAnalysisType(task.getAnalysisCard().getTitle());
        }
        
        alarm.setDescription(event.getEventDescription());
        alarm.setStatus(AlarmStatus.PENDING);
        alarm.setCreatedAt(LocalDateTime.now());
        
        // 处理时间信息
        processEventTimeInfo(alarm, event);
        
        // 处理视频窗口信息
        processVideoWindowInfo(alarm, callback);
        
        // 处理快照图片
        processSnapshot(alarm, event);
        
        // 保存告警
        int result = analysisAlarmMapper.insert(alarm);
        if (result > 0) {
            log.info("告警创建成功，告警ID: {}, 任务ID: {}", alarm.getId(), task.getId());
            
            // 发送实时通知
            sendAlarmNotification(alarm);
        } else {
            log.error("告警创建失败，任务ID: {}", task.getId());
        }
    }

    /**
     * 处理事件时间信息
     */
    private void processEventTimeInfo(AnalysisAlarm alarm, VLMAnalysisResult.Event event) {
        try {
            // 设置告警时间
            if (!StringUtils.isEmpty(event.getEventStartUtc())) {
                alarm.setEventStartTime(LocalDateTime.parse(event.getEventStartUtc(), UTC_FORMATTER));
                alarm.setAlarmTime(alarm.getEventStartTime()); // 使用事件开始时间作为告警时间
            } else {
                alarm.setAlarmTime(LocalDateTime.now());
            }
            
            if (!StringUtils.isEmpty(event.getEventEndUtc())) {
                alarm.setEventEndTime(LocalDateTime.parse(event.getEventEndUtc(), UTC_FORMATTER));
            }
            
            alarm.setEventTimeRange(event.getEventTimeRange());
            
        } catch (Exception e) {
            log.warn("解析事件时间失败，使用当前时间，错误: {}", e.getMessage());
            alarm.setAlarmTime(LocalDateTime.now());
        }
    }

    /**
     * 处理视频窗口信息
     */
    private void processVideoWindowInfo(AnalysisAlarm alarm, VLMAnalysisResult callback) {
        if (callback.getVideoWindow() != null) {
            VideoWindowInfo windowInfo = new VideoWindowInfo();
            windowInfo.setWindowStartPts(callback.getVideoWindow().getWindowStartPts());
            windowInfo.setWindowEndPts(callback.getVideoWindow().getWindowEndPts());
            windowInfo.setWindowStartUtc(callback.getVideoWindow().getWindowStartUtc());
            windowInfo.setWindowEndUtc(callback.getVideoWindow().getWindowEndUtc());
            
            // 计算窗口持续时长
            if (windowInfo.getWindowStartPts() != null && windowInfo.getWindowEndPts() != null) {
                windowInfo.setWindowDurationSeconds(windowInfo.getWindowEndPts() - windowInfo.getWindowStartPts());
            }
            
            alarm.setVideoWindowInfo(windowInfo);
        }
    }

    /**
     * 处理快照图片
     */
    private void processSnapshot(AnalysisAlarm alarm, VLMAnalysisResult.Event event) {
        if (StringUtils.isEmpty(event.getSnapshotBase64())) {
            return;
        }
        
        try {
            // 解码Base64图片数据
            byte[] imageData = Base64.getDecoder().decode(event.getSnapshotBase64());
            
            // 生成文件路径
            String fileName = String.format("snapshot_%s_%s.jpg", 
                    alarm.getId(), 
                    alarm.getAlarmTime().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
            
            Path storageDir = Paths.get(snapshotStoragePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
            
            Path filePath = storageDir.resolve(fileName);
            Files.write(filePath, imageData);
            
            alarm.setSnapshotPath(filePath.toString());
            
            log.debug("快照保存成功，告警ID: {}, 文件路径: {}", alarm.getId(), filePath);
            
        } catch (Exception e) {
            log.error("保存快照失败，告警ID: {}", alarm.getId(), e);
            // 不阻断告警创建，快照保存失败时继续创建告警
        }
    }

    /**
     * 发送告警通知
     */
    private void sendAlarmNotification(AnalysisAlarm alarm) {
        try {
            // TODO: 集成WebSocket服务发送实时通知
            // webSocketService.sendAlarmNotification(alarm);
            
            log.info("发送告警通知，告警ID: {}, 设备: {}, 通道: {}", 
                    alarm.getId(), alarm.getDeviceId(), alarm.getChannelId());
                    
        } catch (Exception e) {
            log.error("发送告警通知失败，告警ID: {}", alarm.getId(), e);
        }
    }

    /**
     * 验证回调数据
     */
    private void validateCallback(VLMAnalysisResult callback) {
        if (callback == null) {
            throw new IllegalArgumentException("回调数据不能为空");
        }
        
        if (StringUtils.isEmpty(callback.getJobId())) {
            throw new IllegalArgumentException("VLM作业ID不能为空");
        }
        
        if (StringUtils.isEmpty(callback.getDeviceId())) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
        
        if (StringUtils.isEmpty(callback.getChannelId())) {
            throw new IllegalArgumentException("通道ID不能为空");
        }
    }

    /**
     * 清理内存中的图片数据
     */
    private void releaseImageData() {
        // 建议JVM进行垃圾回收，释放图片数据占用的内存
        System.gc();
    }

    /**
     * 获取处理统计信息
     */
    public CallbackProcessStats getProcessStats() {
        // TODO: 实现回调处理统计
        return new CallbackProcessStats();
    }

    /**
     * 回调处理统计信息
     */
    public static class CallbackProcessStats {
        private long totalCallbacks = 0;
        private long successCallbacks = 0;
        private long failedCallbacks = 0;
        private long emergencyEvents = 0;
        private long snapshotsSaved = 0;
        
        // getters and setters
        public long getTotalCallbacks() { return totalCallbacks; }
        public void setTotalCallbacks(long totalCallbacks) { this.totalCallbacks = totalCallbacks; }
        
        public long getSuccessCallbacks() { return successCallbacks; }
        public void setSuccessCallbacks(long successCallbacks) { this.successCallbacks = successCallbacks; }
        
        public long getFailedCallbacks() { return failedCallbacks; }
        public void setFailedCallbacks(long failedCallbacks) { this.failedCallbacks = failedCallbacks; }
        
        public long getEmergencyEvents() { return emergencyEvents; }
        public void setEmergencyEvents(long emergencyEvents) { this.emergencyEvents = emergencyEvents; }
        
        public long getSnapshotsSaved() { return snapshotsSaved; }
        public void setSnapshotsSaved(long snapshotsSaved) { this.snapshotsSaved = snapshotsSaved; }
        
        public double getSuccessRate() {
            return totalCallbacks > 0 ? (double) successCallbacks / totalCallbacks * 100 : 0.0;
        }
    }
}