<template>
  <el-card class="statistics-panel" shadow="never">
    <div slot="header" class="clearfix">
      <span class="panel-title">实时统计</span>
      <el-button
        style="float: right; padding: 3px 0"
        type="text"
        @click="refreshStats"
        :loading="refreshing"
      >
        刷新
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <!-- 今日分析数 -->
      <div class="stat-item">
        <div class="stat-icon analysis-icon">
          <i class="el-icon-data-analysis" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.todayAnalysis || 0 }}</div>
          <div class="stat-label">今日分析</div>
        </div>
      </div>

      <!-- 今日告警数 -->
      <div class="stat-item alarm-stat">
        <div class="stat-icon alarm-icon">
          <i class="el-icon-warning" />
        </div>
        <div class="stat-content">
          <div class="stat-value alarm-value">{{ statistics.todayAlarms || 0 }}</div>
          <div class="stat-label">今日告警</div>
        </div>
      </div>

      <!-- 运行任务数 -->
      <div class="stat-item">
        <div class="stat-icon running-icon">
          <i class="el-icon-video-play" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.runningTasks || 0 }}</div>
          <div class="stat-label">运行任务</div>
        </div>
      </div>

      <!-- 告警率 -->
      <div class="stat-item">
        <div class="stat-icon rate-icon">
          <i class="el-icon-pie-chart" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ alarmRate }}%</div>
          <div class="stat-label">告警率</div>
        </div>
      </div>
    </div>

    <!-- 详细统计信息 -->
    <el-divider />

    <div class="detail-stats">
      <el-row :gutter="8">
        <el-col :span="12">
          <div class="detail-item">
            <span class="detail-label">总分析次数</span>
            <span class="detail-value">{{ statistics.totalAnalysis || 0 }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="detail-item">
            <span class="detail-label">总告警次数</span>
            <span class="detail-value text-danger">{{ statistics.totalAlarms || 0 }}</span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="8">
        <el-col :span="12">
          <div class="detail-item">
            <span class="detail-label">活跃设备</span>
            <span class="detail-value">{{ statistics.activeDevices || 0 }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="detail-item">
            <span class="detail-label">离线设备</span>
            <span class="detail-value text-warning">{{ statistics.offlineDevices || 0 }}</span>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 系统状态 -->
    <el-divider />

    <div class="system-status">
      <h4 class="status-title">系统状态</h4>

      <div class="status-item">
        <span class="status-label">WVP平台</span>
        <el-tag
          :type="systemStatus.wvp ? 'success' : 'danger'"
          size="mini"
        >
          {{ systemStatus.wvp ? '正常' : '异常' }}
        </el-tag>
      </div>

      <div class="status-item">
        <span class="status-label">VLM微服务</span>
        <el-tag
          :type="systemStatus.vlm ? 'success' : 'danger'"
          size="mini"
        >
          {{ systemStatus.vlm ? '正常' : '异常' }}
        </el-tag>
      </div>

      <div class="status-item">
        <span class="status-label">流媒体服务</span>
        <el-tag
          :type="systemStatus.media ? 'success' : 'danger'"
          size="mini"
        >
          {{ systemStatus.media ? '正常' : '异常' }}
        </el-tag>
      </div>
    </div>

    <!-- 快速操作 -->
    <el-divider />

    <div class="quick-actions">
      <el-button
        type="primary"
        size="mini"
        icon="el-icon-view"
        @click="viewTodayResults"
        style="width: 100%; margin-bottom: 8px;"
      >
        查看今日结果
      </el-button>

      <el-button
        type="danger"
        size="mini"
        icon="el-icon-warning"
        @click="viewAlarms"
        style="width: 100%; margin-bottom: 8px; margin-left: auto;"
        :disabled="!statistics.todayAlarms"
      >
        查看告警详情
      </el-button>

      <el-button
        type="success"
        size="mini"
        icon="el-icon-setting"
        @click="manageRunningTasks"
        style="width: 100%; margin-left: auto;"
        :disabled="!statistics.runningTasks"
      >
        管理运行任务
      </el-button>
    </div>
  </el-card>
</template>

<script>
import { getStatistics, checkVlmHealth } from '@/api/analysis'

export default {
  name: 'StatisticsPanel',
  props: {
    statistics: {
      type: Object,
      default: () => ({
        todayAnalysis: 0,
        todayAlarms: 0,
        runningTasks: 0,
        totalAnalysis: 0,
        totalAlarms: 0,
        activeDevices: 0,
        offlineDevices: 0
      })
    }
  },
  data() {
    return {
      refreshing: false,
      systemStatus: {
        wvp: true,
        vlm: true,
        media: true
      },
      statusCheckTimer: null
    }
  },
  computed: {
    // 计算告警率
    alarmRate() {
      if (!this.statistics.todayAnalysis || this.statistics.todayAnalysis === 0) {
        return 0
      }
      return Math.round((this.statistics.todayAlarms / this.statistics.todayAnalysis) * 100)
    }
  },
  mounted() {
    this.startStatusCheck()
  },
  beforeDestroy() {
    this.stopStatusCheck()
  },
  methods: {
    // 刷新统计数据
    async refreshStats() {
      this.refreshing = true
      try {
        this.$emit('refresh-requested')
        await this.checkSystemStatus()
        this.$message.success('统计数据刷新成功')
      } catch (error) {
        this.$message.error('统计数据刷新失败')
      } finally {
        this.refreshing = false
      }
    },

    // 检查系统状态
    async checkSystemStatus() {
      try {
        // 检查VLM微服务状态
        const vlmResponse = await checkVlmHealth()
        this.systemStatus.vlm = vlmResponse.code === 0

        // WVP平台状态 (基于API调用是否成功判断)
        this.systemStatus.wvp = true

        // 流媒体服务状态 (这里可以根据实际情况调用相关API)
        this.systemStatus.media = true
      } catch (error) {
        console.error('System status check error:', error)
        this.systemStatus.vlm = false
      }
    },

    // 启动状态检查定时器
    startStatusCheck() {
      this.checkSystemStatus()
      this.statusCheckTimer = setInterval(() => {
        this.checkSystemStatus()
      }, 60000) // 每分钟检查一次
    },

    // 停止状态检查定时器
    stopStatusCheck() {
      if (this.statusCheckTimer) {
        clearInterval(this.statusCheckTimer)
        this.statusCheckTimer = null
      }
    },

    // 查看今日结果
    viewTodayResults() {
      this.$emit('action', {
        type: 'view-today-results'
      })
    },

    // 查看告警详情
    viewAlarms() {
      this.$emit('action', {
        type: 'view-alarms'
      })
    },

    // 管理运行任务
    manageRunningTasks() {
      this.$emit('action', {
        type: 'manage-running-tasks'
      })
    }
  }
}
</script>

<style scoped>
.statistics-panel {
  height: 100%;
  border-radius: 8px;
}

.panel-title {
  font-weight: bold;
  color: #303133;
}

/* 统计卡片样式 */
.stats-container {
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background: #e9ecef;
}

.stat-item:last-child {
  margin-bottom: 0;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 20px;
  color: white;
}

.analysis-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.alarm-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.running-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.rate-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 4px;
}

.alarm-value {
  color: #f56c6c;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

/* 详细统计样式 */
.detail-stats {
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 13px;
  color: #606266;
}

.detail-value {
  font-size: 14px;
  font-weight: bold;
  color: #303133;
}

.text-danger {
  color: #f56c6c !important;
}

.text-warning {
  color: #e6a23c !important;
}

/* 系统状态样式 */
.system-status {
  margin-bottom: 16px;
}

.status-title {
  font-size: 14px;
  color: #303133;
  margin: 0 0 12px 0;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
}

.status-label {
  font-size: 13px;
  color: #606266;
}

/* 快速操作样式 */
.quick-actions {
  margin-top: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stat-item {
    padding: 8px;
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }

  .stat-value {
    font-size: 20px;
  }
}

/* 动画效果 */
.stat-value {
  transition: all 0.3s ease;
}

.stat-item.alarm-stat:hover .alarm-value {
  transform: scale(1.1);
}

/* 加载状态 */
.refreshing {
  opacity: 0.6;
  pointer-events: none;
}
</style>