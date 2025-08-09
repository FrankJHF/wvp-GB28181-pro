package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.service.IAnalysisAlarmService;
import com.genersoft.iot.vmp.analysis.service.IAnalysisTaskService;
import com.genersoft.iot.vmp.analysis.bean.AnalysisAlarm;
import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.AlarmStatus;
import com.genersoft.iot.vmp.storager.dao.AnalysisAlarmMapper;
import com.genersoft.iot.vmp.conf.exception.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 分析告警管理服务实现类
 * @author Claude
 */
@Service
@Slf4j
public class AnalysisAlarmServiceImpl implements IAnalysisAlarmService {

    @Autowired
    private AnalysisAlarmMapper analysisAlarmMapper;

    @Autowired
    private IAnalysisTaskService analysisTaskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisAlarm createAlarm(AnalysisAlarm alarm) {
        log.info("创建分析告警: {}", alarm.getDescription());
        
        // 验证必填字段
        validateAlarm(alarm, true);
        
        // 生成ID和设置时间
        if (StringUtils.isEmpty(alarm.getId())) {
            alarm.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        if (alarm.getCreatedAt() == null) {
            alarm.setCreatedAt(LocalDateTime.now());
        }
        if (alarm.getAlarmTime() == null) {
            alarm.setAlarmTime(LocalDateTime.now());
        }
        if (alarm.getStatus() == null) {
            alarm.setStatus(AlarmStatus.PENDING);
        }
        
        int result = analysisAlarmMapper.insert(alarm);
        if (result <= 0) {
            throw new ServiceException("创建分析告警失败");
        }
        
        log.info("分析告警创建成功，告警ID: {}", alarm.getId());
        
        // 发送告警通知
        sendAlarmNotification(alarm);
        
        return alarm;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAlarmStatus(String alarmId, AlarmStatus status) {
        log.info("更新告警状态，告警ID: {}, 状态: {}", alarmId, status.getDescription());
        
        if (StringUtils.isEmpty(alarmId)) {
            throw new ServiceException("告警ID不能为空");
        }
        
        if (status == null) {
            throw new ServiceException("告警状态不能为空");
        }
        
        // 检查告警是否存在
        AnalysisAlarm alarm = analysisAlarmMapper.selectById(alarmId);
        if (alarm == null) {
            throw new ServiceException("分析告警不存在，ID: " + alarmId);
        }
        
        int result = analysisAlarmMapper.updateStatus(alarmId, status.getValue());
        
        if (result > 0) {
            log.info("告警状态更新成功，告警ID: {}, 状态: {}", alarmId, status.getDescription());
            return true;
        }
        
        log.warn("告警状态更新失败，告警ID: {}", alarmId);
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateAlarmStatus(List<String> alarmIds, AlarmStatus status) {
        log.info("批量更新告警状态，数量: {}, 状态: {}", alarmIds.size(), status.getDescription());
        
        if (alarmIds == null || alarmIds.isEmpty()) {
            throw new ServiceException("告警ID列表不能为空");
        }
        
        if (status == null) {
            throw new ServiceException("告警状态不能为空");
        }
        
        int result = analysisAlarmMapper.batchUpdateStatus(alarmIds, status.getValue());
        log.info("批量更新告警状态完成，成功数量: {}", result);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAlarm(String alarmId) {
        log.info("删除分析告警: {}", alarmId);
        
        if (StringUtils.isEmpty(alarmId)) {
            throw new ServiceException("告警ID不能为空");
        }
        
        // 检查告警是否存在
        AnalysisAlarm alarm = analysisAlarmMapper.selectById(alarmId);
        if (alarm == null) {
            throw new ServiceException("分析告警不存在，ID: " + alarmId);
        }
        
        // 删除快照文件（如果存在）
        if (!StringUtils.isEmpty(alarm.getSnapshotPath())) {
            try {
                Path snapshotPath = Paths.get(alarm.getSnapshotPath());
                if (Files.exists(snapshotPath)) {
                    Files.delete(snapshotPath);
                    log.debug("快照文件删除成功: {}", alarm.getSnapshotPath());
                }
            } catch (Exception e) {
                log.warn("删除快照文件失败: {}", alarm.getSnapshotPath(), e);
            }
        }
        
        int result = analysisAlarmMapper.delete(alarmId);
        
        if (result > 0) {
            log.info("分析告警删除成功，ID: {}", alarmId);
            return true;
        }
        
        log.warn("分析告警删除失败，ID: {}", alarmId);
        return false;
    }

    @Override
    public AnalysisAlarm getAlarmById(String alarmId) {
        if (StringUtils.isEmpty(alarmId)) {
            throw new ServiceException("告警ID不能为空");
        }
        
        return analysisAlarmMapper.selectById(alarmId);
    }

    @Override
    public AnalysisAlarm getAlarmWithDetailsById(String alarmId) {
        AnalysisAlarm alarm = getAlarmById(alarmId);
        if (alarm != null) {
            // 加载关联的任务信息
            AnalysisTask task = analysisTaskService.getTaskWithDetailsById(alarm.getTaskId());
            alarm.setAnalysisTask(task);
        }
        return alarm;
    }

    @Override
    public List<AnalysisAlarm> getAlarmsByTaskId(String taskId) {
        if (StringUtils.isEmpty(taskId)) {
            throw new ServiceException("任务ID不能为空");
        }
        
        return analysisAlarmMapper.selectByTaskId(taskId);
    }

    @Override
    public List<AnalysisAlarm> getAlarmsByDeviceAndChannel(String deviceId, String channelId) {
        if (StringUtils.isEmpty(deviceId) || StringUtils.isEmpty(channelId)) {
            throw new ServiceException("设备ID和通道ID不能为空");
        }
        
        return analysisAlarmMapper.selectByDeviceAndChannel(deviceId, channelId);
    }

    @Override
    public PageInfo<AnalysisAlarm> getAlarmPage(int pageNum, int pageSize, LocalDateTime startTime, LocalDateTime endTime,
                                               String deviceId, String channelId, String analysisType, 
                                               String status, String taskId) {
        log.debug("分页查询分析告警，页码: {}, 页面大小: {}", pageNum, pageSize);
        
        PageHelper.startPage(pageNum, pageSize);
        List<AnalysisAlarm> alarms = analysisAlarmMapper.selectByTimeRange(startTime, endTime, deviceId, 
                channelId, analysisType, status, taskId);
        
        return new PageInfo<>(alarms);
    }

    @Override
    public List<AnalysisAlarm> getRecentAlarms(int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        
        return analysisAlarmMapper.selectRecent(limit);
    }

    @Override
    public List<AnalysisAlarm> getPendingAlarms() {
        return analysisAlarmMapper.selectPending();
    }

    @Override
    public long countAlarms(LocalDateTime startTime, LocalDateTime endTime, String deviceId, String channelId,
                           String analysisType, String status, String taskId) {
        return analysisAlarmMapper.count(startTime, endTime, deviceId, channelId, analysisType, status, taskId);
    }

    @Override
    public long countAlarmsByTaskId(String taskId) {
        if (StringUtils.isEmpty(taskId)) {
            throw new ServiceException("任务ID不能为空");
        }
        
        return analysisAlarmMapper.countByTaskId(taskId);
    }

    @Override
    public Map<String, Long> countAlarmsByStatus(LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> statusCounts = analysisAlarmMapper.countByStatus(startTime, endTime);
        
        Map<String, Long> result = new HashMap<>();
        for (Map<String, Object> statusCount : statusCounts) {
            String status = (String) statusCount.get("status");
            Long count = ((Number) statusCount.get("count")).longValue();
            result.put(status, count);
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteAlarms(List<String> alarmIds) {
        log.info("批量删除分析告警，数量: {}", alarmIds.size());
        
        if (alarmIds == null || alarmIds.isEmpty()) {
            throw new ServiceException("告警ID列表不能为空");
        }
        
        int deleteCount = 0;
        for (String alarmId : alarmIds) {
            try {
                if (deleteAlarm(alarmId)) {
                    deleteCount++;
                }
            } catch (Exception e) {
                log.error("删除告警失败，告警ID: {}", alarmId, e);
            }
        }
        
        log.info("批量删除分析告警完成，成功数量: {}", deleteCount);
        return deleteCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAlarmsByTaskId(String taskId) {
        log.info("删除任务关联告警，任务ID: {}", taskId);
        
        if (StringUtils.isEmpty(taskId)) {
            throw new ServiceException("任务ID不能为空");
        }
        
        // 先查询要删除的告警，以便删除快照文件
        List<AnalysisAlarm> alarms = analysisAlarmMapper.selectByTaskId(taskId);
        
        // 删除快照文件
        for (AnalysisAlarm alarm : alarms) {
            if (!StringUtils.isEmpty(alarm.getSnapshotPath())) {
                try {
                    Path snapshotPath = Paths.get(alarm.getSnapshotPath());
                    if (Files.exists(snapshotPath)) {
                        Files.delete(snapshotPath);
                    }
                } catch (Exception e) {
                    log.warn("删除快照文件失败: {}", alarm.getSnapshotPath(), e);
                }
            }
        }
        
        int result = analysisAlarmMapper.deleteByTaskId(taskId);
        log.info("任务关联告警删除完成，任务ID: {}, 删除数量: {}", taskId, result);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupAlarmsBeforeTime(LocalDateTime beforeTime) {
        log.info("清理历史告警，时间阈值: {}", beforeTime);
        
        if (beforeTime == null) {
            throw new ServiceException("时间阈值不能为空");
        }
        
        // TODO: 在删除前可以考虑备份重要告警数据
        
        int result = analysisAlarmMapper.deleteBeforeTime(beforeTime);
        log.info("历史告警清理完成，清理数量: {}", result);
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processAlarm(String alarmId) {
        log.info("处理告警，告警ID: {}", alarmId);
        return updateAlarmStatus(alarmId, AlarmStatus.RESOLVED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean ignoreAlarm(String alarmId) {
        log.info("忽略告警，告警ID: {}", alarmId);
        return updateAlarmStatus(alarmId, AlarmStatus.IGNORED);
    }

    @Override
    public void sendAlarmNotification(AnalysisAlarm alarm) {
        try {
            // TODO: 集成WebSocket服务发送实时通知
            // TODO: 集成邮件服务发送邮件通知
            // TODO: 集成短信服务发送短信通知
            
            log.info("发送告警通知，告警ID: {}, 类型: {}, 设备: {}, 通道: {}", 
                    alarm.getId(), alarm.getAnalysisType(), alarm.getDeviceId(), alarm.getChannelId());
                    
        } catch (Exception e) {
            log.error("发送告警通知失败，告警ID: {}", alarm.getId(), e);
        }
    }

    @Override
    public String getSnapshotPath(String alarmId) {
        if (StringUtils.isEmpty(alarmId)) {
            return null;
        }
        
        AnalysisAlarm alarm = analysisAlarmMapper.selectById(alarmId);
        return alarm != null ? alarm.getSnapshotPath() : null;
    }

    @Override
    public boolean isSnapshotExists(String alarmId) {
        String snapshotPath = getSnapshotPath(alarmId);
        
        if (StringUtils.isEmpty(snapshotPath)) {
            return false;
        }
        
        try {
            Path path = Paths.get(snapshotPath);
            return Files.exists(path) && Files.isRegularFile(path);
        } catch (Exception e) {
            log.warn("检查快照文件存在性失败: {}", snapshotPath, e);
            return false;
        }
    }

    /**
     * 验证分析告警数据
     */
    private void validateAlarm(AnalysisAlarm alarm, boolean isCreate) {
        if (alarm == null) {
            throw new ServiceException("分析告警信息不能为空");
        }
        
        if (StringUtils.isEmpty(alarm.getTaskId())) {
            throw new ServiceException("任务ID不能为空");
        }
        
        if (StringUtils.isEmpty(alarm.getDeviceId())) {
            throw new ServiceException("设备ID不能为空");
        }
        
        if (StringUtils.isEmpty(alarm.getChannelId())) {
            throw new ServiceException("通道ID不能为空");
        }
        
        if (StringUtils.isEmpty(alarm.getDescription())) {
            throw new ServiceException("告警描述不能为空");
        }
        
        if (alarm.getDescription().length() > 1000) {
            throw new ServiceException("告警描述长度不能超过1000个字符");
        }
    }
}