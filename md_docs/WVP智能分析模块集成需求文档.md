# WVP-PRO智能分析模块集成需求文档

## 1. 项目概述

### 1.1 背景
在现有WVP-GB28181-pro视频监控平台基础上，集成VLM-Engine智能分析微服务，实现视频流的实时智能分析能力。该集成需要在WVP前端新增智能分析功能模块，后端开发相应API接口，与VLM微服务进行交互。

### 1.2 核心目标
- 在WVP平台中新增智能分析功能模块
- 支持多种预设分析场景的卡片化管理
- 提供任务创建、管理和监控功能
- 实现智能分析结果的告警展示
- 保持与现有WVP平台风格和架构的一致性

## 2. 前端需求

### 2.1 整体架构设计

#### 2.1.1 路由结构
```javascript
{
  path: '/analysis',
  component: Layout,
  name: '智能分析',
  meta: { title: '智能分析', icon: 'analysis' },
  children: [
    {
      path: '/analysis/cards',
      name: 'AnalysisCards',
      component: () => import('@/views/analysis/cards/index'),
      meta: { title: '分析卡片', icon: 'card' }
    },
    {
      path: '/analysis/tasks',
      name: 'AnalysisTasks', 
      component: () => import('@/views/analysis/tasks/index'),
      meta: { title: '任务列表', icon: 'tasks' }
    },
    {
      path: '/analysis/alarms',
      name: 'AnalysisAlarms',
      component: () => import('@/views/analysis/alarms/index'),
      meta: { title: '分析告警', icon: 'alarm' }
    }
  ]
}
```

#### 2.1.2 文件结构
```
web/src/views/analysis/
├── cards/
│   ├─ index.vue          # 分析卡片主页面
│   └─ components/
│      ├─ CardItem.vue    # 单个卡片组件
│      └─ CardEditor.vue  # 卡片编辑对话框
├── tasks/
│   ├─ index.vue          # 任务列表主页面
│   └─ components/
│      ├─ TaskForm.vue    # 任务创建/编辑表单
│      └─ DeviceChannelSelector.vue # 设备通道选择器
└── alarms/
    ├─ index.vue          # 分析告警主页面
    └─ components/
       ├─ AlarmItem.vue   # 告警项组件
       └─ AlarmDetail.vue # 告警详情对话框
```

### 2.2 分析卡片页面详细需求

#### 2.2.1 页面布局
- 采用WVP统一的卡片网格布局风格
- 支持响应式设计，兼容不同屏幕尺寸
- 每行显示3-4个卡片，卡片间距保持一致

#### 2.2.2 卡片组件设计
```vue
<!-- CardItem.vue 结构示例 -->
<template>
  <el-card class="analysis-card" :class="{ 'card-disabled': !card.enabled }">
    <div class="card-header">
      <img :src="card.icon" class="card-icon" />
      <h3 class="card-title">{{ card.title }}</h3>
    </div>
    <div class="card-content">
      <p class="card-description">{{ card.description }}</p>
      <div class="card-tags">
        <el-tag v-for="tag in card.tags" :key="tag" size="mini">{{ tag }}</el-tag>
      </div>
    </div>
    <div class="card-actions">
      <el-button type="primary" size="mini" @click="useCard">使用</el-button>
      <el-button type="text" size="mini" @click="editCard">编辑</el-button>
    </div>
  </el-card>
</template>
```

#### 2.2.3 数据模型
```javascript
// 分析卡片数据结构
const AnalysisCard = {
  id: String,           // 卡片ID
  title: String,        // 卡片标题
  description: String,  // 卡片描述
  icon: String,         // 卡片图标URL
  tags: Array,          // 标签数组
  enabled: Boolean,     // 是否启用
  prompt: String,       // 分析提示词（管理员可见）
  model_type: String,   // 使用的模型类型
  created_at: Date,     // 创建时间
  updated_at: Date      // 更新时间
}
```

#### 2.2.4 功能要求
- **卡片管理**: 管理员可以创建、编辑、删除分析卡片
- **Prompt管理**: 卡片包含不向普通用户展示的prompt信息
- **权限控制**: 根据用户角色决定是否可以编辑卡片
- **状态管理**: 支持卡片的启用/禁用状态切换

