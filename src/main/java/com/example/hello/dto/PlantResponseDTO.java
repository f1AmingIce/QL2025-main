package com.example.hello.dto;

import lombok.Data;

/**
 * 植物识别响应数据传输对象
 * 优化后只返回植物名称
 */
@Data
public class PlantResponseDTO {
    /**
     * 植物名称
     */
    private String name;
}