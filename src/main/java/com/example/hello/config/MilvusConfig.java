package com.example.hello.config;

import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus向量数据库配置类
 * 配置Milvus客户端连接参数
 */
@Configuration
public class MilvusConfig {

    @Value("${milvus.host:localhost}")
    private String host;

    @Value("${milvus.port:19530}")
    private int port;

    @Value("${milvus.collection-name:plant_collection}")
    private String collectionName;

    @Value("${milvus.dimension:1536}")
    private int dimension;

    /**
     * 创建Milvus客户端实例
     * @return 配置好的MilvusClient
     */
    @Bean
    public MilvusClient milvusClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .build();
        return new MilvusServiceClient(connectParam);
    }

    public String getCollectionName() {
        return collectionName;
    }

    public int getDimension() {
        return dimension;
    }
}