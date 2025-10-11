-- BCrypt 加密的 "admin" 密码：$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.RwOBsIUy
INSERT INTO `star-graph`.sg_user (id, username, password, nickname, mobile, status, vip_level)
VALUES (1976682360185856001, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.RwOBsIUy', '管理员', 'admin', 0, 1)
ON DUPLICATE KEY UPDATE password='$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.RwOBsIUy';
