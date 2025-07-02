<template>
  <el-container class="app-layout-container">
    <el-header class="app-header">
      <div class="header-left">
        <el-button icon="el-icon-plus" size="mini" type="primary" @click="addAnalysisTask">
          新增任务
        </el-button>
        <el-button icon="el-icon-download" size="mini" @click="exportAnalysisReport">
          导出报告
        </el-button>
      </div>
      <div class="header-right">
        <el-button
          icon="el-icon-refresh-right"
          circle
          size="mini"
          :loading="refreshLoading"
          @click="refreshAnalysisData"
        />
      </div>
    </el-header>

    <el-container>
      <el-aside width="320px" class="app-aside">
        <FilterPanel
          ref="filterPanel"
          @filter-change="handleFilterChange"
          style="height: 50%;"
        />
        <StatisticsPanel
          ref="statisticsPanel"
          :statistics="realTimeStats"
          style="margin-top: 16px;"
        />
      </el-aside>

      <el-main class="app-main">
        <ResultGrid
          ref="resultGrid"
          :results="analysisResults"
          :loading="getAnalysisResultsLoading"
          :filter-params="currentFilter"
          @load-more="loadMoreAnalysisResults"
          @result-click="showAnalysisResultDetail"
          style="height: 100%;"
        />
      </el-main>
    </el-container>

    <TaskDialog
      ref="taskDialog"
      @task-created="handleTaskCreated"
      @task-updated="handleTaskUpdated"
    />

    <el-dialog
      title="分析结果详情"
      :visible.sync="resultDetailVisible"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-if="currentResult">
        <el-row :gutter="16">
          <el-col :span="12">
            <h4>基本信息</h4>
            <p><strong>设备ID:</strong> {{ currentResult.deviceId }}</p>
            <p><strong>通道ID:</strong> {{ currentResult.channelId }}</p>
            <p><strong>分析时间:</strong> {{ currentResult.resultTimestamp }}</p>
            <p><strong>分析问题:</strong> {{ currentResult.analysisQuestion }}</p>
          </el-col>
          <el-col :span="12">
            <h4>分析结果</h4>
            <el-tag
              :type="currentResult.isAlarm ? 'danger' : 'success'"
              size="medium"
              style="margin-bottom: 8px;"
            >
              {{ currentResult.isAlarm ? 'ALARM' : 'NORMAL' }}
            </el-tag>
            <p><strong>答案:</strong> {{ currentResult.analysisAnswer }}</p>
          </el-col>
        </el-row>
        <div v-if="currentResult.keyFrame" style="margin-top: 16px;">
          <h4>关键帧</h4>
          <el-image
            :src="currentResult.keyFrame"
            fit="contain"
            style="width: 100%; max-height: 300px;"
            :preview-src-list="[currentResult.keyFrame]"
          />
        </div>
      </div>
    </el-dialog>
  </el-container>
</template>

<script>
// ----- 组件引入按照项目规范 -----
import FilterPanel from './components/FilterPanel'
import StatisticsPanel from './components/StatisticsPanel'
import ResultGrid from './components/ResultGrid'
import TaskDialog from './components/TaskDialog'

