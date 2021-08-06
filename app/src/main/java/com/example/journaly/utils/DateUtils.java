package com.example.journaly.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateUtils {

    public static String dayOfMonth(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd", Locale.US);
        return formatter.format(date);
    }

    public static String dayOfMonth(long unixTime) {
        return dayOfMonth(new Date(unixTime));
    }

    public static String monthAndYear(Date date) {
        DateFormat formatter = new SimpleDateFormat("MMM'.' yyyy", Locale.US);
        return formatter.format(date);
    }

    public static String monthAndYear(long unixTime) {
        return monthAndYear(new Date(unixTime));
    }

    public static String getShortenedWeekday(String longWeekday){
        return longWeekday.substring(0, 3);
    }

    public static DayOfWeek stringDayOfWeekToEnum(String day){
        List<String> daysOfWeek = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        return DayOfWeek.of(daysOfWeek.indexOf(day) + 1);
    }
}
