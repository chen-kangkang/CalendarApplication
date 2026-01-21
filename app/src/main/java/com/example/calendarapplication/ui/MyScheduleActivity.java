package com.example.calendarapplication.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.calendarapplication.R;
import com.example.calendarapplication.adapter.ScheduleRecyclerAdapter;
import com.example.calendarapplication.db.ScheduleDAO;
import com.example.calendarapplication.model.Schedule;
import com.example.calendarapplication.utils.DateUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyScheduleActivity extends AppCompatActivity implements View.OnClickListener, ScheduleRecyclerAdapter.OnScheduleClickListener {

    private static final float CLICK_THRESHOLD_DP = 20f;
    private static final float SWIPE_HORIZONTAL_THRESHOLD_DP = 20f;
    private static final float SWIPE_VERTICAL_THRESHOLD_DP = 10f;
    private static final int PAGE_SIZE = 20;

    private float clickThreshold;
    private float swipeHorizontalThreshold;
    private float swipeVerticalThreshold;

    private ImageView ivBack;
    private TextView tvFilter;
    private ImageView ivDropdown;
    private ImageView ivSearch;
    private RecyclerView rvSchedules;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvLoadMore;
    private LinearLayout llLoadMore;

    private PopupWindow filterPopup;
    private LinearLayout llPopup;
    private TextView tvAll;
    private TextView tvUncompleted;
    private TextView tvCompleted;
    private TextView tvOverdue;

    private ScheduleDAO scheduleDAO;
    private Map<String, List<Schedule>> groupedSchedules;
    private List<String> dateList;
    private ScheduleRecyclerAdapter adapter;
    private int currentFilter = 0;
    private int userId;

    // 懒加载配置
    private static final int DAYS_PER_LOAD = 14; // 每次加载14天的数据
    private Calendar startCal; // 当前显示的开始日期
    private Calendar endCal; // 当前显示的结束日期
    private boolean isLoading = false;
    private boolean hasMoreHistory = true; // 是否有更多历史数据
    private boolean hasMoreFuture = true; // 是否有更多未来数据
    private List<Schedule> allDisplayedSchedules = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_schedule);

        initViews();
        initData();
        setListeners();
        loadAllSchedules();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvFilter = findViewById(R.id.tv_filter);
        ivDropdown = findViewById(R.id.iv_dropdown);
        ivSearch = findViewById(R.id.iv_search);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rvSchedules = findViewById(R.id.rv_schedules);
        rvSchedules.setLayoutManager(new LinearLayoutManager(this));

        tvLoadMore = findViewById(R.id.tv_load_more);
        llLoadMore = findViewById(R.id.ll_load_more);
    }

    private void initData() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String phone = sharedPreferences.getString("phone", null);
        if (phone == null) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        long userIdLong = sharedPreferences.getLong("user_id", 0);
        userId = userIdLong > 0 ? (int) userIdLong : 1; // 兼容默认值

        scheduleDAO = new ScheduleDAO(this);
        groupedSchedules = new HashMap<>();
        dateList = new ArrayList<>();

        // 初始化日期范围：默认显示最近两周（今天±7天）
        startCal = Calendar.getInstance();
        startCal.add(Calendar.DAY_OF_MONTH, -7);
        endCal = Calendar.getInstance();
        endCal.add(Calendar.DAY_OF_MONTH, 7);

        initTouchThresholds();
    }

    private void initTouchThresholds() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        float density = displayMetrics.density;

        clickThreshold = CLICK_THRESHOLD_DP * density;
        swipeHorizontalThreshold = SWIPE_HORIZONTAL_THRESHOLD_DP * density;
        swipeVerticalThreshold = SWIPE_VERTICAL_THRESHOLD_DP * density;
    }

    private void setListeners() {
        ivBack.setOnClickListener(this);
        tvFilter.setOnClickListener(this);
        ivDropdown.setOnClickListener(this);
        ivSearch.setOnClickListener(this);

        initFilterPopup();

        swipeRefresh.setColorSchemeResources(android.R.color.holo_orange_light);

        rvSchedules.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                    int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                    // 上拉到底部，加载未来数据（更晚的日期）
                    if (totalItemCount <= lastVisibleItem + 3 && !isLoading && hasMoreFuture) {
                        android.util.Log.d("MySchedule", "上拉加载未来数据");
                        loadFutureData();
                    }
                }
            }
        });

        // 使用 swipeRefresh 下拉刷新来加载历史数据
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoading && hasMoreHistory) {
                    android.util.Log.d("MySchedule", "下拉刷新加载历史数据");
                    loadMoreData();
                } else {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });
    }

    private void initFilterPopup() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View popupView = inflater.inflate(R.layout.popup_filter, null);
        filterPopup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        filterPopup.setOutsideTouchable(true);

        llPopup = popupView.findViewById(R.id.ll_popup);
        tvAll = popupView.findViewById(R.id.tv_all);
        tvUncompleted = popupView.findViewById(R.id.tv_uncompleted);
        tvCompleted = popupView.findViewById(R.id.tv_completed);
        tvOverdue = popupView.findViewById(R.id.tv_overdue);

        tvAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = 0;
                updateFilterText("全部");
                filterPopup.dismiss();
                loadAllSchedules();
            }
        });

        tvUncompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = 1;
                updateFilterText("未完成");
                filterPopup.dismiss();
                loadAllSchedules();
            }
        });

        tvCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = 2;
                updateFilterText("已完成");
                filterPopup.dismiss();
                loadAllSchedules();
            }
        });

        tvOverdue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentFilter = 3;
                updateFilterText("已逾期");
                filterPopup.dismiss();
                loadAllSchedules();
            }
        });
    }

    private void updateFilterText(String text) {
        tvFilter.setText(text);
    }

    private void loadAllSchedules() {
        if (isLoading) return;
        isLoading = true;

        if (llLoadMore != null) {
            llLoadMore.setVisibility(View.VISIBLE);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                String startDate = DateUtils.formatDate(startCal.getTime());
                String endDate = DateUtils.formatDate(endCal.getTime());
                android.util.Log.d("MySchedule", "loadAllSchedules: userId=" + userId + ", startDate=" + startDate + ", endDate=" + endDate);
                final List<Schedule> originalSchedules = scheduleDAO.querySchedulesByDateRange(userId, startDate, endDate);
                android.util.Log.d("MySchedule", "querySchedulesByDateRange returned " + originalSchedules.size() + " schedules");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isLoading = false;
                        android.util.Log.d("MySchedule", "Processing on UI thread, schedules size: " + originalSchedules.size());

                        allDisplayedSchedules.clear();
                        allDisplayedSchedules.addAll(originalSchedules);
                        android.util.Log.d("MySchedule", "allDisplayedSchedules size: " + allDisplayedSchedules.size());
                        processSchedules(allDisplayedSchedules);

                        // 检查是否有更多历史数据
                        String earliestDate = DateUtils.formatDate(startCal.getTime());
                        hasMoreHistory = scheduleDAO.hasSchedulesBeforeDate(userId, earliestDate);
                        android.util.Log.d("MySchedule", "hasMoreHistory: " + hasMoreHistory);

                        // 检查是否有更多未来数据
                        String latestDate = DateUtils.formatDate(endCal.getTime());
                        hasMoreFuture = scheduleDAO.hasSchedulesAfterDate(userId, latestDate);
                        android.util.Log.d("MySchedule", "hasMoreFuture: " + hasMoreFuture);

                        // 更新加载更多提示
                        updateLoadMoreHint();

                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private void refreshData() {
        // 重置日期范围为最近两周
        startCal = Calendar.getInstance();
        startCal.add(Calendar.DAY_OF_MONTH, -7);
        endCal = Calendar.getInstance();
        endCal.add(Calendar.DAY_OF_MONTH, 7);
        hasMoreHistory = true;
        hasMoreFuture = true;
        allDisplayedSchedules.clear();
        loadAllSchedules();
    }

    private void loadMoreData() {
        if (isLoading || !hasMoreHistory) return;
        isLoading = true;

        // 计算新的历史日期范围
        Calendar newStartCal = (Calendar) startCal.clone();
        newStartCal.add(Calendar.DAY_OF_MONTH, -DAYS_PER_LOAD);
        String newStartDate = DateUtils.formatDate(newStartCal.getTime());
        // 计算前一天的日期，避免重复
        Calendar dayBeforeStartCal = (Calendar) startCal.clone();
        dayBeforeStartCal.add(Calendar.DAY_OF_MONTH, -1);
        String dayBeforeStartDate = DateUtils.formatDate(dayBeforeStartCal.getTime());

        android.util.Log.d("MySchedule", "loadMoreData: newStartDate=" + newStartDate + ", dayBeforeStartDate=" + dayBeforeStartDate);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 只查询新增的历史数据（不包含当前startDate）
                final List<Schedule> newHistorySchedules = scheduleDAO.querySchedulesByDateRange(userId, newStartDate, dayBeforeStartDate);
                android.util.Log.d("MySchedule", "loadMoreData returned " + newHistorySchedules.size() + " new history schedules");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isLoading = false;

                        if (!newHistorySchedules.isEmpty()) {
                            // 计算新增数据的扁平项数量
                            int newItemCount = calculateFlatItemCount(newHistorySchedules);
                            android.util.Log.d("MySchedule", "newItemCount: " + newItemCount);
                            
                            // 添加到列表开头
                            allDisplayedSchedules.addAll(0, newHistorySchedules);
                            
                            // 重新处理并更新列表
                            processSchedules(allDisplayedSchedules);
                            
                            // 调整滚动位置，保持用户当前查看的位置
                            if (newItemCount > 0 && rvSchedules.getLayoutManager() != null) {
                                rvSchedules.scrollToPosition(newItemCount);
                            }
                            
                            // 更新开始日期
                            startCal = newStartCal;
                            
                            // 检查是否还有更多历史数据
                            String earliestDate = DateUtils.formatDate(startCal.getTime());
                            hasMoreHistory = scheduleDAO.hasSchedulesBeforeDate(userId, earliestDate);
                            android.util.Log.d("MySchedule", "hasMoreHistory: " + hasMoreHistory);
                        } else {
                            hasMoreHistory = false;
                        }
                        
                        // 关闭下拉刷新
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private void loadFutureData() {
        if (isLoading || !hasMoreFuture) return;
        isLoading = true;

        // 计算新的未来日期范围
        // 计算后一天的日期，避免重复
        Calendar dayAfterEndCal = (Calendar) endCal.clone();
        dayAfterEndCal.add(Calendar.DAY_OF_MONTH, 1);
        String dayAfterEndDate = DateUtils.formatDate(dayAfterEndCal.getTime());
        
        Calendar newEndCal = (Calendar) endCal.clone();
        newEndCal.add(Calendar.DAY_OF_MONTH, DAYS_PER_LOAD);
        String newEndDate = DateUtils.formatDate(newEndCal.getTime());

        android.util.Log.d("MySchedule", "loadFutureData: dayAfterEndDate=" + dayAfterEndDate + ", newEndDate=" + newEndDate);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // 只查询新增的未来数据（不包含当前endDate）
                final List<Schedule> newFutureSchedules = scheduleDAO.querySchedulesByDateRange(userId, dayAfterEndDate, newEndDate);
                android.util.Log.d("MySchedule", "loadFutureData returned " + newFutureSchedules.size() + " new future schedules");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isLoading = false;

                        if (!newFutureSchedules.isEmpty()) {
                            // 添加到列表末尾
                            allDisplayedSchedules.addAll(newFutureSchedules);
                            
                            // 重新处理并更新列表
                            processSchedules(allDisplayedSchedules);
                            
                            // 更新结束日期
                            endCal = newEndCal;
                            
                            // 检查是否还有更多未来数据
                            String latestDate = DateUtils.formatDate(endCal.getTime());
                            hasMoreFuture = scheduleDAO.hasSchedulesAfterDate(userId, latestDate);
                            android.util.Log.d("MySchedule", "hasMoreFuture: " + hasMoreFuture);
                        } else {
                            hasMoreFuture = false;
                        }
                        
                        // 关闭下拉刷新
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private void updateLoadMoreHint() {
        // 隐藏加载更多提示文字
        if (llLoadMore != null) {
            llLoadMore.setVisibility(View.GONE);
        }
    }

    /**
     * 计算日程列表转换为扁平列表后的项数
     * @param schedules 日程列表
     * @return 扁平列表项数
     */
    private int calculateFlatItemCount(List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return 0;
        }

        // 按日期分组
        Map<String, List<Schedule>> tempGrouped = new HashMap<>();
        for (Schedule schedule : schedules) {
            String date = schedule.getDate();
            if (!tempGrouped.containsKey(date)) {
                tempGrouped.put(date, new ArrayList<>());
            }
            tempGrouped.get(date).add(schedule);
        }

        // 扁平项数 = 日期头数量 + 日程数量
        return tempGrouped.size() + schedules.size();
    }

    private void processSchedules(List<Schedule> schedules) {
        android.util.Log.d("MySchedule", "processSchedules: input size=" + schedules.size());
        List<Schedule> displayedSchedules = generateDisplayedSchedules(schedules);
        android.util.Log.d("MySchedule", "generateDisplayedSchedules returned " + displayedSchedules.size());
        List<Schedule> filteredSchedules = applyFilter(displayedSchedules);
        android.util.Log.d("MySchedule", "applyFilter returned " + filteredSchedules.size());
        groupAndSortSchedules(filteredSchedules);
        android.util.Log.d("MySchedule", "groupAndSortSchedules: groupedSchedules size=" + groupedSchedules.size() + ", dateList size=" + dateList.size());

        List<Schedule> flatSchedules = flattenSchedules(groupedSchedules);
        android.util.Log.d("MySchedule", "flattenSchedules returned " + flatSchedules.size());

        if (adapter == null) {
            android.util.Log.d("MySchedule", "Creating new adapter with " + flatSchedules.size() + " items");
            adapter = new ScheduleRecyclerAdapter(this, flatSchedules, scheduleDAO, userId, this);
            rvSchedules.setAdapter(adapter);
        } else {
            android.util.Log.d("MySchedule", "Updating adapter data with " + flatSchedules.size() + " items");
            adapter.updateData(flatSchedules);
        }
    }

    private List<Schedule> flattenSchedules(Map<String, List<Schedule>> groupedSchedules) {
        List<Schedule> result = new ArrayList<>();
        for (String date : dateList) {
            Schedule dateHeader = new Schedule();
            dateHeader.setDate(formatDateForHeader(date));
            dateHeader.setDateHeader(true);
            result.add(dateHeader);

            List<Schedule> schedules = groupedSchedules.get(date);
            if (schedules != null) {
                result.addAll(schedules);
            }
        }
        return result;
    }

    private String formatDateForHeader(String dateStr) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA);
            java.util.Date date = inputFormat.parse(dateStr);
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("yyyy年M月d日", java.util.Locale.CHINA);
            return outputFormat.format(date);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
            return dateStr;
        }
    }

    private List<Schedule> generateDisplayedSchedules(List<Schedule> originalSchedules) {
        android.util.Log.d("MySchedule", "generateDisplayedSchedules: input size=" + (originalSchedules != null ? originalSchedules.size() : 0));
        List<Schedule> result = new ArrayList<>();
        if (originalSchedules == null || originalSchedules.isEmpty()) {
            android.util.Log.d("MySchedule", "generateDisplayedSchedules: input is empty");
            return result;
        }

        // 显示所有日程（包括普通日程、重复日程的独立记录和未来日程）
        // 因为重复日程已经为每个日期创建了独立记录
        for (Schedule schedule : originalSchedules) {
            android.util.Log.d("MySchedule", "Schedule date=" + schedule.getDate() + ", title=" + schedule.getTitle());
            result.add(schedule);
        }
        android.util.Log.d("MySchedule", "generateDisplayedSchedules: output size=" + result.size());
        return result;
    }

    private List<Schedule> applyFilter(List<Schedule> schedules) {
        List<Schedule> result = new ArrayList<>();
        Date now = new Date();

        for (Schedule schedule : schedules) {
            boolean isOverdue = false;
            Date scheduleDateTime = DateUtils.parseDateTime(schedule.getDate(), schedule.getTime());
            if (scheduleDateTime != null && scheduleDateTime.before(now) && schedule.getCompleted() != 1) {
                isOverdue = true;
            }

            switch (currentFilter) {
                case 0:
                    result.add(schedule);
                    break;
                case 1:
                    if (schedule.getCompleted() != 1) {
                        result.add(schedule);
                    }
                    break;
                case 2:
                    if (schedule.getCompleted() == 1) {
                        result.add(schedule);
                    }
                    break;
                case 3:
                    if (isOverdue) {
                        result.add(schedule);
                    }
                    break;
            }
        }

        return result;
    }

    private void groupAndSortSchedules(List<Schedule> schedules) {
        groupedSchedules.clear();
        dateList.clear();

        for (Schedule schedule : schedules) {
            String date = schedule.getDate();
            if (!groupedSchedules.containsKey(date)) {
                groupedSchedules.put(date, new ArrayList<>());
                dateList.add(date);
            }
            groupedSchedules.get(date).add(schedule);
        }

        dateList.sort(new Comparator<String>() {
            @Override
            public int compare(String date1, String date2) {
                try {
                    Date d1 = DateUtils.parseDate(date1);
                    Date d2 = DateUtils.parseDate(date2);
                    return d1.compareTo(d2);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });

        for (String date : dateList) {
            groupedSchedules.get(date).sort(new Comparator<Schedule>() {
                @Override
                public int compare(Schedule s1, Schedule s2) {
                    String time1 = s1.getTime() != null ? s1.getTime() : "";
                    String time2 = s2.getTime() != null ? s2.getTime() : "";
                    return time1.compareTo(time2);
                }
            });
        }
    }

    @Override
    public void onScheduleClick(long scheduleId) {
        Intent intent = new Intent(this, EditScheduleActivity.class);
        intent.putExtra("schedule_id", (int)scheduleId);
        startActivity(intent);
    }

    @Override
    public void onScheduleChecked(long scheduleId, boolean isChecked) {
        scheduleDAO.updateScheduleCompleted((int) scheduleId, isChecked ? 1 : 0, userId);
        loadAllSchedules();
    }

    @Override
    public void onSwipeLeft(long scheduleId) {
        scheduleDAO.deleteSchedule((int) scheduleId, userId);
        Toast.makeText(this, "日程已删除", Toast.LENGTH_SHORT).show();
        loadAllSchedules();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        } else if (v.getId() == R.id.tv_filter || v.getId() == R.id.iv_dropdown) {
            if (filterPopup != null && !filterPopup.isShowing()) {
                filterPopup.showAsDropDown(tvFilter);
            }
        } else if (v.getId() == R.id.iv_search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        }
    }
}
