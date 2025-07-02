import request from '@/utils/request'

// ========== 分析任务管理API (8个接口) ==========

/**
 * 创建分析任务
 * @param {Object} data 任务创建参数
 */
export function createAnalysisTask(data) {
  return request({
    url: '/api/analysis/tasks',
    method: 'post',
    data
  })
}

/**
 * 查询任务列表（分页）
 * @param {Object} params 查询参数
 */
export function getAnalysisTasks(params) {
  return request({
    url: '/api/analysis/tasks',
    method: 'get',
    params
  })
}

/**
 * 获取任务详情
 * @param {number} taskId 任务ID
 */
export function getAnalysisTaskDetail(taskId) {
  return request({
    url: `/api/analysis/tasks/${taskId}`,
    method: 'get'
  })
}

/**
 * 更新任务配置
 * @param {number} taskId 任务ID
 * @param {Object} data 更新参数
 */
export function updateAnalysisTask(taskId, data) {
  return request({
    url: `/api/analysis/tasks/${taskId}`,
    method: 'put',
    data
  })
}

/**
 * 启动分析任务
 * @param {number} taskId 任务ID
 */
export function startAnalysisTask(taskId) {
  return request({
    url: `/api/analysis/tasks/${taskId}/start`,
    method: 'post'
  })
}

/**
 * 停止分析任务
 * @param {number} taskId 任务ID
 */
export function stopAnalysisTask(taskId) {
  return request({
    url: `/api/analysis/tasks/${taskId}/stop`,
    method: 'post'
  })
}

/**
 * 删除分析任务
 * @param {number} taskId 任务ID
 */
export function deleteAnalysisTask(taskId) {
  return request({
    url: `/api/analysis/tasks/${taskId}`,
    method: 'delete'
  })
}

/**
 * 获取运行中的任务
 */
export function getRunningTasks() {
  return request({
    url: '/api/analysis/tasks/running',
    method: 'get'
  })
}

// ========== 设备通道查询API (3个接口) ==========

/**
 * 获取可分析设备列表
 */
export function getAnalysisDevices() {
  return request({
    url: '/api/analysis/devices',
    method: 'get'
  })
}

/**
 * 获取设备通道列表
 * @param {string} deviceId 设备ID
 */
export function getDeviceChannels(deviceId) {
  return request({
    url: `/api/analysis/devices/${deviceId}/channels`,
    method: 'get'
  })
}

/**
 * 验证任务配置
 * @param {Object} data 验证参数
 */
export function validateTaskConfig(data) {
  return request({
    url: '/api/analysis/tasks/validate',
    method: 'post',
    data
  })
}

// ========== 分析结果查询API (7个接口) ==========

/**
 * 查询分析结果（支持多条件筛选）
 * @param {Object} params 查询参数
 */
export function getAnalysisResults(params) {
  return request({
    url: '/api/analysis/results',
    method: 'get',
    params
  })
}

/**
 * 获取结果详情
 * @param {number} resultId 结果ID
 */
export function getAnalysisResultDetail(resultId) {
  return request({
    url: `/api/analysis/results/${resultId}`,
    method: 'get'
  })
}

/**
 * 获取实时统计信息
 */
export function getStatistics() {
  return request({
    url: '/api/analysis/results/statistics',
    method: 'get'
  })
}

/**
 * 获取今日统计
 */
export function getTodayStatistics() {
  return request({
    url: '/api/analysis/results/statistics/today',
    method: 'get'
  })
}

/**
 * 获取告警结果列表
 * @param {Object} params 查询参数
 */
export function getAlarmResults(params) {
  return request({
    url: '/api/analysis/results/alarms',
    method: 'get',
    params
  })
}

/**
 * 根据任务ID查询结果
 * @param {number} taskId 任务ID
 * @param {Object} params 查询参数
 */
export function getResultsByTaskId(taskId, params) {
  return request({
    url: `/api/analysis/results/task/${taskId}`,
    method: 'get',
    params
  })
}

/**
 * 获取分析趋势数据
 * @param {Object} params 查询参数
 */
