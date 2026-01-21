package com.example.calendarapplication.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.calendarapplication.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MonthAdapter extends BaseAdapter {
    // 上下文
    private Context mContext;
    // 用于生成月份日期
    private Calendar mCalendar;
    // 月视图日期列表
    private List<String> myDayList=new ArrayList<>();
    // 当前选中日期得索引
    private int mCurrentDayIndex;
    // 当前选中的日期字符串（用于同步选中状态）
    private String mSelectedDate;
    // 日期点击回调接口
    private OnDateSelectedListener mListener;

    public interface OnDateSelectedListener{
        void onDateSelected(String date);
    }

    public MonthAdapter(Context context,  Calendar calendar, OnDateSelectedListener listener){
        this.mContext=context;
        this.mCalendar=(Calendar) calendar.clone();// 深拷贝避免原对象被修改
        this.mListener = listener;
        generateDayList(); // 生成当前月份的日期列表
        setCurrentDayIndex(); // 定位当前日期的索引
    }

    /**
     * 生成当前月份的日期列表（包含上月/下月的占位空字符串）
     */
    private void generateDayList() {
        // 清除原来的数据
        myDayList.clear();
        // 创建一个独立副本
        Calendar tempCal=(Calendar) mCalendar.clone();
        // 把这个副本日期设置为当前月份的1号
        tempCal.set(Calendar.DAY_OF_MONTH,1);
        // 计算1号对应的是星期几
        int firstDayOfWeek=tempCal.get(Calendar.DAY_OF_WEEK);
        // 计算当月1号之前需要的空白占位数量（适配周一是一周第一天）
        // 若1号是周日（firstDayOfWeek=1）：则需要6个空白（周一到周六）
        // 其他情况：1号是周一（firstDayOfWeek=2）→ 2-2=0个空白；1号是周二（firstDayOfWeek=3）→ 3-2=1个空白（周一）
        int preBlankCount=(firstDayOfWeek==Calendar.SUNDAY)?6:(firstDayOfWeek-2);
        // 添加空白占位符
        for(int i=0;i<preBlankCount;i++){
            myDayList.add("");
        }
        // 获取当月的总天数
        int maxDay=tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for(int i=1;i<=maxDay;i++){
            myDayList.add(String.valueOf(i));
        }
        // 添加下月占位符(一个月最多的跨度是6周)
        int totalSize=42;
        while(myDayList.size() < totalSize){
            myDayList.add("");
        }
    }

    /**
     * 定位当前日期在列表中的索引
     * @param selectedDate 可选的选中日期字符串，用于同步选中状态
     */
    private void setCurrentDayIndex(String selectedDate){
        Calendar tempCal = (Calendar) mCalendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        
        // 如果传入了选中日期字符串，则根据它计算索引
        if (selectedDate != null && !selectedDate.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Calendar selectedCal = Calendar.getInstance();
                selectedCal.setTime(sdf.parse(selectedDate));

                // 验证选中的日期是否在当前月份中
                int selectedMonth = selectedCal.get(Calendar.MONTH);
                int currentMonth = mCalendar.get(Calendar.MONTH);

                if (selectedMonth == currentMonth && selectedCal.get(Calendar.YEAR) == mCalendar.get(Calendar.YEAR)) {
                    // 选中的日期在当前月份中，计算其索引
                    int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
                    int preBlankCount;
                    if (firstDayOfWeek == Calendar.MONDAY) {
                        preBlankCount = 0;
                    } else if (firstDayOfWeek == Calendar.SUNDAY) {
                        preBlankCount = 6;
                    } else {
                        preBlankCount = firstDayOfWeek - 2;
                    }
                    mCurrentDayIndex = preBlankCount + selectedCal.get(Calendar.DAY_OF_MONTH) - 1;
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 如果没有选中日期或选中日期不在当前月份，则使用当前日期
        int currentDay=mCalendar.get(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
        int preBlankCount;
        if (firstDayOfWeek == Calendar.MONDAY) {
            preBlankCount = 0;
        } else if (firstDayOfWeek == Calendar.SUNDAY) {
            preBlankCount = 6;
        } else {
            preBlankCount = firstDayOfWeek - 2;
        }
        mCurrentDayIndex = preBlankCount + currentDay - 1;
    }

    /**
     * 定位当前日期在列表中的索引（无参数版本，使用当前日期）
     */
    private void setCurrentDayIndex(){
        setCurrentDayIndex(null);
    }

    public void updateMonth(Calendar newCalendar) {
        this.mCalendar = (Calendar) newCalendar.clone();
        generateDayList();
        setCurrentDayIndex();
        notifyDataSetChanged();
    }

    /**
     * 更新月份并同步选中日期
     * @param newCalendar 新的月份日历
     * @param selectedDate 选中的日期字符串（yyyy-MM-dd格式）
     */
    public void updateMonth(Calendar newCalendar, String selectedDate) {
        this.mCalendar = (Calendar) newCalendar.clone();
        this.mSelectedDate = selectedDate;
        generateDayList();
        setCurrentDayIndex(selectedDate);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return myDayList.size();
    }

    @Override
    public Object getItem(int position) {
        return myDayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // ViewHolder内部类
    static class ViewHolder{
        TextView tvDay;// 日期数字文本框
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 复用ViewHolder优化性能
        ViewHolder holder;
        if(convertView==null){
            convertView=View.inflate(mContext, R.layout.item_month_day, null);
            holder=new ViewHolder();
            holder.tvDay=convertView.findViewById(R.id.tv_month_day);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder) convertView.getTag();
        }

        // 获取当前位置的日期
        String day=myDayList.get(position);
        holder.tvDay.setText(day);

        // 处理空日期（上月或下月的日期不显示）
        if(day.isEmpty()){
            holder.tvDay.setVisibility(View.INVISIBLE);
            return convertView;
        }
        holder.tvDay.setVisibility(View.VISIBLE);

        // 控制日期样式：选中态，未选中态
        if(position==mCurrentDayIndex){
            holder.tvDay.setBackgroundResource(R.drawable.shape_circle_orange);
            holder.tvDay.setTextColor(mContext.getResources().getColor(android.R.color.white));
        }else{
            holder.tvDay.setBackgroundResource(R.drawable.shape_circle_white);
            holder.tvDay.setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));
        }

        // 日期点击事件
        convertView.setOnClickListener(v->{
            if(mListener!=null){
                // 创建日期格式化对象，指定格式为“年-月-日”，并使用系统默认地区
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                // 克隆当前日历对象（mCalendar），避免修改原对象的日期状态
                Calendar tempCal = (Calendar) mCalendar.clone();
                // 将克隆后的日历对象的“日”设置为当前点击的日期数字（day是点击的日期字符串，如"15"）
                tempCal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
                // 将日历对象转换为指定格式的完整日期字符串（如2025-01-15）
                String fullDate = sdf.format(tempCal.getTime());
                // 通过回调接口将选中的完整日期传递给上层组件（如Activity）
                mListener.onDateSelected(fullDate);
            }
        });
        return convertView;
    }

    // 更新当前选中的日期索引（用于切换选中状态）
    public void setCurrentDayIndex(int currentDayIndex){
        this.mCurrentDayIndex=currentDayIndex;
        notifyDataSetChanged();
    }

}
