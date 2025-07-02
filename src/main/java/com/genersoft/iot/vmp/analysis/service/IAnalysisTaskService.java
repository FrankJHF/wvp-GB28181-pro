package com.genersoft.iot.vmp.analysis.service;

import com.genersoft.iot.vmp.analysis.entity.AnalysisTask;
import com.genersoft.iot.vmp.analysis.dto.TaskCreateRequest;
import com.genersoft.iot.vmp.analysis.dto.TaskUpdateRequest;
import com.genersoft.iot.vmp.analysis.dto.DeviceChannelResponse;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 分析任务服务接口
 */
public interface IAnalysisTaskService {

    /**
     * 创建分析任务
     * @param request 任务创建请求
     * @return 任务ID
     */
    Integer createTask(TaskCreateRequest request);

    /**
     * 更新分析任务
     * @param taskId 任务ID (UUID字符串)
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateTask(String taskId, TaskUpdateRequest request);

    /**
     * 删除分析任务
     * @param taskId 任务ID (UUID字符串)
     * @return 是否成功
     */
    boolean deleteTask(String taskId);

    /**
     * 启动分析任务
     * @param taskId 任务ID (UUID字符串)
     * @return 是否成功
     */
    boolean startTask(String taskId);

    /**
     * 停止分析任务
     * @param taskId 任务ID (UUID字符串)
     * @return 是否成功
     */
    boolean stopTask(String taskId);

    /**
     * 获取任务详情
     * @param taskId 任务ID (UUID字符串)
     * @return 任务信息
     */
    AnalysisTask getTask(String taskId);

    /**
     * 通过主键ID获取任务详情 (内部使用)
     * @param id 数据库主键ID
     * @return 任务信息
     */
    AnalysisTask getTaskById(Integer id);

    /**
     * 获取任务列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageInfo<AnalysisTask> getTasks(int page, int pageSize);

    /**
     * 根据设备通道获取任务
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @return 任务信息
     */
    AnalysisTask getTaskByDeviceChannel(String deviceId, String channelId);

    /**
     * 获取运行中的任务列表
     * @return 运行中的任务列表
     */
    List<AnalysisTask> getRunningTasks();

    /**
     * 获取可分析的设备列表
     * @return 设备列表
     */
    List<DeviceChannelResponse.DeviceInfo> getAnalysisDevices();

    /**
     * 获取设备的通道列表
     * @param deviceId 设备ID
     * @return 通道列表
     */
    List<DeviceChannelResponse.ChannelInfo> getDeviceChannels(String deviceId);

    /**
     * 验证任务配置是否有效
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @return 验证结果
     */
    boolean validateTaskConfig(String deviceId, String channelId);

    /**
     * 检查设备通道是否已有运行任务
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @return 是否已有运行任务
     */
    boolean hasRunningTask(String deviceId, String channelId);
}