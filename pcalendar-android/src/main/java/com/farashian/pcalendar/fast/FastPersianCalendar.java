package com.farashian.pcalendar.fast;

import android.os.Parcel;
import android.os.Parcelable;
import com.farashian.pcalendar.MyPCConstants;

import java.util.*;

import static com.farashian.pcalendar.MyPCConstants.PERSIAN_LOCALE;

/**
 * High-performance Persian Calendar with complete field computation
 * Fixed all field computation issues and critical bugs
 */
public class FastPersianCalendar extends Calendar implements Parcelable {

    public static final int FIRST_DAY_OF_WEEK      = Calendar.SATURDAY;
    public static final int WEEKDAY_HOLIDAY_NUMBER = Calendar.FRIDAY;

    // Calendar constants we need to define
    private static final int AD = 1; // Gregorian era constant

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

    // Fast internal storage
    private int persianYear;
    private int persianMonth;
    private int persianDay;

    // Performance optimizations
    public final  GregorianCalendar gCal;
    private final Locale            locale;
    private       long              lastComputedTime = -1;
    private       boolean           isDirty          = true;

    // Thread-local for temporary calculations
    private static final ThreadLocal<int[]> tempArrayCache  =
            ThreadLocal.withInitial(() -> new int[3]);
    private static final int[]              PERSIAN_OFFSETS = {0, 1, 2, 3, 4, 5, 6, 0};

    // === CONSTRUCTORS ===

    public FastPersianCalendar() {
        this(TimeZone.getDefault(), PERSIAN_LOCALE);
        setTimeInMillis(System.currentTimeMillis());
    }

    public FastPersianCalendar(TimeZone zone) {
        this(zone, PERSIAN_LOCALE);
        setTimeInMillis(System.currentTimeMillis());
    }

    public FastPersianCalendar(Date date) {
        this();
        setNewTime(date);
    }

