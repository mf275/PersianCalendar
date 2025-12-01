package com.farashian.pcalendar.fast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Fast Persian Date Formatter - Direct replacement of MyPersianCalendar with FastPersianCalendar
 */
public class FastPersianDateFormat {

    public enum PersianDateNumberCharacter {
        ENGLISH, FARSI
    }

    private String pattern;
    private PersianDateNumberCharacter numberCharacter = PersianDateNumberCharacter.ENGLISH;
    private Locale locale;
    private TimeZone timeZone;

    public FastPersianDateFormat() {
        this.locale = new Locale("fa");
        this.timeZone = TimeZone.getTimeZone("Asia/Tehran");
    }

    public FastPersianDateFormat(String pattern) {
        this();
        this.pattern = pattern;
    }

    public FastPersianDateFormat(String pattern, Locale locale) {
        this(pattern);
        this.locale = locale;
    }

    public FastPersianDateFormat(String pattern, Locale locale, TimeZone timeZone) {
        this(pattern, locale);
        this.timeZone = timeZone;
    }

    // === SETTERS & GETTERS ===

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public void setNumberCharacter(PersianDateNumberCharacter numberCharacter) {
        this.numberCharacter = numberCharacter;
    }

    public PersianDateNumberCharacter getNumberCharacter() {
        return numberCharacter;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    // === FORMATTING METHODS ===

    public String format(FastPersianCalendar calendar) {
        if (pattern == null) {
            return calendar.getLongDate();
        }

        return formatWithPattern(calendar, pattern);
    }

    public String format(Date date) {
        FastPersianCalendar calendar = new FastPersianCalendar();
        calendar.setTime(date);
        return format(calendar);
    }

    public String format(long milliseconds) {
        FastPersianCalendar calendar = new FastPersianCalendar();
        calendar.setTimeInMillis(milliseconds);
        return format(calendar);
    }

    private String formatWithPattern(FastPersianCalendar calendar, String formatPattern) {
        if (formatPattern == null || formatPattern.isEmpty()) {
            return calendar.getLongDate();
        }

        String result = formatPattern;

        // Replace pattern tokens
        result = result.replace("DDDD", calendar.getWeekdayName());
        result = result.replace("ddd", calendar.getWeekdayName()); // Using full name for short
        result = result.replace("MMMM", calendar.getMonthName());
        result = result.replace("MMM", calendar.getMonthName()); // Using full name for short
        result = result.replace("MM", formatToTwoDigits(calendar.getMonth() + 1));
        result = result.replace("dd", formatToTwoDigits(calendar.getDayOfMonth()));
        result = result.replace("yyyy", formatToTwoDigits(calendar.getYear()));
        result = result.replace("yy", formatToTwoDigits(calendar.getYear() % 100));
        result = result.replace("HH", formatToTwoDigits(calendar.get(HOUR_OF_DAY)));
        result = result.replace("hh", formatToTwoDigits(calendar.get(HOUR) == 0 ? 12 : calendar.get(HOUR)));
        result = result.replace("mm", formatToTwoDigits(calendar.get(MINUTE)));
        result = result.replace("ss", formatToTwoDigits(calendar.get(SECOND)));
        result = result.replace("a", getAmPm(calendar));

        // Handle single 'd' and 'M'
        result = result.replace("d", String.valueOf(calendar.getDayOfMonth()));
        result = result.replace("M", String.valueOf(calendar.getMonth() + 1));

        return convertNumbersToLocale(result);
    }

    private String formatToTwoDigits(int number) {
        if (number < 10) {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    private String getAmPm(FastPersianCalendar calendar) {
        int amPm = calendar.get(AM_PM);
        if (locale.getLanguage().equals("fa")) {
            return amPm == AM ? "ق.ظ" : "ب.ظ";
        } else {
            return amPm == AM ? "AM" : "PM";
        }
    }

    private String convertNumbersToLocale(String text) {
        if (numberCharacter == PersianDateNumberCharacter.ENGLISH) {
            return text;
        }

        // Convert English digits to Farsi
        return convertToFarsiNumbers(text);
    }

    private String convertToFarsiNumbers(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = convertDigitToFarsi(chars[i]);
        }
        return new String(chars);
    }

    private char convertDigitToFarsi(char digit) {
        switch (digit) {
            case '0': return '۰';
            case '1': return '۱';
            case '2': return '۲';
            case '3': return '۳';
            case '4': return '۴';
            case '5': return '۵';
            case '6': return '۶';
            case '7': return '۷';
            case '8': return '۸';
            case '9': return '۹';
            default: return digit;
        }
    }

    // === PARSING METHODS ===

    public FastPersianCalendar parse(String dateString) throws ParseException {
        if (pattern == null) {
            return parseDefault(dateString);
        }
        return parse(dateString, pattern);
    }

    public FastPersianCalendar parse(String dateString, String parsePattern) throws ParseException {
        try {
            // For "yyyy/MM/dd HH:mm" pattern
            if ("yyyy/MM/dd HH:mm".equals(parsePattern)) {
                return parseDateTimePattern(dateString, parsePattern);
            }

            // For other patterns with time
            if (parsePattern.contains("HH") || parsePattern.contains("mm") || parsePattern.contains("ss")) {
                return parseDateTimePattern(dateString, parsePattern);
            }

            // Date-only parsing
            return parseStandardDate(dateString);

        } catch (Exception e) {
            throw new ParseException("Cannot parse date: " + dateString + " with pattern: " + parsePattern, 0);
        }
    }

    private FastPersianCalendar parseDateTimePattern(String dateString, String parsePattern) throws ParseException {
        try {
            // Remove any extra spaces
            dateString = dateString.trim();

            // For "yyyy/MM/dd HH:mm" pattern, split by space
            String[] parts = dateString.split("\\s+");
            if (parts.length != 2) {
                throw new ParseException("Invalid date time format. Expected format: " + parsePattern, 0);
            }

            String dateStr = parts[0];
            String timeStr = parts[1];

            // Parse date part (yyyy/MM/dd)
            String[] dateParts = dateStr.split("/");
            if (dateParts.length != 3) {
                throw new ParseException("Invalid date format. Expected yyyy/MM/dd", 0);
            }

            int year = Integer.parseInt(convertFarsiToEnglish(dateParts[0]));
            int month = Integer.parseInt(convertFarsiToEnglish(dateParts[1])) - 1;
            int day = Integer.parseInt(convertFarsiToEnglish(dateParts[2]));

            // Parse time part (HH:mm)
            String[] timeParts = timeStr.split(":");
            if (timeParts.length < 2) {
                throw new ParseException("Invalid time format. Expected HH:mm", 0);
            }

            int hour = Integer.parseInt(convertFarsiToEnglish(timeParts[0]));
            int minute = Integer.parseInt(convertFarsiToEnglish(timeParts[1]));
            int second = 0;
            if (timeParts.length >= 3) {
                second = Integer.parseInt(convertFarsiToEnglish(timeParts[2]));
            }

            // Create and set calendar
            FastPersianCalendar calendar = new FastPersianCalendar(timeZone, locale);
            calendar.setPersianDate(year, month, day);
            calendar.set(HOUR_OF_DAY, hour);
            calendar.set(MINUTE, minute);
            calendar.set(SECOND, second);
            calendar.set(MILLISECOND, 0);

            return calendar;

        } catch (NumberFormatException e) {
            throw new ParseException("Invalid number in date time: " + dateString, 0);
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid date time: " + dateString, 0);
        }
    }

    public FastPersianCalendar parseGrg(String dateString, String pattern) throws ParseException {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(convertPatternToGregorian(pattern));
            sdf.setTimeZone(timeZone);
            Date date = sdf.parse(dateString);

            FastPersianCalendar result = new FastPersianCalendar(timeZone, locale);
            result.setTime(date);

            return result;
        } catch (java.text.ParseException e) {
            throw new ParseException("Invalid Gregorian date: " + dateString, 0);
        }
    }

    private FastPersianCalendar parseDefault(String dateString) throws ParseException {
        // Try common formats
        try {
            return parseStandardDate(dateString);
        } catch (Exception e) {
            throw new ParseException("Cannot parse date: " + dateString, 0);
        }
    }

    private FastPersianCalendar parseStandardDate(String dateString) throws ParseException {
        String normalized = dateString.replaceAll("[\\-/\\s]", "/");
        String[] parts = normalized.split("/");

        if (parts.length != 3) {
            throw new ParseException("Invalid date format: " + dateString, 0);
        }

        try {
            int year = Integer.parseInt(convertFarsiToEnglish(parts[0]));
            int month = Integer.parseInt(convertFarsiToEnglish(parts[1])) - 1;
            int day = Integer.parseInt(convertFarsiToEnglish(parts[2]));

            FastPersianCalendar calendar = new FastPersianCalendar(timeZone, locale);
            calendar.setPersianDate(year, month, day);
            return calendar;

        } catch (NumberFormatException e) {
            throw new ParseException("Invalid number in date: " + dateString, 0);
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid date: " + dateString, 0);
        }
    }

    private FastPersianCalendar parseDateTime(String dateString, String parsePattern) throws ParseException {
        // Simple implementation - parse date part and set time
        FastPersianCalendar calendar = parseStandardDate(dateString);

        // Extract time components if present in pattern
        if (parsePattern.contains("HH")) {
            // Simple time extraction - you might need more sophisticated parsing
            String[] timeParts = dateString.split("\\s+");
            if (timeParts.length > 1) {
                String timeStr = timeParts[1];
                String[] hms = timeStr.split(":");
                if (hms.length >= 1) {
                    calendar.set(HOUR_OF_DAY, Integer.parseInt(convertFarsiToEnglish(hms[0])));
                }
                if (hms.length >= 2) {
                    calendar.set(MINUTE, Integer.parseInt(convertFarsiToEnglish(hms[1])));
                }
                if (hms.length >= 3) {
                    calendar.set(SECOND, Integer.parseInt(convertFarsiToEnglish(hms[2])));
                }
            }
        }

        return calendar;
    }

    private String convertFarsiToEnglish(String text) {
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = convertFarsiDigitToEnglish(chars[i]);
        }
        return new String(chars);
    }

