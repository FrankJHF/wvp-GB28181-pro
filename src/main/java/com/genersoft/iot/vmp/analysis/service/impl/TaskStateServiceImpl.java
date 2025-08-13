package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.service.ITaskStateService;
import com.genersoft.iot.vmp.analysis.service.IVLMClientService;
import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.TaskAction;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;
import com.genersoft.iot.vmp.analysis.bean.VLMStatusMapper;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobActionResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.JobStatusResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.JobStatusUpdateRequest;
import com.genersoft.iot.vmp.analysis.bean.dto.JobCancelResponse;
import com.genersoft.iot.vmp.storager.dao.AnalysisTaskMapper;
import com.genersoft.iot.vmp.conf.exception.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

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
            try {
                performTaskAction(taskId, TaskAction.START, forceRestart);
            } catch (ServiceException e) {
                log.error("启动任务失败，任务ID: {}", taskId, e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> pauseTask(String taskId) {
        return CompletableFuture.runAsync(() -> {
            try {
                performTaskAction(taskId, TaskAction.PAUSE, false);
            } catch (ServiceException e) {
                log.error("暂停任务失败，任务ID: {}", taskId, e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> resumeTask(String taskId) {
        return CompletableFuture.runAsync(() -> {
            try {
                performTaskAction(taskId, TaskAction.RESUME, false);
            } catch (ServiceException e) {
                log.error("恢复任务失败，任务ID: {}", taskId, e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> cancelTask(String taskId) {
        return CompletableFuture.runAsync(() -> {
            try {
                performTaskAction(taskId, TaskAction.CANCEL, false);
            } catch (ServiceException e) {
                log.error("取消任务失败，任务ID: {}", taskId, e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    @Async("taskExecutor")
    @Deprecated
    public CompletableFuture<Void> stopTask(String taskId) {
        // 为了向后兼容，委托给cancelTask
        return cancelTask(taskId);
    }

    @Override
    public TaskStatus syncTaskStatus(String taskId) throws ServiceException {
        log.debug("同步任务状态，任务ID: {}", taskId);
        
        if (taskId == null || taskId.trim().isEmpty()) {
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
            
            if (task.getVlmJobId() == null || task.getVlmJobId().trim().isEmpty()) {
                log.debug("任务未关联VLM作业，跳过状态同步，任务ID: {}", taskId);
                return task.getStatus();
            }
            
            // 查询VLM作业状态
            JobStatusResponse vlmJob = vlmClientService.getJobStatus(task.getVlmJobId());
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
        
        // 查询需要同步的任务（只查询活跃状态的任务）
        List<String> activeStatuses = List.of("running", "paused");
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
    public TaskStatus getTaskStatus(String taskId) throws ServiceException {
        if (taskId == null || taskId.trim().isEmpty()) {
            throw new ServiceException("任务ID不能为空");
        }
        
        AnalysisTask task = analysisTaskMapper.selectById(taskId);
        return task != null ? task.getStatus() : null;
    }

    @Override
    public boolean canPerformAction(String taskId, TaskAction action) {
        if (taskId == null || taskId.trim().isEmpty() || action == null) {
            return false;
        }
        
        AnalysisTask task = analysisTaskMapper.selectById(taskId);
        return task != null && task.canPerformAction(action);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean markTaskError(String taskId, String errorMessage) {
        log.warn("标记任务为错误状态，任务ID: {}, 错误信息: {}", taskId, errorMessage);
        
        return forceUpdateTaskStatus(taskId, TaskStatus.FAILED, errorMessage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean forceUpdateTaskStatus(String taskId, TaskStatus status, String errorMessage) {
        if (taskId == null || taskId.trim().isEmpty() || status == null) {
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
        
        int result = analysisTaskMapper.batchUpdateStatus(taskIds, status, errorMessage);
        
        log.info("批量更新任务状态完成，成功数量: {}", result);
        return result;
    }

    /**
     * 执行任务操作
     * @param taskId 任务ID
     * @param action 操作类型
     * @param forceRestart 是否强制重启（仅对启动操作有效）
     */
    private void performTaskAction(String taskId, TaskAction action, boolean forceRestart) throws ServiceException {
        log.info("执行任务操作，任务ID: {}, 操作: {}", taskId, action.getDescription());
        
        if (taskId == null || taskId.trim().isEmpty()) {
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
            
            // 对于取消操作，根据任务状态采用不同策略
            if (action == TaskAction.CANCEL) {
                // 对于CREATED和FAILED状态，直接更新为CANCELLED，不调用VLM
                if (task.getStatus() == TaskStatus.CREATED || task.getStatus() == TaskStatus.FAILED) {
                    TaskStatus originalStatus = task.getStatus();
                    log.info("任务状态为{}，直接更新为CANCELLED，不调用VLM服务", originalStatus);
                    task.setStatus(TaskStatus.CANCELLED);
                    task.updateLastStatusSync();
                    analysisTaskMapper.update(task);
                    
                    log.info("任务取消成功，任务ID: {}, 原状态: {} -> CANCELLED", taskId, originalStatus);
                    return;
                }
                
                // 对于RUNNING和PAUSED状态，需要调用VLM DELETE接口
                if (task.getStatus() != TaskStatus.RUNNING && task.getStatus() != TaskStatus.PAUSED) {
                    throw new ServiceException("当前状态不支持VLM取消操作: " + task.getStatus());
                }
            }
            
            // 检查VLM作业ID（非取消操作或需要调用VLM的取消操作）
            if (task.getVlmJobId() == null || task.getVlmJobId().trim().isEmpty()) {
                throw new ServiceException("任务未关联VLM作业，无法执行操作");
            }
            
            // 清除之前的错误信息，准备执行操作
            task.setErrorMessage(null);
            task.updateLastActiveTime();
            
            // 调用VLM服务
            VLMJobActionResponse response = callVlmService(task.getVlmJobId(), action, forceRestart);
            
            if (response != null && response.isSuccess()) {
                // 操作成功，根据VLM返回的状态更新任务状态
                TaskStatus vlmMappedStatus = VLMStatusMapper.mapVLMStatusToTaskStatus(response.getCurrentStatus());
                
                if (vlmMappedStatus != null) {
                    task.setStatus(vlmMappedStatus);
                    log.info("根据VLM响应更新任务状态，任务ID: {}, VLM状态: {} -> WVP状态: {}", 
                            taskId, response.getCurrentStatus(), vlmMappedStatus.getDescription());
                } else {
                    // 如果无法映射VLM状态，使用预期的目标状态
                    TaskStatus targetStatus = task.getTargetStatus(action);
                    task.setStatus(targetStatus);
                    log.warn("无法映射VLM状态 '{}', 使用预期状态: {}", 
                            response.getCurrentStatus(), targetStatus.getDescription());
                }
                
                task.updateLastStatusSync();
                analysisTaskMapper.update(task);
                
                log.info("任务操作执行成功，任务ID: {}, 操作: {}, 最终状态: {}", 
                        taskId, action.getDescription(), task.getStatus().getDescription());
            } else {
                // 操作失败，更新为错误状态
                String errorMsg = response != null ? response.getErrorInfo() : "VLM服务调用失败";
                task.setStatus(TaskStatus.FAILED);
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
     * 调用VLM服务 - 使用新的统一接口
     */
    private VLMJobActionResponse callVlmService(String vlmJobId, TaskAction action, boolean forceRestart) throws ServiceException {
        switch (action) {
            case START:
                JobStatusUpdateRequest startRequest = new JobStatusUpdateRequest("start", forceRestart);
                return vlmClientService.updateJobStatus(vlmJobId, startRequest);
            case PAUSE:
                JobStatusUpdateRequest pauseRequest = new JobStatusUpdateRequest("pause");
                return vlmClientService.updateJobStatus(vlmJobId, pauseRequest);
            case RESUME:
                JobStatusUpdateRequest resumeRequest = new JobStatusUpdateRequest("resume");
                return vlmClientService.updateJobStatus(vlmJobId, resumeRequest);
            case CANCEL:
                // 取消操作使用cancelJob接口
                JobCancelResponse cancelResponse = vlmClientService.cancelJob(vlmJobId);
                // 适配为统一的VLMJobActionResponse格式
                VLMJobActionResponse actionResponse = new VLMJobActionResponse();
                actionResponse.setJobId(cancelResponse.getJobId());
                actionResponse.setMessage(cancelResponse.getMessage());
                actionResponse.setCurrentStatus("cancelled");
                actionResponse.setActionTimestamp(cancelResponse.getCancelledAt());
                return actionResponse;
            default:
                throw new ServiceException("不支持的任务操作: " + action);
        }
    }

    /**
     * 将VLM状态映射为任务状态
     * 直接使用VLMStatusMapper进行统一映射
     */
    private TaskStatus mapVlmStatusToTaskStatus(String vlmStatus) {
        TaskStatus mappedStatus = VLMStatusMapper.mapVLMStatusToTaskStatus(vlmStatus);
        if (mappedStatus == null) {
            log.warn("无法映射VLM状态: {}，返回FAILED状态", vlmStatus);
            return TaskStatus.FAILED;
        }
        return mappedStatus;
    }
}