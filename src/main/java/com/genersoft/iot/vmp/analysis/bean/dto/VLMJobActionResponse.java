package com.genersoft.iot.vmp.analysis.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * VLM作业操作响应数据传输对象
 * @author Claude
 */
@Data
@Schema(description = "VLM作业操作响应")
public class VLMJobActionResponse {

    @Schema(description = "作业ID")
    @JsonProperty("job_id")
    private String jobId;

    @Schema(description = "操作前状态")
    @JsonProperty("previous_status")
    private String previousStatus;

    @Schema(description = "操作后状态")
    @JsonProperty("current_status")
    private String currentStatus;

    @Schema(description = "操作结果消息")
    private String message;

    @Schema(description = "操作时间")
    @JsonProperty("action_timestamp")
    private String actionTimestamp;

    @Schema(description = "错误码")
    @JsonProperty("error_code")
    private String errorCode;

    @Schema(description = "错误详情")
    @JsonProperty("error_details")
    private String errorDetails;

    /**
     * 判断操作是否成功
     * @return true如果操作成功
     */
    public boolean isSuccess() {
        return errorCode == null || "SUCCESS".equals(errorCode);
    }

    /**
     * 判断状态是否发生变化
     * @return true如果状态发生变化
     */
    public boolean isStatusChanged() {
        return previousStatus != null && currentStatus != null && 
               !previousStatus.equals(currentStatus);
    }

    /**
     * 获取错误信息
     * @return 错误信息
     */
    public String getErrorInfo() {
        if (errorCode != null && !"SUCCESS".equals(errorCode)) {
            return errorCode + (errorDetails != null ? ": " + errorDetails : "");
        }
        return null;
    }
}