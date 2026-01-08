package com.farashian.pcalendar;


import com.farashian.pcalendar.util.HijriConverter;
import com.farashian.pcalendar.util.PCalendarUtils;

import java.util.*;

import static com.farashian.pcalendar.util.HijriConverter.*;
import static com.farashian.pcalendar.PCConstants.*;
import static com.farashian.pcalendar.util.PCalendarUtils.*;

public class PersianCalendar extends Calendar {

    public static final int FIRST_DAY_OF_WEEK      = Calendar.SATURDAY;
    public static final int WEEKDAY_HOLIDAY_NUMBER = Calendar.FRIDAY;

    // Month constants (PUBLIC 1-BASED API)
    public static final int FARVARDIN   = 1;
    public static final int ORDIBEHESHT = 2;
    public static final int KHORDAD     = 3;
    public static final int TIR         = 4;
    public static final int MORDAD      = 5;
    public static final int SHAHRIVAR   = 6;
    public static final int MEHR        = 7;
    public static final int ABAN        = 8;
    public static final int AZAR        = 9;
    public static final int DEY         = 10;
    public static final int BAHMAN      = 11;
    public static final int ESFAND      = 12;

    // Internal 0-based month constants (for Calendar compatibility)
    private static final int FARVARDIN_0   = 0;
    private static final int ORDIBEHESHT_0 = 1;
    private static final int KHORDAD_0     = 2;
    private static final int TIR_0         = 3;
    private static final int MORDAD_0      = 4;
    private static final int SHAHRIVAR_0   = 5;
    private static final int MEHR_0        = 6;
    private static final int ABAN_0        = 7;
    private static final int AZAR_0        = 8;
    private static final int DEY_0         = 9;
    private static final int BAHMAN_0      = 10;
    private static final int ESFAND_0      = 11;

    // Era constant
    private static final int AD = 1;

    protected final GregorianCalendar gCal;
    protected final Locale            locale;
    int[] ymd; // [year, month0, day] - month is 0-based internally

    // Caching for performance
    private              long  lastComputedTime = -1;
    private              int[] lastComputedYmd  = {0, 0, 0};
    private static final int[] PERSIAN_OFFSETS  = {0, 1, 2, 3, 4, 5, 6, 0};


    public PersianCalendar() {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setTimeInMillis(System.currentTimeMillis());
    }

    public PersianCalendar(TimeZone zone) {
        this(zone, PERSIAN_LOCALE);
        setTimeInMillis(System.currentTimeMillis());
    }

    public PersianCalendar(TimeZone zone, Locale locale) {
        super(zone, locale);
        this.locale = locale;
        this.gCal   = new GregorianCalendar(zone, locale);
        this.ymd    = new int[]{1400, 0, 1}; // Default date (month is 0-based)
    }

    public PersianCalendar(long timeStamp) {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setTimeInMillis(timeStamp);
    }

    public PersianCalendar(PersianCalendar pc) {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setPersianDate(pc.getYear(), pc.getMonth(), pc.getDayOfMonth());
    }

    // ✅ PUBLIC CONSTRUCTOR: 1-based month (1=Farvardin, 12=Esfand)
    public PersianCalendar(int year, int month, int dayOfMonth) {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setPersianDate(year, month, dayOfMonth);
    }

