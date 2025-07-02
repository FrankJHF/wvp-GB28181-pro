<template>
  <el-card
    class="result-card"
    :class="{
      'alarm-card': result.isAlarm,
      'normal-card': !result.isAlarm
    }"
    shadow="hover"
    @click.native="handleCardClick"
  >
    <!-- 卡片头部 -->
    <div slot="header" class="card-header">
      <div class="header-left">
        <el-tag
          :type="result.isAlarm ? 'danger' : 'success'"
          size="small"
          class="status-tag"
        >
          {{ result.isAlarm ? 'ALARM' : 'NORMAL' }}
        </el-tag>

        <span class="device-info">{{ result.deviceId }}</span>
      </div>

      <div class="header-right">
        <el-dropdown @command="handleAction" trigger="click">
          <span class="el-dropdown-link">
            <i class="el-icon-more el-icon--right" />
          </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="view-detail">查看详情</el-dropdown-item>
            <el-dropdown-item command="view-image" :disabled="!result.keyFrame">查看关键帧</el-dropdown-item>
            <el-dropdown-item command="copy-info">复制信息</el-dropdown-item>
            <el-dropdown-item command="export" divided>导出数据</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>

    <!-- 卡片内容 -->
    <div class="card-content">
      <!-- 时间信息 -->
      <div class="time-info">
        <i class="el-icon-time" />
        <span>{{ formatTime(result.resultTimestamp) }}</span>
      </div>

      <!-- 设备通道信息 -->
      <div class="device-channel-info">
        <div class="info-item">
          <label>通道:</label>
          <span>{{ result.channelId }}</span>
        </div>
      </div>

      <!-- 分析问题 -->
      <div class="question-section">
        <div class="section-title">
          <i class="el-icon-question" />
          <span>分析问题</span>
        </div>
        <div class="question-content" :title="result.analysisQuestion">
          {{ result.analysisQuestion }}
        </div>
      </div>

      <!-- 分析答案 -->
      <div class="answer-section">
        <div class="section-title">
          <i class="el-icon-chat-dot-round" />
          <span>分析结果</span>
        </div>
        <div
          class="answer-content"
          :class="{ 'alarm-answer': result.isAlarm }"
          :title="result.analysisAnswer"
        >
          {{ result.analysisAnswer }}
        </div>
      </div>

      <!-- 关键帧预览 -->
      <div v-if="result.keyFrame" class="keyframe-section">
        <div class="section-title">
          <i class="el-icon-picture" />
          <span>关键帧</span>
        </div>
        <div class="keyframe-preview" @click.stop="viewKeyFrame">
          <el-image
            :src="result.keyFrame"
            fit="cover"
            class="keyframe-image"
            :preview-src-list="[result.keyFrame]"
          >
            <div slot="error" class="image-error">
              <i class="el-icon-picture-outline" />
              <span>图片加载失败</span>
            </div>
          </el-image>

          <!-- 图片覆盖层 -->
          <div class="image-overlay">
            <i class="el-icon-zoom-in" />
            <span>点击查看</span>
          </div>
        </div>
      </div>

      <!-- 置信度信息 -->
      <div v-if="result.confidence" class="confidence-section">
        <div class="confidence-bar">
          <span class="confidence-label">置信度</span>
          <div class="confidence-progress">
            <el-progress
              :percentage="Math.round(result.confidence * 100)"
              :color="getConfidenceColor(result.confidence)"
              :show-text="false"
              :stroke-width="6"
            />
          </div>
          <span class="confidence-value">{{ Math.round(result.confidence * 100) }}%</span>
        </div>
      </div>
    </div>

    <!-- 卡片底部 -->
    <div class="card-footer">
      <div class="footer-left">
        <el-tooltip content="任务ID" placement="top">
          <span class="task-id">Task #{{ result.taskId }}</span>
        </el-tooltip>
      </div>

      <div class="footer-right">
        <el-button
          type="text"
          size="mini"
          @click.stop="handleCardClick"
          class="detail-btn"
        >
          查看详情
          <i class="el-icon-arrow-right" />
        </el-button>
      </div>
    </div>

    <!-- 告警脉冲效果 -->
    <div v-if="result.isAlarm" class="alarm-pulse" />
  </el-card>
</template>

<script>
import dayjs from 'dayjs'

