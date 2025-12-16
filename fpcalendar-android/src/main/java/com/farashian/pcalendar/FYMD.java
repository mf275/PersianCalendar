package com.farashian.pcalendar;

import org.jetbrains.annotations.NotNull;

import static java.lang.String.format;

public class FYMD {

    public int year, month, day;
    public FYMD(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public FYMD(int[] ymd) {
        this.year = ymd[0];
        this.month= ymd[1];
        this.day= ymd[2];
    }

    @NotNull
    @Override
    public String toString() {
        return format("%04d/%02d/%02d", year, month, day);
    }
}
