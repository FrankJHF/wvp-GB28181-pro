<template>
  <div class="device-channel-selector">
    <el-cascader
      v-model="selectedValue"
      :options="cascaderOptions"
      :props="cascaderProps"
      :show-all-levels="false"
      placeholder="请选择设备和通道"
      filterable
      clearable
      style="width: 100%"
      @change="handleChange"
      @expand-change="handleExpandChange"
    />
    <div v-if="selectedChannelInfo" class="channel-info">
      <div class="info-row">
        <span class="label">设备:</span> {{ selectedChannelInfo.deviceName }}
      </div>
      <div class="info-row">
        <span class="label">通道:</span> {{ selectedChannelInfo.channelName }}
      </div>
      <div class="info-row" v-if="selectedChannelInfo.online !== undefined">
        <span class="label">状态:</span> 
        <el-tag :type="selectedChannelInfo.online ? 'success' : 'danger'" size="mini">
          {{ selectedChannelInfo.online ? '在线' : '离线' }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script>
import { queryDevices, queryChannels } from '@/api/device'

export default {
  name: 'DeviceChannelSelector',
  props: {
    value: {
      type: [String, Number, Array],
      default: null
    }
  },
  data() {
    return {
      selectedValue: this.value,
      cascaderOptions: [],
      cascaderProps: {
        value: 'id',
        label: 'name',
        children: 'children',
        lazy: true,
        lazyLoad: this.loadChannels,
        expandTrigger: 'hover'
      },
      devicesMap: new Map(),
      channelsMap: new Map(),
      selectedChannelInfo: null
    }
  },
  watch: {
    value(val) {
      this.selectedValue = val
      this.updateSelectedChannelInfo()
    },
    selectedValue(val) {
      this.$emit('input', val)
    }
  },
  async created() {
    await this.loadDevices()
    if (this.value) {
      this.updateSelectedChannelInfo()
    }
  },
  methods: {
    async loadDevices() {
      try {
        const response = await queryDevices({
          page: 1,
          count: 1000,
          status: 'ON'
        })
        
        const devices = response.data?.list || []
        this.cascaderOptions = devices.map(device => {
          this.devicesMap.set(device.deviceId, device)
          return {
            id: device.deviceId,
            name: `${device.name || device.deviceId}`,
            deviceInfo: device,
            leaf: false
          }
        })
      } catch (error) {
        console.error('加载设备列表失败:', error)
        this.$message.error('加载设备列表失败')
      }
    },

    async loadChannels(node, resolve) {
      const deviceId = node.value
      
      try {
        const response = await queryChannels(deviceId, {
          page: 1,
          count: 1000,
          online: true,
          channelType: ''
        })

        const channels = response.data?.list || []
        const channelNodes = channels.map(channel => {
          const channelInfo = {
            ...channel,
            deviceId: deviceId,
            deviceName: node.data.name,
            rtspUrl: this.buildRtspUrl(deviceId, channel.channelId)
          }
          this.channelsMap.set(`${deviceId}-${channel.channelId}`, channelInfo)
          
          return {
            id: `${deviceId}-${channel.channelId}`,
            name: `${channel.name || channel.channelId} ${channel.online ? '(在线)' : '(离线)'}`,
            channelInfo: channelInfo,
            leaf: true
          }
        })

        resolve(channelNodes)
      } catch (error) {
        console.error(`加载设备 ${deviceId} 通道列表失败:`, error)
        resolve([])
      }
    },

    buildRtspUrl(deviceId, channelId) {
      // 根据WVP的RTSP地址格式构建
      const serverHost = window.location.hostname
      const rtspPort = '554' // 默认RTSP端口，实际项目中应该从配置获取
      return `rtsp://${serverHost}:${rtspPort}/rtp/${deviceId}/${channelId}`
    },

    handleChange(value) {
      this.selectedValue = value
      this.updateSelectedChannelInfo()
      this.$emit('change', this.selectedChannelInfo)
    },

    handleExpandChange(activeNames) {
      // 级联选择器展开时的处理逻辑
    },

    updateSelectedChannelInfo() {
      if (this.selectedValue && Array.isArray(this.selectedValue) && this.selectedValue.length === 2) {
        const channelKey = this.selectedValue[1]
        this.selectedChannelInfo = this.channelsMap.get(channelKey)
      } else if (typeof this.selectedValue === 'string' && this.selectedValue.includes('-')) {
        // 兼容直接传入 "deviceId-channelId" 格式的情况
        this.selectedChannelInfo = this.channelsMap.get(this.selectedValue)
      } else {
        this.selectedChannelInfo = null
      }
    },

    getSelectedChannelInfo() {
      return this.selectedChannelInfo
    },

    validate() {
      return this.selectedChannelInfo && this.selectedChannelInfo.online
    }
  }
}
</script>

<style scoped>
.device-channel-selector {
  width: 100%;
}

.channel-info {
  margin-top: 8px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.label {
  font-weight: 600;
  color: #606266;
  margin-right: 8px;
  min-width: 40px;
}
</style>