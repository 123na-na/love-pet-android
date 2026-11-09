package com.example.love_pet;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class AppointmentBookActivity extends AppCompatActivity {

    public static final String EXTRA_HOSPITAL_NAME = "hospital_name";
    public static final String EXTRA_HOSPITAL_LEVEL = "hospital_level";
    public static final String EXTRA_HOSPITAL_ADDRESS = "hospital_address";

    private LinearLayout layoutDepartments;
    private LinearLayout layoutDates;
    private LinearLayout layoutMorning;
    private LinearLayout layoutAfternoon;
    private LinearLayout layoutEvening;

    private int selectedDept = 0;
    private int selectedDate = 0;
    private String selectedTime = "";

    private String[] departments = {"内科", "外科", "皮肤科", "牙科", "眼科"};
    private String[] morningTimes = {"08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30"};
    private String[] afternoonTimes = {"14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30"};
    private String[] eveningTimes = {"18:00", "18:30", "19:00", "19:30", "20:00"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_book);

        String hospitalName = getIntent().getStringExtra(EXTRA_HOSPITAL_NAME);
        String hospitalLevel = getIntent().getStringExtra(EXTRA_HOSPITAL_LEVEL);
        String hospitalAddress = getIntent().getStringExtra(EXTRA_HOSPITAL_ADDRESS);

        if (hospitalName != null) {
            ((TextView) findViewById(R.id.tv_hospital_name)).setText(hospitalName);
        }
        if (hospitalLevel != null) {
            ((TextView) findViewById(R.id.tv_hospital_level)).setText(hospitalLevel);
        }
        if (hospitalAddress != null) {
            ((TextView) findViewById(R.id.tv_hospital_address)).setText("地址：" + hospitalAddress);
        }

        findViewById(R.id.iv_back_appointment).setOnClickListener(v -> finish());

        layoutDepartments = findViewById(R.id.layout_departments);
        layoutDates = findViewById(R.id.layout_dates);
        layoutMorning = findViewById(R.id.layout_morning);
        layoutAfternoon = findViewById(R.id.layout_afternoon);
        layoutEvening = findViewById(R.id.layout_evening);

        initDepartments();
        initDates();
        initTimeSlots();

        findViewById(R.id.btn_confirm_appointment).setOnClickListener(v -> {
            if (selectedTime.isEmpty()) {
                Toast.makeText(this, "请选择就诊时间", Toast.LENGTH_SHORT).show();
                return;
            }
            showSuccessDialog();
        });
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_appointment_success, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        String hospitalName = getIntent().getStringExtra(EXTRA_HOSPITAL_NAME);
        String dept = departments[selectedDept];
        String date = getDateString(selectedDate);
        String time = selectedTime;

        Random random = new Random();
        int floor = random.nextInt(5) + 1;
        int roomNum = random.nextInt(20) + 1;
        String room = floor + "楼 " + roomNum + "号诊室";

        ((TextView) dialogView.findViewById(R.id.tv_success_hospital)).setText(hospitalName != null ? hospitalName : "萌宠宠物医院");
        ((TextView) dialogView.findViewById(R.id.tv_success_dept)).setText(dept);
        ((TextView) dialogView.findViewById(R.id.tv_success_time)).setText(date + " " + time);
        ((TextView) dialogView.findViewById(R.id.tv_success_room)).setText(room);
        ((TextView) dialogView.findViewById(R.id.tv_success_price)).setText("¥50.00");

        dialogView.findViewById(R.id.btn_success_confirm).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void initDepartments() {
        for (int i = 0; i < departments.length; i++) {
            final int index = i;
            TextView tv = new TextView(this);
            tv.setText(departments[i]);
            tv.setTextSize(13);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setPadding(dp2px(16), dp2px(6), dp2px(16), dp2px(6));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) params.leftMargin = dp2px(8);
            tv.setLayoutParams(params);

            updateDeptTextStyle(tv, i == selectedDept);

            tv.setOnClickListener(v -> {
                selectedDept = index;
                updateDeptSelection();
            });

            layoutDepartments.addView(tv);
        }
    }

    private void updateDeptSelection() {
        for (int i = 0; i < layoutDepartments.getChildCount(); i++) {
            TextView tv = (TextView) layoutDepartments.getChildAt(i);
            updateDeptTextStyle(tv, i == selectedDept);
        }
    }

    private void updateDeptTextStyle(TextView tv, boolean selected) {
        if (selected) {
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundResource(R.drawable.bg_button_orange);
        } else {
            tv.setTextColor(Color.parseColor("#666666"));
            tv.setBackgroundResource(R.drawable.bg_gray_round);
        }
    }

    private void initDates() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
        SimpleDateFormat weekFormat = new SimpleDateFormat("EEE");

        for (int i = 0; i < 7; i++) {
            final int index = i;
            Calendar dateCal = (Calendar) cal.clone();
            dateCal.add(Calendar.DAY_OF_YEAR, i);

            LinearLayout dateItem = new LinearLayout(this);
            dateItem.setOrientation(LinearLayout.VERTICAL);
            dateItem.setGravity(android.view.Gravity.CENTER);
            dateItem.setPadding(dp2px(12), dp2px(8), dp2px(12), dp2px(8));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp2px(56), LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) params.leftMargin = dp2px(8);
            dateItem.setLayoutParams(params);

            TextView weekTv = new TextView(this);
            weekTv.setText(i == 0 ? "今天" : (i == 1 ? "明天" : weekFormat.format(dateCal.getTime())));
            weekTv.setTextSize(12);
            weekTv.setGravity(android.view.Gravity.CENTER);
            dateItem.addView(weekTv);

            TextView dayTv = new TextView(this);
            dayTv.setText(dayFormat.format(dateCal.getTime()));
            dayTv.setTextSize(18);
            dayTv.setTypeface(null, android.graphics.Typeface.BOLD);
            dayTv.setGravity(android.view.Gravity.CENTER);
            dayTv.setPadding(0, dp2px(4), 0, 0);
            dateItem.addView(dayTv);

            updateDateItemStyle(dateItem, i == selectedDate);

            dateItem.setOnClickListener(v -> {
                selectedDate = index;
                selectedTime = "";
                updateDateSelection();
                updateTimeSlots();
            });

            layoutDates.addView(dateItem);
        }
    }

    private void updateDateSelection() {
        for (int i = 0; i < layoutDates.getChildCount(); i++) {
            LinearLayout item = (LinearLayout) layoutDates.getChildAt(i);
            updateDateItemStyle(item, i == selectedDate);
        }
    }

    private void updateDateItemStyle(LinearLayout item, boolean selected) {
        TextView weekTv = (TextView) item.getChildAt(0);
        TextView dayTv = (TextView) item.getChildAt(1);
        if (selected) {
            item.setBackgroundResource(R.drawable.bg_button_orange);
            weekTv.setTextColor(Color.WHITE);
            dayTv.setTextColor(Color.WHITE);
        } else {
            item.setBackgroundResource(R.drawable.bg_gray_round);
            weekTv.setTextColor(Color.parseColor("#999999"));
            dayTv.setTextColor(Color.parseColor("#333333"));
        }
    }

    private String getDateString(int offset) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, offset);
        SimpleDateFormat format = new SimpleDateFormat("MM月dd日");
        return format.format(cal.getTime());
    }

    private void initTimeSlots() {
        updateTimeSlots();
    }

    private void updateTimeSlots() {
        layoutMorning.removeAllViews();
        layoutAfternoon.removeAllViews();
        layoutEvening.removeAllViews();

        for (int i = 0; i < morningTimes.length; i++) {
            final String time = morningTimes[i];
            addTimeSlot(layoutMorning, time, i);
        }

        for (int i = 0; i < afternoonTimes.length; i++) {
            final String time = afternoonTimes[i];
            addTimeSlot(layoutAfternoon, time, i);
        }

        for (int i = 0; i < eveningTimes.length; i++) {
            final String time = eveningTimes[i];
            addTimeSlot(layoutEvening, time, i);
        }
    }

    private void addTimeSlot(LinearLayout parent, String time, int index) {
        TextView tv = new TextView(this);
        tv.setText(time);
        tv.setTextSize(13);
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setPadding(dp2px(12), dp2px(8), dp2px(12), dp2px(8));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dp2px(70), LinearLayout.LayoutParams.WRAP_CONTENT);
        if (index > 0) params.leftMargin = dp2px(8);
        tv.setLayoutParams(params);

        boolean isSelected = time.equals(selectedTime);
        updateTimeSlotStyle(tv, isSelected);

        tv.setOnClickListener(v -> {
            selectedTime = time;
            updateAllTimeSlots();
        });

        parent.addView(tv);
    }

    private void updateAllTimeSlots() {
        updateTimeSlotGroup(layoutMorning);
        updateTimeSlotGroup(layoutAfternoon);
        updateTimeSlotGroup(layoutEvening);
    }

    private void updateTimeSlotGroup(LinearLayout group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            TextView tv = (TextView) group.getChildAt(i);
            boolean isSelected = tv.getText().toString().equals(selectedTime);
            updateTimeSlotStyle(tv, isSelected);
        }
    }

    private void updateTimeSlotStyle(TextView tv, boolean selected) {
        if (selected) {
            tv.setTextColor(Color.WHITE);
            tv.setBackgroundResource(R.drawable.bg_button_orange);
        } else {
            tv.setTextColor(Color.parseColor("#666666"));
            tv.setBackgroundResource(R.drawable.bg_gray_round);
        }
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
