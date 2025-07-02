package com.genersoft.iot.vmp.analysis.service;

import com.genersoft.iot.vmp.analysis.entity.AnalysisResult;
import com.genersoft.iot.vmp.analysis.dto.ResultQueryRequest;
import com.genersoft.iot.vmp.analysis.dto.CallbackRequest;
import com.github.pagehelper.PageInfo;

import java.util.List;
import java.util.Map;

/**
 * 分析结果服务接口
 */
public interface IAnalysisResultService {

    /**
     * 保存分析结果（通过回调）
     * @param request 回调请求
     * @return 结果ID
     */
    Integer saveAnalysisResult(CallbackRequest request);

    /**
     * 查询分析结果（分页）
     * @param request 查询请求
     * @return 分页结果
     */
    PageInfo<AnalysisResult> queryResults(ResultQueryRequest request);

    /**
     * 获取结果详情
     * @param resultId 结果ID
     * @return 结果详情
     */
    AnalysisResult getResult(Integer resultId);

    /**
     * 删除分析结果
     * @param resultId 结果ID
     * @return 是否成功
     */
    boolean deleteResult(Integer resultId);

    /**
     * 获取实时统计信息
     * @return 统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 获取今日统计
     * @return 今日统计信息
     */
    Map<String, Object> getTodayStatistics();

    /**
     * 获取告警统计（按时间区间）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 告警统计
     */
    List<Map<String, Object>> getAlarmStatistics(String startDate, String endDate);

    /**
     * 获取设备分析统计
     * @param deviceId 设备ID（可选）
     * @return 设备统计信息
     */
    List<Map<String, Object>> getDeviceStatistics(String deviceId);

    /**
     * 导出分析报告数据
     * @param request 查询条件
     * @return 导出数据
     */
    List<AnalysisResult> exportAnalysisReport(ResultQueryRequest request);

    /**
     * 获取最新的分析结果
     * @param limit 数量限制
     * @return 最新结果列表
     */
    List<AnalysisResult> getLatestResults(int limit);

    /**
     * 根据任务ID获取分析结果
     * @param taskId 任务ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageInfo<AnalysisResult> getResultsByTaskId(Integer taskId, int page, int pageSize);

    /**
     * 清理过期数据
     * @param beforeDate 清理此日期之前的数据
     * @return 清理的记录数
     */
    int cleanExpiredData(String beforeDate);

    /**
     * 处理Python微服务回调
     * @param request 回调请求
     * @return 处理结果
     */
    boolean handleCallback(CallbackRequest request);

    /**
     * 批量删除分析结果
     * @param resultIds 结果ID数组
     * @return 删除的记录数
     */
    boolean batchDeleteResults(Integer[] resultIds);

    /**
     * 获取分析趋势数据
     * @param days 天数
     * @return 趋势数据
     */
    Map<String, Object> getAnalysisTrends(int days);
}