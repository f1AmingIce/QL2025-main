package com.example.hello.service.impl;

import com.example.hello.config.MilvusConfig;
import com.example.hello.service.MilvusVectorService;
import io.milvus.client.MilvusClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import io.milvus.response.FieldDataWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Milvus向量数据库服务实现类
 * 提供向量存储、检索等操作
 */
@Service
public class MilvusVectorServiceImpl implements MilvusVectorService {

    private static final Logger logger = LoggerFactory.getLogger(MilvusVectorServiceImpl.class);
    
    @Autowired
    private MilvusClient milvusClient;
    
    @Autowired
    private MilvusConfig milvusConfig;
    
    // 向量字段名称
    private static final String VECTOR_FIELD = "vector";
    // ID字段名称
    private static final String ID_FIELD = "id";
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

    /**
     * 初始化向量集合
     * 如果集合不存在则创建，存在则加载到内存
     * @return 是否成功
     */
    @Override
    public boolean initCollection() {
        try {
            // 检查集合是否存在
            HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .build();
            R<Boolean> hasCollectionResponse = milvusClient.hasCollection(hasCollectionParam);
            
            if (hasCollectionResponse.getData()) {
                // 集合已存在，加载到内存
                LoadCollectionParam loadCollectionParam = LoadCollectionParam.newBuilder()
                        .withCollectionName(milvusConfig.getCollectionName())
                        .build();
                R<RpcStatus> loadResponse = milvusClient.loadCollection(loadCollectionParam);
                return loadResponse.getStatus() == R.Status.Success.getCode();
            } else {
                // 集合不存在，创建新集合
                // 定义字段
                List<FieldType> fieldTypes = new ArrayList<>();
                
                // ID字段，主键
                fieldTypes.add(FieldType.newBuilder()
                        .withName(ID_FIELD)
                        .withDataType(DataType.VarChar)
                        .withMaxLength(100)
                        .withPrimaryKey(true)
                        .withAutoID(false)
                        .build());
                
                // 向量字段
                fieldTypes.add(FieldType.newBuilder()
                        .withName(VECTOR_FIELD)
                        .withDataType(DataType.FloatVector)
                        .withDimension(milvusConfig.getDimension())
                        .build());
                
                // 植物ID字段
                fieldTypes.add(FieldType.newBuilder()
                        .withName(PLANT_ID_FIELD)
                        .withDataType(DataType.Int64)
                        .build());
                
                // 植物名称字段
                fieldTypes.add(FieldType.newBuilder()
                        .withName(PLANT_NAME_FIELD)
                        .withDataType(DataType.VarChar)
                        .withMaxLength(255)
                        .build());
                
                // 相似度阈值字段
                fieldTypes.add(FieldType.newBuilder()
                        .withName(SIMILARITY_THRESHOLD_FIELD)
                        .withDataType(DataType.Float)
                        .build());
                
                // 图片URL字段
                fieldTypes.add(FieldType.newBuilder()
                        .withName(IMAGE_URL_FIELD)
                        .withDataType(DataType.VarChar)
                        .withMaxLength(1024)
                        .build());
                
                // 识别准确度字段
                fieldTypes.add(FieldType.newBuilder()
                        .withName(RECOGNITION_ACCURACY_FIELD)
                        .withDataType(DataType.Float)
                        .build());
                
                // 创建时间字段 (存储为字符串)
                fieldTypes.add(FieldType.newBuilder()
                        .withName(CREATE_TIME_FIELD)
                        .withDataType(DataType.VarChar)
                        .withMaxLength(30)
                        .build());
                
                // 更新时间字段 (存储为字符串)
                fieldTypes.add(FieldType.newBuilder()
                        .withName(UPDATE_TIME_FIELD)
                        .withDataType(DataType.VarChar)
                        .withMaxLength(30)
                        .build());
                
                // 创建集合参数
                CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                        .withCollectionName(milvusConfig.getCollectionName())
                        .withDescription("植物向量集合")
                        .withFieldTypes(fieldTypes)
                        .build();
                
                // 创建集合
                R<RpcStatus> createResponse = milvusClient.createCollection(createCollectionParam);
                
                if (createResponse.getStatus() == R.Status.Success.getCode()) {
                    // 创建索引
                    IndexType indexType = IndexType.HNSW;
                    Map<String, String> indexParams = new HashMap<>();
                    indexParams.put("M", "16");
                    indexParams.put("efConstruction", "64");
                    
                    // 将Map转换为JSON字符串
                    String extraParamJson = String.format("{\"M\":\"%s\",\"efConstruction\":\"%s\"}", 
                            indexParams.get("M"), indexParams.get("efConstruction"));
                    
                    CreateIndexParam createIndexParam = CreateIndexParam.newBuilder()
                            .withCollectionName(milvusConfig.getCollectionName())
                            .withFieldName(VECTOR_FIELD)
                            .withIndexType(indexType)
                            .withMetricType(MetricType.IP)
                            .withExtraParam(extraParamJson)
                            .withSyncMode(Boolean.TRUE)
                            .build();
                    
                    R<RpcStatus> indexResponse = milvusClient.createIndex(createIndexParam);
                    
                    if (indexResponse.getStatus() == R.Status.Success.getCode()) {
                        // 加载集合到内存
                        LoadCollectionParam loadCollectionParam = LoadCollectionParam.newBuilder()
                                .withCollectionName(milvusConfig.getCollectionName())
                                .build();
                        R<RpcStatus> loadResponse = milvusClient.loadCollection(loadCollectionParam);
                        return loadResponse.getStatus() == R.Status.Success.getCode();
                    }
                }
                return false;
            }
        } catch (Exception e) {
            logger.error("初始化Milvus集合失败", e);
            return false;
        }
    }

