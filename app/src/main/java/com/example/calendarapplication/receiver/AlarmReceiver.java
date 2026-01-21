package com.example.calendarapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.calendarapplication.db.ScheduleDBHelper;
import com.example.calendarapplication.model.Schedule;
import com.example.calendarapplication.util.AlarmTool;
import com.example.calendarapplication.util.NotificationTool;

/**
 * 闹钟广播接收器，处理闹钟触发事件和通知操作按钮的点击事件
 */
public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Log.e(TAG, "接收到空的Intent");
            return;
        }

        // 获取Intent中的数据
        int scheduleId = intent.getIntExtra("scheduleId", -1);
        String title = intent.getStringExtra("title");
        String time = intent.getStringExtra("time");
        String remark = intent.getStringExtra("remark");
        String date = intent.getStringExtra("date");

        // 获取Action，判断是闹钟触发还是通知操作
        String action = intent.getAction();
        if (action == null) {
            // 闹钟触发事件
            handleAlarmTrigger(context, scheduleId, title, time, remark, date);
        } else {
            // 通知操作按钮点击事件
            handleNotificationAction(context, action, scheduleId, title, time, remark, date);
        }
    }

    /**
     * 处理闹钟触发事件，发送提醒通知
     */
    private void handleAlarmTrigger(Context context, int scheduleId, String title, String time, String remark, String date) {
        if (scheduleId == -1 || title == null || time == null) {
            Log.e(TAG, "闹钟触发事件缺少必要数据");
            return;
        }

        Log.d(TAG, "收到闹钟触发事件: 日程ID=" + scheduleId + ", 标题=" + title + ", 时间=" + time);

        // 发送提醒通知
        NotificationTool.sendReminderNotification(context, scheduleId, title, time, remark, date);
    }

    /**
     * 处理通知操作按钮点击事件
     */
    private void handleNotificationAction(Context context, String action, int scheduleId, String title, String time, String remark, String date) {
        if (scheduleId == -1) {
            Log.e(TAG, "通知操作缺少必要数据");
            return;
        }

        Log.d(TAG, "收到通知操作: Action=" + action + ", 日程ID=" + scheduleId);

        switch (action) {
            case NotificationTool.ACTION_LATER:
                // 处理"稍后提醒"操作，延迟10分钟
                handleLaterAction(context, scheduleId, title, time, remark, date);
                break;

            case NotificationTool.ACTION_COMPLETE:
                // 处理"标记已完成"操作
                handleCompleteAction(context, scheduleId);
                break;

            default:
                Log.w(TAG, "未知的通知操作: " + action);
                break;
        }
    }

    /**
     * 处理"稍后提醒"操作
     * @param context 上下文
     * @param scheduleId 日程ID
     * @param title 日程标题
     * @param time 当前提醒时间
     * @param remark 日程备注
     * @param date 日程日期
     */
    private void handleLaterAction(Context context, int scheduleId, String title, String time, String remark, String date) {
        // 取消当前通知
        NotificationTool.cancelNotification(context, scheduleId);

        // 设置延迟10分钟的新闹钟
        AlarmTool.delayAlarm(context, scheduleId, title, time, remark, date, 10);

        Log.d(TAG, "稍后提醒设置成功: 日程ID=" + scheduleId + ", 延迟10分钟");
    }

    /**
     * 处理"标记已完成"操作
     * @param context 上下文
     * @param scheduleId 日程ID
     */
    private void handleCompleteAction(Context context, int scheduleId) {
        // 取消当前通知
        NotificationTool.cancelNotification(context, scheduleId);

        // 从数据库中获取日程并标记为已完成
        ScheduleDBHelper dbHelper = new ScheduleDBHelper(context);
        Schedule schedule = dbHelper.getScheduleById(scheduleId);

        if (schedule != null) {
            // 更新日程状态为已完成
            schedule.setCompleted(1);
            dbHelper.updateSchedule(schedule);

            // 取消相关闹钟
            AlarmTool.cancelAlarm(context, scheduleId);

            Log.d(TAG, "日程标记为已完成: 日程ID=" + scheduleId);
        } else {
            Log.e(TAG, "找不到要标记为已完成的日程: 日程ID=" + scheduleId);
        }

        // 关闭数据库连接
        dbHelper.close();
    }
}