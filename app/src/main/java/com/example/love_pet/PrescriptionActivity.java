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

public class PrescriptionActivity extends AppCompatActivity {

    private LinearLayout layoutMedicines;
    private String doctorName;
    private String department;
    private String petName;
    private List<Medicine> medicineList;

    public static class Medicine {
        public String name;
        public String spec;
        public String dosage;
        public String frequency;
        public String duration;
        public float price;
        public int iconRes;

        public Medicine(String name, String spec, String dosage, String frequency, String duration, float price, int iconRes) {
            this.name = name;
            this.spec = spec;
            this.dosage = dosage;
            this.frequency = frequency;
            this.duration = duration;
            this.price = price;
            this.iconRes = iconRes;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        doctorName = getIntent().getStringExtra("doctor_name");
        department = getIntent().getStringExtra("department");
        petName = getIntent().getStringExtra("pet_name");

        if (doctorName == null) doctorName = "李医生";
        if (department == null) department = "内科";
        if (petName == null) petName = "未知宠物";

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutMedicines = findViewById(R.id.layout_medicines);

        TextView tvDoctor = findViewById(R.id.tv_doctor);
        TextView tvDept = findViewById(R.id.tv_dept);
        TextView tvPet = findViewById(R.id.tv_pet);
        TextView tvDate = findViewById(R.id.tv_date);

        tvDoctor.setText("医师：" + doctorName);
        tvDept.setText("科室：" + department);
        tvPet.setText("患宠：" + petName);
        tvDate.setText("开具时间：" + getCurrentTime());

        loadMedicines();

        findViewById(R.id.btn_add_cart).setOnClickListener(v -> {
            addAllToCart();
            Toast.makeText(this, "已全部加入购物车", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_buy_now).setOnClickListener(v -> {
            addAllToCart();
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_add_reminder).setOnClickListener(v -> {
            addAllToReminder();
            Toast.makeText(this, "已一键添加用药提醒", Toast.LENGTH_SHORT).show();
        });
    }

    private void addAllToReminder() {
        if (medicineList == null || medicineList.isEmpty()) return;

        Intent intent = new Intent(this, MedicationReminderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("add_from_prescription", true);
        intent.putExtra("pet_name", petName);
        intent.putParcelableArrayListExtra("medicines", new ArrayList<>());
        startActivity(intent);
    }

    private void addAllToCart() {
        if (medicineList == null || medicineList.isEmpty()) return;

        int productId = 10000;
        for (Medicine med : medicineList) {
            CartItem item = new CartItem(
                    productId++,
                    med.name,
                    med.spec,
                    String.format("%.2f", med.price),
                    "e_medicine",
                    "宠物大药房",
                    1
            );
            CartItem.addToCart(this, item);
        }
    }

    private String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new java.util.Date());
    }

    private void loadMedicines() {
        medicineList = getMedicines();
        float total = 0;
        for (Medicine med : medicineList) {
            View item = createMedicineItem(med);
            layoutMedicines.addView(item);
            total += med.price;
        }

        TextView tvTotal = findViewById(R.id.tv_total);
        tvTotal.setText("¥ " + String.format("%.2f", total));
    }

    private List<Medicine> getMedicines() {
        List<Medicine> list = new ArrayList<>();
        list.add(new Medicine("蒙脱石散", "3g/袋", "每次1袋", "每日2次", "3天用量", 15.0f, R.drawable.e_medicine));
        list.add(new Medicine("益生菌", "5g/袋", "每次1袋", "每日1次", "7天用量", 68.0f, R.drawable.e_medicine));
        list.add(new Medicine("阿莫西林克拉维酸钾", "0.25g/片", "按体重50mg/kg", "每日2次", "5天用量", 45.0f, R.drawable.e_medicine));
        return list;
    }

    private View createMedicineItem(Medicine medicine) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setBackgroundColor(Color.parseColor("#FAFAFA"));
        item.setPadding(dp2px(12), dp2px(12), dp2px(12), dp2px(12));

        ImageView icon = new ImageView(this);
        icon.setImageResource(medicine.iconRes);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp2px(48), dp2px(48));
        item.addView(icon, iconParams);

        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        infoLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        infoLayout.setPadding(dp2px(12), 0, 0, 0);
        item.addView(infoLayout);

        TextView nameTv = new TextView(this);
        nameTv.setText(medicine.name);
        nameTv.setTextSize(15);
        nameTv.setTextColor(Color.parseColor("#333333"));
        nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
        infoLayout.addView(nameTv);

        TextView specTv = new TextView(this);
        specTv.setText("规格：" + medicine.spec);
        specTv.setTextSize(12);
        specTv.setTextColor(Color.parseColor("#999999"));
        specTv.setPadding(0, dp2px(4), 0, 0);
        infoLayout.addView(specTv);

        TextView dosageTv = new TextView(this);
        dosageTv.setText("用法：" + medicine.dosage + "，" + medicine.frequency);
        dosageTv.setTextSize(12);
        dosageTv.setTextColor(Color.parseColor("#666666"));
        dosageTv.setPadding(0, dp2px(2), 0, 0);
        infoLayout.addView(dosageTv);

        TextView durationTv = new TextView(this);
        durationTv.setText(medicine.duration);
        durationTv.setTextSize(12);
        durationTv.setTextColor(Color.parseColor("#FF9500"));
        durationTv.setPadding(0, dp2px(2), 0, 0);
        infoLayout.addView(durationTv);

        TextView priceTv = new TextView(this);
        priceTv.setText("¥" + String.format("%.2f", medicine.price));
        priceTv.setTextSize(16);
        priceTv.setTextColor(Color.parseColor("#FF6B35"));
        priceTv.setTypeface(null, android.graphics.Typeface.BOLD);
        priceTv.setGravity(android.view.Gravity.CENTER_VERTICAL);
        item.addView(priceTv);

        return item;
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
