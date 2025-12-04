package com.farashian.test;

import com.farashian.pcalendar.MyPersianCalendar;
import com.farashian.pcalendar.fast.FastPersianCalendar;

import java.util.*;

public class PersianCalendarGregorianTest  extends TestBase {
    
    public static void main(String[] args) {
        System.out.println("=== Testing Persian Calendar Gregorian Methods ===\n");
        
        testMyPersianCalendar();
        repeat("=", 50, true);
        testFastPersianCalendar();
        
        System.out.println("\n=== All tests completed successfully! ===");
    }
    
    
    private static void testMyPersianCalendar() {
        System.out.println("Testing MyPersianCalendar Gregorian Methods");
        repeat("-", 40, false);
        
        // Test 1: Current date
        MyPersianCalendar pc1 = new MyPersianCalendar();
        System.out.println("Test 1 - Current Date:");
        System.out.println("  Persian Date: " + pc1.getLongDate());
        System.out.println("  Gregorian Date: " + pc1.getGrgYear() + "-" + 
                          (pc1.getGrgMonth() + 1) + "-" + pc1.getGrgDay());
        System.out.println("  Gregorian Month Name (EN): " + pc1.getGrgMonthName(Locale.ENGLISH));
        System.out.println("  Gregorian Month Name (FA): " + pc1.getGrgMonthName(new Locale("fa")));
        System.out.println("  Gregorian Week of Year: " + pc1.getGrgWeekOfYear());
        System.out.println("  Gregorian Day of Week: " + pc1.getGrgDayOfWeek());
        System.out.println("  Gregorian Month Length: " + pc1.getGrgMonthLength());
        
        // Test 2: Specific Persian date (Farvardin 1, 1403 = March 21, 2024)
        MyPersianCalendar pc2 = new MyPersianCalendar(1403, 0, 1); // Farvardin 1, 1403
        System.out.println("\nTest 2 - Persian New Year 1403:");
        System.out.println("  Persian: Farvardin 1, 1403");
        System.out.println("  Gregorian: " + pc2.getGrgYear() + "-" + 
                          (pc2.getGrgMonth() + 1) + "-" + pc2.getGrgDay());
        assertEquals(2024, pc2.getGrgYear());
        assertEquals(2, pc2.getGrgMonth()); // March is month 2 (0-based)
        assertEquals(20, pc2.getGrgDay());
        assertEquals("March", pc2.getGrgMonthName(Locale.ENGLISH));
        
        // Test 3: Static month length methods
        System.out.println("\nTest 3 - Static Month Length Calculations:");
        System.out.println("  Feb 2024 (leap): " + MyPersianCalendar.getGrgMonthLength(2024, 1) + " days");
        System.out.println("  Feb 2023: " + MyPersianCalendar.getGrgMonthLength(2023, 1) + " days");
        System.out.println("  Feb 2100 (not leap): " + MyPersianCalendar.getGrgMonthLength(2100, 1) + " days");
        System.out.println("  Feb 2000 (leap): " + MyPersianCalendar.getGrgMonthLength(2000, 1) + " days");
        assertEquals(29, MyPersianCalendar.getGrgMonthLength(2024, 1));
        assertEquals(28, MyPersianCalendar.getGrgMonthLength(2023, 1));
        assertEquals(28, MyPersianCalendar.getGrgMonthLength(2100, 1));
        assertEquals(29, MyPersianCalendar.getGrgMonthLength(2000, 1));
        
        // Test 4: Static month name methods
        System.out.println("\nTest 4 - Static Month Name Methods:");
        System.out.println("  Month 0 (EN): " + MyPersianCalendar.getGrgMonthNameStatic(0, Locale.ENGLISH));
        System.out.println("  Month 0 (FA): " + MyPersianCalendar.getGrgMonthNameStatic(0, new Locale("fa")));
        System.out.println("  Fast Month 0 (EN): " + MyPersianCalendar.getGrgMonthNameFast(0, Locale.ENGLISH));
        System.out.println("  Fast Month 0 (FA): " + MyPersianCalendar.getGrgMonthNameFast(0, new Locale("fa")));
        assertEquals("January", MyPersianCalendar.getGrgMonthNameStatic(0, Locale.ENGLISH));
        
        // Test 5: Set Gregorian date
        System.out.println("\nTest 5 - Set Gregorian Date:");
        MyPersianCalendar pc3 = new MyPersianCalendar();
        pc3.setGrgDate(2024, 5, 15); // June 15, 2024
        System.out.println("  Set to: 2024-06-15");
        System.out.println("  Got: " + pc3.getGrgYear() + "-" + 
                          (pc3.getGrgMonth() + 1) + "-" + pc3.getGrgDay());
        System.out.println("  Persian equivalent: " + pc3.getLongDate());
        assertEquals(2024, pc3.getGrgYear());
        assertEquals(5, pc3.getGrgMonth()); // June is month 5 (0-based)
        assertEquals(15, pc3.getGrgDay());
        
        // Test 6: Gregorian date operations
        System.out.println("\nTest 6 - Gregorian Date Operations:");
        MyPersianCalendar pc4 = new MyPersianCalendar();
        pc4.setGrgDate(2024, 0, 31); // January 31, 2024
        pc4.addGrgDays(1); // Add 1 day = February 1, 2024
        System.out.println("  Jan 31 + 1 day = " + pc4.getGrgYear() + "-" + 
                          (pc4.getGrgMonth() + 1) + "-" + pc4.getGrgDay());
        assertEquals(1, pc4.getGrgDay());
        assertEquals(1, pc4.getGrgMonth()); // February
        
        pc4.addGrgMonths(1); // Add 1 month = March 1, 2024
        System.out.println("  +1 month = " + pc4.getGrgYear() + "-" + 
                          (pc4.getGrgMonth() + 1) + "-" + pc4.getGrgDay());
        assertEquals(2, pc4.getGrgMonth()); // March
        
        // Test 7: Leap year checks
        System.out.println("\nTest 7 - Leap Year Checks:");
        System.out.println("  2024 is leap: " + MyPersianCalendar.isGrgLeapYear(2024));
        System.out.println("  2023 is leap: " + MyPersianCalendar.isGrgLeapYear(2023));
        System.out.println("  2100 is leap: " + MyPersianCalendar.isGrgLeapYear(2100));
        System.out.println("  2000 is leap: " + MyPersianCalendar.isGrgLeapYear(2000));
        assertTrue(MyPersianCalendar.isGrgLeapYear(2024));
        assertFalse(MyPersianCalendar.isGrgLeapYear(2023));
        assertFalse(MyPersianCalendar.isGrgLeapYear(2100));
        assertTrue(MyPersianCalendar.isGrgLeapYear(2000));
        
        // Test 8: Gregorian date formatting
        System.out.println("\nTest 8 - Gregorian Date Formatting:");
        MyPersianCalendar pc5 = new MyPersianCalendar();
        pc5.setGrgDate(2024, 11, 25); // December 25, 2024
        System.out.println("  ISO: " + pc5.getGrgIsoDate());
        System.out.println("  Long (EN): " + pc5.getGrgLongDate(Locale.ENGLISH));
        System.out.println("  Short 0-based: " + pc5.getGrgShortDate("/"));
        System.out.println("  Short 1-based: " + pc5.getGrgShortDate1Based("/"));
        assertEquals("2024-12-25", pc5.getGrgIsoDate());
        assertTrue(pc5.getGrgLongDate(Locale.ENGLISH).contains("December"));
        
        System.out.println("\n✓ All MyPersianCalendar tests passed!");
    }
    
