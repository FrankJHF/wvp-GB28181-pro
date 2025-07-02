/*WVP-VLM 智能分析系统数据表 - MySQL修正版本*/

-- 删除已存在的表
DROP TABLE IF EXISTS wvp_analysis_results;
DROP TABLE IF EXISTS wvp_analysis_tasks;

-- 创建分析任务表
CREATE TABLE wvp_analysis_tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL,
    task_name VARCHAR(255) NOT NULL,
    device_id VARCHAR(50) NOT NULL,
    channel_id VARCHAR(50) NOT NULL,
    channel_name VARCHAR(255) NOT NULL,
    vlm_question TEXT NOT NULL,
    clip_duration INT DEFAULT 60 NOT NULL,
    status VARCHAR(20) DEFAULT 'STOPPED' NOT NULL,
    create_time VARCHAR(50) NOT NULL,
    update_time VARCHAR(50) NOT NULL,
    KEY idx_device_id (device_id),
    KEY idx_channel_id (channel_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time),
    UNIQUE KEY uk_analysis_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能分析任务表';

-- 创建分析结果表
CREATE TABLE wvp_analysis_results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(50) NOT NULL,
    result_time VARCHAR(50) NOT NULL,
    vlm_question TEXT NOT NULL,
    vlm_answer TEXT NOT NULL,
    is_alarm TINYINT(1) DEFAULT 0 NOT NULL,
    video_clip_path VARCHAR(500),
    key_frame_path VARCHAR(500),
    create_time VARCHAR(50) NOT NULL,
    KEY idx_task_id (task_id),
    KEY idx_result_time (result_time),
    KEY idx_is_alarm (is_alarm),
    KEY idx_task_result_time (task_id, result_time),
    KEY idx_alarm_result_time (is_alarm, result_time),
    FOREIGN KEY (task_id) REFERENCES wvp_analysis_tasks(task_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能分析结果表';

-- 插入示例分析任务（可选）
INSERT INTO wvp_analysis_tasks (
    task_id, task_name, device_id, channel_id, channel_name,
    vlm_question, clip_duration, status, create_time, update_time
) VALUES (
    'demo-task-001',
    '仓库安全监控演示',
    '34020000001320000001',
    '34020000001320000001',
    '仓库监控点1',
    '画面中是否有未授权的人员进入？请详细描述所看到的情况。',
    120,
    'STOPPED',
    NOW(),
    NOW()
) ON DUPLICATE KEY UPDATE task_name = task_name;

-- 验证表创建
SELECT 'Tables created successfully' AS status;
SHOW TABLES LIKE 'wvp_analysis%';