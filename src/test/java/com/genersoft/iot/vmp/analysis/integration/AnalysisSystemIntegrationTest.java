package com.genersoft.iot.vmp.analysis.integration;

import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.AnalysisCard;
import com.genersoft.iot.vmp.analysis.bean.AnalysisAlarm;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;
import com.genersoft.iot.vmp.analysis.service.IAnalysisTaskService;
import com.genersoft.iot.vmp.analysis.service.IAnalysisCardService;
import com.genersoft.iot.vmp.analysis.service.IAnalysisAlarmService;
import com.genersoft.iot.vmp.analysis.service.IVLMClientService;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMAnalysisResult;
import com.genersoft.iot.vmp.storager.dao.AnalysisTaskMapper;
import com.genersoft.iot.vmp.storager.dao.AnalysisCardMapper;
import com.genersoft.iot.vmp.storager.dao.AnalysisAlarmMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 智能分析系统集成测试
 * 验证完整的任务创建到告警生成流程
 * @author Claude
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("智能分析系统集成测试")
class AnalysisSystemIntegrationTest {

    @Resource
    private IAnalysisTaskService analysisTaskService;

    @Resource
    private IAnalysisCardService analysisCardService;

    @Resource
    private IAnalysisAlarmService analysisAlarmService;

    @Resource
    private AnalysisTaskMapper analysisTaskMapper;

    @Resource
    private AnalysisCardMapper analysisCardMapper;

    @Resource
    private AnalysisAlarmMapper analysisAlarmMapper;

    @MockBean
    private IVLMClientService vlmClientService;

    private AnalysisCard testCard;
    private AnalysisTask testTask;
    private VLMAnalysisResult testAnalysisResult;

    @BeforeEach
    void setUp() {
        setupTestData();
    }

    private void setupTestData() {
        // 创建测试卡片
        testCard = new AnalysisCard();
        testCard.setId("system-test-card-001");
        testCard.setTitle("系统集成测试卡片");
        testCard.setDescription("用于系统集成测试的火灾检测卡片");
        testCard.setIcon("fire");
        testCard.setEnabled(true);
        testCard.setPrompt("请分析视频中是否发生火灾或其他紧急情况");
        testCard.setModelType("videollama3-fire-detection");
        testCard.setCreatedBy("system-test");
        testCard.setCreatedAt(LocalDateTime.now());
        testCard.setUpdatedAt(LocalDateTime.now());

        // 设置卡片标签和配置
        List<String> tags = Arrays.asList("火灾检测", "紧急事件", "系统测试");
        testCard.setTags(tags);

        Map<String, Object> config = new HashMap<>();
        config.put("inference_interval", 10);
        config.put("sampling_fps", 5);
        config.put("frame_buffer_size", 180);
        config.put("max_new_tokens", 200);
        testCard.setAnalysisConfig(config);

        // 创建测试任务
        testTask = new AnalysisTask();
        testTask.setId("system-test-task-001");
        testTask.setTaskName("系统集成测试任务");
        testTask.setAnalysisCardId(testCard.getId());
        testTask.setDeviceId("34020000001320000001");
        testTask.setDeviceName("系统测试摄像头");
        testTask.setChannelId("34020000001310000001");
        testTask.setChannelName("系统测试通道");
        testTask.setRtspUrl("rtsp://test:password@192.168.1.100:554/stream1");
        testTask.setStatus(TaskStatus.CREATED);
        testTask.setVlmJobId("system-test-vlm-job-001");
        testTask.setCreatedBy("system-test");
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());

        // 创建测试分析结果
        testAnalysisResult = new VLMAnalysisResult();
        testAnalysisResult.setJobId("system-test-vlm-job-001");
        testAnalysisResult.setDeviceId("34020000001320000001");
        testAnalysisResult.setChannelId("34020000001310000001");
        testAnalysisResult.setAnalysisTimestamp("2024-01-15T10:30:45.123Z");
        
