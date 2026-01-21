package com.example.calendarapplication.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.calendarapplication.model.User;
import com.example.calendarapplication.util.PasswordUtils;

public class UserDAO {
    private ScheduleDBHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new ScheduleDBHelper(context);
    }

    // 用户注册
    public long registerUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        // 生成盐值并哈希密码
        String salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hashPassword(user.getPassword(), salt);
        
        values.put(ScheduleDBHelper.USER_COLUMN_PHONE, user.getPhone());
        values.put(ScheduleDBHelper.USER_COLUMN_PASSWORD, hashedPassword);
        values.put(ScheduleDBHelper.USER_COLUMN_SALT, salt);
        values.put(ScheduleDBHelper.USER_COLUMN_NICKNAME, user.getNickname());
        values.put(ScheduleDBHelper.USER_COLUMN_AVATAR, user.getAvatar());
        values.put(ScheduleDBHelper.USER_COLUMN_GENDER, user.getGender());
        values.put(ScheduleDBHelper.USER_COLUMN_AGE, user.getAge());

        long id = db.insert(ScheduleDBHelper.USER_TABLE_NAME, null, values);
        db.close();
        return id;
    }

    // 根据手机号查询用户
    @SuppressLint("Range")
    public User queryUserByPhone(String phone) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ScheduleDBHelper.USER_TABLE_NAME,
                null,
                ScheduleDBHelper.USER_COLUMN_PHONE + "=?",
                new String[]{phone},
                null, null, null
        );

        User user = null;
        if (cursor.moveToNext()) {
            user = new User();
            user.setId(cursor.getLong(cursor.getColumnIndex(ScheduleDBHelper.USER_COLUMN_ID)));
            user.setPhone(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.USER_COLUMN_PHONE)));
            user.setPassword(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.USER_COLUMN_PASSWORD)));
            user.setSalt(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.USER_COLUMN_SALT)));
            user.setNickname(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.USER_COLUMN_NICKNAME)));
            user.setAvatar(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.USER_COLUMN_AVATAR)));
            user.setGender(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.USER_COLUMN_GENDER)));
            user.setAge(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.USER_COLUMN_AGE)));
        }

        cursor.close();
        db.close();
        return user;
    }

    // 用户登录验证
    public boolean login(String phone, String password) {
        User user = queryUserByPhone(phone);
        if (user != null) {
            return PasswordUtils.verifyPassword(password, user.getSalt(), user.getPassword());
        }
        return false;
    }

    // 更新用户信息
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ScheduleDBHelper.USER_COLUMN_NICKNAME, user.getNickname());
        values.put(ScheduleDBHelper.USER_COLUMN_AVATAR, user.getAvatar());
        values.put(ScheduleDBHelper.USER_COLUMN_GENDER, user.getGender());
        values.put(ScheduleDBHelper.USER_COLUMN_AGE, user.getAge());

        int rows = db.update(
                ScheduleDBHelper.USER_TABLE_NAME,
                values,
                ScheduleDBHelper.USER_COLUMN_PHONE + "=?",
                new String[]{user.getPhone()}
        );
        db.close();
        return rows;
    }

    // 更新密码
    public int updatePassword(String phone, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        // 生成新盐值并哈希新密码
        String salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hashPassword(newPassword, salt);
        
        values.put(ScheduleDBHelper.USER_COLUMN_PASSWORD, hashedPassword);
        values.put(ScheduleDBHelper.USER_COLUMN_SALT, salt);

        int rows = db.update(
                ScheduleDBHelper.USER_TABLE_NAME,
                values,
                ScheduleDBHelper.USER_COLUMN_PHONE + "=?",
                new String[]{phone}
        );
        db.close();
        return rows;
    }

    // 删除用户
    public int deleteUser(String phone) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                ScheduleDBHelper.USER_TABLE_NAME,
                ScheduleDBHelper.USER_COLUMN_PHONE + "=?",
                new String[]{phone}
        );
        db.close();
        return rows;
    }
}