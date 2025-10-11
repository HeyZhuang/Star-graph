package cn.itcast;

import cn.hutool.crypto.digest.BCrypt;
import cn.itcast.star.graph.core.mapper.UserMapper;
import cn.itcast.star.graph.core.pojo.User;
import cn.itcast.star.graph.core.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
public class UserTest {

    @Autowired
    UserMapper userMapper;
    @Autowired
    UserService userService;
//1521030727
    @Test
    public void testAddUser(){
        User user = new User();
        user.setAvatar("https://img1.baidu.com/it/u=1090452517,2487311686&fm=253&app=120&size=w931&n=0&f=JPEG&fmt=auto?sec=1729098000&t=bde85038085ca68a7e26aef57b0b6b2b");
        user.setUsername("admin1");
        user.setPassword(BCrypt.hashpw("admin"));
        user.setMobile("1521030727");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Test
    public void testCleanupAndAddTestUsers(){
        // 先清理所有测试用户
        System.out.println("清理旧的测试用户...");
        userMapper.delete(com.baomidou.mybatisplus.core.toolkit.Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, "admin")
                .or()
                .eq(User::getUsername, "testuser")
                .or()
                .eq(User::getUsername, "admin1")
                .or()
                .eq(User::getMobile, "13800138000")
                .or()
                .eq(User::getMobile, "13800138001")
                .or()
                .eq(User::getMobile, "1521030727")
        );

        // 创建管理员账号
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(BCrypt.hashpw("123456"));
        admin.setMobile("13800138000");
        admin.setEmail("admin@stargraph.com");
        admin.setNickname("管理员");
        admin.setAvatar("https://avatar.example.com/admin.jpg");
        admin.setGender(0);
        admin.setStatus(0);
        admin.setVipLevel(1);
        admin.setDeleted(0);
        admin.setCreateTime(LocalDateTime.now());
        admin.setUpdateTime(LocalDateTime.now());
        userMapper.insert(admin);
        System.out.println("管理员账号创建成功，ID: " + admin.getId());
        System.out.println("用户名: admin, 手机: 13800138000, 密码: 123456");

        // 创建测试用户账号
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(BCrypt.hashpw("123456"));
        testUser.setMobile("13800138001");
        testUser.setEmail("test@stargraph.com");
        testUser.setNickname("测试用户");
        testUser.setAvatar("https://avatar.example.com/test.jpg");
        testUser.setGender(0);
        testUser.setStatus(0);
        testUser.setVipLevel(0);
        testUser.setDeleted(0);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());
        userMapper.insert(testUser);
        System.out.println("测试用户创建成功，ID: " + testUser.getId());
        System.out.println("用户名: testuser, 手机: 13800138001, 密码: 123456");
    }
}
