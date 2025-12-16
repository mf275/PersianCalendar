package com.farashian.test; // Adjust package as necessary


import com.farashian.pcalendar.PersianCalendar;

import java.text.ParseException;
import java.util.Calendar;

public class PersianCalendarTest extends TestBase {

    public static void main(String[] args) throws ParseException {
        System.out.println("=== Running PersianCalendar Tests ===\n");

        testLeapYearAndInitialization();
        testDateArithmetic();
        testDateFormatting();
        testDateComparison();
        testDateParsing();
        testHelperMethods();
        testEdgeCases();

        System.out.println("\n=== Test Summary ===");
        System.out.println("Total tests: " + testCount);
        System.out.println("Passed: " + passedCount);
        System.out.println("Failed: " + failedCount);

        if (failedCount == 0) {
            System.out.println("\n✅ All tests passed!");
        } else {
            System.out.println("\n❌ Some tests failed!");
        }
    }

    public static void testLeapYearAndInitialization() {
        System.out.println("Test 1: Initialization and Leap Year");

        int[] leapYears = {
                1201, 1205, 1210, 1214, 1218, 1222, 1226, 1230, 1234, 1238, 1243,
                1247, 1251, 1255, 1259, 1263, 1267, 1271, 1276, 1280, 1284, 1288,
                1292, 1296, 1300, 1304, 1309, 1313, 1317, 1321, 1325, 1329, 1333,
                1337, 1342, 1346, 1350, 1354, 1358, 1362, 1366, 1370, 1375, 1379,
                1383, 1387, 1391, 1395, 1399, 1403, 1408, 1412, 1416, 1420, 1424,
                1428, 1432, 1436, 1441, 1445, 1449, 1453, 1457, 1461, 1465, 1469,
                1474, 1478, 1482, 1486, 1490, 1494, 1498, 1502, 1507, 1511, 1515,
                1519, 1523, 1527, 1531, 1535, 1540, 1544, 1548, 1552, 1556, 1560,
                1564, 1568, 1573, 1577, 1581, 1585, 1589, 1593, 1597};
        int[] nonLeapYears = {1374, 1376, 1380, 1384, 1388, 1392, 1396, 1400, 1404, 1407, 1411};

        for (int year : leapYears) {
            PersianCalendar date = new PersianCalendar(year, 11, 30); // Esfand 30
            assertTrue(date.isLeapYear(), year + " should be a leap year");
            assertEquals(30, date.getDaysInMonth(), "Esfand in " + year + " should have 30 days");
        }

        for (int year : nonLeapYears) {
            PersianCalendar date = new PersianCalendar(year, 11, 29); // Esfand 29
            assertFalse(date.isLeapYear(), year + " should NOT be a leap year");
            assertEquals(29, date.getDaysInMonth(), "Esfand in " + year + " should have 29 days");
        }

        // Test month day counts
        PersianCalendar date = new PersianCalendar(1402, 0, 1); // Farvardin
        for (int month = 0; month < 6; month++) {
            date.set(PersianCalendar.MONTH, month);
            assertEquals(31, date.getDaysInMonth(), "Month " + month + " should have 31 days");
        }

        for (int month = 6; month < 11; month++) {
            date.set(PersianCalendar.MONTH, month);
            assertEquals(30, date.getDaysInMonth(), "Month " + month + " should have 30 days");
        }

        System.out.println();
    }


