package com.farashian.test;

public class TestBase {

    public static int testCount   = 0;
    public static int passedCount = 0;
    public static int failedCount = 0;

    // --- Additional Assertion Methods ---
    public static void assertNotNull(Object obj, String message) {
        testCount++;
        if (obj == null) {
            System.err.println("❌ FAIL: " + message + " - expected: not null but was: null");
            failedCount++;
        } else {
            passedCount++;
        }
    }

    public static void assertNull(Object obj, String message) {
        testCount++;
        if (obj != null) {
            System.err.println("❌ FAIL: " + message + " - expected: null but was: " + obj);
            failedCount++;
        } else {
            passedCount++;
        }
    }

    public static void assertTrue(boolean condition) {
        testCount++;
        if (!condition) {
            System.err.println("❌ FAIL: expected: <true> but was: <false>");
            failedCount++;
        } else {
            passedCount++;
        }
    }
    public static void assertFalse(boolean condition) {
        testCount++;
        if (condition) {
            System.err.println("❌ FAIL: expected: <false> but was: <true>");
            failedCount++;
        } else {
            passedCount++;
        }
    }

    public static void assertFalse(boolean condition, String message) {
        testCount++;
        if (condition) {
            System.err.println("❌ FAIL: " + message + " - expected: <false> but was: <true>");
            failedCount++;
        } else {
            passedCount++;
        }
    }

    public static void assertTrue(boolean condition, String message) {
        testCount++;
        if (!condition) {
            System.err.println("❌ FAIL: " + message + " - expected: <true> but was: <false>");
            failedCount++;
        } else {
            passedCount++;
        }
    }

    public static void assertEquals(int expected, int actual) {
        testCount++;
        if (expected != actual) {
            System.err.println("❌ FAIL: expected: " + expected + " but was: " + actual);
            failedCount++;
        } else {
            passedCount++;
        }
    }

    public static void assertEquals(int expected, int actual, String message) {
        testCount++;
        if (expected != actual) {
            System.err.println("❌ FAIL: " + message + " - expected: " + expected + " but was: " + actual);
            failedCount++;
        } else {
            passedCount++;
        }
    }

    public static void assertEquals(long expected, long actual, String message) {
        testCount++;
        if (expected != actual) {
            System.err.println("❌ FAIL: " + message + " - expected: " + expected + " but was: " + actual);
            failedCount++;
        } else {
            passedCount++;
        }
    }

    public static void assertEquals(String expected, String actual, String message) {
        testCount++;
        if (!expected.equals(actual)) {
            System.err.println("❌ FAIL: " + message + " - expected: \"" + expected + "\" but was: \"" + actual + "\"");
            failedCount++;
        } else {
            passedCount++;
        }
    }

    public static void assertEquals(String expected, String actual) {
        testCount++;
        if (!expected.equals(actual)) {
            System.err.println("❌ FAIL: expected: \"" + expected + "\" but was: \"" + actual + "\"");
            failedCount++;
        } else {
            passedCount++;
        }
    }

    public static void repeat(String expected, int count, boolean addNewLine) {
        if (addNewLine) {
            System.out.println();
        }

        for (int i = 1; i <= count; i++) {
            System.out.print(expected);
        }
        if (addNewLine) {
            System.out.println();
        }
    }

}
