package com.genersoft.iot.vmp.analysis.service.impl;

import com.genersoft.iot.vmp.analysis.entity.AnalysisResult;
import com.genersoft.iot.vmp.analysis.mapper.AnalysisResultMapper;
import com.genersoft.iot.vmp.analysis.dto.ResultQueryRequest;
import com.genersoft.iot.vmp.analysis.dto.CallbackRequest;
import com.genersoft.iot.vmp.analysis.service.IAnalysisResultService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 分析结果服务实现类
 */
@Service
public class AnalysisResultServiceImpl implements IAnalysisResultService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisResultServiceImpl.class);

    @Autowired
    private AnalysisResultMapper resultMapper;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public PageInfo<AnalysisResult> queryResults(ResultQueryRequest request) {
        logger.info("查询分析结果: {}", request);

        // 设置分页参数
        PageHelper.startPage(request.getPage(), request.getPageSize());

        // 构建查询条件
        Map<String, Object> params = buildQueryParams(request);

        // 执行查询
        List<AnalysisResult> results = resultMapper.selectResultsByConditions(params);

        // 返回分页结果
        PageInfo<AnalysisResult> pageInfo = new PageInfo<>(results);
        logger.info("查询分析结果完成: 总数={}, 当前页数据量={}", pageInfo.getTotal(), results.size());

        return pageInfo;
    }

    @Override
    public AnalysisResult getResult(Integer resultId) {
        logger.info("获取分析结果详情: {}", resultId);
        return resultMapper.selectResultById(resultId);
    }

    @Override
    @Transactional
    public boolean handleCallback(CallbackRequest callbackRequest) {
        logger.info("处理VLM回调: taskId={}, timestamp={}",
                callbackRequest.getTaskId(), callbackRequest.getResultTimestamp());

        try {
            // 构建分析结果实体
            AnalysisResult analysisResult = new AnalysisResult();
            analysisResult.setTaskId(callbackRequest.getTaskId()); // 直接使用String类型的taskId
            analysisResult.setResultTimestamp(callbackRequest.getResultTimestamp());
            analysisResult.setAnalysisAnswer(callbackRequest.getAnalysisAnswer());
            analysisResult.setConfidenceScore(callbackRequest.getConfidenceScore());
            analysisResult.setIsAlarm(callbackRequest.getIsAlarm());
            analysisResult.setFrameImagePath(callbackRequest.getFrameImagePath());
            analysisResult.setVideoSegmentPath(callbackRequest.getVideoSegmentPath());

            // 处理Base64图片数据
            if (StringUtils.hasText(callbackRequest.getFrameData())) {
                // TODO: 保存Base64图片到文件系统或对象存储
                // String imagePath = saveBase64Image(callbackRequest.getFrameData());
                // analysisResult.setFrameImagePath(imagePath);
                logger.debug("接收到关键帧数据，长度: {}", callbackRequest.getFrameData().length());
            }

            // 自动告警判断
            if (callbackRequest.getIsAlarm() == null) {
                boolean isAlarm = analyzeAlarmStatus(callbackRequest.getAnalysisAnswer());
                analysisResult.setIsAlarm(isAlarm);
                logger.info("自动告警判断结果: taskId={}, isAlarm={}", callbackRequest.getTaskId(), isAlarm);
            }

            // 设置创建时间
            analysisResult.setCreateTime(dateFormat.format(new Date()));

            // 保存到数据库
            int saveResult = resultMapper.insertResult(analysisResult);

            if (saveResult > 0) {
                logger.info("成功保存分析结果: taskId={}, resultId={}",
                        callbackRequest.getTaskId(), analysisResult.getId());
                return true;
            } else {
                logger.error("保存分析结果失败: taskId={}", callbackRequest.getTaskId());
                return false;
            }

        } catch (Exception e) {
            logger.error("处理VLM回调异常: taskId={}", callbackRequest.getTaskId(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getStatistics() {
        logger.info("获取实时统计信息");

        Map<String, Object> statistics = new HashMap<>();

        try {
            // 今日统计
            String today = dateFormat.format(new Date()).substring(0, 10) + " 00:00:00";
            String tomorrow = dateFormat.format(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)).substring(0, 10) + " 00:00:00";

            Map<String, Object> todayParams = new HashMap<>();
            todayParams.put("startDate", today);
            todayParams.put("endDate", tomorrow);

            int todayAnalysisCount = resultMapper.countResultsByConditions(todayParams);

            todayParams.put("onlyAlarm", true);
            int todayAlarmCount = resultMapper.countResultsByConditions(todayParams);

            statistics.put("todayAnalysisCount", todayAnalysisCount);
            statistics.put("todayAlarmCount", todayAlarmCount);

            // 总统计
            Map<String, Object> totalParams = new HashMap<>();
            int totalAnalysisCount = resultMapper.countResultsByConditions(totalParams);

            totalParams.put("onlyAlarm", true);
            int totalAlarmCount = resultMapper.countResultsByConditions(totalParams);

            statistics.put("totalAnalysisCount", totalAnalysisCount);
            statistics.put("totalAlarmCount", totalAlarmCount);

            // 计算告警率
            double alarmRate = totalAnalysisCount > 0 ?
                    (double) totalAlarmCount / totalAnalysisCount * 100 : 0;
            statistics.put("alarmRate", Math.round(alarmRate * 100.0) / 100.0);

            logger.info("统计信息: {}", statistics);

        } catch (Exception e) {
            logger.error("获取统计信息失败", e);
            // 返回默认值
            statistics.put("todayAnalysisCount", 0);
            statistics.put("todayAlarmCount", 0);
            statistics.put("totalAnalysisCount", 0);
            statistics.put("totalAlarmCount", 0);
            statistics.put("alarmRate", 0.0);
        }

        return statistics;
    }

    @Override
    public Map<String, Object> getTodayStatistics() {
        logger.info("获取今日统计信息");

        Map<String, Object> todayStats = new HashMap<>();

        try {
            String today = dateFormat.format(new Date()).substring(0, 10) + " 00:00:00";
            String tomorrow = dateFormat.format(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)).substring(0, 10) + " 00:00:00";

            Map<String, Object> params = new HashMap<>();
            params.put("startDate", today);
            params.put("endDate", tomorrow);

            // 今日分析总数
            int analysisCount = resultMapper.countResultsByConditions(params);
            todayStats.put("analysisCount", analysisCount);

            // 今日告警数
            params.put("onlyAlarm", true);
            int alarmCount = resultMapper.countResultsByConditions(params);
            todayStats.put("alarmCount", alarmCount);

            // 今日正常数
            todayStats.put("normalCount", analysisCount - alarmCount);

            // 今日告警率
            double alarmRate = analysisCount > 0 ?
                    (double) alarmCount / analysisCount * 100 : 0;
            todayStats.put("alarmRate", Math.round(alarmRate * 100.0) / 100.0);

        } catch (Exception e) {
            logger.error("获取今日统计信息失败", e);
            todayStats.put("analysisCount", 0);
            todayStats.put("alarmCount", 0);
            todayStats.put("normalCount", 0);
            todayStats.put("alarmRate", 0.0);
        }

        return todayStats;
    }

    @Override
    @Transactional
    public boolean deleteResult(Integer resultId) {
        logger.info("删除分析结果: {}", resultId);

        try {
            int result = resultMapper.deleteResult(resultId);
            if (result > 0) {
                logger.info("成功删除分析结果: {}", resultId);
                return true;
            } else {
                logger.warn("删除分析结果失败，可能不存在: {}", resultId);
                return false;
            }
        } catch (Exception e) {
            logger.error("删除分析结果异常: {}", resultId, e);
            throw new RuntimeException("删除分析结果失败", e);
        }
    }

    @Override
    @Transactional
    public boolean batchDeleteResults(Integer[] resultIds) {
        logger.info("批量删除分析结果: count={}", resultIds.length);

        try {
            int deletedCount = 0;
            for (Integer resultId : resultIds) {
                try {
                    int result = resultMapper.deleteResult(resultId);
                    if (result > 0) {
                        deletedCount++;
                    }
                } catch (Exception e) {
                    logger.error("删除单个结果失败: {}", resultId, e);
                }
            }

            logger.info("批量删除完成: 总数={}, 成功={}", resultIds.length, deletedCount);
            return deletedCount > 0;

        } catch (Exception e) {
            logger.error("批量删除分析结果异常", e);
            throw new RuntimeException("批量删除失败", e);
        }
    }

    @Override
    public Map<String, Object> getAnalysisTrends(int days) {
        logger.info("获取分析趋势数据: days={}", days);

        // 创建trends容器
        Map<String, Object> trends = new HashMap<>();

        try {
            // 计算开始日期
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -days);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String startDate = sdf.format(calendar.getTime());
            String endDate = sdf.format(new Date());

            // 获取趋势数据 - 使用已有方法构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("startDate", startDate);
            params.put("endDate", endDate);

            // 获取总数和告警数
            int totalCount = resultMapper.countResultsByConditions(params);

            params.put("onlyAlarm", true);
            int alarmCount = resultMapper.countResultsByConditions(params);

            // 构建趋势数据
            List<Map<String, Object>> trendData = new ArrayList<>();
            Map<String, Object> trendItem = new HashMap<>();
            trendItem.put("date", startDate);
            trendItem.put("count", totalCount);
            trendItem.put("alarmCount", alarmCount);
            trendData.add(trendItem);

            trends.put("trendData", trendData);

            // 计算汇总统计
            trends.put("totalCount", totalCount);
            trends.put("alarmCount", alarmCount);
            trends.put("normalCount", totalCount - alarmCount);
            trends.put("alarmRate", totalCount > 0 ? (double) alarmCount / totalCount * 100 : 0.0);
            trends.put("days", days);

            return trends;
        } catch (Exception e) {
            logger.error("获取分析趋势数据失败", e);
            throw new RuntimeException("获取趋势数据失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public int cleanExpiredData(String beforeDate) {
        logger.info("清理过期数据: beforeDate={}", beforeDate);

        try {
            int deletedCount = resultMapper.deleteExpiredData(beforeDate);
            logger.info("成功清理过期数据: 删除{}条记录", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            logger.error("清理过期数据失败: beforeDate={}", beforeDate, e);
            throw new RuntimeException("清理过期数据失败", e);
        }
    }

    @Override
    public PageInfo<AnalysisResult> getResultsByTaskId(Integer taskId, int page, int pageSize) {
        logger.info("根据任务ID获取分析结果: taskId={}, page={}, pageSize={}", taskId, page, pageSize);

        try {
            // 构建查询条件
            ResultQueryRequest request = new ResultQueryRequest();
            request.setTaskId(taskId);
            request.setPage(page);
            request.setPageSize(pageSize);

            return queryResults(request);
        } catch (Exception e) {
            logger.error("根据任务ID获取分析结果失败: taskId={}", taskId, e);
            throw new RuntimeException("获取结果失败: " + e.getMessage());
        }
    }

    /**
     * 构建查询参数
     */
    private Map<String, Object> buildQueryParams(ResultQueryRequest request) {
        Map<String, Object> params = new HashMap<>();

        if (StringUtils.hasText(request.getStartDate())) {
            params.put("startDate", request.getStartDate());
        }
        if (StringUtils.hasText(request.getEndDate())) {
            params.put("endDate", request.getEndDate());
        }
        if (request.getTaskId() != null) {
            params.put("taskId", request.getTaskId());
        }
        if (StringUtils.hasText(request.getDeviceId())) {
            params.put("deviceId", request.getDeviceId());
        }
        if (StringUtils.hasText(request.getChannelId())) {
            params.put("channelId", request.getChannelId());
        }
        if (StringUtils.hasText(request.getKeyword())) {
            params.put("keyword", "%" + request.getKeyword() + "%");
        }
        if (request.getOnlyAlarm() != null && request.getOnlyAlarm()) {
            params.put("onlyAlarm", true);
        }

        return params;
    }

    /**
     * 分析告警状态
     * 基于VLM回答内容判断是否为告警
     */
    private boolean analyzeAlarmStatus(String analysisAnswer) {
        if (!StringUtils.hasText(analysisAnswer)) {
            return false;
        }

        String answer = analysisAnswer.toLowerCase();

        // 告警关键词列表
        String[] alarmKeywords = {
            "异常", "告警", "危险", "错误", "故障", "问题", "警告",
            "火灾", "烟雾", "入侵", "跌倒", "打架", "暴力",
            "abnormal", "alarm", "danger", "error", "fault", "problem", "warning",
            "fire", "smoke", "intrusion", "fall", "fight", "violence"
        };

        for (String keyword : alarmKeywords) {
            if (answer.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<AnalysisResult> getLatestResults(int limit) {
        logger.info("获取最新分析结果: limit={}", limit);

        try {
            // 构建查询条件：按时间倒序查询
            Map<String, Object> params = new HashMap<>();
            params.put("orderBy", "result_timestamp desc");
            params.put("limit", limit);

            List<AnalysisResult> results = resultMapper.selectResultsByConditions(params);
            logger.info("获取最新分析结果完成: 实际返回{}条", results.size());

            return results;
        } catch (Exception e) {
            logger.error("获取最新分析结果失败: limit={}", limit, e);
            throw new RuntimeException("获取最新结果失败: " + e.getMessage());
        }
    }

    @Override
    public Integer saveAnalysisResult(CallbackRequest request) {
        logger.info("保存分析结果: taskId={}", request.getTaskId());

        try {
            // 构建分析结果实体
            AnalysisResult analysisResult = new AnalysisResult();
            analysisResult.setTaskId(String.valueOf(request.getTaskId()));
            analysisResult.setResultTimestamp(request.getResultTimestamp());
            analysisResult.setAnalysisAnswer(request.getAnalysisAnswer());
            analysisResult.setConfidenceScore(request.getConfidenceScore());
            analysisResult.setIsAlarm(request.getIsAlarm());
            analysisResult.setFrameImagePath(request.getFrameImagePath());
            analysisResult.setVideoSegmentPath(request.getVideoSegmentPath());
            analysisResult.setCreateTime(dateFormat.format(new Date()));

            // 保存到数据库
            int result = resultMapper.insertResult(analysisResult);

            if (result > 0) {
                logger.info("成功保存分析结果: resultId={}", analysisResult.getId());
                return analysisResult.getId();
            } else {
                logger.error("保存分析结果失败");
                throw new RuntimeException("保存分析结果失败");
            }
        } catch (Exception e) {
            logger.error("保存分析结果异常: taskId={}", request.getTaskId(), e);
            throw new RuntimeException("保存分析结果失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getAlarmStatistics(String startDate, String endDate) {
        logger.info("获取告警统计: startDate={}, endDate={}", startDate, endDate);

        try {
            Map<String, Object> params = new HashMap<>();
            if (StringUtils.hasText(startDate)) {
                params.put("startDate", startDate);
            }
            if (StringUtils.hasText(endDate)) {
                params.put("endDate", endDate);
            }
            params.put("onlyAlarm", true);

            // 这里可以调用Mapper的统计方法，暂时返回简单统计
            int alarmCount = resultMapper.countResultsByConditions(params);

            List<Map<String, Object>> statistics = new ArrayList<>();
            Map<String, Object> stat = new HashMap<>();
            stat.put("alarmCount", alarmCount);
            stat.put("startDate", startDate);
            stat.put("endDate", endDate);
            statistics.add(stat);

            return statistics;
        } catch (Exception e) {
            logger.error("获取告警统计失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getDeviceStatistics(String deviceId) {
        logger.info("获取设备分析统计: deviceId={}", deviceId);

        try {
            Map<String, Object> params = new HashMap<>();
            if (StringUtils.hasText(deviceId)) {
                params.put("deviceId", deviceId);
            }

            int totalCount = resultMapper.countResultsByConditions(params);

            params.put("onlyAlarm", true);
            int alarmCount = resultMapper.countResultsByConditions(params);

            List<Map<String, Object>> statistics = new ArrayList<>();
            Map<String, Object> stat = new HashMap<>();
            stat.put("deviceId", deviceId);
            stat.put("totalCount", totalCount);
            stat.put("alarmCount", alarmCount);
            stat.put("normalCount", totalCount - alarmCount);
            statistics.add(stat);

            return statistics;
        } catch (Exception e) {
            logger.error("获取设备分析统计失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<AnalysisResult> exportAnalysisReport(ResultQueryRequest request) {
        logger.info("导出分析报告数据: {}", request);

        try {
            // 构建查询条件，不使用分页
            Map<String, Object> params = buildQueryParams(request);

            // 获取所有匹配的结果
            List<AnalysisResult> results = resultMapper.selectResultsByConditions(params);
            logger.info("导出分析报告完成: 总数={}", results.size());

            return results;
        } catch (Exception e) {
            logger.error("导出分析报告失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }
}