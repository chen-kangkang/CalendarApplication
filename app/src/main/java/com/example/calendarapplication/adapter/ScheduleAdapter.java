package com.example.calendarapplication.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.calendarapplication.R;
import com.example.calendarapplication.db.ScheduleDAO;
import com.example.calendarapplication.model.Schedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ScheduleAdapter extends BaseAdapter {
    private Context mContext;
    private List<Schedule> mSchedules;
    private ScheduleDAO mScheduleDAO;
    private int mUserId;
    private OnScheduleClickListener mOnScheduleClickListener;

    public interface OnScheduleClickListener {
        void onScheduleClick(long scheduleId);
        void onScheduleChecked(long scheduleId, boolean isChecked);
        void onSwipeLeft(long scheduleId);
    }

    public ScheduleAdapter(Context context, List<Schedule> schedules, ScheduleDAO scheduleDAO,
                           int userId, OnScheduleClickListener listener) {
        this.mContext = context;
        this.mSchedules = schedules;
        this.mScheduleDAO = scheduleDAO;
        this.mUserId = userId;
        this.mOnScheduleClickListener = listener;
    }

    @Override
    public int getCount() {
        return mSchedules != null ? mSchedules.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mSchedules != null ? mSchedules.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_schedule, parent, false);
            holder = new ViewHolder();
            holder.tvTitle = convertView.findViewById(R.id.tv_title);
            holder.tvTime = convertView.findViewById(R.id.tv_time);
            holder.cbSchedule = convertView.findViewById(R.id.cb_schedule);
            holder.btnDelete = convertView.findViewById(R.id.btn_delete);
            holder.tvOverdue = convertView.findViewById(R.id.tv_overdue);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Schedule schedule = mSchedules.get(position);
        if (schedule == null) return convertView;

        long scheduleId = schedule.getId();

        holder.tvTitle.setText(schedule.getTitle());
        holder.tvTime.setText(schedule.getTime());
        holder.cbSchedule.setChecked(schedule.getCompleted() == 1);

        if (schedule.getCompleted() == 1) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        boolean isCompleted = schedule.getCompleted() == 1;
        boolean isOverdue = isScheduleOverdue(schedule.getDate(), schedule.getTime());
        if (!isCompleted && isOverdue) {
            holder.tvOverdue.setVisibility(View.VISIBLE);
        } else {
            holder.tvOverdue.setVisibility(View.GONE);
        }

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(mContext)
                    .setTitle("删除确认")
                    .setMessage("确定要删除这个日程吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        new Thread(() -> {
                            mScheduleDAO.deleteSchedule((int) scheduleId, mUserId);
                        }).start();
                        mSchedules.remove(position);
                        notifyDataSetChanged();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        holder.itemView = convertView;
        convertView.setOnClickListener(v -> {
            if (mOnScheduleClickListener != null) {
                mOnScheduleClickListener.onScheduleClick(scheduleId);
            }
        });

        holder.cbSchedule.setOnClickListener(v -> {
            boolean isChecked = holder.cbSchedule.isChecked();
            schedule.setCompleted(isChecked ? 1 : 0);
            if (mOnScheduleClickListener != null) {
                mOnScheduleClickListener.onScheduleChecked(scheduleId, isChecked);
            }
        });

        return convertView;
    }

    public void updateData(List<Schedule> newSchedules) {
        this.mSchedules = newSchedules;
        notifyDataSetChanged();
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

    static class ViewHolder {
        TextView tvTitle;
        TextView tvTime;
        TextView tvOverdue;
        CheckBox cbSchedule;
        Button btnDelete;
        View itemView;
    }
}
