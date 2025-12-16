package com.farashian.test;

import com.farashian.pcalendar.PersianCalendar;
import com.farashian.pcalendar.fast.FastPersianCalendar;

import java.util.Calendar;


// Performance test class
public class PersianCalendarBenchmark {
    
    public static void main(String[] args) {
        int iterations = 100000;
        
        // Test original vs fast implementation
        long start1 = System.currentTimeMillis();
        testOriginalCalendar(iterations);
        long duration1 = System.currentTimeMillis() - start1;
        
        long start2 = System.currentTimeMillis();
        testFastCalendar(iterations);
        long duration2 = System.currentTimeMillis() - start2;
        
        System.out.println("Original: " + duration1 + "ms");
        System.out.println("Fast: " + duration2 + "ms");
        System.out.println("Speedup: " + (duration1 / (double)duration2) + "x");
    }
    
    private static void testOriginalCalendar(int iterations) {
        for (int i = 0; i < iterations; i++) {
            PersianCalendar cal = new PersianCalendar(1400 + (i % 3), i % 12, (i % 28) + 1);
            cal.add(Calendar.DAY_OF_MONTH, 1);
            String formatted = cal.getShortDate();
        }
    }
    
    private static void testFastCalendar(int iterations) {
        FastPersianCalendar cal = new FastPersianCalendar();
        for (int i = 0; i < iterations; i++) {
            cal.setPersianDate(1400 + (i % 3), i % 12, (i % 28) + 1);
            cal.addDays(1);
            String formatted = cal.getShortDate();
           /* String formatted = formatDateFast(
                cal.getPersianYear(), cal.getPersianMonth(), cal.getPersianDay(), "/");*/
        }
    }

    private static final ThreadLocal<StringBuilder> stringBuilderCache =
            ThreadLocal.withInitial(() -> new StringBuilder(16));
    public static String formatDateFast(int year, int month, int day, String delimiter) {
        StringBuilder sb = stringBuilderCache.get();
        sb.setLength(0);

        // Fast integer to string conversion
        if (year < 1000) sb.append('0');
        if (year < 100) sb.append('0');
        if (year < 10) sb.append('0');
        sb.append(year);
        sb.append(delimiter);

        int monthNum = month + 1;
        if (monthNum < 10) sb.append('0');
        sb.append(monthNum);
        sb.append(delimiter);

        if (day < 10) sb.append('0');
        sb.append(day);

        return sb.toString();
    }

}