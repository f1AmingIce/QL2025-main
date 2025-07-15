package com.example.hello.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hello.entity.PlantVector;
import org.apache.ibatis.annotations.Mapper;

/**
 * 植物向量Mapper接口
 */
@Mapper
public interface PlantVectorMapper extends BaseMapper<PlantVector> {
}