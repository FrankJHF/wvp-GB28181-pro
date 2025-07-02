package com.genersoft.iot.vmp.analysis.controller;

import com.genersoft.iot.vmp.analysis.dto.CallbackRequest;
import com.genersoft.iot.vmp.analysis.service.IAnalysisResultService;
import com.genersoft.iot.vmp.vmanager.bean.WVPResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 内部回调控制器
 * 用于处理来自VLM微服务的分析结果回调
 */
@RestController
@RequestMapping("/api/internal")
public class InternalCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(InternalCallbackController.class);

    @Autowired
    private IAnalysisResultService analysisResultService;

    /**
     * VLM微服务分析结果回调接口
     *
     * @param callbackRequest 回调请求数据
     * @return 处理结果
     */
    @PostMapping("/callback")
    public WVPResult<Boolean> handleAnalysisCallback(@Valid @RequestBody CallbackRequest callbackRequest) {
        try {
            logger.info("接收到VLM微服务回调: taskId={}, isAlarm={}, timestamp={}",
                    callbackRequest.getTaskId(),
                    callbackRequest.getIsAlarm(),
                    callbackRequest.getResultTimestamp());

            // 记录详细的回调信息用于调试
            logger.debug("回调详情: {}", callbackRequest);

            // 处理分析结果回调
            boolean result = analysisResultService.handleCallback(callbackRequest);

            if (result) {
                logger.info("成功处理VLM回调: taskId={}", callbackRequest.getTaskId());
                return WVPResult.success(true, "回调处理成功");
            } else {
                logger.warn("VLM回调处理失败: taskId={}", callbackRequest.getTaskId());
                return WVPResult.fail(-1, "回调处理失败");
            }

        } catch (Exception e) {
            logger.error("处理VLM回调时发生异常: taskId={}",
                    callbackRequest != null ? callbackRequest.getTaskId() : "unknown", e);
            return WVPResult.fail(-1, "回调处理异常: " + e.getMessage());
        }
    }

    /**
     * VLM微服务健康检查接口
     * VLM微服务可以通过此接口检查WVP平台的连接状态
     */
    @GetMapping("/health")
    public WVPResult<String> healthCheck() {
        try {
            logger.debug("VLM微服务健康检查");
            return WVPResult.success("healthy", "WVP平台服务正常");
        } catch (Exception e) {
            logger.error("健康检查失败", e);
            return WVPResult.fail(-1, "服务异常");
        }
    }

    /**
     * VLM微服务心跳接口
     * 用于VLM微服务定期报告运行状态
     */
    @PostMapping("/heartbeat")
    public WVPResult<Boolean> heartbeat(@RequestBody(required = false) String serviceInfo) {
        try {
            logger.debug("收到VLM微服务心跳: {}", serviceInfo);
            return WVPResult.success(true, "心跳接收成功");
        } catch (Exception e) {
            logger.error("处理心跳失败", e);
            return WVPResult.fail(-1, "心跳处理失败");
        }
    }

    /**
     * VLM微服务错误报告接口
     * 用于VLM微服务报告处理过程中的错误
     */
    @PostMapping("/error")
    public WVPResult<Boolean> reportError(
            @RequestParam Integer taskId,
            @RequestParam String errorMessage,
            @RequestBody(required = false) String errorDetails) {
        try {
            logger.error("VLM微服务报告错误: taskId={}, message={}, details={}",
                    taskId, errorMessage, errorDetails);

            // 可以在这里实现错误处理逻辑，比如：
            // 1. 更新任务状态为错误
            // 2. 发送告警通知
            // 3. 记录错误统计

            // TODO: 实现错误处理业务逻辑
            // analysisResultService.handleTaskError(taskId, errorMessage, errorDetails);

            return WVPResult.success(true, "错误报告已记录");
        } catch (Exception e) {
            logger.error("处理错误报告失败: taskId={}", taskId, e);
            return WVPResult.fail(-1, "错误报告处理失败");
        }
    }

    /**
     * VLM微服务状态更新接口
     * 用于VLM微服务更新任务执行状态
     */
    @PostMapping("/status")
    public WVPResult<Boolean> updateTaskStatus(
            @RequestParam Integer taskId,
            @RequestParam String status,
            @RequestBody(required = false) String statusDetails) {
        try {
            logger.info("VLM微服务更新任务状态: taskId={}, status={}, details={}",
                    taskId, status, statusDetails);

            // TODO: 实现状态更新业务逻辑
            // analysisResultService.updateTaskStatus(taskId, status, statusDetails);

            return WVPResult.success(true, "状态更新成功");
        } catch (Exception e) {
            logger.error("更新任务状态失败: taskId={}", taskId, e);
            return WVPResult.fail(-1, "状态更新失败");
        }
    }

    /**
     * 批量回调接口
     * 用于VLM微服务批量提交分析结果
     */
    @PostMapping("/callback/batch")
    public WVPResult<Integer> handleBatchCallback(@Valid @RequestBody CallbackRequest[] callbackRequests) {
        try {
            logger.info("接收到VLM微服务批量回调: count={}", callbackRequests.length);

            int successCount = 0;
            for (CallbackRequest request : callbackRequests) {
                try {
                    boolean result = analysisResultService.handleCallback(request);
                    if (result) {
                        successCount++;
                    }
                } catch (Exception e) {
                    logger.error("批量回调中单个请求处理失败: taskId={}", request.getTaskId(), e);
                }
            }

            logger.info("批量回调处理完成: 总数={}, 成功={}, 失败={}",
                    callbackRequests.length, successCount, callbackRequests.length - successCount);

            return WVPResult.success(successCount,
                    String.format("批量回调处理完成，成功%d个，失败%d个",
                            successCount, callbackRequests.length - successCount));

        } catch (Exception e) {
            logger.error("批量回调处理异常", e);
            return WVPResult.fail(-1, "批量回调处理异常: " + e.getMessage());
        }
    }
}