### 2.3 任务列表页面详细需求

#### 2.3.1 页面布局
- 采用WVP统一的表格布局风格，与设备列表页面保持一致
- 顶部工具栏包含：新建任务、批量操作、搜索过滤
- 表格支持分页、排序、筛选功能

#### 2.3.2 表格列设计
```javascript
const taskTableColumns = [
  { prop: 'task_name', label: '任务名称', width: 150 },
  { prop: 'analysis_card', label: '分析类型', width: 120 },
  { prop: 'device_info', label: '关联设备', width: 200 },
  { prop: 'channel_info', label: '关联通道', width: 150 },
  { prop: 'status', label: '状态', width: 100 },
  { prop: 'created_at', label: '创建时间', width: 150 },
  { prop: 'updated_at', label: '最后更新', width: 150 },
  { prop: 'actions', label: '操作', width: 200, fixed: 'right' }
]
```

#### 2.3.3 数据模型
```javascript
// 分析任务数据结构
const AnalysisTask = {
  id: String,               // 任务ID
  task_name: String,        // 任务名称
  analysis_card_id: String, // 关联的分析卡片ID
  device_id: String,        // 设备ID
  device_name: String,      // 设备名称
  channel_id: String,       // 通道ID
  channel_name: String,     // 通道名称
  rtsp_url: String,         // RTSP流地址
  status: String,           // 任务状态: enabled/disabled/running/error
  vlm_job_id: String,       // VLM微服务返回的job_id
  config: Object,           // 任务配置参数
  created_at: Date,         // 创建时间
  updated_at: Date          // 更新时间
}
```

#### 2.3.4 功能要求
- **任务创建**: 选择分析卡片、设备通道，创建分析任务
- **任务管理**: 启用/禁用任务，编辑任务配置
- **设备关联**: 从现有设备通道中选择关联的监控源
- **状态监控**: 实时显示任务运行状态和错误信息
- **批量操作**: 支持批量启用、禁用、删除任务

### 2.4 分析告警页面详细需求

#### 2.4.1 页面布局
- 采用时间轴布局或列表布局，按时间倒序显示告警
- 支持按设备、通道、告警类型进行筛选
- 告警项支持展开显示详细信息

#### 2.4.2 告警项组件设计
```vue
<!-- AlarmItem.vue 结构示例 -->
<template>
  <div class="alarm-item" :class="alarmLevelClass">
    <div class="alarm-header">
      <div class="alarm-info">
        <h4>{{ alarm.device_name }} - {{ alarm.channel_name }}</h4>
        <span class="alarm-time">{{ formatTime(alarm.alarm_time) }}</span>
      </div>
      <div class="alarm-status">
        <el-tag :type="getAlarmTypeTag(alarm.analysis_type)">
          {{ alarm.analysis_type }}
        </el-tag>
      </div>
    </div>
    <div class="alarm-content">
      <div class="alarm-snapshot" v-if="alarm.snapshot">
        <img :src="alarm.snapshot" @click="viewSnapshot" />
      </div>
      <div class="alarm-description">
        <p>{{ alarm.description }}</p>
      </div>
    </div>
  </div>
</template>
```

#### 2.4.3 数据模型
```javascript
// 分析告警数据结构
const AnalysisAlarm = {
  id: String,              // 告警ID
  task_id: String,         // 关联任务ID
  device_id: String,       // 设备ID
  device_name: String,     // 设备名称
  channel_id: String,      // 通道ID
  channel_name: String,    // 通道名称
  analysis_type: String,   // 分析类型
  description: String,     // 告警描述
  snapshot: String,        // 快照图片base64或URL
  alarm_time: Date,        // 告警时间
  event_start_time: Date,  // 事件开始时间
  event_end_time: Date,    // 事件结束时间
  severity: String,        // 告警级别: low/medium/high/critical
  status: String,          // 处理状态: pending/processing/resolved
  created_at: Date         // 创建时间
}
```

#### 2.4.4 功能要求
- **实时告警**: 支持WebSocket实时推送新告警
- **告警筛选**: 按时间、设备、通道、告警类型筛选
- **告警处理**: 支持标记告警状态（已处理、忽略等）
- **快照查看**: 点击快照可放大查看详情
- **历史查询**: 支持按时间范围查询历史告警

