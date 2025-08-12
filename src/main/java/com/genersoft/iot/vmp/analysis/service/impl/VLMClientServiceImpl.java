package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.service.IVLMClientService;
import com.genersoft.iot.vmp.analysis.service.IVLMClientService.VLMHealthResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobRequest;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobActionResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.JobStatusUpdateRequest;
import com.genersoft.iot.vmp.analysis.bean.dto.JobStatusResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.JobCancelResponse;
import com.genersoft.iot.vmp.conf.exception.ServiceException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * VLM微服务客户端实现类
 * @author Claude
 */
@Service
@Slf4j
public class VLMClientServiceImpl implements IVLMClientService {

    @Resource
    private RestTemplate restTemplate;

    @Value("${vlm.service.base-url:http://localhost:8001}")
    private String vlmBaseUrl;

    @Value("${vlm.service.timeout:30000}")
    private int timeout;

    @Value("${vlm.service.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${vlm.service.retry.delay:1000}")
    private int retryDelay;

    private static final String JOBS_ENDPOINT = "/api/vlm/jobs";
    private static final String HEALTH_ENDPOINT = "/health";

    @Override
    @Retryable(value = {RestClientException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public VLMJobResponse createJob(VLMJobRequest request, boolean autoStart) throws ServiceException {
        log.info("创建VLM作业，设备ID: {}, 通道ID: {}, 自动启动: {}", 
                request.getDeviceId(), request.getChannelId(), autoStart);
        
        try {
            // auto_start 作为 URL 参数传递
            String url = vlmBaseUrl + JOBS_ENDPOINT + "?auto_start=" + autoStart;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<VLMJobRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<VLMJobResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, VLMJobResponse.class);
            
            VLMJobResponse result = response.getBody();
            
            if (result != null && result.isSuccess()) {
                log.info("VLM作业创建成功，作业ID: {}, 状态: {}", result.getJobId(), result.getStatus());
            } else {
                log.error("VLM作业创建失败: {}", result != null ? result.getMessage() : "未知错误");
            }
            
            return result;
            
        } catch (RestClientException e) {
            log.error("调用VLM服务创建作业失败，设备ID: {}, 通道ID: {}", 
                    request.getDeviceId(), request.getChannelId(), e);
            throw new ServiceException("VLM服务不可用: " + e.getMessage());
        }
    }

    @Override
    @Retryable(value = {RestClientException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public VLMJobActionResponse updateJobStatus(String jobId, JobStatusUpdateRequest statusRequest) throws ServiceException {
        log.info("更新VLM作业状态，作业ID: {}, 操作: {}", jobId, statusRequest.getAction());
        
        try {
            String url = vlmBaseUrl + JOBS_ENDPOINT + "/" + jobId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<JobStatusUpdateRequest> entity = new HttpEntity<>(statusRequest, headers);
            
            ResponseEntity<VLMJobActionResponse> response = restTemplate.exchange(
                url, HttpMethod.PATCH, entity, VLMJobActionResponse.class);
            
            VLMJobActionResponse result = response.getBody();
            
            if (result != null && result.isSuccess()) {
                log.info("VLM作业状态更新成功，作业ID: {}, 前状态: {}, 后状态: {}", 
                        jobId, result.getPreviousStatus(), result.getCurrentStatus());
            } else {
                log.error("VLM作业状态更新失败，作业ID: {}, 错误: {}", jobId, 
                        result != null ? result.getErrorInfo() : "未知错误");
            }
            
            return result;
            
        } catch (RestClientException e) {
            log.error("调用VLM服务更新作业状态失败，作业ID: {}", jobId, e);
            throw new ServiceException("VLM服务更新作业状态失败: " + e.getMessage());
        }
    }

    @Override
    @Retryable(value = {RestClientException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public JobCancelResponse cancelJob(String jobId) throws ServiceException {
        log.info("取消VLM作业，作业ID: {}", jobId);
        
        try {
            String url = vlmBaseUrl + JOBS_ENDPOINT + "/" + jobId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<JobCancelResponse> response = restTemplate.exchange(
                url, HttpMethod.DELETE, entity, JobCancelResponse.class);
            
            JobCancelResponse result = response.getBody();
            
            if (result != null && result.isSuccess()) {
                log.info("VLM作业取消成功，作业ID: {}", jobId);
            } else {
                log.error("VLM作业取消失败，作业ID: {}, 错误: {}", jobId, 
                        result != null ? result.getMessage() : "未知错误");
            }
            
            return result;
            
        } catch (RestClientException e) {
            log.error("调用VLM服务取消作业失败，作业ID: {}", jobId, e);
            throw new ServiceException("VLM服务取消作业失败: " + e.getMessage());
        }
    }

    @Override
    public JobStatusResponse getJobStatus(String jobId) throws ServiceException {
        log.debug("查询VLM作业详细状态，作业ID: {}", jobId);
        
        try {
            String url = vlmBaseUrl + JOBS_ENDPOINT + "/" + jobId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<JobStatusResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JobStatusResponse.class);
            
            JobStatusResponse result = response.getBody();
            
            if (result != null) {
                log.debug("VLM作业详细状态查询成功，作业ID: {}, 状态: {}", jobId, result.getStatus());
            }
            
            return result;
            
        } catch (RestClientException e) {
            log.error("调用VLM服务查询作业详细状态失败，作业ID: {}", jobId, e);
            throw new ServiceException("VLM服务查询作业详细状态失败: " + e.getMessage());
        }
    }

    @Override
    public VLMHealthResponse checkHealth() {
        log.debug("检查VLM服务健康状态");
        
        try {
            String url = vlmBaseUrl + HEALTH_ENDPOINT;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<VLMHealthResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, VLMHealthResponse.class);
            
            VLMHealthResponse result = response.getBody();
            
            if (result != null) {
                log.debug("VLM服务健康检查完成，状态: {}", result.getStatus());
            }
            
            return result;
            
        } catch (RestClientException e) {
            log.warn("VLM服务健康检查失败: {}", e.getMessage());
            return new VLMHealthResponse("unhealthy", "服务连接失败: " + e.getMessage());
        }
    }
}