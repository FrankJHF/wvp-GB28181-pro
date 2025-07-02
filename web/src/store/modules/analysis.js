import {
  getAnalysisResults,
  getStatistics,
  createAnalysisTask,
  updateAnalysisTask,
  deleteAnalysisTask,
  startAnalysisTask,
  stopAnalysisTask,
  getAnalysisTasks
} from '@/api/analysis'

const actions = {
  getAnalysisResults({ commit }, params) {
    return new Promise((resolve, reject) => {
      getAnalysisResults(params).then(response => {
        if (response.code === 0) {
          resolve(response.data)
        } else {
          reject(response.msg || '获取分析结果失败')
        }
      }).catch(error => {
        reject(error)
      })
    })
  },
  getStatistics({ commit }) {
    return new Promise((resolve, reject) => {
      getStatistics().then(response => {
        if (response.code === 0) {
          resolve(response.data)
        } else {
          reject(response.msg || '获取统计数据失败')
        }
      }).catch(error => {
        reject(error)
      })
    })
  },
  createAnalysisTask({ commit }, taskData) {
    return new Promise((resolve, reject) => {
      createAnalysisTask(taskData).then(response => {
        if (response.code === 0) {
          resolve(response.data)
        } else {
          reject(response.msg || '创建任务失败')
        }
      }).catch(error => {
        reject(error)
      })
    })
  },
  updateAnalysisTask({ commit }, { taskId, taskData }) {
    return new Promise((resolve, reject) => {
      updateAnalysisTask(taskId, taskData).then(response => {
        if (response.code === 0) {
          resolve(response.data)
        } else {
          reject(response.msg || '更新任务失败')
        }
      }).catch(error => {
        reject(error)
      })
    })
  },
  deleteAnalysisTask({ commit }, taskId) {
    return new Promise((resolve, reject) => {
      deleteAnalysisTask(taskId).then(response => {
        if (response.code === 0) {
          resolve(response.data)
        } else {
          reject(response.msg || '删除任务失败')
        }
      }).catch(error => {
        reject(error)
      })
    })
  },
  startAnalysisTask({ commit }, taskId) {
    return new Promise((resolve, reject) => {
      startAnalysisTask(taskId).then(response => {
        if (response.code === 0) {
          resolve(response.data)
        } else {
          reject(response.msg || '启动任务失败')
        }
      }).catch(error => {
        reject(error)
      })
    })
  },
  stopAnalysisTask({ commit }, taskId) {
    return new Promise((resolve, reject) => {
      stopAnalysisTask(taskId).then(response => {
        if (response.code === 0) {
          resolve(response.data)
        } else {
          reject(response.msg || '停止任务失败')
        }
      }).catch(error => {
        reject(error)
      })
    })
  },
  getAnalysisTasks({ commit }, params) {
    return new Promise((resolve, reject) => {
      getAnalysisTasks(params).then(response => {
        if (response.code === 0) {
          resolve(response.data)
        } else {
          reject(response.msg || '获取任务列表失败')
        }
      }).catch(error => {
        reject(error)
      })
    })
  }
}

export default {
  namespaced: true,
  actions
}