package com.farashian.pcalendar;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Iranian Hijri (lunar) calendar converter, anchored to Iran's official data.
 *
 * Epoch:
 *   Hijri 1447-07-01  ==  Gregorian 2025-12-22 (Tehran midnight)
 *
 * - Uses official Iranian month lengths when available (1340–1448).
 * - Falls back to tabular 30/29 month pattern + 30-year leap cycle when missing.
 * - All conversions are done relative to Tehran time.
 *
 * NOTE: For years outside 1340–1448, results are approximate (tabular).
 *       Extend HIJRI_MONTH_DATA if you add more official years.
 */
public class HijriConverter {

    private static final long MILLIS_PER_DAY = 24L * 60 * 60 * 1000L;

    private static final TimeZone TEHRAN_TIMEZONE = TimeZone.getTimeZone("Asia/Tehran");

    private static final GregorianCalendar EPOCH_GREGORIAN_TEHRAN;

    //1-based Hijri epoch date: 1447-07-01
    private static final YMD EPOCH_HIJRI = new YMD(1447, 7, 1);

    //Official Iranian Hijri month lengths (1-based months, index 0 = Muharram)
    private static final Map<Integer, int[]> HIJRI_MONTH_DATA = getIranianHijriMonthData();

    static {
        EPOCH_GREGORIAN_TEHRAN = new GregorianCalendar(TEHRAN_TIMEZONE);
        EPOCH_GREGORIAN_TEHRAN.set(Calendar.YEAR, 2025);
        EPOCH_GREGORIAN_TEHRAN.set(Calendar.MONTH, Calendar.DECEMBER); //11
        EPOCH_GREGORIAN_TEHRAN.set(Calendar.DAY_OF_MONTH, 22);
        EPOCH_GREGORIAN_TEHRAN.set(Calendar.HOUR_OF_DAY, 0);
        EPOCH_GREGORIAN_TEHRAN.set(Calendar.MINUTE, 0);
        EPOCH_GREGORIAN_TEHRAN.set(Calendar.SECOND, 0);
        EPOCH_GREGORIAN_TEHRAN.set(Calendar.MILLISECOND, 0);
    }

    public static YMD gregorianToHijri(GregorianCalendar gc) {
        GregorianCalendar tehranTime = toTehranTimezone(gc);
        normalizeToMidnight(tehranTime);
        return calculateHijriFromGregorian(tehranTime);
    }

    public static YMD gregorianToHijri(int year, int month, int day, TimeZone inputTimezone) {
        GregorianCalendar gc = new GregorianCalendar(inputTimezone);
        gc.set(Calendar.YEAR, year);
        gc.set(Calendar.MONTH, month);
        gc.set(Calendar.DAY_OF_MONTH, day);
        normalizeToMidnight(gc);
        return gregorianToHijri(gc);
    }

    public static YMD gregorianToHijri(int year, int month, int day) {
        return gregorianToHijri(year, month, day, TEHRAN_TIMEZONE);
    }

    public static GregorianCalendar hijriToGregorian(YMD hijri) {
        int offsetDays = calculateHijriOffsetDays(hijri);

        GregorianCalendar result = (GregorianCalendar) EPOCH_GREGORIAN_TEHRAN.clone();
        result.add(Calendar.DAY_OF_MONTH, offsetDays);
        return result;
    }

    public static GregorianCalendar hijriToGregorian(int year, int month, int day) {
        return hijriToGregorian(new YMD(year, month, day));
    }

    public static GregorianCalendar hijriToGregorian(YMD hijri, TimeZone outputTimezone) {
        GregorianCalendar tehranResult = hijriToGregorian(hijri);
        return toTimezone(tehranResult, outputTimezone);
    }

    public static GregorianCalendar hijriToGregorian(int year, int month, int day, TimeZone outputTimezone) {
        return hijriToGregorian(new YMD(year, month, day), outputTimezone);
    }

    /**
     * Month index is 0-based in this helper: islamicMonth0 = 0 => Muharram.
     */
    public static Calendar findFirstDayOfHijriMonth(int islamicYear, int islamicMonth0) {
        return hijriToGregorian(islamicYear, islamicMonth0 + 1, 1);
    }

    public static Calendar findFirstDayOfHijriMonth(int islamicYear, int islamicMonth0, TimeZone outputTimezone) {
        return hijriToGregorian(islamicYear, islamicMonth0 + 1, 1, outputTimezone);
    }

