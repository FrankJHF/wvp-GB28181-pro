package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.service.IVLMClientService;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobRequest;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobActionResponse;
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
import java.util.concurrent.TimeUnit;

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
    public VLMJobResponse createJob(VLMJobRequest request, boolean autoStart) {
        log.info("创建VLM作业，设备ID: {}, 通道ID: {}, 自动启动: {}", 
                request.getDeviceId(), request.getChannelId(), autoStart);
        
        try {
            request.setAutoStart(autoStart);
            String url = vlmBaseUrl + JOBS_ENDPOINT;
            
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
    public VLMJobActionResponse startJob(String jobId, boolean forceRestart) {
        log.info("启动VLM作业，作业ID: {}, 强制重启: {}", jobId, forceRestart);
        
        try {
            String url = vlmBaseUrl + JOBS_ENDPOINT + "/" + jobId + "/start";
            if (forceRestart) {
                url += "?force_restart=true";
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<VLMJobActionResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, VLMJobActionResponse.class);
            
            VLMJobActionResponse result = response.getBody();
            
            if (result != null && result.isSuccess()) {
                log.info("VLM作业启动成功，作业ID: {}, 当前状态: {}", jobId, result.getCurrentStatus());
            } else {
                log.error("VLM作业启动失败，作业ID: {}, 错误: {}", jobId, 
                        result != null ? result.getErrorInfo() : "未知错误");
            }
            
            return result;
            
        } catch (RestClientException e) {
            log.error("调用VLM服务启动作业失败，作业ID: {}", jobId, e);
            throw new ServiceException("VLM服务启动作业失败: " + e.getMessage());
        }
    }

    @Override
    @Retryable(value = {RestClientException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public VLMJobActionResponse pauseJob(String jobId) {
        log.info("暂停VLM作业，作业ID: {}", jobId);
        
        try {
            String url = vlmBaseUrl + JOBS_ENDPOINT + "/" + jobId + "/pause";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<VLMJobActionResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, VLMJobActionResponse.class);
            
            VLMJobActionResponse result = response.getBody();
            
            if (result != null && result.isSuccess()) {
                log.info("VLM作业暂停成功，作业ID: {}, 当前状态: {}", jobId, result.getCurrentStatus());
            } else {
                log.error("VLM作业暂停失败，作业ID: {}, 错误: {}", jobId, 
                        result != null ? result.getErrorInfo() : "未知错误");
            }
            
            return result;
            
        } catch (RestClientException e) {
            log.error("调用VLM服务暂停作业失败，作业ID: {}", jobId, e);
            throw new ServiceException("VLM服务暂停作业失败: " + e.getMessage());
        }
    }

    @Override
    @Retryable(value = {RestClientException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public VLMJobActionResponse resumeJob(String jobId) {
        log.info("恢复VLM作业，作业ID: {}", jobId);
        
        try {
            String url = vlmBaseUrl + JOBS_ENDPOINT + "/" + jobId + "/resume";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<VLMJobActionResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, VLMJobActionResponse.class);
            
            VLMJobActionResponse result = response.getBody();
            
            if (result != null && result.isSuccess()) {
                log.info("VLM作业恢复成功，作业ID: {}, 当前状态: {}", jobId, result.getCurrentStatus());
            } else {
                log.error("VLM作业恢复失败，作业ID: {}, 错误: {}", jobId, 
                        result != null ? result.getErrorInfo() : "未知错误");
            }
            
            return result;
            
        } catch (RestClientException e) {
            log.error("调用VLM服务恢复作业失败，作业ID: {}", jobId, e);
            throw new ServiceException("VLM服务恢复作业失败: " + e.getMessage());
        }
    }

    @Override
    @Retryable(value = {RestClientException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public VLMJobActionResponse stopJob(String jobId) {
        log.info("停止VLM作业，作业ID: {}", jobId);
        
        try {
            String url = vlmBaseUrl + JOBS_ENDPOINT + "/" + jobId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<VLMJobActionResponse> response = restTemplate.exchange(
                url, HttpMethod.DELETE, entity, VLMJobActionResponse.class);
            
            VLMJobActionResponse result = response.getBody();
            
            if (result != null && result.isSuccess()) {
                log.info("VLM作业停止成功，作业ID: {}, 当前状态: {}", jobId, result.getCurrentStatus());
            } else {
                log.error("VLM作业停止失败，作业ID: {}, 错误: {}", jobId, 
                        result != null ? result.getErrorInfo() : "未知错误");
            }
            
            return result;
            
        } catch (RestClientException e) {
            log.error("调用VLM服务停止作业失败，作业ID: {}", jobId, e);
            throw new ServiceException("VLM服务停止作业失败: " + e.getMessage());
        }
    }

    @Override
    public VLMJobResponse getJobStatus(String jobId) {
        log.debug("查询VLM作业状态，作业ID: {}", jobId);
        
        try {
            String url = vlmBaseUrl + JOBS_ENDPOINT + "/" + jobId;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<VLMJobResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, VLMJobResponse.class);
            
            VLMJobResponse result = response.getBody();
            
            if (result != null) {
                log.debug("VLM作业状态查询成功，作业ID: {}, 状态: {}", jobId, result.getStatus());
            }
            
            return result;
            
        } catch (RestClientException e) {
            log.error("调用VLM服务查询作业状态失败，作业ID: {}", jobId, e);
            throw new ServiceException("VLM服务查询作业状态失败: " + e.getMessage());
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