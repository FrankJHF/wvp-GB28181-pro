package com.genersoft.iot.vmp.analysis.service;

import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 分析任务管理服务接口
 * @author Claude
 */
public interface IAnalysisTaskService {

    /**
     * 创建分析任务
     * @param task 分析任务信息
     * @return 创建成功的任务
     */
    AnalysisTask createTask(AnalysisTask task);

    /**
     * 更新分析任务
     * @param task 分析任务信息
     * @return 更新成功的任务
     */
    AnalysisTask updateTask(AnalysisTask task);

    /**
     * 删除分析任务
     * @param taskId 任务ID
     * @return 是否删除成功
     */
    boolean deleteTask(String taskId);

    /**
     * 根据ID查询分析任务
     * @param taskId 任务ID
     * @return 分析任务
     */
    AnalysisTask getTaskById(String taskId);

    /**
     * 根据ID查询分析任务（包含关联信息）
     * @param taskId 任务ID
     * @return 分析任务
     */
    AnalysisTask getTaskWithDetailsById(String taskId);

    /**
     * 根据VLM作业ID查询任务
     * @param vlmJobId VLM作业ID
     * @return 分析任务
     */
    AnalysisTask getTaskByVlmJobId(String vlmJobId);

    /**
     * 分页查询分析任务
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @param analysisCardId 分析卡片ID
     * @param status 任务状态
     * @param createdBy 创建人
     * @param taskName 任务名称关键词
     * @return 分页结果
     */
    PageInfo<AnalysisTask> getTaskPage(int pageNum, int pageSize, String deviceId, String channelId,
                                      String analysisCardId, String status, String createdBy, String taskName);

    /**
     * 根据设备和通道查询任务
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @return 任务列表
     */
    List<AnalysisTask> getTasksByDeviceAndChannel(String deviceId, String channelId);

    /**
     * 根据状态查询任务
     * @param statuses 状态列表
     * @return 任务列表
     */
    List<AnalysisTask> getTasksByStatuses(List<TaskStatus> statuses);

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
     * 统计任务数量
     * @param deviceId 设备ID
     * @param status 任务状态
     * @param createdBy 创建人
     * @return 任务数量
     */
    long countTasks(String deviceId, String status, String createdBy);

    /**
     * 批量删除任务
     * @param taskIds 任务ID列表
     * @return 删除成功的数量
     */
    int batchDeleteTasks(List<String> taskIds);

    /**
     * 检查设备通道是否可以创建新任务
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @param analysisCardId 分析卡片ID
     * @return 是否可以创建
     */
    boolean canCreateTask(String deviceId, String channelId, String analysisCardId);

    /**
     * 获取设备通道的RTSP流地址
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @return RTSP流地址
     */
    String getDeviceChannelRtspUrl(String deviceId, String channelId);

    /**
     * 验证设备通道的有效性
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @return 是否有效
     */
    boolean validateDeviceChannel(String deviceId, String channelId);
}