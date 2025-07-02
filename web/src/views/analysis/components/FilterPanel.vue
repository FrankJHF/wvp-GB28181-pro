<template>
  <el-card class="filter-panel" shadow="never">
    <div slot="header" class="clearfix">
      <span class="panel-title">筛选条件</span>
      <el-button
        style="float: right; padding: 3px 0"
        type="text"
        @click="resetFilter"
      >
        重置
      </el-button>
    </div>

    <el-form ref="filterForm" :model="filterForm" label-position="top" size="small">
      <!-- 时间范围筛选 -->
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="yyyy-MM-dd HH:mm:ss"
          value-format="yyyy-MM-dd HH:mm:ss"
          style="width: 100%;"
          @change="onTimeRangeChange"
        />
      </el-form-item>

      <!-- 设备选择 -->
      <el-form-item label="选择设备">
        <el-select
          v-model="filterForm.deviceId"
          placeholder="请选择设备"
          style="width: 100%;"
          clearable
          filterable
          @change="onDeviceChange"
          @clear="onDeviceClear"
        >
          <el-option
            v-for="device in deviceList"
            :key="device.deviceId"
            :label="`${device.name} (${device.deviceId})`"
            :value="device.deviceId"
          >
            <span style="float: left">{{ device.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 12px">
              {{ device.online ? '在线' : '离线' }}
            </span>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 通道选择 -->
      <el-form-item label="选择通道">
        <el-select
          v-model="filterForm.channelId"
          placeholder="请选择通道"
          style="width: 100%;"
          clearable
          filterable
          :disabled="!filterForm.deviceId"
          @change="onChannelChange"
        >
          <el-option
            v-for="channel in channelList"
            :key="channel.channelId"
            :label="`${channel.name} (${channel.channelId})`"
            :value="channel.channelId"
          >
            <span style="float: left">{{ channel.name }}</span>
            <span style="float: right; color: #8492a6; font-size: 12px">
              {{ channel.status }}
            </span>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- 关键词搜索 -->
      <el-form-item label="关键词搜索">
        <el-input
          v-model="filterForm.keyword"
          placeholder="搜索分析内容..."
          clearable
          @input="onKeywordChange"
          @clear="onKeywordChange"
        >
          <i slot="prefix" class="el-input__icon el-icon-search" />
        </el-input>
      </el-form-item>

      <!-- 结果类型筛选 -->
      <el-form-item label="结果类型">
        <el-checkbox-group v-model="resultTypes" @change="onResultTypeChange">
          <el-checkbox label="normal">正常结果</el-checkbox>
          <el-checkbox label="alarm">告警结果</el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <!-- 仅显示告警开关 -->
      <el-form-item>
        <el-switch
          v-model="filterForm.onlyAlarm"
          active-text="仅显示告警"
          @change="onAlarmOnlyChange"
        />
      </el-form-item>

      <!-- 快速筛选按钮 -->
      <el-form-item label="快速筛选">
        <el-button-group style="width: 100%;">
          <el-button
            size="mini"
            @click="setTimeRange('today')"
            :type="currentQuickFilter === 'today' ? 'primary' : ''"
          >
            今天
          </el-button>
          <el-button
            size="mini"
            @click="setTimeRange('yesterday')"
            :type="currentQuickFilter === 'yesterday' ? 'primary' : ''"
          >
            昨天
          </el-button>
          <el-button
            size="mini"
            @click="setTimeRange('week')"
            :type="currentQuickFilter === 'week' ? 'primary' : ''"
          >
            本周
          </el-button>
        </el-button-group>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script>
import { getAnalysisDevices, getDeviceChannels } from '@/api/analysis'
import dayjs from 'dayjs'

export default {
  name: 'FilterPanel',
  data() {
    return {
      // 筛选表单数据
      filterForm: {
        deviceId: '',
        channelId: '',
        keyword: '',
        onlyAlarm: false,
        startDate: '',
        endDate: ''
      },

      // 时间范围
      timeRange: [],

      // 结果类型
      resultTypes: ['normal', 'alarm'],

      // 设备和通道数据
      deviceList: [],
      channelList: [],

      // 加载状态
      deviceLoading: false,
      channelLoading: false,

      // 当前快速筛选
      currentQuickFilter: '',

      // 防抖定时器
      debounceTimer: null
    }
  },
  mounted() {
    this.initData()
  },
  methods: {
    // 初始化数据
    async initData() {
      await this.loadDevices()
      // 默认设置今天的时间范围
      this.setTimeRange('today')
    },

    // 加载设备列表
    async loadDevices() {
      try {
        this.deviceLoading = true
        const response = await getAnalysisDevices()
        if (response.code === 0) {
          this.deviceList = response.data || []
        }
      } catch (error) {
        this.$message.error('加载设备列表失败')
        console.error('Load devices error:', error)
      } finally {
        this.deviceLoading = false
      }
    },

    // 加载通道列表
    async loadChannels(deviceId) {
      try {
        this.channelLoading = true
        this.channelList = []

        const response = await getDeviceChannels(deviceId)
        if (response.code === 0) {
          this.channelList = response.data || []
        }
      } catch (error) {
        this.$message.error('加载通道列表失败')
        console.error('Load channels error:', error)
      } finally {
        this.channelLoading = false
      }
    },

    // 设备变更事件
    async onDeviceChange(deviceId) {
      this.filterForm.channelId = ''
      this.channelList = []

      if (deviceId) {
        await this.loadChannels(deviceId)
      }

      this.emitFilterChange()
    },

    // 设备清除事件
    onDeviceClear() {
      this.filterForm.channelId = ''
      this.channelList = []
      this.emitFilterChange()
    },

    // 通道变更事件
    onChannelChange() {
      this.emitFilterChange()
    },

    // 时间范围变更事件
    onTimeRangeChange(timeRange) {
      if (timeRange && timeRange.length === 2) {
        this.filterForm.startDate = timeRange[0]
        this.filterForm.endDate = timeRange[1]
      } else {
        this.filterForm.startDate = ''
        this.filterForm.endDate = ''
      }
      this.currentQuickFilter = ''
      this.emitFilterChange()
    },

    // 关键词变更事件（防抖处理）
    onKeywordChange() {
      if (this.debounceTimer) {
        clearTimeout(this.debounceTimer)
      }

      this.debounceTimer = setTimeout(() => {
        this.emitFilterChange()
      }, 500)
    },

    // 结果类型变更事件
    onResultTypeChange() {
      // 根据选择的结果类型更新onlyAlarm
      if (this.resultTypes.length === 1 && this.resultTypes[0] === 'alarm') {
        this.filterForm.onlyAlarm = true
      } else {
        this.filterForm.onlyAlarm = false
      }
      this.emitFilterChange()
    },

    // 仅显示告警变更事件
    onAlarmOnlyChange(value) {
      if (value) {
        this.resultTypes = ['alarm']
      } else {
        this.resultTypes = ['normal', 'alarm']
      }
      this.emitFilterChange()
    },

    // 设置时间范围
    setTimeRange(type) {
      this.currentQuickFilter = type
      const now = dayjs()

      switch (type) {
        case 'today':
          this.timeRange = [
            now.startOf('day').format('YYYY-MM-DD HH:mm:ss'),
            now.endOf('day').format('YYYY-MM-DD HH:mm:ss')
          ]
          break
        case 'yesterday':
          const yesterday = now.subtract(1, 'day')
          this.timeRange = [
            yesterday.startOf('day').format('YYYY-MM-DD HH:mm:ss'),
            yesterday.endOf('day').format('YYYY-MM-DD HH:mm:ss')
          ]
          break
        case 'week':
          this.timeRange = [
            now.startOf('week').format('YYYY-MM-DD HH:mm:ss'),
            now.endOf('week').format('YYYY-MM-DD HH:mm:ss')
          ]
          break
      }

      this.onTimeRangeChange(this.timeRange)
    },

    // 重置筛选条件
    resetFilter() {
      this.filterForm = {
        deviceId: '',
        channelId: '',
        keyword: '',
        onlyAlarm: false,
        startDate: '',
        endDate: ''
      }
      this.timeRange = []
      this.resultTypes = ['normal', 'alarm']
      this.channelList = []
      this.currentQuickFilter = ''

      // 设置为今天
      this.$nextTick(() => {
        this.setTimeRange('today')
      })
    },

    // 发出筛选变更事件
    emitFilterChange() {
      const filterParams = {
        ...this.filterForm
      }

      this.$emit('filter-change', filterParams)
    },

    // 获取当前筛选参数
    getCurrentFilter() {
      return {
        ...this.filterForm
      }
    }
  }
}
</script>

<style scoped>
.filter-panel {
  height: 100%;
  border-radius: 8px;
}

.panel-title {
  font-weight: bold;
  color: #303133;
}

.el-form-item {
  margin-bottom: 16px;
}

.el-form-item:last-child {
  margin-bottom: 0;
}

.el-button-group {
  display: flex;
}

.el-button-group .el-button {
  flex: 1;
}

/* 设备选择项样式 */
.el-select-dropdown__item {
  padding: 8px 16px;
}

/* 加载状态 */
.el-loading-mask {
  border-radius: 4px;
}

/* 复选框组样式 */
.el-checkbox-group {
  display: flex;
  flex-direction: column;
}

.el-checkbox {
  margin-right: 0;
  margin-bottom: 8px;
}

.el-checkbox:last-child {
  margin-bottom: 0;
}
</style>