package com.example.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.hello.entity.PlantVector;

/**
 * 植物向量服务接口
 */
public interface PlantVectorService extends IService<PlantVector> {
    
    /**
     * 根据植物ID获取向量信息
     * @param plantId 植物ID
     * @return 植物向量信息
     */
    PlantVector getByPlantId(Long plantId);
    
    /**
     * 根据向量ID获取向量信息
     * @param vectorId 向量ID
     * @return 植物向量信息
     */
    PlantVector getByVectorId(String vectorId);
    
    /**
     * 保存植物向量信息
     * @param plantVector 植物向量实体
     * @return 是否保存成功
     */
    boolean savePlantVector(PlantVector plantVector);
}