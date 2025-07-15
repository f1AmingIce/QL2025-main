CREATE TABLE IF NOT EXISTS `plant_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '植物名称',
  `image_url` varchar(255) DEFAULT NULL COMMENT '图片URL',
  `recognition_accuracy` float DEFAULT NULL COMMENT '识别准确度',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='植物信息表';


CREATE TABLE IF NOT EXISTS `plant_vectors` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plant_id` bigint(20) NOT NULL COMMENT '植物ID',
  `vector_data` blob NOT NULL COMMENT '向量数据(二进制格式)',
  `vector_dimension` int NOT NULL DEFAULT 512 COMMENT '向量维度',
  `vector_model` varchar(100) DEFAULT NULL COMMENT '向量模型名称',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_plant_id` (`plant_id`),
  CONSTRAINT `fk_vector_plant` FOREIGN KEY (`plant_id`) REFERENCES `plant_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='植物向量表';