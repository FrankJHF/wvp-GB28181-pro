# WVP智能分析模块集成实施计划

## 1. 数据库和实体层实现

- [ ] 1.1 创建数据库表结构
  - 在`数据库/2.7.4/`目录下创建SQL脚本，建立wvp_analysis_card、wvp_analysis_task、wvp_analysis_alarm三张表
  - 使用WVP表命名规范（wvp_前缀），主键使用自增ID，字段使用snake_case命名
  - 添加必要的索引和外键约束
  - _需求: 1.3, 2.5, 3.2_

- [ ] 1.2 实现分析卡片实体和枚举类
  - 在`com.genersoft.iot.vmp.analysis.bean`包下创建AnalysisCard实体类
  - 实现TaskStatus、AlarmStatus等枚举类，遵循WVP命名规范
  - 使用snake_case到camelCase的字段映射
  - _需求: 1.1, 1.3_

- [ ] 1.3 实现分析任务实体类
  - 在`com.genersoft.iot.vmp.analysis.bean`包下创建AnalysisTask实体类
  - 实现状态转换辅助方法（canStart、canPause等），包含完整的状态生命周期管理
  - 编写状态转换逻辑的单元测试
  - _需求: 2.5, 7.10, 7.11_

- [ ] 1.4 实现分析告警实体类
  - 在`com.genersoft.iot.vmp.analysis.bean`包下创建AnalysisAlarm实体类
  - 实现VideoWindowInfo内嵌类处理视频窗口信息
  - 建立与AnalysisTask的外键关联关系
  - _需求: 3.2, 3.3_

## 2. VLM微服务集成层

- [ ] 2.1 创建VLM客户端服务接口
  - 在`com.genersoft.iot.vmp.analysis.service`包下定义IVLMClientService接口
  - 在`com.genersoft.iot.vmp.analysis.bean.dto`包下创建VLM请求和响应的数据传输对象
  - 实现VLMJobRequest、VLMJobResponse、VLMJobActionResponse等模型类
  - _需求: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 2.2 实现VLM客户端服务
  - 在`com.genersoft.iot.vmp.analysis.service`包下实现VLMClientService类
  - 使用RestTemplate调用VLM微服务接口，支持auto_start参数控制
  - 实现所有VLM操作方法（createJob、startJob、pauseJob、resumeJob、stopJob）
  - 添加连接超时、重试机制和错误处理
  - _需求: 4.1, 4.2, 4.3, 4.4, 4.5, 4.7_

- [ ] 2.3 实现VLM健康检查服务
  - 实现checkHealth方法调用VLM的/health接口
  - 创建@Scheduled定时任务，每分钟检查VLM服务健康状态
  - 实现健康状态异常时的告警通知机制
  - _需求: 4.9, 4.10_

- [ ] 2.4 编写VLM集成服务的单元测试
  - 使用@MockBean模拟RestTemplate测试各种成功和失败场景
  - 测试重试机制、超时处理和异常处理逻辑
  - 验证健康检查和告警通知功能
  - _需求: 4.7_

## 3. 数据访问层实现

- [ ] 3.1 创建分析卡片数据访问层
  - 在`com.genersoft.iot.vmp.storager.dao`包下实现AnalysisCardMapper接口
  - 使用MyBatis注解方式实现CRUD操作（@Select、@Insert、@Update、@Delete）
  - 实现分页查询和条件查询方法，支持动态SQL
  - _需求: 1.1, 1.2, 1.3, 1.4_

- [ ] 3.2 创建分析任务数据访问层
  - 在`com.genersoft.iot.vmp.storager.dao`包下实现AnalysisTaskMapper接口
  - 使用MyBatis注解实现按状态查询、按设备通道查询等方法
  - 添加状态同步时间查询支持，处理复杂查询条件
  - _需求: 2.1, 2.6, 2.7, 2.8, 2.9_

