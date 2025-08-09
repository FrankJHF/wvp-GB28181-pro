package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.service.IAnalysisTaskService;
import com.genersoft.iot.vmp.analysis.service.IAnalysisCardService;
import com.genersoft.iot.vmp.analysis.service.ITaskStateService;
import com.genersoft.iot.vmp.analysis.service.IVLMClientService;
import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.AnalysisCard;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobRequest;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse;
import com.genersoft.iot.vmp.storager.dao.AnalysisTaskMapper;
import com.genersoft.iot.vmp.conf.exception.ServiceException;

// 临时导入，实际应该从WVP现有服务中获取
// import com.genersoft.iot.vmp.gb28181.service.IDeviceService;
// import com.genersoft.iot.vmp.gb28181.bean.Device;
// import com.genersoft.iot.vmp.gb28181.bean.DeviceChannel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 分析任务管理服务实现类
 * @author Claude
 */
@Service
@Slf4j
public class AnalysisTaskServiceImpl implements IAnalysisTaskService {

    @Autowired
    private AnalysisTaskMapper analysisTaskMapper;

    @Autowired
    private IAnalysisCardService analysisCardService;

    @Autowired
    private ITaskStateService taskStateService;

    @Autowired
    private IVLMClientService vlmClientService;

    // TODO: 注入WVP现有的设备服务
    // @Autowired
    // private IDeviceService deviceService;

