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
        <el-option label="运行中" value="running" />
        <el-option label="已暂停" value="paused" />
        <el-option label="已停止" value="stopped" />
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
      <el-table-column label="任务名称" prop="taskName" min-width="150px" />
      <el-table-column label="分析卡片" prop="analysisCard.title" min-width="120px" />
      <el-table-column label="设备" prop="deviceName" min-width="120px" />
      <el-table-column label="通道" prop="channelName" min-width="120px" />
      <el-table-column label="状态" min-width="80px" align="center">
        <template slot-scope="{row}">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" prop="createdAt" min-width="120px" />
      <el-table-column label="操作" align="center" min-width="200px" class-name="small-padding fixed-width">
        <template slot-scope="{row}">
          <el-button type="primary" size="mini" @click="handleStart(row)" v-if="canStart(row)">
            启动
          </el-button>
          <el-button type="warning" size="mini" @click="handlePause(row)" v-if="canPause(row)">
            暂停
          </el-button>
          <el-button type="success" size="mini" @click="handleResume(row)" v-if="canResume(row)">
            恢复
          </el-button>
          <el-button type="danger" size="mini" @click="handleStop(row)" v-if="canStop(row)">
            停止
          </el-button>
          <el-button type="info" size="mini" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="danger" size="mini" @click="handleDelete(row)" v-if="canDelete(row)">
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
      @success="getList"
    />
  </div>
</template>

<script>
import waves from '@/directive/waves'
import Pagination from '@/components/Pagination'
import TaskForm from './components/TaskForm'
import { getTasks, deleteTask, startTask, pauseTask, resumeTask, stopTask, syncTaskStatuses } from '@/api/analysis'

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
        status: '',
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
      getTasks(this.listQuery).then(response => {
        this.list = response.data.list
        this.total = response.data.total
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
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
      this.executeTaskAction(startTask(row.id), '启动', row.taskName)
    },
    handlePause(row) {
      this.executeTaskAction(pauseTask(row.id), '暂停', row.taskName)
    },
    handleResume(row) {
      this.executeTaskAction(resumeTask(row.id), '恢复', row.taskName)
    },
    handleStop(row) {
      this.executeTaskAction(stopTask(row.id), '停止', row.taskName)
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
    executeTaskAction(promise, action, taskName) {
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
        this.getList()
      }).catch(error => {
        this.$message({
          type: 'error',
          message: `${action}失败: ${error.message || error}`
        })
      }).finally(() => {
        loading.close()
      })
    },
    getStatusType(status) {
      const map = {
        created: '',
        running: 'success',
        paused: 'warning',
        stopped: 'info',
        error: 'danger'
      }
      return map[status] || ''
    },
    getStatusText(status) {
      const map = {
        created: '已创建',
        running: '运行中',
        paused: '已暂停', 
        stopped: '已停止',
        error: '错误'
      }
      return map[status] || status
    },
    canStart(row) {
      return ['created', 'stopped', 'error'].includes(row.status)
    },
    canPause(row) {
      return row.status === 'running'
    },
    canResume(row) {
      return row.status === 'paused'
    },
    canStop(row) {
      return ['running', 'paused'].includes(row.status)
    },
    canDelete(row) {
      return ['created', 'stopped', 'error'].includes(row.status)
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