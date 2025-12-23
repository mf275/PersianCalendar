package com.farashian.pcalendar.fast.util;


import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

public class IranianHijriConverter {

    private static final long MILLIS_PER_DAY = 24L * 60 * 60 * 1000;

    // Epoch: 1 Rajab 1447 (Iranian Hijri) = 2025-12-22 Gregorian (Tehran)
    private static final GregorianCalendar EPOCH_GREGORIAN;
    private static final YMD               EPOCH_HIJRI = new YMD(1447, 7, 1); // 1-based month

    static {
        TimeZone tehran = TimeZone.getTimeZone("Asia/Tehran");
        EPOCH_GREGORIAN = new GregorianCalendar(tehran);
        EPOCH_GREGORIAN.set(Calendar.YEAR, 2025);
        EPOCH_GREGORIAN.set(Calendar.MONTH, Calendar.DECEMBER); // 11
        EPOCH_GREGORIAN.set(Calendar.DAY_OF_MONTH, 22);

        // normalize time-of-day to midnight
        EPOCH_GREGORIAN.set(Calendar.HOUR_OF_DAY, 0);
        EPOCH_GREGORIAN.set(Calendar.MINUTE, 0);
        EPOCH_GREGORIAN.set(Calendar.SECOND, 0);
        EPOCH_GREGORIAN.set(Calendar.MILLISECOND, 0);
    }

    // Your official Iranian Hijri month-length data (0-based months)
    private static final HashMap<Integer, int[]> HIJRI_MONTH_DATA = getIranianHijriMonthData();

    /**
     * Gregorian -> Iranian Hijri (Iran official, within data range)
     */
    public static YMD gregorianToIranianHijri(GregorianCalendar gc) {
        GregorianCalendar g = (GregorianCalendar) gc.clone();
        normalizeToMidnight(g);

        long diffMillis = g.getTimeInMillis() - EPOCH_GREGORIAN.getTimeInMillis();
        int diffDays = (int) (diffMillis / MILLIS_PER_DAY);

        int y = EPOCH_HIJRI.year;
        //int m = EPOCH_HIJRI.month - 1; // 0-based
        int m = EPOCH_HIJRI.month - 1; // 0-based
        int d = EPOCH_HIJRI.day;

        if (diffDays > 0) {
            for (int i = 0; i < diffDays; i++) {
                d++;
                int[] monthLengths = HIJRI_MONTH_DATA.get(y);
                if (monthLengths == null) {
                    throw new IllegalStateException("No Hijri data for year " + y);
                }
                if (d > monthLengths[m]) {
                    d = 1;
                    m++;
                    if (m >= 12) {
                        m = 0;
                        y++;
                        if (!HIJRI_MONTH_DATA.containsKey(y)) {
                            throw new IllegalStateException("No Hijri data for year " + y);
                        }
                    }
                }
            }
        } else if (diffDays < 0) {
            for (int i = 0; i < -diffDays; i++) {
                d--;
                if (d == 0) {
                    m--;
                    if (m < 0) {
                        y--;
                        if (!HIJRI_MONTH_DATA.containsKey(y)) {
                            throw new IllegalStateException("No Hijri data for year " + y);
                        }
                        m = 11;
                    }
                    int[] monthLengths = HIJRI_MONTH_DATA.get(y);
                    d = monthLengths[m];
                }
            }
        }

        return new YMD(y, m+1, d); // month back to 1-based
    }

    /**
     * Iranian Hijri -> Gregorian (reverse, exact within data range)
     */
    public static GregorianCalendar iranianHijriToGregorian(YMD hijri) {
        int targetY = hijri.year;
        int targetM = hijri.month - 1; // 0-based
        int targetD = hijri.day;

        // validate range
        if (!HIJRI_MONTH_DATA.containsKey(targetY)) {
            throw new IllegalArgumentException("Hijri year out of supported range: " + targetY);
        }
        int[] monthLengths = HIJRI_MONTH_DATA.get(targetY);
        if (targetM < 0 || targetM >= 12) {
            throw new IllegalArgumentException("Hijri month out of range: " + hijri.month);
        }
        if (targetD < 1 || targetD > monthLengths[targetM]) {
            throw new IllegalArgumentException("Hijri day out of range: " + hijri);
        }

        // walk from epoch Hijri to target Hijri, counting days
        int y = EPOCH_HIJRI.year;
        int m = EPOCH_HIJRI.month - 1;
        int d = EPOCH_HIJRI.day;

        int offsetDays = 0;

        // Decide direction
        if (isBeforeHijri(targetY, targetM, targetD, y, m, d)) {
            // Walk backwards
            while (!(y == targetY && m == targetM && d == targetD)) {
                d--;
                offsetDays--;
                if (d == 0) {
                    m--;
                    if (m < 0) {
                        y--;
                        if (!HIJRI_MONTH_DATA.containsKey(y)) {
                            throw new IllegalStateException("No Hijri data for year " + y);
                        }
                        m = 11;
                    }
                    int[] ml = HIJRI_MONTH_DATA.get(y);
                    d = ml[m];
                }
            }
        } else {
            // Walk forwards
            while (!(y == targetY && m == targetM && d == targetD)) {
                d++;
                offsetDays++;
                int[] ml = HIJRI_MONTH_DATA.get(y);
                if (d > ml[m]) {
                    d = 1;
                    m++;
                    if (m >= 12) {
                        m = 0;
                        y++;
                        if (!HIJRI_MONTH_DATA.containsKey(y)) {
                            throw new IllegalStateException("No Hijri data for year " + y);
                        }
                    }
                }
            }
        }

        GregorianCalendar result = (GregorianCalendar) EPOCH_GREGORIAN.clone();
        result.add(Calendar.DAY_OF_MONTH, offsetDays);
        return result;
    }

    // ----------------- helpers -----------------

    private static boolean isBeforeHijri(int y1, int m1, int d1,
                                         int y2, int m2, int d2) {
        if (y1 != y2) return y1 < y2;
        if (m1 != m2) return m1 < m2;
        return d1 < d2;
    }

    private static void normalizeToMidnight(GregorianCalendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    private static HashMap<Integer, int[]> getIranianHijriMonthData() {
        HashMap<Integer, int[]> hijriData = new HashMap<>();
        // Add all the data from your table here (1340-1448)
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
        hijriData.put(1448, new int[]{30, 29, 30, 29, 30, 30, 30, 29, 30, 29, 30,
                29}); // Note: Last 2 months added from pattern

        return hijriData;
    }

    // Quick test
    public static void main(String[] args) {
        TimeZone tehran = TimeZone.getTimeZone("Asia/Tehran");

        GregorianCalendar g = new GregorianCalendar(tehran);
        g.set(2025, Calendar.DECEMBER, 22);
        normalizeToMidnight(g);

        YMD h = gregorianToIranianHijri(g);
        System.out.println("G -> H: " + g.getTime() + " => " + h);

        GregorianCalendar back = iranianHijriToGregorian(h);
        System.out.println("H -> G: " + h + " => " + back.getTime());
    }
}