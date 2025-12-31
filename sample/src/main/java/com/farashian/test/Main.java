package com.farashian.test;

import com.farashian.pcalendar.PersianCalendar;
import com.farashian.pcalendar.PersianDateFormat;

import java.text.ParseException;

//Autor Morteza
public class Main {
    public static void main(String[] args) {
        System.out.println("=== PersianCalendar & PersianDateFormat Test ===\n");

        //Test 1: Basic Calendar Creation
        System.out.println("1. BASIC CALENDAR CREATION");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        PersianCalendar now = new PersianCalendar();
        System.out.println("Current Persian Date: " + now.getLongDate());
        System.out.println("Current DateTime: " + now.getLongDateTime());
        System.out.println("Short Date: " + now.getShortDate());
        System.out.println("Year: " + now.getYear() + ", Month: " + now.getMonth() +
                           ", Day: " + now.getDayOfMonth());
        System.out.println("Is Leap Year: " + now.isLeapYear());
        System.out.println();

        //Test 2: Specific Date Creation
        System.out.println("2. SPECIFIC DATE CREATION");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        PersianCalendar specificDate = new PersianCalendar(1402, 9, 15); //15 Dey 1402
        System.out.println("Specific Date: " + specificDate.getLongDate());
        System.out.println("Day of Week: " + specificDate.getWeekdayName());
        System.out.println("Month Name: " + specificDate.getMonthName());
        System.out.println("Days in Month: " + specificDate.getDaysInMonth());
        System.out.println();

        //Test 3: Date Formatting Tests
        System.out.println("3. DATE FORMATTING TESTS");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        PersianDateFormat formatter = new PersianDateFormat();

        //Test different patterns
        String[] testPatterns = {
                "yyyy/MM/dd",
                "yyyy-MM-dd",
                "dddd, d MMMM yyyy",
                "ddd, MMM d, yyyy",
                "HH:mm:ss",
                "hh:mm a",
                "yyyy/MM/dd HH:mm:ss",
                "'Today is' dddd",
                "'Date:' yyyy/MM/dd 'Time:' HH:mm",
                "Weekday: ddd, Month: MMMM, Year: yyyy"
        };

        for (String pattern : testPatterns) {
            formatter.setPattern(pattern);
            System.out.println(pattern + " → " + formatter.format(now));
        }
        System.out.println();

        //Test 4: Farsi Number Formatting
        System.out.println("4. FARSI NUMBER FORMATTING");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        formatter.setNumberCharacter(PersianDateFormat.PersianDateNumberCharacter.FARSI);
        formatter.setPattern("yyyy/MM/dd HH:mm:ss");
        System.out.println("With Farsi numbers: " + formatter.format(now));

        formatter.setPattern("dddd, d MMMM yyyy");
        System.out.println("Full date Farsi: " + formatter.format(now));

        //Switch back to English
        formatter.setNumberCharacter(PersianDateFormat.PersianDateNumberCharacter.ENGLISH);
        System.out.println();

        //Test 5: Static Format Methods
        System.out.println("5. STATIC FORMAT METHODS");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        String staticFormatted = PersianDateFormat.format(
                now,
                "yyyy-MM-dd HH:mm",
                PersianDateFormat.PersianDateNumberCharacter.ENGLISH
        );
        System.out.println("Static format: " + staticFormatted);

        String staticFarsi = PersianDateFormat.format(
                now,
                "dddd, d MMMM yyyy",
                PersianDateFormat.PersianDateNumberCharacter.FARSI
        );
        System.out.println("Static Farsi: " + staticFarsi);
        System.out.println();

        //Test 6: Date Parsing
        System.out.println("6. DATE PARSING");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        try {
            PersianCalendar parsedDate = formatter.parse("1402/10/15", "yyyy/MM/dd");
            System.out.println("Parsed date: " + parsedDate.getLongDate());

            PersianCalendar parsedWithTime = formatter.parse("1402/10/15 14:30", "yyyy/MM/dd HH:mm");
            System.out.println("Parsed with time: " + parsedWithTime.getLongDateTime());

        } catch (ParseException e) {
            System.out.println("Parse error: " + e.getMessage());
        }
        System.out.println();

        //Test 7: Gregorian Parsing
        System.out.println("7. GREGORIAN PARSING");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        try {
            PersianCalendar fromGregorian = formatter.parseGrg("2024-01-05", "yyyy-MM-dd");
            System.out.println("From Gregorian 2024-01-05: " + fromGregorian.getLongDate());
        } catch (ParseException e) {
            System.out.println("Gregorian parse error: " + e.getMessage());
        }
        System.out.println();

        //Test 8: Date Arithmetic
        System.out.println("8. DATE ARITHMETIC");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        PersianCalendar testDate = new PersianCalendar(1402, 0, 1); //1 Farvardin 1402
        System.out.println("Original: " + testDate.getLongDate());

        //Add 10 days
        testDate.add(PersianCalendar.DAY_OF_MONTH, 10);
        System.out.println("After adding 10 days: " + testDate.getLongDate());

        //Add 2 months
        testDate.add(PersianCalendar.MONTH, 2);
        System.out.println("After adding 2 months: " + testDate.getLongDate());

        //Add 1 year
        testDate.add(PersianCalendar.YEAR, 1);
        System.out.println("After adding 1 year: " + testDate.getLongDate());
        System.out.println();

        //Test 9: Date Comparison
        System.out.println("9. DATE COMPARISON");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        PersianCalendar date1 = new PersianCalendar(1402, 0, 1);
        PersianCalendar date2 = new PersianCalendar(1402, 0, 15);

        System.out.println("Date 1: " + date1.getLongDate());
        System.out.println("Date 2: " + date2.getLongDate());
        System.out.println("Date 1 equals Date 2: " + date1.equals(date2));
        System.out.println("Date 1 before Date 2: " + date1.before(date2));
        System.out.println("Date 1 after Date 2: " + date1.after(date2));
        System.out.println();

        //Test 10: Edge Cases
        System.out.println("10. EDGE CASES");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        //Leap year test
        PersianCalendar leapYear = new PersianCalendar(1403, 11, 30); //Esfand in leap year
        System.out.println("Leap year Esfand 30: " + leapYear.getLongDate());
        System.out.println("Is 1403 leap year: " + leapYear.isLeapYear());

        //Month boundaries
        PersianCalendar firstOfMonth = new PersianCalendar(1402, 5, 31); //Last day of Shahrivar
        System.out.println("Last day of Shahrivar: " + firstOfMonth.getLongDate());
        firstOfMonth.add(PersianCalendar.DAY_OF_MONTH, 1);
        System.out.println("Next day: " + firstOfMonth.getLongDate());

        System.out.println("\n=== All Tests Completed Successfully ===");
    }
}