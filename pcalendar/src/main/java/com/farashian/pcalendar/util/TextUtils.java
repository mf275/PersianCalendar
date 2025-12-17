package com.farashian.pcalendar.util;

public class TextUtils {
    private static final String TAG = "TextUtils";
    // Unicode control characters for RTL embedding
    private static final String RTL_EMBEDDING_START = "\u202B";
    private static final String RTL_EMBEDDING_END = "\u202C";

    /**
     * Returns true if the string is null or 0-length.
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }


    public static String nullIfEmpty(String str) {
        return isEmpty(str) ? null : str;
    }

    
    public static String emptyIfNull(String str) {
        return str == null ? "" : str;
    }

    /**
     * Returns whether the given CharSequence contains only digits.
     */
    public static boolean isDigitsOnly(CharSequence str) {
        final int len = str.length();
        for (int cp, i = 0; i < len; i += Character.charCount(cp)) {
            cp = Character.codePointAt(str, i);
            if (!Character.isDigit(cp)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Wraps a string with RTL embedding characters to ensure it is displayed
     * from right to left.
     *
     * @param text The text to format.
     * @return The formatted text, wrapped with RTL control characters.
     */
    public static String toRtl(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return RTL_EMBEDDING_START + text + RTL_EMBEDDING_END;
    }

    /**
     * @hide
     */
    public static boolean isPrintableAscii(final char c) {
        final int asciiFirst = 0x20;
        final int asciiLast = 0x7E;  // included
        return (asciiFirst <= c && c <= asciiLast) || c == '\r' || c == '\n';
    }
    public static boolean isPrintableAsciiOnly(final CharSequence str) {
        final int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!isPrintableAscii(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
