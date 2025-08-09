package com.genersoft.iot.vmp.analysis.bean;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 分析任务状态枚举
 * @author Claude
 */
@Schema(description = "分析任务状态枚举")
public enum TaskStatus {
    
    @Schema(description = "已创建")
    CREATED("created", "已创建"),
    
    @Schema(description = "启动中")
    STARTING("starting", "启动中"),
    
    @Schema(description = "运行中")
    RUNNING("running", "运行中"),
    
    @Schema(description = "暂停中")
    PAUSING("pausing", "暂停中"),
    
    @Schema(description = "已暂停")
    PAUSED("paused", "已暂停"),
    
    @Schema(description = "恢复中")
    RESUMING("resuming", "恢复中"),
    
    @Schema(description = "停止中")
    STOPPING("stopping", "停止中"),
    
    @Schema(description = "已停止")
    STOPPED("stopped", "已停止"),
    
    @Schema(description = "失败")
    FAILED("failed", "失败"),
    
    @Schema(description = "错误")
    ERROR("error", "错误");

    private final String value;
    private final String description;

    TaskStatus(String value, String description) {
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
     * 判断是否为过渡状态（正在进行操作的状态）
     * @return true如果是过渡状态
     */
    public boolean isTransitioning() {
        return this == STARTING || this == PAUSING || this == RESUMING || this == STOPPING;
    }

    /**
     * 判断是否为稳定状态
     * @return true如果是稳定状态
     */
    public boolean isStable() {
        return !isTransitioning();
    }

    /**
     * 判断是否为终止状态
     * @return true如果是终止状态
     */
    public boolean isTerminated() {
        return this == STOPPED || this == FAILED || this == ERROR;
    }

    /**
     * 判断是否为活跃状态
     * @return true如果是活跃状态
     */
    public boolean isActive() {
        return this == RUNNING || this == STARTING || this == PAUSING || this == RESUMING;
    }

    /**
     * 根据字符串值获取枚举
     * @param value 状态值
     * @return 对应的枚举
     */
    public static TaskStatus fromValue(String value) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}