package com.genersoft.iot.vmp.analysis.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Max;

/**
 * 更新分析任务请求DTO
 */
public class TaskUpdateRequest {

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 分析问题/提示词
     */
    private String analysisQuestion;

    /**
     * 分析频率（秒）
     */
    @Min(value = 10, message = "分析频率不能少于10秒")
    @Max(value = 3600, message = "分析频率不能超过3600秒")
    private Integer analysisInterval;

    /**
     * 任务描述
     */
    private String taskDescription;

    public TaskUpdateRequest() {}

    public TaskUpdateRequest(String taskName, String analysisQuestion, Integer analysisInterval) {
        this.taskName = taskName;
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

    @Override
    public String toString() {
        return "TaskUpdateRequest{" +
                "taskName='" + taskName + '\'' +
                ", analysisQuestion='" + analysisQuestion + '\'' +
                ", analysisInterval=" + analysisInterval +
                ", taskDescription='" + taskDescription + '\'' +
                '}';
    }
}