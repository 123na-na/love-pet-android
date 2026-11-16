package com.example.love_pet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PetDoctorActivity extends AppCompatActivity {

    private LinearLayout layoutConsultList;
    private LinearLayout layoutEmptyConsult;
    private LinearLayout layoutDoctorList;

    public static class ConsultRecord {
        public String doctorName;
        public String department;
        public String symptom;
        public String time;
        public String status;
        public int avatarRes;

        public ConsultRecord(String doctorName, String department, String symptom, String time, String status, int avatarRes) {
            this.doctorName = doctorName;
            this.department = department;
            this.symptom = symptom;
            this.time = time;
            this.status = status;
            this.avatarRes = avatarRes;
        }
    }

    public static class Doctor {
        public String name;
        public String title;
        public String department;
        public String hospital;
        public float rating;
        public int consultCount;
        public int avatarRes;

        public Doctor(String name, String title, String department, String hospital, float rating, int consultCount, int avatarRes) {
            this.name = name;
            this.title = title;
            this.department = department;
            this.hospital = hospital;
            this.rating = rating;
            this.consultCount = consultCount;
            this.avatarRes = avatarRes;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_doctor);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutConsultList = findViewById(R.id.layout_consult_list);
        layoutEmptyConsult = findViewById(R.id.layout_empty_consult);
        layoutDoctorList = findViewById(R.id.layout_doctor_list);

        findViewById(R.id.btn_quick_consult).setOnClickListener(v -> {
            startActivity(new Intent(this, VideoConsultActivity.class));
        });

        findViewById(R.id.btn_ai_check).setOnClickListener(v -> {
            startActivity(new Intent(this, AiCheckActivity.class));
        });

        findViewById(R.id.btn_text_consult).setOnClickListener(v -> {
            Intent intent = new Intent(this, ConsultChatActivity.class);
            intent.putExtra("doctor_name", "在线医生");
            intent.putExtra("department", "综合科");
            intent.putExtra("doctor_avatar", R.drawable.doctor);
            startActivity(intent);
        });

        findViewById(R.id.btn_video_consult).setOnClickListener(v -> {
            startActivity(new Intent(this, VideoConsultActivity.class));
        });

        findViewById(R.id.btn_emergency).setOnClickListener(v -> {
            showEmergencyDialog();
        });

        findViewById(R.id.btn_department).setOnClickListener(v -> {
            startActivity(new Intent(this, DepartmentActivity.class));
        });

        findViewById(R.id.btn_pet_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, AllPetsActivity.class));
        });

        findViewById(R.id.btn_prescription).setOnClickListener(v -> {
            startActivity(new Intent(this, PrescriptionListActivity.class));
        });

        findViewById(R.id.btn_report).setOnClickListener(v -> {
            startActivity(new Intent(this, ReportAnalysisActivity.class));
        });

        findViewById(R.id.btn_nursing).setOnClickListener(v -> {
            startActivity(new Intent(this, NursingPlanActivity.class));
        });

        findViewById(R.id.btn_medication).setOnClickListener(v -> {
            startActivity(new Intent(this, MedicationReminderActivity.class));
        });

        findViewById(R.id.btn_vaccine).setOnClickListener(v -> {
            startActivity(new Intent(this, VaccineRecordActivity.class));
        });

        findViewById(R.id.btn_hospital).setOnClickListener(v -> {
            startActivity(new Intent(this, HospitalAppointmentActivity.class));
        });

        renderConsultRecords();
        renderDoctors();
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    private List<ConsultRecord> getConsultRecords() {
        List<ConsultRecord> list = new ArrayList<>();
        list.add(new ConsultRecord("李医生", "内科", "猫咪呕吐拉稀", "2024-01-15 14:30", "已完成", R.drawable.doctor4));
        return list;
    }

    private void renderConsultRecords() {
        List<ConsultRecord> records = getConsultRecords();
        if (records.isEmpty()) {
            layoutEmptyConsult.setVisibility(View.VISIBLE);
            return;
        }
        layoutEmptyConsult.setVisibility(View.GONE);

        for (ConsultRecord record : records) {
            View card = createConsultCard(record);
            layoutConsultList.addView(card);
        }
    }

    private View createConsultCard(final ConsultRecord record) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp2px(16), dp2px(14), dp2px(16), dp2px(14));
        card.setBackgroundResource(R.drawable.bg_input);
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConsultChatActivity.class);
            intent.putExtra("doctor_name", record.doctorName);
            intent.putExtra("department", record.department);
            intent.putExtra("doctor_avatar", record.avatarRes);
            startActivity(intent);
        });

        ImageView ivAvatar = new ImageView(this);
        ivAvatar.setImageResource(record.avatarRes);
        ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivAvatar.setClipToOutline(true);
        ivAvatar.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp2px(44), dp2px(44));
        card.addView(ivAvatar, avatarParams);

        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.leftMargin = dp2px(12);
        card.addView(infoLayout, infoParams);

        TextView nameTv = new TextView(this);
        nameTv.setText(record.doctorName + " · " + record.department);
        nameTv.setTextSize(15);
        nameTv.setTextColor(Color.parseColor("#333333"));
        nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
        infoLayout.addView(nameTv);

        TextView symptomTv = new TextView(this);
        symptomTv.setText(record.symptom);
        symptomTv.setTextSize(13);
        symptomTv.setTextColor(Color.parseColor("#666666"));
        symptomTv.setPadding(0, dp2px(4), 0, 0);
        infoLayout.addView(symptomTv);

        TextView timeTv = new TextView(this);
        timeTv.setText(record.time);
        timeTv.setTextSize(12);
        timeTv.setTextColor(Color.parseColor("#999999"));
        timeTv.setPadding(0, dp2px(4), 0, 0);
        infoLayout.addView(timeTv);

        TextView statusTv = new TextView(this);
        statusTv.setText(record.status);
        statusTv.setTextSize(12);
        statusTv.setTextColor(Color.parseColor("#FF9500"));
        statusTv.setPadding(dp2px(8), dp2px(2), dp2px(8), dp2px(2));
        GradientDrawable statusBg = new GradientDrawable();
        statusBg.setColor(Color.parseColor("#FFF5E6"));
        statusBg.setCornerRadius(dp2px(4));
        statusTv.setBackground(statusBg);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        statusParams.topMargin = dp2px(8);
        card.addView(statusTv, statusParams);

        return card;
    }

    private List<Doctor> getDoctors() {
        List<Doctor> list = new ArrayList<>();
        list.add(new Doctor("王晓明", "主治医师", "内科", "萌宠宠物医院", 4.9f, 1286, R.drawable.doctor));
        list.add(new Doctor("李雪梅", "副主任医师", "皮肤科", "爱宠医疗中心", 4.8f, 956, R.drawable.doctor2));
        list.add(new Doctor("张建国", "主任医师", "外科", "萌宠宠物医院", 4.9f, 2103, R.drawable.doctor3));
        return list;
    }

    private void renderDoctors() {
        List<Doctor> doctors = getDoctors();
        for (int i = 0; i < doctors.size(); i++) {
            View card = createDoctorCard(doctors.get(i));
            layoutDoctorList.addView(card);
            if (i < doctors.size() - 1) {
                View space = new View(this);
                LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp2px(10));
                layoutDoctorList.addView(space, sp);
            }
        }
    }

    private View createDoctorCard(final Doctor doctor) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_input);
        card.setPadding(dp2px(14), dp2px(12), dp2px(14), dp2px(12));
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, ConsultChatActivity.class);
            intent.putExtra("doctor_name", doctor.name);
            intent.putExtra("department", doctor.department);
            intent.putExtra("doctor_avatar", doctor.avatarRes);
            startActivity(intent);
        });

        ImageView ivAvatar = new ImageView(this);
        ivAvatar.setImageResource(doctor.avatarRes);
        ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivAvatar.setClipToOutline(true);
        ivAvatar.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp2px(52), dp2px(52));
        card.addView(ivAvatar, avatarParams);

        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.leftMargin = dp2px(12);
        card.addView(infoLayout, infoParams);

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        infoLayout.addView(nameRow);

        TextView nameTv = new TextView(this);
        nameTv.setText(doctor.name);
        nameTv.setTextSize(16);
        nameTv.setTextColor(Color.parseColor("#333333"));
        nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
        nameRow.addView(nameTv);

        TextView titleTv = new TextView(this);
        titleTv.setText("  " + doctor.title);
        titleTv.setTextSize(12);
        titleTv.setTextColor(Color.parseColor("#666666"));
        nameRow.addView(titleTv);

        TextView deptTv = new TextView(this);
        deptTv.setText(doctor.department + " | " + doctor.hospital);
        deptTv.setTextSize(12);
        deptTv.setTextColor(Color.parseColor("#999999"));
        deptTv.setPadding(0, dp2px(4), 0, 0);
        infoLayout.addView(deptTv);

        LinearLayout ratingRow = new LinearLayout(this);
        ratingRow.setOrientation(LinearLayout.HORIZONTAL);
        ratingRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        ratingRow.setPadding(0, dp2px(6), 0, 0);
        infoLayout.addView(ratingRow);

        TextView ratingTv = new TextView(this);
        ratingTv.setText("★ " + doctor.rating);
        ratingTv.setTextSize(12);
        ratingTv.setTextColor(Color.parseColor("#FF9500"));
        ratingRow.addView(ratingTv);

        TextView countTv = new TextView(this);
        countTv.setText("  接诊 " + doctor.consultCount + "次");
        countTv.setTextSize(12);
        countTv.setTextColor(Color.parseColor("#999999"));
        ratingRow.addView(countTv);

        TextView consultBtn = new TextView(this);
        consultBtn.setText("问诊");
        consultBtn.setTextSize(13);
        consultBtn.setTextColor(Color.WHITE);
        consultBtn.setGravity(android.view.Gravity.CENTER);
        consultBtn.setPadding(dp2px(16), dp2px(6), dp2px(16), dp2px(6));
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor("#FFD93D"));
        btnBg.setCornerRadius(dp2px(16));
        consultBtn.setBackground(btnBg);
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        card.addView(consultBtn, btnParams);

        return card;
    }

    private void showEmergencyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_emergency, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btn_call_emergency).setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:4001201200"));
            startActivity(intent);
        });

        dialogView.findViewById(R.id.btn_cancel_emergency).setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }
}
