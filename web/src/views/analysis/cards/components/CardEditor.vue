<template>
  <el-dialog
    :title="isEdit ? '编辑分析卡片' : '创建分析卡片'"
    :visible.sync="dialogVisible"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="cardForm"
      :model="cardForm"
      :rules="cardRules"
      label-width="100px"
      v-loading="loading"
    >
      <el-form-item label="卡片标题" prop="title">
        <el-input
          v-model="cardForm.title"
          placeholder="请输入卡片标题"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="卡片描述" prop="description">
        <el-input
          v-model="cardForm.description"
          type="textarea"
          :rows="3"
          placeholder="请输入卡片描述"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="分析类型" prop="analysisType">
        <el-select v-model="cardForm.analysisType" placeholder="请选择分析类型" style="width: 100%">
          <el-option label="目标检测" value="detection" />
          <el-option label="目标识别" value="recognition" />
          <el-option label="目标跟踪" value="tracking" />
          <el-option label="行为分析" value="analysis" />
        </el-select>
      </el-form-item>

      <el-form-item label="模型类型" prop="modelType">
        <el-select v-model="cardForm.modelType" placeholder="请选择模型类型" style="width: 100%">
          <el-option label="YOLO" value="yolo" />
          <el-option label="ResNet" value="resnet" />
          <el-option label="MobileNet" value="mobilenet" />
          <el-option label="自定义" value="custom" />
        </el-select>
      </el-form-item>

      <el-form-item label="图标类型" prop="iconType">
        <el-select v-model="cardForm.iconType" placeholder="请选择图标" style="width: 100%">
          <el-option label="视频" value="el-icon-video-play" />
          <el-option label="搜索" value="el-icon-search" />
          <el-option label="定位" value="el-icon-location" />
          <el-option label="图表" value="el-icon-pie-chart" />
          <el-option label="眼睛" value="el-icon-view" />
          <el-option label="相机" value="el-icon-camera" />
        </el-select>
      </el-form-item>

      <el-form-item label="标签">
        <el-tag
          v-for="(tag, index) in cardForm.tags"
          :key="index"
          closable
          @close="removeTag(index)"
          class="tag-item"
        >
          {{ tag }}
        </el-tag>
        <el-input
          v-if="tagInputVisible"
          ref="tagInput"
          v-model="tagInputValue"
          size="small"
          style="width: 80px"
          @keyup.enter.native="addTag"
          @blur="addTag"
        />
        <el-button
          v-else
          size="small"
          type="primary"
          plain
          @click="showTagInput"
          :disabled="cardForm.tags.length >= 5"
        >
          + 添加标签
        </el-button>
      </el-form-item>

      <el-form-item label="分析提示词" prop="prompt">
        <el-input
          v-model="cardForm.prompt"
          type="textarea"
          :rows="4"
          placeholder="请输入分析提示词，用于指导AI模型进行分析"
          maxlength="1000"
          show-word-limit
        />
        <div class="form-tip">
          提示词用于指导VLM模型进行视频分析，请详细描述需要分析的内容和要求
        </div>
      </el-form-item>

      <el-form-item label="分析配置">
        <el-input
          v-model="cardForm.analysisConfig"
          type="textarea"
          :rows="3"
          placeholder="请输入JSON格式的分析配置参数"
        />
        <div class="form-tip">
          JSON格式的配置参数，例如：{"confidence": 0.8, "max_objects": 10}
        </div>
      </el-form-item>

      <el-form-item label="启用状态">
        <el-switch
          v-model="cardForm.enabled"
          active-text="启用"
          inactive-text="禁用"
        />
      </el-form-item>
    </el-form>

    <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        {{ isEdit ? '更新' : '创建' }}
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
import { createCard, updateCard } from '@/api/analysis'

export default {
  name: 'CardEditor',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    card: {
      type: Object,
      default: null
    },
    isEdit: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: false,
      tagInputVisible: false,
      tagInputValue: '',
      cardForm: {
        title: '',
        description: '',
        analysisType: '',
        modelType: '',
        iconType: 'el-icon-video-play',
        tags: [],
        prompt: '',
        analysisConfig: '',
        enabled: true
      },
      cardRules: {
        title: [
          { required: true, message: '请输入卡片标题', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        description: [
          { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
        ],
        analysisType: [
          { required: true, message: '请选择分析类型', trigger: 'change' }
        ],
        modelType: [
          { required: true, message: '请选择模型类型', trigger: 'change' }
        ],
        prompt: [
          { required: true, message: '请输入分析提示词', trigger: 'blur' },
          { min: 10, max: 1000, message: '长度在 10 到 1000 个字符', trigger: 'blur' }
        ]
      }
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
    }
  },
  watch: {
    visible(val) {
      if (val) {
        this.initForm()
      }
    }
  },
  methods: {
    initForm() {
      if (this.isEdit && this.card) {
        this.cardForm = {
          ...this.card,
          tags: this.card.tags ? [...this.card.tags] : [],
          analysisConfig: this.card.analysisConfig || ''
        }
      } else {
        this.resetForm()
      }
    },
    resetForm() {
      this.cardForm = {
        title: '',
        description: '',
        analysisType: '',
        modelType: '',
        iconType: 'el-icon-video-play',
        tags: [],
        prompt: '',
        analysisConfig: '',
        enabled: true
      }
      if (this.$refs.cardForm) {
        this.$refs.cardForm.clearValidate()
      }
    },
    showTagInput() {
      this.tagInputVisible = true
      this.$nextTick(() => {
        this.$refs.tagInput.focus()
      })
    },
    addTag() {
      const tag = this.tagInputValue.trim()
      if (tag && !this.cardForm.tags.includes(tag) && this.cardForm.tags.length < 5) {
        this.cardForm.tags.push(tag)
      }
      this.tagInputVisible = false
      this.tagInputValue = ''
    },
    removeTag(index) {
      this.cardForm.tags.splice(index, 1)
    },
    handleSubmit() {
      this.$refs.cardForm.validate(valid => {
        if (valid) {
          this.loading = true
          
          // 验证JSON格式
          if (this.cardForm.analysisConfig) {
            try {
              JSON.parse(this.cardForm.analysisConfig)
            } catch (e) {
              this.$message.error('分析配置不是有效的JSON格式')
              this.loading = false
              return
            }
          }

          const submitData = { ...this.cardForm }
          
          const submitPromise = this.isEdit 
            ? updateCard(this.card.id, submitData)
            : createCard(submitData)

          submitPromise.then(() => {
            this.$message.success(this.isEdit ? '更新成功!' : '创建成功!')
            this.handleClose()
            this.$emit('success')
          }).catch(error => {
            this.$message.error(error.message || (this.isEdit ? '更新失败' : '创建失败'))
          }).finally(() => {
            this.loading = false
          })
        }
      })
    },
    handleClose() {
      this.dialogVisible = false
      this.resetForm()
    }
  }
}
</script>

<style scoped>
.tag-item {
  margin-right: 8px;
  margin-bottom: 8px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.dialog-footer {
  text-align: right;
}
</style>