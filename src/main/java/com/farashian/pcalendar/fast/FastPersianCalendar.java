package com.farashian.pcalendar.fast;

import com.farashian.pcalendar.MyPCConstants;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Arrays;
import java.util.Objects;

import static com.farashian.pcalendar.MyPCConstants.PERSIAN_LOCALE;

/**
 * High-performance Persian Calendar with complete field computation
 * Fixed all field computation issues and critical bugs
 */
public class FastPersianCalendar extends Calendar {

    public static final int FIRST_DAY_OF_WEEK = Calendar.SATURDAY;
    public static final int WEEKDAY_HOLIDAY_NUMBER = Calendar.FRIDAY;

    // Calendar constants we need to define
    private static final int AD = 1; // Gregorian era constant

    // Month constants
    public static final int FARVARDIN = 0;
    public static final int ORDIBEHESHT = 1;
    public static final int KHORDAD = 2;
    public static final int TIR = 3;
    public static final int MORDAD = 4;
    public static final int SHAHRIVAR = 5;
    public static final int MEHR = 6;
    public static final int ABAN = 7;
    public static final int AZAR = 8;
    public static final int DEY = 9;
    public static final int BAHMAN = 10;
    public static final int ESFAND = 11;

    // Fast internal storage
    private int persianYear;
    private int persianMonth;
    private int persianDay;

    // Performance optimizations
    private final GregorianCalendar gCal;
    private final Locale locale;
    private long lastComputedTime = -1;
    private boolean isDirty = true;

    // Thread-local for temporary calculations
    private static final ThreadLocal<int[]> tempArrayCache =
            ThreadLocal.withInitial(() -> new int[3]);

    // === CONSTRUCTORS ===

    public FastPersianCalendar() {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setTimeInMillis(System.currentTimeMillis());
    }

    public FastPersianCalendar(TimeZone zone) {
        this(zone, PERSIAN_LOCALE);
        setTimeInMillis(System.currentTimeMillis());
    }

    public FastPersianCalendar(TimeZone zone, Locale locale) {
        super(zone, locale);
        this.locale = locale;
        this.gCal = new GregorianCalendar(zone, locale);
        // Initialize with reasonable defaults
        this.persianYear = 1400;
        this.persianMonth = 0;
        this.persianDay = 1;
    }

    public FastPersianCalendar(FastPersianCalendar pc) {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setPersianDateInternal(pc.getYear(), pc.getMonth(), pc.getDayOfMonth()); // FIXED: Use actual day
    }

    public FastPersianCalendar(int year, int month, int dayOfMonth) {
        this();
        setPersianDateInternal(year, month, dayOfMonth);
    }

