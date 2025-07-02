package com.genersoft.iot.vmp.analysis.controller;

import com.genersoft.iot.vmp.analysis.entity.AnalysisResult;
import com.genersoft.iot.vmp.analysis.dto.ResultQueryRequest;
import com.genersoft.iot.vmp.analysis.service.IAnalysisResultService;
import com.genersoft.iot.vmp.vmanager.bean.WVPResult;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 智能分析结果查询控制器
 */
@RestController
@RequestMapping("/api/analysis/results")
public class AnalysisResultController {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisResultController.class);

    @Autowired
    private IAnalysisResultService analysisResultService;

    /**
     * 查询分析结果（支持分页和多条件筛选）
     */
    @GetMapping
    public WVPResult<PageInfo<AnalysisResult>> queryResults(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer taskId,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String channelId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAlarm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            logger.info("查询分析结果: startDate={}, endDate={}, taskId={}, deviceId={}, channelId={}, keyword={}, onlyAlarm={}, page={}, pageSize={}",
                    startDate, endDate, taskId, deviceId, channelId, keyword, onlyAlarm, page, pageSize);

            // 构建查询请求
            ResultQueryRequest request = new ResultQueryRequest();
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setTaskId(taskId);
            request.setDeviceId(deviceId);
            request.setChannelId(channelId);
            request.setKeyword(keyword);
            request.setOnlyAlarm(onlyAlarm);
            request.setPage(page);
            request.setPageSize(pageSize);

            PageInfo<AnalysisResult> pageInfo = analysisResultService.queryResults(request);
            return WVPResult.success(pageInfo, "查询成功");
        } catch (Exception e) {
            logger.error("查询分析结果失败", e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 根据ID获取分析结果详情
     */
    @GetMapping("/{resultId}")
    public WVPResult<AnalysisResult> getResult(@PathVariable Integer resultId) {
        try {
            logger.info("获取分析结果详情: {}", resultId);
            AnalysisResult result = analysisResultService.getResult(resultId);
            if (result == null) {
                return WVPResult.fail(-1, "分析结果不存在");
            }
            return WVPResult.success(result, "查询成功");
        } catch (Exception e) {
            logger.error("获取分析结果详情失败: {}", resultId, e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 获取实时统计信息
     */
    @GetMapping("/statistics")
    public WVPResult<Map<String, Object>> getStatistics() {
        try {
            logger.info("获取实时统计信息");
            Map<String, Object> statistics = analysisResultService.getStatistics();
            return WVPResult.success(statistics, "查询成功");
        } catch (Exception e) {
            logger.error("获取实时统计信息失败", e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 获取今日统计信息
     */
    @GetMapping("/statistics/today")
    public WVPResult<Map<String, Object>> getTodayStatistics() {
        try {
            logger.info("获取今日统计信息");
            Map<String, Object> todayStats = analysisResultService.getTodayStatistics();
            return WVPResult.success(todayStats, "查询成功");
        } catch (Exception e) {
            logger.error("获取今日统计信息失败", e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 获取告警结果列表
     */
    @GetMapping("/alarms")
    public WVPResult<PageInfo<AnalysisResult>> getAlarmResults(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String channelId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            logger.info("查询告警结果: startDate={}, endDate={}, deviceId={}, channelId={}, page={}, pageSize={}",
                    startDate, endDate, deviceId, channelId, page, pageSize);

            // 构建查询请求（只查询告警）
            ResultQueryRequest request = new ResultQueryRequest();
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setDeviceId(deviceId);
            request.setChannelId(channelId);
            request.setOnlyAlarm(true); // 只查询告警结果
            request.setPage(page);
            request.setPageSize(pageSize);

            PageInfo<AnalysisResult> pageInfo = analysisResultService.queryResults(request);
            return WVPResult.success(pageInfo, "查询成功");
        } catch (Exception e) {
            logger.error("查询告警结果失败", e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 根据任务ID查询结果
     */
    @GetMapping("/task/{taskId}")
    public WVPResult<PageInfo<AnalysisResult>> getResultsByTask(
            @PathVariable Integer taskId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            logger.info("根据任务ID查询结果: taskId={}, page={}, pageSize={}", taskId, page, pageSize);

            ResultQueryRequest request = new ResultQueryRequest();
            request.setTaskId(taskId);
            request.setPage(page);
            request.setPageSize(pageSize);

            PageInfo<AnalysisResult> pageInfo = analysisResultService.queryResults(request);
            return WVPResult.success(pageInfo, "查询成功");
        } catch (Exception e) {
            logger.error("根据任务ID查询结果失败: {}", taskId, e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 删除分析结果
     */
    @DeleteMapping("/{resultId}")
    public WVPResult<Boolean> deleteResult(@PathVariable Integer resultId) {
        try {
            logger.info("删除分析结果: {}", resultId);
            boolean result = analysisResultService.deleteResult(resultId);
            return WVPResult.success(result, "删除成功");
        } catch (Exception e) {
            logger.error("删除分析结果失败: {}", resultId, e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 批量删除分析结果
     */
    @DeleteMapping("/batch")
    public WVPResult<Boolean> batchDeleteResults(@RequestBody Integer[] resultIds) {
        try {
            logger.info("批量删除分析结果: {}", (Object) resultIds);
            boolean result = analysisResultService.batchDeleteResults(resultIds);
            return WVPResult.success(result, "批量删除成功");
        } catch (Exception e) {
            logger.error("批量删除分析结果失败", e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 获取分析趋势数据
     */
    @GetMapping("/trends")
    public WVPResult<Map<String, Object>> getAnalysisTrends(
            @RequestParam(required = false, defaultValue = "7") int days) {
        try {
            logger.info("获取分析趋势数据: days={}", days);
            Map<String, Object> trends = analysisResultService.getAnalysisTrends(days);
            return WVPResult.success(trends, "查询成功");
        } catch (Exception e) {
            logger.error("获取分析趋势数据失败", e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }

    /**
     * 根据设备通道查询最新结果
     */
    @GetMapping("/latest")
    public WVPResult<PageInfo<AnalysisResult>> getLatestResults(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String channelId,
            @RequestParam(defaultValue = "10") int count) {
        try {
            logger.info("查询最新分析结果: deviceId={}, channelId={}, count={}", deviceId, channelId, count);

            ResultQueryRequest request = new ResultQueryRequest();
            request.setDeviceId(deviceId);
            request.setChannelId(channelId);
            request.setPage(1);
            request.setPageSize(count);

            PageInfo<AnalysisResult> pageInfo = analysisResultService.queryResults(request);
            return WVPResult.success(pageInfo, "查询成功");
        } catch (Exception e) {
            logger.error("查询最新分析结果失败", e);
            return WVPResult.fail(-1, e.getMessage());
        }
    }
}