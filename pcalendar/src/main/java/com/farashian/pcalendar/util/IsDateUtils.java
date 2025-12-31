package com.farashian.pcalendar.util;

public class IsDateUtils {
    /**
     * Static method: Check if Hijri year is leap year
     * @param year Hijri year
     * @return true if leap year
     */
    public static boolean isHijriLeapYear(int year) {
        //Hijri leap years occur in 2, 5, 7, 10, 13, 16, 18, 21, 24, 26, 29 year cycles
        return ((year * 11) + 14) % 30 < 11;
    }

    /**
     * Get days in Hijri month
     * @param year Hijri year
     * @param month Hijri month (1-12)
     * @return number of days in month (29 or 30)
     */
    public static int getHijriMonthLength(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Hijri month must be between 1 and 12, got: " + month);
        }

        //Odd months have 30 days, even months have 29 days in normal years
        //Except Dhu al-Hijjah (month 12) which has 30 days in leap years
        if (month == 12) {
            return isHijriLeapYear(year) ? 30 : 29;
        }
        return (month % 2 == 1) ? 30 : 29;
    }

    /**
     * Convert Gregorian date to Hijri date using Umm al-Qura algorithm
     * @param gy Gregorian year
     * @param gm Gregorian month (0-based: 0=January)
     * @param gd Gregorian day
     * @return int array [islamicYear, islamicMonth, islamicDay]
     */
    public static int[] convertGregorianToHijri(int gy, int gm, int gd) {
        //Convert to 1-based month for calculations
        gm = gm + 1;

        //Algorithm: Umm al-Qura calendar (Saudi Arabia official calendar)
        //Simplified version that works for modern dates (after 1937 CE)

        //First convert to Julian Day
        int jd = gregorianToJulianDay(gy, gm, gd);

        //Hijri epoch (July 16, 622 CE = Julian Day 1948439.5)
        int islamicEpoch = 1948439;

        //Calculate days since Hijri epoch
        int daysSinceEpoch = jd - islamicEpoch;

        //Approximate Hijri year
        int year = (int) Math.floor((daysSinceEpoch - 0.5) / 354.366);

        //Refine year calculation
        int testYear = year;
        while (true) {
            //Calculate days for this year
            int daysInYear = isHijriLeapYear(testYear) ? 355 : 354;
            int daysAtStart = (int) Math.floor(testYear * 354.366 + 0.5);

            if (daysSinceEpoch < daysAtStart + daysInYear) {
                year = testYear;
                break;
            }
            testYear++;
        }

        //Calculate days at start of year
        int daysAtStartOfYear = (int) Math.floor(year * 354.366 + 0.5);
        int dayOfYear = daysSinceEpoch - daysAtStartOfYear + 1;

        //Determine month
        int month = 1;
        int daysInMonth;
        for (; month <= 12; month++) {
            daysInMonth = getHijriMonthLength(year, month);
            if (dayOfYear <= daysInMonth) {
                break;
            }
            dayOfYear -= daysInMonth;
        }

        //Ensure valid day
        if (dayOfYear < 1) dayOfYear = 1;
        if (dayOfYear > getHijriMonthLength(year, month)) {
            dayOfYear = getHijriMonthLength(year, month);
        }

        //Hijri year starts from year 1
        return new int[]{year + 1, month, dayOfYear};
    }

    /**
     * Convert Hijri date to Gregorian date
     * @param iy Hijri year
     * @param im Hijri month (1-12)
     * @param id Hijri day
     * @return int array [gregorianYear, gregorianMonth(0-based), gregorianDay]
     */
    public static int[] convertHijriToGregorian(int iy, int im, int id) {
        //Convert to 0-based year (epoch year 0)
        int year = iy - 1;

        //Calculate days since Hijri epoch
        int totalDays = 0;
        for (int y = 0; y < year; y++) {
            totalDays += isHijriLeapYear(y) ? 355 : 354;
        }

        //Add days for completed months in current year
        for (int m = 1; m < im; m++) {
            totalDays += getHijriMonthLength(iy, m);
        }

        //Add days in current month
        totalDays += id - 1;

        //Hijri epoch (July 16, 622 CE = Julian Day 1948439.5)
        int islamicEpoch = 1948439;
        int jd = islamicEpoch + totalDays;

        //Convert Julian Day to Gregorian
        return julianDayToGregorian(jd);
    }

    /**
     * Convert Gregorian date to Julian Day
     */
    private static int gregorianToJulianDay(int year, int month, int day) {
        int a = (14 - month) / 12;
        int y = year + 4800 - a;
        int m = month + 12 * a - 3;

        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045;
    }

    /**
     * Convert Julian Day to Gregorian date
     */
    private static int[] julianDayToGregorian(int jd) {
        int a = jd + 32044;
        int b = (4 * a + 3) / 146097;
        int c = a - (146097 * b) / 4;
        int d = (4 * c + 3) / 1461;
        int e = c - (1461 * d) / 4;
        int m = (5 * e + 2) / 153;

        int day = e - (153 * m + 2) / 5 + 1;
        int month = m + 3 - 12 * (m / 10);
        int year = 100 * b + d - 4800 + (m / 10);

        return new int[]{year, month - 1, day}; //Return 0-based month
    }
}
