package com.farashian.pcalendar.fast;


import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.farashian.pcalendar.PCConstants.PERSIAN_MONTH_NAMES;


public class FDateUtils {

    public static int      THIS_YEAR;
    static        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    static FastPersianDateFormat dayOfWeek           = new FastPersianDateFormat("dddd"); //Day of week
    static FastPersianDateFormat fullDateWithDay     = new FastPersianDateFormat("dddd, dd MMMM yyyy");
    static FastPersianDateFormat fullDate            = new FastPersianDateFormat("dd MMMM yyyy");
    static FastPersianDateFormat dashDate1           = new FastPersianDateFormat("dd-MMM-yyyy");
    static FastPersianDateFormat dateTime            = new FastPersianDateFormat("dd MMMM yyyy HH:mm");
    static FastPersianDateFormat fullDateTimeWithDay = new FastPersianDateFormat("dddd, dd MMMM yyyy HH:mm");
    static FastPersianDateFormat timestampDash       = new FastPersianDateFormat("yyyy-MM-dd-HH:mm");
    static FastPersianDateFormat timestampUnderscore = new FastPersianDateFormat("yyyy-MM-dd_HH-mm");
    static FastPersianDateFormat slashDate           = new FastPersianDateFormat("yyyy/MM/dd");
    static FastPersianDateFormat dashDate            = new FastPersianDateFormat("yyyy-MM-dd");
    static FastPersianDateFormat time                = new FastPersianDateFormat("HH:mm");
    static FastPersianDateFormat hour                = new FastPersianDateFormat("HH");
    static FastPersianDateFormat minute              = new FastPersianDateFormat("mm");
    static FastPersianDateFormat timeWithSeconds     = new FastPersianDateFormat("HH:mm:ss");

    public static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

