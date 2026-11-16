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

public class VideoConsultActivity extends AppCompatActivity {

    private LinearLayout layoutPackages;
    private int selectedPackage = 0;

    public static class VideoPackage {
        public String name;
        public String duration;
        public String price;
        public String originalPrice;
        public String desc;
        public String tag;
        public boolean isHot;

        public VideoPackage(String name, String duration, String price, String originalPrice, String desc, String tag, boolean isHot) {
            this.name = name;
            this.duration = duration;
            this.price = price;
            this.originalPrice = originalPrice;
            this.desc = desc;
            this.tag = tag;
            this.isHot = isHot;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_consult);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutPackages = findViewById(R.id.layout_packages);

        loadPackages();

        findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            List<VideoPackage> packages = getPackages();
            VideoPackage pkg = packages.get(selectedPackage);
            Intent intent = new Intent(this, VideoCallActivity.class);
            intent.putExtra(VideoCallActivity.EXTRA_DOCTOR_NAME, "视频问诊医师");
            intent.putExtra(VideoCallActivity.EXTRA_DOCTOR_AVATAR, R.drawable.doctor3);
            intent.putExtra(VideoCallActivity.EXTRA_PACKAGE_NAME, pkg.name + "(" + pkg.duration + ")");
            startActivity(intent);
        });
    }

    private List<VideoPackage> getPackages() {
        List<VideoPackage> list = new ArrayList<>();
        list.add(new VideoPackage("视频问诊", "15分钟", "49.9", "89.9", "面对面视频沟通，直观高效", "热门推荐", true));
        list.add(new VideoPackage("视频问诊", "30分钟", "89.9", "159.9", "深度问诊，复杂病情推荐", "", false));
        list.add(new VideoPackage("急诊加急", "不限时", "199.9", "299.9", "紧急情况优先接诊，24小时在线", "24h急诊", false));
        return list;
    }

    private void loadPackages() {
        List<VideoPackage> packages = getPackages();
        for (int i = 0; i < packages.size(); i++) {
            final int index = i;
            View item = createPackageItem(packages.get(i), i);
            item.setOnClickListener(v -> {
                selectedPackage = index;
                updatePackageSelection();
            });
            layoutPackages.addView(item);
        }
    }

    private View createPackageItem(VideoPackage pkg, int index) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setBackgroundColor(Color.WHITE);
        item.setPadding(dp2px(16), dp2px(16), dp2px(16), dp2px(16));
        item.setTag(index);

        if (index > 0) {
            View divider = new View(this);
            divider.setBackgroundColor(Color.parseColor("#F0F0F0"));
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp2px(1));
            item.addView(divider, dividerParams);
        }

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        topRow.setPadding(0, dp2px(12), 0, 0);
        item.addView(topRow);

        TextView nameTv = new TextView(this);
        nameTv.setText(pkg.name);
        nameTv.setTextSize(16);
        nameTv.setTextColor(Color.parseColor("#333333"));
        nameTv.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        topRow.addView(nameTv, nameParams);

        if (pkg.tag != null && !pkg.tag.isEmpty()) {
            TextView tagTv = new TextView(this);
            tagTv.setText(pkg.tag);
            tagTv.setTextSize(11);
            tagTv.setTextColor(pkg.isHot ? Color.WHITE : Color.parseColor("#FF6B35"));
            tagTv.setBackgroundColor(pkg.isHot ? Color.parseColor("#FF6B35") : Color.parseColor("#FFF0E6"));
            tagTv.setPadding(dp2px(8), dp2px(3), dp2px(8), dp2px(3));
            topRow.addView(tagTv);
        }

        TextView durationTv = new TextView(this);
        durationTv.setText("时长：" + pkg.duration);
        durationTv.setTextSize(13);
        durationTv.setTextColor(Color.parseColor("#666666"));
        durationTv.setPadding(0, dp2px(8), 0, 0);
        item.addView(durationTv);

        TextView descTv = new TextView(this);
        descTv.setText(pkg.desc);
        descTv.setTextSize(13);
        descTv.setTextColor(Color.parseColor("#999999"));
        descTv.setPadding(0, dp2px(4), 0, 0);
        item.addView(descTv);

        LinearLayout priceRow = new LinearLayout(this);
        priceRow.setOrientation(LinearLayout.HORIZONTAL);
        priceRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        priceRow.setPadding(0, dp2px(12), 0, 0);
        item.addView(priceRow);

        TextView priceTv = new TextView(this);
        priceTv.setText("¥" + pkg.price);
        priceTv.setTextSize(20);
        priceTv.setTextColor(Color.parseColor("#FF6B35"));
        priceTv.setTypeface(null, android.graphics.Typeface.BOLD);
        priceRow.addView(priceTv);

        TextView originalPriceTv = new TextView(this);
        originalPriceTv.setText("¥" + pkg.originalPrice);
        originalPriceTv.setTextSize(13);
        originalPriceTv.setTextColor(Color.parseColor("#CCCCCC"));
        originalPriceTv.setPaintFlags(originalPriceTv.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        originalPriceTv.setPadding(dp2px(8), 0, 0, 0);
        priceRow.addView(originalPriceTv);

        return item;
    }

    private void updatePackageSelection() {
        for (int i = 0; i < layoutPackages.getChildCount(); i++) {
            View child = layoutPackages.getChildAt(i);
            if (i == selectedPackage) {
                child.setBackgroundColor(Color.parseColor("#FFF9F5"));
            } else {
                child.setBackgroundColor(Color.WHITE);
            }
        }
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
