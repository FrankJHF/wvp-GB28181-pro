package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.entity.AnalysisTask;
import com.genersoft.iot.vmp.analysis.mapper.AnalysisTaskMapper;
import com.genersoft.iot.vmp.analysis.dto.TaskCreateRequest;
import com.genersoft.iot.vmp.analysis.dto.TaskUpdateRequest;
import com.genersoft.iot.vmp.analysis.dto.DeviceChannelResponse;
import com.genersoft.iot.vmp.analysis.service.IAnalysisTaskService;
import com.genersoft.iot.vmp.analysis.service.IVlmMicroserviceClient;
import com.genersoft.iot.vmp.gb28181.service.IDeviceService;
import com.genersoft.iot.vmp.gb28181.service.IDeviceChannelService;
import com.genersoft.iot.vmp.gb28181.bean.Device;
import com.genersoft.iot.vmp.gb28181.bean.DeviceChannel;
import com.genersoft.iot.vmp.web.gb28181.dto.DeviceChannelExtend;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分析任务服务实现类
 */
@Service
public class AnalysisTaskServiceImpl implements IAnalysisTaskService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisTaskServiceImpl.class);

    @Autowired
    private AnalysisTaskMapper taskMapper;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IDeviceChannelService deviceChannelService;

    @Autowired
    private IVlmMicroserviceClient vlmMicroserviceClient;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public Integer createTask(TaskCreateRequest request) {
        logger.info("创建分析任务: {}", request);

        // 1. 验证设备通道是否存在和有效
        if (!validateTaskConfig(request.getDeviceId(), request.getChannelId())) {
            throw new RuntimeException("设备或通道不存在或不可用");
        }

        // 2. 检查是否已有运行中的任务
        if (hasRunningTask(request.getDeviceId(), request.getChannelId())) {
            throw new RuntimeException("该设备通道已有运行中的分析任务");
        }

        // 3. 创建任务实体
        AnalysisTask task = new AnalysisTask();
        task.setTaskName(request.getTaskName());
        task.setDeviceId(request.getDeviceId());
        task.setChannelId(request.getChannelId());
        task.setAnalysisQuestion(request.getAnalysisQuestion());
        task.setAnalysisInterval(request.getAnalysisInterval());
        task.setTaskDescription(request.getTaskDescription());
        task.setTaskStatus("STOPPED"); // 默认停止状态

        String currentTime = dateFormat.format(new Date());
        task.setCreateTime(currentTime);
        task.setUpdateTime(currentTime);

        // 4. 保存到数据库
        int result = taskMapper.insertTask(task);
        if (result <= 0) {
            throw new RuntimeException("创建任务失败");
        }

        // 5. 如果需要自动启动，则启动任务
        if (Boolean.TRUE.equals(request.getAutoStart())) {
            startTask(task.getId());
        }

        logger.info("成功创建分析任务，ID: {}", task.getId());
        return task.getId();
    }

    @Override
    @Transactional
    public boolean updateTask(Integer taskId, TaskUpdateRequest request) {
        logger.info("更新分析任务 {}: {}", taskId, request);

        AnalysisTask existingTask = taskMapper.selectTaskById(taskId);
        if (existingTask == null) {
            throw new RuntimeException("任务不存在");
        }

        // 运行中的任务不允许修改核心参数
        if ("RUNNING".equals(existingTask.getTaskStatus())) {
            throw new RuntimeException("运行中的任务不允许修改配置，请先停止任务");
        }

        // 更新任务信息
        AnalysisTask updateTask = new AnalysisTask();
        updateTask.setId(taskId);
        updateTask.setUpdateTime(dateFormat.format(new Date()));

        if (StringUtils.hasText(request.getTaskName())) {
            updateTask.setTaskName(request.getTaskName());
        }
        if (StringUtils.hasText(request.getAnalysisQuestion())) {
            updateTask.setAnalysisQuestion(request.getAnalysisQuestion());
        }
        if (request.getAnalysisInterval() != null) {
            updateTask.setAnalysisInterval(request.getAnalysisInterval());
        }
        if (request.getTaskDescription() != null) {
            updateTask.setTaskDescription(request.getTaskDescription());
        }

        int result = taskMapper.updateTask(updateTask);
        if (result > 0) {
            logger.info("成功更新分析任务: {}", taskId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean deleteTask(Integer taskId) {
        logger.info("删除分析任务: {}", taskId);

        AnalysisTask task = taskMapper.selectTaskById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 如果任务正在运行，先停止
        if ("RUNNING".equals(task.getTaskStatus())) {
            stopTask(taskId);
        }

        int result = taskMapper.deleteTask(taskId);
        if (result > 0) {
            logger.info("成功删除分析任务: {}", taskId);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean startTask(Integer taskId) {
        logger.info("启动分析任务: {}", taskId);

        AnalysisTask task = taskMapper.selectTaskById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if ("RUNNING".equals(task.getTaskStatus())) {
            logger.warn("任务 {} 已在运行中", taskId);
            return true;
        }

        // 再次验证设备通道可用性
        if (!validateTaskConfig(task.getDeviceId(), task.getChannelId())) {
            throw new RuntimeException("设备或通道不可用，无法启动任务");
        }

        try {
            // 调用VLM微服务启动分析
            boolean vlmResult = vlmMicroserviceClient.startAnalysisTask(task);
            if (!vlmResult) {
                throw new RuntimeException("VLM微服务启动失败");
            }

            // 更新任务状态
            AnalysisTask updateTask = new AnalysisTask();
            updateTask.setId(taskId);
            updateTask.setTaskStatus("RUNNING");
            updateTask.setStartTime(dateFormat.format(new Date()));
            updateTask.setUpdateTime(dateFormat.format(new Date()));

            int result = taskMapper.updateTask(updateTask);
            if (result > 0) {
                logger.info("成功启动分析任务: {}", taskId);
                return true;
            }
            return false;

        } catch (Exception e) {
            logger.error("启动分析任务失败: {}", taskId, e);
            throw new RuntimeException("启动任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean stopTask(Integer taskId) {
        logger.info("停止分析任务: {}", taskId);

        AnalysisTask task = taskMapper.selectTaskById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if ("STOPPED".equals(task.getTaskStatus())) {
            logger.warn("任务 {} 已停止", taskId);
            return true;
        }

        try {
            // 调用VLM微服务停止分析
            boolean vlmResult = vlmMicroserviceClient.stopAnalysisTask(taskId);
            if (!vlmResult) {
                logger.warn("VLM微服务停止失败，但继续更新本地状态");
            }

            // 更新任务状态
            AnalysisTask updateTask = new AnalysisTask();
            updateTask.setId(taskId);
            updateTask.setTaskStatus("STOPPED");
            updateTask.setStopTime(dateFormat.format(new Date()));
            updateTask.setUpdateTime(dateFormat.format(new Date()));

            int result = taskMapper.updateTask(updateTask);
            if (result > 0) {
                logger.info("成功停止分析任务: {}", taskId);
                return true;
            }
            return false;

        } catch (Exception e) {
            logger.error("停止分析任务失败: {}", taskId, e);
            throw new RuntimeException("停止任务失败: " + e.getMessage());
        }
    }

    @Override
    public AnalysisTask getTask(Integer taskId) {
        return taskMapper.selectTaskById(taskId);
    }

    @Override
    public PageInfo<AnalysisTask> getTasks(int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<AnalysisTask> tasks = taskMapper.selectAllTasks();
        return new PageInfo<>(tasks);
    }

    @Override
    public AnalysisTask getTaskByDeviceChannel(String deviceId, String channelId) {
        return taskMapper.selectTaskByDeviceChannel(deviceId, channelId);
    }

    @Override
    public List<AnalysisTask> getRunningTasks() {
        return taskMapper.selectTasksByStatus("RUNNING");
    }

    @Override
    public List<DeviceChannelResponse.DeviceInfo> getAnalysisDevices() {
        // 获取所有设备（可配置为仅在线设备）
        List<Device> devices = deviceService.getAllByStatus(null); // null表示所有设备

        return devices.stream().map(device -> {
            DeviceChannelResponse.DeviceInfo deviceInfo = new DeviceChannelResponse.DeviceInfo();
            deviceInfo.setDeviceId(device.getDeviceId());
            deviceInfo.setDeviceName(device.getName());
            deviceInfo.setStatus(device.isOnLine() ? "ON" : "OFF");
            deviceInfo.setLastOnlineTime(device.getKeepaliveTime());
            deviceInfo.setDeviceType(device.getModel());
            return deviceInfo;
        }).collect(Collectors.toList());
    }

        @Override
    public List<DeviceChannelResponse.ChannelInfo> getDeviceChannels(String deviceId) {
        try {
            // 获取设备信息
            Device device = deviceService.getDeviceByDeviceId(deviceId);
            if (device == null) {
                return new ArrayList<>();
            }

            // 获取设备通道列表（使用现有的WVP通道查询接口）
            List<DeviceChannel> channels = deviceChannelService.queryChaneListByDeviceId(deviceId);
            if (channels == null || channels.isEmpty()) {
                // 如果没有通道，为设备创建一个默认通道
                DeviceChannelResponse.ChannelInfo defaultChannel = new DeviceChannelResponse.ChannelInfo();
                defaultChannel.setChannelId(deviceId); // 使用设备ID作为通道ID
                defaultChannel.setChannelName("默认通道");
                defaultChannel.setStatus(device.isOnLine() ? "ON" : "OFF");

                // 设置支持的协议
                List<String> protocols = Arrays.asList("RTSP", "RTMP", "FLV", "HLS", "WebSocket");
                defaultChannel.setSupportedProtocols(protocols);
                defaultChannel.setAnalysisSupported(true);

                return Arrays.asList(defaultChannel);
            }

            return channels.stream().map(channel -> {
                DeviceChannelResponse.ChannelInfo channelInfo = new DeviceChannelResponse.ChannelInfo();
                // 基于技术要点，我们使用设备ID作为通道ID
                channelInfo.setChannelId(deviceId);
                channelInfo.setChannelName(channel.getName());
                channelInfo.setStatus(channel.getStatus());

                // 设置支持的协议
                List<String> protocols = Arrays.asList("RTSP", "RTMP", "FLV", "HLS", "WebSocket");
                channelInfo.setSupportedProtocols(protocols);
                channelInfo.setAnalysisSupported(true);

                return channelInfo;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("获取设备通道列表失败: {}", deviceId, e);
            // 如果出错，至少返回设备本身作为通道
            DeviceChannelResponse.ChannelInfo defaultChannel = new DeviceChannelResponse.ChannelInfo();
            defaultChannel.setChannelId(deviceId);
            defaultChannel.setChannelName("设备通道");
            defaultChannel.setStatus("UNKNOWN");
            defaultChannel.setSupportedProtocols(Arrays.asList("RTSP", "RTMP", "FLV", "HLS"));
            defaultChannel.setAnalysisSupported(true);
            return Arrays.asList(defaultChannel);
        }
    }

    @Override
    public boolean validateTaskConfig(String deviceId, String channelId) {
        try {
            // 1. 验证设备是否存在
            Device device = deviceService.getDeviceByDeviceId(deviceId);
            if (device == null) {
                logger.warn("设备不存在: {}", deviceId);
                return false;
            }

            // 2. 验证通道是否存在（基于我们的发现，通道ID通常是设备ID）
            if (!StringUtils.hasText(channelId)) {
                logger.warn("通道ID为空");
                return false;
            }

            // 3. 检查VLM微服务连接状态
            if (!vlmMicroserviceClient.testConnection()) {
                logger.warn("VLM微服务连接失败");
                return false;
            }

            return true;

        } catch (Exception e) {
            logger.error("验证任务配置失败: deviceId={}, channelId={}", deviceId, channelId, e);
            return false;
        }
    }

    @Override
    public boolean hasRunningTask(String deviceId, String channelId) {
        AnalysisTask runningTask = taskMapper.selectTaskByDeviceChannel(deviceId, channelId);
        return runningTask != null && "RUNNING".equals(runningTask.getTaskStatus());
    }
}