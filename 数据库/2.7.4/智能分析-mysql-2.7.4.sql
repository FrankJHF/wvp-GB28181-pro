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
    model_type VARCHAR(50) DEFAULT 'SYSU-FireVED-v2' COMMENT '模型类型',
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
    status ENUM('CREATED', 'RUNNING', 'PAUSED', 'FAILED', 'CANCELLED') DEFAULT 'CREATED' COMMENT '任务状态',
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
    description TEXT COMMENT '告警描述',
    snapshot_path VARCHAR(500) COMMENT '快照图片路径',
    snapshot_base64 LONGTEXT COMMENT '快照图片Base64数据',
    alarm_time TIMESTAMP NOT NULL COMMENT '告警时间',
    event_start_time TIMESTAMP COMMENT '事件开始时间',
    event_end_time TIMESTAMP COMMENT '事件结束时间',
    event_time_range VARCHAR(50) COMMENT '相对时间范围',
    video_window_info JSON COMMENT '视频窗口时间信息',
    status ENUM('PENDING', 'PROCESSING', 'RESOLVED', 'IGNORED') DEFAULT 'PENDING' COMMENT '处理状态',
    processed_at TIMESTAMP NULL COMMENT '处理时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (task_id) REFERENCES wvp_analysis_task(id),
    INDEX idx_task_time (task_id, alarm_time),
    INDEX idx_device_time (device_id, alarm_time),
    INDEX idx_status (status),
    INDEX idx_alarm_time (alarm_time),
    INDEX idx_created_at (created_at)
);

-- 插入示例分析卡片数据
INSERT INTO wvp_analysis_card (id, title, description, icon, tags, enabled, prompt, model_type, created_by) VALUES
('fire-emergency-detection', '消防应急事件检测', '实时监测区域内是否存在消防应急事件，包括火焰、烟雾、爆炸等异常现象', '/icons/fire.png', '["智慧消防", "公共安全", "安全生产"]', TRUE,
'<video>\n请你分析视频片段，判断其中是否发生了与火灾或其他突发情况相关的应急事件。请指出各事件其在视频片段中的时间范围，并简要描述事件内容。若存在多个不同的事件，请按时间顺序输出多个json单元；当视频中发生新的、有意义的、与火灾相关的事件，或当前事件状态发生显著变化时，请开始一个新的事件段。事件段可以重叠。\n每个事件输出要求如下（严格遵循 JSON 格式）：\n{\'event_time\': \'起始秒-结束秒\',  // 时间范围，单位为秒，保留一位小数\n\'event_des\': \'事件简要描述\' ,\n\'emergency_exist\': \'是\' 或 \'否\' // 是否发生应急事件,必须根据事件描述内容来回答是或否}\n注意事项：时间是相对于该视频片段的局部时间（即片段起点为 0s）；所有事件的范围交集要求覆盖全时段。请仅输出符合上述格式的 JSON，无需额外解释说明。有连续时间的相同事件请合并输出，切忌零碎。',
'SYSU-FireVED-v2', 'admin'),

('illegal-fire-detection', '违规用火检测', '实时监测火灾高风险区域内的违规用火行为，包括抽烟、纵火、违规动火作业等', '/icons/person.png', '["智慧消防", "安全生产"]', TRUE,
'<video>\n请分析视频中是否存在违规用火行为，包括抽烟、纵火、违规动火作业等。请详细描述发现的违规行为的类型、位置和时间范围。',
'SYSU-FireVED-v2', 'admin'),

('fire-lane-occupation', '消防通道占用检测', '实时监测消防通道、安全出口区域的违规占用行为，包括杂物堆积、车辆违停等', '/icons/vehicle.png', '["智慧消防", "公共安全"]', TRUE,
'<video>\n请分析视频中消防通道、安全出口区域是否存在违规占用行为，包括杂物堆积、车辆违停等。请详细描述占用物品的类型、位置和可能造成的安全隐患。',
'SYSU-FireVED-v2', 'admin');