package com.farashian.pcalendar;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static com.farashian.pcalendar.PCConstants.PERSIAN_MONTH_NAMES_SHORT;
import static com.farashian.pcalendar.NumberConvertor.convertToEnglishNumbers;
import static com.farashian.pcalendar.PCalendarUtils.getLocaleFromTimezone;
import static com.farashian.pcalendar.PersianCalendar.getMonthName;


@Keep
public final class PersianDateFormat {

    private static final String TAG = "PersianDateFormat";

    //Number Format
    public enum PersianDateNumberCharacter {
        ENGLISH,
        FARSI
    }

    private String                     pattern         = "dddd, d MMMM yyyy HH:mm:ss";
    private PersianDateNumberCharacter numberCharacter = PersianDateNumberCharacter.ENGLISH;
    private Locale                     locale;

    //Pattern matcher for tokens
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "yyyy|yy|MMMM|MMM|MM|M|dddd|ddd|dd|d|HH|H|hh|h|mm|m|ss|s|a|A|'[^']*'"
    );

    //Thread-safe formatter functions
    private static final Map<String, BiFunction<PersianCalendar, Locale, String>> FORMATTERS =
            Collections.unmodifiableMap(createFormatters());

    //Thread-local cache for SimpleDateFormat instances (important for performance)
    private static final ThreadLocal<SimpleDateFormat> sDateFormatCache =
            new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("yyyy/MM/dd", Locale.US);
                }
            };

    //Context for resource-based localization (optional)
    @Nullable
    private static Context appContext;


    public PersianDateFormat() {
        //Use device locale if Persian, otherwise use default Persian locale
        Locale defaultLocale = Locale.getDefault();
        this.locale = defaultLocale.getLanguage().equals("fa")
                ? defaultLocale
                : PCConstants.PERSIAN_LOCALE;
    }

    public PersianDateFormat(@NonNull String pattern) {
        this();
        setPattern(pattern);
    }

    public PersianDateFormat(@NonNull String pattern,
            @NonNull PersianDateNumberCharacter numberCharacter) {
        this();
        setPattern(pattern);
        setNumberCharacter(numberCharacter);
    }

    public PersianDateFormat(@NonNull String pattern,
            @NonNull PersianDateNumberCharacter numberCharacter,
            @NonNull Locale locale) {
        setPattern(pattern);
        setNumberCharacter(numberCharacter);
        setLocale(locale);
    }


    public void setPattern(@NonNull String pattern) {
        if (TextUtils.isEmpty(pattern)) {
            Log.w(TAG, "Pattern is empty, using default");
            this.pattern = "dddd, d MMMM yyyy HH:mm:ss";
        } else {
            this.pattern = pattern;
        }
    }

    public void setNumberCharacter(@NonNull PersianDateNumberCharacter numberCharacter) {
        this.numberCharacter = numberCharacter;
    }

    public void setLocale(@NonNull Locale locale) {
        this.locale = locale;
    }

    //Static method to set application context for resource-based localization
    public static void setAppContext(@Nullable Context context) {
        appContext = context != null ? context.getApplicationContext() : null;
    }


    @NonNull
    public static String format(@NonNull PersianCalendar date,
            @Nullable String pattern) {
        return format(date, pattern, PersianDateNumberCharacter.ENGLISH,
                      getDefaultLocale());
    }

    @NonNull
    public static String format(@NonNull PersianCalendar date,
            @Nullable String pattern,
            @NonNull PersianDateNumberCharacter numberCharacter) {
        return format(date, pattern, numberCharacter, getDefaultLocale());
    }

    @NonNull
    public static String format(@NonNull PersianCalendar date,
            @Nullable String pattern,
            @NonNull PersianDateNumberCharacter numberCharacter,
            @NonNull Locale locale) {
        if (TextUtils.isEmpty(pattern)) {
            pattern = "dddd, d MMMM yyyy HH:mm:ss";
        }

        StringBuilder           result    = new StringBuilder(pattern.length() + 50);
        java.util.regex.Matcher matcher   = TOKEN_PATTERN.matcher(pattern);
        int                     lastIndex = 0;

        while (matcher.find()) {
            //Append text between tokens
            result.append(pattern, lastIndex, matcher.start());

            String token = matcher.group();

            if (token.startsWith("'") && token.endsWith("'")) {
                //Literal text in quotes
                result.append(token, 1, token.length() - 1);
            } else if (FORMATTERS.containsKey(token)) {
                String formatted = FORMATTERS.get(token).apply(date, locale);
                result.append(formatted);
            } else {
                //Unknown token, keep as is
                result.append(token);
            }

            lastIndex = matcher.end();
        }

        //Append remaining text
        result.append(pattern.substring(lastIndex));

        String formattedString = result.toString();

        //Convert numbers to Farsi if requested
        if (numberCharacter == PersianDateNumberCharacter.FARSI) {
            formattedString = convertToFarsiNumbers(formattedString);
        }

        return formattedString;
    }

    @NonNull
    public String format(@NonNull PersianCalendar date) {
        return format(date, this.pattern, this.numberCharacter, this.locale);
    }

    public String format(YMD ymd, String pattern) {
        //Create a PersianCalendar object and set the date from YMD, and set time to zero.
        PersianCalendar pc = new PersianCalendar();
        // ✅ YMD month is 1-based, pass it directly
        pc.setPersianDate(ymd.year, ymd.month, ymd.day);
        pc.set(Calendar.HOUR_OF_DAY, 0);
        pc.set(Calendar.MINUTE, 0);
        pc.set(Calendar.SECOND, 0);
        pc.set(Calendar.MILLISECOND, 0);

        //Use the static format method with the instance's numberCharacter and locale
        return format(pc, pattern, this.numberCharacter, this.locale);
    }

    @NonNull
    public PersianCalendar parse(@NonNull String date) throws ParseException {
        return parse(date, this.pattern);
    }

    @NonNull
    public PersianCalendar parse(@NonNull String date,
            @NonNull String pattern) throws ParseException {
        Log.d(TAG, "Parsing date: " + date + " with pattern: " + pattern);

        if (TextUtils.isEmpty(date)) {
            throw new ParseException("Date string is null or empty", 0);
        }

        Map<String, Integer> values = new HashMap<>();
        values.put("year", 1400);
        // ✅ Store 1-based month for constructor
        values.put("month", 1);
        values.put("day", 1);
        values.put("hour", 0);
        values.put("minute", 0);
        values.put("second", 0);

        //Remove Farsi numbers and convert to English for parsing
        String normalizedDate = convertToEnglishNumbers(date);

        //Simple pattern matching for common formats
        if (pattern.equals("yyyy/MM/dd") || pattern.equals("yyyy-MM-dd")) {
            String   delimiter = pattern.contains("/") ? "/" : "-";
            String[] parts     = normalizedDate.split(Pattern.quote(delimiter));
            if (parts.length == 3) {
                values.put("year", safeParseInt(parts[0], 1400));
                // ✅ Parse as 1-based month (from string)
                values.put("month", safeParseInt(parts[1], 1));
                values.put("day", safeParseInt(parts[2], 1));
            } else {
                throw new ParseException("Invalid date format. Expected yyyy/MM/dd or yyyy-MM-dd", 0);
            }
        } else if (pattern.equals("yyyy/MM/dd HH:mm") || pattern.equals("yyyy-MM-dd HH:mm")) {
            String[] dateTimeParts = normalizedDate.split(" ");
            if (dateTimeParts.length == 2) {
                String datePart = dateTimeParts[0];
                String timePart = dateTimeParts[1];

                String   delimiter = pattern.contains("/") ? "/" : "-";
                String[] dateParts = datePart.split(Pattern.quote(delimiter));
                String[] timeParts = timePart.split(":");

                if (dateParts.length == 3 && timeParts.length >= 2) {
                    values.put("year", safeParseInt(dateParts[0], 1400));
                    // ✅ Parse as 1-based month
                    values.put("month", safeParseInt(dateParts[1], 1));
                    values.put("day", safeParseInt(dateParts[2], 1));
                    values.put("hour", safeParseInt(timeParts[0], 0));
                    values.put("minute", safeParseInt(timeParts[1], 0));
                    if (timeParts.length > 2) {
                        values.put("second", safeParseInt(timeParts[2], 0));
                    }
                } else {
                    throw new ParseException("Invalid date/time format", 0);
                }
            } else {
                throw new ParseException("Invalid date/time format. Expected yyyy/MM/dd HH:mm", 0);
            }
        } else if (pattern.equals("yyyy/MM/dd HH:mm:ss") || pattern.equals("yyyy-MM-dd HH:mm:ss")) {
            String[] dateTimeParts = normalizedDate.split(" ");
            if (dateTimeParts.length == 2) {
                String datePart = dateTimeParts[0];
                String timePart = dateTimeParts[1];

                String   delimiter = pattern.contains("/") ? "/" : "-";
                String[] dateParts = datePart.split(Pattern.quote(delimiter));
                String[] timeParts = timePart.split(":");

                if (dateParts.length == 3 && timeParts.length >= 3) {
                    values.put("year", safeParseInt(dateParts[0], 1400));
                    // ✅ Parse as 1-based month
                    values.put("month", safeParseInt(dateParts[1], 1));
                    values.put("day", safeParseInt(dateParts[2], 1));
                    values.put("hour", safeParseInt(timeParts[0], 0));
                    values.put("minute", safeParseInt(timeParts[1], 0));
                    values.put("second", safeParseInt(timeParts[2], 0));
                } else {
                    throw new ParseException("Invalid date/time format", 0);
                }
            } else {
                throw new ParseException("Invalid date/time format. Expected yyyy/MM/dd HH:mm:ss", 0);
            }
        } else {
            throw new ParseException("Unsupported pattern for parsing: " + pattern, 0);
        }

        //Validate the parsed values
        validateParsedDate(values);

        return new PersianCalendar(
                values.get("year"),
                values.get("month"),  // ✅ Already 1-based
                values.get("day"),
                values.get("hour"),
                values.get("minute"),
                values.get("second")
        );
    }

    @NonNull
    public PersianCalendar parseGrg(@NonNull String date) throws ParseException {
        return parseGrg(date, "yyyy/MM/dd");
    }

    @NonNull
    public PersianCalendar parseGrg(@NonNull String date,
            @NonNull String pattern) throws ParseException {
        if (TextUtils.isEmpty(date)) {
            throw new ParseException("Date string is null or empty", 0);
        }

        SimpleDateFormat sdf = sDateFormatCache.get();
        try {
            sdf.applyPattern(pattern);
            Date grgDate = sdf.parse(date);
            if (grgDate == null) {
                throw new ParseException("Failed to parse date: " + date, 0);
            }
            PersianCalendar persianCalendar = new PersianCalendar();
            persianCalendar.setTimeInMillis(grgDate.getTime());
            return persianCalendar;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing Gregorian date: " + date, e);
            throw new ParseException("Error parsing Gregorian date: " + e.getMessage(), 0);
        }
    }

    @NonNull
    private static Map<String, BiFunction<PersianCalendar, Locale, String>> createFormatters() {
        Map<String, BiFunction<PersianCalendar, Locale, String>> formatters = new ConcurrentHashMap<>();

        //Year
        formatters.put("yyyy", (cal, loc) -> String.format(loc, "%04d", cal.getYear()));
        formatters.put("yy", (cal, loc) -> String.format(loc, "%02d", cal.getYear() % 100));

        //Month
        formatters.put("MMMM", (cal, loc) -> getMonthName(cal.getMonth(), loc));  // getMonth() returns 1-based
        formatters.put("MMM", (cal, loc) -> getShortMonthName(cal.getMonth(), loc));
        // ✅ Format 1-based month (MM = 01, 02, ..., 12)
        formatters.put("MM", (cal, loc) -> String.format(loc, "%02d", cal.getMonth()));
        formatters.put("M", (cal, loc) -> String.valueOf(cal.getMonth()));

        //Day
        formatters.put("dddd", (cal, loc) -> getWeekdayName(cal.get(PersianCalendar.DAY_OF_WEEK), loc));
        formatters.put("ddd", (cal, loc) -> getShortDayName(cal.get(PersianCalendar.DAY_OF_WEEK), loc));
        formatters.put("dd", (cal, loc) -> String.format(loc, "%02d", cal.getDayOfMonth()));
        formatters.put("d", (cal, loc) -> String.format(loc, "%d", cal.getDayOfMonth()));

        //Hour
        formatters.put("HH", (cal, loc) -> String.format(loc, "%02d", cal.get(PersianCalendar.HOUR_OF_DAY)));
        formatters.put("H", (cal, loc) -> String.format(loc, "%d", cal.get(PersianCalendar.HOUR_OF_DAY)));
        formatters.put("hh", (cal, loc) -> {
            int hour12 = cal.get(PersianCalendar.HOUR_OF_DAY) % 12;
            hour12 = hour12 == 0 ? 12 : hour12;
            return String.format(loc, "%02d", hour12);
        });
        formatters.put("h", (cal, loc) -> {
            int hour12 = cal.get(PersianCalendar.HOUR_OF_DAY) % 12;
            hour12 = hour12 == 0 ? 12 : hour12;
            return String.valueOf(hour12);
        });

        //Minute
        formatters.put("mm", (cal, loc) -> String.format(loc, "%02d", cal.get(PersianCalendar.MINUTE)));
        formatters.put("m", (cal, loc) -> String.valueOf(cal.get(PersianCalendar.MINUTE)));

        //Second
        formatters.put("ss", (cal, loc) -> String.format(loc, "%02d", cal.get(PersianCalendar.SECOND)));
        formatters.put("s", (cal, loc) -> String.valueOf(cal.get(PersianCalendar.SECOND)));

        //AM/PM
        formatters.put("a", (cal, loc) -> getAmPm(cal, loc, false));
        formatters.put("A", (cal, loc) -> getAmPm(cal, loc, true));

        return formatters;
    }

    @NonNull
    private static String getShortMonthName(int month, @NonNull Locale locale) {
        // ✅ Month is 1-based, convert to 0-based for array access
        int month0 = month - 1;
        String fullName = PERSIAN_MONTH_NAMES_SHORT[month0];
        if (locale.getLanguage().equals("fa")) {
            //For Persian, return the full name as short name (common in Persian)
            return fullName;
        } else {
            //For English, return first 3 letters
            String[] englishMonths = {"Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
                    "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"};
            String fullEnglish = englishMonths[month0];
            return fullEnglish.substring(0, Math.min(3, fullEnglish.length()));
        }
    }

    @NonNull
    private static String getShortDayName(int dayOfWeek, @NonNull Locale locale) {
        String fullName = getWeekdayName(dayOfWeek, locale);
        if (locale.getLanguage().equals("fa")) {
            //For Persian, return the full name as short name
            return fullName;
        } else {
            //For English, return first 3 letters
            String[] englishDays = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
            int      index       = (dayOfWeek - 1) % 7;
            if (index < 0) index += 7;
            String fullEnglish = englishDays[index];
            return fullEnglish.substring(0, Math.min(3, fullEnglish.length()));
        }
    }

    @NonNull
    private static String getAmPm(@NonNull PersianCalendar cal,
            @NonNull Locale locale,
            boolean uppercase) {
        int     hour = cal.get(PersianCalendar.HOUR_OF_DAY);
        boolean isAm = hour < 12;

        if (locale.getLanguage().equals("fa")) {
            return isAm ? (uppercase ? "ق.ظ" : "ق.ظ") : (uppercase ? "ب.ظ" : "ب.ظ");
        } else {
            return isAm ? (uppercase ? "AM" : "am") : (uppercase ? "PM" : "pm");
        }
    }

    @NonNull
    private static String convertToFarsiNumbers(@NonNull String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }

        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') {
                //Convert ASCII digit to Persian digit
                result.append((char) ('۰' + (c - '0')));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }


    private int safeParseInt(@NonNull String str, int defaultValue) {
        if (TextUtils.isEmpty(str)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            Log.w(TAG, "Failed to parse integer: " + str, e);
            return defaultValue;
        }
    }

    private void validateParsedDate(@NonNull Map<String, Integer> values) throws ParseException {
        int year   = values.get("year");
        int month  = values.get("month");  // ✅ 1-based month
        int day    = values.get("day");
        int hour   = values.get("hour");
        int minute = values.get("minute");
        int second = values.get("second");

        if (year < 1 || year > 9999) {
            throw new ParseException("Invalid year: " + year + ". Must be between 1-9999", 0);
        }

        // ✅ Validate 1-based month
        if (month < 1 || month > 12) {
            throw new ParseException("Invalid month: " + month + ". Must be between 1-12", 0);
        }

        int maxDays = getDaysInMonth(year, month);
        if (day < 1 || day > maxDays) {
            throw new ParseException("Invalid day: " + day + " for month " + month +
                                     ". Must be between 1-" + maxDays, 0);
        }

        if (hour < 0 || hour > 23) {
            throw new ParseException("Invalid hour: " + hour + ". Must be between 0-23", 0);
        }

        if (minute < 0 || minute > 59) {
            throw new ParseException("Invalid minute: " + minute + ". Must be between 0-59", 0);
        }

        if (second < 0 || second > 59) {
            throw new ParseException("Invalid second: " + second + ". Must be between 0-59", 0);
        }
    }

    private int getDaysInMonth(int year, int month) {
        // ✅ Month is 1-based, convert to 0-based for calculation
        int month0 = month - 1;

        if (month0 < 0 || month0 > 11) {
            return 31; //Fallback
        }

        if (month0 < 6) {
            return 31;
        } else if (month0 < 11) {
            return 30;
        } else {
            return PersianCalendar.isLeapYear(year) ? 30 : 29;
        }
    }

    @NonNull
    public static String getDayName(@NonNull PersianCalendar date) {
        return getWeekdayName(date.get(PersianCalendar.DAY_OF_WEEK), getDefaultLocale());
    }

    public static boolean isLeapYear(@NonNull PersianCalendar date) {
        return PersianCalendar.isLeapYear(date.getYear());
    }

    @NonNull
    public static String getWeekdayName(int dayOfWeek, @NonNull Locale locale) {
        int index = (dayOfWeek - 1) % 7;
        if (index < 0) index += 7;

        //Try to get from Android resources first if context is available
        if (appContext != null) {
            try {
                String resName = "weekday_" + (index + 1);
                int resId = appContext.getResources().getIdentifier(
                        resName, "string", appContext.getPackageName()
                );
                if (resId != 0) {
                    return appContext.getString(resId);
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to load weekday name from resources", e);
            }
        }

        boolean isPersian = locale.getLanguage().equals("fa");
        return isPersian
                ? PCConstants.WEEKDAY_NAMES[index]
                : PCConstants.WEEKDAY_NAMES_SHORT_IN_ENGLISH[index];
    }

    //Helper method to get default locale
    @NonNull
    private static Locale getDefaultLocale() {
        Locale defaultLocale = getLocaleFromTimezone();
        return defaultLocale.getLanguage().equals("fa")
                ? defaultLocale
                : PCConstants.PERSIAN_LOCALE;
    }

    //=== BUILDER PATTERN (Optional but useful) ===

    public static class Builder {
        private String                     pattern         = "dddd, d MMMM yyyy HH:mm:ss";
        private PersianDateNumberCharacter numberCharacter = PersianDateNumberCharacter.ENGLISH;
        private Locale                     locale          = getDefaultLocale();

        public Builder() {
        }

        public Builder pattern(@NonNull String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder numberCharacter(@NonNull PersianDateNumberCharacter numberCharacter) {
            this.numberCharacter = numberCharacter;
            return this;
        }

        public Builder locale(@NonNull Locale locale) {
            this.locale = locale;
            return this;
        }

        @NonNull
        public PersianDateFormat build() {
            return new PersianDateFormat(pattern, numberCharacter, locale);
        }
    }

    //=== EQUALS AND HASHCODE ===

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersianDateFormat that = (PersianDateFormat) o;

        if (!pattern.equals(that.pattern)) return false;
        if (numberCharacter != that.numberCharacter) return false;
        return locale.equals(that.locale);
    }

    @Override
    public int hashCode() {
        int result = pattern.hashCode();
        result = 31 * result + numberCharacter.hashCode();
        result = 31 * result + locale.hashCode();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "PersianDateFormat{" +
               "pattern='" + pattern + '\'' +
               ", numberCharacter=" + numberCharacter +
               ", locale=" + locale +
               '}';
    }
}