export default {
  name: 'AnalysisIndex',
  components: {
    FilterPanel,
    StatisticsPanel,
    ResultGrid,
    TaskDialog
  },
  data() {
    return {
      // 页面数据
      analysisResults: [],
      realTimeStats: {
        todayAnalysis: 0,
        todayAlarms: 0,
        runningTasks: 0
      },
      currentFilter: {
        startDate: '',
        endDate: '',
        deviceId: '',
        channelId: '',
        keyword: '',
        onlyAlarm: false,
        page: 1,
        pageSize: 20
      },
      // 界面状态
      refreshLoading: false,
      getAnalysisResultsLoading: false,
      resultDetailVisible: false,
      currentResult: null,
      // 定时器
      updateLooper: null
    }
  },
  mounted() {
    this.initData()
    this.startDataUpdateLoop()
  },
  destroyed() {
    this.clearDataUpdateLoop()
  },
  methods: {
    // 初始化数据
    initData() {
      this.getAnalysisResults()
      this.getAnalysisStatistics()
    },
    // 加载分析结果
    getAnalysisResults(reset = true) {
      this.getAnalysisResultsLoading = true
      if (reset) {
        this.currentFilter.page = 1
        this.analysisResults = []
      }
      this.$store.dispatch('analysis/getAnalysisResults', this.currentFilter)
        .then(data => {
          if (reset) {
            this.analysisResults = data.list || []
          } else {
            this.analysisResults.push(...(data.list || []))
          }
        })
        .catch(error => {
          this.$message.error('加载分析结果失败')
          console.error('Load results error:', error)
        })
        .finally(() => {
          this.getAnalysisResultsLoading = false
        })
    },
    // 加载统计数据
    getAnalysisStatistics() {
      this.$store.dispatch('analysis/getStatistics')
        .then(data => {
          this.realTimeStats = data
        })
        .catch(error => {
          console.error('Load statistics error:', error)
        })
    },
    // 筛选条件变更
    handleFilterChange(filterParams) {
      this.currentFilter = { ...this.currentFilter, ...filterParams }
      this.getAnalysisResults(true)
    },
    // 加载更多结果
    loadMoreAnalysisResults() {
      this.currentFilter.page++
      this.getAnalysisResults(false)
    },
    // 显示结果详情
    showAnalysisResultDetail(result) {
      this.currentResult = result
      this.resultDetailVisible = true
    },
    // 显示任务对话框
    addAnalysisTask() {
      this.$refs.taskDialog.show()
    },
    // 任务创建成功
    handleTaskCreated() {
      this.$message.success('任务创建成功')
      this.getAnalysisStatistics()
    },
    // 任务更新成功
    handleTaskUpdated() {
      this.$message.success('任务更新成功')
      this.getAnalysisStatistics()
    },
    // 导出报告
    exportAnalysisReport() {
      this.$message.info('导出功能开发中...')
    },
    // 刷新数据
    refreshAnalysisData() {
      this.refreshLoading = true
      this.initData()
      this.$message.success('数据刷新成功')
      this.refreshLoading = false
    },
    // 启动数据更新循环
    startDataUpdateLoop() {
      this.updateLooper = setInterval(() => {
        if (this.$route.name === 'AnalysisIndex') {
          this.getAnalysisStatistics()
        }
      }, 30000) // 30秒刷新一次统计数据
    },
    // 清除定时器
    clearDataUpdateLoop() {
      if (this.updateLooper) {
        clearInterval(this.updateLooper)
        this.updateLooper = null
      }
    }
  }
}
</script>

<style lang="scss" scoped>
// 整体布局容器样式
.app-layout-container {
  height: 100vh; // 占满整个视口高度
  background-color: #f0f2f5; // 添加一个淡灰色背景，更像后台系统
}

// 顶部栏样式
.app-header {
  background-color: #fff;
  display: flex;
  justify-content: space-between; // 两端对齐
  align-items: center; // 垂直居中
  border-bottom: 1px solid #dcdfe6;
  padding: 0 20px;
}

// 左侧边栏样式
.app-aside {
  background-color: #fff;
  padding: 16px;
  border-right: 1px solid #dcdfe6;
  // 当内容超出时，允许滚动
  overflow-y: auto;
}

// 右侧主内容区样式
.app-main {
  padding: 16px;
  // 使用 flex 布局让内部元素占满高度
  display: flex;
  flex-direction: column;
}

// 响应式优化 (可选，但建议)
@media (max-width: 992px) {
  .app-aside {
    // 在较小的屏幕上，可以适当减小侧边栏宽度
    width: 280px !important;
  }
}

@media (max-width: 768px) {
  // 在手机端，可以将侧边栏隐藏，或者变为上下布局
  .app-layout-container {
    .el-container {
      flex-direction: column;
    }
    .app-aside {
      width: 100% !important;
      border-right: none;
      border-bottom: 1px solid #dcdfe6;
      margin-bottom: 16px;
      overflow-y: hidden; // 恢复默认滚动
    }
  }
}
</style>