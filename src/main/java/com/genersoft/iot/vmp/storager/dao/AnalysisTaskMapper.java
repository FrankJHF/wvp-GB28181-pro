package com.genersoft.iot.vmp.storager.dao;

import com.genersoft.iot.vmp.analysis.bean.AnalysisTask;
import com.genersoft.iot.vmp.analysis.bean.TaskStatus;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分析任务数据访问层
 * @author Claude
 */
@Mapper
@Repository
public interface AnalysisTaskMapper {

    /**
     * 新增分析任务
     */
    @Insert("INSERT INTO wvp_analysis_task (id, task_name, analysis_card_id, device_id, device_name, channel_id, channel_name, " +
            "rtsp_url, status, vlm_job_id, config, error_message, last_active_time, last_status_sync, created_by, created_at, updated_at) " +
            "VALUES (#{id}, #{taskName}, #{analysisCardId}, #{deviceId}, #{deviceName}, #{channelId}, #{channelName}, " +
            "#{rtspUrl}, #{status}, #{vlmJobId}, #{config,typeHandler=com.genersoft.iot.vmp.utils.JsonTypeHandler}, " +
            "#{errorMessage}, #{lastActiveTime}, #{lastStatusSync}, #{createdBy}, #{createdAt}, #{updatedAt})")
    int insert(AnalysisTask task);

    /**
     * 更新分析任务
     */
    @Update({" <script>" +
            "UPDATE wvp_analysis_task " +
            "SET updated_at = NOW() " +
            "<if test=\"taskName != null\">, task_name = #{taskName}</if>" +
            "<if test=\"deviceName != null\">, device_name = #{deviceName}</if>" +
            "<if test=\"channelName != null\">, channel_name = #{channelName}</if>" +
            "<if test=\"rtspUrl != null\">, rtsp_url = #{rtspUrl}</if>" +
            "<if test=\"status != null\">, status = #{status}</if>" +
            "<if test=\"vlmJobId != null\">, vlm_job_id = #{vlmJobId}</if>" +
            "<if test=\"config != null\">, config = #{config,typeHandler=com.genersoft.iot.vmp.utils.JsonTypeHandler}</if>" +
            "<if test=\"errorMessage != null\">, error_message = #{errorMessage}</if>" +
            "<if test=\"lastActiveTime != null\">, last_active_time = #{lastActiveTime}</if>" +
            "<if test=\"lastStatusSync != null\">, last_status_sync = #{lastStatusSync}</if>" +
            "WHERE id = #{id}" +
            " </script>"})
    int update(AnalysisTask task);

    /**
     * 删除分析任务
     */
    @Delete("DELETE FROM wvp_analysis_task WHERE id = #{id}")
    int delete(@Param("id") String id);

    /**
     * 根据ID查询分析任务
     */
    @Select("SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task WHERE id = #{id}")
    AnalysisTask selectById(@Param("id") String id);

    /**
     * 根据VLM作业ID查询分析任务
     */
    @Select("SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task WHERE vlm_job_id = #{vlmJobId}")
    AnalysisTask selectByVlmJobId(@Param("vlmJobId") String vlmJobId);

    /**
     * 根据设备和通道查询任务
     */
    @Select("SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task WHERE device_id = #{deviceId} AND channel_id = #{channelId}")
    List<AnalysisTask> selectByDeviceAndChannel(@Param("deviceId") String deviceId, @Param("channelId") String channelId);

