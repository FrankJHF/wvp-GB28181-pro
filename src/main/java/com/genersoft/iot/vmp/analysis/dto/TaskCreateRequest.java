package com.genersoft.iot.vmp.analysis.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/**
 * 创建分析任务请求DTO
 */
public class TaskCreateRequest {

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    /**
     * 设备ID
     */
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    /**
     * 通道ID
     */
    @NotBlank(message = "通道ID不能为空")
    private String channelId;

    /**
     * 分析问题/提示词
     */
    @NotBlank(message = "分析问题不能为空")
    private String analysisQuestion;

    /**
     * 分析频率（秒）
     */
    @NotNull(message = "分析频率不能为空")
    @Min(value = 10, message = "分析频率不能少于10秒")
    @Max(value = 3600, message = "分析频率不能超过3600秒")
    private Integer analysisInterval;

    /**
     * 任务描述
     */
    private String taskDescription;

    /**
     * 是否自动启动
     */
    private Boolean autoStart = false;

    public TaskCreateRequest() {}

    public TaskCreateRequest(String taskName, String deviceId, String channelId,
                           String analysisQuestion, Integer analysisInterval) {
        this.taskName = taskName;
        this.deviceId = deviceId;
        this.channelId = channelId;
        this.analysisQuestion = analysisQuestion;
        this.analysisInterval = analysisInterval;
    }

    // Getters and Setters
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getAnalysisQuestion() {
        return analysisQuestion;
    }

    public void setAnalysisQuestion(String analysisQuestion) {
        this.analysisQuestion = analysisQuestion;
    }

    public Integer getAnalysisInterval() {
        return analysisInterval;
    }

    public void setAnalysisInterval(Integer analysisInterval) {
        this.analysisInterval = analysisInterval;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public Boolean getAutoStart() {
        return autoStart;
    }

    public void setAutoStart(Boolean autoStart) {
        this.autoStart = autoStart;
    }

    @Override
    public String toString() {
        return "TaskCreateRequest{" +
                "taskName='" + taskName + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", analysisQuestion='" + analysisQuestion + '\'' +
                ", analysisInterval=" + analysisInterval +
                ", taskDescription='" + taskDescription + '\'' +
                ", autoStart=" + autoStart +
                '}';
    }
}