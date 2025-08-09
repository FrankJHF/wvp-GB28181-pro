package com.genersoft.iot.vmp.storager.dao;

import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 分析任务数据访问层集成测试
 * @author Claude
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("分析任务Mapper集成测试")
class AnalysisTaskMapperTest {

    @Resource
    private AnalysisTaskMapper analysisTaskMapper;

    private AnalysisTask testTask;

    @BeforeEach
    void setUp() {
        testTask = createTestTask();
    }

    private AnalysisTask createTestTask() {
        AnalysisTask task = new AnalysisTask();
        task.setId("test-task-001");
        task.setTaskName("火灾检测任务");
        task.setAnalysisCardId("card-001");
        task.setDeviceId("34020000001320000001");
        task.setDeviceName("前门摄像头");
        task.setChannelId("34020000001310000001");
        task.setChannelName("主通道");
        task.setRtspUrl("rtsp://admin:password@192.168.1.100:554/stream1");
        task.setStatus(TaskStatus.CREATED);
        task.setVlmJobId("vlm-job-001");
        task.setCreatedBy("admin");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setLastActiveTime(LocalDateTime.now().minusMinutes(5));
        task.setLastStatusSync(LocalDateTime.now().minusMinutes(2));

        // 设置任务配置
        Map<String, Object> config = new HashMap<>();
        config.put("inference_interval", 5);
        config.put("sampling_fps", 5);
        config.put("frame_buffer_size", 180);
        config.put("max_new_tokens", 180);
        task.setConfig(config);

        return task;
    }

    @Test
    @DisplayName("测试插入分析任务")
    void testInsert() {
        // Act
        int result = analysisTaskMapper.insert(testTask);

        // Assert
        assertEquals(1, result);

        // 验证插入的数据
        AnalysisTask inserted = analysisTaskMapper.selectById(testTask.getId());
        assertNotNull(inserted);
        assertEquals(testTask.getId(), inserted.getId());
        assertEquals(testTask.getTaskName(), inserted.getTaskName());
        assertEquals(testTask.getAnalysisCardId(), inserted.getAnalysisCardId());
        assertEquals(testTask.getDeviceId(), inserted.getDeviceId());
        assertEquals(testTask.getChannelId(), inserted.getChannelId());
        assertEquals(testTask.getRtspUrl(), inserted.getRtspUrl());
        assertEquals(testTask.getStatus(), inserted.getStatus());
        assertEquals(testTask.getVlmJobId(), inserted.getVlmJobId());
        assertEquals(testTask.getCreatedBy(), inserted.getCreatedBy());

        // 验证JSON配置字段
        assertNotNull(inserted.getConfig());
        assertTrue(inserted.getConfig() instanceof Map);
        Map<String, Object> config = (Map<String, Object>) inserted.getConfig();
        assertEquals(5, config.get("inference_interval"));
        assertEquals(180, config.get("frame_buffer_size"));
    }

    @Test
    @DisplayName("测试根据ID查询分析任务")
    void testSelectById() {
        // Arrange
        analysisTaskMapper.insert(testTask);

        // Act
        AnalysisTask found = analysisTaskMapper.selectById(testTask.getId());

        // Assert
        assertNotNull(found);
        assertEquals(testTask.getId(), found.getId());
        assertEquals(testTask.getTaskName(), found.getTaskName());
        assertEquals(testTask.getStatus(), found.getStatus());
    }

    @Test
    @DisplayName("测试更新分析任务")
    void testUpdate() {
        // Arrange
        analysisTaskMapper.insert(testTask);

        // 修改数据
        testTask.setTaskName("更新后的任务名称");
        testTask.setStatus(TaskStatus.RUNNING);
        testTask.setErrorMessage("测试错误消息");
        testTask.setLastActiveTime(LocalDateTime.now());
        testTask.setLastStatusSync(LocalDateTime.now());

        Map<String, Object> newConfig = new HashMap<>();
        newConfig.put("inference_interval", 10);
        newConfig.put("sampling_fps", 10);
        testTask.setConfig(newConfig);

        // Act
        int result = analysisTaskMapper.update(testTask);

        // Assert
        assertEquals(1, result);

        // 验证更新结果
        AnalysisTask updated = analysisTaskMapper.selectById(testTask.getId());
        assertNotNull(updated);
        assertEquals("更新后的任务名称", updated.getTaskName());
        assertEquals(TaskStatus.RUNNING, updated.getStatus());
        assertEquals("测试错误消息", updated.getErrorMessage());
        
        Map<String, Object> updatedConfig = (Map<String, Object>) updated.getConfig();
        assertEquals(10, updatedConfig.get("inference_interval"));
        assertEquals(10, updatedConfig.get("sampling_fps"));
    }

