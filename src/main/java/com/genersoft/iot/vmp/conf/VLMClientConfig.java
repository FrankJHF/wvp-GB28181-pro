package com.genersoft.iot.vmp.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

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
     * 用于VLM微服务HTTP通信
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory());
        return restTemplate;
    }

    /**
     * 配置HTTP请求工厂
     * 设置连接超时和读取超时
     */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(timeout);
        return factory;
    }
}