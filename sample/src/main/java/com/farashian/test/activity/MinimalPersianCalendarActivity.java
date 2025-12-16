package com.farashian.test.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;


import com.farashian.pcalendar.PersianCalendar;
import com.farashian.test.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MinimalPersianCalendarActivity extends Activity {

    private TextView        txtDate;
    private PersianCalendar calendar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minimal_persian);
        
        calendar = new PersianCalendar();
        txtDate = findViewById(R.id.txtDate);
        
        Button btnPrev = findViewById(R.id.btnPrev);
        Button btnNext = findViewById(R.id.btnNext);
        Button btnToday = findViewById(R.id.btnToday);
        
        updateDate();
        
        btnPrev.setOnClickListener(v -> {
            calendar.add(PersianCalendar.DAY_OF_MONTH, -1);
            updateDate();
        });
        
        btnNext.setOnClickListener(v -> {
            calendar.add(PersianCalendar.DAY_OF_MONTH, 1);
            updateDate();
        });
        
        btnToday.setOnClickListener(v -> {
            calendar = new PersianCalendar();
            updateDate();
        });
    }
    
    private void updateDate() {
        String persianDate = String.format(Locale.US, "%04d/%02d/%02d",
                calendar.getYear(),
                calendar.getMonth() + 1,
                calendar.getDayOfMonth());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
        String gregorianDate = sdf.format(new Date(calendar.getTimeInMillis()));
        
        String displayText = String.format("📅 Persian: %s\n🌍 Gregorian: %s",
                persianDate, gregorianDate);
        
        txtDate.setText(displayText);
    }
}