package com.genersoft.iot.vmp.analysis.service;

import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobRequest;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.VLMJobActionResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.JobStatusUpdateRequest;
import com.genersoft.iot.vmp.analysis.bean.dto.JobStatusResponse;
import com.genersoft.iot.vmp.analysis.bean.dto.JobCancelResponse;
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
     * 统一的作业状态更新接口
     * @param jobId 作业ID
     * @param statusRequest 状态更新请求
     * @return 作业操作响应
     */
    @Schema(description = "统一的作业状态更新接口")
    VLMJobActionResponse updateJobStatus(String jobId, JobStatusUpdateRequest statusRequest) throws ServiceException;

    /**
     * 取消VLM作业
     * @param jobId 作业ID
     * @return 作业取消响应
     */
    @Schema(description = "取消VLM作业")
    JobCancelResponse cancelJob(String jobId) throws ServiceException;

    /**
     * 查询VLM作业详细状态
     * @param jobId 作业ID
     * @return VLM作业状态响应
     */
    @Schema(description = "查询VLM作业详细状态")
    JobStatusResponse getJobStatus(String jobId) throws ServiceException;

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
        private String timestamp;

        public VLMHealthResponse() {}

        public VLMHealthResponse(String status, String message) {
            this.status = status;
            this.message = message;
            this.timestamp = java.time.Instant.now().toString();
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

        public boolean isHealthy() {
            return "healthy".equals(status);
        }
    }
}