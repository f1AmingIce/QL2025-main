package com.example.hello.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("plant_info")
public class Plant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    //private String scientificName;
    //private String family;
   // private String genus;
   // private String description;
    private String imageUrl;
    //private String growthEnvironment;
   // private String usage;
    private Float recognitionAccuracy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}