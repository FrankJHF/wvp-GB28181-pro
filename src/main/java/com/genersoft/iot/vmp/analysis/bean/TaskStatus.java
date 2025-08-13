package com.genersoft.iot.vmp.analysis.bean;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 分析任务状态枚举
 * 只包含VLM服务的5个核心状态，移除所有中间态
 * @author Claude
 */
@Schema(description = "分析任务状态枚举")
public enum TaskStatus {
    
    @Schema(description = "已创建")
    CREATED("created", "已创建"),
    
    @Schema(description = "运行中")
    RUNNING("running", "运行中"),
    
    @Schema(description = "已暂停")
    PAUSED("paused", "已暂停"),
    
    @Schema(description = "失败")
    FAILED("failed", "失败"),
    
    @Schema(description = "已取消")
    CANCELLED("cancelled", "已取消");

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
     * 判断是否为终止状态
     * @return true如果是终止状态
     */
    public boolean isTerminated() {
        return this == FAILED || this == CANCELLED;
    }

    /**
     * 判断是否为活跃状态（可以进行操作的状态）
     * @return true如果是活跃状态
     */
    public boolean isActive() {
        return this == RUNNING || this == PAUSED;
    }

    /**
     * 判断是否可以启动
     * @return true如果可以启动
     */
    public boolean canStart() {
        return this == CREATED;
    }

    /**
     * 判断是否可以暂停
     * @return true如果可以暂停
     */
    public boolean canPause() {
        return this == RUNNING;
    }

    /**
     * 判断是否可以恢复
     * @return true如果可以恢复
     */
    public boolean canResume() {
        return this == PAUSED;
    }

    /**
     * 判断是否可以取消
     * @return true如果可以取消
     */
    public boolean canCancel() {
        return this == RUNNING || this == PAUSED;
    }

    /**
     * 判断是否可以删除
     * @return true如果可以删除
     */
    public boolean canDelete() {
        return this == CREATED || this == FAILED || this == CANCELLED;
    }

    /**
     * 根据字符串值获取枚举
     * @param value 状态值
     * @return 对应的枚举
     */
    public static TaskStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (TaskStatus status : TaskStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}