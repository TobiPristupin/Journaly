package com.example.journaly.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String dayOfMonth(Date date){
        DateFormat formatter = new SimpleDateFormat("dd", Locale.US);
        return formatter.format(date);
    }

    public static String dayOfMonth(long unixTime){
        return dayOfMonth(new Date(unixTime));
    }

    public static String monthAndYear(Date date){
        DateFormat formatter = new SimpleDateFormat("MMM'.' yyyy", Locale.US);
        return formatter.format(date);
    }

    public static String monthAndYear(long unixTime){
        return monthAndYear(new Date(unixTime));
    }
}
