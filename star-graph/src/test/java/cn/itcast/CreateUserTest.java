package cn.itcast;

import cn.hutool.crypto.digest.BCrypt;
import cn.itcast.star.graph.core.mapper.UserMapper;
import cn.itcast.star.graph.core.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CreateUserTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void createAdminUser() {
        // 先删除已存在的 admin 用户（如果有）
        try {
            userMapper.deleteById(1976682360185856001L);
        } catch (Exception e) {
            // 忽略删除错误
        }

        // 创建新的 admin 用户
        User user = new User();
        user.setId(1976682360185856001L);
        user.setUsername("admin");
        user.setMobile("admin");
        user.setPassword(BCrypt.hashpw("admin", BCrypt.gensalt()));
        user.setNickname("管理员");
        user.setStatus(0);
        user.setVipLevel(1);
        user.setAvatar("https://avatar.example.com/admin.jpg");

        int result = userMapper.insert(user);

        System.out.println("========================================");
        System.out.println("Admin user created successfully!");
        System.out.println("Username: admin");
        System.out.println("Password: admin");
        System.out.println("User ID: " + user.getId());
        System.out.println("Insert result: " + result);
        System.out.println("========================================");

        // 验证用户创建成功
        User savedUser = userMapper.selectById(user.getId());
        if (savedUser != null) {
            System.out.println("✓ User verification successful!");
            System.out.println("✓ Username: " + savedUser.getUsername());
            System.out.println("✓ Password hash: " + savedUser.getPassword().substring(0, 20) + "...");
        } else {
            System.err.println("✗ User verification failed!");
        }
    }
}