    public FastPersianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute) {
        this(year, month, dayOfMonth, hourOfDay, minute, 0);
    }

    public FastPersianCalendar(int year, int month, int dayOfMonth, int hourOfDay, int minute, int second) {
        this();
        setPersianDateInternal(year, month, dayOfMonth);
        // Set time fields directly
        setInternalField(HOUR_OF_DAY, hourOfDay);
        setInternalField(MINUTE, minute);
        setInternalField(SECOND, second);
        isDirty = true;
    }

    // === PUBLIC API METHODS ===

    public int getYear() {
        ensureComputed();
        return persianYear;
    }

    public int getMonth() {
        ensureComputed();
        return persianMonth;
    }

    public int getDayOfMonth() {
        ensureComputed();
        return persianDay;
    }

    public int getDaysInMonth() {
        return getDaysInMonth(getYear(), getMonth());
    }

    protected int getDaysInMonth(int year, int month) {
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

    // Date formatter methods
    public String getMonthName() {
        return getMonthName(getMonth(), locale);
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
                persianYear, persianMonth, persianDay, get(DAY_OF_WEEK), locale);
    }

    public String getLongDateTime() {
        complete(); // Ensure all fields are computed
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

    public void setPersianDate(int year, int month, int day) {
        validateDate(year, month, day);
        setPersianDateInternal(year, month, day);
    }

    public void parse(String dateString) {
        FastPersianCalendar parsed = parseOrNullToCompat(dateString);
        if (parsed != null) {
            // Create fresh instance instead of copying internal state
            setPersianDateInternal(parsed.persianYear, parsed.persianMonth, parsed.persianDay);
            // Copy time fields if they exist
            if (parsed.isSet[HOUR_OF_DAY]) set(HOUR_OF_DAY, parsed.get(HOUR_OF_DAY));
            if (parsed.isSet[MINUTE]) set(MINUTE, parsed.get(MINUTE));
            if (parsed.isSet[SECOND]) set(SECOND, parsed.get(SECOND));
        }
    }

    public boolean isLeapYear() {
        return isLeapYear(getYear());
    }

    private static boolean isLeapYear(int year) {
        int remainder = year % 33;
        return remainder == 1 || remainder == 5 || remainder == 9 ||
               remainder == 13 || remainder == 17 || remainder == 22 ||
               remainder == 26 || remainder == 30;
    }

    // === CORE CALENDAR METHODS (FIXED) ===

    @Override
    protected void computeTime() {
        if (isDirty || lastComputedTime != time || !areFieldsSet) {
            computeGregorianFromPersianFast();
            time = gCal.getTimeInMillis();
            lastComputedTime = time;
            isDirty = false;

            // Invalidate fields since time changed
            areFieldsSet = false;
        }
    }

    @Override
    protected void computeFields() {
        if (time != lastComputedTime || !areFieldsSet) {
            computePersianFromGregorianFast();
            lastComputedTime = time;
            isDirty = false;

            // Compute all calendar fields
            computeAllFields();
        }
    }

    /**
     * Compute all calendar fields that parent class expects
     * FIXED: Proper field computation with Persian week calculations
     */
    private void computeAllFields() {
        // Use Gregorian calendar to compute all fields reliably
        gCal.setTimeInMillis(time);

        // Set basic Persian date fields
        fields[YEAR] = persianYear;
        fields[MONTH] = persianMonth;
        fields[DAY_OF_MONTH] = persianDay;

        // Set time fields from Gregorian (they're the same)
        fields[HOUR_OF_DAY] = gCal.get(HOUR_OF_DAY);
        fields[MINUTE] = gCal.get(MINUTE);
        fields[SECOND] = gCal.get(SECOND);
        fields[MILLISECOND] = gCal.get(MILLISECOND);

        // CRITICAL FIX: Use Gregorian's properly computed day-of-week
        fields[DAY_OF_WEEK] = gCal.get(DAY_OF_WEEK);

        // Calculate proper Persian day of year and week fields
        fields[DAY_OF_YEAR] = calculateDayOfYear();
        calculatePersianWeekFields();

        // Set other fields
        fields[AM_PM] = gCal.get(AM_PM);
        fields[HOUR] = gCal.get(HOUR);
        fields[DST_OFFSET] = gCal.get(DST_OFFSET);
        fields[ZONE_OFFSET] = gCal.get(ZONE_OFFSET);

        // ERA is always AD for Persian calendar (modern dates)
        fields[ERA] = AD;

        // Mark all fields as set
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

        // Calculate week of year (Persian year starts with Farvardin 1 = Saturday)
        int weekOfYear = (dayOfYear - 1 + ((dayOfWeek - Calendar.SATURDAY + 7) % 7)) / 7 + 1;
        fields[WEEK_OF_YEAR] = weekOfYear;

        // Calculate week of month
        int dayOfMonth = fields[DAY_OF_MONTH];
        int firstDayOfMonth = calculateFirstDayOfMonth();
        int weekOfMonth = (dayOfMonth - 1 + ((dayOfWeek - firstDayOfMonth + 7) % 7)) / 7 + 1;
        fields[WEEK_OF_MONTH] = weekOfMonth;

        // Calculate day of week in month
        fields[DAY_OF_WEEK_IN_MONTH] = (dayOfMonth - 1) / 7 + 1;
    }

    /**
     * Calculate the day of week for the first day of the current month
     */
    private int calculateFirstDayOfMonth() {
        // Save current state
        int savedDay = persianDay;

        // Set to first day of month and compute
        persianDay = 1;
        computeGregorianFromPersianFast();

        // Get day of week for first day
        int firstDayOfWeek = gCal.get(DAY_OF_WEEK);

        // Restore original state
        persianDay = savedDay;
        computeGregorianFromPersianFast();

        return firstDayOfWeek;
    }

    /**
     * Calculate day of year for Persian calendar
     */
    private int calculateDayOfYear() {
        int dayOfYear = 0;
        for (int month = 0; month < persianMonth; month++) {
            dayOfYear += getDaysInMonth(persianYear, month);
        }
        dayOfYear += persianDay;
        return dayOfYear;
    }

    /**
     * FIXED: Convert Persian date to Gregorian
     * Now correctly handles 0-based month to 1-based month conversion
     */
    private void computeGregorianFromPersianFast() {
        int[] temp = tempArrayCache.get();

        // Convert 0-based month (0-11) to 1-based month (1-12) for the algorithm
        jalaliToGregorianFast(persianYear, persianMonth + 1, persianDay, temp);

        // Get current time fields before setting date
        int hour = internalGet(HOUR_OF_DAY, 0);
        int minute = internalGet(MINUTE, 0);
        int second = internalGet(SECOND, 0);
        int millis = internalGet(MILLISECOND, 0);

        // temp[1] is 1-based Gregorian month, convert to 0-based for GregorianCalendar
        gCal.set(temp[0], temp[1] - 1, temp[2], hour, minute, second);
        gCal.set(MILLISECOND, millis);
    }

    /**
     * FIXED: Convert Gregorian date to Persian
     * Now correctly handles 1-based month to 0-based month conversion
     */
    private void computePersianFromGregorianFast() {
        int[] temp = tempArrayCache.get();

        // Convert Gregorian 0-based month to 1-based for the algorithm
        gregorianToJalaliFast(gCal.get(Calendar.YEAR), gCal.get(Calendar.MONTH) + 1,
                              gCal.get(Calendar.DAY_OF_MONTH), temp);

        // The algorithm returns 1-based month, convert to 0-based for internal storage
        persianYear = temp[0];
        persianMonth = temp[1] - 1;  // FIXED: Convert 1-based to 0-based
        persianDay = temp[2];
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

    // === ARITHMETIC METHODS (FIXED) ===

    @Override
    public void add(int field, int amount) {
        if (amount == 0) return;
        if (field < 0 || field >= ZONE_OFFSET) return;

        complete(); // Ensure fields are computed

        // For Persian date fields, use direct Persian math
        if (field == YEAR || field == MONTH || field == DAY_OF_MONTH) {
            handlePersianDateArithmetic(field, amount);
        } else {
            // For other fields, use Gregorian but optimized
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
     * FIXED: Handle Persian date arithmetic with proper day adjustment
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
                    persianYear = totalMonths / 12;
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

        // Recompute Gregorian date and sync
        computeGregorianFromPersianFast();
        setTimeInMillis(gCal.getTimeInMillis());
        isDirty = true;
        lastComputedTime = -1;
        areFieldsSet = false;
    }

    /**
     * FIXED: Normalize date by handling overflow/underflow of days
     */
    private void normalizeDate() {
        // Handle positive overflow
        while (persianDay > getDaysInMonth(persianYear, persianMonth)) {
            persianDay -= getDaysInMonth(persianYear, persianMonth);
            persianMonth++;
            if (persianMonth > 11) {
                persianMonth = 0;
                persianYear++;
            }
        }

        // Handle negative underflow
        while (persianDay < 1) {
            persianMonth--;
            if (persianMonth < 0) {
                persianMonth = 11;
                persianYear--;
                if (persianYear < 1) persianYear = 1;
            }
            persianDay += getDaysInMonth(persianYear, persianMonth);
        }
    }

    /**
     * FIXED: Adjust day when month or year changes to ensure valid date
     */
    private void adjustDayForNewMonth() {
        int maxDays = getDaysInMonth(persianYear, persianMonth);
        if (persianDay > maxDays) {
            persianDay = maxDays;
        }
    }

    @Override
    public void roll(int field, boolean up) {
        complete(); // Ensure fields are computed

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
            int maxDays = getDaysInMonth(persianYear, persianMonth);
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

        // Recompute Gregorian date and sync
        computeGregorianFromPersianFast();
        setTimeInMillis(gCal.getTimeInMillis());
        isDirty = true;
        lastComputedTime = -1;
        areFieldsSet = false;
    }

    // === OVERRIDES (FIXED) ===

    @Override
    public void setTimeInMillis(long millis) {
        super.setTimeInMillis(millis);
        lastComputedTime = -1;
        isDirty = true;
        areFieldsSet = false;
    }

    @Override
    public void setTimeZone(TimeZone zone) {
        super.setTimeZone(zone);
        gCal.setTimeZone(zone);
        lastComputedTime = -1;
        isDirty = true;
        areFieldsSet = false;
    }

    @Override
    public void set(int field, int value) {
        // Handle setting Persian fields specially
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
                int maxDays = getDaysInMonth(persianYear, persianMonth);
                if (value < 1 || value > maxDays) {
                    throw new IllegalArgumentException("Day must be between 1 and " + maxDays + ", got: " + value);
                }
                persianDay = value;
                break;
            default:
                // For other fields, use standard mechanism
                super.set(field, value);
                return;
        }

        // Recompute Gregorian date and sync everything
        computeGregorianFromPersianFast();
        setTimeInMillis(gCal.getTimeInMillis());
        areFieldsSet = false;
        lastComputedTime = -1;
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
    public String toString() {
        return getLongDate();
    }

    // === FAST CONVERSION ALGORITHMS ===

    private static void gregorianToJalaliFast(int gy, int gm, int gd, int[] out) {
        int jy = (gm > 2) ? (gy + 1) : gy;

        // Fast month day offsets
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

        // Fast month calculation
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

    // === INTERNAL HELPERS ===

    private void setPersianDateInternal(int year, int month, int day) {
        this.persianYear = year;
        this.persianMonth = month;
        this.persianDay = day;
        this.isDirty = true;
        this.lastComputedTime = -1;
        this.areFieldsSet = false;

        computeGregorianFromPersianFast();
        setTimeInMillis(gCal.getTimeInMillis());
    }

    private void setInternalField(int field, int value) {
        fields[field] = value;
        isSet[field] = true;
    }

    private int internalGet(int field, int defaultValue) {
        return isSet[field] ? fields[field] : defaultValue;
    }

    // === REQUIRED ABSTRACT METHODS ===

    @Override
    public int getMinimum(int field) {
        switch (field) {
            case YEAR: return 1;
            case MONTH: return FARVARDIN;
            case DAY_OF_MONTH: return 1;
            case DAY_OF_WEEK: return SUNDAY;
            case HOUR_OF_DAY: return 0;
            case MINUTE: return 0;
            case SECOND: return 0;
            case MILLISECOND: return 0;
            case ERA: return AD;
            default: return 0;
        }
    }

    @Override
    public int getMaximum(int field) {
        switch (field) {
            case YEAR: return 9999;
            case MONTH: return ESFAND;
            case DAY_OF_MONTH: return 31;
            case DAY_OF_WEEK: return SATURDAY;
            case HOUR_OF_DAY: return 23;
            case MINUTE: return 59;
            case SECOND: return 59;
            case MILLISECOND: return 999;
            case WEEK_OF_MONTH: return 6;
            case WEEK_OF_YEAR: return 53;
            case ERA: return AD;
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
            case DAY_OF_MONTH: return 29;
            case WEEK_OF_MONTH: return 4;
            case WEEK_OF_YEAR: return 52;
            default: return getMaximum(field);
        }
    }

    // === COMPATIBILITY METHODS ===

    /**
     * For backward compatibility - returns current state as array
     */
    public int[] getYmd() {
        ensureComputed();
        return new int[]{persianYear, persianMonth, persianDay};
    }

    /**
     * Fast method to get day of week (1-7, where 1=Sunday, 7=Saturday)
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

    // === STATIC UTILITY METHODS ===

    public static FastPersianCalendar parseOrNullToCompat(String dateString) {
        return parseOrNullToCompat(dateString, "/");
    }

    public static FastPersianCalendar parseOrNullToCompat(String dateString, String delimiter) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        // Auto-detect delimiter
        if (delimiter == null || delimiter.isEmpty() || !dateString.contains(delimiter)) {
            if (dateString.contains("/")) delimiter = "/";
            else if (dateString.contains("-")) delimiter = "-";
            else if (dateString.contains(".")) delimiter = "\\.";
            else return null;
        }

        String[] tokens = dateString.split(delimiter);
        if (tokens.length != 3) return null;

        try {
            int year = Integer.parseInt(tokens[0].trim());
            int month = Integer.parseInt(tokens[1].trim()) - 1;
            int day = Integer.parseInt(tokens[2].trim());

            // Basic validation
            if (year < 1 || month < 0 || month > 11 || day < 1 || day > 31) {
                return null;
            }

            // Additional validation for actual month length
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
                ? MyPCConstants.WEEKDAY_NAMES[index]
                : MyPCConstants.WEEKDAY_NAMES_SHORT_IN_ENGLISH[index];
    }

    public static String getMonthName(int month, Locale locale) {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Month must be between 0 and 11, got: " + month);
        }

        return locale.getLanguage().equals("fa")
                ? MyPCConstants.MONTH_NAMES[month]
                : MyPCConstants.MONTH_NAMES_IN_ENGLISH[month];
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
}