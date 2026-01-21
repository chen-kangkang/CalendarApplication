package com.example.calendarapplication.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.app.DatePickerDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.calendarapplication.R;
import com.example.calendarapplication.db.ScheduleDAO;
import com.example.calendarapplication.model.Schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ViewActivity extends AppCompatActivity implements View.OnClickListener, GestureDetector.OnGestureListener {

    // 数据库和适配器
    private ScheduleDAO scheduleDAO;

    // 日期与日历
    private String currentSelectedDate;
    private Calendar currentCalendar;
    private SimpleDateFormat sdf;
    private SimpleDateFormat sdfMonth;

    // UI组件
    private DrawerLayout drawerLayout;
    private TextView tvDateTitle;
    private LinearLayout llDateBar;
    private TextView[] dateViews;
    private TextView[] weekDayViews;
    private ScrollView llDayView; // 改为ScrollView类型
    private ScrollView llWeekView; // 改为ScrollView类型
    private LinearLayout llMonthView;
    private FrameLayout llDayScheduleArea; // 改为FrameLayout类型
    private LinearLayout[] weekScheduleAreas;
    private GridLayout glMonthGrid;
    // 底部导航栏组件
    private LinearLayout llNavSchedule;
    private LinearLayout llNavView;
    private LinearLayout llNavMine;
    // 侧边栏
    private TextView tvDayView;
    private TextView tvWeekView;
    private TextView tvMonthView;

    // 视图状态
    private int currentViewMode = 0; // 0: 日视图, 1: 周视图, 2: 月视图
    private int currentDateIndex = 1; // 当前选中的日期索引

    // 手势检测
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        // 检查登录状态，未登录则跳转到登录页面
        if (!isUserLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 初始化数据库和日期
        initData();
        // 初始化UI组件
        initViews();
        // 设置点击事件
        setListeners();
        // 初始化手势检测器
        gestureDetector = new GestureDetector(this, this);
        // 为日期栏添加手势监听
        llDateBar.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selected_date")) {
            try {
                String externalDate = intent.getStringExtra("selected_date");
                currentSelectedDate = externalDate;
                currentCalendar.setTime(sdf.parse(externalDate));
                currentDateIndex = calculateCurrentDateIndex();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // 加载周日期
        loadWeekDates();

        // 设置初始标题显示
        updateDateTitle();

        // 检查是否有视图模式参数
        int viewMode = 0;
        if (intent != null && intent.hasExtra("view_mode")) {
            viewMode = intent.getIntExtra("view_mode", 0);
        }
        switchViewMode(viewMode);
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

    /**
     * 更新顶部日期标题显示
     */
    private void updateDateTitle() {
        if (tvDateTitle != null && currentCalendar != null) {
            tvDateTitle.setText(sdfMonth.format(currentCalendar.getTime()));
        }
    }

    /**
     * 初始化基础数据（日期、数据库）
     */
    private void initData() {
        currentCalendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        sdfMonth = new SimpleDateFormat("yyyy.MM", Locale.getDefault());
        currentSelectedDate = sdf.format(currentCalendar.getTime());
        currentDateIndex = calculateCurrentDateIndex();
        scheduleDAO = new ScheduleDAO(this);
    }

    /**
     * 计算当前日期在周中的索引（0=周一，6=周日）
     */
    private int calculateCurrentDateIndex() {
        int dayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
        return (dayOfWeek == Calendar.SUNDAY) ? 6 : dayOfWeek - Calendar.MONDAY;
    }

    /**
     * 根据选中的日期字符串同步currentDateIndex
     */
    private void syncDateIndexWithSelectedDate() {
        try {
            currentCalendar.setTime(sdf.parse(currentSelectedDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        currentDateIndex = calculateCurrentDateIndex();
    }

    /**
     * 初始化UI组件
     */
    @SuppressLint("WrongViewCast")
    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        llDateBar = findViewById(R.id.ll_date_bar);
        llDayView = findViewById(R.id.ll_day_view); // 修复：ScrollView类型
        llWeekView = findViewById(R.id.ll_week_view); // 修复：ScrollView类型
        llDayScheduleArea = findViewById(R.id.ll_day_schedule_area); // 修复：FrameLayout类型
        tvDateTitle = findViewById(R.id.tv_date);

        // 初始化日期视图
        dateViews = new TextView[7];
        dateViews[0] = findViewById(R.id.tv_date_1);
        dateViews[1] = findViewById(R.id.tv_date_2);
        dateViews[2] = findViewById(R.id.tv_date_3);
        dateViews[3] = findViewById(R.id.tv_date_4);
        dateViews[4] = findViewById(R.id.tv_date_5);
        dateViews[5] = findViewById(R.id.tv_date_6);
        dateViews[6] = findViewById(R.id.tv_date_7);

        // 初始化星期视图
        weekDayViews = new TextView[7];
        weekDayViews[0] = findViewById(R.id.tv_week_day_1);
        weekDayViews[1] = findViewById(R.id.tv_week_day_2);
        weekDayViews[2] = findViewById(R.id.tv_week_day_3);
        weekDayViews[3] = findViewById(R.id.tv_week_day_4);
        weekDayViews[4] = findViewById(R.id.tv_week_day_5);
        weekDayViews[5] = findViewById(R.id.tv_week_day_6);
        weekDayViews[6] = findViewById(R.id.tv_week_day_7);

        // 初始化周视图日程区域
        weekScheduleAreas = new LinearLayout[7];
        weekScheduleAreas[0] = findViewById(R.id.ll_week_schedule_1);
        weekScheduleAreas[1] = findViewById(R.id.ll_week_schedule_2);
        weekScheduleAreas[2] = findViewById(R.id.ll_week_schedule_3);
        weekScheduleAreas[3] = findViewById(R.id.ll_week_schedule_4);
        weekScheduleAreas[4] = findViewById(R.id.ll_week_schedule_5);
        weekScheduleAreas[5] = findViewById(R.id.ll_week_schedule_6);
        weekScheduleAreas[6] = findViewById(R.id.ll_week_schedule_7);

        // 初始化月视图组件
        llMonthView = findViewById(R.id.ll_month_view);
        glMonthGrid = findViewById(R.id.gl_month_grid);

        // 设置当前日期
        tvDateTitle.setText(currentSelectedDate);

        // 隐藏不需要的侧边栏选项
        findViewById(R.id.tv_calendar).setVisibility(View.GONE);
        findViewById(R.id.tv_quadrant).setVisibility(View.GONE);
        findViewById(R.id.tv_statistics).setVisibility(View.GONE);
        findViewById(R.id.tv_all_schedules).setVisibility(View.GONE);
        findViewById(R.id.tv_recycle).setVisibility(View.GONE);
        findViewById(R.id.tv_setting).setVisibility(View.GONE);

        tvDayView = findViewById(R.id.tv_day_view);
        tvWeekView = findViewById(R.id.tv_week_view);
        tvMonthView = findViewById(R.id.tv_month_view);

        // 初始化底部导航栏组件
        llNavSchedule = findViewById(R.id.ll_nav_schedule);
        llNavView = findViewById(R.id.ll_nav_view);
        llNavMine = findViewById(R.id.ll_nav_mine);

        // 日期选择下拉按钮
        ImageView ivDropdown = findViewById(R.id.iv_dropdown);

        switch (currentViewMode) {
            case 0: // 日视图
                tvWeekView.setBackgroundColor(0x00000000);
                tvMonthView.setBackgroundColor(0x00000000);
                tvDayView.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light));
                break;
            case 1: // 周视图
                tvDayView.setBackgroundColor(0x00000000);
                tvMonthView.setBackgroundColor(0x00000000);
                tvWeekView.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light));
                break;
            case 2: // 月视图
                tvWeekView.setBackgroundColor(0x00000000);
                tvDayView.setBackgroundColor(0x00000000);
                tvMonthView.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light));
                break;
        }
    }

    /**
     * 设置点击事件
     */
    private void setListeners() {
        findViewById(R.id.iv_menu).setOnClickListener(this);
        findViewById(R.id.iv_add).setOnClickListener(this);
        findViewById(R.id.iv_dropdown).setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                // 设置日期选择器的初始日期为当前选中日期
                calendar.setTime(sdf.parse(currentSelectedDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                currentSelectedDate = sdf.format(calendar.getTime());
                currentCalendar = calendar;
                // 更新顶部年月标题
                updateDateTitle();
                // 重新加载日期栏和当前视图数据
                loadWeekDates();
                switch (currentViewMode) {
                    case 0: // 日视图
                        loadDayView();
                        break;
                    case 1: // 周视图
                        loadWeekView();
                        break;
                    case 2: // 月视图
                        loadMonthView();
                        break;
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 日期点击事件
        for (int i = 0; i < dateViews.length; i++) {
            final int index = i;
            dateViews[i].setOnClickListener(v -> {
                selectDate(index);
            });
        }

        // 侧边栏菜单点击事件
        tvDayView.setOnClickListener(this);
        tvWeekView.setOnClickListener(this);
        tvMonthView.setOnClickListener(this);

        // 底部导航栏点击事件
        llNavSchedule.setOnClickListener(this);
        llNavView.setOnClickListener(this);
        llNavMine.setOnClickListener(this);
    }

    /**
     * 加载一周的日期
     */
    private void loadWeekDates() {
        try {
            currentCalendar.setTime(sdf.parse(currentSelectedDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.setFirstDayOfWeek(Calendar.MONDAY);
        tempCal.add(Calendar.DAY_OF_YEAR, -calculateCurrentDateIndex());

        for (int i = 0; i < 7; i++) {
            int day = tempCal.get(Calendar.DAY_OF_MONTH);
            dateViews[i].setText(String.valueOf(day));
            tempCal.add(Calendar.DAY_OF_YEAR, 1);
        }

        currentDateIndex = calculateCurrentDateIndex();
        refreshDateBarSelection();
        updateTvDate();
    }

    /**
     * 更新顶部tv_date的显示
     */
    private void updateTvDate() {
        try {
            // 获取当前显示的星期的所有日期
            Calendar tempCal = (Calendar) currentCalendar.clone();
            tempCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            Date[] weekDates = new Date[7];
            for (int i = 0; i < 7; i++) {
                weekDates[i] = tempCal.getTime();
                tempCal.add(Calendar.DAY_OF_YEAR, 1);
            }

            // 获取选中日期
            Date selectedDate = sdf.parse(currentSelectedDate);

            // 检查选中日期是否在当前显示的星期中
            boolean selectedDateInWeek = false;
            for (Date date : weekDates) {
                if (sdf.format(date).equals(sdf.format(selectedDate))) {
                    selectedDateInWeek = true;
                    break;
                }
            }

            // 设置显示内容
            if (selectedDateInWeek) {
                // 如果选中日期在当前显示的星期中，显示选中日期的年份和月份
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTime(selectedDate);
                tvDateTitle.setText(sdfMonth.format(selectedDate));
            } else {
                // 否则显示当前星期的星期一所对应的年份和月份
                Calendar mondayCal = Calendar.getInstance();
                mondayCal.setTime(weekDates[0]);
                tvDateTitle.setText(sdfMonth.format(weekDates[0]));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            // 默认显示当前日期的月份
            tvDateTitle.setText(sdfMonth.format(currentCalendar.getTime()));
        }
    }

    /**
     * 切换到上一个月
     */
    private void prevMonth() {
        currentCalendar.add(Calendar.MONTH, -1);
        updateDateTitle();
        loadWeekDates();

        // 如果当前是月视图，重新加载月视图
        if (currentViewMode == 2) {
            loadMonthView();
        }
    }

    /**
     * 切换到下一个月
     */
    private void nextMonth() {
        currentCalendar.add(Calendar.MONTH, 1);
        updateDateTitle();
        loadWeekDates();

        // 如果当前是月视图，重新加载月视图
        if (currentViewMode == 2) {
            loadMonthView();
        }
    }

    /**
     * 切换到上一个星期
     */
    private void prevWeek() {
        currentCalendar.add(Calendar.WEEK_OF_YEAR, -1);
        loadWeekDates();

        // 根据当前视图模式重新加载视图
        if (currentViewMode == 0) {
            loadDayView();
        } else if (currentViewMode == 1) {
            loadWeekView();
        } else if (currentViewMode == 2) {
            loadMonthView();
        }
    }

    /**
     * 切换到下一个星期
     */
    private void nextWeek() {
        currentCalendar.add(Calendar.WEEK_OF_YEAR, 1);
        loadWeekDates();

        // 根据当前视图模式重新加载视图
        if (currentViewMode == 0) {
            loadDayView();
        } else if (currentViewMode == 1) {
            loadWeekView();
        } else if (currentViewMode == 2) {
            loadMonthView();
        }
    }

    /**
     * 选择日期
     * @param index 日期索引
     */
    private void selectDate(int index) {
        if (currentDateIndex >= 0 && currentDateIndex < 7) {
            dateViews[currentDateIndex].setBackgroundResource(R.drawable.shape_circle_white);
            dateViews[currentDateIndex].setTextColor(Color.BLACK);
        }

        currentDateIndex = index;

        Calendar weekCal = (Calendar) currentCalendar.clone();
        weekCal.setFirstDayOfWeek(Calendar.MONDAY);
        weekCal.add(Calendar.DAY_OF_YEAR, -calculateCurrentDateIndex()); // 定位到本周一
        weekCal.add(Calendar.DAY_OF_YEAR, index); // 加索引（0=周一，6=周日）
        currentSelectedDate = sdf.format(weekCal.getTime());

        try {
            currentCalendar.setTime(sdf.parse(currentSelectedDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        loadWeekDates();

        if (currentViewMode == 0) {
            loadDayView();
        } else if (currentViewMode == 1) {
            loadWeekView();
        } else if (currentViewMode == 2) {
            loadMonthView();
        }

        // 刷新选中状态
        dateViews[currentDateIndex].setBackgroundResource(R.drawable.circle_orange_bg);
        dateViews[currentDateIndex].setTextColor(Color.WHITE);
    }

    /**
     * 根据日期字符串选择日期（跨视图使用）
     * @param dateStr 日期字符串（yyyy-MM-dd格式）
     */
    private void selectDateByString(String dateStr) {
        try {
            currentSelectedDate = dateStr;
            // 确保currentCalendar完全基于currentSelectedDate设置
            currentCalendar.setTime(sdf.parse(dateStr));
            // 同步更新currentDateIndex
            syncDateIndexWithSelectedDate();
            // 刷新日期栏选中状态
            refreshDateBarSelection();
            // 更新顶部标题
            updateDateTitle();
            // 重新加载当前视图
            if (currentViewMode == 0) {
                loadDayView();
            } else if (currentViewMode == 1) {
                loadWeekView();
            } else if (currentViewMode == 2) {
                loadMonthView();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载日视图
     */
    private void loadDayView() {
        try {
            currentCalendar.setTime(sdf.parse(currentSelectedDate));
            currentDateIndex = calculateCurrentDateIndex();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        LinearLayout llDayTimeSlots = findViewById(R.id.ll_day_time_slots);
        FrameLayout llDayScheduleContainer = findViewById(R.id.ll_day_schedule_area);
        if (llDayTimeSlots == null || llDayScheduleContainer == null) {
            Toast.makeText(this, "日视图容器初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 保留时间标签布局，只清除日程项
        for (int i = llDayScheduleContainer.getChildCount() - 1; i >= 0; i--) {
            View child = llDayScheduleContainer.getChildAt(i);
            if (child.getId() != R.id.ll_day_time_slots) {
                llDayScheduleContainer.removeViewAt(i);
            }
        }

        // 获取当前用户ID
        long userId = getSharedPreferences("user_info", MODE_PRIVATE).getLong("user_id", 0);
        if (userId == 0) {
            return;
        }

        // 从数据库获取当前日期的日程（包括重复日程）
        List<Schedule> schedules = getSchedulesForDate(currentSelectedDate, (int) userId);

        // 计算每个日程的位置和尺寸（避免重叠）
        List<ScheduleLayoutInfo> layoutInfoList = calculateScheduleLayouts(schedules);

        llDayTimeSlots.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 移除监听（避免重复触发）
                llDayTimeSlots.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // 获取正确的时间区域宽度
                int availableWidth = llDayTimeSlots.getWidth();
                if (availableWidth <= 0) {
                    availableWidth = dpToPx(300); // 兜底默认值
                }

                // 添加日程项（将原来的添加逻辑移到这里）
                for (ScheduleLayoutInfo layoutInfo : layoutInfoList) {
                    Schedule schedule = layoutInfo.schedule;

                    // 计算日程项高度（基于精确时间）
                    int height = calculateScheduleHeight(schedule.getTime());

                    // 计算宽度和左边距
                    int itemWidth = availableWidth * layoutInfo.widthPercent / 100 - 4;
                    int leftMargin = layoutInfo.leftPercent * availableWidth / 100 + dpToPx(2);

                    // 创建日程项视图
                    LinearLayout scheduleItem = createScheduleItem(schedule, height, itemWidth);

                    // 设置日程项的位置和宽度
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(itemWidth, height);
                    int topMargin = calculateTimePosition(schedule.getTime().split("-")[0]);
                    params.setMargins(leftMargin, topMargin + dpToPx(2), 0, dpToPx(2));
                    params.gravity = Gravity.TOP | Gravity.START;

                    llDayScheduleContainer.addView(scheduleItem, params);
                }
            }
        });
    }

    /**
     * 计算日程的布局信息（宽度和位置），避免时间重叠
     */
    private List<ScheduleLayoutInfo> calculateScheduleLayouts(List<Schedule> schedules) {
        List<ScheduleLayoutInfo> result = new ArrayList<>();

        if (schedules.isEmpty()) {
            return result;
        }

        // 解析时间范围并按开始时间排序（开始时间相同则按结束时间排序）
        List<TimeRange> timeRanges = new ArrayList<>();
        for (Schedule schedule : schedules) {
            String[] times = schedule.getTime().split("-");
            if (times.length < 2) {
                timeRanges.add(new TimeRange(0, 30, schedule));
                continue;
            }
            int startMinute = parseTimeToMinute(times[0]);
            int endMinute = parseTimeToMinute(times[1]);
            if (startMinute >= endMinute) {
                endMinute = startMinute + 30;
            }
            timeRanges.add(new TimeRange(startMinute, endMinute, schedule));
        }
        timeRanges.sort((a, b) -> {
            if (a.startMinute != b.startMinute) {
                return Integer.compare(a.startMinute, b.startMinute);
            } else {
                return Integer.compare(a.endMinute, b.endMinute);
            }
        });

        // 正确划分重叠组：同一时间段内的所有重叠日程为一组
        List<List<TimeRange>> overlappingGroups = new ArrayList<>();
        for (TimeRange range : timeRanges) {
            boolean added = false;
            // 遍历已有的组，检查当前日程是否与组内任意日程重叠
            for (List<TimeRange> group : overlappingGroups) {
                for (TimeRange existing : group) {
                    // 判定重叠：双向检查 - 当前日程的开始时间 < 组内日程的结束时间
                    // 且组内日程的开始时间 < 当前日程的结束时间
                    if (range.startMinute < existing.endMinute && existing.startMinute < range.endMinute) {
                        group.add(range);
                        added = true;
                        break;
                    }
                }
                if (added) {
                    break;
                }
            }
            // 若未加入任何组，新建组
            if (!added) {
                List<TimeRange> newGroup = new ArrayList<>();
                newGroup.add(range);
                overlappingGroups.add(newGroup);
            }
        }

        // 为组内日程分配宽度（平分组内宽度）
        for (List<TimeRange> group : overlappingGroups) {
            int groupSize = group.size();
            int widthPercent = 100 / groupSize;
            for (int i = 0; i < group.size(); i++) {
                TimeRange range = group.get(i);
                result.add(new ScheduleLayoutInfo(
                        range.schedule,
                        widthPercent,
                        widthPercent * i  // 左边距按索引分配
                ));
            }
        }

        return result;
    }

    /**
     * 将时间字符串转换为分钟数
     */
    private int parseTimeToMinute(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        return hour * 60 + minute;
    }

    /**
     * 日程布局信息内部类
     */
    private static class ScheduleLayoutInfo {
        Schedule schedule;
        int widthPercent;
        int leftPercent;

        ScheduleLayoutInfo(Schedule schedule, int widthPercent, int leftPercent) {
            this.schedule = schedule;
            this.widthPercent = widthPercent;
            this.leftPercent = leftPercent;
        }
    }

    /**
     * 时间范围内部类
     */
    private static class TimeRange {
        int startMinute;
        int endMinute;
        Schedule schedule;

        TimeRange(int startMinute, int endMinute, Schedule schedule) {
            this.startMinute = startMinute;
            this.endMinute = endMinute;
            this.schedule = schedule;
        }
    }

    /**
     * 检查是否为重复日程
     * @param schedule 日程对象
     * @return true表示是重复日程
     */
    private boolean isRecurringSchedule(Schedule schedule) {
        String repeatType = schedule.getRepeatType();
        return repeatType != null && !repeatType.equals("NONE") && !repeatType.isEmpty();
    }

    /**
     * 获取指定日期应该显示的日程列表
     * @param targetDate 目标日期（yyyy-MM-dd格式）
     * @param userId 用户ID
     * @return 该日期应该显示的日程列表
     */
    private List<Schedule> getSchedulesForDate(String targetDate, int userId) {
        // 直接获取当天的日程（包括普通日程和重复日程的独立记录）
        // 因为重复日程已经为每个日期创建了独立记录
        return scheduleDAO.querySchedulesByDate(targetDate, userId);
    }

    /**
     * 检查重复日程是否应该在指定日期显示
     * @param schedule 重复日程
     * @param targetDate 目标日期（yyyy-MM-dd格式）
     * @return true表示应该显示
     */
    private boolean shouldShowRecurringSchedule(Schedule schedule, String targetDate) {
        try {
            String repeatType = schedule.getRepeatType();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            Date scheduleDate = sdf.parse(schedule.getDate());
            Date target = sdf.parse(targetDate);

            // 如果目标日期早于日程创建日期，不显示
            if (target.before(scheduleDate)) {
                return false;
            }
            
            Calendar scheduleCal = Calendar.getInstance();
            scheduleCal.setTime(scheduleDate);
            
            Calendar targetCal = Calendar.getInstance();
            targetCal.setTime(target);
            
            switch (repeatType) {
                case "DAILY": // 每天重复
                    return true;
                    
                case "WEEKLY": // 每周重复
                    return scheduleCal.get(Calendar.DAY_OF_WEEK) == targetCal.get(Calendar.DAY_OF_WEEK);
                    
                case "BIWEEKLY": // 每两周重复
                    if (scheduleCal.get(Calendar.DAY_OF_WEEK) == targetCal.get(Calendar.DAY_OF_WEEK)) {
                        long diffDays = (targetCal.getTimeInMillis() - scheduleCal.getTimeInMillis()) / (1000 * 60 * 60 * 24);
                        long weeks = diffDays / 7;
                        return weeks % 2 == 0;
                    }
                    return false;
                    
                case "MONTHLY": // 每月重复
                    return scheduleCal.get(Calendar.DAY_OF_MONTH) == targetCal.get(Calendar.DAY_OF_MONTH);
                    
                case "YEARLY": // 每年重复
                    return scheduleCal.get(Calendar.DAY_OF_MONTH) == targetCal.get(Calendar.DAY_OF_MONTH) &&
                           scheduleCal.get(Calendar.MONTH) == targetCal.get(Calendar.MONTH);
                    
                default:
                    return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 加载周视图
     */
    private void loadWeekView() {
        // 确保currentCalendar正确设置为选中日期
        try {
            currentCalendar.setTime(sdf.parse(currentSelectedDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 创建临时Calendar实例，基于当前选中的日期计算周视图
        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.add(Calendar.DAY_OF_YEAR, -currentDateIndex);

        int selectedIndex = -1;
        // 遍历日期栏的7个TextView，更新日期显示和选中状态
        for (int i = 0; i < 7; i++) {
            // 获取当前临时日历所代表的日期数字，并设置到 TextView 上
            String dayStr = String.valueOf(tempCal.get(Calendar.DAY_OF_MONTH));
            dateViews[i].setText(dayStr);

            // 将当前临时日历的日期格式设置成"yyyy-MM-dd"字符串
            final String dateStr = sdf.format(tempCal.getTime());
            // 如果这个日期是当前选中日期，设置为选中状态
            if (dateStr.equals(currentSelectedDate)) {
                dateViews[i].setBackgroundResource(R.drawable.circle_orange_bg);
                dateViews[i].setTextColor(getResources().getColor(android.R.color.white));
                selectedIndex = i;
            } else {
                // 给TextView设置透明背景或未选中状态的背景
                dateViews[i].setBackgroundResource(R.drawable.shape_circle_white);
                dateViews[i].setTextColor(getResources().getColor(android.R.color.black));
            }

            // 为每个日期视图设置点击事件
            final int index = i;
            dateViews[i].setOnClickListener(v -> {
                // 使用selectDateByString确保所有视图同步
                selectDateByString(dateStr);
            });

            // 将临时日历向后推移一天，准备处理下一个日期
            tempCal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // 同步更新currentDateIndex，确保索引和视觉选中状态一致
        if (selectedIndex != -1) {
            currentDateIndex = selectedIndex;
        } else {
            // 如果没找到选中日期在当前周中的位置，重新计算
            syncDateIndexWithSelectedDate();
        }

        // 清空每一列中除了分隔线和View之外的所有子视图（用于动态添加时间标签和日程）
        for (LinearLayout area : weekScheduleAreas) {
            // 只保留View（分隔线），移除所有LinearLayout（时间槽）及其内容
            // 我们将使用FrameLayout的方式来动态添加日程
            for (int i = area.getChildCount() - 1; i >= 0; i--) {
                View child = area.getChildAt(i);
                if (child instanceof LinearLayout) {
                    area.removeViewAt(i);
                }
            }
        }

        // 重新加载一周的日程 - 使用独立的tempCal，不影响currentCalendar
        tempCal = (Calendar) currentCalendar.clone();
        tempCal.add(Calendar.DAY_OF_YEAR, -currentDateIndex);

        // 获取当前用户ID
        long userId = getSharedPreferences("user_info", MODE_PRIVATE).getLong("user_id", 0);
        if (userId == 0) {
            return;
        }

        // 获取屏幕宽度，计算每列的默认宽度
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int defaultColumnWidth = screenWidth / 7;

        for (int day = 0; day < 7; day++) {
            String date = sdf.format(tempCal.getTime());
            List<Schedule> schedules = getSchedulesForDate(date, (int) userId);

            // 计算每个日程的布局信息（避免重叠）
            List<ScheduleLayoutInfo> layoutInfoList = calculateScheduleLayouts(schedules);

            // 获取当前列的容器
            LinearLayout dayColumn = weekScheduleAreas[day];

            // 获取容器宽度，使用屏幕宽度/7作为默认值
            int availableWidth = dayColumn.getWidth();
            if (availableWidth <= 0) {
                availableWidth = defaultColumnWidth;
            }

            // 清除之前可能存在的所有日程项视图和FrameLayout容器
            for (int i = dayColumn.getChildCount() - 1; i >= 0; i--) {
                View child = dayColumn.getChildAt(i);
                if (child != null && child.getTag() != null && 
                    (child.getTag().equals("schedule_item") || child.getTag().equals("schedule_container"))) {
                    dayColumn.removeViewAt(i);
                }
            }

            // 创建FrameLayout容器用于绝对定位（解决LinearLayout不支持FrameLayout.LayoutParams的问题）
            FrameLayout scheduleContainer = new FrameLayout(this);
            scheduleContainer.setTag("schedule_container");
            FrameLayout.LayoutParams containerParams = new FrameLayout.LayoutParams(
                    availableWidth - dpToPx(8),
                    dpToPx(24 * 60)  // 24小时 * 60dp/小时
            );
            containerParams.setMargins(dpToPx(4), 0, 0, 0);
            dayColumn.addView(scheduleContainer, containerParams);

            // 添加日程项到FrameLayout容器中
            for (ScheduleLayoutInfo layoutInfo : layoutInfoList) {
                Schedule schedule = layoutInfo.schedule;

                // 计算日程项的垂直位置和高度（基于精确时间）
                int[] timeInfo = calculateTimePositionAndHeight(schedule.getTime());
                int topPosition = timeInfo[0];
                int height = timeInfo[1];

                // 周视图宽度计算：使用容器实际宽度
                int itemWidth = (availableWidth - dpToPx(12)) * layoutInfo.widthPercent / 100;
                int leftMargin = layoutInfo.leftPercent * (availableWidth - dpToPx(12)) / 100;

                // 创建日程项视图
                LinearLayout scheduleItem = createScheduleItem(schedule, height, itemWidth);
                scheduleItem.setTag("schedule_item");

                // 使用FrameLayout.LayoutParams进行绝对定位（在FrameLayout容器中生效）
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        itemWidth,
                        height
                );
                params.setMargins(leftMargin, topPosition, 0, 0);
                params.gravity = Gravity.START | Gravity.TOP;

                scheduleContainer.addView(scheduleItem, params);
            }

            tempCal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    /**
     * 刷新日期栏选中状态
     */
    private void refreshDateBarSelection() {
        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.setFirstDayOfWeek(Calendar.MONDAY);
        // 定位到本周一（通过减去当前日期的索引）
        tempCal.add(Calendar.DAY_OF_YEAR, -calculateCurrentDateIndex());

        for (int i = 0; i < 7; i++) {
            String itemDateStr = sdf.format(tempCal.getTime());
            if (itemDateStr.equals(currentSelectedDate)) {
                dateViews[i].setBackgroundResource(R.drawable.shape_circle_orange);
                dateViews[i].setTextColor(getResources().getColor(android.R.color.white));
            } else {
                dateViews[i].setBackgroundResource(R.drawable.shape_circle_white);
                dateViews[i].setTextColor(getResources().getColor(android.R.color.black));
            }
            tempCal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    /**
     * 根据时间计算日程项高度
     * @param time 时间字符串（格式：HH:mm-HH:mm）
     * @return 日程项高度
     */
    private int calculateScheduleHeight(String time) {
        try {
            String[] parts = time.split("-");
            if (parts.length < 2) {
                return dpToPx(60);
            }

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            Date startTime = timeFormat.parse(parts[0]);
            Date endTime = timeFormat.parse(parts[1]);

            if (startTime.equals(endTime)) {
                return dpToPx(30);
            }

            int startPosition = calculateTimePosition(parts[0]);
            int endPosition = calculateTimePosition(parts[1]);
            int height = endPosition - startPosition;

            return Math.max(height, dpToPx(30));
        } catch (ParseException e) {
            e.printStackTrace();
            return dpToPx(60);
        }
    }

    /**
     * dp转换为px
     * @param dp dp值
     * @return px值
     */
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    /**
     * 根据时间字符串计算该时间在日视图中的垂直位置
     * @param time 时间字符串（格式：HH:mm）
     * @return 垂直位置（像素）
     */
    private int calculateTimePosition(String time) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            Date timeDate = timeFormat.parse(time);
            Calendar cal = Calendar.getInstance();
            cal.setTime(timeDate);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            // 每个小时60dp高度，1分钟1dp
            return dpToPx(60) * hour + dpToPx(minute);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 根据时间字符串计算日程项的垂直位置和高度
     * @param time 时间字符串（格式：HH:mm-HH:mm）
     * @return int数组，[0]为顶部位置，[1]为高度
     */
    private int[] calculateTimePositionAndHeight(String time) {
        int[] result = new int[2];
        try {
            String[] parts = time.split("-");
            if (parts.length < 2) {
                result[0] = 0;
                result[1] = dpToPx(60);
                return result;
            }

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            Date startTime = timeFormat.parse(parts[0]);
            Date endTime = timeFormat.parse(parts[1]);

            // 计算开始时间的位置
            int startPosition = calculateTimePosition(parts[0]);

            // 如果开始时间和结束时间相同，给最小高度30dp
            if (startTime.equals(endTime)) {
                result[0] = startPosition;
                result[1] = dpToPx(30);
                return result;
            }

            // 计算结束时间的位置
            int endPosition = calculateTimePosition(parts[1]);

            // 计算高度
            int height = endPosition - startPosition;
            if (height < dpToPx(30)) {
                height = dpToPx(30);
            }

            result[0] = startPosition;
            result[1] = height;
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
            result[0] = 0;
            result[1] = dpToPx(60);
            return result;
        }
    }

    /**
     * 创建日程项视图
     * @param schedule 日程对象
     * @param height 日程项高度
     * @param customWidth 自定义宽度（-1表示使用默认MATCH_PARENT）
     * @return 日程项视图
     */
    private LinearLayout createScheduleItem(Schedule schedule, int height, int customWidth) {
        LinearLayout scheduleItem = new LinearLayout(this);
        LinearLayout.LayoutParams params;
        if (customWidth > 0) {
            params = new LinearLayout.LayoutParams(customWidth, height);
        } else {
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    height
            );
        }
        params.setMargins(0, 2, 0, 2);
        scheduleItem.setLayoutParams(params);
        scheduleItem.setOrientation(LinearLayout.VERTICAL);
        scheduleItem.setPadding(8, 4, 8, 4);
        scheduleItem.setBackgroundColor(getColorFromSchedule(schedule.getColor()));
        scheduleItem.setClickable(true);
        final int scheduleId = schedule.getId();
        scheduleItem.setTag(scheduleId);

        // 设置点击事件
        scheduleItem.setOnClickListener(v -> {
            // 使用 final 变量，避免类型转换问题
            Intent intent = new Intent(ViewActivity.this, EditScheduleActivity.class);
            intent.putExtra("schedule_id", scheduleId);
            startActivity(intent);
        });

        // 标题
        TextView titleView = new TextView(this);
        titleView.setText(schedule.getTitle());
        titleView.setTextSize(12);
        titleView.setTextColor(Color.WHITE);
        titleView.setSingleLine(true);
        scheduleItem.addView(titleView);

        // 时间
        TextView timeView = new TextView(this);
        timeView.setText(schedule.getTime());
        timeView.setTextSize(10);
        timeView.setTextColor(Color.WHITE);
        scheduleItem.addView(timeView);

        return scheduleItem;
    }

    /**
     * 根据日程颜色值获取颜色
     * @param colorIndex 颜色索引
     * @return 颜色值
     */
    private int getColorFromSchedule(int colorIndex) {
        final int[] COLORS = {
                R.color.color_0, R.color.color_1, R.color.color_2, R.color.color_3,
                R.color.color_4, R.color.color_5, R.color.color_6, R.color.color_7,
                R.color.color_8, R.color.color_9, R.color.orange
        };
        if (colorIndex >= 0 && colorIndex < COLORS.length) {
            return getResources().getColor(COLORS[colorIndex]);
        } else {
            // 默认返回橙色
            return getResources().getColor(R.color.orange);
        }
    }

    /**
     * 根据时间字符串获取时间槽索引
     * @param time 时间字符串（格式：HH:mm-HH:mm）
     * @return 时间槽索引
     */
    private int getTimeSlotIndex(String time) {
        if (time == null || time.isEmpty()) {
            return -1;
        }

        try {
            String[] parts = time.split("-");
            if (parts.length < 1) {
                return -1;
            }

            String startTime = parts[0];
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            Date date = timeFormat.parse(startTime);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            // 计算时间槽索引（每小时1个槽，从00:00开始）
            int slotIndex = hour;
            return slotIndex;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 切换视图模式
     * @param mode 视图模式
     */
    private void switchViewMode(int mode) {
        // 如果切换到不同的视图，先同步日期索引
        syncDateIndexWithSelectedDate();

        currentViewMode = mode;

        // 隐藏所有视图
        llDayView.setVisibility(View.GONE);
        llWeekView.setVisibility(View.GONE);
        llMonthView.setVisibility(View.GONE);

        // 显示选中的视图
        switch (mode) {
            case 0: // 日视图
                tvWeekView.setBackgroundColor(0x00000000);
                tvMonthView.setBackgroundColor(0x00000000);
                tvDayView.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light));
                llDayView.setVisibility(View.VISIBLE);
                refreshDateBarSelection();
                loadDayView();
                break;
            case 1: // 周视图
                tvDayView.setBackgroundColor(0x00000000);
                tvMonthView.setBackgroundColor(0x00000000);
                tvWeekView.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light));
                llWeekView.setVisibility(View.VISIBLE);
                refreshDateBarSelection();
                loadWeekView();
                break;
            case 2: // 月视图
                tvWeekView.setBackgroundColor(0x00000000);
                tvDayView.setBackgroundColor(0x00000000);
                tvMonthView.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light));
                llMonthView.setVisibility(View.VISIBLE);
                // 重新设置currentCalendar为选中日期，确保显示正确的月份
                try {
                    currentCalendar.setTime(sdf.parse(currentSelectedDate));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                refreshDateBarSelection();
                loadMonthView();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_menu) {
            drawerLayout.openDrawer(Gravity.LEFT);
        } else if (id == R.id.iv_add) {
            // 跳转到添加日程页面
            Intent intent = new Intent(this, AddScheduleActivity.class);
            intent.putExtra("selected_date", currentSelectedDate);
            startActivity(intent);
        } else if (id == R.id.tv_day_view) {
            switchViewMode(0);
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else if (id == R.id.tv_week_view) {
            switchViewMode(1);
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else if (id == R.id.tv_month_view) {
            switchViewMode(2);
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else if (id == R.id.ll_nav_schedule) {
            // 跳转到日程页面
            Intent intent = new Intent(ViewActivity.this, MainActivity.class);
            intent.putExtra("selected_date", currentSelectedDate);
            startActivity(intent);
            finish();
        } else if (id == R.id.ll_nav_view) {
            // 已经在视图页面，不做操作
        } else if (id == R.id.ll_nav_mine) {
            // 跳转到"我的"页面
            Intent intent = new Intent(ViewActivity.this, MyActivity.class);
            startActivity(intent);
        }
    }

    // GestureDetector.OnGestureListener接口方法实现
    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // 检测左右滑动手势
        float diffX = e2.getX() - e1.getX();
        float diffY = e2.getY() - e1.getY();

        // 检查是否是水平滑动（左右滑动）
        if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 100) {
            if (diffX > 0) {
                // 向右滑动，切换到下一个星期
                nextWeek();
            } else {
                // 向左滑动，切换到上一个星期（修复：原逻辑错误，应该是上一周而不是上一个月）
                prevWeek();
            }
            return true;
        }

        return false;
    }

    /**
     * 加载月视图
     */
    private void loadMonthView() {
        // 清空之前的视图
        glMonthGrid.removeAllViews();

        // 确保currentCalendar正确设置为选中日期所在的月份
        try {
            currentCalendar.setTime(sdf.parse(currentSelectedDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 获取当前月份的第一天
        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
        // 计算月份第一天需要向前填充的空格子数
        // Calendar.DAY_OF_WEEK: 周日=1, 周一=2, ..., 周六=7
        // 我们需要: 周一=0, 周二=1, ..., 周日=6
        // 所以空格子数 = firstDayOfWeek - 2 (周日时为 -1，需要特殊处理)
        int emptyCellsBefore;
        if (firstDayOfWeek == Calendar.SUNDAY) {
            emptyCellsBefore = 0;
        } else {
            emptyCellsBefore = firstDayOfWeek - 2;
        }

        // 获取当前月份的天数
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // 获取上个月的天数
        tempCal.add(Calendar.MONTH, -1);
        int daysInPrevMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        tempCal.add(Calendar.MONTH, 1);

        int prevMonthStartDay = daysInPrevMonth - emptyCellsBefore + 1;
        // 填充上个月的日期
        for (int i = 0; i <  emptyCellsBefore; i++) {
            int day =  prevMonthStartDay + i;
            View dateView = createDateCell(String.valueOf(day), null, false, null);
            glMonthGrid.addView(dateView);
        }

        // 填充当前月的日期
        for (int day = 1; day <= daysInMonth; day++) {
            // 设置日期
            tempCal.set(Calendar.DAY_OF_MONTH, day);
            String dateStr = sdf.format(tempCal.getTime());

            // 获取当前用户ID
            long userId = getSharedPreferences("user_info", MODE_PRIVATE).getLong("user_id", 0);
            if (userId == 0) {
                // 处理未登录用户的情况
                continue;
            }

            // 获取当天的日程（包括重复日程）
            List<Schedule> schedules = getSchedulesForDate(dateStr, (int) userId);

            // 创建日期单元格
            View dateView = createDateCell(String.valueOf(day), schedules, true, dateStr);
            glMonthGrid.addView(dateView);
        }

        // 填充下个月的日期
        int totalCellsFilled = emptyCellsBefore + daysInMonth;
        int remainingCells = 42 - totalCellsFilled; // 6行x7列=42个单元格
        for (int day = 1; day <= remainingCells; day++) {
            View dateView = createDateCell(String.valueOf(day), null, false, null);
            glMonthGrid.addView(dateView);
        }

        // 同步更新currentDateIndex，确保索引和视觉选中状态一致
        Calendar weekStartCal = (Calendar) currentCalendar.clone();
        weekStartCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        int originalDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
        int tempDay = weekStartCal.get(Calendar.DAY_OF_MONTH);
        if (tempDay > originalDay || currentCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            weekStartCal.add(Calendar.WEEK_OF_YEAR, -1);
        }

        for (int i = 0; i < 7; i++) {
            String weekDateStr = sdf.format(weekStartCal.getTime());
            if (weekDateStr.equals(currentSelectedDate)) {
                currentDateIndex = i;
                break;
            }
            weekStartCal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    /**
     * 创建日期单元格
     * @param day 日期
     * @param schedules 当天的日程列表
     * @param isCurrentMonth 是否是当前月份
     * @param dateStr 日期字符串（yyyy-MM-dd格式）
     * @return 日期单元格视图
     */
    private View createDateCell(String day, List<Schedule> schedules, boolean isCurrentMonth, String dateStr) {
        // 创建日期单元格布局
        LinearLayout cellLayout = new LinearLayout(this);
        GridLayout.LayoutParams gridParams = new GridLayout.LayoutParams();
        gridParams.width = 0;
        gridParams.height = dpToPx(80); // 设置固定高度
        gridParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
        gridParams.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f);
        gridParams.setMargins(2, 2, 2, 2);
        cellLayout.setLayoutParams(gridParams);
        cellLayout.setOrientation(LinearLayout.VERTICAL);
        cellLayout.setBackgroundResource(R.drawable.date_selector);
        cellLayout.setPadding(8, 8, 8, 8);

        // 创建日期文本视图
        TextView dateView = new TextView(this);
        dateView.setText(day);
        dateView.setTextSize(16);
        dateView.setGravity(Gravity.CENTER_HORIZONTAL);
        dateView.setTag(day);

        // 检查是否是当前选中的日期
        boolean isSelected = isCurrentMonth && dateStr != null && dateStr.equals(currentSelectedDate);

        // 设置日期颜色和选中状态
        if (isCurrentMonth) {
            if (isSelected) {
                cellLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_light));
            } else {
                cellLayout.setBackgroundResource(R.drawable.date_selector);
            }
        } else {
            dateView.setTextColor(Color.GRAY);
        }

        // 如果是当前月份的日期，添加点击事件
        if (isCurrentMonth && dateStr != null) {
            cellLayout.setOnClickListener(v -> {
                selectDateByString(dateStr);
            });
        }

        // 添加日期到单元格布局
        cellLayout.addView(dateView);

        // 添加日程标签
        if (schedules != null && !schedules.isEmpty()) {
            // 最多显示3个日程
            int maxSchedules = Math.min(schedules.size(), 3);
            for (int i = 0; i < maxSchedules; i++) {
                Schedule schedule = schedules.get(i);
                TextView scheduleView = new TextView(this);
                scheduleView.setText(schedule.getTitle());
                scheduleView.setTextSize(10);
                scheduleView.setSingleLine(true);
                scheduleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
                scheduleView.setBackgroundColor(getColorFromSchedule(schedule.getColor()));
                scheduleView.setTextColor(Color.WHITE);
                scheduleView.setPadding(4, 2, 4, 2);
                LinearLayout.LayoutParams scheduleParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                scheduleParams.setMargins(0, 2, 0, 0);
                scheduleView.setLayoutParams(scheduleParams);

                // 设置点击事件
                scheduleView.setTag(schedule.getId());
                scheduleView.setOnClickListener(v -> {
                    int scheduleId = (int) v.getTag();
                    // 跳转到编辑页面
                    Intent intent = new Intent(ViewActivity.this, EditScheduleActivity.class);
                    intent.putExtra("schedule_id", scheduleId);
                    startActivity(intent);
                });

                cellLayout.addView(scheduleView);
            }
        }

        // 返回单元格布局
        return cellLayout;
    }

    /**
     * 选择月视图中的日期
     * @param day 日期
     */
    private void selectMonthDate(int day) {
        // 更新当前选中的日期
        Calendar tempCal = (Calendar) currentCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, day);
        currentSelectedDate = sdf.format(tempCal.getTime());

        // 更新日期栏
        loadWeekDates();

        // 切换到日视图
        switchViewMode(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 重新加载视图
        if (currentViewMode == 0) {
            loadDayView();
        } else if (currentViewMode == 1) {
            loadWeekView();
        } else if (currentViewMode == 2) {
            loadMonthView();
        }
    }
}