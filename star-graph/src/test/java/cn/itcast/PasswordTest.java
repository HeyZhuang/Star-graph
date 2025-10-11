package cn.itcast;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.Test;

public class PasswordTest {

    @Test
    public void testGeneratePassword() {
        String password = "admin";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("BCrypt hashed password for 'admin': " + hashedPassword);

        // 验证密码
        boolean matches = BCrypt.checkpw(password, hashedPassword);
        System.out.println("Password verification: " + matches);
    }
}
