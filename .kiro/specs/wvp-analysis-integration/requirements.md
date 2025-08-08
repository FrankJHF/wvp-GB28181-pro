# WVP智能分析模块集成需求文档

## 项目概述

在现有WVP-GB28181-pro视频监控平台基础上，集成VLM-Engine智能分析微服务，实现视频流的实时智能分析能力。该集成需要在WVP前端新增智能分析功能模块，后端开发相应API接口，与VLM微服务进行交互。

## 需求

### 需求 1: 分析卡片管理功能

**用户故事：** 作为系统管理员，我希望能够创建和管理多种预设的智能分析场景，以便为不同监控场景提供标准化的分析模板。

#### 验收标准

1. WHEN 管理员访问分析卡片页面 THEN 系统 SHALL 显示现有分析卡片的网格布局，每行3-4个卡片
2. WHEN 管理员点击"创建卡片"按钮 THEN 系统 SHALL 打开卡片编辑对话框，包含标题、描述、图标、标签、提示词、模型类型等字段
3. WHEN 管理员填写卡片信息并保存 THEN 系统 SHALL 验证数据完整性，创建新的分析卡片记录，并刷新页面显示
4. WHEN 管理员点击现有卡片的"编辑"按钮 THEN 系统 SHALL 加载卡片数据到编辑对话框并允许修改
5. WHEN 管理员修改卡片状态为禁用 THEN 系统 SHALL 更新卡片的enabled状态为false，界面显示为禁用状态
6. IF 用户不是管理员角色 THEN 系统 SHALL 隐藏编辑和删除按钮，仅显示"使用"按钮
7. WHEN 普通用户查看卡片 THEN 系统 SHALL 隐藏prompt字段，仅显示标题、描述、标签等用户界面信息

### 需求 2: 分析任务管理功能

**用户故事：** 作为监控操作员，我希望能够基于预设的分析卡片创建和管理分析任务，以便对特定的设备通道启用智能分析功能。

#### 验收标准

1. WHEN 用户访问任务列表页面 THEN 系统 SHALL 显示现有分析任务的表格，包含任务名称、分析类型、关联设备、状态等信息
2. WHEN 用户点击"新建任务"按钮 THEN 系统 SHALL 打开任务创建表单，包含任务名称、分析卡片选择、设备通道选择等字段
3. WHEN 用户选择分析卡片 THEN 系统 SHALL 自动填充该卡片的配置参数，并允许用户调整
4. WHEN 用户选择设备通道 THEN 系统 SHALL 验证设备通道的有效性，并自动获取RTSP流地址
5. WHEN 用户创建任务 THEN 系统 SHALL 保存任务信息，初始状态设置为created，调用VLM微服务创建作业但不自动启动
6. WHEN 用户点击"启动任务"按钮 THEN 系统 SHALL 调用VLM微服务启动接口，更新任务状态为starting，成功后更新为running
7. WHEN 用户点击"暂停任务"按钮 THEN 系统 SHALL 调用VLM微服务暂停接口，更新任务状态为pausing，成功后更新为paused
8. WHEN 用户点击"恢复任务"按钮 THEN 系统 SHALL 调用VLM微服务恢复接口，更新任务状态为resuming，成功后更新为running
9. WHEN 用户点击"停止任务"按钮 THEN 系统 SHALL 调用VLM微服务停止接口，更新任务状态为stopping，成功后更新为stopped
10. IF VLM微服务调用失败 THEN 系统 SHALL 更新任务状态为error，并记录错误信息
11. WHEN 任务处于running状态 THEN 系统 SHALL 在界面上提供暂停和停止操作按钮
12. WHEN 任务处于paused状态 THEN 系统 SHALL 在界面上提供恢复和停止操作按钮

### 需求 3: 智能告警管理功能

**用户故事：** 作为监控人员，我希望能够接收和管理智能分析产生的告警信息，以便及时响应异常事件。

#### 验收标准

1. WHEN VLM微服务发送回调数据 THEN 系统 SHALL 接收并验证回调数据的完整性和有效性
2. WHEN 回调数据包含emergency_exist=true的事件 THEN 系统 SHALL 创建告警记录，包含事件描述、快照图片、时间信息等
3. IF 回调数据包含snapshot_base64字段 THEN 系统 SHALL 解码并存储快照图片，在告警记录中保存图片路径
4. WHEN 系统创建告警记录 THEN 系统 SHALL 通过WebSocket向前端推送实时告警通知
5. WHEN 用户访问告警页面 THEN 系统 SHALL 按时间倒序显示告警列表，支持设备、通道、类型筛选
6. WHEN 用户点击告警快照 THEN 系统 SHALL 在弹窗中放大显示完整快照图片
7. WHEN 用户标记告警状态 THEN 系统 SHALL 更新告警的处理状态为已处理、已忽略等
8. WHEN 用户查询历史告警 THEN 系统 SHALL 支持按时间范围、设备、通道等条件过滤查询

### 需求 4: VLM微服务集成

**用户故事：** 作为系统架构师，我希望WVP平台能够稳定可靠地与VLM微服务集成，以便提供智能分析能力。

#### 验收标准

