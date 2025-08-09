import request from '@/utils/request'

// 分析卡片相关接口
export function getCards(params) {
  return request({
    url: '/api/vmanager/analysis/cards',
    method: 'get',
    params
  })
}

export function getCard(id) {
  return request({
    url: `/api/vmanager/analysis/cards/${id}`,
    method: 'get'
  })
}

export function createCard(data) {
  return request({
    url: '/api/vmanager/analysis/cards',
    method: 'post',
    data
  })
}

export function updateCard(id, data) {
  return request({
    url: `/api/vmanager/analysis/cards/${id}`,
    method: 'put',
    data
  })
}

export function deleteCard(id) {
  return request({
    url: `/api/vmanager/analysis/cards/${id}`,
    method: 'delete'
  })
}

export function toggleCard(id, enabled) {
  return request({
    url: `/api/vmanager/analysis/cards/${id}/toggle`,
    method: 'post',
    params: { enabled }
  })
}

export function getAvailableCards(params) {
  return request({
    url: '/api/vmanager/analysis/cards/available',
    method: 'get',
    params
  })
}

export function countCards(params) {
  return request({
    url: '/api/vmanager/analysis/cards/count',
    method: 'get',
    params
  })
}

// 分析任务相关接口
export function getTasks(params) {
  return request({
    url: '/api/vmanager/analysis/tasks',
    method: 'get',
    params
  })
}

export function getTask(id) {
  return request({
    url: `/api/vmanager/analysis/tasks/${id}`,
    method: 'get'
  })
}

export function createTask(data) {
  return request({
    url: '/api/vmanager/analysis/tasks',
    method: 'post',
    data
  })
}

export function updateTask(id, data) {
  return request({
    url: `/api/vmanager/analysis/tasks/${id}`,
    method: 'put',
    data
  })
}

export function deleteTask(id) {
  return request({
    url: `/api/vmanager/analysis/tasks/${id}`,
    method: 'delete'
  })
}

export function startTask(id, forceRestart = false) {
  return request({
    url: `/api/vmanager/analysis/tasks/${id}/start`,
    method: 'post',
    params: { forceRestart }
  })
}

export function pauseTask(id) {
  return request({
    url: `/api/vmanager/analysis/tasks/${id}/pause`,
    method: 'post'
  })
}

export function resumeTask(id) {
  return request({
    url: `/api/vmanager/analysis/tasks/${id}/resume`,
    method: 'post'
  })
}

export function stopTask(id) {
  return request({
    url: `/api/vmanager/analysis/tasks/${id}/stop`,
    method: 'post'
  })
}

export function batchDeleteTasks(taskIds) {
  return request({
    url: '/api/vmanager/analysis/tasks/batch',
    method: 'delete',
    data: taskIds
  })
}

export function syncTaskStatuses() {
  return request({
    url: '/api/vmanager/analysis/tasks/sync-status',
    method: 'post'
  })
}

export function countTasks(params) {
  return request({
    url: '/api/vmanager/analysis/tasks/count',
    method: 'get',
    params
  })
}

// 分析告警相关接口
export function getAlarms(params) {
  return request({
    url: '/api/vmanager/analysis/alarms',
    method: 'get',
    params
  })
}

export function getAlarm(id) {
  return request({
    url: `/api/vmanager/analysis/alarms/${id}`,
    method: 'get'
  })
}

export function getAlarmsByTaskId(taskId) {
  return request({
    url: `/api/vmanager/analysis/alarms/task/${taskId}`,
    method: 'get'
  })
}

export function getRecentAlarms(limit = 10) {
  return request({
    url: '/api/vmanager/analysis/alarms/recent',
    method: 'get',
    params: { limit }
  })
}

export function getPendingAlarms() {
  return request({
    url: '/api/vmanager/analysis/alarms/pending',
    method: 'get'
  })
}

export function updateAlarmStatus(id, status) {
  return request({
    url: `/api/vmanager/analysis/alarms/${id}/status`,
    method: 'put',
    params: { status }
  })
}

export function batchUpdateAlarmStatus(alarmIds, status) {
  return request({
    url: '/api/vmanager/analysis/alarms/batch-status',
    method: 'put',
    data: { alarmIds, status }
  })
}

export function processAlarm(id) {
  return request({
    url: `/api/vmanager/analysis/alarms/${id}/process`,
    method: 'post'
  })
}

export function ignoreAlarm(id) {
  return request({
    url: `/api/vmanager/analysis/alarms/${id}/ignore`,
    method: 'post'
  })
}

export function batchDeleteAlarms(alarmIds) {
  return request({
    url: '/api/vmanager/analysis/alarms/batch',
    method: 'delete',
    data: alarmIds
  })
}

export function countAlarms(params) {
  return request({
    url: '/api/vmanager/analysis/alarms/count',
    method: 'get',
    params
  })
}

export function countAlarmsByStatus(params) {
  return request({
    url: '/api/vmanager/analysis/alarms/count-by-status',
    method: 'get',
    params
  })
}

export function isSnapshotExists(id) {
  return request({
    url: `/api/vmanager/analysis/alarms/${id}/snapshot/exists`,
    method: 'get'
  })
}