    @Test
    @DisplayName("测试删除分析任务")
    void testDelete() {
        // Arrange
        analysisTaskMapper.insert(testTask);
        assertNotNull(analysisTaskMapper.selectById(testTask.getId()));

        // Act
        int result = analysisTaskMapper.delete(testTask.getId());

        // Assert
        assertEquals(1, result);
        assertNull(analysisTaskMapper.selectById(testTask.getId()));
    }

    @Test
    @DisplayName("测试按状态查询任务")
    void testSelectByStatus() {
        // Arrange
        analysisTaskMapper.insert(testTask);

        AnalysisTask runningTask = createTestTask();
        runningTask.setId("test-task-002");
        runningTask.setStatus(TaskStatus.RUNNING);
        analysisTaskMapper.insert(runningTask);

        AnalysisTask stoppedTask = createTestTask();
        stoppedTask.setId("test-task-003");
        stoppedTask.setStatus(TaskStatus.STOPPED);
        analysisTaskMapper.insert(stoppedTask);

        // Act
        List<AnalysisTask> createdTasks = analysisTaskMapper.selectByStatus(TaskStatus.CREATED);
        List<AnalysisTask> runningTasks = analysisTaskMapper.selectByStatus(TaskStatus.RUNNING);
        List<AnalysisTask> stoppedTasks = analysisTaskMapper.selectByStatus(TaskStatus.STOPPED);

        // Assert
        assertEquals(1, createdTasks.size());
        assertEquals(testTask.getId(), createdTasks.get(0).getId());

        assertEquals(1, runningTasks.size());
        assertEquals(runningTask.getId(), runningTasks.get(0).getId());

        assertEquals(1, stoppedTasks.size());
        assertEquals(stoppedTask.getId(), stoppedTasks.get(0).getId());
    }

    @Test
    @DisplayName("测试按设备和通道查询任务")
    void testSelectByDeviceAndChannel() {
        // Arrange
        analysisTaskMapper.insert(testTask);

        AnalysisTask otherDeviceTask = createTestTask();
        otherDeviceTask.setId("test-task-002");
        otherDeviceTask.setDeviceId("34020000001320000002");
        analysisTaskMapper.insert(otherDeviceTask);

        AnalysisTask otherChannelTask = createTestTask();
        otherChannelTask.setId("test-task-003");
        otherChannelTask.setChannelId("34020000001310000002");
        analysisTaskMapper.insert(otherChannelTask);

        // Act
        List<AnalysisTask> deviceTasks = analysisTaskMapper.selectByDeviceId(testTask.getDeviceId());
        List<AnalysisTask> channelTasks = analysisTaskMapper.selectByChannelId(testTask.getChannelId());
        List<AnalysisTask> deviceChannelTasks = analysisTaskMapper.selectByDeviceAndChannel(
                testTask.getDeviceId(), testTask.getChannelId());

        // Assert
        assertEquals(2, deviceTasks.size()); // testTask 和 otherChannelTask
        assertEquals(2, channelTasks.size()); // testTask 和 otherDeviceTask
        assertEquals(1, deviceChannelTasks.size()); // 只有 testTask
        assertEquals(testTask.getId(), deviceChannelTasks.get(0).getId());
    }