1. WHEN 系统需要创建VLM作业 THEN 系统 SHALL 调用POST /api/vlm/jobs接口，传递设备ID、通道ID、RTSP地址、回调地址等参数，设置auto_start=false
2. WHEN 系统需要启动VLM作业 THEN 系统 SHALL 调用POST /api/vlm/jobs/{jobId}/start接口，支持force_restart参数
3. WHEN 系统需要暂停VLM作业 THEN 系统 SHALL 调用POST /api/vlm/jobs/{jobId}/pause接口
4. WHEN 系统需要恢复VLM作业 THEN 系统 SHALL 调用POST /api/vlm/jobs/{jobId}/resume接口
5. WHEN 系统需要停止VLM作业 THEN 系统 SHALL 调用DELETE /api/vlm/jobs/{jobId}接口
6. WHEN 系统需要查询作业状态 THEN 系统 SHALL 调用GET /api/vlm/jobs/{jobId}接口，识别created/pending/running/paused/completed/failed/cancelled状态
7. WHEN VLM微服务不可用 THEN 系统 SHALL 记录错误日志，更新相关任务状态为error，并发送告警通知
8. WHEN 系统定时同步任务状态 THEN 系统 SHALL 每30秒查询所有非stopped状态任务的VLM作业状态
9. WHEN 系统进行健康检查 THEN 系统 SHALL 每分钟调用VLM微服务的/health接口，监控各组件状态
10. IF VLM微服务健康状态为unhealthy THEN 系统 SHALL 发送告警通知给系统管理员
11. WHEN VLM作业状态变化 THEN 系统 SHALL 同步更新WVP任务状态，确保状态一致性

### 需求 5: 系统监控与性能优化

**用户故事：** 作为系统运维人员，我希望系统能够高效处理VLM回调并提供监控指标，以便保障系统稳定运行。

#### 验收标准

1. WHEN 系统接收VLM回调 THEN 系统 SHALL 异步处理回调数据，避免阻塞VLM微服务
2. WHEN 回调处理失败 THEN 系统 SHALL 执行重试机制，最多重试3次，重试间隔1秒
3. WHEN 系统处理快照图片 THEN 系统 SHALL 将base64编码的图片存储到文件系统，数据库仅保存文件路径
4. WHEN 系统记录监控指标 THEN 系统 SHALL 统计回调接收频率、处理成功率、失败率等关键指标
5. WHEN 系统批量写入告警数据 THEN 系统 SHALL 考虑批量操作以提高数据库写入性能
6. WHEN 系统处理完图片数据 THEN 系统 SHALL 及时释放内存中的图片数据

### 需求 7: 任务状态生命周期管理

**用户故事：** 作为监控操作员，我希望系统能够准确跟踪和展示任务的详细状态变化，以便了解任务的完整生命周期。

#### 验收标准

1. WHEN 任务刚创建时 THEN 系统 SHALL 将任务状态设置为created，界面显示"已创建"状态
2. WHEN VLM作业正在启动时 THEN 系统 SHALL 将任务状态设置为starting，界面显示"启动中"状态和加载指示器
3. WHEN VLM作业成功启动后 THEN 系统 SHALL 将任务状态更新为running，界面显示"运行中"状态和绿色状态指示器
4. WHEN 用户执行暂停操作时 THEN 系统 SHALL 将任务状态设置为pausing，界面显示"暂停中"状态
5. WHEN VLM作业成功暂停后 THEN 系统 SHALL 将任务状态更新为paused，界面显示"已暂停"状态和橙色状态指示器
6. WHEN 用户执行恢复操作时 THEN 系统 SHALL 将任务状态设置为resuming，界面显示"恢复中"状态
7. WHEN 用户执行停止操作时 THEN 系统 SHALL 将任务状态设置为stopping，界面显示"停止中"状态
8. WHEN VLM作业成功停止后 THEN 系统 SHALL 将任务状态更新为stopped，界面显示"已停止"状态和灰色状态指示器
9. WHEN VLM作业发生异常 THEN 系统 SHALL 将任务状态设置为failed，界面显示"失败"状态和红色状态指示器
10. WHEN 任务状态为transitioning状态（starting/pausing/resuming/stopping）时 THEN 系统 SHALL 禁用相关操作按钮，防止重复操作
11. WHEN 系统检测到状态不一致 THEN 系统 SHALL 提供"同步状态"功能按钮，允许手动触发状态同步

### 需求 8: 用户界面一致性

**用户故事：** 作为最终用户，我希望智能分析模块的界面风格与WVP平台保持一致，以便获得良好的用户体验。

#### 验收标准

1. WHEN 用户访问智能分析页面 THEN 系统 SHALL 使用Element UI组件库，保持与现有页面风格一致
2. WHEN 页面在不同屏幕尺寸下显示 THEN 系统 SHALL 提供响应式设计，支持移动端和桌面端适配
3. WHEN 用户操作表格功能 THEN 系统 SHALL 提供与设备列表页面一致的分页、排序、筛选功能
4. WHEN 用户查看按钮和表单 THEN 系统 SHALL 遵循WVP的颜色方案和字体规范
5. WHEN 系统显示状态标识 THEN 系统 SHALL 使用与WVP平台一致的状态颜色和图标风格