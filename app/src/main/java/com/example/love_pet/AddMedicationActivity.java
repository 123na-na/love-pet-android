package com.example.love_pet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddMedicationActivity extends AppCompatActivity {

    public static final String EXTRA_MED_NAME = "med_name";
    public static final String EXTRA_PET_NAME = "pet_name";
    public static final String EXTRA_DOSAGE = "dosage";
    public static final String EXTRA_FREQUENCY = "frequency";
    public static final String EXTRA_DAYS = "days";

    private EditText etMedName;
    private EditText etPetName;
    private EditText etDosage;
    private EditText etDays;
    private Spinner spinnerFrequency;
    private LinearLayout layoutTimes;
    private TextView tvTimeCount;
    private Switch switchAlarm;
    private Switch switchVibrate;

    private List<String> timeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        etMedName = findViewById(R.id.et_med_name);
        etPetName = findViewById(R.id.et_pet_name);
        etDosage = findViewById(R.id.et_dosage);
        etDays = findViewById(R.id.et_days);
        spinnerFrequency = findViewById(R.id.spinner_frequency);
        layoutTimes = findViewById(R.id.layout_times);
        tvTimeCount = findViewById(R.id.tv_time_count);
        switchAlarm = findViewById(R.id.switch_alarm);
        switchVibrate = findViewById(R.id.switch_vibrate);

        findViewById(R.id.iv_back_add_med).setOnClickListener(v -> finish());

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_MED_NAME)) {
            etMedName.setText(intent.getStringExtra(EXTRA_MED_NAME));
            etPetName.setText(intent.getStringExtra(EXTRA_PET_NAME));
            etDosage.setText(intent.getStringExtra(EXTRA_DOSAGE));
            String freq = intent.getStringExtra(EXTRA_FREQUENCY);
            etDays.setText(intent.getStringExtra(EXTRA_DAYS));
        }

        initFrequencySpinner();
        initTimeList();
        setupTimeClickListeners();

        findViewById(R.id.btn_add_time).setOnClickListener(v -> showTimePicker());

        findViewById(R.id.btn_save_med).setOnClickListener(v -> saveMedication());
    }

    private void initFrequencySpinner() {
        String[] frequencies = {"每日1次", "每日2次", "每日3次", "每日4次", "隔日1次", "按需服用"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, frequencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);
        spinnerFrequency.setSelection(2);
    }

    private void initTimeList() {
        timeList.clear();
        timeList.add("08:00");
        timeList.add("12:00");
        timeList.add("18:00");
        updateTimeCount();
    }

    private void setupTimeClickListeners() {
        for (int i = 0; i < layoutTimes.getChildCount(); i++) {
            final int index = i;
            View child = layoutTimes.getChildAt(i);
            child.setOnClickListener(v -> showTimePickerForIndex(index));
            ImageView deleteBtn = child.findViewById(R.id.iv_delete);
            if (deleteBtn != null) {
                deleteBtn.setOnClickListener(v -> removeTime(index));
            }
        }
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, m) -> {
            String time = String.format("%02d:%02d", h, m);
            addTime(time);
        }, hour, minute, true);
        dialog.show();
    }

    private void showTimePickerForIndex(final int index) {
        if (index >= timeList.size()) return;
        String currentTime = timeList.get(index);
        String[] parts = currentTime.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        TimePickerDialog dialog = new TimePickerDialog(this, (view, h, m) -> {
            String time = String.format("%02d:%02d", h, m);
            updateTime(index, time);
        }, hour, minute, true);
        dialog.show();
    }

    private void addTime(String time) {
        timeList.add(time);
        refreshTimeViews();
    }

    private void updateTime(int index, String time) {
        if (index < timeList.size()) {
            timeList.set(index, time);
            refreshTimeViews();
        }
    }

    private void removeTime(int index) {
        if (timeList.size() <= 1) {
            Toast.makeText(this, "至少保留一个提醒时间", Toast.LENGTH_SHORT).show();
            return;
        }
        timeList.remove(index);
        refreshTimeViews();
    }

    private void refreshTimeViews() {
        layoutTimes.removeAllViews();
        for (int i = 0; i < timeList.size(); i++) {
            final int index = i;
            LinearLayout timeRow = new LinearLayout(this);
            timeRow.setOrientation(LinearLayout.HORIZONTAL);
            timeRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
            timeRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp2px(40)));

            TextView timeTv = new TextView(this);
            timeTv.setText(timeList.get(i));
            timeTv.setTextSize(14);
            timeTv.setTextColor(android.graphics.Color.parseColor("#333333"));
            LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            timeRow.addView(timeTv, timeParams);

            ImageView deleteIv = new ImageView(this);
            deleteIv.setImageResource(R.drawable.ic_delete);
            LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(dp2px(20), dp2px(20));
            timeRow.addView(deleteIv, deleteParams);

            timeRow.setOnClickListener(v -> showTimePickerForIndex(index));
            deleteIv.setOnClickListener(v -> removeTime(index));

            layoutTimes.addView(timeRow);
        }
        updateTimeCount();
    }

    private void updateTimeCount() {
        tvTimeCount.setText("每天" + timeList.size() + "次");
    }

    private void saveMedication() {
        String medName = etMedName.getText().toString().trim();
        String petName = etPetName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String frequency = spinnerFrequency.getSelectedItem().toString();
        String daysStr = etDays.getText().toString().trim();

        if (medName.isEmpty()) {
            Toast.makeText(this, "请输入药品名称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (petName.isEmpty()) {
            Toast.makeText(this, "请输入宠物名称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dosage.isEmpty()) {
            Toast.makeText(this, "请输入用量", Toast.LENGTH_SHORT).show();
            return;
        }
        if (daysStr.isEmpty()) {
            Toast.makeText(this, "请输入服用天数", Toast.LENGTH_SHORT).show();
            return;
        }

        int days = Integer.parseInt(daysStr);

        if (switchAlarm.isChecked()) {
            setAlarms(medName, petName, days);
        }

        Toast.makeText(this, "用药提醒已保存", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void setAlarms(String medName, String petName, int days) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        for (int day = 0; day < days; day++) {
            for (int i = 0; i < timeList.size(); i++) {
                String time = timeList.get(i);
                String[] parts = time.split(":");
                int hour = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, 0);
                cal.add(Calendar.DAY_OF_YEAR, day);

                if (cal.before(Calendar.getInstance())) {
                    continue;
                }

                int requestCode = (day * 100) + i + (int) (Math.random() * 1000);

                Intent intent = new Intent(this, MedicationAlarmReceiver.class);
                intent.putExtra("med_name", medName);
                intent.putExtra("pet_name", petName);
                intent.putExtra("request_code", requestCode);

                PendingIntent pendingIntent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
                } else {
                    pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, 0);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
                }
            }
        }
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
