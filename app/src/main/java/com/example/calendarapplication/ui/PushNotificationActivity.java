package com.example.calendarapplication.ui;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapplication.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PushNotificationActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "PushNotificationPrefs";
    private static final String KEY_NOTIFY_AT_START = "notify_at_start";
    private static final String KEY_DEFAULT_REMINDER = "default_reminder";
    private static final String KEY_ALL_DAY_REMINDER_TIME = "all_day_reminder_time";

    private ToggleButton toggleNotifyAtStart;
    private ToggleButton toggleDefaultReminder;
    private TextView tvAllDayReminderTime;
    private SharedPreferences sharedPreferences;
    private Calendar reminderTimeCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_notification);

        // 初始化控件
        ImageView ivBack = findViewById(R.id.iv_back);
        toggleNotifyAtStart = findViewById(R.id.toggle_notify_at_start);
        toggleDefaultReminder = findViewById(R.id.toggle_default_reminder);

        // 设置返回按钮点击事件
        ivBack.setOnClickListener(v -> finish());

        // 初始化SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 初始化提醒时间日历
        reminderTimeCalendar = Calendar.getInstance();

        // 加载保存的设置
        loadSettings();

        // 设置开关监听器
        setupToggleListeners();
    }

    /**
     * 加载保存的设置
     */
    private void loadSettings() {
        // 日程开始时通知，默认关闭
        boolean notifyAtStart = sharedPreferences.getBoolean(KEY_NOTIFY_AT_START, false);
        toggleNotifyAtStart.setChecked(notifyAtStart);
        toggleNotifyAtStart.setBackgroundResource(notifyAtStart ? R.drawable.ic_switch_open : R.drawable.ic_switch_off);

        // 新建日程默认开启提醒，默认开启
        boolean defaultReminder = sharedPreferences.getBoolean(KEY_DEFAULT_REMINDER, true);
        toggleDefaultReminder.setChecked(defaultReminder);
        toggleDefaultReminder.setBackgroundResource(defaultReminder ? R.drawable.ic_switch_open : R.drawable.ic_switch_off);

        // 全天日程提醒时间，默认08:00
        long reminderTime = sharedPreferences.getLong(KEY_ALL_DAY_REMINDER_TIME, 0);
        if (reminderTime > 0) {
            reminderTimeCalendar.setTimeInMillis(reminderTime);
        } else {
            reminderTimeCalendar.set(Calendar.HOUR_OF_DAY, 8);
            reminderTimeCalendar.set(Calendar.MINUTE, 0);
        }
    }

    /**
     * 设置开关监听器
     */
    private void setupToggleListeners() {
        toggleNotifyAtStart.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 切换图标
                toggleNotifyAtStart.setBackgroundResource(isChecked ? R.drawable.ic_switch_open : R.drawable.ic_switch_off);
                // 保存设置
                sharedPreferences.edit().putBoolean(KEY_NOTIFY_AT_START, isChecked).apply();
            }
        });

        toggleDefaultReminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // 切换图标
                toggleDefaultReminder.setBackgroundResource(isChecked ? R.drawable.ic_switch_open : R.drawable.ic_switch_off);
                // 保存设置
                sharedPreferences.edit().putBoolean(KEY_DEFAULT_REMINDER, isChecked).apply();
            }
        });
    }

}