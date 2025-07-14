package com.example.hello.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.hello.entity.Plant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlantMapper extends BaseMapper<Plant> {
}