### 2.5 UI风格规范

#### 2.5.1 遵循现有WVP风格
- 使用Element UI组件库，保持与现有页面一致
- 遵循WVP的颜色方案和字体规范
- 保持按钮、表单、表格等组件的统一样式

#### 2.5.2 响应式设计
- 支持移动端和桌面端适配
- 使用WVP现有的响应式布局策略

## 3. 后端需求

### 3.1 数据库设计

#### 3.1.1 分析卡片表（analysis_cards）
```sql
CREATE TABLE analysis_cards (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(100) NOT NULL COMMENT '卡片标题',
    description TEXT COMMENT '卡片描述',
    icon VARCHAR(200) COMMENT '卡片图标URL',
    tags JSON COMMENT '标签数组',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    prompt TEXT NOT NULL COMMENT '分析提示词',
    model_type VARCHAR(50) DEFAULT 'videollama3' COMMENT '模型类型',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 3.1.2 分析任务表（analysis_tasks）
```sql
CREATE TABLE analysis_tasks (
    id VARCHAR(50) PRIMARY KEY,
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    analysis_card_id VARCHAR(50) NOT NULL COMMENT '分析卡片ID',
    device_id VARCHAR(50) NOT NULL COMMENT '设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    channel_id VARCHAR(50) NOT NULL COMMENT '通道ID',
    channel_name VARCHAR(100) COMMENT '通道名称',
    rtsp_url VARCHAR(500) COMMENT 'RTSP流地址',
    status ENUM('disabled', 'enabling', 'enabled', 'disabling', 'error') DEFAULT 'disabled',
    vlm_job_id VARCHAR(50) COMMENT 'VLM微服务Job ID',
    config JSON COMMENT '任务配置',
    error_message TEXT COMMENT '错误信息',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (analysis_card_id) REFERENCES analysis_cards(id),
    INDEX idx_device_channel (device_id, channel_id),
    INDEX idx_status (status)
);
```

#### 3.1.3 分析告警表（analysis_alarms）
```sql
CREATE TABLE analysis_alarms (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL COMMENT '任务ID',
    device_id VARCHAR(50) NOT NULL COMMENT '设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    channel_id VARCHAR(50) NOT NULL COMMENT '通道ID',
    channel_name VARCHAR(100) COMMENT '通道名称',
    analysis_type VARCHAR(50) COMMENT '分析类型',
    description TEXT COMMENT '告警描述',
    snapshot LONGTEXT COMMENT '快照图片base64',
    alarm_time TIMESTAMP NOT NULL COMMENT '告警时间',
    event_start_time TIMESTAMP COMMENT '事件开始时间',
    event_end_time TIMESTAMP COMMENT '事件结束时间',
    severity ENUM('low', 'medium', 'high', 'critical') DEFAULT 'medium',
    status ENUM('pending', 'processing', 'resolved', 'ignored') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES analysis_tasks(id),
    INDEX idx_task_time (task_id, alarm_time),
    INDEX idx_device_time (device_id, alarm_time)
);
```

### 3.2 API接口设计

#### 3.2.1 分析卡片相关接口

```java
// 控制器: AnalysisCardController.java
@RestController
@RequestMapping("/api/analysis/cards")
public class AnalysisCardController {
    
    @GetMapping
    public WVPResult<PageInfo<AnalysisCard>> getCards(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "15") int count) {
        // 获取分析卡片列表
    }
    
    @PostMapping
    public WVPResult<AnalysisCard> createCard(@RequestBody AnalysisCardParam card) {
        // 创建分析卡片
    }
    
    @PutMapping("/{id}")
    public WVPResult<AnalysisCard> updateCard(
        @PathVariable String id, 
        @RequestBody AnalysisCardParam card) {
        // 更新分析卡片
    }
    
    @DeleteMapping("/{id}")
    public WVPResult<Void> deleteCard(@PathVariable String id) {
        // 删除分析卡片
    }
}
```

#### 3.2.2 分析任务相关接口

```java
// 控制器: AnalysisTaskController.java
@RestController
@RequestMapping("/api/analysis/tasks")
public class AnalysisTaskController {
    