export default {
  name: 'ResultCard',
  props: {
    result: {
      type: Object,
      required: true,
      default: () => ({
        id: '',
        taskId: '',
        deviceId: '',
        channelId: '',
        analysisQuestion: '',
        analysisAnswer: '',
        isAlarm: false,
        resultTimestamp: '',
        keyFrame: '',
        confidence: null
      })
    }
  },
  methods: {
    // 处理卡片点击
    handleCardClick() {
      this.$emit('click', this.result)
    },

    // 处理下拉菜单操作
    handleAction(command) {
      this.$emit('action', {
        type: command,
        result: this.result
      })

      switch (command) {
        case 'view-detail':
          this.handleCardClick()
          break
        case 'view-image':
          this.viewKeyFrame()
          break
        case 'copy-info':
          this.copyResultInfo()
          break
        case 'export':
          this.exportResult()
          break
      }
    },

    // 查看关键帧
    viewKeyFrame() {
      if (this.result.keyFrame) {
        // 触发图片预览
        this.$emit('action', {
          type: 'view-keyframe',
          result: this.result
        })
      }
    },

    // 复制结果信息
    copyResultInfo() {
      const info = `
设备ID: ${this.result.deviceId}
通道ID: ${this.result.channelId}
分析时间: ${this.result.resultTimestamp}
状态: ${this.result.isAlarm ? '告警' : '正常'}
问题: ${this.result.analysisQuestion}
答案: ${this.result.analysisAnswer}
      `.trim()

      navigator.clipboard.writeText(info).then(() => {
        this.$message.success('信息已复制到剪贴板')
      }).catch(() => {
        this.$message.error('复制失败')
      })
    },

    // 导出结果
    exportResult() {
      this.$emit('action', {
        type: 'export-result',
        result: this.result
      })
    },

    // 格式化时间
    formatTime(timestamp) {
      if (!timestamp) return '--'

      const now = dayjs()
      const time = dayjs(timestamp)
      const diffInMinutes = now.diff(time, 'minute')

      if (diffInMinutes < 1) {
        return '刚刚'
      } else if (diffInMinutes < 60) {
        return `${diffInMinutes}分钟前`
      } else if (diffInMinutes < 1440) { // 24小时
        return `${Math.floor(diffInMinutes / 60)}小时前`
      } else {
        return time.format('MM-DD HH:mm')
      }
    },

    // 获取置信度颜色
    getConfidenceColor(confidence) {
      if (confidence >= 0.8) return '#67c23a'
      if (confidence >= 0.6) return '#e6a23c'
      if (confidence >= 0.4) return '#f56c6c'
      return '#909399'
    }
  }
}
</script>

<style scoped>
.result-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.result-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 告警卡片样式 */
.alarm-card {
  border-left: 4px solid #f56c6c;
  position: relative;
}

.alarm-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #f56c6c, #ff8a8a);
}

/* 正常卡片样式 */
.normal-card {
  border-left: 4px solid #67c23a;
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
  margin: -8px -8px 8px -8px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-tag {
  font-weight: bold;
  border-radius: 12px;
}

.device-info {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}

.header-right {
  color: #909399;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.header-right:hover {
  background: #e9ecef;
  color: #606266;
}

/* 卡片内容 */
.card-content {
  padding: 0;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 12px;
}

.device-channel-info {
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin-bottom: 4px;
}

.info-item label {
  color: #909399;
  font-weight: normal;
  min-width: 40px;
}

.info-item span {
  color: #606266;
  font-family: monospace;
}

/* 问题和答案区域 */
.question-section,
.answer-section {
  margin-bottom: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.question-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

.answer-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  font-weight: 500;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  word-break: break-word;
}

.alarm-answer {
  color: #f56c6c;
  font-weight: 600;
}

/* 关键帧区域 */
.keyframe-section {
  margin-bottom: 12px;
}

.keyframe-preview {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  height: 120px;
  cursor: pointer;
}

.keyframe-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: #f5f7fa;
  color: #c0c4cc;
}

.image-error i {
  font-size: 24px;
  margin-bottom: 4px;
}

.image-error span {
  font-size: 12px;
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.keyframe-preview:hover .image-overlay {
  opacity: 1;
}

.image-overlay i {
  font-size: 20px;
  margin-bottom: 4px;
}

.image-overlay span {
  font-size: 12px;
}

/* 置信度区域 */
.confidence-section {
  margin-bottom: 12px;
}

.confidence-bar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.confidence-label {
  font-size: 12px;
  color: #909399;
  min-width: 40px;
}

.confidence-progress {
  flex: 1;
}

.confidence-value {
  font-size: 12px;
  font-weight: bold;
  color: #303133;
  min-width: 30px;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.task-id {
  font-size: 11px;
  color: #c0c4cc;
  font-family: monospace;
}

.detail-btn {
  font-size: 12px;
  padding: 0;
  color: #409eff;
}

.detail-btn:hover {
  color: #66b1ff;
}

/* 告警脉冲效果 */
.alarm-pulse {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 12px;
  height: 12px;
  background: #f56c6c;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.7);
  }

  70% {
    transform: scale(1);
    box-shadow: 0 0 0 10px rgba(245, 108, 108, 0);
  }

  100% {
    transform: scale(0.95);
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0);
  }
}

/* 下拉菜单样式 */
.el-dropdown-link {
  cursor: pointer;
  color: #409EFF;
}

.el-dropdown-link:hover {
  color: #66b1ff;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .card-header {
    margin: -12px -12px 12px -12px;
    padding: 12px;
  }

  .question-content,
  .answer-content {
    font-size: 12px;
  }

  .keyframe-preview {
    height: 100px;
  }
}

/* 加载动画 */
.result-card.loading {
  opacity: 0.6;
  pointer-events: none;
}

/* 卡片焦点状态 */
.result-card:focus {
  outline: 2px solid #409eff;
  outline-offset: 2px;
}
</style>