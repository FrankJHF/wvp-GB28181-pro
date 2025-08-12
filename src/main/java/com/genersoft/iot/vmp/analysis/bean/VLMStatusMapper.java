package com.genersoft.iot.vmp.analysis.bean;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * VLM服务状态到WVP任务状态的映射工具类
 * @author Claude
 */
@Slf4j
public class VLMStatusMapper {

    /**
     * VLM状态到WVP状态的映射表
     */
    private static final Map<String, TaskStatus> VLM_TO_WVP_STATUS_MAP = new HashMap<>();

    static {
        // VLM创建状态映射
        VLM_TO_WVP_STATUS_MAP.put("created", TaskStatus.CREATED);
        VLM_TO_WVP_STATUS_MAP.put("CREATED", TaskStatus.CREATED);
        
        // VLM运行状态映射
        VLM_TO_WVP_STATUS_MAP.put("running", TaskStatus.RUNNING);
        VLM_TO_WVP_STATUS_MAP.put("RUNNING", TaskStatus.RUNNING);
        VLM_TO_WVP_STATUS_MAP.put("active", TaskStatus.RUNNING);
        VLM_TO_WVP_STATUS_MAP.put("ACTIVE", TaskStatus.RUNNING);
        
        // VLM过渡状态映射
        VLM_TO_WVP_STATUS_MAP.put("starting", TaskStatus.STARTING);
        VLM_TO_WVP_STATUS_MAP.put("STARTING", TaskStatus.STARTING);
        VLM_TO_WVP_STATUS_MAP.put("pausing", TaskStatus.PAUSING);
        VLM_TO_WVP_STATUS_MAP.put("PAUSING", TaskStatus.PAUSING);
        VLM_TO_WVP_STATUS_MAP.put("resuming", TaskStatus.RESUMING);
        VLM_TO_WVP_STATUS_MAP.put("RESUMING", TaskStatus.RESUMING);
        VLM_TO_WVP_STATUS_MAP.put("stopping", TaskStatus.STOPPING);
        VLM_TO_WVP_STATUS_MAP.put("STOPPING", TaskStatus.STOPPING);
        
        // VLM暂停状态映射
        VLM_TO_WVP_STATUS_MAP.put("paused", TaskStatus.PAUSED);
        VLM_TO_WVP_STATUS_MAP.put("PAUSED", TaskStatus.PAUSED);
        VLM_TO_WVP_STATUS_MAP.put("suspended", TaskStatus.PAUSED);
        VLM_TO_WVP_STATUS_MAP.put("SUSPENDED", TaskStatus.PAUSED);
        
        // VLM终止状态映射
        VLM_TO_WVP_STATUS_MAP.put("stopped", TaskStatus.STOPPED);
        VLM_TO_WVP_STATUS_MAP.put("STOPPED", TaskStatus.STOPPED);
        VLM_TO_WVP_STATUS_MAP.put("cancelled", TaskStatus.CANCELLED);
        VLM_TO_WVP_STATUS_MAP.put("CANCELLED", TaskStatus.CANCELLED);
        VLM_TO_WVP_STATUS_MAP.put("canceled", TaskStatus.CANCELLED); // 美式拼写兼容
        VLM_TO_WVP_STATUS_MAP.put("CANCELED", TaskStatus.CANCELLED);
        
        // VLM错误状态映射
        VLM_TO_WVP_STATUS_MAP.put("failed", TaskStatus.FAILED);
        VLM_TO_WVP_STATUS_MAP.put("FAILED", TaskStatus.FAILED);
        VLM_TO_WVP_STATUS_MAP.put("error", TaskStatus.ERROR);
        VLM_TO_WVP_STATUS_MAP.put("ERROR", TaskStatus.ERROR);
        VLM_TO_WVP_STATUS_MAP.put("exception", TaskStatus.ERROR);
        VLM_TO_WVP_STATUS_MAP.put("EXCEPTION", TaskStatus.ERROR);
    }

    /**
     * 将VLM服务的状态映射到WVP任务状态
     * @param vlmStatus VLM服务返回的状态字符串
     * @return 对应的WVP任务状态，如果无法映射则返回null
     */
    public static TaskStatus mapVLMStatusToTaskStatus(String vlmStatus) {
        if (vlmStatus == null || vlmStatus.trim().isEmpty()) {
            return null;
        }
        
        TaskStatus mappedStatus = VLM_TO_WVP_STATUS_MAP.get(vlmStatus.trim());
        
        if (mappedStatus == null) {
            log.warn("未知的VLM状态: {}, 使用默认状态CREATED", vlmStatus);
            return TaskStatus.CREATED;
        }
        
        return mappedStatus;
    }

    /**
     * 判断VLM状态是否表示作业已启动运行
     * @param vlmStatus VLM服务返回的状态字符串
     * @return true如果表示已启动运行
     */
    public static boolean isVLMStatusRunning(String vlmStatus) {
        TaskStatus status = mapVLMStatusToTaskStatus(vlmStatus);
        return status == TaskStatus.RUNNING || status == TaskStatus.STARTING;
    }

    /**
     * 判断VLM状态是否表示作业创建成功但未启动
     * @param vlmStatus VLM服务返回的状态字符串
     * @return true如果表示创建但未启动
     */
    public static boolean isVLMStatusCreated(String vlmStatus) {
        TaskStatus status = mapVLMStatusToTaskStatus(vlmStatus);
        return status == TaskStatus.CREATED;
    }

    /**
     * 获取所有支持的VLM状态字符串
     * @return VLM状态字符串集合
     */
    public static String[] getSupportedVLMStatuses() {
        return VLM_TO_WVP_STATUS_MAP.keySet().toArray(new String[0]);
    }
}