    @Value("${vlm.callback.base-url:http://localhost:18080}")
    private String callbackBaseUrl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisTask createTask(AnalysisTask task) {
        log.info("创建分析任务: {}", task.getTaskName());
        
        // 验证必填字段
        validateTask(task, true);
        
        // 检查是否可以创建任务
        if (!canCreateTask(task.getDeviceId(), task.getChannelId(), task.getAnalysisCardId())) {
            throw new ServiceException("该设备通道已存在相同类型的分析任务");
        }
        
        // 验证设备通道
        if (!validateDeviceChannel(task.getDeviceId(), task.getChannelId())) {
            throw new ServiceException("设备通道不存在或不可用");
        }
        
        // 获取分析卡片信息
        AnalysisCard card = analysisCardService.getCardById(task.getAnalysisCardId());
        if (card == null) {
            throw new ServiceException("分析卡片不存在");
        }
        if (!card.getEnabled()) {
            throw new ServiceException("分析卡片已被禁用");
        }
        
        // 获取RTSP流地址
        String rtspUrl = getDeviceChannelRtspUrl(task.getDeviceId(), task.getChannelId());
        if (StringUtils.isEmpty(rtspUrl)) {
            throw new ServiceException("无法获取设备通道的RTSP流地址");
        }
        
        // 生成ID和设置时间
        if (StringUtils.isEmpty(task.getId())) {
            task.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        task.setRtspUrl(rtspUrl);
        task.setStatus(TaskStatus.CREATED);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        // 创建VLM作业
        VLMJobRequest vlmRequest = createVLMJobRequest(task, card, rtspUrl);
        VLMJobResponse vlmResponse = vlmClientService.createJob(vlmRequest, false); // 不自动启动
        
        if (vlmResponse == null || !vlmResponse.isSuccess()) {
            throw new ServiceException("创建VLM作业失败: " + 
                    (vlmResponse != null ? vlmResponse.getMessage() : "VLM服务不可用"));
        }
        
        task.setVlmJobId(vlmResponse.getJobId());
        
        // 保存任务
        int result = analysisTaskMapper.insert(task);
        if (result <= 0) {
            throw new ServiceException("创建分析任务失败");
        }
        
        log.info("分析任务创建成功，任务ID: {}, VLM作业ID: {}", task.getId(), task.getVlmJobId());
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisTask updateTask(AnalysisTask task) {
        log.info("更新分析任务: {}", task.getId());
        
        // 验证必填字段
        validateTask(task, false);
        
        // 检查任务是否存在
        AnalysisTask existingTask = analysisTaskMapper.selectById(task.getId());
        if (existingTask == null) {
            throw new ServiceException("分析任务不存在，ID: " + task.getId());
        }
        
        // 检查任务是否可以更新（不能是正在执行的任务）
        if (existingTask.isTransitioning()) {
            throw new ServiceException("任务正在执行操作，无法更新");
        }
        
        int result = analysisTaskMapper.update(task);
        if (result <= 0) {
            throw new ServiceException("更新分析任务失败");
        }
        
        log.info("分析任务更新成功，ID: {}", task.getId());
        return analysisTaskMapper.selectById(task.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTask(String taskId) {
        log.info("删除分析任务: {}", taskId);
        
        if (StringUtils.isEmpty(taskId)) {
            throw new ServiceException("任务ID不能为空");
        }
        
        // 检查任务是否存在
        AnalysisTask task = analysisTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException("分析任务不存在，ID: " + taskId);
        }
        
        // 检查任务是否可以删除
        if (!task.canDelete()) {
            throw new ServiceException("任务状态不允许删除，请先停止任务");
        }
        
        // 停止VLM作业（如果存在）
        if (!StringUtils.isEmpty(task.getVlmJobId())) {
            try {
                vlmClientService.stopJob(task.getVlmJobId());
            } catch (Exception e) {
                log.warn("停止VLM作业失败，继续删除任务，VLM作业ID: {}", task.getVlmJobId(), e);
            }
        }
        
        int result = analysisTaskMapper.delete(taskId);
        if (result <= 0) {
            log.warn("分析任务删除失败，ID: {}", taskId);
            return false;
        }
        
        log.info("分析任务删除成功，ID: {}", taskId);
        return true;
    }

    @Override
    public AnalysisTask getTaskById(String taskId) {
        if (StringUtils.isEmpty(taskId)) {
            throw new ServiceException("任务ID不能为空");
        }
        
        return analysisTaskMapper.selectById(taskId);
    }

    @Override
    public AnalysisTask getTaskWithDetailsById(String taskId) {
        AnalysisTask task = getTaskById(taskId);
        if (task != null) {
            // 加载关联的分析卡片信息
            AnalysisCard card = analysisCardService.getCardById(task.getAnalysisCardId());
            task.setAnalysisCard(card);
        }
        return task;
    }

    @Override
    public AnalysisTask getTaskByVlmJobId(String vlmJobId) {
        if (StringUtils.isEmpty(vlmJobId)) {
            throw new ServiceException("VLM作业ID不能为空");
        }
        
        return analysisTaskMapper.selectByVlmJobId(vlmJobId);
    }

    @Override
    public PageInfo<AnalysisTask> getTaskPage(int pageNum, int pageSize, String deviceId, String channelId,
                                             String analysisCardId, String status, String createdBy, String taskName) {
        log.debug("分页查询分析任务，页码: {}, 页面大小: {}", pageNum, pageSize);
        
        PageHelper.startPage(pageNum, pageSize);
        List<AnalysisTask> tasks = analysisTaskMapper.selectAll(deviceId, channelId, 
                analysisCardId, status, createdBy, taskName);
        
        return new PageInfo<>(tasks);
    }

    @Override
    public List<AnalysisTask> getTasksByDeviceAndChannel(String deviceId, String channelId) {
        if (StringUtils.isEmpty(deviceId) || StringUtils.isEmpty(channelId)) {
            throw new ServiceException("设备ID和通道ID不能为空");
        }
        
        return analysisTaskMapper.selectByDeviceAndChannel(deviceId, channelId);
    }

    @Override
    public List<AnalysisTask> getTasksByStatuses(List<TaskStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        
        List<String> statusValues = statuses.stream()
                .map(TaskStatus::getValue)
                .collect(Collectors.toList());
        
        return analysisTaskMapper.selectByStatuses(statusValues);
    }

    @Override
    public CompletableFuture<Void> startTask(String taskId, boolean forceRestart) {
        return taskStateService.startTask(taskId, forceRestart);
    }

    @Override
    public CompletableFuture<Void> pauseTask(String taskId) {
        return taskStateService.pauseTask(taskId);
    }

    @Override
    public CompletableFuture<Void> resumeTask(String taskId) {
        return taskStateService.resumeTask(taskId);
    }

    @Override
    public CompletableFuture<Void> stopTask(String taskId) {
        return taskStateService.stopTask(taskId);
    }

    @Override
    public int syncAllActiveTaskStatuses() {
        return taskStateService.syncAllActiveTaskStatuses();
    }

    @Override
    public int checkAndSyncStaleTaskStatuses(int thresholdMinutes) {
        return taskStateService.checkAndSyncStaleTaskStatuses(thresholdMinutes);
    }

    @Override
    public long countTasks(String deviceId, String status, String createdBy) {
        return analysisTaskMapper.count(deviceId, status, createdBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDeleteTasks(List<String> taskIds) {
        log.info("批量删除分析任务，数量: {}", taskIds.size());
        
        if (taskIds == null || taskIds.isEmpty()) {
            throw new ServiceException("任务ID列表不能为空");
        }
        
        int deleteCount = 0;
        for (String taskId : taskIds) {
            try {
                if (deleteTask(taskId)) {
                    deleteCount++;
                }
            } catch (Exception e) {
                log.error("删除任务失败，任务ID: {}", taskId, e);
            }
        }
        
        log.info("批量删除分析任务完成，成功数量: {}", deleteCount);
        return deleteCount;
    }

    @Override
    public boolean canCreateTask(String deviceId, String channelId, String analysisCardId) {
        if (StringUtils.isEmpty(deviceId) || StringUtils.isEmpty(channelId) || StringUtils.isEmpty(analysisCardId)) {
            return false;
        }
        
        // 检查是否存在相同设备通道和分析卡片的任务
        List<AnalysisTask> existingTasks = analysisTaskMapper.selectByDeviceAndChannel(deviceId, channelId);
        
        return existingTasks.stream()
                .noneMatch(task -> analysisCardId.equals(task.getAnalysisCardId()) && 
                          !task.isTerminated());
    }

    @Override
    public String getDeviceChannelRtspUrl(String deviceId, String channelId) {
        // TODO: 集成WVP现有的设备服务获取RTSP流地址
        // 这里应该调用WVP现有的设备服务来获取真实的RTSP地址
        // Device device = deviceService.getDevice(deviceId);
        // DeviceChannel channel = deviceService.getChannel(deviceId, channelId);
        // return playService.getRtspUrl(device, channel);
        
        // 临时实现：生成模拟的RTSP地址
        log.warn("使用模拟RTSP地址，生产环境需要集成真实的设备服务");
        return String.format("rtsp://192.168.1.100:554/device/%s/channel/%s", deviceId, channelId);
    }

    @Override
    public boolean validateDeviceChannel(String deviceId, String channelId) {
        // TODO: 集成WVP现有的设备服务验证设备通道
        // Device device = deviceService.getDevice(deviceId);
        // if (device == null || !device.getOnLine()) {
        //     return false;
        // }
        // DeviceChannel channel = deviceService.getChannel(deviceId, channelId);
        // return channel != null && "ON".equals(channel.getStatus());
        
        // 临时实现：假设所有设备通道都有效
        log.warn("使用模拟设备通道验证，生产环境需要集成真实的设备服务");
        return !StringUtils.isEmpty(deviceId) && !StringUtils.isEmpty(channelId);
    }

    /**
     * 创建VLM作业请求
     */
    private VLMJobRequest createVLMJobRequest(AnalysisTask task, AnalysisCard card, String rtspUrl) {
        VLMJobRequest request = new VLMJobRequest();
        request.setDeviceId(task.getDeviceId());
        request.setChannelId(task.getChannelId());
        request.setInputData(rtspUrl);
        request.setCallbackUrl(callbackBaseUrl + "/api/vlm/callback");
        request.setAnalysisPrompt(card.getPrompt());
        request.setModelName(card.getModelType());
        request.setAnalysisConfig(card.getAnalysisConfig());
        request.setAutoStart(false);
        
        return request;
    }

    /**
     * 验证分析任务数据
     */
    private void validateTask(AnalysisTask task, boolean isCreate) {
        if (task == null) {
            throw new ServiceException("分析任务信息不能为空");
        }
        
        if (StringUtils.isEmpty(task.getTaskName())) {
            throw new ServiceException("任务名称不能为空");
        }
        
        if (task.getTaskName().length() > 100) {
            throw new ServiceException("任务名称长度不能超过100个字符");
        }
        
        if (StringUtils.isEmpty(task.getAnalysisCardId())) {
            throw new ServiceException("分析卡片ID不能为空");
        }
        
        if (StringUtils.isEmpty(task.getDeviceId())) {
            throw new ServiceException("设备ID不能为空");
        }
        
        if (StringUtils.isEmpty(task.getChannelId())) {
            throw new ServiceException("通道ID不能为空");
        }
        
        if (isCreate && StringUtils.isEmpty(task.getCreatedBy())) {
            throw new ServiceException("创建人不能为空");
        }
    }
}