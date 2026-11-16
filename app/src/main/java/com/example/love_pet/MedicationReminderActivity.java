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

public class MedicationReminderActivity extends AppCompatActivity {

    private static final int REQUEST_ADD_MEDICATION = 2001;
    private LinearLayout layoutReminders;
    private LinearLayout layoutEmpty;

    public static class Medication {
        public String name;
        public String petName;
        public String dosage;
        public String frequency;
        public String nextTime;
        public int daysLeft;
        public boolean isTaken;

        public Medication(String name, String petName, String dosage, String frequency, String nextTime, int daysLeft, boolean isTaken) {
            this.name = name;
            this.petName = petName;
            this.dosage = dosage;
            this.frequency = frequency;
            this.nextTime = nextTime;
            this.daysLeft = daysLeft;
            this.isTaken = isTaken;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_reminder);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutReminders = findViewById(R.id.layout_reminders);
        layoutEmpty = findViewById(R.id.layout_empty);

        findViewById(R.id.btn_add).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMedicationActivity.class);
            startActivityForResult(intent, REQUEST_ADD_MEDICATION);
        });

        loadReminders();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_MEDICATION && resultCode == RESULT_OK) {
            layoutReminders.removeAllViews();
            loadReminders();
        }
    }

    private List<Medication> getMedications() {
        List<Medication> list = new ArrayList<>();
        list.add(new Medication("蒙脱石散", "橘猫", "每次1袋", "每日2次", "今天 08:00", 3, true));
        list.add(new Medication("益生菌", "橘猫", "每次1袋", "每日1次", "今天 12:00", 7, false));
        list.add(new Medication("酮康唑软膏", "金毛", "外用涂抹", "每日2次", "今天 18:00", 14, false));
        list.add(new Medication("复合维生素B", "金毛", "每次1片", "每日1次", "明天 09:00", 30, false));
        return list;
    }

    private void loadReminders() {
        List<Medication> meds = getMedications();

        if (meds.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            layoutReminders.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        layoutReminders.setVisibility(View.VISIBLE);

        for (Medication med : meds) {
            View item = createMedicationItem(med);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.bottomMargin = dp2px(12);
            item.setLayoutParams(params);
            layoutReminders.addView(item);
        }
    }

    private View createMedicationItem(final Medication med) {
        View item = getLayoutInflater().inflate(R.layout.item_medication_reminder, null);

        TextView nameTv = item.findViewById(R.id.tv_med_name);
        nameTv.setText(med.name);

        TextView petTv = item.findViewById(R.id.tv_pet_name);
        petTv.setText("患宠：" + med.petName);

        TextView statusTv = item.findViewById(R.id.tv_med_status);
        if (med.isTaken) {
            statusTv.setText("已服用");
            statusTv.setTextColor(Color.parseColor("#34C759"));
            statusTv.setBackgroundResource(R.drawable.bg_status_green);
        } else {
            statusTv.setText("待服用");
            statusTv.setTextColor(Color.parseColor("#FF6B35"));
            statusTv.setBackgroundResource(R.drawable.bg_status_orange);
        }

        TextView dosageTv = item.findViewById(R.id.tv_med_dosage);
        dosageTv.setText("用量：" + med.dosage + "，" + med.frequency);

        TextView timeTv = item.findViewById(R.id.tv_next_time);
        timeTv.setText("下次：" + med.nextTime);

        TextView daysTv = item.findViewById(R.id.tv_days_left);
        daysTv.setText("剩余" + med.daysLeft + "天");

        return item;
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
