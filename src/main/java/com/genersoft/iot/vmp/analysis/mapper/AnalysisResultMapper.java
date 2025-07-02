package com.genersoft.iot.vmp.analysis.mapper;

import com.genersoft.iot.vmp.analysis.entity.AnalysisResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


import java.util.List;

/**
 * 分析结果 Mapper 接口
 *
 * @author AI Assistant
 * @since 2024-01-20
 */
@Mapper
public interface AnalysisResultMapper {

    /**
     * 新增分析结果
     *
     * @param result 分析结果信息
     * @return 影响行数
     */
    int insert(AnalysisResult result);

    /**
     * 根据结果ID删除结果
     *
     * @param resultId 结果ID
     * @return 影响行数
     */
    int deleteByResultId(@Param("resultId") Long resultId);

    /**
     * 根据任务ID删除所有相关结果
     *
     * @param taskId 任务ID
     * @return 影响行数
     */
    int deleteByTaskId(@Param("taskId") String taskId);

    /**
     * 根据结果ID查询结果详情
     *
     * @param resultId 结果ID
     * @return 分析结果
     */
    AnalysisResult selectByResultId(@Param("resultId") Long resultId);

    /**
     * 根据任务ID查询结果列表
     *
     * @param taskId 任务ID
     * @return 结果列表
     */
    List<AnalysisResult> selectByTaskId(@Param("taskId") String taskId);

        /**
     * 多条件分页查询分析结果
     *
     * @param offset 偏移量
     * @param limit 限制数量
     * @param startDate 开始时间（可选）
     * @param endDate 结束时间（可选）
     * @param taskId 任务ID（可选）
     * @param channelId 通道ID（可选）
     * @param keyword 关键词搜索（可选，搜索问题和答案）
     * @param onlyAlarm 仅显示报警（可选）
     * @return 结果列表
     */
    List<AnalysisResult> selectByPage(@Param("offset") int offset,
                                     @Param("limit") int limit,
                                     @Param("startDate") String startDate,
                                     @Param("endDate") String endDate,
                                     @Param("taskId") String taskId,
                                     @Param("channelId") String channelId,
                                     @Param("keyword") String keyword,
                                     @Param("onlyAlarm") Boolean onlyAlarm);

    /**
     * 统计符合条件的结果总数
     *
     * @param startDate 开始时间（可选）
     * @param endDate 结束时间（可选）
     * @param taskId 任务ID（可选）
     * @param channelId 通道ID（可选）
     * @param keyword 关键词搜索（可选）
     * @param onlyAlarm 仅显示报警（可选）
     * @return 总数
     */
    int countByCondition(@Param("startDate") String startDate,
                        @Param("endDate") String endDate,
                        @Param("taskId") String taskId,
                        @Param("channelId") String channelId,
                        @Param("keyword") String keyword,
                        @Param("onlyAlarm") Boolean onlyAlarm);

    /**
     * 统计今日分析总数
     *
     * @param today 今日开始时间
     * @return 今日分析总数
     */
    int countTodayTotal(@Param("today") String today);

    /**
     * 统计今日报警数量
     *
     * @param today 今日开始时间
     * @return 今日报警数量
     */
    int countTodayAlarms(@Param("today") String today);

    /**
     * 统计指定时间范围内的分析结果
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 统计结果
     */
    AnalysisStatistics countByDateRange(@Param("startDate") String startDate,
                                       @Param("endDate") String endDate);

    /**
     * 查询最近的分析结果
     *
     * @param limit 限制数量
     * @return 最近结果列表
     */
    List<AnalysisResult> selectRecent(@Param("limit") int limit);

    /**
     * 查询指定任务的最新结果
     *
     * @param taskId 任务ID
     * @return 最新结果
     */
    AnalysisResult selectLatestByTaskId(@Param("taskId") String taskId);

        /**
     * 按小时统计分析数量（用于趋势分析）
     *
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 小时统计列表
     */
    List<HourlyCount> countByHour(@Param("startDate") String startDate,
                                 @Param("endDate") String endDate);

