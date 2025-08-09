package com.genersoft.iot.vmp.analysis.bean;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 分析告警状态枚举
 * @author Claude
 */
@Schema(description = "分析告警状态枚举")
public enum AlarmStatus {
    
    @Schema(description = "待处理")
    PENDING("pending", "待处理"),
    
    @Schema(description = "处理中")
    PROCESSING("processing", "处理中"),
    
    @Schema(description = "已解决")
    RESOLVED("resolved", "已解决"),
    
    @Schema(description = "已忽略")
    IGNORED("ignored", "已忽略");

    private final String value;
    private final String description;

    AlarmStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断是否为未处理状态
     * @return true如果是未处理状态
     */
    public boolean isUnprocessed() {
        return this == PENDING;
    }

    /**
     * 判断是否为已处理状态
     * @return true如果是已处理状态
     */
    public boolean isProcessed() {
        return this == RESOLVED || this == IGNORED;
    }

    /**
     * 根据字符串值获取枚举
     * @param value 状态值
     * @return 对应的枚举
     */
    public static AlarmStatus fromValue(String value) {
        for (AlarmStatus status : AlarmStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown alarm status: " + value);
    }
}