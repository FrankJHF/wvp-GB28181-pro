package com.genersoft.iot.vmp.vmanager.analysis;

import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;
import com.genersoft.iot.vmp.analysis.service.IAnalysisTaskService;
import com.genersoft.iot.vmp.vmanager.bean.WVPResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.github.pagehelper.PageInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 分析任务控制器集成测试
 * @author Claude
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("分析任务控制器集成测试")
class AnalysisTaskControllerTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    @MockBean
    private IAnalysisTaskService analysisTaskService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AnalysisTask testTask;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        
        // 创建测试数据
        setupTestData();
    }

    private void setupTestData() {
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
    }

    @Test
    @DisplayName("测试分页查询分析任务")
    void testGetTasks() throws Exception {
        // Arrange
        List<AnalysisTask> tasks = Arrays.asList(testTask);
        PageInfo<AnalysisTask> pageInfo = new PageInfo<>(tasks);
        pageInfo.setTotal(1);
        pageInfo.setPages(1);
        pageInfo.setPageNum(1);
        pageInfo.setPageSize(20);

        when(analysisTaskService.getTaskPage(
                eq(1), eq(20), isNull(), isNull(), isNull(), isNull(), anyString(), isNull()))
                .thenReturn(pageInfo);

        // Act & Assert
        mockMvc.perform(get("/api/vmanager/analysis/tasks")
                .param("page", "1")
                .param("count", "20")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("查询成功"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value("test-task-001"))
                .andExpect(jsonPath("$.data.list[0].taskName").value("火灾检测任务"));

        verify(analysisTaskService, times(1))
                .getTaskPage(eq(1), eq(20), isNull(), isNull(), isNull(), isNull(), anyString(), isNull());
    }

    @Test
    @DisplayName("测试根据ID查询任务详情")
    void testGetTaskById() throws Exception {
        // Arrange
        when(analysisTaskService.getTaskWithDetailsById("test-task-001")).thenReturn(testTask);

        // Act & Assert
        mockMvc.perform(get("/api/vmanager/analysis/tasks/{id}", "test-task-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.id").value("test-task-001"))
                .andExpect(jsonPath("$.data.taskName").value("火灾检测任务"))
                .andExpect(jsonPath("$.data.status").value("CREATED"));

        verify(analysisTaskService, times(1)).getTaskWithDetailsById("test-task-001");
    }

    @Test
    @DisplayName("测试查询不存在的任务")
    void testGetTaskByIdNotFound() throws Exception {
        // Arrange
        when(analysisTaskService.getTaskWithDetailsById("non-existing")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/api/vmanager/analysis/tasks/{id}", "non-existing")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("任务不存在"));

        verify(analysisTaskService, times(1)).getTaskWithDetailsById("non-existing");
    }

    @Test
    @DisplayName("测试创建分析任务")
    void testCreateTask() throws Exception {
        // Arrange
        AnalysisTask newTask = new AnalysisTask();
        newTask.setTaskName("新的火灾检测任务");
        newTask.setAnalysisCardId("card-001");
        newTask.setDeviceId("34020000001320000001");
        newTask.setChannelId("34020000001310000001");

        when(analysisTaskService.createTask(any(AnalysisTask.class))).thenReturn(testTask);

        // Act & Assert
        mockMvc.perform(post("/api/vmanager/analysis/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("创建成功"))
                .andExpect(jsonPath("$.data.id").value("test-task-001"));

        verify(analysisTaskService, times(1)).createTask(any(AnalysisTask.class));
    }

    @Test
    @DisplayName("测试更新分析任务")
    void testUpdateTask() throws Exception {
        // Arrange
        AnalysisTask updateTask = new AnalysisTask();
        updateTask.setId("test-task-001");
        updateTask.setTaskName("更新后的任务名称");

        testTask.setTaskName("更新后的任务名称");
        when(analysisTaskService.updateTask(any(AnalysisTask.class))).thenReturn(testTask);

        // Act & Assert
        mockMvc.perform(put("/api/vmanager/analysis/tasks/{id}", "test-task-001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("更新成功"))
                .andExpect(jsonPath("$.data.taskName").value("更新后的任务名称"));

        verify(analysisTaskService, times(1)).updateTask(any(AnalysisTask.class));
    }

    @Test
    @DisplayName("测试删除分析任务")
    void testDeleteTask() throws Exception {
        // Arrange
        when(analysisTaskService.deleteTask("test-task-001")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/vmanager/analysis/tasks/{id}", "test-task-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("删除成功"));

        verify(analysisTaskService, times(1)).deleteTask("test-task-001");
    }

    @Test
    @DisplayName("测试启动任务")
    void testStartTask() throws Exception {
        // Arrange
        when(analysisTaskService.startTask("test-task-001", false))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act & Assert
        mockMvc.perform(post("/api/vmanager/analysis/tasks/{id}/start", "test-task-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("启动任务成功"));

        verify(analysisTaskService, times(1)).startTask("test-task-001", false);
    }

    @Test
    @DisplayName("测试暂停任务")
    void testPauseTask() throws Exception {
        // Arrange
        when(analysisTaskService.pauseTask("test-task-001"))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act & Assert
        mockMvc.perform(post("/api/vmanager/analysis/tasks/{id}/pause", "test-task-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("暂停任务成功"));

        verify(analysisTaskService, times(1)).pauseTask("test-task-001");
    }

    @Test
    @DisplayName("测试恢复任务")
    void testResumeTask() throws Exception {
        // Arrange
        when(analysisTaskService.resumeTask("test-task-001"))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act & Assert
        mockMvc.perform(post("/api/vmanager/analysis/tasks/{id}/resume", "test-task-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("恢复任务成功"));

        verify(analysisTaskService, times(1)).resumeTask("test-task-001");
    }

    @Test
    @DisplayName("测试停止任务")
    void testStopTask() throws Exception {
        // Arrange
        when(analysisTaskService.stopTask("test-task-001"))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act & Assert
        mockMvc.perform(post("/api/vmanager/analysis/tasks/{id}/stop", "test-task-001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("停止任务成功"));

        verify(analysisTaskService, times(1)).stopTask("test-task-001");
    }

    @Test
    @DisplayName("测试批量删除任务")
    void testBatchDeleteTasks() throws Exception {
        // Arrange
        List<String> taskIds = Arrays.asList("task-001", "task-002", "task-003");
        when(analysisTaskService.batchDeleteTasks(taskIds)).thenReturn(3);

        // Act & Assert
        mockMvc.perform(delete("/api/vmanager/analysis/tasks/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("批量删除成功，共删除 3 个任务"));

        verify(analysisTaskService, times(1)).batchDeleteTasks(taskIds);
    }

    @Test
    @DisplayName("测试参数验证失败")
    void testValidationError() throws Exception {
        // 创建缺少必填字段的任务
        AnalysisTask invalidTask = new AnalysisTask();
        // 缺少taskName、analysisCardId等必填字段

        // Act & Assert
        mockMvc.perform(post("/api/vmanager/analysis/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试服务异常处理")
    void testServiceException() throws Exception {
        // Arrange
        when(analysisTaskService.getTaskWithDetailsById("error-task"))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // Act & Assert
        mockMvc.perform(get("/api/vmanager/analysis/tasks/{id}", "error-task")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(500));

        verify(analysisTaskService, times(1)).getTaskWithDetailsById("error-task");
    }

    @Test
    @DisplayName("测试分页参数验证")
    void testPaginationParameters() throws Exception {
        // 测试非法的分页参数
        mockMvc.perform(get("/api/vmanager/analysis/tasks")
                .param("page", "0")  // 非法页码
                .param("count", "0") // 非法数量
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // 测试过大的分页参数
        mockMvc.perform(get("/api/vmanager/analysis/tasks")
                .param("page", "999999")
                .param("count", "10000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试查询条件组合")
    void testQueryWithMultipleFilters() throws Exception {
        // Arrange
        List<AnalysisTask> tasks = Arrays.asList(testTask);
        PageInfo<AnalysisTask> pageInfo = new PageInfo<>(tasks);

        when(analysisTaskService.getTaskPage(
                eq(1), eq(20), eq("34020000001320000001"), eq("34020000001310000001"),
                eq("card-001"), eq("created"), anyString(), eq("火灾")))
                .thenReturn(pageInfo);

        // Act & Assert
        mockMvc.perform(get("/api/vmanager/analysis/tasks")
                .param("page", "1")
                .param("count", "20")
                .param("deviceId", "34020000001320000001")
                .param("channelId", "34020000001310000001")
                .param("analysisCardId", "card-001")
                .param("status", "created")
                .param("taskName", "火灾")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(analysisTaskService, times(1))
                .getTaskPage(eq(1), eq(20), eq("34020000001320000001"), eq("34020000001310000001"),
                        eq("card-001"), eq("created"), anyString(), eq("火灾"));
    }
}