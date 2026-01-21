package com.example.calendarapplication.util;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.calendarapplication.R;

/**
 * 权限工具类，封装精确闹钟权限和通知权限的申请与引导逻辑
 * 适配Android 12+的精确闹钟权限和Android 13+的通知权限要求
 */
public class PermissionTool {
    private static final String TAG = "PermissionTool";

    /**
     * 检查是否有精确闹钟权限（Android 12+需要）
     * @param context 上下文
     * @return true表示有精确闹钟权限，false表示没有
     */
    public static boolean hasExactAlarmPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        // Android 11及以下不需要精确闹钟权限
        return true;
    }

    /**
     * 检查是否有通知权限（Android 13+需要）
     * @param context 上下文
     * @return true表示有通知权限，false表示没有
     */
    public static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        // Android 12及以下不需要显式申请通知权限
        return true;
    }

    /**
     * 请求通知权限（Android 13+需要）
     * @param activity 当前Activity
     * @param requestCode 请求码，用于onRequestPermissionsResult回调
     */
    public static void requestNotificationPermission(androidx.appcompat.app.AppCompatActivity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        requestCode
                );
                Log.d(TAG, "请求通知权限");
            }
        }
    }

    /**
     * 引导用户去设置页面开启精确闹钟权限
     * @param context 上下文
     */
    public static void showExactAlarmPermissionDialog(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.exact_alarm_permission_message)
                    .setPositiveButton(R.string.permission_dialog_go_to_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 跳转到应用的精确闹钟权限设置页面
                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            intent.setData(Uri.parse("package:" + context.getPackageName()));
                            context.startActivity(intent);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.permission_dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }

    /**
     * 引导用户去设置页面开启通知权限
     * @param context 上下文
     */
    public static void showNotificationPermissionDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.permission_dialog_title)
                .setMessage(R.string.notification_permission_message)
                .setPositiveButton(R.string.permission_dialog_go_to_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用的通知权限设置页面
                        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
                        context.startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.permission_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .show();
    }

    /**
     * 检查是否有后台运行权限（避免被系统休眠杀死）
     * @param context 上下文
     * @return true表示有后台运行权限，false表示没有
     */
    public static boolean hasBackgroundPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        // Android 5.1及以下不需要后台运行权限
        return true;
    }

    /**
     * 引导用户去设置页面开启后台运行权限
     * @param context 上下文
     */
    public static void showBackgroundPermissionDialog(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.background_permission_message)
                    .setPositiveButton(R.string.permission_dialog_go_to_settings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 跳转到应用的电池优化设置页面
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + context.getPackageName()));
                            context.startActivity(intent);
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.permission_dialog_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }
    }
}