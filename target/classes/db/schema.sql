-- 创建植物信息表
CREATE TABLE IF NOT EXISTS `plant_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '植物名称',
  `image_url` varchar(255) DEFAULT NULL COMMENT '图片URL',
  `recognition_accuracy` float DEFAULT NULL COMMENT '识别准确度',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='植物信息表';