- [ ] 3.3 创建分析告警数据访问层
  - 在`com.genersoft.iot.vmp.storager.dao`包下实现AnalysisAlarmMapper接口
  - 实现按时间范围、设备、通道的复合查询，使用注解处理动态SQL
  - 支持告警状态更新操作和分页查询
  - _需求: 3.5, 3.7, 3.8_

- [ ] 3.4 编写数据访问层集成测试
  - 使用@SpringBootTest测试各种查询方法
  - 验证分页、排序、条件查询功能
  - 测试数据库约束和级联操作
  - _需求: 全部数据查询相关需求_

## 4. 业务服务层

- [ ] 4.1 实现分析卡片服务
  - 创建IAnalysisCardService接口（使用I前缀）和AnalysisCardService实现类
  - 实现卡片管理的完整业务逻辑（创建、更新、删除、查询）
  - 添加权限控制，普通用户隐藏prompt字段
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7_

- [ ] 4.2 实现任务状态管理服务
  - 创建ITaskStateService接口和TaskStateService实现类
  - 实现任务状态转换方法，包含状态验证和并发控制
  - 集成VLM客户端服务，实现状态同步和一致性检查机制
  - _需求: 2.6, 2.7, 2.8, 2.9, 7.1-7.11_

- [ ] 4.3 实现分析任务管理服务
  - 创建IAnalysisTaskService接口和AnalysisTaskService实现类
  - 实现任务创建时的设备通道验证和RTSP地址获取
  - 集成VLM客户端服务调用和任务配置参数的自动填充逻辑
  - _需求: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ] 4.4 实现VLM回调处理服务
  - 创建VLMCallbackProcessor组件，使用@Async("taskExecutor")异步处理
  - 实现回调数据验证、解析和emergency_exist事件处理
  - 实现快照图片的base64解码和文件存储功能
  - _需求: 3.1, 3.2, 3.3, 5.1, 5.3_

- [ ] 4.5 实现告警管理服务
  - 创建IAnalysisAlarmService接口和AnalysisAlarmService实现类
  - 实现告警状态更新、查询和历史筛选功能
  - 集成WebSocket实时推送服务
  - _需求: 3.4, 3.5, 3.6, 3.7, 3.8_

- [ ] 4.6 编写业务服务层单元测试
  - 测试各服务类的核心业务逻辑
  - 使用@MockBean模拟Mapper和外部依赖
  - 验证异常处理和边界条件
  - _需求: 所有业务逻辑相关需求_

## 5. Web控制器层

- [ ] 5.1 实现分析卡片控制器
  - 在`com.genersoft.iot.vmp.vmanager.analysis`包下创建AnalysisCardController类
  - 实现GET /api/vmanager/analysis/cards（分页查询）接口
  - 实现POST、PUT、DELETE接口，使用WVPResult统一响应格式
  - 添加参数验证和权限控制
  - _需求: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ] 5.2 实现分析任务控制器
  - 在`com.genersoft.iot.vmp.vmanager.analysis`包下创建AnalysisTaskController类
  - 实现任务管理的完整REST接口，路径为/api/vmanager/analysis/tasks
  - 实现任务操作接口（start、pause、resume、stop），返回WVPResult格式
  - 添加参数验证和状态检查
  - _需求: 2.1, 2.2, 2.6, 2.7, 2.8, 2.9, 2.11, 2.12_

- [ ] 5.3 实现分析告警控制器
  - 在`com.genersoft.iot.vmp.vmanager.analysis`包下创建AnalysisAlarmController类
  - 实现GET /api/vmanager/analysis/alarms（告警列表查询）接口
  - 实现POST /api/vlm/callback（VLM回调接收）接口
  - 实现告警状态更新接口，使用统一的错误处理机制
  - _需求: 3.1, 3.5, 3.7_

- [ ] 5.4 实现全局异常处理器
  - 扩展现有的GlobalExceptionHandler或创建AnalysisExceptionHandler
  - 定义分析模块专用的业务异常类和错误码
  - 确保所有异常都返回WVPResult格式的响应
  - _需求: 所有错误处理相关需求_

