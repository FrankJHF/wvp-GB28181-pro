<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.taskName"
        placeholder="任务名称"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-select
        v-model="listQuery.status"
        placeholder="任务状态"
        clearable
        class="filter-item"
        style="width: 130px"
      >
        <el-option label="已创建" value="created" />
        <el-option label="启动中" value="starting" />
        <el-option label="运行中" value="running" />
        <el-option label="暂停中" value="pausing" />
        <el-option label="已暂停" value="paused" />
        <el-option label="恢复中" value="resuming" />
        <el-option label="停止中" value="stopping" />
        <el-option label="已停止" value="stopped" />
        <el-option label="已取消" value="cancelled" />
        <el-option label="失败" value="failed" />
        <el-option label="错误" value="error" />
      </el-select>
      <el-button v-waves class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">
        搜索
      </el-button>
      <el-button class="filter-item" style="margin-left: 10px;" type="primary" icon="el-icon-plus" @click="handleCreate">
        创建任务
      </el-button>
      <el-button class="filter-item" type="info" icon="el-icon-refresh" @click="handleSyncStatus">
        同步状态
      </el-button>
    </div>

    <el-table
      :key="tableKey"
      v-loading="loading"
      :data="list"
      border
      fit
      highlight-current-row
      style="width: 100%;"
    >
      <el-table-column label="任务名称" prop="task_name" min-width="150px" />
      <el-table-column label="分析卡片" prop="analysisCard.title" min-width="120px" />
      <el-table-column label="设备" prop="device_name" min-width="120px" />
      <el-table-column label="通道" prop="channel_name" min-width="120px" />
      <el-table-column label="状态" min-width="100px" align="center">
        <template slot-scope="{row}">
          <el-tag :type="getStatusType(row.status)">
            <i v-if="isTransitioning(row.status)" class="el-icon-loading" style="margin-right: 5px;"></i>
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="created_at" min-width="120px" />
      <el-table-column label="操作" align="center" min-width="280px" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button 
            type="primary" 
            size="mini" 
            @click="handleStart(row)" 
            v-if="canStart(row)"
            :disabled="isTransitioning(row.status)"
          >
            启动
          </el-button>
          <el-button 
            type="warning" 
            size="mini" 
            @click="handlePause(row)" 
            v-if="canPause(row)"
            :disabled="isTransitioning(row.status)"
          >
            暂停
          </el-button>
          <el-button 
            type="success" 
            size="mini" 
            @click="handleResume(row)" 
            v-if="canResume(row)"
            :disabled="isTransitioning(row.status)"
          >
            恢复
          </el-button>
          <el-button 
            type="danger" 
            size="mini" 
            @click="handleCancel(row)" 
            v-if="canCancel(row)"
            :disabled="isTransitioning(row.status)"
          >
            取消
          </el-button>
          <el-button 
            type="info" 
            size="mini" 
            @click="handleEdit(row)"
            :disabled="isTransitioning(row.status)"
          >
            编辑
          </el-button>
          <el-button 
            type="danger" 
            size="mini" 
            @click="handleDelete(row)" 
            v-if="canDelete(row)"
            :disabled="isTransitioning(row.status)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="listQuery.page"
      :limit.sync="listQuery.limit"
      @pagination="getList"
    />

    <!-- 任务表单对话框 -->
    <task-form
      :visible.sync="dialogVisible"
      :task="currentTask"
      :is-edit="isEdit"
      :default-card-id="defaultCardId"
      @success="handleCreationSuccess"
    />
  </div>
</template>

<script>
import waves from '@/directive/waves'
import Pagination from '@/components/Pagination'
import TaskForm from './components/TaskForm'
import { getTasks, deleteTask, startTask, pauseTask, resumeTask, cancelTask, syncTaskStatuses } from '@/api/analysis'

