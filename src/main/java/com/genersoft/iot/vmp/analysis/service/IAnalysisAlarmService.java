package com.genersoft.iot.vmp.analysis.service;

import com.genersoft.iot.vmp.analysis.bean.AnalysisAlarm;
import com.genersoft.iot.vmp.analysis.bean.AlarmStatus;
import com.genersoft.iot.vmp.conf.exception.ServiceException;
import com.github.pagehelper.PageInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分析告警管理服务接口
 * @author Claude
 */
public interface IAnalysisAlarmService {

    /**
     * 创建分析告警
     * @param alarm 分析告警信息
     * @return 创建成功的告警
     */
    AnalysisAlarm createAlarm(AnalysisAlarm alarm) throws ServiceException;

    /**
     * 更新告警状态
     * @param alarmId 告警ID
     * @param status 告警状态
     * @return 是否更新成功
     */
    boolean updateAlarmStatus(String alarmId, AlarmStatus status) throws ServiceException;

    /**
     * 批量更新告警状态
     * @param alarmIds 告警ID列表
     * @param status 告警状态
     * @return 更新成功的数量
     */
    int batchUpdateAlarmStatus(List<String> alarmIds, AlarmStatus status) throws ServiceException;

    /**
     * 删除告警
     * @param alarmId 告警ID
     * @return 是否删除成功
     */
    boolean deleteAlarm(String alarmId) throws ServiceException;

    /**
     * 根据ID查询告警
     * @param alarmId 告警ID
     * @return 分析告警
     */
    AnalysisAlarm getAlarmById(String alarmId) throws ServiceException;

    /**
     * 根据ID查询告警（包含关联信息）
     * @param alarmId 告警ID
     * @return 分析告警
     */
    AnalysisAlarm getAlarmWithDetailsById(String alarmId) throws ServiceException;

    /**
     * 根据任务ID查询告警
     * @param taskId 任务ID
     * @return 告警列表
     */
    List<AnalysisAlarm> getAlarmsByTaskId(String taskId) throws ServiceException;

    /**
     * 根据设备和通道查询告警
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @return 告警列表
     */
    List<AnalysisAlarm> getAlarmsByDeviceAndChannel(String deviceId, String channelId) throws ServiceException;

    /**
     * 分页查询告警（支持时间范围和条件过滤）
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @param analysisType 分析类型
     * @param status 告警状态
     * @param taskId 任务ID
     * @return 分页结果
     */
    PageInfo<AnalysisAlarm> getAlarmPage(int pageNum, int pageSize, LocalDateTime startTime, LocalDateTime endTime,
                                        String deviceId, String channelId, String analysisType, 
                                        String status, String taskId) throws ServiceException;

    /**
     * 查询最近的告警
     * @param limit 数量限制
     * @return 告警列表
     */
    List<AnalysisAlarm> getRecentAlarms(int limit) throws ServiceException;

    /**
     * 查询待处理告警
     * @return 待处理告警列表
     */
    List<AnalysisAlarm> getPendingAlarms() throws ServiceException;

    /**
     * 统计告警数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @param analysisType 分析类型
     * @param status 告警状态
     * @param taskId 任务ID
     * @return 告警数量
     */
    long countAlarms(LocalDateTime startTime, LocalDateTime endTime, String deviceId, String channelId,
                    String analysisType, String status, String taskId) throws ServiceException;

    /**
     * 根据任务ID统计告警数量
     * @param taskId 任务ID
     * @return 告警数量
     */
    long countAlarmsByTaskId(String taskId) throws ServiceException;

    /**
     * 统计各状态告警数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 状态统计结果
     */
    Map<String, Long> countAlarmsByStatus(LocalDateTime startTime, LocalDateTime endTime) throws ServiceException;

    /**
     * 批量删除告警
     * @param alarmIds 告警ID列表
     * @return 删除成功的数量
     */
    int batchDeleteAlarms(List<String> alarmIds) throws ServiceException;

    /**
     * 根据任务ID删除关联告警
     * @param taskId 任务ID
     * @return 删除成功的数量
     */
    int deleteAlarmsByTaskId(String taskId) throws ServiceException;

    /**
     * 清理指定时间之前的告警
     * @param beforeTime 时间阈值
     * @return 清理的数量
     */
    int cleanupAlarmsBeforeTime(LocalDateTime beforeTime) throws ServiceException;

    /**
     * 处理告警（标记为已处理）
     * @param alarmId 告警ID
     * @return 是否处理成功
     */
    boolean processAlarm(String alarmId) throws ServiceException;

    /**
     * 忽略告警（标记为已忽略）
     * @param alarmId 告警ID
     * @return 是否忽略成功
     */
    boolean ignoreAlarm(String alarmId) throws ServiceException;

    /**
     * 发送告警通知
     * @param alarm 告警信息
     */
    void sendAlarmNotification(AnalysisAlarm alarm) throws ServiceException;

    /**
     * 获取快照图片文件路径
     * @param alarmId 告警ID
     * @return 快照文件路径
     */
    String getSnapshotPath(String alarmId) throws ServiceException;

    /**
     * 检查快照文件是否存在
     * @param alarmId 告警ID
     * @return 快照是否存在
     */
    boolean isSnapshotExists(String alarmId) throws ServiceException;
}