    private char convertFarsiDigitToEnglish(char digit) {
        switch (digit) {
            case '۰': return '0';
            case '۱': return '1';
            case '۲': return '2';
            case '۳': return '3';
            case '۴': return '4';
            case '۵': return '5';
            case '۶': return '6';
            case '۷': return '7';
            case '۸': return '8';
            case '۹': return '9';
            default: return digit;
        }
    }

    private String convertPatternToGregorian(String persianPattern) {
        // Simple conversion
        return persianPattern
                .replace("yyyy", "yyyy")
                .replace("MM", "MM")
                .replace("dd", "dd")
                .replace("HH", "HH")
                .replace("mm", "mm")
                .replace("ss", "ss");
    }

    // === STATIC UTILITY METHODS ===

    public static String format(FastPersianCalendar calendar, String pattern) {
        return format(calendar, pattern, PersianDateNumberCharacter.ENGLISH);
    }

    public static String format(FastPersianCalendar calendar, String pattern,
            PersianDateNumberCharacter numberCharacter) {
        FastPersianDateFormat formatter = new FastPersianDateFormat(pattern);
        formatter.setNumberCharacter(numberCharacter);
        return formatter.format(calendar);
    }

    public static String format(Date date, String pattern) {
        FastPersianCalendar calendar = new FastPersianCalendar();
        calendar.setTime(date);
        return format(calendar, pattern);
    }

