# 植物识别后端程序

## 项目简介

本项目是一个基于SpringBoot、MyBatis-Plus和SpringMVC开发的植物识别后端程序，可以接收来自微信小程序的请求，实现植物图片的上传、识别和结果返回功能。项目采用RESTful API设计风格，具有良好的可读性和可维护性。

## 技术栈

- **框架**: Spring Boot 3.3.2
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL
- **向量数据库**: Chroma
- **大模型集成**: 支持调用LLM API进行植物识别

## 功能特点

1. **植物图片上传**: 支持从微信小程序上传植物图片
2. **植物识别**: 调用大模型API或向量数据库进行植物识别
3. **历史记录查询**: 提供历史识别记录查询功能
4. **植物详情查看**: 支持查看植物详细信息

## 接口说明

### 1. 植物识别接口

- **URL**: `/api/plant/identify`
- **方法**: POST
- **参数**: 
  - `file`: 植物图片文件（MultipartFile）
- **返回**: 
  ```json
  {
    "name": "植物名称",
    "recognitionAccuracy": 0.95,
    "imageUrl": "/uploads/xxx.jpg",
    "recognitionTime": "2023-07-15T10:30:00",
    "plantId": 1
  }
  ```

### 2. 历史记录查询接口

- **URL**: `/api/plant/history`
- **方法**: GET
- **参数**: 
  - `limit`: 返回记录数量，默认10条（可选）
- **返回**: 
  ```json
  [
    {
      "name": "植物1",
      "recognitionAccuracy": 0.95,
      "imageUrl": "/uploads/xxx.jpg",
      "recognitionTime": "2023-07-15T10:30:00",
      "plantId": 1
    },
    {
      "name": "植物2",
      "recognitionAccuracy": 0.88,
      "imageUrl": "/uploads/yyy.jpg",
      "recognitionTime": "2023-07-15T11:20:00",
      "plantId": 2
    }
  ]
  ```

### 3. 植物详情查询接口

- **URL**: `/api/plant/{id}`
- **方法**: GET
- **参数**: 
  - `id`: 植物ID（路径参数）
- **返回**: 
  ```json
  {
    "name": "植物名称",
    "recognitionAccuracy": 0.95,
    "imageUrl": "/uploads/xxx.jpg",
    "recognitionTime": "2023-07-15T10:30:00",
    "plantId": 1
  }
  ```

## 配置说明

### 数据库配置

在`application.properties`中配置MySQL数据库连接信息：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/plant_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### LLM API配置

在`application.properties`中配置LLM API信息：

```properties
llm.api-url=https://api.doubao.com/plant-recognition
llm.api-key=your_api_key_here
```

### Chroma向量数据库配置

在`application.properties`中配置Chroma服务器地址：

```properties
chroma.server-url=http://localhost:8000
```

### 微信小程序配置

在`application.properties`中配置微信小程序信息：

```properties
wechat.miniapp.appid=your_appid_here
wechat.miniapp.secret=your_secret_here
```

## 部署说明

1. 确保已安装JDK 17或更高版本
2. 确保已安装MySQL数据库并创建`plant_db`数据库
3. 执行`schema.sql`脚本创建数据表
4. 修改`application.properties`中的配置信息
5. 使用Maven构建项目：`mvn clean package`
6. 运行生成的JAR文件：`java -jar target/hello-0.0.1-SNAPSHOT.jar`

## 开发指南

1. 克隆项目到本地
2. 导入到IDE中（如IntelliJ IDEA或Eclipse）
3. 配置Maven依赖
4. 修改配置文件
5. 运行`HelloApplication`类启动应用

## 注意事项

1. 上传的图片大小限制为10MB
2. 需要确保uploads目录有写入权限
3. 使用前请替换LLM API密钥和微信小程序配置