    // --- Test 2: Date Arithmetic (Add/Subtract) ---
    public static void testDateArithmetic() {
        System.out.println("Test 2: Date Arithmetic");

        // Test 1: Simple day addition within month
        PersianCalendar date = new PersianCalendar(1402, 0, 15); // Farvardin 15
        date.add(PersianCalendar.DAY_OF_MONTH, 10);
        assertEquals(25, date.getDayOfMonth(), "Should be Farvardin 25");
        assertEquals(0, date.getMonth(), "Should still be Farvardin");

        // Test 2: Day addition crossing month boundary
        date.setPersianDate(1402, 0, 28); // Farvardin 28
        date.add(PersianCalendar.DAY_OF_MONTH, 5);
        assertEquals(2, date.getDayOfMonth(), "Should be Ordibehesht 2");
        assertEquals(1, date.getMonth(), "Should be Ordibehesht (1)");

        // Test 3: Month addition
        date.setPersianDate(1402, 10, 15); // Bahman 15
        date.add(PersianCalendar.MONTH, 1);
        assertEquals(11, date.getMonth(), "Should be Esfand (11)");
        assertEquals(1402, date.getYear());

        // Test 4: Month addition crossing year boundary
        date.setPersianDate(1402, 11, 15); // Esfand 15
        date.add(PersianCalendar.MONTH, 1);
        assertEquals(0, date.getMonth(), "Should be Farvardin (0)");
        assertEquals(1403, date.getYear());

        // Test 5: Year addition
        date.setPersianDate(1402, 5, 15); // Shahrivar 15
        date.add(PersianCalendar.YEAR, 1);
        assertEquals(1403, date.getYear());
        assertEquals(5, date.getMonth(), "Should still be Shahrivar");

        // Test 6: Negative addition (subtraction)
        date.setPersianDate(1402, 0, 1); // Farvardin 1
        date.add(PersianCalendar.DAY_OF_MONTH, -1);
        assertEquals(1401, date.getYear(), "Should roll back to 1401");
        assertEquals(11, date.getMonth(), "Should be Esfand (11)");
        int expectedDay = new PersianCalendar(1401, 11, 1).isLeapYear() ? 30 : 29;
        assertEquals(expectedDay, date.getDayOfMonth(), "Should be last day of Esfand 1401");

        System.out.println();
    }

    // --- Test 3: Date Formatting ---
    public static void testDateFormatting() {
        System.out.println("Test 3: Date Formatting");

        PersianCalendar date = new PersianCalendar(1402, 8, 10, 14, 5, 9); // Azar 10, 14:05:09

        // Test built-in formatting methods
        System.out.println("Short date: " + date.getShortDate());
        System.out.println("Short date 2: " + date.getShortDate2());
        System.out.println("Long date: " + date.getLongDate());
        System.out.println("Long date time: " + date.getLongDateTime());
        System.out.println("Month name: " + date.getMonthName());
        System.out.println("Weekday name: " + date.getWeekdayName());

        // Verify format consistency
        String shortDate = date.getShortDate();
        assertTrue(shortDate.contains("1402"), "Should contain year 1402");
        assertTrue(shortDate.contains("/"), "Should contain separator");

        // Test with different delimiter
        String customDate = date.getShortDate("-");
        assertTrue(customDate.contains("1402"), "Should contain year 1402");
        assertTrue(customDate.contains("-"), "Should contain custom delimiter");

        System.out.println();
    }

    // --- Test 4: Date Parsing ---
    public static void testDateParsing() {
        System.out.println("Test 4: Date Parsing");

        // Test parse method
        PersianCalendar date = new PersianCalendar();

        // Test valid date parsing
        date.parse("1402/07/20");
        assertEquals(1402, date.getYear(), "Year should be 1402");
        assertEquals(6, date.getMonth(), "Month should be 6 (Mehr)");
        assertEquals(20, date.getDayOfMonth(), "Day should be 20");

        // Test with different delimiter
        date.parse("1402-07-20");
        assertEquals(1402, date.getYear(), "Year should be 1402 with dash delimiter");
        assertEquals(6, date.getMonth(), "Month should be 6 with dash delimiter");
        assertEquals(20, date.getDayOfMonth(), "Day should be 20 with dash delimiter");

        // Test parseOrNullToCompat static method
        PersianCalendar parsed1 = PersianCalendar.parseOrNullToCompat("1402/07/20");
        assertNotNull(parsed1, "parseOrNullToCompat should return non-null for valid date");
        if (parsed1 != null) {
            assertEquals(1402, parsed1.getYear(), "Static parse year should be 1402");
            assertEquals(6, parsed1.getMonth(), "Static parse month should be 6");
            assertEquals(20, parsed1.getDayOfMonth(), "Static parse day should be 20");
        }

        // Test invalid dates
        PersianCalendar parsed2 = PersianCalendar.parseOrNullToCompat("invalid");
        assertNull(parsed2, "parseOrNullToCompat should return null for invalid date");

        PersianCalendar parsed3 = PersianCalendar.parseOrNullToCompat("1402/13/01");
        assertNull(parsed3, "parseOrNullToCompat should return null for invalid month");

        PersianCalendar parsed4 = PersianCalendar.parseOrNullToCompat("1402/01/32");
        assertNull(parsed4, "parseOrNullToCompat should return null for invalid day");

        // Test that parse handles time preservation
        date.setPersianDate(1402, 0, 1);
        date.set(PersianCalendar.HOUR_OF_DAY, 14);
        date.set(PersianCalendar.MINUTE, 30);
        date.parse("1403/06/15");
        // Date should change, but time might be preserved depending on implementation
        assertEquals(1403, date.getYear());
        assertEquals(5, date.getMonth()); // Shahrivar

        System.out.println();
    }

