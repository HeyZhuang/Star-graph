#!/bin/bash
# 初始化数据库表

# 等待 MySQL 准备就绪
sleep 2

# 创建 sg_user_result 表
docker exec mysql-422 sh -c "exec mysql -u root -p'87sdhf298TYUUIz2!' <<'EOF'
USE star-graph;

CREATE TABLE IF NOT EXISTS sg_user_result (
  id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  user_id BIGINT(20) NOT NULL COMMENT '用户ID',
  collect INT(1) DEFAULT 0 COMMENT '是否收藏',
  url VARCHAR(500) DEFAULT NULL COMMENT '图片地址',
  PRIMARY KEY (id),
  KEY idx_user_id (user_id),
  KEY idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户生成结果表';

SHOW TABLES;
EOF
"

echo "数据库表初始化完成"
