<template>
  <div class="result-grid-container">
    <!-- 结果统计和排序 -->
    <div class="grid-header">
      <div class="result-summary">
        <span class="total-count">共 {{ totalResults }} 条结果</span>
        <el-tag
          v-if="alarmCount > 0"
          type="danger"
          size="mini"
          style="margin-left: 8px;"
        >
          {{ alarmCount }} 条告警
        </el-tag>
      </div>

      <div class="grid-controls">
        <el-select
          v-model="sortBy"
          size="mini"
          style="width: 120px;"
          @change="onSortChange"
        >
          <el-option label="时间降序" value="time_desc" />
          <el-option label="时间升序" value="time_asc" />
          <el-option label="告警优先" value="alarm_first" />
        </el-select>

        <el-button-group style="margin-left: 8px;">
          <el-button
            :type="viewMode === 'grid' ? 'primary' : ''"
            size="mini"
            icon="el-icon-menu"
            @click="setViewMode('grid')"
          />
          <el-button
            :type="viewMode === 'list' ? 'primary' : ''"
            size="mini"
            icon="el-icon-s-order"
            @click="setViewMode('list')"
          />
        </el-button-group>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading && results.length === 0" class="loading-container">
      <el-empty description="正在加载分析结果...">
        <i class="el-icon-loading" style="font-size: 48px; color: #409EFF;" />
      </el-empty>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading && results.length === 0" class="empty-container">
      <el-empty description="暂无分析结果">
        <el-button type="primary" @click="$emit('refresh-requested')">刷新数据</el-button>
      </el-empty>
    </div>

    <!-- 结果展示区域 -->
    <div v-else class="results-container" :class="{ 'list-view': viewMode === 'list' }">
      <!-- 网格视图 -->
      <div v-if="viewMode === 'grid'" class="grid-view">
        <div class="result-grid">
          <ResultCard
            v-for="result in sortedResults"
            :key="`${result.id}-${result.resultTimestamp}`"
            :result="result"
            @click="onResultClick(result)"
            @action="onCardAction"
            class="result-card-item"
          />
        </div>
      </div>

      <!-- 列表视图 -->
      <div v-else class="list-view">
        <el-table
          :data="sortedResults"
          style="width: 100%"
          size="small"
          @row-click="onResultClick"
          row-class-name="result-row"
        >
          <el-table-column width="60">
            <template v-slot:default="scope">
              <el-tag
                :type="scope.row.isAlarm ? 'danger' : 'success'"
                size="mini"
              >
                {{ scope.row.isAlarm ? 'ALARM' : 'OK' }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="deviceId" label="设备ID" width="160" />
          <el-table-column prop="channelId" label="通道ID" width="160" />
          <el-table-column prop="resultTimestamp" label="分析时间" width="160" />

          <el-table-column label="分析结果" min-width="200">
            <template v-slot:default="scope">
              <div class="result-content">
                <div class="question">{{ scope.row.analysisQuestion }}</div>
                <div class="answer">{{ scope.row.analysisAnswer }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="120">
            <template v-slot:default="scope">
              <el-button
                type="text"
                size="mini"
                @click.stop="onResultClick(scope.row)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore" class="load-more-container">
      <el-button
        v-if="!autoLoading"
        type="text"
        @click="loadMore"
        :loading="loadingMore"
        style="width: 100%;"
      >
        加载更多
      </el-button>

      <!-- 自动加载指示器 -->
      <div
        v-if="autoLoading && loadingMore"
        class="auto-loading"
        ref="loadTrigger"
      >
        <i class="el-icon-loading" /> 正在加载更多...
      </div>
    </div>

    <!-- 没有更多数据提示 -->
    <div v-if="!hasMore && results.length > 0" class="no-more-container">
      <el-divider>
        <span style="color: #909399; font-size: 12px;">没有更多数据了</span>
      </el-divider>
    </div>
  </div>
</template>

<script>
import ResultCard from './ResultCard'

export default {
  name: 'ResultGrid',
  components: {
    ResultCard
  },
  props: {
    results: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    },
    filterParams: {
      type: Object,
      default: () => ({})
    },
    hasMore: {
      type: Boolean,
      default: true
    },
    autoLoading: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      viewMode: 'grid', // 'grid' 或 'list'
      sortBy: 'time_desc',
      loadingMore: false,
      observer: null
    }
  },
  computed: {
    // 排序后的结果
    sortedResults() {
      const results = [...this.results]

      switch (this.sortBy) {
        case 'time_desc':
          return results.sort((a, b) => new Date(b.resultTimestamp) - new Date(a.resultTimestamp))
        case 'time_asc':
          return results.sort((a, b) => new Date(a.resultTimestamp) - new Date(b.resultTimestamp))
        case 'alarm_first':
          return results.sort((a, b) => {
            if (a.isAlarm && !b.isAlarm) return -1
            if (!a.isAlarm && b.isAlarm) return 1
            return new Date(b.resultTimestamp) - new Date(a.resultTimestamp)
          })
        default:
          return results
      }
    },

    // 总结果数
    totalResults() {
      return this.results.length
    },

    // 告警数量
    alarmCount() {
      return this.results.filter(result => result.isAlarm).length
    }
  },
  mounted() {
    if (this.autoLoading) {
      this.setupIntersectionObserver()
    }
  },
  beforeDestroy() {
    this.destroyIntersectionObserver()
  },
  methods: {
    // 设置视图模式
    setViewMode(mode) {
      this.viewMode = mode
      this.$emit('view-mode-change', mode)
    },

    // 排序变更
    onSortChange() {
      this.$emit('sort-change', this.sortBy)
    },

    // 结果点击事件
    onResultClick(result) {
      this.$emit('result-click', result)
    },

    // 卡片操作事件
    onCardAction(action) {
      this.$emit('card-action', action)
    },

    // 加载更多
    async loadMore() {
      if (this.loadingMore || !this.hasMore) return

      this.loadingMore = true
      try {
        await this.$emit('load-more')
      } finally {
        this.loadingMore = false
      }
    },

    // 设置无限滚动观察器
    setupIntersectionObserver() {
      this.$nextTick(() => {
        const target = this.$refs.loadTrigger
        if (!target) return

        this.observer = new IntersectionObserver(
          (entries) => {
            entries.forEach(entry => {
              if (entry.isIntersecting && this.hasMore && !this.loadingMore) {
                this.loadMore()
              }
            })
          },
          {
            rootMargin: '100px'
          }
        )

        this.observer.observe(target)
      })
    },

    // 销毁观察器
    destroyIntersectionObserver() {
      if (this.observer) {
        this.observer.disconnect()
        this.observer = null
      }
    },

    // 滚动到顶部
    scrollToTop() {
      const container = this.$refs.resultsContainer
      if (container) {
        container.scrollTop = 0
      }
    },

    // 刷新数据
    refresh() {
      this.$emit('refresh-requested')
    }
  },
  watch: {
    // 监听autoLoading变化
    autoLoading: {
      handler(newVal) {
        if (newVal) {
          this.setupIntersectionObserver()
        } else {
          this.destroyIntersectionObserver()
        }
      },
      immediate: true
    }
  }
}
</script>