    public PersianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        this(year, month, dayOfMonth, hourOfDay, minute, 0);
    }

    public PersianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        this();
        setPersianDate(year, month, dayOfMonth);
        set(HOUR_OF_DAY, hourOfDay);
        set(MINUTE, minute);
        set(SECOND, second);
    }

    /**
     * Constructor that accepts Java GregorianCalendar object
     */
    public PersianCalendar(GregorianCalendar gregorianCalendar) {
        this();

        if (gregorianCalendar == null) {
            throw new IllegalArgumentException("GregorianCalendar cannot be null");
        }

        int year   = gregorianCalendar.get(Calendar.YEAR);
        int month  = gregorianCalendar.get(Calendar.MONTH) + 1; // 0-based to 1-based
        int day    = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
        int hour   = gregorianCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = gregorianCalendar.get(Calendar.MINUTE);
        int second = gregorianCalendar.get(Calendar.SECOND);

        validateGregorianDate(year, month, day);
        setGregorianDate(year, month, day);
        set(HOUR_OF_DAY, hour);
        set(MINUTE, minute);
        set(SECOND, second);
        setTimeZone(gregorianCalendar.getTimeZone());
    }

    /**
     * Constructor that accepts Java Date object
     */
    public PersianCalendar(Date date) {
        this();

        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        GregorianCalendar gCal = new GregorianCalendar();
        gCal.setTime(date);
        gCal.setTimeZone(getTimeZone());

        int year   = gCal.get(Calendar.YEAR);
        int month  = gCal.get(Calendar.MONTH) + 1; // 0-based to 1-based
        int day    = gCal.get(Calendar.DAY_OF_MONTH);
        int hour   = gCal.get(Calendar.HOUR_OF_DAY);
        int minute = gCal.get(Calendar.MINUTE);
        int second = gCal.get(Calendar.SECOND);

        validateGregorianDate(year, month, day);
        setGregorianDate(year, month, day);
        set(HOUR_OF_DAY, hour);
        set(MINUTE, minute);
        set(SECOND, second);
    }

    public static PersianCalendar gregorianToPersian(int gYear, int gMonth, int gDay) {
        PersianCalendar result = new PersianCalendar();
        result.setGregorianDate(gYear, gMonth, gDay);
        return result;
    }

    /**
     * Create PersianCalendar from Gregorian date string
     */
    public static PersianCalendar fromGregorianStringYmd(String dateString) {
        return fromGregorianStringYmd(dateString, "-");
    }

    public static PersianCalendar fromGregorianStringYmd(String dateString, String delimiter) {
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }

        dateString = convertToEnglishNumbers(dateString);

        String[] parts = dateString.split(delimiter);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid date format. Expected YYYY" + delimiter + "MM" + delimiter + "dd, got: " + dateString);
        }

        try {
            int year  = Integer.parseInt(parts[0].trim());
            int month = Integer.parseInt(parts[1].trim()); // Already 1-based from string
            int day   = Integer.parseInt(parts[2].trim());

            return new PersianCalendar(year, month, day);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in date string: " + dateString, e);
        }
    }

    public static PersianCalendar fromGregorianTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        return new PersianCalendar(date);
    }

    public int getYear() {
        ensureComputed();
        return ymd[0];
    }

    /** ✅ PUBLIC API: 1-based month, returns 1-12 **/
    public int getMonth() {
        ensureComputed();
        return ymd[1] + 1; // Convert 0-based to 1-based
    }

    /** ✅ Get internal 0-based month (for Calendar compatibility) */
    private int getMonth0() {
        ensureComputed();
        return ymd[1];
    }

    public int getDayOfMonth() {
        ensureComputed();
        return ymd[2];
    }

    public int getDaysInMonth() {
        return getDaysInMonth(getYear(), getMonth());
    }

    /**
     * ✅ PUBLIC API: 1-based month (1=Farvardin, 12=Esfand)
     */
    public int getDaysInMonth(int year, int month) {
        // Convert 1-based month to 0-based for internal calculation
        int month0 = month - 1;

        if (month0 < 0 || month0 > 11) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        if (month0 < 6) {
            return 31;
        } else if (month0 < 11) {
            return 30;
        } else {
            return isLeapYear(year) ? 30 : 29;
        }
    }

    /**
     * ✅ PUBLIC API: 1-based month (1=Farvardin, 12=Esfand)
     */
    public static int getDaysInMonthStatic(int year, int month) {
        // Convert 1-based month to 0-based for internal calculation
        int month0 = month - 1;

        if (month0 < 0 || month0 > 11) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        if (month0 < 6) {
            return 31;
        } else if (month0 < 11) {
            return 30;
        } else {
            return isLeapYear(year) ? 30 : 29;
        }
    }

    public static int getMaximumMonthWeeks() {
        return 6;
    }

    public int getGrgMonthLength() {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    // Date formatter methods
    public String getMonthName() {
        return getMonthName(getMonth(), locale); // getMonth() returns 1-based
    }

    //=== GREGORIAN DATE METHODS WITHOUT CACHE ===

    /**
     * Get Gregorian year from the underlying GregorianCalendar
     */
    public int getGrgYear() {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.get(Calendar.YEAR);
    }

    /**
     * ✅ Get Gregorian month (1-based: 1=January, 12=December)
     */
    public int getGrgMonth() {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.get(Calendar.MONTH) + 1;
    }

    /**
     * Get Gregorian day of month (1-31)
     */
    public int getGrgDay() {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get Gregorian week of year (ISO 8601 week numbering)
     */
    public int getGrgWeekOfYear() {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Get Gregorian week of month
     */
    public int getGrgWeekOfMonth() {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.get(Calendar.WEEK_OF_MONTH);
    }

    /**
     * Get Gregorian day of week (Calendar.SUNDAY=1, Calendar.SATURDAY=7)
     */
    public int getGrgDayOfWeek() {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Get Gregorian day of week name
     */
    public String getGrgDayOfWeekName(Locale locale) {
        gCal.setTimeInMillis(getTimeInMillis());
        int dayOfWeek = gCal.get(Calendar.DAY_OF_WEEK);

        if (locale.getLanguage().equals("fa")) {
            String[] persianWeekdays = {"یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه",
                    "پنجشنبه", "جمعه", "شنبه"};
            return persianWeekdays[dayOfWeek - 1];
        } else {
            return gCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale);
        }
    }

    /**
     * Get Gregorian month length for specific year and month
     * ✅ @param month 1-based Gregorian month (1=January, 12=December)
     */
    public static int getGrgMonthLength(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        int month0 = month - 1; // Convert to 0-based

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
                return ((year % 4 == 0) && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
            default:
                return 31;
        }
    }

    /**
     * Get Gregorian month name (fast version without Calendar)
     * ✅ @param month 1-based Gregorian month (1=January, 12=December)
     */
    public static String getGrgMonthNameFast(int month, Locale locale) {
        int month0 = month - 1; // Convert to 0-based

        if (month0 < 0 || month0 > 11) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        if (locale.getLanguage().equals("fa")) {
            String[] persianMonths = {
                    "ژانویه", "فوریه", "مارس", "آوریل", "می", "ژوئن",
                    "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
            };
            return persianMonths[month0];
        } else {
            String[] englishMonths = {
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
            };
            return englishMonths[month0];
        }
    }

    public static String getGrgMonthName(int month) {
        Locale locale1 = getLocaleFromTimezone();

        return getGrgMonthNameFast(month, locale1);
    }

    /**
     * Get Gregorian month short name (3-letter abbreviation)
     * ✅ @param month 1-based Gregorian month (1=January, 12=December)
     */
    public static String getGrgMonthShortName(int month, Locale locale) {
        int month0 = month - 1; // Convert to 0-based

        if (month0 < 0 || month0 > 11) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        if (locale.getLanguage().equals("fa")) {
            String[] persianShortMonths = {
                    "ژان", "فور", "مار", "آور", "می", "ژوئ",
                    "ژوئ", "اوت", "سپت", "اکت", "نوا", "دسا"
            };
            return persianShortMonths[month0];
        } else {
            String[] englishShortMonths = {
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            };
            return englishShortMonths[month0];
        }
    }

    /**
     * Static method: Get Gregorian month name without creating calendar instance
     * ✅ @param month 1-based Gregorian month (1=January, 12=December)
     */
    public static String getGrgMonthNameStatic(int month, Locale locale) {
        int month0 = month - 1; // Convert to 0-based

        if (month0 < 0 || month0 > 11) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        Calendar temp = new GregorianCalendar(locale);
        temp.set(Calendar.MONTH, month0);
        temp.set(Calendar.DAY_OF_MONTH, 1);
        return temp.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
    }

    /**
     * Get full Gregorian date as string
     */
    public String getGrgLongDate(Locale locale) {
        gCal.setTimeInMillis(getTimeInMillis());
        return String.format(locale, "%s, %d %s %d",
                             getGrgDayOfWeekName(locale),
                             gCal.get(Calendar.DAY_OF_MONTH),
                             getGrgMonthName(locale),
                             gCal.get(Calendar.YEAR));
    }

    public String getGrgLongDate() {
        complete();
        return String.format(locale, "%s, %d %s %d",
                             getGrgDayOfWeekName(locale),
                             getGrgDay(),
                             getGrgMonthName(locale),
                             getGrgYear());
    }

    public String getHijriLongDate() {
        YMD hijri = getHijriDate();
        return String.format(locale, "%4d, %s %s",
                             hijri.getDay(),
                             getHijriMonthName(hijri.getMonth() - 1),
                             hijri.getYear());
    }

    /**
     * Get Gregorian month name using instance's locale
     */
    public String getGrgMonthName() {
        return getGrgMonthName(this.locale);
    }

    /**
     * Get Gregorian month name with specified locale
     */
    public String getGrgMonthName(Locale locale) {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
    }

    /**
     * Get Gregorian month short name (3-letter abbreviation)
     */
    public String getGrgMonthShortName(Locale locale) {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale);
    }

    /**
     * Get Gregorian month name with number (e.g., "January (01)")
     */
    public String getGrgMonthNameWithNumber(Locale locale) {
        gCal.setTimeInMillis(getTimeInMillis());
        String monthName   = getGrgMonthName(locale);
        int    monthNumber = getGrgMonth(); // Already 1-based
        return String.format(locale, "%s (%02d)", monthName, monthNumber);
    }

    /**
     * Convert Gregorian date to Persian date
     * ✅ @param month 1-based Gregorian month (1=January)
     */
    public static PersianCalendar fromGregorian(int year, int month, int day) {
        PersianCalendar result = new PersianCalendar();
        result.setGregorianDate(year, month, day);
        return result;
    }

    /**
     * Get current Gregorian date
     */
    public static GregorianCalendar currentGregorian() {
        PersianCalendar result = new PersianCalendar();
        return result.gCal;
    }

    /**
     * Get Gregorian date in ISO format (YYYY-MM-dd)
     */
    public String getGrgIsoDate() {
        gCal.setTimeInMillis(getTimeInMillis());
        return String.format(Locale.US, "%04d-%02d-%02d",
                             gCal.get(Calendar.YEAR),
                             gCal.get(Calendar.MONTH) + 1,
                             gCal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * ✅ Get Gregorian date with 1-based month format
     */
    public String getGrgShortDate(String delimiter) {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.get(Calendar.YEAR) + delimiter +
               String.format("%02d", gCal.get(Calendar.MONTH) + 1) + delimiter +
               String.format("%02d", gCal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Check if current Gregorian date is today
     */
    public boolean isGrgToday() {
        GregorianCalendar today = new GregorianCalendar();
        today.setTimeZone(getTimeZone());
        today.setTime(new Date());

        gCal.setTimeInMillis(getTimeInMillis());
        return today.get(Calendar.YEAR) == gCal.get(Calendar.YEAR) &&
               today.get(Calendar.MONTH) == gCal.get(Calendar.MONTH) &&
               today.get(Calendar.DAY_OF_MONTH) == gCal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get Gregorian date as Date object
     */
    public Date getGrgDate() {
        return new Date(getTimeInMillis());
    }

    /**
     * Get Gregorian date as milliseconds
     */
    public long getGrgTimeInMillis() {
        return getTimeInMillis();
    }

    /**
     * ✅ Set Gregorian date
     * @param month 1-based Gregorian month (1=January, 12=December)
     */
    public void setGregorianDate(int year, int month, int day) {
        int month0 = month - 1; // Convert to 0-based

        if (month0 < 0 || month0 > 11) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }
        if (day < 1 || day > getGrgMonthLength(year, month)) {
            throw new IllegalArgumentException("Invalid day for month: " + day);
        }

        gCal.set(year, month0, day);
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * ✅ NEW: Parse Gregorian date string
     */
    public static PersianCalendar parseGregorianString(String dateString, String delimiter) {
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }

        dateString = convertToEnglishNumbers(dateString);

        String[] parts = dateString.split(delimiter);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid date format");
        }

        int year = Integer.parseInt(parts[0].trim());
        int month = Integer.parseInt(parts[1].trim()); // 1-based from string
        int day = Integer.parseInt(parts[2].trim());

        PersianCalendar result = new PersianCalendar();
        result.setGregorianDate(year, month, day);
        return result;
    }

    public static PersianCalendar parseGregorianString(String dateString) {
        return parseGregorianString(dateString, "-");
    }

    /**
     * Add days to Gregorian date
     */
    public void addGrgDays(int days) {
        complete();
        gCal.setTimeInMillis(getTimeInMillis());
        gCal.add(Calendar.DAY_OF_MONTH, days);
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * Add months to Gregorian date
     */
    public void addGrgMonths(int months) {
        complete();
        gCal.setTimeInMillis(getTimeInMillis());
        gCal.add(Calendar.MONTH, months);
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * Add years to Gregorian date
     */
    public void addGrgYears(int years) {
        complete();
        gCal.setTimeInMillis(getTimeInMillis());
        gCal.add(Calendar.YEAR, years);
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * Get difference in days between this Gregorian date and another
     */
    public long grgDaysBetween(PersianCalendar other) {
        long thisMillis  = this.getGrgTimeInMillis();
        long otherMillis = other.getGrgTimeInMillis();
        return Math.abs(thisMillis - otherMillis) / (1000 * 60 * 60 * 24);
    }

    /**
     * Check if Gregorian year is leap year
     */
    public static boolean isGrgLeapYear(int year) {
        return (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
    }

    /**
     * Check if current Gregorian year is leap year
     */
    public boolean isGrgLeapYear() {
        gCal.setTimeInMillis(getTimeInMillis());
        return isGrgLeapYear(gCal.get(Calendar.YEAR));
    }

    /**
     * Get day of year for Gregorian date (1-365/366)
     */
    public int getGrgDayOfYear() {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Get the first day of week for Gregorian calendar
     */
    public int getGrgFirstDayOfWeek() {
        return gCal.getFirstDayOfWeek();
    }

    /**
     * Check if Gregorian date is weekend (Saturday or Sunday)
     */
    public boolean isGrgWeekend() {
        gCal.setTimeInMillis(getTimeInMillis());
        int dayOfWeek = gCal.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    /**
     * Check if Gregorian date is weekday (Monday to Friday)
     */
    public boolean isGrgWeekday() {
        return !isGrgWeekend();
    }

    //=== CORE CALENDAR METHODS ===

    @Override
    protected void computeTime() {
        if (!areFieldsSet || lastComputedTime != time) {
            computeGregorianFromPersian();
            time             = gCal.getTimeInMillis();
            lastComputedTime = time;
            lastComputedYmd  = ymd.clone();
        }
    }

    @Override
    protected void computeFields() {
        if (time != lastComputedTime) {
            computePersianFromGregorian();
            lastComputedTime = time;
            lastComputedYmd  = ymd.clone();

            computeAllCalendarFields();
        }
    }

    /**
     * Compute all calendar fields expected by parent Calendar class
     */
    private void computeAllCalendarFields() {
        gCal.setTimeInMillis(time);
        computePersianFromGregorian();

        fields[YEAR]         = ymd[0];
        fields[MONTH]        = ymd[1]; // Store as 0-based for Calendar compatibility
        fields[DAY_OF_MONTH] = ymd[2];

        fields[HOUR_OF_DAY] = gCal.get(HOUR_OF_DAY);
        fields[MINUTE]      = gCal.get(MINUTE);
        fields[SECOND]      = gCal.get(SECOND);
        fields[MILLISECOND] = gCal.get(MILLISECOND);

        int gregorianDayOfWeek = gCal.get(Calendar.DAY_OF_WEEK);
        int persianOffset      = calculatePersianOffset(gregorianDayOfWeek);

        int persianDayOfWeek;
        if (persianOffset == 0) {
            persianDayOfWeek = Calendar.SATURDAY;
        } else {
            persianDayOfWeek = persianOffset;
        }

        fields[DAY_OF_WEEK] = persianDayOfWeek;
        fields[DAY_OF_YEAR] = calculateDayOfYear();

        calculateWeekFields();

        fields[AM_PM]       = gCal.get(HOUR_OF_DAY) < 12 ? Calendar.AM : Calendar.PM;
        fields[HOUR]        = gCal.get(HOUR_OF_DAY) % 12;
        fields[DST_OFFSET]  = gCal.get(DST_OFFSET);
        fields[ZONE_OFFSET] = gCal.get(ZONE_OFFSET);
        fields[ERA]         = AD;

        for (int i = 0; i < FIELD_COUNT; i++) {
            isSet[i] = true;
        }
        areFieldsSet = true;
    }

    /**
     * Calculate week fields based on Persian calendar rules
     */
    private void calculateWeekFields() {
        int dayOfYear = fields[DAY_OF_YEAR];
        int dayOfWeek = fields[DAY_OF_WEEK];

        int weekOfYear = (dayOfYear - 1 + ((dayOfWeek - FIRST_DAY_OF_WEEK + 7) % 7)) / 7 + 1;
        fields[WEEK_OF_YEAR] = weekOfYear;

        int dayOfMonth      = fields[DAY_OF_MONTH];
        int firstDayOfMonth = calculateFirstDayOfMonth();
        int weekOfMonth     = (dayOfMonth - 1 + ((dayOfWeek - firstDayOfMonth + 7) % 7)) / 7 + 1;
        fields[WEEK_OF_MONTH] = weekOfMonth;

        fields[DAY_OF_WEEK_IN_MONTH] = (dayOfMonth - 1) / 7 + 1;
    }

    /**
     * Calculate the day of week for the first day of the current month
     */
    private int calculateFirstDayOfMonth() {
        int savedDay = ymd[2];

        ymd[2] = 1;
        computeGregorianFromPersian();

        int gregorianDayOfWeek = gCal.get(DAY_OF_WEEK);
        int persianOffset      = calculatePersianOffset(gregorianDayOfWeek);
        int persianDayOfWeek;
        if (persianOffset == 0) {
            persianDayOfWeek = Calendar.SATURDAY;
        } else {
            persianDayOfWeek = persianOffset;
        }

        ymd[2] = savedDay;
        computeGregorianFromPersian();

        return persianDayOfWeek;
    }

    /**
     * Calculate day of year for Persian calendar
     */
    private int calculateDayOfYear() {
        int dayOfYear = 0;
        for (int month = 0; month < ymd[1]; month++) {
            dayOfYear += getDaysInMonth(ymd[0], month + 1); // Convert 0-based to 1-based
        }
        dayOfYear += ymd[2];
        return dayOfYear;
    }

    /**
     * Convert Persian date to Gregorian
     */
    private void computeGregorianFromPersian() {
        // Note: jalali_to_gregorian expects 1-based month, so we convert from internal 0-based
        int[] g = jalali_to_gregorian(ymd[0], ymd[1] + 1, ymd[2]);

        int hour   = internalGet(HOUR_OF_DAY, 0);
        int minute = internalGet(MINUTE, 0);
        int second = internalGet(SECOND, 0);
        int millis = internalGet(MILLISECOND, 0);

        gCal.set(g[0], g[1] - 1, g[2], hour, minute, second);
        gCal.set(MILLISECOND, millis);
    }

    /**
     * Convert Gregorian date to Persian
     */
    private void computePersianFromGregorian() {
        ymd = gregorian_to_jalali(
                gCal.get(Calendar.YEAR), gCal.get(Calendar.MONTH) + 1, gCal.get(Calendar.DAY_OF_MONTH));

        // Convert 1-based month from algorithm to 0-based internal storage
        ymd[1] = ymd[1] - 1;
    }

    public boolean isSameDay(PersianCalendar other) {
        if (other == null) return false;
        ensureComputed();
        return this.ymd[0] == other.getYear() &&
               this.ymd[1] == other.getMonth0() &&
               this.ymd[2] == other.getDayOfMonth();
    }

    public PersianCalendar copy() {
        PersianCalendar copy = new PersianCalendar();
        copy.ymd[0] = this.ymd[0];
        copy.ymd[1] = this.ymd[1];
        copy.ymd[2] = this.ymd[2];
        copy.setTimeInMillis(this.getTimeInMillis());
        return copy;
    }

    private void ensureComputed() {
        if (lastComputedTime != time) {
            computeFields();
        }
    }

    @Override
    public void add(int field, int amount) {
        if (amount == 0) return;
        if (field < 0 || field >= ZONE_OFFSET) return;

        complete();

        if (field == YEAR || field == MONTH || field == DAY_OF_MONTH) {
            handlePersianDateArithmetic(field, amount);
        } else {
            computeTime();
            gCal.add(field, amount);
            computePersianFromGregorian();
        }

        areFieldsSet     = false;
        lastComputedTime = -1;
        complete();
    }

    /**
     * Handle Persian date arithmetic directly
     */
    private void handlePersianDateArithmetic(int field, int amount) {
        switch (field) {
            case YEAR:
                ymd[0] += amount;
                if (ymd[0] < 1) ymd[0] = 1;
                adjustDayForNewMonth();
                break;
            case MONTH:
                // Handle month arithmetic on 0-based month
                int totalMonths = ymd[1] + amount;
                if (totalMonths >= 0) {
                    ymd[0] += totalMonths / 12;
                    ymd[1] = totalMonths % 12;
                } else {
                    int yearsToSubtract = (-totalMonths + 11) / 12;
                    ymd[0] -= yearsToSubtract;
                    if (ymd[0] < 1) ymd[0] = 1;
                    ymd[1] = (totalMonths % 12 + 12) % 12;
                }
                adjustDayForNewMonth();
                break;
            case DAY_OF_MONTH:
                ymd[2] += amount;
                normalizeDate();
                break;
        }

        computeGregorianFromPersian();
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * Normalize date by handling overflow/underflow of days
     */
    private void normalizeDate() {
        while (ymd[2] > getDaysInMonth(ymd[0], ymd[1] + 1)) { // Convert 0-based to 1-based
            ymd[2] -= getDaysInMonth(ymd[0], ymd[1] + 1);
            ymd[1]++;
            if (ymd[1] > 11) {
                ymd[1] = 0;
                ymd[0]++;
            }
        }

        while (ymd[2] < 1) {
            ymd[1]--;
            if (ymd[1] < 0) {
                ymd[1] = 11;
                ymd[0]--;
                if (ymd[0] < 1) ymd[0] = 1;
            }
            ymd[2] += getDaysInMonth(ymd[0], ymd[1] + 1); // Convert 0-based to 1-based
        }
    }

    protected void pinDayOfMonth() {
        int monthLen = getDaysInMonth(internalGet(YEAR), internalGet(MONTH) + 1); // Convert 0-based to 1-based
        int dom      = internalGet(DAY_OF_MONTH);
        if (dom > monthLen) {
            set(DAY_OF_MONTH, monthLen);
        }
    }

    @Override
    public void roll(int field, boolean up) {
        complete();

        if (field == YEAR) {
            ymd[0] += up ? 1 : -1;
            if (ymd[0] <= 0) ymd[0] = 1;
            adjustDayForNewMonth();
        } else if (field == MONTH) {
            if (up) {
                if (ymd[1] == 11) {
                    ymd[1] = 0;
                    ymd[0]++;
                } else {
                    ymd[1]++;
                }
            } else {
                if (ymd[1] == 0) {
                    ymd[1] = 11;
                    ymd[0]--;
                    if (ymd[0] < 1) ymd[0] = 1;
                } else {
                    ymd[1]--;
                }
            }
            adjustDayForNewMonth();
        } else if (field == DAY_OF_MONTH) {
            int maxDays = getDaysInMonth(ymd[0], ymd[1] + 1); // Convert 0-based to 1-based
            if (up) {
                if (ymd[2] == maxDays) {
                    ymd[2] = 1;
                } else {
                    ymd[2]++;
                }
            } else {
                if (ymd[2] == 1) {
                    ymd[2] = maxDays;
                } else {
                    ymd[2]--;
                }
            }
        } else {
            computeTime();
            gCal.roll(field, up);
            computePersianFromGregorian();
        }

        computeGregorianFromPersian();
        setTimeInMillis(gCal.getTimeInMillis());
        areFieldsSet     = false;
        lastComputedTime = -1;
    }

    @Override
    public void set(int field, int value) {
        switch (field) {
            case YEAR:
                if (value < 1 || value > 9999) {
                    throw new IllegalArgumentException("Year must be between 1 and 9999, got: " + value);
                }
                ymd[0] = value;
                adjustDayForNewMonth();
                break;
            case MONTH:
                // Calendar uses 0-based months, convert from 1-based if needed
                int month0 = value;
                if (month0 < 0 || month0 > 11) {
                    throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month0);
                }
                ymd[1] = month0;
                adjustDayForNewMonth();
                break;
            case DAY_OF_MONTH:
                int maxDays = getDaysInMonth(ymd[0], ymd[1] + 1); // Convert 0-based to 1-based
                if (value < 1 || value > maxDays) {
                    throw new IllegalArgumentException("Day must be between 1 and " + maxDays + ", got: " + value);
                }
                ymd[2] = value;
                break;
            default:
                super.set(field, value);
                return;
        }

        computeGregorianFromPersian();
        setTimeInMillis(gCal.getTimeInMillis());
        areFieldsSet     = false;
        lastComputedTime = -1;
    }

    /**
     * Adjust day when month or year changes to ensure valid date
     */
    private void adjustDayForNewMonth() {
        int maxDays = getDaysInMonth(ymd[0], ymd[1] + 1); // Convert 0-based to 1-based
        if (ymd[2] > maxDays) {
            ymd[2] = maxDays;
        }
    }

    //=== OVERRIDES ===

    @Override
    public void setTimeInMillis(long millis) {
        super.setTimeInMillis(millis);
        lastComputedTime = -1;
    }

    @Override
    public void setTimeZone(TimeZone zone) {
        super.setTimeZone(zone);
        gCal.setTimeZone(zone);
        lastComputedTime = -1;
    }

    @Override
    public String toString() {
        return getLongDate();
    }

    private void validateGregorianDate(int year, int month, int day) {
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException("Year must be between 1 and 9999, got: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }
        int maxDays = getGrgMonthLength(year, month);
        if (day < 1 || day > maxDays) {
            throw new IllegalArgumentException("Day must be between 1 and " + maxDays + " for month " + month + ", got: " + day);
        }
    }

    @Override
    public int getMinimum(int field) {
        switch (field) {
            case YEAR:
                return 1;
            case MONTH:
                return FARVARDIN_0;
            case DAY_OF_MONTH:
                return 1;
            case DAY_OF_WEEK:
                return SUNDAY;
            case DAY_OF_WEEK_IN_MONTH:
                return 1;
            case DAY_OF_YEAR:
                return 1;
            case WEEK_OF_YEAR:
                return 1;
            case WEEK_OF_MONTH:
                return 1;
            case HOUR_OF_DAY:
                return 0;
            case HOUR:
                return 1;
            case MINUTE:
                return 0;
            case SECOND:
                return 0;
            case MILLISECOND:
                return 0;
            case AM_PM:
                return AM;
            case ERA:
                return AD;
            case ZONE_OFFSET:
                return -13 * 60 * 60 * 1000;
            case DST_OFFSET:
                return 0;
            default:
                return 0;
        }
    }

    @Override
    public int getGreatestMinimum(int field) {
        return getMinimum(field);
    }

    @Override
    public int getLeastMaximum(int field) {
        switch (field) {
            case DAY_OF_MONTH:
                return 29;
            case WEEK_OF_MONTH:
                return 4;
            case WEEK_OF_YEAR:
                return 52;
            default:
                return getMaximum(field);
        }
    }

    @Override
    public int getMaximum(int field) {
        switch (field) {
            case YEAR:
                return 9999;
            case MONTH:
                return ESFAND_0;
            case DAY_OF_MONTH:
                return 31;
            case DAY_OF_WEEK:
                return SATURDAY;
            case DAY_OF_WEEK_IN_MONTH:
                return 6;
            case DAY_OF_YEAR:
                return 366;
            case WEEK_OF_YEAR:
                return 53;
            case WEEK_OF_MONTH:
                return 6;
            case HOUR_OF_DAY:
                return 23;
            case HOUR:
                return 11;
            case MINUTE:
                return 59;
            case SECOND:
                return 59;
            case MILLISECOND:
                return 999;
            case AM_PM:
                return PM;
            case ERA:
                return AD;
            case ZONE_OFFSET:
                return 14 * 60 * 60 * 1000;
            case DST_OFFSET:
                return 2 * 60 * 60 * 1000;
            default:
                return 0;
        }
    }

    @Override
    public int getActualMaximum(int field) {
        switch (field) {
            case DAY_OF_MONTH:
                return getDaysInMonth(getYear(), getMonth());
            case DAY_OF_YEAR:
                return isLeapYear(getYear()) ? 366 : 365;
            case WEEK_OF_YEAR:
                // Calculate actual weeks in year
                int dayOfYear = get(DAY_OF_YEAR);
                int dayOfWeek = get(DAY_OF_WEEK);
                // Weeks start on Saturday in Persian calendar
                int weeks = (dayOfYear - 1 + ((dayOfWeek - SATURDAY + 7) % 7)) / 7 + 1;
                return Math.min(weeks, 53);
            case WEEK_OF_MONTH:
                int daysInMonth = getDaysInMonth(getYear(), getMonth());
                int firstDayOfMonth = getFirstDayOfMonth();
                int firstDayWeek = (firstDayOfMonth - SATURDAY + 7) % 7;
                return (daysInMonth - 1 + firstDayWeek) / 7 + 1;
            default:
                return getMaximum(field);
        }
    }

    /**
     * Get the internal ymd array (for backward compatibility)
     */
    public YMD getYmd() {
        ensureComputed();
        return new YMD(ymd[0], ymd[1] + 1, ymd[2]); // Convert 0-based to 1-based
    }

    /**
     * Convenience method to get day of week
     */
    public int getDayOfWeek() {
        return get(DAY_OF_WEEK);
    }

    /**
     * Get era (always returns AD for Persian calendar)
     */
    public int getEra() {
        return AD;
    }

    //=== OBJECT METHODS ===

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        PersianCalendar that = (PersianCalendar) obj;
        return this.time == that.time &&
               Arrays.equals(this.ymd, that.ymd) &&
               this.getTimeZone().equals(that.getTimeZone());
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, Arrays.hashCode(ymd), getTimeZone());
    }

    //=== INTERNAL HELPERS ===

    private int internalGet(int field, int defaultValue) {
        return isSet[field] ? fields[field] : defaultValue;
    }

    //=== DEBUG METHODS ===

    public String debugInfo() {
        ensureComputed();
        return String.format(Locale.US,
                             "Persian: %d/%d/%d, Gregorian: %d/%d/%d, Month Name: %s, Day of Week: %s",
                             ymd[0], ymd[1] + 1, ymd[2],
                             gCal.get(Calendar.YEAR), gCal.get(Calendar.MONTH) + 1, gCal.get(Calendar.DAY_OF_MONTH),
                             getMonthName(), getWeekdayName());
    }

    public static boolean isLeapYear(int year) {
        int remainder = year % 33;
        return remainder == 1 || remainder == 5 || remainder == 9 ||
               remainder == 13 || remainder == 17 || remainder == 22 ||
               remainder == 26 || remainder == 30;
    }

    private int getFirstDayOfMonth() {
        int currentDay = get(DAY_OF_MONTH);
        set(DAY_OF_MONTH, 1);
        int firstDayOfWeek = get(DAY_OF_WEEK);
        set(DAY_OF_MONTH, currentDay);
        return firstDayOfWeek;
    }

    protected static boolean isLeapYearOld(int year) {
        if (year >= 1200 && year <= 1600) {
            return leapYears.contains(year);
        }
        int remainder = year % 33;
        switch (remainder) {
            case 1:
            case 5:
            case 9:
            case 13:
            case 17:
            case 22:
            case 26:
            case 30:
                return true;
            default:
                return false;
        }
    }

    /**
     * Convert Gregorian to Jalali date
     * Algorithm by JDF.SCR.IR - GNU/LGPL License
     */
    static int[] gregorian_to_jalali(int gy, int gm, int gd) {
        int[] out = {(gm > 2) ? (gy + 1) : gy, 0, 0};

        int[] g_d_m = {0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334};
        out[2] = 355666 + (365 * gy) + ((out[0] + 3) / 4) - ((out[0] + 99) / 100)
                 + ((out[0] + 399) / 400) + gd + g_d_m[gm - 1];

        out[0] = -1595 + (33 * (out[2] / 12053));
        out[2] %= 12053;
        out[0] += 4 * (out[2] / 1461);
        out[2] %= 1461;

        if (out[2] > 365) {
            out[0] += (out[2] - 1) / 365;
            out[2] = (out[2] - 1) % 365;
        }

        if (out[2] < 186) {
            out[1] = 1 + out[2] / 31;
            out[2] = 1 + (out[2] % 31);
        } else {
            out[1] = 7 + (out[2] - 186) / 30;
            out[2] = 1 + ((out[2] - 186) % 30);
        }
        return out;
    }

    /**
     * Convert Jalali to Gregorian date
     * Algorithm by JDF.SCR.IR - GNU/LGPL License
     */
    static int[] jalali_to_gregorian(int jy, int jm, int jd) {
        jy += 1595;
        int[] out = {0, 0, -355668 + (365 * jy) + ((jy / 33) * 8) +
                           (((jy % 33) + 3) / 4) + jd + ((jm < 7) ? (jm - 1) * 31 : ((jm - 7) * 30) + 186)};

        out[0] = 400 * (out[2] / 146097);
        out[2] %= 146097;

        if (out[2] > 36524) {
            out[0] += 100 * ((--out[2]) / 36524);
            out[2] %= 36524;
            if (out[2] >= 365) out[2]++;
        }

        out[0] += 4 * (out[2] / 1461);
        out[2] %= 1461;

        if (out[2] > 365) {
            out[0] += (out[2] - 1) / 365;
            out[2] = (out[2] - 1) % 365;
        }

        int[] sal_a = {0, 31, ((out[0] % 4 == 0 && out[0] % 100 != 0) || (out[0] % 400 == 0)) ? 29 : 28,
                31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        for (out[2]++; out[1] < 13 && out[2] > sal_a[out[1]]; out[1]++) {
            out[2] -= sal_a[out[1]];
        }
        return out;
    }

    public static String getWeekdayName(int dayOfWeek, Locale locale) {
        int index = (dayOfWeek - 1) % 7;
        if (index < 0) index += 7;

        boolean isPersian = locale.getLanguage().equals("fa");
        return isPersian
                ? PCConstants.WEEKDAY_NAMES[index]
                : PCConstants.WEEKDAY_NAMES_SHORT_IN_ENGLISH[index];
    }

    /**
     * Get Persian month name
     * ✅ @param month 1-based Persian month (1=Farvardin, 12=Esfand)
     */
    public static String getMonthName(int month, Locale locale) {
        int month0 = month - 1; // Convert to 0-based

        if (month0 < 0 || month0 > 11) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        if (locale.getLanguage().equals("fa")) {
            return PCConstants.PERSIAN_MONTH_NAMES[month0];
        }
        return PCConstants.PERSIAN_MONTH_NAMES_IN_ENGLISH[month0];
    }

    public static String getMonthName(int month) {
        Locale locale1 = getLocaleFromTimezone();
        return PCalendarUtils.getPersianMonthName(month, locale1);
    }

    public String getMonthNameShort() {
        return getMonthNameShort(getMonth(), locale);
    }

    /**
     * Get Persian month short name
     * ✅ @param month 1-based Persian month (1=Farvardin, 12=Esfand)
     */
    public static String getMonthNameShort(int month, Locale locale) {
        int month0 = month - 1; // Convert to 0-based

        if (month0 < 0 || month0 > 11) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        return locale.getLanguage().equals("fa")
                ? PCConstants.PERSIAN_MONTH_NAMES_SHORT[month0]
                : PCConstants.PERSIAN_MONTH_NAMES_ENGLISH_SHORT[month0];
    }

    /**
     * Get the day of week as Persian name
     */
    public String getPersianWeekdayName() {
        return getWeekdayName(get(DAY_OF_WEEK), PERSIAN_LOCALE);
    }

    /**
     * Get the month name in Persian
     */
    public String getPersianMonthName() {
        return getMonthName(getMonth(), PERSIAN_LOCALE);
    }

    /**
     * Get the month name in English
     */
    public String getEnglishMonthName() {
        return getMonthName(getMonth(), Locale.ENGLISH);
    }

    public static String formatToTwoDigits(int num, Locale locale) {
        return String.format(locale, "%02d", num);
    }

    public static String getLongDate(int year, int month, int day, int dayOfWeek, Locale locale) {
        return getWeekdayName(dayOfWeek, locale) + " " +
               formatToTwoDigits(day, locale) + " " +
               getMonthName(month, locale) + " " +
               formatToTwoDigits(year, locale);
    }

    public static String getLongDateTime(int year, int month, int day, int dayOfWeek,
            int hour, int minute, int second, Locale locale) {
        return getLongDate(year, month, day, dayOfWeek, locale) + ", " +
               formatToTwoDigits(hour, locale) + ":" +
               formatToTwoDigits(minute, locale) + ":" +
               formatToTwoDigits(second, locale);
    }

    public static String getShortDate(int year, int month, int day, String delimiter, Locale locale) {
        return formatToTwoDigits(year, locale) + delimiter +
               formatToTwoDigits(month, locale) + delimiter + // Already 1-based
               formatToTwoDigits(day, locale);
    }

    public static PersianCalendar parseOrNullToCompat(String dateString) {
        return parseOrNullToCompat(dateString, "/");
    }

    public static PersianCalendar parseOrNullToCompat(String dateString, String delimiter) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        if (delimiter == null || delimiter.isEmpty() || !dateString.contains(delimiter)) {
            if (dateString.contains("/")) delimiter = "/";
            else if (dateString.contains("-")) delimiter = "-";
            else if (dateString.contains(".")) delimiter = "\\.";
            else return null;
        }

        String[] tokens = dateString.split(delimiter);
        if (tokens.length != 3) return null;

        try {
            int year  = Integer.parseInt(tokens[0].trim());
            int month = Integer.parseInt(tokens[1].trim()); // Already 1-based from string
            int day   = Integer.parseInt(tokens[2].trim());

            if (year < 1 || month < 1 || month > 12 || day < 1 || day > 31) {
                return null;
            }

            int maxDays = (new PersianCalendar()).getDaysInMonth(year, month);
            if (day > maxDays) {
                return null;
            }

            return new PersianCalendar(year, month, day);
        } catch (Exception e) {
            return null;
        }
    }

    //=== HELPER METHODS FOR DATE MANIPULATION ===

    /**
     * Add days to the current date
     */
    public void addDays(int days) {
        add(DAY_OF_MONTH, days);
    }

    /**
     * Add weeks to the current date
     */
    public void addWeeks(int weeks) {
        addDays(weeks * 7);
    }

    /**
     * Add months to the current date
     */
    public void addMonths(int months) {
        add(MONTH, months);
    }

    /**
     * Add years to the current date
     */
    public void addYears(int years) {
        add(YEAR, years);
    }

    /**
     * Get a copy of this calendar with days added
     */
    public PersianCalendar plusDays(int days) {
        PersianCalendar result = new PersianCalendar(this);
        result.addDays(days);
        return result;
    }

    /**
     * Get a copy of this calendar with days subtracted
     */
    public PersianCalendar minusDays(int days) {
        return plusDays(-days);
    }

    /**
     * Get a copy of this calendar with weeks added
     */
    public PersianCalendar plusWeeks(int weeks) {
        PersianCalendar result = new PersianCalendar(this);
        result.addWeeks(weeks);
        return result;
    }

    /**
     * Get a copy of this calendar with weeks subtracted
     */
    public PersianCalendar minusWeeks(int weeks) {
        return plusWeeks(-weeks);
    }

    /**
     * Get a copy of this calendar with months added
     */
    public PersianCalendar plusMonths(int months) {
        PersianCalendar result = new PersianCalendar(this);
        result.addMonths(months);
        return result;
    }

    /**
     * Get a copy of this calendar with months subtracted
     */
    public PersianCalendar minusMonths(int months) {
        return plusMonths(-months);
    }

    /**
     * Get a copy of this calendar with years added
     */
    public PersianCalendar plusYears(int years) {
        PersianCalendar result = new PersianCalendar(this);
        result.addYears(years);
        return result;
    }

    /**
     * Get a copy of this calendar with years subtracted
     */
    public PersianCalendar minusYears(int years) {
        return plusYears(-years);
    }

    /**
     * Check if this date is before another Persian date
     */
    public boolean isBefore(PersianCalendar other) {
        if (other == null) return false;
        return this.getTimeInMillis() < other.getTimeInMillis();
    }

    /**
     * Check if this date is after another Persian date
     */
    public boolean isAfter(PersianCalendar other) {
        if (other == null) return false;
        return this.getTimeInMillis() > other.getTimeInMillis();
    }

    /**
     * Check if this date is equal to another Persian date
     */
    public boolean isEqual(PersianCalendar other) {
        ensureComputed();
        if (other == null) return false;
        return this.ymd[0] == other.getYear() &&
               this.ymd[1] == other.getMonth0() &&
               this.ymd[2] == other.getDayOfMonth();
    }

    /**
     * Get the number of days between this date and another date
     */
    public long daysBetween(PersianCalendar other) {
        if (other == null) return 0;
        long diffMillis = this.getTimeInMillis() - other.getTimeInMillis();
        return diffMillis / (1000 * 60 * 60 * 24);
    }

    public int getHourOfDay() {
        return get(HOUR_OF_DAY);
    }

    public int getMinute() {
        return get(MINUTE);
    }

    public int getSecond() {
        return get(SECOND);
    }

    /**
     * Check if the date is valid
     */
    public boolean isValid() {
        try {
            validatePersianDate(this.ymd[0], this.ymd[1] + 1, this.ymd[2]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void validatePersianDate(int year, int month, int day) {
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException("Year must be between 1 and 9999, got: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }
        int maxDays = getDaysInMonth(year, month);
        if (day < 1 || day > maxDays) {
            throw new IllegalArgumentException("Day must be between 1 and " + maxDays + " for year " + year + " month " + month + ", got: " + day);
        }
    }

    /**
     * Check if the current date is a holiday (Friday in Persian calendar)
     */
    public boolean isHoliday() {
        return get(DAY_OF_WEEK) == WEEKDAY_HOLIDAY_NUMBER;
    }

    /**
     * Check if the current date is today
     */
    public boolean isToday() {
        PersianCalendar today = new PersianCalendar();
        return isEqual(today);
    }

    /**
     * Get the first day of the current month
     */
    public PersianCalendar withFirstDayOfMonth() {
        PersianCalendar result = new PersianCalendar(this);
        result.set(DAY_OF_MONTH, 1);
        return result;
    }

    /**
     * Get the last day of the current month
     */
    public PersianCalendar withLastDayOfMonth() {
        PersianCalendar result  = new PersianCalendar(this);
        int             lastDay = result.getDaysInMonth();
        result.set(DAY_OF_MONTH, lastDay);
        return result;
    }

    /**
     * Get the first day of the current year
     */
    public PersianCalendar withFirstDayOfYear() {
        PersianCalendar result = new PersianCalendar(this);
        result.set(MONTH, FARVARDIN_0);
        result.set(DAY_OF_MONTH, 1);
        return result;
    }

    /**
     * Get the last day of the current year
     */
    public PersianCalendar withLastDayOfYear() {
        PersianCalendar result = new PersianCalendar(this);
        result.set(MONTH, ESFAND_0);
        int lastDay = isLeapYear(result.getYear()) ? 30 : 29;
        result.set(DAY_OF_MONTH, lastDay);
        return result;
    }

    /**
     * Get the start of the day (00:00:00.000)
     */
    public PersianCalendar atStartOfDay() {
        PersianCalendar result = new PersianCalendar(this);
        result.set(HOUR_OF_DAY, 0);
        result.set(MINUTE, 0);
        result.set(SECOND, 0);
        result.set(MILLISECOND, 0);
        return result;
    }

    /**
     * Get the end of the day (23:59:59.999)
     */
    public PersianCalendar atEndOfDay() {
        PersianCalendar result = new PersianCalendar(this);
        result.set(HOUR_OF_DAY, 23);
        result.set(MINUTE, 59);
        result.set(SECOND, 59);
        result.set(MILLISECOND, 999);
        return result;
    }

    /**
     * Get the age in years based on a reference date (usually today)
     */
    public int getAge(PersianCalendar referenceDate) {
        int age = referenceDate.getYear() - this.getYear();

        if (referenceDate.getMonth() < this.getMonth() ||
            (referenceDate.getMonth() == this.getMonth() &&
             referenceDate.getDayOfMonth() < this.getDayOfMonth())) {
            age--;
        }

        return Math.max(0, age);
    }

    /**
     * Get the age in years based on today's date
     */
    public int getAge() {
        return getAge(new PersianCalendar());
    }

    /**
     * Check if the current date is within a date range (inclusive)
     */
    public boolean isBetween(PersianCalendar startDate, PersianCalendar endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !this.isBefore(startDate) && !this.isAfter(endDate);
    }

    /**
     * Get the day of year (1 to 365/366)
     */
    public int getDayOfYear() {
        return get(DAY_OF_YEAR);
    }

    /**
     * Get the week of year
     */
    public int getWeekOfYear() {
        return get(WEEK_OF_YEAR);
    }

    /**
     * Get the week of month
     */
    public int getWeekOfMonth() {
        return get(WEEK_OF_MONTH);
    }

    /**
     * Create a copy of this calendar
     */
    @Override
    public PersianCalendar clone() {
        PersianCalendar clone = new PersianCalendar(this.getTimeZone(), this.locale);

        clone.ymd = this.ymd.clone();
        clone.setTimeInMillis(this.getTimeInMillis());

        clone.lastComputedTime = this.lastComputedTime;
        clone.lastComputedYmd  = this.lastComputedYmd.clone();

        clone.areFieldsSet = this.areFieldsSet;
        for (int i = 0; i < FIELD_COUNT; i++) {
            if (this.isSet[i]) {
                clone.fields[i] = this.fields[i];
                clone.isSet[i]  = true;
            }
        }

        return clone;
    }

    /**
     * Check if the current date is a weekend (Friday in Persian calendar)
     */
    public boolean isWeekend() {
        return isHoliday();
    }

    /**
     * Check if the current date is a weekday (Saturday through Thursday)
     */
    public boolean isWeekday() {
        return !isHoliday();
    }

    /**
     * Get the number of days in the current year
     */
    public int getDaysInYear() {
        return isLeapYear() ? 366 : 365;
    }

    /**
     * Get the quarter (3-month period) of the year
     */
    public int getQuarter() {
        return (getMonth0() / 3) + 1;
    }

    /**
     * Check if the current date is the last day of the month
     */
    public boolean isLastDayOfMonth() {
        return getDayOfMonth() == getDaysInMonth();
    }

    /**
     * Check if the current date is the first day of the month
     */
    public boolean isFirstDayOfMonth() {
        return getDayOfMonth() == 1;
    }

    /**
     * Get the number of days remaining in the current month
     */
    public int getDaysRemainingInMonth() {
        return getDaysInMonth() - getDayOfMonth();
    }

    /**
     * Get the number of days passed in the current month
     */
    public int getDaysPassedInMonth() {
        return getDayOfMonth() - 1;
    }

    public int getDaysPassedFromStartOfYear() {
        int year  = getYear();
        int month = getMonth();
        int day   = getDayOfMonth();

        int daysPassed = day - 1;

        for (int i = 1; i < month; i++) {
            daysPassed += getDaysInMonth(year, i);
        }

        return daysPassed;
    }

    public int getRemainingDaysUntilEndOfYear() {
        int year            = getYear();
        int totalDaysInYear = isLeapYear(year) ? 366 : 365;
        int daysPassed      = getDaysPassedFromStartOfYear();

        return totalDaysInYear - daysPassed;
    }

    public int getGregorianDaysPassedFromStartOfYear() {
        return gCal.get(Calendar.DAY_OF_YEAR) - 1;
    }

    public int getGregorianRemainingDaysUntilEndOfYear() {
        int totalDaysInYear = gCal.getActualMaximum(Calendar.DAY_OF_YEAR);
        int dayOfYear       = gCal.get(Calendar.DAY_OF_YEAR);
        return totalDaysInYear - dayOfYear + 1;
    }

    public int getHijriDaysPassedFromStartOfYear() {
        YMD hijriDate = gregorianToHijri(
                gCal.get(Calendar.YEAR),
                gCal.get(Calendar.MONTH) + 1,
                gCal.get(Calendar.DAY_OF_MONTH)
        );

        // Reset both calendars to midnight for accurate day calculation
        GregorianCalendar startOfYearGreg = hijriToGregorian(hijriDate.year, 1, 1);
        startOfYearGreg.set(Calendar.HOUR_OF_DAY, 0);
        startOfYearGreg.set(Calendar.MINUTE, 0);
        startOfYearGreg.set(Calendar.SECOND, 0);
        startOfYearGreg.set(Calendar.MILLISECOND, 0);

        GregorianCalendar currentGreg = (GregorianCalendar) gCal.clone();
        currentGreg.set(Calendar.HOUR_OF_DAY, 0);
        currentGreg.set(Calendar.MINUTE, 0);
        currentGreg.set(Calendar.SECOND, 0);
        currentGreg.set(Calendar.MILLISECOND, 0);

        long startMillis   = startOfYearGreg.getTimeInMillis();
        long currentMillis = currentGreg.getTimeInMillis();
        long diffMillis    = currentMillis - startMillis;

        // Return days between start of year and now
        return (int) (diffMillis / (24 * 60 * 60 * 1000));
    }

    public int getHijriRemainingDaysUntilEndOfYear() {
        YMD hijriDate = gregorianToHijri(
                gCal.get(Calendar.YEAR),
                gCal.get(Calendar.MONTH) + 1,
                gCal.get(Calendar.DAY_OF_MONTH)
        );

        // Get the Gregorian date for start of NEXT Hijri year
        GregorianCalendar nextYearStartGreg = hijriToGregorian(hijriDate.year + 1, 1, 1);
        nextYearStartGreg.set(Calendar.HOUR_OF_DAY, 0);
        nextYearStartGreg.set(Calendar.MINUTE, 0);
        nextYearStartGreg.set(Calendar.SECOND, 0);
        nextYearStartGreg.set(Calendar.MILLISECOND, 0);

        GregorianCalendar currentGreg = (GregorianCalendar) gCal.clone();
        currentGreg.set(Calendar.HOUR_OF_DAY, 0);
        currentGreg.set(Calendar.MINUTE, 0);
        currentGreg.set(Calendar.SECOND, 0);
        currentGreg.set(Calendar.MILLISECOND, 0);

        long nextYearMillis = nextYearStartGreg.getTimeInMillis();
        long currentMillis  = currentGreg.getTimeInMillis();
        long diffMillis     = nextYearMillis - currentMillis;

        // Return days between now and start of next year
        return (int) (diffMillis / (24 * 60 * 60 * 1000));
    }

    public int getHijriDayOfYear() {
        YMD hijriDate = gregorianToHijri(
                gCal.get(Calendar.YEAR),
                gCal.get(Calendar.MONTH),
                gCal.get(Calendar.DAY_OF_MONTH)
        );

        int dayOfYear = hijriDate.day;

        for (int month = 1; month < hijriDate.month; month++) {
            dayOfYear += HijriConverter.getMonthLength(hijriDate.year, month);
        }

        return dayOfYear;
    }

    public PersianCalendar getStartOfPersianYear() {
        return new PersianCalendar(getYear(), FARVARDIN, 1);
    }

    // Helper method to find start of Hijri year
    public PersianCalendar getStartOfHijriYear() {
        YMD hijriDate = gregorianToHijri(
                gCal.get(Calendar.YEAR),
                gCal.get(Calendar.MONTH),
                gCal.get(Calendar.DAY_OF_MONTH)
        );

        GregorianCalendar startGreg = hijriToGregorian(hijriDate.year, 1, 1);
        return new PersianCalendar(startGreg);
    }

    // Helper method to find start of Gregorian year
    public Calendar getStartOfGregorianYear() {
        Calendar startCal = Calendar.getInstance();
        startCal.set(gCal.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
        return startCal;
    }

    // Helper methods for Hijri calculations
    private int getHijriDaysInMonth(int hijriYear, int hijriMonth) {
        // Hijri months 1-11: Odd months have 30 days, even months have 29 days
        if (hijriMonth <= 11) {
            return (hijriMonth % 2 == 1) ? 30 : 29;
        }

        // Month 12 (Dhu al-Hijjah): 30 in leap years, 29 in common years
        return isHijriLeapYear(hijriYear) ? 30 : 29;
    }

    /////////////////////
    // Add this method to get the next Persian New Year date
    public static PersianCalendar getNextNewYear(PersianCalendar persianDate) {
        if (persianDate == null) {
            throw new IllegalArgumentException("Persian date cannot be null");
        }

        int currentYear  = persianDate.getYear();
        int currentMonth = persianDate.getMonth();
        int currentDay   = persianDate.getDayOfMonth();

        // If current date is before or on New Year (1st of Farvardin)
        // then next New Year is in current year, otherwise next year
        if (currentMonth < FARVARDIN || (currentMonth == FARVARDIN && currentDay <= 1)) {
            // Next New Year is in current year
            return new PersianCalendar(currentYear, FARVARDIN, 1);
        } else {
            // Next New Year is in next year
            return new PersianCalendar(currentYear + 1, FARVARDIN, 1);
        }
    }

    public PersianCalendar getNextNewYear() {
        int currentYear  = getYear();
        int currentMonth = getMonth();
        int currentDay   = getDayOfMonth();

        // If current date is on or after 1st of Farvardin, next new year is next year
        if (currentMonth > FARVARDIN || (currentMonth == FARVARDIN && currentDay >= 1)) {
            currentYear++; // Move to next year
        }

        // Return 1st of Farvardin of the calculated year
        return new PersianCalendar(currentYear, FARVARDIN, 1);
    }

    /**
     * Get the previous Friday (weekend start)
     * @return date of previous Friday
     */
    public PersianCalendar getPreviousFriday() {
        PersianCalendar result           = new PersianCalendar(this);
        int             currentDayOfWeek = get(DAY_OF_WEEK);
        int             daysToSubtract   = (currentDayOfWeek - FRIDAY + 7) % 7;
        if (daysToSubtract == 0) {
            daysToSubtract = 7; // If it's already Friday, go to previous Friday
        }
        result.addDays(-daysToSubtract);
        return result;
    }

    /**
     * Get the next Friday (weekend start)
     * @return date of next Friday
     */
    public PersianCalendar getNextFriday() {
        PersianCalendar result           = new PersianCalendar(this);
        int             currentDayOfWeek = get(DAY_OF_WEEK);
        int             daysToAdd        = (FRIDAY - currentDayOfWeek + 7) % 7;
        if (daysToAdd == 0) {
            daysToAdd = 7; // If it's already Friday, go to next Friday
        }
        result.addDays(daysToAdd);
        return result;
    }

    /**
     * Check if the date is in the past
     * @return true if date is before today
     */
    public boolean isPast() {
        return isBefore(new PersianCalendar());
    }

    /**
     * Check if the date is in the future
     * @return true if date is after today
     */
    public boolean isFuture() {
        return isAfter(new PersianCalendar());
    }

    /**
     * Get the difference in months between this date and another date
     * @param other the date to compare with
     * @return number of months difference (positive if this date is later)
     */
    public int monthsBetween(PersianCalendar other) {
        int yearDiff  = this.ymd[0] - other.ymd[0];
        int monthDiff = this.ymd[1] - other.ymd[1];
        return yearDiff * 12 + monthDiff;
    }

    /**
     * Get the difference in years between this date and another date
     * @param other the date to compare with
     * @return number of years difference (positive if this date is later)
     */
    public int yearsBetween(PersianCalendar other) {
        int yearDiff = this.ymd[0] - other.ymd[0];

        // Adjust if the month/day hasn't occurred yet this year
        if (yearDiff > 0) {
            if (this.ymd[1] < other.ymd[1] ||
                (this.ymd[1] == other.ymd[1] && this.ymd[2] < other.ymd[2])) {
                yearDiff--;
            }
        } else if (yearDiff < 0) {
            if (this.ymd[1] > other.ymd[1] ||
                (this.ymd[1] == other.ymd[1] && this.ymd[2] > other.ymd[2])) {
                yearDiff++;
            }
        }

        return yearDiff;
    }

    /**
     * Get current Persian date as a formatted string
     */
    public String getFormattedDate(String pattern) {
        complete();

        if (pattern == null || pattern.isEmpty()) {
            return getLongDate();
        }

        switch (pattern) {
            case "yyyy/MM/dd":
                return String.format(Locale.US, "%04d/%02d/%02d",
                                     getYear(), getMonth(), getDayOfMonth());
            case "yyyy-MM-dd":
                return String.format(Locale.US, "%04d-%02d-%02d",
                                     getYear(), getMonth(), getDayOfMonth());
            case "dd/MM/yyyy":
                return String.format(Locale.US, "%02d/%02d/%04d",
                                     getDayOfMonth(), getMonth(), getYear());
            case "MM/dd/yyyy":
                return String.format(Locale.US, "%02d/%02d/%04d",
                                     getMonth(), getDayOfMonth(), getYear());
            case "dd MMMM yyyy":
                return String.format(Locale.getDefault(), "%02d %s %04d",
                                     getDayOfMonth(), getMonthName(), getYear());
            case "MMMM yyyy":
                return String.format(Locale.getDefault(), "%s %04d",
                                     getMonthName(), getYear());
            case "yyyy":
                return String.format(Locale.US, "%04d", getYear());
            case "MM":
                return String.format(Locale.US, "%02d", getMonth());
            case "dd":
                return String.format(Locale.US, "%02d", getDayOfMonth());
            default:
                PersianDateFormat formatter = new PersianDateFormat(pattern);
                return formatter.format(this);
        }
    }

    /**
     * Get formatted date with Persian/Farsi numbers
     */
    public String getFormattedDate(String pattern, boolean usePersianNumbers) {
        String result = getFormattedDate(pattern);

        if (usePersianNumbers) {
            result = convertToPersianNumbers(result);
        }

        return result;
    }

    /**
     * Get date as String for Android TextView
     */
    public CharSequence toCharSequence() {
        return getLongDate();
    }

    /**
     * Compare with another Persian calendar (for sorting in Android lists)
     */
    public int compareTo(PersianCalendar other) {
        if (other == null) return 1;
        return Long.compare(this.getTimeInMillis(), other.getTimeInMillis());
    }

    public void setPersianDate(int year, int month, int day, int hourOfDay, int minute, int second) {
        setPersianDate(year, month, day);
        set(HOUR_OF_DAY, hourOfDay);
        set(MINUTE, minute);
        set(SECOND, second);
    }

    public boolean isSameMonth(PersianCalendar other) {
        if (other == null) return false;
        ensureComputed();
        return this.ymd[0] == other.getYear() &&
               this.ymd[1] == other.getMonth0();
    }

    public boolean isSameYear(PersianCalendar other) {
        if (other == null) return false;
        ensureComputed();
        return this.ymd[0] == other.getYear();
    }

    public Date getDate() {
        return new Date(getTimeInMillis());
    }

    public void setDate(Date date) {
        setTimeInMillis(date.getTime());
    }

    public String getWeekdayName() {
        return getWeekdayName(get(DAY_OF_WEEK), locale);
    }

    public String getLongDateTime() {
        return getLongDateTime(
                getYear(), getMonth(), getDayOfMonth(), get(DAY_OF_WEEK),
                get(HOUR_OF_DAY), get(MINUTE), get(SECOND), locale);
    }

    public String getLongDate() {
        return getLongDate(
                getYear(), getMonth(), getDayOfMonth(), get(DAY_OF_WEEK), locale);
    }

    public String getShortDate() {
        return getShortDate("/");
    }

    public String getShortDate2() {
        return getYear() + " " + getMonthName();
    }

    public String getShortDate(String delimiter) {
        ensureComputed();
        return getShortDate(getYear(), getMonth(), getDayOfMonth(), delimiter, locale);
    }

    public YMD getHijriDate() {
        complete();
        return gregorianToHijri(gCal);
    }

    /**
     * Set the date using Islamic (Hijri) calendar values
     * @param hYear Hijri year
     * @param hMonth Hijri month (1-12)
     * @param hDay Hijri day of month
     * @param timeZone Target timezone for the date
     */
    public void setHijriDate(int hYear, int hMonth, int hDay, TimeZone timeZone) {
        // Convert Hijri date to Gregorian using IranianHijriConverter
        GregorianCalendar gCalendar = hijriToGregorian(hYear, hMonth, hDay, timeZone);

        // Adjust to target timezone
        long              timeInMillis     = gCalendar.getTimeInMillis();
        GregorianCalendar adjustedCalendar = new GregorianCalendar(timeZone);
        adjustedCalendar.setTimeInMillis(timeInMillis);

        // Set the Gregorian date
        setGregorianDate(adjustedCalendar.get(Calendar.YEAR),
                         adjustedCalendar.get(Calendar.MONTH) + 1,
                         adjustedCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public void setHijriDate(int hYear, int hMonth, int hDay) {
        GregorianCalendar gCalendar = hijriToGregorian(hYear, hMonth, hDay);
        // gCalendar is in Tehran timezone - EXTRACT Tehran date
        setGregorianDate(gCalendar.get(Calendar.YEAR),
                         gCalendar.get(Calendar.MONTH) + 1,
                         gCalendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Find the Gregorian date corresponding to the first day (Hijri day 1)
     * of a given Hijri month and year.
     *
     * @param islamicYear   Hijri year (e.g., 1446)
     * @param islamicMonth0 Hijri month (0-based: 0 = Muharram, 1 = Safar, ..., 11 = Dhu al-Hijjah)
     * @return Calendar object representing the Gregorian date of the first day of the Hijri month
     */
    public static Calendar findFirstDayOfHijriMonth(int islamicYear, int islamicMonth0) {
        // Convert 0-based month to 1-based for YMD class
        int islamicMonth1Based = islamicMonth0 + 1;

        // Use existing hijriToGregorian method to convert to Gregorian
        return hijriToGregorian(islamicYear, islamicMonth1Based, 1);
    }

    /**
     * ✅ PUBLIC API: 1-based month (1=Farvardin, 12=Esfand)
     */
    public void setPersianDate(int year, int month, int day) {
        validatePersianDate(year, month, day);

        this.ymd[0] = year;
        this.ymd[1] = month - 1; // Convert 1-based to 0-based for internal storage
        this.ymd[2] = day;

        computeGregorianFromPersian();
        setTimeInMillis(gCal.getTimeInMillis());
        lastComputedTime = -1;
    }

    public void parse(String dateString) {
        PersianCalendar pCal = parseOrNullToCompat(dateString);
        if (pCal != null) {
            setPersianDate(pCal.getYear(), pCal.getMonth(), pCal.getDayOfMonth());
            if (pCal.isSet[HOUR_OF_DAY]) set(HOUR_OF_DAY, pCal.get(HOUR_OF_DAY));
            if (pCal.isSet[MINUTE]) set(MINUTE, pCal.get(MINUTE));
            if (pCal.isSet[SECOND]) set(SECOND, pCal.get(SECOND));
        }
    }

    public boolean isLeapYear() {
        return isLeapYear(getYear());
    }

    /**
     * Converts Java Calendar weekday constant to Persian calendar offset.
     */
    public int calculatePersianOffset(int dayOfWeek) {
        if (dayOfWeek < 1 || dayOfWeek > 7) return 0;
        return PERSIAN_OFFSETS[dayOfWeek];
    }

    public int calculatePersianOffset() {
        int javaDayOfWeek = get(DAY_OF_WEEK);
        if (javaDayOfWeek < 1 || javaDayOfWeek > 7) return 0;
        return PERSIAN_OFFSETS[javaDayOfWeek];
    }

    public int calculateGregorianOffsetISO(int javaDayOfWeek) {
        if (javaDayOfWeek < 1 || javaDayOfWeek > 7) return 0;
        return (javaDayOfWeek + 5) % 7;
    }

    public PersianCalendar newInstance() {
        return new PersianCalendar();
    }

    /**
     * Get Persian horoscope (برج فلکی) based on Persian month
     * ✅ @param persianMonth Persian month (1 to 12)
     * @return Persian horoscope name
     */
    public static String getPersianHoroscope(int persianMonth) {
        int month0 = persianMonth - 1; // Convert to 0-based

        if (month0 < 0 || month0 > 11) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + persianMonth);
        }
        return PERSIAN_HOROSCOPE_NAMES[month0];
    }

    /**
     * Get Persian horoscope based on Persian month with English name
     * ✅ @param persianMonth Persian month (1 to 12)
     * @return Array containing [persianName, englishName]
     */
    public static String[] getPersianHoroscopeWithEnglish(int persianMonth) {
        int month0 = persianMonth - 1; // Convert to 0-based

        if (month0 < 0 || month0 > 11) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + persianMonth);
        }
        return new String[]{
                PERSIAN_HOROSCOPE_NAMES[month0],
                PERSIAN_HOROSCOPE_NAMES_EN[month0]
        };
    }

    //=== STATIC UTILITIES ===

    /**
     * Get current Persian date as a new instance
     */
    public static PersianCalendar now() {
        return new PersianCalendar();
    }

    /**
     * Get yesterday's date
     */
    public static PersianCalendar yesterday() {
        PersianCalendar cal = new PersianCalendar();
        cal.addDays(-1);
        return cal;
    }

    /**
     * Get tomorrow's date
     */
    public static PersianCalendar tomorrow() {
        PersianCalendar cal = new PersianCalendar();
        cal.addDays(1);
        return cal;
    }

    /**
     * Get the first day of the given Persian year
     */
    public static PersianCalendar firstDayOfYear(int year) {
        return new PersianCalendar(year, FARVARDIN, 1);
    }

    /**
     * Get the last day of the given Persian year
     */
    public static PersianCalendar lastDayOfYear(int year) {
        int lastDay = isLeapYear(year) ? 30 : 29;
        return new PersianCalendar(year, ESFAND, lastDay);
    }
}