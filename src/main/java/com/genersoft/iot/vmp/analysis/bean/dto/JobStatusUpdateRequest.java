package com.genersoft.iot.vmp.analysis.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * VLM作业状态更新请求数据传输对象
 * @author Claude
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "VLM作业状态更新请求")
public class JobStatusUpdateRequest {

    @Schema(description = "执行的操作: start, pause, resume", allowableValues = {"start", "pause", "resume"})
    private String action;

    @Schema(description = "强制重启标志（仅用于start操作）")
    private Boolean forceRestart = false;

    /**
     * 构造函数，仅传入action
     * @param action 操作类型
     */
    public JobStatusUpdateRequest(String action) {
        this.action = action;
        this.forceRestart = false;
    }
}