    @GetMapping
    public WVPResult<PageInfo<AnalysisTask>> getTasks(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "15") int count,
        @RequestParam(required = false) String deviceId,
        @RequestParam(required = false) String status) {
        // 获取分析任务列表
    }
    
    @PostMapping
    public WVPResult<AnalysisTask> createTask(@RequestBody AnalysisTaskParam task) {
        // 创建分析任务
    }
    
    @PutMapping("/{id}/enable")
    public WVPResult<Void> enableTask(@PathVariable String id) {
        // 启用分析任务 - 调用VLM微服务创建Job
    }
    
    @PutMapping("/{id}/disable") 
    public WVPResult<Void> disableTask(@PathVariable String id) {
        // 禁用分析任务 - 调用VLM微服务删除Job
    }
    
    @DeleteMapping("/{id}")
    public WVPResult<Void> deleteTask(@PathVariable String id) {
        // 删除分析任务
    }
}
```

#### 3.2.3 分析告警相关接口

```java
// 控制器: AnalysisAlarmController.java
@RestController
@RequestMapping("/api/analysis/alarms")
public class AnalysisAlarmController {
    
    @GetMapping
    public WVPResult<PageInfo<AnalysisAlarm>> getAlarms(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "15") int count,
        @RequestParam(required = false) String deviceId,
        @RequestParam(required = false) String startTime,
        @RequestParam(required = false) String endTime) {
        // 获取分析告警列表
    }
    
    @PostMapping("/callback")
    public WVPResult<Void> receiveCallback(@RequestBody VLMAnalysisResult callback) {
        // 接收VLM微服务回调 - 处理完整的分析结果
        // 根据events列表中的emergency_exist字段决定是否创建告警
    }
    
    @PutMapping("/{id}/status")
    public WVPResult<Void> updateAlarmStatus(
        @PathVariable String id,
        @RequestParam String status) {
        // 更新告警处理状态
    }
}

// VLM回调数据模型（基于实际API规范）
@Data
public class VLMAnalysisResult {
    private String jobId;                    // 作业ID
    private String deviceId;                 // 设备ID  
    private String channelId;                // 通道ID
    private String analysisTimestampUtc;     // 分析时间戳
    private VideoWindow videoWindow;         // 视频窗口时间信息
    private List<Event> events;              // 检测到的事件列表
}

@Data
public class VideoWindow {
    private String streamStartUtc;           // 窗口起始时间
    private String streamEndUtc;             // 窗口结束时间  
    private Double streamStartPts;           // 起始PTS时间戳
    private Double streamEndPts;             // 结束PTS时间戳
}

@Data  
public class Event {
    private Double eventStartPts;            // 事件起始PTS
    private Double eventEndPts;              // 事件结束PTS
    private String eventStartUtc;            // 事件起始UTC时间
    private String eventEndUtc;              // 事件结束UTC时间
    private String eventTimeRange;           // 相对时间范围 "15.0-35.0"
    private String eventDescription;         // 事件描述
    private Boolean emergencyExist;          // 是否存在紧急事件
    private String snapshotBase64;           // 快照图片base64（可选）
}
```

### 3.3 VLM微服务集成

#### 3.3.1 VLM客户端服务
```java
@Service
public class VLMClientService {
    
    @Value("${vlm.service.url}")
    private String vlmServiceUrl;
    
    /**
     * 创建VLM分析作业
     * @param request 作业请求参数
     * @return 作业响应信息
     */
    public VLMJobResponse createJob(VLMJobRequest request) {
        // 调用 POST /api/vlm/jobs
        // request包含: deviceId, channelId, inputData(RTSP地址), 
        // callbackUrl, analysisConfig, analysisPrompt, modelName
        return restTemplate.postForObject(vlmServiceUrl + "/api/vlm/jobs", request, VLMJobResponse.class);
    }
    
    /**
     * 停止VLM分析作业
     * @param jobId 作业ID
     */
    public VLMJobCancelResponse stopJob(String jobId) {
        // 调用 DELETE /api/vlm/jobs/{jobId}
        return restTemplate.exchange(
            vlmServiceUrl + "/api/vlm/jobs/" + jobId,
            HttpMethod.DELETE,
            null,
            VLMJobCancelResponse.class
        ).getBody();
    }
    
