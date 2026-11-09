package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wash);

        initViews();
    }

    private void initViews() {
        ImageView ivBackWash = findViewById(R.id.iv_back_wash);
        ivBackWash.setOnClickListener(v -> finish());

        View layoutLocation = findViewById(R.id.layout_location);
        layoutLocation.setOnClickListener(v -> openMap());

        View cardHomeCare = findViewById(R.id.card_home_care);
        cardHomeCare.setOnClickListener(v -> {
        });

        View cardNormalWash = findViewById(R.id.card_normal_wash);
        cardNormalWash.setOnClickListener(v -> {
        });

        View cardPremiumWash = findViewById(R.id.card_premium_wash);
        cardPremiumWash.setOnClickListener(v -> {
        });

        TextView btnContact1 = findViewById(R.id.btn_contact_1);
        btnContact1.setOnClickListener(v -> openContact("宜家宠物洗护", 0));

        TextView btnBook1 = findViewById(R.id.btn_book_1);
        btnBook1.setOnClickListener(v -> openBooking("宜家宠物洗护", "湛江市霞山8:30—20:00可接宠物洗护", 0));

        TextView btnContact2 = findViewById(R.id.btn_contact_2);
        btnContact2.setOnClickListener(v -> openContact("这里是家宠物店", 1));

        TextView btnBook2 = findViewById(R.id.btn_book_2);
        btnBook2.setOnClickListener(v -> openBooking("这里是家宠物店", "吴川市8:30—20:00可接受宠洗澡", 1));
    }

    private void openContact(String shopName, int shopType) {
        Intent intent = new Intent(this, WashShopContactActivity.class);
        intent.putExtra(WashShopContactActivity.EXTRA_SHOP_NAME, shopName);
        intent.putExtra(WashShopContactActivity.EXTRA_SHOP_TYPE, "洗护");
        startActivity(intent);
    }

    private void openBooking(String shopName, String shopAddress, int shopType) {
        Intent intent = new Intent(this, BookingActivity.class);
        intent.putExtra(BookingActivity.EXTRA_SHOP_NAME, shopName);
        intent.putExtra(BookingActivity.EXTRA_SHOP_ADDRESS, shopAddress);
        intent.putExtra(BookingActivity.EXTRA_SHOP_TYPE, shopType);
        startActivity(intent);
    }

    private void openMap() {
        Intent intent = new Intent(this, WashMapActivity.class);
        startActivity(intent);
    }
}