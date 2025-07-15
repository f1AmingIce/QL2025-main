package com.example.hello.service.impl;

import com.example.hello.entity.Plant;
import com.example.hello.entity.PlantVector;
import com.example.hello.service.PlantRecognitionService;
import com.example.hello.service.PlantService;
import com.example.hello.service.PlantVectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;

/**
 * 植物识别服务实现类
 * 负责调用LLM API进行植物识别、处理识别结果并与数据库交互
 */
@Service
public class PlantRecognitionServiceImpl implements PlantRecognitionService {

    // 注入HTTP客户端，用于调用外部API
    @Autowired
    private RestTemplate restTemplate;

    // 注入植物服务，用于数据库操作
    @Autowired
    private PlantService plantService;
    
    // 注入植物向量服务，用于向量数据库操作
    @Autowired
    private PlantVectorService plantVectorService;

    // 从配置文件注入LLM API地址
    @Value("${llm.api-url}")
    private String llmApiUrl;

    // 从配置文件注入LLM API密钥
    @Value("${llm.api-key}")
    private String llmApiKey;

    // 从配置文件注入Chroma向量数据库地址
    @Value("${chroma.server-url}")
    private String chromaServerUrl;
    
    // 相似度阈值，用于判断向量相似度是否足够高
    private static final float SIMILARITY_THRESHOLD = 0.8f;

