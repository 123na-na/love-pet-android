package com.example.love_pet;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class NursingPlanActivity extends AppCompatActivity {

    private LinearLayout layoutCategories;
    private LinearLayout layoutPlans;

    public static class NursingCategory {
        public String name;
        public int icon;

        public NursingCategory(String name, int icon) {
            this.name = name;
            this.icon = icon;
        }
    }

    public static class NursingPlan {
        public String title;
        public String category;
        public String desc;
        public String duration;
        public String difficulty;
        public List<String> steps;
        public List<String> notes;

        public NursingPlan(String title, String category, String desc, String duration, String difficulty) {
            this.title = title;
            this.category = category;
            this.desc = desc;
            this.duration = duration;
            this.difficulty = difficulty;
            this.steps = new ArrayList<>();
            this.notes = new ArrayList<>();
        }
    }

    private int selectedCategory = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nursing_plan);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutCategories = findViewById(R.id.layout_categories);
        layoutPlans = findViewById(R.id.layout_plans);

        loadCategories();
        loadPlans();
    }

    private List<NursingCategory> getCategories() {
        List<NursingCategory> list = new ArrayList<>();
        list.add(new NursingCategory("全部", 0));
        list.add(new NursingCategory("术后护理", 0));
        list.add(new NursingCategory("皮肤护理", 0));
        list.add(new NursingCategory("肠胃护理", 0));
        list.add(new NursingCategory("口腔护理", 0));
        list.add(new NursingCategory("日常保健", 0));
        return list;
    }

    private List<NursingPlan> getPlans() {
        List<NursingPlan> list = new ArrayList<>();

        NursingPlan p1 = new NursingPlan("猫咪绝育术后护理", "术后护理", "猫咪绝育手术后7天居家护理方案，帮助猫咪快速恢复", "7天", "中等");
        p1.steps.add("术后2小时内不要喂食喂水，保持安静环境");
        p1.steps.add("术后6小时可少量饮水，12小时后可少量喂食");
        p1.steps.add("每天检查伤口是否有红肿、渗液");
        p1.steps.add("佩戴伊丽莎白圈，防止舔舐伤口");
        p1.steps.add("按医嘱按时服用消炎药和止痛药");
        p1.notes.add("术后一周内不要洗澡");
        p1.notes.add("如出现精神萎靡、食欲不振请及时就医");
        list.add(p1);

        NursingPlan p2 = new NursingPlan("犬猫皮肤病护理", "皮肤护理", "真菌/细菌性皮肤病居家护理指南", "14天", "简单");
        p2.steps.add("每天用温水清洁患处，保持皮肤干燥");
        p2.steps.add("按医嘱涂抹外用药膏，涂抹后按摩2分钟");
        p2.steps.add("佩戴伊丽莎白圈，防止抓挠舔舐");
        p2.steps.add("定期药浴，每周2-3次");
        p2.steps.add("补充维生素B，促进皮肤修复");
        p2.notes.add("保持居住环境清洁干燥");
        p2.notes.add("如症状加重请及时复诊");
        list.add(p2);

        NursingPlan p3 = new NursingPlan("急性肠胃炎护理", "肠胃护理", "呕吐腹泻后的肠胃调理方案", "3-5天", "简单");
        p3.steps.add("断食12-24小时，期间可少量多次饮水");
        p3.steps.add("断食结束后喂食易消化的流食（如泡软的猫粮）");
        p3.steps.add("少量多餐，每天喂食4-6次");
        p3.steps.add("按医嘱服用益生菌和肠胃药");
        p3.steps.add("观察呕吐腹泻次数和精神状态");
        p3.notes.add("切勿自行使用人用止泻药");
        p3.notes.add("如出现脱水症状请立即就医");
        list.add(p3);

        NursingPlan p4 = new NursingPlan("宠物口腔清洁护理", "口腔护理", "日常口腔清洁，预防牙结石和口臭", "长期坚持", "简单");
        p4.steps.add("每天用宠物专用牙刷刷牙");
        p4.steps.add("使用宠物专用牙膏，切勿使用人用牙膏");
        p4.steps.add("配合洁齿零食和漱口水");
        p4.steps.add("定期检查口腔健康状况");
        p4.steps.add("每年做一次专业洁牙");
        p4.notes.add("刷牙从幼犬/幼猫开始训练");
        p4.notes.add("循序渐进，让宠物逐渐适应");
        list.add(p4);

        NursingPlan p5 = new NursingPlan("老年犬猫日常保健", "日常保健", "7岁以上老年宠物日常护理方案", "长期坚持", "中等");
        p5.steps.add("定期体检，每半年一次全面检查");
        p5.steps.add("选择老年宠物专用粮，控制体重");
        p5.steps.add("适量运动，避免剧烈运动");
        p5.steps.add("注意关节保护，补充软骨素");
        p5.steps.add("保持作息规律，减少环境变化");
        p5.notes.add("密切关注饮水量和排尿情况");
        p5.notes.add("如出现行为异常请及时就医");
        list.add(p5);

        NursingPlan p6 = new NursingPlan("骨折术后康复护理", "术后护理", "骨折手术后的康复训练和护理", "30天", "困难");
        p6.steps.add("术后前两周限制活动，笼养休息");
        p6.steps.add("两周后逐渐增加活动量");
        p6.steps.add("按医嘱进行康复训练");
        p6.steps.add("补充钙质和关节营养剂");
        p6.steps.add("定期复查X光片，观察愈合情况");
        p6.notes.add("切勿过早剧烈运动");
        p6.notes.add("康复训练需在医生指导下进行");
        list.add(p6);

        return list;
    }

    private void loadCategories() {
        List<NursingCategory> categories = getCategories();
        for (int i = 0; i < categories.size(); i++) {
            final int index = i;
            TextView tv = new TextView(this);
            tv.setText(categories.get(i).name);
            tv.setTextSize(13);
            tv.setPadding(dp2px(16), dp2px(8), dp2px(16), dp2px(8));
            tv.setOnClickListener(v -> {
                selectedCategory = index;
                updateCategoryStyle();
                loadPlans();
            });
            layoutCategories.addView(tv);
        }
        updateCategoryStyle();
    }

    private void updateCategoryStyle() {
        List<NursingCategory> categories = getCategories();
        for (int i = 0; i < layoutCategories.getChildCount(); i++) {
            TextView tv = (TextView) layoutCategories.getChildAt(i);
            if (i == selectedCategory) {
                tv.setTextColor(Color.parseColor("#FF6B35"));
                tv.setBackgroundColor(Color.parseColor("#FFF0E6"));
            } else {
                tv.setTextColor(Color.parseColor("#666666"));
                tv.setBackgroundColor(Color.parseColor("#F5F5F5"));
            }
        }
    }

    private void loadPlans() {
        layoutPlans.removeAllViews();
        List<NursingPlan> plans = getPlans();
        List<NursingCategory> categories = getCategories();
        String catName = categories.get(selectedCategory).name;

        for (NursingPlan plan : plans) {
            if (!"全部".equals(catName) && !plan.category.equals(catName)) continue;
            View item = createPlanItem(plan);
            layoutPlans.addView(item);
        }
    }

    private View createPlanItem(final NursingPlan plan) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setBackgroundColor(Color.WHITE);
        item.setPadding(dp2px(16), dp2px(16), dp2px(16), dp2px(16));
        item.setOnClickListener(v -> showPlanDetail(plan));

        TextView titleTv = new TextView(this);
        titleTv.setText(plan.title);
        titleTv.setTextSize(15);
        titleTv.setTextColor(Color.parseColor("#333333"));
        titleTv.setTypeface(null, android.graphics.Typeface.BOLD);
        item.addView(titleTv);

        LinearLayout tagRow = new LinearLayout(this);
        tagRow.setOrientation(LinearLayout.HORIZONTAL);
        tagRow.setPadding(0, dp2px(8), 0, 0);
        item.addView(tagRow);

        TextView catTv = new TextView(this);
        catTv.setText(plan.category);
        catTv.setTextSize(11);
        catTv.setTextColor(Color.parseColor("#FF6B35"));
        catTv.setBackgroundColor(Color.parseColor("#FFF0E6"));
        catTv.setPadding(dp2px(8), dp2px(3), dp2px(8), dp2px(3));
        tagRow.addView(catTv);

        TextView durationTv = new TextView(this);
        durationTv.setText("周期：" + plan.duration);
        durationTv.setTextSize(11);
        durationTv.setTextColor(Color.parseColor("#666666"));
        durationTv.setPadding(dp2px(10), 0, 0, 0);
        tagRow.addView(durationTv);

        TextView diffTv = new TextView(this);
        diffTv.setText("难度：" + plan.difficulty);
        diffTv.setTextSize(11);
        diffTv.setTextColor(Color.parseColor("#666666"));
        diffTv.setPadding(dp2px(10), 0, 0, 0);
        tagRow.addView(diffTv);

        TextView descTv = new TextView(this);
        descTv.setText(plan.desc);
        descTv.setTextSize(13);
        descTv.setTextColor(Color.parseColor("#999999"));
        descTv.setPadding(0, dp2px(8), 0, 0);
        descTv.setMaxLines(2);
        descTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        item.addView(descTv);

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#F0F0F0"));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp2px(1));
        dividerParams.topMargin = dp2px(16);
        item.addView(divider, dividerParams);

        return item;
    }

    private void showPlanDetail(NursingPlan plan) {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp2px(20), dp2px(20), dp2px(20), dp2px(20));
        scrollView.addView(layout);

        TextView titleTv = new TextView(this);
        titleTv.setText(plan.title);
        titleTv.setTextSize(18);
        titleTv.setTextColor(Color.parseColor("#333333"));
        titleTv.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(titleTv);

        LinearLayout tagRow = new LinearLayout(this);
        tagRow.setOrientation(LinearLayout.HORIZONTAL);
        tagRow.setPadding(0, dp2px(12), 0, 0);
        layout.addView(tagRow);

        TextView catTv = new TextView(this);
        catTv.setText(plan.category);
        catTv.setTextSize(12);
        catTv.setTextColor(Color.parseColor("#FF6B35"));
        catTv.setBackgroundColor(Color.parseColor("#FFF0E6"));
        catTv.setPadding(dp2px(10), dp2px(4), dp2px(10), dp2px(4));
        tagRow.addView(catTv);

        TextView stepTitle = new TextView(this);
        stepTitle.setText("护理步骤");
        stepTitle.setTextSize(15);
        stepTitle.setTextColor(Color.parseColor("#333333"));
        stepTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        stepTitle.setPadding(0, dp2px(20), 0, 0);
        layout.addView(stepTitle);

        for (int i = 0; i < plan.steps.size(); i++) {
            TextView stepTv = new TextView(this);
            stepTv.setText((i + 1) + ". " + plan.steps.get(i));
            stepTv.setTextSize(14);
            stepTv.setTextColor(Color.parseColor("#666666"));
            stepTv.setLineSpacing(dp2px(4), 1);
            stepTv.setPadding(0, dp2px(10), 0, 0);
            layout.addView(stepTv);
        }

        TextView noteTitle = new TextView(this);
        noteTitle.setText("注意事项");
        noteTitle.setTextSize(15);
        noteTitle.setTextColor(Color.parseColor("#333333"));
        noteTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        noteTitle.setPadding(0, dp2px(20), 0, 0);
        layout.addView(noteTitle);

        for (String note : plan.notes) {
            TextView noteTv = new TextView(this);
            noteTv.setText("• " + note);
            noteTv.setTextSize(14);
            noteTv.setTextColor(Color.parseColor("#FF6B35"));
            noteTv.setPadding(0, dp2px(8), 0, 0);
            layout.addView(noteTv);
        }

        new AlertDialog.Builder(this)
                .setView(scrollView)
                .setPositiveButton("知道了", null)
                .show();
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
