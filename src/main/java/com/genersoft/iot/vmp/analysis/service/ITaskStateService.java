package com.genersoft.iot.vmp.analysis.service;

import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.TaskAction;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;

import java.util.concurrent.CompletableFuture;

/**
 * 任务状态管理服务接口
 * @author Claude
 */
public interface ITaskStateService {

    /**
     * 启动任务
     * @param taskId 任务ID
     * @param forceRestart 是否强制重启
     * @return 异步操作结果
     */
    CompletableFuture<Void> startTask(String taskId, boolean forceRestart);

    /**
     * 暂停任务
     * @param taskId 任务ID
     * @return 异步操作结果
     */
    CompletableFuture<Void> pauseTask(String taskId);

    /**
     * 恢复任务
     * @param taskId 任务ID
     * @return 异步操作结果
     */
    CompletableFuture<Void> resumeTask(String taskId);

    /**
     * 停止任务
     * @param taskId 任务ID
     * @return 异步操作结果
     */
    CompletableFuture<Void> stopTask(String taskId);

    /**
     * 同步任务状态
     * @param taskId 任务ID
     * @return 同步后的任务状态
     */
    TaskStatus syncTaskStatus(String taskId);

    /**
     * 同步所有活跃任务状态
     * @return 同步的任务数量
     */
    int syncAllActiveTaskStatuses();

    /**
     * 检查并同步过期任务状态
     * @param thresholdMinutes 过期阈值（分钟）
     * @return 同步的任务数量
     */
    int checkAndSyncStaleTaskStatuses(int thresholdMinutes);

    /**
     * 获取任务当前状态
     * @param taskId 任务ID
     * @return 任务状态
     */
    TaskStatus getTaskStatus(String taskId);

    /**
     * 检查任务是否可以执行指定操作
     * @param taskId 任务ID
     * @param action 操作类型
     * @return 是否可以执行
     */
    boolean canPerformAction(String taskId, TaskAction action);

    /**
     * 更新任务状态为错误状态
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     * @return 是否更新成功
     */
    boolean markTaskError(String taskId, String errorMessage);

    /**
     * 强制更新任务状态
     * @param taskId 任务ID
     * @param status 目标状态
     * @param errorMessage 错误信息（可选）
     * @return 是否更新成功
     */
    boolean forceUpdateTaskStatus(String taskId, TaskStatus status, String errorMessage);

    /**
     * 批量更新任务状态
     * @param taskIds 任务ID列表
     * @param status 目标状态
     * @param errorMessage 错误信息（可选）
     * @return 更新成功的任务数量
     */
    int batchUpdateTaskStatus(java.util.List<String> taskIds, TaskStatus status, String errorMessage);
}