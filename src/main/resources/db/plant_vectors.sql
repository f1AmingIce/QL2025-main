-- 创建植物向量表，用于存储植物图片的向量信息
CREATE TABLE IF NOT EXISTS `plant_vectors` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plant_id` bigint(20) NOT NULL COMMENT '关联的植物ID',
  `vector_id` varchar(100) NOT NULL COMMENT 'Chroma中的向量ID',
  `similarity_threshold` float DEFAULT 0.8 COMMENT '相似度阈值，默认0.8',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_plant_id` (`plant_id`),
  UNIQUE KEY `idx_vector_id` (`vector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='植物向量信息表';