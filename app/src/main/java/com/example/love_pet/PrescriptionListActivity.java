package com.example.love_pet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionListActivity extends AppCompatActivity {

    private LinearLayout layoutPrescriptionList;
    private LinearLayout layoutEmpty;

    public static class PrescriptionRecord {
        public int id;
        public String doctorName;
        public String department;
        public String petName;
        public String date;
        public String diagnosis;
        public List<PrescriptionActivity.Medicine> medicines;
        public String status;

        public PrescriptionRecord(int id, String doctorName, String department, String petName, String date, String diagnosis, String status) {
            this.id = id;
            this.doctorName = doctorName;
            this.department = department;
            this.petName = petName;
            this.date = date;
            this.diagnosis = diagnosis;
            this.status = status;
            this.medicines = new ArrayList<>();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription_list);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutPrescriptionList = findViewById(R.id.layout_prescription_list);
        layoutEmpty = findViewById(R.id.layout_empty);

        loadPrescriptions();
    }

    private void loadPrescriptions() {
        List<PrescriptionRecord> records = getPrescriptionRecords();

        if (records.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            layoutPrescriptionList.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        layoutPrescriptionList.setVisibility(View.VISIBLE);

        for (PrescriptionRecord record : records) {
            View item = createPrescriptionItem(record);
            layoutPrescriptionList.addView(item);
        }
    }

    private List<PrescriptionRecord> getPrescriptionRecords() {
        List<PrescriptionRecord> list = new ArrayList<>();

        PrescriptionRecord r1 = new PrescriptionRecord(1, "王晓明", "内科", "橘猫", "2024-01-15 14:30", "急性肠胃炎", "已完成");
        r1.medicines.add(new PrescriptionActivity.Medicine("蒙脱石散", "3g/袋", "每次1袋", "每日2次", "3天用量", 15.0f, R.drawable.e_medicine));
        r1.medicines.add(new PrescriptionActivity.Medicine("益生菌", "5g/袋", "每次1袋", "每日1次", "7天用量", 68.0f, R.drawable.e_medicine));
        r1.medicines.add(new PrescriptionActivity.Medicine("阿莫西林克拉维酸钾", "0.25g/片", "按体重50mg/kg", "每日2次", "5天用量", 45.0f, R.drawable.e_medicine));
        list.add(r1);

        PrescriptionRecord r2 = new PrescriptionRecord(2, "李雪梅", "皮肤科", "金毛", "2024-01-10 10:20", "真菌感染（猫癣）", "已完成");
        r2.medicines.add(new PrescriptionActivity.Medicine("酮康唑软膏", "15g/支", "外用涂抹", "每日2次", "2周用量", 38.0f, R.drawable.e_medicine));
        r2.medicines.add(new PrescriptionActivity.Medicine("复合维生素B", "100片/瓶", "每次1片", "每日1次", "1月用量", 28.0f, R.drawable.e_medicine));
        list.add(r2);

        PrescriptionRecord r3 = new PrescriptionRecord(3, "张建国", "外科", "布偶", "2024-01-05 16:45", "术后护理", "待取药");
        r3.medicines.add(new PrescriptionActivity.Medicine("速诺（阿莫西林克拉维酸钾）", "0.5g/片", "按体重25mg/kg", "每日2次", "7天用量", 89.0f, R.drawable.e_medicine));
        r3.medicines.add(new PrescriptionActivity.Medicine("止痛片", "10片/盒", "疼痛时服用", "按需服用", "3天用量", 56.0f, R.drawable.e_medicine));
        list.add(r3);

        return list;
    }

    private View createPrescriptionItem(final PrescriptionRecord record) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setBackgroundColor(Color.WHITE);
        item.setPadding(dp2px(16), dp2px(16), dp2px(16), dp2px(16));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        item.addView(topRow);

        TextView deptTv = new TextView(this);
        deptTv.setText(record.department);
        deptTv.setTextSize(15);
        deptTv.setTextColor(Color.parseColor("#333333"));
        deptTv.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams deptParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        topRow.addView(deptTv, deptParams);

        TextView statusTv = new TextView(this);
        statusTv.setText(record.status);
        statusTv.setTextSize(12);
        if ("已完成".equals(record.status)) {
            statusTv.setTextColor(Color.parseColor("#999999"));
        } else {
            statusTv.setTextColor(Color.parseColor("#FF6B35"));
        }
        topRow.addView(statusTv);

        TextView doctorTv = new TextView(this);
        doctorTv.setText("医师：" + record.doctorName);
        doctorTv.setTextSize(13);
        doctorTv.setTextColor(Color.parseColor("#666666"));
        doctorTv.setPadding(0, dp2px(8), 0, 0);
        item.addView(doctorTv);

        TextView petTv = new TextView(this);
        petTv.setText("患宠：" + record.petName);
        petTv.setTextSize(13);
        petTv.setTextColor(Color.parseColor("#666666"));
        petTv.setPadding(0, dp2px(4), 0, 0);
        item.addView(petTv);

        TextView diagTv = new TextView(this);
        diagTv.setText("诊断：" + record.diagnosis);
        diagTv.setTextSize(13);
        diagTv.setTextColor(Color.parseColor("#666666"));
        diagTv.setPadding(0, dp2px(4), 0, 0);
        item.addView(diagTv);

        TextView dateTv = new TextView(this);
        dateTv.setText(record.date);
        dateTv.setTextSize(12);
        dateTv.setTextColor(Color.parseColor("#999999"));
        dateTv.setPadding(0, dp2px(8), 0, 0);
        item.addView(dateTv);

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(android.view.Gravity.END);
        bottomRow.setPadding(0, dp2px(12), 0, 0);
        item.addView(bottomRow);

        TextView detailBtn = new TextView(this);
        detailBtn.setText("查看详情");
        detailBtn.setTextSize(13);
        detailBtn.setTextColor(Color.parseColor("#FF6B35"));
        detailBtn.setBackgroundColor(Color.parseColor("#FFF0E6"));
        detailBtn.setPadding(dp2px(16), dp2px(6), dp2px(16), dp2px(6));
        detailBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, PrescriptionActivity.class);
            intent.putExtra("prescription_id", record.id);
            intent.putExtra("doctor_name", record.doctorName);
            intent.putExtra("department", record.department);
            intent.putExtra("pet_name", record.petName);
            intent.putExtra("date", record.date);
            intent.putExtra("diagnosis", record.diagnosis);
            startActivity(intent);
        });
        bottomRow.addView(detailBtn);

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
