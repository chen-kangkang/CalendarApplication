package com.example.calendarapplication.ui;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.calendarapplication.R;
import com.example.calendarapplication.adapter.ScheduleRecyclerAdapter;
import com.example.calendarapplication.db.ScheduleDAO;
import com.example.calendarapplication.model.Schedule;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener, ScheduleRecyclerAdapter.OnScheduleClickListener {

    private EditText etSearch;
    private TextView tvCancel;
    private RecyclerView rvSearchResults;
    private LinearLayout llInitialPrompt;
    private LinearLayout llEmptyResult;

    private ScheduleDAO scheduleDAO;
    private List<Schedule> searchResults;
    private ScheduleRecyclerAdapter adapter;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // 检查登录状态，未登录则跳转到登录页面
        if (!isUserLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
        initData();
        setListeners();
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

    @Override
    public void onScheduleClick(long scheduleID) {
        Intent intent = new Intent(SearchActivity.this, EditScheduleActivity.class);
        intent.putExtra("schedule_id", (int)scheduleID);
        startActivity(intent);
    }

    @Override
    public void onScheduleChecked(long scheduleId, boolean isCompleted) {
        new Thread(() -> {
            scheduleDAO.updateScheduleCompleted((int) scheduleId, isCompleted ? 1 : 0, userId);
        }).start();
    }

    @Override
    public void onSwipeLeft(long scheduleId) {
    }

    private void initViews() {
        etSearch = findViewById(R.id.et_search);
        tvCancel = findViewById(R.id.tv_cancel);
        rvSearchResults = findViewById(R.id.rv_search_results);
        llInitialPrompt = findViewById(R.id.ll_initial_prompt);
        llEmptyResult = findViewById(R.id.ll_empty_result);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initData() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String phone = sharedPreferences.getString("phone", null);

        long userIdLong = sharedPreferences.getLong("user_id", 0);
        // 兼容：如果没有存储user_id，用EditActivity的默认逻辑（避免硬编码1）
        userId = userIdLong > 0 ? (int) userIdLong : 0;

        scheduleDAO = new ScheduleDAO(this);
        searchResults = new ArrayList<>();

        adapter = new ScheduleRecyclerAdapter(this, searchResults, scheduleDAO, userId, this);
        rvSearchResults.setAdapter(adapter);
    }

    private void setListeners() {
        tvCancel.setOnClickListener(this);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    performSearch(s.toString());
                } else {
                    clearSearchResults();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void performSearch(String keyword) {
        searchResults.clear();

        List<Schedule> results = scheduleDAO.searchSchedulesByUserIdAndKeyword(userId, keyword);
        searchResults.addAll(results);

        updateSearchResults();
    }

    private void clearSearchResults() {
        searchResults.clear();
        llInitialPrompt.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
        llEmptyResult.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }

    private void updateSearchResults() {
        llInitialPrompt.setVisibility(View.GONE);
        
        if (searchResults.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            llEmptyResult.setVisibility(View.VISIBLE);
        } else {
            rvSearchResults.setVisibility(View.VISIBLE);
            llEmptyResult.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_cancel) {
            finish();
        }
    }
}