- [ ] 5.5 编写控制器集成测试
  - 使用@SpringBootTest测试各个REST接口
  - 验证请求参数验证、响应格式和异常处理
  - 测试VLM回调接收的完整流程
  - _需求: 所有API接口相关需求_

## 6. 前端路由和页面结构

- [ ] 6.1 创建前端路由配置
  - 在web/src/router/index.js中添加智能分析模块路由
  - 创建/analysis父路由和cards、tasks、alarms三个子路由
  - 配置路由权限控制和导航菜单集成
  - _需求: 8.1, 8.3_

- [ ] 6.2 创建前端页面文件结构
  - 创建web/src/views/analysis目录结构
  - 创建cards/index.vue、tasks/index.vue、alarms/index.vue主页面文件
  - 为每个页面创建对应的components子目录
  - _需求: 8.1_

## 7. 分析卡片前端实现

- [ ] 7.1 实现卡片列表页面
  - 创建views/analysis/cards/index.vue主页面组件
  - 实现Element UI卡片网格布局，每行3-4个卡片
  - 添加新建卡片按钮和基于用户角色的权限控制
  - 集成Element UI分页组件和搜索功能
  - _需求: 1.1, 1.6, 8.1, 8.3_

- [ ] 7.2 实现单个卡片组件
  - 创建views/analysis/cards/components/CardItem.vue组件
  - 实现卡片信息展示和禁用状态的视觉效果
  - 添加使用、编辑按钮和权限控制逻辑
  - _需求: 1.1, 1.5, 1.6_

- [ ] 7.3 实现卡片编辑对话框
  - 创建views/analysis/cards/components/CardEditor.vue对话框组件
  - 实现完整的表单字段（标题、描述、图标、标签、提示词、模型类型）
  - 添加Element UI表单验证和数据提交逻辑
  - 支持创建和编辑两种模式的切换
  - _需求: 1.2, 1.3, 1.4, 1.7_

- [ ] 7.4 实现卡片API接口封装
  - 创建web/src/api/analysis.js文件，封装所有智能分析相关API
  - 实现卡片相关方法：getCards、createCard、updateCard、deleteCard
  - 添加请求错误处理、loading状态管理和响应数据格式化
  - _需求: 1.1, 1.2, 1.3, 1.4_

## 8. 分析任务前端实现

- [ ] 8.1 实现任务列表页面
  - 创建views/analysis/tasks/index.vue主页面组件
  - 使用Element UI表格组件实现任务列表布局
  - 添加新建任务、批量操作、搜索过滤和状态筛选功能
  - 实现任务状态的可视化显示和操作按钮的动态控制
  - _需求: 2.1, 2.11, 2.12, 8.3_

- [ ] 8.2 实现任务创建/编辑表单
  - 创建views/analysis/tasks/components/TaskForm.vue表单组件
  - 实现任务名称、分析卡片选择、设备通道选择等字段
  - 添加卡片选择时配置参数的自动填充逻辑
  - 实现Element UI表单验证和提交功能
  - _需求: 2.2, 2.3, 2.4, 2.5_

- [ ] 8.3 实现设备通道选择器
  - 创建views/analysis/tasks/components/DeviceChannelSelector.vue组件
  - 实现设备和通道的Element UI级联选择器
  - 添加设备通道有效性验证和RTSP流地址自动获取
  - _需求: 2.4_

- [ ] 8.4 实现任务状态管理组件
  - 创建任务状态的可视化指示器（颜色、图标、进度条）
  - 实现启动、暂停、恢复、停止操作按钮组
  - 添加过渡状态时的按钮禁用和加载指示逻辑
  - 实现状态同步功能按钮
  - _需求: 7.1-7.11_

- [ ] 8.5 实现任务API接口封装
  - 扩展api/analysis.js添加任务相关API方法
  - 实现getTasks、createTask、startTask、pauseTask、resumeTask、stopTask方法
  - 添加任务状态轮询机制和WebSocket状态实时更新
  - _需求: 2.1, 2.6, 2.7, 2.8, 2.9_