export function getAnalysisTrends(params) {
  return request({
    url: '/api/analysis/results/trends',
    method: 'get',
    params
  })
}

/**
 * 查询最新分析结果
 * @param {Object} params 查询参数
 */
export function getLatestResults(params) {
  return request({
    url: '/api/analysis/results/latest',
    method: 'get',
    params
  })
}

/**
 * 删除分析结果
 * @param {number} resultId 结果ID
 */
export function deleteAnalysisResult(resultId) {
  return request({
    url: `/api/analysis/results/${resultId}`,
    method: 'delete'
  })
}

/**
 * 批量删除分析结果
 * @param {Array} resultIds 结果ID数组
 */
export function batchDeleteResults(resultIds) {
  return request({
    url: '/api/analysis/results/batch',
    method: 'delete',
    data: { ids: resultIds }
  })
}

// ========== 内部回调API (用于监控微服务状态) ==========

/**
 * 检查VLM微服务健康状态
 */
export function checkVlmHealth() {
  return request({
    url: '/api/internal/health',
    method: 'get'
  })
}

// ========== 辅助函数 ==========

/**
 * 格式化查询参数
 * @param {Object} params 原始参数
 * @returns {Object} 格式化后的参数
 */
export function formatQueryParams(params) {
  const formatted = { ...params }

  // 移除空值
  Object.keys(formatted).forEach(key => {
    if (formatted[key] === '' || formatted[key] === null || formatted[key] === undefined) {
      delete formatted[key]
    }
  })

  return formatted
}

/**
 * 构建时间范围查询参数
 * @param {string} startTime 开始时间
 * @param {string} endTime 结束时间
 * @returns {Object} 时间范围参数
 */
export function buildTimeRangeParams(startTime, endTime) {
  const params = {}

  if (startTime) {
    params.startTime = startTime
  }
  if (endTime) {
    params.endTime = endTime
  }

  return params
}

/**
 * 分析结果状态映射
 */
export const ANALYSIS_STATUS = {
  NORMAL: { text: '正常', type: 'success' },
  ALARM: { text: '告警', type: 'danger' },
  ERROR: { text: '错误', type: 'warning' }
}

/**
 * 任务状态映射
 */
export const TASK_STATUS = {
  CREATED: { text: '已创建', type: 'info' },
  RUNNING: { text: '运行中', type: 'success' },
  STOPPED: { text: '已停止', type: 'warning' },
  ERROR: { text: '错误', type: 'danger' }
}

/**
 * 预设分析问题模板
 */
export const QUESTION_TEMPLATES = [
  {
    name: '人员检测',
    question: '画面中是否有人员？请描述人员数量和行为'
  },
  {
    name: '车辆监控',
    question: '画面中是否有车辆？请描述车辆类型和数量'
  },
  {
    name: '异常行为',
    question: '画面中是否存在异常行为？如打架、摔倒等'
  },
  {
    name: '安全违规',
    question: '画面中是否存在安全违规行为？如未戴安全帽、违规操作等'
  },
  {
    name: '设备状态',
    question: '画面中的设备运行状态是否正常？'
  },
  {
    name: '环境监控',
    question: '画面中的环境状况如何？是否存在异常？'
  }
]

export default {
  // 任务管理
  createAnalysisTask,
  getAnalysisTasks,
  getAnalysisTaskDetail,
  updateAnalysisTask,
  startAnalysisTask,
  stopAnalysisTask,
  deleteAnalysisTask,
  getRunningTasks,

  // 设备通道
  getAnalysisDevices,
  getDeviceChannels,
  validateTaskConfig,

  // 结果查询
  getAnalysisResults,
  getAnalysisResultDetail,
  getStatistics,
  getTodayStatistics,
  getAlarmResults,
  getResultsByTaskId,
  getAnalysisTrends,
  getLatestResults,
  deleteAnalysisResult,
  batchDeleteResults,

  // 系统监控
  checkVlmHealth,

  // 辅助函数
  formatQueryParams,
  buildTimeRangeParams,

  // 常量
  ANALYSIS_STATUS,
  TASK_STATUS,
  QUESTION_TEMPLATES
}