    // --- Test 5: Date Comparison ---
    public static void testDateComparison() {
        System.out.println("Test 5: Date Comparison");

        PersianCalendar date1 = new PersianCalendar(1402, 5, 10); // Shahrivar 10
        PersianCalendar date2 = new PersianCalendar(1402, 5, 10); // Shahrivar 10
        PersianCalendar date3 = new PersianCalendar(1402, 5, 11); // Shahrivar 11
        PersianCalendar date4 = new PersianCalendar(1403, 0, 1);  // Farvardin 1, 1403

        // Test equality using isEqual (helper method we added)
        assertTrue(date1.isEqual(date2), "Dates with same values should be equal");
        assertFalse(date1.isEqual(date3), "Different dates should not be equal");

        // Test isBefore/isAfter
        assertTrue(date1.isBefore(date3), "Date 1 should be before Date 3");
        assertTrue(date3.isAfter(date1), "Date 3 should be after Date 1");
        assertFalse(date1.isAfter(date3), "Date 1 should not be after Date 3");

        // Test compare across years
        assertTrue(date3.isBefore(date4), "Date 3 should be before Date 4");
        assertTrue(date4.isAfter(date1), "Date 4 should be after Date 1");

        // Test daysBetween
        PersianCalendar dateA    = new PersianCalendar(1402, 0, 1);
        PersianCalendar dateB    = new PersianCalendar(1402, 0, 10);
        long            daysDiff = dateA.daysBetween(dateB);
        assertEquals(-9, daysDiff, "Should be 9 days difference");

        // Test with same date
        long sameDayDiff = date1.daysBetween(date2);
        assertEquals(0, sameDayDiff, "Same date should have 0 days difference");

        System.out.println();
    }

    // --- Test 6: Helper Methods ---
    public static void testHelperMethods() {
        System.out.println("Test 6: Helper Methods");

        PersianCalendar date = new PersianCalendar(1402, 8, 15, 10, 30, 45); // Azar 15

        // Test plus/minus methods
        PersianCalendar nextWeek = date.plusDays(7);
        assertEquals(22, nextWeek.getDayOfMonth(), "Should be Azar 22");
        assertEquals(8, nextWeek.getMonth(), "Should still be Azar");

        PersianCalendar lastWeek = date.minusDays(7);
        assertEquals(8, lastWeek.getDayOfMonth(), "Should be Azar 8");

        PersianCalendar nextMonth = date.plusMonths(1);
        assertEquals(9, nextMonth.getMonth(), "Should be Dey (9)");
        assertEquals(1402, nextMonth.getYear());

        PersianCalendar lastYear = date.minusYears(1);
        assertEquals(1401, lastYear.getYear());
        assertEquals(8, lastYear.getMonth(), "Should still be Azar");

        // Test withFirstDayOfMonth / withLastDayOfMonth
        PersianCalendar firstDay = date.withFirstDayOfMonth();
        assertEquals(1, firstDay.getDayOfMonth(), "Should be 1st day of month");
        assertEquals(8, firstDay.getMonth(), "Should still be Azar");

        PersianCalendar lastDay = date.withLastDayOfMonth();
        assertEquals(30, lastDay.getDayOfMonth(), "Azar should have 30 days");

        // Test atStartOfDay / atEndOfDay
        PersianCalendar startOfDay = date.atStartOfDay();
        assertEquals(0, startOfDay.get(Calendar.HOUR_OF_DAY), "Should be 00:00");
        assertEquals(0, startOfDay.get(Calendar.MINUTE), "Should be 00:00");
        assertEquals(0, startOfDay.get(Calendar.SECOND), "Should be 00:00");

        PersianCalendar endOfDay = date.atEndOfDay();
        assertEquals(23, endOfDay.get(Calendar.HOUR_OF_DAY), "Should be 23:59");
        assertEquals(59, endOfDay.get(Calendar.MINUTE), "Should be 23:59");
        assertEquals(59, endOfDay.get(Calendar.SECOND), "Should be 23:59");

        // Test isBetween
        PersianCalendar start = new PersianCalendar(1402, 0, 1);
        PersianCalendar end   = new PersianCalendar(1402, 11, 29);
        assertTrue(date.isBetween(start, end), "Azar 15 should be between Farvardin 1 and Esfand 29");

        // Test isOutside range
        PersianCalendar futureDate = new PersianCalendar(1403, 0, 1);
        assertFalse(futureDate.isBetween(start, end), "1403 date should not be in 1402 range");

        // Test isHoliday / isWeekend (Friday)
        // Create a known Friday (you might need to adjust based on actual calendar)
        PersianCalendar testFriday = new PersianCalendar();
        // Find a Friday by checking
        for (int i = 0; i < 7; i++) {
            testFriday.addDays(1);
            if (testFriday.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                assertTrue(testFriday.isHoliday(), "Friday should be holiday");
                assertTrue(testFriday.isWeekend(), "Friday should be weekend");
                break;
            }
        }

        // Test getAge
        PersianCalendar birthDate = new PersianCalendar(1370, 0, 1);
        PersianCalendar now       = new PersianCalendar(1402, 0, 1);
        int             age       = birthDate.getAge(now);
        assertEquals(32, age, "Age from 1370 to 1402 should be 32");

        // Test with birthday not happened yet this year
        birthDate.setPersianDate(1370, 6, 15); // Mehr 15
        now.setPersianDate(1402, 5, 1); // Shahrivar 1 (before birthday)
        age = birthDate.getAge(now);
        assertEquals(31, age, "Age should be 31 if birthday hasn't occurred yet");

        System.out.println();
    }

