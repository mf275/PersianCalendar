package com.farashian.pcalendar.fast.util;

import static java.lang.String.format;

public class YMD {

    public int year, month, day;

    public YMD(int year, int month, int day) {
        this.year  = year;
        this.month = month;
        this.day   = day;
    }

    public YMD(int[] ymd) {
        this.year  = ymd[0];
        this.month = ymd[1];
        this.day   = ymd[2];
    }

    public int[] toIntArray() {
        int[] ymd = new int[3];
        ymd[0] = this.year;
        ymd[1] = this.month;
        ymd[2] = this.day;

        return ymd;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public String getFormattedDate(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return toString();
        }

        switch (pattern) {
            case "yyyy/MM/dd":
                return String.format("%04d/%02d/%02d",
                                     getYear(), getMonth(), getDay());
            case "yyyy-MM-dd":
                return String.format("%04d-%02d-%02d",
                                     getYear(), getMonth(), getDay());
            case "dd MMMM yyyy":
                return String.format("%02d %s %04d",
                                     getDay(), getMonth(), getYear());
            default:
                return toString();
        }
    }

    @Override
    public String toString() {
        return format("%04d/%02d/%02d", year, month, day);
    }
}
