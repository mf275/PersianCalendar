package com.farashian.test.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.farashian.pcalendar.PersianCalendar;
import com.farashian.pcalendar.PersianDateFormat;
import com.farashian.test.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SimplePersianCalendarActivity extends Activity {

    //UI Components
    private TextView tvCurrentDate, tvCurrentGregorian, tvConversionResult;
    private TextView tvDayName, tvMonthInfo, tvLeapYear;
    private EditText edtPersianDate, edtGregorianDate;
    private Button btnToday, btnConvertToGregorian, btnConvertToPersian;
    private Button btnAddDay, btnSubtractDay;
    
    //Calendar
    private PersianCalendar persianCalendar;
    private boolean         useFastLibrary = false; //Switch between libraries

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_persian_calendar);
        
        //Initialize calendar
        persianCalendar = new PersianCalendar();
        
        //Initialize UI
        initViews();
        setupListeners();
        updateCurrentDate();
    }
    
    private void initViews() {
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        tvCurrentGregorian = findViewById(R.id.tvCurrentGregorian);
        tvConversionResult = findViewById(R.id.tvPResult);
        tvDayName = findViewById(R.id.tvDayName);
        tvMonthInfo = findViewById(R.id.tvMonthInfo);
        tvLeapYear = findViewById(R.id.tvLeapYear);
        
        edtPersianDate = findViewById(R.id.edtPersianDate);
        edtGregorianDate = findViewById(R.id.edtGregorianDate);
        
        btnToday = findViewById(R.id.btnToday);
        btnConvertToGregorian = findViewById(R.id.btnConvertToGregorian);
        btnConvertToPersian = findViewById(R.id.btnConvertToPersian);
        btnAddDay = findViewById(R.id.btnAddDay);
        btnSubtractDay = findViewById(R.id.btnSubtractDay);
        
        //Set default values
        edtPersianDate.setText("1402/10/25");
        edtGregorianDate.setText("2024/01/15");
    }
    
    private void setupListeners() {
        //Today button
        btnToday.setOnClickListener(v -> {
            persianCalendar = new PersianCalendar();
            updateCurrentDate();
            showMessage("Reset to today");
        });
        
        //Convert Persian to Gregorian
        btnConvertToGregorian.setOnClickListener(v -> convertPersianToGregorian());
        
        //Convert Gregorian to Persian
        btnConvertToPersian.setOnClickListener(v -> convertGregorianToPersian());
        
        //Add/Subtract days
        btnAddDay.setOnClickListener(v -> {
            persianCalendar.add(PersianCalendar.DAY_OF_MONTH, 1);
            updateCurrentDate();
            showMessage("Added 1 day");
        });
        
        btnSubtractDay.setOnClickListener(v -> {
            persianCalendar.add(PersianCalendar.DAY_OF_MONTH, -1);
            updateCurrentDate();
            showMessage("Subtracted 1 day");
        });
        
        //Real-time conversion as user types
        edtPersianDate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 10) { //When full date is entered (1402/10/25)
                    convertPersianToGregorian();
                }
            }
        });
        
        edtGregorianDate.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 10) { //When full date is entered (2024/01/15)
                    convertGregorianToPersian();
                }
            }
        });
    }
    
    private void updateCurrentDate() {
        try {
            //Update Persian date
            String persianDate = formatPersianDate(persianCalendar);
            tvCurrentDate.setText(persianDate);
            
            //Update Gregorian equivalent
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            String gregorianDate = sdf.format(new Date(persianCalendar.getTimeInMillis()));
            tvCurrentGregorian.setText("Gregorian: " + gregorianDate);
            
            //Update date information
            updateDateInfo();
            
            //Hide any previous conversion result
            tvConversionResult.setVisibility(View.GONE);
            
        } catch (Exception e) {
            showError("Error updating date: " + e.getMessage());
        }
    }

    private void updateDateInfo() {
        try {
            //Day name
            String dayName = PersianDateFormat.getDayName(persianCalendar);
            tvDayName.setText("📅 Day: " + dayName);

            //Month info - USE getDaysInMonth() instead of getActualMaximum()
            String monthName = PersianDateFormat.getMonthName(persianCalendar);

            //CORRECT WAY: Use the Persian calendar's method
            int daysInMonth = persianCalendar.getDaysInMonth(); //Your custom method
            //OR: int daysInMonth = PersianCalendar.getDaysInMonth(
            //   persianCalendar.getYear(),
            //   persianCalendar.getMonth()
            //);

            tvMonthInfo.setText("📆 Month: " + monthName + " (" + daysInMonth + " days)");

            //Leap year
            boolean isLeap = PersianDateFormat.isLeapYear(persianCalendar);
            tvLeapYear.setText(isLeap ? "🌟 Leap Year" : "📅 Common Year");

        } catch (Exception e) {
            tvDayName.setText("Error loading date info: " + e.getMessage());
        }
    }
    
    private void updateDateInfo1() {
        try {
            //Day name
            String dayName = PersianDateFormat.getDayName(persianCalendar);
            tvDayName.setText("📅 Day: " + dayName);
            
            //Month info
            String monthName = PersianDateFormat.getMonthName(persianCalendar);
            int daysInMonth = persianCalendar.getActualMaximum(PersianCalendar.DAY_OF_MONTH);
            tvMonthInfo.setText("📆 Month: " + monthName + " (" + daysInMonth + " days)");
            
            //Leap year
            boolean isLeap = PersianDateFormat.isLeapYear(persianCalendar);
            tvLeapYear.setText(isLeap ? "🌟 Leap Year" : "📅 Common Year");
            
        } catch (Exception e) {
            tvDayName.setText("Error loading date info");
        }
    }
    
    private void convertPersianToGregorian() {
        String persianDateStr = edtPersianDate.getText().toString().trim();
        
        if (persianDateStr.isEmpty()) {
            showError("Please enter a Persian date");
            return;
        }
        
        try {
            //Parse Persian date
            String[] parts = persianDateStr.split("/");
            if (parts.length != 3) {
                showError("Invalid format. Use: yyyy/mm/dd");
                return;
            }
            
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; //Convert to 0-based
            int day = Integer.parseInt(parts[2]);
            
            //Set the date
            persianCalendar.setPersianDate(year, month, day);
            
            //Update display
            updateCurrentDate();
            
            //Show conversion result
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US);
            String result = sdf.format(new Date(persianCalendar.getTimeInMillis()));
            showConversionResult("Gregorian: " + result);
            
            showMessage("Converted successfully");
            
        } catch (NumberFormatException e) {
            showError("Invalid numbers in date");
        } catch (IllegalArgumentException e) {
            showError("Invalid date: " + e.getMessage());
        } catch (Exception e) {
            showError("Conversion error: " + e.getMessage());
        }
    }
    
    private void convertGregorianToPersian() {
        String gregorianDateStr = edtGregorianDate.getText().toString().trim();
        
        if (gregorianDateStr.isEmpty()) {
            showError("Please enter a Gregorian date");
            return;
        }
        
        try {
            //Parse Gregorian date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            Date date = sdf.parse(gregorianDateStr);
            
            if (date == null) {
                showError("Invalid date format");
                return;
            }
            
            //Set the date
            persianCalendar.setTime(date);
            
            //Update display
            updateCurrentDate();
            
            //Show conversion result
            String result = formatPersianDate(persianCalendar);
            showConversionResult("Persian: " + result);
            
            showMessage("Converted successfully");
            
        } catch (java.text.ParseException e) {
            showError("Invalid format. Use: yyyy/mm/dd");
        } catch (Exception e) {
            showError("Conversion error: " + e.getMessage());
        }
    }
    
    private String formatPersianDate(PersianCalendar calendar) {
        return String.format(Locale.US, "%04d/%02d/%02d",
                calendar.getYear(),
                calendar.getMonth() + 1,
                calendar.getDayOfMonth());
    }
    
    private void showConversionResult(String message) {
        tvConversionResult.setText(message);
        tvConversionResult.setTextColor(getResources().getColor(R.color.success, null));
        tvConversionResult.setVisibility(View.VISIBLE);
    }
    
    private void showError(String message) {
        tvConversionResult.setText("❌ " + message);
        tvConversionResult.setTextColor(getResources().getColor(R.color.error, null));
        tvConversionResult.setVisibility(View.VISIBLE);
    }
    
    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}