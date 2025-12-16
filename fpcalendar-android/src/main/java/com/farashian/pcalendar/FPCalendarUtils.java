package com.farashian.pcalendar;

import java.util.Locale;

/**
 * Utility class with static methods for Persian calendar operations.
 */
public final class FPCalendarUtils {
    
    private FPCalendarUtils() {
    }
    
    /**
     * Validates a Persian date
     * @param year Persian year
     * @param month Persian month (0-11)
     * @param day day of month
     * @throws IllegalArgumentException if date is invalid
     */
    public static void validatePersianDate(int year, int month, int day) {
        if (year < 1) {
            throw new IllegalArgumentException("Year must be positive, got: " + year);
        }
        
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }
        
        int maxDays = getDaysInMonthStatic(year, month);
        if (day < 1 || day > maxDays) {
            throw new IllegalArgumentException("Day must be between 1 and " + maxDays +
                                               " for year " + year + " month " + month + ", got: " + day);
        }
    }


    /**
     * Validates a Gregorian date
     * @param year Gregorian year
     * @param month Gregorian month (0-11)
     * @param day day of month
     * @throws IllegalArgumentException if date is invalid
     */
    public static void validateGregorianDate(int year, int month, int day) {
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException("Year must be between 1 and 9999, got: " + year);
        }
        
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }
        
        int maxDays = getGrgMonthLength(year, month);
        if (day < 1 || day > maxDays) {
            throw new IllegalArgumentException("Day must be between 1 and " + maxDays +
                                               " for month " + month  + "/" + year + ", got: " + day);
        }
    }
    
    
    /**
     * Static method to check if a Persian year is a leap year
     * @param year Persian year
     * @return true if leap year
     */
    public static boolean isLeapYearStatic(int year) {
        int remainder = year % 33;
        return remainder == 1 || remainder == 5 || remainder == 9 ||
               remainder == 13 || remainder == 17 || remainder == 22 ||
               remainder == 26 || remainder == 30;
    }
    
    /**
     * Static method to get number of days in a Persian month
     * @param year Persian year
     * @param month Persian month (0-11)
     * @return days in month
     */
    public static int getDaysInMonthStatic(int year, int month) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }
        
        if (month < 6) {
            return 31;
        } else if (month < 11) {
            return 30;
        } else {
            return isLeapYearStatic(year) ? 30 : 29;
        }
    }
    
    /**
     * Static method to get Gregorian month length
     * @param year Gregorian year
     * @param month Gregorian month (0-11)
     * @return days in month
     */
    public static int getGrgMonthLength(int year, int month) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }
        
        switch (month) {
            case 0:  // January
            case 2:  // March
            case 4:  // May
            case 6:  // July
            case 7:  // August
            case 9:  // October
            case 11: // December
                return 31;
            case 3:  // April
            case 5:  // June
            case 8:  // September
            case 10: // November
                return 30;
            case 1:  // February
                return isGrgLeapYear(year) ? 29 : 28;
            default:
                return 31;
        }
    }
    
    /**
     * Static method to check if Gregorian year is leap year
     * @param year Gregorian year
     * @return true if leap year
     */
    public static boolean isGrgLeapYear(int year) {
        return (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
    }
    
    
    /**
     * Converts Persian date to Gregorian date
     * @param year Persian year
     * @param month Persian month (0-11)
     * @param day Persian day
     * @return array with [Gregorian year, Gregorian month (0-11), Gregorian day]
     */
    public static int[] persianToGregorian(int year, int month, int day) {
        validatePersianDate(year, month, day);
        
        int[] out = new int[3];
        // Convert 0-based month to 1-based for algorithm
        jalaliToGregorianFast(year, month + 1, day, out);
        // Convert 1-based month back to 0-based
        out[1] = out[1] - 1;
        return out;
    }
    
    /**
     * Converts Gregorian date to Persian date
     * @param year Gregorian year
     * @param month Gregorian month (0-11)
     * @param day Gregorian day
     * @return array with [Persian year, Persian month (0-11), Persian day]
     */
    public static int[] gregorianToPersian(int year, int month, int day) {
        validateGregorianDate(year, month, day);
        
        int[] out = new int[3];
        // Convert 0-based month to 1-based for algorithm
        gregorianToJalaliFast(year, month + 1, day, out);
        // Convert 1-based month back to 0-based
        out[1] = out[1] - 1;
        return out;
    }
    
    
    /**
     * Static method to format a Persian date
     * @param year Persian year
     * @param month Persian month (0-11)
     * @param day Persian day
     * @param delimiter delimiter to use
     * @return formatted date string
     */
    public static String formatShortDate(int year, int month, int day, String delimiter) {
        return String.format(Locale.US, "%04d%s%02d%s%02d", 
                             year, delimiter, month + 1, delimiter, day);
    }
    
    /**
     * Formats a number to two digits
     * @param num the number to format
     * @return two-digit string
     */
    public static String formatToTwoDigits(int num) {
        return String.format(Locale.US, "%02d", num);
    }
    

    
    private static void gregorianToJalaliFast(int gy, int gm, int gd, int[] out) {
        // Implementation from FastPersianCalendar
        int jy = (gm > 2) ? (gy + 1) : gy;

        final int[] g_d_m = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
        int dayOfYear = 355666 + (365 * gy) + ((jy + 3) / 4) - ((jy + 99) / 100)
                        + ((jy + 399) / 400) + gd + g_d_m[gm - 1];

        jy = -1595 + (33 * (dayOfYear / 12053));
        dayOfYear %= 12053;
        jy += 4 * (dayOfYear / 1461);
        dayOfYear %= 1461;

        if (dayOfYear > 365) {
            jy += (dayOfYear - 1) / 365;
            dayOfYear = (dayOfYear - 1) % 365;
        }

        int jm, jd;
        if (dayOfYear < 186) {
            jm = 1 + dayOfYear / 31;
            jd = 1 + (dayOfYear % 31);
        } else {
            jm = 7 + (dayOfYear - 186) / 30;
            jd = 1 + ((dayOfYear - 186) % 30);
        }

        out[0] = jy;
        out[1] = jm;
        out[2] = jd;
    }
    
    private static void jalaliToGregorianFast(int jy, int jm, int jd, int[] out) {
        // Implementation from FastPersianCalendar
        jy += 1595;
        int dayOfYear = -355668 + (365 * jy) + ((jy / 33) * 8) +
                        (((jy % 33) + 3) / 4) + jd + ((jm < 7) ? (jm - 1) * 31 : ((jm - 7) * 30) + 186);

        int gy = 400 * (dayOfYear / 146097);
        dayOfYear %= 146097;

        if (dayOfYear > 36524) {
            gy += 100 * ((--dayOfYear) / 36524);
            dayOfYear %= 36524;
            if (dayOfYear >= 365) dayOfYear++;
        }

        gy += 4 * (dayOfYear / 1461);
        dayOfYear %= 1461;

        if (dayOfYear > 365) {
            gy += (dayOfYear - 1) / 365;
            dayOfYear = (dayOfYear - 1) % 365;
        }

        final int[] monthDays = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if ((gy % 4 == 0) && ((gy % 100 != 0) || (gy % 400 == 0))) {
            monthDays[2] = 29;
        }

        int gm = 1;
        for (dayOfYear++; gm < 13 && dayOfYear > monthDays[gm]; gm++) {
            dayOfYear -= monthDays[gm];
        }

        out[0] = gy;
        out[1] = gm;
        out[2] = dayOfYear;
    }
}