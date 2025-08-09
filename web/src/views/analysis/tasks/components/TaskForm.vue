<template>
  <el-dialog
    :title="isEdit ? '编辑分析任务' : '创建分析任务'"
    :visible.sync="dialogVisible"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="taskForm"
      :model="taskForm"
      :rules="taskRules"
      label-width="120px"
      v-loading="loading"
    >
      <el-form-item label="任务名称" prop="taskName">
        <el-input
          v-model="taskForm.taskName"
          placeholder="请输入任务名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="分析卡片" prop="analysisCardId">
        <el-select
          v-model="taskForm.analysisCardId"
          placeholder="请选择分析卡片"
          style="width: 100%"
          @change="handleCardChange"
          filterable
        >
          <el-option
            v-for="card in availableCards"
            :key="card.id"
            :label="card.title"
            :value="card.id"
            :disabled="!card.enabled"
          >
            <span style="float: left">{{ card.title }}</span>
            <span style="float: right; color: #8492a6; font-size: 12px">{{ card.modelType }}</span>
          </el-option>
        </el-select>
        <div v-if="selectedCard" class="card-preview">
          <div class="card-info">
            <span class="info-label">描述:</span> {{ selectedCard.description || '无' }}
          </div>
          <div class="card-info">
            <span class="info-label">类型:</span> {{ selectedCard.analysisType || '无' }}
          </div>
        </div>
      </el-form-item>

      <el-form-item label="设备通道" prop="deviceChannelId">
        <device-channel-selector
          v-model="taskForm.deviceChannelId"
          @change="handleChannelChange"
          ref="channelSelector"
        />
        <div v-if="rtspUrl" class="rtsp-info">
          <span class="info-label">RTSP地址:</span>
          <el-input v-model="rtspUrl" readonly size="mini" />
        </div>
      </el-form-item>

      <el-form-item label="任务描述">
        <el-input
          v-model="taskForm.description"
          type="textarea"
          :rows="3"
          placeholder="请输入任务描述"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="分析配置">
        <el-input
          v-model="taskForm.analysisConfig"
          type="textarea"
          :rows="4"
          placeholder="请输入JSON格式的分析配置，留空则使用卡片默认配置"
        />
        <div class="form-tip">
          JSON格式的配置参数，会覆盖卡片的默认配置。例如：{"confidence": 0.8, "interval": 1000}
        </div>
      </el-form-item>

      <el-form-item label="自动启动">
        <el-switch
          v-model="taskForm.autoStart"
          active-text="是"
          inactive-text="否"
        />
        <div class="form-tip">
          创建任务后是否自动启动分析
        </div>
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
import DeviceChannelSelector from './DeviceChannelSelector'
import { getAvailableCards, createTask, updateTask } from '@/api/analysis'

export default {
  name: 'TaskForm',
  components: { DeviceChannelSelector },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    task: {
      type: Object,
      default: null
    },
    isEdit: {
      type: Boolean,
      default: false
    },
    defaultCardId: {
      type: [String, Number],
      default: null
    }
  },
  data() {
    return {
      loading: false,
      availableCards: [],
      taskForm: {
        taskName: '',
        analysisCardId: null,
        deviceChannelId: null,
        description: '',
        analysisConfig: '',
        autoStart: false
      },
      taskRules: {
        taskName: [
          { required: true, message: '请输入任务名称', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        analysisCardId: [
          { required: true, message: '请选择分析卡片', trigger: 'change' }
        ],
        deviceChannelId: [
          { required: true, message: '请选择设备通道', trigger: 'change' }
        ]
      },
      rtspUrl: '',
      selectedCard: null
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
        this.loadAvailableCards()
      }
    }
  },
  methods: {
    async loadAvailableCards() {
      try {
        const response = await getAvailableCards({ enabled: true })
        this.availableCards = response.data.list || response.data
      } catch (error) {
        console.error('加载分析卡片失败:', error)
        this.$message.error('加载分析卡片失败')
      }
    },
    initForm() {
      if (this.isEdit && this.task) {
        this.taskForm = {
          ...this.task,
          analysisConfig: this.task.analysisConfig || '',
          autoStart: false
        }
        this.handleCardChange(this.taskForm.analysisCardId)
        if (this.taskForm.deviceChannelId) {
          this.handleChannelChange(this.taskForm.deviceChannelId)
        }
      } else {
        this.resetForm()
        if (this.defaultCardId) {
          this.taskForm.analysisCardId = this.defaultCardId
          this.$nextTick(() => {
            this.handleCardChange(this.defaultCardId)
          })
        }
      }
    },
    resetForm() {
      this.taskForm = {
        taskName: '',
        analysisCardId: null,
        deviceChannelId: null,
        description: '',
        analysisConfig: '',
        autoStart: false
      }
      this.rtspUrl = ''
      this.selectedCard = null
      if (this.$refs.taskForm) {
        this.$refs.taskForm.clearValidate()
      }
    },
    handleCardChange(cardId) {
      this.selectedCard = this.availableCards.find(card => card.id === cardId)
      if (this.selectedCard && this.selectedCard.analysisConfig) {
        try {
          this.taskForm.analysisConfig = JSON.stringify(JSON.parse(this.selectedCard.analysisConfig), null, 2)
        } catch (e) {
          this.taskForm.analysisConfig = this.selectedCard.analysisConfig
        }
      }
    },
    handleChannelChange(channelInfo) {
      if (channelInfo && channelInfo.rtspUrl) {
        this.rtspUrl = channelInfo.rtspUrl
      } else {
        this.rtspUrl = ''
      }
    },
    handleSubmit() {
      this.$refs.taskForm.validate(async (valid) => {
        if (valid) {
          this.loading = true
          
          try {
            if (this.taskForm.analysisConfig) {
              JSON.parse(this.taskForm.analysisConfig)
            }
          } catch (e) {
            this.$message.error('分析配置不是有效的JSON格式')
            this.loading = false
            return
          }

          const channelInfo = this.$refs.channelSelector.getSelectedChannelInfo()
          if (!channelInfo) {
            this.$message.error('请选择有效的设备通道')
            this.loading = false
            return
          }

          const submitData = {
            ...this.taskForm,
            deviceId: channelInfo.deviceId,
            channelId: channelInfo.channelId,
            deviceName: channelInfo.deviceName,
            channelName: channelInfo.channelName,
            rtspUrl: channelInfo.rtspUrl
          }
          
          try {
            if (this.isEdit) {
              await updateTask(this.task.id, submitData)
              this.$message.success('更新成功!')
            } else {
              await createTask(submitData)
              this.$message.success('创建成功!')
            }
            this.handleClose()
            this.$emit('success')
          } catch (error) {
            this.$message.error(error.message || (this.isEdit ? '更新失败' : '创建失败'))
          } finally {
            this.loading = false
          }
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
.card-preview {
  margin-top: 8px;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
}

.card-info {
  margin-bottom: 4px;
}

.card-info:last-child {
  margin-bottom: 0;
}

.info-label {
  font-weight: 600;
  color: #606266;
}

.rtsp-info {
  margin-top: 8px;
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