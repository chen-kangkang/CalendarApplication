package com.example.calendarapplication.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.example.calendarapplication.db.ScheduleDBHelper;
import com.example.calendarapplication.model.Schedule;
import com.example.calendarapplication.util.AlarmTool;

import java.util.List;

/**
 * 设备重启广播接收器，用于在设备重启后重新设置未完成日程的闹钟提醒
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Log.e(TAG, "接收到空的Intent");
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            Log.e(TAG, "Intent中缺少Action");
            return;
        }

        // 检查是否是设备重启相关的Action
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals("android.intent.action.QUICKBOOT_POWERON") ||
                action.equals("com.htc.intent.action.QUICKBOOT_POWERON")) {

            Log.d(TAG, "收到设备重启广播，准备重新设置闹钟");

            // 使用异步任务读取数据库，避免主线程阻塞
            new ResetAlarmsTask(context).execute();
        }
    }

    /**
     * 异步任务，用于从数据库读取未完成的日程并重新设置闹钟
     */
    private static class ResetAlarmsTask extends AsyncTask<Void, Void, List<Schedule>> {
        private Context context;

        public ResetAlarmsTask(Context context) {
            this.context = context;
        }

        @Override
        protected List<Schedule> doInBackground(Void... voids) {
            // 在后台线程中读取数据库
            ScheduleDBHelper dbHelper = new ScheduleDBHelper(context);
            List<Schedule> uncompletedSchedules = dbHelper.getUncompletedSchedules();
            dbHelper.close();
            return uncompletedSchedules;
        }

        @Override
        protected void onPostExecute(List<Schedule> schedules) {
            super.onPostExecute(schedules);

            if (schedules == null || schedules.isEmpty()) {
                Log.d(TAG, "没有需要重新设置的未完成日程");
                return;
            }

            Log.d(TAG, "开始重新设置闹钟，共 " + schedules.size() + " 个未完成日程");

            // 为每个未完成的日程重新设置闹钟
            for (Schedule schedule : schedules) {
                if (schedule.getReminder() != null && !schedule.getReminder().isEmpty()) {
                    AlarmTool.setAlarm(context, schedule);
                }
            }

            Log.d(TAG, "所有闹钟重新设置完成");
        }
    }
}