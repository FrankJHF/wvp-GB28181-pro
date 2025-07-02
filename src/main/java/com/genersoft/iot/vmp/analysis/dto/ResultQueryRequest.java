package com.genersoft.iot.vmp.analysis.dto;

/**
 * 分析结果查询请求DTO
 */
public class ResultQueryRequest {

    /**
     * 开始日期 (yyyy-MM-dd HH:mm:ss)
     */
    private String startDate;

    /**
     * 结束日期 (yyyy-MM-dd HH:mm:ss)
     */
    private String endDate;

    /**
     * 任务ID
     */
    private Integer taskId;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 通道ID
     */
    private String channelId;

    /**
     * 关键词搜索
     */
    private String keyword;

    /**
     * 仅显示告警 (true:仅告警, false:全部, null:全部)
     */
    private Boolean onlyAlarm;

    /**
     * 页码 (从1开始)
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;

    /**
     * 排序字段 (默认按时间倒序)
     */
    private String sortBy = "result_timestamp";

    /**
     * 排序方向 (asc/desc)
     */
    private String sortDirection = "desc";

    public ResultQueryRequest() {}

    public ResultQueryRequest(String startDate, String endDate, Integer page, Integer pageSize) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.page = page;
        this.pageSize = pageSize;
    }

    // Getters and Setters
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Boolean getOnlyAlarm() {
        return onlyAlarm;
    }

    public void setOnlyAlarm(Boolean onlyAlarm) {
        this.onlyAlarm = onlyAlarm;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    @Override
    public String toString() {
        return "ResultQueryRequest{" +
                "startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", taskId=" + taskId +
                ", deviceId='" + deviceId + '\'' +
                ", channelId='" + channelId + '\'' +
                ", keyword='" + keyword + '\'' +
                ", onlyAlarm=" + onlyAlarm +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                '}';
    }
}