    /**
     * 插入向量数据
     * @param vectorId 向量ID
     * @param vector 向量数据
     * @param metadata 元数据，如植物ID、名称等
     * @return 是否成功
     */
    @Override
    public boolean insertVector(String vectorId, float[] vector, Map<String, Object> metadata) {
        try {
            // 准备插入数据
            List<InsertParam.Field> fields = new ArrayList<>();
            
            // ID字段
            fields.add(new InsertParam.Field(ID_FIELD, Collections.singletonList(vectorId)));
            
            // 向量字段
            fields.add(new InsertParam.Field(VECTOR_FIELD, Collections.singletonList(vector)));
            
            // 植物ID字段
            if (metadata.containsKey(PLANT_ID_FIELD)) {
                fields.add(new InsertParam.Field(PLANT_ID_FIELD, 
                        Collections.singletonList(Long.parseLong(metadata.get(PLANT_ID_FIELD).toString()))));
            }
            
            // 植物名称字段
            if (metadata.containsKey(PLANT_NAME_FIELD)) {
                fields.add(new InsertParam.Field(PLANT_NAME_FIELD, 
                        Collections.singletonList(metadata.get(PLANT_NAME_FIELD).toString())));
            }
            
            // 相似度阈值字段
            if (metadata.containsKey(SIMILARITY_THRESHOLD_FIELD)) {
                fields.add(new InsertParam.Field(SIMILARITY_THRESHOLD_FIELD, 
                        Collections.singletonList(Float.parseFloat(metadata.get(SIMILARITY_THRESHOLD_FIELD).toString()))));
            }
            
            // 图片URL字段
            if (metadata.containsKey(IMAGE_URL_FIELD)) {
                fields.add(new InsertParam.Field(IMAGE_URL_FIELD, 
                        Collections.singletonList(metadata.get(IMAGE_URL_FIELD).toString())));
            }
            
            // 识别准确度字段
            if (metadata.containsKey(RECOGNITION_ACCURACY_FIELD)) {
                fields.add(new InsertParam.Field(RECOGNITION_ACCURACY_FIELD, 
                        Collections.singletonList(Float.parseFloat(metadata.get(RECOGNITION_ACCURACY_FIELD).toString()))));
            }
            
            // 创建时间字段
            if (metadata.containsKey(CREATE_TIME_FIELD)) {
                fields.add(new InsertParam.Field(CREATE_TIME_FIELD, 
                        Collections.singletonList(metadata.get(CREATE_TIME_FIELD).toString())));
            }
            
            // 更新时间字段
            if (metadata.containsKey(UPDATE_TIME_FIELD)) {
                fields.add(new InsertParam.Field(UPDATE_TIME_FIELD, 
                        Collections.singletonList(metadata.get(UPDATE_TIME_FIELD).toString())));
            }
            
            // 构建插入参数
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .withFields(fields)
                    .build();
            
            // 执行插入操作
            R<MutationResult> response = milvusClient.insert(insertParam);
            
            return response.getStatus() == R.Status.Success.getCode();
        } catch (Exception e) {
            logger.error("插入向量数据失败", e);
            return false;
        }
    }