    /**
     * Returns the month length for given Hijri year and 1-based month.
     * Uses official Iranian data if present; otherwise falls back to tabular model.
     */
    public static int getMonthLength(int year, int month) {
        int official = getOfficialMonthLength(year, month);
        if (official != -1) {
            return official;
        }
        //Fallback (non-official): tabular model.
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Hijri month out of range: " + month);
        }
        if (month == 12) {
            return isHijriLeapYear(year) ? 30 : 29;
        }
        return (month % 2 == 1) ? 30 : 29; //odd months 30, even months 29
    }

    /**
     * Returns official month length for given year and 1-based month,
     * or -1 if no official data exists for that month.
     */
    public static int getOfficialMonthLength(int year, int month) {
        if (!hasOfficialData(year, month)) {
            return -1;
        }
        int[] months = HIJRI_MONTH_DATA.get(year);
        return months[month - 1]; //convert to 0-based index
    }

    /**
     * True if we have official data for this specific year and month.
     */
    public static boolean hasOfficialData(int year, int month) {
        if (month < 1 || month > 12) {
            return false;
        }
        int[] monthLengths = HIJRI_MONTH_DATA.get(year);
        if (monthLengths == null) {
            return false;
        }
        return month <= monthLengths.length;
    }

    /**
     * True if we have official data for this year (all 12 months).
     */
    public static boolean hasOfficialData(int year) {
        return HIJRI_MONTH_DATA.containsKey(year);
    }

    /**
     * Convert a Tehran-normalized Gregorian date to Hijri using the epoch anchor
     * and walking day-by-day, respecting official data when available.
     */
    private static YMD calculateHijriFromGregorian(GregorianCalendar tehranDate) {
        long diffMillis = tehranDate.getTimeInMillis() - EPOCH_GREGORIAN_TEHRAN.getTimeInMillis();
        int diffDays = (int) (diffMillis / MILLIS_PER_DAY); //safe at midnight

        int y = EPOCH_HIJRI.year;
        int m = EPOCH_HIJRI.month - 1; //0-based
        int d = EPOCH_HIJRI.day;

        if (diffDays > 0) {
            for (int i = 0; i < diffDays; i++) {
                d++;
                int[] monthLengths = getMonthLengthsForYear(y);
                if (d > monthLengths[m]) {
                    d = 1;
                    m++;
                    if (m >= 12) {
                        m = 0;
                        y++;
                    }
                }
            }
        } else if (diffDays < 0) {
            for (int i = 0; i < -diffDays; i++) {
                d--;
                if (d == 0) {
                    m--;
                    if (m < 0) {
                        m = 11;
                        y--;
                    }
                    int[] monthLengths = getMonthLengthsForYear(y);
                    d = monthLengths[m];
                }
            }
        }
        return new YMD(y, m + 1, d); //back to 1-based month
    }

    /**
     * Calculate offset in days between epoch Hijri date and target Hijri date.
     * Positive: target is after epoch. Negative: target is before epoch.
     */
    private static int calculateHijriOffsetDays(YMD hijri) {
        int targetY = hijri.year;
        int targetM = hijri.month - 1; //0-based
        int targetD = hijri.day;

        if (targetM < 0 || targetM >= 12) {
            throw new IllegalArgumentException("Hijri month out of range: " + hijri.month);
        }

        int[] monthLengthsTargetYear = getMonthLengthsForYear(targetY);
        int maxDay = monthLengthsTargetYear[targetM];
        if (targetD < 1 || targetD > maxDay) {
            throw new IllegalArgumentException(
                    "Hijri day out of range: " + targetD + " for " + targetY + "/" + (targetM + 1)
            );
        }

        int y = EPOCH_HIJRI.year;
        int m = EPOCH_HIJRI.month - 1;
        int d = EPOCH_HIJRI.day;

        int offsetDays = 0;

        if (isBeforeHijri(targetY, targetM, targetD, y, m, d)) {
            //Target before epoch: walk backwards from epoch to target.
            while (!(y == targetY && m == targetM && d == targetD)) {
                d--;
                offsetDays--;

                if (d == 0) {
                    m--;
                    if (m < 0) {
                        m = 11;
                        y--;
                    }
                    int[] ml = getMonthLengthsForYear(y);
                    d = ml[m];
                }
            }
        } else {
            //Target on/after epoch: walk forwards from epoch to target.
            while (!(y == targetY && m == targetM && d == targetD)) {
                d++;
                offsetDays++;

                int[] ml = getMonthLengthsForYear(y);
                if (d > ml[m]) {
                    d = 1;
                    m++;
                    if (m >= 12) {
                        m = 0;
                        y++;
                    }
                }
            }
        }

        return offsetDays;
    }

    /**
     * Returns a fresh array of month lengths for the given Hijri year.
     * If official Iranian data is present, returns a copy of that.
     * Otherwise uses tabular 30/29 pattern + leap year rule.
     */
    private static int[] getMonthLengthsForYear(int year) {
        int[] lengths = HIJRI_MONTH_DATA.get(year);
        if (lengths != null) {
            //Defensive copy: never expose internal arrays.
            return Arrays.copyOf(lengths, lengths.length);
        }
        return getTabularMonthLengths(year);
    }

    /**
     * Tabular Hijri month lengths for a given year (non-official).
     * Months alternate 30,29,... with Dhu al-Hijjah = 30 in leap years.
     */
    private static int[] getTabularMonthLengths(int year) {
        int[] lengths = new int[12];
        for (int i = 0; i < 12; i++) {
            lengths[i] = (i % 2 == 0) ? 30 : 29; //Muharram (0) = 30, Safar (1) = 29, ...
        }
        //Dhu al-Hijjah (index 11) can be 30 in leap years
        if (isHijriLeapYear(year)) {
            lengths[11] = 30;
        }
        return lengths;
    }

    /**
     * Tabular Hijri leap year rule (30-year cycle).
     * Only used for fallback years (no official data).
     */
    public static boolean isHijriLeapYear(int year) {
        int cycleYear = year % 30;
        return cycleYear == 2 || cycleYear == 5 || cycleYear == 7 ||
               cycleYear == 10 || cycleYear == 13 || cycleYear == 16 ||
               cycleYear == 18 || cycleYear == 21 || cycleYear == 24 ||
               cycleYear == 26 || cycleYear == 29;
    }

    private static boolean isBeforeHijri(int y1, int m1, int d1, int y2, int m2, int d2) {
        if (y1 != y2) return y1 < y2;
        if (m1 != m2) return m1 < m2;
        return d1 < d2;
    }

    private static GregorianCalendar toTehranTimezone(GregorianCalendar gc) {
        GregorianCalendar tehran = new GregorianCalendar(TEHRAN_TIMEZONE);
        tehran.setTimeInMillis(gc.getTimeInMillis());
        return tehran;
    }

    private static GregorianCalendar toTimezone(GregorianCalendar gc, TimeZone targetTimezone) {
        GregorianCalendar result = new GregorianCalendar(targetTimezone);
        result.setTimeInMillis(gc.getTimeInMillis());
        return result;
    }

    private static void normalizeToMidnight(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    private static Map<Integer, int[]> getIranianHijriMonthData() {
        Map<Integer, int[]> hijriData = new HashMap<>();

        hijriData.put(1340, new int[]{29, 30, 29, 30, 30, 30, 29, 30, 30, 29, 29, 30});
        hijriData.put(1341, new int[]{29, 29, 30, 29, 30, 29, 30, 30, 30, 29, 30, 29});
        hijriData.put(1342, new int[]{30, 29, 29, 30, 29, 30, 29, 30, 30, 30, 29, 30});
        hijriData.put(1343, new int[]{29, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30});
        hijriData.put(1344, new int[]{29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30});
        hijriData.put(1345, new int[]{29, 30, 30, 29, 30, 30, 29, 29, 30, 29, 29, 30});
        hijriData.put(1346, new int[]{29, 30, 30, 29, 30, 30, 30, 29, 29, 30, 29, 29});
        hijriData.put(1347, new int[]{30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 30, 29});
        hijriData.put(1348, new int[]{29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 30});
        hijriData.put(1349, new int[]{29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, 29});
        hijriData.put(1350, new int[]{30, 29, 29, 30, 30, 29, 29, 30, 29, 30, 30, 29});
        hijriData.put(1351, new int[]{30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 29});
        hijriData.put(1352, new int[]{30, 30, 29, 30, 30, 30, 29, 29, 29, 30, 29, 30});
        hijriData.put(1353, new int[]{29, 30, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1354, new int[]{29, 30, 30, 30, 29, 30, 30, 29, 29, 30, 29, 30});
        hijriData.put(1355, new int[]{29, 29, 30, 30, 29, 30, 30, 29, 30, 29, 30, 29});
        hijriData.put(1356, new int[]{30, 29, 29, 30, 30, 29, 30, 29, 30, 30, 29, 30});
        hijriData.put(1357, new int[]{29, 30, 29, 30, 29, 30, 29, 29, 30, 30, 29, 30});
        hijriData.put(1358, new int[]{30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30});
        hijriData.put(1359, new int[]{30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 29, 30});
        hijriData.put(1360, new int[]{30, 30, 29, 30, 30, 29, 30, 29, 29, 30, 29, 30});
        hijriData.put(1361, new int[]{29, 30, 29, 30, 30, 29, 30, 30, 29, 29, 30, 29});
        hijriData.put(1362, new int[]{30, 29, 30, 29, 30, 30, 29, 30, 29, 30, 29, 30});
        hijriData.put(1363, new int[]{29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30, 29});
        hijriData.put(1364, new int[]{30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30});
        hijriData.put(1365, new int[]{29, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30});
        hijriData.put(1366, new int[]{29, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30});
        hijriData.put(1367, new int[]{30, 29, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29});
        hijriData.put(1368, new int[]{30, 29, 30, 30, 29, 30, 30, 29, 29, 30, 29, 30});
        hijriData.put(1369, new int[]{29, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30, 30});
        hijriData.put(1370, new int[]{29, 29, 30, 29, 30, 29, 30, 29, 30, 30, 29, 30});
        hijriData.put(1371, new int[]{29, 30, 29, 30, 29, 30, 29, 29, 30, 30, 30, 29});
        hijriData.put(1372, new int[]{30, 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, 30});
        hijriData.put(1373, new int[]{29, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 30});
        hijriData.put(1374, new int[]{29, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30});
        hijriData.put(1375, new int[]{29, 30, 30, 29, 30, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1376, new int[]{30, 29, 30, 29, 30, 30, 29, 30, 30, 29, 29, 30});
        hijriData.put(1377, new int[]{29, 30, 29, 29, 30, 30, 29, 30, 30, 30, 29, 30});
        hijriData.put(1378, new int[]{29, 29, 30, 29, 29, 30, 29, 30, 30, 30, 29, 30});
        hijriData.put(1379, new int[]{30, 29, 29, 30, 29, 29, 30, 29, 30, 30, 29, 30});
        hijriData.put(1380, new int[]{30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30});
        hijriData.put(1381, new int[]{30, 29, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1382, new int[]{30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 29, 30});
        hijriData.put(1383, new int[]{29, 29, 30, 30, 29, 30, 30, 30, 29, 30, 29, 29});
        hijriData.put(1384, new int[]{30, 29, 29, 30, 29, 30, 30, 30, 29, 30, 30, 29});
        hijriData.put(1385, new int[]{29, 30, 29, 29, 30, 29, 30, 30, 29, 30, 30, 30});
        hijriData.put(1386, new int[]{29, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30});
        hijriData.put(1387, new int[]{29, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 30});
        hijriData.put(1388, new int[]{29, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30});
        hijriData.put(1389, new int[]{29, 30, 30, 29, 30, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1390, new int[]{30, 29, 30, 29, 30, 30, 30, 29, 30, 29, 30, 29});
        hijriData.put(1391, new int[]{29, 30, 29, 29, 30, 30, 30, 29, 30, 30, 29, 30});
        hijriData.put(1392, new int[]{29, 29, 30, 29, 29, 30, 30, 29, 30, 30, 29, 30});
        hijriData.put(1393, new int[]{30, 29, 29, 30, 29, 29, 30, 30, 29, 30, 29, 30});
        hijriData.put(1394, new int[]{30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 29, 30});
        hijriData.put(1395, new int[]{30, 30, 29, 30, 29, 30, 29, 30, 29, 29, 29, 30});
        hijriData.put(1396, new int[]{30, 29, 30, 30, 30, 29, 30, 29, 30, 29, 29, 30});
        hijriData.put(1397, new int[]{29, 30, 29, 30, 30, 30, 29, 30, 29, 30, 29, 29});
        hijriData.put(1398, new int[]{30, 29, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30});
        hijriData.put(1399, new int[]{29, 30, 29, 30, 29, 29, 30, 30, 29, 30, 30, 29});
        hijriData.put(1400, new int[]{30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 30, 30});
        hijriData.put(1401, new int[]{29, 30, 30, 29, 29, 30, 29, 29, 30, 29, 30, 29});
        hijriData.put(1402, new int[]{30, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30});
        hijriData.put(1403, new int[]{29, 30, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1404, new int[]{30, 29, 30, 30, 30, 29, 30, 29, 30, 29, 29, 30});
        hijriData.put(1405, new int[]{29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 30});
        hijriData.put(1406, new int[]{29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30});
        hijriData.put(1407, new int[]{30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 29});
        hijriData.put(1408, new int[]{30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 29});
        hijriData.put(1409, new int[]{30, 30, 30, 29, 29, 30, 29, 30, 29, 29, 30, 30});
        hijriData.put(1410, new int[]{29, 30, 30, 29, 30, 29, 30, 29, 30, 29, 29, 30});
        hijriData.put(1411, new int[]{30, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 29});
        hijriData.put(1412, new int[]{30, 30, 29, 30, 29, 30, 29, 30, 29, 30, 30, 29});
        hijriData.put(1413, new int[]{30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30});
        hijriData.put(1414, new int[]{29, 30, 29, 29, 30, 29, 30, 29, 29, 30, 30, 30});
        hijriData.put(1415, new int[]{30, 30, 29, 29, 29, 30, 29, 29, 29, 30, 30, 30});
        hijriData.put(1416, new int[]{30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 29});
        hijriData.put(1417, new int[]{30, 30, 30, 29, 29, 30, 29, 30, 29, 30, 29, 29});
        hijriData.put(1418, new int[]{30, 30, 29, 30, 30, 29, 30, 29, 29, 30, 30, 29});
        hijriData.put(1419, new int[]{29, 30, 29, 30, 29, 30, 30, 29, 29, 30, 30, 30});
        hijriData.put(1420, new int[]{29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, 29});
        hijriData.put(1421, new int[]{30, 29, 29, 30, 29, 29, 30, 30, 29, 30, 30, 30});
        hijriData.put(1422, new int[]{29, 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, 30});
        hijriData.put(1423, new int[]{29, 30, 30, 29, 29, 30, 29, 30, 29, 30, 29, 30});
        hijriData.put(1424, new int[]{30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29});
        hijriData.put(1425, new int[]{30, 29, 30, 30, 29, 30, 30, 29, 29, 30, 29, 30});
        hijriData.put(1426, new int[]{29, 29, 30, 29, 30, 30, 30, 29, 30, 30, 29, 29});
        hijriData.put(1427, new int[]{30, 29, 29, 30, 29, 30, 30, 30, 29, 30, 29, 30});
        hijriData.put(1428, new int[]{29, 30, 29, 29, 29, 30, 30, 29, 30, 30, 30, 29});
        hijriData.put(1429, new int[]{30, 29, 30, 29, 29, 29, 30, 30, 29, 30, 30, 29});
        hijriData.put(1430, new int[]{30, 30, 29, 29, 30, 29, 30, 29, 29, 30, 30, 29});
        hijriData.put(1431, new int[]{30, 30, 29, 30, 29, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1432, new int[]{30, 30, 29, 30, 30, 30, 29, 30, 29, 29, 30, 29});
        hijriData.put(1433, new int[]{29, 30, 29, 30, 30, 30, 29, 30, 29, 30, 29, 30});
        hijriData.put(1434, new int[]{29, 29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29});
        hijriData.put(1435, new int[]{29, 30, 29, 30, 29, 30, 29, 30, 30, 30, 29, 30});
        hijriData.put(1436, new int[]{29, 30, 29, 29, 30, 29, 30, 29, 30, 29, 30, 30});
        hijriData.put(1437, new int[]{29, 30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30});
        hijriData.put(1438, new int[]{29, 30, 30, 30, 29, 30, 29, 29, 30, 29, 29, 30});
        hijriData.put(1439, new int[]{29, 30, 30, 30, 30, 29, 30, 29, 29, 30, 29, 29});
        hijriData.put(1440, new int[]{30, 29, 30, 30, 30, 29, 30, 30, 29, 29, 30, 29});
        hijriData.put(1441, new int[]{29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 30});
        hijriData.put(1442, new int[]{29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, 29});
        hijriData.put(1443, new int[]{29, 30, 30, 29, 29, 30, 29, 30, 30, 29, 30, 29});
        hijriData.put(1444, new int[]{30, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 29});
        hijriData.put(1445, new int[]{30, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29, 29});
        hijriData.put(1446, new int[]{30, 30, 30, 29, 30, 30, 29, 30, 29, 29, 29, 30});
        hijriData.put(1447, new int[]{29, 30, 30, 29, 30, 30, 30, 29, 30, 29, 29, 29});
        hijriData.put(1448, new int[]{30, 29, 30, 29, 30, 30, 30, 29, 30, 29, 30, 29});

        return hijriData;
    }
}