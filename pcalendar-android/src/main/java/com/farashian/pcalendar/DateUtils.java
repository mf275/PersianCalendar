package com.farashian.pcalendar;


import android.icu.util.IslamicCalendar;
import android.os.Build;
import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class DateUtils {

    public static int      THIS_YEAR;
    static        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    static PersianDateFormat dayOfWeek           = new PersianDateFormat("dddd"); // Day of week
    static PersianDateFormat fullDateWithDay     = new PersianDateFormat("dddd dd MMMM yyyy");
    static PersianDateFormat fullDate            = new PersianDateFormat("dd MMMM yyyy");
    static PersianDateFormat dashDate1           = new PersianDateFormat("dd-MMM-yyyy");
    static PersianDateFormat dateTime            = new PersianDateFormat("dd MMMM yyyy HH:mm");
    static PersianDateFormat timestampDash       = new PersianDateFormat("yyyy-MM-dd-HH:mm");
    static PersianDateFormat timestampUnderscore = new PersianDateFormat("yyyy-MM-dd_HH-mm");
    static PersianDateFormat slashDate           = new PersianDateFormat("yyyy/MM/dd");
    static PersianDateFormat dashDate            = new PersianDateFormat("yyyy-MM-dd");
    static PersianDateFormat time                = new PersianDateFormat("HH:mm");
    static PersianDateFormat timeWithSeconds     = new PersianDateFormat("HH:mm:ss");

    public static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

    static {
        THIS_YEAR = new PersianCalendar().getYear();
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public static Date from(LocalDate localDate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }

        return null;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public static Date from(LocalDateTime localDateTime) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }

        return null;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public static LocalDate fromDate(Date date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        }

        return null;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public static LocalDateTime asLocalDateTime(Date date) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        return null;
    }

    public static String getTimeStamp() {
        return new SimpleDateFormat(TIMESTAMP_FORMAT, Locale.US).format(new Date());
    }

    public static long calculateInitialDelay(int targetHour, int targetMinute) {
        Calendar now    = Calendar.getInstance();
        Calendar target = Calendar.getInstance();

        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, targetMinute);
        target.set(Calendar.SECOND, 0);

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1);
        }

        return target.getTimeInMillis() - now.getTimeInMillis();
    }

    public static boolean isOverdue(Date date) {
        if (date == null) {
            return false;
        }
        return date.before(new Date());
    }

    public static String nowFarsiDashDate() {
        PersianCalendar pdate = new PersianCalendar();
        return dashDate.format(pdate);
    }

    public static String getFarsidate(String pattern) {
        PersianCalendar   pdate = new PersianCalendar();
        PersianDateFormat pdf   = new PersianDateFormat(pattern);
        return pdf.format(pdate);
    }


    public static Date toDate(PersianCalendar date) {
        return date.getTime();
    }

    public static PersianCalendar toPersianDate(Date date) {
        if (date == null) return null;
        return new PersianCalendar(date.getTime());
    }

    public static PersianCalendar toPersianDate(String date) {
        if (date == null) return null;

        try {
            PersianCalendar pc = new PersianCalendar();
            // Parse date in format "yyyy/MM/dd"
            String[] parts = date.split("/");
            if (parts.length == 3) {
                pc.setPersianDate(
                        Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2])
                );
                return pc;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static String toSlashDate(PersianCalendar date) {
        if (date == null) return null;
        return slashDate.format(date);
    }

    public static String nowFullDateFarsi() {
        PersianCalendar pdate = new PersianCalendar();
        return fullDate.format(pdate);
    }

    public static String nowFullDateWithDayFarsi() {
        PersianCalendar pdate = new PersianCalendar();
        return fullDateWithDay.format(pdate);
    }

    public static String nowFarsiDay() {
        PersianCalendar pdate = new PersianCalendar();
        return dayOfWeek.format(pdate);
    }

    public static String nowDashDateFarsi() {
        PersianCalendar pdate = new PersianCalendar();
        return dashDate.format(pdate);
    }

    public static String getFarsiFullDate(long timeStamp) {
        PersianCalendar pdate = new PersianCalendar(timeStamp);
        return fullDate.format(pdate);
    }

    public static String getFarsiFullDateWithDay(long timeStamp) {
        PersianCalendar pdate = new PersianCalendar(timeStamp);
        return fullDateWithDay.format(pdate);
    }

    public static String getFarsiFullDate(Date date) {
        PersianCalendar pdate = new PersianCalendar(date.getTime());
        return fullDate.format(pdate);
    }

    public static String getFarsiFullDateWithDay(Date date) {
        PersianCalendar pdate = new PersianCalendar(date.getTime());
        return fullDateWithDay.format(pdate);
    }

    public static String getFarsiDateWithTime(Date date) {
        PersianCalendar pdate = new PersianCalendar(date.getTime());
        return dateTime.format(pdate);
    }

    public static String getFarsiDateWithTime(long dateInMilis) {
        PersianCalendar pdate = new PersianCalendar(dateInMilis);
        return dateTime.format(pdate);
    }

    public static String getFarsiDateWithTime() {
        PersianCalendar pdate = new PersianCalendar();
        return dateTime.format(pdate);
    }

    public static String nowSlashDateFarsi() {
        PersianCalendar pdate = new PersianCalendar();
        return slashDate.format(pdate);
    }

    public static String getSlashDateFarsi(long dateInMilis) {
        PersianCalendar pdate = new PersianCalendar(dateInMilis);
        return slashDate.format(pdate);
    }

    public static String dateBeforeNowFarsi(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);
        PersianCalendar pdate = new PersianCalendar(cal.getTimeInMillis());
        return slashDate.format(pdate);
    }

    public static String[] nowOnlyTime() {
        PersianCalendar pdate = new PersianCalendar();
        return time.format(pdate).split(":");
    }

    public static String nowTimeStampDashFarsi() {
        PersianCalendar pdate = new PersianCalendar();
        return timestampDash.format(pdate);
    }

    public static String nowTimeStampUnderscoreFarsi() {
        PersianCalendar pdate = new PersianCalendar();
        return timestampUnderscore.format(pdate);
    }

    public static String getTime(Date date) {
        if (date == null) return "";
        PersianCalendar pdate = new PersianCalendar(date.getTime());
        return time.format(pdate);
    }

    public static String getNowTime() {
        PersianCalendar pdate = new PersianCalendar();
        return time.format(pdate);
    }

    public static String getNowTimeWithSeconds() {
        PersianCalendar pdate = new PersianCalendar();
        return timeWithSeconds.format(pdate);
    }

    public static String nowFarsi(String pattern) {
        PersianCalendar   pdate = new PersianCalendar();
        PersianDateFormat pdf   = new PersianDateFormat(pattern);
        return pdf.format(pdate);
    }

    public static long dateBeforeNow(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);
        return cal.getTimeInMillis();
    }

    public static long getStartDate(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getStartDate(int persianYear, int persianMonth, int persianDay) {
        PersianCalendar PersianCalendar = new PersianCalendar();
        PersianCalendar.setPersianDate(persianYear, persianMonth, persianDay);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(PersianCalendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long getEndDate(int persianYear, int persianMonth, int persianDay) {
        PersianCalendar PersianCalendar = new PersianCalendar();
        PersianCalendar.setPersianDate(persianYear, persianMonth, persianDay);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(PersianCalendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTimeInMillis();
    }

    public static long getEndDate(long dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public static long convertUTCToLocal(long utcDate) {
        if (utcDate <= 0) return 0;
        int offset = TimeZone.getDefault().getOffset(utcDate);
        return utcDate + offset;
    }

    public static long convertToUTC(long localTimeMillis) {
        if (localTimeMillis == -1) return -1;
        int localTimeZoneOffset = TimeZone.getDefault().getOffset(localTimeMillis);
        return localTimeMillis - localTimeZoneOffset;
    }

    public static Date addOneDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public static Date addOneDayAndSetTimeToMidnight(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 2);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static String getNowInUtc() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    public static Date reduceOneMinuteUtc(Date currentDateUTC) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(currentDateUTC);
        calendar.add(Calendar.MINUTE, -1);
        return calendar.getTime();
    }

    public static Date reduceOneMinute(Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, -1);
        return calendar.getTime();
    }

    public static YMD islamicFromGregorian2(GregorianCalendar gc) {
        // Tabular conversion
        IslamicCalendar islamicCalendar = new IslamicCalendar(new Locale("fa"));
        islamicCalendar.setTime(gc.getTime());

        int year  = islamicCalendar.get(Calendar.YEAR);
        int month = islamicCalendar.get(Calendar.MONTH);
        int day   = islamicCalendar.get(Calendar.DAY_OF_MONTH);

        // Simple correction: Iran often lags tabular by 1 day
        day -= 1;
        if (day == 0) {
            month -= 1;
            if (month == 0) {
                month = 12;
                year -= 1;
            }
            day = 30; // fallback, you’d need month length table
        }

        return new YMD(year, month, day);
    }

    // If you don't want ICU4J dependency, you can use this approximate method
    public static YMD gregorianToIranianHijri(GregorianCalendar gc) {
        // Known reference: 1 Muharram 1340 AH ≈ July 16, 1921 CE
        GregorianCalendar epoch = new GregorianCalendar(1921, 6, 16); // Month is 0-indexed

        // Calculate days difference
        long diffMillis = gc.getTimeInMillis() - epoch.getTimeInMillis();
        int  daysDiff   = (int) (diffMillis / (1000 * 60 * 60 * 24));

        // Now use the month length table to convert daysDiff to YMD
        return null;// convertDaysToIranianHijri(daysDiff);
    }

    public static GregorianCalendar islamicToGregorian(IslamicCalendar islamicCalendar) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(islamicCalendar.getTime());
        return gc;
    }

    public int getIslamicMonthLength(int year, int month) {
        IslamicCalendar islamicCalendar = new IslamicCalendar(Locale.US);
        islamicCalendar.set(Calendar.YEAR, year);
        islamicCalendar.set(Calendar.MONTH, month);
        // Set to the first day of the month to obtain the proper maximum.
        islamicCalendar.set(Calendar.DAY_OF_MONTH, 1);
        return islamicCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private static final HashMap<Integer, int[]> HIJRI_MONTH_DATA = getIranianHijriMonthData();

    /**
     * Check if a given Hijri year/month falls within the official data range
     * @param year - Hijri year
     * @param month - Hijri month (1-12)
     * @return True if official data exists for this date
     */
    public static boolean hasOfficialData(int year, int month) {
        // Check if year exists in data
        if (!HIJRI_MONTH_DATA.containsKey(year)) {
            return false;
        }

        // Check if month is valid (1-12)
        if (month < 0 || month > 11) {
            return false;
        }

        // Check if the specific month exists in the data array
        int[] monthLengths = HIJRI_MONTH_DATA.get(year);
        if (month > monthLengths.length) {
            return false;
        }

        return true;
    }

    // Overloaded method for checking just the year
    public static boolean hasOfficialData(int year) {
        return hasOfficialData(year, 1);
    }

    /**
     * Get the number of days in a specific Hijri month from official data
     * @param year - Hijri year
     * @param month - Hijri month (1-12)
     * @return Number of days (29 or 30), or -1 if no official data
     */
    public static int getOfficialMonthLength(int year, int month) {
        if (!hasOfficialData(year, month)) {
            return -1; // Using -1 instead of null for primitive return type
        }

        // Month is 1-indexed, array is 0-indexed
        return HIJRI_MONTH_DATA.get(year)[month];
    }

    /**
     * Validate if a Hijri date is valid according to official data
     */
    public static boolean isValidHijriDate(int year, int month, int day) {
        if (!hasOfficialData(year, month)) {
            return false;
        }

        int monthLength = getOfficialMonthLength(year, month);
        return day >= 1 && day <= monthLength;
    }

    public static YMD islamicFromGregorian(GregorianCalendar gc) {
        IslamicCalendar islamicCalendar = new IslamicCalendar(Locale.US);
        islamicCalendar.setTime(gc.getTime());

        // Get year, month, day from standard Islamic calendar
        int year  = islamicCalendar.get(Calendar.YEAR);
        int month = islamicCalendar.get(Calendar.MONTH); // Convert 0-index to 1-index
        int day   = islamicCalendar.get(Calendar.DAY_OF_MONTH);

        // Adjust for Iranian Hijri using official table for years 1340-1448
        if (HIJRI_MONTH_DATA.containsKey(year)) {
            return adjustForIranianHijri(year, month, day);
        }

        // For years outside 1340-1448, return standard Islamic date
        return new YMD(year, month, day);
    }

    private static YMD adjustForIranianHijri(int year, int month, int day) {
        int[] monthLengths = HIJRI_MONTH_DATA.get(year);

        // If day exceeds month length, adjust to next month
        while (day > monthLengths[month]) {
            day -= monthLengths[month];
            month++;
            if (month > 11) {
                month = 1;
                year++;
                // Get new year's month lengths if available
                if (HIJRI_MONTH_DATA.containsKey(year)) {
                    monthLengths = HIJRI_MONTH_DATA.get(year);
                } else {
                    break; // Exit if next year not in table
                }
            }
        }

        return new YMD(year, month, day);
    }

    public static GregorianCalendar gregorianFromIslamic(YMD hijriDate) {
        // To convert from Iranian Hijri to Gregorian, we need to calculate Julian Day Number
        // This is more complex and requires knowing the start date of each month
        // For now, return approximate conversion using standard Islamic calendar

        IslamicCalendar islamicCalendar = new IslamicCalendar(Locale.US);
        islamicCalendar.set(Calendar.YEAR, hijriDate.year);
        islamicCalendar.set(Calendar.MONTH, hijriDate.month); // Convert 1-index to 0-index
        islamicCalendar.set(Calendar.DAY_OF_MONTH, hijriDate.day);

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(islamicCalendar.getTime());
        return gc;
    }

    // Get day of year for Iranian Hijri date
    public static int getDayOfIranianHijriYear(YMD hijriDate) {
        if (!HIJRI_MONTH_DATA.containsKey(hijriDate.year)) {
            return -1; // Year not in table
        }

        int[] monthLengths = HIJRI_MONTH_DATA.get(hijriDate.year);
        int   dayOfYear    = hijriDate.day;

        for (int i = 0; i < hijriDate.month; i++) {
            dayOfYear += monthLengths[i];
        }

        return dayOfYear;
    }

    // Check if a date is valid in Iranian Hijri calendar
    public static boolean isValidIranianHijriDate(YMD hijriDate) {
        if (!HIJRI_MONTH_DATA.containsKey(hijriDate.year)) {
            return false;
        }

        if (hijriDate.month < 0 || hijriDate.month > 11) {
            return false;
        }

        int[] monthLengths = HIJRI_MONTH_DATA.get(hijriDate.year);
        return hijriDate.day >= 1 && hijriDate.day <= monthLengths[hijriDate.month];
    }

    // Copy the table from previous response (truncated for brevity)
    private static HashMap<Integer, int[]> getIranianHijriMonthData() {
        HashMap<Integer, int[]> hijriData = new HashMap<>();
        // Add all the data from your table here (1340-1448)
        hijriData.put(1340, new int[]{29, 30, 29, 30, 30, 30, 29, 30, 30, 29, 29, 30});
        hijriData.put(1341, new int[]{29, 29, 30, 29, 30, 29, 30, 30, 30, 29, 30, 29});
        hijriData.put(1342, new int[]{30, 29, 29, 30, 29, 30, 29, 30, 30, 30, 29, 30});
        hijriData.put(1343, new int[]{29, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30});
        hijriData.put(1344, new int[]{29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30});
        hijriData.put(1345, new int[]{29, 30, 30, 29, 30, 30, 29, 29, 30, 29, 29, 30});
        hijriData.put(1346, new int[]{29, 30, 30, 29, 30, 30, 30, 29, 29, 30, 29, 29});
        hijriData.put(1347, new int[]{30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 30, 29});
        hijriData.put(1348, new int[]{29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 30});
        hijriData.put(1349, new int[]{29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, 29});
        hijriData.put(1350, new int[]{30, 29, 29, 30, 30, 29, 29, 30, 29, 30, 30, 29});
        hijriData.put(1351, new int[]{30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 29});
        hijriData.put(1352, new int[]{30, 30, 29, 30, 30, 30, 29, 29, 29, 30, 29, 30});
        hijriData.put(1353, new int[]{29, 30, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1354, new int[]{29, 30, 30, 30, 29, 30, 30, 29, 29, 30, 29, 30});
        hijriData.put(1355, new int[]{29, 29, 30, 30, 29, 30, 30, 29, 30, 29, 30, 29});
        hijriData.put(1356, new int[]{30, 29, 29, 30, 30, 29, 30, 29, 30, 30, 29, 30});
        hijriData.put(1357, new int[]{29, 30, 29, 30, 29, 30, 29, 29, 30, 30, 29, 30});
        hijriData.put(1358, new int[]{30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30});
        hijriData.put(1359, new int[]{30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 29, 30});
        hijriData.put(1360, new int[]{30, 30, 29, 30, 30, 29, 30, 29, 29, 30, 29, 30});
        hijriData.put(1361, new int[]{29, 30, 29, 30, 30, 29, 30, 30, 29, 29, 30, 29});
        hijriData.put(1362, new int[]{30, 29, 30, 29, 30, 30, 29, 30, 29, 30, 29, 30});
        hijriData.put(1363, new int[]{29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30, 29});
        hijriData.put(1364, new int[]{30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30});
        hijriData.put(1365, new int[]{29, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30});
        hijriData.put(1366, new int[]{29, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30});
        hijriData.put(1367, new int[]{30, 29, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29});
        hijriData.put(1368, new int[]{30, 29, 30, 30, 29, 30, 30, 29, 29, 30, 29, 30});
        hijriData.put(1369, new int[]{29, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30, 30});
        hijriData.put(1370, new int[]{29, 29, 30, 29, 30, 29, 30, 29, 30, 30, 29, 30});
        hijriData.put(1371, new int[]{29, 30, 29, 30, 29, 30, 29, 29, 30, 30, 30, 29});
        hijriData.put(1372, new int[]{30, 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, 30});
        hijriData.put(1373, new int[]{29, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 30});
        hijriData.put(1374, new int[]{29, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30});
        hijriData.put(1375, new int[]{29, 30, 30, 29, 30, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1376, new int[]{30, 29, 30, 29, 30, 30, 29, 30, 30, 29, 29, 30});
        hijriData.put(1377, new int[]{29, 30, 29, 29, 30, 30, 29, 30, 30, 30, 29, 30});
        hijriData.put(1378, new int[]{29, 29, 30, 29, 29, 30, 29, 30, 30, 30, 29, 30});
        hijriData.put(1379, new int[]{30, 29, 29, 30, 29, 29, 30, 29, 30, 30, 29, 30});
        hijriData.put(1380, new int[]{30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30});
        hijriData.put(1381, new int[]{30, 29, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1382, new int[]{30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 29, 30});
        hijriData.put(1383, new int[]{29, 29, 30, 30, 29, 30, 30, 30, 29, 30, 29, 29});
        hijriData.put(1384, new int[]{30, 29, 29, 30, 29, 30, 30, 30, 29, 30, 30, 29});
        hijriData.put(1385, new int[]{29, 30, 29, 29, 30, 29, 30, 30, 29, 30, 30, 30});
        hijriData.put(1386, new int[]{29, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30});
        hijriData.put(1387, new int[]{29, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 30});
        hijriData.put(1388, new int[]{29, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30});
        hijriData.put(1389, new int[]{29, 30, 30, 29, 30, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1390, new int[]{30, 29, 30, 29, 30, 30, 30, 29, 30, 29, 30, 29});
        hijriData.put(1391, new int[]{29, 30, 29, 29, 30, 30, 30, 29, 30, 30, 29, 30});
        hijriData.put(1392, new int[]{29, 29, 30, 29, 29, 30, 30, 29, 30, 30, 29, 30});
        hijriData.put(1393, new int[]{30, 29, 29, 30, 29, 29, 30, 30, 29, 30, 29, 30});
        hijriData.put(1394, new int[]{30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 29, 30});
        hijriData.put(1395, new int[]{30, 30, 29, 30, 29, 30, 29, 30, 29, 29, 29, 30});
        hijriData.put(1396, new int[]{30, 29, 30, 30, 30, 29, 30, 29, 30, 29, 29, 30});
        hijriData.put(1397, new int[]{29, 30, 29, 30, 30, 30, 29, 30, 29, 30, 29, 29});
        hijriData.put(1398, new int[]{30, 29, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30});
        hijriData.put(1399, new int[]{29, 30, 29, 30, 29, 29, 30, 30, 29, 30, 30, 29});
        hijriData.put(1400, new int[]{30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 30, 30});
        hijriData.put(1401, new int[]{29, 30, 30, 29, 29, 30, 29, 29, 30, 29, 30, 29});
        hijriData.put(1402, new int[]{30, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30});
        hijriData.put(1403, new int[]{29, 30, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1404, new int[]{30, 29, 30, 30, 30, 29, 30, 29, 30, 29, 29, 30});
        hijriData.put(1405, new int[]{29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 30});
        hijriData.put(1406, new int[]{29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30});
        hijriData.put(1407, new int[]{30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 29});
        hijriData.put(1408, new int[]{30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 29});
        hijriData.put(1409, new int[]{30, 30, 30, 29, 29, 30, 29, 30, 29, 29, 30, 30});
        hijriData.put(1410, new int[]{29, 30, 30, 29, 30, 29, 30, 29, 30, 29, 29, 30});
        hijriData.put(1411, new int[]{30, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 29});
        hijriData.put(1412, new int[]{30, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30, 29});
        hijriData.put(1413, new int[]{30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30});
        hijriData.put(1414, new int[]{29, 30, 29, 29, 30, 29, 30, 29, 29, 30, 30, 30});
        hijriData.put(1415, new int[]{30, 30, 29, 29, 29, 30, 29, 29, 29, 30, 30, 30});
        hijriData.put(1416, new int[]{30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 29});
        hijriData.put(1417, new int[]{30, 30, 30, 29, 29, 30, 29, 30, 29, 30, 29, 29});
        hijriData.put(1418, new int[]{30, 30, 29, 30, 30, 29, 30, 29, 29, 30, 30, 29});
        hijriData.put(1419, new int[]{29, 30, 29, 30, 29, 30, 30, 29, 29, 30, 30, 30});
        hijriData.put(1420, new int[]{29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, 29});
        hijriData.put(1421, new int[]{30, 29, 29, 30, 29, 29, 30, 30, 29, 30, 30, 30});
        hijriData.put(1422, new int[]{29, 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, 30});
        hijriData.put(1423, new int[]{29, 30, 30, 29, 29, 30, 29, 30, 29, 30, 29, 30});
        hijriData.put(1424, new int[]{30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29});
        hijriData.put(1425, new int[]{30, 29, 30, 30, 29, 30, 30, 29, 29, 30, 29, 30});
        hijriData.put(1426, new int[]{29, 29, 30, 29, 30, 30, 30, 29, 30, 30, 29, 29});
        hijriData.put(1427, new int[]{30, 29, 29, 30, 29, 30, 30, 30, 29, 30, 29, 30});
        hijriData.put(1428, new int[]{29, 30, 29, 29, 29, 30, 30, 29, 30, 30, 30, 29});
        hijriData.put(1429, new int[]{30, 29, 30, 29, 29, 29, 30, 30, 29, 30, 30, 29});
        hijriData.put(1430, new int[]{30, 30, 29, 29, 30, 29, 30, 29, 29, 30, 30, 29});
        hijriData.put(1431, new int[]{30, 30, 29, 30, 29, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1432, new int[]{30, 30, 29, 30, 30, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1433, new int[]{29, 30, 29, 30, 30, 30, 29, 30, 29, 30, 29, 30});
        hijriData.put(1434, new int[]{29, 29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29});
        hijriData.put(1435, new int[]{29, 30, 29, 30, 29, 30, 29, 30, 30, 30, 29, 30});
        hijriData.put(1436, new int[]{29, 30, 29, 29, 30, 29, 30, 29, 30, 29, 30, 30});
        hijriData.put(1437, new int[]{29, 30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30});
        hijriData.put(1438, new int[]{29, 30, 30, 30, 29, 30, 29, 29, 30, 29, 29, 30});
        hijriData.put(1439, new int[]{29, 30, 30, 30, 30, 29, 30, 29, 29, 30, 29, 29});
        hijriData.put(1440, new int[]{30, 29, 30, 30, 30, 29, 30, 30, 29, 29, 30, 29});
        hijriData.put(1441, new int[]{29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 30});
        hijriData.put(1442, new int[]{29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, 29});
        hijriData.put(1443, new int[]{29, 30, 30, 29, 29, 30, 29, 30, 30, 29, 30, 29});
        hijriData.put(1444, new int[]{30, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 29});
        hijriData.put(1445, new int[]{30, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29, 29});
        hijriData.put(1446, new int[]{30, 30, 30, 29, 30, 30, 29, 30, 29, 29, 29, 30});
        hijriData.put(1447, new int[]{29, 30, 30, 29, 30, 30, 30, 29, 30, 29, 29, 29});
        hijriData.put(1448, new int[]{30, 29, 30, 29, 30, 30, 30, 29, 30, 29, 30,
                29}); // Note: Last 2 months added from pattern

        return hijriData;
    }


    // Example usage
    public static void main(String[] args) {
        // Current date
        GregorianCalendar now = new GregorianCalendar();

        // Convert to Iranian Hijri
        YMD hijriDate = islamicFromGregorian(now);
        System.out.println("Iranian Hijri date: " + hijriDate);

        // Check if valid
        if (isValidIranianHijriDate(hijriDate)) {
            System.out.println("Valid Iranian Hijri date");
            int dayOfYear = getDayOfIranianHijriYear(hijriDate);
            System.out.println("Day of year: " + dayOfYear);
        }

        // Convert back to Gregorian (approximate)
        GregorianCalendar approxGregorian = gregorianFromIslamic(hijriDate);
        System.out.println("Approximate Gregorian date: " + approxGregorian.getTime());
    }

}
