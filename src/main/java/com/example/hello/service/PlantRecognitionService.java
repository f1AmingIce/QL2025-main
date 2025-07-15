package com.example.hello.service;

import com.example.hello.entity.Plant;
import org.springframework.web.multipart.MultipartFile;

public interface PlantRecognitionService {
    /**
     * 识别植物并返回结果
     * 优化后的流程：先通过向量相似度查找，若不存在则调用大模型识别
     * @param file 植物图片文件
     * @return 识别到的植物信息
     */
    Plant recognizePlant(MultipartFile file);

    /**
     * 从图片生成向量并存储到Chroma
     * @param plant 植物信息
     * @param file 植物图片
     * @return 是否成功
     */
    boolean storePlantVector(Plant plant, MultipartFile file);
    
    /**
     * 通过图片向量查找相似植物
     * @param file 植物图片文件
     * @return 找到的相似植物，如果没有找到则返回null
     */
    Plant findSimilarPlantByVector(MultipartFile file);

    void savePlant(Plant plant);
}