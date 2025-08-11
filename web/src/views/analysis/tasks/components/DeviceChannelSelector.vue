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
      <div class="info-row" v-if="selectedChannelInfo.status !== undefined">
        <span class="label">状态:</span>
        <el-tag :type="selectedChannelInfo.status === 'ON' ? 'success' : 'danger'" size="mini">
          {{ selectedChannelInfo.status === 'ON' ? '在线' : '离线' }}
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

        console.log('设备查询响应:', response)
        const devices = response.data?.list || []
        console.log('设备数据:', devices.map(d => ({ deviceId: d.deviceId, name: d.name })))
        this.cascaderOptions = devices.map(device => {
          this.devicesMap.set(device.deviceId, device)
          const option = {
            id: device.deviceId,
            name: `${device.name || device.deviceId}`,
            deviceInfo: device,
            leaf: false
          }
          console.log('创建设备选项:', option)
          return option
        })
      } catch (error) {
        console.error('加载设备列表失败:', error)
        this.$message.error('加载设备列表失败')
      }
    },

    async loadChannels(node, resolve) {
      // 根节点(level 0)或叶子节点不进行懒加载
      if (node.level === 0 || node.leaf) {
        resolve([])
        return
      }

      // 获取原始设备ID - node.value在el-cascader中对应配置的value字段
      let deviceId = node.value
      
      // 确保传递给API的是纯设备ID，不包含通道ID
      if (typeof deviceId === 'string' && deviceId.includes('-')) {
        // 如果ID包含连字符，取第一部分作为设备ID
        deviceId = deviceId.split('-')[0]
      }
      
      console.log('loadChannels - 节点信息:', {
        level: node.level,
        value: node.value,
        label: node.label,
        leaf: node.leaf,
        nodeType: typeof node.value,
        parent: node.parent?.value
      })

      // 验证设备ID格式 - GB28181设备ID通常为20位数字，但也允许其他格式
      if (!deviceId || typeof deviceId !== 'string' || deviceId.trim() === '') {
        console.error('无效的设备ID:', deviceId)
        resolve([])
        return
      }

      // 放宽ID格式验证，允许更灵活的设备ID格式
      if (!/^[A-Za-z0-9]{10,30}$/.test(deviceId)) {
        console.error('设备ID格式不符合预期:', deviceId, '应为10-30位字母数字组合')
        resolve([])
        return
      }

      try {
        const response = await queryChannels(deviceId, {
          page: 1,
          count: 1000,
          online: true,
          channelType: ''
        })

        const channels = response.data?.list || []
        const channelNodes = channels.map(channel => {
          // 使用node.label获取设备名，这是el-cascader的标准属性
          const deviceName = node.label || this.devicesMap.get(deviceId)?.name || deviceId
          const channelInfo = {
            ...channel,
            deviceId: deviceId, // 父设备ID
            channelId: channel.deviceId, // DeviceChannel中的deviceId字段实际上是通道ID
            channelName: channel.name || channel.deviceId,
            deviceName: deviceName,
            rtspUrl: this.buildRtspUrl(deviceId, channel.deviceId)
          }
          this.channelsMap.set(`${deviceId}-${channel.deviceId}`, channelInfo)

          return {
            id: `${deviceId}-${channel.deviceId}`,
            name: `${channel.name || channel.deviceId} ${channel.status === 'ON' ? '(在线)' : '(离线)'}`,
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
      const rtspPort = '554' // 默认RTSP端口
      return `rtsp://${serverHost}:${rtspPort}/rtp/${deviceId}_${channelId}?originTypeStr=rtp_push`
    },

    handleChange(value) {
      this.selectedValue = value
      this.updateSelectedChannelInfo()
      this.$emit('change', this.selectedChannelInfo)
    },

    handleExpandChange() {
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
      return this.selectedChannelInfo && this.selectedChannelInfo.status === 'ON'
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