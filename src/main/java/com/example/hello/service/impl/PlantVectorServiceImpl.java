package com.example.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.hello.entity.PlantVector;
import com.example.hello.mapper.PlantVectorMapper;
import com.example.hello.service.PlantVectorService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 植物向量服务实现类
 */
@Service
public class PlantVectorServiceImpl extends ServiceImpl<PlantVectorMapper, PlantVector> implements PlantVectorService {

    /**
     * 根据植物ID获取向量信息
     * @param plantId 植物ID
     * @return 植物向量信息
     */
    @Override
    public PlantVector getByPlantId(Long plantId) {
        LambdaQueryWrapper<PlantVector> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlantVector::getPlantId, plantId);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 根据向量ID获取向量信息
     * @param vectorId 向量ID
     * @return 植物向量信息
     */
    @Override
    public PlantVector getByVectorId(String vectorId) {
        LambdaQueryWrapper<PlantVector> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PlantVector::getVectorId, vectorId);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 保存植物向量信息
     * @param plantVector 植物向量实体
     * @return 是否保存成功
     */
    @Override
    public boolean savePlantVector(PlantVector plantVector) {
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        if (plantVector.getCreateTime() == null) {
            plantVector.setCreateTime(now);
        }
        plantVector.setUpdateTime(now);
        return saveOrUpdate(plantVector);
    }
}