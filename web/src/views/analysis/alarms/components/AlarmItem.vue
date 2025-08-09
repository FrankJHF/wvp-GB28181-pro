<template>
  <el-card class="alarm-item" :class="getAlarmClass()">
    <div class="alarm-header">
      <div class="alarm-title">
        <i :class="getAlarmIcon()" />
        <span class="title-text">{{ alarm.description || '未知告警' }}</span>
        <el-tag :type="getStatusTagType()" size="mini" class="status-tag">
          {{ getStatusText() }}
        </el-tag>
      </div>
      <div class="alarm-time">
        {{ formatTime(alarm.alarmTime) }}
      </div>
    </div>

    <div class="alarm-content">
      <div class="alarm-info">
        <div class="info-item">
          <span class="info-label">任务:</span>
          <span class="info-value">{{ alarm.taskName || '--' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">设备:</span>
          <span class="info-value">{{ alarm.deviceName || alarm.deviceId || '--' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">通道:</span>
          <span class="info-value">{{ alarm.channelName || alarm.channelId || '--' }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">分析类型:</span>
          <span class="info-value">{{ alarm.analysisType || '--' }}</span>
        </div>
      </div>

      <div v-if="alarm.snapshotPath" class="alarm-snapshot">
        <el-image
          :src="getSnapshotUrl()"
          :preview-src-list="[getSnapshotUrl()]"
          class="snapshot-image"
          fit="cover"
        >
          <div slot="error" class="image-slot">
            <i class="el-icon-picture-outline"></i>
          </div>
        </el-image>
      </div>
    </div>

    <div v-if="alarm.videoWindowInfo" class="video-window-info">
      <div class="window-title">检测区域信息:</div>
      <div class="window-details">
        <span>X: {{ alarm.videoWindowInfo.x || 0 }}</span>
        <span>Y: {{ alarm.videoWindowInfo.y || 0 }}</span>
        <span>宽: {{ alarm.videoWindowInfo.width || 0 }}</span>
        <span>高: {{ alarm.videoWindowInfo.height || 0 }}</span>
      </div>
    </div>

    <div class="alarm-actions">
      <el-button type="primary" size="mini" @click="handleDetail" icon="el-icon-view">
        详情
      </el-button>
      <el-button 
        v-if="canProcess" 
        type="success" 
        size="mini" 
        @click="handleProcess"
        icon="el-icon-check"
      >
        处理
      </el-button>
      <el-button 
        v-if="canIgnore" 
        type="warning" 
        size="mini" 
        @click="handleIgnore"
        icon="el-icon-close"
      >
        忽略
      </el-button>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'AlarmItem',
  props: {
    alarm: {
      type: Object,
      required: true
    }
  },
  computed: {
    canProcess() {
      return this.alarm.status === 'pending'
    },
    canIgnore() {
      return this.alarm.status === 'pending'
    }
  },
  methods: {
    getAlarmClass() {
      return `alarm-${this.alarm.status || 'pending'}`
    },
    getAlarmIcon() {
      const typeIconMap = {
        'detection': 'el-icon-view',
        'recognition': 'el-icon-search',
        'tracking': 'el-icon-location',
        'analysis': 'el-icon-pie-chart',
        'emergency_exit': 'el-icon-warning-outline'
      }
      return typeIconMap[this.alarm.analysisType] || 'el-icon-warning'
    },
    getStatusTagType() {
      const typeMap = {
        pending: 'danger',
        resolved: 'success',
        ignored: 'info'
      }
      return typeMap[this.alarm.status] || 'danger'
    },
    getStatusText() {
      const textMap = {
        pending: '待处理',
        resolved: '已处理', 
        ignored: '已忽略'
      }
      return textMap[this.alarm.status] || '未知'
    },
    getSnapshotUrl() {
      if (!this.alarm.snapshotPath) return ''
      // 如果快照路径是完整URL，直接返回
      if (this.alarm.snapshotPath.startsWith('http')) {
        return this.alarm.snapshotPath
      }
      // 否则拼接静态资源路径
      return `/api/vmanager/analysis/alarms/${this.alarm.id}/snapshot`
    },
    formatTime(timeStr) {
      if (!timeStr) return '--'
      const date = new Date(timeStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    },
    handleDetail() {
      this.$emit('detail', this.alarm)
    },
    handleProcess() {
      this.$emit('process', this.alarm)
    },
    handleIgnore() {
      this.$emit('ignore', this.alarm)
    }
  }
}
</script>

<style scoped>
.alarm-item {
  margin-bottom: 16px;
  border-left: 4px solid #dcdfe6;
}

.alarm-pending {
  border-left-color: #f56c6c;
}

.alarm-resolved {
  border-left-color: #67c23a;
}

.alarm-ignored {
  border-left-color: #909399;
}

.alarm-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.alarm-title {
  display: flex;
  align-items: center;
  flex: 1;
}

.alarm-title i {
  font-size: 18px;
  color: #f56c6c;
  margin-right: 8px;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-right: 8px;
}

.status-tag {
  margin-left: auto;
}

.alarm-time {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.alarm-content {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.alarm-info {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.info-item {
  display: flex;
  font-size: 13px;
}

.info-label {
  color: #909399;
  font-weight: 600;
  margin-right: 8px;
  min-width: 60px;
}

.info-value {
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.alarm-snapshot {
  flex-shrink: 0;
}

.snapshot-image {
  width: 120px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.video-window-info {
  margin-bottom: 12px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
}

.window-title {
  font-weight: 600;
  color: #606266;
  margin-bottom: 4px;
}

.window-details {
  display: flex;
  gap: 12px;
  color: #909399;
}

.alarm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}
</style>