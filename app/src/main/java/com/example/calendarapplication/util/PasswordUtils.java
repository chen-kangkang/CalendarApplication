package com.example.calendarapplication.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
// 1. 替换Base64的导入：去掉java.util.Base64，改用android.util.Base64
import android.util.Base64;

public class PasswordUtils {

    // 生成随机盐值
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.encodeToString(salt, Base64.DEFAULT);
    }

    // 将密码和盐值一起哈希
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hashedPassword = md.digest(password.getBytes());
            // 3. 修改Base64调用：添加flags参数（Base64.DEFAULT）
            return Base64.encodeToString(hashedPassword, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    // 验证密码
    public static boolean verifyPassword(String password, String salt, String hashedPassword) {
        String hashToVerify = hashPassword(password, salt);
        return hashToVerify.equals(hashedPassword);
    }
}