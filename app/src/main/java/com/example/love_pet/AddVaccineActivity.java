package com.example.love_pet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddVaccineActivity extends AppCompatActivity {

    private TextView tvVaccineName;
    private TextView tvPetName;
    private TextView tvVaccineDate;
    private TextView tvTypeInactivated;
    private TextView tvTypeLive;
    private TextView tvTypeOther;
    private SwitchCompat switchRemind;
    private TextView tvNextDate;

    private String selectedVaccine = "";
    private String selectedPet = "";
    private String selectedDate = "";
    private String selectedType = "灭活疫苗";
    private String nextDate = "";
    private boolean hasRemind = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vaccine);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        tvVaccineName = findViewById(R.id.tv_vaccine_name);
        tvPetName = findViewById(R.id.tv_pet_name);
        tvVaccineDate = findViewById(R.id.tv_vaccine_date);
        tvTypeInactivated = findViewById(R.id.tv_type_inactivated);
        tvTypeLive = findViewById(R.id.tv_type_live);
        tvTypeOther = findViewById(R.id.tv_type_other);
        switchRemind = findViewById(R.id.switch_remind);
        tvNextDate = findViewById(R.id.tv_next_date);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        selectedDate = today;
        tvVaccineDate.setText(today);
        tvVaccineDate.setTextColor(0xFF333333);

        updateTypeUI();

        tvVaccineName.setOnClickListener(v -> showVaccinePicker());
        tvPetName.setOnClickListener(v -> showPetPicker());
        tvVaccineDate.setOnClickListener(v -> showDatePicker(false));
        tvNextDate.setOnClickListener(v -> showDatePicker(true));

        tvTypeInactivated.setOnClickListener(v -> {
            selectedType = "灭活疫苗";
            updateTypeUI();
        });
        tvTypeLive.setOnClickListener(v -> {
            selectedType = "活疫苗";
            updateTypeUI();
        });
        tvTypeOther.setOnClickListener(v -> {
            selectedType = "其他";
            updateTypeUI();
        });

        switchRemind.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hasRemind = isChecked;
            tvNextDate.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        findViewById(R.id.btn_save).setOnClickListener(v -> saveVaccineRecord());
    }

    private void updateTypeUI() {
        tvTypeInactivated.setBackgroundResource(selectedType.equals("灭活疫苗") ?
                R.drawable.bg_vaccine_type_selected : R.drawable.bg_vaccine_type_normal);
        tvTypeInactivated.setTextColor(selectedType.equals("灭活疫苗") ?
                0xFFFF6B35 : 0xFF666666);

        tvTypeLive.setBackgroundResource(selectedType.equals("活疫苗") ?
                R.drawable.bg_vaccine_type_selected : R.drawable.bg_vaccine_type_normal);
        tvTypeLive.setTextColor(selectedType.equals("活疫苗") ?
                0xFFFF6B35 : 0xFF666666);

        tvTypeOther.setBackgroundResource(selectedType.equals("其他") ?
                R.drawable.bg_vaccine_type_selected : R.drawable.bg_vaccine_type_normal);
        tvTypeOther.setTextColor(selectedType.equals("其他") ?
                0xFFFF6B35 : 0xFF666666);
    }

    private void showVaccinePicker() {
        final String[] vaccines = {"狂犬疫苗", "三联疫苗", "四联疫苗", "五联疫苗", "六联疫苗", "八联疫苗", "猫三联", "猫五联", "其他疫苗"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("选择疫苗名称")
                .setItems(vaccines, (dialog, which) -> {
                    selectedVaccine = vaccines[which];
                    tvVaccineName.setText(selectedVaccine);
                    tvVaccineName.setTextColor(0xFF333333);
                })
                .show();
    }

    private void showPetPicker() {
        final String[] pets = {"橘猫", "金毛", "布偶", "其他宠物"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("选择宠物")
                .setItems(pets, (dialog, which) -> {
                    selectedPet = pets[which];
                    tvPetName.setText(selectedPet);
                    tvPetName.setTextColor(0xFF333333);
                })
                .show();
    }

    private void showDatePicker(final boolean isNextDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String dateStr = String.format(Locale.getDefault(), "%d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    if (isNextDate) {
                        nextDate = dateStr;
                        tvNextDate.setText(dateStr);
                        tvNextDate.setTextColor(0xFF333333);
                    } else {
                        selectedDate = dateStr;
                        tvVaccineDate.setText(dateStr);
                        tvVaccineDate.setTextColor(0xFF333333);
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void saveVaccineRecord() {
        if (selectedVaccine.isEmpty()) {
            Toast.makeText(this, "请选择疫苗名称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedPet.isEmpty()) {
            Toast.makeText(this, "请选择宠物", Toast.LENGTH_SHORT).show();
            return;
        }
        if (hasRemind && nextDate.isEmpty()) {
            Toast.makeText(this, "请选择下次接种日期", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText etHospital = findViewById(R.id.et_hospital);
        String hospital = etHospital.getText().toString().trim();
        if (hospital.isEmpty()) {
            hospital = "未填写";
        }

        EditText etRemark = findViewById(R.id.et_remark);
        String remark = etRemark.getText().toString().trim();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("vaccine_name", selectedVaccine);
        resultIntent.putExtra("pet_name", selectedPet);
        resultIntent.putExtra("vaccine_date", selectedDate);
        resultIntent.putExtra("hospital", hospital);
        resultIntent.putExtra("vaccine_type", selectedType);
        resultIntent.putExtra("remark", remark);
        resultIntent.putExtra("has_remind", hasRemind);
        resultIntent.putExtra("next_date", nextDate);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
