package com.farashian.test;

import com.farashian.pcalendar.DateUtils;
import com.farashian.pcalendar.PersianCalendar;
import com.farashian.pcalendar.YMD;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TestDateUtils {
    
    public static void main(String[] args) {
        System.out.println("=== Testing DateUtils ===\n");
        
        // Test 1: Basic Persian date formatting
        System.out.println("🔍 Current Persian Dates:");
        System.out.println("Now (full): " + DateUtils.nowFullDateWithDayFarsi());
        System.out.println("Now (dash): " + DateUtils.nowDashDateFarsi());
        System.out.println("Now (slash): " + DateUtils.nowSlashDateFarsi());
        System.out.println("Day of week: " + DateUtils.nowFarsiDay());
        System.out.println("Time: " + DateUtils.getNowTime());
        System.out.println("Time with seconds: " + DateUtils.getNowTimeWithSeconds());
        System.out.println("DateTime: " + DateUtils.getFarsiDateWithTime());
        System.out.println();
        
        // Test 2: Timestamp formatting
        System.out.println("📅 Timestamp Formats:");
        System.out.println("Timestamp dash: " + DateUtils.nowTimeStampDashFarsi());
        System.out.println("Timestamp underscore: " + DateUtils.nowTimeStampUnderscoreFarsi());
        System.out.println("Custom pattern (yyyy/MM/dd HH:mm): " + DateUtils.nowFarsi("yyyy/MM/dd HH:mm"));
        System.out.println();
        
        // Test 3: Date conversion
        System.out.println("🔄 Date Conversions:");
        Date            currentDate = new Date();
        PersianCalendar persianDate = DateUtils.toPersianDate(currentDate);
        System.out.println("Current Gregorian date: " + currentDate);
        System.out.println("Converted to Persian: " + DateUtils.getFarsiFullDate(currentDate));
        
        // Convert back
        Date convertedBack = DateUtils.toDate(persianDate);
        System.out.println("Converted back to Gregorian: " + convertedBack);
        System.out.println();
        
        // Test 4: Date manipulation
        System.out.println("⚙️ Date Manipulation:");
        long startOfDay = DateUtils.getStartDate(System.currentTimeMillis());
        long endOfDay = DateUtils.getEndDate(System.currentTimeMillis());
        System.out.println("Start of today (UTC): " + new Date(startOfDay));
        System.out.println("End of today (UTC): " + new Date(endOfDay));
        
        // Add one day
        Date tomorrow = DateUtils.addOneDay(currentDate);
        System.out.println("Tomorrow: " + tomorrow);
        System.out.println();
        
        // Test 5: Date before now
        System.out.println("⏮️ Past Dates:");
        String threeDaysAgo = DateUtils.dateBeforeNowFarsi(3);
        System.out.println("3 days ago (Persian): " + threeDaysAgo);
        
        long threeDaysAgoMillis = DateUtils.dateBeforeNow(3);
        System.out.println("3 days ago (millis): " + threeDaysAgoMillis);
        System.out.println();
        
        // Test 6: Islamic calendar conversion
        System.out.println("🌙 Islamic Calendar Conversion:");
        GregorianCalendar gregorian = new GregorianCalendar(2024, Calendar.MARCH, 11);
        YMD               hijriDate = DateUtils.islamicFromGregorian(gregorian);
        System.out.println("Gregorian: " + gregorian.getTime());
        System.out.println("Iranian Hijri: " + hijriDate);
        
        // Check if valid
        if (DateUtils.isValidIranianHijriDate(hijriDate)) {
            System.out.println("Valid Iranian Hijri date");
            int dayOfYear = DateUtils.getDayOfIranianHijriYear(hijriDate);
            System.out.println("Day of year: " + dayOfYear);
        }
        
        // Test official data lookup
        System.out.println("Has official data for 1444 AH: " + DateUtils.hasOfficialData(1444));
        System.out.println("Month length for 1444-1: " + DateUtils.getOfficialMonthLength(1444, 1));
        System.out.println();

        System.out.println("=== Test Complete ===");
    }
}