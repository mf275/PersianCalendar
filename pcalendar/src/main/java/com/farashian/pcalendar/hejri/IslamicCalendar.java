package com.farashian.pcalendar.hejri;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.farashian.pcalendar.util.HijriConverter.getIranianHijriMonthData;

public class IslamicCalendar extends Calendar {
    private int[] today = {1446, 1, 1};
    private int[] selectedDay = {1403, 1, 1};
    private int[] todayGregorian = {2024, 1, 1};
    private int startJulianDay = 2192399;
    private int endJulianDay = 2195868;
    Map<Integer, int[]> hijriMonthsDays;

    private ScheduledExecutorService scheduler;
    private Map<String, Boolean> options = new HashMap<>();
    
    private final String[] persianMonthNames = {
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    };
    
    private final String[] hijriMonthNames = {
        "محرم", "صفر", "ربیع الاول", "ربیع الثانی", "جمادی الاول", "جمادی الثانیه",
        "رجب", "شعبان", "رمضان", "شوال", "ذیقعده", "ذیحجه"
    };
    
    private final String[] gregorianMonthNames = {
        "ژانویه", "فوریه", "مارس", "آپریل", "می", "ژوئن",
        "جولای", "آگوست", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
    };
    
    private final String[] dayNames = {"شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"};
    

    public IslamicCalendar() {
        this(new HashMap<>());
    }
    
    public IslamicCalendar(Map<String, Boolean> options) {
        if (options != null) {
            this.options = options;
        }
        hijriMonthsDays = getIranianHijriMonthData();
    }

    public IslamicCalendar(Locale us) {
        this(new HashMap<>());
    }


    public int[] jalaliToHijri(int year, int month, int day) {
        year += 1595;
        
        double julianDay = 1365392 +
                          (365 * year) +
                          (Math.floor(year / 33.0) * 8) +
                          Math.floor((year % 33 + 3) / 4.0) +
                          day +
                          ((month < 7) ? (month - 1) * 31 : ((month - 7) * 30) + 186) - 0.5;
        
        int hijriYear = (int)Math.floor(((30 * (julianDay - 194843.5)) + 10646) / 10631);
        double temp = julianDay - (1948439.5 + ((hijriYear - 1) * 354) + Math.floor((3 + (11 * hijriYear)) / 30.0));
        
        hijriYear -= 990;
        
        if (julianDay >= startJulianDay && julianDay <= endJulianDay) {
            int hijriDay = (int)Math.floor(julianDay - startJulianDay + 1);
            
            for (Map.Entry<Integer, int[]> entry : hijriMonthsDays.entrySet()) {
                int y = entry.getKey();
                int[] yearData = entry.getValue();
                
                if (hijriDay > yearData[0]) {
                    hijriDay -= yearData[0];
                } else {
                    int hijriMonth = 1;
                    while (hijriMonth < 13 && hijriDay > yearData[hijriMonth]) {
                        hijriDay -= yearData[hijriMonth];
                        hijriMonth++;
                    }
                    
                    return new int[]{y, hijriMonth, hijriDay};
                }
            }
        }
        
        int hijriMonth = (int)Math.floor(((temp - 29) / 29.5) + 1.99);
        if (hijriMonth > 12) hijriMonth = 12;
        
        int hijriDay = (int)Math.floor(1 + temp - Math.floor((29.5 * (hijriMonth - 1)) + 0.5));
        
        return new int[]{hijriYear, hijriMonth, hijriDay};
    }

    public int[] hijriToJalali(int year, int month, int day) {
        year += 990;
        
        double julianDay = 1948439.5 + ((year - 1) * 354) + Math.floor((3 + (11 * year)) / 30.0);
        
        if (month > 1) {
            for (int i = 1; i < month; i++) {
                julianDay += (i % 2 == 1) ? 30 : 29;
            }
        }
        
        julianDay += day;
        
        int jalaliYear = (int)Math.floor(((julianDay - 1365392) * 0.00273785) + 0.5);
        jalaliYear -= 1595;
        
        double temp = julianDay - (1365392 + (365 * (jalaliYear + 1595)) + 
                   Math.floor((jalaliYear + 1595) / 33.0) * 8 + 
                   Math.floor(((jalaliYear + 1595) % 33 + 3) / 4.0));
        
        int jalaliMonth = (temp <= 186) ? (int)Math.ceil(temp / 31.0) : (int)Math.ceil((temp - 186) / 30.0) + 6;
        int jalaliDay = (int)(temp - ((jalaliMonth <= 6) ? (jalaliMonth - 1) * 31 : ((jalaliMonth - 7) * 30) + 186));
        
        return new int[]{jalaliYear, jalaliMonth, jalaliDay};
    }

    public int[] gregorianToHijri(int year, int month, int day) {
        int[] jalali = gregorianToJalali(year, month, day);
        return jalaliToHijri(jalali[0], jalali[1], jalali[2]);
    }


