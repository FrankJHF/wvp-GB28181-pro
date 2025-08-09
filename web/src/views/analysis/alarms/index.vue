<template>
  <div class="app-container">
    <div class="filter-container">
      <el-date-picker
        v-model="dateRange"
        type="datetimerange"
        range-separator="至"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        class="filter-item"
        style="width: 350px"
        @change="handleDateRangeChange"
      />
      <el-input
        v-model="listQuery.deviceId"
        placeholder="设备ID"
        style="width: 150px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-select
        v-model="listQuery.status"
        placeholder="告警状态"
        clearable
        class="filter-item"
        style="width: 130px"
      >
        <el-option label="待处理" value="pending" />
        <el-option label="已处理" value="resolved" />
        <el-option label="已忽略" value="ignored" />
      </el-select>
      <el-button v-waves class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">
        搜索
      </el-button>
      <el-button class="filter-item" type="success" icon="el-icon-refresh" @click="handleRefresh">
        刷新
      </el-button>
    </div>

    <el-timeline>
      <el-timeline-item
        v-for="alarm in list"
        :key="alarm.id"
        :timestamp="alarm.alarmTime"
        placement="top"
        :type="getTimelineType(alarm.status)"
      >
        <alarm-item
          :alarm="alarm"
          @detail="handleDetail"
          @process="handleProcess"
          @ignore="handleIgnore"
        />
      </el-timeline-item>
    </el-timeline>

    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="listQuery.page"
      :limit.sync="listQuery.limit"
      @pagination="getList"
    />

    <!-- 告警详情对话框 -->
    <alarm-detail
      :visible.sync="detailDialogVisible"
      :alarm="currentAlarm"
    />
  </div>
</template>

<script>
import waves from '@/directive/waves'
import Pagination from '@/components/Pagination'
import AlarmItem from './components/AlarmItem'
import AlarmDetail from './components/AlarmDetail'
import { getAlarms, processAlarm, ignoreAlarm } from '@/api/analysis'

export default {
  name: 'AnalysisAlarms',
  components: { Pagination, AlarmItem, AlarmDetail },
  directives: { waves },
  data() {
    return {
      list: [],
      total: 0,
      loading: false,
      listQuery: {
        page: 1,
        limit: 20,
        startTime: null,
        endTime: null,
        deviceId: '',
        channelId: '',
        analysisType: '',
        status: '',
        taskId: ''
      },
      dateRange: [],
      detailDialogVisible: false,
      currentAlarm: null,
      autoRefresh: false,
      refreshTimer: null
    }
  },
  created() {
    // 默认查询最近24小时的告警
    const now = new Date()
    const yesterday = new Date(now.getTime() - 24 * 60 * 60 * 1000)
    this.dateRange = [yesterday, now]
    this.handleDateRangeChange(this.dateRange)
    
    this.getList()
    this.startAutoRefresh()
  },
  beforeDestroy() {
    this.stopAutoRefresh()
  },
  methods: {
    getList() {
      this.loading = true
      getAlarms(this.listQuery).then(response => {
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
    handleRefresh() {
      this.getList()
    },
    handleDateRangeChange(dates) {
      if (dates && dates.length === 2) {
        this.listQuery.startTime = dates[0]
        this.listQuery.endTime = dates[1]
      } else {
        this.listQuery.startTime = null
        this.listQuery.endTime = null
      }
    },
    handleDetail(alarm) {
      this.currentAlarm = alarm
      this.detailDialogVisible = true
    },
    handleProcess(alarm) {
      this.$confirm(`确认处理告警"${alarm.description}"？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        return processAlarm(alarm.id)
      }).then(() => {
        this.$message({
          type: 'success',
          message: '处理成功!'
        })
        this.getList()
      }).catch(() => {})
    },
    handleIgnore(alarm) {
      this.$confirm(`确认忽略告警"${alarm.description}"？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return ignoreAlarm(alarm.id)
      }).then(() => {
        this.$message({
          type: 'success',
          message: '忽略成功!'
        })
        this.getList()
      }).catch(() => {})
    },
    getTimelineType(status) {
      const map = {
        pending: 'warning',
        resolved: 'success',
        ignored: 'info'
      }
      return map[status] || 'primary'
    },
    startAutoRefresh() {
      this.autoRefresh = true
      this.refreshTimer = setInterval(() => {
        if (this.autoRefresh) {
          this.getList()
        }
      }, 30000) // 30秒刷新一次
    },
    stopAutoRefresh() {
      this.autoRefresh = false
      if (this.refreshTimer) {
        clearInterval(this.refreshTimer)
        this.refreshTimer = null
      }
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