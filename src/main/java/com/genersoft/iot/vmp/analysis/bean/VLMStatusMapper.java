package com.genersoft.iot.vmp.analysis.bean;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * VLM服务状态到WVP任务状态的映射工具类
 * 简化为1:1映射，只支持5个核心状态
 * @author Claude
 */
@Slf4j
public class VLMStatusMapper {

    /**
     * VLM状态到WVP状态的映射表
     * 直接1:1映射，无复杂转换逻辑
     */
    private static final Map<String, TaskStatus> VLM_TO_WVP_STATUS_MAP = new HashMap<>();

    static {
        // 直接映射VLM的5个核心状态
        VLM_TO_WVP_STATUS_MAP.put("created", TaskStatus.CREATED);
        VLM_TO_WVP_STATUS_MAP.put("CREATED", TaskStatus.CREATED);
        
        VLM_TO_WVP_STATUS_MAP.put("running", TaskStatus.RUNNING);
        VLM_TO_WVP_STATUS_MAP.put("RUNNING", TaskStatus.RUNNING);
        
        VLM_TO_WVP_STATUS_MAP.put("paused", TaskStatus.PAUSED);
        VLM_TO_WVP_STATUS_MAP.put("PAUSED", TaskStatus.PAUSED);
        
        VLM_TO_WVP_STATUS_MAP.put("failed", TaskStatus.FAILED);
        VLM_TO_WVP_STATUS_MAP.put("FAILED", TaskStatus.FAILED);
        
        VLM_TO_WVP_STATUS_MAP.put("cancelled", TaskStatus.CANCELLED);
        VLM_TO_WVP_STATUS_MAP.put("CANCELLED", TaskStatus.CANCELLED);
        
        // 兼容美式拼写
        VLM_TO_WVP_STATUS_MAP.put("canceled", TaskStatus.CANCELLED);
        VLM_TO_WVP_STATUS_MAP.put("CANCELED", TaskStatus.CANCELLED);
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
            log.warn("未知的VLM状态: {}, 无法映射到WVP状态", vlmStatus);
            return null;
        }
        
        return mappedStatus;
    }

    /**
     * 判断VLM状态是否表示作业正在运行
     * @param vlmStatus VLM服务返回的状态字符串
     * @return true如果表示正在运行
     */
    public static boolean isVLMStatusRunning(String vlmStatus) {
        TaskStatus status = mapVLMStatusToTaskStatus(vlmStatus);
        return status == TaskStatus.RUNNING;
    }

    /**
     * 判断VLM状态是否表示作业已创建但未启动
     * @param vlmStatus VLM服务返回的状态字符串
     * @return true如果表示创建但未启动
     */
    public static boolean isVLMStatusCreated(String vlmStatus) {
        TaskStatus status = mapVLMStatusToTaskStatus(vlmStatus);
        return status == TaskStatus.CREATED;
    }

    /**
     * 判断VLM状态是否表示作业已终止
     * @param vlmStatus VLM服务返回的状态字符串
     * @return true如果表示已终止
     */
    public static boolean isVLMStatusTerminated(String vlmStatus) {
        TaskStatus status = mapVLMStatusToTaskStatus(vlmStatus);
        return status != null && status.isTerminated();
    }

    /**
     * 获取所有支持的VLM状态字符串
     * @return VLM状态字符串集合
     */
    public static String[] getSupportedVLMStatuses() {
        return VLM_TO_WVP_STATUS_MAP.keySet().toArray(new String[0]);
    }
}