    /**
     * 查询作业状态
     * @param jobId 作业ID
     * @return 作业状态信息
     */
    public VLMJobStatusResponse getJobStatus(String jobId) {
        // 调用 GET /api/vlm/jobs/{jobId}
        return restTemplate.getForObject(
            vlmServiceUrl + "/api/vlm/jobs/" + jobId, 
            VLMJobStatusResponse.class
        );
    }
    
    /**
     * 健康检查
     * @return 服务健康状态
     */
    public VLMHealthResponse checkHealth() {
        // 调用 GET /health
        return restTemplate.getForObject(vlmServiceUrl + "/health", VLMHealthResponse.class);
    }
}

// VLM作业请求模型（基于实际API规范）
@Data
public class VLMJobRequest {
    private String deviceId;           // 设备ID（必需，最大50字符）
    private String channelId;          // 通道ID（必需，最大50字符）
    private String inputType = "rtsp_stream";  // 输入类型（固定值）
    private String inputData;          // RTSP流地址（必需，最大500字符）
    private String callbackUrl;        // 回调地址（必需，最大2083字符）
    private AnalysisConfig analysisConfig;  // 分析配置（可选）
    private String analysisPrompt;     // 自定义分析提示词（可选，最大2000字符）
    private String modelName;          // 模型名称（可选，最大100字符）
}

@Data
public class AnalysisConfig {
    private Integer inferenceInterval = 5;    // 推理间隔秒数（1-60，默认5）
    private Integer frameBufferSize = 180;    // 帧缓冲区大小（30-300，默认180）
    private Integer samplingFps = 5;          // 采样帧率（1-30，默认5）
    private Integer maxNewTokens = 180;       // 最大生成token数（50-500，默认180）
    private String analysisPrompt;            // 分析提示词（最大2000字符）
    private String modelName;                 // 模型名称（最大100字符）
}
```

#### 3.3.2 任务状态同步
```java
@Service
public class TaskSyncService {
    
    @Autowired
    private VLMClientService vlmClientService;
    
    @Scheduled(fixedDelay = 30000) // 每30秒执行一次
    public void syncTaskStatus() {
        // 同步所有启用任务的状态
        List<AnalysisTask> enabledTasks = getTasksByStatus("enabled");
        for (AnalysisTask task : enabledTasks) {
            if (StringUtils.hasText(task.getVlmJobId())) {
                try {
                    VLMJobStatusResponse status = vlmClientService.getJobStatus(task.getVlmJobId());
                    updateTaskStatus(task.getId(), status.getStatus(), status.getErrorMessage());
                } catch (Exception e) {
                    // 处理VLM微服务不可用的情况
                    log.error("Failed to sync status for task {}: {}", task.getId(), e.getMessage());
                    updateTaskStatus(task.getId(), "error", "VLM服务连接失败");
                }
            }
        }
    }
    
    /**
     * 健康检查和服务监控
     */
    @Scheduled(fixedDelay = 60000) // 每分钟检查一次
    public void checkVLMServiceHealth() {
        try {
            VLMHealthResponse health = vlmClientService.checkHealth();
            if (!"healthy".equals(health.getStatus())) {
                // 通知系统管理员VLM服务不健康
                alertService.sendAlert("VLM微服务状态异常: " + health.getMessage());
            }
        } catch (Exception e) {
            log.error("VLM健康检查失败: {}", e.getMessage());
            alertService.sendAlert("VLM微服务连接失败: " + e.getMessage());
        }
    }
}

// VLM作业状态响应模型（基于实际API规范）
@Data
public class VLMJobStatusResponse {
    private String jobId;              // 作业ID
    private String status;             // 状态: pending/running/completed/failed/cancelled
    private String deviceId;           // 设备ID
    private String channelId;          // 通道ID
    private LocalDateTime createdAt;   // 创建时间
    private LocalDateTime updatedAt;   // 更新时间
    private VLMAnalysisResult result;  // 分析结果（仅completed状态时存在）
    private String errorMessage;       // 错误信息（仅failed状态时存在）
}

