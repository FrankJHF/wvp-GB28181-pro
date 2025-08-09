package com.genersoft.iot.vmp.conf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * 智能分析静态资源配置
 * 配置快照图片的HTTP访问映射
 */
@Configuration
@ConfigurationProperties(prefix = "analysis.snapshot")
public class AnalysisResourceConfig implements WebMvcConfigurer {

    /**
     * 存储路径
     */
    private String storagePath = System.getProperty("user.home") + "/wvp-analysis/snapshots";

    /**
     * URL访问前缀
     */
    private String urlPrefix = "/api/analysis/snapshots";

    /**
     * 文件保留天数
     */
    private int retentionDays = 30;

    /**
     * 是否启用文件清理
     */
    private boolean cleanupEnabled = true;

    /**
     * 文件清理任务执行时间
     */
    private String cleanupCron = "0 0 2 * * ?";

    @PostConstruct
    public void init() {
        // 确保存储目录存在
        File storageDir = new File(storagePath);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置快照图片的静态资源映射
        String resourcePath = "file:" + storagePath + "/";
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations(resourcePath)
                .setCachePeriod(3600); // 缓存1小时
    }

    // Getters and Setters
    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    public int getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
        this.retentionDays = retentionDays;
    }

    public boolean isCleanupEnabled() {
        return cleanupEnabled;
    }

    public void setCleanupEnabled(boolean cleanupEnabled) {
        this.cleanupEnabled = cleanupEnabled;
    }

    public String getCleanupCron() {
        return cleanupCron;
    }

    public void setCleanupCron(String cleanupCron) {
        this.cleanupCron = cleanupCron;
    }
}