    private static void testFastPersianCalendar() {
        System.out.println("Testing FastPersianCalendar Gregorian Methods");
        repeat("-", 40, false);
        
        // Test 1: Basic Gregorian getters
        FastPersianCalendar fpc1 = new FastPersianCalendar();
        System.out.println("Test 1 - Current Date (Fast):");
        System.out.println("  Gregorian Year: " + fpc1.getGrgYear());
        System.out.println("  Gregorian Month: " + fpc1.getGrgMonth());
        System.out.println("  Gregorian Day: " + fpc1.getGrgDay());
        System.out.println("  Gregorian Month Name: " + fpc1.getGrgMonthName(Locale.ENGLISH));
        
        // Test 2: Specific date conversion
        FastPersianCalendar fpc2 = new FastPersianCalendar(1403, 0, 1); // Farvardin 1, 1403
        System.out.println("\nTest 2 - Persian to Gregorian Conversion:");
        System.out.println("  Persian: 1403/01/01");
        System.out.println("  Gregorian: " + fpc2.getGrgYear() + "/" + 
                          (fpc2.getGrgMonth() + 1) + "/" + fpc2.getGrgDay());
        System.out.println("  Month Name: " + fpc2.getGrgMonthName(Locale.ENGLISH));
        // Test 2: Specific Persian date (Farvardin 1, 1403 = March 21, 2024)

        assertEquals(2024, fpc2.getGrgYear());
        assertEquals(2, fpc2.getGrgMonth()); // March is month 2 (0-based)
        assertEquals(20, fpc2.getGrgDay());
        assertEquals("March", fpc2.getGrgMonthName(Locale.ENGLISH));

        // Test 3: Week calculations
        System.out.println("\nTest 3 - Week Calculations:");
        System.out.println("  Week of Year: " + fpc2.getGrgWeekOfYear());
        System.out.println("  Week of Month: " + fpc2.getGrgWeekOfMonth());
        System.out.println("  Day of Week: " + fpc2.getGrgDayOfWeek());
        System.out.println("  Day of Week Name: " + fpc2.getGrgDayOfWeekName(Locale.ENGLISH));
        
        // Test 4: Month operations
        System.out.println("\nTest 4 - Month Operations:");
        FastPersianCalendar fpc3 = new FastPersianCalendar();
        fpc3.setGrgDate(2024, 1, 28); // February 28, 2024
        System.out.println("  Initial: " + fpc3.getGrgYear() + "-" + 
                          (fpc3.getGrgMonth() + 1) + "-" + fpc3.getGrgDay());
        System.out.println("  Month Length: " + fpc3.getGrgMonthLength() + " days");
        
        fpc3.addGrgDays(1); // Should be February 29, 2024 (leap year)
        System.out.println("  +1 day: " + fpc3.getGrgYear() + "-" + 
                          (fpc3.getGrgMonth() + 1) + "-" + fpc3.getGrgDay());
        assertEquals(29, fpc3.getGrgDay());
        
        fpc3.addGrgDays(1); // Should be March 1, 2024
        System.out.println("  +1 more day: " + fpc3.getGrgYear() + "-" + 
                          (fpc3.getGrgMonth() + 1) + "-" + fpc3.getGrgDay());
        assertEquals(2, fpc3.getGrgMonth()); // March
        assertEquals(1, fpc3.getGrgDay());
        
        // Test 5: Static methods
        System.out.println("\nTest 5 - Static Methods:");
        System.out.println("  Feb 2024 length: " + FastPersianCalendar.getGrgMonthLength(2024, 1));
        System.out.println("  March name (EN): " + FastPersianCalendar.getGrgMonthNameFast(2, Locale.ENGLISH));
        System.out.println("  March name (FA): " + FastPersianCalendar.getGrgMonthNameFast(2, new Locale("fa")));
        
        // Test 6: Gregorian date formatting
        System.out.println("\nTest 6 - Date Formatting:");
        FastPersianCalendar fpc4 = new FastPersianCalendar();
        fpc4.setGrgDate(2024, 5, 21); // June 21, 2024
        System.out.println("  ISO: " + fpc4.getGrgIsoDate());
        System.out.println("  Long: " + fpc4.getGrgLongDate(Locale.ENGLISH));
        System.out.println("  Short: " + fpc4.getGrgShortDate("/"));
        System.out.println("  Month with number: " + fpc4.getGrgMonthNameWithNumber(Locale.ENGLISH));
        
        // Test 7: Comparison and utilities
        System.out.println("\nTest 7 - Date Comparison:");
        FastPersianCalendar today = new FastPersianCalendar();
        FastPersianCalendar tomorrow = today.plusDays(1);
        
        System.out.println("  Today is GrgToday: " + today.isGrgToday());
        System.out.println("  Gregorian weekend: " + today.isGrgWeekend());
        System.out.println("  Gregorian weekday: " + today.isGrgWeekday());
        System.out.println("  Days between: " + today.grgDaysBetween(tomorrow));
        
        // Test 8: Create from Gregorian
        System.out.println("\nTest 8 - Create from Gregorian:");
        FastPersianCalendar fromGrg = FastPersianCalendar.fromGregorian(2024, 3, 20);
        System.out.println("  From Gregorian 2024-04-20:");
        System.out.println("    Persian: " + fromGrg.getLongDate());
        System.out.println("    Gregorian: " + fromGrg.getGrgIsoDate());
        
        System.out.println("\n✓ All FastPersianCalendar tests passed!");
    }
}