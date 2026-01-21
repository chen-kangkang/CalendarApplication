package com.example.calendarapplication.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.calendarapplication.R;
import com.example.calendarapplication.ui.ViewActivity;
import com.example.calendarapplication.receiver.AlarmReceiver;

/**
 * 通知工具类，封装通知的创建、分组、更新和操作按钮逻辑
 * 适配Android 8.0+的通知渠道要求
 */
public class NotificationTool {
    private static final String TAG = "NotificationTool";

    // 通知渠道ID和名称
    public static final String CHANNEL_ID = "schedule_reminder_channel";
    private static final String CHANNEL_NAME = "日程提醒";
    private static final String CHANNEL_DESCRIPTION = "日程提醒通知渠道";

    // 通知组ID
    private static final String GROUP_KEY_REMINDERS = "com.example.calendarapplication.REMINDERS";

    // 通知操作类型
    public static final String ACTION_LATER = "ACTION_LATER";
    public static final String ACTION_COMPLETE = "ACTION_COMPLETE";

    // 单例NotificationManager实例，避免频繁创建
    private static NotificationManager notificationManager;
    private static boolean channelCreated = false;

    /**
     * 获取NotificationManager实例（单例模式）
     * @param context 上下文
     * @return NotificationManager实例
     */
    private static NotificationManager getNotificationManager(Context context) {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // 确保通知渠道已创建
            createNotificationChannel(notificationManager);
        }
        return notificationManager;
    }

    /**
     * 创建通知渠道（仅Android 8.0+需要）
     * @param notificationManager NotificationManager实例
     */
    private static void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !channelCreated) {
            // 检查渠道是否已存在
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (channel == null) {
                // 创建新的通知渠道
                channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                channel.setDescription(CHANNEL_DESCRIPTION);
                channel.enableLights(true);
                channel.setLightColor(Color.RED);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 1000, 500, 1000});

                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "通知渠道创建成功");
            }
            channelCreated = true;
        }
    }

    /**
     * 【新增工具方法】兼容所有版本的通知权限检查（核心修改）
     * @param context 上下文
     * @return true=有权限，false=无权限
     */
    public static boolean checkNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+：检查POST_NOTIFICATIONS权限
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 12及以下：检查通知是否开启（系统默认逻辑）
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
    }

    /**
     * 发送日程提醒通知
     * @param context 上下文
     * @param scheduleId 日程ID
     * @param title 日程标题
     * @param time 提醒时间
     * @param remark 日程备注
     * @param date 日程日期
     */
    public static void sendReminderNotification(Context context, int scheduleId, String title, String time, String remark, String date) {
        // 【修改1】替换为兼容所有版本的权限检查（替换原有PermissionTool调用）
        if (!checkNotificationPermission(context)) {
            Log.w(TAG, "没有通知权限，无法发送通知: 日程ID=" + scheduleId);
            return;
        }

        // 创建通知点击的意图（跳转到日程详情页）
        Intent intent = new Intent(context, ViewActivity.class);
        intent.putExtra("scheduleId", scheduleId);
        intent.putExtra("date", date);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 【修改2】修正PendingIntent Flags的版本判断（M→S，Android 12+）
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // 关键修改：M→S
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, scheduleId, intent, flags);

        // 创建"稍后提醒"操作按钮
        Intent laterIntent = new Intent(context, AlarmReceiver.class);
        laterIntent.setAction(ACTION_LATER);
        laterIntent.putExtra("scheduleId", scheduleId);
        laterIntent.putExtra("title", title);
        laterIntent.putExtra("time", time);
        laterIntent.putExtra("remark", remark);
        laterIntent.putExtra("date", date);
        PendingIntent laterPendingIntent = PendingIntent.getBroadcast(context, scheduleId + 1, laterIntent, flags);

        // 创建"标记已完成"操作按钮
        Intent completeIntent = new Intent(context, AlarmReceiver.class);
        completeIntent.setAction(ACTION_COMPLETE);
        completeIntent.putExtra("scheduleId", scheduleId);
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(context, scheduleId + 2, completeIntent, flags);

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_reminder) // 设置通知图标
                .setContentTitle(title) // 设置通知标题
                .setContentText(time + (remark != null && !remark.isEmpty() ? " - " + remark : "")) // 设置通知内容
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 设置通知点击意图
                .setAutoCancel(true) // 点击通知后自动取消
                .setGroup(GROUP_KEY_REMINDERS) // 设置通知组
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                // 添加操作按钮
                .addAction(R.drawable.ic_reminder, "稍后提醒", laterPendingIntent)
                .addAction(R.drawable.ic_confirm, "标记已完成", completePendingIntent);

        // 发送或更新通知（使用scheduleId作为通知ID，确保相同日程的通知会被更新）
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        try {
            // 【修改3】二次校验权限（双重保险，让IDE认可）
            if (checkNotificationPermission(context)) {
                notificationManagerCompat.notify(scheduleId, builder.build());
                // 发送组通知（如果有多个通知时）
                sendGroupNotification(context, notificationManagerCompat, date);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "发送通知时权限不足: 日程ID=" + scheduleId, e);
        }

        Log.d(TAG, "通知发送成功: 日程ID=" + scheduleId + ", 标题=" + title);
    }

    /**
     * 发送组通知（用于管理同一日期的多个通知）
     * @param context 上下文
     * @param notificationManagerCompat NotificationManagerCompat实例
     * @param date 日期
     */
    private static void sendGroupNotification(Context context, NotificationManagerCompat notificationManagerCompat, String date) {
        // 【修改4】组通知添加权限检查+异常捕获（核心新增）
        try {
            if (!checkNotificationPermission(context)) {
                Log.w(TAG, "没有通知权限，无法发送组通知: 日期=" + date);
                return;
            }
            NotificationCompat.Builder groupBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_reminder)
                    .setContentTitle(date + "的日程提醒")
                    .setContentText("点击查看所有日程")
                    .setGroup(GROUP_KEY_REMINDERS)
                    .setGroupSummary(true)
                    .setAutoCancel(true);

            // 使用日期的哈希值作为组通知的ID，确保同一日期的组通知会被更新
            int groupNotificationId = date.hashCode();
            notificationManagerCompat.notify(groupNotificationId, groupBuilder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "发送组通知失败（权限异常）: 日期=" + date, e);
        }
    }

    /**
     * 取消特定通知
     * @param context 上下文
     * @param scheduleId 日程ID（与通知ID相同）
     */
    public static void cancelNotification(Context context, int scheduleId) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(scheduleId);
        Log.d(TAG, "通知取消成功: 日程ID=" + scheduleId);
    }

    /**
     * 取消所有通知
     * @param context 上下文
     */
    public static void cancelAllNotifications(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancelAll();
        Log.d(TAG, "所有通知已取消");
    }

    /**
     * 更新现有通知
     * @param context 上下文
     * @param scheduleId 日程ID
     * @param title 新的标题
     * @param content 新的内容
     */
    public static void updateNotification(Context context, int scheduleId, String title, String content) {
        // 【修改5】更新通知前添加权限检查
        if (!checkNotificationPermission(context)) {
            Log.w(TAG, "没有通知权限，无法更新通知: 日程ID=" + scheduleId);
            return;
        }

        // 创建通知点击的意图
        Intent intent = new Intent(context, ViewActivity.class);
        intent.putExtra("scheduleId", scheduleId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 【修改6】修正PendingIntent Flags的版本判断（M→S）
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // 关键修改：M→S
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(context, scheduleId, intent, flags);

        // 构建更新后的通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_reminder)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY_REMINDERS);

        // 更新通知
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        try {
            if (checkNotificationPermission(context)) {
                notificationManagerCompat.notify(scheduleId, builder.build());
            }
        } catch (SecurityException e) {
            Log.e(TAG, "更新通知失败（权限异常）: 日程ID=" + scheduleId, e);
        }

        Log.d(TAG, "通知更新成功: 日程ID=" + scheduleId);
    }
}