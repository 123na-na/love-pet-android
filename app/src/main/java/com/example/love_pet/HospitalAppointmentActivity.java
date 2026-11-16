package com.example.love_pet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class HospitalAppointmentActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_HOSPITAL = 1001;
    private LinearLayout layoutHospitals;
    private TextView tvLocation;
    private String currentLocation = "北京市朝阳区";

    public static class Hospital {
        public String name;
        public String level;
        public String address;
        public String distance;
        public float rating;
        public int consultCount;
        public String[] departments;

        public Hospital(String name, String level, String address, String distance, float rating, int consultCount, String[] departments) {
            this.name = name;
            this.level = level;
            this.address = address;
            this.distance = distance;
            this.rating = rating;
            this.consultCount = consultCount;
            this.departments = departments;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_appointment);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutHospitals = findViewById(R.id.layout_hospitals);
        tvLocation = findViewById(R.id.tv_location);

        findViewById(R.id.layout_location).setOnClickListener(v -> {
            Intent intent = new Intent(this, HospitalMapActivity.class);
            startActivityForResult(intent, REQUEST_SELECT_HOSPITAL);
        });

        loadHospitals();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_HOSPITAL && resultCode == RESULT_OK && data != null) {
            String hospital = data.getStringExtra("selected_hospital");
            if (hospital != null) {
                Toast.makeText(this, "已选择：" + hospital, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private List<Hospital> getHospitals() {
        List<Hospital> list = new ArrayList<>();
        list.add(new Hospital("萌宠宠物医院", "三级甲等", "朝阳区建国路88号", "1.2km", 4.9f, 1286,
                new String[]{"内科", "外科", "皮肤科", "牙科", "眼科"}));
        list.add(new Hospital("爱宠动物医院", "三级乙等", "海淀区中关村大街1号", "3.5km", 4.8f, 956,
                new String[]{"内科", "外科", "影像科", "检验科"}));
        list.add(new Hospital("宠爱宠物诊所", "二级甲等", "西城区西单北大街10号", "2.1km", 4.7f, 634,
                new String[]{"内科", "皮肤科", "牙科"}));
        list.add(new Hospital("博爱动物医院", "三级甲等", "东城区王府井大街5号", "4.8km", 4.9f, 2103,
                new String[]{"内科", "外科", "皮肤科", "牙科", "眼科", "异宠专科"}));
        return list;
    }

    private void loadHospitals() {
        List<Hospital> hospitals = getHospitals();
        for (Hospital hospital : hospitals) {
            View item = createHospitalItem(hospital);
            layoutHospitals.addView(item);
        }
    }

    private View createHospitalItem(final Hospital hospital) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setBackgroundColor(Color.WHITE);
        item.setPadding(dp2px(16), dp2px(16), dp2px(16), dp2px(16));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        item.addView(topRow);

        TextView nameTv = new TextView(this);
        nameTv.setText(hospital.name);
        nameTv.setTextSize(16);
        nameTv.setTextColor(Color.parseColor("#333333"));
        nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        topRow.addView(nameTv, nameParams);

        TextView levelTv = new TextView(this);
        levelTv.setText(hospital.level);
        levelTv.setTextSize(11);
        levelTv.setTextColor(Color.parseColor("#007AFF"));
        levelTv.setBackgroundColor(Color.parseColor("#E8F0FF"));
        levelTv.setPadding(dp2px(8), dp2px(3), dp2px(8), dp2px(3));
        topRow.addView(levelTv);

        LinearLayout ratingRow = new LinearLayout(this);
        ratingRow.setOrientation(LinearLayout.HORIZONTAL);
        ratingRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        ratingRow.setPadding(0, dp2px(8), 0, 0);
        item.addView(ratingRow);

        TextView ratingTv = new TextView(this);
        ratingTv.setText("★ " + hospital.rating);
        ratingTv.setTextSize(13);
        ratingTv.setTextColor(Color.parseColor("#FF9500"));
        ratingRow.addView(ratingTv);

        TextView countTv = new TextView(this);
        countTv.setText("  接诊" + hospital.consultCount + "次");
        countTv.setTextSize(12);
        countTv.setTextColor(Color.parseColor("#999999"));
        ratingRow.addView(countTv);

        TextView distTv = new TextView(this);
        distTv.setText("  距离" + hospital.distance);
        distTv.setTextSize(12);
        distTv.setTextColor(Color.parseColor("#999999"));
        ratingRow.addView(distTv);

        TextView addrTv = new TextView(this);
        addrTv.setText("地址：" + hospital.address);
        addrTv.setTextSize(13);
        addrTv.setTextColor(Color.parseColor("#666666"));
        addrTv.setPadding(0, dp2px(6), 0, 0);
        item.addView(addrTv);

        LinearLayout deptLayout = new LinearLayout(this);
        deptLayout.setOrientation(LinearLayout.HORIZONTAL);
        deptLayout.setPadding(0, dp2px(12), 0, 0);
        item.addView(deptLayout);

        for (int i = 0; i < Math.min(hospital.departments.length, 4); i++) {
            TextView deptTv = new TextView(this);
            deptTv.setText(hospital.departments[i]);
            deptTv.setTextSize(11);
            deptTv.setTextColor(Color.parseColor("#666666"));
            deptTv.setBackgroundColor(Color.parseColor("#F5F5F5"));
            deptTv.setPadding(dp2px(8), dp2px(3), dp2px(8), dp2px(3));
            LinearLayout.LayoutParams deptParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) deptParams.leftMargin = dp2px(8);
            deptTv.setLayoutParams(deptParams);
            deptLayout.addView(deptTv);
        }

        if (hospital.departments.length > 4) {
            TextView moreTv = new TextView(this);
            moreTv.setText("+" + (hospital.departments.length - 4));
            moreTv.setTextSize(11);
            moreTv.setTextColor(Color.parseColor("#999999"));
            moreTv.setBackgroundColor(Color.parseColor("#F5F5F5"));
            moreTv.setPadding(dp2px(8), dp2px(3), dp2px(8), dp2px(3));
            LinearLayout.LayoutParams moreParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            moreParams.leftMargin = dp2px(8);
            moreTv.setLayoutParams(moreParams);
            deptLayout.addView(moreTv);
        }

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(android.view.Gravity.END);
        btnRow.setPadding(0, dp2px(16), 0, 0);
        item.addView(btnRow);

        TextView phoneBtn = new TextView(this);
        phoneBtn.setText("电话咨询");
        phoneBtn.setTextSize(13);
        phoneBtn.setTextColor(Color.parseColor("#666666"));
        phoneBtn.setBackgroundColor(Color.parseColor("#F5F5F5"));
        phoneBtn.setPadding(dp2px(16), dp2px(8), dp2px(16), dp2px(8));
        phoneBtn.setOnClickListener(v -> {
            Toast.makeText(this, "正在拨打医院电话...", Toast.LENGTH_SHORT).show();
        });
        btnRow.addView(phoneBtn);

        TextView apptBtn = new TextView(this);
        apptBtn.setText("立即挂号");
        apptBtn.setTextSize(13);
        apptBtn.setTextColor(Color.WHITE);
        apptBtn.setBackgroundColor(Color.parseColor("#FF6B35"));
        apptBtn.setPadding(dp2px(16), dp2px(8), dp2px(16), dp2px(8));
        LinearLayout.LayoutParams apptParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        apptParams.leftMargin = dp2px(12);
        apptBtn.setLayoutParams(apptParams);
        apptBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HospitalAppointmentActivity.this, AppointmentBookActivity.class);
            intent.putExtra(AppointmentBookActivity.EXTRA_HOSPITAL_NAME, hospital.name);
            intent.putExtra(AppointmentBookActivity.EXTRA_HOSPITAL_LEVEL, hospital.level);
            intent.putExtra(AppointmentBookActivity.EXTRA_HOSPITAL_ADDRESS, hospital.address);
            startActivity(intent);
        });
        btnRow.addView(apptBtn);

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#F0F0F0"));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp2px(1));
        dividerParams.topMargin = dp2px(16);
        item.addView(divider, dividerParams);

        return item;
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
