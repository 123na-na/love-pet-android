package com.example.love_pet;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AiResultActivity extends AppCompatActivity {

    private LinearLayout layoutDiseases;
    private LinearLayout layoutAdvice;
    private TextView tvResultLevel;
    private TextView tvResultDesc;
    private ImageView ivResultIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_result);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutDiseases = findViewById(R.id.layout_diseases);
        layoutAdvice = findViewById(R.id.layout_advice);
        tvResultLevel = findViewById(R.id.tv_result_level);
        tvResultDesc = findViewById(R.id.tv_result_desc);
        ivResultIcon = findViewById(R.id.iv_result_icon);

        String petType = getIntent().getStringExtra("petType");
        String age = getIntent().getStringExtra("age");
        ArrayList<String> symptoms = getIntent().getStringArrayListExtra("symptoms");
        String description = getIntent().getStringExtra("description");

        analyzeSymptoms(symptoms, petType, age, description);

        findViewById(R.id.btn_online_consult).setOnClickListener(v -> {
            Intent intent = new Intent(this, ConsultChatActivity.class);
            intent.putExtra("doctor_name", "AI推荐医生");
            intent.putExtra("department", "综合科");
            intent.putExtra("doctor_avatar", R.drawable.doctor);
            startActivity(intent);
        });

        findViewById(R.id.btn_recheck).setOnClickListener(v -> finish());
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    private void analyzeSymptoms(List<String> symptoms, String petType, String age, String description) {
        AiDiagnosisService.DiagnosisResult result = AiDiagnosisService.analyze(petType, age, symptoms, description);

        tvResultLevel.setText(result.level);
        tvResultLevel.setTextColor(result.levelColor);
        tvResultDesc.setText(result.levelDesc);

        addDiseases(result.diseases);
        addAdvice(result.adviceList);
    }

    private void addDiseases(List<AiDiagnosisService.DiseaseInfo> diseases) {
        int index = 1;
        for (AiDiagnosisService.DiseaseInfo disease : diseases) {
            View item = createDiseaseItem(index, disease.name, disease.probability, disease.description, disease.department);
            layoutDiseases.addView(item);
            if (index < diseases.size()) {
                View divider = new View(this);
                LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp2px(1));
                dp.topMargin = dp2px(10);
                dp.bottomMargin = dp2px(10);
                divider.setBackgroundColor(Color.parseColor("#F0F0F0"));
                layoutDiseases.addView(divider, dp);
            }
            index++;
        }
    }

    private View createDiseaseItem(int index, String name, float probability, String description, String department) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        item.addView(topRow);

        TextView indexTv = new TextView(this);
        indexTv.setText(index + ".");
        indexTv.setTextSize(14);
        indexTv.setTextColor(Color.parseColor("#333333"));
        indexTv.setTypeface(null, android.graphics.Typeface.BOLD);
        topRow.addView(indexTv);

        TextView nameTv = new TextView(this);
        nameTv.setText(name);
        nameTv.setTextSize(14);
        nameTv.setTextColor(Color.parseColor("#333333"));
        nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
        nameTv.setPadding(dp2px(4), 0, 0, 0);
        topRow.addView(nameTv);

        TextView probTv = new TextView(this);
        probTv.setText("  概率 " + (int)(probability * 100) + "%");
        probTv.setTextSize(12);
        probTv.setTextColor(Color.parseColor("#FF9500"));
        topRow.addView(probTv);

        View barBg = new View(this);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp2px(6));
        barParams.topMargin = dp2px(8);
        barBg.setBackgroundResource(R.drawable.bg_input_round);
        item.addView(barBg, barParams);

        TextView descTv = new TextView(this);
        descTv.setText(description);
        descTv.setTextSize(12);
        descTv.setTextColor(Color.parseColor("#999999"));
        descTv.setPadding(0, dp2px(6), 0, 0);
        item.addView(descTv);

        TextView deptTv = new TextView(this);
        deptTv.setText("建议就诊科室：" + department);
        deptTv.setTextSize(12);
        deptTv.setTextColor(Color.parseColor("#5AC8FA"));
        deptTv.setPadding(0, dp2px(4), 0, 0);
        item.addView(deptTv);

        return item;
    }

    private void addAdvice(List<String> adviceList) {
        for (int i = 0; i < adviceList.size(); i++) {
            TextView tv = new TextView(this);
            tv.setText("• " + adviceList.get(i));
            tv.setTextSize(14);
            tv.setTextColor(Color.parseColor("#666666"));
            tv.setLineSpacing(dp2px(4), 1);
            if (i > 0) {
                tv.setPadding(0, dp2px(8), 0, 0);
            }
            layoutAdvice.addView(tv);
        }
    }
}
