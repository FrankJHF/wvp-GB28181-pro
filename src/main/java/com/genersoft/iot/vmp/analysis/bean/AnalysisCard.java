package com.genersoft.iot.vmp.analysis.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 智能分析卡片实体类
 * @author Claude
 */
@Data
@Schema(description = "智能分析卡片")
public class AnalysisCard {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "卡片标题")
    private String title;

    @Schema(description = "卡片描述")
    private String description;

    @Schema(description = "卡片图标URL")
    private String icon;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "分析提示词")
    private String prompt;

    @Schema(description = "模型类型")
    @JsonProperty("model_type")
    private String modelType;

    @Schema(description = "VLM分析配置参数")
    @JsonProperty("analysis_config")
    private Map<String, Object> analysisConfig;

    @Schema(description = "创建人")
    @JsonProperty("created_by")
    private String createdBy;

    @Schema(description = "创建时间")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}