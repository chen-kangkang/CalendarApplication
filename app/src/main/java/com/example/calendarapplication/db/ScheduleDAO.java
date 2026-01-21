package com.example.calendarapplication.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.calendarapplication.model.Schedule;
import com.example.calendarapplication.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// 数据操作类：ScheduleDAO.java
public class ScheduleDAO {
    private ScheduleDBHelper dbHelper;
    public ScheduleDAO(Context context){
        dbHelper = new ScheduleDBHelper(context);
    }
    // 插入日程
    public long insertSchedule(Schedule schedule, int userId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ScheduleDBHelper.COLUMN_DATE, schedule.getDate());
        values.put(ScheduleDBHelper.COLUMN_TITLE, schedule.getTitle());
        values.put(ScheduleDBHelper.COLUMN_TIME,schedule.getTime());
        values.put(ScheduleDBHelper.COLUMN_COMPLETED,schedule.getCompleted());
        values.put(ScheduleDBHelper.COLUMN_QUADRANT,schedule.getQuadrant());
        values.put(ScheduleDBHelper.COLUMN_REMARK, schedule.getRemark()); // 备注字段
        values.put(ScheduleDBHelper.COLUMN_COLOR, schedule.getColor());
        values.put(ScheduleDBHelper.COLUMN_DURATION, schedule.getDuration());
        values.put(ScheduleDBHelper.COLUMN_REMINDER, schedule.getReminder());
        values.put(ScheduleDBHelper.COLUMN_REPEAT, schedule.getRepeatType());
        values.put(ScheduleDBHelper.COLUMN_USER_ID, userId);
        long id = db.insert(ScheduleDBHelper.TABLE_NAME, null, values);
        db.close();
        return id;
    }
    // 更新日期
    public int updateSchedule(Schedule schedule, int userId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        // 更新所有可编辑字段
        values.put(ScheduleDBHelper.COLUMN_DATE, schedule.getDate());
        values.put(ScheduleDBHelper.COLUMN_TITLE, schedule.getTitle());
        values.put(ScheduleDBHelper.COLUMN_TIME, schedule.getTime());
        values.put(ScheduleDBHelper.COLUMN_QUADRANT, schedule.getQuadrant());
        values.put(ScheduleDBHelper.COLUMN_REMARK, schedule.getRemark());
        values.put(ScheduleDBHelper.COLUMN_COLOR, schedule.getColor());
        values.put(ScheduleDBHelper.COLUMN_DURATION, schedule.getDuration());
        values.put(ScheduleDBHelper.COLUMN_REMINDER, schedule.getReminder());
        values.put(ScheduleDBHelper.COLUMN_REPEAT, schedule.getRepeatType());
        // 注意：完成状态（completed）单独通过updateScheduleCompleted更新，这里不包含
        int rows = db.update(ScheduleDBHelper.TABLE_NAME, values, 
                ScheduleDBHelper.COLUMN_ID + "=? AND " + ScheduleDBHelper.COLUMN_USER_ID + "=?", 
                new String[]{String.valueOf(schedule.getId()), String.valueOf(userId)});
        db.close();
        return rows;
    }
    // 更新完成状态
    public int updateScheduleCompleted(int id, int completed, int userId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ScheduleDBHelper.COLUMN_COMPLETED,completed);
        int rows = db.update(ScheduleDBHelper.TABLE_NAME, values, 
                ScheduleDBHelper.COLUMN_ID + "=? AND " + ScheduleDBHelper.COLUMN_USER_ID + "=?", 
                new String[]{String.valueOf(id), String.valueOf(userId)});
        db.close();
        return rows;
    }
    // 根据id查询单个日程
    @SuppressLint("Range")
    public Schedule queryScheduleById(int id, int userId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(
                ScheduleDBHelper.TABLE_NAME,
                null,
                ScheduleDBHelper.COLUMN_ID + "=? AND " + ScheduleDBHelper.COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(id), String.valueOf(userId)},
                null, null, null
        );
        Schedule schedule=null;
        if(cursor.moveToNext()){
            schedule=new Schedule();
            schedule.setId(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_ID)));
            schedule.setDate(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_DATE)));
            schedule.setTitle(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_TITLE)));
            schedule.setTime(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_TIME)));
            schedule.setCompleted(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_COMPLETED)));
            schedule.setQuadrant(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_QUADRANT)));
            schedule.setRemark(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_REMARK)));
            schedule.setColor(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_COLOR)));
            schedule.setDuration(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_DURATION)));
            schedule.setReminder(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_REMINDER)));
            schedule.setRepeatType(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_REPEAT)));
            schedule.setUserId(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_USER_ID)));
        }
        cursor.close();
        db.close();
        return schedule;
    }
    // 根据日期查询日程
    @SuppressLint("Range")
    public List<Schedule> querySchedulesByDate(String date, int userId){
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(ScheduleDBHelper.TABLE_NAME,
                null,
                ScheduleDBHelper.COLUMN_DATE + "=? AND " + ScheduleDBHelper.COLUMN_USER_ID + "=?",
                new String[]{date, String.valueOf(userId)},
                null, null, null
        );
        while(cursor.moveToNext()){
            Schedule schedule = new Schedule();
            schedule.setId(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_ID)));
            schedule.setDate(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_DATE)));
            schedule.setTitle(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_TITLE)));
            schedule.setTime(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_TIME)));
            schedule.setCompleted(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_COMPLETED)));
            schedule.setQuadrant(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_QUADRANT)));
            schedule.setRemark(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_REMARK)));
            schedule.setColor(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_COLOR)));
            schedule.setDuration(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_DURATION)));
            schedule.setReminder(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_REMINDER)));
            schedule.setRepeatType(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_REPEAT)));
            schedule.setUserId(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_USER_ID)));
            schedules.add(schedule);
        }
        cursor.close();
        db.close();
        return schedules;
    }

    // 查询目标日期及之前的所有日程（用于处理重复规则）
    public List<Schedule> querySchedulesBeforeDate(String date, int userId){
        // 存储查询结果
        List<Schedule> scheduleList = new ArrayList<>();
        // 获取数据库只读实例（查询操作使用只读模式，避免误写，提升性能）
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        Cursor cursor=db.query(
                ScheduleDBHelper.TABLE_NAME,// 参数1：查询的表名
                null,// 参数2：查询的列名
                ScheduleDBHelper.COLUMN_DATE+"<=? AND " + ScheduleDBHelper.COLUMN_USER_ID + "=?",// 参数3：查询条件（WHERE 子句）
                new String[]{date, String.valueOf(userId)},// 参数4：条件占位符的实际值（替换上面的 ?）必须用字符串数组传递，即使只有一个参数，确保参数类型匹配
                null,// 参数5：GROUP BY 子句（null 表示不分组）
                null,// 参数6：HAVING 子句（null 表示不筛选分组结果）
                ScheduleDBHelper.COLUMN_DATE+" ASC, "+ScheduleDBHelper.COLUMN_TIME+" ASC"// 参数7：排序规则（ORDER BY 子句）
        );
        // 遍历查询结果的游标（Cursor 是结果集的迭代器，需逐行读取）
        while(cursor.moveToNext()){// moveToNext()：移动到下一行，有数据返回 true，无数据返回 false
            // 创建空的日程对象，用于存储当前行的数据
            Schedule schedule=new Schedule();
            // 调用工具方法，将游标当前行的数据填充到日程对象中
            // （fillScheduleFromCursor 内部通过列名获取索引，读取对应值并设置到 schedule 的属性）
            fillScheduleFromCursor(cursor, schedule);
            scheduleList.add(schedule);
        }
        cursor.close();
        db.close();
        return scheduleList;
    }

    // 查询所有重复日程
    public List<Schedule> queryAllRepeatSchedules(int userId) {
        List<Schedule> scheduleList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ScheduleDBHelper.TABLE_NAME,
                null,
                ScheduleDBHelper.COLUMN_USER_ID + "=? AND " + ScheduleDBHelper.COLUMN_REPEAT + "!=? AND " + ScheduleDBHelper.COLUMN_REPEAT + "!=?",
                new String[]{String.valueOf(userId), "", "NONE"},
                null,
                null,
                ScheduleDBHelper.COLUMN_DATE + " ASC"
        );
        while (cursor.moveToNext()) {
            Schedule schedule = new Schedule();
            fillScheduleFromCursor(cursor, schedule);
            scheduleList.add(schedule);
        }
        cursor.close();
        db.close();
        return scheduleList;
    }

    @SuppressLint("Range")
    private void fillScheduleFromCursor(Cursor cursor, Schedule schedule) {
        schedule.setId(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_ID)));
        schedule.setDate(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_DATE)));
        schedule.setTitle(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_TITLE)));
        schedule.setTime(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_TIME)));
        schedule.setCompleted(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_COMPLETED)));
        schedule.setQuadrant(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_QUADRANT)));
        schedule.setRemark(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_REMARK)));
        schedule.setColor(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_COLOR)));
        schedule.setDuration(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_DURATION)));
        schedule.setReminder(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_REMINDER)));
        schedule.setRepeatType(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_REPEAT)));
        schedule.setUserId(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_USER_ID)));
    }

    // 删除日程
    public int deleteSchedule(int id, int userId){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(
                ScheduleDBHelper.TABLE_NAME,
                ScheduleDBHelper.COLUMN_ID + "=? AND " + ScheduleDBHelper.COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(id), String.valueOf(userId)}
        );
        db.close();
        return rows;
    }

    // 删除重复日程的方法
    // deleteOption: 0-仅当前, 1-当前及未来, 2-所有
    @SuppressLint("Range")
    public int deleteRecurringSchedules(int id, int userId, int deleteOption) {
        android.util.Log.d("ScheduleDAO", "deleteRecurringSchedules called with id: " + id + ", userId: " + userId + ", deleteOption: " + deleteOption);
        
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            android.util.Log.d("ScheduleDAO", "Database opened successfully");
            
            // 首先查询日程信息
            Schedule schedule = null;
            Cursor cursor = null;
            try {
                cursor = db.query(
                        ScheduleDBHelper.TABLE_NAME,
                        null,
                        ScheduleDBHelper.COLUMN_ID + "=? AND " + ScheduleDBHelper.COLUMN_USER_ID + "=?",
                        new String[]{String.valueOf(id), String.valueOf(userId)},
                        null, null, null
                );
                
                if (cursor != null && cursor.moveToNext()) {
                    schedule = new Schedule();
                    schedule.setId(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_ID)));
                    schedule.setDate(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_DATE)));
                    schedule.setTitle(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_TITLE)));
                    schedule.setTime(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_TIME)));
                    schedule.setCompleted(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_COMPLETED)));
                    schedule.setQuadrant(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_QUADRANT)));
                    schedule.setRemark(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_REMARK)));
                    schedule.setColor(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_COLOR)));
                    schedule.setDuration(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_DURATION)));
                    schedule.setReminder(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_REMINDER)));
                    schedule.setRepeatType(cursor.getString(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_REPEAT)));
                    schedule.setUserId(cursor.getInt(cursor.getColumnIndex(ScheduleDBHelper.COLUMN_USER_ID)));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            
            android.util.Log.d("ScheduleDAO", "Query result - schedule: " + (schedule != null ? schedule.getTitle() : "null"));
            
            int rows = 0;

            if (schedule != null) {
                String repeatType = schedule.getRepeatType();
                String date = schedule.getDate();
                android.util.Log.d("ScheduleDAO", "Schedule details - repeatType: " + repeatType + ", date: " + date);

                if (!"NONE".equals(repeatType)) {
                    switch (deleteOption) {
                        case 0: // 仅删除当前
                            android.util.Log.d("ScheduleDAO", "Executing delete current only operation");
                            rows = db.delete(
                                    ScheduleDBHelper.TABLE_NAME,
                                    ScheduleDBHelper.COLUMN_ID + "=? AND " + ScheduleDBHelper.COLUMN_USER_ID + "=?",
                                    new String[]{String.valueOf(id), String.valueOf(userId)}
                            );
                            android.util.Log.d("ScheduleDAO", "Delete current operation affected " + rows + " rows");
                            break;
                        case 1: // 删除当前及未来
                            android.util.Log.d("ScheduleDAO", "Executing delete current and future operation");
                            rows = db.delete(
                                    ScheduleDBHelper.TABLE_NAME,
                                    ScheduleDBHelper.COLUMN_TITLE + "=? AND " + 
                                    ScheduleDBHelper.COLUMN_REPEAT + "=? AND " + 
                                    ScheduleDBHelper.COLUMN_DATE + ">=? AND " + 
                                    ScheduleDBHelper.COLUMN_USER_ID + "=?",
                                    new String[]{schedule.getTitle(), repeatType, date, String.valueOf(userId)}
                            );
                            android.util.Log.d("ScheduleDAO", "Delete current and future operation affected " + rows + " rows");
                            break;
                        case 2: // 删除所有
                            android.util.Log.d("ScheduleDAO", "Executing delete all operation");
                            rows = db.delete(
                                    ScheduleDBHelper.TABLE_NAME,
                                    ScheduleDBHelper.COLUMN_TITLE + "=? AND " + 
                                    ScheduleDBHelper.COLUMN_REPEAT + "=? AND " + 
                                    ScheduleDBHelper.COLUMN_USER_ID + "=?",
                                    new String[]{schedule.getTitle(), repeatType, String.valueOf(userId)}
                            );
                            android.util.Log.d("ScheduleDAO", "Delete all operation affected " + rows + " rows");
                            break;
                    }
                } else {
                    // 如果不是重复日程，直接删除
                    android.util.Log.d("ScheduleDAO", "Schedule is not repeating, deleting single instance");
                    rows = db.delete(
                            ScheduleDBHelper.TABLE_NAME,
                            ScheduleDBHelper.COLUMN_ID + "=? AND " + ScheduleDBHelper.COLUMN_USER_ID + "=?",
                            new String[]{String.valueOf(id), String.valueOf(userId)}
                    );
                    android.util.Log.d("ScheduleDAO", "Single delete operation affected " + rows + " rows");
                }
            } else {
                android.util.Log.d("ScheduleDAO", "Schedule is null, cannot delete");
            }
            
            android.util.Log.d("ScheduleDAO", "Total rows affected: " + rows);
            return rows;
            
        } catch (Exception e) {
            android.util.Log.e("ScheduleDAO", "Error in deleteRecurringSchedules", e);
            return 0;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
                android.util.Log.d("ScheduleDAO", "Database closed");
            }
        }
    }
    // 查询所有日程
    @SuppressLint("Range")
    public List<Schedule> queryAllSchedules(int userId) {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ScheduleDBHelper.TABLE_NAME,
                null,
                ScheduleDBHelper.COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null,
                ScheduleDBHelper.COLUMN_DATE + " DESC" // 按日期倒序
        );
        while (cursor.moveToNext()) {
            Schedule schedule = new Schedule();
            fillScheduleFromCursor(cursor, schedule);
            schedules.add(schedule);
        }
        cursor.close();
        db.close();
        return schedules;
    }

    // 分页查询日程
    @SuppressLint("Range")
    public List<Schedule> querySchedulesByPage(int userId, int page, int pageSize) {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int offset = (page - 1) * pageSize;
        String limit = pageSize + " OFFSET " + offset;
        Cursor cursor = db.query(
                ScheduleDBHelper.TABLE_NAME,
                null,
                ScheduleDBHelper.COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null,
                ScheduleDBHelper.COLUMN_DATE + " DESC",
                limit // LIMIT and OFFSET
        );
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Schedule schedule = new Schedule();
            fillScheduleFromCursor(cursor, schedule);
            schedules.add(schedule);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return schedules;
    }

    /**
     * 查询指定日期范围内的日程
     * @param userId 用户ID
     * @param startDate 开始日期（yyyy-MM-dd）
     * @param endDate 结束日期（yyyy-MM-dd）
     * @return 日程列表
     */
    public List<Schedule> querySchedulesByDateRange(int userId, String startDate, String endDate) {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = ScheduleDBHelper.COLUMN_USER_ID + "=? AND " +
                ScheduleDBHelper.COLUMN_DATE + " >= ? AND " +
                ScheduleDBHelper.COLUMN_DATE + " <= ?";
        String[] selectionArgs = {String.valueOf(userId), startDate, endDate};
        Cursor cursor = db.query(
                ScheduleDBHelper.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                ScheduleDBHelper.COLUMN_DATE + " DESC, " + ScheduleDBHelper.COLUMN_TIME + " ASC"
        );
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Schedule schedule = new Schedule();
            fillScheduleFromCursor(cursor, schedule);
            schedules.add(schedule);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return schedules;
    }

    /**
     * 检查指定日期之前是否有日程
     * @param userId 用户ID
     * @param date 日期（yyyy-MM-dd）
     * @return true表示有日程
     */
    public boolean hasSchedulesBeforeDate(int userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = ScheduleDBHelper.COLUMN_USER_ID + "=? AND " +
                ScheduleDBHelper.COLUMN_DATE + " < ?";
        String[] selectionArgs = {String.valueOf(userId), date};
        Cursor cursor = db.query(
                ScheduleDBHelper.TABLE_NAME,
                new String[]{"COUNT(*)"},
                selection,
                selectionArgs,
                null, null, null
        );
        boolean hasSchedules = false;
        if (cursor.moveToFirst()) {
            hasSchedules = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return hasSchedules;
    }

    /**
     * 检查指定日期之后是否有日程
     * @param userId 用户ID
     * @param date 日期（yyyy-MM-dd）
     * @return true表示有日程
     */
    public boolean hasSchedulesAfterDate(int userId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = ScheduleDBHelper.COLUMN_USER_ID + "=? AND " +
                ScheduleDBHelper.COLUMN_DATE + " > ?";
        String[] selectionArgs = {String.valueOf(userId), date};
        Cursor cursor = db.query(
                ScheduleDBHelper.TABLE_NAME,
                new String[]{"COUNT(*)"},
                selection,
                selectionArgs,
                null, null, null
        );
        boolean hasSchedules = false;
        if (cursor.moveToFirst()) {
            hasSchedules = cursor.getInt(0) > 0;
        }
        cursor.close();
        db.close();
        return hasSchedules;
    }

    // 查询日程总数
    @SuppressLint("Range")
    public int getScheduleCount(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + ScheduleDBHelper.TABLE_NAME + 
                " WHERE " + ScheduleDBHelper.COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}
        );
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    // 根据用户ID和关键词搜索日程
    @SuppressLint("Range")
    public List<Schedule> searchSchedulesByUserIdAndKeyword(int userId, String keyword) {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // 构建查询条件：搜索标题或备注中包含关键词的记录
        String selection = ScheduleDBHelper.COLUMN_USER_ID + "=? AND (" + 
                ScheduleDBHelper.COLUMN_TITLE + " LIKE ? OR " + 
                ScheduleDBHelper.COLUMN_REMARK + " LIKE ?)";
        
        // 构建查询参数
        String[] selectionArgs = new String[]{
                String.valueOf(userId),
                "%" + keyword + "%",
                "%" + keyword + "%"
        };
        
        // 执行查询
        Cursor cursor = db.query(
                ScheduleDBHelper.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null, null,
                ScheduleDBHelper.COLUMN_DATE + " DESC, " + ScheduleDBHelper.COLUMN_TIME + " DESC"
        );
        
        // 处理查询结果
        while (cursor.moveToNext()) {
            Schedule schedule = new Schedule();
            fillScheduleFromCursor(cursor, schedule);
            schedules.add(schedule);
        }
        
        // 关闭资源
        cursor.close();
        db.close();
        
        return schedules;
    }
    
    // 根据用户ID和筛选条件查询日程
    @SuppressLint("Range")
    public List<Schedule> querySchedulesByUserIdAndFilter(int userId, int filterType, String startDateStr) {
        List<Schedule> schedules = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        // 构建查询条件
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();
        
        // 基本条件：用户ID和开始日期
        selection.append(ScheduleDBHelper.COLUMN_USER_ID).append("=? AND ");
        selection.append(ScheduleDBHelper.COLUMN_DATE).append(">=?");
        selectionArgs.add(String.valueOf(userId));
        selectionArgs.add(startDateStr);
        
        // 根据筛选类型添加额外条件
        switch (filterType) {
            case 1: // 未完成
                selection.append(" AND ").append(ScheduleDBHelper.COLUMN_COMPLETED).append("=0");
                break;
            case 2: // 已完成
                selection.append(" AND ").append(ScheduleDBHelper.COLUMN_COMPLETED).append("=1");
                break;
            case 3: // 已逾期
                // 已逾期：日期小于当前日期且未完成
                selection.append(" AND ").append(ScheduleDBHelper.COLUMN_DATE).append("<?");
                selection.append(" AND ").append(ScheduleDBHelper.COLUMN_COMPLETED).append("=0");
                selectionArgs.add(DateUtils.formatDate(new Date()));
                break;
            // case 0: 全部，不需要额外条件
        }
        
        // 执行查询
        Cursor cursor = db.query(
                ScheduleDBHelper.TABLE_NAME,
                null,
                selection.toString(),
                selectionArgs.toArray(new String[0]),
                null, null,
                ScheduleDBHelper.COLUMN_DATE + " ASC, " + ScheduleDBHelper.COLUMN_TIME + " ASC"
        );
        
        // 处理查询结果
        while (cursor.moveToNext()) {
            Schedule schedule = new Schedule();
            fillScheduleFromCursor(cursor, schedule);
            schedules.add(schedule);
        }
        
        // 关闭资源
        cursor.close();
        db.close();
        
        return schedules;
    }
}
