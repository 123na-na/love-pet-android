package com.example.love_pet;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ConsultEvaluateActivity extends AppCompatActivity {

    private int professionalRating = 0;
    private int responseRating = 0;
    private int attitudeRating = 0;

    private LinearLayout layoutProfessional;
    private LinearLayout layoutResponse;
    private LinearLayout layoutAttitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consult_evaluate);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutProfessional = findViewById(R.id.layout_professional);
        layoutResponse = findViewById(R.id.layout_response);
        layoutAttitude = findViewById(R.id.layout_attitude);

        initStars(layoutProfessional, 0);
        initStars(layoutResponse, 1);
        initStars(layoutAttitude, 2);

        findViewById(R.id.btn_submit).setOnClickListener(v -> {
            if (professionalRating == 0 || responseRating == 0 || attitudeRating == 0) {
                Toast.makeText(this, "请完成所有评分", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "评价提交成功，感谢您的反馈！", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void initStars(LinearLayout layout, final int type) {
        for (int i = 0; i < 5; i++) {
            final int index = i;
            TextView star = new TextView(this);
            star.setText("★");
            star.setTextSize(28);
            star.setTextColor(Color.parseColor("#E0E0E0"));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = dp2px(4);
            star.setLayoutParams(params);
            star.setOnClickListener(v -> setRating(type, index + 1));
            layout.addView(star);
        }
    }

    private void setRating(int type, int rating) {
        LinearLayout layout;
        switch (type) {
            case 0:
                professionalRating = rating;
                layout = layoutProfessional;
                break;
            case 1:
                responseRating = rating;
                layout = layoutResponse;
                break;
            case 2:
                attitudeRating = rating;
                layout = layoutAttitude;
                break;
            default:
                return;
        }

        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(i < rating ? Color.parseColor("#FFD700") : Color.parseColor("#E0E0E0"));
            }
        }
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