    /**
     * 根据状态查询任务
     */
    @Select({" <script>" +
            "SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task " +
            "<where>" +
            "<if test=\"statuses != null and statuses.size() > 0\">" +
            "status IN " +
            "<foreach collection=\"statuses\" item=\"status\" open=\"(\" separator=\",\" close=\" )\">" +
            "#{status}" +
            "</foreach>" +
            "</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            " </script>"})
    List<AnalysisTask> selectByStatuses(@Param("statuses") List<String> statuses);

    /**
     * 查询所有任务（支持条件查询）
     */
    @Select({" <script>" +
            "SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task " +
            "<where>" +
            "<if test=\"deviceId != null and deviceId != ''\"> AND device_id = #{deviceId}</if>" +
            "<if test=\"channelId != null and channelId != ''\"> AND channel_id = #{channelId}</if>" +
            "<if test=\"analysisCardId != null and analysisCardId != ''\"> AND analysis_card_id = #{analysisCardId}</if>" +
            "<if test=\"status != null\"> AND status = #{status}</if>" +
            "<if test=\"createdBy != null and createdBy != ''\"> AND created_by = #{createdBy}</if>" +
            "<if test=\"taskName != null and taskName != ''\"> AND task_name LIKE CONCAT('%', #{taskName}, '%')</if>" +
            "</where>" +
            "ORDER BY created_at DESC" +
            " </script>"})
    List<AnalysisTask> selectAll(@Param("deviceId") String deviceId,
                                @Param("channelId") String channelId,
                                @Param("analysisCardId") String analysisCardId,
                                @Param("status") String status,
                                @Param("createdBy") String createdBy,
                                @Param("taskName") String taskName);

    /**
     * 查询需要状态同步的任务
     */
    @Select("SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task " +
            "WHERE status IN ('running', 'paused') " +
            "AND (last_status_sync IS NULL OR last_status_sync < #{beforeTime})")
    List<AnalysisTask> selectTasksNeedSync(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 统计任务数量
     */
    @Select({" <script>" +
            "SELECT COUNT(*) FROM wvp_analysis_task " +
            "<where>" +
            "<if test=\"deviceId != null and deviceId != ''\"> AND device_id = #{deviceId}</if>" +
            "<if test=\"status != null\"> AND status = #{status}</if>" +
            "<if test=\"createdBy != null and createdBy != ''\"> AND created_by = #{createdBy}</if>" +
            "</where>" +
            " </script>"})
    long count(@Param("deviceId") String deviceId,
               @Param("status") String status,
               @Param("createdBy") String createdBy);

    /**
     * 根据分析卡片ID统计关联任务数
     */
    @Select("SELECT COUNT(*) FROM wvp_analysis_task WHERE analysis_card_id = #{analysisCardId}")
    long countByAnalysisCardId(@Param("analysisCardId") String analysisCardId);

    /**
     * 批量更新任务状态
     */
    @Update({" <script>" +
            "UPDATE wvp_analysis_task SET status = #{status}, updated_at = NOW() " +
            "<if test=\"errorMessage != null\">, error_message = #{errorMessage}</if>" +
            "WHERE id IN " +
            "<foreach collection=\"taskIds\" item=\"taskId\" open=\"(\" separator=\",\" close=\" )\">" +
            "#{taskId}" +
            "</foreach>" +
            " </script>"})
    int batchUpdateStatus(@Param("taskIds") List<String> taskIds, 
                         @Param("status") TaskStatus status,
                         @Param("errorMessage") String errorMessage);

    /**
     * 根据状态查询任务
     */
    @Select("SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task WHERE status = #{status} ORDER BY created_at DESC")
    List<AnalysisTask> selectByStatus(@Param("status") TaskStatus status);

    /**
     * 根据设备ID查询任务
     */
    @Select("SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task WHERE device_id = #{deviceId} ORDER BY created_at DESC")
    List<AnalysisTask> selectByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 根据通道ID查询任务
     */
    @Select("SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task WHERE channel_id = #{channelId} ORDER BY created_at DESC")
    List<AnalysisTask> selectByChannelId(@Param("channelId") String channelId);

    /**
     * 查询活跃任务
     */
    @Select("SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task WHERE status IN ('running', 'paused') ORDER BY created_at DESC")
    List<AnalysisTask> selectActiveTasks();

    /**
     * 查询需要状态同步的任务（重载方法）
     */
    @Select("SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task " +
            "WHERE status IN ('running', 'paused') " +
            "AND (last_status_sync IS NULL OR last_status_sync < DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE))")
    List<AnalysisTask> selectTasksNeedingStatusSync(@Param("minutes") int minutes);

    /**
     * 统计任务数量（重载方法）
     */
    @Select({" <script>" +
            "SELECT COUNT(*) FROM wvp_analysis_task " +
            "<where>" +
            "<if test=\"status != null\"> AND status = #{status}</if>" +
            "<if test=\"createdBy != null and createdBy != ''\"> AND created_by = #{createdBy}</if>" +
            "</where>" +
            " </script>"})
    long countTasks(@Param("status") TaskStatus status, @Param("createdBy") String createdBy);

    /**
     * 根据分析卡片ID查询任务
     */
    @Select("SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task WHERE analysis_card_id = #{analysisCardId} ORDER BY created_at DESC")
    List<AnalysisTask> selectByAnalysisCardId(@Param("analysisCardId") String analysisCardId);

    /**
     * 根据状态和创建者查询任务
     */
    @Select("SELECT id, task_name, analysis_card_id, device_id, device_name, " +
            "channel_id, channel_name, rtsp_url, status, vlm_job_id, " +
            "config, error_message, last_active_time, last_status_sync, " +
            "created_by, created_at, updated_at " +
            "FROM wvp_analysis_task WHERE status = #{status} AND created_by = #{createdBy} ORDER BY created_at DESC")
    List<AnalysisTask> selectByStatusAndCreatedBy(@Param("status") TaskStatus status, @Param("createdBy") String createdBy);

    /**
     * 更新任务的最后活跃时间
     */
    @Update("UPDATE wvp_analysis_task SET last_active_time = NOW(), updated_at = NOW() WHERE id = #{taskId}")
    int updateLastActiveTime(@Param("taskId") String taskId);
}