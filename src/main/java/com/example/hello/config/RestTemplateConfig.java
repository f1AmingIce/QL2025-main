package com.example.hello.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 * 配置HTTP请求超时时间，确保调用第三方接口的可靠性
 */
@Configuration
public class RestTemplateConfig {
    /**
     * 创建RestTemplate实例并配置超时时间
     * @return 配置好的RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // 连接超时时间：5秒
        requestFactory.setConnectTimeout(5000);
        // 读取超时时间：10秒
        requestFactory.setReadTimeout(10000);
        return new RestTemplate(requestFactory);
    }
}