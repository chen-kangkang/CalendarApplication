package com.example.calendarapplication.model;
// 日程实体类：Schedule.java
public class Schedule {
    private int id;
    private String date;
    private String title;
    private String time;
    private int completed;
    private int quadrant;
    private String remark;
    private int color;
    private int duration;
    private String reminder;
    private String repeatType;
    private int userId;
    private boolean isDateHeader;
    public Schedule(){}

    public boolean isDateHeader() {
        return isDateHeader;
    }

    public void setDateHeader(boolean dateHeader) {
        isDateHeader = dateHeader;
    }

    public Schedule(String date, String title, String time, int completed, int quadrant, String remark, int color, int duration, String reminder, String repeatType, int userId){
        this.date=date;
        this.title=title;
        this.time=time;
        this.completed=completed;
        this.quadrant=quadrant;
        this.remark=remark;
        this.color = color;
        this.duration = duration;
        this.reminder = reminder;
        this.repeatType = repeatType;
        this.userId = userId;
    }
    // Getter和Setter方法
    public int getId(){return id;}

    public void setId(int id) { this.id = id; }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public int getQuadrant() {
        return quadrant;
    }

    public void setQuadrant(int quadrant) {
        this.quadrant = quadrant;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
