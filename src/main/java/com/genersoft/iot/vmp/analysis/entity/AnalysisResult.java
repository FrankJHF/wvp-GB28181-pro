package com.genersoft.iot.vmp.analysis.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.ibatis.type.Alias;

import java.util.Objects;

/**
 * 分析结果实体类
 * 持久化存储每一次视频切片分析后产生的结果，是实现历史查询、统计和报告功能的基础
 *
 * @author AI Assistant
 * @since 2024-01-20
 */
@Alias("AnalysisResult")
@Schema(description = "分析结果实体")
public class AnalysisResult {

    /**
     * 自增主键ID
     */
    @Schema(description = "主键ID", example = "1")
    private Integer id;

    /**
     * 指向 wvp_analysis_tasks 表，表明此结果由哪个任务产生
     */
    @Schema(description = "任务ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String taskId;

    /**
     * 分析完成的时间点，也是视频切片覆盖的结束时间，是数据筛选的核心时间戳
     */
    @Schema(description = "分析完成时间")
    private String resultTime;

    /**
     * 本次分析所使用问题的快照，用于在结果中直接展示（冗余存储）
     */
    @Schema(description = "VLM分析问题", example = "画面中是否有未授权的人员进入？")
    private String vlmQuestion;

    /**
     * VLM 模型返回的完整原始答案
     */
    @Schema(description = "VLM回答", example = "画面中检测到一名未授权人员正在进入受限区域")
    private String vlmAnswer;

    /**
     * 一个由后端业务逻辑判定的标志。如果 VLM 的答案内容符合预设的报警规则，此字段为 true
     */
    @Schema(description = "是否为报警", example = "true")
    private Boolean isAlarm;

    /**
     * 指向该次分析所用视频切片文件的存储路径或 URL，用于潜在的视频回溯功能
     */
    @Schema(description = "视频切片路径", example = "/storage/videos/clip_20240120_103000.mp4")
    private String videoClipPath;

    /**
     * 指向从视频中提取的关键帧图像的存储路径或 URL
     */
    @Schema(description = "关键帧路径", example = "/storage/frames/frame_20240120_103000.jpg")
    private String keyFramePath;

    /**
     * 记录创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 关联的任务信息（非数据库字段，用于查询时关联显示）
     */
    @Schema(description = "关联的任务信息")
    private AnalysisTask task;

        // 构造方法
    public AnalysisResult() {
    }

    public AnalysisResult(String taskId, String vlmQuestion, String vlmAnswer,
                         Boolean isAlarm, String videoClipPath, String keyFramePath) {
        this.taskId = taskId;
        this.vlmQuestion = vlmQuestion;
        this.vlmAnswer = vlmAnswer;
        this.isAlarm = isAlarm;
        this.videoClipPath = videoClipPath;
        this.keyFramePath = keyFramePath;
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

    public String getResultTime() {
        return resultTime;
    }

    public void setResultTime(String resultTime) {
        this.resultTime = resultTime;
    }

    public String getVlmQuestion() {
        return vlmQuestion;
    }

    public void setVlmQuestion(String vlmQuestion) {
        this.vlmQuestion = vlmQuestion;
    }

    public String getVlmAnswer() {
        return vlmAnswer;
    }

    public void setVlmAnswer(String vlmAnswer) {
        this.vlmAnswer = vlmAnswer;
    }

    public Boolean getIsAlarm() {
        return isAlarm;
    }

    public void setIsAlarm(Boolean isAlarm) {
        this.isAlarm = isAlarm;
    }

    public String getVideoClipPath() {
        return videoClipPath;
    }

    public void setVideoClipPath(String videoClipPath) {
        this.videoClipPath = videoClipPath;
    }

    public String getKeyFramePath() {
        return keyFramePath;
    }

    public void setKeyFramePath(String keyFramePath) {
        this.keyFramePath = keyFramePath;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public AnalysisTask getTask() {
        return task;
    }

    public void setTask(AnalysisTask task) {
        this.task = task;
    }

    /**
     * 别名方法，与resultTime相同
     */
    public String getResultTimestamp() {
        return resultTime;
    }

    public void setResultTimestamp(String resultTimestamp) {
        this.resultTime = resultTimestamp;
    }

    /**
     * 别名方法，与vlmAnswer相同
     */
    public String getAnalysisAnswer() {
        return vlmAnswer;
    }

    public void setAnalysisAnswer(String analysisAnswer) {
        this.vlmAnswer = analysisAnswer;
    }

    /**
     * 置信度分数（暂时存储为额外信息，或扩展实体类）
     */
    public void setConfidenceScore(Double confidenceScore) {
        // 暂时不存储，或可以考虑添加confidence字段到实体类
    }

    /**
     * 别名方法，与keyFramePath相同
     */
    public void setFrameImagePath(String frameImagePath) {
        this.keyFramePath = frameImagePath;
    }

    /**
     * 别名方法，与videoClipPath相同
     */
    public void setVideoSegmentPath(String videoSegmentPath) {
        this.videoClipPath = videoSegmentPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisResult that = (AnalysisResult) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "id=" + id +
                ", taskId='" + taskId + '\'' +
                ", resultTime='" + resultTime + '\'' +
                ", vlmQuestion='" + vlmQuestion + '\'' +
                ", vlmAnswer='" + vlmAnswer + '\'' +
                ", isAlarm=" + isAlarm +
                ", videoClipPath='" + videoClipPath + '\'' +
                ", keyFramePath='" + keyFramePath + '\'' +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}