    @Test
    @DisplayName("测试查询活跃任务")
    void testSelectActiveTasks() {
        // Arrange
        analysisTaskMapper.insert(testTask);

        AnalysisTask runningTask = createTestTask();
        runningTask.setId("test-task-002");
        runningTask.setStatus(TaskStatus.RUNNING);
        analysisTaskMapper.insert(runningTask);

        AnalysisTask pausedTask = createTestTask();
        pausedTask.setId("test-task-003");
        pausedTask.setStatus(TaskStatus.PAUSED);
        analysisTaskMapper.insert(pausedTask);

        AnalysisTask stoppedTask = createTestTask();
        stoppedTask.setId("test-task-004");
        stoppedTask.setStatus(TaskStatus.STOPPED);
        analysisTaskMapper.insert(stoppedTask);

        // Act
        List<AnalysisTask> activeTasks = analysisTaskMapper.selectActiveTasks();

        // Assert
        assertEquals(1, activeTasks.size()); // 只有 RUNNING 状态的任务
        assertEquals(runningTask.getId(), activeTasks.get(0).getId());
    }

    @Test
    @DisplayName("测试查询需要同步状态的任务")
    void testSelectTasksNeedingStatusSync() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // 创建需要同步的任务（超过5分钟未同步）
        AnalysisTask needsSyncTask = createTestTask();
        needsSyncTask.setId("needs-sync-001");
        needsSyncTask.setStatus(TaskStatus.RUNNING);
        needsSyncTask.setLastStatusSync(now.minusMinutes(10));
        analysisTaskMapper.insert(needsSyncTask);

        // 创建不需要同步的任务（最近同步过）
        AnalysisTask recentSyncTask = createTestTask();
        recentSyncTask.setId("recent-sync-001");
        recentSyncTask.setStatus(TaskStatus.RUNNING);
        recentSyncTask.setLastStatusSync(now.minusMinutes(2));
        analysisTaskMapper.insert(recentSyncTask);

        // 创建已终止的任务
        AnalysisTask terminatedTask = createTestTask();
        terminatedTask.setId("terminated-001");
        terminatedTask.setStatus(TaskStatus.STOPPED);
        terminatedTask.setLastStatusSync(now.minusMinutes(10));
        analysisTaskMapper.insert(terminatedTask);

        // Act
        List<AnalysisTask> tasksNeedingSync = analysisTaskMapper.selectTasksNeedingStatusSync(5);

