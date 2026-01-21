package com.example.calendarapplication.ui;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;



import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DatePickerDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import com.example.calendarapplication.R;
import com.example.calendarapplication.adapter.MonthAdapter;
import com.example.calendarapplication.adapter.QuadrantAdapter;
import com.example.calendarapplication.adapter.ScheduleRecyclerAdapter;
import com.example.calendarapplication.db.ScheduleDAO;
import com.example.calendarapplication.model.Schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ScheduleRecyclerAdapter.OnScheduleClickListener{

    // 数据库和适配器
    private ScheduleDAO scheduleDAO; // 数据库操作对象：用于处理日程数据的增删改查

    private QuadrantAdapter quadrantAdapter;
    private MonthAdapter monthAdapter;

    // 日期与日历
    private String currentSelectedDate; // 当前选中的日期（格式：yyyy-MM-dd）
    private Calendar currentCalendar; // 系统日历实例：用于处理日期计算（如获取当前日期、切换日期等）
    private SimpleDateFormat sdf; // 用于在 String 和 Date/Calendar 对象之间进行格式化转换
    private SimpleDateFormat sdfMonth; // 年月格式化（yyyy.MM）
    private SimpleDateFormat sdfFull; // 完整日期格式化（yyyy.MM.dd）

    // 添加日程弹窗
    private Dialog addScheduleDialog;

    // UI组件
    private TextView tvDateTitle;      // 顶部日期标题（如：2025.11）
    private LinearLayout llWeekView;
    private GridView gvMonthView; // 月视图网格控件（展示整个月的日期）
    private ImageView ivChangeView;
    private LinearLayout llQuadrantContainer;

    // 周视图相关
    private TextView[] weekDayNumViews;

    // 月视图相关
    private List<String> monthDayList = new ArrayList<>(); // 月视图日期数据（包含空字符串占位）
    private int currentMonthDayIndex;  // 月视图中当前选中日期的索引（用于高亮显示）

    // 底部导航
    private LinearLayout llNavSchedule, llNavView, llNavMine;
    private ImageView ivNavSchedule, ivNavView, ivNavMine;

    // 状态标记
    private boolean isMonthViewVisible = false;
    private boolean isQuadrantMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 检查登录状态，未登录则跳转到登录页面
        if (!isUserLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        // 初始化基础数据和日历
        initData();
        // 初始化视图和点击事件
        initViews();
        // 调用返回键处理方法
        initBackPressedDispatcher();
        // 加载周视图日期
        loadWeekView();
        // 初始化为四象限模式
        switchToQuadrantMode();
    }


    /**
     * 初始化所有 UI 组件的引用，并为可点击的组件设置点击监听器。
     */
    private void initViews() {
        
        tvDateTitle = findViewById(R.id.tv_date);
        llWeekView = findViewById(R.id.ll_week_view);
        gvMonthView = findViewById(R.id.gv_month_view);
        ivChangeView = findViewById(R.id.iv_change_view);
        llQuadrantContainer = findViewById(R.id.ll_quadrant_container);
        // 底部导航视图
        llNavSchedule = findViewById(R.id.ll_nav_schedule);
        llNavView = findViewById(R.id.ll_nav_view);
        llNavMine = findViewById(R.id.ll_nav_mine);
        ivNavSchedule = findViewById(R.id.iv_nav_schedule);
        ivNavView = findViewById(R.id.iv_nav_view);
        ivNavMine = findViewById(R.id.iv_nav_mine);

        // 设置顶部日期标题（使用sdfMonth）
        tvDateTitle.setText(sdfMonth.format(currentCalendar.getTime()));

        findViewById(R.id.iv_add).setOnClickListener(this);
        ivChangeView.setOnClickListener(this);
        llNavSchedule.setOnClickListener(this);
        llNavView.setOnClickListener(this);
        llNavMine.setOnClickListener(this);
        
        // 日期选择下拉按钮
        ImageView ivDropdown = findViewById(R.id.iv_dropdown);
        ivDropdown.setOnClickListener(v -> {
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
                tvDateTitle.setText(sdfMonth.format(currentCalendar.getTime()));
                // 重新加载周视图和日程数据
                loadWeekView();
                // 更新月视图
                if (monthAdapter != null) {
                    monthAdapter.updateMonth(currentCalendar);
                }
                if (isQuadrantMode) {
                    loadQuadrants();
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 初始化底部导航选中状态
        updateNavSelection(0); // 0: 日程, 1: 视图, 2: 我的
    }


    /**
     * 初始化基础数据（日期、数据库）
     */
    // 当前登录用户ID
    private long currentUserId;
    
    private void initData() {
        currentCalendar=Calendar.getInstance();
        sdf=new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());// 创建一个 SimpleDateFormat 实例，指定日期格式为 "年-月-日"，并使用默认语言环境
        sdfMonth = new SimpleDateFormat("yyyy.MM", Locale.getDefault());
        sdfFull = new SimpleDateFormat("yyyy.MM.dd", Locale.getDefault());
        currentSelectedDate=sdf.format(currentCalendar.getTime());
        scheduleDAO=new ScheduleDAO(this);// 创建 ScheduleDAO 的实例，用于后续的数据库操作。
        // 获取当前登录用户ID
        currentUserId = getCurrentUserId();
    }
    
    // 获取当前登录用户ID
    private long getCurrentUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        return sharedPreferences.getLong("user_id", 0);
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
     * 加载并显示当前选中日期的四象限视图。
     */
    public void loadQuadrants() {
        // 查询原始日程并筛选（复用重复规则处理逻辑）
        List<Schedule> originalSchedules = scheduleDAO.querySchedulesBeforeDate(currentSelectedDate, (int) currentUserId);
        List<Schedule> displayedSchedules = generateDisplayedSchedules(originalSchedules, currentSelectedDate);
        // 更新四象限容器
        llQuadrantContainer.removeAllViews();// 清空原有视图（避免重复添加）
        quadrantAdapter = new QuadrantAdapter(this, displayedSchedules, scheduleDAO, (int) currentUserId);// 初始化四象限适配器
        // 调用适配器的 bindToContainer 方法，将四象限的卡片视图动态添加到容器中。
        quadrantAdapter.bindToContainer(llQuadrantContainer);
    }

    /**
     * 根据原始日程和目标日期，生成应显示的日程列表（处理重复规则）
     * @param originalSchedules 数据库中查询到的原始日程（包含重复规则）
     * @param targetDate 目标日期（当前选中的日期，格式：yyyy-MM-dd）
     * @return 目标日期应显示的所有日程（包含重复生成的日程）
     */
    private List<Schedule> generateDisplayedSchedules(List<Schedule> originalSchedules, String targetDate) {
        List<Schedule> result = new ArrayList<>(); // 最终要返回的日程列表
        if(originalSchedules == null || originalSchedules.isEmpty())return result;// 无原始日程直接返回空
        
        try {
            // 将目标日期转换为Calendar对象（用于日期比较和计算）
            Calendar targetCal = Calendar.getInstance();
            targetCal.setTime(sdf.parse(targetDate));

            // 遍历所有原始日程，只显示日期匹配的记录
            // 因为AddScheduleActivity已经为每个重复日期创建了独立记录
            for (Schedule schedule : originalSchedules) {
                // 只显示日期等于目标日期的记录
                if (schedule.getDate().equals(targetDate)) {
                    result.add(schedule);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace(); // 日期解析异常（理论上不会发生，因日期格式统一）
        }
        return result;
    }

    public void onScheduleClick(long scheduleId){
        android.util.Log.d("MainActivity", "onScheduleClick called with scheduleId: " + scheduleId);
        Intent intent = new Intent(this, EditScheduleActivity.class);
        intent.putExtra("schedule_id", scheduleId);
        startActivity(intent);
    }

    @Override
    public void onScheduleChecked(long scheduleId, boolean isCompleted) {
        android.util.Log.d("MainActivity", "onScheduleChecked called with scheduleId: " + scheduleId + ", isCompleted: " + isCompleted);
        new Thread(() -> {
            scheduleDAO.updateScheduleCompleted((int) scheduleId, isCompleted ? 1 : 0, (int) currentUserId);
        }).start();
    }

    @Override
    public void onSwipeLeft(long scheduleId) {
        android.util.Log.d("MainActivity", "onSwipeLeft called with scheduleId: " + scheduleId);
    }

    /**
     * 加载周视图的日期按钮
     */
    private void loadWeekView() {
        // 1. 移除所有子视图，防止重复添加
        llWeekView.removeAllViews();

        // 2. 创建临时Calendar实例（关键：重新new一个Calendar，避免修改原currentCalendar）
        // 错误根源：直接clone currentCalendar会导致后续操作污染原对象，改用全新实例
        Calendar tempCal = Calendar.getInstance();
        // 先把临时日历设置为当前选中的日期（和currentCalendar保持一致）
        tempCal.setTimeInMillis(currentCalendar.getTimeInMillis());

        // 计算当前选中日期距离周一的偏移量（解决周日/跨月问题）
        int dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK); // 用tempCal获取星期，避免动原对象
        int offset = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
        tempCal.add(Calendar.DAY_OF_YEAR, -offset); // 定位到包含选中日期的周一

        // 3. 创建并添加每日视图
        for (int i = 0; i < 7; i++) {
            View dayView = View.inflate(this, R.layout.item_month_day, null);
            TextView tvDayNum = dayView.findViewById(R.id.tv_month_day);

            // 设置日期数字
            String dayStr = String.valueOf(tempCal.get(Calendar.DAY_OF_MONTH));
            tvDayNum.setText(dayStr);

            // 格式化当前日期为yyyy-MM-dd
            final String dateStr = sdf.format(tempCal.getTime());

            // 设置选中状态
            if (dateStr.equals(currentSelectedDate)) {
                tvDayNum.setBackgroundResource(R.drawable.shape_circle_orange);
                tvDayNum.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                tvDayNum.setBackgroundResource(R.drawable.shape_circle_white);
                tvDayNum.setTextColor(getResources().getColor(android.R.color.black));
            }

            // 日期点击事件（修复：点击日期仅更新选中状态，不修改周基准）
            dayView.setOnClickListener(v -> {
                // 1. 仅更新选中日期，不修改currentCalendar的周基准
                currentSelectedDate = dateStr;

                // 2. 直接刷新选中状态（不再重新计算整周）
                updateWeekViewSelectedState(dateStr);

                // 3. 刷新内容区（日程/四象限）和月视图
                // 使用选中的日期字符串解析出日期来更新显示
                try {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.setTime(sdf.parse(dateStr));
                    tvDateTitle.setText(sdfMonth.format(selectedCal.getTime())); // 更新顶部年月标题
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                
                if (monthAdapter != null) {
                    // 给月视图传递当前选中日期，确保切换回月视图时选中状态同步
                    Calendar temp = Calendar.getInstance();
                    try {
                        temp.setTime(sdf.parse(dateStr));
                        monthAdapter.updateMonth(temp, currentSelectedDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if (isQuadrantMode) {
                    loadQuadrants();
                }
            });

            // 设置布局参数
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            llWeekView.addView(dayView, params);

            // 临时日历向后推移一天（仅修改tempCal，不影响原currentCalendar）
            tempCal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    /**
     * 辅助方法：仅更新周视图选中状态（替代递归调用，性能更优）
     */
    private void updateWeekViewSelectedState(String selectedDateStr) {
        // 遍历已创建的日期视图，仅更新样式，不重复创建
        for (int i = 0; i < llWeekView.getChildCount(); i++) {
            View dayView = llWeekView.getChildAt(i);
            TextView tvDayNum = dayView.findViewById(R.id.tv_month_day);

            // 重新计算当前item对应的日期（和loadWeekView逻辑完全一致）
            Calendar tempCal = (Calendar) currentCalendar.clone();
            int dayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
            int offset = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
            tempCal.add(Calendar.DAY_OF_YEAR, -offset);
            tempCal.add(Calendar.DAY_OF_YEAR, i);
            String itemDateStr = sdf.format(tempCal.getTime());

            // 更新选中样式
            if (itemDateStr.equals(selectedDateStr)) {
                tvDayNum.setBackgroundResource(R.drawable.shape_circle_orange);
                tvDayNum.setTextColor(getResources().getColor(android.R.color.white));
            } else {
                tvDayNum.setBackgroundResource(R.drawable.shape_circle_white);
                tvDayNum.setTextColor(getResources().getColor(android.R.color.black));
            }
        }
    }

    /**
     * 当在月视图中选择一个日期时触发的回调方法。
     * @param date 选中的日期，格式为 "yyyy-MM-dd"。
     */
    private void onDateSelected(String date){
        currentSelectedDate=date;
        try{
            currentCalendar.setTime(sdf.parse(date));
            tvDateTitle.setText(sdfMonth.format(currentCalendar.getTime()));
        }catch (ParseException e){
            e.printStackTrace();
        }
        // 更新月视图选中状态，同时传递选中日期
        if(monthAdapter != null) {
            monthAdapter.updateMonth(currentCalendar, currentSelectedDate);
        }
        if(isQuadrantMode){
            loadQuadrants();
        }
    }

    /**
     * 加载并显示月视图。
     * 如果适配器已存在，则更新其数据；如果不存在，则创建新的适配器并设置给 GridView。
     */
    private void loadMonthView(){
        if(monthAdapter==null){
            // 构造函数需要传入上下文、当前的 Calendar 对象和一个日期选中的回调接口。
            monthAdapter = new MonthAdapter(this, currentCalendar, this::onDateSelected);
            // 将适配器设置给月视图的 GridView
            gvMonthView.setAdapter(monthAdapter);
        }else{
            // 如果适配器已存在，则调用其 updateMonth 方法来刷新月份数据，并传递选中日期
            monthAdapter.updateMonth(currentCalendar, currentSelectedDate);
        }
    }

    /**
     * 切换周视图和月视图的显示状态。
     */
    private void toggleCalendarView() {
        isMonthViewVisible=!isMonthViewVisible;
        if(isMonthViewVisible){
            // 显示月视图
            gvMonthView.setVisibility(View.VISIBLE);
            llWeekView.setVisibility(View.GONE);
            loadMonthView();
            ivChangeView.setImageResource(R.drawable.ic_expand_more);
        }else{
            // 显示周视图
            gvMonthView.setVisibility(View.GONE);
            llWeekView.setVisibility(View.VISIBLE);
            loadWeekView();
            // 设置按钮图标为展开
            ivChangeView.setImageResource(R.drawable.ic_collapse_less);
        }
    }

    /**
     * 切换到四象限视图模式。
     */
    private void switchToQuadrantMode(){
        if(!isQuadrantMode){
            // 不再需要切换tvCalendar和tvQuadrant的背景色，因为没有日历模式了
            isQuadrantMode=true;
            // 四象限容器始终可见
            llQuadrantContainer.setVisibility(View.VISIBLE);
            loadQuadrants();
        }
    }


    /**
     * 更新底部导航栏的选中状态。
     * @param index 要选中的选项索引 (0: 日程, 1: 视图, 2: 我的)。
     */
    private void updateNavSelection(int index){
        // 首先，将所有导航项设置为未选中状态
        ivNavSchedule.setImageResource(R.drawable.ic_nav_schedule_unselected);
        // getChildAt() 方法返回的是一个通用的 View 对象。它并没有 setTextColor() 这种特定于文本的方法。
        ((TextView) llNavSchedule.getChildAt(1)).setTextColor(getResources().getColor(android.R.color.darker_gray));

        ivNavView.setImageResource(R.drawable.ic_nav_view_unselected);
        ((TextView) llNavView.getChildAt(1)).setTextColor(getResources().getColor(android.R.color.darker_gray));

        ivNavMine.setImageResource(R.drawable.ic_nav_mine_unselected);
        ((TextView) llNavMine.getChildAt(1)).setTextColor(getResources().getColor(android.R.color.darker_gray));
        // 然后，根据传入的索引，将对应的导航项设置为选中状态。
        switch (index) {
            case 0: // 日程
                ivNavSchedule.setImageResource(R.drawable.ic_nav_schedule_selected);
                ((TextView) llNavSchedule.getChildAt(1)).setTextColor(getResources().getColor(R.color.orange));
                break;
            case 1: // 视图
                ivNavView.setImageResource(R.drawable.ic_nav_view_selected);
                ((TextView) llNavView.getChildAt(1)).setTextColor(getResources().getColor(R.color.orange));
                break;
            case 2: // 我的
                ivNavMine.setImageResource(R.drawable.ic_nav_mine_selected);
                ((TextView) llNavMine.getChildAt(1)).setTextColor(getResources().getColor(R.color.orange));
                break;
        }
    }

    /**
     * 统一处理所有组件的点击事件。
     * @param v 被点击的 View 组件。
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_menu) {
            // 菜单按钮已移除，侧边栏功能不存在
        } else if (id == R.id.iv_add) {
            // 跳转到添加日程页面
            Intent intent = new Intent(this, AddScheduleActivity.class);
            intent.putExtra("selected_date", currentSelectedDate);
            startActivity(intent);
        } else if (id == R.id.iv_change_view) {
            toggleCalendarView(); // 切换周/月视图
        } else if (id == R.id.ll_nav_schedule) {
            // 底部导航切换到四象限模式（列表模式已移除）
            switchToQuadrantMode();
            updateNavSelection(0);
        } else if (id == R.id.ll_nav_view) {
            // 跳转到视图页面
            Intent intent = new Intent(this, ViewActivity.class);
            startActivity(intent);
            updateNavSelection(1);
        } else if (id == R.id.ll_nav_mine) {
                // 跳转到"我的"页面
                Intent intent = new Intent(this, MyActivity.class);
                startActivity(intent);
                updateNavSelection(2);
        }
    }

    // 接收AddScheduleActivity的返回，刷新数据
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            // 刷新四象限视图
            if (isQuadrantMode) {
                loadQuadrants();
            }
        }
    }

    /**
     * 当Activity从后台返回时，刷新数据并将选中日期恢复为当天
     */
    @Override
    protected void onResume() {
        super.onResume();
        // 只在首次进入应用时才设置为当天日期
        // 从其他页面返回时保持用户选中的日期
        if (currentSelectedDate == null || currentSelectedDate.isEmpty()){
            currentCalendar = Calendar.getInstance();
            currentSelectedDate = sdf.format(currentCalendar.getTime());
            // 更新顶部年月标题
            tvDateTitle.setText(sdfMonth.format(currentCalendar.getTime()));
        }
        // 刷新所有视图
        loadWeekView();
        if (monthAdapter != null) {
            monthAdapter.updateMonth(currentCalendar);
        }
        if (isQuadrantMode) {
            loadQuadrants();
        }
    }

    /**
     * 处理返回键事件
     */
    private void initBackPressedDispatcher() {
        // 获取返回键调度器，添加回调处理
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // 直接退出页面（侧边栏已移除）
                finish();
            }
        });
    }

}