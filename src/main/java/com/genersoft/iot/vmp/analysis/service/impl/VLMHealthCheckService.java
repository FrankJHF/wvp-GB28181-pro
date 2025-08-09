package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.service.IVLMClientService;
import com.genersoft.iot.vmp.analysis.service.IVLMClientService.VLMHealthResponse;
import com.genersoft.iot.vmp.analysis.service.IAnalysisTaskService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * VLM健康检查和任务状态同步服务
 * @author Claude
 */
@Service
@Slf4j
public class VLMHealthCheckService {

    @Autowired
    private IVLMClientService vlmClientService;

    @Autowired(required = false)
    private IAnalysisTaskService analysisTaskService;

    @Value("${vlm.health-check.enabled:true}")
    private boolean healthCheckEnabled;

    @Value("${vlm.health-check.alert-threshold:3}")
    private int alertThreshold;

    @Value("${vlm.task-sync.enabled:true}")
    private boolean taskSyncEnabled;

    @Value("${vlm.task-sync.interval-minutes:5}")
    private int syncIntervalMinutes;

    private final AtomicBoolean lastHealthStatus = new AtomicBoolean(true);
    private volatile int consecutiveFailures = 0;
    private volatile LocalDateTime lastHealthCheck = LocalDateTime.now();
    private volatile LocalDateTime lastTaskSync = LocalDateTime.now();

    /**
     * 每分钟执行VLM服务健康检查
     */
    @Scheduled(fixedRate = 60000) // 60秒
    public void performHealthCheck() {
        if (!healthCheckEnabled) {
            return;
        }

        try {
            log.debug("开始执行VLM服务健康检查");
            
            VLMHealthResponse health = vlmClientService.checkHealth();
            boolean isHealthy = health != null && health.isHealthy();
            
            lastHealthCheck = LocalDateTime.now();
            
            if (isHealthy) {
                // 健康状态恢复
                if (!lastHealthStatus.get()) {
                    log.info("VLM服务健康状态已恢复: {}", health.getMessage());
                    sendHealthRecoveryAlert();
                }
                lastHealthStatus.set(true);
                consecutiveFailures = 0;
            } else {
                // 健康状态异常
                consecutiveFailures++;
                lastHealthStatus.set(false);
                
                String errorMsg = health != null ? health.getMessage() : "服务无响应";
                log.warn("VLM服务健康检查失败 (连续{}次): {}", consecutiveFailures, errorMsg);
                
                // 达到告警阈值时发送告警
                if (consecutiveFailures >= alertThreshold) {
                    sendHealthAlert(errorMsg, consecutiveFailures);
                }
            }
            
        } catch (Exception e) {
            consecutiveFailures++;
            lastHealthStatus.set(false);
            log.error("VLM服务健康检查异常 (连续{}次): {}", consecutiveFailures, e.getMessage());
            
            if (consecutiveFailures >= alertThreshold) {
                sendHealthAlert("健康检查异常: " + e.getMessage(), consecutiveFailures);
            }
        }
    }

    /**
     * 每30秒执行任务状态同步
     */
    @Scheduled(fixedRate = 30000) // 30秒
    public void syncTaskStatuses() {
        if (!taskSyncEnabled || analysisTaskService == null) {
            return;
        }

        try {
            log.debug("开始执行任务状态同步");
            
            // 只有VLM服务健康时才进行状态同步
            if (!lastHealthStatus.get()) {
                log.debug("VLM服务状态异常，跳过任务状态同步");
                return;
            }
            
            // 同步所有活跃任务的状态
            analysisTaskService.syncAllActiveTaskStatuses();
            lastTaskSync = LocalDateTime.now();
            
            log.debug("任务状态同步完成");
            
        } catch (Exception e) {
            log.error("任务状态同步失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每5分钟检查长时间无状态更新的任务
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void checkStaleTaskStatuses() {
        if (!taskSyncEnabled || analysisTaskService == null) {
            return;
        }

        try {
            log.debug("开始检查过期任务状态");
            
            // 检查超过指定时间未同步的任务
            analysisTaskService.checkAndSyncStaleTaskStatuses(syncIntervalMinutes);
            
        } catch (Exception e) {
            log.error("检查过期任务状态失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送健康状态告警
     * @param errorMessage 错误信息
     * @param failureCount 连续失败次数
     */
    private void sendHealthAlert(String errorMessage, int failureCount) {
        // 这里可以集成消息通知服务，比如邮件、短信、WebSocket等
        log.error("【VLM服务告警】服务连续{}次健康检查失败: {}", failureCount, errorMessage);
        
        // TODO: 集成告警通知系统
        // - 发送邮件通知管理员
        // - 发送WebSocket实时通知到前端
        // - 记录告警日志到数据库
    }

    /**
     * 发送健康状态恢复通知
     */
    private void sendHealthRecoveryAlert() {
        log.info("【VLM服务恢复】服务健康状态已恢复正常");
        
        // TODO: 发送恢复通知
        // - 发送恢复通知邮件
        // - 发送WebSocket恢复通知到前端
    }

    /**
     * 获取VLM服务当前健康状态
     * @return true如果服务健康
     */
    public boolean isVLMServiceHealthy() {
        return lastHealthStatus.get();
    }

    /**
     * 获取连续失败次数
     * @return 连续失败次数
     */
    public int getConsecutiveFailures() {
        return consecutiveFailures;
    }

    /**
     * 获取最后健康检查时间
     * @return 最后健康检查时间
     */
    public LocalDateTime getLastHealthCheckTime() {
        return lastHealthCheck;
    }

    /**
     * 获取最后任务同步时间
     * @return 最后任务同步时间
     */
    public LocalDateTime getLastTaskSyncTime() {
        return lastTaskSync;
    }

    /**
     * 手动触发健康检查
     * @return 健康检查结果
     */
    public VLMHealthResponse manualHealthCheck() {
        log.info("手动触发VLM服务健康检查");
        return vlmClientService.checkHealth();
    }

    /**
     * 手动触发任务状态同步
     */
    public void manualTaskSync() {
        if (analysisTaskService != null) {
            log.info("手动触发任务状态同步");
            analysisTaskService.syncAllActiveTaskStatuses();
            lastTaskSync = LocalDateTime.now();
        }
    }
}