package com.example.hello.controller;

import com.example.hello.entity.Plant;
import com.example.hello.service.PlantRecognitionService;
import com.example.hello.service.PlantService;
import com.example.hello.dto.PlantResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/plant")
public class PlantController {

    // 注入植物识别服务
    @Autowired
    private PlantRecognitionService plantRecognitionService;
    
    // 注入植物服务
    @Autowired
    private PlantService plantService;

    // 从配置文件注入LLM API地址
    @Value("${llm.api-url}")
    private String llmApiUrl;

    // 从配置文件注入LLM API密钥
    @Value("${llm.api-key}")
    private String llmApiKey;

    /**
     * 植物识别接口
     * @param file 上传的植物图片文件
     * @return 识别结果响应
     */
    @PostMapping("/identify")
    public ResponseEntity<PlantResponseDTO> identifyPlant(@RequestParam("file") MultipartFile file) {
        // 检查文件是否为空
        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            // 生成唯一文件名，避免重复
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            // 构建文件保存路径（项目根目录下的uploads文件夹）
            String filePath = System.getProperty("user.dir") + File.separator + "uploads";
            // 创建文件对象
            File dest = new File(filePath + File.separator + fileName);
            // 如果父目录不存在则创建
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            // 保存上传的文件到目标路径
            file.transferTo(dest);
            
            // 构建可访问的图片URL
            String imageUrl = "/uploads/" + fileName;

            // 调用植物识别服务处理图片
            Plant plant = plantRecognitionService.recognizePlant(file);
            
            // 设置图片URL到植物对象
            if (plant != null) {
                plant.setImageUrl(imageUrl);
                // 保存更新后的植物信息
                plantService.savePlant(plant);
            }
            // 检查识别结果是否不为空
            if (plant != null) {
                // 创建响应DTO对象
                PlantResponseDTO responseDTO = new PlantResponseDTO();
                // 只设置植物名称到DTO
                responseDTO.setName(plant.getName());
                // 其他信息暂时注释掉
                // responseDTO.setRecognitionAccuracy(plant.getRecognitionAccuracy());
                // responseDTO.setImageUrl(plant.getImageUrl());
                // responseDTO.setPlantId(plant.getId());
                // responseDTO.setRecognitionTime(java.time.LocalDateTime.now().toString());
                // 返回成功响应和DTO数据
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                // 识别失败，返回未找到响应
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IOException e) {
            // 打印异常堆栈信息
            e.printStackTrace();
            // 返回服务器内部错误响应
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取历史识别记录
     * @param limit 限制返回记录数量，默认为10
     * @return 历史识别记录列表
     */
    @GetMapping("/history")
    public ResponseEntity<List<PlantResponseDTO>> getPlantHistory(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        try {
            // 获取最近的植物识别记录
            List<Plant> plants = plantService.getRecentPlants(limit);
            List<PlantResponseDTO> responseDTOs = new ArrayList<>();
            
            // 转换为响应DTO
            for (Plant plant : plants) {
                PlantResponseDTO dto = new PlantResponseDTO();
                dto.setName(plant.getName());
                dto.setRecognitionAccuracy(plant.getRecognitionAccuracy());
                dto.setImageUrl(plant.getImageUrl());
                dto.setPlantId(plant.getId());
                // 使用更新时间作为识别时间
                dto.setRecognitionTime(plant.getUpdateTime() != null ? 
                        plant.getUpdateTime().toString() : 
                        plant.getCreateTime().toString());
                responseDTOs.add(dto);
            }
            
            return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 根据ID获取植物详情
     * @param id 植物ID
     * @return 植物详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlantResponseDTO> getPlantById(@PathVariable("id") Long id) {
        try {
            // 根据ID获取植物信息
            Plant plant = plantService.getPlantById(id);
            
            if (plant != null) {
                // 转换为响应DTO
                PlantResponseDTO dto = new PlantResponseDTO();
                dto.setName(plant.getName());
                dto.setRecognitionAccuracy(plant.getRecognitionAccuracy());
                dto.setImageUrl(plant.getImageUrl());
                dto.setPlantId(plant.getId());
                dto.setRecognitionTime(plant.getUpdateTime() != null ? 
                        plant.getUpdateTime().toString() : 
                        plant.getCreateTime().toString());
                
                return new ResponseEntity<>(dto, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}