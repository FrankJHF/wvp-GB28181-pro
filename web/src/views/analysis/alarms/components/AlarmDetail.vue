<template>
  <el-dialog
    title="告警详情"
    :visible.sync="dialogVisible"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div v-if="alarm" class="alarm-detail">
      <el-row :gutter="24">
        <el-col :span="12">
          <div class="detail-section">
            <h4 class="section-title">基本信息</h4>
            <div class="detail-item">
              <label>告警描述:</label>
              <span>{{ alarm.description || '--' }}</span>
            </div>
            <div class="detail-item">
              <label>告警状态:</label>
              <el-tag :type="getStatusTagType()">{{ getStatusText() }}</el-tag>
            </div>
            <div class="detail-item">
              <label>告警时间:</label>
              <span>{{ formatTime(alarm.alarmTime) }}</span>
            </div>
            <div class="detail-item">
              <label>处理时间:</label>
              <span>{{ alarm.processedAt ? formatTime(alarm.processedAt) : '--' }}</span>
            </div>
          </div>

          <div class="detail-section">
            <h4 class="section-title">任务信息</h4>
            <div class="detail-item">
              <label>任务名称:</label>
              <span>{{ alarm.taskName || '--' }}</span>
            </div>
            <div class="detail-item">
              <label>分析类型:</label>
              <span>{{ getAnalysisTypeText() }}</span>
            </div>
            <div class="detail-item">
              <label>分析卡片:</label>
              <span>{{ alarm.analysisCardTitle || '--' }}</span>
            </div>
          </div>

          <div class="detail-section">
            <h4 class="section-title">设备信息</h4>
            <div class="detail-item">
              <label>设备名称:</label>
              <span>{{ alarm.deviceName || alarm.deviceId || '--' }}</span>
            </div>
            <div class="detail-item">
              <label>设备ID:</label>
              <span>{{ alarm.deviceId || '--' }}</span>
            </div>
            <div class="detail-item">
              <label>通道名称:</label>
              <span>{{ alarm.channelName || alarm.channelId || '--' }}</span>
            </div>
            <div class="detail-item">
              <label>通道ID:</label>
              <span>{{ alarm.channelId || '--' }}</span>
            </div>
          </div>
        </el-col>

        <el-col :span="12">
          <div class="detail-section">
            <h4 class="section-title">快照图片</h4>
            <div class="snapshot-container">
              <el-image
                v-if="alarm.snapshotPath"
                :src="getSnapshotUrl()"
                :preview-src-list="[getSnapshotUrl()]"
                class="detail-snapshot"
                fit="contain"
              >
                <div slot="error" class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                  <p>快照加载失败</p>
                </div>
              </el-image>
              <div v-else class="no-snapshot">
                <i class="el-icon-picture-outline"></i>
                <p>无快照图片</p>
              </div>
            </div>
          </div>

          <div v-if="alarm.videoWindowInfo" class="detail-section">
            <h4 class="section-title">检测区域</h4>
            <div class="window-info">
              <div class="window-item">
                <label>X坐标:</label>
                <span>{{ alarm.videoWindowInfo.x || 0 }}px</span>
              </div>
              <div class="window-item">
                <label>Y坐标:</label>
                <span>{{ alarm.videoWindowInfo.y || 0 }}px</span>
              </div>
              <div class="window-item">
                <label>宽度:</label>
                <span>{{ alarm.videoWindowInfo.width || 0 }}px</span>
              </div>
              <div class="window-item">
                <label>高度:</label>
                <span>{{ alarm.videoWindowInfo.height || 0 }}px</span>
              </div>
            </div>
          </div>

          <div v-if="alarm.analysisResult" class="detail-section">
            <h4 class="section-title">分析结果</h4>
            <div class="analysis-result">
              <pre>{{ formatAnalysisResult() }}</pre>
            </div>
          </div>
        </el-col>
      </el-row>

      <div v-if="alarm.notes" class="detail-section">
        <h4 class="section-title">处理备注</h4>
        <div class="notes-content">
          {{ alarm.notes }}
        </div>
      </div>
    </div>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">关闭</el-button>
      <el-button 
        v-if="canProcess" 
        type="success" 
        @click="handleProcess"
      >
        标记为已处理
      </el-button>
      <el-button 
        v-if="canIgnore" 
        type="warning" 
        @click="handleIgnore"
      >
        忽略此告警
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import { processAlarm, ignoreAlarm } from '@/api/analysis'