    /**
     * 识别植物图片并返回植物信息
     * 优化后的流程：先通过向量相似度查找，若不存在则调用大模型识别
     * @param file 上传的植物图片文件
     * @return 识别出的植物信息，未识别则返回null
     */
    @Override
    public Plant recognizePlant(MultipartFile file) {
        try {
            // 1. 首先尝试通过向量相似度查找已有的植物
            Plant similarPlant = findSimilarPlantByVector(file);
            if (similarPlant != null) {
                // 如果找到相似植物，直接返回
                return similarPlant;
            }
            
            // 2. 如果没有找到相似植物，则调用大模型进行识别
            // 将图片文件转换为Base64编码字符串
            byte[] imageBytes = file.getBytes();
            String base64Image = Base64Utils.encodeToString(imageBytes);

            // 创建HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            // 设置请求内容类型为JSON
            headers.setContentType(MediaType.APPLICATION_JSON);
            // 添加认证头信息
            headers.set("Authorization", "Bearer " + llmApiKey);

            // 创建请求体参数
            Map<String, Object> requestBody = new HashMap<>();
            // 添加Base64编码的图片数据
            requestBody.put("image", base64Image);
            // 设置返回结果数量
            requestBody.put("top_k", 1);

            // 创建HTTP请求实体
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            try {
                // 调用LLM API进行植物识别
                ResponseEntity<Map> response = restTemplate.postForEntity(llmApiUrl, request, Map.class);

                // 检查响应状态是否为成功且响应体不为空
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    // 获取响应体内容
                    Map<String, Object> responseBody = response.getBody();
                    // 提取识别结果数据
                    Object resultObj = responseBody.get("result");
                    
                    // 处理不同的响应格式
                    if (resultObj instanceof Map) {
                        Map<String, Object> result = (Map<String, Object>) resultObj;
                        // 创建植物实体对象
                        Plant plant = new Plant();
                        // 设置植物名称
                        if (result.containsKey("name")) {
                            plant.setName((String) result.get("name"));
                        } else {
                            // 如果没有name字段，尝试其他可能的字段名
                            plant.setName("未知植物");
                        }
                        
                        // 安全处理识别置信度值转换
                        Object confidenceObj = result.get("confidence");
                        if (confidenceObj != null) {
                            try {
                                // 将置信度转换为Float类型
                                plant.setRecognitionAccuracy(Float.parseFloat(confidenceObj.toString()));
                            } catch (NumberFormatException e) {
                                // 转换失败时设置默认值
                                plant.setRecognitionAccuracy(0.0f);
                            }
                        } else {
                            // 置信度不存在时设置默认值
                            plant.setRecognitionAccuracy(0.0f);
                        }
                        
                        // 设置图片URL（此处使用原始文件名作为示例）
                        plant.setImageUrl(file.getOriginalFilename());

                        // 先保存植物信息到数据库，获取自动生成的ID
                        plantService.savePlant(plant);
                        
                        // 使用保存后的植物ID存储向量信息
                        storePlantVector(plant, file);
                        // 返回识别到的植物信息
                        return plant;
                    } else if (resultObj instanceof List && !((List<?>) resultObj).isEmpty()) {
                        // 如果结果是列表，取第一个元素
                        Object firstResult = ((List<?>) resultObj).get(0);
                        if (firstResult instanceof Map) {
                            Map<String, Object> result = (Map<String, Object>) firstResult;
                            // 创建植物实体对象
                            Plant plant = new Plant();
                            // 设置植物名称
                            if (result.containsKey("name")) {
                                plant.setName((String) result.get("name"));
                            } else {
                                plant.setName("未知植物");
                            }
                            
                            // 安全处理识别置信度值转换
                            Object confidenceObj = result.get("confidence");
                            if (confidenceObj != null) {
                                try {
                                    plant.setRecognitionAccuracy(Float.parseFloat(confidenceObj.toString()));
                                } catch (NumberFormatException e) {
                                    plant.setRecognitionAccuracy(0.0f);
                                }
                            } else {
                                plant.setRecognitionAccuracy(0.0f);
                            }
                            
                            plant.setImageUrl(file.getOriginalFilename());
                            plantService.savePlant(plant);
                            storePlantVector(plant, file);
                            return plant;
                        }
                    }
                }
            } catch (Exception e) {
                // 捕获API调用异常
                e.printStackTrace();
                // 创建一个默认植物对象，表示识别失败
                Plant plant = new Plant();
                plant.setName("识别失败");
                plant.setRecognitionAccuracy(0.0f);
                plant.setImageUrl(file.getOriginalFilename());
                return plant;
            }
        } catch (IOException e) {
            // 打印异常堆栈信息
            e.printStackTrace();
        }
        // 识别失败返回带有默认名称的植物对象
        Plant plant = new Plant();
        plant.setName("识别失败");
        plant.setRecognitionAccuracy(0.0f);
        try {
            plant.setImageUrl(file.getOriginalFilename());
        } catch (Exception ex) {
            plant.setImageUrl("");
        }
        return plant;
    }

    /**
     * 将植物信息和图像向量存储到Chroma向量数据库
     * @param plant 植物实体对象
     * @param file 植物图片文件
     * @return 存储成功返回true，失败返回false
     */
    @Override
    public boolean storePlantVector(Plant plant, MultipartFile file) {
        try {
            // 创建HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            // 设置请求内容类型为JSON
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 创建向量存储请求参数
            Map<String, Object> requestBody = new HashMap<>();
            // 使用植物ID作为向量唯一标识
            String vectorId = "plant_" + plant.getId();
            requestBody.put("ids", new String[]{vectorId});
            // 生成并添加图像向量
            float[] imageEmbedding = generateImageEmbedding(file);
            requestBody.put("embeddings", new float[][]{imageEmbedding});
            // 添加植物元数据
            requestBody.put("metadatas", new Map[]{Map.of(
                "name", plant.getName(),
                "plant_id", plant.getId().toString()
            )});

            // 创建HTTP请求实体
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // 构建Chroma API完整URL，处理斜杠拼接问题
            String chromaUrl = chromaServerUrl + (chromaServerUrl.endsWith("/") ? "" : "/") + "api/v1/collections/plant_collection/add";
            // 调用Chroma API存储向量
            ResponseEntity<Map> response = restTemplate.postForEntity(
                chromaUrl,
                request,
                Map.class
            );

            // 如果存储成功，同时保存向量信息到数据库
            if (response.getStatusCode() == HttpStatus.OK) {
                PlantVector plantVector = new PlantVector();
                plantVector.setPlantId(plant.getId());
                plantVector.setVectorId(vectorId);
                plantVector.setSimilarityThreshold(SIMILARITY_THRESHOLD);
                plantVectorService.savePlantVector(plantVector);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            // 打印异常堆栈信息
            e.printStackTrace();
            // 存储失败返回false
            return false;
        }
    }

    /**
     * 生成图像向量
     * @param file 图像文件
     * @return 生成的图像向量
     * @throws IOException 文件读取异常
     */
    private float[] generateImageEmbedding(MultipartFile file) throws IOException {
        try {
            // 将图片文件转换为Base64编码字符串
            byte[] imageBytes = file.getBytes();
            String base64Image = Base64Utils.encodeToString(imageBytes);

            // 创建HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + llmApiKey);

            // 创建请求体参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image", base64Image);
            requestBody.put("model", "image-embedding-model"); // 指定使用的模型

            // 创建HTTP请求实体
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 调用图像向量生成API
            String embeddingApiUrl = llmApiUrl.replace("/plant-recognition", "/image-embedding");
            ResponseEntity<Map> response = restTemplate.postForEntity(embeddingApiUrl, request, Map.class);

            // 检查响应状态是否为成功且响应体不为空
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 获取响应体内容
                Map<String, Object> responseBody = response.getBody();
                // 提取向量数据
                Object embeddingObj = responseBody.get("embedding");
                
                if (embeddingObj instanceof Object[]) {
                    Object[] embeddingArray = (Object[]) embeddingObj;
                    float[] embedding = new float[embeddingArray.length];
                    
                    // 将Object数组转换为float数组
                    for (int i = 0; i < embeddingArray.length; i++) {
                        if (embeddingArray[i] instanceof Number) {
                            embedding[i] = ((Number) embeddingArray[i]).floatValue();
                        }
                    }
                    
                    return embedding;
                }
            }
            
            // 如果API调用失败或返回格式不正确，返回默认向量
            return new float[512];
        } catch (Exception e) {
            e.printStackTrace();
            // 异常情况下返回默认向量
            return new float[512];
        }
    }
    /**
     * 通过图片向量查找相似植物
     * @param file 植物图片文件
     * @return 找到的相似植物，如果没有找到则返回null
     */
    @Override
    public Plant findSimilarPlantByVector(MultipartFile file) {
        try {
            // 生成图片向量
            float[] imageEmbedding = generateImageEmbedding(file);
            
            // 创建HTTP请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // 创建请求体参数
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query_embeddings", new float[][]{imageEmbedding});
            requestBody.put("n_results", 1); // 只返回最相似的一个结果
            
            // 创建HTTP请求实体
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // 构建Chroma API完整URL，用于查询相似向量
            String chromaUrl = chromaServerUrl + (chromaServerUrl.endsWith("/") ? "" : "/") + "api/v1/collections/plant_collection/query";
            
            // 调用Chroma API查询相似向量
            ResponseEntity<Map> response = restTemplate.postForEntity(chromaUrl, request, Map.class);
            
            // 检查响应状态是否为成功且响应体不为空
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                // 提取相似度数据
                List<List<Double>> distances = (List<List<Double>>) responseBody.get("distances");
                // 提取向量ID数据
                List<List<String>> ids = (List<List<String>>) responseBody.get("ids");
                // 提取元数据
                List<List<Map<String, Object>>> metadatas = (List<List<Map<String, Object>>>) responseBody.get("metadatas");
                
                // 检查是否有结果返回
                if (distances != null && !distances.isEmpty() && 
                    distances.get(0) != null && !distances.get(0).isEmpty() &&
                    ids != null && !ids.isEmpty() && 
                    ids.get(0) != null && !ids.get(0).isEmpty()) {
                    
                    // 获取第一个结果的相似度
                    double similarity = 1.0 - distances.get(0).get(0); // Chroma返回的是距离，需要转换为相似度
                    // 获取第一个结果的向量ID
                    String vectorId = ids.get(0).get(0);
                    
                    // 如果相似度高于阈值，则认为找到了相似植物
                    if (similarity >= SIMILARITY_THRESHOLD) {
                        // 从元数据中获取植物ID
                        String plantId = (String) metadatas.get(0).get(0).get("plant_id");
                        if (plantId != null) {
                            // 根据植物ID查询植物信息
                            return plantService.getPlantById(Long.parseLong(plantId));
                        } else {
                            // 如果元数据中没有植物ID，则尝试从向量ID中提取
                            if (vectorId.startsWith("plant_")) {
                                String idStr = vectorId.substring(6); // 去掉"plant_"前缀
                                try {
                                    Long id = Long.parseLong(idStr);
                                    return plantService.getPlantById(id);
                                } catch (NumberFormatException e) {
                                    // 转换失败，忽略
                                }
                            }
                        }
                    }
                }
            }
            
            // 没有找到相似植物或相似度不够高
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 保存已识别的植物信息到数据库
     * @param plant 植物实体对象
     */
    @Override
    public void savePlant(Plant plant) {
        // 调用植物服务保存植物信息
        plantService.savePlant(plant);
    }
}