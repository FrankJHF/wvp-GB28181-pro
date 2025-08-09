package com.genersoft.iot.vmp.vmanager.analysis;

import com.genersoft.iot.vmp.analysis.bean.AnalysisAlarm;
import com.genersoft.iot.vmp.analysis.bean.AlarmStatus;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMAnalysisResult;
import com.genersoft.iot.vmp.analysis.service.IAnalysisAlarmService;
import com.genersoft.iot.vmp.analysis.service.impl.VLMCallbackProcessor;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分析告警管理控制器
 * @author Claude
 */
@Tag(name = "智能分析 - 分析告警管理")
@RestController
@Slf4j
public class AnalysisAlarmController {

    @Autowired
    private IAnalysisAlarmService analysisAlarmService;

    @Autowired
    private VLMCallbackProcessor vlmCallbackProcessor;

    /**
     * 分页查询分析告警
     */
    @GetMapping("/api/vmanager/analysis/alarms")
    @Operation(summary = "分页查询分析告警", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<PageInfo<AnalysisAlarm>> getAlarms(
            @Parameter(name = "page", description = "页码，默认1") 
            @RequestParam(defaultValue = "1") int page,
            @Parameter(name = "count", description = "每页数量，默认20") 
            @RequestParam(defaultValue = "20") int count,
            @Parameter(name = "startTime", description = "开始时间") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(name = "endTime", description = "结束时间") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(name = "deviceId", description = "设备ID") 
            @RequestParam(required = false) String deviceId,
            @Parameter(name = "channelId", description = "通道ID") 
            @RequestParam(required = false) String channelId,
            @Parameter(name = "analysisType", description = "分析类型") 
            @RequestParam(required = false) String analysisType,
            @Parameter(name = "status", description = "告警状态") 
            @RequestParam(required = false) String status,
            @Parameter(name = "taskId", description = "任务ID") 
            @RequestParam(required = false) String taskId) {
        
        try {
            PageInfo<AnalysisAlarm> pageResult = analysisAlarmService.getAlarmPage(
                    page, count, startTime, endTime, deviceId, channelId, analysisType, status, taskId);
            
            return WVPResult.success(pageResult, "查询成功");
        } catch (Exception e) {
            log.error("查询分析告警失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "查询分析告警失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询告警详情
     */
    @GetMapping("/api/vmanager/analysis/alarms/{alarmId}")
    @Operation(summary = "根据ID查询告警详情", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<AnalysisAlarm> getAlarm(
            @Parameter(name = "alarmId", description = "告警ID", required = true) 
            @PathVariable String alarmId) {
        
        if (StringUtils.isEmpty(alarmId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "告警ID不能为空");
        }
        
        try {
            AnalysisAlarm alarm = analysisAlarmService.getAlarmWithDetailsById(alarmId);
            if (alarm == null) {
                throw new ControllerException(ErrorCode.ERROR404.getCode(), "分析告警不存在");
            }
            
            return WVPResult.success(alarm, "查询成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("查询分析告警失败，ID: {}", alarmId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "查询分析告警失败: " + e.getMessage());
        }
    }

    /**
     * 根据任务ID查询告警
     */
    @GetMapping("/api/vmanager/analysis/alarms/task/{taskId}")
    @Operation(summary = "根据任务ID查询告警", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<List<AnalysisAlarm>> getAlarmsByTaskId(
            @Parameter(name = "taskId", description = "任务ID", required = true) 
            @PathVariable String taskId) {
        
        if (StringUtils.isEmpty(taskId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "任务ID不能为空");
        }
        
        try {
            List<AnalysisAlarm> alarms = analysisAlarmService.getAlarmsByTaskId(taskId);
            return WVPResult.success(alarms, "查询成功");
        } catch (Exception e) {
            log.error("查询任务告警失败，任务ID: {}", taskId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "查询任务告警失败: " + e.getMessage());
        }
    }

    /**
     * 获取最近告警
     */
    @GetMapping("/api/vmanager/analysis/alarms/recent")
    @Operation(summary = "获取最近告警", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<List<AnalysisAlarm>> getRecentAlarms(
            @Parameter(name = "limit", description = "数量限制，默认10") 
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            List<AnalysisAlarm> alarms = analysisAlarmService.getRecentAlarms(limit);
            return WVPResult.success(alarms, "查询成功");
        } catch (Exception e) {
            log.error("查询最近告警失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "查询最近告警失败: " + e.getMessage());
        }
    }

    /**
     * 获取待处理告警
     */
    @GetMapping("/api/vmanager/analysis/alarms/pending")
    @Operation(summary = "获取待处理告警", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<List<AnalysisAlarm>> getPendingAlarms() {
        
        try {
            List<AnalysisAlarm> alarms = analysisAlarmService.getPendingAlarms();
            return WVPResult.success(alarms, "查询成功");
        } catch (Exception e) {
            log.error("查询待处理告警失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "查询待处理告警失败: " + e.getMessage());
        }
    }

    /**
     * 更新告警状态
     */
    @PutMapping("/api/vmanager/analysis/alarms/{alarmId}/status")
    @Operation(summary = "更新告警状态", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Void> updateAlarmStatus(
            @Parameter(name = "alarmId", description = "告警ID", required = true) 
            @PathVariable String alarmId,
            @Parameter(name = "status", description = "告警状态", required = true) 
            @RequestParam String status) {
        
        if (StringUtils.isEmpty(alarmId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "告警ID不能为空");
        }
        
        if (StringUtils.isEmpty(status)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "告警状态不能为空");
        }
        
        try {
            AlarmStatus alarmStatus = AlarmStatus.fromValue(status);
            if (alarmStatus == null) {
                throw new ControllerException(ErrorCode.ERROR400.getCode(), "无效的告警状态: " + status);
            }
            
            boolean success = analysisAlarmService.updateAlarmStatus(alarmId, alarmStatus);
            if (!success) {
                throw new ControllerException(ErrorCode.ERROR500.getCode(), "更新告警状态失败");
            }
            
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            log.info("用户 {} 更新了告警状态: {} -> {}", currentUser, alarmId, alarmStatus.getDescription());
            
            return WVPResult.success(null, "状态更新成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新告警状态失败，告警ID: {}", alarmId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "更新告警状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新告警状态
     */
    @PutMapping("/api/vmanager/analysis/alarms/batch-status")
    @Operation(summary = "批量更新告警状态", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Integer> batchUpdateAlarmStatus(
            @RequestBody Map<String, Object> params) {
        
        @SuppressWarnings("unchecked")
        List<String> alarmIds = (List<String>) params.get("alarmIds");
        String status = (String) params.get("status");
        
        if (alarmIds == null || alarmIds.isEmpty()) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "告警ID列表不能为空");
        }
        
        if (StringUtils.isEmpty(status)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "告警状态不能为空");
        }
        
        try {
            AlarmStatus alarmStatus = AlarmStatus.fromValue(status);
            if (alarmStatus == null) {
                throw new ControllerException(ErrorCode.ERROR400.getCode(), "无效的告警状态: " + status);
            }
            
            int updateCount = analysisAlarmService.batchUpdateAlarmStatus(alarmIds, alarmStatus);
            
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            log.info("用户 {} 批量更新了 {} 个告警状态为: {}", currentUser, updateCount, alarmStatus.getDescription());
            
            return WVPResult.success(updateCount, "批量更新成功，更新 " + updateCount + " 个告警");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量更新告警状态失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "批量更新告警状态失败: " + e.getMessage());
        }
    }

    /**
     * 处理告警（标记为已处理）
     */
    @PostMapping("/api/vmanager/analysis/alarms/{alarmId}/process")
    @Operation(summary = "处理告警", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Void> processAlarm(
            @Parameter(name = "alarmId", description = "告警ID", required = true) 
            @PathVariable String alarmId) {
        
        if (StringUtils.isEmpty(alarmId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "告警ID不能为空");
        }
        
        try {
            boolean success = analysisAlarmService.processAlarm(alarmId);
            if (!success) {
                throw new ControllerException(ErrorCode.ERROR500.getCode(), "处理告警失败");
            }
            
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            log.info("用户 {} 处理了告警: {}", currentUser, alarmId);
            
            return WVPResult.success(null, "告警处理成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("处理告警失败，告警ID: {}", alarmId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "处理告警失败: " + e.getMessage());
        }
    }

    /**
     * 忽略告警
     */
    @PostMapping("/api/vmanager/analysis/alarms/{alarmId}/ignore")
    @Operation(summary = "忽略告警", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Void> ignoreAlarm(
            @Parameter(name = "alarmId", description = "告警ID", required = true) 
            @PathVariable String alarmId) {
        
        if (StringUtils.isEmpty(alarmId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "告警ID不能为空");
        }
        
        try {
            boolean success = analysisAlarmService.ignoreAlarm(alarmId);
            if (!success) {
                throw new ControllerException(ErrorCode.ERROR500.getCode(), "忽略告警失败");
            }
            
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            log.info("用户 {} 忽略了告警: {}", currentUser, alarmId);
            
            return WVPResult.success(null, "告警忽略成功");
        } catch (ControllerException e) {
            throw e;
        } catch (Exception e) {
            log.error("忽略告警失败，告警ID: {}", alarmId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "忽略告警失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除告警
     */
    @DeleteMapping("/api/vmanager/analysis/alarms/batch")
    @Operation(summary = "批量删除告警", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Integer> batchDeleteAlarms(@RequestBody List<String> alarmIds) {
        
        if (alarmIds == null || alarmIds.isEmpty()) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "告警ID列表不能为空");
        }
        
        // 验证权限 - 只有已登录用户可以批量删除告警
        if (SecurityUtils.getUserInfo() == null) {
            throw new ControllerException(ErrorCode.ERROR403.getCode(), "用户未登录");
        }
        
        try {
            int deleteCount = analysisAlarmService.batchDeleteAlarms(alarmIds);
            
            String currentUser = SecurityUtils.getUserInfo() != null ? SecurityUtils.getUserInfo().getUsername() : "unknown";
            log.info("用户 {} 批量删除了 {} 个告警", currentUser, deleteCount);
            
            return WVPResult.success(deleteCount, "批量删除完成，成功删除 " + deleteCount + " 个告警");
        } catch (Exception e) {
            log.error("批量删除告警失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "批量删除告警失败: " + e.getMessage());
        }
    }

    /**
     * 统计告警数量
     */
    @GetMapping("/api/vmanager/analysis/alarms/count")
    @Operation(summary = "统计告警数量", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Long> countAlarms(
            @Parameter(name = "startTime", description = "开始时间") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(name = "endTime", description = "结束时间") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(name = "deviceId", description = "设备ID") 
            @RequestParam(required = false) String deviceId,
            @Parameter(name = "channelId", description = "通道ID") 
            @RequestParam(required = false) String channelId,
            @Parameter(name = "analysisType", description = "分析类型") 
            @RequestParam(required = false) String analysisType,
            @Parameter(name = "status", description = "告警状态") 
            @RequestParam(required = false) String status,
            @Parameter(name = "taskId", description = "任务ID") 
            @RequestParam(required = false) String taskId) {
        
        try {
            long count = analysisAlarmService.countAlarms(startTime, endTime, deviceId, channelId, 
                    analysisType, status, taskId);
            return WVPResult.success(count, "统计成功");
        } catch (Exception e) {
            log.error("统计告警数量失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "统计告警数量失败: " + e.getMessage());
        }
    }

    /**
     * 按状态统计告警数量
     */
    @GetMapping("/api/vmanager/analysis/alarms/count-by-status")
    @Operation(summary = "按状态统计告警数量", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Map<String, Long>> countAlarmsByStatus(
            @Parameter(name = "startTime", description = "开始时间") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(name = "endTime", description = "结束时间") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            Map<String, Long> statusCounts = analysisAlarmService.countAlarmsByStatus(startTime, endTime);
            return WVPResult.success(statusCounts, "统计成功");
        } catch (Exception e) {
            log.error("按状态统计告警数量失败", e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "按状态统计告警数量失败: " + e.getMessage());
        }
    }

    /**
     * 检查快照是否存在
     */
    @GetMapping("/api/vmanager/analysis/alarms/{alarmId}/snapshot/exists")
    @Operation(summary = "检查快照是否存在", security = @SecurityRequirement(name = JwtUtils.HEADER))
    public WVPResult<Boolean> isSnapshotExists(
            @Parameter(name = "alarmId", description = "告警ID", required = true) 
            @PathVariable String alarmId) {
        
        if (StringUtils.isEmpty(alarmId)) {
            throw new ControllerException(ErrorCode.ERROR400.getCode(), "告警ID不能为空");
        }
        
        try {
            boolean exists = analysisAlarmService.isSnapshotExists(alarmId);
            return WVPResult.success(exists, exists ? "快照存在" : "快照不存在");
        } catch (Exception e) {
            log.error("检查快照存在性失败，告警ID: {}", alarmId, e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "检查快照存在性失败: " + e.getMessage());
        }
    }

    /**
     * VLM回调接口
     */
    @PostMapping("/api/vlm/callback")
    @Operation(summary = "VLM分析结果回调接口")
    public WVPResult<Void> vlmCallback(@RequestBody VLMAnalysisResult callback) {
        
        log.info("接收到VLM回调数据，作业ID: {}, 设备ID: {}, 通道ID: {}", 
                callback.getJobId(), callback.getDeviceId(), callback.getChannelId());
        
        try {
            // 异步处理回调数据
            vlmCallbackProcessor.processCallback(callback);
            
            return WVPResult.success(null, "回调处理成功");
        } catch (Exception e) {
            log.error("处理VLM回调失败，作业ID: {}", callback.getJobId(), e);
            throw new ControllerException(ErrorCode.ERROR500.getCode(), 
                    "处理VLM回调失败: " + e.getMessage());
        }
    }
}