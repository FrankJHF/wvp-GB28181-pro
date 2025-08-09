package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.service.ITaskStateService;
import com.genersoft.iot.vmp.analysis.service.IVLMClientService;
import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.TaskAction;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobActionResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse;
import com.genersoft.iot.vmp.storager.dao.AnalysisTaskMapper;
import com.genersoft.iot.vmp.conf.exception.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务状态管理服务实现类
 * @author Claude
 */
@Service
@Slf4j
public class TaskStateServiceImpl implements ITaskStateService {

    @Autowired
    private AnalysisTaskMapper analysisTaskMapper;

    @Autowired
    private IVLMClientService vlmClientService;

    // 任务操作锁，防止并发操作同一任务
    private final ConcurrentHashMap<String, ReentrantLock> taskLocks = new ConcurrentHashMap<>();

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> startTask(String taskId, boolean forceRestart) {
        return CompletableFuture.runAsync(() -> {
            performTaskAction(taskId, TaskAction.START, forceRestart);
        });
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> pauseTask(String taskId) {
        return CompletableFuture.runAsync(() -> {
            performTaskAction(taskId, TaskAction.PAUSE, false);
        });
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> resumeTask(String taskId) {
        return CompletableFuture.runAsync(() -> {
            performTaskAction(taskId, TaskAction.RESUME, false);
        });
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> stopTask(String taskId) {
        return CompletableFuture.runAsync(() -> {
            performTaskAction(taskId, TaskAction.STOP, false);
        });
    }

    @Override
    public TaskStatus syncTaskStatus(String taskId) {
        log.debug("同步任务状态，任务ID: {}", taskId);
        
        if (StringUtils.isEmpty(taskId)) {
            throw new ServiceException("任务ID不能为空");
        }
        
        ReentrantLock lock = taskLocks.computeIfAbsent(taskId, k -> new ReentrantLock());
        lock.lock();
        
        try {
            AnalysisTask task = analysisTaskMapper.selectById(taskId);
            if (task == null) {
                log.warn("任务不存在，ID: {}", taskId);
                return null;
            }
            
            if (StringUtils.isEmpty(task.getVlmJobId())) {
                log.debug("任务未关联VLM作业，跳过状态同步，任务ID: {}", taskId);
                return task.getStatus();
            }
            
            // 查询VLM作业状态
            VLMJobResponse vlmJob = vlmClientService.getJobStatus(task.getVlmJobId());
            if (vlmJob == null) {
                log.warn("VLM作业状态查询失败，任务ID: {}, VLM作业ID: {}", taskId, task.getVlmJobId());
                return task.getStatus();
            }
            
            // 状态映射和更新
            TaskStatus newStatus = mapVlmStatusToTaskStatus(vlmJob.getStatus());
            if (newStatus != task.getStatus()) {
                log.info("任务状态发生变化，任务ID: {}, 原状态: {}, 新状态: {}", 
                        taskId, task.getStatus(), newStatus);
                
                task.setStatus(newStatus);
                task.updateLastActiveTime();
                task.updateLastStatusSync();
                
                analysisTaskMapper.update(task);
            } else {
                // 即使状态没有变化，也更新同步时间
                task.updateLastStatusSync();
                analysisTaskMapper.update(task);
            }
            
            return newStatus;
            
        } catch (Exception e) {
            log.error("同步任务状态失败，任务ID: {}", taskId, e);
            return null;
        } finally {
            lock.unlock();
            // 清理锁（避免内存泄漏）
            if (!lock.hasQueuedThreads()) {
                taskLocks.remove(taskId);
            }
        }
    }

    @Override
    public int syncAllActiveTaskStatuses() {
        log.debug("开始同步所有活跃任务状态");
        
        // 查询需要同步的任务
        List<String> activeStatuses = List.of("starting", "running", "pausing", "paused", "resuming", "stopping");
        List<AnalysisTask> activeTasks = analysisTaskMapper.selectByStatuses(activeStatuses);
        
        int syncCount = 0;
        for (AnalysisTask task : activeTasks) {
            try {
                syncTaskStatus(task.getId());
                syncCount++;
            } catch (Exception e) {
                log.error("同步任务状态失败，任务ID: {}", task.getId(), e);
            }
        }
        
        log.debug("活跃任务状态同步完成，同步数量: {}", syncCount);
        return syncCount;
    }

    @Override
    public int checkAndSyncStaleTaskStatuses(int thresholdMinutes) {
        log.debug("检查并同步过期任务状态，阈值: {} 分钟", thresholdMinutes);
        
        LocalDateTime beforeTime = LocalDateTime.now().minusMinutes(thresholdMinutes);
        List<AnalysisTask> staleTasks = analysisTaskMapper.selectTasksNeedSync(beforeTime);
        
        int syncCount = 0;
        for (AnalysisTask task : staleTasks) {
            try {
                TaskStatus newStatus = syncTaskStatus(task.getId());
                if (newStatus != null) {
                    syncCount++;
                }
            } catch (Exception e) {
                log.error("同步过期任务状态失败，任务ID: {}", task.getId(), e);
            }
        }
        
        log.debug("过期任务状态同步完成，同步数量: {}", syncCount);
        return syncCount;
    }

    @Override
    public TaskStatus getTaskStatus(String taskId) {
        if (StringUtils.isEmpty(taskId)) {
            throw new ServiceException("任务ID不能为空");
        }
        
        AnalysisTask task = analysisTaskMapper.selectById(taskId);
        return task != null ? task.getStatus() : null;
    }

    @Override
    public boolean canPerformAction(String taskId, TaskAction action) {
        if (StringUtils.isEmpty(taskId) || action == null) {
            return false;
        }
        
        AnalysisTask task = analysisTaskMapper.selectById(taskId);
        return task != null && task.canPerformAction(action);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markTaskError(String taskId, String errorMessage) {
        log.warn("标记任务为错误状态，任务ID: {}, 错误信息: {}", taskId, errorMessage);
        
        return forceUpdateTaskStatus(taskId, TaskStatus.ERROR, errorMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean forceUpdateTaskStatus(String taskId, TaskStatus status, String errorMessage) {
        if (StringUtils.isEmpty(taskId) || status == null) {
            return false;
        }
        
        AnalysisTask task = new AnalysisTask();
        task.setId(taskId);
        task.setStatus(status);
        task.setErrorMessage(errorMessage);
        task.updateLastActiveTime();
        task.updateLastStatusSync();
        
        int result = analysisTaskMapper.update(task);
        
        if (result > 0) {
            log.info("任务状态强制更新成功，任务ID: {}, 状态: {}", taskId, status);
            return true;
        }
        
        log.warn("任务状态强制更新失败，任务ID: {}", taskId);
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateTaskStatus(List<String> taskIds, TaskStatus status, String errorMessage) {
        if (taskIds == null || taskIds.isEmpty() || status == null) {
            return 0;
        }
        
        log.info("批量更新任务状态，数量: {}, 目标状态: {}", taskIds.size(), status);
        
        int result = analysisTaskMapper.batchUpdateStatus(taskIds, status.getValue(), errorMessage);
        
        log.info("批量更新任务状态完成，成功数量: {}", result);
        return result;
    }

    /**
     * 执行任务操作
     * @param taskId 任务ID
     * @param action 操作类型
     * @param forceRestart 是否强制重启（仅对启动操作有效）
     */
    private void performTaskAction(String taskId, TaskAction action, boolean forceRestart) {
        log.info("执行任务操作，任务ID: {}, 操作: {}", taskId, action.getDescription());
        
        if (StringUtils.isEmpty(taskId)) {
            throw new ServiceException("任务ID不能为空");
        }
        
        ReentrantLock lock = taskLocks.computeIfAbsent(taskId, k -> new ReentrantLock());
        lock.lock();
        
        try {
            // 获取任务信息
            AnalysisTask task = analysisTaskMapper.selectById(taskId);
            if (task == null) {
                throw new ServiceException("任务不存在，ID: " + taskId);
            }
            
            // 检查操作权限
            if (!task.canPerformAction(action)) {
                throw new ServiceException(String.format("任务当前状态 %s 不允许执行 %s 操作", 
                        task.getStatus().getDescription(), action.getDescription()));
            }
            
            // 检查VLM作业ID
            if (StringUtils.isEmpty(task.getVlmJobId())) {
                throw new ServiceException("任务未关联VLM作业，无法执行操作");
            }
            
            // 更新为过渡状态
            TaskStatus transitioningStatus = task.getTransitioningStatus(action);
            task.setStatus(transitioningStatus);
            task.setErrorMessage(null); // 清除之前的错误信息
            task.updateLastActiveTime();
            analysisTaskMapper.update(task);
            
            // 调用VLM服务
            VLMJobActionResponse response = callVlmService(task.getVlmJobId(), action, forceRestart);
            
            if (response != null && response.isSuccess()) {
                // 操作成功，更新为最终状态
                TaskStatus finalStatus = task.getFinalStatus(action);
                task.setStatus(finalStatus);
                task.updateLastStatusSync();
                analysisTaskMapper.update(task);
                
                log.info("任务操作执行成功，任务ID: {}, 操作: {}, 最终状态: {}", 
                        taskId, action.getDescription(), finalStatus);
            } else {
                // 操作失败，更新为错误状态
                String errorMsg = response != null ? response.getErrorInfo() : "VLM服务调用失败";
                task.setStatus(TaskStatus.ERROR);
                task.setErrorMessage(errorMsg);
                analysisTaskMapper.update(task);
                
                log.error("任务操作执行失败，任务ID: {}, 操作: {}, 错误: {}", 
                        taskId, action.getDescription(), errorMsg);
                
                throw new ServiceException("任务操作失败: " + errorMsg);
            }
            
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("执行任务操作异常，任务ID: {}, 操作: {}", taskId, action.getDescription(), e);
            
            // 标记为错误状态
            markTaskError(taskId, "操作执行异常: " + e.getMessage());
            
            throw new ServiceException("任务操作执行异常: " + e.getMessage());
        } finally {
            lock.unlock();
            // 清理锁
            if (!lock.hasQueuedThreads()) {
                taskLocks.remove(taskId);
            }
        }
    }

    /**
     * 调用VLM服务
     */
    private VLMJobActionResponse callVlmService(String vlmJobId, TaskAction action, boolean forceRestart) {
        switch (action) {
            case START:
                return vlmClientService.startJob(vlmJobId, forceRestart);
            case PAUSE:
                return vlmClientService.pauseJob(vlmJobId);
            case RESUME:
                return vlmClientService.resumeJob(vlmJobId);
            case STOP:
                return vlmClientService.stopJob(vlmJobId);
            default:
                throw new ServiceException("不支持的任务操作: " + action);
        }
    }

    /**
     * 映射VLM状态到任务状态
     */
    private TaskStatus mapVlmStatusToTaskStatus(String vlmStatus) {
        if (StringUtils.isEmpty(vlmStatus)) {
            return TaskStatus.ERROR;
        }
        
        switch (vlmStatus.toLowerCase()) {
            case "created":
            case "pending":
                return TaskStatus.CREATED;
            case "running":
                return TaskStatus.RUNNING;
            case "paused":
                return TaskStatus.PAUSED;
            case "completed":
            case "cancelled":
                return TaskStatus.STOPPED;
            case "failed":
                return TaskStatus.FAILED;
            default:
                return TaskStatus.ERROR;
        }
    }
}