    /**
     * 通过向量相似度搜索
     * @param vector 查询向量
     * @param topK 返回结果数量
     * @param similarityThreshold 相似度阈值
     * @return 搜索结果，包含向量ID、相似度和元数据
     */
    @Override
    public List<SearchResult> searchByVector(float[] vector, int topK, float similarityThreshold) {
        try {
            // 构建搜索参数
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                    .withMetricType(MetricType.IP)
                    .withOutFields(Arrays.asList(
                        PLANT_ID_FIELD, 
                        PLANT_NAME_FIELD, 
                        SIMILARITY_THRESHOLD_FIELD,
                        IMAGE_URL_FIELD,
                        RECOGNITION_ACCURACY_FIELD,
                        CREATE_TIME_FIELD,
                        UPDATE_TIME_FIELD
                    ))
                    .withTopK(topK)
                    .withVectors(Collections.singletonList(vector))
                    .withVectorFieldName(VECTOR_FIELD)
                    .build();
            
            // 执行搜索操作
            R<SearchResults> response = milvusClient.search(searchParam);
            
            if (response.getStatus() == R.Status.Success.getCode()) {
                SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData().getResults());
                
                // 处理搜索结果
                List<SearchResult> results = new ArrayList<>();
                List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);
                
                for (int i = 0; i < scores.size(); i++) {
                    SearchResultsWrapper.IDScore score = scores.get(i);
                    // 获取ID，根据API可能是String类型
                    String id = score.toString().split(":")[0].trim();
                    // Milvus的IP距离需要转换为相似度
                    float similarity = score.getScore();
                    
                    // 只返回相似度大于阈值的结果
                    if (similarity >= similarityThreshold) {
                        Map<String, Object> metadata = new HashMap<>();
                        
                        // 获取植物ID
                        FieldDataWrapper plantIdWrapper = wrapper.getFieldWrapper(PLANT_ID_FIELD);
                        if (plantIdWrapper != null) {
                            List<Long> plantIds = (List<Long>) plantIdWrapper.getFieldData().get(0);
                            metadata.put(PLANT_ID_FIELD, plantIds.get(i));
                        }
                        
                        // 获取植物名称
                        FieldDataWrapper plantNameWrapper = wrapper.getFieldWrapper(PLANT_NAME_FIELD);
                        if (plantNameWrapper != null) {
                            List<String> plantNames = (List<String>) plantNameWrapper.getFieldData().get(0);
                            metadata.put(PLANT_NAME_FIELD, plantNames.get(i));
                        }
                        
                        // 获取相似度阈值
                        FieldDataWrapper thresholdWrapper = wrapper.getFieldWrapper(SIMILARITY_THRESHOLD_FIELD);
                        if (thresholdWrapper != null) {
                            List<Float> thresholds = (List<Float>) thresholdWrapper.getFieldData().get(0);
                            metadata.put(SIMILARITY_THRESHOLD_FIELD, thresholds.get(i));
                        }
                        
                        // 获取图片URL
                        FieldDataWrapper imageUrlWrapper = wrapper.getFieldWrapper(IMAGE_URL_FIELD);
                        if (imageUrlWrapper != null) {
                            List<String> imageUrls = (List<String>) imageUrlWrapper.getFieldData().get(0);
                            metadata.put(IMAGE_URL_FIELD, imageUrls.get(i));
                        }
                        
                        // 获取识别准确度
                        FieldDataWrapper accuracyWrapper = wrapper.getFieldWrapper(RECOGNITION_ACCURACY_FIELD);
                        if (accuracyWrapper != null) {
                            List<Float> accuracies = (List<Float>) accuracyWrapper.getFieldData().get(0);
                            metadata.put(RECOGNITION_ACCURACY_FIELD, accuracies.get(i));
                        }
                        
                        // 获取创建时间
                        FieldDataWrapper createTimeWrapper = wrapper.getFieldWrapper(CREATE_TIME_FIELD);
                        if (createTimeWrapper != null) {
                            List<String> createTimes = (List<String>) createTimeWrapper.getFieldData().get(0);
                            metadata.put(CREATE_TIME_FIELD, createTimes.get(i));
                        }
                        
                        // 获取更新时间
                        FieldDataWrapper updateTimeWrapper = wrapper.getFieldWrapper(UPDATE_TIME_FIELD);
                        if (updateTimeWrapper != null) {
                            List<String> updateTimes = (List<String>) updateTimeWrapper.getFieldData().get(0);
                            metadata.put(UPDATE_TIME_FIELD, updateTimes.get(i));
                        }
                        
                        results.add(new MilvusVectorService.SearchResult(id, similarity, metadata));
                    }
                }
                
                return results;
            }
            
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("向量搜索失败", e);
            return Collections.emptyList();
        }
    }
    


    /**
     * 删除向量
     * @param vectorId 向量ID
     * @return 是否成功
     */
    @Override
    public boolean deleteVector(String vectorId) {
        try {
            // 构建删除表达式
            String expr = String.format("%s == \"%s\"", ID_FIELD, vectorId);
            
            // 构建删除参数
            DeleteParam deleteParam = DeleteParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .withExpr(expr)
                    .build();
            
            // 执行删除操作
            R<MutationResult> response = milvusClient.delete(deleteParam);
            
            return response.getStatus() == R.Status.Success.getCode();
        } catch (Exception e) {
            logger.error("删除向量失败", e);
            return false;
        }
    }
}