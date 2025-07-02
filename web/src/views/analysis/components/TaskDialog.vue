<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="dialogVisible"
    width="800px"
    :close-on-click-modal="false"
    :before-close="handleClose"
    class="task-dialog"
  >
    <el-form
      ref="taskForm"
      :model="taskForm"
      :rules="formRules"
      label-width="100px"
      size="small"
    >
      <!-- 任务基本信息 -->
      <el-card class="form-section" shadow="never">
        <div slot="header" class="section-header">
          <i class="el-icon-info" />
          <span>基本信息</span>
        </div>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="任务名称" prop="taskName">
              <el-input
                v-model="taskForm.taskName"
                placeholder="请输入任务名称"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分析间隔" prop="analysisInterval">
              <el-input-number
                v-model="taskForm.analysisInterval"
                :min="10"
                :max="3600"
                :step="10"
                placeholder="秒"
                style="width: 100%;"
                controls-position="right"
              />
              <span class="form-help">建议30-300秒</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="任务描述">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :rows="2"
            placeholder="可选，描述任务用途"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-card>

      <!-- 设备通道选择 -->
      <el-card class="form-section" shadow="never">
        <div slot="header" class="section-header">
          <i class="el-icon-video-camera" />
          <span>设备通道</span>
        </div>

        <DeviceChannelSelector
          ref="deviceChannelSelector"
          v-model="deviceChannelSelection"
          @device-change="onDeviceChange"
          @channel-change="onChannelChange"
          @validation-change="onDeviceValidationChange"
        />

        <!-- 设备状态信息 -->
        <div v-if="selectedDeviceInfo" class="device-status">
          <el-alert
            :title="deviceStatusMessage"
            :type="selectedDeviceInfo.online ? 'success' : 'warning'"
            :closable="false"
            show-icon
          />
        </div>
      </el-card>

      <!-- 分析问题配置 -->
      <el-card class="form-section" shadow="never">
        <div slot="header" class="section-header">
          <i class="el-icon-chat-dot-round" />
          <span>分析配置</span>
        </div>

        <!-- 问题模板选择 -->
        <el-form-item label="快速模板">
          <el-select
            v-model="selectedTemplate"
            placeholder="选择预设问题模板"
            @change="onTemplateChange"
            style="width: 100%;"
            clearable
          >
            <el-option
              v-for="template in questionTemplates"
              :key="template.name"
              :label="template.name"
              :value="template.question"
            >
              <span style="float: left">{{ template.name }}</span>
              <span style="float: right; color: #8492a6; font-size: 12px">
                {{ template.question.substring(0, 20) }}...
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 分析问题 -->
        <el-form-item label="分析问题" prop="analysisQuestion">
          <el-input
            v-model="taskForm.analysisQuestion"
            type="textarea"
            :rows="3"
            placeholder="请描述需要分析的问题，例如：画面中是否有人员？请描述人员数量和行为"
            maxlength="500"
            show-word-limit
          />
          <div class="form-help">
            <i class="el-icon-info" />
            问题描述要具体清晰，有助于提高分析准确性
          </div>
        </el-form-item>

        <!-- 告警关键词 -->
        <el-form-item label="告警关键词">
          <el-input
            v-model="taskForm.alarmKeywords"
            placeholder="多个关键词用逗号分隔，如：异常,危险,告警"
            clearable
          />
          <div class="form-help">
            <i class="el-icon-info" />
            当分析结果包含这些关键词时，将标记为告警
          </div>
        </el-form-item>
      </el-card>

      <!-- 高级设置 -->
      <el-card class="form-section" shadow="never">
        <div slot="header" class="section-header">
          <i class="el-icon-setting" />
          <span>高级设置</span>
          <el-switch
            v-model="showAdvanced"
            style="float: right;"
            size="mini"
          />
        </div>

        <div v-show="showAdvanced">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="启用状态">
                <el-switch
                  v-model="taskForm.enabled"
                  active-text="启用"
                  inactive-text="停用"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="自动启动">
                <el-switch
                  v-model="taskForm.autoStart"
                  active-text="创建后自动启动"
                  inactive-text="手动启动"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="最大重试">
                <el-input-number
                  v-model="taskForm.maxRetries"
                  :min="0"
                  :max="10"
                  style="width: 100%;"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="超时时间">
                <el-input-number
                  v-model="taskForm.timeout"
                  :min="10"
                  :max="300"
                  :step="5"
                  style="width: 100%;"
                  controls-position="right"
                />
                <span class="form-help">秒</span>
              </el-form-item>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </el-form>

    <!-- 对话框底部 -->
    <div slot="footer" class="dialog-footer">
      <div class="footer-left">
        <el-button
          v-if="isEdit"
          type="danger"
          size="small"
          @click="deleteTask"
          :loading="deleteLoading"
        >
          删除任务
        </el-button>
      </div>

      <div class="footer-right">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          @click="validateAndSubmit"
          :loading="submitLoading"
        >
          {{ isEdit ? '更新任务' : '创建任务' }}
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import { QUESTION_TEMPLATES } from '@/api/analysis'
import {
  createAnalysisTask,
  updateAnalysisTask,
  deleteAnalysisTask,
  validateTaskConfig
} from '@/api/analysis'
import DeviceChannelSelector from './DeviceChannelSelector'

