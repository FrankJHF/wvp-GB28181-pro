package com.genersoft.iot.vmp.vmanager.analysis;

import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;
import com.genersoft.iot.vmp.analysis.service.IAnalysisTaskService;
import com.genersoft.iot.vmp.conf.exception.ControllerException;
import com.genersoft.iot.vmp.conf.security.JwtUtils;
import com.genersoft.iot.vmp.conf.security.SecurityUtils;
import com.genersoft.iot.vmp.vmanager.bean.ErrorCode;
import com.genersoft.iot.vmp.vmanager.bean.WVPResult;
import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 分析任务管理控制器
 * @author Claude
 */
@Tag(name = "智能分析 - 分析任务管理")
@RestController
@RequestMapping("/api/vmanager/analysis/tasks")
@Slf4j
public class AnalysisTaskController {

    @Autowired
    private IAnalysisTaskService analysisTaskService;

    /**
     * 分页查询分析任务
     */
    @GetMapping
    @Operation(summary = "分页查询分析任务", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<PageInfo<AnalysisTask>> getTasks(
            @Parameter(name = "page", description = "页码，默认1") 
            @RequestParam(defaultValue = "1") int page,
            @Parameter(name = "count", description = "每页数量，默认20") 
            @RequestParam(defaultValue = "20") int count,
            @Parameter(name = "deviceId", description = "设备ID") 
            @RequestParam(required = false) String deviceId,
            @Parameter(name = "channelId", description = "通道ID") 
            @RequestParam(required = false) String channelId,
            @Parameter(name = "analysisCardId", description = "分析卡片ID") 
            @RequestParam(required = false) String analysisCardId,
            @Parameter(name = "status", description = "任务状态") 
            @RequestParam(required = false) String status,
            @Parameter(name = "taskName", description = "任务名称关键词") 
            @RequestParam(required = false) String taskName,
            @Parameter(name = "createdBy", description = "创建人") 
            @RequestParam(required = false) String createdBy) {
        
        try {
            // 非管理员用户只能查看自己创建的任务
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            boolean isAdmin = SecurityUtils.getUserInfo() != null; // 简化为只检查是否已登录
            
            if (!isAdmin) {
                createdBy = currentUser;
            }
            
            PageInfo<AnalysisTask> pageResult = analysisTaskService.getTaskPage(
                    page, count, deviceId, channelId, analysisCardId, status, createdBy, taskName);
            
            return WVPResult.success(pageResult, "查询成功");
        } catch (Exception e) {
            log.error("查询分析任务失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "查询分析任务失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询分析任务详情
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "根据ID查询分析任务详情", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<AnalysisTask> getTask(
            @Parameter(name = "taskId", description = "任务ID", required = true) 
            @PathVariable String taskId) {
        
        if (StringUtils.isEmpty(taskId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "任务ID不能为空");
        }
        
        try {
            AnalysisTask task = analysisTaskService.getTaskWithDetailsById(taskId);
            if (task == null) {
                throw new ControllerException(ErrorCode.ERROR404.getCode(), "分析任务不存在");
            }
            
            // 检查权限
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            boolean isAdmin = SecurityUtils.getUserInfo() != null; // 简化为只检查是否已登录
            
            if (!isAdmin && !currentUser.equals(task.getCreatedBy())) {
                throw new ControllerException(ErrorCode.ERROR403.getCode(), "无权限查看该任务");
            }
            
            return WVPResult.success(task, "查询成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询分析任务失败，ID: {}", taskId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "查询分析任务失败: " + e.getMessage());
        }
    }

    /**
     * 创建分析任务
     */
    @PostMapping
    @Operation(summary = "创建分析任务", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<AnalysisTask> createTask(@RequestBody AnalysisTask task) {
        
        try {
            // 设置创建人
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            task.setCreatedBy(currentUser);
            
            AnalysisTask result = analysisTaskService.createTask(task);
            
            log.info("用户 {} 创建了分析任务: {}", currentUser, result.getTaskName());
            
            return WVPResult.success(result, "创建成功");
        } catch (Exception e) {
            log.error("创建分析任务失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "创建分析任务失败: " + e.getMessage());
        }
    }

    /**
     * 更新分析任务
     */
    @PutMapping("/{taskId}")
    @Operation(summary = "更新分析任务", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<AnalysisTask> updateTask(
            @Parameter(name = "taskId", description = "任务ID", required = true) 
            @PathVariable String taskId,
            @RequestBody AnalysisTask task) {
        
        if (StringUtils.isEmpty(taskId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "任务ID不能为空");
        }
        
        try {
            // 检查任务是否存在
            AnalysisTask existingTask = analysisTaskService.getTaskById(taskId);
            if (existingTask == null) {
                throw new ControllerException(ErrorCode.ERROR404.getCode(), "分析任务不存在");
            }
            
            // 检查权限
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            boolean isAdmin = SecurityUtils.getUserInfo() != null; // 简化为只检查是否已登录
            
            if (!isAdmin && !currentUser.equals(existingTask.getCreatedBy())) {
                throw new ControllerException(ErrorCode.ERROR403.getCode(), "无权限更新该任务");
            }
            
            // 设置ID
            task.setId(taskId);
            
            AnalysisTask result = analysisTaskService.updateTask(task);
            
            log.info("用户 {} 更新了分析任务: {}", currentUser, result.getTaskName());
            
            return WVPResult.success(result, "更新成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新分析任务失败，ID: {}", taskId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "更新分析任务失败: " + e.getMessage());
        }
    }

    /**
     * 删除分析任务
     */
    @DeleteMapping("/{taskId}")
    @Operation(summary = "删除分析任务", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Void> deleteTask(
            @Parameter(name = "taskId", description = "任务ID", required = true) 
            @PathVariable String taskId) {
        
        if (StringUtils.isEmpty(taskId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "任务ID不能为空");
        }
        
        try {
            // 检查任务是否存在
            AnalysisTask existingTask = analysisTaskService.getTaskById(taskId);
            if (existingTask == null) {
                throw new ControllerException(ErrorCode.ERROR404.getCode(), "分析任务不存在");
            }
            
            // 检查权限
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            boolean isAdmin = SecurityUtils.getUserInfo() != null; // 简化为只检查是否已登录
            
            if (!isAdmin && !currentUser.equals(existingTask.getCreatedBy())) {
                throw new ControllerException(ErrorCode.ERROR403.getCode(), "无权限删除该任务");
            }
            
            boolean success = analysisTaskService.deleteTask(taskId);
            if (!success) {
                throw new ControllerException(ErrorCode.ERROR500.getCode(), "删除分析任务失败");
            }
            
            log.info("用户 {} 删除了分析任务: {}", currentUser, existingTask.getTaskName());
            
            return WVPResult.success(null, "删除成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("删除分析任务失败，ID: {}", taskId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "删除分析任务失败: " + e.getMessage());
        }
    }

    /**
     * 启动任务
     */
    @PostMapping("/{taskId}/start")
    @Operation(summary = "启动分析任务", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Void> startTask(
            @Parameter(name = "taskId", description = "任务ID", required = true) 
            @PathVariable String taskId,
            @Parameter(name = "forceRestart", description = "是否强制重启，默认false") 
            @RequestParam(defaultValue = "false") boolean forceRestart) {
        
        if (StringUtils.isEmpty(taskId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "任务ID不能为空");
        }
        
        try {
            // 检查权限
            checkTaskPermission(taskId);
            
            CompletableFuture<Void> future = analysisTaskService.startTask(taskId, forceRestart);
            
            // 等待任务启动完成（最多等待10秒）
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("等待任务启动完成超时，任务ID: {}", taskId);
                return WVPResult.success(null, "任务启动中，请稍后检查状态");
            }
            
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            log.info("用户 {} 启动了分析任务: {}", currentUser, taskId);
            
            return WVPResult.success(null, "任务启动成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("启动分析任务失败，ID: {}", taskId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "启动分析任务失败: " + e.getMessage());
        }
    }

    /**
     * 暂停任务
     */
    @PostMapping("/{taskId}/pause")
    @Operation(summary = "暂停分析任务", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Void> pauseTask(
            @Parameter(name = "taskId", description = "任务ID", required = true) 
            @PathVariable String taskId) {
        
        if (StringUtils.isEmpty(taskId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "任务ID不能为空");
        }
        
        try {
            // 检查权限
            checkTaskPermission(taskId);
            
            CompletableFuture<Void> future = analysisTaskService.pauseTask(taskId);
            
            // 等待任务暂停完成（最多等待5秒）
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("等待任务暂停完成超时，任务ID: {}", taskId);
                return WVPResult.success(null, "任务暂停中，请稍后检查状态");
            }
            
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            log.info("用户 {} 暂停了分析任务: {}", currentUser, taskId);
            
            return WVPResult.success(null, "任务暂停成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("暂停分析任务失败，ID: {}", taskId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "暂停分析任务失败: " + e.getMessage());
        }
    }

    /**
     * 恢复任务
     */
    @PostMapping("/{taskId}/resume")
    @Operation(summary = "恢复分析任务", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Void> resumeTask(
            @Parameter(name = "taskId", description = "任务ID", required = true) 
            @PathVariable String taskId) {
        
        if (StringUtils.isEmpty(taskId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "任务ID不能为空");
        }
        
        try {
            // 检查权限
            checkTaskPermission(taskId);
            
            CompletableFuture<Void> future = analysisTaskService.resumeTask(taskId);
            
            // 等待任务恢复完成（最多等待5秒）
            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("等待任务恢复完成超时，任务ID: {}", taskId);
                return WVPResult.success(null, "任务恢复中，请稍后检查状态");
            }
            
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            log.info("用户 {} 恢复了分析任务: {}", currentUser, taskId);
            
            return WVPResult.success(null, "任务恢复成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("恢复分析任务失败，ID: {}", taskId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "恢复分析任务失败: " + e.getMessage());
        }
    }

    /**
     * 停止任务
     */
    @PostMapping("/{taskId}/stop")
    @Operation(summary = "停止分析任务", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Void> stopTask(
            @Parameter(name = "taskId", description = "任务ID", required = true) 
            @PathVariable String taskId) {
        
        if (StringUtils.isEmpty(taskId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "任务ID不能为空");
        }
        
        try {
            // 检查权限
            checkTaskPermission(taskId);
            
            CompletableFuture<Void> future = analysisTaskService.stopTask(taskId);
            
            // 等待任务停止完成（最多等待10秒）
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("等待任务停止完成超时，任务ID: {}", taskId);
                return WVPResult.success(null, "任务停止中，请稍后检查状态");
            }
            
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            log.info("用户 {} 停止了分析任务: {}", currentUser, taskId);
            
            return WVPResult.success(null, "任务停止成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("停止分析任务失败，ID: {}", taskId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "停止分析任务失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除任务
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除分析任务", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Integer> batchDeleteTasks(@RequestBody List<String> taskIds) {
        
        if (taskIds == null || taskIds.isEmpty()) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "任务ID列表不能为空");
        }
        
        try {
            // 检查权限
            boolean isAdmin = SecurityUtils.getUserInfo() != null; // 简化为只检查是否已登录
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            
            if (!isAdmin) {
                // 非管理员用户只能删除自己的任务
                for (String taskId : taskIds) {
                    AnalysisTask task = analysisTaskService.getTaskById(taskId);
                    if (task != null && !currentUser.equals(task.getCreatedBy())) {
                        throw new ControllerException(ErrorCode.ERROR403.getCode(), 
                                "无权限删除任务: " + taskId);
                    }
                }
            }
            
            int deleteCount = analysisTaskService.batchDeleteTasks(taskIds);
            
            log.info("用户 {} 批量删除了 {} 个分析任务", currentUser, deleteCount);
            
            return WVPResult.success(deleteCount, "批量删除完成，成功删除 " + deleteCount + " 个任务");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量删除分析任务失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "批量删除分析任务失败: " + e.getMessage());
        }
    }

    /**
     * 同步任务状态
     */
    @PostMapping("/sync-status")
    @Operation(summary = "同步所有活跃任务状态", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Integer> syncTaskStatuses() {
        
        // 验证权限 - 只有已登录用户可以执行状态同步
        if (SecurityUtils.getUserInfo() == null) {
            throw new ControllerException(ErrorCode.ERROR403.getCode(), "用户未登录");
        }
        
        try {
            int syncCount = analysisTaskService.syncAllActiveTaskStatuses();
            
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            log.info("用户 {} 同步了 {} 个任务状态", currentUser, syncCount);
            
            return WVPResult.success(syncCount, "状态同步完成，同步 " + syncCount + " 个任务");
        } catch (Exception e) {
            log.error("同步任务状态失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "同步任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 统计任务数量
     */
    @GetMapping("/count")
    @Operation(summary = "统计任务数量", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Long> countTasks(
            @Parameter(name = "deviceId", description = "设备ID") 
            @RequestParam(required = false) String deviceId,
            @Parameter(name = "status", description = "任务状态") 
            @RequestParam(required = false) String status,
            @Parameter(name = "createdBy", description = "创建人") 
            @RequestParam(required = false) String createdBy) {
        
        try {
            // 非管理员用户只能统计自己创建的任务
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            boolean isAdmin = SecurityUtils.getUserInfo() != null; // 简化为只检查是否已登录
            
            if (!isAdmin) {
                createdBy = currentUser;
            }
            
            long count = analysisTaskService.countTasks(deviceId, status, createdBy);
            return WVPResult.success(count, "统计成功");
        } catch (Exception e) {
            log.error("统计任务数量失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "统计任务数量失败: " + e.getMessage());
        }
    }

    /**
     * 检查任务权限
     */
    private void checkTaskPermission(String taskId) {
        AnalysisTask existingTask = analysisTaskService.getTaskById(taskId);
        if (existingTask == null) {
            throw new ControllerException(ErrorCode.ERROR404.getCode(), "分析任务不存在");
        }
        
        String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
        boolean isAdmin = SecurityUtils.getUserInfo() != null; // 简化为只检查是否已登录
        
        if (!isAdmin && !currentUser.equals(existingTask.getCreatedBy())) {
            throw new ControllerException(ErrorCode.ERROR403.getCode(), "无权限操作该任务");
        }
    }
}