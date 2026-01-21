package com.example.calendarapplication.ui;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.example.calendarapplication.R;
import com.example.calendarapplication.db.ScheduleDAO;
import com.example.calendarapplication.model.Schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class  AddScheduleActivity extends AppCompatActivity implements View.OnClickListener {
    private ScheduleDAO scheduleDAO;

    // 时间格式化工具
    private final SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat reminderSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    // 控件声明
        private ImageView ivBack, ivShare, ivCheck;
        private EditText etTitle, etRemark;
        private TextView tvStartTime, tvEndTime, tvRepeat, tvReminder, tvQuadrant;
        private LinearLayout llColorContainer;

    // 数据变量
    private String selectedDate;
    private String selectedStartTime = "14:00";
    private String selectedEndTime = "15:00";
    private int remindMinutes = 15;
    private int quadrant = 3; // 默认是第4象限（不紧急不重要）
    private String repeatType = "NONE";
    private int selectedColorIndex = 0; // 默认选中第一个颜色

    // 颜色数组（与EditScheduleActivity保持一致）
    private final int[] COLORS = {
            R.color.color_0, R.color.color_1, R.color.color_2, R.color.color_3,
            R.color.color_4, R.color.color_5, R.color.color_6, R.color.color_7,
            R.color.color_8, R.color.color_9
    };

    // 权限请求码
    private static final int REQUEST_NOTIFICATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule); // 使用与编辑页面相同的布局

        // 检查登录状态，未登录则跳转到登录页面
        if (!isUserLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 初始化数据库
        scheduleDAO = new ScheduleDAO(this);
        // 获取MainActivity传递的选中日期
        selectedDate = getIntent().getStringExtra("selected_date");

        // 绑定控件（完全匹配布局文件）
        initViews();
        
        // 修改顶部标题为"添加日程"
        TextView tvTitle = findViewById(R.id.tv_title);
        if (tvTitle != null) {
            tvTitle.setText("添加日程");
        }
        
        // 初始化显示文本
        updateDisplayTexts();
        
        // 初始化颜色选择器
        setupColorSelector();
        
        // 调整底部按钮以适应添加功能
        adjustBottomButtons();
    }

    /**
     * 检查用户是否已登录
     */
    private boolean isUserLoggedIn() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String phone = sharedPreferences.getString("phone", null);
        long loginTime = sharedPreferences.getLong("login_time", 0);

        if (phone != null && loginTime != 0) {
            // 计算登录时间和当前时间的差值（毫秒）
            long currentTime = System.currentTimeMillis();
            long diffTime = currentTime - loginTime;
            // 3天的毫秒数：3 * 24 * 60 * 60 * 1000 = 259200000
            return diffTime < 3 * 24 * 60 * 60 * 1000;
        }
        return false;
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

        // 设置点击事件（加非空）
        if (ivBack != null) ivBack.setOnClickListener(this);
        if (ivShare != null) ivShare.setOnClickListener(this);
        if (ivCheck != null) ivCheck.setOnClickListener(this);
        if (tvStartTime != null) tvStartTime.setOnClickListener(this);
        if (tvEndTime != null) tvEndTime.setOnClickListener(this);
        if (tvRepeat != null) tvRepeat.setOnClickListener(this);
        if (tvReminder != null) tvReminder.setOnClickListener(this);
        if (tvQuadrant != null) tvQuadrant.setOnClickListener(this);

        if (llColorContainer != null) {
            llColorContainer.setClickable(true);
            llColorContainer.setOnClickListener(v -> showColorPickerDialog());
        }

        // 直接执行更新和初始化
        updateDisplayTexts();
        setupColorSelector();
    }

    /**
     * 更新显示的文本，展示当前选择的内容
     */
    private void updateDisplayTexts() {
        // 更新开始时间和结束时间显示（加非空）
        if (tvStartTime != null) tvStartTime.setText(selectedStartTime);
        if (tvEndTime != null) tvEndTime.setText(selectedEndTime);

        // 更新四象限显示（加非空）
        String[] quadrantTexts = {"重要且紧急", "重要不紧急", "紧急不重要", "不重要不紧急"};
        if (tvQuadrant != null) tvQuadrant.setText(quadrantTexts[quadrant]);

        // 更新提醒时间显示（加非空）
        String remindText;
        if (remindMinutes == 0) {
            remindText = "不提醒";
        } else {
            remindText = String.format(Locale.getDefault(), "提前%d分钟", remindMinutes);
        }
        if (tvReminder != null) tvReminder.setText(remindText);

        // 更新重复类型显示（加非空）
        String repeatText;
        switch (repeatType) {
            case "DAILY":
                repeatText = "每天";
                break;
            case "WEEKLY":
                repeatText = "每周";
                break;
            case "MONTHLY":
                repeatText = "每月";
                break;
            default:
                repeatText = "不重复";
                break;
        }
        if (tvRepeat != null) tvRepeat.setText(repeatText);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            // 返回按钮点击事件
            finish();
        } else if (id == R.id.iv_check) {
            // 复选框点击事件（暂不实现）
            Toast.makeText(this, "完成状态暂未实现", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.tv_start_time) {
            // 点击开始时间：弹出时间选择器
            showTimePicker(true);
        } else if (id == R.id.tv_end_time) {
            // 点击结束时间：弹出时间选择器
            showTimePicker(false);
        } else if (id == R.id.tv_quadrant) {
            // 选择四象限：弹出单选选择框
            showQuadrantSelector();
        } else if (id == R.id.tv_reminder) {
            // 选择提醒时间：弹出单选选择框
            showRemindSelector();
        } else if (id == R.id.tv_repeat) {
            // 选择重复规则：弹出单选选择框
            showRepeatSelector();
        
        }
    }

    /**
     * 弹出时间选择器（区分开始/结束时间）
     * @param isStartTime true=选择开始时间，false=选择结束时间
     */
    private void showTimePicker(boolean isStartTime) {
        // 获取当前要修改的时间（开始/结束）
        String targetTime = isStartTime ? selectedStartTime : selectedEndTime;
        Calendar calendar = Calendar.getInstance();

        // 解析目标时间，设置时间选择器的默认值（避免每次都显示当前系统时间）
        try {
            Date timeDate = timeSdf.parse(targetTime);
            calendar.setTime(timeDate);
        } catch (ParseException e) {
            // 解析失败时，使用系统当前时间
            e.printStackTrace();
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute1) -> {
                    // 格式化时间为“HH:mm”
                    String newTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                    // 根据类型更新对应时间
                    if (isStartTime) {
                        selectedStartTime = newTime;
                        // 如果开始时间晚于结束时间，自动调整结束时间（+30分钟）
                        if (compareTime(selectedStartTime, selectedEndTime) > 0) {
                            Calendar tempCal = Calendar.getInstance();
                            try {
                                tempCal.setTime(timeSdf.parse(selectedStartTime));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            tempCal.add(Calendar.MINUTE, 30); // 加30分钟
                            selectedEndTime = timeSdf.format(tempCal.getTime());
                        }
                    } else {
                        selectedEndTime = newTime;
                        // 如果结束时间早于开始时间，提示用户并恢复原时间
                        if (compareTime(selectedEndTime, selectedStartTime) < 0) {
                            Toast.makeText(this, "结束时间不能早于开始时间", Toast.LENGTH_SHORT).show();
                            // 恢复原来的结束时间
                            selectedEndTime = targetTime;
                        }
                    }
                    // 更新显示文本
                    updateDisplayTexts();
                },
                hour,
                minute,
                true
        );
        timePicker.show();
    }

    /**
     * 比较两个时间的大小（HH:mm格式）
     * @param time1 第一个时间
     * @param time2 第二个时间
     * @return 1=time1晚于time2，0=相等，-1=time1早于time2
     */
    private int compareTime(String time1, String time2) {
        try {
            Date date1 = timeSdf.parse(time1);
            Date date2 = timeSdf.parse(time2);
            return date1.compareTo(date2);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 计算开始时间和结束时间的差值（返回分钟数）
     * @return 时长（分钟），失败返回默认60分钟
     */
    private int calculateDuration() {
        try {
            Date startDate = timeSdf.parse(selectedStartTime);
            Date endDate = timeSdf.parse(selectedEndTime);
            // 计算毫秒差值，转换为分钟
            long diffMs = endDate.getTime() - startDate.getTime();
            int diffMin = (int) (diffMs / (1000 * 60));
            // 确保时长为正数，最小为1分钟
            return Math.max(diffMin, 1);
        } catch (ParseException e) {
            e.printStackTrace();
            return 60; // 默认60分钟
        }
    }

    /**
     * 弹出四象限选择框
     */
    private void showQuadrantSelector() {
        // 四象限选项文本
        String[] quadrantOptions = {"重要且紧急", "重要不紧急", "紧急不重要", "不重要不紧急"};
        // 弹出单选对话框
        new AlertDialog.Builder(this)
                .setTitle("选择四象限")
                .setSingleChoiceItems(quadrantOptions, quadrant, (dialog, which) -> {
                    // which是选中的索引（0-3），赋值给quadrant
                    quadrant = which;
                    // 关闭对话框
                    dialog.dismiss();
                    // 更新显示文本
                    updateDisplayTexts();
                    // 提示用户选择结果
                    Toast.makeText(this, "已选择：" + quadrantOptions[which], Toast.LENGTH_SHORT).show();
                })
                .setCancelable(true) // 点击外部可取消
                .show();
    }

    /**
     * 弹出提醒时间选择框
     */
    private void showRemindSelector() {
        // 提醒提前时间选项（分钟）
        int[] remindOptions = {0, 5, 10, 15, 30, 60}; // 0表示不提醒、5分钟、10分钟、15分钟、30分钟、1小时
        // 转换为文本显示
        String[] remindTexts = new String[remindOptions.length];
        int defaultIndex = 0; // 默认选中第一个
        // 遍历设置文本，并找到当前选中的索引
        for (int i = 0; i < remindOptions.length; i++) {
            remindTexts[i] = remindOptions[i] == 0 ? "不提醒" : remindOptions[i] + "分钟";
            if (remindOptions[i] == remindMinutes) {
                defaultIndex = i;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("选择提醒时间")
                .setSingleChoiceItems(remindTexts, defaultIndex, (dialog, which) -> {
                    // 赋值选中的提醒分钟数
                    remindMinutes = remindOptions[which];
                    dialog.dismiss();
                    updateDisplayTexts();
                    if (remindMinutes == 0) {
                        Toast.makeText(this, "已选择：不提醒", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "已选择：提前" + remindOptions[which] + "分钟提醒", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(true)
                .show();
    }

    /**
     * 弹出重复规则选择框
     */
    private void showRepeatSelector() {
        // 重复规则选项文本和对应值
        String[] repeatTexts = {"不重复", "每天", "每周", "每月"};
        String[] repeatValues = {"NONE", "DAILY", "WEEKLY", "MONTHLY"};
        // 找到当前repeatType对应的索引
        int defaultIndex = 0;
        for (int i = 0; i < repeatValues.length; i++) {
            if (repeatValues[i].equals(repeatType)) {
                defaultIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("选择重复规则")
                .setSingleChoiceItems(repeatTexts, defaultIndex, (dialog, which) -> {
                    // 赋值选中的重复类型
                    repeatType = repeatValues[which];
                    dialog.dismiss();
                    updateDisplayTexts();
                    Toast.makeText(this, "已选择：" + repeatTexts[which], Toast.LENGTH_SHORT).show();
                })
                .setCancelable(true)
                .show();
    }

    /**
     * 调整底部按钮以适应添加功能
     */
    private void adjustBottomButtons() {
        // 找到保存和删除按钮
        Button btnSave = findViewById(R.id.btn_save);
        Button btnDelete = findViewById(R.id.btn_delete);
        
        // 隐藏删除按钮
        if (btnDelete != null) {
            btnDelete.setVisibility(View.GONE);
        }
        
        // 修改保存按钮文本为"确定"
        if (btnSave != null) {
            btnSave.setText("确定");
            btnSave.setOnClickListener(v -> saveSchedule());
        }
    }

    /**
     * 初始化颜色选择器
     */
    private void setupColorSelector() {
        // 加非空校验
        if (llColorContainer == null) {
            return;
        }

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

    /**
     * 弹出颜色选择对话框
     */
    private void showColorPickerDialog() {
        // 加布局加载容错
        View dialogView;
        try {
            dialogView = getLayoutInflater().inflate(R.layout.color_picker_dialog, null);
        } catch (Exception e) {
            Toast.makeText(this, "颜色选择弹窗加载失败", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        LinearLayout llColorSelector = dialogView.findViewById(R.id.ll_color_selector);
        Button btnOk = dialogView.findViewById(R.id.btn_ok);

        // 加控件非空容错
        if (llColorSelector == null || btnOk == null) {
            dialog.dismiss();
            return;
        }

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

            // 设置点击事件
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
            // 更新颜色选择器显示
            setupColorSelector();
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * 保存日程到数据库（自动计算时长）
     */
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
        String remark = etRemark.getText().toString().trim();

        // 校验标题不为空
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入事项标题", Toast.LENGTH_SHORT).show();
            return;
        }

        // 校验结束时间是否晚于开始时间
        if (compareTime(selectedEndTime, selectedStartTime) <= 0) {
            Toast.makeText(this, "结束时间必须晚于开始时间", Toast.LENGTH_SHORT).show();
            return;
        }

        // 从COLORS数组中获取选中的颜色值（int类型）
        int colorInt;
        if (selectedColorIndex >= 0 && selectedColorIndex < COLORS.length) {
            colorInt = getResources().getColor(COLORS[selectedColorIndex], getTheme());
        } else {
            // 默认使用橙色
            colorInt = getResources().getColor(R.color.orange, getTheme());
        }

        // 自动计算时长（分钟）
        int durationMinutes = calculateDuration();

        String timeString=selectedStartTime+"-"+selectedEndTime;
        
        // 根据重复类型生成所有需要插入的日期
        java.util.List<String> datesToInsert = new java.util.ArrayList<>();
        
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            calendar.setTime(sdf.parse(selectedDate));
            
            // 根据重复类型生成日期
            switch (repeatType) {
                case "DAILY":
                    // 每天重复，生成30天
                    for (int i = 0; i < 30; i++) {
                        datesToInsert.add(sdf.format(calendar.getTime()));
                        calendar.add(java.util.Calendar.DAY_OF_MONTH, 1);
                    }
                    break;
                case "WEEKLY":
                    // 每周重复，生成12周
                    for (int i = 0; i < 12; i++) {
                        datesToInsert.add(sdf.format(calendar.getTime()));
                        calendar.add(java.util.Calendar.DAY_OF_MONTH, 7);
                    }
                    break;
                case "MONTHLY":
                    // 每月重复，生成6个月
                    for (int i = 0; i < 6; i++) {
                        datesToInsert.add(sdf.format(calendar.getTime()));
                        calendar.add(java.util.Calendar.MONTH, 1);
                    }
                    break;
                case "YEARLY":
                    // 每年重复，生成2年
                    for (int i = 0; i < 2; i++) {
                        datesToInsert.add(sdf.format(calendar.getTime()));
                        calendar.add(java.util.Calendar.YEAR, 1);
                    }
                    break;
                case "NONE":
                default:
                    // 不重复，只添加当天
                    datesToInsert.add(selectedDate);
                    break;
            }
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            datesToInsert.add(selectedDate);
        }
        
        // 为每个日期创建独立的日程记录
        int successCount = 0;
        for (String date : datesToInsert) {
            // 计算该日期的提醒时间
            java.text.SimpleDateFormat dateSdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.text.SimpleDateFormat timeSdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            java.text.SimpleDateFormat reminderSdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
            
            try {
                java.util.Date dateObj = dateSdf.parse(date);
                java.util.Date startTimeObj = timeSdf.parse(selectedStartTime);
                
                if (dateObj == null || startTimeObj == null) {
                    continue;
                }
                
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(dateObj);
                
                java.util.Calendar timeCal = java.util.Calendar.getInstance();
                timeCal.setTime(startTimeObj);
                cal.set(java.util.Calendar.HOUR_OF_DAY, timeCal.get(java.util.Calendar.HOUR_OF_DAY));
                cal.set(java.util.Calendar.MINUTE, timeCal.get(java.util.Calendar.MINUTE));
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                
                // 减去提醒分钟数
                cal.add(java.util.Calendar.MINUTE, -remindMinutes);
                
                String dateReminderTime = reminderSdf.format(cal.getTime());
                
                // 构造Schedule对象
                Schedule schedule = new Schedule(
                        date,              // 每个重复日期都有独立记录
                        title,              // 2. String：标题
                        timeString,         // 3. String：时间（格式：HH:mm-HH:mm）
                        0,                  // 4. int：未完成（0）
                        quadrant,           // 5. int：优先级
                        remark,             // 6. String：备注
                        selectedColorIndex, // 7. int：颜色索引（0-9）
                        durationMinutes,    // 8. int：时长（自动计算）
                        dateReminderTime,   // 9. String：提醒时间（yyyy-MM-dd HH:mm格式）
                        repeatType,         // 10. String：保持重复类型
                        (int) userId        // 11. int：用户ID
                );
                
                // 插入数据库（每条记录都有独立的id）
                long result = scheduleDAO.insertSchedule(schedule, (int) userId);
                if (result > 0) {
                    successCount++;
                    // 只在第一次插入时检查权限并设置闹钟
                    if (successCount == 1) {
                        setupAlarmForSchedule(schedule);
                    } else {
                        // 后续记录直接设置闹钟（权限已检查过）
                        com.example.calendarapplication.util.AlarmTool.setAlarm(this, schedule);
                    }
                }
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }
        
        if (successCount > 0) {
            String message = successCount == 1 ? "事项添加成功" : "事项添加成功，共创建" + successCount + "个事项";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "事项添加失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 计算提醒时间
     * @return 提醒时间（yyyy-MM-dd HH:mm格式）
     */
    private String calculateReminderTime() {
        try {
            // 解析日期和开始时间
            SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = dateSdf.parse(selectedDate);

            SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date startTime = timeSdf.parse(selectedStartTime);

            if (date == null || startTime == null) {
                return "";
            }

            // 计算提醒时间
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // 设置时间部分
            Calendar startTimeCalendar = Calendar.getInstance();
            startTimeCalendar.setTime(startTime);
            calendar.set(Calendar.HOUR_OF_DAY, startTimeCalendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, startTimeCalendar.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // 减去提前提醒的分钟数
            calendar.add(Calendar.MINUTE, -remindMinutes);

            return reminderSdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 设置闹钟提醒（带权限检查）
     * @param schedule 日程对象
     */
    private void setupAlarmForSchedule(Schedule schedule) {
        // 权限检查顺序：先检查精确闹钟权限，再检查通知权限
        if (!com.example.calendarapplication.util.PermissionTool.hasExactAlarmPermission(this)) {
            // Android 12+需要精确闹钟权限
            com.example.calendarapplication.util.PermissionTool.showExactAlarmPermissionDialog(this);
        } else if (!com.example.calendarapplication.util.PermissionTool.hasNotificationPermission(this)) {
            // Android 13+需要通知权限
            com.example.calendarapplication.util.PermissionTool.requestNotificationPermission(this, REQUEST_NOTIFICATION_PERMISSION);
        } else {
            // 所有权限都已获取，设置闹钟
            com.example.calendarapplication.util.AlarmTool.setAlarm(this, schedule);
        }
    }

    /**
     * 处理权限请求结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            // 检查通知权限请求结果
            if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                // 权限被授予，可以设置闹钟
                // 注意：这里需要重新获取刚保存的日程对象，因为之前的schedule对象还没有ID
                SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
                long userId = sharedPreferences.getLong("user_id", 0);
                if (userId != 0) {
                    // 查询最新的日程（假设是最后插入的那个）
                    List<Schedule> schedules = scheduleDAO.querySchedulesByDate(selectedDate, (int) userId);
                    if (!schedules.isEmpty()) {
                        // 获取最后一个日程（最近插入的）
                        Schedule latestSchedule = schedules.get(schedules.size() - 1);
                        com.example.calendarapplication.util.AlarmTool.setAlarm(this, latestSchedule);
                    }
                }
            } else {
                // 权限被拒绝，显示引导用户手动开启的对话框
                com.example.calendarapplication.util.PermissionTool.showNotificationPermissionDialog(this);
            }
        }
    }
}