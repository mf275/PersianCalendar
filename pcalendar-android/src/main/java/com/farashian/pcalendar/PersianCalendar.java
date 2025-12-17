package com.farashian.pcalendar;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.*;

import static com.farashian.pcalendar.DateUtils.islamicFromGregorian;
import static com.farashian.pcalendar.PCConstants.PERSIAN_LOCALE;
import static com.farashian.pcalendar.PCConstants.leapYears;
import static com.farashian.pcalendar.PCalendarUtils.*;

public class PersianCalendar extends Calendar implements Parcelable {

    public static final int FIRST_DAY_OF_WEEK      = Calendar.SATURDAY;
    public static final int WEEKDAY_HOLIDAY_NUMBER = Calendar.FRIDAY;

    // Month constants
    public static final int FARVARDIN   = 0;
    public static final int ORDIBEHESHT = 1;
    public static final int KHORDAD     = 2;
    public static final int TIR         = 3;
    public static final int MORDAD      = 4;
    public static final int SHAHRIVAR   = 5;
    public static final int MEHR        = 6;
    public static final int ABAN        = 7;
    public static final int AZAR        = 8;
    public static final int DEY         = 9;
    public static final int BAHMAN      = 10;
    public static final int ESFAND      = 11;

    // Era constant
    private static final int AD = 1;

    protected final GregorianCalendar gCal;
    protected final Locale            locale;
    int[] ymd; // [year, month, day]

    // Caching for performance
    private              long  lastComputedTime = -1;
    private              int[] lastComputedYmd  = {0, 0, 0};
    private static final int[] PERSIAN_OFFSETS  = {0, 1, 2, 3, 4, 5, 6, 0};

    // Gregorian offsets (US convention: Sunday-first)
    // SUNDAY=0, MONDAY=1, TUESDAY=2, WEDNESDAY=3, THURSDAY=4, FRIDAY=5, SATURDAY=6
    private static final int[] GREGORIAN_OFFSETS = {0, 0, 1, 2, 3, 4, 5, 6};

    // Gregorian offsets (ISO convention: Monday-first)
    // MONDAY=0, TUESDAY=1, WEDNESDAY=2, THURSDAY=3, FRIDAY=4, SATURDAY=5, SUNDAY=6
    private static final int[] ISO_OFFSETS = {0, 6, 0, 1, 2, 3, 4, 5};

