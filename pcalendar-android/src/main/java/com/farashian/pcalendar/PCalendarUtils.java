package com.farashian.pcalendar;


import java.util.Locale;
import java.util.TimeZone;

import static com.farashian.pcalendar.PCConstants.GREGORIAN_MONTH_NAMES_ENG;
import static com.farashian.pcalendar.PCConstants.HIJRI_MONTH_NAMES;

/**
 * Utility class with static methods for Persian calendar operations.
 */
public final class PCalendarUtils {

    private PCalendarUtils() {
    }

    /**
     * Validates a Persian date
     * @param year Persian year
     * @param month Persian month (1-12)
     * @param day day of month
     * @throws IllegalArgumentException if date is invalid
     */
    public static void validatePersianDate(int year, int month, int day) {
        if (year < 1) {
            throw new IllegalArgumentException("Year must be positive, got: " + year);
        }

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        int maxDays = getDaysInMonth(year, month);
        if (day < 1 || day > maxDays) {
            throw new IllegalArgumentException("Day must be between 1 and " + maxDays +
                                               " for year " + year + " month " + month + ", got: " + day);
        }
    }

    /**
     * Validates a Gregorian date
     * @param year Gregorian year
     * @param month Gregorian month (1-12)
     * @param day day of month
     * @throws IllegalArgumentException if date is invalid
     */
    public static void validateGregorianDate(int year, int month, int day) {
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException("Year must be between 1 and 9999, got: " + year);
        }

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        int maxDays = getGrgMonthLength(year, month);
        if (day < 1 || day > maxDays) {
            throw new IllegalArgumentException("Day must be between 1 and " + maxDays +
                                               " for month " + month + "/" + year + ", got: " + day);
        }
    }

    /**
     * Static method to check if a Persian year is a leap year
     * @param year Persian year
     * @return true if leap year
     */
    public static boolean isLeapYear(int year) {
        int remainder = year % 33;
        return remainder == 1 || remainder == 5 || remainder == 9 ||
               remainder == 13 || remainder == 17 || remainder == 22 ||
               remainder == 26 || remainder == 30;
    }

    /**
     * Static method to get number of days in a Persian month
     * @param year Persian year
     * @param month Persian month (1-12)
     * @return days in month
     */
    public static int getDaysInMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        // Convert to 0-based for calculation
        int month0 = month - 1;

        if (month0 < 6) {
            return 31;
        } else if (month0 < 11) {
            return 30;
        } else {
            return isLeapYear(year) ? 30 : 29;
        }
    }

    /**
     * Static method to get Gregorian month length
     * @param year Gregorian year
     * @param month Gregorian month (1-12)
     * @return days in month
     */
    public static int getGrgMonthLength(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        // Convert to 0-based for calculation
        int month0 = month - 1;

        switch (month0) {
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
     * @param month Persian month (1-12)
     * @param day Persian day
     * @return array with [Gregorian year, Gregorian month (1-12), Gregorian day]
     */
    public static int[] persianToGregorian(int year, int month, int day) {
        validatePersianDate(year, month, day);

        int[] out = new int[3];
        // Convert 1-based month to 1-based for algorithm
        jalaliToGregorianFast(year, month, day, out);
        // Return 1-based month
        return out;
    }

    /**
     * Converts Gregorian date to Persian date
     * @param year Gregorian year
     * @param month Gregorian month (1-12)
     * @param day Gregorian day
     * @return array with [Persian year, Persian month (1-12), Persian day]
     */
    public static int[] gregorianToPersian(int year, int month, int day) {
        validateGregorianDate(year, month, day);

        int[] out = new int[3];
        // Convert 1-based month to 1-based for algorithm
        gregorianToJalaliFast(year, month, day, out);
        // Return 1-based month
        return out;
    }

    /**
     * Static method to format a Persian date
     * @param year Persian year
     * @param month Persian month (1-12)
     * @param day Persian day
     * @param delimiter delimiter to use
     * @return formatted date string
     */
    public static String formatShortDate(int year, int month, int day, String delimiter) {
        return String.format(Locale.US, "%04d%s%02d%s%02d",
                             year, delimiter, month, delimiter, day);
    }

    /**
     * Formats a number to two digits
     * @param num the number to format
     * @return two-digit string
     */
    public static String formatToTwoDigits(int num) {
        return String.format(Locale.US, "%02d", num);
    }

    /**
     * Convert Gregorian to Jalali date (algorithm expects 1-based months)
     */
    private static void gregorianToJalaliFast(int gy, int gm, int gd, int[] out) {
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

    /**
     * Convert Jalali to Gregorian date (algorithm expects 1-based months)
     */
    private static void jalaliToGregorianFast(int jy, int jm, int jd, int[] out) {
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

    /**
     * Get Persian month name
     * @param month Persian month (1-12)
     * @param locale locale for month name
     * @return month name
     */
    public static String getPersianMonthName(int month, Locale locale) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        if (locale != null) {
            if ("fa".equals(locale.getLanguage())) {
                if ("IR".equals(locale.getCountry())) {
                    return PCConstants.PERSIAN_MONTH_NAMES[month - 1];
                } else if ("AF".equals(locale.getCountry())) {
                    return PCConstants.AFGHAN_MONTH_NAMES[month-1];
                }
            } else if ("ps".equals(locale.getLanguage())) {
                if ("AF".equals(locale.getCountry())) {
                    return PCConstants.PASHTO_AFGHAN_MONTH_NAMES[month-1];
                }
            }
        }

        return PCConstants.PERSIAN_MONTH_NAMES_IN_ENGLISH[month-1];
    }

    /**
     * Get Gregorian month name
     * @param month Gregorian month (1-12)
     * @return month name in English
     */
    public static String getGregorianMonthName(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }
        return GREGORIAN_MONTH_NAMES_ENG[month-1];
    }

    /**
     * Get Hijri month name
     * @param month Hijri month (1-12)
     * @return month name
     */
    public static String getHijriMonthName(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }
        return HIJRI_MONTH_NAMES[month-1];
    }

    /**
     * Helper method for backward compatibility
     * @deprecated Use getDaysInMonth() instead
     */
    @Deprecated
    public static int getDaysInMonthStatic(int year, int month) {
        return getDaysInMonth(year, month);
    }

    /**
     * Helper method for backward compatibility
     * @deprecated Use isLeapYear() instead
     */
    @Deprecated
    public static boolean isLeapYearStatic(int year) {
        return isLeapYear(year);
    }

    public static Locale getLocaleFromTimezone() {
        String tzId = TimeZone.getDefault().getID();

        // Simplified heuristic mapping (incomplete — extend as needed)
        switch (tzId) {
            case "America/New_York":
            case "America/Chicago":
            case "America/Denver":
            case "America/Los_Angeles":
                return Locale.US; // en-US
            case "Europe/London":
                return Locale.UK; // en-GB
            case "Europe/Paris":
            case "Europe/Berlin":
            case "Europe/Rome":
                return Locale.FRANCE; // or Locale.GERMANY — arbitrary!
            case "Asia/Tokyo":
                return Locale.JAPAN; // ja-JP
            case "Asia/Kabul":
                return Locale.forLanguageTag("fa-AF");
            case "Asia/Tehran":
                return Locale.forLanguageTag("fa-IR");
            case "Asia/Shanghai":
            case "Asia/Hong_Kong":
                return Locale.CHINA; // zh-CN (note: HK/TW differ!)
            case "Asia/Seoul":
                return Locale.KOREA; // ko-KR
            case "Australia/Sydney":
                return Locale.forLanguageTag("en-AU");
            default:
                return Locale.getDefault(); // fallback
        }
    }

}