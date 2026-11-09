package com.example.love_pet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class DepartmentActivity extends AppCompatActivity {

    private LinearLayout layoutDeptList;

    public static class Department {
        public String name;
        public String desc;
        public String symptoms;
        public int color;
        public int avatarRes;
        public int iconRes;

        public Department(String name, String desc, String symptoms, int color, int avatarRes, int iconRes) {
            this.name = name;
            this.desc = desc;
            this.symptoms = symptoms;
            this.color = color;
            this.avatarRes = avatarRes;
            this.iconRes = iconRes;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_department);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutDeptList = findViewById(R.id.layout_dept_list);

        renderDepartments();
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    private List<Department> getDepartments() {
        List<Department> list = new ArrayList<>();
        list.add(new Department("内科", "消化系统、呼吸系统、心血管系统等内科疾病", "呕吐、拉稀、咳嗽、发烧、食欲不振", Color.parseColor("#5AC8FA"), R.drawable.doctor, R.drawable.internal_medicine));
        list.add(new Department("皮肤科", "皮肤感染、过敏、寄生虫等皮肤问题", "掉毛、瘙痒、红疹、皮屑、结痂", Color.parseColor("#FF9500"), R.drawable.doctor2, R.drawable.dermatology_2));
        list.add(new Department("外科", "外伤、骨折、肿瘤、手术等外科问题", "伤口、骨折、肿块、跛行", Color.parseColor("#34C759"), R.drawable.doctor3, R.drawable.surgery));
        list.add(new Department("牙科", "口腔疾病、牙齿问题", "口臭、牙结石、牙龈炎、牙齿松动", Color.parseColor("#AF52DE"), R.drawable.doctor4, R.drawable.dentistry));
        list.add(new Department("眼科", "眼部疾病、视力问题", "眼红、流泪、眼屎多、视力下降", Color.parseColor("#007AFF"), R.drawable.doctor5, R.drawable.ophthalmology));
        list.add(new Department("异宠专科", "兔子、龙猫、鹦鹉、爬宠等异宠医疗", "异宠各类健康问题", Color.parseColor("#FF6B35"), R.drawable.doctor2, R.drawable.icon_tomcat));
        list.add(new Department("急诊24h", "突发急症、夜间急诊", "中毒、难产、抽搐、大出血、尿闭", Color.parseColor("#FF3B30"), R.drawable.doctor3, R.drawable.emergency));
        return list;
    }

    private void renderDepartments() {
        List<Department> departments = getDepartments();
        for (int i = 0; i < departments.size(); i++) {
            View card = createDeptCard(departments.get(i));
            layoutDeptList.addView(card);
            if (i < departments.size() - 1) {
                View space = new View(this);
                LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp2px(10));
                layoutDeptList.addView(space, sp);
            }
        }
    }

    private View createDeptCard(final Department dept) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_input);
        card.setPadding(dp2px(16), dp2px(14), dp2px(16), dp2px(14));
        card.setOnClickListener(v -> {
            Intent intent = new Intent(DepartmentActivity.this, ConsultChatActivity.class);
            intent.putExtra("doctor_name", dept.name + "医生");
            intent.putExtra("department", dept.name);
            intent.putExtra("doctor_avatar", dept.avatarRes);
            startActivity(intent);
        });

        ImageView iconIv = new ImageView(this);
        iconIv.setImageResource(dept.iconRes);
        iconIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp2px(56), dp2px(56));
        card.addView(iconIv, iconParams);

        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.leftMargin = dp2px(12);
        infoParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        card.addView(infoLayout, infoParams);

        TextView nameTv = new TextView(this);
        nameTv.setText(dept.name);
        nameTv.setTextSize(16);
        nameTv.setTextColor(Color.parseColor("#333333"));
        nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
        infoLayout.addView(nameTv);

        TextView descTv = new TextView(this);
        descTv.setText(dept.desc);
        descTv.setTextSize(12);
        descTv.setTextColor(Color.parseColor("#999999"));
        descTv.setPadding(0, dp2px(4), 0, 0);
        infoLayout.addView(descTv);

        TextView symptomTv = new TextView(this);
        symptomTv.setText("常见症状：" + dept.symptoms);
        symptomTv.setTextSize(11);
        symptomTv.setTextColor(Color.parseColor("#BBBBBB"));
        symptomTv.setPadding(0, dp2px(4), 0, 0);
        symptomTv.setSingleLine();
        symptomTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        infoLayout.addView(symptomTv);

        ImageView arrowIv = new ImageView(this);
        arrowIv.setImageResource(R.drawable.ic_eye_close);
        arrowIv.setAlpha(0.3f);
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(dp2px(20), dp2px(20));
        arrowParams.gravity = android.view.Gravity.CENTER_VERTICAL;
        card.addView(arrowIv, arrowParams);

        return card;
    }
}
