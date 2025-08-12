package com.genersoft.iot.vmp.analysis.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * VLM作业状态查询响应数据传输对象
 * @author Claude
 */
@Data
@Schema(description = "VLM作业状态查询响应")
public class JobStatusResponse {

    @Schema(description = "作业ID")
    @JsonProperty("job_id")
    private String jobId;

    @Schema(description = "作业状态: created, running, paused, failed, cancelled")
    private String status;

    @Schema(description = "设备ID")
    @JsonProperty("device_id")
    private String deviceId;

    @Schema(description = "通道ID")
    @JsonProperty("channel_id")
    private String channelId;

    @Schema(description = "创建时间")
    @JsonProperty("created_at")
    private String createdAt;

    @Schema(description = "更新时间")
    @JsonProperty("updated_at")
    private String updatedAt;

    @Schema(description = "最近的分析结果")
    private VLMAnalysisResult result;

    @Schema(description = "错误消息（仅在failed状态时存在）")
    @JsonProperty("error_message")
    private String errorMessage;

    /**
     * 判断作业是否成功运行
     * @return true如果作业正在运行
     */
    public boolean isRunning() {
        return "running".equals(status);
    }

    /**
     * 判断作业是否失败
     * @return true如果作业失败
     */
    public boolean isFailed() {
        return "failed".equals(status);
    }
}