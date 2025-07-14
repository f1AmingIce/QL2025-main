package com.example.hello.service;

import com.example.hello.entity.Plant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 植物识别服务测试类
 */
@SpringBootTest
public class PlantRecognitionServiceTest {

    @Autowired
    private PlantRecognitionService plantRecognitionService;

    /**
     * 测试植物识别功能
     * 注意：此测试需要有效的API密钥和测试图片
     */
    @Test
    public void testRecognizePlant() throws IOException {
        // 测试图片路径，需要替换为实际存在的图片路径
        String imagePath = "src/test/resources/test-plant.jpg";
        File imageFile = new File(imagePath);
        
        // 如果测试图片存在，则进行测试
        if (imageFile.exists()) {
            // 创建模拟的MultipartFile
            MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test-plant.jpg",
                "image/jpeg",
                new FileInputStream(imageFile)
            );
            
            // 调用识别服务
            Plant plant = plantRecognitionService.recognizePlant(multipartFile);
            
            // 验证识别结果不为空
            assertNotNull(plant, "植物识别结果不应为空");
            assertNotNull(plant.getName(), "植物名称不应为空");
            System.out.println("识别结果：" + plant.getName() + ", 准确度：" + plant.getRecognitionAccuracy());
        } else {
            System.out.println("测试图片不存在，跳过测试");
        }
    }
}