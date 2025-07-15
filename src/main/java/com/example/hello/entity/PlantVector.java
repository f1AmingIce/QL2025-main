package com.example.hello.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 植物向量实体类
 * 用于存储植物图片的向量信息
 */
@Data
@TableName("plant_vectors")
public class PlantVector {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 关联的植物ID
     */
    private Long plantId;
    
    /**
     * Chroma中的向量ID
     */
    private String vectorId;
    
    /**
     * 相似度阈值，默认0.8
     */
    private Float similarityThreshold = 0.8f;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}