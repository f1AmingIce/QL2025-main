package com.example.hello.service;

import java.util.List;

/**
 * Milvus向量数据库服务接口
 * 提供向量存储、检索等操作
 */
public interface MilvusVectorService {
    
    /**
     * 初始化向量集合
     * 如果集合不存在则创建，存在则加载到内存
     * @return 是否成功
     */
    boolean initCollection();
    
    /**
     * 插入向量数据
     * @param vectorId 向量ID
     * @param vector 向量数据
     * @param metadata 元数据，如植物ID、名称等
     * @return 是否成功
     */
    boolean insertVector(String vectorId, float[] vector, java.util.Map<String, Object> metadata);
    
    /**
     * 通过向量相似度搜索
     * @param vector 查询向量
     * @param topK 返回结果数量
     * @param similarityThreshold 相似度阈值
     * @return 搜索结果，包含向量ID、相似度和元数据
     */
    List<SearchResult> searchByVector(float[] vector, int topK, float similarityThreshold);
    
    /**
     * 删除向量
     * @param vectorId 向量ID
     * @return 是否成功
     */
    boolean deleteVector(String vectorId);
    
    /**
     * 搜索结果类
     * 包含向量ID、相似度和元数据
     */
    class SearchResult {
        private String vectorId;
        private float similarity;
        private java.util.Map<String, Object> metadata;
        
        // 植物ID字段名称
        private static final String PLANT_ID_FIELD = "plant_id";
        // 植物名称字段名称
        private static final String PLANT_NAME_FIELD = "plant_name";
        // 相似度阈值字段名称
        private static final String SIMILARITY_THRESHOLD_FIELD = "similarity_threshold";
        // 图片URL字段名称
        private static final String IMAGE_URL_FIELD = "image_url";
        // 识别准确度字段名称
        private static final String RECOGNITION_ACCURACY_FIELD = "recognition_accuracy";
        // 创建时间字段名称
        private static final String CREATE_TIME_FIELD = "create_time";
        // 更新时间字段名称
        private static final String UPDATE_TIME_FIELD = "update_time";
        
        public SearchResult(String vectorId, float similarity, java.util.Map<String, Object> metadata) {
            this.vectorId = vectorId;
            this.similarity = similarity;
            this.metadata = metadata;
        }
        
        public String getVectorId() {
            return vectorId;
        }
        
        public float getSimilarity() {
            return similarity;
        }
        
        public java.util.Map<String, Object> getMetadata() {
            return metadata;
        }
        
        public Long getPlantId() {
            return metadata != null && metadata.containsKey(PLANT_ID_FIELD) ? 
                    ((Number) metadata.get(PLANT_ID_FIELD)).longValue() : null;
        }
        
        public String getPlantName() {
            return metadata != null ? (String) metadata.get(PLANT_NAME_FIELD) : null;
        }
        
        public Float getSimilarityThreshold() {
            return metadata != null && metadata.containsKey(SIMILARITY_THRESHOLD_FIELD) ? 
                    ((Number) metadata.get(SIMILARITY_THRESHOLD_FIELD)).floatValue() : null;
        }
        
        public String getImageUrl() {
            return metadata != null ? (String) metadata.get(IMAGE_URL_FIELD) : null;
        }
        
        public Float getRecognitionAccuracy() {
            return metadata != null && metadata.containsKey(RECOGNITION_ACCURACY_FIELD) ? 
                    ((Number) metadata.get(RECOGNITION_ACCURACY_FIELD)).floatValue() : null;
        }
        
        public String getCreateTime() {
            return metadata != null ? (String) metadata.get(CREATE_TIME_FIELD) : null;
        }
        
        public String getUpdateTime() {
            return metadata != null ? (String) metadata.get(UPDATE_TIME_FIELD) : null;
        }
    }
}