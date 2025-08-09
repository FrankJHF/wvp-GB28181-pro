/*智能分析模块-PostgreSQL 2.7.4*/

-- 分析卡片表
DROP TABLE IF EXISTS wvp_analysis_card;
CREATE TABLE IF NOT EXISTS wvp_analysis_card (
    id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    icon VARCHAR(200),
    tags JSONB,
    enabled BOOLEAN DEFAULT TRUE,
    prompt TEXT NOT NULL,
    model_type VARCHAR(50) DEFAULT 'videollama3',
    analysis_config JSONB,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE wvp_analysis_card IS '分析卡片表';
COMMENT ON COLUMN wvp_analysis_card.id IS '主键ID';
COMMENT ON COLUMN wvp_analysis_card.title IS '卡片标题';
COMMENT ON COLUMN wvp_analysis_card.description IS '卡片描述';
COMMENT ON COLUMN wvp_analysis_card.icon IS '卡片图标URL';
COMMENT ON COLUMN wvp_analysis_card.tags IS '标签数组';
COMMENT ON COLUMN wvp_analysis_card.enabled IS '是否启用';
COMMENT ON COLUMN wvp_analysis_card.prompt IS '分析提示词';
COMMENT ON COLUMN wvp_analysis_card.model_type IS '模型类型';
COMMENT ON COLUMN wvp_analysis_card.analysis_config IS 'VLM分析配置参数';
COMMENT ON COLUMN wvp_analysis_card.created_by IS '创建人';
COMMENT ON COLUMN wvp_analysis_card.created_at IS '创建时间';
COMMENT ON COLUMN wvp_analysis_card.updated_at IS '更新时间';

CREATE INDEX idx_analysis_card_enabled ON wvp_analysis_card (enabled);
CREATE INDEX idx_analysis_card_created_by ON wvp_analysis_card (created_by);
CREATE INDEX idx_analysis_card_created_at ON wvp_analysis_card (created_at);

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为分析卡片表创建更新时间触发器
CREATE TRIGGER update_analysis_card_updated_at 
    BEFORE UPDATE ON wvp_analysis_card 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 分析任务表
DROP TABLE IF EXISTS wvp_analysis_task;
CREATE TABLE IF NOT EXISTS wvp_analysis_task (
    id VARCHAR(50) PRIMARY KEY,
    task_name VARCHAR(100) NOT NULL,
    analysis_card_id VARCHAR(50) NOT NULL,
    device_id VARCHAR(50) NOT NULL,
    device_name VARCHAR(100),
    channel_id VARCHAR(50) NOT NULL,
    channel_name VARCHAR(100),
    rtsp_url VARCHAR(500),
    status VARCHAR(20) DEFAULT 'created' CHECK (status IN ('created', 'starting', 'running', 'pausing', 'paused', 'resuming', 'stopping', 'stopped', 'failed', 'error')),
    vlm_job_id VARCHAR(50),
    config JSONB,
    error_message TEXT,
    last_active_time TIMESTAMP,
    last_status_sync TIMESTAMP,
    created_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE wvp_analysis_task IS '分析任务表';
COMMENT ON COLUMN wvp_analysis_task.id IS '主键ID';
COMMENT ON COLUMN wvp_analysis_task.task_name IS '任务名称';
COMMENT ON COLUMN wvp_analysis_task.analysis_card_id IS '分析卡片ID';
COMMENT ON COLUMN wvp_analysis_task.device_id IS '设备ID';
COMMENT ON COLUMN wvp_analysis_task.device_name IS '设备名称';
COMMENT ON COLUMN wvp_analysis_task.channel_id IS '通道ID';
COMMENT ON COLUMN wvp_analysis_task.channel_name IS '通道名称';
COMMENT ON COLUMN wvp_analysis_task.rtsp_url IS 'RTSP流地址';
COMMENT ON COLUMN wvp_analysis_task.status IS '任务状态';
COMMENT ON COLUMN wvp_analysis_task.vlm_job_id IS 'VLM微服务Job ID';
COMMENT ON COLUMN wvp_analysis_task.config IS '任务配置参数';
COMMENT ON COLUMN wvp_analysis_task.error_message IS '错误信息';
COMMENT ON COLUMN wvp_analysis_task.last_active_time IS '最后活跃时间';
COMMENT ON COLUMN wvp_analysis_task.last_status_sync IS '最后状态同步时间';
COMMENT ON COLUMN wvp_analysis_task.created_by IS '创建人';
COMMENT ON COLUMN wvp_analysis_task.created_at IS '创建时间';
COMMENT ON COLUMN wvp_analysis_task.updated_at IS '更新时间';

ALTER TABLE wvp_analysis_task ADD CONSTRAINT fk_task_analysis_card 
    FOREIGN KEY (analysis_card_id) REFERENCES wvp_analysis_card(id);

CREATE INDEX idx_analysis_task_device_channel ON wvp_analysis_task (device_id, channel_id);
CREATE INDEX idx_analysis_task_status ON wvp_analysis_task (status);
CREATE INDEX idx_analysis_task_vlm_job_id ON wvp_analysis_task (vlm_job_id);
CREATE INDEX idx_analysis_task_last_status_sync ON wvp_analysis_task (last_status_sync);
CREATE INDEX idx_analysis_task_created_at ON wvp_analysis_task (created_at);
CREATE UNIQUE INDEX uk_analysis_task_device_channel_card ON wvp_analysis_task (device_id, channel_id, analysis_card_id);

-- 为分析任务表创建更新时间触发器
CREATE TRIGGER update_analysis_task_updated_at 
    BEFORE UPDATE ON wvp_analysis_task 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 分析告警表
DROP TABLE IF EXISTS wvp_analysis_alarm;
CREATE TABLE IF NOT EXISTS wvp_analysis_alarm (
    id VARCHAR(50) PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL,
    device_id VARCHAR(50) NOT NULL,
    device_name VARCHAR(100),
    channel_id VARCHAR(50) NOT NULL,
    channel_name VARCHAR(100),
    analysis_type VARCHAR(50),
    description TEXT,
    snapshot_path VARCHAR(500),
    alarm_time TIMESTAMP NOT NULL,
    event_start_time TIMESTAMP,
    event_end_time TIMESTAMP,
    event_time_range VARCHAR(50),
    video_window_info JSONB,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN ('pending', 'processing', 'resolved', 'ignored')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE wvp_analysis_alarm IS '分析告警表';
COMMENT ON COLUMN wvp_analysis_alarm.id IS '主键ID';
COMMENT ON COLUMN wvp_analysis_alarm.task_id IS '任务ID';
COMMENT ON COLUMN wvp_analysis_alarm.device_id IS '设备ID';
COMMENT ON COLUMN wvp_analysis_alarm.device_name IS '设备名称';
COMMENT ON COLUMN wvp_analysis_alarm.channel_id IS '通道ID';
COMMENT ON COLUMN wvp_analysis_alarm.channel_name IS '通道名称';
COMMENT ON COLUMN wvp_analysis_alarm.analysis_type IS '分析类型';
COMMENT ON COLUMN wvp_analysis_alarm.description IS '告警描述';
COMMENT ON COLUMN wvp_analysis_alarm.snapshot_path IS '快照图片路径';
COMMENT ON COLUMN wvp_analysis_alarm.alarm_time IS '告警时间';
COMMENT ON COLUMN wvp_analysis_alarm.event_start_time IS '事件开始时间';
COMMENT ON COLUMN wvp_analysis_alarm.event_end_time IS '事件结束时间';
COMMENT ON COLUMN wvp_analysis_alarm.event_time_range IS '相对时间范围';
COMMENT ON COLUMN wvp_analysis_alarm.video_window_info IS '视频窗口时间信息';
COMMENT ON COLUMN wvp_analysis_alarm.status IS '处理状态';
COMMENT ON COLUMN wvp_analysis_alarm.created_at IS '创建时间';

ALTER TABLE wvp_analysis_alarm ADD CONSTRAINT fk_alarm_analysis_task 
    FOREIGN KEY (task_id) REFERENCES wvp_analysis_task(id);

CREATE INDEX idx_analysis_alarm_task_time ON wvp_analysis_alarm (task_id, alarm_time);
CREATE INDEX idx_analysis_alarm_device_time ON wvp_analysis_alarm (device_id, alarm_time);
CREATE INDEX idx_analysis_alarm_status ON wvp_analysis_alarm (status);
CREATE INDEX idx_analysis_alarm_alarm_time ON wvp_analysis_alarm (alarm_time);
CREATE INDEX idx_analysis_alarm_created_at ON wvp_analysis_alarm (created_at);

-- 插入示例分析卡片数据
INSERT INTO wvp_analysis_card (id, title, description, icon, tags, enabled, prompt, model_type, analysis_config, created_by) VALUES 
('fire-detection', '火灾检测', '识别视频中的火焰和烟雾，及时发现火灾隐患', '/icons/fire.png', '["安全", "火灾", "预警"]'::jsonb, TRUE, 
'请仔细分析这个视频片段，检测是否存在火焰、烟雾或其他火灾迹象。如果发现异常情况，请详细描述火灾的位置、严重程度和可能的危险性。', 
'videollama3', '{"confidence_threshold": 0.8, "detection_interval": 2}'::jsonb, 'admin'),

('person-intrusion', '人员入侵检测', '检测禁区内的人员入侵行为', '/icons/person.png', '["安全", "入侵", "监控"]'::jsonb, TRUE,
'分析视频中是否有人员进入禁止区域。请关注人员的行为特征，如果发现有人员在不应该出现的区域活动，请详细描述入侵者的位置、数量和行为。',
'videollama3', '{"confidence_threshold": 0.75, "detection_interval": 3}'::jsonb, 'admin'),

('vehicle-detection', '车辆违规检测', '检测违规停车、逆行等交通违法行为', '/icons/vehicle.png', '["交通", "违规", "监控"]'::jsonb, TRUE,
'分析视频中的车辆行为，检测是否存在违规停车、逆行、超速或其他交通违法行为。如果发现违规行为，请描述车辆类型、违规类型和位置。',
'videollama3', '{"confidence_threshold": 0.7, "detection_interval": 5}'::jsonb, 'admin');