-- 智能分析测试数据库初始化脚本
-- 基于MySQL语法，H2兼容模式

-- 创建分析卡片表
CREATE TABLE IF NOT EXISTS wvp_analysis_card (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '卡片标题',
    description TEXT COMMENT '卡片描述',
    icon VARCHAR(50) COMMENT '图标名称',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    prompt TEXT NOT NULL COMMENT '分析提示词',
    model_type VARCHAR(100) NOT NULL COMMENT '使用的模型类型',
    tags TEXT COMMENT '标签列表，JSON格式',
    analysis_config TEXT COMMENT '分析配置，JSON格式',
    created_by VARCHAR(50) NOT NULL COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_enabled (enabled),
    INDEX idx_model_type (model_type),
    INDEX idx_created_by (created_by)
) COMMENT='智能分析卡片表';

-- 创建分析任务表
CREATE TABLE IF NOT EXISTS wvp_analysis_task (
    id VARCHAR(50) PRIMARY KEY,
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    analysis_card_id VARCHAR(50) NOT NULL COMMENT '关联的分析卡片ID',
    device_id VARCHAR(50) NOT NULL COMMENT '设备ID',
    device_name VARCHAR(200) COMMENT '设备名称',
    channel_id VARCHAR(50) NOT NULL COMMENT '通道ID', 
    channel_name VARCHAR(200) COMMENT '通道名称',
    rtsp_url VARCHAR(500) NOT NULL COMMENT 'RTSP流地址',
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED' COMMENT '任务状态',
    vlm_job_id VARCHAR(100) COMMENT 'VLM作业ID',
    error_message TEXT COMMENT '错误信息',
    last_active_time TIMESTAMP COMMENT '最后活跃时间',
    last_status_sync TIMESTAMP COMMENT '最后状态同步时间',
    created_by VARCHAR(50) NOT NULL COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (analysis_card_id) REFERENCES wvp_analysis_card(id) ON DELETE CASCADE,
    INDEX idx_device_channel (device_id, channel_id),
    INDEX idx_status (status),
    INDEX idx_card_id (analysis_card_id),
    INDEX idx_created_by (created_by),
    INDEX idx_vlm_job_id (vlm_job_id)
) COMMENT='智能分析任务表';

-- 创建分析告警表
CREATE TABLE IF NOT EXISTS wvp_analysis_alarm (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL COMMENT '关联的任务ID',
    device_id VARCHAR(50) NOT NULL COMMENT '设备ID',
    channel_id VARCHAR(50) NOT NULL COMMENT '通道ID',
    event_description TEXT COMMENT '事件描述',
    emergency_exist BOOLEAN DEFAULT FALSE COMMENT '是否存在紧急情况',
    snapshot_base64 LONGTEXT COMMENT 'Base64编码的快照图片',
    snapshot_url VARCHAR(500) COMMENT '快照图片URL',
    event_start_time TIMESTAMP COMMENT '事件开始时间',
    event_end_time TIMESTAMP COMMENT '事件结束时间',
    processed BOOLEAN DEFAULT FALSE COMMENT '是否已处理',
    processed_by VARCHAR(50) COMMENT '处理人',
    processed_at TIMESTAMP COMMENT '处理时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (task_id) REFERENCES wvp_analysis_task(id) ON DELETE CASCADE,
    INDEX idx_task_id (task_id),
    INDEX idx_device_id (device_id),
    INDEX idx_emergency (emergency_exist),
    INDEX idx_processed (processed),
    INDEX idx_event_time (event_start_time, event_end_time)
) COMMENT='智能分析告警表';