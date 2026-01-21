package com.example.calendarapplication.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    
    /**
     * 将日期字符串解析为Date对象
     * @param dateStr 日期字符串，格式为yyyy-MM-dd
     * @return Date对象
     * @throws ParseException 解析异常
     */
    public static Date parseDate(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.parse(dateStr);
    }
    
    /**
     * 将Date对象格式化为字符串
     * @param date Date对象
     * @return 日期字符串，格式为yyyy-MM-dd
     */
    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }
    
    public static Date parseDateTime(String dateStr, String timeStr) {
        try {
            String endTimePart = timeStr.split("-")[1];
            String dateTimeStr = dateStr + " " + endTimePart;
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT);
            return sdf.parse(dateTimeStr);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
