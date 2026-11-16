package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WashMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wash_map);

        ImageView ivBack = findViewById(R.id.iv_back_map);
        ivBack.setOnClickListener(v -> finish());

        TextView tvRefresh = findViewById(R.id.tv_refresh);
        tvRefresh.setOnClickListener(v -> {
            Toast.makeText(this, "正在刷新位置...", Toast.LENGTH_SHORT).show();
        });

        ImageView ivLocateMe = findViewById(R.id.iv_locate_me);
        ivLocateMe.setOnClickListener(v -> {
            Toast.makeText(this, "已定位到当前位置", Toast.LENGTH_SHORT).show();
        });

        LinearLayout layoutShop1 = findViewById(R.id.layout_shop1);
        layoutShop1.setOnClickListener(v -> highlightShop(0));

        LinearLayout layoutShop2 = findViewById(R.id.layout_shop2);
        layoutShop2.setOnClickListener(v -> highlightShop(1));

        TextView btnNav1 = findViewById(R.id.btn_nav_shop1);
        btnNav1.setOnClickListener(v -> {
            Toast.makeText(this, "正在打开导航...", Toast.LENGTH_SHORT).show();
        });

        TextView btnNav2 = findViewById(R.id.btn_nav_shop2);
        btnNav2.setOnClickListener(v -> {
            Toast.makeText(this, "正在打开导航...", Toast.LENGTH_SHORT).show();
        });

        TextView btnBook1 = findViewById(R.id.btn_book_shop1);
        btnBook1.setOnClickListener(v -> openBooking(0));

        TextView btnBook2 = findViewById(R.id.btn_book_shop2);
        btnBook2.setOnClickListener(v -> openBooking(1));
    }

    private void highlightShop(int index) {
        LinearLayout layoutShop1 = findViewById(R.id.layout_shop1);
        LinearLayout layoutShop2 = findViewById(R.id.layout_shop2);

        if (index == 0) {
            layoutShop1.setBackgroundResource(R.drawable.bg_shop_card_map);
            layoutShop2.setBackgroundResource(R.drawable.bg_white_stroke);
        } else {
            layoutShop1.setBackgroundResource(R.drawable.bg_white_stroke);
            layoutShop2.setBackgroundResource(R.drawable.bg_shop_card_map);
        }
    }

    private void openBooking(int shopType) {
        Intent intent = new Intent(this, BookingActivity.class);
        if (shopType == 0) {
            intent.putExtra(BookingActivity.EXTRA_SHOP_NAME, "宜家宠物洗护");
            intent.putExtra(BookingActivity.EXTRA_SHOP_ADDRESS, "湛江市霞山区解放西路88号");
        } else {
            intent.putExtra(BookingActivity.EXTRA_SHOP_NAME, "这里是家宠物店");
            intent.putExtra(BookingActivity.EXTRA_SHOP_ADDRESS, "吴川市梅菉街道文化路126号");
        }
        intent.putExtra(BookingActivity.EXTRA_SHOP_TYPE, shopType);
        startActivity(intent);
    }
}