<style scoped>
.result-grid-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 头部样式 */
.grid-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.result-summary {
  display: flex;
  align-items: center;
}

.total-count {
  font-size: 14px;
  color: #606266;
}

.grid-controls {
  display: flex;
  align-items: center;
}

/* 结果容器样式 */
.results-container {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
}

/* 网格视图样式 */
.grid-view {
  height: 100%;
}

.result-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  padding: 0 4px;
}

.result-card-item {
  height: fit-content;
}

/* 列表视图样式 */
.list-view .el-table {
  border-radius: 8px;
  overflow: hidden;
}

.result-content .question {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-content .answer {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 加载状态样式 */
.loading-container,
.empty-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.load-more-container {
  padding: 16px;
  text-align: center;
  border-top: 1px solid #ebeef5;
  margin-top: 16px;
}

.auto-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #909399;
  font-size: 14px;
}

.auto-loading i {
  margin-right: 8px;
  animation: rotating 2s linear infinite;
}

.no-more-container {
  margin-top: 16px;
}

/* 动画效果 */
@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 表格行样式 */
:deep(.result-row) {
  cursor: pointer;
  transition: background-color 0.3s ease;
}

:deep(.result-row:hover) {
  background-color: #f5f7fa;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .result-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .grid-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .grid-controls {
    align-self: flex-end;
  }
}

@media (max-width: 480px) {
  .results-container {
    padding-right: 0;
  }

  .result-grid {
    gap: 8px;
    padding: 0;
  }
}

/* 滚动条样式 */
.results-container::-webkit-scrollbar {
  width: 6px;
}

.results-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.results-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.results-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>