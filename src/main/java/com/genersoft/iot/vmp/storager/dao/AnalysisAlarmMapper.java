package com.genersoft.iot.vmp.storager.dao;

import com.genersoft.iot.vmp.analysis.bean.AnalysisAlarm;
import com.genersoft.iot.vmp.analysis.bean.AlarmStatus;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分析告警数据访问层
 * @author Claude
 */
@Mapper
@Repository
public interface AnalysisAlarmMapper {

    /**
     * 新增分析告警
     */
    @Insert("INSERT INTO wvp_analysis_alarm (id, task_id, device_id, device_name, channel_id, channel_name, " +
            "analysis_type, description, snapshot_path, alarm_time, event_start_time, event_end_time, " +
            "event_time_range, video_window_info, status, created_at) " +
            "VALUES (#{id}, #{taskId}, #{deviceId}, #{deviceName}, #{channelId}, #{channelName}, " +
            "#{analysisType}, #{description}, #{snapshotPath}, #{alarmTime}, #{eventStartTime}, #{eventEndTime}, " +
            "#{eventTimeRange}, #{videoWindowInfo,typeHandler=com.genersoft.iot.vmp.utils.JsonTypeHandler}, " +
            "#{status}, #{createdAt})")
    int insert(AnalysisAlarm alarm);

