package com.farashian.pcalendar;


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

    public String toString(String pattern) {
        PersianDateFormat pdf = new PersianDateFormat(pattern);
        return pdf.format(this, pattern);
    }

    @Override
    public String toString() {
        return format("%04d/%02d/%02d", year, month, day);
    }
}
