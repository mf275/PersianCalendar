package com.farashian.test.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.farashian.pcalendar.PersianCalendar;
import com.farashian.pcalendar.PersianDateFormat;
import com.farashian.pcalendar.YMD;
import com.farashian.pcalendar.fast.FastPersianCalendar;
import com.farashian.pcalendar.fast.FastPersianDateFormat;
import com.farashian.test.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.farashian.pcalendar.PCalendarUtils.getHijriMonthName;
import static com.farashian.pcalendar.fast.FastPersianCalendar.getMonthName;
import static com.farashian.pcalendar.fast.FastPersianCalendar.gregorianToJalaliFast;
import static com.farashian.pcalendar.fast.util.HijriConvertor.gregorianToHijri;
import static com.farashian.pcalendar.fast.util.NumberConvertor.convertToEnglishNumbers;
import static java.lang.String.format;

public class PersianCalendarActivity extends Activity {

    //UI Components
    private Spinner      spnLibrary;
    private Spinner      spnPattern;
    private Spinner      spnNumberFormat;
    private EditText     edtPersianDate;
    private EditText     edtGregorianDate;
    private EditText     edtCustomPattern;
    private TextView     tvCurrentDate;
    private TextView     tvConvertedDate;
    private TextView     tvParsedDate;
    private TextView     tvFastLibrary;
    private TextView     tvIResult;
    private TextView     tvPResult;
    private TextView     tvGResult;
    private TextView     tvUpdatedDate;
    private Button       btnConvertToPersian;
    private Button       btnConvertToGregorian;
    private Button       btnParseDate;
    private Button       btnFormatNow;
    private Button       btnTestPerformance;
    private RecyclerView recyclerTestCases;

    //Adapters
    private TestCaseAdapter      testCaseAdapter;
    private ArrayAdapter<String> libraryAdapter;
    private ArrayAdapter<String> patternAdapter;
    private ArrayAdapter<String> numberFormatAdapter;

    //Libraries
    private PersianDateFormat     myPersianDateFormat;
    private FastPersianDateFormat fastPersianDateFormat;

    //Test patterns
    private static final String[] PATTERNS = {
            "yyyy/MM/dd",
            "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm",
            "yyyy/MM/dd HH:mm:ss",
            "dddd, d MMMM yyyy",
            "dddd, d MMMM yyyy HH:mm:ss",
            "ddd, d MMM yy",
            "yyyy/MM/dd hh:mm a",
            "yyyy-MM-dd hh:mm:ss a",
            "MMMM yyyy"
    };

