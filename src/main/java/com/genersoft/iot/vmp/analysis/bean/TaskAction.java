package com.genersoft.iot.vmp.analysis.bean;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 任务操作枚举
 * @author Claude
 */
@Schema(description = "任务操作枚举")
public enum TaskAction {
    
    @Schema(description = "启动")
    START("start", "启动"),
    
    @Schema(description = "暂停")
    PAUSE("pause", "暂停"),
    
    @Schema(description = "恢复")
    RESUME("resume", "恢复"),
    
    @Schema(description = "停止")
    STOP("stop", "停止"),
    
    @Schema(description = "删除")
    DELETE("delete", "删除");

    private final String value;
    private final String description;

    TaskAction(String value, String description) {
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
     * 根据字符串值获取枚举
     * @param value 操作值
     * @return 对应的枚举
     */
    public static TaskAction fromValue(String value) {
        for (TaskAction action : TaskAction.values()) {
            if (action.value.equals(value)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown task action: " + value);
    }
}