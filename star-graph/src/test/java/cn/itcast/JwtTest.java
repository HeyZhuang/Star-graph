package cn.itcast;

import cn.itcast.star.graph.core.pojo.User;
import cn.itcast.star.graph.core.utils.JwtUtils;
import org.junit.jupiter.api.Test;

public class JwtTest {

    @Test
    public void testJwtGenAndParse() {
        // 创建用户
        User user = new User();
        user.setId(1976682360185856001L);
        user.setUsername("admin");

        // 生成 token
        String token = JwtUtils.genToekn(user);
        System.out.println("Generated token: " + token);

        // 解析 token
        try {
            User parsedUser = JwtUtils.getToekn(token);
            if (parsedUser != null) {
                System.out.println("✓ Token parsed successfully!");
                System.out.println("✓ User ID: " + parsedUser.getId());
                System.out.println("✓ Username: " + parsedUser.getUsername());
            } else {
                System.err.println("✗ Token parsing returned null!");
            }
        } catch (Exception e) {
            System.err.println("✗ Token parsing failed: " + e.getMessage());
            e.printStackTrace();
        }

        // 测试实际的 token
        String actualToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjE5NzY2ODIzNjAxODU4NTYwMDEsInVuYW1lIjoiYWRtaW4iLCJleHAiOjE3OTE2ODg5NDF9.fjKygYo_F9skyPTf2d1MJWudJmV_yULWv0nE-bXJf8Q";
        System.out.println("\n--- Testing actual token from login ---");
        try {
            User actualUser = JwtUtils.getToekn(actualToken);
            if (actualUser != null) {
                System.out.println("✓ Actual token parsed successfully!");
                System.out.println("✓ User ID: " + actualUser.getId());
                System.out.println("✓ Username: " + actualUser.getUsername());
            } else {
                System.err.println("✗ Actual token parsing returned null!");
            }
        } catch (Exception e) {
            System.err.println("✗ Actual token parsing failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
