-- 测试数据初始化脚本
-- 用于在运行集成测试时提供基础测试数据

-- 清理现有测试数据
DELETE FROM wvp_analysis_alarm WHERE created_at < CURRENT_TIMESTAMP;
DELETE FROM wvp_analysis_task WHERE created_at < CURRENT_TIMESTAMP;
DELETE FROM wvp_analysis_card WHERE created_at < CURRENT_TIMESTAMP;

-- 插入测试分析卡片
INSERT INTO wvp_analysis_card (
    id, title, description, icon, enabled, prompt, model_type, 
    tags, analysis_config, created_by, created_at, updated_at
) VALUES 
('test-card-fire', '火灾检测卡片', '用于检测火灾和烟雾的智能分析卡片', 'fire', true,
 '请分析视频中是否发生火灾，包括明火、浓烟等现象', 'videollama3-fire-detection',
 '["火灾检测", "紧急事件", "安全监控"]',
 '{"inference_interval": 5, "sampling_fps": 5, "frame_buffer_size": 180, "max_new_tokens": 200}',
 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('test-card-intrusion', '入侵检测卡片', '用于检测非法入侵的智能分析卡片', 'security', true,
 '请分析视频中是否有人员非法入侵或可疑行为', 'videollama3-intrusion-detection', 
 '["入侵检测", "安全监控", "异常行为"]',
 '{"inference_interval": 10, "sampling_fps": 3, "frame_buffer_size": 120, "max_new_tokens": 150}',
 'system', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('test-card-disabled', '已禁用测试卡片', '用于测试禁用状态的分析卡片', 'warning', false,
 '这是一个测试用的禁用卡片', 'videollama3-test',
 '["测试", "禁用状态"]',
 '{"inference_interval": 30, "sampling_fps": 1}',
 'test-user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入测试分析任务
INSERT INTO wvp_analysis_task (
    id, task_name, analysis_card_id, device_id, device_name, channel_id, channel_name,
    rtsp_url, status, vlm_job_id, created_by, created_at, updated_at
) VALUES 
('test-task-001', '前门火灾监控任务', 'test-card-fire', 
 '34020000001320000001', '前门摄像头', '34020000001310000001', '主通道',
 'rtsp://admin:password@192.168.1.101:554/stream1', 'CREATED', 'vlm-job-001',
 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('test-task-002', '后门入侵检测任务', 'test-card-intrusion',
 '34020000001320000002', '后门摄像头', '34020000001310000002', '副通道', 
 'rtsp://admin:password@192.168.1.102:554/stream1', 'RUNNING', 'vlm-job-002',
 'admin', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('test-task-003', '停车场监控任务', 'test-card-fire',
 '34020000001320000003', '停车场摄像头', '34020000001310000003', '停车场通道',
 'rtsp://admin:password@192.168.1.103:554/stream1', 'PAUSED', 'vlm-job-003',
 'user1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入测试告警数据  
INSERT INTO wvp_analysis_alarm (
    id, task_id, device_id, channel_id, event_description, emergency_exist,
    snapshot_base64, event_start_time, event_end_time, processed, created_at, updated_at
) VALUES 
('test-alarm-001', 'test-task-001', '34020000001320000001', '34020000001310000001',
 '检测到明显的火焰和浓烟，疑似火灾事件，建议立即处理', true,
 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/test1',
 DATEADD('MINUTE', -30, CURRENT_TIMESTAMP), DATEADD('MINUTE', -25, CURRENT_TIMESTAMP), 
 false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('test-alarm-002', 'test-task-002', '34020000001320000002', '34020000001310000002', 
 '检测到有人员在非工作时间进入限制区域', true,
 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/test2',
 DATEADD('MINUTE', -60, CURRENT_TIMESTAMP), DATEADD('MINUTE', -58, CURRENT_TIMESTAMP),
 true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

('test-alarm-003', 'test-task-001', '34020000001320000001', '34020000001310000001',
 '检测到疑似烟雾，但火焰不明显，需要进一步确认', false, 
 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/test3',
 DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), DATEADD('MINUTE', -8, CURRENT_TIMESTAMP),
 false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);