    // --- Test 7: Edge Cases ---
    public static void testEdgeCases() {
        System.out.println("Test 7: Edge Cases");

        // Test minimum year (1)
        PersianCalendar minDate = new PersianCalendar(1, 0, 1);
        assertEquals(1, minDate.getYear(), "Minimum year should be 1");

        // Test adding to minimum year
        minDate.add(PersianCalendar.YEAR, -1);
        assertEquals(1, minDate.getYear(), "Year should not go below 1");

        // Test maximum day in month adjustment
        PersianCalendar date = new PersianCalendar(1402, 0, 31); // Farvardin 31
        date.set(PersianCalendar.MONTH, 1); // Ordibehesht has 31 days too, so should be OK
        assertEquals(31, date.getDayOfMonth(), "Day should remain 31");

        date.setPersianDate(1402, 0, 31);
        date.set(PersianCalendar.MONTH, 6); // Mehr has 30 days, should adjust to 30
        assertEquals(30, date.getDayOfMonth(), "Day should adjust to 30 for 30-day month");

        // Test leap year adjustment
        date.setPersianDate(1403, 10, 30); // Bahman 30 in leap year 1403
        date.set(PersianCalendar.MONTH, 11); // Esfand in leap year has 30 days
        assertEquals(30, date.getDayOfMonth(), "Day should remain 30 in leap year Esfand");

        date.setPersianDate(1402, 10, 30); // Bahman 30 in non-leap year 1402
        date.set(PersianCalendar.MONTH, 11); // Esfand in non-leap year has 29 days
        assertEquals(29, date.getDayOfMonth(), "Day should adjust to 29 in non-leap year Esfand");

        // Test clone method
        PersianCalendar original = new PersianCalendar(1402, 5, 15, 10, 30, 45);
        PersianCalendar clone    = original.clone();
        assertTrue(original.isEqual(clone), "Clone should be equal to original");
        assertEquals(original.getTimeInMillis(), clone.getTimeInMillis(), "Clone should have same time");

        // Test roll method
        date.setPersianDate(1402, 11, 15); // Esfand 15
        date.roll(PersianCalendar.MONTH, true); // Roll forward
        assertEquals(0, date.getMonth(), "Should roll to Farvardin");
        assertEquals(1403, date.getYear(), "Should increment year when rolling from Esfand");

        date.setPersianDate(1402, 0, 15); // Farvardin 15
        date.roll(PersianCalendar.MONTH, false); // Roll backward
        assertEquals(11, date.getMonth(), "Should roll to Esfand");
        assertEquals(1401, date.getYear(), "Should decrement year when rolling from Farvardin");

        System.out.println();
    }

}