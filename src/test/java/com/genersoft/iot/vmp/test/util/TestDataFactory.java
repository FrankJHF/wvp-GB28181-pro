package com.genersoft.iot.vmp.test.util;

import com.genersoft.iot.vmp.analysis.bean.AnalysisCard;
import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.AnalysisAlarm;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobRequest;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMAnalysisResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试数据工厂
 * 提供测试用的标准数据对象
 * 
 * @author Claude
 */
public class TestDataFactory {

    /**
     * 创建测试用分析卡片
     */
    public static AnalysisCard createTestAnalysisCard() {
        AnalysisCard card = new AnalysisCard();
        card.setId("test-card-001");
        card.setTitle("测试火灾检测卡片");
        card.setDescription("用于单元测试的火灾检测分析卡片");
        card.setIcon("fire");
        card.setEnabled(true);
        card.setPrompt("请分析视频中是否发生火灾或其他紧急情况");
        card.setModelType("videollama3-fire-detection");
        
        // 设置标签
        List<String> tags = Arrays.asList("火灾检测", "紧急事件", "测试");
        card.setTags(tags);
        
        // 设置分析配置
        Map<String, Object> config = new HashMap<>();
        config.put("inference_interval", 5);
        config.put("sampling_fps", 5);
        config.put("frame_buffer_size", 180);
        config.put("max_new_tokens", 200);
        card.setAnalysisConfig(config);
        
        card.setCreatedBy("test-user");
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        
        return card;
    }
    
    /**
     * 创建测试用分析任务
     */
    public static AnalysisTask createTestAnalysisTask() {
        AnalysisTask task = new AnalysisTask();
        task.setId("test-task-001");
        task.setTaskName("测试火灾检测任务");
        task.setAnalysisCardId("test-card-001");
        task.setDeviceId("34020000001320000001");
        task.setDeviceName("测试摄像头");
        task.setChannelId("34020000001310000001");
        task.setChannelName("测试通道");
        task.setRtspUrl("rtsp://test:password@192.168.1.100:554/stream1");
        task.setStatus(TaskStatus.CREATED);
        task.setVlmJobId("test-vlm-job-001");
        task.setCreatedBy("test-user");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        return task;
    }
    
    /**
     * 创建测试用分析告警
     */
    public static AnalysisAlarm createTestAnalysisAlarm() {
        AnalysisAlarm alarm = new AnalysisAlarm();
        alarm.setId("test-alarm-001");
        alarm.setTaskId("test-task-001");
        alarm.setDeviceId("34020000001320000001");
        alarm.setChannelId("34020000001310000001");
        alarm.setEventDescription("检测到火灾事件");
        alarm.setEmergencyExist(true);
        alarm.setSnapshotBase64("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ...");
        alarm.setEventStartTime(LocalDateTime.now().minusMinutes(5));
        alarm.setEventEndTime(LocalDateTime.now());
        alarm.setProcessed(false);
        alarm.setCreatedAt(LocalDateTime.now());
        alarm.setUpdatedAt(LocalDateTime.now());
        
        return alarm;
    }
    
    /**
     * 创建测试用VLM作业请求
     */
    public static VLMJobRequest createTestVLMJobRequest() {
        VLMJobRequest request = new VLMJobRequest();
        request.setDeviceId("34020000001320000001");
        request.setChannelId("34020000001310000001");
        request.setInputType("rtsp_stream");
        request.setInputData("rtsp://test:password@192.168.1.100:554/stream1");
        request.setCallbackUrl("http://localhost:18080/api/vlm/callback");
        request.setAnalysisPrompt("请分析视频中是否发生火灾或其他突发情况");
        request.setModelName("videollama3-fire-detection");
        request.setAutoStart(false);
        
        Map<String, Object> config = new HashMap<>();
        config.put("inference_interval", 5);
        config.put("sampling_fps", 5);
        request.setAnalysisConfig(config);
        
        return request;
    }
    
    /**
     * 创建测试用VLM作业响应
     */
    public static VLMJobResponse createTestVLMJobResponse() {
        VLMJobResponse response = new VLMJobResponse();
        response.setJobId("test-vlm-job-001");
        response.setStatus("created");
        response.setMessage("Job created successfully");
        response.setErrorCode("SUCCESS");
        
        return response;
    }
    
    /**
     * 创建测试用VLM分析结果
     */
    public static VLMAnalysisResult createTestVLMAnalysisResult() {
        VLMAnalysisResult result = new VLMAnalysisResult();
        result.setJobId("test-vlm-job-001");
        result.setDeviceId("34020000001320000001");
        result.setChannelId("34020000001310000001");
        result.setAnalysisTimestamp("2024-01-15T10:30:45.123Z");
        
        // 创建事件
        VLMAnalysisResult.Event event = new VLMAnalysisResult.Event();
        event.setEventStartPts(120.5);
        event.setEventEndPts(135.8);
        event.setEventStartUtc("2024-01-15T10:30:00.000Z");
        event.setEventEndUtc("2024-01-15T10:30:15.000Z");
        event.setEventTimeRange("0.0-15.3");
        event.setEventDescription("检测到明显的火焰和烟雾，疑似火灾事件");
        event.setEmergencyExist(true);
        event.setSnapshotBase64("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ...");
        
        result.setEvents(Arrays.asList(event));
        
        return result;
    }
    
    /**
     * 创建批量测试数据 - 卡片
     */
    public static List<AnalysisCard> createTestAnalysisCards(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> {
                AnalysisCard card = createTestAnalysisCard();
                card.setId("test-card-" + String.format("%03d", i));
                card.setTitle("测试卡片-" + i);
                return card;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 创建批量测试数据 - 任务
     */
    public static List<AnalysisTask> createTestAnalysisTasks(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> {
                AnalysisTask task = createTestAnalysisTask();
                task.setId("test-task-" + String.format("%03d", i));
                task.setTaskName("测试任务-" + i);
                return task;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 创建批量测试数据 - 告警
     */
    public static List<AnalysisAlarm> createTestAnalysisAlarms(int count) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> {
                AnalysisAlarm alarm = createTestAnalysisAlarm();
                alarm.setId("test-alarm-" + String.format("%03d", i));
                alarm.setEventDescription("测试告警-" + i);
                return alarm;
            })
            .collect(java.util.stream.Collectors.toList());
    }
}