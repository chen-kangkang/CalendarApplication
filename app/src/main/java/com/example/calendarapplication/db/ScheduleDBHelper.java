package com.example.calendarapplication.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.calendarapplication.model.Schedule;

import java.util.ArrayList;
import java.util.List;

// 数据库帮助类：ScheduleDBHelper.java
public class ScheduleDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ScheduleDb.DB";
    private static final int DB_VERSION=4;
    public static final String TABLE_NAME="schedule";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date"; // 格式：yyyy-MM-dd
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_TIME = "time"; // 格式：HH:mm-HH:mm
    public static final String COLUMN_COMPLETED = "completed"; // 0-未完成，1-已完成
    public static final String COLUMN_QUADRANT = "quadrant"; // 0-红(重要紧急),1-黄(重要不紧急),2-绿(紧急不重要),3-蓝(不紧急不重要)
    public static final String COLUMN_REMARK = "remark"; // 备注内容
    public static final String COLUMN_COLOR="color";// 事项颜色
    public static final String COLUMN_DURATION = "duration";// 持续时间
    public static final String COLUMN_REMINDER = "reminder";
    public static final String COLUMN_REPEAT = "repeat_type";
    public static final String COLUMN_USER_ID = "user_id"; // 关联用户ID的外键

    // 用户表常量
    public static final String USER_TABLE_NAME = "user";
    public static final String USER_COLUMN_ID = "_id";
    public static final String USER_COLUMN_PHONE = "phone";
    public static final String USER_COLUMN_PASSWORD = "password";
    public static final String USER_COLUMN_SALT = "salt";
    public static final String USER_COLUMN_NICKNAME = "nickname";
    public static final String USER_COLUMN_AVATAR = "avatar";
    public static final String USER_COLUMN_GENDER = "gender";
    public static final String USER_COLUMN_AGE = "age";
    public ScheduleDBHelper(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }    

    // 获取数据库实例
    public SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    public SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    // 获取单个日程
    @SuppressLint("Range")
    public Schedule getScheduleById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Schedule schedule = null;
        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor.moveToNext()) {
            schedule = new Schedule();
            schedule.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            schedule.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
            schedule.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
            schedule.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_TIME)));
            schedule.setCompleted(cursor.getInt(cursor.getColumnIndex(COLUMN_COMPLETED)));
            schedule.setQuadrant(cursor.getInt(cursor.getColumnIndex(COLUMN_QUADRANT)));
            schedule.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_REMARK)));
            schedule.setColor(cursor.getInt(cursor.getColumnIndex(COLUMN_COLOR)));
            schedule.setDuration(cursor.getInt(cursor.getColumnIndex(COLUMN_DURATION)));
            schedule.setReminder(cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER)));
            schedule.setRepeatType(cursor.getString(cursor.getColumnIndex(COLUMN_REPEAT)));
            schedule.setUserId(cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID)));
        }

        cursor.close();
        db.close();
        return schedule;
    }

    // 更新单个日程
    public int updateSchedule(Schedule schedule) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, schedule.getDate());
        values.put(COLUMN_TITLE, schedule.getTitle());
        values.put(COLUMN_TIME, schedule.getTime());
        values.put(COLUMN_COMPLETED, schedule.getCompleted());
        values.put(COLUMN_QUADRANT, schedule.getQuadrant());
        values.put(COLUMN_REMARK, schedule.getRemark());
        values.put(COLUMN_COLOR, schedule.getColor());
        values.put(COLUMN_DURATION, schedule.getDuration());
        values.put(COLUMN_REMINDER, schedule.getReminder());
        values.put(COLUMN_REPEAT, schedule.getRepeatType());
        values.put(COLUMN_USER_ID, schedule.getUserId());

        int rowsAffected = db.update(
                TABLE_NAME,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(schedule.getId())}
        );

        db.close();
        return rowsAffected;
    }

    // 获取所有未完成的日程
    @SuppressLint("Range")
    public List<Schedule> getUncompletedSchedules() {
        SQLiteDatabase db = getReadableDatabase();
        List<Schedule> schedules = new ArrayList<>();
        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                COLUMN_COMPLETED + "=?",
                new String[]{"0"},
                null, null, null
        );

        while (cursor.moveToNext()) {
            Schedule schedule = new Schedule();
            schedule.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            schedule.setDate(cursor.getString(cursor.getColumnIndex(COLUMN_DATE)));
            schedule.setTitle(cursor.getString(cursor.getColumnIndex(COLUMN_TITLE)));
            schedule.setTime(cursor.getString(cursor.getColumnIndex(COLUMN_TIME)));
            schedule.setCompleted(cursor.getInt(cursor.getColumnIndex(COLUMN_COMPLETED)));
            schedule.setQuadrant(cursor.getInt(cursor.getColumnIndex(COLUMN_QUADRANT)));
            schedule.setRemark(cursor.getString(cursor.getColumnIndex(COLUMN_REMARK)));
            schedule.setColor(cursor.getInt(cursor.getColumnIndex(COLUMN_COLOR)));
            schedule.setDuration(cursor.getInt(cursor.getColumnIndex(COLUMN_DURATION)));
            schedule.setReminder(cursor.getString(cursor.getColumnIndex(COLUMN_REMINDER)));
            schedule.setRepeatType(cursor.getString(cursor.getColumnIndex(COLUMN_REPEAT)));
            schedule.setUserId(cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID)));
            schedules.add(schedule);
        }

        cursor.close();
        db.close();
        return schedules;
    }

    // 添加必要的import语句
    // import android.database.Cursor;
    // import android.util.Log;
    // import com.example.calendarapplication.model.Schedule;
    // import com.example.calendarapplication.db.ScheduleDAO;
    // import java.util.ArrayList;
    // import java.util.List;

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建日程表
        String createTableSql = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_COMPLETED + " INTEGER, " +
                COLUMN_QUADRANT + " INTEGER, "+ 
                COLUMN_REMARK+" TEXT, "+ 
                COLUMN_COLOR + " INTEGER DEFAULT 0, " +
                COLUMN_DURATION + " INTEGER DEFAULT 1, " +
                COLUMN_REMINDER + " TEXT, " +
                COLUMN_REPEAT + " TEXT DEFAULT 'NONE', " +
                COLUMN_USER_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + USER_TABLE_NAME + "(" + USER_COLUMN_ID + ") ON DELETE CASCADE" +")";

        db.execSQL(createTableSql);

        // 创建用户表
        String createUserTableSql = "CREATE TABLE " + USER_TABLE_NAME + "(" +
                USER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USER_COLUMN_PHONE + " TEXT UNIQUE NOT NULL, " +
                USER_COLUMN_PASSWORD + " TEXT NOT NULL, " +
                USER_COLUMN_SALT + " TEXT NOT NULL, " +
                USER_COLUMN_NICKNAME + " TEXT DEFAULT '我是一个用户昵称', " +
                USER_COLUMN_AVATAR + " TEXT DEFAULT '', " +
                USER_COLUMN_GENDER + " TEXT DEFAULT '', " +
                USER_COLUMN_AGE + " INTEGER DEFAULT 0" +")";

        db.execSQL(createUserTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 如果是从版本1升级到版本2，添加用户表
        if (oldVersion < 2) {
            // 创建用户表
            String createUserTableSql = "CREATE TABLE " + USER_TABLE_NAME + "(" +
                    USER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    USER_COLUMN_PHONE + " TEXT UNIQUE NOT NULL, " +
                    USER_COLUMN_PASSWORD + " TEXT NOT NULL, " +
                    USER_COLUMN_SALT + " TEXT NOT NULL, " +
                    USER_COLUMN_NICKNAME + " TEXT DEFAULT '我是一个用户昵称', " +
                    USER_COLUMN_AVATAR + " TEXT DEFAULT '', " +
                    USER_COLUMN_GENDER + " TEXT DEFAULT '', " +
                    USER_COLUMN_AGE + " INTEGER DEFAULT 0" +")";

            db.execSQL(createUserTableSql);
        }
        
        // 如果是从版本2升级到版本3，添加盐值列
        if (oldVersion < 3) {
            // 为现有用户表添加盐值列
            db.execSQL("ALTER TABLE " + USER_TABLE_NAME + " ADD COLUMN " + USER_COLUMN_SALT + " TEXT NOT NULL DEFAULT ''");
            
            // 为现有用户生成随机盐值并更新密码哈希
            // 注意：这只是一个简单的示例，实际应用中可能需要更复杂的逻辑
        }
        
        // 如果是从版本3升级到版本4，添加用户ID外键列
        if (oldVersion < 4) {
            // 为现有日程表添加用户ID外键列
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_USER_ID + " INTEGER NOT NULL DEFAULT 1");
            // 添加外键约束
            // 注意：SQLite不支持直接在ALTER TABLE中添加外键约束
            // 这里我们只添加列，外键约束会在新创建的数据库中生效
        }
    }
}
