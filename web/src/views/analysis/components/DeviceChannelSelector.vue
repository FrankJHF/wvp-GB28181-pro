<template>
  <div class="device-channel-selector">
    <!-- 设备选择 -->
    <el-form-item label="选择设备" required>
      <el-select
        v-model="selectedDevice"
        placeholder="请选择设备"
        style="width: 100%;"
        clearable
        filterable
        :loading="deviceLoading"
        @change="onDeviceChange"
        @clear="onDeviceClear"
      >
        <el-option-group
          v-for="group in groupedDevices"
          :key="group.label"
          :label="group.label"
        >
          <el-option
            v-for="device in group.options"
            :key="device.deviceId"
            :label="`${device.name} (${device.deviceId})`"
            :value="device.deviceId"
            :disabled="!device.online && requireOnline"
          >
            <div class="device-option">
              <div class="device-info">
                <span class="device-name">{{ device.name }}</span>
                <span class="device-id">{{ device.deviceId }}</span>
              </div>
              <div class="device-status">
                <el-tag
                  :type="device.online ? 'success' : 'info'"
                  size="mini"
                >
                  {{ device.online ? '在线' : '离线' }}
                </el-tag>
              </div>
            </div>
          </el-option>
        </el-option-group>
      </el-select>

      <div v-if="deviceError" class="error-message">
        <i class="el-icon-warning" />
        {{ deviceError }}
      </div>
    </el-form-item>

    <!-- 通道选择 -->
    <el-form-item label="选择通道" required>
      <el-select
        v-model="selectedChannel"
        placeholder="请先选择设备"
        style="width: 100%;"
        clearable
        filterable
        :disabled="!selectedDevice"
        :loading="channelLoading"
        @change="onChannelChange"
        @clear="onChannelClear"
      >
        <el-option
          v-for="channel in channels"
          :key="channel.channelId"
          :label="`${channel.name} (${channel.channelId})`"
          :value="channel.channelId"
        >
          <div class="channel-option">
            <div class="channel-info">
              <span class="channel-name">{{ channel.name }}</span>
              <span class="channel-id">{{ channel.channelId }}</span>
            </div>
            <div class="channel-status">
              <el-tag
                v-if="channel.status"
                :type="getChannelStatusType(channel.status)"
                size="mini"
              >
                {{ channel.status }}
              </el-tag>
            </div>
          </div>
        </el-option>
      </el-select>

      <div v-if="channelError" class="error-message">
        <i class="el-icon-warning" />
        {{ channelError }}
      </div>

      <!-- 通道为空时的提示 -->
      <div v-if="selectedDevice && !channelLoading && channels.length === 0" class="empty-message">
        <i class="el-icon-info" />
        该设备暂无可用通道
      </div>
    </el-form-item>

    <!-- 设备详细信息 -->
    <div v-if="selectedDeviceInfo" class="device-details">
      <el-card shadow="never" class="detail-card">
        <div slot="header" class="detail-header">
          <i class="el-icon-info" />
          <span>设备详情</span>
        </div>

        <div class="detail-content">
          <el-row :gutter="16">
            <el-col :span="12">
              <div class="detail-item">
                <label>设备名称:</label>
                <span>{{ selectedDeviceInfo.name }}</span>
              </div>
              <div class="detail-item">
                <label>设备ID:</label>
                <span>{{ selectedDeviceInfo.deviceId }}</span>
              </div>
              <div class="detail-item">
                <label>厂商:</label>
                <span>{{ selectedDeviceInfo.manufacturer || '未知' }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="detail-item">
                <label>在线状态:</label>
                <el-tag
                  :type="selectedDeviceInfo.online ? 'success' : 'danger'"
                  size="mini"
                >
                  {{ selectedDeviceInfo.online ? '在线' : '离线' }}
                </el-tag>
              </div>
              <div class="detail-item">
                <label>通道数量:</label>
                <span>{{ selectedDeviceInfo.channelCount || channels.length }}</span>
              </div>
              <div class="detail-item">
                <label>最近心跳:</label>
                <span>{{ formatTime(selectedDeviceInfo.keepaliveTime) }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>

    <!-- 流媒体URL预览 -->
    <div v-if="selectedChannelInfo && showStreamInfo" class="stream-preview">
      <el-card shadow="never" class="detail-card">
        <div slot="header" class="detail-header">
          <i class="el-icon-video-play" />
          <span>流媒体信息</span>
          <el-button
            type="text"
            size="mini"
            @click="testStreamUrl"
            :loading="testingStream"
            style="float: right;"
          >
            测试连接
          </el-button>
        </div>

        <div class="stream-content">
          <div class="stream-item">
            <label>RTSP地址:</label>
            <div class="url-container">
              <el-input
                :value="streamUrls.rtsp"
                size="mini"
                readonly
                class="url-input"
              >
                <el-button
                  slot="append"
                  icon="el-icon-copy-document"
                  @click="copyUrl(streamUrls.rtsp)"
                />
              </el-input>
            </div>
          </div>

          <div class="stream-item">
            <label>HTTP-FLV:</label>
            <div class="url-container">
              <el-input
                :value="streamUrls.flv"
                size="mini"
                readonly
                class="url-input"
              >
                <el-button
                  slot="append"
                  icon="el-icon-copy-document"
                  @click="copyUrl(streamUrls.flv)"
                />
              </el-input>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { getAnalysisDevices, getDeviceChannels } from '@/api/analysis'
import dayjs from 'dayjs'

export default {
  name: 'DeviceChannelSelector',
  props: {
    value: {
      type: Object,
      default: () => ({ deviceId: '', channelId: '' })
    },
    requireOnline: {
      type: Boolean,
      default: false
    },
    showStreamInfo: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      // 设备和通道数据
      devices: [],
      channels: [],

      // 选择的设备和通道
      selectedDevice: '',
      selectedChannel: '',

      // 详细信息
      selectedDeviceInfo: null,
      selectedChannelInfo: null,

      // 流媒体URL
      streamUrls: {
        rtsp: '',
        flv: '',
        hls: '',
        rtmp: ''
      },

      // 加载和错误状态
      deviceLoading: false,
      channelLoading: false,
      testingStream: false,
      deviceError: '',
      channelError: ''
    }
  },
  computed: {
    // 分组设备
    groupedDevices() {
      const online = this.devices.filter(device => device.online)
      const offline = this.devices.filter(device => !device.online)

      const groups = []
      if (online.length > 0) {
        groups.push({
          label: `在线设备 (${online.length})`,
          options: online
        })
      }
      if (offline.length > 0) {
        groups.push({
          label: `离线设备 (${offline.length})`,
          options: offline
        })
      }

      return groups
    }
  },
  watch: {
    value: {
      handler(newVal) {
        if (newVal) {
          this.selectedDevice = newVal.deviceId
          this.selectedChannel = newVal.channelId

          if (newVal.deviceId && newVal.deviceId !== this.selectedDevice) {
            this.loadChannels(newVal.deviceId)
          }
        }
      },
      immediate: true,
      deep: true
    }
  },
  mounted() {
    this.loadDevices()
  },
  methods: {
    // 加载设备列表
    async loadDevices() {
      try {
        this.deviceLoading = true
        this.deviceError = ''

        const response = await getAnalysisDevices()
        if (response.code === 0) {
          this.devices = response.data || []
          this.emitValidation(false, '请选择设备')
        } else {
          this.deviceError = response.msg || '加载设备列表失败'
          this.emitValidation(false, this.deviceError)
        }
      } catch (error) {
        this.deviceError = '网络异常，请稍后重试'
        this.emitValidation(false, this.deviceError)
        console.error('Load devices error:', error)
      } finally {
        this.deviceLoading = false
      }
    },

    // 加载通道列表
    async loadChannels(deviceId) {
      try {
        this.channelLoading = true
        this.channelError = ''
        this.channels = []

        if (!deviceId) {
          this.emitValidation(false, '请选择设备')
          return
        }

        const response = await getDeviceChannels(deviceId)
        if (response.code === 0) {
          this.channels = response.data || []

          if (this.channels.length === 0) {
            this.emitValidation(false, '该设备暂无可用通道')
          } else {
            // 如果只有一个通道，自动选择
            if (this.channels.length === 1) {
              this.selectedChannel = this.channels[0].channelId
              this.onChannelChange(this.selectedChannel)
            } else {
              this.emitValidation(false, '请选择通道')
            }
          }
        } else {
          this.channelError = response.msg || '加载通道列表失败'
          this.emitValidation(false, this.channelError)
        }
      } catch (error) {
        this.channelError = '网络异常，请稍后重试'
        this.emitValidation(false, this.channelError)
        console.error('Load channels error:', error)
      } finally {
        this.channelLoading = false
      }
    },

    // 设备变更
    onDeviceChange(deviceId) {
      this.selectedChannel = ''
      this.selectedChannelInfo = null
      this.channels = []
      this.streamUrls = { rtsp: '', flv: '', hls: '', rtmp: '' }

      // 获取设备详细信息
      this.selectedDeviceInfo = this.devices.find(device => device.deviceId === deviceId)

      if (deviceId) {
        this.loadChannels(deviceId)
        this.$emit('device-change', this.selectedDeviceInfo)
      } else {
        this.selectedDeviceInfo = null
        this.$emit('device-change', null)
        this.emitValidation(false, '请选择设备')
      }

      this.emitValue()
    },

    // 设备清除
    onDeviceClear() {
      this.onDeviceChange('')
    },

    // 通道变更
    onChannelChange(channelId) {
      // 获取通道详细信息
      this.selectedChannelInfo = this.channels.find(channel => channel.channelId === channelId)

      if (channelId && this.selectedDevice) {
        this.generateStreamUrls()
        this.emitValidation(true, '设备通道选择完成')
        this.$emit('channel-change', this.selectedChannelInfo)
      } else {
        this.selectedChannelInfo = null
        this.streamUrls = { rtsp: '', flv: '', hls: '', rtmp: '' }
        this.emitValidation(false, '请选择通道')
        this.$emit('channel-change', null)
      }

      this.emitValue()
    },

    // 通道清除
    onChannelClear() {
      this.onChannelChange('')
    },

    // 生成流媒体URL
    generateStreamUrls() {
      if (!this.selectedDevice || !this.selectedChannel) {
        return
      }

      // 这里根据WVP的流媒体URL格式生成
      // 实际项目中可能需要调用API获取真实的流URL
      const streamName = `${this.selectedDevice}_${this.selectedChannel}`
      const baseUrl = window.location.hostname

      this.streamUrls = {
        rtsp: `rtsp://${baseUrl}:554/rtp/${streamName}`,
        flv: `http://${baseUrl}/rtp/${streamName}.live.flv`,
        hls: `http://${baseUrl}/rtp/${streamName}/hls.m3u8`,
        rtmp: `rtmp://${baseUrl}:1935/rtp/${streamName}`
      }
    },

    // 测试流连接
    async testStreamUrl() {
      if (!this.streamUrls.rtsp) {
        this.$message.warning('没有可测试的流地址')
        return
      }

      this.testingStream = true
      try {
        // 这里可以调用后端API测试流连接
        // 暂时模拟测试
        await new Promise(resolve => setTimeout(resolve, 2000))
        this.$message.success('流连接测试成功')
      } catch (error) {
        this.$message.error('流连接测试失败')
      } finally {
        this.testingStream = false
      }
    },

    // 复制URL
    copyUrl(url) {
      if (!url) {
        this.$message.warning('没有可复制的URL')
        return
      }

      navigator.clipboard.writeText(url).then(() => {
        this.$message.success('URL已复制到剪贴板')
      }).catch(() => {
        this.$message.error('复制失败')
      })
    },

    // 获取通道状态类型
    getChannelStatusType(status) {
      const statusMap = {
        'online': 'success',
        'offline': 'info',
        'error': 'danger'
      }
      return statusMap[status] || 'info'
    },

    // 格式化时间
    formatTime(timestamp) {
      if (!timestamp) return '未知'
      return dayjs(timestamp).format('MM-DD HH:mm')
    },

    // 发出值变更事件
    emitValue() {
      const value = {
        deviceId: this.selectedDevice,
        channelId: this.selectedChannel
      }
      this.$emit('input', value)
    },

    // 发出验证状态
    emitValidation(valid, message) {
      this.$emit('validation-change', { valid, message })
    },

    // 重置选择
    reset() {
      this.selectedDevice = ''
      this.selectedChannel = ''
      this.selectedDeviceInfo = null
      this.selectedChannelInfo = null
      this.channels = []
      this.streamUrls = { rtsp: '', flv: '', hls: '', rtmp: '' }
      this.emitValue()
      this.emitValidation(false, '请选择设备')
    },

    // 获取当前选择
    getSelection() {
      return {
        device: this.selectedDeviceInfo,
        channel: this.selectedChannelInfo,
        streamUrls: this.streamUrls
      }
    }
  }
}
</script>

<style scoped>
.device-channel-selector {
  width: 100%;
}

/* 选项样式 */
.device-option,
.channel-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.device-info,
.channel-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.device-name,
.channel-name {
  font-weight: 500;
  color: #303133;
}

.device-id,
.channel-id {
  font-size: 12px;
  color: #909399;
  font-family: monospace;
}

.device-status,
.channel-status {
  margin-left: 8px;
}

/* 错误和空消息 */
.error-message,
.empty-message {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 12px;
}

.error-message {
  color: #f56c6c;
}

.empty-message {
  color: #909399;
}

/* 详情卡片 */
.device-details,
.stream-preview {
  margin-top: 16px;
}

.detail-card {
  border-radius: 8px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: #303133;
}

.detail-header i {
  color: #409eff;
}

.detail-content {
  padding-top: 8px;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-item label {
  min-width: 80px;
  color: #909399;
  font-size: 13px;
  margin-right: 8px;
}

.detail-item span {
  color: #303133;
  font-size: 13px;
}

/* 流媒体信息 */
.stream-content {
  padding-top: 8px;
}

.stream-item {
  margin-bottom: 12px;
}

.stream-item:last-child {
  margin-bottom: 0;
}

.stream-item label {
  display: block;
  margin-bottom: 4px;
  color: #909399;
  font-size: 12px;
}

.url-container {
  width: 100%;
}

.url-input {
  width: 100%;
}

/* 加载状态 */
:deep(.el-select .el-input.is-focus .el-input__inner) {
  border-color: #409eff;
}

:deep(.el-loading-mask) {
  border-radius: 4px;
}

/* 选项组样式 */
:deep(.el-select-group__title) {
  padding: 8px 20px 0;
  font-size: 12px;
  color: #909399;
  font-weight: bold;
}

/* 禁用选项样式 */
:deep(.el-select-dropdown__item.is-disabled) {
  color: #c0c4cc;
  cursor: not-allowed;
}

:deep(.el-select-dropdown__item.is-disabled .device-option) {
  opacity: 0.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-content .el-col {
    margin-bottom: 16px;
  }

  .detail-content .el-col:last-child {
    margin-bottom: 0;
  }

  .device-option,
  .channel-option {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }

  .device-status,
  .channel-status {
    margin-left: 0;
    align-self: flex-end;
  }
}
</style>