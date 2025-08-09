package com.genersoft.iot.vmp.analysis.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * 智能分析告警实体类
 * @author Claude
 */
@Data
@Schema(description = "智能分析告警")
public class AnalysisAlarm {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "任务ID")
    @JsonProperty("task_id")
    private String taskId;

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

    @Schema(description = "分析类型")
    @JsonProperty("analysis_type")
    private String analysisType;

    @Schema(description = "告警描述")
    private String description;

    @Schema(description = "快照图片路径")
    @JsonProperty("snapshot_path")
    private String snapshotPath;

    @Schema(description = "告警时间")
    @JsonProperty("alarm_time")
    private LocalDateTime alarmTime;

    @Schema(description = "事件开始时间")
    @JsonProperty("event_start_time")
    private LocalDateTime eventStartTime;

    @Schema(description = "事件结束时间")
    @JsonProperty("event_end_time")
    private LocalDateTime eventEndTime;

    @Schema(description = "相对时间范围")
    @JsonProperty("event_time_range")
    private String eventTimeRange;

    @Schema(description = "视频窗口时间信息")
    @JsonProperty("video_window_info")
    private VideoWindowInfo videoWindowInfo;

    @Schema(description = "处理状态")
    private AlarmStatus status;

    @Schema(description = "创建时间")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    // 关联的任务信息（非数据库字段）
    @Schema(description = "关联的分析任务信息")
    private transient AnalysisTask analysisTask;

    // ==================== 业务辅助方法 ====================

    /**
     * 判断是否为紧急告警
     * @return true如果是紧急告警
     */
    public boolean isEmergency() {
        return description != null && description.toLowerCase().contains("emergency");
    }

    /**
     * 判断告警是否已处理
     * @return true如果已处理
     */
    public boolean isProcessed() {
        return status != null && status.isProcessed();
    }

    /**
     * 判断告警是否待处理
     * @return true如果待处理
     */
    public boolean isPending() {
        return status == AlarmStatus.PENDING;
    }

    /**
     * 获取告警等级描述
     * @return 告警等级
     */
    public String getAlarmLevel() {
        if (isEmergency()) {
            return "紧急";
        }
        
        if (description != null) {
            String desc = description.toLowerCase();
            if (desc.contains("高") || desc.contains("严重") || desc.contains("危险")) {
                return "高";
            } else if (desc.contains("中") || desc.contains("注意")) {
                return "中";
            } else {
                return "低";
            }
        }
        
        return "低";
    }

    /**
     * 获取事件持续时长（毫秒）
     * @return 持续时长，如果开始或结束时间为空则返回null
     */
    public Long getEventDurationMillis() {
        if (eventStartTime == null || eventEndTime == null) {
            return null;
        }
        return java.time.Duration.between(eventStartTime, eventEndTime).toMillis();
    }

    /**
     * 获取事件持续时长描述
     * @return 持续时长描述
     */
    public String getEventDurationDescription() {
        Long durationMillis = getEventDurationMillis();
        if (durationMillis == null) {
            return "未知";
        }
        
        long seconds = durationMillis / 1000;
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            return (seconds / 60) + "分钟";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "小时" + (minutes > 0 ? minutes + "分钟" : "");
        }
    }

    /**
     * 判断是否有快照图片
     * @return true如果有快照图片
     */
    public boolean hasSnapshot() {
        return snapshotPath != null && !snapshotPath.trim().isEmpty();
    }

    /**
     * 获取快照文件名
     * @return 快照文件名，如果没有快照则返回null
     */
    public String getSnapshotFileName() {
        if (!hasSnapshot()) {
            return null;
        }
        String path = snapshotPath.trim();
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
}