    /**
     * 更新告警状态
     */
    @Update("UPDATE wvp_analysis_alarm SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") String id, @Param("status") String status);

    /**
     * 批量更新告警状态
     */
    @Update({" <script>" +
            "UPDATE wvp_analysis_alarm SET status = #{status} " +
            "WHERE id IN " +
            "<foreach collection=\"alarmIds\" item=\"alarmId\" open=\"(\" separator=\",\" close=\")\">" +
            "#{alarmId}" +
            "</foreach>" +
            " </script>"})
    int batchUpdateStatus(@Param("alarmIds") List<String> alarmIds, @Param("status") String status);

    /**
     * 删除告警
     */
    @Delete("DELETE FROM wvp_analysis_alarm WHERE id = #{id}")
    int delete(@Param("id") String id);

    /**
     * 根据ID查询告警
     */
    @Select("SELECT id, task_id as taskId, device_id as deviceId, device_name as deviceName, " +
            "channel_id as channelId, channel_name as channelName, analysis_type as analysisType, " +
            "description, snapshot_path as snapshotPath, alarm_time as alarmTime, " +
            "event_start_time as eventStartTime, event_end_time as eventEndTime, event_time_range as eventTimeRange, " +
            "video_window_info as videoWindowInfo, status, created_at as createdAt " +
            "FROM wvp_analysis_alarm WHERE id = #{id}")
    @Results({
        @Result(property = "videoWindowInfo", column = "video_window_info", 
                typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class),
        @Result(property = "status", column = "status", javaType = AlarmStatus.class)
    })
    AnalysisAlarm selectById(@Param("id") String id);

    /**
     * 根据任务ID查询告警
     */
    @Select("SELECT id, task_id as taskId, device_id as deviceId, device_name as deviceName, " +
            "channel_id as channelId, channel_name as channelName, analysis_type as analysisType, " +
            "description, snapshot_path as snapshotPath, alarm_time as alarmTime, " +
            "event_start_time as eventStartTime, event_end_time as eventEndTime, event_time_range as eventTimeRange, " +
            "video_window_info as videoWindowInfo, status, created_at as createdAt " +
            "FROM wvp_analysis_alarm WHERE task_id = #{taskId} ORDER BY alarm_time DESC")
    @Results({
        @Result(property = "videoWindowInfo", column = "video_window_info", 
                typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class),
        @Result(property = "status", column = "status", javaType = AlarmStatus.class)
    })
    List<AnalysisAlarm> selectByTaskId(@Param("taskId") String taskId);

    /**
     * 根据设备和通道查询告警
     */
    @Select("SELECT id, task_id as taskId, device_id as deviceId, device_name as deviceName, " +
            "channel_id as channelId, channel_name as channelName, analysis_type as analysisType, " +
            "description, snapshot_path as snapshotPath, alarm_time as alarmTime, " +
            "event_start_time as eventStartTime, event_end_time as eventEndTime, event_time_range as eventTimeRange, " +
            "video_window_info as videoWindowInfo, status, created_at as createdAt " +
            "FROM wvp_analysis_alarm WHERE device_id = #{deviceId} AND channel_id = #{channelId} " +
            "ORDER BY alarm_time DESC")
    @Results({
        @Result(property = "videoWindowInfo", column = "video_window_info", 
                typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class),
        @Result(property = "status", column = "status", javaType = AlarmStatus.class)
    })
    List<AnalysisAlarm> selectByDeviceAndChannel(@Param("deviceId") String deviceId, @Param("channelId") String channelId);

    /**
     * 查询告警列表（支持时间范围和条件过滤）
     */
    @Select({" <script>" +
            "SELECT id, task_id as taskId, device_id as deviceId, device_name as deviceName, " +
            "channel_id as channelId, channel_name as channelName, analysis_type as analysisType, " +
            "description, snapshot_path as snapshotPath, alarm_time as alarmTime, " +
            "event_start_time as eventStartTime, event_end_time as eventEndTime, event_time_range as eventTimeRange, " +
            "video_window_info as videoWindowInfo, status, created_at as createdAt " +
            "FROM wvp_analysis_alarm " +
            "<where>" +
            "<if test=\"startTime != null\"> AND alarm_time &gt;= #{startTime}</if>" +
            "<if test=\"endTime != null\"> AND alarm_time &lt;= #{endTime}</if>" +
            "<if test=\"deviceId != null and deviceId != ''\"> AND device_id = #{deviceId}</if>" +
            "<if test=\"channelId != null and channelId != ''\"> AND channel_id = #{channelId}</if>" +
            "<if test=\"analysisType != null and analysisType != ''\"> AND analysis_type = #{analysisType}</if>" +
            "<if test=\"status != null\"> AND status = #{status}</if>" +
            "<if test=\"taskId != null and taskId != ''\"> AND task_id = #{taskId}</if>" +
            "</where>" +
            "ORDER BY alarm_time DESC" +
            " </script>"})
    @Results({
        @Result(property = "videoWindowInfo", column = "video_window_info", 
                typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class),
        @Result(property = "status", column = "status", javaType = AlarmStatus.class)
    })
    List<AnalysisAlarm> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime,
                                         @Param("deviceId") String deviceId,
                                         @Param("channelId") String channelId,
                                         @Param("analysisType") String analysisType,
                                         @Param("status") String status,
                                         @Param("taskId") String taskId);

    /**
     * 查询最近的告警
     */
    @Select("SELECT id, task_id as taskId, device_id as deviceId, device_name as deviceName, " +
            "channel_id as channelId, channel_name as channelName, analysis_type as analysisType, " +
            "description, snapshot_path as snapshotPath, alarm_time as alarmTime, " +
            "event_start_time as eventStartTime, event_end_time as eventEndTime, event_time_range as eventTimeRange, " +
            "video_window_info as videoWindowInfo, status, created_at as createdAt " +
            "FROM wvp_analysis_alarm " +
            "ORDER BY alarm_time DESC LIMIT #{limit}")
    @Results({
        @Result(property = "videoWindowInfo", column = "video_window_info", 
                typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class),
        @Result(property = "status", column = "status", javaType = AlarmStatus.class)
    })
    List<AnalysisAlarm> selectRecent(@Param("limit") int limit);

    /**
     * 查询待处理告警
     */
    @Select("SELECT id, task_id as taskId, device_id as deviceId, device_name as deviceName, " +
            "channel_id as channelId, channel_name as channelName, analysis_type as analysisType, " +
            "description, snapshot_path as snapshotPath, alarm_time as alarmTime, " +
            "event_start_time as eventStartTime, event_end_time as eventEndTime, event_time_range as eventTimeRange, " +
            "video_window_info as videoWindowInfo, status, created_at as createdAt " +
            "FROM wvp_analysis_alarm WHERE status = 'pending' ORDER BY alarm_time DESC")
    @Results({
        @Result(property = "videoWindowInfo", column = "video_window_info", 
                typeHandler = com.genersoft.iot.vmp.utils.JsonTypeHandler.class),
        @Result(property = "status", column = "status", javaType = AlarmStatus.class)
    })
    List<AnalysisAlarm> selectPending();

    /**
     * 统计告警数量
     */
    @Select({" <script>" +
            "SELECT COUNT(*) FROM wvp_analysis_alarm " +
            "<where>" +
            "<if test=\"startTime != null\"> AND alarm_time &gt;= #{startTime}</if>" +
            "<if test=\"endTime != null\"> AND alarm_time &lt;= #{endTime}</if>" +
            "<if test=\"deviceId != null and deviceId != ''\"> AND device_id = #{deviceId}</if>" +
            "<if test=\"channelId != null and channelId != ''\"> AND channel_id = #{channelId}</if>" +
            "<if test=\"analysisType != null and analysisType != ''\"> AND analysis_type = #{analysisType}</if>" +
            "<if test=\"status != null\"> AND status = #{status}</if>" +
            "<if test=\"taskId != null and taskId != ''\"> AND task_id = #{taskId}</if>" +
            "</where>" +
            " </script>"})
    long count(@Param("startTime") LocalDateTime startTime,
               @Param("endTime") LocalDateTime endTime,
               @Param("deviceId") String deviceId,
               @Param("channelId") String channelId,
               @Param("analysisType") String analysisType,
               @Param("status") String status,
               @Param("taskId") String taskId);

    /**
     * 根据任务ID统计告警数量
     */
    @Select("SELECT COUNT(*) FROM wvp_analysis_alarm WHERE task_id = #{taskId}")
    long countByTaskId(@Param("taskId") String taskId);

    /**
     * 统计各状态告警数量
     */
    @Select("SELECT status, COUNT(*) as count FROM wvp_analysis_alarm " +
            "WHERE alarm_time >= #{startTime} AND alarm_time <= #{endTime} " +
            "GROUP BY status")
    List<java.util.Map<String, Object>> countByStatus(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的告警
     */
    @Delete("DELETE FROM wvp_analysis_alarm WHERE created_at < #{beforeTime}")
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 根据任务ID删除关联告警
     */
    @Delete("DELETE FROM wvp_analysis_alarm WHERE task_id = #{taskId}")
    int deleteByTaskId(@Param("taskId") String taskId);
}