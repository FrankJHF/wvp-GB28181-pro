package com.genersoft.iot.vmp.analysis.bean.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * VLM作业请求数据传输对象
 * @author Claude
 */
@Data
@Schema(description = "VLM作业请求")
public class VLMJobRequest {

    @Schema(description = "设备ID")
    @JsonProperty("device_id")
    private String deviceId;

    @Schema(description = "通道ID")
    @JsonProperty("channel_id")
    private String channelId;

    @Schema(description = "输入类型")
    @JsonProperty("input_type")
    private String inputType = "rtsp_stream";

    @Schema(description = "输入数据（RTSP地址）")
    @JsonProperty("input_data")
    private String inputData;

    @Schema(description = "回调地址")
    @JsonProperty("callback_url")
    private String callbackUrl;

    @Schema(description = "分析配置参数")
    @JsonProperty("analysis_config")
    private Map<String, Object> analysisConfig;

    @Schema(description = "分析提示词")
    @JsonProperty("analysis_prompt")
    private String analysisPrompt;

    @Schema(description = "模型名称")
    @JsonProperty("model_name")
    private String modelName;

    @Schema(description = "是否自动启动")
    @JsonProperty("auto_start")
    private Boolean autoStart = false;
}