    // === CONSTRUCTORS ===

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
        this.locale   = locale;
        this.gCal     = new GregorianCalendar(zone, locale);
        this.ymd      = new int[]{1400, 0, 1}; // Default date
    }

    public PersianCalendar(long timeStamp) {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setTimeInMillis(timeStamp);
    }

    public PersianCalendar(PersianCalendar pc) {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setPersianDate(pc.getYear(), pc.getMonth(), pc.getDayOfMonth());
    }

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
    public PersianCalendar(GregorianCalendar georgianCalendar) {
        this();

        if (georgianCalendar == null) {
            throw new IllegalArgumentException("GregorianCalendar cannot be null");
        }

        int year   = georgianCalendar.get(Calendar.YEAR);
        int month  = georgianCalendar.get(Calendar.MONTH);
        int day    = georgianCalendar.get(Calendar.DAY_OF_MONTH);
        int hour   = georgianCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = georgianCalendar.get(Calendar.MINUTE);
        int second = georgianCalendar.get(Calendar.SECOND);

        validateGregorianDate(year, month, day);
        setGregorianDate1Based(year, month, day);
        set(HOUR_OF_DAY, hour);
        set(MINUTE, minute);
        set(SECOND, second);
        setTimeZone(georgianCalendar.getTimeZone());
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
        int month  = gCal.get(Calendar.MONTH) + 1;
        int day    = gCal.get(Calendar.DAY_OF_MONTH);
        int hour   = gCal.get(Calendar.HOUR_OF_DAY);
        int minute = gCal.get(Calendar.MINUTE);
        int second = gCal.get(Calendar.SECOND);

        validateGregorianDate(year, month, day);
        setGregorianDate1Based(year, month, day);
        set(HOUR_OF_DAY, hour);
        set(MINUTE, minute);
        set(SECOND, second);
    }


    /**
     * Create PersianCalendar from Gregorian date string
     */
    public static PersianCalendar fromGeorgianStringYmd(String dateString) {
        return fromGeorgianStringYmd(dateString, "-");
    }

    public static PersianCalendar fromGeorgianStringYmd(String dateString, String delimiter) {
        if (dateString == null || dateString.isEmpty()) {
            throw new IllegalArgumentException("Date string cannot be null or empty");
        }

        String[] parts = dateString.split(delimiter);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid date format. Expected YYYY" + delimiter + "MM" + delimiter + "DD, got: " + dateString);
        }

        try {
            int year  = Integer.parseInt(parts[0].trim());
            int month = Integer.parseInt(parts[1].trim());
            int day   = Integer.parseInt(parts[2].trim());

            return new PersianCalendar(year, month, day);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in date string: " + dateString, e);
        }
    }

    /**
     * Constructor for Parcelable
     */
    protected PersianCalendar(Parcel in) {
        this();
        long timeInMillis = in.readLong();
        setTimeInMillis(timeInMillis);

        boolean hasTimeZone = (in.readByte() == 1);
        if (hasTimeZone) {
            String timeZoneId = in.readString();
            setTimeZone(TimeZone.getTimeZone(timeZoneId));
        }

        int firstDayOfWeek = in.readInt();
        if (firstDayOfWeek > 0) {
            setFirstDayOfWeek(firstDayOfWeek);
        }
    }

    public static final Creator<PersianCalendar> CREATOR = new Creator<PersianCalendar>() {
        @Override
        public PersianCalendar createFromParcel(Parcel in) {
            return new PersianCalendar(in);
        }

        @Override
        public PersianCalendar[] newArray(int size) {
            return new PersianCalendar[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getTimeInMillis());

        TimeZone tz = getTimeZone();
        if (tz != null) {
            dest.writeByte((byte) 1);
            dest.writeString(tz.getID());
        } else {
            dest.writeByte((byte) 0);
        }

        dest.writeInt(getFirstDayOfWeek());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static PersianCalendar fromParcel(Parcel parcel) {
        return CREATOR.createFromParcel(parcel);
    }

    public static PersianCalendar fromParcelable(Parcelable parcelable) {
        if (parcelable instanceof PersianCalendar) {
            return (PersianCalendar) parcelable;
        }
        return new PersianCalendar();
    }

    public android.os.Bundle toBundle() {
        complete();
        android.os.Bundle bundle = new android.os.Bundle();
        bundle.putLong("timeInMillis", getTimeInMillis());
        bundle.putInt("persianYear", getYear());
        bundle.putInt("persianMonth", getMonth());
        bundle.putInt("persianDay", getDayOfMonth());
        bundle.putInt("hour", get(HOUR_OF_DAY));
        bundle.putInt("minute", get(MINUTE));
        bundle.putInt("second", get(SECOND));
        bundle.putString("timeZone", getTimeZone().getID());
        return bundle;
    }

    public static PersianCalendar fromBundle(android.os.Bundle bundle) {
        if (bundle == null) {
            return new PersianCalendar();
        }

        PersianCalendar calendar = new PersianCalendar();

        if (bundle.containsKey("timeInMillis")) {
            calendar.setTimeInMillis(bundle.getLong("timeInMillis"));
        } else if (bundle.containsKey("persianYear")) {
            int year  = bundle.getInt("persianYear", 1400);
            int month = bundle.getInt("persianMonth", 0);
            int day   = bundle.getInt("persianDay", 1);

            calendar.setPersianDate(year, month, day);

            if (bundle.containsKey("hour")) {
                calendar.set(HOUR_OF_DAY, bundle.getInt("hour"));
            }
            if (bundle.containsKey("minute")) {
                calendar.set(MINUTE, bundle.getInt("minute"));
            }
            if (bundle.containsKey("second")) {
                calendar.set(SECOND, bundle.getInt("second"));
            }
        }

        if (bundle.containsKey("timeZone")) {
            String tzId = bundle.getString("timeZone");
            if (tzId != null && !tzId.isEmpty()) {
                calendar.setTimeZone(java.util.TimeZone.getTimeZone(tzId));
            }
        }

        return calendar;
    }

    public static PersianCalendar fromGeorgianTimestamp(long timestamp) {
        Date date = new Date(timestamp);
        return new PersianCalendar(date);
    }

    public int getYear() {
        ensureComputed();
        return ymd[0];
    }

    /** zero-based month, start from 0 to 11 **/
    public int getMonth() {
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

    public int getDaysInMonth(int year, int month) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }

        if (month < 6) {
            return 31;
        } else if (month < 11) {
            return 30;
        } else {
            return isLeapYear(year) ? 30 : 29;
        }
    }

    public static int getDaysInMonthStatic(int year, int month) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }

        if (month < 6) {
            return 31;
        } else if (month < 11) {
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
        return getMonthName(getMonth(), locale);
    }

    // === GREGORIAN DATE METHODS WITHOUT CACHE ===

    /**
     * Get Gregorian year from the underlying GregorianCalendar
     */
    public int getGrgYear() {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.get(Calendar.YEAR);
    }

    /**
     * Get Gregorian month (0-based: 0=January, 11=December)
     */
    public int getGrgMonth() {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.get(Calendar.MONTH);
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
                return ((year % 4 == 0) && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
            default:
                return 31;
        }
    }

    /**
     * Get Gregorian month name (fast version without Calendar)
     */
    public static String getGrgMonthNameFast(int month, Locale locale) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }

        if (locale.getLanguage().equals("fa")) {
            String[] persianMonths = {
                    "ژانویه", "فوریه", "مارس", "آوریل", "می", "ژوئن",
                    "ژوئیه", "اوت", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
            };
            return persianMonths[month];
        } else {
            String[] englishMonths = {
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
            };
            return englishMonths[month];
        }
    }

    /**
     * Get Gregorian month short name (3-letter abbreviation)
     */
    public static String getGrgMonthShortName(int month, Locale locale) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }

        if (locale.getLanguage().equals("fa")) {
            String[] persianShortMonths = {
                    "ژان", "فور", "مار", "آور", "می", "ژوئ",
                    "ژوئ", "اوت", "سپت", "اکت", "نوا", "دسا"
            };
            return persianShortMonths[month];
        } else {
            String[] englishShortMonths = {
                    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            };
            return englishShortMonths[month];
        }
    }

    /**
     * Static method: Get Gregorian month name without creating calendar instance
     */
    public static String getGrgMonthNameStatic(int month, Locale locale) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }

        Calendar temp = new GregorianCalendar(locale);
        temp.set(Calendar.MONTH, month);
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
        int    monthNumber = gCal.get(Calendar.MONTH) + 1;
        return String.format(locale, "%s (%02d)", monthName, monthNumber);
    }

    /**
     * Convert Gregorian date to Persian date
     */
    public static PersianCalendar fromGregorian(int year, int month, int day) {
        PersianCalendar result = new PersianCalendar();
        result.setGregorianDate(year, month, day);
        return result;
    }

    /**
     * Convert Gregorian date to Persian date (1-based month)
     */
    public static PersianCalendar fromGregorian1Based(int year, int month1Based, int day) {
        if (month1Based < 1 || month1Based > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month1Based);
        }
        return fromGregorian(year, month1Based - 1, day);
    }

    /**
     * Get current Gregorian date
     */
    public static PersianCalendar currentGregorian() {
        PersianCalendar result = new PersianCalendar();
        return result;
    }

    /**
     * Get Gregorian date in ISO format (YYYY-MM-DD)
     */
    public String getGrgIsoDate() {
        gCal.setTimeInMillis(getTimeInMillis());
        return String.format(Locale.US, "%04d-%02d-%02d",
                             gCal.get(Calendar.YEAR),
                             gCal.get(Calendar.MONTH) + 1,
                             gCal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Get Gregorian date with 0-based month format
     */
    public String getGrgShortDate(String delimiter) {
        gCal.setTimeInMillis(getTimeInMillis());
        return gCal.get(Calendar.YEAR) + delimiter +
               String.format("%02d", gCal.get(Calendar.MONTH)) + delimiter +
               String.format("%02d", gCal.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Get Gregorian date with 1-based month format (traditional)
     */
    public String getGrgShortDate1Based(String delimiter) {
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
     * Set Gregorian date
     */
    public void setGregorianDate(int year, int month, int day) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }
        if (day < 1 || day > getGrgMonthLength(year, month)) {
            throw new IllegalArgumentException("Invalid day for month: " + day);
        }

        gCal.set(year, month, day);
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * Set Gregorian date with 1-based month (convenience method)
     */
    public void setGregorianDate1Based(int year, int month1Based, int day) {
        if (month1Based < 1 || month1Based > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month1Based);
        }
        setGregorianDate(year, month1Based - 1, day);
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

    // === CORE CALENDAR METHODS ===

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
        fields[MONTH]        = ymd[1];
        fields[DAY_OF_MONTH] = ymd[2];

        fields[HOUR_OF_DAY] = gCal.get(HOUR_OF_DAY);
        fields[MINUTE]      = gCal.get(MINUTE);
        fields[SECOND]      = gCal.get(SECOND);
        fields[MILLISECOND] = gCal.get(MILLISECOND);

        int gregorianDayOfWeek = gCal.get(Calendar.DAY_OF_WEEK);
        int persianOffset = calculatePersianOffset(gregorianDayOfWeek);

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
        fields[ERA] = AD;

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
        int persianOffset = calculatePersianOffset(gregorianDayOfWeek);
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
            dayOfYear += getDaysInMonth(ymd[0], month);
        }
        dayOfYear += ymd[2];
        return dayOfYear;
    }

    /**
     * FIXED: Convert Persian date to Gregorian
     */
    private void computeGregorianFromPersian() {
        int[] g = jalali_to_gregorian(ymd[0], ymd[1] + 1, ymd[2]);

        int hour   = internalGet(HOUR_OF_DAY, 0);
        int minute = internalGet(MINUTE, 0);
        int second = internalGet(SECOND, 0);
        int millis = internalGet(MILLISECOND, 0);

        gCal.set(g[0], g[1] - 1, g[2], hour, minute, second);
        gCal.set(MILLISECOND, millis);
    }

    /**
     * FIXED: Convert Gregorian date to Persian
     */
    private void computePersianFromGregorian() {
        ymd = gregorian_to_jalali(
                gCal.get(Calendar.YEAR), gCal.get(Calendar.MONTH) + 1, gCal.get(Calendar.DAY_OF_MONTH));

        ymd[1] = ymd[1] - 1;
    }

    public boolean isSameDay(PersianCalendar other) {
        if (other == null) return false;
        ensureComputed();
        return this.ymd[0] == other.getYear() &&
               this.ymd[1] == other.getMonth() &&
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
        while (ymd[2] > getDaysInMonth(ymd[0], ymd[1])) {
            ymd[2] -= getDaysInMonth(ymd[0], ymd[1]);
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
            ymd[2] += getDaysInMonth(ymd[0], ymd[1]);
        }
    }

    protected void pinDayOfMonth() {
        int monthLen = getDaysInMonth(internalGet(YEAR), internalGet(MONTH));
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
            int maxDays = getDaysInMonth(ymd[0], ymd[1]);
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
                if (value < 0 || value > 11) {
                    throw new IllegalArgumentException("Month must be between 0 and 11, got: " + value);
                }
                ymd[1] = value;
                adjustDayForNewMonth();
                break;
            case DAY_OF_MONTH:
                int maxDays = getDaysInMonth(ymd[0], ymd[1]);
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
        int maxDays = getDaysInMonth(ymd[0], ymd[1]);
        if (ymd[2] > maxDays) {
            ymd[2] = maxDays;
        }
    }

    // === OVERRIDES ===

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

    // === VALIDATION METHODS ===

    private void validateDate(int year, int month, int day) {
        if (year < 1) {
            throw new IllegalArgumentException("Year must be positive, got: " + year);
        }

        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }

        int maxDays = getDaysInMonth(year, month);
        if (day < 1 || day > maxDays) {
            throw new IllegalArgumentException("Day must be between 1 and " + maxDays +
                                               " for year " + year + " month " + month + ", got: " + day);
        }
    }

    @Override
    public int getMinimum(int field) {
        switch (field) {
            case YEAR:
                return 1;
            case MONTH:
                return FARVARDIN;
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
                return 11;
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
                //Calculate actual weeks in year
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
        return new YMD(ymd);
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

    // === OBJECT METHODS ===

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

    // === INTERNAL HELPERS ===

    private int internalGet(int field, int defaultValue) {
        return isSet[field] ? fields[field] : defaultValue;
    }

    // === DEBUG METHODS ===

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

    public static String getMonthName(int month, Locale locale) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Invalid month index for getMonthName: " + month);
        }

        if (locale.getLanguage().equals("fa")) {
            return PCConstants.PERSIAN_MONTH_NAMES[month];
        }
        return PCConstants.PERSIAN_MONTH_NAMES_IN_ENGLISH[month];
    }

    public String getMonthNameShort() {
        return getMonthNameShort(getMonth(), locale);

    }

    public static String getMonthNameShort(int month, Locale locale) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }

        return locale.getLanguage().equals("fa")
                ? PCConstants.PERSIAN_MONTH_NAMES_SHORT[month]
                : PCConstants.PERSIAN_MONTH_NAMES_ENGLISH_SHORT[month];
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
               formatToTwoDigits(month + 1, locale) + delimiter +
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
            int month = Integer.parseInt(tokens[1].trim()) - 1;
            int day   = Integer.parseInt(tokens[2].trim());

            if (year < 1 || month < 0 || month > 11 || day < 1 || day > 31) {
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

    // === HELPER METHODS FOR DATE MANIPULATION ===

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
               this.ymd[1] == other.getMonth() &&
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
            validatePersianDate(this.ymd[0], this.ymd[1], this.ymd[2]);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
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
        result.set(MONTH, FARVARDIN);
        result.set(DAY_OF_MONTH, 1);
        return result;
    }

    /**
     * Get the last day of the current year
     */
    public PersianCalendar withLastDayOfYear() {
        PersianCalendar result = new PersianCalendar(this);
        result.set(MONTH, ESFAND);
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
        return getMonth() / 3 + 1;
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
                                     getYear(), getMonth() + 1, getDayOfMonth());
            case "yyyy-MM-dd":
                return String.format(Locale.US, "%04d-%02d-%02d",
                                     getYear(), getMonth() + 1, getDayOfMonth());
            case "dd/MM/yyyy":
                return String.format(Locale.US, "%02d/%02d/%04d",
                                     getDayOfMonth(), getMonth() + 1, getYear());
            case "MM/dd/yyyy":
                return String.format(Locale.US, "%02d/%02d/%04d",
                                     getMonth() + 1, getDayOfMonth(), getYear());
            case "dd MMMM yyyy":
                return String.format(Locale.getDefault(), "%02d %s %04d",
                                     getDayOfMonth(), getMonthName(), getYear());
            case "MMMM yyyy":
                return String.format(Locale.getDefault(), "%s %04d",
                                     getMonthName(), getYear());
            case "yyyy":
                return String.format(Locale.US, "%04d", getYear());
            case "MM":
                return String.format(Locale.US, "%02d", getMonth() + 1);
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
               this.ymd[1] == other.getMonth();
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

    public YMD getIslamicDate() {
        complete();
        return islamicFromGregorian(gCal);
    }

    public void setPersianDate(int year, int month, int day) {
        validateDate(year, month, day);

        this.ymd[0] = year;
        this.ymd[1] = month;
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

    /**
     * Calculate offset for Gregorian calendar (US convention: Sunday-first week)
     */
    public int calculateGeorgianOffset(int javaDayOfWeek) {
        if (javaDayOfWeek < 1 || javaDayOfWeek > 7) return 0;
        return GREGORIAN_OFFSETS[javaDayOfWeek];
    }

    /**
     * Calculate offset for Gregorian calendar (ISO convention: Monday-first week)
     */
    public int calculateGeorgianOffsetISO(int javaDayOfWeek) {
        if (javaDayOfWeek < 1 || javaDayOfWeek > 7) return 0;
        return ISO_OFFSETS[javaDayOfWeek];
    }

    public PersianCalendar newInstance() {
        return new PersianCalendar();
    }
}