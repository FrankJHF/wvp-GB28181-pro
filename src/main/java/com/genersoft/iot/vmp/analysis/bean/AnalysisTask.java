package com.genersoft.iot.vmp.analysis.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 智能分析任务实体类
 * @author Claude
 */
@Data
@Schema(description = "智能分析任务")
public class AnalysisTask {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "任务名称")
    @JsonProperty("task_name")
    private String taskName;

    @Schema(description = "分析卡片ID")
    @JsonProperty("analysis_card_id")
    private String analysisCardId;

    @Schema(description = "设备ID")
    @JsonProperty("device_id")
    private String deviceId;

    @Schema(description = "设备名称")
    @JsonProperty("device_name")
    private String deviceName;

    @Schema(description = "通道ID")
    @JsonProperty("channel_id")
    private String channelId;

    @Schema(description = "通道名称")
    @JsonProperty("channel_name")
    private String channelName;

    @Schema(description = "RTSP流地址")
    @JsonProperty("rtsp_url")
    private String rtspUrl;

    @Schema(description = "任务状态")
    private TaskStatus status;

    @Schema(description = "VLM微服务Job ID")
    @JsonProperty("vlm_job_id")
    private String vlmJobId;

    @Schema(description = "任务配置参数")
    private Map<String, Object> config;

    @Schema(description = "错误信息")
    @JsonProperty("error_message")
    private String errorMessage;

    @Schema(description = "最后活跃时间")
    @JsonProperty("last_active_time")
    private LocalDateTime lastActiveTime;

    @Schema(description = "最后状态同步时间")
    @JsonProperty("last_status_sync")
    private LocalDateTime lastStatusSync;

    @Schema(description = "创建人")
    @JsonProperty("created_by")
    private String createdBy;

    @Schema(description = "创建时间")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // 关联的分析卡片信息（非数据库字段）
    @Schema(description = "关联的分析卡片信息")
    private transient AnalysisCard analysisCard;

    // ==================== 状态转换辅助方法 ====================

    /**
     * 判断任务是否处于过渡状态
     * @return true如果处于过渡状态
     */
    public boolean isTransitioning() {
        return status != null && status.isTransitioning();
    }

    /**
     * 判断任务是否可以启动
     * @return true如果可以启动
     */
    public boolean canStart() {
        return status == TaskStatus.CREATED || 
               status == TaskStatus.STOPPED || 
               status == TaskStatus.FAILED || 
               status == TaskStatus.ERROR;
    }

    /**
     * 判断任务是否可以暂停
     * @return true如果可以暂停
     */
    public boolean canPause() {
        return status == TaskStatus.RUNNING;
    }

    /**
     * 判断任务是否可以恢复
     * @return true如果可以恢复
     */
    public boolean canResume() {
        return status == TaskStatus.PAUSED;
    }

    /**
     * 判断任务是否可以停止
     * @return true如果可以停止
     */
    public boolean canStop() {
        return status == TaskStatus.RUNNING || 
               status == TaskStatus.PAUSED || 
               status == TaskStatus.STARTING ||
               status == TaskStatus.PAUSING ||
               status == TaskStatus.RESUMING;
    }

    /**
     * 判断任务是否可以删除
     * @return true如果可以删除
     */
    public boolean canDelete() {
        return status == TaskStatus.CREATED || 
               status == TaskStatus.STOPPED || 
               status == TaskStatus.FAILED || 
               status == TaskStatus.ERROR;
    }

    /**
     * 判断任务是否处于活跃状态（需要定期同步状态）
     * @return true如果处于活跃状态
     */
    public boolean isActive() {
        return status != null && status.isActive();
    }

    /**
     * 判断任务是否已终止
     * @return true如果已终止
     */
    public boolean isTerminated() {
        return status != null && status.isTerminated();
    }

    /**
     * 检查是否允许执行指定操作
     * @param action 要执行的操作
     * @return true如果允许执行该操作
     */
    public boolean canPerformAction(TaskAction action) {
        if (action == null) {
            return false;
        }

        switch (action) {
            case START:
                return canStart();
            case PAUSE:
                return canPause();
            case RESUME:
                return canResume();
            case STOP:
                return canStop();
            case DELETE:
                return canDelete();
            default:
                return false;
        }
    }

    /**
     * 获取状态转换的目标状态
     * @param action 要执行的操作
     * @return 转换后的状态
     */
    public TaskStatus getTransitioningStatus(TaskAction action) {
        if (action == null) {
            return status;
        }

        switch (action) {
            case START:
                return TaskStatus.STARTING;
            case PAUSE:
                return TaskStatus.PAUSING;
            case RESUME:
                return TaskStatus.RESUMING;
            case STOP:
                return TaskStatus.STOPPING;
            default:
                return status;
        }
    }

    /**
     * 获取操作成功后的最终状态
     * @param action 执行的操作
     * @return 成功后的状态
     */
    public TaskStatus getFinalStatus(TaskAction action) {
        if (action == null) {
            return status;
        }

        switch (action) {
            case START:
                return TaskStatus.RUNNING;
            case PAUSE:
                return TaskStatus.PAUSED;
            case RESUME:
                return TaskStatus.RUNNING;
            case STOP:
                return TaskStatus.STOPPED;
            default:
                return status;
        }
    }

    /**
     * 更新最后活跃时间为当前时间
     */
    public void updateLastActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
    }

    /**
     * 更新最后状态同步时间为当前时间
     */
    public void updateLastStatusSync() {
        this.lastStatusSync = LocalDateTime.now();
    }

    /**
     * 判断任务是否需要同步状态（距离上次同步时间超过阈值）
     * @param thresholdMinutes 阈值分钟数
     * @return true如果需要同步状态
     */
    public boolean needsStatusSync(long thresholdMinutes) {
        if (lastStatusSync == null) {
            return true;
        }
        return lastStatusSync.isBefore(LocalDateTime.now().minusMinutes(thresholdMinutes));
    }
}