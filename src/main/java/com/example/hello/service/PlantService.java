package com.example.hello.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.hello.entity.Plant;

import java.util.List;

/**
 * 植物服务接口
 * 提供植物信息的CRUD操作
 */
public interface PlantService extends IService<Plant> {
    /**
     * 根据ID获取植物信息
     * @param id 植物ID
     * @return 植物信息
     */
    Plant getPlantById(Long id);
    
    /**
     * 保存植物信息
     * @param plant 植物实体
     * @return 是否保存成功
     */
    boolean savePlant(Plant plant);
    
    /**
     * 获取最近的植物识别记录
     * @param limit 限制返回记录数量
     * @return 植物记录列表
     */
    List<Plant> getRecentPlants(int limit);
}