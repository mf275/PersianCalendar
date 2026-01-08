package com.farashian.pcalendar.fast;

import com.farashian.pcalendar.YMD;
import com.farashian.pcalendar.util.HijriConverter;
import com.farashian.pcalendar.PCConstants;
import com.farashian.pcalendar.util.PCalendarUtils;

import java.util.*;

import static com.farashian.pcalendar.util.HijriConverter.*;
import static com.farashian.pcalendar.PCConstants.*;
import static com.farashian.pcalendar.util.PCalendarUtils.*;


/**
 * High-performance Persian Calendar with complete field computation
 * Fixed all field computation issues and critical bugs
 */
public class FastPersianCalendar extends Calendar {

    public static final int FIRST_DAY_OF_WEEK      = Calendar.SATURDAY;
    public static final int WEEKDAY_HOLIDAY_NUMBER = Calendar.FRIDAY;

    //Calendar constants we need to define
    private static final int AD = 1; //Gregorian era constant

    //Month constants (INTERNAL 0-BASED)
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

    //Fast internal storage
    private int persianYear;
    private int persianMonth; // 0-based INTERNAL
    private int persianDay;

    //Performance optimizations
    public final  GregorianCalendar gCal;
    private final Locale            locale;
    private       long              lastComputedTime = -1;
    private       boolean           isDirty          = true;

    //Persian offsets (Persian week starts on Saturday)
    //Java Calendar constants: SUNDAY=1, MONDAY=2, ..., SATURDAY=7
    //We map: Saturday=0, Sunday=1, Monday=2, Tuesday=3, Wednesday=4, Thursday=5, Friday=6
    private static final int[] PERSIAN_OFFSETS = {0, 1, 2, 3, 4, 5, 6, 0};


    public FastPersianCalendar() {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setTimeInMillis(System.currentTimeMillis());
    }

    public FastPersianCalendar(TimeZone zone) {
        this(zone, PERSIAN_LOCALE);
        setTimeInMillis(System.currentTimeMillis());
    }

    public FastPersianCalendar(Date date) {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setTimeInMillis(date.getTime());
    }

    public FastPersianCalendar(long timeStamp) {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setTimeInMillis(timeStamp);
    }

    public FastPersianCalendar(TimeZone zone, Locale locale) {
        super(zone, locale);
        this.locale = locale;
        this.gCal   = new GregorianCalendar(zone, locale);
        //Initialize with reasonable defaults
        this.persianYear  = 1400;
        this.persianMonth = 0;
        this.persianDay   = 1;
    }

    public FastPersianCalendar(FastPersianCalendar pc) {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setPersianDateInternal(pc.getYear(), pc.getMonth() - 1, pc.getDayOfMonth());
    }

    // ✅ PUBLIC CONSTRUCTORS: 1-based month (1=Farvardin, 12=Esfand)
    public FastPersianCalendar(int year, int month, int dayOfMonth) {
        this();
        setPersianDateInternal(year, month - 1, dayOfMonth);
    }

