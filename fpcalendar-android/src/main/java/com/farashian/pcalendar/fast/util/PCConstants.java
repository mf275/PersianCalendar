package com.farashian.pcalendar.fast.util;


import java.util.*;

public class PCConstants {
    /*public static final Set<Integer> leapYears = Set.of(
            1201, 1205, 1210, 1214, 1218, 1222, 1226, 1230, 1234, 1238, 1243,
            1247, 1251, 1255, 1259, 1263, 1267, 1271, 1276, 1280, 1284, 1288,
            1292, 1296, 1300, 1304, 1309, 1313, 1317, 1321, 1325, 1329, 1333,
            1337, 1342, 1346, 1350, 1354, 1358, 1362, 1366, 1370, 1375, 1379,
            1383, 1387, 1391, 1395, 1399, 1403, 1408, 1412, 1416, 1420, 1424,
            1428, 1432, 1436, 1441, 1445, 1449, 1453, 1457, 1461, 1465, 1469,
            1474, 1478, 1482, 1486, 1490, 1494, 1498, 1502, 1507, 1511, 1515,
            1519, 1523, 1527, 1531, 1535, 1540, 1544, 1548, 1552, 1556, 1560,
            1564, 1568, 1573, 1577, 1581, 1585, 1589, 1593, 1597
    );*/


    public static final Set<Integer> leapYears;

    static {
        Set<Integer> temp = new HashSet<>();
        temp.addAll(Arrays.asList(
                1201, 1205, 1210, 1214, 1218, 1222, 1226, 1230, 1234, 1238, 1243,
                1247, 1251, 1255, 1259, 1263, 1267, 1271, 1276, 1280, 1284, 1288,
                1292, 1296, 1300, 1304, 1309, 1313, 1317, 1321, 1325, 1329, 1333,
                1337, 1342, 1346, 1350, 1354, 1358, 1362, 1366, 1370, 1375, 1379,
                1383, 1387, 1391, 1395, 1399, 1403, 1408, 1412, 1416, 1420, 1424,
                1428, 1432, 1436, 1441, 1445, 1449, 1453, 1457, 1461, 1465, 1469,
                1474, 1478, 1482, 1486, 1490, 1494, 1498, 1502, 1507, 1511, 1515,
                1519, 1523, 1527, 1531, 1535, 1540, 1544, 1548, 1552, 1556, 1560,
                1564, 1568, 1573, 1577, 1581, 1585, 1589, 1593, 1597
        ));
        leapYears = Collections.unmodifiableSet(temp);
    }

    public static final Locale PERSIAN_LOCALE = new Locale("fa", "IR");


    public static final String[] WEEKDAY_NAMES = {
            "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه"
    };

    public static final String[] WEEKDAY_NAMES_SHORT = {
            "ی", "د", "س", "چ", "پ", "ج", "ش"
    };

    public static final String[] PERSIAN_MONTH_NAMES = {
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    };

    public static final String[] PERSIAN_MONTH_NAMES_SHORT = {
            "فرو", "ارد", "خرو", "تیر", "مرد", "شهـر",
            "مهر", "آبـان", "آذر", "دی", "بهـم", "اسف"
    };

    public static final String[] HIJRI_MONTH_NAMES = {
            "محرم", "صفر", "ربیع الاول", "ربیع الثانی", "جمادی الاول", "جمادی الثانیه",
            "رجب", "شعبان", "رمضان", "شوال", "ذیقعده", "ذیحجه"
    };

    public static final String[] GREGORIAN_MONTH_NAMES = {
            "ژانویه", "فوریه", "مارس", "آپریل", "می", "ژوئن",
            "جولای", "آگوست", "سپتامبر", "اکتبر", "نوامبر", "دسامبر"
    };

    public static final String[] GREGORIAN_MONTH_NAMES_ENG = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };

    public static final String[] WEEKDAY_NAMES_IN_ENGLISH = {
            "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };

    public static final String[] WEEKDAY_NAMES_SHORT_IN_ENGLISH = {
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    };

    public static final String[] PERSIAN_MONTH_NAMES_IN_ENGLISH = {
            "Farvardin", "Ordibehesht", "Khordad", "Tir", "Mordad", "Shahrivar",
            "Mehr", "Aban", "Azar", "Dey", "Bahman", "Esfand"
    };

    public static final String[] PERSIAN_MONTH_NAMES_ENGLISH_SHORT = {
            "Far", // Farvardin
            "Ord", // Ordibehesht
            "Kho", // Khordad
            "Tir", // Tir
            "Mor", // Mordad
            "Sha", // Shahrivar
            "Meh", // Mehr
            "Aba", // Aban
            "Aza", // Azar
            "Dey", // Dey
            "Bah", // Bahman
            "Esf"  // Esfand
    };
}