        // Assert
        assertEquals(1, tasksNeedingSync.size());
        assertEquals(needsSyncTask.getId(), tasksNeedingSync.get(0).getId());
    }

    @Test
    @DisplayName("测试批量更新任务状态")
    void testBatchUpdateStatus() {
        // Arrange
        analysisTaskMapper.insert(testTask);

        AnalysisTask task2 = createTestTask();
        task2.setId("test-task-002");
        task2.setStatus(TaskStatus.CREATED);
        analysisTaskMapper.insert(task2);

        AnalysisTask task3 = createTestTask();
        task3.setId("test-task-003");
        task3.setStatus(TaskStatus.PAUSED);
        analysisTaskMapper.insert(task3);

        List<String> taskIds = Arrays.asList(testTask.getId(), task2.getId());

        // Act
        int result = analysisTaskMapper.batchUpdateStatus(taskIds, TaskStatus.RUNNING);

        // Assert
        assertEquals(2, result);

        // 验证更新结果
        AnalysisTask updated1 = analysisTaskMapper.selectById(testTask.getId());
        AnalysisTask updated2 = analysisTaskMapper.selectById(task2.getId());
        AnalysisTask unchanged = analysisTaskMapper.selectById(task3.getId());

        assertEquals(TaskStatus.RUNNING, updated1.getStatus());
        assertEquals(TaskStatus.RUNNING, updated2.getStatus());
        assertEquals(TaskStatus.PAUSED, unchanged.getStatus()); // 未包含在更新列表中
    }

    @Test
    @DisplayName("测试统计任务数量")
    void testCountTasks() {
        // Arrange
        analysisTaskMapper.insert(testTask);

        AnalysisTask runningTask = createTestTask();
        runningTask.setId("test-task-002");
        runningTask.setStatus(TaskStatus.RUNNING);
        runningTask.setCreatedBy("user1");
        analysisTaskMapper.insert(runningTask);

        // Act & Assert
        long totalCount = analysisTaskMapper.countTasks(null, null);
        assertEquals(2, totalCount);

        long createdCount = analysisTaskMapper.countTasks(TaskStatus.CREATED, null);
        assertEquals(1, createdCount);

        long runningCount = analysisTaskMapper.countTasks(TaskStatus.RUNNING, null);
        assertEquals(1, runningCount);

        long adminCount = analysisTaskMapper.countTasks(null, "admin");
        assertEquals(1, adminCount);

        long userCount = analysisTaskMapper.countTasks(null, "user1");
        assertEquals(1, userCount);
    }

    @Test
    @DisplayName("测试按分析卡片查询任务")
    void testSelectByAnalysisCardId() {
        // Arrange
        analysisTaskMapper.insert(testTask);

        AnalysisTask otherCardTask = createTestTask();
        otherCardTask.setId("test-task-002");
        otherCardTask.setAnalysisCardId("card-002");
        analysisTaskMapper.insert(otherCardTask);

        // Act
        List<AnalysisTask> cardTasks = analysisTaskMapper.selectByAnalysisCardId(testTask.getAnalysisCardId());

        // Assert
        assertEquals(1, cardTasks.size());
        assertEquals(testTask.getId(), cardTasks.get(0).getId());
        assertEquals(testTask.getAnalysisCardId(), cardTasks.get(0).getAnalysisCardId());
    }

    @Test
    @DisplayName("测试复杂条件查询")
    void testComplexQuery() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // 创建多个不同状态和创建者的任务
        AnalysisTask task1 = createTestTask();
        task1.setId("task-001");
        task1.setStatus(TaskStatus.RUNNING);
        task1.setCreatedBy("admin");
        task1.setCreatedAt(now.minusDays(1));
        analysisTaskMapper.insert(task1);

        AnalysisTask task2 = createTestTask();
        task2.setId("task-002");
        task2.setStatus(TaskStatus.PAUSED);
        task2.setCreatedBy("admin");
        task2.setCreatedAt(now.minusDays(2));
        analysisTaskMapper.insert(task2);

        AnalysisTask task3 = createTestTask();
        task3.setId("task-003");
        task3.setStatus(TaskStatus.RUNNING);
        task3.setCreatedBy("user1");
        task3.setCreatedAt(now.minusDays(1));
        analysisTaskMapper.insert(task3);

        // Act - 查询admin创建的运行中任务
        List<AnalysisTask> adminRunningTasks = analysisTaskMapper.selectByStatusAndCreatedBy(
                TaskStatus.RUNNING, "admin");

        // Assert
        assertEquals(1, adminRunningTasks.size());
        assertEquals(task1.getId(), adminRunningTasks.get(0).getId());
    }

    @Test
    @DisplayName("测试NULL和空值处理")
    void testNullAndEmptyHandling() {
        // Arrange
        AnalysisTask minimalTask = new AnalysisTask();
        minimalTask.setId("minimal-001");
        minimalTask.setTaskName("最小任务");
        minimalTask.setAnalysisCardId("card-001");
        minimalTask.setDeviceId("device-001");
        minimalTask.setChannelId("channel-001");
        minimalTask.setRtspUrl("rtsp://test");
        minimalTask.setStatus(TaskStatus.CREATED);
        minimalTask.setCreatedBy("admin");
        minimalTask.setCreatedAt(LocalDateTime.now());
        minimalTask.setUpdatedAt(LocalDateTime.now());
        
        // 设置null值
        minimalTask.setVlmJobId(null);
        minimalTask.setConfig(null);
        minimalTask.setErrorMessage(null);
        minimalTask.setLastActiveTime(null);
        minimalTask.setLastStatusSync(null);

        // Act
        int result = analysisTaskMapper.insert(minimalTask);

        // Assert
        assertEquals(1, result);
        
        AnalysisTask retrieved = analysisTaskMapper.selectById(minimalTask.getId());
        assertNotNull(retrieved);
        assertEquals("最小任务", retrieved.getTaskName());
        assertNull(retrieved.getVlmJobId());
        assertNull(retrieved.getConfig());
        assertNull(retrieved.getErrorMessage());
        assertNull(retrieved.getLastActiveTime());
        assertNull(retrieved.getLastStatusSync());
    }
}