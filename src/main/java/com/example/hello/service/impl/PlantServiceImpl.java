package com.example.hello.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.hello.entity.Plant;
import com.example.hello.mapper.PlantMapper;
import com.example.hello.service.PlantService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 植物服务实现类
 * 提供植物信息的CRUD操作实现
 */
@Service
public class PlantServiceImpl extends ServiceImpl<PlantMapper, Plant> implements PlantService {
    /**
     * 根据ID获取植物信息
     * @param id 植物ID
     * @return 植物信息
     */
    @Override
    public Plant getPlantById(Long id) {
        return baseMapper.selectById(id);
    }

    /**
     * 保存植物信息
     * @param plant 植物实体
     * @return 是否保存成功
     */
    @Override
    public boolean savePlant(Plant plant) {
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        if (plant.getCreateTime() == null) {
            plant.setCreateTime(now);
        }
        plant.setUpdateTime(now);
        return saveOrUpdate(plant);
    }
    
    /**
     * 获取最近的植物识别记录
     * @param limit 限制返回记录数量
     * @return 植物记录列表
     */
    @Override
    public List<Plant> getRecentPlants(int limit) {
        // 创建查询条件，按创建时间降序排序
        LambdaQueryWrapper<Plant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Plant::getCreateTime);
        // 限制返回记录数量
        queryWrapper.last("LIMIT " + limit);
        // 执行查询并返回结果
        return baseMapper.selectList(queryWrapper);
    }
}