    /**
     * 按通道统计分析数量
     *
     * @param startDate 开始时间（可选）
     * @param endDate 结束时间（可选）
     * @return 通道统计列表
     */
    List<ChannelCount> countByChannel(@Param("startDate") String startDate,
                                     @Param("endDate") String endDate);

    /**
     * 导出数据查询（不分页）
     *
     * @param startDate 开始时间（可选）
     * @param endDate 结束时间（可选）
     * @param taskId 任务ID（可选）
     * @param channelId 通道ID（可选）
     * @param keyword 关键词搜索（可选）
     * @param onlyAlarm 仅显示报警（可选）
     * @return 导出数据列表
     */
    List<AnalysisResult> selectForExport(@Param("startDate") String startDate,
                                        @Param("endDate") String endDate,
                                        @Param("taskId") String taskId,
                                        @Param("channelId") String channelId,
                                        @Param("keyword") String keyword,
                                        @Param("onlyAlarm") Boolean onlyAlarm);

    /**
     * 清理过期数据
     *
     * @param beforeDate 删除此时间之前的数据
     * @return 删除行数
     */
    int deleteExpiredData(@Param("beforeDate") String beforeDate);

    // 别名方法，为了与Service实现兼容
    default int insertResult(AnalysisResult result) {
        return insert(result);
    }

    default AnalysisResult selectResultById(Integer id) {
        return selectByResultId(id.longValue());
    }

    default List<AnalysisResult> selectResultsByConditions(java.util.Map<String, Object> conditions) {
        String startDate = (String) conditions.get("startDate");
        String endDate = (String) conditions.get("endDate");
        String taskId = (String) conditions.get("taskId");
        String channelId = (String) conditions.get("channelId");
        String keyword = (String) conditions.get("keyword");
        Boolean onlyAlarm = (Boolean) conditions.get("onlyAlarm");
        Integer offset = (Integer) conditions.get("offset");
        Integer limit = (Integer) conditions.get("limit");

        if (offset == null) offset = 0;
        if (limit == null) limit = 20;

        return selectByPage(offset, limit, startDate, endDate, taskId, channelId, keyword, onlyAlarm);
    }

    default int countResultsByConditions(java.util.Map<String, Object> conditions) {
        String startDate = (String) conditions.get("startDate");
        String endDate = (String) conditions.get("endDate");
        String taskId = (String) conditions.get("taskId");
        String channelId = (String) conditions.get("channelId");
        String keyword = (String) conditions.get("keyword");
        Boolean onlyAlarm = (Boolean) conditions.get("onlyAlarm");

        return countByCondition(startDate, endDate, taskId, channelId, keyword, onlyAlarm);
    }

    default int deleteResult(Integer id) {
        return deleteByResultId(id.longValue());
    }

    /**
     * 分析统计信息类
     */
    class AnalysisStatistics {
        private Integer totalCount;
        private Integer alarmCount;
        private Integer normalCount;

        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }

        public Integer getAlarmCount() {
            return alarmCount;
        }

        public void setAlarmCount(Integer alarmCount) {
            this.alarmCount = alarmCount;
        }

        public Integer getNormalCount() {
            return normalCount;
        }

        public void setNormalCount(Integer normalCount) {
            this.normalCount = normalCount;
        }
    }

    /**
     * 小时统计信息类
     */
    class HourlyCount {
        private String hour; // 格式: yyyy-MM-dd HH
        private Integer totalCount;
        private Integer alarmCount;

        public String getHour() {
            return hour;
        }

        public void setHour(String hour) {
            this.hour = hour;
        }

        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }

        public Integer getAlarmCount() {
            return alarmCount;
        }

        public void setAlarmCount(Integer alarmCount) {
            this.alarmCount = alarmCount;
        }
    }

    /**
     * 通道统计信息类
     */
    class ChannelCount {
        private String channelId;
        private String channelName;
        private Integer totalCount;
        private Integer alarmCount;

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

        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }

        public Integer getAlarmCount() {
            return alarmCount;
        }

        public void setAlarmCount(Integer alarmCount) {
            this.alarmCount = alarmCount;
        }
    }
}