    static {
        THIS_YEAR = new FastPersianCalendar().getYear();
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static Date nowDate() {
        return new Date(System.currentTimeMillis());
    }

    public static Date from(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

    }

    public static Date from(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

    }

    public static LocalDate fromDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault())
                .toLocalDate();

    }

    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault())
                .toLocalDateTime();

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

    public static String getDateFarsiDash() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return dashDate.format(pdate);
    }


    public static String getDateFarsi(FastPersianCalendar pdate, String pattern) {
        FastPersianDateFormat pdf = new FastPersianDateFormat(pattern);
        return pdf.format(pdate);
    }


    public static Date toDate(FastPersianCalendar date) {
        return date.getTime();
    }

    public static FastPersianCalendar toFastPersianCalendar(Date date) {
        if (date == null) return null;
        return new FastPersianCalendar(date.getTime());
    }

    public static FastPersianCalendar toFastPersianCalendar(String date) {
        if (date == null) return null;

        try {
            FastPersianCalendar pc = new FastPersianCalendar();
            //Parse date in format "yyyy/MM/dd"
            String[] parts = date.split("/");
            if (parts.length == 3) {
                pc.setDate(
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

    public static String toSlashDate(FastPersianCalendar date) {
        if (date == null) return null;
        return slashDate.format(date);
    }

    public static String getFullDateWithDayFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return fullDateWithDay.format(pdate);
    }

    public static String getFullDateTimeWithDayFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return fullDateTimeWithDay.format(pdate);
    }

    public static String toDateString(Date date) {
        if (date == null) return "";
        FastPersianCalendar pdate = new FastPersianCalendar(date);

        return slashDate.format(pdate);
    }

    public static String toDateString(FastPersianCalendar pdate) {
        if (pdate == null) return "";
        slashDate.setNumberCharacter(FastPersianDateFormat.NumberCharacter.ENGLISH);
        return slashDate.format(pdate);
    }

    public static String toDateStringFarsi(FastPersianCalendar pdate) {
        if (pdate == null) return "";
        slashDate.setNumberCharacter(FastPersianDateFormat.NumberCharacter.FARSI);
        return slashDate.format(pdate);
    }

    public static String toDateString(long timeStamp) {
        FastPersianCalendar pdate = new FastPersianCalendar(timeStamp);
        slashDate.setNumberCharacter(FastPersianDateFormat.NumberCharacter.ENGLISH);
        return slashDate.format(pdate);
    }

    public static String getDayName() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return dayOfWeek.format(pdate);
    }

    public static String getDayName(FastPersianCalendar pdate) {
        return dayOfWeek.format(pdate);
    }


    public static String getDateFarsiDash(FastPersianCalendar pdate) {
        return dashDate.format(pdate);
    }

    public static String getDateFarsiDash(Date date) {
        FastPersianCalendar pdate = new FastPersianCalendar(date.getTime());
        return fullDate.format(pdate);
    }

    public static String getFullDateFarsi(long timeStamp) {
        FastPersianCalendar pdate = new FastPersianCalendar(timeStamp);
        return fullDate.format(pdate);
    }

    public static String getFullDateFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return fullDate.format(pdate);
    }

    public static String getFullDateFarsi(Date date) {
        FastPersianCalendar pdate = new FastPersianCalendar(date.getTime());
        return fullDate.format(pdate);
    }

    public static String getFullDateFarsi(FastPersianCalendar pdate) {
        if (pdate == null) return "";
        return fullDate.format(pdate);
    }

    public static String getFullDateTimeWithDayFarsi(long timeStamp) {
        FastPersianCalendar pdate = new FastPersianCalendar(timeStamp);
        return fullDateTimeWithDay.format(pdate);
    }

    public static String getFullDateTimeWithDayFarsi(Date date) {
        FastPersianCalendar pdate = new FastPersianCalendar(date.getTime());
        return fullDateTimeWithDay.format(pdate);
    }

    public static String getFullDateTimeWithDayFarsi(FastPersianCalendar pdate) {
        if (pdate == null) return "";
        return fullDateTimeWithDay.format(pdate);
    }

    public static String getFullDateWithDayFarsi(long timeStamp) {
        FastPersianCalendar pdate = new FastPersianCalendar(timeStamp);
        return fullDateWithDay.format(pdate);
    }

    public static String getFullDateWithDayFarsi(FastPersianCalendar pdate) {
        if (pdate == null) return "";
        return fullDateWithDay.format(pdate);
    }

    public static String getFullDateMiladi() {
        FastPersianCalendar pdate = new FastPersianCalendar();

        return pdate.getGrgLongDate();
    }

    public static String getFullDateWithDayMiladi() {
        FastPersianCalendar pdate = new FastPersianCalendar();

        return pdate.getGrgLongDateWithDay();
    }

    public static String getFullDateWithDayFarsi(Date date) {
        if (date == null) return "";
        FastPersianCalendar pdate = new FastPersianCalendar(date.getTime());
        return fullDateWithDay.format(pdate);
    }

    public static String getDateWithTimeFarsi(Date date) {
        FastPersianCalendar pdate = new FastPersianCalendar(date.getTime());
        return dateTime.format(pdate);
    }

    public static String getDateWithTimeFarsi(FastPersianCalendar pdate) {
        return dateTime.format(pdate);
    }

    public static String getDateWithTimeFarsi(long dateInMilis) {
        FastPersianCalendar pdate = new FastPersianCalendar(dateInMilis);
        return dateTime.format(pdate);
    }

    public static String getDateWithTimeFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return dateTime.format(pdate);
    }

    public static String getDateSlashFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return slashDate.format(pdate);
    }

    public static String getDateSlashFarsi(Date date) {
        if (date == null) return "";
        FastPersianCalendar pdate = new FastPersianCalendar(date);
        return slashDate.format(pdate);
    }

    public static String getDateSlashFarsi(long dateInMilis) {
        FastPersianCalendar pdate = new FastPersianCalendar(dateInMilis);
        return slashDate.format(pdate);
    }

    public static String getDateSlashFarsi(FastPersianCalendar pdate) {
        if (pdate == null) return "";
        return slashDate.format(pdate);
    }

    public static String dateBeforeNowFarsi(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);
        FastPersianCalendar pdate = new FastPersianCalendar(cal.getTimeInMillis());
        return slashDate.format(pdate);
    }

    public static String[] getTimeOnly() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return time.format(pdate).split(":");
    }

    public static String getDateTimeStampDashFarsi(Date date) {
        FastPersianCalendar pdate = new FastPersianCalendar(date);
        return timestampDash.format(pdate);
    }

    public static String getDateTimeStampDashFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return timestampDash.format(pdate);
    }

    public static String getDateTimeStampUnderscoreFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return timestampUnderscore.format(pdate);
    }

    public static String getDateTimeStampUnderscoreEnglish() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return timestampUnderscore.format(pdate, FastPersianDateFormat.NumberCharacter.ENGLISH);
    }

    public static String getDateTimeStampUnderscoreFarsi(Date date) {
        FastPersianCalendar pdate = new FastPersianCalendar(date.getTime());
        return timestampUnderscore.format(pdate);
    }

    public static String getTime(Date date) {
        if (date == null) return "";
        FastPersianCalendar pdate = new FastPersianCalendar(date.getTime());
        return time.format(pdate);
    }

    public static String getTime() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return time.format(pdate);
    }

    public static String getTimeWithSeconds() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return timeWithSeconds.format(pdate);
    }

    public static String getDateFarsi(String pattern) {
        if (pattern == null) return "";
        FastPersianCalendar   pdate = new FastPersianCalendar();
        FastPersianDateFormat pdf   = new FastPersianDateFormat(pattern);
        return pdf.format(pdate);
    }


    public static String[] weekNames = new String[]{
            "", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه"

    };

    public static int getDay(String day) {
        for (int i = 0; i < weekNames.length; i++) {
            if (day.equals(weekNames[i]))
                return i;
        }

        return -1;
    }

    public static String getDay(int day) {
        return weekNames[day];
    }

    public static int getMonth(Date date) {
        FastPersianCalendar pdate = new FastPersianCalendar(date);
        return pdate.getMonth();
    }

    public static String getMonthName(Date date) {
        FastPersianCalendar pdate = new FastPersianCalendar(date);
        return pdate.getMonthName();
    }

    public static String getMonthName(long date) {
        FastPersianCalendar pdate = new FastPersianCalendar(date);
        return pdate.getMonthName();
    }

    public static String getMonthNameByNumber(int number) {
        return PERSIAN_MONTH_NAMES[number - 1];
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

    public static long getStartDate(FastPersianCalendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long getStartDate1(int persianYear, int persianMonth, int persianDay) {
        FastPersianCalendar persianCalendar = new FastPersianCalendar();
        persianCalendar.setDate(persianYear, persianMonth, persianDay);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(persianCalendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long getStartDate(int persianYear, int persianMonth, int persianDay) {
        FastPersianCalendar persianCalendar = new FastPersianCalendar();
        persianCalendar.setDate(persianYear, persianMonth, persianDay);

        persianCalendar.set(Calendar.HOUR_OF_DAY, 0);
        persianCalendar.set(Calendar.MINUTE, 0);
        persianCalendar.set(Calendar.SECOND, 0);
        persianCalendar.set(Calendar.MILLISECOND, 0);

        return persianCalendar.getTimeInMillis();
    }

    public static long getStartDate(int persianYear) {
        FastPersianCalendar persianCalendar = new FastPersianCalendar();
        persianCalendar.setDate(persianYear, 1, 1);

        persianCalendar.set(Calendar.HOUR_OF_DAY, 0);
        persianCalendar.set(Calendar.MINUTE, 0);
        persianCalendar.set(Calendar.SECOND, 0);
        persianCalendar.set(Calendar.MILLISECOND, 0);

        return persianCalendar.getTimeInMillis();
    }

    public static long getStartOfCurrentYear() {
        FastPersianCalendar persianCalendar = new FastPersianCalendar();
        int                 currentYear     = persianCalendar.getPersianYear();

        persianCalendar.setDate(currentYear, 1, 1);
        persianCalendar.set(Calendar.HOUR_OF_DAY, 0);
        persianCalendar.set(Calendar.MINUTE, 0);
        persianCalendar.set(Calendar.SECOND, 0);
        persianCalendar.set(Calendar.MILLISECOND, 0);

        return persianCalendar.getTimeInMillis();
    }

    public static long getStartOfYear(int persianYear) {
        FastPersianCalendar persianCalendar = new FastPersianCalendar();
        persianCalendar.setDate(persianYear, 1, 1);

        persianCalendar.set(Calendar.HOUR_OF_DAY, 0);
        persianCalendar.set(Calendar.MINUTE, 0);
        persianCalendar.set(Calendar.SECOND, 0);
        persianCalendar.set(Calendar.MILLISECOND, 0);

        return persianCalendar.getTimeInMillis();
    }

    public static long getEndOfYear(int persianYear) {
        FastPersianCalendar persianCalendar = new FastPersianCalendar();

        // Check if the year is a leap year FIRST
        int lastDay = FastPersianCalendar.isLeapYear(persianYear) ? 30 : 29;

        // Then set the date to the last day of Esfand
        persianCalendar.setDate(persianYear, 12, lastDay);

        // Set to end of day
        persianCalendar.set(Calendar.HOUR_OF_DAY, 23);
        persianCalendar.set(Calendar.MINUTE, 59);
        persianCalendar.set(Calendar.SECOND, 59);
        persianCalendar.set(Calendar.MILLISECOND, 999); // Should be 999, not 59

        return persianCalendar.getTimeInMillis();
    }

    public static long getStartOfMonth(int persianYear, int persianMonth) {
        FastPersianCalendar cal = new FastPersianCalendar();
        cal.setDate(persianYear, persianMonth, 1);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    public static long getEndOfMonth(int persianYear, int persianMonth) {
        FastPersianCalendar cal = new FastPersianCalendar();

        int lastDay;
        if (persianMonth <= 6) {
            lastDay = 31;
        } else if (persianMonth <= 11) {
            lastDay = 30;
        } else {
            lastDay = FastPersianCalendar.isLeapYear(persianYear) ? 30 : 29;
        }

        cal.setDate(persianYear, persianMonth, lastDay);

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return cal.getTimeInMillis();
    }


    public static long getEndDate1(int persianYear, int persianMonth, int persianDay) {
        FastPersianCalendar FastPersianCalendar = new FastPersianCalendar();
        FastPersianCalendar.setDate(persianYear, persianMonth, persianDay);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(FastPersianCalendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTimeInMillis();
    }

    public static long getEndDate(int persianYear, int persianMonth, int persianDay) {
        FastPersianCalendar calendar = new FastPersianCalendar();
        calendar.setDate(persianYear, persianMonth, persianDay);

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

    public static long getCurrentDateEndDate() {
        FastPersianCalendar calendar = new FastPersianCalendar();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        return calendar.getTimeInMillis();
    }

    public static long getEndDate(FastPersianCalendar calendar) {
        //Calendar calendar = Calendar.getInstance();
        //calendar.setTimeInMillis(dateTime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }

    public static int calculateDaysBetween(FastPersianCalendar startDate,
            FastPersianCalendar endDate) {
        long startMillis = startDate.getTimeInMillis();
        long endMillis   = endDate.getTimeInMillis();
        long diffMillis  = endMillis - startMillis;
        return (int) (diffMillis / (24 * 60 * 60 * 1000));
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
}