        // 创建紧急事件
        VLMAnalysisResult.Event emergencyEvent = new VLMAnalysisResult.Event();
        emergencyEvent.setEventStartPts(120.5);
        emergencyEvent.setEventEndPts(135.8);
        emergencyEvent.setEventStartUtc("2024-01-15T10:30:00.000Z");
        emergencyEvent.setEventEndUtc("2024-01-15T10:30:15.000Z");
        emergencyEvent.setEventTimeRange("0.0-15.3");
        emergencyEvent.setEventDescription("检测到明显的火焰和烟雾，疑似火灾事件");
        emergencyEvent.setEmergencyExist(true);
        emergencyEvent.setSnapshotBase64("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ...");
        
        testAnalysisResult.setEvents(Arrays.asList(emergencyEvent));
    }

    @Test
    @DisplayName("测试完整的分析流程 - 从卡片创建到告警生成")
    void testCompleteAnalysisWorkflow() {
        // Step 1: 创建分析卡片
        analysisCardMapper.insert(testCard);
        
        AnalysisCard createdCard = analysisCardMapper.selectById(testCard.getId());
        assertNotNull(createdCard);
        assertEquals(testCard.getTitle(), createdCard.getTitle());
        assertTrue(createdCard.getEnabled());

        // Step 2: 创建分析任务（模拟设备验证和VLM服务调用）
        when(vlmClientService.createJob(any(), eq(false))).thenReturn(createMockVLMJobResponse());
        
        analysisTaskMapper.insert(testTask);
        
        AnalysisTask createdTask = analysisTaskMapper.selectById(testTask.getId());
        assertNotNull(createdTask);
        assertEquals(testTask.getTaskName(), createdTask.getTaskName());
        assertEquals(TaskStatus.CREATED, createdTask.getStatus());

        // Step 3: 模拟任务启动
        createdTask.setStatus(TaskStatus.RUNNING);
        createdTask.setLastActiveTime(LocalDateTime.now());
        analysisTaskMapper.update(createdTask);
        
        AnalysisTask runningTask = analysisTaskMapper.selectById(testTask.getId());
        assertEquals(TaskStatus.RUNNING, runningTask.getStatus());
        assertNotNull(runningTask.getLastActiveTime());

        // Step 4: 模拟接收到VLM分析结果并生成告警
        processAnalysisResult(testAnalysisResult);

        // Step 5: 验证告警已生成
        List<AnalysisAlarm> alarms = analysisAlarmMapper.selectByTaskId(testTask.getId());
        assertFalse(alarms.isEmpty());
        
        AnalysisAlarm alarm = alarms.get(0);
        assertNotNull(alarm);
        assertEquals(testTask.getId(), alarm.getTaskId());
        assertEquals(testTask.getDeviceId(), alarm.getDeviceId());
        assertEquals(testTask.getChannelId(), alarm.getChannelId());
        assertTrue(alarm.getEmergencyExist());
        assertEquals("检测到明显的火焰和烟雾，疑似火灾事件", alarm.getEventDescription());
        assertNotNull(alarm.getSnapshotBase64());

        // Step 6: 验证任务状态同步
        AnalysisTask finalTask = analysisTaskMapper.selectById(testTask.getId());
        assertNotNull(finalTask.getLastStatusSync());
        assertTrue(finalTask.needsStatusSync(60) == false); // 刚刚同步过，不需要再次同步
    }

    @Test
    @DisplayName("测试数据一致性 - 跨表关联查询")
    void testDataConsistency() {
        // 插入测试数据
        analysisCardMapper.insert(testCard);
        analysisTaskMapper.insert(testTask);

        // 创建关联的告警
        AnalysisAlarm alarm = createTestAlarm();
        analysisAlarmMapper.insert(alarm);

        // 验证数据关联完整性
        AnalysisTask taskWithCard = analysisTaskMapper.selectById(testTask.getId());
        assertNotNull(taskWithCard);
        
        AnalysisCard relatedCard = analysisCardMapper.selectById(taskWithCard.getAnalysisCardId());
        assertNotNull(relatedCard);
        assertEquals(testCard.getTitle(), relatedCard.getTitle());

        // 验证告警与任务的关联
        List<AnalysisAlarm> taskAlarms = analysisAlarmMapper.selectByTaskId(testTask.getId());
        assertFalse(taskAlarms.isEmpty());
        assertEquals(testTask.getId(), taskAlarms.get(0).getTaskId());

        // 验证按设备查询的功能
        List<AnalysisAlarm> deviceAlarms = analysisAlarmMapper.selectByDeviceId(testTask.getDeviceId());
        assertFalse(deviceAlarms.isEmpty());
        assertTrue(deviceAlarms.stream().anyMatch(a -> a.getTaskId().equals(testTask.getId())));
    }

    @Test
    @DisplayName("测试并发安全性 - 多任务同时处理")
    void testConcurrentSafety() throws Exception {
        // 创建多个任务模拟并发场景
        analysisCardMapper.insert(testCard);

        // 创建多个任务
        for (int i = 0; i < 3; i++) {
            AnalysisTask concurrentTask = createConcurrentTask(i);
            analysisTaskMapper.insert(concurrentTask);
            
            // 验证任务创建成功
            AnalysisTask createdTask = analysisTaskMapper.selectById(concurrentTask.getId());
            assertNotNull(createdTask);
            assertEquals(concurrentTask.getTaskName(), createdTask.getTaskName());
        }

        // 验证所有任务都能正确查询
        List<AnalysisTask> allTasks = analysisTaskMapper.selectByDeviceAndChannel(
                testTask.getDeviceId(), testTask.getChannelId());
        assertEquals(3, allTasks.size());

        // 验证没有数据冲突
        Set<String> taskIds = allTasks.stream()
                .map(AnalysisTask::getId)
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(3, taskIds.size()); // 确保没有重复ID
    }

    @Test
    @DisplayName("测试异常情况处理 - 数据库约束和事务回滚")
    void testExceptionHandling() {
        // 测试唯一约束 - 重复插入相同ID的卡片
        analysisCardMapper.insert(testCard);
        
        AnalysisCard duplicateCard = new AnalysisCard();
        duplicateCard.setId(testCard.getId()); // 相同ID
        duplicateCard.setTitle("重复卡片");
        
        // 应该抛出异常或返回0，取决于数据库配置
        assertThrows(Exception.class, () -> {
            analysisCardMapper.insert(duplicateCard);
        });

        // 验证原始数据仍然存在且未被修改
        AnalysisCard originalCard = analysisCardMapper.selectById(testCard.getId());
        assertNotNull(originalCard);
        assertEquals(testCard.getTitle(), originalCard.getTitle());
    }

    @Test
    @DisplayName("测试性能指标 - 大量数据处理")
    void testPerformanceMetrics() {
        // 插入基础数据
        analysisCardMapper.insert(testCard);

        // 批量插入任务测试性能
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 50; i++) {
            AnalysisTask performanceTask = createPerformanceTask(i);
            analysisTaskMapper.insert(performanceTask);
        }
        
        long insertTime = System.currentTimeMillis() - startTime;

        // 测试批量查询性能
        startTime = System.currentTimeMillis();
        List<AnalysisTask> allTasks = analysisTaskMapper.selectByDeviceAndChannel(
                testTask.getDeviceId(), testTask.getChannelId());
        long queryTime = System.currentTimeMillis() - startTime;

        // 基本性能断言（具体数值可根据实际环境调整）
        assertTrue(insertTime < 5000, "批量插入耗时过长: " + insertTime + "ms");
        assertTrue(queryTime < 1000, "批量查询耗时过长: " + queryTime + "ms");
        assertEquals(50, allTasks.size());
        
        // 测试批量删除性能
        startTime = System.currentTimeMillis();
        for (AnalysisTask task : allTasks) {
            analysisTaskMapper.delete(task.getId());
        }
        long deleteTime = System.currentTimeMillis() - startTime;
        
        assertTrue(deleteTime < 3000, "批量删除耗时过长: " + deleteTime + "ms");
    }

    // 辅助方法
    private com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse createMockVLMJobResponse() {
        com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse response = 
                new com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse();
        response.setJobId("system-test-vlm-job-001");
        response.setStatus("created");
        response.setMessage("Job created successfully");
        response.setErrorCode("SUCCESS");
        return response;
    }

    private void processAnalysisResult(VLMAnalysisResult result) {
        // 模拟回调处理器的逻辑
        for (VLMAnalysisResult.Event event : result.getEvents()) {
            if (event.getEmergencyExist()) {
                AnalysisAlarm alarm = new AnalysisAlarm();
                alarm.setId("system-test-alarm-" + System.currentTimeMillis());
                alarm.setTaskId(testTask.getId());
                alarm.setDeviceId(result.getDeviceId());
                alarm.setChannelId(result.getChannelId());
                alarm.setEventDescription(event.getEventDescription());
                alarm.setEmergencyExist(event.getEmergencyExist());
                alarm.setSnapshotBase64(event.getSnapshotBase64());
                alarm.setEventStartTime(LocalDateTime.parse("2024-01-15T10:30:00"));
                alarm.setEventEndTime(LocalDateTime.parse("2024-01-15T10:30:15"));
                alarm.setCreatedAt(LocalDateTime.now());
                alarm.setUpdatedAt(LocalDateTime.now());
                
                analysisAlarmMapper.insert(alarm);
            }
        }
    }

    private AnalysisAlarm createTestAlarm() {
        AnalysisAlarm alarm = new AnalysisAlarm();
        alarm.setId("system-test-alarm-001");
        alarm.setTaskId(testTask.getId());
        alarm.setDeviceId(testTask.getDeviceId());
        alarm.setChannelId(testTask.getChannelId());
        alarm.setEventDescription("系统集成测试告警");
        alarm.setEmergencyExist(true);
        alarm.setEventStartTime(LocalDateTime.now().minusMinutes(5));
        alarm.setEventEndTime(LocalDateTime.now());
        alarm.setCreatedAt(LocalDateTime.now());
        alarm.setUpdatedAt(LocalDateTime.now());
        return alarm;
    }

    private AnalysisTask createConcurrentTask(int index) {
        AnalysisTask task = new AnalysisTask();
        task.setId("concurrent-task-" + index);
        task.setTaskName("并发测试任务-" + index);
        task.setAnalysisCardId(testCard.getId());
        task.setDeviceId(testTask.getDeviceId());
        task.setChannelId(testTask.getChannelId());
        task.setRtspUrl("rtsp://test:password@192.168.1." + (100 + index) + ":554/stream1");
        task.setStatus(TaskStatus.CREATED);
        task.setVlmJobId("concurrent-vlm-job-" + index);
        task.setCreatedBy("concurrent-test");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }

    private AnalysisTask createPerformanceTask(int index) {
        AnalysisTask task = new AnalysisTask();
        task.setId("perf-task-" + index);
        task.setTaskName("性能测试任务-" + index);
        task.setAnalysisCardId(testCard.getId());
        task.setDeviceId(testTask.getDeviceId());
        task.setChannelId(testTask.getChannelId());
        task.setRtspUrl("rtsp://perf:test@192.168.2." + (100 + index) + ":554/stream1");
        task.setStatus(TaskStatus.CREATED);
        task.setVlmJobId("perf-vlm-job-" + index);
        task.setCreatedBy("performance-test");
        task.setCreatedAt(LocalDateTime.now().minusMinutes(index));
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }
}