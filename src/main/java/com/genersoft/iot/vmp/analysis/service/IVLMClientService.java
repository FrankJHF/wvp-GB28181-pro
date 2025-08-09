package com.genersoft.iot.vmp.analysis.service;

import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobRequest;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobActionResponse;
import com.genersoft.iot.vmp.conf.exception.ServiceException;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * VLM微服务客户端接口
 * @author Claude
 */
public interface IVLMClientService {

    /**
     * 创建VLM分析作业
     * @param request 作业请求参数
     * @param autoStart 是否自动启动
     * @return VLM作业响应
     */
    @Schema(description = "创建VLM分析作业")
    VLMJobResponse createJob(VLMJobRequest request, boolean autoStart) throws ServiceException;

    /**
     * 启动VLM作业
     * @param jobId 作业ID
     * @param forceRestart 是否强制重启
     * @return 作业操作响应
     */
    @Schema(description = "启动VLM作业")
    VLMJobActionResponse startJob(String jobId, boolean forceRestart) throws ServiceException;

    /**
     * 暂停VLM作业
     * @param jobId 作业ID
     * @return 作业操作响应
     */
    @Schema(description = "暂停VLM作业")
    VLMJobActionResponse pauseJob(String jobId) throws ServiceException;

    /**
     * 恢复VLM作业
     * @param jobId 作业ID
     * @return 作业操作响应
     */
    @Schema(description = "恢复VLM作业")
    VLMJobActionResponse resumeJob(String jobId) throws ServiceException;

    /**
     * 停止VLM作业
     * @param jobId 作业ID
     * @return 作业操作响应
     */
    @Schema(description = "停止VLM作业")
    VLMJobActionResponse stopJob(String jobId) throws ServiceException;

    /**
     * 查询VLM作业状态
     * @param jobId 作业ID
     * @return VLM作业响应
     */
    @Schema(description = "查询VLM作业状态")
    VLMJobResponse getJobStatus(String jobId) throws ServiceException;

    /**
     * 检查VLM微服务健康状态
     * @return 健康检查响应
     */
    @Schema(description = "检查VLM微服务健康状态")
    VLMHealthResponse checkHealth();

    /**
     * VLM健康检查响应
     */
    @Schema(description = "VLM健康检查响应")
    class VLMHealthResponse {
        private String status;
        private String message;
        private String version;
        private long timestamp;

        public VLMHealthResponse() {}

        public VLMHealthResponse(String status, String message) {
            this.status = status;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

        public boolean isHealthy() {
            return "healthy".equals(status);
        }
    }
}