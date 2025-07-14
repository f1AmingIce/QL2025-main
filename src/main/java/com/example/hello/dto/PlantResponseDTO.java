package com.example.hello.dto;

import lombok.Data;

/**
 * 植物识别响应数据传输对象
 * 用于向前端返回植物识别结果
 */
@Data
public class PlantResponseDTO {
    /**
     * 植物名称
     */
    private String name;
    
    /**
     * 识别准确度
     */
    private Float recognitionAccuracy;
    
    /**
     * 植物图片URL
     */
    private String imageUrl;
    
    /**
     * 识别时间
     */
    private String recognitionTime;
    
    /**
     * 植物ID
     */
    private Long plantId;
}