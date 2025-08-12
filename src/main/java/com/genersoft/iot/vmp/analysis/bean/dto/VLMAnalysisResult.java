package com.genersoft.iot.vmp.analysis.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.genersoft.iot.vmp.analysis.bean.VideoWindowInfo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VLM分析结果数据传输对象
 * @author Claude
 */
@Data
@Schema(description = "VLM分析结果")
public class VLMAnalysisResult {

    @Schema(description = "作业ID")
    @JsonProperty("job_id")
    private String jobId;

    @Schema(description = "设备ID")
    @JsonProperty("device_id")
    private String deviceId;

    @Schema(description = "通道ID")
    @JsonProperty("channel_id")
    private String channelId;

    @Schema(description = "分析时间戳UTC")
    @JsonProperty("analysis_timestamp_utc")
    private String analysisTimestampUtc;

    @Schema(description = "分析时间戳（兼容字段）")
    @JsonProperty("analysis_timestamp")
    private String analysisTimestamp;

    @Schema(description = "视频窗口信息")
    @JsonProperty("video_window")
    private VideoWindow videoWindow;

    @Schema(description = "事件列表")
    private List<Event> events;

    /**
     * 视频窗口信息
     */
    @Data
    @Schema(description = "视频窗口")
    public static class VideoWindow {
        
        @Schema(description = "流开始UTC时间")
        @JsonProperty("stream_start_utc")
        private String streamStartUtc;

        @Schema(description = "流结束UTC时间")
        @JsonProperty("stream_end_utc")
        private String streamEndUtc;

        @Schema(description = "流开始PTS")
        @JsonProperty("stream_start_pts")
        private Double streamStartPts;

        @Schema(description = "流结束PTS")
        @JsonProperty("stream_end_pts")
        private Double streamEndPts;
    }

    /**
     * 事件信息
     */
    @Data
    @Schema(description = "分析事件")
    public static class Event {

        @Schema(description = "事件开始PTS")
        @JsonProperty("event_start_pts")
        private Double eventStartPts;

        @Schema(description = "事件结束PTS")
        @JsonProperty("event_end_pts")
        private Double eventEndPts;

        @Schema(description = "事件开始UTC时间")
        @JsonProperty("event_start_utc")
        private String eventStartUtc;

        @Schema(description = "事件结束UTC时间")
        @JsonProperty("event_end_utc")
        private String eventEndUtc;

        @Schema(description = "事件时间范围描述")
        @JsonProperty("event_time_range")
        private String eventTimeRange;

        @Schema(description = "事件描述")
        @JsonProperty("event_description")
        private String eventDescription;

        @Schema(description = "是否存在紧急情况")
        @JsonProperty("emergency_exist")
        private Boolean emergencyExist;

        @Schema(description = "快照图片Base64数据")
        @JsonProperty("snapshot_base64")
        private String snapshotBase64;
    }

    /**
     * 判断是否有紧急事件
     * @return true如果有紧急事件
     */
    public boolean hasEmergencyEvents() {
        if (events == null || events.isEmpty()) {
            return false;
        }
        return events.stream().anyMatch(event -> 
            event.getEmergencyExist() != null && event.getEmergencyExist());
    }

    /**
     * 获取紧急事件数量
     * @return 紧急事件数量
     */
    public long getEmergencyEventCount() {
        if (events == null || events.isEmpty()) {
            return 0;
        }
        return events.stream().mapToLong(event -> 
            (event.getEmergencyExist() != null && event.getEmergencyExist()) ? 1 : 0).sum();
    }

    /**
     * 获取包含快照的事件数量
     * @return 包含快照的事件数量
     */
    public long getEventsWithSnapshotCount() {
        if (events == null || events.isEmpty()) {
            return 0;
        }
        return events.stream().mapToLong(event -> 
            (event.getSnapshotBase64() != null && !event.getSnapshotBase64().trim().isEmpty()) ? 1 : 0).sum();
    }
}