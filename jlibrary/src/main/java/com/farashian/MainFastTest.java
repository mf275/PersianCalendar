package com.farashian;

import com.farashian.pcalendar.MyPersianCalendar;
import com.farashian.pcalendar.MyPersianDateFormat;
import com.farashian.pcalendar.fast.FastPersianCalendar;
import com.farashian.pcalendar.fast.FastPersianDateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//Autor Morteza
public class MainFastTest {
    public static void main(String[] args) {
        System.out.println("=== FastPersianCalendar & FastPersianDateFormat Test ===\n");
        convertGregorianToPersian("1979/08/18");
        // Test 1: Basic Calendar Creation
        System.out.println("1. BASIC CALENDAR CREATION");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        FastPersianCalendar now = new FastPersianCalendar();
        System.out.println("Current Persian Date: " + now.getLongDate());
        System.out.println("Current DateTime: " + now.getLongDateTime());
        System.out.println("Short Date: " + now.getShortDate());
        System.out.println("Year: " + now.getYear() + ", Month: " + now.getMonth() +
                           ", Day: " + now.getDayOfMonth());
        System.out.println("Is Leap Year: " + now.isLeapYear());
        System.out.println();

        // Test 2: Specific Date Creation
        System.out.println("2. SPECIFIC DATE CREATION");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        FastPersianCalendar specificDate = new FastPersianCalendar(1402, 9, 15); // 15 Dey 1402
        System.out.println("Specific Date: " + specificDate.getLongDate());
        System.out.println("Day of Week: " + specificDate.getWeekdayName());
        System.out.println("Month Name: " + specificDate.getMonthName());
        System.out.println("Days in Month: " + specificDate.getDaysInMonth());
        System.out.println();

        // Test 3: Date Formatting Tests
        System.out.println("3. DATE FORMATTING TESTS");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        FastPersianDateFormat formatter = new FastPersianDateFormat();

        // Test different patterns
        String[] testPatterns = {
                "yyyy/MM/dd",
                "yyyy-MM-dd",
                "DDDD, d MMMM yyyy",
                "ddd, MMM d, yyyy",
                "HH:mm:ss",
                "hh:mm a",
                "yyyy/MM/dd HH:mm:ss",
                "'Today is' DDDD",
                "'Date:' yyyy/MM/dd 'Time:' HH:mm",
                "Weekday: ddd, Month: MMMM, Year: yyyy"
        };

        for (String pattern : testPatterns) {
            formatter.setPattern(pattern);
            System.out.println(pattern + " → " + formatter.format(now));
        }
        System.out.println();

        // Test 4: Farsi Number Formatting
        System.out.println("4. FARSI NUMBER FORMATTING");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        formatter.setNumberCharacter(FastPersianDateFormat.PersianDateNumberCharacter.FARSI);
        formatter.setPattern("yyyy/MM/dd HH:mm:ss");
        System.out.println("With Farsi numbers: " + formatter.format(now));

        formatter.setPattern("DDDD, d MMMM yyyy");
        System.out.println("Full date Farsi: " + formatter.format(now));

        // Switch back to English
        formatter.setNumberCharacter(FastPersianDateFormat.PersianDateNumberCharacter.ENGLISH);
        System.out.println();

        // Test 5: Static Format Methods
        System.out.println("5. STATIC FORMAT METHODS");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        String staticFormatted = FastPersianDateFormat.format(
                now,
                "yyyy-MM-dd HH:mm",
                FastPersianDateFormat.PersianDateNumberCharacter.ENGLISH
        );
        System.out.println("Static format: " + staticFormatted);

        String staticFarsi = FastPersianDateFormat.format(
                now,
                "DDDD, d MMMM yyyy",
                FastPersianDateFormat.PersianDateNumberCharacter.FARSI
        );
        System.out.println("Static Farsi: " + staticFarsi);
        System.out.println();

        // Test 6: Date Parsing
        System.out.println("6. DATE PARSING");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        try {
            FastPersianCalendar parsedDate = formatter.parse("1402/10/15", "yyyy/MM/dd");
            System.out.println("Parsed date: " + parsedDate.getLongDate());

            FastPersianCalendar parsedWithTime = formatter.parse("1402/10/15 14:30", "yyyy/MM/dd HH:mm");
            System.out.println("Parsed with time: " + parsedWithTime.getLongDateTime());

        } catch (ParseException e) {
            System.out.println("Parse error: " + e.getMessage());
        }
        System.out.println();

        // Test 7: Gregorian Parsing
        System.out.println("7. GREGORIAN PARSING");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        try {
            FastPersianCalendar fromGregorian = formatter.parseGrg("2024-01-05", "yyyy-MM-dd");
            System.out.println("From Gregorian 2024-01-05: " + fromGregorian.getLongDate());
        } catch (ParseException e) {
            System.out.println("Gregorian parse error: " + e.getMessage());
        }
        System.out.println();

        // Test 8: Date Arithmetic
        System.out.println("8. DATE ARITHMETIC");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        FastPersianCalendar testDate = new FastPersianCalendar(1402, 0, 1); // 1 Farvardin 1402
        System.out.println("Original: " + testDate.getLongDate());

        // Add 10 days
        testDate.add(FastPersianCalendar.DAY_OF_MONTH, 10);
        System.out.println("After adding 10 days: " + testDate.getLongDate());

        // Add 2 months
        testDate.add(FastPersianCalendar.MONTH, 2);
        System.out.println("After adding 2 months: " + testDate.getLongDate());

        // Add 1 year
        testDate.add(FastPersianCalendar.YEAR, 1);
        System.out.println("After adding 1 year: " + testDate.getLongDate());
        System.out.println();

        // Test 9: Date Comparison
        System.out.println("9. DATE COMPARISON");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        FastPersianCalendar date1 = new FastPersianCalendar(1402, 0, 1);
        FastPersianCalendar date2 = new FastPersianCalendar(1402, 0, 15);

        System.out.println("Date 1: " + date1.getLongDate());
        System.out.println("Date 2: " + date2.getLongDate());
        System.out.println("Date 1 equals Date 2: " + date1.equals(date2));
        System.out.println("Date 1 before Date 2: " + date1.before(date2));
        System.out.println("Date 1 after Date 2: " + date1.after(date2));
        System.out.println();

        // Test 10: Edge Cases
        System.out.println("10. EDGE CASES");
        //System.out.println("=".repeat(40));
        System.out.println("========================================");


        // Leap year test
        FastPersianCalendar leapYear = new FastPersianCalendar(1403, 11, 30); // Esfand in leap year
        System.out.println("Leap year Esfand 30: " + leapYear.getLongDate());
        System.out.println("Is 1403 leap year: " + leapYear.isLeapYear());

        // Month boundaries
        FastPersianCalendar firstOfMonth = new FastPersianCalendar(1402, 5, 31); // Last day of Shahrivar
        System.out.println("Last day of Shahrivar: " + firstOfMonth.getLongDate());
        firstOfMonth.add(FastPersianCalendar.DAY_OF_MONTH, 1);
        System.out.println("Next day: " + firstOfMonth.getLongDate());

        System.out.println("\n=== All Tests Completed Successfully ===");

        testBugFix();
    }
    private static void testBugFix() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            Date date = sdf.parse("1979/08/18");

