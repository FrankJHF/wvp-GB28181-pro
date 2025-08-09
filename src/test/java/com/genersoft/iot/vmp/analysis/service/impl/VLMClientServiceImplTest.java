package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.service.IVLMClientService;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobRequest;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobActionResponse;
import com.genersoft.iot.vmp.conf.exception.ServiceException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * VLM客户端服务单元测试
 * @author Claude
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VLM客户端服务测试")
class VLMClientServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private VLMClientServiceImpl vlmClientService;

    private VLMJobRequest jobRequest;
    private VLMJobResponse jobResponse;
    private VLMJobActionResponse actionResponse;

    @BeforeEach
    void setUp() {
        // 设置配置参数
        ReflectionTestUtils.setField(vlmClientService, "vlmBaseUrl", "http://localhost:8001");
        ReflectionTestUtils.setField(vlmClientService, "timeout", 30000);
        ReflectionTestUtils.setField(vlmClientService, "maxRetryAttempts", 3);
        ReflectionTestUtils.setField(vlmClientService, "retryDelay", 1000);

        // 创建测试数据
        setupTestData();
    }

    private void setupTestData() {
        // 创建作业请求
        jobRequest = new VLMJobRequest();
        jobRequest.setDeviceId("34020000001320000001");
        jobRequest.setChannelId("34020000001310000001");
        jobRequest.setInputType("rtsp_stream");
        jobRequest.setInputData("rtsp://admin:password@192.168.1.100:554/stream1");
        jobRequest.setCallbackUrl("http://wvp-server:8080/api/vlm/callback");
        jobRequest.setAnalysisPrompt("请分析视频中是否发生火灾或其他突发情况");
        jobRequest.setModelName("videollama3-fire-detection");
        
        Map<String, Object> config = new HashMap<>();
        config.put("inference_interval", 5);
        config.put("sampling_fps", 5);
        jobRequest.setAnalysisConfig(config);

        // 创建作业响应
        jobResponse = new VLMJobResponse();
        jobResponse.setJobId("test-job-001");
        jobResponse.setStatus("created");
        jobResponse.setMessage("Job created successfully");
        jobResponse.setErrorCode("SUCCESS");

        // 创建操作响应
        actionResponse = new VLMJobActionResponse();
        actionResponse.setJobId("test-job-001");
        actionResponse.setPreviousStatus("created");
        actionResponse.setCurrentStatus("running");
        actionResponse.setMessage("Job started successfully");
    }

    @Test
    @DisplayName("测试创建作业成功")
    void testCreateJobSuccess() {
        // Arrange
        ResponseEntity<VLMJobResponse> responseEntity = 
                new ResponseEntity<>(jobResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq("http://localhost:8001/api/vlm/jobs"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(VLMJobResponse.class)
        )).thenReturn(responseEntity);

        // Act
        VLMJobResponse result = vlmClientService.createJob(jobRequest, true);

        // Assert
        assertNotNull(result);
        assertEquals("test-job-001", result.getJobId());
        assertEquals("created", result.getStatus());
        assertTrue(result.isSuccess());
        assertTrue(jobRequest.getAutoStart());

        verify(restTemplate, times(1)).exchange(
                eq("http://localhost:8001/api/vlm/jobs"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(VLMJobResponse.class)
        );
    }

    @Test
    @DisplayName("测试创建作业失败")
    void testCreateJobFailed() {
        // Arrange
        jobResponse.setErrorCode("INVALID_PARAMS");
        jobResponse.setMessage("Invalid parameters");
        ResponseEntity<VLMJobResponse> responseEntity = 
                new ResponseEntity<>(jobResponse, HttpStatus.BAD_REQUEST);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(VLMJobResponse.class)
        )).thenReturn(responseEntity);

        // Act
        VLMJobResponse result = vlmClientService.createJob(jobRequest, false);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Invalid parameters", result.getMessage());
        assertFalse(jobRequest.getAutoStart());
    }

    @Test
    @DisplayName("测试创建作业网络异常")
    void testCreateJobNetworkException() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(VLMJobResponse.class)
        )).thenThrow(new RestClientException("Connection timeout"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            vlmClientService.createJob(jobRequest, true);
        });
        
        assertEquals("VLM服务不可用: Connection timeout", exception.getMessage());
    }

    @Test
    @DisplayName("测试启动作业成功")
    void testStartJobSuccess() {
        // Arrange
        ResponseEntity<VLMJobActionResponse> responseEntity = 
                new ResponseEntity<>(actionResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq("http://localhost:8001/api/vlm/jobs/test-job-001/start"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(VLMJobActionResponse.class)
        )).thenReturn(responseEntity);

        // Act
        VLMJobActionResponse result = vlmClientService.startJob("test-job-001", false);

        // Assert
        assertNotNull(result);
        assertEquals("test-job-001", result.getJobId());
        assertEquals("created", result.getPreviousStatus());
        assertEquals("running", result.getCurrentStatus());
    }

    @Test
    @DisplayName("测试强制重启作业")
    void testStartJobWithForceRestart() {
        // Arrange
        ResponseEntity<VLMJobActionResponse> responseEntity = 
                new ResponseEntity<>(actionResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq("http://localhost:8001/api/vlm/jobs/test-job-001/start?force_restart=true"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(VLMJobActionResponse.class)
        )).thenReturn(responseEntity);

        // Act
        VLMJobActionResponse result = vlmClientService.startJob("test-job-001", true);

        // Assert
        assertNotNull(result);
        
        verify(restTemplate, times(1)).exchange(
                eq("http://localhost:8001/api/vlm/jobs/test-job-001/start?force_restart=true"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(VLMJobActionResponse.class)
        );
    }

    @Test
    @DisplayName("测试暂停作业成功")
    void testPauseJobSuccess() {
        // Arrange
        actionResponse.setPreviousStatus("running");
        actionResponse.setCurrentStatus("paused");
        actionResponse.setMessage("Job paused successfully");
        
        ResponseEntity<VLMJobActionResponse> responseEntity = 
                new ResponseEntity<>(actionResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq("http://localhost:8001/api/vlm/jobs/test-job-001/pause"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(VLMJobActionResponse.class)
        )).thenReturn(responseEntity);

        // Act
        VLMJobActionResponse result = vlmClientService.pauseJob("test-job-001");

        // Assert
        assertNotNull(result);
        assertEquals("running", result.getPreviousStatus());
        assertEquals("paused", result.getCurrentStatus());
        assertEquals("Job paused successfully", result.getMessage());
    }

    @Test
    @DisplayName("测试查询作业状态成功")
    void testGetJobStatusSuccess() {
        // Arrange
        jobResponse.setStatus("running");
        jobResponse.setMessage("Job is running");
        
        ResponseEntity<VLMJobResponse> responseEntity = 
                new ResponseEntity<>(jobResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq("http://localhost:8001/api/vlm/jobs/test-job-001"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(VLMJobResponse.class)
        )).thenReturn(responseEntity);

        // Act
        VLMJobResponse result = vlmClientService.getJobStatus("test-job-001");

        // Assert
        assertNotNull(result);
        assertEquals("test-job-001", result.getJobId());
        assertEquals("running", result.getStatus());
        assertEquals("Job is running", result.getMessage());
    }

    @Test
    @DisplayName("测试健康检查成功")
    void testCheckHealthSuccess() {
        // Arrange
        IVLMClientService.VLMHealthResponse healthResponse = 
                new IVLMClientService.VLMHealthResponse("healthy", "All systems operational");
        healthResponse.setVersion("1.0.0");
        
        ResponseEntity<IVLMClientService.VLMHealthResponse> responseEntity = 
                new ResponseEntity<>(healthResponse, HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq("http://localhost:8001/health"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(IVLMClientService.VLMHealthResponse.class)
        )).thenReturn(responseEntity);

        // Act
        IVLMClientService.VLMHealthResponse result = vlmClientService.checkHealth();

        // Assert
        assertNotNull(result);
        assertEquals("healthy", result.getStatus());
        assertEquals("All systems operational", result.getMessage());
        assertEquals("1.0.0", result.getVersion());
        assertTrue(result.isHealthy());
    }

    @Test
    @DisplayName("测试所有操作的异常处理")
    void testOperationsExceptionHandling() {
        // Arrange
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)
        )).thenThrow(new RestClientException("Service unavailable"));

        // Act & Assert - 各种操作都应该抛出ServiceException
        assertThrows(ServiceException.class, () -> {
            vlmClientService.createJob(jobRequest, true);
        });
        
        assertThrows(ServiceException.class, () -> {
            vlmClientService.startJob("test-job-001", false);
        });
        
        assertThrows(ServiceException.class, () -> {
            vlmClientService.pauseJob("test-job-001");
        });
        
        assertThrows(ServiceException.class, () -> {
            vlmClientService.resumeJob("test-job-001");
        });
        
        assertThrows(ServiceException.class, () -> {
            vlmClientService.stopJob("test-job-001");
        });
        
        assertThrows(ServiceException.class, () -> {
            vlmClientService.getJobStatus("test-job-001");
        });
        
        assertThrows(ServiceException.class, () -> {
            vlmClientService.checkHealth();
        });
    }

    @Test
    @DisplayName("测试VLM健康响应对象")
    void testVLMHealthResponse() {
        // 测试构造函数
        IVLMClientService.VLMHealthResponse response1 = 
                new IVLMClientService.VLMHealthResponse();
        assertNull(response1.getStatus());
        assertNull(response1.getMessage());
        
        IVLMClientService.VLMHealthResponse response2 = 
                new IVLMClientService.VLMHealthResponse("healthy", "OK");
        assertEquals("healthy", response2.getStatus());
        assertEquals("OK", response2.getMessage());
        assertTrue(response2.getTimestamp() > 0);

        // 测试setter和getter
        response1.setStatus("unhealthy");
        response1.setMessage("Error");
        response1.setVersion("1.0.0");
        response1.setTimestamp(123456L);

        assertEquals("unhealthy", response1.getStatus());
        assertEquals("Error", response1.getMessage());
        assertEquals("1.0.0", response1.getVersion());
        assertEquals(123456L, response1.getTimestamp());
        assertFalse(response1.isHealthy());

        // 测试isHealthy方法
        response1.setStatus("healthy");
        assertTrue(response1.isHealthy());
        
        response1.setStatus("unknown");
        assertFalse(response1.isHealthy());
    }
}