    public static String format(Date date, String pattern, PersianDateNumberCharacter numberCharacter) {
        FastPersianCalendar calendar = new FastPersianCalendar();
        calendar.setTime(date);
        return format(calendar, pattern, numberCharacter);
    }

    public static String format(long milliseconds, String pattern) {
        FastPersianCalendar calendar = new FastPersianCalendar();
        calendar.setTimeInMillis(milliseconds);
        return format(calendar, pattern);
    }

    public static String format(long milliseconds, String pattern, PersianDateNumberCharacter numberCharacter) {
        FastPersianCalendar calendar = new FastPersianCalendar();
        calendar.setTimeInMillis(milliseconds);
        return format(calendar, pattern, numberCharacter);
    }

    // === CALENDAR CONSTANTS ===

    private static final int HOUR_OF_DAY = FastPersianCalendar.HOUR_OF_DAY;
    private static final int MINUTE = FastPersianCalendar.MINUTE;
    private static final int SECOND = FastPersianCalendar.SECOND;
    private static final int MILLISECOND = FastPersianCalendar.MILLISECOND;
    private static final int HOUR = FastPersianCalendar.HOUR;
    private static final int AM_PM = FastPersianCalendar.AM_PM;
    private static final int AM = FastPersianCalendar.AM;
    private static final int PM = FastPersianCalendar.PM;
}