@Data
public class VLMHealthResponse {
    private String status;                     // healthy/unhealthy
    private LocalDateTime timestamp;           // 检查时间
    private Map<String, Boolean> components;   // 各组件状态
    private String message;                    // 状态信息
    private Integer uptimeSeconds;             // 运行时间
}
```

### 3.4 WVP开发规范遵循

#### 3.4.1 REST API规范
- 遵循WVP现有的API响应格式（WVPResult）
- 使用统一的错误码和错误处理机制
- 参数验证和异常处理保持一致

#### 3.4.2 代码规范
- 遵循阿里巴巴Java代码规范
- 使用WVP现有的注解和工具类
- 保持与现有Service、Controller、Mapper的命名和结构一致

#### 3.4.3 数据库规范
- 遵循WVP现有的表命名和字段命名规范
- 使用统一的主键生成策略
- 建立适当的索引优化查询性能

## 4. 微服务功能完善建议

基于VLM微服务实际API文档分析，目前微服务设计已经相对完善，主要优势包括：

### 4.1 已实现的完善功能
- **完整的作业生命周期管理**: 支持创建、查询状态、停止作业
- **丰富的分析配置**: 支持推理间隔、帧缓冲、采样率等参数配置
- **完善的回调机制**: 提供详细的时空坐标信息和事件检测结果
- **健康检查机制**: 包含各组件状态监控
- **灵活的提示词配置**: 支持自定义分析提示词和模型选择

### 4.2 建议增强功能

#### 4.2.1 配置管理优化
```python
# 建议新增预设配置管理接口
@app.get("/api/vlm/presets")
async def get_analysis_presets():
    """获取预设分析配置模板"""
    return {
        "fire_detection": {
            "analysis_prompt": "请仔细分析视频中是否出现火灾或烟雾...",
            "model_name": "videollama3-fire-detection",
            "inference_interval": 5,
            "sampling_fps": 5
        },
        "security_monitoring": {
            "analysis_prompt": "请分析视频中是否有异常人员活动...",
            "model_name": "videollama3-security",
            "inference_interval": 10,
            "sampling_fps": 3
        }
    }

@app.get("/api/vlm/models")
async def get_available_models():
    """获取可用的分析模型列表"""
```

#### 4.2.2 批量作业管理
```python
@app.post("/api/vlm/jobs/batch")
async def create_batch_jobs(batch_request: BatchJobRequest):
    """批量创建作业"""

@app.delete("/api/vlm/jobs/batch")  
async def stop_batch_jobs(job_ids: List[str]):
    """批量停止作业"""
```

### 4.4 反向审查：WVP端回调接口缺失项分析

基于VLM微服务的回调数据格式，WVP平台需要确保以下回调处理能力：

#### 4.4.1 必需实现的回调接口
```java
@PostMapping("/api/vlm/callback")
public WVPResult<Void> handleVLMCallback(@RequestBody VLMAnalysisResult callback) {
    // 必须处理的关键字段：
    // 1. job_id - 关联到WVP的analysis_task记录
    // 2. device_id, channel_id - 设备通道信息验证
    // 3. analysis_timestamp_utc - 分析时间戳
    // 4. video_window - 视频窗口时间坐标
    // 5. events[] - 事件列表（重点处理emergency_exist=true的事件）
    // 6. snapshot_base64 - 快照图片处理和存储
    
    // 处理逻辑：
    // - 验证job_id对应的任务是否存在且为enabled状态
    // - 遍历events列表，仅对emergency_exist=true的创建告警
    // - 解析并存储snapshot_base64图片
    // - 记录完整的时空坐标信息用于后续查询
    // - 发送WebSocket实时告警通知
}
```

#### 4.4.2 必需处理的数据验证
1. **作业ID验证**: 确认callback中的job_id在WVP系统中存在对应的analysis_task
2. **设备通道验证**: 验证device_id和channel_id是否与创建任务时一致
3. **时间戳处理**: 正确解析UTC时间戳并转换为系统时区
4. **快照数据处理**: 验证base64格式并实现高效存储（建议压缩存储）

#### 4.4.3 错误处理和重试机制
```java
@Component
public class VLMCallbackProcessor {
    
