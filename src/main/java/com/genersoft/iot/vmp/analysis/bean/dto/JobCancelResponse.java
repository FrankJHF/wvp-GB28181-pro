package com.genersoft.iot.vmp.analysis.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * VLM作业取消响应数据传输对象
 * @author Claude
 */
@Data
@Schema(description = "VLM作业取消响应")
public class JobCancelResponse {

    @Schema(description = "被取消的作业ID")
    @JsonProperty("job_id")
    private String jobId;

    @Schema(description = "取消操作结果消息")
    private String message;

    @Schema(description = "取消操作时间")
    @JsonProperty("cancelled_at")
    private String cancelledAt;

    /**
     * 判断取消操作是否成功
     * @return true如果取消成功
     */
    public boolean isSuccess() {
        return jobId != null && !jobId.trim().isEmpty() && message != null;
    }
}