    //Test cases
    private List<TestCase> testCases = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persian_calendar);

        //Initialize libraries
        myPersianDateFormat   = new PersianDateFormat();
        fastPersianDateFormat = new FastPersianDateFormat();

        //Initialize UI
        initViews();
        setupSpinners();
        setupButtons();
        setupRecyclerView();
        loadTestCases();
        updateCurrentDate();
    }

    private void initViews() {
        spnLibrary            = findViewById(R.id.spnLibrary);
        spnPattern            = findViewById(R.id.spnPattern);
        spnNumberFormat       = findViewById(R.id.spnNumberFormat);
        edtPersianDate        = findViewById(R.id.edtPersianDate);
        edtGregorianDate      = findViewById(R.id.edtGregorianDate);
        edtCustomPattern      = findViewById(R.id.edtCustomPattern);
        tvCurrentDate         = findViewById(R.id.tvCurrentDate);
        tvConvertedDate       = findViewById(R.id.tvConvertedDate);
        tvParsedDate          = findViewById(R.id.tvParsedDate);
        tvFastLibrary         = findViewById(R.id.tvFastLibrary);
        tvIResult             = findViewById(R.id.tvIResult);
        tvPResult             = findViewById(R.id.tvPResult);
        tvGResult             = findViewById(R.id.tvGResult);
        tvUpdatedDate         = findViewById(R.id.tvUpdatedDate);
        btnConvertToPersian   = findViewById(R.id.btnConvertToPersian);
        btnConvertToGregorian = findViewById(R.id.btnConvertToGregorian);
        btnParseDate          = findViewById(R.id.btnParseDate);
        btnFormatNow          = findViewById(R.id.btnFormatNow);
        btnTestPerformance    = findViewById(R.id.btn_test_performance);
        recyclerTestCases     = findViewById(R.id.recycler_test_cases);

        //Set default values
        edtGregorianDate.setText("2024/01/15");
        edtPersianDate.setText("1402/10/25");
        edtCustomPattern.setText("dddd, d MMMM yyyy HH:mm:ss");
    }

    private void setupSpinners() {
        //Library spinner
        String[] libraries = {"PersianCalendar", "FastPersianCalendar"};
        libraryAdapter = new ArrayAdapter<>(this,
                                            android.R.layout.simple_spinner_item, libraries);
        libraryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLibrary.setAdapter(libraryAdapter);
        spnLibrary.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCurrentDate();
                tvFastLibrary.setText(position == 1 ? "Using Fast Library" : "Using Standard Library");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Pattern spinner
        patternAdapter = new ArrayAdapter<>(this,
                                            android.R.layout.simple_spinner_item, PATTERNS);
        patternAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnPattern.setAdapter(patternAdapter);
        spnPattern.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                edtCustomPattern.setText(PATTERNS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Number format spinner
        String[] numberFormats = {"English Numbers", "Farsi Numbers"};
        numberFormatAdapter = new ArrayAdapter<>(this,
                                                 android.R.layout.simple_spinner_item, numberFormats);
        numberFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnNumberFormat.setAdapter(numberFormatAdapter);
    }

    private boolean isDatePickerOpen = false;

    private void setupButtons() {
        btnConvertToPersian.setOnClickListener(v -> convertGregorianToPersian());
        btnConvertToGregorian.setOnClickListener(v -> convertPersianToGregorian());
        btnParseDate.setOnClickListener(v -> parseDateString());
        btnFormatNow.setOnClickListener(v -> formatCurrentDate());
        btnTestPerformance.setOnClickListener(v -> runPerformanceTest());

        //Add text change listeners for real-time conversion
        edtGregorianDate.addTextChangedListener(gregorianDateTextWatcher);


        edtGregorianDate.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && !isDatePickerOpen) {
                isDatePickerOpen = true;
                convertGregorianToPersian();
                hideKeyboard(edtGregorianDate);

                //Reset flag after a delay to prevent double triggering
                edtGregorianDate.postDelayed(() -> isDatePickerOpen = false, 500);
            }
            return true;
        });


        edtPersianDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() >= 10) {
                    convertPersianToGregorian();
                }
            }
        });
    }

    private void setupRecyclerView() {
        testCaseAdapter = new TestCaseAdapter(testCases, testCase -> {
            //Execute test case when clicked
            executeTestCase(testCase);
        });

        recyclerTestCases.setLayoutManager(new LinearLayoutManager(this));
        recyclerTestCases.setAdapter(testCaseAdapter);
    }

    private void testFinalFix() {
        try {
            //testCalendarMethods();
            SimpleDateFormat sdf  = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            Date             date = sdf.parse("1979/08/18");

            StringBuilder result = new StringBuilder();
            result.append("Testing 1979/08/18 conversion:\n\n");

            //Create calendar with Date constructor
            FastPersianCalendar fastCal = new FastPersianCalendar(date);

            //Force complete computation
            //fastCal.compute();

            result.append("Calendar Instance: ")
                    .append(fastCal.getYear()).append("/")
                    .append(fastCal.getMonth() + 1).append("/")
                    .append(fastCal.getDayOfMonth()).append("\n\n");

            //Debug info
            result.append("Debug Info:\n");
            result.append("Calendar Time in millis: ").append(fastCal.getTimeInMillis()).append("\n");
            result.append("Gregorian from calendar: ")
                    .append(fastCal.gCal.get(Calendar.YEAR)).append("/")
                    .append(fastCal.gCal.get(Calendar.MONTH) + 1).append("/")
                    .append(fastCal.gCal.get(Calendar.DAY_OF_MONTH)).append("\n\n");

            result.append("Expected: 1358/05/27\n\n");

            boolean calendarCorrect = fastCal.getYear() == 1358 &&
                                      (fastCal.getMonth() + 1) == 5 &&
                                      fastCal.getDayOfMonth() == 27;

            result.append("Calendar Instance: ").append(calendarCorrect ? "✓ CORRECT" : "✗ WRONG");

            tvConvertedDate.setText(result.toString());

            //Log debug info
            Log.d("PersianCalendar", fastCal.toDebugString());

        } catch (Exception e) {
            tvConvertedDate.setText("Error: " + e.getMessage());
            Log.e("PersianCalendar", "Test error", e);
        }
    }

    private void testCalendarMethods() {
        PersianCalendar cal = new PersianCalendar();
        cal.setPersianDate(1402, 7, 15); //Aban (month 7, 0-based)

        StringBuilder result = new StringBuilder();
        result.append("Testing Calendar Methods:\n\n");

        //Test getActualMaximum for DAY_OF_MONTH
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        result.append("Aban (month 8) max days: ").append(maxDays).append("\n");

        //Test different months
        int[] testMonths = {0, 5, 7, 11}; //Farvardin, Shahrivar, Aban, Esfand
        for (int month : testMonths) {
            cal.set(Calendar.MONTH, month);
            int    days      = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            String monthName = getMonthName(month, Locale.ENGLISH);
            result.append(format("%-12s: %d days\n", monthName, days));
        }

        //Show results
        tvPResult.setText(result.toString());
        tvPResult.setVisibility(View.VISIBLE);
    }

    /**
     * Verify that conversion is working for key dates
     */
    public static String verifyConversion(int gregorianYear, int gregorianMonth, int gregorianDay) {
        try {
            //Create date
            Calendar cal = Calendar.getInstance();
            cal.set(gregorianYear, gregorianMonth - 1, gregorianDay);
            Date date = cal.getTime();

            //Create Persian calendar
            FastPersianCalendar persianCal = new FastPersianCalendar(date);
            //persianCal.complete();

            //Test algorithm directly
            int[] directResult = new int[3];
            gregorianToJalaliFast(gregorianYear, gregorianMonth, gregorianDay, directResult);

            return format(Locale.US,
                          "Gregorian: %04d/%02d/%02d\n" +
                          "Direct Algorithm: %04d/%02d/%02d\n" +
                          "Calendar Instance: %04d/%02d/%02d\n" +
                          "Match: %s",
                          gregorianYear, gregorianMonth, gregorianDay,
                          directResult[0], directResult[1], directResult[2],
                          persianCal.getYear(), persianCal.getMonth(), persianCal.getDayOfMonth(),
                          (directResult[0] == persianCal.getYear() &&
                           directResult[1] == persianCal.getMonth() &&
                           directResult[2] == persianCal.getDayOfMonth()) ? "✓" : "✗");
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void loadTestCases() {
        testCases.clear();

        //Test case 1: Basic date formatting
        testCases.add(new TestCase(
                "Basic Date Format",
                "Format current date in yyyy/MM/dd",
                "Click to format current date"
        ));

        //Test case 2: Date parsing
        testCases.add(new TestCase(
                "Date Parsing",
                "Parse Persian date string",
                "Parse: 1402/10/25"
        ));

        //Test case 3: Gregorian to Persian
        testCases.add(new TestCase(
                "Gregorian → Persian",
                "Convert Gregorian to Persian",
                "Convert: 2024/01/15"
        ));

        //Test case 4: Persian to Gregorian
        testCases.add(new TestCase(
                "Persian → Gregorian",
                "Convert Persian to Gregorian",
                "Convert: 1402/10/25"
        ));

        //Test case 5: Custom formatting
        testCases.add(new TestCase(
                "Custom Format",
                "Format with custom pattern",
                "Pattern: dddd, d MMMM yyyy"
        ));

        //Test case 6: Farsi numbers
        testCases.add(new TestCase(
                "Farsi Numbers",
                "Format with Farsi numbers",
                "۱۴۰۲/۱۰/۲۵"
        ));

        //Test case 7: Time formatting
        testCases.add(new TestCase(
                "Time Format",
                "Format with time",
                "Pattern: yyyy/MM/dd HH:mm:ss"
        ));

        //Test case 8: Month names
        testCases.add(new TestCase(
                "Month Names",
                "Test Persian month names",
                "Format: MMMM yyyy"
        ));

        //Test case 9: Weekday names
        testCases.add(new TestCase(
                "Weekday Names",
                "Test Persian weekday names",
                "Format: dddd"
        ));

        //Test case 10: Leap year test
        testCases.add(new TestCase(
                "Leap Year",
                "Check if year is leap",
                "Test years: 1401, 1402, 1403"
        ));

        testCaseAdapter.notifyDataSetChanged();
    }

    private void executeTestCase(TestCase testCase) {
        switch (testCase.getTitle()) {
            case "Basic Date Format":
                formatCurrentDate();
                break;
            case "Date Parsing":
                edtPersianDate.setText("1402/10/25");
                parseDateString();
                break;
            case "Gregorian → Persian":
                edtGregorianDate.setText("2024/01/15");
                convertGregorianToPersian();
                break;
            case "Persian → Gregorian":
                edtPersianDate.setText("1402/10/25");
                convertPersianToGregorian();
                break;
            case "Custom Format":
                edtCustomPattern.setText("dddd, d MMMM yyyy");
                formatCurrentDate();
                break;
            case "Farsi Numbers":
                spnNumberFormat.setSelection(1); //Farsi numbers
                formatCurrentDate();
                break;
            case "Time Format":
                edtCustomPattern.setText("yyyy/MM/dd HH:mm:ss");
                formatCurrentDate();
                break;
            case "Month Names":
                edtCustomPattern.setText("MMMM yyyy");
                formatCurrentDate();
                break;
            case "Weekday Names":
                edtCustomPattern.setText("dddd");
                formatCurrentDate();
                break;
            case "Leap Year":
                testLeapYears();
                break;
        }
    }


    private void convertGregorianToPersian() {
        //Prevent recursive calls from TextWatcher
        edtGregorianDate.removeTextChangedListener(gregorianDateTextWatcher);

        //Create a DatePicker dialog
        Calendar calendar = Calendar.getInstance();

        //If there's already a date in the input field, use it as default
        String existingDate = "";//edtGregorianDate.getText().toString().trim();
        if (!existingDate.isEmpty()) {
            try {
                SimpleDateFormat sdf  = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
                Date             date = sdf.parse(existingDate);
                calendar.setTime(date);
            } catch (ParseException e) {
                //Use current date if parsing fails
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    //Format selected date
                    String selectedDate = format(Locale.US, "%04d/%02d/%02d", year, month, dayOfMonth);
                    edtGregorianDate.setText(selectedDate);

                    //Automatically trigger conversion
                    performConversion(selectedDate);

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    //Separate the conversion logic to avoid recursion
    private void performConversion(String gregorianDate) {
        try {
            SimpleDateFormat sdf  = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            Date             date = sdf.parse(gregorianDate);

            if (date == null) {
                showToast("Invalid Gregorian date format");
                return;
            }

            String pattern = edtCustomPattern.getText().toString().trim();
            fastNumberFormat = spnNumberFormat.getSelectedItemPosition() == 0 ?
                    FastPersianDateFormat.PersianDateNumberCharacter.ENGLISH :
                    FastPersianDateFormat.PersianDateNumberCharacter.FARSI;

            numberFormat = spnNumberFormat.getSelectedItemPosition() == 0 ?
                    PersianDateFormat.PersianDateNumberCharacter.ENGLISH :
                    PersianDateFormat.PersianDateNumberCharacter.FARSI;

            String result;
            int[]  iresult  = null;
            YMD    iresult2 = null;
            int    perMonth;
            String           pRresult;
            String           gRresult;
            String           iRresult = "";
            if (spnLibrary.getSelectedItemPosition() == 0) {
                //PersianCalendar
                PersianCalendar persianCalendar = new PersianCalendar();
                persianCalendar.setTime(date);
                PersianDateFormat formatter = new PersianDateFormat(pattern);

               /* result       = PersianDateFormat.format(persianCalendar, pattern, numberFormat);
                iresult2     = persianCalendar.getHijriDate();
                perMonth     = persianCalendar.getMonth();
*/
                pRresult   = formatter.format(persianCalendar, numberFormat);
                formatter.setCalendarType(PersianDateFormat.CalendarType.GREGORIAN);
                gRresult   = formatter.format(persianCalendar, numberFormat);
                formatter.setCalendarType(PersianDateFormat.CalendarType.HEJRI);
                iRresult   = formatter.format(persianCalendar, numberFormat);
            } else {
                //FastPersianCalendar
                FastPersianCalendar fastCalendar = new FastPersianCalendar();
                fastCalendar.setTime(date);
                FastPersianDateFormat formatter = new FastPersianDateFormat(pattern);
                //date     = new Date(fastCalendar.getTimeInMillis());
                //iresult  = fastCalendar.getHijriDate().toIntArray();
                pRresult   = formatter.format(fastCalendar, fastNumberFormat);
                formatter.setCalendarType(FastPersianDateFormat.CalendarType.GREGORIAN);
                gRresult   = formatter.format(fastCalendar, fastNumberFormat);
                formatter.setCalendarType(FastPersianDateFormat.CalendarType.HEJRI);
                iRresult   = formatter.format(fastCalendar, fastNumberFormat);
                /*result   = FastPersianDateFormat.format(fastCalendar, pattern, fastNumberFormat);
                iresult  = gregorianToHijri(fastCalendar.gCal).toIntArray();
                perMonth = fastCalendar.getPersianMonth();*/
            }
            tvPResult.setText(format("Persian Date: %s", pRresult));
            tvGResult.setText(format("Gregorian Date: %s", gRresult));
            tvIResult.setText(format("Hijri Date: %s", iRresult));

            /*tvPResult.setText(format("Persian Date: %s", result));
            tvGResult.setText(format("Gregorian Date: %s", gregorianDate));
            tvPResult.setText(format("Persian Date: %s %s", result, getMonthName(perMonth)));
            if (spnLibrary.getSelectedItemPosition() == 0) {
                tvIResult.setText(format("Hijri Date: %s %s", iresult2.toString(), getHijriMonthName(iresult2.month)));
            } else {
                String ir = format("%04d/%02d/%02d", iresult[0], iresult[1], iresult[2]);
                tvIResult.setText(format("Hijri Date: %s %s", ir, getHijriMonthName(iresult[1])));
            }*/
            hideKeyboard(tvPResult);
        } catch (ParseException e) {
            e.printStackTrace();
            showToast("Error parsing Gregorian date: " + e.getMessage());
            tvPResult.setText(format("Error: %s", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error: " + e.getMessage());
            tvPResult.setText(format("Error: %s", e.getMessage()));
        }
    }


    private void convertPersianToGregorian() {
        String persianDate = edtPersianDate.getText().toString().trim();
        if (persianDate.isEmpty()) {
            showToast("Please enter a Persian date");
            return;
        }
        int perMonth = 0;
        int gMonth   = 0;

        try {

            SimpleDateFormat sdf      = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            String           pattern  = "yyyy/MM/dd";
            String           pattern1 = edtCustomPattern.getText().toString().trim();
            String           pRresult;
            String           gRresult;
            String           iRresult = "";

            fastNumberFormat = spnNumberFormat.getSelectedItemPosition() == 0 ?
                    FastPersianDateFormat.PersianDateNumberCharacter.ENGLISH :
                    FastPersianDateFormat.PersianDateNumberCharacter.FARSI;

            numberFormat = spnNumberFormat.getSelectedItemPosition() == 0 ?
                    PersianDateFormat.PersianDateNumberCharacter.ENGLISH :
                    PersianDateFormat.PersianDateNumberCharacter.FARSI;

            Date             date;
            int[]            iresult  = null;
            YMD              iresult2 = null;
            String result="";
            if (spnLibrary.getSelectedItemPosition() == 0) {
                //PersianCalendar
                PersianCalendar persianCalendar = myPersianDateFormat.parse(persianDate, pattern);
                PersianDateFormat formatter = new PersianDateFormat(pattern1);

                //date     = new Date(persianCalendar.getTimeInMillis());
                //iresult2 = persianCalendar.getHijriDate();
                //perMonth = persianCalendar.getMonth();
                //result = sdf.format(date);
                pRresult   = formatter.format(persianCalendar, numberFormat);
                formatter.setCalendarType(PersianDateFormat.CalendarType.GREGORIAN);
                gRresult   = formatter.format(persianCalendar, numberFormat);
                formatter.setCalendarType(PersianDateFormat.CalendarType.HEJRI);
                iRresult   = formatter.format(persianCalendar, numberFormat);
                //gMonth   = persianCalendar.getGrgMonth();
                //gRresult = format("Gregorian Date: %s %s", result, PersianCalendar.getGrgMonthName(gMonth));
            } else {
                //FastPersianCalendar
                FastPersianCalendar fastCalendar = fastPersianDateFormat.parse(persianDate, pattern);
                FastPersianDateFormat formatter = new FastPersianDateFormat(pattern1);
                //date     = new Date(fastCalendar.getTimeInMillis());
                //iresult  = fastCalendar.getHijriDate().toIntArray();
                pRresult   = formatter.format(fastCalendar, fastNumberFormat);
                formatter.setCalendarType(FastPersianDateFormat.CalendarType.GREGORIAN);
                gRresult   = formatter.format(fastCalendar, fastNumberFormat);
                formatter.setCalendarType(FastPersianDateFormat.CalendarType.HEJRI);
                iRresult   = formatter.format(fastCalendar, fastNumberFormat);
                //perMonth = fastCalendar.getPersianMonth();
                //gMonth   = fastCalendar.getGrgMonth();
            }



            //tvGResult.setText("Gregorian Date: " + result);
            //tvPResult.setText("Persian Date: " + persianDate);

            tvPResult.setText(format("Persian Date: %s", pRresult));
            //tvGResult.setText(format("Gregorian Date: %s", result));
            tvGResult.setText(format("Gregorian Date: %s", gRresult));
            tvIResult.setText(format("Hijri Date: %s", iRresult));
           // tvGResult.setText(format("Gregorian Date: %s %s", result, PersianCalendar.getGrgMonthName(gMonth)));
            //tvPResult.setText(format("Persian Date: %s %s", persianDate, getMonthName(perMonth)));
            /*if (spnLibrary.getSelectedItemPosition() == 0) {
                tvIResult.setText(format("Hijri Date: %s %s", iresult2.toString(), getHijriMonthName(iresult2.month)));
            } else {
                //String ir = format("%04d/%02d/%02d", iresult[0], iresult[1], iresult[2]);
                //tvIResult.setText(format("Hijri Date: %s %s", ir, getHijriMonthName(iresult[1])));
                tvIResult.setText(format("Hijri Date: %s", iRresult));
            }*/
            //showToast("Gregorian Date: " + result);
            hideKeyboard(tvPResult);
        } catch (ParseException e) {
            e.printStackTrace();
            showToast("Error parsing Persian date: " + e.getMessage());
            tvPResult.setText("Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error: " + e.getMessage());
            tvPResult.setText("Error: " + e.getMessage());
        }
    }

    public static void hideKeyboard(@NonNull View view) {
        WindowInsetsControllerCompat windowController = ViewCompat.getWindowInsetsController(view);
        if (windowController != null) {
            windowController.hide(WindowInsetsCompat.Type.ime());
            return;
        }

        InputMethodManager imm = ContextCompat.getSystemService(view.getContext(), InputMethodManager.class);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }
    //Define the TextWatcher as a class member

    private TextWatcher gregorianDateTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().trim().length() >= 10) {
                //Only perform conversion if the date was entered manually
                performConversion(s.toString().trim());
            }
        }
    };

    private void parseDateString() {
        String dateString = edtPersianDate.getText().toString().trim();
        if (dateString.isEmpty()) {
            showToast("Please enter a date string");
            return;
        }
        dateString = convertToEnglishNumbers(dateString);

        try {
            //For parsing simple dates like "1402/10/25", use appropriate patterns
            String pattern;

            //Determine which pattern to use based on the date string format
            if (dateString.contains("/")) {
                String[] parts = dateString.split("/");
                if (parts.length == 3) {
                    if (parts[0].length() == 4) {
                        pattern = "yyyy/MM/dd";
                    } else {
                        pattern = "yy/MM/dd";
                    }
                } else {
                    pattern = "yyyy/MM/dd"; //default
                }
            } else if (dateString.contains("-")) {
                pattern = "yyyy-MM-dd";
            } else if (dateString.contains(" ")) {
                //Contains spaces, might have time
                if (dateString.contains(":")) {
                    if (dateString.split(":").length == 3) {
                        pattern = "yyyy/MM/dd HH:mm:ss";
                    } else {
                        pattern = "yyyy/MM/dd HH:mm";
                    }
                } else {
                    pattern = "dddd, d MMMM yyyy";
                }
            } else {
                //Try to auto-detect
                pattern = "yyyy/MM/dd";
            }

            if (spnLibrary.getSelectedItemPosition() == 0) {
                //PersianCalendar
                PersianCalendar calendar  = myPersianDateFormat.parse(dateString, pattern);
                String          formatted = PersianDateFormat.format(calendar, "dddd, d MMMM yyyy HH:mm:ss");
                tvParsedDate.setText("Parsed: " + formatted);
                showToast("Parsed: " + formatted);
                tvUpdatedDate.setText(formatted);

            } else {
                //FastPersianCalendar
                FastPersianCalendar calendar  = fastPersianDateFormat.parse(dateString, pattern);
                String              formatted = FastPersianDateFormat.format(calendar, "dddd, d MMMM yyyy HH:mm:ss");
                tvParsedDate.setText("Parsed: " + formatted);
                showToast("Parsed: " + formatted);
                tvUpdatedDate.setText(formatted);
            }

        } catch (ParseException e) {
            showToast("Error parsing date: " + e.getMessage());
            tvParsedDate.setText("Error: " + e.getMessage());
        } catch (Exception e) {
            showToast("Error: " + e.getMessage());
            tvParsedDate.setText("Error: " + e.getMessage());
        }
    }

    PersianDateFormat.PersianDateNumberCharacter     numberFormat;
    FastPersianDateFormat.PersianDateNumberCharacter fastNumberFormat;

    private void formatCurrentDate() {
        try {
            String pattern = edtCustomPattern.getText().toString().trim();
            if (pattern.isEmpty()) {
                pattern = "yyyy/MM/dd";
            }


            numberFormat = spnNumberFormat.getSelectedItemPosition() == 0 ?
                    PersianDateFormat.PersianDateNumberCharacter.ENGLISH :
                    PersianDateFormat.PersianDateNumberCharacter.FARSI;

            String result;
            if (spnLibrary.getSelectedItemPosition() == 0) {
                //PersianCalendar
                PersianCalendar now = new PersianCalendar();
                result = PersianDateFormat.format(now, pattern, numberFormat);
            } else {
                //FastPersianCalendar
                FastPersianCalendar now = new FastPersianCalendar();

                fastNumberFormat = spnNumberFormat.getSelectedItemPosition() == 0 ?
                        FastPersianDateFormat.PersianDateNumberCharacter.ENGLISH :
                        FastPersianDateFormat.PersianDateNumberCharacter.FARSI;
                result           = FastPersianDateFormat.format(now, pattern, fastNumberFormat);
            }

            tvCurrentDate.setText("Formatted: " + result);
            showToast("Formatted: " + result);

        } catch (Exception e) {
            showToast("Error formatting date: " + e.getMessage());
            tvCurrentDate.setText("Error: " + e.getMessage());
        }
    }

    private void updateCurrentDate() {
        try {
            if (spnLibrary.getSelectedItemPosition() == 0) {
                //PersianCalendar
                PersianCalendar now       = new PersianCalendar();
                String          formatted = PersianDateFormat.format(now, "dddd, d MMMM yyyy HH:mm:ss");
                tvCurrentDate.setText("Current: " + formatted);
                showToast("Current: " + formatted);
            } else {
                //FastPersianCalendar
                FastPersianCalendar now       = new FastPersianCalendar();
                String              formatted = FastPersianDateFormat.format(now, "dddd, d MMMM yyyy HH:mm:ss");
                tvCurrentDate.setText("Current: " + formatted);
                showToast("Current: " + formatted);
            }
        } catch (Exception e) {
            tvCurrentDate.setText("Error: " + e.getMessage());
        }
    }

    private void runPerformanceTest() {
        new Thread(() -> {
            int iterations = 10000;

            //Test PersianCalendar
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                PersianCalendar cal = new PersianCalendar();
                PersianDateFormat.format(cal, "yyyy/MM/dd HH:mm:ss");
            }
            long myLibTime = System.currentTimeMillis() - startTime;

            //Test FastPersianCalendar
            startTime = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                FastPersianCalendar cal = new FastPersianCalendar();
                FastPersianDateFormat.format(cal, "yyyy/MM/dd HH:mm:ss");
            }
            long fastLibTime = System.currentTimeMillis() - startTime;

            runOnUiThread(() -> {
                String comparisonText;
                double percentage;

                if (fastLibTime < myLibTime) {
                    //Fast is faster
                    percentage     = ((double) (myLibTime - fastLibTime) / myLibTime) * 100;
                    comparisonText = format(Locale.US, "Fast library is %.1f%% faster", percentage);
                } else {
                    //Fast is slower
                    percentage     = ((double) (fastLibTime - myLibTime) / myLibTime) * 100;
                    comparisonText = format(Locale.US, "Fast library is %.1f%% slower", percentage);
                }

                String result = format(Locale.US,
                                       "Performance Test (%d iterations):\n" +
                                       "PersianCalendar: %d ms\n" +
                                       "FastPersianCalendar: %d ms\n" +
                                       "%s",
                                       iterations, myLibTime, fastLibTime,
                                       comparisonText);

                tvConvertedDate.setText(result);
                showToast("Performance test completed");
            });
        }).start();
    }

    private void runPerformanceTestold() {
        new Thread(() -> {
            int iterations = 10000;

            //Test PersianCalendar
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                PersianCalendar cal = new PersianCalendar();
                PersianDateFormat.format(cal, "yyyy/MM/dd HH:mm:ss");
            }
            long myLibTime = System.currentTimeMillis() - startTime;

            //Test FastPersianCalendar
            startTime = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                FastPersianCalendar cal = new FastPersianCalendar();
                FastPersianDateFormat.format(cal, "yyyy/MM/dd HH:mm:ss");
            }
            long fastLibTime = System.currentTimeMillis() - startTime;

            runOnUiThread(() -> {
                String result = format(Locale.US,
                                       "Performance Test (%d iterations):\n" +
                                       "PersianCalendar: %d ms\n" +
                                       "FastPersianCalendar: %d ms\n" +
                                       "Fast library is %.1f%% faster",
                                       iterations, myLibTime, fastLibTime,
                                       ((double) (myLibTime - fastLibTime) / myLibTime) * 100);

                tvConvertedDate.setText(result);
                showToast("Performance test completed");
            });
        }).start();
    }

    private void testLeapYears() {
        StringBuilder result = new StringBuilder("Leap Year Test:\n");
        int[] testYears = {
                1374, 1376, 1380, 1384, 1388, 1392, 1396, 1400, 1404, 1407, 1411,
                1201, 1205, 1210, 1214, 1218, 1222, 1226, 1230, 1234, 1238, 1243,
                1247, 1251, 1255, 1259, 1263, 1267, 1271, 1276, 1280, 1284, 1288,
                1292, 1296, 1300, 1304, 1309, 1313, 1317, 1321, 1325, 1329, 1333,
                1337, 1342, 1346, 1350, 1354, 1358, 1362, 1366, 1370, 1375, 1379,
                1383, 1387, 1391, 1395, 1399, 1403, 1408, 1412, 1416, 1420, 1424,
                1428, 1432, 1436, 1441, 1445, 1449, 1453, 1457, 1461, 1465, 1469,
                1474, 1478, 1482, 1486, 1490, 1494, 1498, 1502, 1507, 1511, 1515,
                1519, 1523, 1527, 1531, 1535, 1540, 1544, 1548, 1552, 1556, 1560,
                1564, 1568, 1573, 1577, 1581, 1585, 1589, 1593, 1597};

        //int[] testYears = {1400, 1401, 1402, 1403, 1404, 1399, 1405};

        for (int year : testYears) {
            boolean isLeap;

            isLeap = FastPersianCalendar.isLeapYear(year);
            result.append(format(Locale.US, "%d: %s\n", year, isLeap ? "Leap" : "Common"));
        }

        tvConvertedDate.setText(result.toString());
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(
                PersianCalendarActivity.this,
                message,
                Toast.LENGTH_SHORT
        ).show());
    }

    //Test Case Model
    public static class TestCase {
        private String title;
        private String description;
        private String sample;

        public TestCase(String title, String description, String sample) {
            this.title       = title;
            this.description = description;
            this.sample      = sample;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getSample() {
            return sample;
        }
    }
}