package com.example.love_pet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class VaccineRecordActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_VACCINE = 1001;

    private LinearLayout layoutRecords;
    private LinearLayout layoutNext;
    private List<VaccineRecord> recordList = new ArrayList<>();
    private List<NextVaccine> nextList = new ArrayList<>();

    public static class VaccineRecord {
        public String name;
        public String petName;
        public String date;
        public String hospital;
        public String type;

        public VaccineRecord(String name, String petName, String date, String hospital, String type) {
            this.name = name;
            this.petName = petName;
            this.date = date;
            this.hospital = hospital;
            this.type = type;
        }
    }

    public static class NextVaccine {
        public String name;
        public String petName;
        public String nextDate;
        public int daysLeft;

        public NextVaccine(String name, String petName, String nextDate, int daysLeft) {
            this.name = name;
            this.petName = petName;
            this.nextDate = nextDate;
            this.daysLeft = daysLeft;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vaccine_record);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutRecords = findViewById(R.id.layout_records);
        layoutNext = findViewById(R.id.layout_next);

        findViewById(R.id.btn_add).setOnClickListener(v -> {
            Intent intent = new Intent(VaccineRecordActivity.this, AddVaccineActivity.class);
            startActivityForResult(intent, REQUEST_ADD_VACCINE);
        });

        loadNextVaccine();
        loadRecords();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_VACCINE && resultCode == RESULT_OK && data != null) {
            String vaccineName = data.getStringExtra("vaccine_name");
            String petName = data.getStringExtra("pet_name");
            String vaccineDate = data.getStringExtra("vaccine_date");
            String hospital = data.getStringExtra("hospital");
            String vaccineType = data.getStringExtra("vaccine_type");
            boolean hasRemind = data.getBooleanExtra("has_remind", false);
            String nextDate = data.getStringExtra("next_date");

            VaccineRecord newRecord = new VaccineRecord(vaccineName, petName, vaccineDate, hospital, vaccineType);
            recordList.add(0, newRecord);
            refreshRecords();

            if (hasRemind && nextDate != null && !nextDate.isEmpty()) {
                int daysLeft = calculateDaysLeft(nextDate);
                NextVaccine nextVaccine = new NextVaccine(vaccineName, petName, nextDate, daysLeft);
                nextList.add(nextVaccine);
                refreshNextVaccine();
            }

            Toast.makeText(this, "疫苗记录添加成功", Toast.LENGTH_SHORT).show();
        }
    }

    private int calculateDaysLeft(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date targetDate = sdf.parse(dateStr);
            long diff = targetDate.getTime() - System.currentTimeMillis();
            return (int) (diff / (1000 * 60 * 60 * 24));
        } catch (Exception e) {
            return 30;
        }
    }

    private void refreshNextVaccine() {
        layoutNext.removeAllViews();
        for (NextVaccine vac : nextList) {
            View item = createNextVaccineItem(vac);
            layoutNext.addView(item);
        }
    }

    private void refreshRecords() {
        layoutRecords.removeAllViews();
        for (VaccineRecord record : recordList) {
            View item = createRecordItem(record);
            layoutRecords.addView(item);
        }
    }

    private List<NextVaccine> getNextVaccines() {
        List<NextVaccine> list = new ArrayList<>();
        list.add(new NextVaccine("狂犬疫苗", "橘猫", "2024-03-15", 60));
        list.add(new NextVaccine("三联疫苗", "金毛", "2024-02-20", 35));
        return list;
    }

    private List<VaccineRecord> getRecords() {
        List<VaccineRecord> list = new ArrayList<>();
        list.add(new VaccineRecord("狂犬疫苗", "橘猫", "2023-03-15", "萌宠宠物医院", "灭活疫苗"));
        list.add(new VaccineRecord("三联疫苗", "橘猫", "2023-02-20", "萌宠宠物医院", "活疫苗"));
        list.add(new VaccineRecord("狂犬疫苗", "金毛", "2023-08-10", "爱宠动物医院", "灭活疫苗"));
        list.add(new VaccineRecord("六联疫苗", "金毛", "2023-08-10", "爱宠动物医院", "活疫苗"));
        list.add(new VaccineRecord("狂犬疫苗", "布偶", "2023-11-05", "萌宠宠物医院", "灭活疫苗"));
        return list;
    }

    private void loadNextVaccine() {
        nextList = getNextVaccines();
        for (NextVaccine vac : nextList) {
            View item = createNextVaccineItem(vac);
            layoutNext.addView(item);
        }
    }

    private View createNextVaccineItem(NextVaccine vac) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setBackgroundColor(Color.parseColor("#FFF9F5"));
        item.setPadding(dp2px(16), dp2px(16), dp2px(16), dp2px(16));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp2px(12);
        item.setLayoutParams(params);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        item.addView(topRow);

        TextView nameTv = new TextView(this);
        nameTv.setText(vac.name);
        nameTv.setTextSize(15);
        nameTv.setTextColor(Color.parseColor("#333333"));
        nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        topRow.addView(nameTv, nameParams);

        TextView daysTv = new TextView(this);
        daysTv.setText("还有" + vac.daysLeft + "天");
        daysTv.setTextSize(14);
        daysTv.setTextColor(Color.parseColor("#FF6B35"));
        daysTv.setTypeface(null, android.graphics.Typeface.BOLD);
        topRow.addView(daysTv);

        TextView petTv = new TextView(this);
        petTv.setText("宠物：" + vac.petName);
        petTv.setTextSize(13);
        petTv.setTextColor(Color.parseColor("#666666"));
        petTv.setPadding(0, dp2px(8), 0, 0);
        item.addView(petTv);

        TextView dateTv = new TextView(this);
        dateTv.setText("下次接种：" + vac.nextDate);
        dateTv.setTextSize(13);
        dateTv.setTextColor(Color.parseColor("#666666"));
        dateTv.setPadding(0, dp2px(4), 0, 0);
        item.addView(dateTv);

        return item;
    }

    private void loadRecords() {
        recordList = getRecords();
        for (VaccineRecord record : recordList) {
            View item = createRecordItem(record);
            layoutRecords.addView(item);
        }
    }

    private View createRecordItem(VaccineRecord record) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setBackgroundColor(Color.WHITE);
        item.setPadding(dp2px(16), dp2px(16), dp2px(16), dp2px(16));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        item.addView(topRow);

        TextView nameTv = new TextView(this);
        nameTv.setText(record.name);
        nameTv.setTextSize(15);
        nameTv.setTextColor(Color.parseColor("#333333"));
        nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        topRow.addView(nameTv, nameParams);

        TextView typeTv = new TextView(this);
        typeTv.setText(record.type);
        typeTv.setTextSize(11);
        typeTv.setTextColor(Color.parseColor("#666666"));
        typeTv.setBackgroundColor(Color.parseColor("#F5F5F5"));
        typeTv.setPadding(dp2px(8), dp2px(3), dp2px(8), dp2px(3));
        topRow.addView(typeTv);

        TextView petTv = new TextView(this);
        petTv.setText("宠物：" + record.petName);
        petTv.setTextSize(13);
        petTv.setTextColor(Color.parseColor("#666666"));
        petTv.setPadding(0, dp2px(8), 0, 0);
        item.addView(petTv);

        TextView dateTv = new TextView(this);
        dateTv.setText("接种日期：" + record.date);
        dateTv.setTextSize(13);
        dateTv.setTextColor(Color.parseColor("#666666"));
        dateTv.setPadding(0, dp2px(4), 0, 0);
        item.addView(dateTv);

        TextView hospitalTv = new TextView(this);
        hospitalTv.setText("接种医院：" + record.hospital);
        hospitalTv.setTextSize(13);
        hospitalTv.setTextColor(Color.parseColor("#666666"));
        hospitalTv.setPadding(0, dp2px(4), 0, 0);
        item.addView(hospitalTv);

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
