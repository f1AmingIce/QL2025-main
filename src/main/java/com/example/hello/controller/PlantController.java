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
     * 优化后的流程：先通过向量相似度查找，若不存在则调用大模型识别
     * @param file 上传的植物图片文件
     * @return 识别结果响应，只包含植物名称
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

            // 调用优化后的植物识别服务处理图片
            // 该服务会先通过向量相似度查找，若不存在则调用大模型识别
            Plant plant = plantRecognitionService.recognizePlant(file);
            
            // 设置图片URL到植物对象并保存
            if (plant != null) {
                plant.setImageUrl(imageUrl);
                plantService.savePlant(plant);
                
                // 创建简化的响应DTO对象，只包含植物名称
                PlantResponseDTO responseDTO = new PlantResponseDTO();
                responseDTO.setName(plant.getName());
                
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
     * @return 历史识别记录列表，只包含植物名称
     */
    @GetMapping("/history")
    public ResponseEntity<List<PlantResponseDTO>> getPlantHistory(@RequestParam(value = "limit", defaultValue = "10") int limit) {
        try {
            // 获取最近的植物识别记录
            List<Plant> plants = plantService.getRecentPlants(limit);
            List<PlantResponseDTO> responseDTOs = new ArrayList<>();
            
            // 转换为响应DTO，只包含植物名称
            for (Plant plant : plants) {
                PlantResponseDTO dto = new PlantResponseDTO();
                dto.setName(plant.getName());
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
     * @return 植物详情，只包含植物名称
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlantResponseDTO> getPlantById(@PathVariable("id") Long id) {
        try {
            // 根据ID获取植物信息
            Plant plant = plantService.getPlantById(id);
            
            if (plant != null) {
                // 转换为响应DTO，只包含植物名称
                PlantResponseDTO dto = new PlantResponseDTO();
                dto.setName(plant.getName());
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