            StringBuilder result = new StringBuilder();
            result.append("Testing 1979/08/18 conversion:\n\n");
            FastPersianCalendar gg = new FastPersianCalendar();
            // Test 1: Direct algorithm test
            int[] directResult = new int[3];
            gg.gregorianToJalaliFast(1979, 8, 18, directResult);
            result.append("Direct Algorithm: ")
                    .append(directResult[0]).append("/")
                    .append(directResult[1]).append("/")
                    .append(directResult[2]).append("\n");

            // Test 2: Using FastPersianCalendar instance
            FastPersianCalendar fastCal = new FastPersianCalendar();

            // CRITICAL: Set the time correctly
            fastCal.setTimeInMillis(date.getTime());

            // Force computation
             //fastCal.complete();

            result.append("Calendar Instance: ")
                    .append(fastCal.getYear()).append("/")
                    .append(fastCal.getMonth() + 1).append("/")
                    .append(fastCal.getDayOfMonth()).append("\n");

            // Test 3: Debug the internal state
            result.append("\nDebug Info:\n");
            result.append("Calendar Time in millis: ").append(fastCal.getTimeInMillis()).append("\n");
            result.append("Gregorian from calendar: ")
                    .append(fastCal.gCal.get(Calendar.YEAR)).append("/")
                    .append(fastCal.gCal.get(Calendar.MONTH) + 1).append("/")
                    .append(fastCal.gCal.get(Calendar.DAY_OF_MONTH)).append("\n");

            result.append("\nExpected: 1358/05/27\n");

            boolean directCorrect = directResult[0] == 1358 && directResult[1] == 5 && directResult[2] == 27;
            boolean calendarCorrect = fastCal.getYear() == 1358 &&
                                      (fastCal.getMonth() + 1) == 5 &&
                                      fastCal.getDayOfMonth() == 27;

            result.append("\nDirect Algorithm: ").append(directCorrect ? "✓ CORRECT" : "✗ WRONG").append("\n");
            result.append("Calendar Instance: ").append(calendarCorrect ? "✓ CORRECT" : "✗ WRONG");

            System.out.println(result.toString());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void convertGregorianToPersian(String gregorianDate) {
        
        if (gregorianDate.isEmpty()) {
            System.out.println("Please enter a Gregorian date");
            return;
        }

        try {
            SimpleDateFormat sdf  = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            Date             date = sdf.parse(gregorianDate);

            if (date == null) {
                System.out.println("Invalid Gregorian date format");
                return;
            }



            String result;
             {
                // FastPersianCalendar
                FastPersianCalendar fastCalendar = new FastPersianCalendar();
                fastCalendar.setTime(date);
                FastPersianDateFormat.PersianDateNumberCharacter fastNumberFormat =
                                FastPersianDateFormat.PersianDateNumberCharacter.FARSI;
                result = FastPersianDateFormat.format(fastCalendar, "yyyy/MM/dd", fastNumberFormat);
            }

            System.out.println("Persian Date: " + result);
        } catch (ParseException e) {
            System.out.println("Error parsing Gregorian date: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}