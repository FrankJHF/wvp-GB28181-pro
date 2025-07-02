package com.genersoft.iot.vmp.analysis.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.ibatis.type.Alias;

import java.util.Objects;

/**
 * 分析任务实体类
 * 用于存储用户创建的、可配置的、持久化的分析任务的配置信息
 *
 * @author AI Assistant
 * @since 2024-01-20
 */
@Alias("AnalysisTask")
@Schema(description = "分析任务实体")
public class AnalysisTask {

    /**
     * 自增主键ID
     */
    @Schema(description = "主键ID", example = "1")
    private Integer id;

    /**
     * 全局唯一的任务标识符
     */
    @Schema(description = "任务ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String taskId;

    /**
     * 用户为任务指定的、易于识别的名称
     */
    @Schema(description = "任务名称", example = "仓库防盗监控")
    private String taskName;

    /**
     * 关联的 WVP 平台中的设备 ID
     */
    @Schema(description = "设备ID", example = "34020000001320000001")
    private String deviceId;

    /**
     * 关联的 WVP 平台中的通道 ID
     */
    @Schema(description = "通道ID", example = "34020000001320000001")
    private String channelId;

    /**
     * 通道名称的快照，用于在查询结果时直接显示，无需二次关联查询
     */
    @Schema(description = "通道名称", example = "监控点1")
    private String channelName;

    /**
     * 用户为该任务设定的、需要视觉语言模型（VLM）回答的核心问题
     */
    @Schema(description = "VLM分析问题", example = "画面中是否有未授权的人员进入？")
    private String vlmQuestion;

    /**
     * 定义视频流被切片并送去分析的频率，单位为秒
     */
    @Schema(description = "视频切片时长(秒)", example = "60")
    private Integer clipDuration;

    /**
     * 任务的当前运行状态
     */
    @Schema(description = "任务状态", example = "RUNNING", allowableValues = {"RUNNING", "STOPPED", "ERROR"})
    private String status;

    /**
     * 记录任务被创建的时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 记录任务状态最后一次发生变化的时间
     */
    @Schema(description = "更新时间")
    private String updateTime;

    /**
     * 任务描述
     */
    @Schema(description = "任务描述")
    private String taskDescription;

    /**
     * 任务开始时间
     */
    @Schema(description = "开始时间")
    private String startTime;

    /**
     * 任务停止时间
     */
    @Schema(description = "停止时间")
    private String stopTime;

    /**
     * VLM分析问题 (别名，与vlmQuestion相同)
     */
    public String getAnalysisQuestion() {
        return vlmQuestion;
    }

    public void setAnalysisQuestion(String analysisQuestion) {
        this.vlmQuestion = analysisQuestion;
    }

    /**
     * 视频切片时长 (别名，与clipDuration相同)
     */
    public Integer getAnalysisInterval() {
        return clipDuration;
    }

    public void setAnalysisInterval(Integer analysisInterval) {
        this.clipDuration = analysisInterval;
    }

    /**
     * 任务状态 (别名，与status相同)
     */
    public String getTaskStatus() {
        return status;
    }

    public void setTaskStatus(String taskStatus) {
        this.status = taskStatus;
    }

        // 构造方法
    public AnalysisTask() {
    }

    public AnalysisTask(String taskId, String taskName, String deviceId, String channelId,
                       String channelName, String vlmQuestion, Integer clipDuration) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.deviceId = deviceId;
        this.channelId = channelId;
        this.channelName = channelName;
        this.vlmQuestion = vlmQuestion;
        this.clipDuration = clipDuration;
        this.status = "STOPPED";
    }

    // Getter和Setter方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

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

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getVlmQuestion() {
        return vlmQuestion;
    }

    public void setVlmQuestion(String vlmQuestion) {
        this.vlmQuestion = vlmQuestion;
    }

    public Integer getClipDuration() {
        return clipDuration;
    }

    public void setClipDuration(Integer clipDuration) {
        this.clipDuration = clipDuration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisTask that = (AnalysisTask) o;
        return Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }

    @Override
    public String toString() {
        return "AnalysisTask{" +
                "id=" + id +
                ", taskId='" + taskId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", channelName='" + channelName + '\'' +
                ", vlmQuestion='" + vlmQuestion + '\'' +
                ", clipDuration=" + clipDuration +
                ", status='" + status + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                '}';
    }
}