    @Async
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public void processCallback(VLMAnalysisResult callback) {
        try {
            // 1. 验证回调数据完整性
            validateCallbackData(callback);
            
            // 2. 查找对应的分析任务
            AnalysisTask task = findTaskByJobId(callback.getJobId());
            if (task == null) {
                log.warn("Received callback for unknown job_id: {}", callback.getJobId());
                return;
            }
            
            // 3. 处理事件列表，创建告警
            for (Event event : callback.getEvents()) {
                if (Boolean.TRUE.equals(event.getEmergencyExist())) {
                    createAlarmFromEvent(task, event, callback);
                }
            }
            
            // 4. 更新任务最后活跃时间
            updateTaskLastActiveTime(task.getId(), callback.getAnalysisTimestampUtc());
            
            // 5. 发送WebSocket实时通知
            websocketService.sendAlarmNotification(callback);
            
        } catch (Exception e) {
            log.error("Failed to process VLM callback: {}", e.getMessage(), e);
            throw e; // 触发重试机制
        }
    }
}
```

#### 4.4.4 性能优化要求
1. **异步处理**: 回调处理必须异步进行，避免阻塞VLM微服务
2. **批量存储**: 考虑批量写入告警数据以提高性能
3. **图片存储优化**: 快照图片建议存储到文件系统或对象存储，数据库仅存储路径
4. **内存管理**: 及时释放base64解码后的图片数据

#### 4.4.5 监控和告警
```java
// 需要监控的关键指标
@Component  
public class VLMIntegrationMonitor {
    
    @EventListener
    public void onCallbackReceived(VLMCallbackEvent event) {
        // 统计回调接收频率
        meterRegistry.counter("vlm.callback.received").increment();
    }
    
    @EventListener
    public void onCallbackProcessingFailed(VLMCallbackFailedEvent event) {
        // 统计处理失败次数  
        meterRegistry.counter("vlm.callback.failed").increment();
        
        // 发送告警通知
        alertService.sendAlert("VLM回调处理失败", event.getErrorMessage());
    }
}
```

## 5. 开发计划

### 5.1 开发阶段

#### Phase 1: 基础框架搭建（1周）
- 创建前端页面结构和路由配置
- 设计数据库表结构
- 搭建后端API框架

#### Phase 2: 核心功能开发（2周）  
- 实现分析卡片管理功能
- 开发任务创建和管理功能
- 集成VLM微服务调用

#### Phase 3: 告警系统开发（1周）
- 实现告警接收和存储
- 开发告警展示和处理功能
- 完善WebSocket实时推送

#### Phase 4: 测试和优化（1周）
- 功能测试和性能优化
- UI/UX优化和bug修复
- 文档编写和部署准备

### 5.2 技术风险和对策

#### 5.2.1 VLM微服务集成风险
- **风险**: 微服务接口稳定性和性能问题
- **对策**: 实现降级机制和重试策略，完善监控告警

#### 5.2.2 实时性要求
- **风险**: 告警推送的实时性和可靠性
- **对策**: 结合WebSocket推送和定期轮询的双重保障机制

#### 5.2.3 数据一致性
- **风险**: WVP和VLM微服务间的数据同步问题  
- **对策**: 实现状态同步机制和数据校验

---

## 附录

### A. WVP平台集成术语对照

| VLM微服务术语 | WVP平台术语 | 说明 |
|-------------|------------|-----|
| Job | Task | VLM中的Job对应WVP中的Task |
| Analysis Card | 分析卡片 | 预设的分析场景配置 |
| Callback | 回调通知 | VLM向WVP推送分析结果 |
| Prompt | 提示词 | 模型分析的指导信息 |

### B. 关键配置参数

```yaml
# application.yml 中的VLM相关配置
vlm:
  service:
    url: http://localhost:8001  # VLM微服务地址
    apiKey: your-api-key       # API访问密钥
    timeout: 30                # 请求超时时间(秒)
  sync:
    interval: 30               # 状态同步间隔(秒)
    batchSize: 10             # 批量同步数量
```

这份需求文档为WVP平台集成VLM智能分析微服务提供了完整的技术方案，确保新功能能够无缝融入现有平台架构。