package com.example.calendarapplication.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.calendarapplication.R;
import com.example.calendarapplication.db.ScheduleDAO;
import com.example.calendarapplication.model.Schedule;
import com.example.calendarapplication.ui.EditScheduleActivity;
import com.example.calendarapplication.ui.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuadrantAdapter {
    private Context mContext;
    private List<Schedule> mAllSchedules;
    private ScheduleDAO mScheduleDAO;
    private int mUserId;
    private List<Schedule>[] mQuadrantSchedules = new List[4];
    private final int[] QUADRANT_TITLE_RES={
            R.string.quadrant_1_title,
            R.string.quadrant_2_title,
            R.string.quadrant_3_title,
            R.string.quadrant_4_title
    };
    private final int[] QUADRANT_BACKGROUNDS={
            R.drawable.bg_quadrant_1,
            R.drawable.bg_quadrant_2,
            R.drawable.bg_quadrant_3,
            R.drawable.bg_quadrant_4
    };
    private final int[] QUADRANT_TEXT_COLORS={
            R.color.quadrant_1_text,
            R.color.quadrant_2_text,
            R.color.quadrant_3_text,
            R.color.quadrant_4_text
    };

    public QuadrantAdapter(Context context, List<Schedule> schedules, ScheduleDAO scheduleDAO, int userId) {
        this.mContext = context;
        this.mAllSchedules = schedules;
        this.mScheduleDAO = scheduleDAO;
        this.mUserId = userId;
        initQuadrantData();
    }

    private void initQuadrantData() {
        for(int i=0;i<4;i++){
            mQuadrantSchedules[i]=new ArrayList<>();
        }
        categorizeSchedules();
    }

    private void categorizeSchedules() {
        for(Schedule schedule:mAllSchedules){
            int quadrant=schedule.getQuadrant();
            if(quadrant<0||quadrant>=4){
                quadrant=3;
            }
            mQuadrantSchedules[quadrant].add(schedule);
        }
    }

    public void bindToContainer(LinearLayout container){
        container.removeAllViews();
        LinearLayout gridContainer=new LinearLayout(mContext);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        container.addView(gridContainer,new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout row1=new LinearLayout(mContext);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setWeightSum(2);
        gridContainer.addView(row1, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        LinearLayout row2=new LinearLayout(mContext);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setWeightSum(2);
        gridContainer.addView(row2,new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        for(int i=0;i<4;i++){
            View quadrantCard= LayoutInflater.from(mContext).inflate(R.layout.item_quadrant_card, null);
            setQuadrantCardStyle(quadrantCard, i);
            addSchedulesToCard(quadrantCard, mQuadrantSchedules[i]);

            LinearLayout.LayoutParams cardParams=new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1
            );
            cardParams.setMargins(8,8,8,8);

            if(i<2){
                row1.addView(quadrantCard, cardParams);
            }else{
                row2.addView(quadrantCard,cardParams);
            }
        }
    }

    private void setQuadrantCardStyle(View cardView, int quadrant) {
        cardView.setBackgroundResource(QUADRANT_BACKGROUNDS[quadrant]);
        TextView titleView = cardView.findViewById(R.id.tv_quadrant_title);
        titleView.setText(mContext.getString(QUADRANT_TITLE_RES[quadrant]));
        titleView.setTextColor(mContext.getResources().getColor(QUADRANT_TEXT_COLORS[quadrant]));
        titleView.setVisibility(View.VISIBLE);
        titleView.setTextSize(16);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setPadding(16, 16, 16, 8);
    }

    private void addSchedulesToCard(View cardView, List<Schedule> schedules){
        LinearLayout scheduleContainer=cardView.findViewById(R.id.ll_schedules);
        for(Schedule schedule:schedules){
            View scheduleItem=LayoutInflater.from(mContext).inflate(R.layout.item_schedule,null);
            bindScheduleData(scheduleItem, schedule);
            scheduleContainer.addView(scheduleItem);
        }
    }

    private void bindScheduleData(View itemView, Schedule schedule) {
        CheckBox checkBox = itemView.findViewById(R.id.cb_schedule);
        TextView titleView = itemView.findViewById(R.id.tv_title);
        TextView timeView = itemView.findViewById(R.id.tv_time);
        TextView overdueView = itemView.findViewById(R.id.tv_overdue);
        Button btnDelete = itemView.findViewById(R.id.btn_delete);
        LinearLayout llContent = itemView.findViewById(R.id.ll_content);

        boolean isCompleted = schedule.getCompleted() == 1;

        checkBox.setChecked(isCompleted);
        titleView.setText(schedule.getTitle());
        timeView.setText(schedule.getDate() + " " + schedule.getTime());

        if (isCompleted) {
            titleView.setPaintFlags(titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            titleView.setTextColor(android.graphics.Color.GRAY);
            timeView.setTextColor(android.graphics.Color.DKGRAY);
        } else {
            titleView.setPaintFlags(titleView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            titleView.setTextColor(android.graphics.Color.BLACK);
            timeView.setTextColor(android.graphics.Color.DKGRAY);
        }

        boolean isOverdue = isScheduleOverdue(schedule.getDate(), schedule.getTime());
        if (!isCompleted && isOverdue) {
            overdueView.setVisibility(View.VISIBLE);
        } else {
            overdueView.setVisibility(View.GONE);
        }

        final float[] lastX = {0};
        final float[] startXY = new float[2];
        final boolean[] isSliding = new boolean[1];
        final int scheduleId = schedule.getId();
        final Schedule finalSchedule = schedule;

        itemView.setOnTouchListener((v, event) -> {
            float currentX = event.getX();
            float currentY = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX[0] = currentX;
                    startXY[0] = currentX;
                    startXY[1] = currentY;
                    isSliding[0] = false;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = currentX - lastX[0];
                    float currentTranslateX = llContent.getTranslationX();
                    float newTranslateX = currentTranslateX + dx;
                    
                    float slideDistanceX = Math.abs(currentX - startXY[0]);
                    float slideDistanceY = Math.abs(currentY - startXY[1]);
                    
                    if (slideDistanceX > slideDistanceY && slideDistanceX > 10) {
                        isSliding[0] = true;
                    }
                    
                    if (newTranslateX < 0 && Math.abs(newTranslateX) <= 80) {
                        llContent.setTranslationX(newTranslateX);
                        btnDelete.setVisibility(View.VISIBLE);
                    }
                    lastX[0] = currentX;
                    break;
                case MotionEvent.ACTION_UP:
                    float finalTranslateX = llContent.getTranslationX();
                    if (Math.abs(finalTranslateX) > 40) {
                        llContent.setTranslationX(-80);
                        btnDelete.setVisibility(View.VISIBLE);
                    } else {
                        llContent.setTranslationX(0);
                        btnDelete.setVisibility(View.GONE);
                    }
                    isSliding[0] = false;
                    break;
            }
            return false;
        });

        checkBox.setOnClickListener(v -> {
            boolean isChecked = checkBox.isChecked();
            int newStatus = isChecked ? 1 : 0;

            new Thread(() -> {
                mScheduleDAO.updateScheduleCompleted(scheduleId, newStatus, mUserId);
            }).start();

            if (isChecked) {
                titleView.setPaintFlags(titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                titleView.setTextColor(android.graphics.Color.GRAY);
                timeView.setTextColor(android.graphics.Color.DKGRAY);
            } else {
                titleView.setPaintFlags(titleView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                titleView.setTextColor(android.graphics.Color.BLACK);
                timeView.setTextColor(android.graphics.Color.DKGRAY);
            }

            finalSchedule.setCompleted(newStatus);
        });

        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, EditScheduleActivity.class);
            intent.putExtra("schedule_id", scheduleId);
            mContext.startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(mContext)
                .setTitle("删除确认")
                .setMessage("确定要删除这个日程吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    new Thread(() -> {
                        mScheduleDAO.deleteSchedule(scheduleId, mUserId);
                    }).start();
                    if (mContext instanceof MainActivity) {
                        ((MainActivity) mContext).loadQuadrants();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        });
    }

    private boolean isScheduleOverdue(String date, String time) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String scheduleEndTime = date + " " + time.split("-")[1];
            Date nowDate = dateFormat.parse(dateFormat.format(new Date()));
            Date endDate = dateFormat.parse(scheduleEndTime);
            return nowDate != null && endDate != null && nowDate.after(endDate);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
