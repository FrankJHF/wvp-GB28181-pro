package com.genersoft.iot.vmp.analysis.mapper;

import com.genersoft.iot.vmp.analysis.entity.AnalysisTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;

/**
 * 分析任务 Mapper 接口
 *
 * @author AI Assistant
 * @since 2024-01-20
 */
@Mapper
public interface AnalysisTaskMapper {

    /**
     * 新增分析任务
     *
     * @param task 任务信息
     * @return 影响行数
     */
    int insert(AnalysisTask task);

    /**
     * 根据任务ID删除任务
     *
     * @param taskId 任务ID
     * @return 影响行数
     */
    int deleteByTaskId(@Param("taskId") String taskId);

    /**
     * 根据主键ID删除任务
     *
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Integer id);

    /**
     * 更新任务信息
     *
     * @param task 任务信息
     * @return 影响行数
     */
    int update(AnalysisTask task);

        /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 新状态
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int updateStatus(@Param("taskId") String taskId,
                    @Param("status") String status,
                    @Param("updateTime") String updateTime);

    /**
     * 根据任务ID查询任务
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    AnalysisTask selectByTaskId(@Param("taskId") String taskId);

    /**
     * 根据主键ID查询任务
     *
     * @param id 主键ID
     * @return 任务信息
     */
    AnalysisTask selectById(@Param("id") Integer id);

    /**
     * 查询所有任务列表
     *
     * @return 任务列表
     */
    List<AnalysisTask> selectAll();

        /**
     * 根据状态查询任务列表
     *
     * @param status 任务状态
     * @return 任务列表
     */
    List<AnalysisTask> selectByStatus(@Param("status") String status);

    /**
     * 根据设备ID和通道ID查询任务列表
     *
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @return 任务列表
     */
    List<AnalysisTask> selectByDeviceAndChannel(@Param("deviceId") String deviceId,
                                               @Param("channelId") String channelId);

    /**
     * 分页查询任务列表
     *
     * @param offset 偏移量
     * @param limit 限制数量
     * @param status 状态筛选（可选）
     * @param keyword 关键词搜索（可选，搜索任务名称和通道名称）
     * @return 任务列表
     */
    List<AnalysisTask> selectByPage(@Param("offset") int offset,
                                   @Param("limit") int limit,
                                   @Param("status") String status,
                                   @Param("keyword") String keyword);

    /**
     * 统计任务总数
     *
     * @param status 状态筛选（可选）
     * @param keyword 关键词搜索（可选）
     * @return 总数
     */
    int countByCondition(@Param("status") String status,
                        @Param("keyword") String keyword);

    /**
     * 统计各状态的任务数量
     *
     * @return 状态统计列表，包含status和count字段
     */
    List<StatusCount> countByStatus();

    /**
     * 检查任务名称是否已存在
     *
     * @param taskName 任务名称
     * @param excludeTaskId 排除的任务ID（用于编辑时检查）
     * @return 存在的任务数量
     */
    int countByTaskName(@Param("taskName") String taskName,
                       @Param("excludeTaskId") String excludeTaskId);

    /**
     * 批量更新任务状态
     *
     * @param taskIds 任务ID列表
     * @param status 新状态
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int batchUpdateStatus(@Param("taskIds") List<String> taskIds,
                         @Param("status") String status,
                         @Param("updateTime") String updateTime);

    // 别名方法，为了与Service实现兼容
    default int insertTask(AnalysisTask task) {
        return insert(task);
    }

    default AnalysisTask selectTaskById(Integer id) {
        return selectById(id);
    }

    default AnalysisTask selectTaskById(String taskId) {
        return selectByTaskId(taskId);
    }

    default int updateTask(AnalysisTask task) {
        return update(task);
    }

    default int deleteTask(Integer id) {
        return deleteById(id);
    }

    default int deleteTask(String taskId) {
        return deleteByTaskId(taskId);
    }

    default List<AnalysisTask> selectAllTasks() {
        return selectAll();
    }

    default AnalysisTask selectTaskByDeviceChannel(String deviceId, String channelId) {
        List<AnalysisTask> tasks = selectByDeviceAndChannel(deviceId, channelId);
        return tasks.isEmpty() ? null : tasks.get(0);
    }

    default List<AnalysisTask> selectTasksByStatus(String status) {
        return selectByStatus(status);
    }

    /**
     * 查询指定设备通道上正在运行的任务
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @return 正在运行的任务，如果没有则返回null
     */
    default AnalysisTask selectRunningTaskByChannel(String deviceId, String channelId) {
        List<AnalysisTask> tasks = selectByDeviceAndChannel(deviceId, channelId);
        return tasks.stream()
                .filter(task -> "RUNNING".equals(task.getTaskStatus()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 状态统计内部类
     */
    class StatusCount {
        private String status;
        private Integer count;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }
}