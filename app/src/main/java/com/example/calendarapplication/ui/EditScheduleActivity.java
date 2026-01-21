package com.example.calendarapplication.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapplication.R;
import com.example.calendarapplication.db.ScheduleDAO;
import com.example.calendarapplication.model.Schedule;
import com.example.calendarapplication.util.AlarmTool;
import com.example.calendarapplication.util.PermissionTool;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditScheduleActivity extends AppCompatActivity implements View.OnClickListener {

    private ScheduleDAO scheduleDAO;
    private Schedule currentSchedule;
    private long scheduleId = -1;
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    private ImageView ivBack, ivCheck;
    private EditText etTitle, etRemark;
    private TextView tvStartTime, tvEndTime, tvRepeat, tvReminder, tvQuadrant;
    private LinearLayout llColorContainer;
    private Button btnSave;

    private Calendar startCalendar, endCalendar, reminderCalendar;
    private int selectedColorIndex = 0;
    private int selectedQuadrantIndex = 3;
    private String selectedRepeatType = "NONE";

    private final int[] COLORS = {
            R.color.color_0, R.color.color_1, R.color.color_2, R.color.color_3,
            R.color.color_4, R.color.color_5, R.color.color_6, R.color.color_7,
            R.color.color_8, R.color.color_9
    };
    private final String[] QUADRANTS = {"重要且紧急", "重要不紧急", "紧急不重要", "不重要不紧急"};
    private final int[] QUADRANT_COLORS = {
            R.color.edit_checkbox_urgent_important,
            R.color.edit_checkbox_important,
            R.color.edit_checkbox_urgent,
            R.color.edit_checkbox_not_urgent_important
    };
    
    // 权限请求码
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);

        scheduleDAO = new ScheduleDAO(this);

        if (getIntent().hasExtra("schedule_id")) {
            scheduleId = getIntent().getIntExtra("schedule_id", -1);
        }

        initCalendars();
        initViews();

        if (scheduleId != -1) {
            loadScheduleData();
        } else {
            setupDefaultValues();
        }

        setupColorSelector();
    }

    private void initCalendars() {
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        endCalendar.add(Calendar.HOUR_OF_DAY, 1);
        reminderCalendar = Calendar.getInstance();
        reminderCalendar.set(Calendar.HOUR_OF_DAY, 9);
        reminderCalendar.set(Calendar.MINUTE, 0);
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivCheck = findViewById(R.id.iv_check);
        etTitle = findViewById(R.id.et_title);
        etRemark = findViewById(R.id.et_remark);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvEndTime = findViewById(R.id.tv_end_time);
        tvRepeat = findViewById(R.id.tv_repeat);
        tvReminder = findViewById(R.id.tv_reminder);
        tvQuadrant = findViewById(R.id.tv_quadrant);
        llColorContainer = findViewById(R.id.ll_color_container);
        btnSave = findViewById(R.id.btn_save);
        Button btnDelete = findViewById(R.id.btn_delete);

        ivBack.setOnClickListener(this);
        ivCheck.setOnClickListener(this);
        tvStartTime.setOnClickListener(this);
        tvEndTime.setOnClickListener(this);
        tvRepeat.setOnClickListener(this);
        tvReminder.setOnClickListener(this);
        tvQuadrant.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
    }

    private void loadScheduleData() {
        // 获取当前用户ID
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_id", 0);
        if (userId == 0) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        currentSchedule = scheduleDAO.queryScheduleById((int) scheduleId, (int) userId);
        if (currentSchedule != null) {
            try {
                etTitle.setText(currentSchedule.getTitle());
                etRemark.setText(currentSchedule.getRemark());
                ivCheck.setSelected(currentSchedule.getCompleted() == 1);

                // 处理时间格式，确保即使只有一个时间值也能正确解析
                String timeString = currentSchedule.getTime();
                String[] timeParts = new String[]{};
                if (timeString != null && !timeString.isEmpty()) {
                    timeParts = timeString.split("-");
                }
                String startTime = "00:00";
                String endTime = "00:00";
                if (timeParts != null) {
                    if (timeParts.length > 0) {
                        startTime = timeParts[0].trim();
                        endTime = startTime; // 默认结束时间与开始时间相同
                    }
                    if (timeParts.length > 1) {
                        endTime = timeParts[1].trim();
                    }
                }
                
                startCalendar.setTime(sdfDateTime.parse(currentSchedule.getDate() + " " + startTime));
                endCalendar.setTime(sdfDateTime.parse(currentSchedule.getDate() + " " + endTime));

                if (currentSchedule.getReminder() != null) {
                    reminderCalendar.setTime(sdfDateTime.parse(currentSchedule.getReminder()));
                }

                // 设置四象限分类，确保索引在有效范围内
                selectedQuadrantIndex = currentSchedule.getQuadrant();
                if (selectedQuadrantIndex < 0 || selectedQuadrantIndex >= QUADRANTS.length) {
                    selectedQuadrantIndex = 3; // 默认设置为第4象限（索引3）
                }
                tvQuadrant.setText(QUADRANTS[selectedQuadrantIndex]);

                // 设置颜色索引，确保索引在有效范围内
                selectedColorIndex = currentSchedule.getColor();
                if (selectedColorIndex < 0 || selectedColorIndex >= COLORS.length) {
                    selectedColorIndex = 0; // 默认使用橙色
                }
                selectedRepeatType = currentSchedule.getRepeatType();

                updateCheckboxColorByQuadrant();
                updateTimeDisplay();
                updateReminderDisplay();
                updateRepeatDisplay();
                highlightSelectedColor();

            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this, "数据加载失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "未找到日程", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupDefaultValues() {
        if (getIntent().hasExtra("selected_date")) {
            try {
                Date selectedDate = sdfDate.parse(getIntent().getStringExtra("selected_date"));
                startCalendar.setTime(selectedDate);
                endCalendar.setTime(selectedDate);
                endCalendar.add(Calendar.HOUR_OF_DAY, 1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // 设置默认颜色为-1，表示使用橙色
        selectedColorIndex = -1;
        
        tvQuadrant.setText(QUADRANTS[selectedQuadrantIndex]);
        updateCheckboxColorByQuadrant();
        updateTimeDisplay();
        updateReminderDisplay();
        updateRepeatDisplay();
    }

    private void setupColorSelector() {
        llColorContainer.removeAllViews();
        
        // 创建一个显示当前选中颜色的视图
        View currentColorView = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
        currentColorView.setLayoutParams(params);
        
        // 如果是默认状态，使用橙色
        if (selectedColorIndex < 0 || selectedColorIndex >= COLORS.length) {
            currentColorView.setBackgroundResource(R.color.orange);
        } else {
            currentColorView.setBackgroundResource(COLORS[selectedColorIndex]);
        }
        
        llColorContainer.addView(currentColorView);
        
        // 设置颜色容器的点击事件，弹出颜色选择对话框
        llColorContainer.setOnClickListener(v -> showColorPickerDialog());
    }

    private void highlightSelectedColor() {
        // 移除旧的当前颜色视图
        llColorContainer.removeAllViews();
        
        // 创建新的当前颜色视图
        View currentColorView = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
        currentColorView.setLayoutParams(params);
        
        // 如果是默认状态，使用橙色
        if (selectedColorIndex < 0 || selectedColorIndex >= COLORS.length) {
            currentColorView.setBackgroundResource(R.color.orange);
        } else {
            currentColorView.setBackgroundResource(COLORS[selectedColorIndex]);
        }
        
        llColorContainer.addView(currentColorView);
    }
    
    private void showColorPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.color_picker_dialog, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        
        LinearLayout llColorSelector = dialogView.findViewById(R.id.ll_color_selector);
        Button btnOk = dialogView.findViewById(R.id.btn_ok);
        
        // 添加颜色选择项
        for (int i = 0; i < COLORS.length; i++) {
            View colorView = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(48, 48);
            params.setMargins(8, 0, 8, 0);
            colorView.setLayoutParams(params);
            colorView.setBackgroundResource(COLORS[i]);
            colorView.setTag(i);
            
            // 高亮当前选中的颜色
            if (i == selectedColorIndex) {
                colorView.setScaleX(1.2f);
                colorView.setScaleY(1.2f);
            }
            
            colorView.setOnClickListener(v -> {
                selectedColorIndex = (int) v.getTag();
                
                // 更新所有颜色视图的选中状态
                for (int j = 0; j < llColorSelector.getChildCount(); j++) {
                    View child = llColorSelector.getChildAt(j);
                    if (j == selectedColorIndex) {
                        child.setScaleX(1.2f);
                        child.setScaleY(1.2f);
                    } else {
                        child.setScaleX(1.0f);
                        child.setScaleY(1.0f);
                    }
                }
            });
            
            llColorSelector.addView(colorView);
        }
        
        // 确定按钮点击事件
        btnOk.setOnClickListener(v -> {
            // 更新当前显示的颜色
            highlightSelectedColor();
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void updateCheckboxColorByQuadrant() {
        // 确保四象限索引在有效范围内
        int safeQuadrantIndex = selectedQuadrantIndex;
        if (safeQuadrantIndex < 0 || safeQuadrantIndex >= QUADRANT_COLORS.length) {
            safeQuadrantIndex = 3; // 默认使用第4象限颜色
        }
        int colorResId = QUADRANT_COLORS[safeQuadrantIndex];
        ivCheck.setColorFilter(getResources().getColor(colorResId));
    }

    private void updateTimeDisplay() {
        tvStartTime.setText(sdfDateTime.format(startCalendar.getTime()));
        tvEndTime.setText(sdfTime.format(endCalendar.getTime()));
    }

    private void updateReminderDisplay() {
        if ("NONE".equals(selectedRepeatType)) {
            tvReminder.setText(sdfDateTime.format(reminderCalendar.getTime()));
        } else {
            tvReminder.setText("每天 " + sdfTime.format(reminderCalendar.getTime()));
        }
    }

    private void updateRepeatDisplay() {
        switch (selectedRepeatType) {
            case "DAILY":
                tvRepeat.setText("每天");
                break;
            case "WEEKLY":
                tvRepeat.setText("每周");
                break;
            case "MONTHLY":
                tvRepeat.setText("每月");
                break;
            default:
                tvRepeat.setText("不重复");
                break;
        }
    }

    private void showDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    startCalendar.set(year, month, dayOfMonth);
                    endCalendar.set(year, month, dayOfMonth);
                    updateTimeDisplay();
                },
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar targetCalendar = isStartTime ? startCalendar : endCalendar;
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    targetCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    targetCalendar.set(Calendar.MINUTE, minute);
                    if (!isStartTime && targetCalendar.before(startCalendar)) {
                        Toast.makeText(this, "结束时间不能早于开始时间", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updateTimeDisplay();
                },
                targetCalendar.get(Calendar.HOUR_OF_DAY),
                targetCalendar.get(Calendar.MINUTE),
                true
        );
        dialog.show();
    }

    private void showRepeatDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View dialogView = getLayoutInflater().inflate(R.layout.repeat_dialog, null);
        bottomSheetDialog.setContentView(dialogView);

        TextView textNone = dialogView.findViewById(R.id.text_none);
        TextView textDaily = dialogView.findViewById(R.id.text_daily);
        TextView textWeekly = dialogView.findViewById(R.id.text_weekly);
        TextView textMonthly = dialogView.findViewById(R.id.text_monthly);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        View.OnClickListener listener = v -> {
            if (v.getId() == R.id.text_none) {
                selectedRepeatType = "NONE";
            } else if (v.getId() == R.id.text_daily) {
                selectedRepeatType = "DAILY";
            } else if (v.getId() == R.id.text_weekly) {
                selectedRepeatType = "WEEKLY";
            } else if (v.getId() == R.id.text_monthly) {
                selectedRepeatType = "MONTHLY";
            }
            updateRepeatDisplay();
            bottomSheetDialog.dismiss();
        };

        textNone.setOnClickListener(listener);
        textDaily.setOnClickListener(listener);
        textWeekly.setOnClickListener(listener);
        textMonthly.setOnClickListener(listener);
        buttonCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void showReminderDialog() {
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    reminderCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    reminderCalendar.set(Calendar.MINUTE, minute);
                    updateReminderDisplay();
                },
                reminderCalendar.get(Calendar.HOUR_OF_DAY),
                reminderCalendar.get(Calendar.MINUTE),
                true
        );
        dialog.setTitle("设置提醒时间");
        dialog.show();
    }

    private void showQuadrantDialog() {
        new AlertDialog.Builder(this)
                .setTitle("选择四象限分类")
                .setItems(QUADRANTS, (dialog, which) -> {
                    selectedQuadrantIndex = which;
                    tvQuadrant.setText(QUADRANTS[which]);
                    updateCheckboxColorByQuadrant();
                })
                .show();
    }

    private void saveSchedule() {
        // 获取当前用户ID
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_id", 0);
        if (userId == 0) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入日程标题", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentSchedule == null) {
            currentSchedule = new Schedule();
        }

        currentSchedule.setTitle(title);
        currentSchedule.setDate(sdfDate.format(startCalendar.getTime()));
        currentSchedule.setTime(
                sdfTime.format(startCalendar.getTime()) + "-" +
                        sdfTime.format(endCalendar.getTime())
        );
        currentSchedule.setCompleted(ivCheck.isSelected() ? 1 : 0);
        currentSchedule.setQuadrant(selectedQuadrantIndex);
        currentSchedule.setRemark(etRemark.getText().toString().trim());
        currentSchedule.setColor(selectedColorIndex);
        currentSchedule.setDuration(1); // 保留字段，暂不使用
        currentSchedule.setReminder(sdfDateTime.format(reminderCalendar.getTime())); // 使用用户设置的提醒时间
        currentSchedule.setRepeatType(selectedRepeatType);
        currentSchedule.setUserId((int) userId);

        if (scheduleId == -1) {
            long newId = scheduleDAO.insertSchedule(currentSchedule, (int) userId);
            if (newId != -1) {
                Toast.makeText(this, "日程创建成功", Toast.LENGTH_SHORT).show();
                // 设置闹钟提醒（需要先检查权限）
                setupAlarmForSchedule(currentSchedule);
                finish();
            } else {
                Toast.makeText(this, "日程创建失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            currentSchedule.setId((int) scheduleId);
            int rowsAffected = scheduleDAO.updateSchedule(currentSchedule, (int) userId);
            if (rowsAffected > 0) {
                Toast.makeText(this, "日程更新成功", Toast.LENGTH_SHORT).show();
                // 设置闹钟提醒（需要先检查权限）
                setupAlarmForSchedule(currentSchedule);
                finish();
            } else {
                Toast.makeText(this, "日程更新失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 设置闹钟提醒（带权限检查）
     * @param schedule 日程对象
     */
    private void setupAlarmForSchedule(Schedule schedule) {
        // 权限检查顺序：先检查精确闹钟权限，再检查通知权限
        if (!PermissionTool.hasExactAlarmPermission(this)) {
            // Android 12+需要精确闹钟权限
            PermissionTool.showExactAlarmPermissionDialog(this);
        } else if (!PermissionTool.hasNotificationPermission(this)) {
            // Android 13+需要通知权限
            PermissionTool.requestNotificationPermission(this, REQUEST_NOTIFICATION_PERMISSION);
        } else {
            // 所有权限都已获取，设置闹钟
            AlarmTool.setAlarm(this, schedule);
        }
    }

    /**
     * 显示删除确认对话框
     */
    private void showDeleteConfirmationDialog() {
        // 添加调试信息
        android.util.Log.d("EditScheduleActivity", "showDeleteConfirmationDialog called");
        android.util.Log.d("EditScheduleActivity", "currentSchedule is null: " + (currentSchedule == null));
        
        if (currentSchedule != null) {
            String repeatType = currentSchedule.getRepeatType();
            android.util.Log.d("EditScheduleActivity", "repeatType: " + repeatType);
            android.util.Log.d("EditScheduleActivity", "is repeat schedule: " + (!"NONE".equals(repeatType)));
        }
        
        if (currentSchedule != null && !"NONE".equals(currentSchedule.getRepeatType())) {
            // 如果是重复日程，显示特殊的删除选项对话框
            android.util.Log.d("EditScheduleActivity", "Showing recurring delete dialog with custom layout");
            
            // 创建选择项列表
            final String[] options = {"仅删除当前日程", "删除当前及未来全部日程", "删除所有日程"};
            
            // 使用自定义布局创建对话框
            LayoutInflater inflater = LayoutInflater.from(this);
            View dialogView = inflater.inflate(R.layout.dialog_repeat_delete, null);
            
            // 创建对话框
            final AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setCancelable(false) // 防止点击外部关闭
                    .create();
            
            // 设置ListView适配器
            ListView lvOptions = dialogView.findViewById(R.id.lv_delete_options);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options);
            lvOptions.setAdapter(adapter);
            
            // 设置ListView点击事件
            lvOptions.setOnItemClickListener((parent, view, position, id) -> {
                android.util.Log.d("EditScheduleActivity", "User selected option: " + position + ": " + options[position]);
                dialog.dismiss();
                // 根据用户选择执行不同的删除操作
                deleteRecurringSchedule(position);
            });
            
            // 设置取消按钮
            TextView tvCancel = dialogView.findViewById(R.id.tv_cancel);
            tvCancel.setOnClickListener(v -> {
                android.util.Log.d("EditScheduleActivity", "User cancelled delete dialog");
                dialog.dismiss();
            });
            
            dialog.show();
            
        } else {
            // 普通日程，显示简单的确认对话框
            android.util.Log.d("EditScheduleActivity", "Showing normal delete dialog");
            new AlertDialog.Builder(this)
                    .setTitle("确认删除")
                    .setMessage("确定要删除这个日程吗？此操作不可撤销。")
                    .setPositiveButton("删除", (dialog, which) -> deleteSchedule())
                    .setNegativeButton("取消", null)
                    .show();
        }
    }

    /**
     * 删除普通日程并取消关联的闹钟
     */
    private void deleteSchedule() {
        // 获取当前用户ID
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_id", 0);
        if (userId == 0) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 取消关联的闹钟
        if (currentSchedule != null) {
            AlarmTool.cancelAlarm(this, currentSchedule.getId());
        }

        // 从数据库中删除日程
        int rowsAffected = scheduleDAO.deleteSchedule((int) scheduleId, (int) userId);
        if (rowsAffected > 0) {
            Toast.makeText(this, "日程删除成功", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "日程删除失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 删除重复日程
     * @param deleteOption 0-仅当前, 1-当前及未来, 2-所有
     */
    private void deleteRecurringSchedule(int deleteOption) {
        android.util.Log.d("EditScheduleActivity", "deleteRecurringSchedule called with deleteOption: " + deleteOption);
        android.util.Log.d("EditScheduleActivity", "scheduleId: " + scheduleId);
        
        // 获取当前用户ID
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        long userId = sharedPreferences.getLong("user_id", 0);
        android.util.Log.d("EditScheduleActivity", "userId: " + userId);
        
        if (userId == 0) {
            Toast.makeText(this, "用户未登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 从数据库中删除日程
        android.util.Log.d("EditScheduleActivity", "Calling scheduleDAO.deleteRecurringSchedules with id: " + scheduleId + ", userId: " + userId + ", deleteOption: " + deleteOption);
        int rowsAffected = scheduleDAO.deleteRecurringSchedules((int) scheduleId, (int) userId, deleteOption);
        android.util.Log.d("EditScheduleActivity", "Delete operation affected " + rowsAffected + " rows");
        
        if (rowsAffected > 0) {
            Toast.makeText(this, "日程删除成功", Toast.LENGTH_SHORT).show();
            android.util.Log.d("EditScheduleActivity", "Showing success toast and finishing activity");
            finish();
        } else {
            Toast.makeText(this, "日程删除失败", Toast.LENGTH_SHORT).show();
            android.util.Log.d("EditScheduleActivity", "Showing failure toast - no rows affected");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            finish();
        } else if (id == R.id.iv_check) {
            ivCheck.setSelected(!ivCheck.isSelected());
        } else if (id == R.id.tv_start_time) {
            showDatePicker();
        } else if (id == R.id.tv_end_time) {
            showTimePicker(false);
        } else if (id == R.id.tv_repeat) {
            showRepeatDialog();
        } else if (id == R.id.tv_reminder) {
            showReminderDialog();
        } else if (id == R.id.tv_quadrant) {
            showQuadrantDialog();
        } else if (id == R.id.btn_save) {
            saveSchedule();
        } else if (id == R.id.btn_delete) {
            showDeleteConfirmationDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // 通知权限已授予，设置闹钟
                if (currentSchedule != null) {
                    AlarmTool.setAlarm(this, currentSchedule);
                }
            } else {
                Toast.makeText(this, "需要通知权限才能发送提醒", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
