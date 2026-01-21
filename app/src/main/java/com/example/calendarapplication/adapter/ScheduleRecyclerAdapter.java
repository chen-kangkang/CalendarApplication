package com.example.calendarapplication.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calendarapplication.R;
import com.example.calendarapplication.db.ScheduleDAO;
import com.example.calendarapplication.model.Schedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ScheduleRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATE_HEADER = 0;
    private static final int VIEW_TYPE_SCHEDULE = 1;

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

    public ScheduleRecyclerAdapter(Context context, List<Schedule> schedules, ScheduleDAO scheduleDAO,
                                    int userId, OnScheduleClickListener listener) {
        this.mContext = context;
        this.mSchedules = schedules;
        this.mScheduleDAO = scheduleDAO;
        this.mUserId = userId;
        this.mOnScheduleClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Schedule schedule = mSchedules.get(position);
        if (schedule.isDateHeader()) {
            return VIEW_TYPE_DATE_HEADER;
        }
        return VIEW_TYPE_SCHEDULE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATE_HEADER) {
            View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(convertView);
        } else {
            View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_schedule, parent, false);
            return new ScheduleViewHolder(convertView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Schedule schedule = mSchedules.get(position);
        if (schedule == null) return;

        if (holder instanceof DateHeaderViewHolder) {
            DateHeaderViewHolder dateHeaderHolder = (DateHeaderViewHolder) holder;
            dateHeaderHolder.tvDateHeader.setText(schedule.getDate());
        } else if (holder instanceof ScheduleViewHolder) {
            ScheduleViewHolder scheduleHolder = (ScheduleViewHolder) holder;
            bindScheduleItem(scheduleHolder, schedule, position);
        }
    }

    private void bindScheduleItem(ScheduleViewHolder holder, Schedule schedule, int position) {
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

        holder.itemView.setTag(scheduleId);
        holder.itemView.setOnTouchListener((v, event) -> {
            android.util.Log.d("ScheduleAdapter", "Root RelativeLayout onTouch: action=" + event.getAction() + ", view=" + v);
            return false;
        });

        android.util.Log.d("ScheduleAdapter", "Setting click listener for item, scheduleId: " + scheduleId + ", listener: " + (mOnScheduleClickListener != null ? "set" : "NULL"));
        holder.itemView.setOnClickListener(v -> {
            android.util.Log.d("ScheduleAdapter", "=== Item onClick triggered ===");
            android.util.Log.d("ScheduleAdapter", "scheduleId: " + scheduleId);
            if (mOnScheduleClickListener != null) {
                android.util.Log.d("ScheduleAdapter", "Calling onScheduleClick with id: " + scheduleId);
                mOnScheduleClickListener.onScheduleClick(scheduleId);
            } else {
                android.util.Log.d("ScheduleAdapter", "ERROR: mOnScheduleClickListener is null!");
            }
        });

        holder.llContent.setTag(scheduleId);
        holder.llContent.setOnTouchListener((v, event) -> {
            android.util.Log.d("ScheduleAdapter", "llContent onTouch: action=" + event.getAction() + ", view=" + v);
            return false;
        });

        android.util.Log.d("ScheduleAdapter", "Setting checkbox click listener for scheduleId: " + scheduleId);
        holder.cbSchedule.setOnClickListener(v -> {
            android.util.Log.d("ScheduleAdapter", "=== Checkbox onClick triggered ===");
            android.util.Log.d("ScheduleAdapter", "scheduleId: " + scheduleId);
            android.util.Log.d("ScheduleAdapter", "current checked state: " + holder.cbSchedule.isChecked());
            if (mOnScheduleClickListener != null) {
                boolean isChecked = holder.cbSchedule.isChecked();
                android.util.Log.d("ScheduleAdapter", "Calling onScheduleChecked with id: " + scheduleId + ", checked: " + isChecked);
                schedule.setCompleted(isChecked ? 1 : 0);
                mOnScheduleClickListener.onScheduleChecked(scheduleId, isChecked);
            } else {
                android.util.Log.d("ScheduleAdapter", "ERROR: mOnScheduleClickListener is null!");
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSchedules != null ? mSchedules.size() : 0;
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

    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateHeader;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tv_date_header);
        }
    }

    class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvTime;
        TextView tvOverdue;
        CheckBox cbSchedule;
        Button btnDelete;
        View rootView;
        LinearLayout llContent;

        public ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            this.rootView = itemView;
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            cbSchedule = itemView.findViewById(R.id.cb_schedule);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            tvOverdue = itemView.findViewById(R.id.tv_overdue);
            llContent = itemView.findViewById(R.id.ll_content);
        }
    }
}