    public int[] gregorianToJalali(int year, int month, int day) {
        int gy = year - 1600;
        int gm = month - 1;
        int gd = day - 1;

        int gDayNo = 365 * gy + (int)Math.floor((gy + 3) / 4) - (int)Math.floor((gy + 99) / 100) + (int)Math.floor((gy + 399) / 400);

        for (int i = 0; i < gm; ++i) {
            gDayNo += GregorianMonthDays(i + 1, gy + 1600);
        }

        gDayNo += gd;

        int jDayNo = gDayNo - 79;
        int j_np = (int)Math.floor(jDayNo / 12053);
        jDayNo = jDayNo % 12053;

        int jy = 979 + 33 * j_np + 4 * (int)Math.floor(jDayNo / 1461);
        jDayNo %= 1461;

        if (jDayNo >= 366) {
            jy += (int)Math.floor((jDayNo - 1) / 365);
            jDayNo = (jDayNo - 1) % 365;
        }

        int jm, jd;
        for (int i = 0; i < 11; i++) {
            int monthDays = (i < 6) ? 31 : 30;
            if (jDayNo >= monthDays) {
                jDayNo -= monthDays;
            } else {
                jm = i + 1;
                jd = jDayNo + 1;
                return new int[]{jy, jm, jd};
            }
        }

        jm = 12;
        jd = jDayNo + 1;
        return new int[]{jy, jm, jd};
    }

    public int[] jalaliToGregorian(int year, int month, int day) {
        int jy = year - 979;
        int jm = month - 1;
        int jd = day - 1;

        int jDayNo = 365 * jy + (int)Math.floor(jy / 33) * 8 + (int)Math.floor((jy % 33 + 3) / 4);

        for (int i = 0; i < jm; ++i) {
            jDayNo += (i < 6) ? 31 : 30;
        }

        jDayNo += jd;

        int gDayNo = jDayNo + 79;
        int gy = 1600 + 400 * (int)Math.floor(gDayNo / 146097);
        gDayNo = gDayNo % 146097;

        boolean leap = true;
        if (gDayNo >= 36525) {
            gDayNo--;
            gy += 100 * (int)Math.floor(gDayNo / 36524);
            gDayNo = gDayNo % 36524;
            if (gDayNo >= 365) {
                gDayNo++;
            } else {
                leap = false;
            }
        }

        gy += 4 * (int)Math.floor(gDayNo / 1461);
        gDayNo %= 1461;

        if (gDayNo >= 366) {
            leap = false;
            gDayNo--;
            gy += (int)Math.floor(gDayNo / 365);
            gDayNo = gDayNo % 365;
        }

        int i;
        for (i = 0; gDayNo >= GregorianMonthDays(i + 1, gy); i++) {
            gDayNo -= GregorianMonthDays(i + 1, gy);
        }

        int gm = i + 1;
        int gd = gDayNo + 1;

        return new int[]{gy, gm, gd};
    }

    private int GregorianMonthDays(int month, int year) {
        if (month == 2) {
            return isGregorianLeapYear(year) ? 29 : 28;
        }
        return (month == 4 || month == 6 || month == 9 || month == 11) ? 30 : 31;
    }

    private boolean isGregorianLeapYear(int year) {
        return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0));
    }

    public String getPersianMonthName(int month) {
        return (month >= 1 && month <= 12) ? persianMonthNames[month - 1] : "";
    }

    public String getHijriMonthName(int month) {
        return (month >= 1 && month <= 12) ? hijriMonthNames[month - 1] : "";
    }

    public String getGregorianMonthName(int month) {
        return (month >= 1 && month <= 12) ? gregorianMonthNames[month - 1] : "";
    }

    public String getDayName(int dayOfWeek) {
        return (dayOfWeek >= 0 && dayOfWeek < 7) ? dayNames[dayOfWeek] : "";
    }

    public void setSelectedDay(int year, int month, int day) {
        this.selectedDay = new int[]{year, month, day};
    }

    public void startAutoUpdate() {
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            updateToday();
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void stopAutoUpdate() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private void updateToday() {
        Calendar now = Calendar.getInstance();
        int[] hijri = gregorianToHijri(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
        this.today = hijri;
        this.todayGregorian = new int[]{now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH)};
    }

    public int[] getToday() { return today.clone(); }
    public int[] getSelectedDay() { return selectedDay.clone(); }
    public int[] getTodayGregorian() { return todayGregorian.clone(); }
    public String[] getPersianMonthNames() { return persianMonthNames.clone(); }
    public String[] getHijriMonthNames() { return hijriMonthNames.clone(); }
    public String[] getGregorianMonthNames() { return gregorianMonthNames.clone(); }
    public String[] getDayNames() { return dayNames.clone(); }

    @Override
    protected void computeTime() {}
    @Override
    protected void computeFields() {}
    @Override
    public void add(int field, int amount) {}
    @Override
    public void roll(int field, boolean up) {}
    @Override
    public int getMinimum(int field) { return 0; }
    @Override
    public int getMaximum(int field) { return 0; }
    @Override
    public int getGreatestMinimum(int field) { return 0; }
    @Override
    public int getLeastMaximum(int field) { return 0; }
}
