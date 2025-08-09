package com.genersoft.iot.vmp.test.config;

import com.genersoft.iot.vmp.analysis.service.IVLMClientService;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobRequest;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobActionResponse;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

/**
 * 测试环境配置 - Mock服务配置
 * 
 * @author Claude
 */
@TestConfiguration
@Profile("test")
public class TestMockConfig {

    /**
     * Mock VLM客户端服务
     * 在测试环境中使用Mock实现，避免调用真实的VLM服务
     */
    @Bean
    @Primary
    public IVLMClientService mockVLMClientService() {
        IVLMClientService mockService = Mockito.mock(IVLMClientService.class);
        
        // 设置默认的Mock行为
        setupDefaultMockBehavior(mockService);
        
        return mockService;
    }
    
    /**
     * 设置VLM服务Mock的默认行为
     */
    private void setupDefaultMockBehavior(IVLMClientService mockService) {
        try {
            // 创建作业的默认响应
            VLMJobResponse createResponse = new VLMJobResponse();
            createResponse.setJobId("mock-job-001");
            createResponse.setStatus("created");
            createResponse.setMessage("Mock job created successfully");
            createResponse.setErrorCode("SUCCESS");
            
            Mockito.when(mockService.createJob(Mockito.any(VLMJobRequest.class), Mockito.anyBoolean()))
                   .thenReturn(createResponse);
            
            // 操作响应的默认行为
            VLMJobActionResponse actionResponse = new VLMJobActionResponse();
            actionResponse.setJobId("mock-job-001");
            actionResponse.setPreviousStatus("created");
            actionResponse.setCurrentStatus("running");
            actionResponse.setMessage("Mock operation successful");
            
            Mockito.when(mockService.startJob(Mockito.anyString(), Mockito.anyBoolean()))
                   .thenReturn(actionResponse);
            Mockito.when(mockService.pauseJob(Mockito.anyString()))
                   .thenReturn(actionResponse);
            Mockito.when(mockService.resumeJob(Mockito.anyString()))
                   .thenReturn(actionResponse);
            Mockito.when(mockService.stopJob(Mockito.anyString()))
                   .thenReturn(actionResponse);
            
            // 状态查询的默认响应
            VLMJobResponse statusResponse = new VLMJobResponse();
            statusResponse.setJobId("mock-job-001");
            statusResponse.setStatus("running");
            statusResponse.setMessage("Mock job is running");
            statusResponse.setErrorCode("SUCCESS");
            
            Mockito.when(mockService.getJobStatus(Mockito.anyString()))
                   .thenReturn(statusResponse);
            
            // 健康检查的默认响应
            IVLMClientService.VLMHealthResponse healthResponse = 
                new IVLMClientService.VLMHealthResponse("healthy", "Mock service is healthy");
            healthResponse.setVersion("1.0.0-mock");
            
            Mockito.when(mockService.checkHealth())
                   .thenReturn(healthResponse);
                   
        } catch (Exception e) {
            // 忽略Mock设置异常
        }
    }
}