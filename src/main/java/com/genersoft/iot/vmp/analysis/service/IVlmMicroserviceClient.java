package com.genersoft.iot.vmp.analysis.service;

import com.genersoft.iot.vmp.analysis.entity.AnalysisTask;

import java.util.Map;

/**
 * VLM微服务客户端接口
 */
public interface IVlmMicroserviceClient {

    /**
     * 启动分析任务
     * @param task 分析任务
     * @return 是否成功
     */
    boolean startAnalysisTask(AnalysisTask task);

    /**
     * 停止分析任务
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean stopAnalysisTask(Integer taskId);

    /**
     * 获取微服务状态
     * @return 状态信息
     */
    Map<String, Object> getServiceStatus();

    /**
     * 健康检查
     * @return 是否健康
     */
    boolean healthCheck();

    /**
     * 获取任务状态
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    Map<String, Object> getTaskStatus(Integer taskId);

    /**
     * 更新任务配置
     * @param task 分析任务
     * @return 是否成功
     */
    boolean updateAnalysisTask(AnalysisTask task);

    /**
     * 获取系统资源使用情况
     * @return 资源使用信息
     */
    Map<String, Object> getSystemResources();

    /**
     * 测试与微服务的连接
     * @return 连接状态
     */
    boolean testConnection();
}