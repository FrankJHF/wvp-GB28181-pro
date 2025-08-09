<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.title"
        placeholder="卡片标题"
        style="width: 200px;"
        class="filter-item"
        @keyup.enter.native="handleFilter"
      />
      <el-select
        v-model="listQuery.enabled"
        placeholder="状态"
        clearable
        class="filter-item"
        style="width: 130px"
      >
        <el-option label="启用" :value="true" />
        <el-option label="禁用" :value="false" />
      </el-select>
      <el-button v-waves class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">
        搜索
      </el-button>
      <el-button class="filter-item" style="margin-left: 10px;" type="primary" icon="el-icon-plus" @click="handleCreate">
        新建卡片
      </el-button>
    </div>

    <el-row :gutter="20" v-loading="loading">
      <el-col v-for="card in list" :key="card.id" :xs="24" :sm="12" :md="8" :lg="6">
        <card-item 
          :card="card" 
          @use="handleUse"
          @edit="handleEdit"
          @delete="handleDelete"
          @toggle="handleToggle"
        />
      </el-col>
    </el-row>

    <el-pagination
      v-show="total>0"
      :current-page="listQuery.page"
      :page-sizes="[10, 20, 30, 50]"
      :page-size="listQuery.limit"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      style="text-align: right; margin-top: 20px;"
    />

    <!-- 编辑对话框 -->
    <card-editor
      :visible.sync="dialogVisible"
      :card="currentCard"
      :is-edit="isEdit"
      @success="getList"
    />
  </div>
</template>

<script>
import waves from '@/directive/waves'
import CardItem from './components/CardItem'
import CardEditor from './components/CardEditor'
import { getCards, deleteCard, toggleCard } from '@/api/analysis'

export default {
  name: 'AnalysisCards',
  components: { CardItem, CardEditor },
  directives: { waves },
  data() {
    return {
      list: [],
      total: 0,
      loading: false,
      listQuery: {
        page: 1,
        limit: 20,
        title: '',
        enabled: null,
        modelType: '',
        createdBy: ''
      },
      dialogVisible: false,
      isEdit: false,
      currentCard: null
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      getCards(this.listQuery).then(response => {
        this.list = response.data.list || response.data
        this.total = response.data.total || (response.data.list ? response.data.list.length : 0)
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    handleSizeChange(val) {
      this.listQuery.limit = val
      this.getList()
    },
    handleCurrentChange(val) {
      this.listQuery.page = val
      this.getList()
    },
    handleCreate() {
      this.currentCard = null
      this.isEdit = false
      this.dialogVisible = true
    },
    handleUse(card) {
      this.$router.push({
        name: 'AnalysisTasks',
        query: { cardId: card.id }
      })
    },
    handleEdit(card) {
      this.currentCard = card
      this.isEdit = true
      this.dialogVisible = true
    },
    handleDelete(card) {
      this.$confirm(`确认删除分析卡片"${card.title}"？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return deleteCard(card.id)
      }).then(() => {
        this.$message({
          type: 'success',
          message: '删除成功!'
        })
        this.getList()
      }).catch(() => {})
    },
    handleToggle(card) {
      const action = card.enabled ? '禁用' : '启用'
      this.$confirm(`确认${action}分析卡片"${card.title}"？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        return toggleCard(card.id, !card.enabled)
      }).then(() => {
        this.$message({
          type: 'success',
          message: `${action}成功!`
        })
        this.getList()
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.filter-container {
  margin-bottom: 20px;
}
.filter-item {
  margin-right: 10px;
}
</style>