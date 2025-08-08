# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

WVP-PRO是一个基于GB28181-2016标准实现的开箱即用的网络视频平台，负责实现核心信令与设备管理后台部分。项目使用Spring Boot作为后端框架，Vue.js作为前端框架。

## 开发环境配置

### 后端构建命令
- 编译项目：`mvn clean package -Dmaven.test.skip=true`
- 运行应用：`java -jar target/wvp-pro-*.jar`
- 打包脚本：`./buildPackage.sh` (创建发布包)
- 启动服务：`./bootstrap.sh start`
- 停止服务：`./bootstrap.sh stop`
- 重启服务：`./bootstrap.sh restart`

### 前端构建命令  
```bash
cd web
npm install
npm run dev        # 开发环境
npm run build:prod # 生产环境构建
npm run lint       # 代码检查
```

### 数据库
- 支持MySQL、PostgreSQL、金仓数据库
- 数据库脚本位于 `数据库/` 目录，按版本组织
- 当前版本：2.7.4，对应数据库脚本在 `数据库/2.7.4/` 目录

## 核心架构

### 包结构
- `com.genersoft.iot.vmp.gb28181` - GB28181协议实现，包含SIP信令处理、设备管理、通道控制等
- `com.genersoft.iot.vmp.media` - 流媒体服务管理，与ZLMediaKit集成
- `com.genersoft.iot.vmp.service` - 业务服务层，包含用户管理、云录像、移动位置等服务
- `com.genersoft.iot.vmp.storager` - 数据存储层，Redis缓存和数据库操作
- `com.genersoft.iot.vmp.streamProxy` - 流代理服务
- `com.genersoft.iot.vmp.streamPush` - 流推送服务
- `com.genersoft.iot.vmp.vmanager` - 管理接口API
- `com.genersoft.iot.vmp.web` - Web接口控制器

### 关键组件
- **SipLayer**: GB28181 SIP协议处理核心
- **MediaServerService**: 流媒体服务器管理
- **DeviceService**: 设备管理服务
- **PlatformService**: 平台级联服务
- **PlayService**: 视频播放服务
- **RedisRpcService**: Redis RPC远程调用服务

### 前端架构
- 基于Vue 2.6.10 + Element UI构建
- 主要目录：
  - `src/views/` - 页面组件
  - `src/components/` - 公共组件
  - `src/api/` - API接口封装
  - `src/store/` - Vuex状态管理

## 配置文件
- `src/main/resources/application.yml` - 主配置文件
- `src/main/resources/application-dev.yml` - 开发环境配置
- `src/main/resources/application-docker.yml` - Docker环境配置
- `pom.xml` - Maven依赖配置，使用Spring Boot 2.7.18

## 关键特性
- 支持海康、大华、宇视等品牌IPC/NVR接入
- 支持国标级联和平台对接
- 支持多种协议流输出(RTSP/RTMP/HTTP-FLV/WebSocket-FLV/HLS)
- 支持云台控制、预置位、录像回放等功能
- 支持Redis集群和流媒体节点集群
- 前后端分离部署，支持跨域请求

## 第三方依赖
- **ZLMediaKit**: 流媒体服务器 (https://github.com/ZLMediaKit/ZLMediaKit)
- **jessibuca**: Web播放器 (https://github.com/langhuihui/jessibuca/tree/v3)
- **h265web.js**: H265播放器 (https://github.com/numberwolf/h265web.js)

## 开发注意事项
- 项目使用Java 8，Maven 3.x
- 前端使用Node.js >= 8.9，npm >= 3.0.0
- 数据库表结构升级请参考 `数据库/版本号/` 目录下的更新脚本
- Redis用于缓存和集群间通信，生产环境建议使用Redis集群

## 智能分析模块文件结构

### 后端文件结构
```
src/main/java/com/genersoft/iot/vmp/
├── analysis/
│   ├── bean/                        # 实体类
│   │   ├── AnalysisCard.java
│   │   ├── AnalysisTask.java  
│   │   ├── AnalysisAlarm.java
│   │   └── dto/                     # VLM数据传输对象
│   │       ├── VLMJobRequest.java
│   │       └── VLMAnalysisResult.java
│   └── service/                     # 业务服务（接口使用I前缀）
│       ├── IAnalysisCardService.java
│       ├── IAnalysisTaskService.java
│       ├── IVLMClientService.java
│       └── VLMCallbackProcessor.java
├── storager/dao/                    # 数据访问层（MyBatis注解）
│   ├── AnalysisCardMapper.java
│   ├── AnalysisTaskMapper.java
│   └── AnalysisAlarmMapper.java
└── vmanager/analysis/               # 管理API控制器
    ├── AnalysisCardController.java
    ├── AnalysisTaskController.java
    └── AnalysisAlarmController.java
```

### 数据库文件
```
数据库/2.7.4/
├── 智能分析-mysql-2.7.4.sql        # MySQL版本
└── 智能分析-postgresql-2.7.4.sql   # PostgreSQL版本
```

### 前端文件结构
```
web/src/
├── views/analysis/                  # 智能分析页面
│   ├── cards/
│   │   ├── index.vue               # 卡片列表页
│   │   └── components/
│   │       ├── CardItem.vue        # 单个卡片组件
│   │       └── CardEditor.vue      # 卡片编辑对话框
│   ├── tasks/
│   │   ├── index.vue               # 任务列表页
│   │   └── components/
│   │       ├── TaskForm.vue        # 任务表单
│   │       └── DeviceChannelSelector.vue
│   └── alarms/
│       ├── index.vue               # 告警列表页
│       └── components/
│           ├── AlarmItem.vue       # 告警项组件
│           └── AlarmDetail.vue     # 告警详情对话框
├── api/
│   └── analysis.js                 # 智能分析API接口
├── store/modules/
│   └── analysis.js                 # Vuex状态管理
└── utils/
    └── webSocketService.js         # WebSocket服务
```

### API路径规范
- 管理接口：`/api/vmanager/analysis/*`
- VLM回调：`/api/vlm/callback`
- 数据库表：`wvp_analysis_card`、`wvp_analysis_task`、`wvp_analysis_alarm`