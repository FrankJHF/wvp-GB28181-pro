package com.genersoft.iot.vmp.analysis.controller;

import com.genersoft.iot.vmp.analysis.entity.AnalysisTask;
import com.genersoft.iot.vmp.analysis.dto.TaskCreateRequest;
import com.genersoft.iot.vmp.analysis.dto.TaskUpdateRequest;
import com.genersoft.iot.vmp.analysis.dto.DeviceChannelResponse;
import com.genersoft.iot.vmp.analysis.service.IAnalysisTaskService;
import com.genersoft.iot.vmp.vmanager.bean.WVPResult;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 智能分析任务管理控制器
 */
@RestController
@RequestMapping("/api/analysis/tasks")
public class AnalysisTaskController {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisTaskController.class);

    @Autowired
    private IAnalysisTaskService analysisTaskService;

    /**
     * 创建分析任务
     */
    @PostMapping
    public WVPResult<Integer> createTask(@Valid @RequestBody TaskCreateRequest request) {
        try {
            logger.info("创建分析任务: {}", request);
            Integer taskId = analysisTaskService.createTask(request);
            return WVPResult.success(taskId, "任务创建成功");
        } catch (Exception e) {
            logger.error("创建分析任务失败", e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 查询任务列表（分页）
     */
    @GetMapping
    public WVPResult<PageInfo<AnalysisTask>> getTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            logger.info("查询任务列表: page={}, pageSize={}", page, pageSize);
            PageInfo<AnalysisTask> pageInfo = analysisTaskService.getTasks(page, pageSize);
            return WVPResult.success(pageInfo, "查询成功");
        } catch (Exception e) {
            logger.error("查询任务列表失败", e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{taskId}")
    public WVPResult<AnalysisTask> getTask(@PathVariable Integer taskId) {
        try {
            logger.info("获取任务详情: {}", taskId);
            AnalysisTask task = analysisTaskService.getTask(taskId);
            if (task == null) {
                return WVPResult.fail(-1, "任务不存在");
            }
            return WVPResult.success(task, "查询成功");
        } catch (Exception e) {
            logger.error("获取任务详情失败: {}", taskId, e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 更新任务配置
     */
    @PutMapping("/{taskId}")
    public WVPResult<Boolean> updateTask(
            @PathVariable Integer taskId,
            @Valid @RequestBody TaskUpdateRequest request) {
        try {
            logger.info("更新任务配置: taskId={}, request={}", taskId, request);
            boolean result = analysisTaskService.updateTask(taskId, request);
            return WVPResult.success(result, "任务更新成功");
        } catch (Exception e) {
            logger.error("更新任务配置失败: {}", taskId, e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 启动任务
     */
    @PostMapping("/{taskId}/start")
    public WVPResult<Boolean> startTask(@PathVariable Integer taskId) {
        try {
            logger.info("启动分析任务: {}", taskId);
            boolean result = analysisTaskService.startTask(taskId);
            return WVPResult.success(result, "任务启动成功");
        } catch (Exception e) {
            logger.error("启动分析任务失败: {}", taskId, e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 停止任务
     */
    @PostMapping("/{taskId}/stop")
    public WVPResult<Boolean> stopTask(@PathVariable Integer taskId) {
        try {
            logger.info("停止分析任务: {}", taskId);
            boolean result = analysisTaskService.stopTask(taskId);
            return WVPResult.success(result, "任务停止成功");
        } catch (Exception e) {
            logger.error("停止分析任务失败: {}", taskId, e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{taskId}")
    public WVPResult<Boolean> deleteTask(@PathVariable Integer taskId) {
        try {
            logger.info("删除分析任务: {}", taskId);
            boolean result = analysisTaskService.deleteTask(taskId);
            return WVPResult.success(result, "任务删除成功");
        } catch (Exception e) {
            logger.error("删除分析任务失败: {}", taskId, e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 获取可用于分析的设备列表
     */
    @GetMapping("/devices")
    public WVPResult<List<DeviceChannelResponse.DeviceInfo>> getAnalysisDevices() {
        try {
            logger.info("获取可分析设备列表");
            List<DeviceChannelResponse.DeviceInfo> devices = analysisTaskService.getAnalysisDevices();
            return WVPResult.success(devices, "查询成功");
        } catch (Exception e) {
            logger.error("获取可分析设备列表失败", e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 获取指定设备的通道列表
     */
    @GetMapping("/devices/{deviceId}/channels")
    public WVPResult<List<DeviceChannelResponse.ChannelInfo>> getDeviceChannels(
            @PathVariable String deviceId) {
        try {
            logger.info("获取设备通道列表: {}", deviceId);
            List<DeviceChannelResponse.ChannelInfo> channels =
                analysisTaskService.getDeviceChannels(deviceId);
            return WVPResult.success(channels, "查询成功");
        } catch (Exception e) {
            logger.error("获取设备通道列表失败: {}", deviceId, e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 验证任务配置
     */
    @PostMapping("/validate")
    public WVPResult<Boolean> validateTaskConfig(
            @RequestParam String deviceId,
            @RequestParam String channelId) {
        try {
            logger.info("验证任务配置: deviceId={}, channelId={}", deviceId, channelId);
            boolean isValid = analysisTaskService.validateTaskConfig(deviceId, channelId);
            return WVPResult.success(isValid, isValid ? "配置有效" : "配置无效");
        } catch (Exception e) {
            logger.error("验证任务配置失败: deviceId={}, channelId={}", deviceId, channelId, e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 获取运行中的任务列表
     */
    @GetMapping("/running")
    public WVPResult<List<AnalysisTask>> getRunningTasks() {
        try {
            logger.info("获取运行中的任务列表");
            List<AnalysisTask> runningTasks = analysisTaskService.getRunningTasks();
            return WVPResult.success(runningTasks, "查询成功");
        } catch (Exception e) {
            logger.error("获取运行中的任务列表失败", e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }
}