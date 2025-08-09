package com.genersoft.iot.vmp.analysis.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * VLM作业响应数据传输对象
 * @author Claude
 */
@Data
@Schema(description = "VLM作业响应")
public class VLMJobResponse {

    @Schema(description = "作业ID")
    @JsonProperty("job_id")
    private String jobId;

    @Schema(description = "作业状态")
    private String status;

    @Schema(description = "响应消息")
    private String message;

    @Schema(description = "创建时间")
    @JsonProperty("created_at")
    private String createdAt;

    @Schema(description = "错误码")
    @JsonProperty("error_code")
    private String errorCode;

    @Schema(description = "错误详情")
    @JsonProperty("error_details")
    private String errorDetails;

    /**
     * 判断响应是否成功
     * @return true如果成功
     */
    public boolean isSuccess() {
        return errorCode == null || "SUCCESS".equals(errorCode);
    }

    /**
     * 判断作业是否已创建
     * @return true如果作业已创建
     */
    public boolean isJobCreated() {
        return jobId != null && !jobId.trim().isEmpty();
    }
}