export default {
  name: 'AlarmDetail',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    alarm: {
      type: Object,
      default: null
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    },
    canProcess() {
      return this.alarm && this.alarm.status === 'pending'
    },
    canIgnore() {
      return this.alarm && this.alarm.status === 'pending'
    }
  },
  methods: {
    getStatusTagType() {
      if (!this.alarm) return ''
      const typeMap = {
        pending: 'danger',
        resolved: 'success',
        ignored: 'info'
      }
      return typeMap[this.alarm.status] || 'danger'
    },
    getStatusText() {
      if (!this.alarm) return '--'
      const textMap = {
        pending: '待处理',
        resolved: '已处理',
        ignored: '已忽略'
      }
      return textMap[this.alarm.status] || '未知'
    },
    getAnalysisTypeText() {
      if (!this.alarm) return '--'
      const typeMap = {
        'detection': '目标检测',
        'recognition': '目标识别',
        'tracking': '目标跟踪',
        'analysis': '行为分析',
        'emergency_exit': '紧急出口'
      }
      return typeMap[this.alarm.analysisType] || this.alarm.analysisType || '--'
    },
    getSnapshotUrl() {
      if (!this.alarm || !this.alarm.snapshotPath) return ''
      if (this.alarm.snapshotPath.startsWith('http')) {
        return this.alarm.snapshotPath
      }
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
    formatAnalysisResult() {
      if (!this.alarm || !this.alarm.analysisResult) return '无结果数据'
      
      try {
        const result = typeof this.alarm.analysisResult === 'string' 
          ? JSON.parse(this.alarm.analysisResult)
          : this.alarm.analysisResult
        return JSON.stringify(result, null, 2)
      } catch (e) {
        return this.alarm.analysisResult
      }
    },
    handleProcess() {
      this.$confirm('确认将此告警标记为已处理？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        return processAlarm(this.alarm.id)
      }).then(() => {
        this.$message.success('处理成功!')
        this.handleClose()
        this.$emit('updated')
      }).catch(error => {
        if (error !== 'cancel') {
          this.$message.error('处理失败')
        }
      })
    },
    handleIgnore() {
      this.$confirm('确认忽略此告警？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return ignoreAlarm(this.alarm.id)
      }).then(() => {
        this.$message.success('忽略成功!')
        this.handleClose()
        this.$emit('updated')
      }).catch(error => {
        if (error !== 'cancel') {
          this.$message.error('操作失败')
        }
      })
    },
    handleClose() {
      this.dialogVisible = false
    }
  }
}
</script>

<style scoped>
.alarm-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
}

.detail-item {
  display: flex;
  margin-bottom: 12px;
  align-items: center;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item label {
  font-weight: 600;
  color: #606266;
  min-width: 80px;
  margin-right: 12px;
}

.detail-item span {
  color: #303133;
  flex: 1;
}

.snapshot-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.detail-snapshot {
  max-width: 100%;
  max-height: 300px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.no-snapshot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #c0c4cc;
}

.no-snapshot i {
  font-size: 48px;
  margin-bottom: 8px;
}

.image-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 200px;
  background: #f5f7fa;
  color: #909399;
}

.image-slot i {
  font-size: 36px;
  margin-bottom: 8px;
}

.window-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.window-item {
  display: flex;
  align-items: center;
}

.window-item label {
  font-weight: 600;
  color: #606266;
  min-width: 60px;
  margin-right: 8px;
}

.analysis-result {
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  max-height: 200px;
  overflow-y: auto;
}

.analysis-result pre {
  margin: 0;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-all;
}

.notes-content {
  padding: 12px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  line-height: 1.6;
  color: #606266;
}

.dialog-footer {
  text-align: right;
}
</style>