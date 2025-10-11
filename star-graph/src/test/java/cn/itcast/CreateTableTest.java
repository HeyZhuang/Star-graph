package cn.itcast;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class CreateTableTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void createUserResultTable() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS sg_user_result (" +
                "id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键', " +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', " +
                "user_id BIGINT(20) NOT NULL COMMENT '用户ID', " +
                "collect INT(1) DEFAULT 0 COMMENT '是否收藏', " +
                "url VARCHAR(500) DEFAULT NULL COMMENT '图片地址', " +
                "PRIMARY KEY (id), " +
                "KEY idx_user_id (user_id), " +
                "KEY idx_created_time (created_time)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户生成结果表';";

        jdbcTemplate.execute(createTableSql);
        System.out.println("✓ Table sg_user_result created successfully!");

        // 验证表是否存在
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'sg_user_result'",
                Integer.class
        );

        if (count != null && count > 0) {
            System.out.println("✓ Table verification successful!");
        } else {
            System.err.println("✗ Table verification failed!");
        }
    }
}
