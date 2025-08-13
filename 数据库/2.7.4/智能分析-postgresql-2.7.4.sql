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
    model_type VARCHAR(50) DEFAULT 'SYSU-FireVED-v2',
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
    status VARCHAR(20) DEFAULT 'CREATED' CHECK (status IN ('CREATED', 'RUNNING', 'PAUSED', 'FAILED', 'CANCELLED')),
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
INSERT INTO wvp_analysis_card (id, title, description, icon, tags, enabled, prompt, model_type, created_by) VALUES 
('fire-emergency-detection', '消防应急事件检测', '实时监测区域内是否存在消防应急事件，包括火焰、烟雾、爆炸等异常现象', '/icons/fire.png', '["智慧消防", "公共安全", "安全生产"]'::jsonb, TRUE,
'<video>\n请你分析视频片段，判断其中是否发生了与火灾或其他突发情况相关的应急事件。请指出各事件其在视频片段中的时间范围，并简要描述事件内容。若存在多个不同的事件，请按时间顺序输出多个json单元；当视频中发生新的、有意义的、与火灾相关的事件，或当前事件状态发生显著变化时，请开始一个新的事件段。事件段可以重叠。\n每个事件输出要求如下（严格遵循 JSON 格式）：\n{''event_time'': ''起始秒-结束秒'',  // 时间范围，单位为秒，保留一位小数\n''event_des'': ''事件简要描述'' ,\n''emergency_exist'': ''是'' 或 ''否'' // 是否发生应急事件,必须根据事件描述内容来回答是或否}\n注意事项：时间是相对于该视频片段的局部时间（即片段起点为 0s）；所有事件的范围交集要求覆盖全时段。请仅输出符合上述格式的 JSON，无需额外解释说明。有连续时间的相同事件请合并输出，切忌零碎。',
'SYSU-FireVED-v2', 'admin'),

('illegal-fire-detection', '违规用火检测', '实时监测火灾高风险区域内的违规用火行为，包括抽烟、纵火、违规动火作业等', '/icons/person.png', '["智慧消防", "安全生产"]'::jsonb, TRUE,
'<video>\n请分析视频中是否存在违规用火行为，包括抽烟、纵火、违规动火作业等。请详细描述发现的违规行为的类型、位置和时间范围。',
'SYSU-FireVED-v2', 'admin'),

('fire-lane-occupation', '消防通道占用检测', '实时监测消防通道、安全出口区域的违规占用行为，包括杂物堆积、车辆违停等', '/icons/vehicle.png', '["智慧消防", "公共安全"]'::jsonb, TRUE,
'<video>\n请分析视频中消防通道、安全出口区域是否存在违规占用行为，包括杂物堆积、车辆违停等。请详细描述占用物品的类型、位置和可能造成的安全隐患。',
'SYSU-FireVED-v2', 'admin');