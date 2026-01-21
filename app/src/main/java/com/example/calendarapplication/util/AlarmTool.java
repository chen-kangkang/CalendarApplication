package com.example.calendarapplication.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.calendarapplication.model.Schedule;
import com.example.calendarapplication.receiver.AlarmReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 闹钟工具类，封装闹钟的设置、取消和延迟提醒功能
 * 适配Android 8.0+、Android 12+的版本兼容性要求
 */
public class AlarmTool {
    private static final String TAG = "AlarmTool";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * 设置闹钟提醒
     * @param context 上下文
     * @param schedule 日程对象，包含提醒时间等信息
     */
    public static void setAlarm(Context context, Schedule schedule) {
        // 如果没有设置提醒时间或日程已完成，则不设置闹钟
        if (schedule.getReminder() == null || schedule.getReminder().isEmpty() || schedule.getCompleted() == 1) {
            return;
        }

        try {
            // 解析提醒时间
            Date reminderDate = sdf.parse(schedule.getReminder());
            if (reminderDate == null) {
                Log.e(TAG, "提醒时间格式错误: " + schedule.getReminder());
                return;
            }

            // 如果提醒时间已过，则不设置闹钟
            if (reminderDate.before(new Date())) {
                Log.w(TAG, "提醒时间已过，不设置闹钟: " + schedule.getReminder());
                return;
            }

            // 创建AlarmManager实例
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "无法获取AlarmManager实例");
                return;
            }

            // 创建Intent，指向AlarmReceiver
            Intent intent = new Intent(context, AlarmReceiver.class);
            intent.putExtra("scheduleId", schedule.getId());
            intent.putExtra("title", schedule.getTitle());
            intent.putExtra("time", schedule.getReminder());
            intent.putExtra("remark", schedule.getRemark());
            intent.putExtra("date", schedule.getDate());

            // 创建PendingIntent，使用日程ID作为requestCode
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE; // Android 12+ 要求使用FLAG_IMMUTABLE
            }
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    schedule.getId(),
                    intent,
                    flags
            );

            // 根据Android版本选择合适的闹钟设置方式
            long triggerTime = reminderDate.getTime();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+，使用精确闹钟
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    // 如果没有精确闹钟权限，使用非精确闹钟
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0-11，使用精确闹钟
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                // Android 5.1及以下，使用普通闹钟
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }

            Log.d(TAG, "闹钟设置成功: 日程ID=" + schedule.getId() + ", 时间=" + schedule.getReminder());

        } catch (ParseException e) {
            Log.e(TAG, "解析提醒时间失败: " + schedule.getReminder(), e);
        }
    }

    /**
     * 取消闹钟提醒
     * @param context 上下文
     * @param scheduleId 日程ID
     */
    public static void cancelAlarm(Context context, int scheduleId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "无法获取AlarmManager实例");
            return;
        }

        // 创建与设置闹钟时相同的Intent和PendingIntent
        Intent intent = new Intent(context, AlarmReceiver.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId,
                intent,
                flags
        );

        // 取消闹钟
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();

        Log.d(TAG, "闹钟取消成功: 日程ID=" + scheduleId);
    }

    /**
     * 延迟提醒
     * @param context 上下文
     * @param scheduleId 日程ID
     * @param title 日程标题
     * @param time 当前提醒时间
     * @param remark 日程备注
     * @param date 日程日期
     * @param delayMinutes 延迟分钟数
     */
    public static void delayAlarm(Context context, int scheduleId, String title, String time, String remark, String date, int delayMinutes) {
        try {
            // 解析当前提醒时间
            Date currentTime = sdf.parse(time);
            if (currentTime == null) {
                Log.e(TAG, "提醒时间格式错误: " + time);
                return;
            }

            // 计算延迟后的时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentTime);
            calendar.add(Calendar.MINUTE, delayMinutes);
            Date delayedTime = calendar.getTime();

            // 创建AlarmManager实例
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "无法获取AlarmManager实例");
                return;
            }

            // 创建Intent，指向AlarmReceiver
            Intent intent = new Intent(context, AlarmReceiver.class);
            String delayedTimeStr = sdf.format(delayedTime);
            intent.putExtra("scheduleId", scheduleId);
            intent.putExtra("title", title);
            intent.putExtra("time", delayedTimeStr);
            intent.putExtra("remark", remark);
            intent.putExtra("date", date);

            // 创建PendingIntent
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    scheduleId,
                    intent,
                    flags
            );

            // 设置延迟后的闹钟
            long triggerTime = delayedTime.getTime();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }

            Log.d(TAG, "闹钟延迟成功: 日程ID=" + scheduleId + ", 延迟时间=" + delayMinutes + "分钟, 新时间=" + delayedTimeStr);

        } catch (ParseException e) {
            Log.e(TAG, "解析提醒时间失败: " + time, e);
        }
    }
}