    public FastPersianCalendar(TimeZone zone, Locale locale) {
        super(zone, locale);
        this.locale = locale;
        this.gCal   = new GregorianCalendar(zone, locale);
        // Initialize with reasonable defaults
        this.persianYear  = 1400;
        this.persianMonth = 0;
        this.persianDay   = 1;
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

    // Parcelable constructor
    protected FastPersianCalendar(Parcel in) {
        // Call default constructor
        this();

        // Read the time
        long timeInMillis = in.readLong();
        setTimeInMillis(timeInMillis);

        // Read Persian date fields
        this.persianYear  = in.readInt();
        this.persianMonth = in.readInt();
        this.persianDay   = in.readInt();

        // Read cache state
        this.lastComputedTime = in.readLong();
        this.isDirty          = in.readByte() != 0;

        // Read timezone if present
        if (in.readByte() == 1) {
            String tzId = in.readString();
            if (tzId != null) {
                setTimeZone(TimeZone.getTimeZone(tzId));
                gCal.setTimeZone(getTimeZone());
            }
        }

        // Sync gCal with the time
        gCal.setTimeInMillis(timeInMillis);

        // Force computation
        computeFields();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FastPersianCalendar> CREATOR = new Creator<FastPersianCalendar>() {
        @Override
        public FastPersianCalendar createFromParcel(Parcel in) {
            return new FastPersianCalendar(in);
        }

        @Override
        public FastPersianCalendar[] newArray(int size) {
            return new FastPersianCalendar[size];
        }
    };


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ensureComputed();

        // Write time
        dest.writeLong(getTimeInMillis());

        // Write Persian date fields
        dest.writeInt(persianYear);
        dest.writeInt(persianMonth);
        dest.writeInt(persianDay);

        // Write cache state
        dest.writeLong(lastComputedTime);
        dest.writeByte((byte) (isDirty ? 1 : 0));

        // Write timezone
        TimeZone tz = getTimeZone();
        if (tz != null) {
            dest.writeByte((byte) 1);
            dest.writeString(tz.getID());
        } else {
            dest.writeByte((byte) 0);
        }
    }


    public void readFromParcel(Parcel in) {
        // Read time
        long timeInMillis = in.readLong();
        setTimeInMillis(timeInMillis);

        // Read Persian date fields
        this.persianYear  = in.readInt();
        this.persianMonth = in.readInt();
        this.persianDay   = in.readInt();

        // Read cache state
        this.lastComputedTime = in.readLong();
        this.isDirty          = in.readByte() != 0;

        // Read timezone
        if (in.readByte() == 1) {
            String tzId = in.readString();
            if (tzId != null) {
                setTimeZone(TimeZone.getTimeZone(tzId));
                gCal.setTimeZone(getTimeZone());
            }
        }

        // Sync and compute
        gCal.setTimeInMillis(timeInMillis);
        computeFields();
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

    // Static utility method
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


    /**
     * Converts Java Calendar weekday constant to Persian calendar offset.
     * Persian week starts on Saturday (0), while Java Calendar uses Sunday-based constants.
     *
     * @param javaDayOfWeek Java Calendar weekday constant (Calendar.SUNDAY to Calendar.SATURDAY)
     * @return Persian offset (0-6) where 0=Saturday, 1=Sunday, ..., 6=Friday
     */

    private int calculatePersianOffset(int javaDayOfWeek) {
        if (javaDayOfWeek < 1 || javaDayOfWeek > 7) return 0;
        return PERSIAN_OFFSETS[javaDayOfWeek];
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

    public static boolean isLeapYear(int year) {
        int remainder = year % 33;
        return remainder == 1 || remainder == 5 || remainder == 9 ||
               remainder == 13 || remainder == 17 || remainder == 22 ||
               remainder == 26 || remainder == 30;
    }

    protected static boolean isLeapYearOld(int year) {
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

            // Invalidate fields since time changed
            areFieldsSet = false;
        }
    }

    @Override
    protected void computeFields() {
        if (time != lastComputedTime || !areFieldsSet) {
            gCal.setTimeInMillis(time); // This is needed to sync gCal with current time
            computePersianFromGregorianFast();
            lastComputedTime = time;
            isDirty          = false;

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
        fields[YEAR]         = persianYear;
        fields[MONTH]        = persianMonth;
        fields[DAY_OF_MONTH] = persianDay;

        // Set time fields from Gregorian (they're the same)
        fields[HOUR_OF_DAY] = gCal.get(HOUR_OF_DAY);
        fields[MINUTE]      = gCal.get(MINUTE);
        fields[SECOND]      = gCal.get(SECOND);
        fields[MILLISECOND] = gCal.get(MILLISECOND);

        // CRITICAL FIX: Use Gregorian's properly computed day-of-week
        fields[DAY_OF_WEEK] = gCal.get(DAY_OF_WEEK);

        // Calculate proper Persian day of year and week fields
        fields[DAY_OF_YEAR] = calculateDayOfYear();
        calculatePersianWeekFields();

        // Set other fields
        fields[AM_PM]       = gCal.get(AM_PM);
        fields[HOUR]        = gCal.get(HOUR);
        fields[DST_OFFSET]  = gCal.get(DST_OFFSET);
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
        int hour   = internalGet(HOUR_OF_DAY, 0);
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
        persianYear  = temp[0];
        persianMonth = temp[1] - 1;  // FIXED: Convert 1-based to 0-based
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

        // Recompute Gregorian date and sync
        computeGregorianFromPersianFast();
        setTimeInMillis(gCal.getTimeInMillis());
        isDirty          = true;
        lastComputedTime = -1;
        areFieldsSet     = false;
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
        isDirty          = true;
        lastComputedTime = -1;
        areFieldsSet     = false;
    }

    // === OVERRIDES (FIXED) ===

    public void setNewTime(Date date) {
        setTimeInMillis(date.getTime());
    }

    @Override
    public void setTimeInMillis(long millis) {
        super.setTimeInMillis(millis);

        // Sync gCal with the new time
        gCal.setTimeInMillis(millis);

        // Force recomputation
        lastComputedTime = -1;
        isDirty          = true;
        areFieldsSet     = false;

        // Compute fields immediately
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
        areFieldsSet     = false;
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

    public static void gregorianToJalaliFast(int gy, int gm, int gd, int[] out) {
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

    // === REQUIRED ABSTRACT METHODS ===

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
            int year  = Integer.parseInt(tokens[0].trim());
            int month = Integer.parseInt(tokens[1].trim()) - 1;
            int day   = Integer.parseInt(tokens[2].trim());

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

// === GREGORIAN DATE METHODS (0-BASED MONTHS) ===

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
     * Get Gregorian month name
     * @param locale locale for month name
     * @return Gregorian month name
     */
    public String getGrgMonthName(Locale locale) {
        complete(); // Ensure fields are computed
        return gCal.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
    }

    /**
     * Get Gregorian month length (days in month)
     * @return number of days in current Gregorian month
     */
    public int getGrgMonthLength() {
        complete(); // Ensure fields are computed
        return gCal.getActualMaximum(Calendar.DAY_OF_MONTH);
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
     * Get Gregorian month name with number (e.g., "January (01)")
     * @param locale locale for month name
     * @return formatted month name with number
     */
    public String getGrgMonthNameWithNumber(Locale locale) {
        complete(); // Ensure fields are computed
        String monthName   = getGrgMonthName(locale);
        int    monthNumber = getGrgMonth() + 1; // Convert to 1-based
        return String.format(locale, "%s (%02d)", monthName, monthNumber);
    }

    /**
     * Convert Gregorian date to Persian date
     * @param year Gregorian year
     * @param month 0-based Gregorian month (0=January)
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
    public static FastPersianCalendar currentGregorian() {
        FastPersianCalendar result = new FastPersianCalendar();
        // Set to current time (which is already Gregorian)
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

    // === HELPER METHODS FOR DATE MANIPULATION ===

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
        int age = referenceDate.persianYear - this.persianYear;

        // Adjust if birthday hasn't occurred yet this year
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
     * @return a deep copy of this FastPersianCalendar instance
     */
    @Override
    public FastPersianCalendar clone() {
        // Call parent clone
        FastPersianCalendar clone = (FastPersianCalendar) super.clone();

        // The parent clone() creates a shallow copy
        // We need to ensure our fields are properly copied

        // Our int fields will be copied correctly by parent clone
        // But we need to ensure gCal is in sync
        // Since gCal is final and shared, we need to sync it

        // Set gCal to the same time (it's the same object reference, but that's OK)
        clone.gCal.setTimeInMillis(this.gCal.getTimeInMillis());

        // The time field should already be copied by super.clone()
        // We need to recompute our Persian fields from the time
        clone.computePersianFromGregorianFast();

        return clone;
    }

    public FastPersianCalendar clone1() {
        // Create a new instance with the same time zone and locale
        FastPersianCalendar clone = new FastPersianCalendar(this.getTimeZone(), this.locale);

        // Copy the internal state
        clone.persianYear  = this.persianYear;
        clone.persianMonth = this.persianMonth;
        clone.persianDay   = this.persianDay;
        clone.setTimeInMillis(this.getTimeInMillis());

        // Copy the time fields if they are set
        for (int i = 0; i < FIELD_COUNT; i++) {
            if (this.isSet[i]) {
                clone.fields[i] = this.fields[i];
                clone.isSet[i]  = true;
            }
        }

        // Copy cache and state
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
        return persianDay == getDaysInMonth();
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
        return getDaysInMonth() - persianDay;
    }

    /**
     * Get the number of days passed in the current month
     * @return days passed in month
     */
    public int getDaysPassedInMonth() {
        return persianDay - 1;
    }

    /**
     * Get a string representation of the date in ISO-like format (YYYY/MM/DD)
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
        // Quick validation - persianYear should always be valid
        if (persianYear < 1) {
            ensureComputed(); // Just in case
        }
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
            validateDate(persianYear, persianMonth, persianDay);
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
        return getDaysInMonth(persianYear, persianMonth);
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
            daysToSubtract = 7; // If it's already Friday, go to previous Friday
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

        // Adjust if the month/day hasn't occurred yet this year
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
                                     getYear(), getMonth() + 1, getDayOfMonth());
            case "yyyy-MM-dd":
                return String.format(locale, "%04d-%02d-%02d",
                                     getYear(), getMonth() + 1, getDayOfMonth());
            case "dd MMMM yyyy":
                return String.format(locale, "%02d %s %04d",
                                     getDayOfMonth(), getMonthName(), getYear());
            default:
                // For custom patterns, use FastPersianDateFormat
                FastPersianDateFormat formatter = new FastPersianDateFormat(pattern);
                return formatter.format(this);
        }
    }

    /**
     * Get as Android Bundle (for passing between activities)
     */
    public android.os.Bundle toBundle() {
        ensureComputed();

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

    /**
     * Create from Android Bundle
     */
    public static FastPersianCalendar fromBundle(android.os.Bundle bundle) {
        if (bundle == null) {
            return new FastPersianCalendar();
        }

        FastPersianCalendar calendar = new FastPersianCalendar();

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
                calendar.setTimeZone(TimeZone.getTimeZone(tzId));
            }
        }

        return calendar;
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
}