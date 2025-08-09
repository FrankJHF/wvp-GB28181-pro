package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.service.IAnalysisCardService;
import com.genersoft.iot.vmp.analysis.service.ITaskStateService;
import com.genersoft.iot.vmp.analysis.service.IVLMClientService;
import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.AnalysisCard;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;
import com.genersoft.iot.vmp.storager.dao.AnalysisTaskMapper;
import com.genersoft.iot.vmp.conf.exception.ServiceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 分析任务服务单元测试
 * @author Claude
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("分析任务服务测试")
class AnalysisTaskServiceImplTest {

    @Mock
    private AnalysisTaskMapper analysisTaskMapper;

    @Mock
    private IAnalysisCardService analysisCardService;

    @Mock
    private ITaskStateService taskStateService;

    @Mock
    private IVLMClientService vlmClientService;

    @InjectMocks
    private AnalysisTaskServiceImpl analysisTaskService;

    private AnalysisTask testTask;
    private AnalysisCard testCard;

    @BeforeEach
    void setUp() {
        // 设置配置参数
        ReflectionTestUtils.setField(analysisTaskService, "callbackBaseUrl", "http://localhost:18080");

        // 创建测试数据
        setupTestData();
    }

    private void setupTestData() {
        // 创建测试任务
        testTask = new AnalysisTask();
        testTask.setId("test-task-001");
        testTask.setTaskName("火灾检测任务");
        testTask.setAnalysisCardId("card-001");
        testTask.setDeviceId("34020000001320000001");
        testTask.setDeviceName("前门摄像头");
        testTask.setChannelId("34020000001310000001");
        testTask.setChannelName("主通道");
        testTask.setRtspUrl("rtsp://admin:password@192.168.1.100:554/stream1");
        testTask.setStatus(TaskStatus.CREATED);
        testTask.setCreatedBy("admin");
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());

        // 创建测试卡片
        testCard = new AnalysisCard();
        testCard.setId("card-001");
        testCard.setTitle("火灾检测卡片");
        testCard.setEnabled(true);
        testCard.setPrompt("请分析视频中是否发生火灾");
        testCard.setModelType("videollama3-fire-detection");
        
