package com.farashian.pcalendar;

import java.util.*;

import static com.farashian.pcalendar.MyPCConstants.PERSIAN_LOCALE;
import static com.farashian.pcalendar.MyPCConstants.leapYears;

public class MyPersianCalendar extends Calendar {

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
    private long  lastComputedTime = -1;
    private int[] lastComputedYmd  = {0, 0, 0};

    // === CONSTRUCTORS ===

    public MyPersianCalendar() {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setTimeInMillis(System.currentTimeMillis());
    }

    public MyPersianCalendar(TimeZone zone) {
        this(zone, PERSIAN_LOCALE);
        setTimeInMillis(System.currentTimeMillis());
    }

    public MyPersianCalendar(TimeZone zone, Locale locale) {
        super(zone, locale);
        this.locale = locale;
        this.gCal   = new GregorianCalendar(zone, locale);
        this.ymd    = new int[]{1400, 0, 1}; // Default date
    }

    public MyPersianCalendar(MyPersianCalendar pc) {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setPersianDate(pc.getYear(), pc.getMonth(), pc.getDayOfMonth()); // FIXED: Use actual day
    }

    public MyPersianCalendar(int year, int month, int dayOfMonth) {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setPersianDate(year, month, dayOfMonth);
    }

    public MyPersianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        this(year, month, dayOfMonth, hourOfDay, minute, 0);
    }

    public MyPersianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        this();
        setPersianDate(year, month, dayOfMonth);
        set(HOUR_OF_DAY, hourOfDay);
        set(MINUTE, minute);
        set(SECOND, second);
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
            throw new IllegalArgumentException("Month must be between 0 and 11");
        }

        if (month < 6) {
            return 31;
        } else if (month < 11) {
            return 30;
        } else {
            return isLeapYear(year) ? 30 : 29;
        }
    }

    // Static utility method
    public static int getDaysInMonthStatic(int year, int month) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11");
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
        return gCal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


    // Date formatter methods
    public String getMonthName() {
        return getMonthName(getMonth(), locale);
    }

    // === GREGORIAN DATE METHODS ===

    /**
     * Get Gregorian year from the underlying GregorianCalendar
     * @return Gregorian year
     */
    public int getGrgYear() {
        complete(); // Ensure fields are computed
        return gCal.get(Calendar.YEAR);
    }

    /**
     * Get Gregorian month (0-based: 0=January, 11=December)
     * @return Gregorian month (0-11)
     */
    public int getGrgMonth() {
        complete(); // Ensure fields are computed
        return gCal.get(Calendar.MONTH);
    }

    /**
     * Get Gregorian day of month (1-31)
     * @return Gregorian day of month
     */
    public int getGrgDay() {
        complete(); // Ensure fields are computed
        return gCal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get Gregorian week of year (ISO 8601 week numbering)
     * @return Gregorian week of year (1-53)
     */
    public int getGrgWeekOfYear() {
        complete(); // Ensure fields are computed
        return gCal.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Get Gregorian week of month
     * @return Gregorian week of month (1-6)
     */
    public int getGrgWeekOfMonth() {
        complete(); // Ensure fields are computed
        return gCal.get(Calendar.WEEK_OF_MONTH);
    }

    /**
     * Get Gregorian day of week (Calendar.SUNDAY=1, Calendar.SATURDAY=7)
     * @return Gregorian day of week
     */
    public int getGrgDayOfWeek() {
        complete(); // Ensure fields are computed
        return gCal.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Get Gregorian day of week name
     * @param locale locale for weekday name
     * @return Gregorian weekday name
     */
    public String getGrgDayOfWeekName(Locale locale) {
        complete(); // Ensure fields are computed
        int dayOfWeek = gCal.get(Calendar.DAY_OF_WEEK);

        // Map to Persian names if needed
        if (locale.getLanguage().equals("fa")) {
            String[] persianWeekdays = {"یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه",
                    "پنجشنبه", "جمعه", "شنبه"};
            // Calendar.DAY_OF_WEEK: 1=SUNDAY, 2=MONDAY, ..., 7=SATURDAY
            return persianWeekdays[dayOfWeek - 1];
        } else {
            return gCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale);
        }
    }

    /**
     * Get Gregorian month length for specific year and month
     * @param year Gregorian year
     * @param month 0-based Gregorian month (0=January)
     * @return number of days in the month
     */
    public static int getGrgMonthLength(int year, int month) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }

        // Fast switch statement
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
                // Leap year calculation
                return ((year % 4 == 0) && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
            default:
                return 31;
        }
    }
    /**
     * Get Gregorian month name (fast version without Calendar)
     * @param month 0-based Gregorian month (0=January)
     * @param locale locale for month name
     * @return month name
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
     * @param month 0-based Gregorian month (0=January)
     * @param locale locale for month name
     * @return month short name
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
     * @param month 0-based Gregorian month (0=January)
     * @param locale locale for month name
     * @return Gregorian month name
     */
    public static String getGrgMonthNameStatic(int month, Locale locale) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }

        // Create a temporary calendar just for getting the month name
        Calendar temp = new GregorianCalendar(locale);
        temp.set(Calendar.MONTH, month);
        temp.set(Calendar.DAY_OF_MONTH, 1);
        return temp.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
    }

    /**
     * Convert Gregorian date to Persian date
     * @param year Gregorian year
     * @param month 0-based Gregorian month (0=January)
     * @param day Gregorian day
     * @return Persian date as MyPersianCalendar
     */
    public static MyPersianCalendar fromGregorian(int year, int month, int day) {
        MyPersianCalendar result = new MyPersianCalendar();
        result.setGrgDate(year, month, day);
        return result;
    }

    /**
     * Convert Gregorian date to Persian date (1-based month)
     * @param year Gregorian year
     * @param month1Based 1-based Gregorian month (1=January)
     * @param day Gregorian day
     * @return Persian date as MyPersianCalendar
     */
    public static MyPersianCalendar fromGregorian1Based(int year, int month1Based, int day) {
        if (month1Based < 1 || month1Based > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month1Based);
        }
        return fromGregorian(year, month1Based - 1, day);
    }

    /**
     * Get current Gregorian date
     * @return current Gregorian date as MyPersianCalendar
     */
    public static MyPersianCalendar currentGregorian() {
        MyPersianCalendar result = new MyPersianCalendar();
        // The calendar is already initialized with current time
        return result;
    }

    /**
     * Get full Gregorian date as string
     * @param locale locale for formatting
     * @return formatted Gregorian date
     */
    public String getGrgLongDate(Locale locale) {
        complete(); // Ensure fields are computed
        return String.format(locale, "%s, %d %s %d",
                             getGrgDayOfWeekName(locale),
                             getGrgDay(),
                             getGrgMonthName(locale),
                             getGrgYear());
    }

    /**
     * Get Gregorian month name using instance's locale
     * @return Gregorian month name in current locale
     */
    public String getGrgMonthName() {
        return getGrgMonthName(this.locale);
    }

    /**
     * Get Gregorian month name with specified locale
     * @param locale locale for month name
     * @return Gregorian month name
     */
    public String getGrgMonthName1(Locale locale) {
        complete(); // Ensure fields are computed
        return gCal.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
    }

    public String getGrgMonthName(Locale locale) {
        complete(); // Ensure fields are computed

        // For Java 8 compatibility, use our fast method for Persian
        if (locale.getLanguage().equals("fa") || locale.getLanguage().equals("fa_IR")) {
            return getGrgMonthNameFast(gCal.get(Calendar.MONTH), locale);
        }

        // For other locales, try Java's built-in (might work for some)
        String name = gCal.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
        if (name == null || name.isEmpty()) {
            // Fallback to English
            name = getGrgMonthNameFast(gCal.get(Calendar.MONTH), Locale.ENGLISH);
        }
        return name;
    }


    /**
     * Get Gregorian month short name (3-letter abbreviation)
     * @param locale locale for month name
     * @return Gregorian month short name
     */
    public String getGrgMonthShortName(Locale locale) {
        complete(); // Ensure fields are computed
        return gCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale);
    }

    /**
     * Get Gregorian month name with number (e.g., "January (01)")
     * @param locale locale for month name
     * @return formatted month name with number
     */
    public String getGrgMonthNameWithNumber(Locale locale) {
        complete(); // Ensure fields are computed
        String monthName = getGrgMonthName(locale);
        int monthNumber = getGrgMonth() + 1; // Convert to 1-based
        return String.format(locale, "%s (%02d)", monthName, monthNumber);
    }

    /**
     * Get Gregorian date in ISO format (YYYY-MM-DD)
     * @return ISO formatted date
     */
    public String getGrgIsoDate() {
        complete(); // Ensure fields are computed
        return String.format(Locale.US, "%04d-%02d-%02d",
                             getGrgYear(),
                             getGrgMonth() + 1, // Convert to 1-based for ISO
                             getGrgDay());
    }

    /**
     * Get Gregorian date with 0-based month format
     * @param delimiter delimiter between parts
     * @return formatted date with 0-based month
     */
    public String getGrgShortDate(String delimiter) {
        complete(); // Ensure fields are computed
        return getGrgYear() + delimiter +
               String.format("%02d", getGrgMonth()) + delimiter + // 0-based month
               String.format("%02d", getGrgDay());
    }

    /**
     * Get Gregorian date with 1-based month format (traditional)
     * @param delimiter delimiter between parts
     * @return formatted date with 1-based month
     */
    public String getGrgShortDate1Based(String delimiter) {
        complete(); // Ensure fields are computed
        return getGrgYear() + delimiter +
               String.format("%02d", getGrgMonth() + 1) + delimiter + // 1-based month
               String.format("%02d", getGrgDay());
    }

    /**
     * Check if current Gregorian date is today
     * @return true if Gregorian date is today
     */
    public boolean isGrgToday() {
        GregorianCalendar today = new GregorianCalendar();
        today.setTimeZone(getTimeZone());
        today.setTime(new Date());

        return today.get(Calendar.YEAR) == getGrgYear() &&
               today.get(Calendar.MONTH) == getGrgMonth() &&
               today.get(Calendar.DAY_OF_MONTH) == getGrgDay();
    }

    /**
     * Get Gregorian date as Date object
     * @return Date object representing the Gregorian date
     */
    public Date getGrgDate() {
        complete(); // Ensure fields are computed
        return gCal.getTime();
    }

    /**
     * Get Gregorian date as milliseconds
     * @return milliseconds since epoch
     */
    public long getGrgTimeInMillis() {
        complete(); // Ensure fields are computed
        return gCal.getTimeInMillis();
    }

    /**
     * Set Gregorian date
     * @param year Gregorian year
     * @param month 0-based Gregorian month (0=January)
     * @param day day of month (1-31)
     */
    public void setGrgDate(int year, int month, int day) {
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
     * @param year Gregorian year
     * @param month1Based 1-based Gregorian month (1=January)
     * @param day day of month (1-31)
     */
    public void setGrgDate1Based(int year, int month1Based, int day) {
        if (month1Based < 1 || month1Based > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month1Based);
        }
        setGrgDate(year, month1Based - 1, day);
    }

    /**
     * Add days to Gregorian date
     * @param days number of days to add (can be negative)
     */
    public void addGrgDays(int days) {
        complete(); // Ensure fields are computed
        gCal.add(Calendar.DAY_OF_MONTH, days);
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * Add months to Gregorian date
     * @param months number of months to add (can be negative)
     */
    public void addGrgMonths(int months) {
        complete(); // Ensure fields are computed
        gCal.add(Calendar.MONTH, months);
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * Add years to Gregorian date
     * @param years number of years to add (can be negative)
     */
    public void addGrgYears(int years) {
        complete(); // Ensure fields are computed
        gCal.add(Calendar.YEAR, years);
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * Get difference in days between this Gregorian date and another
     * @param other the other date
     * @return number of days difference
     */
    public long grgDaysBetween(MyPersianCalendar other) {
        long thisMillis = this.getGrgTimeInMillis();
        long otherMillis = other.getGrgTimeInMillis();
        return Math.abs(thisMillis - otherMillis) / (1000 * 60 * 60 * 24);
    }

    /**
     * Check if Gregorian year is leap year
     * @param year Gregorian year
     * @return true if leap year
     */
    public static boolean isGrgLeapYear(int year) {
        return (year % 4 == 0) && (year % 100 != 0 || year % 400 == 0);
    }

    /**
     * Check if current Gregorian year is leap year
     * @return true if leap year
     */
    public boolean isGrgLeapYear() {
        return isGrgLeapYear(getGrgYear());
    }

    /**
     * Get day of year for Gregorian date (1-365/366)
     * @return day of year
     */
    public int getGrgDayOfYear() {
        complete(); // Ensure fields are computed
        return gCal.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Get the first day of week for Gregorian calendar
     * @return first day of week (Calendar.SUNDAY or Calendar.MONDAY)
     */
    public int getGrgFirstDayOfWeek() {
        return gCal.getFirstDayOfWeek();
    }

    /**
     * Check if Gregorian date is weekend (Saturday or Sunday)
     * @return true if Saturday or Sunday
     */
    public boolean isGrgWeekend() {
        int dayOfWeek = getGrgDayOfWeek();
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    /**
     * Check if Gregorian date is weekday (Monday to Friday)
     * @return true if Monday to Friday
     */
    public boolean isGrgWeekday() {
        return !isGrgWeekend();
    }

    public int getPersianYear() {
        return getYear();
    }

    public int getPersianMonth() {
        return getMonth();
    }

    public int getPersianDay() {
        return getDayOfMonth();
    }

    public String getWeekdayName() {
        complete(); // Ensure fields are computed
        return getWeekdayName(get(DAY_OF_WEEK), locale);
    }

    public String getLongDate() {
        complete(); // Ensure all fields are computed
        return getLongDate(
                getYear(), getMonth(), getDayOfMonth(), get(DAY_OF_WEEK), locale);
    }

    public String getLongDateTime() {
        complete(); // Ensure all fields are computed
        return getLongDateTime(
                getYear(), getMonth(), getDayOfMonth(), get(DAY_OF_WEEK),
                get(HOUR_OF_DAY), get(MINUTE), get(SECOND), locale);
    }

    public String getShortDate() {
        return getShortDate("/");
    }

    public String getShortDate2() {
        return getPersianYear() + " " + getMonthName();
    }

    public String getShortDate(String delimiter) {
        ensureComputed();
        return getShortDate(getYear(), getMonth(), getDayOfMonth(), delimiter, locale);
    }

    public void setPersianDate(int year, int month, int day) {
        validateDate(year, month, day);

        this.ymd[0] = year;
        this.ymd[1] = month;
        this.ymd[2] = day;

        computeGregorianFromPersian();
        setTimeInMillis(gCal.getTimeInMillis());
        lastComputedTime = -1; // Invalidate cache
    }

    public void parse(String dateString) {
        MyPersianCalendar pCal = parseOrNullToCompat(dateString);
        if (pCal != null) {
            // Create a fresh instance instead of copying internal state
            setPersianDate(pCal.getYear(), pCal.getMonth(), pCal.getDayOfMonth());
            // Copy time fields if they exist
            if (pCal.isSet[HOUR_OF_DAY]) set(HOUR_OF_DAY, pCal.get(HOUR_OF_DAY));
            if (pCal.isSet[MINUTE]) set(MINUTE, pCal.get(MINUTE));
            if (pCal.isSet[SECOND]) set(SECOND, pCal.get(SECOND));
        }
    }

    public boolean isLeapYear() {
        return isLeapYear(getYear());
    }

    // === CORE CALENDAR METHODS (FIXED MONTH CONVERSION) ===

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

            // Compute all calendar fields
            computeAllCalendarFields();
        }
    }

    /**
     * Compute all calendar fields expected by parent Calendar class
     */
    private void computeAllCalendarFields() {
        // Use Gregorian calendar to compute all fields reliably
        gCal.setTimeInMillis(time);

        // First compute Persian date from Gregorian to ensure consistency
        computePersianFromGregorian();

        // Set basic Persian date fields
        fields[YEAR]         = ymd[0];
        fields[MONTH]        = ymd[1];
        fields[DAY_OF_MONTH] = ymd[2];

        // Set time fields from Gregorian
        fields[HOUR_OF_DAY] = gCal.get(HOUR_OF_DAY);
        fields[MINUTE]      = gCal.get(MINUTE);
        fields[SECOND]      = gCal.get(SECOND);
        fields[MILLISECOND] = gCal.get(MILLISECOND);

        // Use Gregorian's properly computed day-of-week (it's consistent and reliable)
        fields[DAY_OF_WEEK] = gCal.get(DAY_OF_WEEK);

        // Calculate proper Persian day of year
        fields[DAY_OF_YEAR] = calculateDayOfYear();

        // Calculate Persian week fields
        calculateWeekFields();

        // Set other fields
        fields[AM_PM]       = gCal.get(AM_PM);
        fields[HOUR]        = gCal.get(HOUR);
        fields[DST_OFFSET]  = gCal.get(DST_OFFSET);
        fields[ZONE_OFFSET] = gCal.get(ZONE_OFFSET);

        // ERA is always AD for Persian calendar
        fields[ERA] = AD;

        // Mark all fields as set
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

        // Calculate week of year (Persian year starts with Farvardin 1)
        int weekOfYear = (dayOfYear - 1 + ((dayOfWeek - Calendar.SATURDAY + 7) % 7)) / 7 + 1;
        fields[WEEK_OF_YEAR] = weekOfYear;

        // Calculate week of month
        int dayOfMonth      = fields[DAY_OF_MONTH];
        int firstDayOfMonth = calculateFirstDayOfMonth();
        int weekOfMonth     = (dayOfMonth - 1 + ((dayOfWeek - firstDayOfMonth + 7) % 7)) / 7 + 1;
        fields[WEEK_OF_MONTH] = weekOfMonth;

        // Calculate day of week in month
        fields[DAY_OF_WEEK_IN_MONTH] = (dayOfMonth - 1) / 7 + 1;
    }

    /**
     * Calculate the day of week for the first day of the current month
     */
    private int calculateFirstDayOfMonth() {
        // Save current state
        int savedDay = ymd[2];

        // Set to first day of month
        ymd[2] = 1;
        computeGregorianFromPersian();

        // Get day of week for first day
        int firstDayOfWeek = gCal.get(DAY_OF_WEEK);

        // Restore original state
        ymd[2] = savedDay;
        computeGregorianFromPersian();

        return firstDayOfWeek;
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
     * Now correctly handles 0-based month to 1-based month conversion
     */
    private void computeGregorianFromPersian() {
        // Convert 0-based month (0-11) to 1-based month (1-12) for the algorithm
        int[] g = jalali_to_gregorian(ymd[0], ymd[1] + 1, ymd[2]);

        // Get current time fields
        int hour   = internalGet(HOUR_OF_DAY, 0);
        int minute = internalGet(MINUTE, 0);
        int second = internalGet(SECOND, 0);
        int millis = internalGet(MILLISECOND, 0);

        // g[1] is 1-based Gregorian month, convert to 0-based for GregorianCalendar
        gCal.set(g[0], g[1] - 1, g[2], hour, minute, second);
        gCal.set(MILLISECOND, millis);
    }

    /**
     * FIXED: Convert Gregorian date to Persian
     * Now correctly handles 1-based month to 0-based month conversion
     */
    private void computePersianFromGregorian() {
        // Convert Gregorian 0-based month to 1-based for the algorithm
        ymd = gregorian_to_jalali(
                gCal.get(Calendar.YEAR), gCal.get(Calendar.MONTH) + 1, gCal.get(Calendar.DAY_OF_MONTH));

        // The algorithm returns 1-based month, convert to 0-based for internal storage
        ymd[1] = ymd[1] - 1;
    }

    private void ensureComputed() {
        if (lastComputedTime != time) {
            computeFields();
        }
    }

    // === ARITHMETIC METHODS ===

    @Override
    public void add(int field, int amount) {
        if (amount == 0) return;
        if (field < 0 || field >= ZONE_OFFSET) return;

        complete(); // Ensure fields are computed

        if (field == YEAR || field == MONTH || field == DAY_OF_MONTH) {
            // Handle Persian date arithmetic directly for better accuracy
            handlePersianDateArithmetic(field, amount);
        } else {
            // For other fields, use Gregorian arithmetic
            computeTime(); // Sync gCal
            gCal.add(field, amount);
            computePersianFromGregorian();
        }

        // Invalidate cache and recompute fields
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

        // Recompute Gregorian date
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
        complete(); // Ensure fields are computed

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

        // Recompute Gregorian date and sync
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

        // Recompute Gregorian date and sync everything
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
        lastComputedTime = -1; // Invalidate cache
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

    // In MyPersianCalendar and FastPersianCalendar:
    @Override
    public int getMinimum(int field) {
        switch (field) {
            case YEAR: return 1;
            case MONTH: return FARVARDIN; // 0
            case DAY_OF_MONTH: return 1;
            case DAY_OF_WEEK: return SUNDAY; // 1
            case DAY_OF_WEEK_IN_MONTH: return 1;
            case DAY_OF_YEAR: return 1;
            case WEEK_OF_YEAR: return 1;
            case WEEK_OF_MONTH: return 1;
            case HOUR_OF_DAY: return 0;
            case HOUR: return 1;
            case MINUTE: return 0;
            case SECOND: return 0;
            case MILLISECOND: return 0;
            case AM_PM: return AM; // 0
            case ERA: return AD; // 1
            case ZONE_OFFSET: return -13 * 60 * 60 * 1000; // -13 hours
            case DST_OFFSET: return 0;
            default: return 0;
        }
    }

    @Override
    public int getGreatestMinimum(int field) {
        return getMinimum(field);
    }

    @Override
    public int getLeastMaximum(int field) {
        switch (field) {
            case DAY_OF_MONTH: return 29; // Esfand in common year
            case WEEK_OF_MONTH: return 4;
            case WEEK_OF_YEAR: return 52;
            default: return getMaximum(field);
        }
    }

    @Override
    public int getMaximum(int field) {
        switch (field) {
            case YEAR:
                return 9999;
            case MONTH:
                return 11; // 0-based months (0-11)
            case DAY_OF_MONTH:
                return 31; // Maximum days in Persian month
            case DAY_OF_WEEK:
                return SATURDAY; // 7
            case DAY_OF_WEEK_IN_MONTH:
                return 6;
            case DAY_OF_YEAR:
                return 366; // Leap year max
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
                return PM; // 1
            case ERA:
                return AD; // 1
            case ZONE_OFFSET:
                return 14 * 60 * 60 * 1000; // 14 hours in milliseconds
            case DST_OFFSET:
                return 2 * 60 * 60 * 1000; // 2 hours DST
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
    public int[] getYmd() {
        ensureComputed();
        return ymd.clone();
    }

    /**
     * Convenience method to get day of week
     */
    public int getDayOfWeek() {
        complete();
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

        MyPersianCalendar that = (MyPersianCalendar) obj;
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

    /**
     * Debug method to check current state
     */
    public String debugInfo() {
        ensureComputed();
        return String.format(Locale.US,
                             "Persian: %d/%d/%d, Gregorian: %d/%d/%d, Month Name: %s, Day of Week: %s",
                             ymd[0], ymd[1] + 1, ymd[2],
                             gCal.get(Calendar.YEAR), gCal.get(Calendar.MONTH) + 1, gCal.get(Calendar.DAY_OF_MONTH),
                             getMonthName(), getWeekdayName());
    }

    public static boolean isLeapYear(int year) {
        // Or use the same algorithm for consistency
        int remainder = year % 33;
        return remainder == 1 || remainder == 5 || remainder == 9 ||
               remainder == 13 || remainder == 17 || remainder == 22 ||
               remainder == 26 || remainder == 30;
    }


    // Helper method to get first day of month
    private int getFirstDayOfMonth() {
        // Save current day
        int currentDay = get(DAY_OF_MONTH);

        // Set to first day of month
        set(DAY_OF_MONTH, 1);
        int firstDayOfWeek = get(DAY_OF_WEEK);

        // Restore original day
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
                ? MyPCConstants.WEEKDAY_NAMES[index]
                : MyPCConstants.WEEKDAY_NAMES_SHORT_IN_ENGLISH[index];
    }

    public static String getMonthName(int month, Locale locale) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Invalid month index for getMonthName: " + month);
        }

        if (locale.getLanguage().equals("fa")) {
            return MyPCConstants.MONTH_NAMES[month];
        }
        return MyPCConstants.MONTH_NAMES_IN_ENGLISH[month];
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

    public static MyPersianCalendar parseOrNullToCompat(String dateString) {
        return parseOrNullToCompat(dateString, "/");
    }

    public static MyPersianCalendar parseOrNullToCompat(String dateString, String delimiter) {
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

            // Basic validation
            if (year < 1 || month < 0 || month > 11 || day < 1 || day > 31) {
                return null;
            }

            // Additional validation for actual month length
            int maxDays = (new MyPersianCalendar()).getDaysInMonth(year, month);
            if (day > maxDays) {
                return null;
            }

            return new MyPersianCalendar(year, month, day);
        } catch (Exception e) {
            return null;
        }
    }

    // === HELPER METHODS FOR DATE MANIPULATION ===

    /**
     * Add days to the current date
     * @param days number of days to add (can be negative to subtract)
     */
    public void addDays(int days) {
        add(DAY_OF_MONTH, days);
    }

    /**
     * Add weeks to the current date
     * @param weeks number of weeks to add (can be negative to subtract)
     */
    public void addWeeks(int weeks) {
        addDays(weeks * 7);
    }

    /**
     * Add months to the current date
     * @param months number of months to add (can be negative to subtract)
     */
    public void addMonths(int months) {
        add(MONTH, months);
    }

    /**
     * Add years to the current date
     * @param years number of years to add (can be negative to subtract)
     */
    public void addYears(int years) {
        add(YEAR, years);
    }

    /**
     * Get a copy of this calendar with days added
     * @param days number of days to add
     * @return new MyPersianCalendar instance with the added days
     */
    public MyPersianCalendar plusDays(int days) {
        MyPersianCalendar result = new MyPersianCalendar(this);
        result.addDays(days);
        return result;
    }

    /**
     * Get a copy of this calendar with days subtracted
     * @param days number of days to subtract
     * @return new MyPersianCalendar instance with the subtracted days
     */
    public MyPersianCalendar minusDays(int days) {
        return plusDays(-days);
    }

    /**
     * Get a copy of this calendar with weeks added
     * @param weeks number of weeks to add
     * @return new MyPersianCalendar instance with the added weeks
     */
    public MyPersianCalendar plusWeeks(int weeks) {
        MyPersianCalendar result = new MyPersianCalendar(this);
        result.addWeeks(weeks);
        return result;
    }

    /**
     * Get a copy of this calendar with weeks subtracted
     * @param weeks number of weeks to subtract
     * @return new MyPersianCalendar instance with the subtracted weeks
     */
    public MyPersianCalendar minusWeeks(int weeks) {
        return plusWeeks(-weeks);
    }

    /**
     * Get a copy of this calendar with months added
     * @param months number of months to add
     * @return new MyPersianCalendar instance with the added months
     */
    public MyPersianCalendar plusMonths(int months) {
        MyPersianCalendar result = new MyPersianCalendar(this);
        result.addMonths(months);
        return result;
    }

    /**
     * Get a copy of this calendar with months subtracted
     * @param months number of months to subtract
     * @return new MyPersianCalendar instance with the subtracted months
     */
    public MyPersianCalendar minusMonths(int months) {
        return plusMonths(-months);
    }

    /**
     * Get a copy of this calendar with years added
     * @param years number of years to add
     * @return new MyPersianCalendar instance with the added years
     */
    public MyPersianCalendar plusYears(int years) {
        MyPersianCalendar result = new MyPersianCalendar(this);
        result.addYears(years);
        return result;
    }

    /**
     * Get a copy of this calendar with years subtracted
     * @param years number of years to subtract
     * @return new MyPersianCalendar instance with the subtracted years
     */
    public MyPersianCalendar minusYears(int years) {
        return plusYears(-years);
    }

    /**
     * Check if this date is before another Persian date
     * @param other the date to compare with
     * @return true if this date is before the other date
     */
    public boolean isBefore(MyPersianCalendar other) {
        return this.getTimeInMillis() < other.getTimeInMillis();
    }

    /**
     * Check if this date is after another Persian date
     * @param other the date to compare with
     * @return true if this date is after the other date
     */
    public boolean isAfter(MyPersianCalendar other) {
        return this.getTimeInMillis() > other.getTimeInMillis();
    }

    /**
     * Check if this date is equal to another Persian date
     * @param other the date to compare with
     * @return true if both dates represent the same day
     */
    public boolean isEqual(MyPersianCalendar other) {
        return this.getYear() == other.getYear() &&
               this.getMonth() == other.getMonth() &&
               this.getDayOfMonth() == other.getDayOfMonth();
    }

    /**
     * Get the number of days between this date and another date
     * @param other the date to compare with
     * @return number of days between the two dates (positive if this date is later)
     */
    public long daysBetween(MyPersianCalendar other) {
        long diffMillis = this.getTimeInMillis() - other.getTimeInMillis();
        return diffMillis / (1000 * 60 * 60 * 24);
    }

    /**
     * Check if the current date is a holiday (Friday in Persian calendar)
     * @return true if the day is Friday
     */
    public boolean isHoliday() {
        return get(DAY_OF_WEEK) == WEEKDAY_HOLIDAY_NUMBER;
    }

    /**
     * Check if the current date is today
     * @return true if the date represents today
     */
    public boolean isToday() {
        MyPersianCalendar today = new MyPersianCalendar();
        return isEqual(today);
    }

    /**
     * Get the first day of the current month
     * @return new MyPersianCalendar instance set to the first day of current month
     */
    public MyPersianCalendar withFirstDayOfMonth() {
        MyPersianCalendar result = new MyPersianCalendar(this);
        result.set(DAY_OF_MONTH, 1);
        return result;
    }

    /**
     * Get the last day of the current month
     * @return new MyPersianCalendar instance set to the last day of current month
     */
    public MyPersianCalendar withLastDayOfMonth() {
        MyPersianCalendar result  = new MyPersianCalendar(this);
        int               lastDay = result.getDaysInMonth();
        result.set(DAY_OF_MONTH, lastDay);
        return result;
    }

    /**
     * Get the first day of the current year
     * @return new MyPersianCalendar instance set to Farvardin 1 of current year
     */
    public MyPersianCalendar withFirstDayOfYear() {
        MyPersianCalendar result = new MyPersianCalendar(this);
        result.set(MONTH, FARVARDIN);
        result.set(DAY_OF_MONTH, 1);
        return result;
    }

    /**
     * Get the last day of the current year
     * @return new MyPersianCalendar instance set to Esfand 29/30 of current year
     */
    public MyPersianCalendar withLastDayOfYear() {
        MyPersianCalendar result = new MyPersianCalendar(this);
        result.set(MONTH, ESFAND);
        int lastDay = isLeapYear(result.getYear()) ? 30 : 29;
        result.set(DAY_OF_MONTH, lastDay);
        return result;
    }

    /**
     * Get the start of the day (00:00:00.000)
     * @return new MyPersianCalendar instance with time set to midnight
     */
    public MyPersianCalendar atStartOfDay() {
        MyPersianCalendar result = new MyPersianCalendar(this);
        result.set(HOUR_OF_DAY, 0);
        result.set(MINUTE, 0);
        result.set(SECOND, 0);
        result.set(MILLISECOND, 0);
        return result;
    }

    /**
     * Get the end of the day (23:59:59.999)
     * @return new MyPersianCalendar instance with time set to end of day
     */
    public MyPersianCalendar atEndOfDay() {
        MyPersianCalendar result = new MyPersianCalendar(this);
        result.set(HOUR_OF_DAY, 23);
        result.set(MINUTE, 59);
        result.set(SECOND, 59);
        result.set(MILLISECOND, 999);
        return result;
    }

    /**
     * Get the age in years based on a reference date (usually today)
     * @param referenceDate the date to calculate age against (usually today)
     * @return age in years
     */
    public int getAge(MyPersianCalendar referenceDate) {
        int age = referenceDate.getYear() - this.getYear();

        // Adjust if birthday hasn't occurred yet this year
        if (referenceDate.getMonth() < this.getMonth() ||
            (referenceDate.getMonth() == this.getMonth() &&
             referenceDate.getDayOfMonth() < this.getDayOfMonth())) {
            age--;
        }

        return Math.max(0, age);
    }

    /**
     * Get the age in years based on today's date
     * @return age in years
     */
    public int getAge() {
        return getAge(new MyPersianCalendar());
    }

    /**
     * Check if the current date is within a date range (inclusive)
     * @param startDate start of the range
     * @param endDate end of the range
     * @return true if current date is between startDate and endDate (inclusive)
     */
    public boolean isBetween(MyPersianCalendar startDate, MyPersianCalendar endDate) {
        return !this.isBefore(startDate) && !this.isAfter(endDate);
    }

    /**
     * Get the day of year (1 to 365/366)
     * @return day of year
     */
    public int getDayOfYear() {
        complete(); // Ensure fields are computed
        return get(DAY_OF_YEAR);
    }

    /**
     * Get the week of year
     * @return week of year (1-53)
     */
    public int getWeekOfYear() {
        complete(); // Ensure fields are computed
        return get(WEEK_OF_YEAR);
    }

    /**
     * Get the week of month
     * @return week of month (1-6)
     */
    public int getWeekOfMonth() {
        complete(); // Ensure fields are computed
        return get(WEEK_OF_MONTH);
    }

    /**
     * Create a copy of this calendar
     * @return a deep copy of this MyPersianCalendar instance
     */

    @Override
    public MyPersianCalendar clone() {
        MyPersianCalendar clone = new MyPersianCalendar(this.getTimeZone(), this.locale);

        // Copy the internal state
        clone.ymd = this.ymd.clone();
        clone.setTimeInMillis(this.getTimeInMillis());

        // Copy cache
        clone.lastComputedTime = this.lastComputedTime;
        clone.lastComputedYmd  = this.lastComputedYmd.clone();

        // Copy Calendar fields
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
     * @return true if the day is Friday
     */
    public boolean isWeekend() {
        return isHoliday();
    }

    /**
     * Check if the current date is a weekday (Saturday through Thursday)
     * @return true if the day is not Friday
     */
    public boolean isWeekday() {
        return !isHoliday();
    }

    /**
     * Get the number of days in the current year
     * @return 365 or 366 depending on leap year
     */
    public int getDaysInYear() {
        return isLeapYear() ? 366 : 365;
    }

    /**
     * Get the quarter (3-month period) of the year
     * @return quarter number (1-4)
     */
    public int getQuarter() {
        return getMonth() / 3 + 1;
    }

    /**
     * Check if the current date is the last day of the month
     * @return true if current day is the last day of the month
     */
    public boolean isLastDayOfMonth() {
        return getDayOfMonth() == getDaysInMonth();
    }

    /**
     * Check if the current date is the first day of the month
     * @return true if current day is the first day of the month
     */
    public boolean isFirstDayOfMonth() {
        return getDayOfMonth() == 1;
    }

    /**
     * Get the number of days remaining in the current month
     * @return days remaining in month
     */
    public int getDaysRemainingInMonth() {
        return getDaysInMonth() - getDayOfMonth();
    }

    /**
     * Get the number of days passed in the current month
     * @return days passed in month
     */
    public int getDaysPassedInMonth() {
        return getDayOfMonth() - 1;
    }


    /**
     * Get current Persian date as a formatted string (for Android UI)
     * @param pattern Date format pattern (e.g., "yyyy/MM/dd", "yyyy-MM-dd", "dd MMMM yyyy")
     * @return Formatted date string
     */
    public String getFormattedDate(String pattern) {
        complete(); // Ensure all fields are computed

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
                // For custom patterns, use MyPersianDateFormat
                MyPersianDateFormat formatter = new MyPersianDateFormat(pattern);
                return formatter.format(this);
        }
    }

    /**
     * Get formatted date with Persian/Farsi numbers
     * @param pattern Date format pattern
     * @param usePersianNumbers Whether to use Persian numbers (۱۲۳) instead of Arabic (123)
     * @return Formatted date string
     */
    public String getFormattedDate(String pattern, boolean usePersianNumbers) {
        String result = getFormattedDate(pattern);

        if (usePersianNumbers) {
            // Convert Western numbers to Persian numbers
            result = convertToPersianNumbers(result);
        }

        return result;
    }

    /**
     * Convert Western numbers (0-9) to Persian numbers (۰-۹)
     */
    private String convertToPersianNumbers(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        char[]        persianDigits = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};
        StringBuilder result        = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') {
                result.append(persianDigits[c - '0']);
            } else {
                result.append(c);
            }
        }

        return result.toString();
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
    public int compareTo(MyPersianCalendar other) {
        if (other == null) return 1;
        return Long.compare(this.getTimeInMillis(), other.getTimeInMillis());
    }

}