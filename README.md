# 📅 Persian Calendar for Java — `persianCalendar` and `FastPersianCalendar` 

[![Java 8+](https://img.shields.io/badge/Java-8%2B-green?logo=java)](https://www.oracle.com/java/)
[![Android Compatible](https://img.shields.io/badge/Android-Compatible-brightgreen?logo=android)](https://developer.android.com/)
[![License: LGPL-3.0](https://img.shields.io/badge/License-LGPL--3.0-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0.html)

A **robust**, **accurate**, and **highly compatible** Persian (Solar Hijri / Jalali) calendar implementation extending `java.util.Calendar`.  
✅ Full `Calendar` API compliance  
✅ Thread-safe, immutable-style helpers  
✅ Rich formatting & parsing  
✅ Gregorian ↔ Persian bidirectional conversion

> 🔍 **Based on the official algorithm from [JDF.scr.ir](https://jdf.scr.ir)** (GNU/LGPL licensed)  
> 🇮🇷 Supports Iranian calendar rules: leap years, month lengths, Friday = holiday

---

## 🚀 Features

| ✅ Core                                                                                                                                      | ✅ Formatting                                                                                                                                                   | ✅ Utilities                                                                                                                                                                                                                                                                                                                                                                                                              |
|---------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [PersianCalendar](\PersianCalendar\pcjava\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L7-L1288) extends `java.util.Calendar` | `PersianDateFormat` with token-based patterns                                                                                                                  | [isLeapYear()](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L353-L355), [isHoliday()](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L1268-L1270), `isToday()`                                                                                                                                     |
| Gregorian ↔ Persian conversion (exact)                                                                                                      | Supports **Farsi numerals** (۱۴۰۳)                                                                                                                             | [plusDays()](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L1150-L1154), [minusMonths()](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L1201-L1203), [withFirstDayOfMonth()](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L1285-L1289) |
| 0-based months (like Java): `FARVARDIN = 0`                                                                                                 | 20+ format tokens: `yyyy`, `MMMM`, `dddd`, [a](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java), etc. | [daysBetween()](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L1259-L1262), [getAge()](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L1373-L1375), [isBetween()](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L1383-L1385)             |
| Time zone aware (`Asia/Tehran` ready)                                                                                                       | Literal text support: `'Today:' yyyy/MM/dd`                                                                                                                    | `atStartOfDay()`, [atEndOfDay()](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L1342-L1349)                                                                                                                                                                                                                                                                   |
| Leap year table (1200–1600 AH) + algorithm                                                                                                  | Parse Persian & Gregorian strings                                                                                                                              | [getQuarter()](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L1471-L1473), [getWeekOfYear()](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L1400-L1403), [getDayOfYear()](file://D:\NewProjects\PersianCalendar\android\src\main\java\com\farashian\pcalendar\PersianCalendar.java#L1391-L1394)    |

---

## 📦 Installation

---
#### Add to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
<groupId>com.github.mf275.PersianCalendar</groupId>
<artifactId>persian-calendar</artifactId>
<version>2.3.4</version>
</dependency>

<dependency>
<groupId>com.github.mf275.PersianCalendar</groupId>
<artifactId>persian-calendar-android</artifactId>
<version>2.3.4</version>
</dependency>
<dependency>
<groupId>com.github.mf275.PersianCalendar</groupId>
<artifactId>fast-persian-calendar-android</artifactId>
<version>2.3.4</version>
</dependency>
```
---

### Project Modules

- **`persian-calendar`** - Pure Java library (no Android dependencies)
- **`persian-calendar-android`** - Android library
- **`fast-persian-calendar-android`** - Android library for FastPersianCalendar
  
- | Module                              | Type              | Purpose                                     | Dependencies          |
  |-------------------------------------|-------------------|---------------------------------------------|-----------------------|
  | **`persian-calendar`**              | Pure Java library | Persian calendar calculations and utilities | Java 11+ only         |
  | **`persian-calendar-android`**      | Android library   | Android-specific features                   | Android SDK, Java 11+ |
  | **`fast-persian-calendar-android`** | Android library   | Android-specific features                   | Android SDK, Java 11+ |

### JitPack (Recommended)

Add JitPack repository to your project:

#### Gradle (Kotlin DSL)
```kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    // For Java/Kotlin projects:
    implementation("com.github.mf275.PersianCalendar:persian-calendar:v2.3.4")

    // For Android projects:
    implementation("com.github.mf275.PersianCalendar:persian-calendar-android:v2.3.4")
    //or
    implementation("com.github.mf275.PersianCalendar:fast-persian-calendar-android:v2.3.4")
}

```
## 💰 Support My Work

If you’d like to support development of **PersianCalendar**, you can donate using the following crypto wallets:

- **TON:** `UQAEX3w2Shyl7CPSYn7Dj0DoAOCNfzj1grovWAfbP0vieKK9`
- **TRC20 (USDT):** `TNqksvpBScW5bA9aP3ouhbq821YeAnMQWd`

Thank you for helping keep this project alive!

# what's new

### version 2.3.4
* - Added fromGregorian(int gYear, int gMonth, int gDay) static method
    Fixed currentGregorian() method to properly return the underlying gCal instance

### version 2.3.2
* - Separated the fast version of PersianCalendar.
* - Add DateUtils and some useful methods
* - Persian ↔ Gregorian conversion (toPersianDate(), toDate())
* - Multiple date formats (full, dash, slash, timestamp styles)

### version 2.2.1
* fix: Correct Persian day of week calculation and remove redundant complete()

* - Remove redundant complete() call from getDayOfWeek() method
  (get() already calls complete() internally)
* - Fix computeAllCalendarFields() to convert Gregorian day of week
  to Persian using calculatePersianOffset()
* - Update calculateFirstDayOfMonth() to return Persian day of week
* - Ensure Persian calendar correctly shows Saturday-first week
  with proper offset conversion (Saturday=7, Sunday=1, etc.)
* - Add Georgian offset calculation methods for Gregorian calendar support

* Previously, getDayOfWeek() was returning Gregorian values because
  computeFields() was using gCal.get(DAY_OF_WEEK) directly without
  conversion to Persian system.

### version 2.2.0
* Persian Calendar Weekday Support - Added calculatePersianOffset() method for converting 
* between Java Calendar weekdays and Persian calendar offsets

### version 2.1.5
* Add comprehensive Gregorian date support to both calendar classes
* Add 20+ new methods for Gregorian date manipulation
* Improve cross-version Java compatibility (Java 8+)
* Expand test suite with Gregorian date tests
---

# ⚡ FastPersianDateFormat & FastPersianCalendar 📅

A high-speed and optimized library for managing the Shamsi (Jalali) calendar in Java. This library is built upon the standard Java `Calendar` and `DateFormat` classes, providing highly optimized performance for heavy processing tasks.

---

[**Read this documentation in Farsi (Persian)**](./README.fa.md)

---

## 1. Date Formatting

Using `FastPersianDateFormat`, you can display Shamsi dates with various patterns and control the display of numbers (Farsi/Persian or English/Latin).
```java
import com.farashian.pcalendar.fast.FastPersianDateFormat;
import com.farashian.pcalendar.fast.FastPersianCalendar;
import com.farashian.pcalendar.fast.FastPersianDateFormat;
import com.farashian.pcalendar.fast.FastPersianCalendar;

public class DateFormatExample {
    public static void main(String[] args) {

        // Create an instance of FastPersianCalendar
        FastPersianCalendar date = new FastPersianCalendar();

        // Create a formatter and set pattern inside a method
        FastPersianDateFormat formatter = new FastPersianDateFormat();
        formatter.setPattern("yyyy/MM/dd");
        
        System.out.println("📅 Simple Date: " + formatter.format(date)); // Output: 1404/09/10 (Example)

        formatter.setPattern("DDDD, d MMMM yyyy");
        formatter.setNumberCharacter(FastPersianDateFormat.PersianDateNumberCharacter.FARSI);
        System.out.println("📝 Full Persian Date: " + formatter.format(date)); // Output: یکشنبه, ۱۰ آذر ۱۴۰۴ (Example)

        formatter.setPattern("yyyy-MM-dd HH:mm:ss");
        formatter.setNumberCharacter(FastPersianDateFormat.PersianDateNumberCharacter.ENGLISH); // Revert to English numbers
        System.out.println("⏰ Date and Time: " + formatter.format(date)); // Output: 1404-09-10 17:54:30 (Example)
    }
}

```

### Formatting Patterns

| Pattern | Description                       | Example (Farsi Output) |
|:--------|:----------------------------------|:-----------------------|
| `yyyy`  | Four-digit year                   | ۱۴۰۴                   |
| `yy`    | Two-digit year                    | ۰۴                     |
| `MMMM`  | Full month name                   | آذر                    |
| `MMM`   | Short month name                  | آذر                    |
| `MM`    | Month number with leading zero    | ۰۹                     |
| `M`     | Month number without leading zero | ۹                      |
| `dd`    | Day of month with leading zero    | ۰۹                     |
| `d`     | Day of month without leading zero | ۹                      |
| `dddd`  | Full day name                     | یکشنبه                 |
| `ddd`   | Short day name                    | یکشنبه                 |
| `HH`    | 24-hour clock                     | ۱۴                     |
| `hh`    | 12-hour clock                     | ۰۲                     |
| `mm`    | Minute                            | ۰۵                     |
| `ss`    | Second                            | ۰۹                     |
| `a`     | AM/PM indicator (ق.ظ / ب.ظ)       | ب.ظ                    |

---

## 2. Date Calculations

Use the `add` method in `FastPersianCalendar` to easily perform addition and subtraction operations on dates.

```java
import com.farashian.pcalendar.fast.FastPersianCalendar;

public class DateExample {
    public static void main(String[] args) {
        FastPersianCalendar date = new FastPersianCalendar(1402, 0, 1); // Farvardin 1, 1402

        // Add days
        date.addDays(10);
        System.out.println("➕ After 10 days: " + date.getLongDate()); // Output: 1402/01/11

        // Add months
        date.addMonths(2);
        System.out.println("📆 After 2 months: " + date.getLongDate()); // Output: 1402/03/11

        
        // Add years
        date.addYears(1);
        System.out.println("🎊 After 1 year: " + date.getLongDate()); // Output: 1403/03/11
        
        // Subtract days (using a negative number)
        date.addDays(-5);
        System.out.println("➖ 5 days before: " + date.getLongDate()); // Output: 1403/03/06

        // Add days  Get a copy of this calendar with days added
        FastPersianCalendar nDate = date.plusDays(10);
        System.out.println("➕ After 10 days: " + date.getLongDate()); // Output: 1403/03/06
        System.out.println("➕ After 10 days: " + nDate.getLongDate()); // Output: 1403/03/16

    }
}

```

---

## 3. Date Conversions

Converting between String and Persian date, and also converting Gregorian dates to Shamsi.

```java
import com.farashian.pcalendar.fast.FastPersianDateFormat;
import com.farashian.pcalendar.fast.FastPersianCalendar;
import java.text.ParseException;

public class Conversions {
    public static void main(String[] args) {
        try {
            FastPersianDateFormat formatter = new FastPersianDateFormat();
            
            // Convert String to Persian Date
            FastPersianCalendar date = formatter.parse("1402/10/15", "yyyy/MM/dd");
            System.out.println("Parsed Date: " + date.getLongDate()); // Output: 1402/10/15
            
            // Convert Gregorian Date to Persian Date (parseGrg)
            FastPersianCalendar fromGregorian = formatter.parseGrg("2024-01-05", "yyyy-MM-dd");
            System.out.println("From Gregorian Date: " + fromGregorian.getLongDate()); // Output: 1402/10/15
            
            // Convert Persian Date to String
            String dateString = formatter.format(date);
            System.out.println("Date to String: " + dateString); // Output: 1402/10/15
            
        } catch (ParseException e) {
            System.out.println("❌ Error parsing date: " + e.getMessage());
        }
    }
}
```

---

## 4. Date Comparison

Comparing two dates and calculating the time difference between them.

```java
import com.farashian.pcalendar.fast.FastPersianCalendar;

public class DateComparisonExample {
    public static void main(String[] args) {

        FastPersianCalendar date1 = new FastPersianCalendar(1402, 0, 1);
        FastPersianCalendar date2 = new FastPersianCalendar(1402, 0, 15);

        // Comparison
        if (date1.before(date2)) {
            System.out.println("Date1 is before Date2");
        }

        if (date2.after(date1)) {
            System.out.println("Date2 is after Date1");
        }

        if (date1.equals(date2)) {
            System.out.println("Dates are equal");
        }

        // Difference in days (calculated based on milliseconds)
        long difference = date2.getTimeInMillis() - date1.getTimeInMillis();
        long days = difference / (1000 * 60 * 60 * 24);
        System.out.println("Difference in days: " + days + " days"); // Output: 14 days
    }
}

```

---

## 5. 🏗️ Project Architecture

The library includes both a standard version and a high-speed version. The `Fast` versions are optimized for environments requiring high performance and heavy date processing.

### File Structure

```text
src/
├── com/farashian/pcalendar/
│   ├── PersianCalendar.java      # Standard Calendar version
│   ├── PersianDateFormat.java    # Standard Formatter
│   ├── PCConstants.java          # Application constants
│   └── fast/                       # High-speed versions
│       ├── FastPersianCalendar.java    # High-speed Calendar
│       └── FastPersianDateFormat.java  # High-speed Formatter
```

### Version Differences

| Feature      | Standard Version (`MyPersian...`) | Fast Version (`FastPersian...`) |
|:-------------|:----------------------------------|:--------------------------------|
| **Speed**    | Adequate                          | **Excellent**                   |
| **Memory**   | Medium                            | **Optimized**                   |
| **Features** | Complete                          | Complete                        |
| **Usage**    | General                           | Heavy/Intensive Processing      |

---

## 6. 📊 Performance Test

Here's a simple test main class for your DateUtils class:

```java
import com.farashian.pcalendar.PersianCalendar;
import com.farashian.pcalendar.fast.FastPersianCalendar;

public class PerformanceTest {
    public static void main(String[] args) {
        int count = 100000;
        
        // Standard Version Test
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            PersianCalendar date = new PersianCalendar();
            date.add(persianCalendar.DAY_OF_MONTH, i);
        }
        long end = System.currentTimeMillis();
        System.out.println("⏳ Standard Version Time: " + (end - start) + " ms");
        
        // Fast Version Test
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            FastPersianCalendar date = new FastPersianCalendar();
            date.add(FastPersianCalendar.DAY_OF_MONTH, i);
        }
        end = System.currentTimeMillis();
        System.out.println("⚡ Fast Version Time: " + (end - start) + " ms");
    }
}
```

**Sample Results:**

```text
⏳ Standard Version Time: 450 ms
⚡ Fast Version Time: 140 ms
```

---

## 7. 🔧 Advanced Settings

### Setting Time Zone

To ensure correct time calculations, you can manually set the time zone.

```java
import com.farashian.pcalendar.fast.FastPersianCalendar;
import com.farashian.pcalendar.fast.FastPersianDateFormat;
import java.util.TimeZone;

public class TimeZoneExample {
    public static void main(String[] args) {
        // Set Tehran time zone
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Tehran");
        FastPersianCalendar calendar = new FastPersianCalendar(timeZone);

        // Or in the formatter
        FastPersianDateFormat formatter = new FastPersianDateFormat();
        formatter.setTimeZone(timeZone);
    }
}

```

### Handling Leap Year

Checking if a year is a leap year and finding the number of days in a month.

```java
import com.farashian.pcalendar.fast.FastPersianCalendar;
public class LeapYearCheck {
    public static void main(String[] args) {
        // Assuming FastPersianCalendar is already imported
        FastPersianCalendar date = new FastPersianCalendar(1403, 11, 30); // Esfand 30, 1403
        if (date.isLeapYear()) {
            System.out.println("✅ Year " + date.getYear() + " is a leap year");
        } else {
            System.out.println("❌ Year " + date.getYear() + " is not a leap year");
        }

        // Days in Esfand month during a leap year
        int daysInEsfand = date.getDaysInMonth();
        System.out.println("📊 Days in Esfand " + date.getYear() + ": " + daysInEsfand + " days"); // Output: 30 days
    }
}

```

---

## 8. 🎯 Practical Examples

### Example 1: Simple Monthly Calendar

Displaying the days of a month along with the day of the week name.

```java
import com.farashian.pcalendar.fast.FastPersianCalendar;

public class MonthlyCalendar {
    public static void main(String[] args) {
        FastPersianCalendar today = new FastPersianCalendar();
        int year = today.getYear();
        int month = today.getMonth();
        
        System.out.println("📅 Calendar for " + today.getMonthName() + " " + year);
        System.out.println("=".repeat(40));
        
        FastPersianCalendar firstOfMonth = new FastPersianCalendar(year, month, 1);
        int daysInMonth = firstOfMonth.getDaysInMonth();
        
        for (int day = 1; day <= daysInMonth; day++) {
            FastPersianCalendar date = new FastPersianCalendar(year, month, day);
            System.out.printf("%2d %s | ", day, date.getWeekdayName());
            
            // Newline every 3 days for better readability
            if (day % 3 == 0) System.out.println();
        }
    }
}
```

### Example 2: Date Calculator

Calculating the number of days and months between two dates.

```java
import com.farashian.pcalendar.fast.FastPersianCalendar;
import com.farashian.pcalendar.fast.FastPersianDateFormat;
import java.text.ParseException;

public class DateCalculator {
    public static void main(String[] args) throws ParseException {
        FastPersianDateFormat formatter = new FastPersianDateFormat("yyyy/MM/dd");
        
        FastPersianCalendar start = formatter.parse("1402/01/01", "yyyy/MM/dd");
        FastPersianCalendar end = formatter.parse("1402/12/29", "yyyy/MM/dd");
        
        long difference = end.getTimeInMillis() - start.getTimeInMillis();
        long days = difference / (1000 * 60 * 60 * 24);
        
        System.out.println("📊 Total days in 1402: " + days + " days"); // Output: 364 days (for a non-leap year)
        // Approximate month calculation
        System.out.println("📈 Total months: " + (end.getMonth() - start.getMonth() + 12) % 12 + " months"); 
    }
}
```

---

## 9. 🐛 Troubleshooting

### Common Issues and Solutions

| Issue                           | Possible Reason               | Solution                                                                          |
|:--------------------------------|:------------------------------|:----------------------------------------------------------------------------------|
| `ClassNotFoundException`        | Class is not in the classpath | Ensure the `src/` folder or the JAR file is correctly added.                      |
| Incorrect date                  | Wrong time zone               | Call `setTimeZone("Asia/Tehran")`.                                                |
| Farsi numbers are not displayed | Not configured                | Set `setNumberCharacter(FastPersianDateFormat.PersianDateNumberCharacter.FARSI)`. |
| Error parsing date              | Incorrect date format         | Check the input date format against the pattern set by `setPattern`.              |

### Debug Logging

To view internal calendar details, you can use methods to access various fields:

```java
import com.farashian.pcalendar.fast.FastPersianCalendar;
import java.util.Calendar;

public class Debugging {
    public static void main(String[] args) {
        FastPersianCalendar date = new FastPersianCalendar();
        
        // Display debug information
        System.out.println("🔍 Debug Information:");
        System.out.println("Persian Date: " + date.getLongDate());
        System.out.println("Gregorian Date: " + date.getTime());
        System.out.println("Year: " + date.getYear());
        System.out.println("Month: " + date.getMonth());
        System.out.println("Day: " + date.getDayOfMonth());
        System.out.println("Day of Week (based on Calendar): " + date.get(Calendar.DAY_OF_WEEK));
    }
}
```

```java

import com.farashian.pcalendar.util.DateUtils;
import com.farashian.pcalendar.PersianCalendar;
import com.farashian.pcalendar.YMD;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TestDateUtils {
    
    public static void main(String[] args) {
        System.out.println("=== Testing DateUtils ===\n");
        
        // Test 1: Basic Persian date formatting
        System.out.println("🔍 Current Persian Dates:");
        System.out.println("Now (full): " + DateUtils.nowFullDateWithDayFarsi());
        System.out.println("Now (dash): " + DateUtils.nowDashDateFarsi());
        System.out.println("Now (slash): " + DateUtils.nowSlashDateFarsi());
        System.out.println("Day of week: " + DateUtils.nowFarsiDay());
        System.out.println("Time: " + DateUtils.getNowTime());
        System.out.println("Time with seconds: " + DateUtils.getNowTimeWithSeconds());
        System.out.println("DateTime: " + DateUtils.getFarsiDateWithTime());
        System.out.println();
        
        // Test 2: Timestamp formatting
        System.out.println("📅 Timestamp Formats:");
        System.out.println("Timestamp dash: " + DateUtils.nowTimeStampDashFarsi());
        System.out.println("Timestamp underscore: " + DateUtils.nowTimeStampUnderscoreFarsi());
        System.out.println("Custom pattern (yyyy/MM/dd HH:mm): " + DateUtils.nowFarsi("yyyy/MM/dd HH:mm"));
        System.out.println();
        
        // Test 3: Date conversion
        System.out.println("🔄 Date Conversions:");
        Date            currentDate = new Date();
        PersianCalendar persianDate = DateUtils.toPersianDate(currentDate);
        System.out.println("Current Gregorian date: " + currentDate);
        System.out.println("Converted to Persian: " + DateUtils.getFarsiFullDate(currentDate));
        
        // Convert back
        Date convertedBack = DateUtils.toDate(persianDate);
        System.out.println("Converted back to Gregorian: " + convertedBack);
        System.out.println();
        
        // Test 4: Date manipulation
        System.out.println("⚙️ Date Manipulation:");
        long startOfDay = DateUtils.getStartDate(System.currentTimeMillis());
        long endOfDay = DateUtils.getEndDate(System.currentTimeMillis());
        System.out.println("Start of today (UTC): " + new Date(startOfDay));
        System.out.println("End of today (UTC): " + new Date(endOfDay));
        
        // Add one day
        Date tomorrow = DateUtils.addOneDay(currentDate);
        System.out.println("Tomorrow: " + tomorrow);
        System.out.println();
        
        // Test 5: Date before now
        System.out.println("⏮️ Past Dates:");
        String threeDaysAgo = DateUtils.dateBeforeNowFarsi(3);
        System.out.println("3 days ago (Persian): " + threeDaysAgo);
        
        long threeDaysAgoMillis = DateUtils.dateBeforeNow(3);
        System.out.println("3 days ago (millis): " + threeDaysAgoMillis);
        System.out.println();
        
        // Test 6: Islamic calendar conversion
        System.out.println("🌙 Islamic Calendar Conversion:");
        GregorianCalendar gregorian = new GregorianCalendar(2024, Calendar.MARCH, 11);
        YMD               hijriDate = DateUtils.islamicFromGregorian(gregorian);
        System.out.println("Gregorian: " + gregorian.getTime());
        System.out.println("Iranian Hijri: " + hijriDate);
        
        // Check if valid
        if (DateUtils.isValidIranianHijriDate(hijriDate)) {
            System.out.println("Valid Iranian Hijri date");
            int dayOfYear = DateUtils.getDayOfIranianHijriYear(hijriDate);
            System.out.println("Day of year: " + dayOfYear);
        }
        
        // Test official data lookup
        System.out.println("Has official data for 1444 AH: " + DateUtils.hasOfficialData(1444));
        System.out.println("Month length for 1444-1: " + DateUtils.getOfficialMonthLength(1444, 1));
        System.out.println();
        
        System.out.println("=== Test Complete ===");
    }
}

```
````
