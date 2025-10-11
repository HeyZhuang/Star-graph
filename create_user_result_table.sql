-- 创建用户结果表
CREATE TABLE IF NOT EXISTS `sg_user_result` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `collect` INT(1) DEFAULT 0 COMMENT '是否收藏(0-未收藏，1-已收藏)',
  `url` VARCHAR(500) DEFAULT NULL COMMENT '图片地址',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户生成结果表';
