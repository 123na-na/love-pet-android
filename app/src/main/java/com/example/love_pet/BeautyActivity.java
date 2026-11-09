package com.example.love_pet;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class BeautyActivity extends AppCompatActivity {

    private LinearLayout llPackageList;
    private LinearLayout llDots;
    private ViewFlipper viewFlipper;
    private int currentTab = 0;
    private int currentBanner = 0;
    private final int BANNER_COUNT = 3;
    private Handler handler = new Handler();
    private Runnable bannerRunnable;

    public static class PackageItem {
        public String name;
        public String shopName;
        public int price;
        public float rating;
        public String availableTime;
        public int imageRes;

        public PackageItem(String name, String shopName, int price, float rating, String availableTime, int imageRes) {
            this.name = name;
            this.shopName = shopName;
            this.price = price;
            this.rating = rating;
            this.availableTime = availableTime;
            this.imageRes = imageRes;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty);

        llPackageList = findViewById(R.id.ll_package_list);
        llDots = findViewById(R.id.ll_dots);
        viewFlipper = findViewById(R.id.view_flipper);

        initDots();
        startBannerAutoPlay();
        initTabs();
        renderPackages(0);
    }

    private void initDots() {
        llDots.removeAllViews();
        for (int i = 0; i < BANNER_COUNT; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(i == 0 ? R.drawable.dot_selected : R.drawable.dot_unselected);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) params.leftMargin = dp2px(6);
            dot.setLayoutParams(params);
            llDots.addView(dot);
        }
    }

    private void updateDots(int index) {
        for (int i = 0; i < llDots.getChildCount(); i++) {
            ImageView dot = (ImageView) llDots.getChildAt(i);
            dot.setImageResource(i == index ? R.drawable.dot_selected : R.drawable.dot_unselected);
        }
    }

    private void startBannerAutoPlay() {
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                currentBanner = (currentBanner + 1) % BANNER_COUNT;
                viewFlipper.showNext();
                updateDots(currentBanner);
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(bannerRunnable, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bannerRunnable == null) {
            startBannerAutoPlay();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bannerRunnable != null) {
            handler.removeCallbacks(bannerRunnable);
            bannerRunnable = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bannerRunnable != null) {
            handler.removeCallbacks(bannerRunnable);
            bannerRunnable = null;
        }
    }

    private void initTabs() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        findViewById(R.id.tab_zaoxing).setOnClickListener(v -> switchTab(0));
        findViewById(R.id.tab_spa).setOnClickListener(v -> switchTab(1));
        findViewById(R.id.tab_chuangyi).setOnClickListener(v -> switchTab(2));
        findViewById(R.id.tab_huli).setOnClickListener(v -> switchTab(3));
    }

    private void switchTab(int index) {
        currentTab = index;
        renderPackages(index);
    }

    private List<PackageItem> getPackages(int category) {
        List<PackageItem> list = new ArrayList<>();
        if (category == 0) {
            list.add(new PackageItem("泰迪精致造型修剪", "宜家宠物洗护", 128, 4.9f, "9:00—21:00", R.drawable.yijia1));
            list.add(new PackageItem("金毛全身造型修剪套餐", "这里是家宠物店", 168, 4.7f, "8:30—20:30", R.drawable.yijia2));
            list.add(new PackageItem("比熊圆头造型设计", "萌宠美容会所", 158, 4.8f, "9:00—20:00", R.drawable.yijia3));
        } else if (category == 1) {
            list.add(new PackageItem("深层滋养毛发SPA套餐", "宜家宠物洗护", 198, 4.9f, "9:00—21:00", R.drawable.yijia2));
            list.add(new PackageItem("精油SPA+毛发护理", "这里是家宠物店", 228, 4.6f, "8:30—20:30", R.drawable.yijia3));
            list.add(new PackageItem("药浴SPA+去味护理", "萌宠美容会所", 188, 4.7f, "9:00—20:00", R.drawable.yijia4));
        } else if (category == 2) {
            list.add(new PackageItem("创意染色装扮套餐", "萌宠美容会所", 268, 4.8f, "9:00—20:00", R.drawable.yijia3));
            list.add(new PackageItem("节日主题造型装扮", "宜家宠物洗护", 298, 4.9f, "9:00—21:00", R.drawable.yijia4));
            list.add(new PackageItem("萌系创意装扮", "这里是家宠物店", 238, 4.6f, "8:30—20:30", R.drawable.yijia1));
        } else {
            list.add(new PackageItem("精致护理全套", "宜家宠物洗护", 168, 4.9f, "9:00—21:00", R.drawable.yijia4));
            list.add(new PackageItem("足部护理+指甲修剪", "这里是家宠物店", 88, 4.7f, "8:30—20:30", R.drawable.yijia1));
            list.add(new PackageItem("耳道清洁+牙齿护理", "萌宠美容会所", 98, 4.8f, "9:00—20:00", R.drawable.yijia2));
        }
        return list;
    }

    private void renderPackages(int category) {
        llPackageList.removeAllViews();
        List<PackageItem> items = getPackages(category);

        for (int i = 0; i < items.size(); i++) {
            View card = createPackageCard(items.get(i));
            llPackageList.addView(card);
            if (i < items.size() - 1) {
                View space = new View(this);
                LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp2px(16));
                llPackageList.addView(space, sp);
            }
        }
    }

    private View createPackageCard(final PackageItem item) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_beauty_package, null);

        ImageView imageView = card.findViewById(R.id.iv_package_image);
        imageView.setImageResource(item.imageRes);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        TextView nameTv = card.findViewById(R.id.tv_package_name);
        nameTv.setText(item.name);

        TextView shopTv = card.findViewById(R.id.tv_shop_name);
        shopTv.setText(item.shopName);

        TextView ratingTv = card.findViewById(R.id.tv_rating);
        ratingTv.setText(String.valueOf(item.rating));

        TextView timeTv = card.findViewById(R.id.tv_available_time);
        timeTv.setText("可使用 " + item.availableTime);

        TextView priceTv = card.findViewById(R.id.tv_price);
        priceTv.setText("¥" + item.price);

        TextView btnContact = card.findViewById(R.id.btn_contact);
        btnContact.setOnClickListener(v -> {
            Intent intent = new Intent(BeautyActivity.this, WashShopContactActivity.class);
            intent.putExtra(WashShopContactActivity.EXTRA_SHOP_NAME, item.shopName);
            intent.putExtra(WashShopContactActivity.EXTRA_SHOP_TYPE, "美容");
            startActivity(intent);
        });

        TextView btnBook = card.findViewById(R.id.btn_book);
        btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(BeautyActivity.this, BookingActivity.class);
            intent.putExtra(BookingActivity.EXTRA_SHOP_NAME, item.shopName);
            intent.putExtra("package_name", item.name);
            intent.putExtra("package_price", item.price);
            startActivity(intent);
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        card.setLayoutParams(lp);

        return card;
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
