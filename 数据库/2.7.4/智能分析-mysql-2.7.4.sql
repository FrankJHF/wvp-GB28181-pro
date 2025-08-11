/*智能分析模块-MySQL 2.7.4*/

-- 分析卡片表
DROP TABLE IF EXISTS wvp_analysis_card;
CREATE TABLE IF NOT EXISTS wvp_analysis_card (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(100) NOT NULL COMMENT '卡片标题',
    description TEXT COMMENT '卡片描述',
    icon VARCHAR(200) COMMENT '卡片图标URL',
    tags JSON COMMENT '标签数组',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    prompt TEXT NOT NULL COMMENT '分析提示词',
    model_type VARCHAR(50) DEFAULT 'videollama3' COMMENT '模型类型',
    analysis_config JSON COMMENT 'VLM分析配置参数',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_enabled (enabled),
    INDEX idx_created_by (created_by),
    INDEX idx_created_at (created_at)
);

-- 分析任务表
DROP TABLE IF EXISTS wvp_analysis_task;
CREATE TABLE IF NOT EXISTS wvp_analysis_task (
    id VARCHAR(50) PRIMARY KEY,
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    analysis_card_id VARCHAR(50) NOT NULL COMMENT '分析卡片ID',
    device_id VARCHAR(50) NOT NULL COMMENT '设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    channel_id VARCHAR(50) NOT NULL COMMENT '通道ID',
    channel_name VARCHAR(100) COMMENT '通道名称',
    rtsp_url VARCHAR(500) COMMENT 'RTSP流地址',
    status ENUM('CREATED', 'STARTING', 'RUNNING', 'PAUSING', 'PAUSED', 'RESUMING', 'STOPPING', 'STOPPED', 'FAILED', 'ERROR') DEFAULT 'CREATED' COMMENT '任务状态',
    vlm_job_id VARCHAR(50) COMMENT 'VLM微服务Job ID',
    config JSON COMMENT '任务配置参数',
    error_message TEXT COMMENT '错误信息',
    last_active_time TIMESTAMP COMMENT '最后活跃时间',
    last_status_sync TIMESTAMP COMMENT '最后状态同步时间',
    created_by VARCHAR(50) COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (analysis_card_id) REFERENCES wvp_analysis_card(id),
    INDEX idx_device_channel (device_id, channel_id),
    INDEX idx_status (status),
    INDEX idx_vlm_job_id (vlm_job_id),
    INDEX idx_last_status_sync (last_status_sync),
    INDEX idx_created_at (created_at),
    UNIQUE KEY uk_device_channel_card (device_id, channel_id, analysis_card_id)
);

-- 分析告警表
DROP TABLE IF EXISTS wvp_analysis_alarm;
CREATE TABLE IF NOT EXISTS wvp_analysis_alarm (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL COMMENT '任务ID',
    device_id VARCHAR(50) NOT NULL COMMENT '设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    channel_id VARCHAR(50) NOT NULL COMMENT '通道ID',
    channel_name VARCHAR(100) COMMENT '通道名称',
    analysis_type VARCHAR(50) COMMENT '分析类型',
    description TEXT COMMENT '告警描述',
    snapshot_path VARCHAR(500) COMMENT '快照图片路径',
    alarm_time TIMESTAMP NOT NULL COMMENT '告警时间',
    event_start_time TIMESTAMP COMMENT '事件开始时间',
    event_end_time TIMESTAMP COMMENT '事件结束时间',
    event_time_range VARCHAR(50) COMMENT '相对时间范围',
    video_window_info JSON COMMENT '视频窗口时间信息',
    status ENUM('pending', 'processing', 'resolved', 'ignored') DEFAULT 'pending' COMMENT '处理状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (task_id) REFERENCES wvp_analysis_task(id),
    INDEX idx_task_time (task_id, alarm_time),
    INDEX idx_device_time (device_id, alarm_time),
    INDEX idx_status (status),
    INDEX idx_alarm_time (alarm_time),
    INDEX idx_created_at (created_at)
);

-- 插入示例分析卡片数据
INSERT INTO wvp_analysis_card (id, title, description, icon, tags, enabled, prompt, model_type, analysis_config, created_by) VALUES
('fire-emergency-detection', '消防应急事件检测', '实时监测区域内是否存在消防应急事件，包括火焰、烟雾、爆炸等异常现象', '/icons/fire.png', '["智慧消防", "公共安全", "安全生产"]', TRUE,
'实时监测视频区域的消防应急事件，包装抽烟、爆炸、火焰等异常现象。',
'SYSU-FireVED-v2', '{"confidence_threshold": 0.8, "detection_interval": 2}', 'admin'),

('illegal-fire-detection', '违规用火检测', '实时监测火灾高风险区域内的违规用火行为，包括抽烟、纵火、违规动火作业等', '/icons/person.png', '["智慧消防", "安全生产"]', TRUE,
'实时监测区域的违规用火行为，包装抽烟、纵火、违规动火作业等。',
'SYSU-FireVED-v2', '{"confidence_threshold": 0.75, "detection_interval": 3}', 'admin'),

('fire-lane-occupation', '消防通道占用检测', '实时监测消防通道、安全出口区域的违规占用行为，包括杂物堆积、车辆违停等', '/icons/vehicle.png', '["智慧消防", "公共安全"]', TRUE,
'实时监测消防通道、安全出口区域的违规占用行为，包括杂物堆积、车辆违停等。',
'SYSU-FireVED-v2', '{"confidence_threshold": 0.7, "detection_interval": 5}', 'admin');