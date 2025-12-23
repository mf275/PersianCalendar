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

public class FDateUtils {

    public static int      THIS_YEAR;
    static        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    static FastPersianDateFormat dayOfWeek           = new FastPersianDateFormat("dddd"); // Day of week
    static FastPersianDateFormat fullDateWithDay     = new FastPersianDateFormat("dddd dd MMMM yyyy");
    static FastPersianDateFormat fullDate            = new FastPersianDateFormat("dd MMMM yyyy");
    static FastPersianDateFormat dashDate1           = new FastPersianDateFormat("dd-MMM-yyyy");
    static FastPersianDateFormat dateTime            = new FastPersianDateFormat("dd MMMM yyyy HH:mm");
    static FastPersianDateFormat timestampDash       = new FastPersianDateFormat("yyyy-MM-dd-HH:mm");
    static FastPersianDateFormat timestampUnderscore = new FastPersianDateFormat("yyyy-MM-dd_HH-mm");
    static FastPersianDateFormat slashDate           = new FastPersianDateFormat("yyyy/MM/dd");
    static FastPersianDateFormat dashDate            = new FastPersianDateFormat("yyyy-MM-dd");
    static FastPersianDateFormat time                = new FastPersianDateFormat("HH:mm");
    static FastPersianDateFormat timeWithSeconds     = new FastPersianDateFormat("HH:mm:ss");

    public static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";

    static {
        THIS_YEAR = new FastPersianCalendar().getYear();
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static Date from(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date from(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate fromDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
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
        FastPersianCalendar pdate = new FastPersianCalendar();
        return dashDate.format(pdate);
    }

    public static String getFarsidate(String pattern) {
        FastPersianCalendar   pdate = new FastPersianCalendar();
        FastPersianDateFormat pdf   = new FastPersianDateFormat(pattern);
        return pdf.format(pdate);
    }


    public static Date toDate(FastPersianCalendar date) {
        return date.getTime();
    }

    public static FastPersianCalendar toPersianDate(Date date) {
        if (date == null) return null;
        return new FastPersianCalendar(date.getTime());
    }

    public static FastPersianCalendar toPersianDate(String date) {
        if (date == null) return null;

        try {
            FastPersianCalendar pc = new FastPersianCalendar();
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

    public static String toSlashDate(FastPersianCalendar date) {
        if (date == null) return null;
        return slashDate.format(date);
    }

    public static String nowFullDateFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return fullDate.format(pdate);
    }

    public static String nowFullDateWithDayFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return fullDateWithDay.format(pdate);
    }

    public static String nowFarsiDay() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return dayOfWeek.format(pdate);
    }

    public static String nowDashDateFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return dashDate.format(pdate);
    }

    public static String getFarsiFullDate(long timeStamp) {
        FastPersianCalendar pdate = new FastPersianCalendar(timeStamp);
        return fullDate.format(pdate);
    }

    public static String getFarsiFullDateWithDay(long timeStamp) {
        FastPersianCalendar pdate = new FastPersianCalendar(timeStamp);
        return fullDateWithDay.format(pdate);
    }

    public static String getFarsiFullDate(Date date) {
        FastPersianCalendar pdate = new FastPersianCalendar(date.getTime());
        return fullDate.format(pdate);
    }

    public static String getFarsiFullDateWithDay(Date date) {
        FastPersianCalendar pdate = new FastPersianCalendar(date.getTime());
        return fullDateWithDay.format(pdate);
    }

    public static String getFarsiDateWithTime(Date date) {
        FastPersianCalendar pdate = new FastPersianCalendar(date.getTime());
        return dateTime.format(pdate);
    }

    public static String getFarsiDateWithTime(long dateInMilis) {
        FastPersianCalendar pdate = new FastPersianCalendar(dateInMilis);
        return dateTime.format(pdate);
    }

    public static String getFarsiDateWithTime() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return dateTime.format(pdate);
    }

    public static String nowSlashDateFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return slashDate.format(pdate);
    }

    public static String getSlashDateFarsi(long dateInMilis) {
        FastPersianCalendar pdate = new FastPersianCalendar(dateInMilis);
        return slashDate.format(pdate);
    }

    public static String dateBeforeNowFarsi(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -days);
        FastPersianCalendar pdate = new FastPersianCalendar(cal.getTimeInMillis());
        return slashDate.format(pdate);
    }

    public static String[] nowOnlyTime() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return time.format(pdate).split(":");
    }

    public static String nowTimeStampDashFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return timestampDash.format(pdate);
    }

    public static String nowTimeStampUnderscoreFarsi() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return timestampUnderscore.format(pdate);
    }

    public static String getTime(Date date) {
        if (date == null) return "";
        FastPersianCalendar pdate = new FastPersianCalendar(date.getTime());
        return time.format(pdate);
    }

    public static String getNowTime() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return time.format(pdate);
    }

    public static String getNowTimeWithSeconds() {
        FastPersianCalendar pdate = new FastPersianCalendar();
        return timeWithSeconds.format(pdate);
    }

    public static String nowFarsi(String pattern) {
        FastPersianCalendar   pdate = new FastPersianCalendar();
        FastPersianDateFormat pdf   = new FastPersianDateFormat(pattern);
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
        FastPersianCalendar FastPersianCalendar = new FastPersianCalendar();
        FastPersianCalendar.setPersianDate(persianYear, persianMonth, persianDay);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(FastPersianCalendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public static long getEndDate(int persianYear, int persianMonth, int persianDay) {
        FastPersianCalendar FastPersianCalendar = new FastPersianCalendar();
        FastPersianCalendar.setPersianDate(persianYear, persianMonth, persianDay);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(FastPersianCalendar.getTime());
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

}
