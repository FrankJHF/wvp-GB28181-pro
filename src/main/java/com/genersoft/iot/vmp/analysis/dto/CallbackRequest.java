package com.genersoft.iot.vmp.analysis.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

/**
 * Python微服务回调请求DTO
 */
public class CallbackRequest {

    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Integer taskId;

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
     * 分析问题
     */
    @NotBlank(message = "分析问题不能为空")
    private String question;

    /**
     * VLM回答
     */
    @NotBlank(message = "VLM回答不能为空")
    private String answer;

    /**
     * 是否为告警
     */
    private Boolean isAlarm = false;

    /**
     * 置信度 (0.0-1.0)
     */
    private Double confidence;

    /**
     * 关键帧图片URL
     */
    private String keyFrameUrl;

    /**
     * 视频片段URL
     */
    private String videoSegmentUrl;

    /**
     * 分析结果时间戳 (yyyy-MM-dd HH:mm:ss)
     */
    @NotBlank(message = "结果时间戳不能为空")
    private String resultTimestamp;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;

    /**
     * 额外信息（JSON格式）
     */
    private String extraInfo;

    public CallbackRequest() {}

    public CallbackRequest(Integer taskId, String deviceId, String channelId,
                         String question, String answer, String resultTimestamp) {
        this.taskId = taskId;
        this.deviceId = deviceId;
        this.channelId = channelId;
        this.question = question;
        this.answer = answer;
        this.resultTimestamp = resultTimestamp;
    }

    // Getters and Setters
    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Boolean getIsAlarm() {
        return isAlarm;
    }

    public void setIsAlarm(Boolean isAlarm) {
        this.isAlarm = isAlarm;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getKeyFrameUrl() {
        return keyFrameUrl;
    }

    public void setKeyFrameUrl(String keyFrameUrl) {
        this.keyFrameUrl = keyFrameUrl;
    }

    public String getVideoSegmentUrl() {
        return videoSegmentUrl;
    }

    public void setVideoSegmentUrl(String videoSegmentUrl) {
        this.videoSegmentUrl = videoSegmentUrl;
    }

    public String getResultTimestamp() {
        return resultTimestamp;
    }

    public void setResultTimestamp(String resultTimestamp) {
        this.resultTimestamp = resultTimestamp;
    }

    public Long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Long processingTime) {
        this.processingTime = processingTime;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    /**
     * 别名方法，与answer相同
     */
    public String getAnalysisAnswer() {
        return answer;
    }

    public void setAnalysisAnswer(String analysisAnswer) {
        this.answer = analysisAnswer;
    }

    /**
     * 别名方法，与confidence相同
     */
    public Double getConfidenceScore() {
        return confidence;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidence = confidenceScore;
    }

    /**
     * 别名方法，与keyFrameUrl相同
     */
    public String getFrameImagePath() {
        return keyFrameUrl;
    }

    public void setFrameImagePath(String frameImagePath) {
        this.keyFrameUrl = frameImagePath;
    }

    /**
     * 别名方法，与videoSegmentUrl相同
     */
    public String getVideoSegmentPath() {
        return videoSegmentUrl;
    }

    public void setVideoSegmentPath(String videoSegmentPath) {
        this.videoSegmentUrl = videoSegmentPath;
    }

    /**
     * 别名方法，返回Base64编码的帧数据
     */
    public String getFrameData() {
        // 这里可以实现从keyFrameUrl读取并转换为Base64的逻辑
        // 现在返回null，表示数据在URL中
        return null;
    }

    @Override
    public String toString() {
        return "CallbackRequest{" +
                "taskId=" + taskId +
                ", deviceId='" + deviceId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                ", isAlarm=" + isAlarm +
                ", confidence=" + confidence +
                ", keyFrameUrl='" + keyFrameUrl + '\'' +
                ", videoSegmentUrl='" + videoSegmentUrl + '\'' +
                ", resultTimestamp='" + resultTimestamp + '\'' +
                ", processingTime=" + processingTime +
                ", extraInfo='" + extraInfo + '\'' +
                '}';
    }
}