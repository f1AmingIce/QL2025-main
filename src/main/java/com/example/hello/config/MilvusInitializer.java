package com.example.hello.config;

import com.example.hello.service.MilvusVectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * Milvus初始化配置类
 * 在应用启动时初始化Milvus集合
 */
@Configuration
public class MilvusInitializer {

    private static final Logger logger = LoggerFactory.getLogger(MilvusInitializer.class);

    @Autowired
    private MilvusVectorService milvusVectorService;

    /**
     * 在Spring上下文刷新时初始化Milvus集合
     * @param event 上下文刷新事件
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("开始初始化Milvus集合...");
        boolean result = milvusVectorService.initCollection();
        if (result) {
            logger.info("Milvus集合初始化成功");
        } else {
            logger.error("Milvus集合初始化失败");
        }
    }
}