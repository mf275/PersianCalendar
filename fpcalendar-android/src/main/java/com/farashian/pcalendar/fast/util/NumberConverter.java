package com.farashian.pcalendar.fast.util;

public class NumberConverter {
    
    private static final char[] PERSIAN_DIGITS = {'۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'};
    
    /**
     * Convert English digits (0-9) to Persian digits (۰-۹)
     * @param text Input string with English digits
     * @return String with Persian digits
     */
    public static String convertToPersianNumbers(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= '0' && c <= '9') {
                result.append(PERSIAN_DIGITS[c - '0']);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
    
    /**
     * Convert Persian/Arabic digits to English digits
     * @param text Input string with Persian/Arabic digits
     * @return String with English digits (0-9)
     */
    public static String convertToEnglishNumbers(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // Convert Persian digits (۰-۹)
            if (c >= '۰' && c <= '۹') {
                result.append((char) ('0' + (c - '۰')));
            }
            // Convert Arabic-Indic digits (٠-٩)
            else if (c >= '٠' && c <= '٩') {
                result.append((char) ('0' + (c - '٠')));
            }
            // Keep other characters as-is
            else {
                result.append(c);
            }
        }

        return result.toString();
    }
    
    /**
     * Check if text contains Persian or Arabic digits
     * @param text Input string to check
     * @return true if contains non-English digits
     */
    public static boolean containsPersianNumbers(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c >= '۰' && c <= '۹') || (c >= '٠' && c <= '٩')) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if text contains only English digits
     * @param text Input string to check
     * @return true if contains only English digits (0-9)
     */
    public static boolean containsOnlyEnglishNumbers(String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Normalize numbers to English digits regardless of input
     * @param text Input string
     * @return String with all digits converted to English (0-9)
     */
    public static String normalizeToEnglishNumbers(String text) {
        return convertToEnglishNumbers(text);
    }
    
    /**
     * Normalize numbers to Persian digits regardless of input
     * First convert any Arabic digits to Persian, then any English to Persian
     * @param text Input string
     * @return String with all digits converted to Persian (۰-۹)
     */
    public static String normalizeToPersianNumbers(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // First convert Arabic-Indic to Persian
        text = text.replace('٠', '۰')
                   .replace('١', '۱')
                   .replace('٢', '۲')
                   .replace('٣', '۳')
                   .replace('٤', '۴')
                   .replace('٥', '۵')
                   .replace('٦', '۶')
                   .replace('٧', '۷')
                   .replace('٨', '۸')
                   .replace('٩', '۹');
        
        // Then convert English to Persian
        return convertToPersianNumbers(text);
    }
}