<template>
  <el-dialog
    :title="isEdit ? '编辑分析任务' : '创建分析任务'"
    :visible.sync="dialogVisible"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="taskForm"
      :model="taskForm"
      :rules="taskRules"
      label-width="100px"
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
        <div v-if="selectedCard" class="form-tip">
          描述: {{ selectedCard.description || '无' }} | 类型: {{ selectedCard.analysisType || '无' }}
        </div>
      </el-form-item>

      <el-form-item label="设备通道" prop="deviceChannelId">
        <device-channel-selector
          v-model="taskForm.deviceChannelId"
          @change="handleChannelChange"
          ref="channelSelector"
        />
        <div v-if="rtspUrl" class="form-tip">
          RTSP地址: {{ rtspUrl }}
        </div>
      </el-form-item>

      <el-form-item label="推理间隔(秒)" prop="analysisConfig.inferenceInterval">
        <el-input-number
          v-model="taskForm.analysisConfig.inferenceInterval"
          :min="1"
          :max="300"
          placeholder="30"
          style="width: 100%"
        />
        <div class="form-tip">VLM模型推理间隔时间</div>
      </el-form-item>

      <el-form-item label="帧缓冲大小" prop="analysisConfig.frameBufferSize">
        <el-input-number
          v-model="taskForm.analysisConfig.frameBufferSize"
          :min="1"
          :max="100"
          placeholder="30"
          style="width: 100%"
        />
        <div class="form-tip">视频帧缓冲区大小</div>
      </el-form-item>

      <el-form-item label="采样帧率(fps)" prop="analysisConfig.samplingFps">
        <el-input-number
          v-model="taskForm.analysisConfig.samplingFps"
          :min="0.1"
          :max="30"
          :step="0.1"
          placeholder="1"
          style="width: 100%"
        />
        <div class="form-tip">视频采样帧率</div>
      </el-form-item>

      <el-form-item label="最大输出长度" prop="analysisConfig.maxNewTokens">
        <el-input-number
          v-model="taskForm.analysisConfig.maxNewTokens"
          :min="50"
          :max="1000"
          placeholder="200"
          style="width: 100%"
        />
        <div class="form-tip">AI模型最大输出token数</div>
      </el-form-item>

      <el-form-item label="自动启动">
        <el-switch
          v-model="taskForm.autoStart"
          active-text="启用"
          inactive-text="禁用"
        />
        <div class="form-tip">创建任务后是否自动启动分析</div>
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
        analysisConfig: {
          inferenceInterval: 30,
          frameBufferSize: 30,
          samplingFps: 1,
          maxNewTokens: 200
        },
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
          analysisConfig: {
            inferenceInterval: (this.task.analysisConfig && this.task.analysisConfig.inference_interval) || 30,
            frameBufferSize: (this.task.analysisConfig && this.task.analysisConfig.frame_buffer_size) || 30,
            samplingFps: (this.task.analysisConfig && this.task.analysisConfig.sampling_fps) || 1,
            maxNewTokens: (this.task.analysisConfig && this.task.analysisConfig.max_new_tokens) || 200
          },
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
        analysisConfig: {
          inferenceInterval: 30,
          frameBufferSize: 30,
          samplingFps: 1,
          maxNewTokens: 200
        },
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
    },
    handleChannelChange(channelInfo) {
      // 通道变更时的处理逻辑，显示RTSP地址
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

          const channelInfo = this.$refs.channelSelector.getSelectedChannelInfo()
          if (!channelInfo) {
            this.$message.error('请选择有效的设备通道')
            this.loading = false
            return
          }

          const submitData = {
            task_name: this.taskForm.taskName,
            analysis_card_id: this.taskForm.analysisCardId,
            description: this.taskForm.description,
            device_id: channelInfo.deviceId,
            channel_id: channelInfo.channelId,
            device_name: channelInfo.deviceName,
            channel_name: channelInfo.channelName,
            rtsp_url: channelInfo.rtspUrl,
            auto_start: this.taskForm.autoStart,
            analysis_config: {
              inference_interval: this.taskForm.analysisConfig.inferenceInterval,
              frame_buffer_size: this.taskForm.analysisConfig.frameBufferSize,
              sampling_fps: this.taskForm.analysisConfig.samplingFps,
              max_new_tokens: this.taskForm.analysisConfig.maxNewTokens
            }
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
.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.json-example {
  background-color: #f4f4f5;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 4px 6px;
  font-family: Consolas, Monaco, 'Andale Mono', 'Ubuntu Mono', monospace;
  font-size: 11px;
  color: #606266;
  display: inline-block;
  margin-top: 4px;
  word-break: break-all;
}

.dialog-footer {
  text-align: right;
}
</style>