    public FastPersianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        this(year, month, dayOfMonth, hourOfDay, minute, 0);
    }

    public FastPersianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        this();
        setPersianDateInternal(year, month - 1, dayOfMonth);
        //Set time fields directly
        setInternalField(HOUR_OF_DAY, hourOfDay);
        setInternalField(MINUTE, minute);
        setInternalField(SECOND, second);
        isDirty = true;
    }


    /**
     * Constructor that accepts Java GregorianCalendar object
     * @param gregorianCalendar GregorianCalendar object
     */
    public FastPersianCalendar(GregorianCalendar gregorianCalendar) {
        this(); //Initialize with default constructor

        if (gregorianCalendar == null) {
            throw new IllegalArgumentException("GregorianCalendar cannot be null");
        }

        //Get date components from GregorianCalendar
        int year   = gregorianCalendar.get(Calendar.YEAR);
        int month  = gregorianCalendar.get(Calendar.MONTH);
        int day    = gregorianCalendar.get(Calendar.DAY_OF_MONTH);
        int hour   = gregorianCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = gregorianCalendar.get(Calendar.MINUTE);
        int second = gregorianCalendar.get(Calendar.SECOND);

        //Validate Gregorian date
        validateGregorianDate(year, month+1, day);

        //Set Gregorian date and convert to Persian
        setGregorianDate(year, month+1, day);
        set(HOUR_OF_DAY, hour);
        set(MINUTE, minute);
        set(SECOND, second);

        //Also set the timezone from the provided GregorianCalendar
        setTimeZone(gregorianCalendar.getTimeZone());
    }

    /**
     * Constructor that accepts a Calendar object (can be any Calendar type)
     * @param calendar Any Calendar object (Gregorian, Persian, etc.)
     */
    public FastPersianCalendar(Calendar calendar) {
        this(); //Initialize with default constructor

        if (calendar == null) {
            throw new IllegalArgumentException("Calendar cannot be null");
        }

        if (calendar instanceof GregorianCalendar) {
            //Direct conversion from GregorianCalendar
            GregorianCalendar gCal = (GregorianCalendar) calendar;
            setTimeInMillis(gCal.getTimeInMillis());
        } else if (calendar instanceof FastPersianCalendar) {
            //Copy from another FastPersianCalendar
            FastPersianCalendar other = (FastPersianCalendar) calendar;
            this.persianYear  = other.persianYear;
            this.persianMonth = other.persianMonth;
            this.persianDay   = other.persianDay;
            setTimeInMillis(other.getTimeInMillis());
        } else {
            //Generic Calendar - use time in millis
            setTimeInMillis(calendar.getTimeInMillis());
        }

        //Set timezone
        setTimeZone(calendar.getTimeZone());
    }


    public int getHourOfDay() {
        complete();
        return get(HOUR_OF_DAY);
    }

    public int getMinute() {
        complete();
        return get(MINUTE);
    }

    public int getSecond() {
        complete();
        return get(SECOND);
    }

    public void setPersianDate(int year, int month, int day, int hourOfDay, int minute, int second) {
        setPersianDate(year, month, day);
        set(HOUR_OF_DAY, hourOfDay);
        set(MINUTE, minute);
        set(SECOND, second);
    }

    public FastPersianCalendar copy() {
        return new FastPersianCalendar(this);
    }

    public FastPersianCalendar newInstance() {
        return new FastPersianCalendar();
    }

    public Date getDate() {
        return new Date(getTimeInMillis());
    }

    public void setDate(Date date) {
        setTimeInMillis(date.getTime());
    }

    public static FastPersianCalendar gregorianToPersian(int gYear, int gMonth, int gDay) {
        FastPersianCalendar result = new FastPersianCalendar();
        result.setGregorianDate(gYear, gMonth, gDay);
        return result;
    }

    /**
     * Create FastPersianCalendar from Gregorian date string
     */
    public static FastPersianCalendar fromGregorianStringYmd(String dateString) {
        return fromGregorianStringYmd(dateString, "-");
    }

    public static FastPersianCalendar fromGregorianStringYmd(String dateString, String delimiter) {
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
            int month = Integer.parseInt(parts[1].trim());
            int day   = Integer.parseInt(parts[2].trim());

            return new FastPersianCalendar(year, month, day);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in date string: " + dateString, e);
        }
    }


    public int getYear() {
        ensureComputed();
        return persianYear;
    }

    /**
     * ✅ PUBLIC API: 1-based month (1=Farvardin, 12=Esfand)
     */
    public int getMonth() {
        ensureComputed();
        return persianMonth + 1;
    }

    public int getDay() {
        ensureComputed();
        return persianDay;
    }

    public int getDayOfMonth() {
        ensureComputed();
        return persianDay;
    }

    public int getDaysInMonth() {
        return getDaysInMonth(getYear(), getMonth());
    }

    /**
     * ✅ PUBLIC API: month is 1-based (1=Farvardin, 12=Esfand)
     */
    public int getDaysInMonth(int year, int month) {
        // Convert 1-based month to 0-based for internal calculation
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }
        int month0Based = month - 1;

        if (month0Based < 6) {
            return 31;
        } else if (month0Based < 11) {
            return 30;
        } else {
            return isLeapYear(year) ? 30 : 29;
        }
    }

    private int getDaysInMonthInternal(int year, int month) {
        int month0Based = month - 1;
        if (month0Based < 6) return 31;
        if (month0Based < 11) return 30;
        return isLeapYear(year) ? 30 : 29;
    }

    /**
     * ✅ PUBLIC API: month is 1-based (1=Farvardin, 12=Esfand)
     */
    public static int getDaysInMonthStatic(int year, int month) {
        // Convert 1-based month to 0-based for internal calculation
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }
        int month0Based = month - 1;

        if (month0Based < 6) {
            return 31;
        } else if (month0Based < 11) {
            return 30;
        } else {
            return isLeapYear(year) ? 30 : 29;
        }
    }

    public static int getMaximumMonthWeeks() {
        return 6;
    }

    //Date formatter methods
    public String getMonthName() {
        return getMonthName(getMonth(), locale);
    }

    public int getPersianYear() {
        return getYear();
    }

    /**
     * ✅ PUBLIC API: 1-based month (1=Farvardin, 12=Esfand)
     */
    public int getPersianMonth() {
        return getMonth();
    }

    public int getPersianDay() {
        return getDayOfMonth();
    }

    public String getWeekdayName() {
        return getWeekdayName(get(DAY_OF_WEEK), locale);
    }

    public String getLongDate() {
        return getLongDate(
                persianYear, persianMonth, persianDay, get(DAY_OF_WEEK), locale);
    }

    public String getLongDateTime() {
        return getLongDateTime(
                persianYear, persianMonth, persianDay, get(DAY_OF_WEEK),
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
        return getShortDate(persianYear, persianMonth, persianDay, delimiter, locale);
    }

    /**
     * Get Islamic (Hijri) year
     * @return Hijri date
     */
    public YMD getHijriDate() {
        return gregorianToHijri(gCal);
    }

    /**
     * Set the date using Hijri (Hijri) calendar values
     * @param hYear Hijri year
     * @param hMonth Hijri month (1-12)
     * @param hDay Hijri day of month
     * @param timeZone Target timezone for the date
     */
    public void setHijriDate(int hYear, int hMonth, int hDay, TimeZone timeZone) {
        //Convert Hijri date to Gregorian using HijriConverter
        GregorianCalendar gCalendar = hijriToGregorian(hYear, hMonth, hDay, timeZone);

        //Adjust to target timezone
        long timeInMillis = gCalendar.getTimeInMillis();
        GregorianCalendar adjustedCalendar = new GregorianCalendar(timeZone);
        adjustedCalendar.setTimeInMillis(timeInMillis);

        //Set the Gregorian date
        setGregorianDate(adjustedCalendar.get(Calendar.YEAR),
                         adjustedCalendar.get(Calendar.MONTH),
                         adjustedCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public void setHijriDate(int hYear, int hMonth, int hDay) {
        GregorianCalendar gCalendar = hijriToGregorian(hYear, hMonth, hDay);
        //gCalendar is in Tehran timezone - EXTRACT Tehran date
        setGregorianDate(gCalendar.get(Calendar.YEAR),
                         gCalendar.get(Calendar.MONTH),
                         gCalendar.get(Calendar.DAY_OF_MONTH));
    }


    /**
     * Find the Gregorian date corresponding to the first day (Hijri day 1)
     * of a given Hijri month and year.
     *
     * @param islamicYear   Hijri year (e.g., 1446)
     * @param islamicMonth Hijri month (1-based: 1 = Muharram, 2 = Safar, ..., 12 = Dhu al-Hijjah)
     * @return Calendar object representing the Gregorian date of the first day of the Hijri month
     */
    public static Calendar findFirstDayOfHijriMonth(int islamicYear, int islamicMonth) {
        //Use existing hijriToGregorian method to convert to Gregorian
        return hijriToGregorian(islamicYear, islamicMonth, 1);
    }

    /**
     * ✅ PUBLIC API: month is 1-based (1=Farvardin, 12=Esfand)
     */
    public void setPersianDate(int year, int month, int day) {
        validatePersianDate(year, month, day);
        setPersianDateInternal(year, month - 1, day);
    }


    /**
     * Converts Java Calendar weekday constant to Persian calendar offset.
     * Persian week starts on Saturday (0), while Java Calendar uses Sunday-based constants.
     *
     * @param javaDayOfWeek Java Calendar weekday constant (Calendar.SUNDAY to Calendar.SATURDAY)
     * @return Persian offset (0-6) where 0=Saturday, 1=Sunday, ..., 6=Friday
     */

    public int calculatePersianOffset(int javaDayOfWeek) {
        if (javaDayOfWeek < 1 || javaDayOfWeek > 7) return 0;
        return PERSIAN_OFFSETS[javaDayOfWeek];
    }

    /**
     * Calculate offset for Gregorian calendar (US convention: Sunday-first week)
     * Returns 0-6 where 0=Sunday, 1=Monday, ..., 6=Saturday
     */
    public int calculateGregorianOffset(int javaDayOfWeek) {
        if (javaDayOfWeek < 1 || javaDayOfWeek > 7) return 0;
        return javaDayOfWeek - 1;
    }

    /**
     * Calculate offset for Gregorian calendar (ISO convention: Monday-first week)
     * Returns 0-6 where 0=Monday, 1=Tuesday, ..., 6=Sunday
     */
    public int calculateGregorianOffsetISO(int javaDayOfWeek) {
        if (javaDayOfWeek < 1 || javaDayOfWeek > 7) return 0;
        return (javaDayOfWeek + 5) % 7;
    }

    public void parse(String dateString) {
        FastPersianCalendar parsed = parseOrNullToCompat(dateString);
        if (parsed != null) {
            //Create fresh instance instead of copying internal state
            setPersianDateInternal(parsed.persianYear, parsed.persianMonth, parsed.persianDay);
            //Copy time fields if they exist
            if (parsed.isSet[HOUR_OF_DAY]) set(HOUR_OF_DAY, parsed.get(HOUR_OF_DAY));
            if (parsed.isSet[MINUTE]) set(MINUTE, parsed.get(MINUTE));
            if (parsed.isSet[SECOND]) set(SECOND, parsed.get(SECOND));
        }
    }

    public static boolean isLeapYear(int year) {
        int remainder = year % 33;
        return remainder == 1 || remainder == 5 || remainder == 9 ||
               remainder == 13 || remainder == 17 || remainder == 22 ||
               remainder == 26 || remainder == 30;
    }

    @Override
    protected void computeTime() {
        if (isDirty || lastComputedTime != time || !areFieldsSet) {
            computeGregorianFromPersianFast();
            time             = gCal.getTimeInMillis();
            lastComputedTime = time;
            isDirty          = false;

            //Invalidate fields since time changed
            areFieldsSet = false;
        }
    }

    @Override
    protected void computeFields() {
        if (time != lastComputedTime || !areFieldsSet) {
            gCal.setTimeInMillis(time); //This is needed to sync gCal with current time
            computePersianFromGregorianFast();
            lastComputedTime = time;
            isDirty          = false;

            //Compute all calendar fields
            computeAllFields();
        }
    }


    /**
     * Compute all calendar fields that parent class expects
     * Proper field computation with Persian week calculations
     */
    private void computeAllFields() {
        //Use Gregorian calendar to compute all fields reliably
        gCal.setTimeInMillis(time);

        //Set basic Persian date fields
        fields[YEAR]         = persianYear;
        fields[MONTH]        = persianMonth;
        fields[DAY_OF_MONTH] = persianDay;

        //Set time fields from Gregorian (they're the same)
        fields[HOUR_OF_DAY] = gCal.get(HOUR_OF_DAY);
        fields[MINUTE]      = gCal.get(MINUTE);
        fields[SECOND]      = gCal.get(SECOND);
        fields[MILLISECOND] = gCal.get(MILLISECOND);

        //CRITICAL FIX: Use Gregorian's properly computed day-of-week
        //Get Gregorian day of week
        int gregorianDayOfWeek = gCal.get(DAY_OF_WEEK);

        //Convert to Persian day of week using your offset
        int persianOffset = calculatePersianOffset(gregorianDayOfWeek);

        //Convert offset to Calendar constant
        //Your PERSIAN_OFFSETS: Saturday=0, Sunday=1, Monday=2, Tuesday=3,
        //Wednesday=4, Thursday=5, Friday=6
        //Calendar expects: Sunday=1, Monday=2, ..., Saturday=7

        //Map: Saturday(0)→7, Sunday(1)→1, Monday(2)→2, Tuesday(3)→3,
        //Wednesday(4)→4, Thursday(5)→5, Friday(6)→6
        int persianDayOfWeek;
        if (persianOffset == 0) {
            persianDayOfWeek = Calendar.SATURDAY;  //7
        } else {
            persianDayOfWeek = persianOffset;      //1-6
        }

        fields[DAY_OF_WEEK] = persianDayOfWeek;

        //Calculate proper Persian day of year and week fields
        fields[DAY_OF_YEAR] = calculateDayOfYear();
        calculatePersianWeekFields();

        //Set other fields
        fields[AM_PM]       = gCal.get(AM_PM);
        fields[HOUR]        = gCal.get(HOUR);
        fields[DST_OFFSET]  = gCal.get(DST_OFFSET);
        fields[ZONE_OFFSET] = gCal.get(ZONE_OFFSET);

        //ERA is always AD for Persian calendar (modern dates)
        fields[ERA] = AD;

        //Mark all fields as set
        for (int i = 0; i < FIELD_COUNT; i++) {
            isSet[i] = true;
        }
        areFieldsSet = true;
    }

    /**
     * Calculate Persian week fields based on Persian calendar rules
     */
    private void calculatePersianWeekFields() {
        int dayOfYear = fields[DAY_OF_YEAR];
        int dayOfWeek = fields[DAY_OF_WEEK];

        //Calculate week of year (Persian year starts with Farvardin 1 = Saturday)
        //Use the actual first day of week (Saturday for Persian)
        int weekOfYear = (dayOfYear - 1 + ((dayOfWeek - FIRST_DAY_OF_WEEK + 7) % 7)) / 7 + 1;
        fields[WEEK_OF_YEAR] = weekOfYear;

        //Calculate week of month
        int dayOfMonth      = fields[DAY_OF_MONTH];
        int firstDayOfMonth = calculateFirstDayOfMonth();
        int weekOfMonth     = (dayOfMonth - 1 + ((dayOfWeek - firstDayOfMonth + 7) % 7)) / 7 + 1;
        fields[WEEK_OF_MONTH] = weekOfMonth;

        //Calculate day of week in month
        fields[DAY_OF_WEEK_IN_MONTH] = (dayOfMonth - 1) / 7 + 1;
    }

    /**
     * Calculate the day of week for the first day of the current month
     * Now uses local arrays and doesn't modify object state
     */
    private int calculateFirstDayOfMonth() {
        //Use local array for conversion
        int[] temp = new int[3];

        //Convert first day of current month from Persian to Gregorian
        //Note: persianMonth is 0-based, add 1 for the algorithm
        jalaliToGregorianFast(persianYear, persianMonth + 1, 1, temp);

        //Create temporary GregorianCalendar for calculation
        GregorianCalendar tempCal = new GregorianCalendar(getTimeZone(), locale);
        tempCal.set(temp[0], temp[1] - 1, temp[2]);

        //Get Gregorian day of week
        int gregorianDayOfWeek = tempCal.get(DAY_OF_WEEK);

        //Convert to Persian day of week
        int persianOffset = calculatePersianOffset(gregorianDayOfWeek);

        //Map to Calendar constant
        if (persianOffset == 0) {
            return Calendar.SATURDAY;  //7
        } else {
            return persianOffset;      //1-6
        }
    }

    /**
     * Calculate day of year for Persian calendar
     */
    private int calculateDayOfYear() {
        int dayOfYear = 0;
        for (int month = 0; month < persianMonth; month++) {
            dayOfYear += getDaysInMonth(persianYear, month + 1);
        }
        dayOfYear += persianDay;
        return dayOfYear;
    }

    /**
     * Convert Persian date to Gregorian
     * Now uses local arrays instead of ThreadLocal to avoid thread safety issues
     */
    private void computeGregorianFromPersianFast() {
        int[] temp = new int[3];  //Local array - thread safe!

        //Convert 0-based month (0-11) to 1-based month (1-12) for the algorithm
        jalaliToGregorianFast(persianYear, persianMonth + 1, persianDay, temp);

        //Get current time fields before setting date
        int hour   = internalGet(HOUR_OF_DAY, 0);
        int minute = internalGet(MINUTE, 0);
        int second = internalGet(SECOND, 0);
        int millis = internalGet(MILLISECOND, 0);

        //temp[1] is 1-based Gregorian month, convert to 0-based for GregorianCalendar
        gCal.set(temp[0], temp[1] - 1, temp[2], hour, minute, second);
        gCal.set(MILLISECOND, millis);
    }

    /**
     * Convert Gregorian date to Persian
     * Now uses local arrays instead of ThreadLocal to avoid thread safety issues
     */
    private void computePersianFromGregorianFast() {
        int[] temp = new int[3];  //Local array - thread safe!

        //Convert Gregorian 0-based month to 1-based for the algorithm
        gregorianToJalaliFast(gCal.get(Calendar.YEAR), gCal.get(Calendar.MONTH) + 1,
                              gCal.get(Calendar.DAY_OF_MONTH), temp);

        //The algorithm returns 1-based month, convert to 0-based for internal storage
        persianYear  = temp[0];
        persianMonth = temp[1] - 1;  //Convert 1-based to 0-based
        persianDay   = temp[2];
    }

    /**
     * Debug method to check current state
     */
    public String debugInfo() {
        ensureComputed();
        return String.format(Locale.US,
                             "Persian: %d/%d/%d, Gregorian: %d/%d/%d, Month Name: %s, Day of Week: %s",
                             persianYear, persianMonth + 1, persianDay,
                             gCal.get(Calendar.YEAR), gCal.get(Calendar.MONTH) + 1, gCal.get(Calendar.DAY_OF_MONTH),
                             getMonthName(), getWeekdayName());
    }

    private void ensureComputed() {
        if (isDirty || time != lastComputedTime || !areFieldsSet) {
            computeFields();
        }
    }

    @Override
    public void add(int field, int amount) {
        if (amount == 0) return;
        if (field < 0 || field >= ZONE_OFFSET) return;

        complete();

        //For Persian date fields, use direct Persian math
        if (field == YEAR || field == MONTH || field == DAY_OF_MONTH) {
            handlePersianDateArithmetic(field, amount);
        } else {
            //For other fields, use Gregorian but optimized
            computeTime();
            gCal.add(field, amount);
            time = gCal.getTimeInMillis();
            computePersianFromGregorianFast();
            areFieldsSet = false;
        }
    }

    public void addDays(int days) {
        add(DAY_OF_MONTH, days);
    }

    /**
     * Handle Persian date arithmetic with proper day adjustment
     */
    private void handlePersianDateArithmetic(int field, int amount) {
        switch (field) {
            case YEAR:
                persianYear += amount;
                if (persianYear < 1) persianYear = 1;
                adjustDayForNewMonth();
                break;
            case MONTH:
                int totalMonths = persianYear * 12 + persianMonth + amount;
                if (totalMonths >= 0) {
                    persianYear  = totalMonths / 12;
                    persianMonth = totalMonths % 12;
                } else {
                    int yearsToSubtract = (-totalMonths + 11) / 12;
                    persianYear -= yearsToSubtract;
                    if (persianYear < 1) persianYear = 1;
                    persianMonth = (totalMonths % 12 + 12) % 12;
                }
                adjustDayForNewMonth();
                break;
            case DAY_OF_MONTH:
                persianDay += amount;
                normalizeDate();
                break;
        }

        //Recompute Gregorian date and sync
        computeGregorianFromPersianFast();
        setTimeInMillis(gCal.getTimeInMillis());
        isDirty          = true;
        lastComputedTime = -1;
        areFieldsSet     = false;
    }

    /**
     * Normalize date by handling overflow/underflow of days
     */
    private void normalizeDate() {
        //Handle positive overflow
        while (persianDay > getDaysInMonth(persianYear, persianMonth + 1)) {
            persianDay -= getDaysInMonth(persianYear, persianMonth + 1);
            persianMonth++;
            if (persianMonth > 11) {
                persianMonth = 0;
                persianYear++;
            }
        }

        //Handle negative underflow
        while (persianDay < 1) {
            persianMonth--;
            if (persianMonth < 0) {
                persianMonth = 11;
                persianYear--;
                if (persianYear < 1) persianYear = 1;
            }
            persianDay += getDaysInMonth(persianYear, persianMonth + 1);
        }
    }

    /**
     * Adjust day when month or year changes to ensure valid date
     */
    private void adjustDayForNewMonth() {
        int maxDays = getDaysInMonth(persianYear, persianMonth + 1);
        if (persianDay > maxDays) {
            persianDay = maxDays;
        }
    }

    @Override
    public void roll(int field, boolean up) {
        complete();

        if (field == YEAR) {
            persianYear += up ? 1 : -1;
            if (persianYear <= 0) persianYear = 1;
            adjustDayForNewMonth();
        } else if (field == MONTH) {
            if (up) {
                if (persianMonth == 11) {
                    persianMonth = 0;
                    persianYear++;
                } else {
                    persianMonth++;
                }
            } else {
                if (persianMonth == 0) {
                    persianMonth = 11;
                    persianYear--;
                    if (persianYear < 1) persianYear = 1;
                } else {
                    persianMonth--;
                }
            }
            adjustDayForNewMonth();
        } else if (field == DAY_OF_MONTH) {
            int maxDays = getDaysInMonth(persianYear, persianMonth + 1);
            if (up) {
                if (persianDay == maxDays) {
                    persianDay = 1;
                } else {
                    persianDay++;
                }
            } else {
                if (persianDay == 1) {
                    persianDay = maxDays;
                } else {
                    persianDay--;
                }
            }
        } else {
            computeTime();
            gCal.roll(field, up);
            time = gCal.getTimeInMillis();
            computePersianFromGregorianFast();
        }

        //Recompute Gregorian date and sync
        computeGregorianFromPersianFast();
        setTimeInMillis(gCal.getTimeInMillis());
        isDirty          = true;
        lastComputedTime = -1;
        areFieldsSet     = false;
    }

    //=== OVERRIDES (FIXED) ===

    public void setNewTime(Date date) {
        setTimeInMillis(date.getTime());
    }

    @Override
    public void setTimeInMillis(long millis) {
        super.setTimeInMillis(millis);

        //Sync gCal with the new time
        gCal.setTimeInMillis(millis);

        //Force recomputation
        lastComputedTime = -1;
        isDirty          = true;
        areFieldsSet     = false;

        //Compute fields immediately
        computeFields();
    }


    @Override
    public void setTimeZone(TimeZone zone) {
        super.setTimeZone(zone);
        gCal.setTimeZone(zone);
        lastComputedTime = -1;
        isDirty          = true;
        areFieldsSet     = false;
    }

    @Override
    public void set(int field, int value) {
        //Handle setting Persian fields specially
        switch (field) {
            case YEAR:
                if (value < 1 || value > 9999) {
                    throw new IllegalArgumentException("Year must be between 1 and 9999, got: " + value);
                }
                persianYear = value;
                adjustDayForNewMonth();
                break;
            case MONTH:
                if (value < 0 || value > 11) {
                    throw new IllegalArgumentException("Month must be between 0 and 11, got: " + value);
                }
                persianMonth = value;
                adjustDayForNewMonth();
                break;
            case DAY_OF_MONTH:
                int maxDays = getDaysInMonth(persianYear, persianMonth + 1);
                if (value < 1 || value > maxDays) {
                    throw new IllegalArgumentException("Day must be between 1 and " + maxDays + ", got: " + value);
                }
                persianDay = value;
                break;
            default:
                //For other fields, use standard mechanism
                super.set(field, value);
                return;
        }

        //Recompute Gregorian date and sync everything
        computeGregorianFromPersianFast();
        setTimeInMillis(gCal.getTimeInMillis());
        areFieldsSet     = false;
        lastComputedTime = -1;
    }

    //=== VALIDATION METHODS ===


    @Override
    public String toString() {
        return getLongDate();
    }

    //=== FAST CONVERSION ALGORITHMS ===

    public static void gregorianToJalaliFast(int gy, int gm, int gd, int[] out) {
        int jy = (gm > 2) ? (gy + 1) : gy;

        //Fast month day offsets
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

        //Fast month calculation
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

    //=== INTERNAL HELPERS ===

    private void setPersianDateInternal(int year, int month, int day) {
        this.persianYear      = year;
        this.persianMonth     = month;
        this.persianDay       = day;
        this.isDirty          = true;
        this.lastComputedTime = -1;
        this.areFieldsSet     = false;

        computeGregorianFromPersianFast();
        setTimeInMillis(gCal.getTimeInMillis());
    }

    private void setInternalField(int field, int value) {
        fields[field] = value;
        isSet[field]  = true;
    }

    private int internalGet(int field, int defaultValue) {
        return isSet[field] ? fields[field] : defaultValue;
    }

    //=== REQUIRED ABSTRACT METHODS ===

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
            case HOUR_OF_DAY:
                return 0;
            case MINUTE:
                return 0;
            case SECOND:
                return 0;
            case MILLISECOND:
                return 0;
            case ERA:
                return AD;
            default:
                return 0;
        }
    }

    @Override
    public int getMaximum(int field) {
        switch (field) {
            case YEAR:
                return 9999;
            case MONTH:
                return ESFAND;
            case DAY_OF_MONTH:
                return 31;
            case DAY_OF_WEEK:
                return SATURDAY;
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
            case WEEK_OF_MONTH:
                return 6;
            case WEEK_OF_YEAR:
                return 53;
            case AM_PM:
                return PM;
            case ERA:
                return AD; //1
            case ZONE_OFFSET:
                return 14 * 60 * 60 * 1000; //14 hours in milliseconds
            case DST_OFFSET:
                return 2 * 60 * 60 * 1000; //2 hours DST
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
                //Weeks start on Saturday in Persian calendar
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

    //Helper method to get first day of month
    private int getFirstDayOfMonth() {
        return calculateFirstDayOfMonth();
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

    //=== COMPATIBILITY METHODS ===

    /**
     * For backward compatibility - returns current state as array
     */
    public YMD getYmd() {
        ensureComputed();
        return new YMD(persianYear, persianMonth + 1, persianDay);
    }

    /**
     * Fast method to get day of week (1-7, where 1=Sunday, 7=Saturday)
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

        FastPersianCalendar that = (FastPersianCalendar) obj;
        return this.time == that.time &&
               this.persianYear == that.persianYear &&
               this.persianMonth == that.persianMonth &&
               this.persianDay == that.persianDay &&
               this.getTimeZone().equals(that.getTimeZone());
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, persianYear, persianMonth, persianDay, getTimeZone());
    }

    //=== STATIC UTILITY METHODS ===

    public static FastPersianCalendar parseOrNullToCompat(String dateString) {
        return parseOrNullToCompat(dateString, "/");
    }

    public static FastPersianCalendar parseOrNullToCompat(String dateString, String delimiter) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        dateString = convertToEnglishNumbers(dateString);

        //Auto-detect delimiter
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
            int month = Integer.parseInt(tokens[1].trim());
            int day   = Integer.parseInt(tokens[2].trim());

            //Basic validation
            if (year < 1 || month < 1 || month > 12 || day < 1 || day > 31) {
                return null;
            }

            //Additional validation for actual month length
            int maxDays = (new FastPersianCalendar()).getDaysInMonth(year, month);
            if (day > maxDays) {
                return null;
            }

            return new FastPersianCalendar(year, month, day);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getWeekdayName(int dayOfWeek, Locale locale) {
        int index = (dayOfWeek - 1) % 7;
        if (index < 0) index += 7;

        return locale.getLanguage().equals("fa")
                ? PCConstants.WEEKDAY_NAMES[index]
                : PCConstants.WEEKDAY_NAMES_SHORT_IN_ENGLISH[index];
    }

    /**
     * @param month 1-based Persian month (1=Farvardin)
     */
    public static String getMonthName(int month, Locale locale) {
        return PCalendarUtils.getPersianMonthName(month, locale);
    }

    public static String getMonthName(int month) {
        Locale locale1 = getLocaleFromTimezone();
        return PCalendarUtils.getPersianMonthName(month, locale1);
    }

    public String getMonthNameShort() {
        return getMonthNameShort(getMonth(), locale);

    }

    /**
     * @param month 1-based Persian month (1=Farvardin)
     */
    public static String getMonthNameShort(int month, Locale locale) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        return locale.getLanguage().equals("fa")
                ? PCConstants.PERSIAN_MONTH_NAMES_SHORT[month-1]
                : PCConstants.PERSIAN_MONTH_NAMES_ENGLISH_SHORT[month-1];
    }

    /**
     * Get Gregorian year from the underlying GregorianCalendar
     * @return Gregorian year
     */
    public int getGrgYear() {
        complete();
        return gCal.get(Calendar.YEAR);
    }

    /**
     * Get Gregorian month (1-based: 1=January, 12=December)
     * @return Gregorian month (1-12)
     */
    public int getGrgMonth() {
        complete();
        return gCal.get(Calendar.MONTH) +1;
    }


    /**
     * Get Gregorian day of month (1-31)
     * @return Gregorian day of month
     */
    public int getGrgDay() {
        complete();
        return gCal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get Gregorian week of year (ISO 8601 week numbering)
     * @return Gregorian week of year (1-53)
     */
    public int getGrgWeekOfYear() {
        complete();
        return gCal.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Get Gregorian week of month
     * @return Gregorian week of month (1-6)
     */
    public int getGrgWeekOfMonth() {
        complete();
        return gCal.get(Calendar.WEEK_OF_MONTH);
    }

    /**
     * Get Gregorian day of week (Calendar.SUNDAY=1, Calendar.SATURDAY=7)
     * @return Gregorian day of week
     */
    public int getGrgDayOfWeek() {
        complete();
        return gCal.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * Get Gregorian day of week name
     * @param locale locale for weekday name
     * @return Gregorian weekday name
     */
    public String getGrgDayOfWeekName(Locale locale) {
        complete();
        int dayOfWeek = gCal.get(Calendar.DAY_OF_WEEK);

        //Map to Persian names if needed
        if (locale.getLanguage().equals("fa")) {
            String[] persianWeekdays = {"یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه",
                    "پنجشنبه", "جمعه", "شنبه"};
            //Calendar.DAY_OF_WEEK: 1=SUNDAY, 2=MONDAY, ..., 7=SATURDAY
            return persianWeekdays[dayOfWeek - 1];
        } else {
            return gCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale);
        }
    }

    /**
     * Get Gregorian month name
     * @param locale locale for month name
     * @return Gregorian month name
     */
    public String getGrgMonthName(Locale locale) {
        complete();
        return gCal.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
    }


    /**
     * Get Gregorian month length (days in month)
     * @return number of days in current Gregorian month
     */
    public int getGrgMonthLength() {
        complete();
        return gCal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get Gregorian month0 length for specific year and month0
     * @param year Gregorian year
     * @param month 1-based Gregorian month (1=January)
     * @return number of days in the month
     */
    public static int getGrgMonthLength(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        int month0 = month -1;

        //Fast switch statement
        switch (month0) {
            case 0:  //January
            case 2:  //March
            case 4:  //May
            case 6:  //July
            case 7:  //August
            case 9:  //October
            case 11: //December
                return 31;
            case 3:  //April
            case 5:  //June
            case 8:  //September
            case 10: //November
                return 30;
            case 1:  //February
                //Leap year calculation
                return ((year % 4 == 0) && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
            default:
                return 31;
        }
    }

    /**
     * Get Gregorian month name (fast version without Calendar)
     * @param month 1-based Gregorian month (1=January)
     * @param locale locale for month name
     * @return month name
     */
    public static String getGrgMonthNameFast(int month, Locale locale) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        int month0 = month -1;
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
     * @param locale locale for month name
     * @return Gregorian month short name
     */
    public String getGrgMonthShortName(Locale locale) {
        complete();
        return gCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale);
    }

    /**
     * Get Gregorian month short name (3-letter abbreviation)
     * @param month 1-based Gregorian month (1=January)
     * @param locale locale for month name
     * @return month short name
     */
    public static String getGrgMonthShortName(int month, Locale locale) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }

        int month0 = month -1;

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
     * @param month 1-based Gregorian month (1=January)
     * @param locale locale for month name
     * @return Gregorian month name
     */
    public static String getGrgMonthNameStatic(int month, Locale locale) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }

        int month0 = month -1;
        //Create a temporary calendar just for getting the month name
        Calendar temp = new GregorianCalendar(locale);
        temp.set(Calendar.MONTH, month0);
        temp.set(Calendar.DAY_OF_MONTH, 1);
        return temp.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
    }


    /**
     * Get Gregorian month name with number (e.g., "January (01)")
     * @param locale locale for month name
     * @return formatted month name with number
     */
    public String getGrgMonthNameWithNumber(Locale locale) {
        complete();
        String monthName   = getGrgMonthName(locale);
        int    monthNumber = getGrgMonth(); //Convert to 1-based
        return String.format(locale, "%s (%02d)", monthName, monthNumber);
    }

    /**
     * Convert Gregorian date to Persian date
     * @param year Gregorian year
     * @param month 1-based Gregorian month (1=January)
     * @param day Gregorian day
     * @return Persian date as FastPersianCalendar
     */
    public static FastPersianCalendar fromGregorian(int year, int month, int day) {
        FastPersianCalendar result = new FastPersianCalendar();
        result.setGregorianDate(year, month, day);
        return result;
    }

    /**
     * Convert Gregorian date to Persian date (1-based month)
     * @param year Gregorian year
     * @param month1Based 1-based Gregorian month (1=January)
     * @param day Gregorian day
     * @return Persian date as FastPersianCalendar
     */
    public static FastPersianCalendar fromGregorian1Based(int year, int month1Based, int day) {
        return fromGregorian(year, month1Based - 1, day);
    }

    /**
     * Get current Gregorian date
     * @return current Gregorian date as FastPersianCalendar
     */
    public static GregorianCalendar currentGregorian() {
        FastPersianCalendar result = new FastPersianCalendar();
        //Set to current time (which is already Gregorian)
        return result.gCal;
    }


    /**
     * Get full Gregorian date as string
     * @param locale locale for formatting
     * @return formatted Gregorian date
     */
    public String getGrgLongDate(Locale locale) {
        complete();
        return String.format(locale, "%s, %d %s %d",
                             getGrgDayOfWeekName(locale),
                             getGrgDay(),
                             getGrgMonthName(locale),
                             getGrgYear());
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
                             getHijriMonthName(hijri.getMonth()),
                             hijri.getYear());
    }

    /**
     * Get Gregorian date with 1-based month format
     * @param delimiter delimiter between parts
     * @return formatted date with 1-based month
     */
    public String getGrgShortDate(String delimiter) {
        complete();
        return getGrgYear() + delimiter +
               String.format("%02d", getGrgMonth()) + delimiter + //1-based month
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
               today.get(Calendar.MONTH) == (getGrgMonth() - 1) &&
               today.get(Calendar.DAY_OF_MONTH) == getGrgDay();
    }

    /**
     * Get Gregorian date as Date object
     * @return Date object representing the Gregorian date
     */
    public Date getGrgDate() {
        complete();
        return gCal.getTime();
    }

    /**
     * Get Gregorian date as milliseconds
     * @return milliseconds since epoch
     */
    public long getGrgTimeInMillis() {
        complete();
        return gCal.getTimeInMillis();
    }

    /**
     * Set Gregorian date
     * @param year Gregorian year
     * @param month 1-based Gregorian month (1=January)
     * @param day day of month (1-31)
     */
    public void setGregorianDate(int year, int month, int day) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }
        if (day < 1 || day > getGrgMonthLength(year, month)) {
            throw new IllegalArgumentException("Invalid day for month: " + day);
        }

        gCal.set(year, month-1, day);
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * Set Gregorian date with 1-based month (convenience method)
     * @param year Gregorian year
     * @param month1Based 1-based Gregorian month (1=January)
     * @param day day of month (1-31)
     */
    public void setGregorianDate1Based(int year, int month1Based, int day) {
        if (month1Based < 1 || month1Based > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month1Based);
        }
        setGregorianDate(year, month1Based - 1, day);
    }

    /**
     * Add days to Gregorian date
     * @param days number of days to add (can be negative)
     */
    public void addGrgDays(int days) {
        complete();
        gCal.add(Calendar.DAY_OF_MONTH, days);
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * Add months to Gregorian date
     * @param months number of months to add (can be negative)
     */
    public void addGrgMonths(int months) {
        complete();
        gCal.add(Calendar.MONTH, months);
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * Add years to Gregorian date
     * @param years number of years to add (can be negative)
     */
    public void addGrgYears(int years) {
        complete();
        gCal.add(Calendar.YEAR, years);
        setTimeInMillis(gCal.getTimeInMillis());
    }

    /**
     * Get difference in days between this Gregorian date and another
     * @param other the other date
     * @return number of days difference
     */
    public long grgDaysBetween(FastPersianCalendar other) {
        long thisMillis  = this.getGrgTimeInMillis();
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
        complete();
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

    //=== HELPER METHODS FOR DATE MANIPULATION ===

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
     * @return new FastPersianCalendar instance with the added days
     */
    public FastPersianCalendar plusDays(int days) {
        FastPersianCalendar result = new FastPersianCalendar(this);
        result.addDays(days);
        return result;
    }

    /**
     * Get a copy of this calendar with days subtracted
     * @param days number of days to subtract
     * @return new FastPersianCalendar instance with the subtracted days
     */
    public FastPersianCalendar minusDays(int days) {
        return plusDays(-days);
    }

    /**
     * Get a copy of this calendar with weeks added
     * @param weeks number of weeks to add
     * @return new FastPersianCalendar instance with the added weeks
     */
    public FastPersianCalendar plusWeeks(int weeks) {
        FastPersianCalendar result = new FastPersianCalendar(this);
        result.addWeeks(weeks);
        return result;
    }

    /**
     * Get a copy of this calendar with weeks subtracted
     * @param weeks number of weeks to subtract
     * @return new FastPersianCalendar instance with the subtracted weeks
     */
    public FastPersianCalendar minusWeeks(int weeks) {
        return plusWeeks(-weeks);
    }

    /**
     * Get a copy of this calendar with months added
     * @param months number of months to add
     * @return new FastPersianCalendar instance with the added months
     */
    public FastPersianCalendar plusMonths(int months) {
        FastPersianCalendar result = new FastPersianCalendar(this);
        result.addMonths(months);
        return result;
    }

    /**
     * Get a copy of this calendar with months subtracted
     * @param months number of months to subtract
     * @return new FastPersianCalendar instance with the subtracted months
     */
    public FastPersianCalendar minusMonths(int months) {
        return plusMonths(-months);
    }

    /**
     * Get a copy of this calendar with years added
     * @param years number of years to add
     * @return new FastPersianCalendar instance with the added years
     */
    public FastPersianCalendar plusYears(int years) {
        FastPersianCalendar result = new FastPersianCalendar(this);
        result.addYears(years);
        return result;
    }

    /**
     * Get a copy of this calendar with years subtracted
     * @param years number of years to subtract
     * @return new FastPersianCalendar instance with the subtracted years
     */
    public FastPersianCalendar minusYears(int years) {
        return plusYears(-years);
    }

    /**
     * Check if this date is before another Persian date
     * @param other the date to compare with
     * @return true if this date is before the other date
     */
    public boolean isBefore(FastPersianCalendar other) {
        return this.getTimeInMillis() < other.getTimeInMillis();
    }

    /**
     * Check if this date is after another Persian date
     * @param other the date to compare with
     * @return true if this date is after the other date
     */
    public boolean isAfter(FastPersianCalendar other) {
        return this.getTimeInMillis() > other.getTimeInMillis();
    }

    /**
     * Check if this date is equal to another Persian date
     * @param other the date to compare with
     * @return true if both dates represent the same day
     */
    public boolean isEqual(FastPersianCalendar other) {
        return this.persianYear == other.persianYear &&
               this.persianMonth == other.persianMonth &&
               this.persianDay == other.persianDay;
    }

    /**
     * Get the number of days between this date and another date
     * @param other the date to compare with
     * @return number of days between the two dates (positive if this date is later)
     */
    public long daysBetween(FastPersianCalendar other) {
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
        FastPersianCalendar today = new FastPersianCalendar();
        return isEqual(today);
    }

    /**
     * Get the first day of the current month
     * @return new FastPersianCalendar instance set to the first day of current month
     */
    public FastPersianCalendar withFirstDayOfMonth() {
        FastPersianCalendar result = new FastPersianCalendar(this);
        result.set(DAY_OF_MONTH, 1);
        return result;
    }

    /**
     * Get the last day of the current month
     * @return new FastPersianCalendar instance set to the last day of current month
     */
    public FastPersianCalendar withLastDayOfMonth() {
        FastPersianCalendar result  = new FastPersianCalendar(this);
        int                 lastDay = result.getDaysInMonth();
        result.set(DAY_OF_MONTH, lastDay);
        return result;
    }

    /**
     * Get the first day of the current year
     * @return new FastPersianCalendar instance set to Farvardin 1 of current year
     */
    public FastPersianCalendar withFirstDayOfYear() {
        FastPersianCalendar result = new FastPersianCalendar(this);
        result.set(MONTH, FARVARDIN);
        result.set(DAY_OF_MONTH, 1);
        return result;
    }

    /**
     * Get the last day of the current year
     * @return new FastPersianCalendar instance set to Esfand 29/30 of current year
     */
    public FastPersianCalendar withLastDayOfYear() {
        FastPersianCalendar result = new FastPersianCalendar(this);
        result.set(MONTH, ESFAND);
        int lastDay = isLeapYear(result.getYear()) ? 30 : 29;
        result.set(DAY_OF_MONTH, lastDay);
        return result;
    }

    /**
     * Get the start of the day (00:00:00.000)
     * @return new FastPersianCalendar instance with time set to midnight
     */
    public FastPersianCalendar atStartOfDay() {
        FastPersianCalendar result = new FastPersianCalendar(this);
        result.set(HOUR_OF_DAY, 0);
        result.set(MINUTE, 0);
        result.set(SECOND, 0);
        result.set(MILLISECOND, 0);
        return result;
    }

    /**
     * Get the end of the day (23:59:59.999)
     * @return new FastPersianCalendar instance with time set to end of day
     */
    public FastPersianCalendar atEndOfDay() {
        FastPersianCalendar result = new FastPersianCalendar(this);
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
    public int getAge(FastPersianCalendar referenceDate) {
        if (referenceDate == null) {
            return getAge();
        }
        int age = referenceDate.persianYear - this.persianYear;

        //Adjust if birthday hasn't occurred yet this year
        if (referenceDate.persianMonth < this.persianMonth ||
            (referenceDate.persianMonth == this.persianMonth &&
             referenceDate.persianDay < this.persianDay)) {
            age--;
        }

        return Math.max(0, age);
    }

    /**
     * Get the age in years based on today's date
     * @return age in years
     */
    public int getAge() {
        return getAge(new FastPersianCalendar());
    }

    /**
     * Check if the current date is within a date range (inclusive)
     * @param startDate start of the range
     * @param endDate end of the range
     * @return true if current date is between startDate and endDate (inclusive)
     */
    public boolean isBetween(FastPersianCalendar startDate, FastPersianCalendar endDate) {
        if (startDate == null || endDate == null) {
            return false;
        }
        return !this.isBefore(startDate) && !this.isAfter(endDate);
    }

    /**
     * Get the day of year (1 to 365/366)
     * @return day of year
     */
    public int getDayOfYear() {
        return get(DAY_OF_YEAR);
    }

    /**
     * Get the week of year
     * @return week of year (1-53)
     */
    public int getWeekOfYear() {
        return get(WEEK_OF_YEAR);
    }

    /**
     * Get the week of month
     * @return week of month (1-6)
     */
    public int getWeekOfMonth() {
        return get(WEEK_OF_MONTH);
    }

    /**
     * Create a copy of this calendar
     * @return a deep copy of this FastPersianCalendar instance
     */
    @Override
    public FastPersianCalendar clone() {
        //Call parent clone
        FastPersianCalendar clone = (FastPersianCalendar) super.clone();

        //The parent clone() creates a shallow copy
        //We need to ensure our fields are properly copied

        //Our int fields will be copied correctly by parent clone
        //But we need to ensure gCal is in sync
        //Since gCal is final and shared, we need to sync it

        //Set gCal to the same time (it's the same object reference, but that's OK)
        clone.gCal.setTimeInMillis(this.gCal.getTimeInMillis());

        //The time field should already be copied by super.clone()
        //We need to recompute our Persian fields from the time
        clone.computePersianFromGregorianFast();

        return clone;
    }

    public FastPersianCalendar clone1() {
        //Create a new instance with the same time zone and locale
        FastPersianCalendar clone = new FastPersianCalendar(this.getTimeZone(), this.locale);

        //Copy the internal state
        clone.persianYear  = this.persianYear;
        clone.persianMonth = this.persianMonth;
        clone.persianDay   = this.persianDay;
        clone.setTimeInMillis(this.getTimeInMillis());

        //Copy the time fields if they are set
        for (int i = 0; i < FIELD_COUNT; i++) {
            if (this.isSet[i]) {
                clone.fields[i] = this.fields[i];
                clone.isSet[i]  = true;
            }
        }

        //Copy cache and state
        clone.lastComputedTime = this.lastComputedTime;
        clone.isDirty          = this.isDirty;
        clone.areFieldsSet     = this.areFieldsSet;

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
        return persianMonth / 3 + 1;
    }

    /**
     * Check if the current date is the last day of the month
     * @return true if current day is the last day of the month
     */
    public boolean isLastDayOfMonth() {
        return persianDay == getDaysInMonth(persianYear, persianMonth + 1);
    }

    /**
     * Check if the current date is the first day of the month
     * @return true if current day is the first day of the month
     */
    public boolean isFirstDayOfMonth() {
        return persianDay == 1;
    }

    /**
     * Get the number of days remaining in the current month
     * @return days remaining in month
     */
    public int getDaysRemainingInMonth() {
        return getDaysInMonth(persianYear, persianMonth + 1) - persianDay;
    }

    /**
     * Get the number of days passed in the current month
     * @return days passed in month
     */
    public int getDaysPassedInMonth() {
        return persianDay - 1;
    }

    /**
     * Get a string representation of the date in ISO-like format (YYYY/MM/dd)
     * @return formatted date string
     */
    public String toIsoString() {
        return String.format(locale, "%04d/%02d/%02d",
                             persianYear, persianMonth + 1, persianDay);
    }

    /**
     * Check if this date is in a leap year
     * @return true if the year is a leap year
     */
    public boolean isLeapYear() {
        return isLeapYear(persianYear);
    }

    /**
     * Get the day of week as Persian name
     * @return Persian weekday name
     */
    public String getPersianWeekdayName() {
        return getWeekdayName(get(DAY_OF_WEEK), PERSIAN_LOCALE);
    }

    /**
     * Get the month name in Persian
     * @return Persian month name
     */
    public String getPersianMonthName() {
        return getMonthName(persianMonth, PERSIAN_LOCALE);
    }

    /**
     * Get the month name in English
     * @return English month name
     */
    public String getEnglishMonthName() {
        return getMonthName(persianMonth, Locale.ENGLISH);
    }

    /**
     * Check if two dates represent the same day (ignoring time)
     * @param other the date to compare with
     * @return true if same year, month, and day
     */
    public boolean isSameDay(FastPersianCalendar other) {
        if (other == null) {
            return false;
        }
        ensureComputed();
        other.ensureComputed();
        return this.persianYear == other.persianYear &&
               this.persianMonth == other.persianMonth &&
               this.persianDay == other.persianDay;
    }

    /**
     * Check if two dates represent the same month and year
     * @param other the date to compare with
     * @return true if same year and month
     */
    public boolean isSameMonth(FastPersianCalendar other) {
        if (other == null) return false;
        ensureComputed();
        other.ensureComputed();
        return this.persianYear == other.persianYear &&
               this.persianMonth == other.persianMonth;
    }

    /**
     * Check if two dates represent the same year
     * @param other the date to compare with
     * @return true if same year
     */
    public boolean isSameYear(FastPersianCalendar other) {
        if (other == null) return false;
        ensureComputed();
        other.ensureComputed();
        return this.persianYear == other.persianYear;
    }

    /**
     * Get midnight timestamp for this date
     * @return milliseconds since epoch for midnight of this date
     */
    public long getMidnightTime() {
        return atStartOfDay().getTimeInMillis();
    }

    /**
     * Check if the date is valid
     * @return true if the date represents a valid Persian date
     */
    public boolean isValid() {
        try {
            validatePersianDate(persianYear, persianMonth, persianDay);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get the maximum possible day for the current month
     * @return maximum day number for current month
     */
    public int getMaxDayOfMonth() {
        return getDaysInMonth(persianYear, persianMonth + 1);
    }


    //Persian calendar methods
    public int getDaysPassedFromStartOfYear() {
        int year = getYear();
        int month = getMonth();
        int day = getDayOfMonth();

        int daysPassed = day - 1;

        for (int i = 0; i < month - 1; i++) {
            daysPassed += getDaysInMonth(year, i + 1);
        }

        return daysPassed;
    }

    public int getRemainingDaysUntilEndOfYear() {
        int year = getYear();
        int totalDaysInYear = isLeapYear(year) ? 366 : 365;
        int daysPassed = getDaysPassedFromStartOfYear();

        return totalDaysInYear - daysPassed;
    }

    public int getGregorianDaysPassedFromStartOfYear() {
        return gCal.get(Calendar.DAY_OF_YEAR) - 1;
    }

    public int getGregorianRemainingDaysUntilEndOfYear() {
        int totalDaysInYear = gCal.getActualMaximum(Calendar.DAY_OF_YEAR);
        int dayOfYear = gCal.get(Calendar.DAY_OF_YEAR);
        return totalDaysInYear - dayOfYear + 1;
    }

    public int getHijriDaysPassedFromStartOfYear() {
        YMD hijriDate = gregorianToHijri(
                gCal.get(Calendar.YEAR),
                gCal.get(Calendar.MONTH) + 1,
                gCal.get(Calendar.DAY_OF_MONTH)
        );

        //Reset both calendars to midnight for accurate day calculation
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

        long startMillis = startOfYearGreg.getTimeInMillis();
        long currentMillis = currentGreg.getTimeInMillis();
        long diffMillis = currentMillis - startMillis;

        //Return days between start of year and now
        return (int) (diffMillis / (24 * 60 * 60 * 1000));
    }

    public int getHijriRemainingDaysUntilEndOfYear() {
        YMD hijriDate = gregorianToHijri(
                gCal.get(Calendar.YEAR),
                gCal.get(Calendar.MONTH) + 1,
                gCal.get(Calendar.DAY_OF_MONTH)
        );

        //Get the Gregorian date for start of NEXT Hijri year
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
        long currentMillis = currentGreg.getTimeInMillis();
        long diffMillis = nextYearMillis - currentMillis;

        //Return days between now and start of next year
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

    public FastPersianCalendar getStartOfPersianYear() {
        return new FastPersianCalendar(getYear(), 1, 1);
    }

    //Helper method to find start of Hijri year
    public FastPersianCalendar getStartOfHijriYear() {
        YMD hijriDate = gregorianToHijri(
                gCal.get(Calendar.YEAR),
                gCal.get(Calendar.MONTH),
                gCal.get(Calendar.DAY_OF_MONTH)
        );

        GregorianCalendar startGreg = hijriToGregorian(hijriDate.year, 1, 1);
        return new FastPersianCalendar(startGreg);
    }

    //Helper method to find start of Gregorian year
    public Calendar getStartOfGregorianYear() {
        Calendar startCal = Calendar.getInstance();
        startCal.set(gCal.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
        return startCal;
    }

    //Helper methods for Hijri calculations
    private int getHijriDaysInMonth(int hijriYear, int hijriMonth) {
        //Hijri months 1-11: Odd months have 30 days, even months have 29 days
        if (hijriMonth <= 11) {
            return (hijriMonth % 2 == 1) ? 30 : 29;
        }

        //Month 12 (Dhu al-Hijjah): 30 in leap years, 29 in common years
        return isHijriLeapYear(hijriYear) ? 30 : 29;
    }



    /////////////////////
    //Add this method to get the next Persian New Year date
    public static FastPersianCalendar getNextNewYear(FastPersianCalendar persianDate) {
        if (persianDate == null) {
            throw new IllegalArgumentException("Persian date cannot be null");
        }

        int currentYear = persianDate.getYear();
        int currentMonth = persianDate.getMonth();
        int currentDay = persianDate.getDayOfMonth();

        //If current date is before or on New Year (1st of Farvardin)
        //then next New Year is in current year, otherwise next year
        if (currentMonth < 1 || (currentMonth == 1 && currentDay <= 1)) {
            //Next New Year is in current year
            return new FastPersianCalendar(currentYear, 1, 1);
        } else {
            //Next New Year is in next year
            return new FastPersianCalendar(currentYear + 1, 1, 1);
        }
    }

    public FastPersianCalendar getNextNewYear() {
        int currentYear = getYear();
        int currentMonth = getMonth();
        int currentDay = getDayOfMonth();

        //If current date is on or after 1st of Farvardin, next new year is next year
        if (currentMonth > 1 || (currentMonth == 1 && currentDay >= 1)) {
            currentYear++; //Move to next year
        }

        //Return 1st of Farvardin of the calculated year
        return new FastPersianCalendar(currentYear, 1, 1);
    }

    /**
     * Get the previous Friday (weekend start)
     * @return date of previous Friday
     */
    public FastPersianCalendar getPreviousFriday() {
        FastPersianCalendar result           = new FastPersianCalendar(this);
        int                 currentDayOfWeek = get(DAY_OF_WEEK);
        int                 daysToSubtract   = (currentDayOfWeek - FRIDAY + 7) % 7;
        if (daysToSubtract == 0) {
            daysToSubtract = 7; //If it's already Friday, go to previous Friday
        }
        result.addDays(-daysToSubtract);
        return result;
    }

    /**
     * Get the next Friday (weekend start)
     * @return date of next Friday
     */
    public FastPersianCalendar getNextFriday() {
        FastPersianCalendar result           = new FastPersianCalendar(this);
        int                 currentDayOfWeek = get(DAY_OF_WEEK);
        int                 daysToAdd        = (FRIDAY - currentDayOfWeek + 7) % 7;
        if (daysToAdd == 0) {
            daysToAdd = 7; //If it's already Friday, go to next Friday
        }
        result.addDays(daysToAdd);
        return result;
    }

    /**
     * Check if the date is in the past
     * @return true if date is before today
     */
    public boolean isPast() {
        return isBefore(new FastPersianCalendar());
    }

    /**
     * Check if the date is in the future
     * @return true if date is after today
     */
    public boolean isFuture() {
        return isAfter(new FastPersianCalendar());
    }

    /**
     * Get the difference in months between this date and another date
     * @param other the date to compare with
     * @return number of months difference (positive if this date is later)
     */
    public int monthsBetween(FastPersianCalendar other) {
        int yearDiff  = this.persianYear - other.persianYear;
        int monthDiff = this.persianMonth - other.persianMonth;
        return yearDiff * 12 + monthDiff;
    }

    /**
     * Get the difference in years between this date and another date
     * @param other the date to compare with
     * @return number of years difference (positive if this date is later)
     */
    public int yearsBetween(FastPersianCalendar other) {
        int yearDiff = this.persianYear - other.persianYear;

        //Adjust if the month/day hasn't occurred yet this year
        if (yearDiff > 0) {
            if (this.persianMonth < other.persianMonth ||
                (this.persianMonth == other.persianMonth && this.persianDay < other.persianDay)) {
                yearDiff--;
            }
        } else if (yearDiff < 0) {
            if (this.persianMonth > other.persianMonth ||
                (this.persianMonth == other.persianMonth && this.persianDay > other.persianDay)) {
                yearDiff++;
            }
        }

        return yearDiff;
    }

    /**
     * Get current Persian date as a formatted string (for Android UI)
     */
    public String getFormattedDate(String pattern) {
        ensureComputed();

        if (pattern == null || pattern.isEmpty()) {
            return getLongDate();
        }

        switch (pattern) {
            case "yyyy/MM/dd":
                return String.format(locale, "%04d/%02d/%02d",
                                     getYear(), getMonth(), getDayOfMonth());
            case "yyyy-MM-dd":
                return String.format(locale, "%04d-%02d-%02d",
                                     getYear(), getMonth(), getDayOfMonth());
            case "dd MMMM yyyy":
                return String.format(locale, "%02d %s %04d",
                                     getDayOfMonth(), getMonthName(), getYear());
            default:
                //For custom patterns, use FastPersianDateFormat
                FastPersianDateFormat formatter = new FastPersianDateFormat(pattern);
                return formatter.format(this);
        }
    }

    /**
     * Get Persian horoscope (برج فلکی) based on Persian month
     * @param persianMonth Persian month (1 to 12)
     * @return Persian horoscope name
     */
    public static String getPersianHoroscope(int persianMonth) {
        if (persianMonth < 1 || persianMonth > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: "+ persianMonth);
        }
        return PERSIAN_HOROSCOPE_NAMES[persianMonth - 1];
    }

    /**
     * Get Persian horoscope based on Persian month with English name
     * @param persianMonth Persian month (1 to 12)
     * @return Array containing [persianName, englishName]
     */
    public static String[] getPersianHoroscopeWithEnglish(int persianMonth) {
        if (persianMonth < 1 || persianMonth > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: "+ persianMonth);
        }
        return new String[]{
                PERSIAN_HOROSCOPE_NAMES[persianMonth - 1],
                PERSIAN_HOROSCOPE_NAMES_EN[persianMonth - 1]
        };
    }

    /**
     * Better debug method for Android Logcat
     */
    public String toDebugString() {
        complete();
        return String.format(Locale.US,
                             "FastPersianCalendar Debug:\n" +
                             "  Time in millis: %d\n" +
                             "  Persian Date: %04d/%02d/%02d\n" +
                             "  Gregorian Date: %04d/%02d/%02d\n" +
                             "  gCal time: %d\n" +
                             "  isDirty: %b, lastComputedTime: %d",
                             getTimeInMillis(),
                             persianYear, persianMonth + 1, persianDay,
                             gCal.get(Calendar.YEAR), gCal.get(Calendar.MONTH) + 1, gCal.get(Calendar.DAY_OF_MONTH),
                             gCal.getTimeInMillis(),
                             isDirty, lastComputedTime);
    }


    /**
     * Check if a 1-based Persian month is valid
     * @param month 1-based month to validate
     * @return true if valid (1-12)
     */
    public static boolean isValidMonth(int month) {
        return month >= 1 && month <= 12;
    }


    /**
     * ✅ NEW: Get combined date with 1-based months for both calendars
     */
    public String getCombinedDate1Based() {
        ensureComputed();
        return String.format(locale, "Persian: %04d/%02d/%02d - Gregorian: %04d/%02d/%02d",
                             getYear(),
                             getMonth(),  // Already 1-based
                             getDayOfMonth(),
                             getGrgYear(),
                             getGrgMonth(),  // 1-based Gregorian
                             getGrgDay());
    }



    /**
     * ✅ NEW: Simple mapping between Persian and Gregorian months
     */
    public static int persianToGregorianMonthApprox(int persianMonth) {
        // Simple mapping: Farvardin (1) ≈ March/April
        // This is approximate - actual conversion depends on the date
        int persian0Based =persianMonth - 1;
        return (persian0Based + 2) % 12 + 1; // Farvardin (0) → March (3)
    }

    public static int gregorianToPersianMonthApprox(int gregorianMonth) {
        // Simple mapping: January (1) ≈ Dey/Bahman
        int gregorian0Based = gregorianMonth - 1;
        return (gregorian0Based + 9) % 12 + 1; // January (0) → Dey (10)
    }

    /**
     * Get the number of days in the current quarter
     */
    public int getDaysInQuarter() {
        int quarter = getQuarter();
        int startMonth = (quarter - 1) * 3;
        int days = 0;

        for (int i = 0; i < 3; i++) {
            days += getDaysInMonth(persianYear, startMonth + i + 1);
        }
        return days;
    }

    /**
     * Get the first day of the current quarter
     */
    public FastPersianCalendar withFirstDayOfQuarter() {
        int quarter = getQuarter();
        int month = (quarter - 1) * 3 + 1; // Convert to 1-based
        return new FastPersianCalendar(getYear(), month, 1);
    }

    /**
     * Get the last day of the current quarter
     */
    public FastPersianCalendar withLastDayOfQuarter() {
        int quarter = getQuarter();
        int month = quarter * 3; // Last month of quarter (1-based)
        int day = getDaysInMonth(getYear(), month);
        return new FastPersianCalendar(getYear(), month, day);
    }

    /**
     * Check if the date is in the first half of the year
     */
    public boolean isFirstHalfOfYear() {
        return getMonth() <= 6;
    }

    /**
     * Check if the date is in the second half of the year
     */
    public boolean isSecondHalfOfYear() {
        return getMonth() > 6;
    }

    /**
     * Get the season based on Persian month
     */
    public String getSeason() {
        int month = getMonth();
        if (month >= 1 && month <= 3) return "بهار";
        if (month >= 4 && month <= 6) return "تابستان";
        if (month >= 7 && month <= 9) return "پاییز";
        return "زمستان";
    }

    /**
     * Get the season in English
     */
    public String getSeasonEnglish() {
        int month = getMonth();
        if (month >= 1 && month <= 3) return "Spring";
        if (month >= 4 && month <= 6) return "Summer";
        if (month >= 7 && month <= 9) return "Autumn";
        return "Winter";
    }

    // ==================== TIME-RELATED METHODS ====================

    /**
     * Get the time of day as a descriptive string
     */
    public String getTimeOfDay() {
        int hour = getHourOfDay();
        if (hour >= 5 && hour < 12) return "صبح";
        if (hour >= 12 && hour < 17) return "بعدازظهر";
        if (hour >= 17 && hour < 20) return "عصر";
        return "شب";
    }

    /**
     * Get the time of day in English
     */
    public String getTimeOfDayEnglish() {
        int hour = getHourOfDay();
        if (hour >= 5 && hour < 12) return "Morning";
        if (hour >= 12 && hour < 17) return "Afternoon";
        if (hour >= 17 && hour < 20) return "Evening";
        return "Night";
    }

    /**
     * Check if it's morning (5 AM to 12 PM)
     */
    public boolean isMorning() {
        int hour = getHourOfDay();
        return hour >= 5 && hour < 12;
    }

    /**
     * Check if it's afternoon (12 PM to 5 PM)
     */
    public boolean isAfternoon() {
        int hour = getHourOfDay();
        return hour >= 12 && hour < 17;
    }

    /**
     * Check if it's evening (5 PM to 8 PM)
     */
    public boolean isEvening() {
        int hour = getHourOfDay();
        return hour >= 17 && hour < 20;
    }

    /**
     * Check if it's night (8 PM to 5 AM)
     */
    public boolean isNight() {
        int hour = getHourOfDay();
        return hour >= 20 || hour < 5;
    }

    /**
     * Check if this date is within the last N days
     */
    public boolean isWithinLastDays(int days) {
        FastPersianCalendar now = new FastPersianCalendar();
        long diffMillis = now.getTimeInMillis() - this.getTimeInMillis();
        long diffDays = diffMillis / (1000 * 60 * 60 * 24);
        return diffDays <= days;
    }

    /**
     * Check if this date is within the next N days
     */
    public boolean isWithinNextDays(int days) {
        FastPersianCalendar now = new FastPersianCalendar();
        long diffMillis = this.getTimeInMillis() - now.getTimeInMillis();
        long diffDays = diffMillis / (1000 * 60 * 60 * 24);
        return diffDays <= days;
    }

    /**
     * Get human-readable difference from now
     */
    public String getHumanReadableDifference() {
        FastPersianCalendar now = new FastPersianCalendar();
        long diffMillis = this.getTimeInMillis() - now.getTimeInMillis();
        long diffDays = diffMillis / (1000 * 60 * 60 * 24);

        if (diffDays == 0) return "امروز";
        if (diffDays == 1) return "فردا";
        if (diffDays == -1) return "دیروز";
        if (diffDays > 0) return diffDays + " روز بعد";
        return Math.abs(diffDays) + " روز قبل";
    }

    /**
     * Get the current Persian date as a new instance
     */
    public static FastPersianCalendar now() {
        return new FastPersianCalendar();
    }

    /**
     * Get yesterday's date
     */
    public static FastPersianCalendar yesterday() {
        FastPersianCalendar cal = new FastPersianCalendar();
        cal.addDays(-1);
        return cal;
    }

    /**
     * Get tomorrow's date
     */
    public static FastPersianCalendar tomorrow() {
        FastPersianCalendar cal = new FastPersianCalendar();
        cal.addDays(1);
        return cal;
    }

    /**
     * Get the first day of the given Persian year
     */
    public static FastPersianCalendar firstDayOfYear(int year) {
        return new FastPersianCalendar(year, 1, 1);
    }

    /**
     * Get the last day of the given Persian year
     */
    public static FastPersianCalendar lastDayOfYear(int year) {
        int lastDay = isLeapYear(year) ? 30 : 29;
        return new FastPersianCalendar(year, 12, lastDay);
    }


    /**
     * Validate Persian date components
     */
    private void validatePersianDate(int year, int month, int day) {
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException("Year must be between 1 and 9999, got: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }
        int maxDays = getDaysInMonthInternal(year, month);
        if (day < 1 || day > maxDays) {
            throw new IllegalArgumentException("Day must be between 1 and " + maxDays + " for month " + (month + 1) + ", got: " + day);
        }
    }

    /**
     * Validate Gregorian date components
     */
    private void validateGregorianDate(int year, int month, int day) {
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException("Year must be between 1 and 9999, got: " + year);
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12, got: " + month);
        }
        int maxDays = getGrgMonthLength(year, month);
        if (day < 1 || day > maxDays) {
            throw new IllegalArgumentException("Day must be between 1 and " + maxDays + " for month " + (month + 1) + ", got: " + day);
        }
    }

    // ==================== SERIALIZATION METHODS ====================

    /**
     * Convert to JSON string
     */
    public String toJson() {
        ensureComputed();
        return String.format(Locale.US,
                             "{\"persian\":{\"year\":%d,\"month\":%d,\"day\":%d}," +
                             "\"gregorian\":{\"year\":%d,\"month\":%d,\"day\":%d}," +
                             "\"time\":%d,\"timezone\":\"%s\"}",
                             persianYear, persianMonth + 1, persianDay,
                             gCal.get(Calendar.YEAR), gCal.get(Calendar.MONTH) + 1, gCal.get(Calendar.DAY_OF_MONTH),
                             getTimeInMillis(), getTimeZone().getID());
    }

    /**
     * Create from JSON string
     */
    public static FastPersianCalendar fromJson(String json) {
        // Simplified JSON parsing - in production, use a proper JSON library
        try {
            // Extract Persian date from JSON
            int persianYear = Integer.parseInt(json.substring(json.indexOf("\"year\":") + 7, json.indexOf(",\"month\"")));
            int persianMonth = Integer.parseInt(json.substring(json.indexOf("\"month\":") + 8, json.indexOf(",\"day\"")));
            int persianDay = Integer.parseInt(json.substring(json.indexOf("\"day\":") + 6, json.indexOf("}")));

            return new FastPersianCalendar(persianYear, persianMonth, persianDay);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON format", e);
        }
    }

    // ==================== CALENDAR COMPATIBILITY METHODS ====================

    /**
     * Get the value of the given calendar field
     */
    @Override
    public int get(int field) {
        ensureComputed();
        return super.get(field);
    }

    /**
     * Check if the given calendar field is set
     */
    public boolean isFieldSet(int field) {
        return isSet[field];
    }

    /**
     * Get a descriptive string about the date
     */
    public String getDateDescription() {
        ensureComputed();
        return String.format(locale,
                             "%s، %d %s %d، ساعت %02d:%02d",
                             getWeekdayName(),
                             getDayOfMonth(),
                             getMonthName(),
                             getYear(),
                             getHourOfDay(),
                             getMinute());
    }

    /**
     * Get the date in a friendly format
     */
    public String toFriendlyString() {
        if (isToday()) return "امروز";
        if (isEqual(yesterday())) return "دیروز";
        if (isEqual(tomorrow())) return "فردا";

        long diffDays = daysBetween(new FastPersianCalendar());
        if (diffDays > 0 && diffDays <= 7) return diffDays + " روز بعد";
        if (diffDays < 0 && diffDays >= -7) return Math.abs(diffDays) + " روز قبل";

        return getLongDate();
    }

    /**
     * Check if this is a special Persian date
     */
    public boolean isSpecialDate() {
        // Check for Nowruz (Persian New Year)
        if (getMonth() == 1 && getDayOfMonth() == 1) return true;

        // Check for other Persian holidays
        if (getMonth() == 1 && getDayOfMonth() == 13) return true; // Sizdah Bedar

        // Check for month transitions (first day of month)
        if (getDayOfMonth() == 1) return true;

        return false;
    }

}