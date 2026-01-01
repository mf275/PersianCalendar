package com.farashian.pcalendar.util;


import com.farashian.pcalendar.PersianCalendar;
import com.farashian.pcalendar.PersianDateFormat;
import com.farashian.pcalendar.fast.FastPersianCalendar;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    public static int      THIS_YEAR;
    static        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    static PersianDateFormat dayOfWeek           = new PersianDateFormat("dddd"); //Day of week
    static PersianDateFormat fullDateWithDay     = new PersianDateFormat("dddd, dd MMMM yyyy");
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
            //Parse date in format "yyyy/MM/dd"
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

    public static String getFarsiFullDate(PersianCalendar pdate) {
        if (pdate == null) return "";
        return fullDate.format(pdate);
    }

    public static String getFarsiFullDateWithDay(PersianCalendar pdate) {
        if (pdate == null) return "";
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

    public static int calculateDaysBetween(FastPersianCalendar startDate, FastPersianCalendar endDate) {
        long startMillis = startDate.getTimeInMillis();
        long endMillis = endDate.getTimeInMillis();
        long diffMillis = endMillis - startMillis;
        return (int) (diffMillis / (24 * 60 * 60 * 1000));
    }

    public static int calculateDaysBetween(PersianCalendar startDate, PersianCalendar endDate) {
        long startMillis = startDate.getTimeInMillis();
        long endMillis = endDate.getTimeInMillis();
        long diffMillis = endMillis - startMillis;
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
