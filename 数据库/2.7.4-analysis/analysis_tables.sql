/*WVP-VLM 智能分析系统数据表*/

drop table IF EXISTS wvp_analysis_tasks;
create table IF NOT EXISTS wvp_analysis_tasks
(
    id           serial primary key,
    task_id      character varying(50) not null,
    task_name    character varying(255) not null,
    device_id    character varying(50) not null,
    channel_id   character varying(50) not null,
    channel_name character varying(255) not null,
    vlm_question text not null,
    clip_duration integer default 60 not null,
    status       character varying(20) default 'STOPPED' not null,
    create_time  character varying(50) not null,
    update_time  character varying(50) not null,
    index (device_id),
    index (channel_id),
    index (status),
    index (create_time),
    constraint uk_analysis_task_id unique (task_id)
);

drop table IF EXISTS wvp_analysis_results;
create table IF NOT EXISTS wvp_analysis_results
(
    id               serial primary key,
    task_id          character varying(50) not null,
    result_time      character varying(50) not null,
    vlm_question     text not null,
    vlm_answer       text not null,
    is_alarm         bool default false not null,
    video_clip_path  character varying(500),
    key_frame_path   character varying(500),
    create_time      character varying(50) not null,
    index (task_id),
    index (result_time),
    index (is_alarm),
    index (task_id, result_time),
    index (is_alarm, result_time)
);

-- 插入示例分析任务（可选）
INSERT INTO wvp_analysis_tasks (task_id, task_name, device_id, channel_id, channel_name, vlm_question, clip_duration, status, create_time, update_time)
VALUES ('demo-task-001', '仓库安全监控演示', '34020000001320000001', '34020000001320000001', '仓库监控点1', '画面中是否有未授权的人员进入？请详细描述所看到的情况。', 120, 'STOPPED', NOW(), NOW())
ON DUPLICATE KEY UPDATE task_name = task_name;