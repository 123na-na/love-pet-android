package com.example.love_pet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AiCheckActivity extends AppCompatActivity {

    private LinearLayout layoutPetType;
    private LinearLayout layoutAge;
    private FlowLayout layoutSymptoms;
    private EditText etDescription;

    private String selectedPetType = "";
    private String selectedAge = "";
    private List<String> selectedSymptoms = new ArrayList<>();

    private String[] petTypes = {"猫咪", "狗狗", "异宠"};
    private String[] ages = {"幼年", "成年", "老年"};
    private String[] symptoms = {"呕吐", "拉稀", "皮肤病", "尿闭", "抽搐", "咳嗽", "打喷嚏", "食欲不振", "精神萎靡", "发烧", "掉毛", "瘙痒"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_check);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutPetType = findViewById(R.id.layout_pet_type);
        layoutAge = findViewById(R.id.layout_age);
        layoutSymptoms = findViewById(R.id.layout_symptoms);
        etDescription = findViewById(R.id.et_description);

        renderPetTypes();
        renderAges();
        renderSymptoms();

        findViewById(R.id.btn_submit).setOnClickListener(v -> {
            if (selectedPetType.isEmpty()) {
                Toast.makeText(this, "请选择宠物类型", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedSymptoms.isEmpty()) {
                Toast.makeText(this, "请选择至少一个症状", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, AiResultActivity.class);
            intent.putExtra("petType", selectedPetType);
            intent.putExtra("age", selectedAge);
            intent.putStringArrayListExtra("symptoms", new ArrayList<>(selectedSymptoms));
            intent.putExtra("description", etDescription.getText().toString());
            startActivity(intent);
        });
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    private void renderPetTypes() {
        for (int i = 0; i < petTypes.length; i++) {
            final String type = petTypes[i];
            final int index = i;
            TextView tv = new TextView(this);
            tv.setText(type);
            tv.setTextSize(14);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setPadding(dp2px(16), dp2px(8), dp2px(16), dp2px(8));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            if (i > 0) {
                params.leftMargin = dp2px(10);
            }
            tv.setLayoutParams(params);

            updateTagStyle(tv, false);
            tv.setOnClickListener(v -> {
                selectedPetType = type;
                for (int j = 0; j < layoutPetType.getChildCount(); j++) {
                    View child = layoutPetType.getChildAt(j);
                    if (child instanceof TextView) {
                        updateTagStyle((TextView) child, j == index);
                    }
                }
            });
            layoutPetType.addView(tv);
        }
    }

    private void renderAges() {
        for (int i = 0; i < ages.length; i++) {
            final String age = ages[i];
            final int index = i;
            TextView tv = new TextView(this);
            tv.setText(age);
            tv.setTextSize(14);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setPadding(dp2px(16), dp2px(8), dp2px(16), dp2px(8));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            if (i > 0) {
                params.leftMargin = dp2px(10);
            }
            tv.setLayoutParams(params);

            updateTagStyle(tv, false);
            tv.setOnClickListener(v -> {
                selectedAge = age;
                for (int j = 0; j < layoutAge.getChildCount(); j++) {
                    View child = layoutAge.getChildAt(j);
                    if (child instanceof TextView) {
                        updateTagStyle((TextView) child, j == index);
                    }
                }
            });
            layoutAge.addView(tv);
        }
    }

    private void renderSymptoms() {
        for (final String symptom : symptoms) {
            final TextView tv = new TextView(this);
            tv.setText(symptom);
            tv.setTextSize(13);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setPadding(dp2px(14), dp2px(8), dp2px(14), dp2px(8));

            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                    FlowLayout.LayoutParams.WRAP_CONTENT, FlowLayout.LayoutParams.WRAP_CONTENT);
            ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(params);
            marginParams.rightMargin = dp2px(10);
            marginParams.bottomMargin = dp2px(10);
            tv.setLayoutParams(marginParams);

            updateTagStyle(tv, false);
            tv.setOnClickListener(v -> {
                if (selectedSymptoms.contains(symptom)) {
                    selectedSymptoms.remove(symptom);
                    updateTagStyle(tv, false);
                } else {
                    selectedSymptoms.add(symptom);
                    updateTagStyle(tv, true);
                }
            });
            layoutSymptoms.addView(tv);
        }
    }

    private void updateTagStyle(TextView tv, boolean selected) {
        GradientDrawable bg = new GradientDrawable();
        if (selected) {
            bg.setColor(Color.parseColor("#FFF5E6"));
            bg.setStroke(dp2px(1), Color.parseColor("#FFD93D"));
            tv.setTextColor(Color.parseColor("#333333"));
        } else {
            bg.setColor(Color.parseColor("#F5F5F5"));
            bg.setStroke(dp2px(1), Color.parseColor("#E0E0E0"));
            tv.setTextColor(Color.parseColor("#666666"));
        }
        bg.setCornerRadius(dp2px(16));
        tv.setBackground(bg);
    }
}
