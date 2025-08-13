package com.genersoft.iot.vmp.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.config.RequestConfig;

/**
 * VLM客户端配置类
 * 配置RestTemplate用于VLM微服务通信
 * 
 * @author Claude
 */
@Configuration
public class VLMClientConfig {

    @Value("${vlm.service.timeout:30000}")
    private int timeout;

    @Value("${vlm.service.connect-timeout:5000}")
    private int connectTimeout;

    /**
     * 配置RestTemplate Bean
     * 用于VLM微服务HTTP通信，支持PATCH方法
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        return restTemplate;
    }

    /**
     * 配置HTTP请求工厂
     * 使用HttpComponentsClientHttpRequestFactory支持PATCH方法
     */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        
        // 配置HttpClient
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(timeout)
                .setConnectionRequestTimeout(connectTimeout)
                .build();
                
        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
                
        factory.setHttpClient(httpClient);
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(timeout);
        
        return factory;
    }
}