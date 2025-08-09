<template>
  <el-card class="card-item" shadow="hover" :class="{ 'card-disabled': !card.enabled }">
    <div class="card-header">
      <div class="card-icon">
        <i :class="getCardIcon()" />
      </div>
      <div class="card-title">{{ card.title }}</div>
      <div class="card-status">
        <el-tag :type="card.enabled ? 'success' : 'info'" size="mini">
          {{ card.enabled ? '启用' : '禁用' }}
        </el-tag>
      </div>
    </div>

    <div class="card-description">
      <p>{{ card.description || '暂无描述' }}</p>
    </div>

    <div class="card-meta">
      <div class="meta-item">
        <span class="meta-label">模型:</span>
        <span class="meta-value">{{ card.modelType || '--' }}</span>
      </div>
      <div class="meta-item">
        <span class="meta-label">创建人:</span>
        <span class="meta-value">{{ card.createdBy || '--' }}</span>
      </div>
      <div class="meta-item">
        <span class="meta-label">创建时间:</span>
        <span class="meta-value">{{ formatDate(card.createdAt) }}</span>
      </div>
    </div>

    <div class="card-tags" v-if="card.tags && card.tags.length > 0">
      <el-tag
        v-for="tag in card.tags.slice(0, 3)"
        :key="tag"
        type="info"
        size="mini"
        class="card-tag"
      >
        {{ tag }}
      </el-tag>
      <el-tag v-if="card.tags.length > 3" size="mini" type="info" class="card-tag">
        +{{ card.tags.length - 3 }}
      </el-tag>
    </div>

    <div class="card-actions">
      <el-button
        type="primary"
        size="mini"
        icon="el-icon-video-play"
        @click="handleUse"
        :disabled="!card.enabled"
      >
        使用
      </el-button>
      <el-button
        type="info"
        size="mini"
        icon="el-icon-edit"
        @click="handleEdit"
        v-if="canEdit"
      >
        编辑
      </el-button>
      <el-dropdown trigger="click" @command="handleCommand">
        <el-button size="mini" type="text" icon="el-icon-more">
          更多
        </el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item :command="`toggle-${card.id}`" :icon="card.enabled ? 'el-icon-close' : 'el-icon-check'">
            {{ card.enabled ? '禁用' : '启用' }}
          </el-dropdown-item>
          <el-dropdown-item :command="`delete-${card.id}`" icon="el-icon-delete" v-if="canDelete">
            删除
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </el-card>
</template>

<script>
export default {
  name: 'CardItem',
  props: {
    card: {
      type: Object,
      required: true
    }
  },
  computed: {
    canEdit() {
      // 这里可以根据用户权限判断是否可以编辑
      return this.$store.getters.roles.includes('admin')
    },
    canDelete() {
      // 这里可以根据用户权限判断是否可以删除
      return this.$store.getters.roles.includes('admin')
    }
  },
  methods: {
    handleUse() {
      this.$emit('use', this.card)
    },
    handleEdit() {
      this.$emit('edit', this.card)
    },
    handleCommand(command) {
      const [action, cardId] = command.split('-')
      if (action === 'toggle') {
        this.$emit('toggle', this.card)
      } else if (action === 'delete') {
        this.$emit('delete', this.card)
      }
    },
    getCardIcon() {
      // 根据卡片类型返回不同图标
      const typeIconMap = {
        'detection': 'el-icon-view',
        'recognition': 'el-icon-search',
        'tracking': 'el-icon-location',
        'analysis': 'el-icon-pie-chart'
      }
      return typeIconMap[this.card.analysisType] || 'el-icon-star-off'
    },
    formatDate(dateString) {
      if (!dateString) return '--'
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }
  }
}
</script>

<style scoped>
.card-item {
  margin-bottom: 20px;
  height: 280px;
  display: flex;
  flex-direction: column;
}

.card-item :deep(.el-card__body) {
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-disabled {
  opacity: 0.6;
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.card-icon {
  font-size: 24px;
  color: #409eff;
  margin-right: 8px;
}

.card-title {
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-status {
  margin-left: 8px;
}

.card-description {
  flex: 1;
  margin-bottom: 12px;
}

.card-description p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  margin-bottom: 12px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  font-size: 12px;
}

.meta-label {
  color: #909399;
}

.meta-value {
  color: #606266;
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-tags {
  margin-bottom: 12px;
  min-height: 20px;
}

.card-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.card-actions .el-button {
  margin: 0;
}

.card-actions .el-dropdown {
  margin-left: auto;
}
</style>