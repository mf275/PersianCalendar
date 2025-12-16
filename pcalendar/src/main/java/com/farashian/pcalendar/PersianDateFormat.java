package com.farashian.pcalendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import static com.farashian.pcalendar.PCConstants.leapYears;

public class PersianDateFormat {
    
    // Number Format
    public enum PersianDateNumberCharacter {
        ENGLISH,
        FARSI
    }
    
    private String pattern = "DDDD, d MMMM yyyy HH:mm:ss";
    private PersianDateNumberCharacter numberCharacter = PersianDateNumberCharacter.ENGLISH;
    private Locale locale;

    // Pattern matcher for tokens
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
        "yyyy|yy|MMMM|MMM|MM|M|dddd|ddd|dd|d|HH|H|hh|h|mm|m|ss|s|a|A|'[^']*'"
    );

    // Formatter functions
    private static final Map<String, BiFunction<PersianCalendar, Locale, String>> FORMATTERS = 
        createFormatters();

    // === CONSTRUCTORS ===
    
    public PersianDateFormat() {
        this.locale = PCConstants.PERSIAN_LOCALE;
    }
    
    public PersianDateFormat(String pattern) {
        this();
        this.pattern = pattern;
    }
    
    public PersianDateFormat(String pattern, PersianDateNumberCharacter numberCharacter) {
        this();
        this.pattern = pattern;
        this.numberCharacter = numberCharacter;
    }
    
    public PersianDateFormat(String pattern, PersianDateNumberCharacter numberCharacter, Locale locale) {
        this.pattern = pattern;
        this.numberCharacter = numberCharacter;
        this.locale = locale;
    }

    // === SETTERS ===
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    public void setNumberCharacter(PersianDateNumberCharacter numberCharacter) {
        this.numberCharacter = numberCharacter;
    }
    
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    // === STATIC FORMAT METHODS ===
    
    public static String format(PersianCalendar date, String pattern) {
        return format(date, pattern, PersianDateNumberCharacter.ENGLISH, PCConstants.PERSIAN_LOCALE);
    }
    
    public static String format(PersianCalendar date, String pattern, PersianDateNumberCharacter numberCharacter) {
        return format(date, pattern, numberCharacter, PCConstants.PERSIAN_LOCALE);
    }
    
    public static String format(PersianCalendar date, String pattern, 
                               PersianDateNumberCharacter numberCharacter, Locale locale) {
        if (pattern == null) pattern = "DDDD, d MMMM yyyy HH:mm:ss";
        
        StringBuilder result = new StringBuilder();
        java.util.regex.Matcher matcher = TOKEN_PATTERN.matcher(pattern);
        int lastIndex = 0;
        
        while (matcher.find()) {
            // Append text between tokens
            result.append(pattern.substring(lastIndex, matcher.start()));
            
            String token = matcher.group();
            
            if (token.startsWith("'") && token.endsWith("'")) {
                // Literal text in quotes
                result.append(token.substring(1, token.length() - 1));
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

    // === INSTANCE FORMAT METHOD ===
    
    public String format(PersianCalendar date) {
        return format(date, this.pattern, this.numberCharacter, this.locale);
    }

    // === PARSING METHODS ===
    
    public PersianCalendar parse(String date) throws ParseException {
        return parse(date, this.pattern);
    }
    
    public PersianCalendar parse(String date, String pattern) throws ParseException {
        // For simplicity, we'll use a regex-based parser for common patterns
        // This can be enhanced for more complex parsing
        
        Map<String, Integer> values = new HashMap<>();
        values.put("year", 1400);
        values.put("month", 0);
        values.put("day", 1);
        values.put("hour", 0);
        values.put("minute", 0);
        values.put("second", 0);
        
        // Simple pattern matching for common formats
        if (pattern.equals("yyyy/MM/dd") || pattern.equals("yyyy-MM-dd")) {
            String delimiter = pattern.contains("/") ? "/" : "-";
            String[] parts = date.split(Pattern.quote(delimiter));
            if (parts.length == 3) {
                values.put("year", Integer.parseInt(parts[0]));
                values.put("month", Integer.parseInt(parts[1]) - 1); // Convert to 0-based
                values.put("day", Integer.parseInt(parts[2]));
            }
        } else if (pattern.equals("yyyy/MM/dd HH:mm") || pattern.equals("yyyy-MM-dd HH:mm")) {
            String[] dateTimeParts = date.split(" ");
            if (dateTimeParts.length == 2) {
                String datePart = dateTimeParts[0];
                String timePart = dateTimeParts[1];
                
                String delimiter = pattern.contains("/") ? "/" : "-";
                String[] dateParts = datePart.split(Pattern.quote(delimiter));
                String[] timeParts = timePart.split(":");
                
                if (dateParts.length == 3 && timeParts.length >= 2) {
                    values.put("year", Integer.parseInt(dateParts[0]));
                    values.put("month", Integer.parseInt(dateParts[1]) - 1);
                    values.put("day", Integer.parseInt(dateParts[2]));
                    values.put("hour", Integer.parseInt(timeParts[0]));
                    values.put("minute", Integer.parseInt(timeParts[1]));
                    if (timeParts.length > 2) {
                        values.put("second", Integer.parseInt(timeParts[2]));
                    }
                }
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
    
    public PersianCalendar parseGrg(String date, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        Date grgDate = sdf.parse(date);
        PersianCalendar persianCalendar = new PersianCalendar();
        persianCalendar.setTimeInMillis(grgDate.getTime());
        return persianCalendar;
    }

    // === PRIVATE HELPER METHODS ===
    
    private static Map<String, BiFunction<PersianCalendar, Locale, String>> createFormatters() {
        Map<String, BiFunction<PersianCalendar, Locale, String>> formatters = new HashMap<>();
        
        // Year
        formatters.put("yyyy", (cal, loc) -> String.format(loc, "%04d", cal.getYear()));
        formatters.put("yy", (cal, loc) -> String.format(loc, "%02d", cal.getYear() % 100));
        
        // Month
        formatters.put("MMMM", (cal, loc) -> getMonthName(cal.getMonth(), loc));
        formatters.put("MMM", (cal, loc) -> getShortMonthName(cal.getMonth(), loc));
        formatters.put("MM", (cal, loc) -> String.format(loc, "%02d", cal.getMonth() + 1));
        formatters.put("M", (cal, loc) -> String.valueOf(cal.getMonth() + 1));
        
        // Day
        formatters.put("dddd", (cal, loc) -> getWeekdayName(cal.get(PersianCalendar.DAY_OF_WEEK), loc));
        formatters.put("ddd", (cal, loc) -> getShortDayName(cal.get(PersianCalendar.DAY_OF_WEEK), loc));
        formatters.put("dd", (cal, loc) -> String.format(loc, "%02d", cal.getDayOfMonth()));
        formatters.put("d", (cal, loc) -> String.valueOf(cal.getDayOfMonth()));
        
        // Hour
        formatters.put("HH", (cal, loc) -> String.format(loc, "%02d", cal.get(PersianCalendar.HOUR_OF_DAY)));
        formatters.put("H", (cal, loc) -> String.valueOf(cal.get(PersianCalendar.HOUR_OF_DAY)));
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
        String fullName = getMonthName(month, locale);
        if (locale.getLanguage().equals("fa")) {
            // For Persian, return the full name as short name (common in Persian)
            return fullName;
        } else {
            // For English, return first 3 letters
            String[] englishMonths = {"Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
                                     "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"};
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
    
    private static String getAmPm(PersianCalendar cal, Locale locale, boolean uppercase) {
        int hour = cal.get(PersianCalendar.HOUR_OF_DAY);
        boolean isAm = hour < 12;
        
        if (locale.getLanguage().equals("fa")) {
            return isAm ? (uppercase ? "ق.ظ" : "ق.ظ") : (uppercase ? "ب.ظ" : "ب.ظ");
        } else {
            return isAm ? (uppercase ? "AM" : "am") : (uppercase ? "PM" : "pm");
        }
    }
    
    private static String convertToFarsiNumbers(String text) {
        String[] persianChars = {"۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹"};
        String[] englishChars = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
        
        String result = text;
        for (int i = 0; i < persianChars.length; i++) {
            result = result.replace(englishChars[i], persianChars[i]);
        }
        return result;
    }
    
    private void validateParsedDate(Map<String, Integer> values) throws ParseException {
        int year = values.get("year");
        int month = values.get("month");
        int day = values.get("day");
        
        if (year < 1) {
            throw new ParseException("Invalid year: " + year, 0);
        }
        
        if (month < 0 || month > 11) {
            throw new ParseException("Invalid month: " + (month + 1), 0);
        }
        
        int maxDays = getDaysInMonth(year, month);
        if (day < 1 || day > maxDays) {
            throw new ParseException("Invalid day: " + day + " for month " + (month + 1), 0);
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
            return isLeapYear(year) ? 30 : 29;
        }
    }

    // === UTILITY METHODS FOR EXTERNAL USE ===
    
    public static String getMonthName(PersianCalendar date) {
        return getMonthName(date.getMonth(), PCConstants.PERSIAN_LOCALE);
    }
    
    public static String getDayName(PersianCalendar date) {
        return getWeekdayName(date.get(PersianCalendar.DAY_OF_WEEK),
                              PCConstants.PERSIAN_LOCALE);
    }
    
    public static boolean isLeapYear(PersianCalendar date) {
        return isLeapYear(date.getYear());
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

    public static boolean isLeapYear(int year) {
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
}