export default {
  name: 'AnalysisTasks',
  components: { Pagination, TaskForm },
  directives: { waves },
  data() {
    return {
      tableKey: 0,
      list: [],
      total: 0,
      loading: false,
      listQuery: {
        page: 1,
        limit: 20,
        taskName: '',
        status: null,
        deviceId: '',
        channelId: '',
        analysisCardId: ''
      },
      dialogVisible: false,
      isEdit: false,
      currentTask: null,
      defaultCardId: null
    }
  },
  created() {
    // 从路由参数获取默认卡片ID
    if (this.$route.query.cardId) {
      this.defaultCardId = this.$route.query.cardId
      this.listQuery.analysisCardId = this.defaultCardId
    }
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      // 构建查询参数，过滤掉空值
      const query = {}
      Object.keys(this.listQuery).forEach(key => {
        const value = this.listQuery[key]
        if (value !== null && value !== '' && value !== undefined) {
          query[key] = value
        }
      })
      
      getTasks(query).then(response => {
        this.list = response.data.list || response.data
        this.total = response.data.total || (response.data.list ? response.data.list.length : 0)
        this.loading = false
      }).catch(error => {
        console.error('获取任务列表失败:', error)
        this.$message.error('获取任务列表失败')
        this.loading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      // 处理状态为null的情况，避免后端查询异常
      const query = { ...this.listQuery }
      if (query.status === null || query.status === '') {
        delete query.status
      }
      this.getList()
    },
    handleCreate() {
      this.currentTask = null
      this.isEdit = false
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.currentTask = row
      this.isEdit = true
      this.dialogVisible = true
    },
    handleDelete(row) {
      this.$confirm(`确认删除任务"${row.taskName}"？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return deleteTask(row.id)
      }).then(() => {
        this.$message({
          type: 'success',
          message: '删除成功!'
        })
        this.getList()
      }).catch(() => {})
    },
    handleStart(row) {
      this.executeTaskAction(startTask(row.id), '启动')
    },
    handlePause(row) {
      this.executeTaskAction(pauseTask(row.id), '暂停')
    },
    handleResume(row) {
      this.executeTaskAction(resumeTask(row.id), '恢复')
    },
    handleCancel(row) {
      this.executeTaskAction(cancelTask(row.id), '取消')
    },
    handleSyncStatus() {
      this.$confirm('确认同步所有任务状态？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        return syncTaskStatuses()
      }).then(response => {
        this.$message({
          type: 'success',
          message: `同步完成，共同步 ${response.data} 个任务状态`
        })
        this.getList()
      }).catch(() => {})
    },
    executeTaskAction(promise, action) {
      const loading = this.$loading({
        lock: true,
        text: `${action}中...`,
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      
      promise.then(() => {
        this.$message({
          type: 'success',
          message: `${action}成功!`
        })
        // 立即刷新列表
        this.getList()
      }).catch(error => {
        this.$message({
          type: 'error',
          message: `${action}失败: ${error.message || error}`
        })
        // 操作失败也要刷新状态
        this.getList()
      }).finally(() => {
        loading.close()
      })
    },
    getStatusType(status) {
      const map = {
        // 创建状态
        'created': '',
        'CREATED': '',
        // 运行状态  
        'running': 'success',
        'RUNNING': 'success',
        // 过渡状态
        'starting': 'warning',
        'STARTING': 'warning',
        'pausing': 'warning',
        'PAUSING': 'warning',
        'resuming': 'warning',
        'RESUMING': 'warning',
        'stopping': 'warning',
        'STOPPING': 'warning',
        // 暂停状态
        'paused': 'info',
        'PAUSED': 'info',
        // 终止状态
        'stopped': 'info',
        'STOPPED': 'info',
        'cancelled': 'info',
        'CANCELLED': 'info',
        // 错误状态
        'failed': 'danger',
        'FAILED': 'danger',
        'error': 'danger',
        'ERROR': 'danger'
      }
      return map[status] || ''
    },
    getStatusText(status) {
      const map = {
        // 创建状态
        'created': '已创建',
        'CREATED': '已创建',
        // 运行状态
        'running': '运行中',
        'RUNNING': '运行中', 
        // 过渡状态
        'starting': '启动中',
        'STARTING': '启动中',
        'pausing': '暂停中',
        'PAUSING': '暂停中',
        'resuming': '恢复中',
        'RESUMING': '恢复中',
        'stopping': '停止中',
        'STOPPING': '停止中',
        // 暂停状态
        'paused': '已暂停',
        'PAUSED': '已暂停',
        // 终止状态
        'stopped': '已停止',
        'STOPPED': '已停止',
        'cancelled': '已取消',
        'CANCELLED': '已取消',
        // 错误状态  
        'failed': '失败',
        'FAILED': '失败',
        'error': '错误',
        'ERROR': '错误'
      }
      return map[status] || status
    },
    canStart(row) {
      const status = row.status ? row.status.toLowerCase() : ''
      // 能启动的状态：已创建、已停止、已取消、失败、错误
      return ['created', 'stopped', 'cancelled', 'failed', 'error'].includes(status)
    },
    canPause(row) {
      const status = row.status ? row.status.toLowerCase() : ''
      // 只有运行中才能暂停
      return status === 'running'
    },
    canResume(row) {
      const status = row.status ? row.status.toLowerCase() : ''
      // 只有已暂停才能恢复
      return status === 'paused'
    },
    canCancel(row) {
      const status = row.status ? row.status.toLowerCase() : ''
      // 能取消的状态：运行中、已暂停（不包括过渡状态）
      return ['running', 'paused'].includes(status)
    },
    canDelete(row) {
      const status = row.status ? row.status.toLowerCase() : ''
      // 能删除的状态：非运行和非过渡状态
      return ['created', 'stopped', 'cancelled', 'failed', 'error'].includes(status)
    },
    isTransitioning(status) {
      const transitioningStates = ['starting', 'pausing', 'resuming', 'stopping']
      return status ? transitioningStates.includes(status.toLowerCase()) : false
    },
    handleCreationSuccess(taskData) {
      // 重置筛选条件，确保新建的任务能显示
      this.listQuery = {
        page: 1,
        limit: 20,
        taskName: '',
        status: null,
        deviceId: '',
        channelId: '',
        analysisCardId: this.defaultCardId || '' // 保留默认卡片ID的筛选
      }
      this.getList()
      
      // 如果是自动启动的任务，监控状态变化
      if (taskData && taskData.autoStart) {
        this.$message.info('任务已设置为自动启动，正在监控状态变化...')
        this.monitorTaskStatus(taskData.id, 3) // 监控3次，每次间隔2秒
      }
    },
    
    // 监控特定任务的状态变化
    monitorTaskStatus(taskId, remainingAttempts) {
      if (remainingAttempts <= 0) {
        return
      }
      
      setTimeout(() => {
        // 获取任务当前状态
        this.getTasks(this.listQuery).then(response => {
          const tasks = response.data.list || response.data
          const task = tasks.find(t => t.id === taskId)
          
          if (task && task.status && task.status.toLowerCase() === 'running') {
            this.$message.success('任务已成功启动并运行中')
            return // 任务已运行，停止监控
          }
          
          // 继续监控
          if (remainingAttempts > 1) {
            this.monitorTaskStatus(taskId, remainingAttempts - 1)
          }
        }).catch(() => {
          // 请求失败，停止监控
        })
      }, 2000) // 2秒后检查
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.filter-container {
  margin-bottom: 20px;
}
.filter-item {
  margin-right: 10px;
}
</style>