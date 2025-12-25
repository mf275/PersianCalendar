package com.farashian.pcalendar;

import com.farashian.pcalendar.util.Log;
import com.farashian.pcalendar.util.TextUtils;

import javax.naming.Context;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static com.farashian.pcalendar.PCConstants.PERSIAN_MONTH_NAMES_SHORT;


public final class PersianDateFormat {
    
    private static final String TAG = "PersianDateFormat";

    // Number Format
    public enum PersianDateNumberCharacter {
        ENGLISH,
        FARSI
    }
    
    private String pattern = "dddd, d MMMM yyyy HH:mm:ss";
    private PersianDateNumberCharacter numberCharacter = PersianDateNumberCharacter.ENGLISH;
    private Locale locale;

    // Pattern matcher for tokens
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
        "yyyy|yy|MMMM|MMM|MM|M|dddd|ddd|dd|d|HH|H|hh|h|mm|m|ss|s|a|A|'[^']*'"
    );

    // Thread-safe formatter functions
    private static final Map<String, BiFunction<PersianCalendar, Locale, String>> FORMATTERS =
        Collections.unmodifiableMap(createFormatters());

    // Thread-local cache for SimpleDateFormat instances (important for performance)
    private static final ThreadLocal<SimpleDateFormat> sDateFormatCache = 
        new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            }
        };

    // Context for resource-based localization (optional)
    
    private static Context appContext;

    
    public PersianDateFormat() {
        // Use device locale if Persian, otherwise use default Persian locale
        Locale defaultLocale = Locale.getDefault();
        this.locale = defaultLocale.getLanguage().equals("fa") 
            ? defaultLocale 
            : PCConstants.PERSIAN_LOCALE;
    }
    
    public PersianDateFormat(String pattern) {
        this();
        setPattern(pattern);
    }
    
    public PersianDateFormat(String pattern,
                               PersianDateNumberCharacter numberCharacter) {
        this();
        setPattern(pattern);
        setNumberCharacter(numberCharacter);
    }
    
    public PersianDateFormat(String pattern,
                               PersianDateNumberCharacter numberCharacter, 
                               Locale locale) {
        setPattern(pattern);
        setNumberCharacter(numberCharacter);
        setLocale(locale);
    }

    
    public void setPattern(String pattern) {
        if (TextUtils.isEmpty(pattern)) {
            Log.w(TAG, "Pattern is empty, using default");
            this.pattern = "dddd, d MMMM yyyy HH:mm:ss";
        } else {
            this.pattern = pattern;
        }
    }
    
    public void setNumberCharacter(PersianDateNumberCharacter numberCharacter) {
        this.numberCharacter = numberCharacter;
    }
    
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    public static String format(PersianCalendar date,
                                String pattern) {
        return format(date, pattern, PersianDateNumberCharacter.ENGLISH,
                      getDefaultLocale());
    }
    
    
    public static String format(PersianCalendar date,
                                String pattern, 
                                PersianDateNumberCharacter numberCharacter) {
        return format(date, pattern, numberCharacter, getDefaultLocale());
    }
    
    
    public static String format(PersianCalendar date,
                                String pattern,
                                PersianDateNumberCharacter numberCharacter, 
                                Locale locale) {
        if (TextUtils.isEmpty(pattern)) {
            pattern = "dddd, d MMMM yyyy HH:mm:ss";
        }
        
        StringBuilder result = new StringBuilder(pattern.length() + 50);
        java.util.regex.Matcher matcher = TOKEN_PATTERN.matcher(pattern);
        int lastIndex = 0;
        
        while (matcher.find()) {
            // Append text between tokens
            result.append(pattern, lastIndex, matcher.start());
            
            String token = matcher.group();
            
            if (token.startsWith("'") && token.endsWith("'")) {
                // Literal text in quotes
                result.append(token, 1, token.length() - 1);
            } else if (FORMATTERS.containsKey(token)) {
                String formatted = FORMATTERS.get(token).apply(date, locale);
                result.append(formatted);
            } else {
                // Unknown token, keep as is
                result.append(token);
            }
            
            lastIndex = matcher.end();
        }
        
        // Append remaining text
        result.append(pattern.substring(lastIndex));
        
        String formattedString = result.toString();
        
        // Convert numbers to Farsi if requested
        if (numberCharacter == PersianDateNumberCharacter.FARSI) {
            formattedString = convertToFarsiNumbers(formattedString);
        }
        
        return formattedString;
    }

    public String format(PersianCalendar date) {
        return format(date, this.pattern, this.numberCharacter, this.locale);
    }

    public String format(YMD ymd, String pattern) {
        // Create a PersianCalendar object and set the date from YMD, and set time to zero.
        PersianCalendar pc = new PersianCalendar();
        pc.setPersianDate(ymd.year, ymd.month - 1, ymd.day);
        pc.set(Calendar.HOUR_OF_DAY, 0);
        pc.set(Calendar.MINUTE, 0);
        pc.set(Calendar.SECOND, 0);
        pc.set(Calendar.MILLISECOND, 0);

        // Use the static format method with the instance's numberCharacter and locale
        return format(pc, pattern, this.numberCharacter, this.locale);
    }
    
    public PersianCalendar parse(String date) throws ParseException {
        return parse(date, this.pattern);
    }
    
    public PersianCalendar parse(String date,
                                   String pattern) throws ParseException {
        Log.d(TAG, "Parsing date: " + date + " with pattern: " + pattern);
        
        if (TextUtils.isEmpty(date)) {
            throw new ParseException("Date string is null or empty", 0);
        }
        
        Map<String, Integer> values = new HashMap<>();
        values.put("year", 1400);
        values.put("month", 0);
        values.put("day", 1);
        values.put("hour", 0);
        values.put("minute", 0);
        values.put("second", 0);
        
        // Remove Farsi numbers and convert to English for parsing
        String normalizedDate = convertToEnglishNumbers(date);
        
        // Simple pattern matching for common formats
        if (pattern.equals("yyyy/MM/dd") || pattern.equals("yyyy-MM-dd")) {
            String delimiter = pattern.contains("/") ? "/" : "-";
            String[] parts = normalizedDate.split(Pattern.quote(delimiter));
            if (parts.length == 3) {
                values.put("year", safeParseInt(parts[0], 1400));
                values.put("month", safeParseInt(parts[1], 1) - 1); // Convert to 0-based
                values.put("day", safeParseInt(parts[2], 1));
            } else {
                throw new ParseException("Invalid date format. Expected yyyy/MM/dd or yyyy-MM-dd", 0);
            }
        } else if (pattern.equals("yyyy/MM/dd HH:mm") || pattern.equals("yyyy-MM-dd HH:mm")) {
            String[] dateTimeParts = normalizedDate.split(" ");
            if (dateTimeParts.length == 2) {
                String datePart = dateTimeParts[0];
                String timePart = dateTimeParts[1];
                
                String delimiter = pattern.contains("/") ? "/" : "-";
                String[] dateParts = datePart.split(Pattern.quote(delimiter));
                String[] timeParts = timePart.split(":");
                
                if (dateParts.length == 3 && timeParts.length >= 2) {
                    values.put("year", safeParseInt(dateParts[0], 1400));
                    values.put("month", safeParseInt(dateParts[1], 1) - 1);
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
                
                String delimiter = pattern.contains("/") ? "/" : "-";
                String[] dateParts = datePart.split(Pattern.quote(delimiter));
                String[] timeParts = timePart.split(":");
                
                if (dateParts.length == 3 && timeParts.length >= 3) {
                    values.put("year", safeParseInt(dateParts[0], 1400));
                    values.put("month", safeParseInt(dateParts[1], 1) - 1);
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
        
        // Validate the parsed values
        validateParsedDate(values);
        
        return new PersianCalendar(
            values.get("year"),
            values.get("month"),
            values.get("day"),
            values.get("hour"),
            values.get("minute"),
            values.get("second")
        );
    }

    public PersianCalendar parseGrg(String date) throws ParseException {
        return parseGrg(date, "yyyy/MM/dd");
    }
    
    
    public PersianCalendar parseGrg(String date,
                                      String pattern) throws ParseException {
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

    // === PRIVATE HELPER METHODS ===
    
    
    private static Map<String, BiFunction<PersianCalendar, Locale, String>> createFormatters() {
        Map<String, BiFunction<PersianCalendar, Locale, String>> formatters = new ConcurrentHashMap<>();
        
        // Year
        formatters.put("yyyy", (cal, loc) -> String.format(loc, "%04d", cal.getYear()));
        formatters.put("yy", (cal, loc) -> String.format(loc, "%02d", cal.getYear() % 100));
        
        // Month
        formatters.put("MMMM", (cal, loc) -> getMonthName(cal.getMonth(), loc));
        formatters.put("MMM", (cal, loc) -> getShortMonthName(cal.getMonth(), loc));
        formatters.put("MM", (cal, loc) -> String.format(loc, "%02d", cal.getMonth() + 1));
        formatters.put("M", (cal, loc) -> String.format("%d", cal.getMonth() + 1));
        
        // Day
        formatters.put("dddd", (cal, loc) -> getWeekdayName(cal.get(PersianCalendar.DAY_OF_WEEK), loc));
        formatters.put("ddd", (cal, loc) -> getShortDayName(cal.get(PersianCalendar.DAY_OF_WEEK), loc));
        formatters.put("dd", (cal, loc) -> String.format(loc, "%02d", cal.getDayOfMonth()));
        formatters.put("d", (cal, loc) -> String.format("%d", cal.getDayOfMonth()));

        // Hour
        formatters.put("HH", (cal, loc) -> String.format(loc, "%02d", cal.get(PersianCalendar.HOUR_OF_DAY)));
        formatters.put("H", (cal, loc) -> String.format(loc, "%d", cal.get(PersianCalendar.HOUR_OF_DAY)));
        //formatters.put("H", (cal, loc) -> String.valueOf(cal.get(PersianCalendar.HOUR_OF_DAY)));
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
        
        // Minute
        formatters.put("mm", (cal, loc) -> String.format(loc, "%02d", cal.get(PersianCalendar.MINUTE)));
        formatters.put("m", (cal, loc) -> String.valueOf(cal.get(PersianCalendar.MINUTE)));
        
        // Second
        formatters.put("ss", (cal, loc) -> String.format(loc, "%02d", cal.get(PersianCalendar.SECOND)));
        formatters.put("s", (cal, loc) -> String.valueOf(cal.get(PersianCalendar.SECOND)));
        
        // AM/PM
        formatters.put("a", (cal, loc) -> getAmPm(cal, loc, false));
        formatters.put("A", (cal, loc) -> getAmPm(cal, loc, true));
        
        return formatters;
    }
    
    
    private static String getShortMonthName(int month, Locale locale) {
        String fullName = PERSIAN_MONTH_NAMES_SHORT[month];
        if (locale.getLanguage().equals("fa")) {
            // For Persian, return the full name as short name (common in Persian)
            return fullName;
        } else {
            // For English, return first 3 letters
            String[] englishMonths = {"Far", "Ord", "Kho", "Tir", "Mor", "Sha",
                    "Meh", "Aba", "Aza", "Dey", "Bah", "Esf"};
            String fullEnglish = englishMonths[month];
            return fullEnglish.substring(0, Math.min(3, fullEnglish.length()));
        }
    }
    
    
    private static String getShortDayName(int dayOfWeek, Locale locale) {
        String fullName = getWeekdayName(dayOfWeek, locale);
        if (locale.getLanguage().equals("fa")) {
            // For Persian, return the full name as short name
            return fullName;
        } else {
            // For English, return first 3 letters
            String[] englishDays = {"Saturday", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
            int index = (dayOfWeek - 1) % 7;
            if (index < 0) index += 7;
            String fullEnglish = englishDays[index];
            return fullEnglish.substring(0, Math.min(3, fullEnglish.length()));
        }
    }
    
    
    private static String getAmPm(PersianCalendar cal,
                                  Locale locale, 
                                  boolean uppercase) {
        int hour = cal.get(PersianCalendar.HOUR_OF_DAY);
        boolean isAm = hour < 12;
        
        if (locale.getLanguage().equals("fa")) {
            return isAm ? (uppercase ? "ق.ظ" : "ق.ظ") : (uppercase ? "ب.ظ" : "ب.ظ");
        } else {
            return isAm ? (uppercase ? "AM" : "am") : (uppercase ? "PM" : "pm");
        }
    }
    
    
    private static String convertToFarsiNumbers(String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') {
                // Convert ASCII digit to Persian digit
                result.append((char) ('۰' + (c - '0')));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    
    private static String convertToEnglishNumbers(String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '۰' && c <= '۹') {
                // Convert Persian digit to ASCII digit
                result.append((char) ('0' + (c - '۰')));
            } else if (c >= '٠' && c <= '٩') {
                // Convert Arabic-Indic digit to ASCII digit
                result.append((char) ('0' + (c - '٠')));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    private int safeParseInt(String str, int defaultValue) {
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
    
    private void validateParsedDate(Map<String, Integer> values) throws ParseException {
        int year = values.get("year");
        int month = values.get("month");
        int day = values.get("day");
        int hour = values.get("hour");
        int minute = values.get("minute");
        int second = values.get("second");
        
        if (year < 1 || year > 9999) {
            throw new ParseException("Invalid year: " + year + ". Must be between 1-9999", 0);
        }
        
        if (month < 0 || month > 11) {
            throw new ParseException("Invalid month: " + (month + 1) + ". Must be between 1-12", 0);
        }
        
        int maxDays = getDaysInMonth(year, month);
        if (day < 1 || day > maxDays) {
            throw new ParseException("Invalid day: " + day + " for month " + (month + 1) + 
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
        if (month < 0 || month > 11) {
            return 31; // Fallback
        }
        
        if (month < 6) {
            return 31;
        } else if (month < 11) {
            return 30;
        } else {
            return PersianCalendar.isLeapYear(year) ? 30 : 29;
        }
    }

    // === UTILITY METHODS FOR EXTERNAL USE ===
    
    
    public static String getMonthName(PersianCalendar date) {
        return getMonthName(date.getMonth(), getDefaultLocale());
    }
    
    
    public static String getDayName(PersianCalendar date) {
        return getWeekdayName(date.get(PersianCalendar.DAY_OF_WEEK), getDefaultLocale());
    }
    
    public static boolean isLeapYear(PersianCalendar date) {
        return PersianCalendar.isLeapYear(date.getYear());
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


    // Helper method to get default locale
    
    private static Locale getDefaultLocale() {
        Locale defaultLocale = Locale.getDefault();
        return defaultLocale.getLanguage().equals("fa") 
            ? defaultLocale 
            : PCConstants.PERSIAN_LOCALE;
    }

    // === BUILDER PATTERN (Optional but useful) ===
    
    public static class Builder {
        private String pattern = "dddd, d MMMM yyyy HH:mm:ss";
        private PersianDateNumberCharacter numberCharacter = PersianDateNumberCharacter.ENGLISH;
        private Locale locale = getDefaultLocale();
        
        public Builder() {}
        
        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }
        
        public Builder numberCharacter(PersianDateNumberCharacter numberCharacter) {
            this.numberCharacter = numberCharacter;
            return this;
        }
        
        public Builder locale(Locale locale) {
            this.locale = locale;
            return this;
        }
        
        
        public PersianDateFormat build() {
            return new PersianDateFormat(pattern, numberCharacter, locale);
        }
    }
    
    // === EQUALS AND HASHCODE ===
    
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
    
    
    @Override
    public String toString() {
        return "PersianDateFormat{" +
               "pattern='" + pattern + '\'' +
               ", numberCharacter=" + numberCharacter +
               ", locale=" + locale +
               '}';
    }
}