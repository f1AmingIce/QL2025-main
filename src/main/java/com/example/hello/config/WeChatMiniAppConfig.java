package com.example.hello.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 微信小程序配置类
 * 用于注入application.properties中的微信小程序配置参数
 */
@Configuration
@ConfigurationProperties(prefix = "wechat.miniapp")
@Data
public class WeChatMiniAppConfig {
    /**
     * 微信小程序appid
     */
    private String appid;

    /**
     * 微信小程序secret
     */
    private String secret;
}