        Map<String, Object> config = new HashMap<>();
        config.put("inference_interval", 5);
        config.put("sampling_fps", 5);
        testCard.setAnalysisConfig(config);
    }

    @Test
    @DisplayName("测试根据ID查询任务")
    void testGetTaskById() {
        // Arrange
        when(analysisTaskMapper.selectById("test-task-001")).thenReturn(testTask);

        // Act
        AnalysisTask result = analysisTaskService.getTaskById("test-task-001");

        // Assert
        assertNotNull(result);
        assertEquals(testTask.getId(), result.getId());
        assertEquals(testTask.getTaskName(), result.getTaskName());
        
        verify(analysisTaskMapper, times(1)).selectById("test-task-001");
    }

    @Test
    @DisplayName("测试查询不存在的任务")
    void testGetTaskByIdNotFound() {
        // Arrange
        when(analysisTaskMapper.selectById("non-existing")).thenReturn(null);

        // Act
        AnalysisTask result = analysisTaskService.getTaskById("non-existing");

        // Assert
        assertNull(result);
        verify(analysisTaskMapper, times(1)).selectById("non-existing");
    }

    @Test
    @DisplayName("测试更新任务成功")
    void testUpdateTaskSuccess() {
        // Arrange
        when(analysisTaskMapper.selectById("test-task-001")).thenReturn(testTask);
        when(analysisTaskMapper.update(any(AnalysisTask.class))).thenReturn(1);

        AnalysisTask updateRequest = new AnalysisTask();
        updateRequest.setId("test-task-001");
        updateRequest.setTaskName("更新后的任务名称");
        updateRequest.setErrorMessage("测试错误");

        // Act
        AnalysisTask result = analysisTaskService.updateTask(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("更新后的任务名称", result.getTaskName());
        assertEquals("测试错误", result.getErrorMessage());
        assertNotNull(result.getUpdatedAt());

        verify(analysisTaskMapper, times(1)).update(any(AnalysisTask.class));
    }

    @Test
    @DisplayName("测试更新不存在的任务")
    void testUpdateTaskNotFound() {
        // Arrange
        when(analysisTaskMapper.selectById("non-existing")).thenReturn(null);

        AnalysisTask updateRequest = new AnalysisTask();
        updateRequest.setId("non-existing");
        updateRequest.setTaskName("更新后的任务名称");

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            analysisTaskService.updateTask(updateRequest);
        });
        
        assertEquals("任务不存在", exception.getMessage());
        verify(analysisTaskMapper, never()).update(any());
    }

    @Test
    @DisplayName("测试删除任务成功")
    void testDeleteTaskSuccess() {
        // Arrange
        testTask.setStatus(TaskStatus.STOPPED);
        when(analysisTaskMapper.selectById("test-task-001")).thenReturn(testTask);
        when(analysisTaskMapper.delete("test-task-001")).thenReturn(1);

        // Act
        boolean result = analysisTaskService.deleteTask("test-task-001");

        // Assert
        assertTrue(result);
        verify(analysisTaskMapper, times(1)).delete("test-task-001");
    }

    @Test
    @DisplayName("测试删除运行中的任务")
    void testDeleteRunningTask() {
        // Arrange
        testTask.setStatus(TaskStatus.RUNNING);
        when(analysisTaskMapper.selectById("test-task-001")).thenReturn(testTask);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            analysisTaskService.deleteTask("test-task-001");
        });
        
        assertEquals("任务状态不允许删除，请先停止任务", exception.getMessage());
        verify(analysisTaskMapper, never()).delete(anyString());
    }

    @Test
    @DisplayName("测试删除不存在的任务")
    void testDeleteTaskNotFound() {
        // Arrange
        when(analysisTaskMapper.selectById("non-existing")).thenReturn(null);

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            analysisTaskService.deleteTask("non-existing");
        });
        
        assertEquals("任务不存在", exception.getMessage());
        verify(analysisTaskMapper, never()).delete(anyString());
    }

    @Test
    @DisplayName("测试根据设备和通道查询任务")
    void testGetTasksByDeviceAndChannel() {
        // Arrange
        List<AnalysisTask> expectedTasks = Arrays.asList(testTask);
        when(analysisTaskMapper.selectByDeviceAndChannel("34020000001320000001", "34020000001310000001"))
                .thenReturn(expectedTasks);

        // Act
        List<AnalysisTask> result = analysisTaskService.getTasksByDeviceAndChannel(
                "34020000001320000001", "34020000001310000001");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTask.getId(), result.get(0).getId());
        
        verify(analysisTaskMapper, times(1))
                .selectByDeviceAndChannel("34020000001320000001", "34020000001310000001");
    }

    @Test
    @DisplayName("测试检查是否可以创建任务 - 可以创建")
    void testCanCreateTaskSuccess() {
        // Arrange
        when(analysisTaskMapper.selectByDeviceAndChannel("34020000001320000001", "34020000001310000001"))
                .thenReturn(Arrays.asList());

        // Act
        boolean result = analysisTaskService.canCreateTask(
                "34020000001320000001", "34020000001310000001", "card-001");

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("测试检查是否可以创建任务 - 已存在相同任务")
    void testCanCreateTaskAlreadyExists() {
        // Arrange
        AnalysisTask existingTask = new AnalysisTask();
        existingTask.setAnalysisCardId("card-001");
        existingTask.setStatus(TaskStatus.RUNNING);
        
        when(analysisTaskMapper.selectByDeviceAndChannel("34020000001320000001", "34020000001310000001"))
                .thenReturn(Arrays.asList(existingTask));

        // Act
        boolean result = analysisTaskService.canCreateTask(
                "34020000001320000001", "34020000001310000001", "card-001");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("测试批量删除任务")
    void testBatchDeleteTasks() {
        // Arrange
        List<String> taskIds = Arrays.asList("task-001", "task-002", "task-003");
        
        // 模拟任务状态检查
        AnalysisTask task1 = new AnalysisTask();
        task1.setId("task-001");
        task1.setStatus(TaskStatus.STOPPED);
        
        AnalysisTask task2 = new AnalysisTask();
        task2.setId("task-002");
        task2.setStatus(TaskStatus.FAILED);
        
        AnalysisTask task3 = new AnalysisTask();
        task3.setId("task-003");
        task3.setStatus(TaskStatus.CREATED);

        when(analysisTaskMapper.selectById("task-001")).thenReturn(task1);
        when(analysisTaskMapper.selectById("task-002")).thenReturn(task2);
        when(analysisTaskMapper.selectById("task-003")).thenReturn(task3);
        
        when(analysisTaskMapper.delete("task-001")).thenReturn(1);
        when(analysisTaskMapper.delete("task-002")).thenReturn(1);
        when(analysisTaskMapper.delete("task-003")).thenReturn(1);

        // Act
        int result = analysisTaskService.batchDeleteTasks(taskIds);

        // Assert
        assertEquals(3, result);
        verify(analysisTaskMapper, times(3)).delete(anyString());
    }

    @Test
    @DisplayName("测试批量删除包含运行中任务")
    void testBatchDeleteTasksWithRunningTask() {
        // Arrange
        List<String> taskIds = Arrays.asList("task-001", "task-002");
        
        AnalysisTask stoppedTask = new AnalysisTask();
        stoppedTask.setId("task-001");
        stoppedTask.setStatus(TaskStatus.STOPPED);
        
        AnalysisTask runningTask = new AnalysisTask();
        runningTask.setId("task-002");
        runningTask.setStatus(TaskStatus.RUNNING);

        when(analysisTaskMapper.selectById("task-001")).thenReturn(stoppedTask);
        when(analysisTaskMapper.selectById("task-002")).thenReturn(runningTask);
        when(analysisTaskMapper.delete("task-001")).thenReturn(1);

        // Act
        int result = analysisTaskService.batchDeleteTasks(taskIds);

        // Assert - 只删除了一个可以删除的任务
        assertEquals(1, result);
        verify(analysisTaskMapper, times(1)).delete("task-001");
        verify(analysisTaskMapper, never()).delete("task-002");
    }

    @Test
    @DisplayName("测试任务状态验证逻辑")
    void testTaskStatusValidation() {
        // 创建不同状态的任务进行测试
        AnalysisTask createdTask = new AnalysisTask();
        createdTask.setStatus(TaskStatus.CREATED);
        assertTrue(createdTask.canStart());
        assertTrue(createdTask.canDelete());
        assertFalse(createdTask.canPause());
        assertFalse(createdTask.canResume());

        AnalysisTask runningTask = new AnalysisTask();
        runningTask.setStatus(TaskStatus.RUNNING);
        assertFalse(runningTask.canStart());
        assertFalse(runningTask.canDelete());
        assertTrue(runningTask.canPause());
        assertTrue(runningTask.canStop());
        assertTrue(runningTask.isActive());

        AnalysisTask pausedTask = new AnalysisTask();
        pausedTask.setStatus(TaskStatus.PAUSED);
        assertFalse(pausedTask.canStart());
        assertFalse(pausedTask.canDelete());
        assertFalse(pausedTask.canPause());
        assertTrue(pausedTask.canResume());
        assertTrue(pausedTask.canStop());

        AnalysisTask stoppedTask = new AnalysisTask();
        stoppedTask.setStatus(TaskStatus.STOPPED);
        assertTrue(stoppedTask.canStart());
        assertTrue(stoppedTask.canDelete());
        assertFalse(stoppedTask.canPause());
        assertFalse(stoppedTask.canResume());
        assertTrue(stoppedTask.isTerminated());
    }

    @Test
    @DisplayName("测试任务时间字段更新")
    void testTaskTimeFields() {
        // 测试任务的时间字段更新逻辑
        AnalysisTask task = new AnalysisTask();
        assertNull(task.getLastActiveTime());
        assertNull(task.getLastStatusSync());

        // 更新活跃时间
        task.updateLastActiveTime();
        assertNotNull(task.getLastActiveTime());
        
        LocalDateTime activeTime = task.getLastActiveTime();
        assertTrue(activeTime.isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(activeTime.isAfter(LocalDateTime.now().minusSeconds(1)));

        // 更新状态同步时间
        task.updateLastStatusSync();
        assertNotNull(task.getLastStatusSync());
        
        LocalDateTime syncTime = task.getLastStatusSync();
        assertTrue(syncTime.isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(syncTime.isAfter(LocalDateTime.now().minusSeconds(1)));

        // 测试是否需要同步状态
        assertTrue(task.needsStatusSync(0)); // 立即需要同步

        task.setLastStatusSync(LocalDateTime.now().minusMinutes(10));
        assertTrue(task.needsStatusSync(5)); // 超过阈值需要同步

        task.setLastStatusSync(LocalDateTime.now().minusMinutes(2));
        assertFalse(task.needsStatusSync(5)); // 未超过阈值不需要同步
    }

    @Test
    @DisplayName("测试空值和边界条件处理")
    void testNullAndBoundaryConditions() {
        // 测试空任务名
        AnalysisTask emptyTask = new AnalysisTask();
        emptyTask.setId("empty-task");
        emptyTask.setTaskName("");
        
        assertNotNull(emptyTask.getId());
        assertEquals("", emptyTask.getTaskName());

        // 测试null状态
        emptyTask.setStatus(null);
        assertNull(emptyTask.getStatus());
        
        // 当状态为null时，相关方法应该返回false
        assertFalse(emptyTask.isActive());
        assertFalse(emptyTask.isTerminated());
        assertFalse(emptyTask.isTransitioning());

        // 测试空设备和通道ID
        emptyTask.setDeviceId("");
        emptyTask.setChannelId("");
        assertEquals("", emptyTask.getDeviceId());
        assertEquals("", emptyTask.getChannelId());
    }
}