export default {
  name: 'TaskDialog',
  components: {
    DeviceChannelSelector
  },
  data() {
    return {
      dialogVisible: false,
      isEdit: false,
      currentTaskId: null,

      // 表单数据
      taskForm: {
        taskName: '',
        description: '',
        deviceId: '',
        channelId: '',
        analysisQuestion: '',
        analysisInterval: 60,
        alarmKeywords: '',
        enabled: true,
        autoStart: true,
        maxRetries: 3,
        timeout: 30
      },

      // 设备通道选择
      deviceChannelSelection: {
        deviceId: '',
        channelId: ''
      },

      // 界面状态
      selectedTemplate: '',
      showAdvanced: false,
      selectedDeviceInfo: null,
      deviceValidation: { valid: false, message: '' },

      // 加载状态
      submitLoading: false,
      deleteLoading: false,

      // 问题模板
      questionTemplates: QUESTION_TEMPLATES,

      // 表单验证规则
      formRules: {
        taskName: [
          { required: true, message: '请输入任务名称', trigger: 'blur' },
          { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
        ],
        analysisQuestion: [
          { required: true, message: '请输入分析问题', trigger: 'blur' },
          { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
        ],
        analysisInterval: [
          { required: true, message: '请设置分析间隔', trigger: 'blur' },
          { type: 'number', min: 10, max: 3600, message: '间隔在 10 到 3600 秒之间', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑分析任务' : '新增分析任务'
    },

    deviceStatusMessage() {
      if (!this.selectedDeviceInfo) return ''

      const { online, name, deviceId } = this.selectedDeviceInfo
      return online
        ? `设备 ${name} (${deviceId}) 在线，可正常进行分析`
        : `设备 ${name} (${deviceId}) 离线，可能无法获取视频流`
    }
  },
  methods: {
    // 显示对话框
    show(task = null) {
      this.dialogVisible = true
      this.isEdit = !!task
      this.currentTaskId = task ? task.id : null

      if (task) {
        this.loadTaskData(task)
      } else {
        this.resetForm()
      }
    },

    // 隐藏对话框
    hide() {
      this.dialogVisible = false
    },

    // 加载任务数据
    loadTaskData(task) {
      this.taskForm = {
        taskName: task.taskName || '',
        description: task.description || '',
        deviceId: task.deviceId || '',
        channelId: task.channelId || '',
        analysisQuestion: task.analysisQuestion || '',
        analysisInterval: task.analysisInterval || 60,
        alarmKeywords: task.alarmKeywords || '',
        enabled: task.enabled !== false,
        autoStart: task.autoStart !== false,
        maxRetries: task.maxRetries || 3,
        timeout: task.timeout || 30
      }

      this.deviceChannelSelection = {
        deviceId: task.deviceId || '',
        channelId: task.channelId || ''
      }
    },

    // 重置表单
    resetForm() {
      this.taskForm = {
        taskName: '',
        description: '',
        deviceId: '',
        channelId: '',
        analysisQuestion: '',
        analysisInterval: 60,
        alarmKeywords: '',
        enabled: true,
        autoStart: true,
        maxRetries: 3,
        timeout: 30
      }

      this.deviceChannelSelection = {
        deviceId: '',
        channelId: ''
      }

      this.selectedTemplate = ''
      this.showAdvanced = false
      this.selectedDeviceInfo = null
      this.deviceValidation = { valid: false, message: '' }

      if (this.$refs.taskForm) {
        this.$refs.taskForm.clearValidate()
      }
    },

    // 模板变更
    onTemplateChange(question) {
      if (question) {
        this.taskForm.analysisQuestion = question
      }
    },

    // 设备变更
    onDeviceChange(deviceInfo) {
      this.selectedDeviceInfo = deviceInfo
      this.taskForm.deviceId = deviceInfo ? deviceInfo.deviceId : ''
    },

    // 通道变更
    onChannelChange(channelInfo) {
      this.taskForm.channelId = channelInfo ? channelInfo.channelId : ''
    },

    // 设备验证状态变更
    onDeviceValidationChange(validation) {
      this.deviceValidation = validation
    },

    // 验证并提交
    async validateAndSubmit() {
      try {
        // 表单验证
        await this.$refs.taskForm.validate()

        // 设备通道验证
        if (!this.deviceValidation.valid) {
          this.$message.error(this.deviceValidation.message || '请选择有效的设备和通道')
          return
        }

        // 配置验证
        const configValid = await this.validateConfig()
        if (!configValid) {
          return
        }

        this.submitLoading = true

        if (this.isEdit) {
          await this.updateTask()
        } else {
          await this.createTask()
        }

      } catch (error) {
        console.error('Form validation error:', error)
      } finally {
        this.submitLoading = false
      }
    },

    // 验证任务配置
    async validateConfig() {
      try {
        const response = await validateTaskConfig({
          deviceId: this.taskForm.deviceId,
          channelId: this.taskForm.channelId,
          analysisQuestion: this.taskForm.analysisQuestion,
          analysisInterval: this.taskForm.analysisInterval
        })

        if (response.code !== 0) {
          this.$message.error(response.msg || '配置验证失败')
          return false
        }

        return true
      } catch (error) {
        this.$message.error('配置验证失败')
        return false
      }
    },

    // 创建任务
    async createTask() {
      const response = await createAnalysisTask(this.taskForm)

      if (response.code === 0) {
        this.$message.success('任务创建成功')
        this.$emit('task-created', response.data)

        if (this.taskForm.autoStart) {
          this.$message.info('任务已自动启动')
        }

        this.hide()
      } else {
        this.$message.error(response.msg || '任务创建失败')
      }
    },

    // 更新任务
    async updateTask() {
      const response = await updateAnalysisTask(this.currentTaskId, this.taskForm)

      if (response.code === 0) {
        this.$message.success('任务更新成功')
        this.$emit('task-updated', response.data)
        this.hide()
      } else {
        this.$message.error(response.msg || '任务更新失败')
      }
    },

    // 删除任务
    async deleteTask() {
      try {
        await this.$confirm('确定要删除这个任务吗？删除后无法恢复。', '确认删除', {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning'
        })

        this.deleteLoading = true
        const response = await deleteAnalysisTask(this.currentTaskId)

        if (response.code === 0) {
          this.$message.success('任务删除成功')
          this.$emit('task-deleted', this.currentTaskId)
          this.hide()
        } else {
          this.$message.error(response.msg || '任务删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('任务删除失败')
        }
      } finally {
        this.deleteLoading = false
      }
    },

    // 关闭对话框
    handleClose() {
      this.hide()
    }
  }
}
</script>

<style scoped>
.task-dialog {
  border-radius: 12px;
}

/* 表单区域样式 */
.form-section {
  margin-bottom: 20px;
  border-radius: 8px;
}

.form-section:last-child {
  margin-bottom: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: #303133;
}

.section-header i {
  color: #409eff;
}

/* 表单辅助文本 */
.form-help {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 设备状态 */
.device-status {
  margin-top: 16px;
}

/* 对话框底部 */
.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.footer-left,
.footer-right {
  display: flex;
  gap: 8px;
}

/* 表单验证错误样式 */
:deep(.el-form-item.is-error .el-input__inner),
:deep(.el-form-item.is-error .el-textarea__inner) {
  border-color: #f56c6c;
}

/* 输入框样式 */
:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  border-radius: 6px;
}

/* 数字输入框样式 */
:deep(.el-input-number .el-input__inner) {
  text-align: left;
  padding-right: 50px;
}

/* 开关样式 */
:deep(.el-switch) {
  height: 22px;
}

/* 选择器样式 */
:deep(.el-select .el-input .el-select__caret) {
  color: #c0c4cc;
}

/* 卡片头部样式 */
:deep(.el-card__header) {
  padding: 16px 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .task-dialog :deep(.el-dialog) {
    width: 95%;
    margin: 0 auto;
  }

  .dialog-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .footer-left,
  .footer-right {
    justify-content: center;
  }
}

/* 表单项间距调整 */
:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

/* 文本域样式 */
:deep(.el-textarea .el-textarea__inner) {
  resize: vertical;
  min-height: 80px;
}

/* 标签样式 */
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}
</style>