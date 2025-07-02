package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.entity.AnalysisTask;
import com.genersoft.iot.vmp.analysis.service.IVlmMicroserviceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * VLM微服务客户端实现类
 */
@Service
public class VlmMicroserviceClientImpl implements IVlmMicroserviceClient {

    private static final Logger logger = LoggerFactory.getLogger(VlmMicroserviceClientImpl.class);

    @Value("${vlm.microservice.url:http://localhost:8000}")
    private String vlmServiceUrl;

    @Value("${vlm.microservice.timeout:30000}")
    private int timeout;

    @Value("${vlm.microservice.retry.times:3}")
    private int retryTimes;

    @Value("${vlm.microservice.retry.interval:2000}")
    private long retryInterval;

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();

        // 设置超时时间
        restTemplate.getRequestFactory();

        logger.info("VLM微服务客户端初始化完成: url={}, timeout={}ms", vlmServiceUrl, timeout);
    }

    @Override
    public boolean startAnalysisTask(AnalysisTask task) {
        logger.info("启动VLM分析任务: taskId={}, deviceId={}, channelId={}",
                task.getId(), task.getDeviceId(), task.getChannelId());

        try {
            // 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("task_id", task.getId());
            requestBody.put("device_id", task.getDeviceId());
            requestBody.put("channel_id", task.getChannelId());
            requestBody.put("analysis_question", task.getAnalysisQuestion());
            requestBody.put("analysis_interval", task.getAnalysisInterval());
            requestBody.put("task_name", task.getTaskName());

            // 发送启动请求
            String url = vlmServiceUrl + "/start_analysis";
            ResponseEntity<Map> response = sendRequestWithRetry(url, HttpMethod.POST, requestBody, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");

                if (Boolean.TRUE.equals(success)) {
                    logger.info("成功启动VLM分析任务: taskId={}", task.getId());
                    return true;
                } else {
                    String message = (String) responseBody.get("message");
                    logger.error("VLM微服务启动任务失败: taskId={}, message={}", task.getId(), message);
                    return false;
                }
            } else {
                logger.error("VLM微服务返回非200状态码: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            logger.error("启动VLM分析任务异常: taskId={}", task.getId(), e);
            return false;
        }
    }

    @Override
    public boolean stopAnalysisTask(Integer taskId) {
        logger.info("停止VLM分析任务: taskId={}", taskId);

        try {
            // 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("task_id", taskId);

            // 发送停止请求
            String url = vlmServiceUrl + "/stop_analysis";
            ResponseEntity<Map> response = sendRequestWithRetry(url, HttpMethod.POST, requestBody, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");

                if (Boolean.TRUE.equals(success)) {
                    logger.info("成功停止VLM分析任务: taskId={}", taskId);
                    return true;
                } else {
                    String message = (String) responseBody.get("message");
                    logger.error("VLM微服务停止任务失败: taskId={}, message={}", taskId, message);
                    return false;
                }
            } else {
                logger.error("VLM微服务返回非200状态码: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            logger.error("停止VLM分析任务异常: taskId={}", taskId, e);
            return false;
        }
    }

    public boolean isServiceHealthy() {
        logger.debug("检查VLM微服务健康状态");

        try {
            String url = vlmServiceUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String status = (String) responseBody.get("status");

                boolean healthy = "healthy".equals(status);
                logger.debug("VLM微服务健康状态: {}", healthy);
                return healthy;
            } else {
                logger.warn("VLM微服务健康检查返回非200状态码: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            logger.error("VLM微服务健康检查异常", e);
            return false;
        }
    }

    public Map<String, Object> getServiceInfo() {
        logger.debug("获取VLM微服务信息");

        try {
            String url = vlmServiceUrl + "/info";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> serviceInfo = response.getBody();
                logger.debug("VLM微服务信息: {}", serviceInfo);
                return serviceInfo;
            } else {
                logger.warn("获取VLM微服务信息失败: {}", response.getStatusCode());
                return new HashMap<>();
            }

        } catch (Exception e) {
            logger.error("获取VLM微服务信息异常", e);
            return new HashMap<>();
        }
    }

    public boolean updateTaskConfig(Integer taskId, String analysisQuestion, Integer analysisInterval) {
        logger.info("更新VLM任务配置: taskId={}, question={}, interval={}",
                taskId, analysisQuestion, analysisInterval);

        try {
            // 构建请求参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("task_id", taskId);
            requestBody.put("analysis_question", analysisQuestion);
            requestBody.put("analysis_interval", analysisInterval);

            // 发送更新请求
            String url = vlmServiceUrl + "/update_task";
            ResponseEntity<Map> response = sendRequestWithRetry(url, HttpMethod.POST, requestBody, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                Boolean success = (Boolean) responseBody.get("success");

                if (Boolean.TRUE.equals(success)) {
                    logger.info("成功更新VLM任务配置: taskId={}", taskId);
                    return true;
                } else {
                    String message = (String) responseBody.get("message");
                    logger.error("VLM微服务更新任务配置失败: taskId={}, message={}", taskId, message);
                    return false;
                }
            } else {
                logger.error("VLM微服务返回非200状态码: {}", response.getStatusCode());
                return false;
            }

        } catch (Exception e) {
            logger.error("更新VLM任务配置异常: taskId={}", taskId, e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getTaskStatus(Integer taskId) {
        logger.debug("获取VLM任务状态: taskId={}", taskId);

        try {
            String url = vlmServiceUrl + "/task_status?task_id=" + taskId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> taskStatus = response.getBody();
                logger.debug("VLM任务状态: taskId={}, status={}", taskId, taskStatus);
                return taskStatus;
            } else {
                logger.warn("获取VLM任务状态失败: taskId={}, status={}", taskId, response.getStatusCode());
                return new HashMap<>();
            }

        } catch (Exception e) {
            logger.error("获取VLM任务状态异常: taskId={}", taskId, e);
            return new HashMap<>();
        }
    }

    /**
     * 带重试机制的HTTP请求发送
     */
    private <T> ResponseEntity<T> sendRequestWithRetry(String url, HttpMethod method,
                                                       Object requestBody, Class<T> responseType) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= retryTimes; attempt++) {
            try {
                logger.debug("发送VLM请求 (尝试 {}/{}): {} {}", attempt, retryTimes, method, url);

                // 设置请求头
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                // 构建请求实体
                HttpEntity<Object> requestEntity = new HttpEntity<>(requestBody, headers);

                // 发送请求
                ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, responseType);

                logger.debug("VLM请求成功: 状态码={}", response.getStatusCode());
                return response;

            } catch (ResourceAccessException e) {
                // 网络连接异常
                lastException = e;
                logger.warn("VLM请求连接失败 (尝试 {}/{}): {}", attempt, retryTimes, e.getMessage());

            } catch (HttpClientErrorException e) {
                // 4xx客户端错误，不重试
                logger.error("VLM请求客户端错误: {}", e.getMessage());
                throw e;

            } catch (HttpServerErrorException e) {
                // 5xx服务器错误，可以重试
                lastException = e;
                logger.warn("VLM请求服务器错误 (尝试 {}/{}): {}", attempt, retryTimes, e.getMessage());

            } catch (Exception e) {
                // 其他异常
                lastException = e;
                logger.error("VLM请求异常 (尝试 {}/{}): {}", attempt, retryTimes, e.getMessage());
            }

            // 如果不是最后一次尝试，等待后重试
            if (attempt < retryTimes) {
                try {
                    TimeUnit.MILLISECONDS.sleep(retryInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // 所有重试都失败
        logger.error("VLM请求最终失败: {} {}, 重试次数: {}", method, url, retryTimes);
        if (lastException instanceof RuntimeException) {
            throw (RuntimeException) lastException;
        } else {
            throw new RuntimeException("VLM微服务请求失败", lastException);
        }
    }

    @Override
    public boolean testConnection() {
        try {
            // 使用健康检查接口测试连接
            return healthCheck();
        } catch (Exception e) {
            logger.error("测试VLM微服务连接失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean healthCheck() {
        return isServiceHealthy();
    }

    @Override
    public Map<String, Object> getServiceStatus() {
        return getServiceInfo();
    }

    @Override
    public boolean updateAnalysisTask(AnalysisTask task) {
        return updateTaskConfig(task.getId(), task.getAnalysisQuestion(), task.getAnalysisInterval());
    }

    @Override
    public Map<String, Object> getSystemResources() {
        logger.debug("获取VLM微服务系统资源信息");

        try {
            String url = vlmServiceUrl + "/resources";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> resources = response.getBody();
                logger.debug("VLM微服务系统资源: {}", resources);
                return resources;
            } else {
                logger.warn("获取VLM微服务系统资源失败: {}", response.getStatusCode());
                return new HashMap<>();
            }

        } catch (Exception e) {
            logger.error("获取VLM微服务系统资源异常", e);
            return new HashMap<>();
        }
    }
}