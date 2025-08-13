package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.bean.AnalysisCard;
import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.VLMStatusMapper;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobRequest;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse;
import com.genersoft.iot.vmp.analysis.service.IAnalysisCardService;
import com.genersoft.iot.vmp.analysis.service.IAnalysisTaskService;
import com.genersoft.iot.vmp.analysis.service.ITaskStateService;
import com.genersoft.iot.vmp.analysis.service.IVLMClientService;
import com.genersoft.iot.vmp.common.StreamInfo;
import com.genersoft.iot.vmp.conf.exception.ServiceException;
import com.genersoft.iot.vmp.gb28181.bean.Device;
import com.genersoft.iot.vmp.gb28181.bean.DeviceChannel;
import com.genersoft.iot.vmp.gb28181.service.IDeviceChannelService;
import com.genersoft.iot.vmp.gb28181.service.IDeviceService;
import com.genersoft.iot.vmp.gb28181.service.IPlayService;
import com.genersoft.iot.vmp.storager.dao.AnalysisTaskMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IDeviceChannelService deviceChannelService;

    @Autowired
    private IPlayService playService;

    @Value("${vlm.callback.base-url:http://localhost:18080}")
    private String callbackBaseUrl;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnalysisTask createTask(AnalysisTask task) throws ServiceException {
        log.info("创建分析任务: {}", task.getTaskName());

        // 验证必填字段
        validateTask(task, true);

        // 检查是否可以创建任务
        if (!canCreateTask(task.getDeviceId(), task.getChannelId(), task.getAnalysisCardId())) {
            throw new ServiceException("该设备通道已存在相同类型的分析任务");
        }

        // 验证设备通道
        Device device = deviceService.getDeviceByDeviceId(task.getDeviceId());
        DeviceChannel channel = deviceChannelService.getOne(task.getDeviceId(), task.getChannelId());
        if (device == null || channel == null) {
            throw new ServiceException("设备或通道不存在");
        }
        if (!device.isOnLine() || !"ON".equals(channel.getStatus())) {
            throw new ServiceException("设备或通道不可用");
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
        if (rtspUrl == null || rtspUrl.trim().isEmpty()) {
            throw new ServiceException("无法获取设备通道的RTSP流地址");
        }

        // 填充任务信息
        if (task.getId() == null || task.getId().trim().isEmpty()) {
            task.setId(UUID.randomUUID().toString().replace("-", ""));
        }
        task.setDeviceName(device.getName());
        task.setChannelName(channel.getName());
        task.setRtspUrl(rtspUrl);
        task.setStatus(TaskStatus.CREATED);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());

        // 创建VLM作业
        VLMJobRequest vlmRequest = createVLMJobRequest(task, card, rtspUrl);
        boolean autoStart = task.getAutoStart() != null ? task.getAutoStart() : false;
        VLMJobResponse vlmResponse = vlmClientService.createJob(vlmRequest, autoStart);

        if (vlmResponse == null || !vlmResponse.isSuccess()) {
            throw new ServiceException("创建VLM作业失败: " +
                    (vlmResponse != null ? vlmResponse.getMessage() : "VLM服务不可用"));
        }

        task.setVlmJobId(vlmResponse.getJobId());

        // 根据VLM服务返回的状态更新任务状态
        TaskStatus finalStatus = VLMStatusMapper.mapVLMStatusToTaskStatus(vlmResponse.getStatus());
        if (finalStatus != null) {
            task.setStatus(finalStatus);
            log.info("根据VLM响应更新任务状态: {} -> {}", vlmResponse.getStatus(), finalStatus.getDescription());
        } else {
            // 如果无法映射VLM状态，保持CREATED状态
            log.warn("无法映射VLM状态 '{}'，保持CREATED状态", vlmResponse.getStatus());
        }

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
    public AnalysisTask updateTask(AnalysisTask task) throws ServiceException {
        log.info("更新分析任务: {}", task.getId());

        // 验证必填字段
        validateTask(task, false);

        // 检查任务是否存在
        AnalysisTask existingTask = analysisTaskMapper.selectById(task.getId());
        if (existingTask == null) {
            throw new ServiceException("分析任务不存在，ID: " + task.getId());
        }

        // 检查任务是否可以更新（不能是活跃状态的任务）
        if (existingTask.isActive()) {
            throw new ServiceException("任务正在运行或暂停中，无法更新");
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
    public boolean deleteTask(String taskId) throws ServiceException {
        log.info("删除分析任务: {}", taskId);

        if (taskId == null || taskId.trim().isEmpty()) {
            throw new ServiceException("任务ID不能为空");
        }

        // 检查任务是否存在
        AnalysisTask task = analysisTaskMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException("分析任务不存在，ID: " + taskId);
        }

        // 检查任务是否可以删除（只有终态任务可以删除）
        if (!task.canDelete()) {
            throw new ServiceException("只能删除已失败或已取消的任务");
        }

        // 对于终态任务，不调用VLM服务，直接删除数据库记录
        // VLM的终态任务（failed/cancelled）不能执行状态转换，调用DELETE会返回409错误

        int result = analysisTaskMapper.delete(taskId);
        if (result <= 0) {
            log.warn("分析任务删除失败，ID: {}", taskId);
            return false;
        }

        log.info("分析任务删除成功，ID: {}", taskId);
        return true;
    }

    @Override
    public AnalysisTask getTaskById(String taskId) throws ServiceException {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new ServiceException("任务ID不能为空");
        }

        return analysisTaskMapper.selectById(taskId);
    }

    @Override
    public AnalysisTask getTaskWithDetailsById(String taskId) throws ServiceException {
        AnalysisTask task = getTaskById(taskId);
        if (task != null) {
            // 加载关联的分析卡片信息
            AnalysisCard card = analysisCardService.getCardById(task.getAnalysisCardId());
            task.setAnalysisCard(card);
        }
        return task;
    }

    @Override
    public AnalysisTask getTaskByVlmJobId(String vlmJobId) throws ServiceException {
        if (vlmJobId == null || vlmJobId.trim().isEmpty()) {
            throw new ServiceException("VLM作业ID不能为空");
        }

        return analysisTaskMapper.selectByVlmJobId(vlmJobId);
    }

    @Override
    public PageInfo<AnalysisTask> getTaskPage(int pageNum, int pageSize, String deviceId, String channelId,
                                             String analysisCardId, String status, String createdBy, String taskName) throws ServiceException {
        log.debug("分页查询分析任务，页码: {}, 页面大小: {}", pageNum, pageSize);

        PageHelper.startPage(pageNum, pageSize);
        List<AnalysisTask> tasks = analysisTaskMapper.selectAll(deviceId, channelId,
                analysisCardId, status, createdBy, taskName);

        // 填充关联信息
        for (AnalysisTask task : tasks) {
            if (task.getAnalysisCardId() != null) {
                AnalysisCard card = analysisCardService.getCardById(task.getAnalysisCardId());
                task.setAnalysisCard(card);
            }
        }

        return new PageInfo<>(tasks);
    }

    @Override
    public List<AnalysisTask> getTasksByDeviceAndChannel(String deviceId, String channelId) throws ServiceException {
        if (deviceId == null || deviceId.trim().isEmpty() || channelId == null || channelId.trim().isEmpty()) {
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
    public CompletableFuture<Void> cancelTask(String taskId) {
        return taskStateService.cancelTask(taskId);
    }

    @Override
    @Deprecated
    public CompletableFuture<Void> stopTask(String taskId) {
        // 为了向后兼容，委托给cancelTask
        return cancelTask(taskId);
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
    public int batchDeleteTasks(List<String> taskIds) throws ServiceException {
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
    public boolean canCreateTask(String deviceId, String channelId, String analysisCardId) throws ServiceException {
        if (deviceId == null || deviceId.trim().isEmpty() || channelId == null || channelId.trim().isEmpty() || analysisCardId == null || analysisCardId.trim().isEmpty()) {
            return false;
        }

        // 检查是否存在相同设备通道和分析卡片的任务
        List<AnalysisTask> existingTasks = analysisTaskMapper.selectByDeviceAndChannel(deviceId, channelId);

        return existingTasks.stream()
                .noneMatch(task -> analysisCardId.equals(task.getAnalysisCardId()) &&
                          !task.isTerminated());
    }

    @Override
    public String getDeviceChannelRtspUrl(String deviceId, String channelId) throws ServiceException {
        Device device = deviceService.getDeviceByDeviceId(deviceId);
        if (device == null) {
            throw new ServiceException("设备不存在");
        }
        DeviceChannel channel = deviceChannelService.getOne(deviceId, channelId);
        if (channel == null) {
            throw new ServiceException("设备通道不存在");
        }

        CompletableFuture<StreamInfo> future = new CompletableFuture<>();
        playService.play(device, channel, (code, msg, streamInfo) -> {
            if (code == 0 && streamInfo != null) {
                future.complete(streamInfo);
            } else {
                future.completeExceptionally(new ServiceException(msg));
            }
        });

        try {
            StreamInfo streamInfo = future.get(15, TimeUnit.SECONDS); // 设置15秒超时
            if (streamInfo.getRtsp() != null) {
                return streamInfo.getRtsp().getUrl();
            } else {
                throw new ServiceException("获取RTSP地址失败: 流信息中无RTSP地址");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("获取RTSP地址失败", e);
            throw new ServiceException("获取RTSP地址失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validateDeviceChannel(String deviceId, String channelId) throws ServiceException {
        Device device = deviceService.getDeviceByDeviceId(deviceId);
        if (device == null || !device.isOnLine()) {
            log.warn("[设备通道验证] 设备不存在或离线, deviceId: {}", deviceId);
            return false;
        }
        DeviceChannel channel = deviceChannelService.getOne(deviceId, channelId);
        boolean result = channel != null && "ON".equals(channel.getStatus());
        if (!result) {
            log.warn("[设备通道验证] 通道不存在或离线, deviceId: {}, channelId: {}", deviceId, channelId);
        }
        return result;
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
        
        // 使用任务的分析配置，如果没有配置则使用默认值
        Map<String, Object> analysisConfig = task.getConfig();
        if (analysisConfig == null || analysisConfig.isEmpty()) {
            // 设置默认配置
            analysisConfig = new java.util.HashMap<>();
            analysisConfig.put("inference_interval", 30);
            analysisConfig.put("frame_buffer_size", 30);
            analysisConfig.put("sampling_fps", 1);
            analysisConfig.put("max_new_tokens", 200);
        }
        request.setAnalysisConfig(analysisConfig);

        return request;
    }

    /**
     * 验证分析任务数据
     */
    private void validateTask(AnalysisTask task, boolean isCreate) throws ServiceException {
        if (task == null) {
            throw new ServiceException("分析任务信息不能为空");
        }

        if (task.getTaskName() == null || task.getTaskName().trim().isEmpty()) {
            throw new ServiceException("任务名称不能为空");
        }

        if (task.getTaskName().length() > 100) {
            throw new ServiceException("任务名称长度不能超过100个字符");
        }

        if (task.getAnalysisCardId() == null || task.getAnalysisCardId().trim().isEmpty()) {
            throw new ServiceException("分析卡片ID不能为空");
        }

        if (task.getDeviceId() == null || task.getDeviceId().trim().isEmpty()) {
            throw new ServiceException("设备ID不能为空");
        }

        if (task.getChannelId() == null || task.getChannelId().trim().isEmpty()) {
            throw new ServiceException("通道ID不能为空");
        }

        if (isCreate && (task.getCreatedBy() == null || task.getCreatedBy().trim().isEmpty())) {
            throw new ServiceException("创建人不能为空");
        }
    }

    @Override
    public boolean updateLastActiveTime(String taskId) throws ServiceException {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new ServiceException("任务ID不能为空");
        }

        log.debug("更新任务最后活跃时间: {}", taskId);
        
        int result = analysisTaskMapper.updateLastActiveTime(taskId);
        if (result <= 0) {
            log.warn("更新任务最后活跃时间失败，任务可能不存在: {}", taskId);
            return false;
        }

        log.debug("任务最后活跃时间更新成功: {}", taskId);
        return true;
    }
}