## 9. 分析告警前端实现

- [ ] 9.1 实现告警列表页面
  - 创建views/analysis/alarms/index.vue主页面组件
  - 实现Element UI时间线或列表组件，按时间倒序显示告警
  - 添加按设备、通道、类型的高级筛选功能
  - 实现时间范围选择器和实时自动刷新功能
  - _需求: 3.5, 3.8, 8.2, 8.3_

- [ ] 9.2 实现告警项组件
  - 创建views/analysis/alarms/components/AlarmItem.vue告警项组件
  - 显示设备信息、告警时间、分析类型和告警级别
  - 实现告警描述展示和快照图片缩略图预览
  - 添加告警状态标记和快速操作功能
  - _需求: 3.6, 3.7_

- [ ] 9.3 实现告警详情对话框
  - 创建views/analysis/alarms/components/AlarmDetail.vue详情对话框
  - 实现快照图片的Element UI图片预览和放大查看功能
  - 显示完整的事件时间信息和视频窗口坐标详情
  - 支持告警状态更新和备注添加功能
  - _需求: 3.6, 3.7_

- [ ] 9.4 实现告警WebSocket服务
  - 创建web/src/utils/webSocketService.js封装WebSocket连接
  - 实现告警实时推送接收、连接状态管理和自动重连机制
  - 集成到告警列表页面实现新告警的实时展示和通知
  - _需求: 3.4_

- [ ] 9.5 实现告警API接口封装
  - 扩展api/analysis.js添加告警相关API方法
  - 实现getAlarms、updateAlarmStatus、getAlarmDetail方法
  - 添加告警查询参数的封装和分页处理
  - _需求: 3.5, 3.7, 3.8_

## 10. 系统集成和配置

- [ ] 10.1 配置数据库连接和事务管理
  - 更新src/main/resources/application.yml添加VLM服务配置
  - 验证数据库连接池配置和事务管理器设置
  - 添加智能分析模块的MyBatis扫描路径配置
  - _需求: 4.8, 4.11_

- [ ] 10.2 配置定时任务和异步处理
  - 确认@EnableScheduling和@EnableAsync注解已启用
  - 配置VLM健康检查和任务状态同步的@Scheduled定时任务
  - 验证异步执行使用全局taskExecutor线程池配置
  - _需求: 4.8, 4.9, 5.1, 5.2_

- [ ] 10.3 配置WebSocket支持
  - 创建或扩展现有WebSocket配置类
  - 配置告警推送的WebSocket端点和消息处理器
  - 实现WebSocket连接管理、用户会话管理和消息广播功能
  - _需求: 3.4_

- [ ] 10.4 配置文件存储和访问
  - 配置快照图片的文件存储路径和访问策略
  - 添加静态资源映射配置，支持图片文件的HTTP访问
  - 实现文件上传下载的工具类和清理策略
  - _需求: 3.3, 5.3_

## 11. 单元测试和集成测试

- [ ] 11.1 完善业务服务测试覆盖
  - 为所有Service类编写完整的单元测试
  - 使用@MockBean和Mockito模拟Mapper和外部依赖
  - 确保核心业务逻辑的测试覆盖率达到80%以上
  - _需求: 所有业务逻辑需求_

- [ ] 11.2 编写控制器集成测试
  - 使用@SpringBootTest和MockMvc测试所有REST接口
  - 测试VLM回调处理的完整端到端场景
  - 验证异常处理、参数验证和WVPResult响应格式
  - _需求: 所有API接口需求_

- [ ] 11.3 编写前端组件测试
  - 使用Vue Test Utils为关键组件编写单元测试
  - 测试用户交互、状态变化和API调用逻辑
  - 验证组件渲染、数据绑定和事件处理功能
  - _需求: 所有前端交互需求_

- [ ] 11.4 编写系统集成测试
  - 创建模拟VLM服务的测试环境
  - 测试完整的任务创建到告警生成流程
  - 验证前后端集成、WebSocket通信和文件存储功能
  - _需求: 需求1-7的综合场景_