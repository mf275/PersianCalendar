package com.farashian.pcalendar.fast;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fast Persian Date Formatter - Optimized for Android
 */
@Keep
public final class FastPersianDateFormat {

    private static final String TAG = "FastPersianDateFormat";
    
    public enum PersianDateNumberCharacter {
        ENGLISH, FARSI
    }

    private String pattern;
    private PersianDateNumberCharacter numberCharacter = PersianDateNumberCharacter.ENGLISH;
    private Locale locale;
    private TimeZone timeZone;
    
    // Thread-local cache for SimpleDateFormat instances
    private static final ThreadLocal<SimpleDateFormat> sGregorianFormatCache = 
        new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US);
            }
        };
    
    // Context for resource-based localization
    @Nullable
    private static Context appContext;
    
    // Pattern cache for performance
    private static final ConcurrentHashMap<String, Pattern> patternCache = new ConcurrentHashMap<>();
    
    // Calendar field constants
    private static final int HOUR_OF_DAY = FastPersianCalendar.HOUR_OF_DAY;
    private static final int MINUTE = FastPersianCalendar.MINUTE;
    private static final int SECOND = FastPersianCalendar.SECOND;
    private static final int MILLISECOND = FastPersianCalendar.MILLISECOND;
    private static final int HOUR = FastPersianCalendar.HOUR;
    private static final int AM_PM = FastPersianCalendar.AM_PM;
    private static final int AM = FastPersianCalendar.AM;
    private static final int PM = FastPersianCalendar.PM;

    // === CONSTRUCTORS ===

    public FastPersianDateFormat() {
        this.locale = Locale.getDefault().getLanguage().equals("fa") 
            ? Locale.getDefault() 
            : new Locale("fa");
        this.timeZone = TimeZone.getTimeZone("Asia/Tehran");
    }

    public FastPersianDateFormat(@NonNull String pattern) {
        this();
        setPattern(pattern);
    }

    public FastPersianDateFormat(@NonNull String pattern, @NonNull Locale locale) {
        this(pattern);
        setLocale(locale);
    }

    public FastPersianDateFormat(@NonNull String pattern, @NonNull Locale locale, 
                                @NonNull TimeZone timeZone) {
        this(pattern, locale);
        setTimeZone(timeZone);
    }
    
    // === SETTERS & GETTERS ===

    public void setPattern(@NonNull String pattern) {
        if (TextUtils.isEmpty(pattern)) {
            Log.w(TAG, "Pattern is empty, using default");
            this.pattern = "DDDD, d MMMM yyyy HH:mm:ss";
        } else {
            this.pattern = pattern;
        }
    }

    @Nullable
    public String getPattern() {
        return pattern;
    }

    public void setNumberCharacter(@NonNull PersianDateNumberCharacter numberCharacter) {
        this.numberCharacter = numberCharacter;
    }

    @NonNull
    public PersianDateNumberCharacter getNumberCharacter() {
        return numberCharacter;
    }

    public void setLocale(@NonNull Locale locale) {
        this.locale = locale;
    }

    @NonNull
    public Locale getLocale() {
        return locale;
    }

    public void setTimeZone(@NonNull TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @NonNull
    public TimeZone getTimeZone() {
        return timeZone;
    }
    
    // Static method to set application context
    public static void setAppContext(@Nullable Context context) {
        appContext = context != null ? context.getApplicationContext() : null;
    }

    // === FORMATTING METHODS ===

    @NonNull
    public String format(@NonNull FastPersianCalendar calendar) {
        if (TextUtils.isEmpty(pattern)) {
            return calendar.getLongDate();
        }

        return formatWithPattern(calendar, pattern);
    }

    @NonNull
    public String format(@NonNull Date date) {
        FastPersianCalendar calendar = new FastPersianCalendar(timeZone, locale);
        calendar.setTime(date);
        return format(calendar);
    }

    @NonNull
    public String format(long milliseconds) {
        FastPersianCalendar calendar = new FastPersianCalendar(timeZone, locale);
        calendar.setTimeInMillis(milliseconds);
        return format(calendar);
    }

    @NonNull
    private String formatWithPattern(@NonNull FastPersianCalendar calendar,
                                    @NonNull String formatPattern) {
        if (formatPattern.isEmpty()) {
            return calendar.getLongDate();
        }

        StringBuilder result = new StringBuilder(formatPattern);
        
        // Replace tokens in order of specificity
        replaceToken(result, "DDDD", calendar.getWeekdayName());
        replaceToken(result, "ddd", getShortWeekdayName(calendar));
        replaceToken(result, "MMMM", calendar.getMonthName());
        replaceToken(result, "MMM", getShortMonthName(calendar));
        replaceToken(result, "MM", formatToTwoDigits(calendar.getMonth() + 1));
        replaceToken(result, "dd", formatToTwoDigits(calendar.getDayOfMonth()));
        replaceToken(result, "yyyy", String.valueOf(calendar.getYear()));
        replaceToken(result, "yy", formatToTwoDigits(calendar.getYear() % 100));
        replaceToken(result, "HH", formatToTwoDigits(calendar.get(HOUR_OF_DAY)));
        
        int hour12 = calendar.get(HOUR) == 0 ? 12 : calendar.get(HOUR);
        replaceToken(result, "hh", formatToTwoDigits(hour12));
        replaceToken(result, "h", String.valueOf(hour12));
        replaceToken(result, "H", String.valueOf(calendar.get(HOUR_OF_DAY)));
        
        replaceToken(result, "mm", formatToTwoDigits(calendar.get(MINUTE)));
        replaceToken(result, "m", String.valueOf(calendar.get(MINUTE)));
        
        replaceToken(result, "ss", formatToTwoDigits(calendar.get(SECOND)));
        replaceToken(result, "s", String.valueOf(calendar.get(SECOND)));
        
        replaceToken(result, "a", getAmPm(calendar, false));
        replaceToken(result, "A", getAmPm(calendar, true));
        
        // Handle single 'd' and 'M' - only if not already replaced by dd/MM
        replaceToken(result, "d", String.valueOf(calendar.getDayOfMonth()));
        replaceToken(result, "M", String.valueOf(calendar.getMonth() + 1));

        String formatted = result.toString();
        return convertNumbersToLocale(formatted);
    }
    
    private void replaceToken(@NonNull StringBuilder builder, @NonNull String token, 
                             @NonNull String replacement) {
        int index;
        while ((index = builder.indexOf(token)) != -1) {
            builder.replace(index, index + token.length(), replacement);
        }
    }

    @NonNull
    private String formatToTwoDigits(int number) {
        return number < 10 ? "0" + number : String.valueOf(number);
    }

    @NonNull
    private String getShortMonthName(@NonNull FastPersianCalendar calendar) {
        String fullName = calendar.getMonthName();
        if (locale.getLanguage().equals("fa")) {
            return fullName; // Persian doesn't typically use short month names
        }
        // For English, return first 3 letters
        return fullName.length() > 3 ? fullName.substring(0, 3) : fullName;
    }

    @NonNull
    private String getShortWeekdayName(@NonNull FastPersianCalendar calendar) {
        String fullName = calendar.getWeekdayName();
        if (locale.getLanguage().equals("fa")) {
            return fullName; // Persian doesn't typically use short weekday names
        }
        // For English, return first 3 letters
        return fullName.length() > 3 ? fullName.substring(0, 3) : fullName;
    }

    @NonNull
    private String getAmPm(@NonNull FastPersianCalendar calendar, boolean uppercase) {
        int amPm = calendar.get(AM_PM);
        if (locale.getLanguage().equals("fa")) {
            return amPm == AM ? (uppercase ? "ق.ظ" : "ق.ظ") : (uppercase ? "ب.ظ" : "ب.ظ");
        } else {
            return amPm == AM ? (uppercase ? "AM" : "am") : (uppercase ? "PM" : "pm");
        }
    }

    @NonNull
    private String convertNumbersToLocale(@NonNull String text) {
        if (numberCharacter == PersianDateNumberCharacter.ENGLISH) {
            return text;
        }

        return convertToFarsiNumbers(text);
    }

    @NonNull
    private String convertToFarsiNumbers(@NonNull String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') {
                result.append((char) ('۰' + (c - '0')));
            } else if (c >= '٠' && c <= '٩') {
                // Arabic-Indic digits - keep as is
                result.append(c);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // === PARSING METHODS ===

    @NonNull
    public FastPersianCalendar parse(@NonNull String dateString) throws ParseException {
        if (TextUtils.isEmpty(dateString)) {
            throw new ParseException("Date string is null or empty", 0);
        }
        
        if (pattern == null) {
            return parseDefault(dateString);
        }
        return parse(dateString, pattern);
    }

    @NonNull
    public FastPersianCalendar parse(@NonNull String dateString, 
                                    @NonNull String parsePattern) throws ParseException {
        Log.d(TAG, "Parsing: " + dateString + " with pattern: " + parsePattern);
        
        if (TextUtils.isEmpty(dateString)) {
            throw new ParseException("Date string is null or empty", 0);
        }

        try {
            // Remove Farsi numbers and convert to English for parsing
            String normalizedDate = convertFarsiToEnglish(dateString.trim());
            
            // Handle different patterns
            if (parsePattern.contains("HH") || parsePattern.contains("mm") || parsePattern.contains("ss")) {
                return parseDateTimePattern(normalizedDate, parsePattern);
            } else {
                return parseDateOnlyPattern(normalizedDate, parsePattern);
            }

        } catch (NumberFormatException e) {
            Log.e(TAG, "Number format error parsing: " + dateString, e);
            throw new ParseException("Invalid number format in date: " + dateString, 0);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Illegal argument parsing: " + dateString, e);
            throw new ParseException("Invalid date: " + dateString, 0);
        }
    }

    @NonNull
    private FastPersianCalendar parseDateTimePattern(@NonNull String dateString,
                                                    @NonNull String parsePattern) throws ParseException {
        try {
            // Extract date and time parts based on pattern
            String datePart = "";
            String timePart = "";
            
            if (parsePattern.contains(" ")) {
                String[] patternParts = parsePattern.split(" ", 2);
                String[] dateStringParts = dateString.split(" ", 2);
                
                if (dateStringParts.length >= 2) {
                    datePart = dateStringParts[0];
                    timePart = dateStringParts[1];
                } else {
                    datePart = dateString;
                }
            } else {
                datePart = dateString;
            }
            
            // Parse date part
            String datePattern = extractDatePattern(parsePattern);
            FastPersianCalendar calendar = parseDateOnlyPattern(datePart, datePattern);
            
            // Parse time part if available
            if (!timePart.isEmpty()) {
                parseAndSetTime(calendar, timePart, parsePattern);
            }
            
            calendar.set(MILLISECOND, 0);
            return calendar;

        } catch (Exception e) {
            Log.e(TAG, "Error parsing date time: " + dateString, e);
            throw new ParseException("Cannot parse date time: " + dateString + 
                                   " with pattern: " + parsePattern, 0);
        }
    }

    @NonNull
    private FastPersianCalendar parseDateOnlyPattern(@NonNull String dateString,
                                                    @NonNull String parsePattern) throws ParseException {
        // Determine delimiter
        char delimiter = '/';
        if (parsePattern.contains("-")) delimiter = '-';
        else if (parsePattern.contains(".")) delimiter = '.';
        
        // Get regex pattern for this format
        Pattern pattern = getCachedPattern(parsePattern);
        Matcher matcher = pattern.matcher(dateString);
        
        if (!matcher.matches()) {
            throw new ParseException("Date does not match pattern: " + parsePattern, 0);
        }
        
        try {
            // Extract components based on pattern
            int year, month, day;
            
            if (parsePattern.startsWith("yyyy")) {
                year = Integer.parseInt(matcher.group(1));
                month = Integer.parseInt(matcher.group(2)) - 1;
                day = Integer.parseInt(matcher.group(3));
            } else if (parsePattern.startsWith("yy")) {
                year = 1400 + Integer.parseInt(matcher.group(1));
                month = Integer.parseInt(matcher.group(2)) - 1;
                day = Integer.parseInt(matcher.group(3));
            } else {
                // Default to yyyy/MM/dd
                String[] parts = dateString.split(Pattern.quote(String.valueOf(delimiter)));
                if (parts.length != 3) {
                    throw new ParseException("Invalid date format: " + dateString, 0);
                }
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]) - 1;
                day = Integer.parseInt(parts[2]);
            }
            
            // Validate date
            validateDate(year, month, day);
            
            FastPersianCalendar calendar = new FastPersianCalendar(timeZone, locale);
            calendar.setPersianDate(year, month, day);
            return calendar;

        } catch (NumberFormatException e) {
            throw new ParseException("Invalid number in date: " + dateString, 0);
        }
    }
    
    @NonNull
    private String extractDatePattern(@NonNull String fullPattern) {
        // Extract date part from pattern (before any time-related tokens)
        if (fullPattern.contains(" ")) {
            return fullPattern.split(" ")[0];
        }
        
        // Remove time tokens
        String datePattern = fullPattern
            .replace("HH", "")
            .replace("hh", "")
            .replace("mm", "")
            .replace("ss", "")
            .replace("a", "")
            .replace("A", "")
            .trim();
            
        return datePattern.isEmpty() ? "yyyy/MM/dd" : datePattern;
    }
    
    private void parseAndSetTime(@NonNull FastPersianCalendar calendar, 
                                @NonNull String timeString,
                                @NonNull String pattern) throws ParseException {
        try {
            // Check for AM/PM
            boolean hasAmPm = pattern.contains("a") || pattern.contains("A");
            boolean is24Hour = pattern.contains("HH");
            
            String[] timeParts = timeString.split(":");
            if (timeParts.length < 2) {
                throw new ParseException("Invalid time format: " + timeString, 0);
            }
            
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);
            int second = timeParts.length > 2 ? Integer.parseInt(timeParts[2]) : 0;
            
            // Handle 12-hour format with AM/PM
            if (hasAmPm && !is24Hour) {
                // Check for AM/PM suffix
                String suffix = timeParts[timeParts.length - 1].toLowerCase();
                boolean isPm = suffix.contains("pm") || suffix.contains("ب.ظ");
                
                if (isPm && hour < 12) {
                    hour += 12;
                } else if (!isPm && hour == 12) {
                    hour = 0;
                }
            }
            
            // Validate time
            if (hour < 0 || hour > 23) {
                throw new ParseException("Invalid hour: " + hour, 0);
            }
            if (minute < 0 || minute > 59) {
                throw new ParseException("Invalid minute: " + minute, 0);
            }
            if (second < 0 || second > 59) {
                throw new ParseException("Invalid second: " + second, 0);
            }
            
            calendar.set(HOUR_OF_DAY, hour);
            calendar.set(MINUTE, minute);
            calendar.set(SECOND, second);

        } catch (NumberFormatException e) {
            throw new ParseException("Invalid number in time: " + timeString, 0);
        }
    }

    @NonNull
    public FastPersianCalendar parseGrg(@NonNull String dateString, 
                                       @NonNull String pattern) throws ParseException {
        if (TextUtils.isEmpty(dateString)) {
            throw new ParseException("Date string is null or empty", 0);
        }
        
        try {
            SimpleDateFormat sdf = sGregorianFormatCache.get();
            sdf.applyPattern(convertPatternToGregorian(pattern));
            sdf.setTimeZone(timeZone);
            
            Date date = sdf.parse(dateString);
            if (date == null) {
                throw new ParseException("Failed to parse Gregorian date: " + dateString, 0);
            }
            
            FastPersianCalendar result = new FastPersianCalendar(timeZone, locale);
            result.setTime(date);
            return result;
            
        } catch (java.text.ParseException e) {
            Log.e(TAG, "Error parsing Gregorian date: " + dateString, e);
            throw new ParseException("Invalid Gregorian date: " + dateString + 
                                   " with pattern: " + pattern, 0);
        }
    }

    @NonNull
    private FastPersianCalendar parseDefault(@NonNull String dateString) throws ParseException {
        // Try common formats
        String[] patterns = {"yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", 
                            "yyyy/MM/dd", "yyyy-MM-dd HH:mm:ss", 
                            "yyyy-MM-dd HH:mm", "yyyy-MM-dd"};
        
        for (String pattern : patterns) {
            try {
                return parse(dateString, pattern);
            } catch (ParseException e) {
                // Try next pattern
                continue;
            }
        }
        
        throw new ParseException("Cannot parse date with any known pattern: " + dateString, 0);
    }

    private void validateDate(int year, int month, int day) throws ParseException {
        if (year < 1 || year > 9999) {
            throw new ParseException("Invalid year: " + year + ". Must be between 1-9999", 0);
        }
        
        if (month < 0 || month > 11) {
            throw new ParseException("Invalid month: " + (month + 1) + ". Must be between 1-12", 0);
        }
        
        // Check day validity based on month
        int maxDays = getDaysInMonth(year, month);
        if (day < 1 || day > maxDays) {
            throw new ParseException("Invalid day: " + day + " for month " + (month + 1) + 
                                   ". Must be between 1-" + maxDays, 0);
        }
    }
    
    private int getDaysInMonth(int year, int month) {
        if (month < 0 || month > 11) return 31;
        
        if (month < 6) {
            return 31;
        } else if (month < 11) {
            return 30;
        } else {
            // Esfand
            return FastPersianCalendar.isLeapYear(year) ? 30 : 29;
        }
    }

    @NonNull
    private String convertFarsiToEnglish(@NonNull String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '۰' && c <= '۹') {
                result.append((char) ('0' + (c - '۰')));
            } else if (c >= '٠' && c <= '٩') {
                result.append((char) ('0' + (c - '٠')));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    @NonNull
    private String convertPatternToGregorian(@NonNull String persianPattern) {
        // Convert Persian pattern to Gregorian SimpleDateFormat pattern
        return persianPattern
                .replace("yyyy", "yyyy")
                .replace("yy", "yy")
                .replace("MMMM", "MMMM")
                .replace("MMM", "MMM")
                .replace("MM", "MM")
                .replace("M", "M")
                .replace("dddd", "EEEE")
                .replace("ddd", "EEE")
                .replace("dd", "dd")
                .replace("d", "d")
                .replace("HH", "HH")
                .replace("hh", "hh")
                .replace("mm", "mm")
                .replace("ss", "ss")
                .replace("a", "a")
                .replace("A", "a");
    }
    
    @NonNull
    private Pattern getCachedPattern(@NonNull String parsePattern) {
        return patternCache.computeIfAbsent(parsePattern, pattern -> {
            // Build regex sequentially
            StringBuilder regexBuilder = new StringBuilder();

            for (int i = 0; i < pattern.length(); i++) {
                // Check for 4-char tokens
                if (i + 3 < pattern.length()) {
                    String fourChars = pattern.substring(i, i + 4);
                    switch (fourChars) {
                        case "yyyy":
                            regexBuilder.append("(\\d{4})");
                            i += 3; // Skip ahead
                            continue;
                        case "DDDD":
                            // Weekday name - for parsing we'll accept any word characters
                            regexBuilder.append("(\\p{L}+)");
                            i += 3;
                            continue;
                        case "MMMM":
                            // Month name - for parsing we'll accept any word characters
                            regexBuilder.append("(\\p{L}+)");
                            i += 3;
                            continue;
                    }
                }

                // Check for 3-char tokens
                if (i + 2 < pattern.length()) {
                    String threeChars = pattern.substring(i, i + 3);
                    switch (threeChars) {
                        case "MMM":
                            // Short month name
                            regexBuilder.append("(\\p{L}+)");
                            i += 2;
                            continue;
                        case "ddd":
                            // Short weekday name
                            regexBuilder.append("(\\p{L}+)");
                            i += 2;
                            continue;
                    }
                }

                // Check for 2-char tokens
                if (i + 1 < pattern.length()) {
                    String twoChars = pattern.substring(i, i + 2);
                    switch (twoChars) {
                        case "yy":
                            regexBuilder.append("(\\d{2})");
                            i += 1;
                            continue;
                        case "MM":
                            regexBuilder.append("(\\d{2})");
                            i += 1;
                            continue;
                        case "dd":
                            regexBuilder.append("(\\d{2})");
                            i += 1;
                            continue;
                        case "HH":
                            regexBuilder.append("(\\d{2})");
                            i += 1;
                            continue;
                        case "hh":
                            regexBuilder.append("(\\d{1,2})");
                            i += 1;
                            continue;
                        case "mm":
                            regexBuilder.append("(\\d{2})");
                            i += 1;
                            continue;
                        case "ss":
                            regexBuilder.append("(\\d{2})");
                            i += 1;
                            continue;
                    }
                }

                // Single character tokens
                char c = pattern.charAt(i);
                switch (c) {
                    case 'M':
                        regexBuilder.append("(\\d{1,2})");
                        break;
                    case 'd':
                        regexBuilder.append("(\\d{1,2})");
                        break;
                    case 'H':
                        regexBuilder.append("(\\d{1,2})");
                        break;
                    case 'h':
                        regexBuilder.append("(\\d{1,2})");
                        break;
                    case 'm':
                        regexBuilder.append("(\\d{1,2})");
                        break;
                    case 's':
                        regexBuilder.append("(\\d{1,2})");
                        break;
                    case '.':
                        regexBuilder.append("\\.");
                        break;
                    case '-':
                        regexBuilder.append("\\-");
                        break;
                    case '/':
                        regexBuilder.append("\\/");
                        break;
                    case ':':
                        regexBuilder.append("\\:");
                        break;
                    case ' ':
                        regexBuilder.append("\\s+");
                        break;
                    case ',':
                        regexBuilder.append("\\s*,\\s*");
                        break;
                    default:
                        // For literal text like "DDDD, " - skip it in parsing mode
                        // or escape it
                        regexBuilder.append(Pattern.quote(String.valueOf(c)));
                        break;
                }
            }

            return Pattern.compile("^" + regexBuilder.toString() + "$", Pattern.CASE_INSENSITIVE);
        });
    }
    @NonNull
    public static String format(@NonNull FastPersianCalendar calendar, @NonNull String pattern) {
        return format(calendar, pattern, PersianDateNumberCharacter.ENGLISH);
    }

    @NonNull
    public static String format(@NonNull FastPersianCalendar calendar, @NonNull String pattern,
                               @NonNull PersianDateNumberCharacter numberCharacter) {
        FastPersianDateFormat formatter = new FastPersianDateFormat(pattern);
        formatter.setNumberCharacter(numberCharacter);
        return formatter.format(calendar);
    }

    @NonNull
    public static String format(@NonNull Date date, @NonNull String pattern) {
        FastPersianCalendar calendar = new FastPersianCalendar();
        calendar.setTime(date);
        return format(calendar, pattern);
    }

    @NonNull
    public static String format(@NonNull Date date, @NonNull String pattern, 
                               @NonNull PersianDateNumberCharacter numberCharacter) {
        FastPersianCalendar calendar = new FastPersianCalendar();
        calendar.setTime(date);
        return format(calendar, pattern, numberCharacter);
    }

    @NonNull
    public static String format(long milliseconds, @NonNull String pattern) {
        FastPersianCalendar calendar = new FastPersianCalendar();
        calendar.setTimeInMillis(milliseconds);
        return format(calendar, pattern);
    }

    @NonNull
    public static String format(long milliseconds, @NonNull String pattern, 
                               @NonNull PersianDateNumberCharacter numberCharacter) {
        FastPersianCalendar calendar = new FastPersianCalendar();
        calendar.setTimeInMillis(milliseconds);
        return format(calendar, pattern, numberCharacter);
    }

    // === BUILDER PATTERN ===
    
    public static class Builder {
        private String pattern;
        private PersianDateNumberCharacter numberCharacter = PersianDateNumberCharacter.ENGLISH;
        private Locale locale;
        private TimeZone timeZone = TimeZone.getTimeZone("Asia/Tehran");
        
        public Builder() {
            this.locale = Locale.getDefault().getLanguage().equals("fa") 
                ? Locale.getDefault() 
                : new Locale("fa");
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
        
        public Builder timeZone(@NonNull TimeZone timeZone) {
            this.timeZone = timeZone;
            return this;
        }
        
        @NonNull
        public FastPersianDateFormat build() {
            FastPersianDateFormat formatter = new FastPersianDateFormat();
            if (pattern != null) {
                formatter.setPattern(pattern);
            }
            formatter.setNumberCharacter(numberCharacter);
            formatter.setLocale(locale);
            formatter.setTimeZone(timeZone);
            return formatter;
        }
    }

    // === EQUALS AND HASHCODE ===
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        FastPersianDateFormat that = (FastPersianDateFormat) o;
        
        if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) return false;
        if (numberCharacter != that.numberCharacter) return false;
        if (!locale.equals(that.locale)) return false;
        return timeZone.equals(that.timeZone);
    }
    
    @Override
    public int hashCode() {
        int result = pattern != null ? pattern.hashCode() : 0;
        result = 31 * result + numberCharacter.hashCode();
        result = 31 * result + locale.hashCode();
        result = 31 * result + timeZone.hashCode();
        return result;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "FastPersianDateFormat{" +
               "pattern='" + pattern + '\'' +
               ", numberCharacter=" + numberCharacter +
               ", locale=" + locale +
               ", timeZone=" + timeZone +
               '}';
    }
}