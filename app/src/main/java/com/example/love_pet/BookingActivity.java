package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {

    public static final String EXTRA_SHOP_NAME = "shop_name";
    public static final String EXTRA_SHOP_ADDRESS = "shop_address";
    public static final String EXTRA_SHOP_TYPE = "shop_type";

    private String selectedType = "洗澡";
    private String selectedTypeName = "洗澡";
    private double selectedPrice = 38.00;
    private String selectedDate = "";
    private String selectedTime = "下午 13:00--20:00";

    private String shopName = "";
    private String shopAddress = "";

    private TextView tvTypeBath, tvTypeHair, tvTypeCut;
    private TextView[] dateViews = new TextView[7];
    private TextView tvTimeMorning, tvTimeAfternoon;

    private List<String> dateDisplayList = new ArrayList<>();
    private List<String> dateFullList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        ImageView ivBack = findViewById(R.id.iv_back_booking);
        ivBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        shopName = intent.getStringExtra(EXTRA_SHOP_NAME);
        shopAddress = intent.getStringExtra(EXTRA_SHOP_ADDRESS);
        int shopType = intent.getIntExtra(EXTRA_SHOP_TYPE, 0);

        TextView tvShopName = findViewById(R.id.tv_shop_name);
        TextView tvShopAddress = findViewById(R.id.tv_shop_address);
        ImageView ivImg1 = findViewById(R.id.iv_img1);
        ImageView ivImg2 = findViewById(R.id.iv_img2);
        ImageView ivImg3 = findViewById(R.id.iv_img3);
        ImageView ivImg4 = findViewById(R.id.iv_img4);

        if (shopName != null) {
            tvShopName.setText(shopName);
        } else {
            shopName = "宜家宠物洗护";
        }
        if (shopAddress != null) {
            tvShopAddress.setText(shopAddress);
        } else {
            shopAddress = "";
        }

        if (shopType == 1) {
            ivImg1.setImageResource(R.drawable.zheli1);
            ivImg2.setImageResource(R.drawable.zheli2);
            ivImg3.setImageResource(R.drawable.zheli3);
            ivImg4.setImageResource(R.drawable.zheli4);
        } else {
            ivImg1.setImageResource(R.drawable.yijia1);
            ivImg2.setImageResource(R.drawable.yijia2);
            ivImg3.setImageResource(R.drawable.yijia3);
            ivImg4.setImageResource(R.drawable.yijia4);
        }

        initTypeViews();
        initDateData();
        initDateViews();
        initTimeViews();

        TextView btnSubmit = findViewById(R.id.btn_booking_submit);
        btnSubmit.setOnClickListener(v -> submitBooking());
    }

    private void initDateData() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.CHINA);
        SimpleDateFormat weekFormat = new SimpleDateFormat("EEE", Locale.CHINA);
        SimpleDateFormat fullFormat = new SimpleDateFormat("M月d日 EEEE", Locale.CHINA);

        for (int i = 0; i < 7; i++) {
            dateDisplayList.add(weekFormat.format(calendar.getTime()) + "\n" + dayFormat.format(calendar.getTime()));
            dateFullList.add(fullFormat.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        selectedDate = dateFullList.get(0);
    }

    private void initTypeViews() {
        tvTypeBath = findViewById(R.id.tv_type_bath);
        tvTypeHair = findViewById(R.id.tv_type_hair);
        tvTypeCut = findViewById(R.id.tv_type_cut);

        tvTypeBath.setOnClickListener(v -> selectType(0));
        tvTypeHair.setOnClickListener(v -> selectType(1));
        tvTypeCut.setOnClickListener(v -> selectType(2));
    }

    private void selectType(int index) {
        tvTypeBath.setBackgroundResource(index == 0 ? R.drawable.bg_type_selected : R.drawable.bg_type_normal);
        tvTypeBath.setTypeface(index == 0 ? android.graphics.Typeface.DEFAULT_BOLD : android.graphics.Typeface.DEFAULT);
        tvTypeHair.setBackgroundResource(index == 1 ? R.drawable.bg_type_selected : R.drawable.bg_type_normal);
        tvTypeHair.setTypeface(index == 1 ? android.graphics.Typeface.DEFAULT_BOLD : android.graphics.Typeface.DEFAULT);
        tvTypeCut.setBackgroundResource(index == 2 ? R.drawable.bg_type_selected : R.drawable.bg_type_normal);
        tvTypeCut.setTypeface(index == 2 ? android.graphics.Typeface.DEFAULT_BOLD : android.graphics.Typeface.DEFAULT);

        switch (index) {
            case 0:
                selectedType = "洗澡 ¥38.00";
                selectedTypeName = "洗澡";
                selectedPrice = 38.00;
                break;
            case 1:
                selectedType = "毛发护理 ¥46.00";
                selectedTypeName = "毛发护理";
                selectedPrice = 46.00;
                break;
            case 2:
                selectedType = "剪发 ¥48.88";
                selectedTypeName = "剪发";
                selectedPrice = 48.88;
                break;
        }
    }

    private void initDateViews() {
        dateViews[0] = findViewById(R.id.tv_date_0);
        dateViews[1] = findViewById(R.id.tv_date_1);
        dateViews[2] = findViewById(R.id.tv_date_2);
        dateViews[3] = findViewById(R.id.tv_date_3);
        dateViews[4] = findViewById(R.id.tv_date_4);
        dateViews[5] = findViewById(R.id.tv_date_5);
        dateViews[6] = findViewById(R.id.tv_date_6);

        for (int i = 0; i < dateViews.length; i++) {
            dateViews[i].setText(dateDisplayList.get(i));
            final int index = i;
            dateViews[i].setOnClickListener(v -> selectDate(index));
        }

        selectDate(0);
    }

    private void selectDate(int index) {
        for (int i = 0; i < dateViews.length; i++) {
            dateViews[i].setBackgroundResource(i == index ? R.drawable.bg_date_selected : R.drawable.bg_date_normal);
            dateViews[i].setTypeface(i == index ? android.graphics.Typeface.DEFAULT_BOLD : android.graphics.Typeface.DEFAULT);
        }
        selectedDate = dateFullList.get(index);
    }

    private void initTimeViews() {
        tvTimeMorning = findViewById(R.id.tv_time_morning);
        tvTimeAfternoon = findViewById(R.id.tv_time_afternoon);

        tvTimeMorning.setOnClickListener(v -> selectTime(0));
        tvTimeAfternoon.setOnClickListener(v -> selectTime(1));
    }

    private void selectTime(int index) {
        tvTimeMorning.setBackgroundResource(index == 0 ? R.drawable.bg_time_selected : R.drawable.bg_time_normal);
        tvTimeMorning.setTypeface(index == 0 ? android.graphics.Typeface.DEFAULT_BOLD : android.graphics.Typeface.DEFAULT);
        tvTimeAfternoon.setBackgroundResource(index == 1 ? R.drawable.bg_time_selected : R.drawable.bg_time_normal);
        tvTimeAfternoon.setTypeface(index == 1 ? android.graphics.Typeface.DEFAULT_BOLD : android.graphics.Typeface.DEFAULT);

        selectedTime = index == 0 ? "早上 8:30--12:30" : "下午 13:00--20:00";
    }

    private void submitBooking() {
        String orderId = OrderManager.addServiceOrder(this, shopName, shopAddress,
                selectedTypeName, selectedDate, selectedTime, selectedPrice);

        String bookingTime = selectedDate + " " + selectedTime;
        int shopLogo = ShopActivity.Product.getShopAvatarRes(shopName);
        BookingMessageManager.BookingMessage msg = new BookingMessageManager.BookingMessage(
                shopName, selectedTypeName, bookingTime, "预约成功", shopLogo, orderId);
        BookingMessageManager.getInstance().addMessage(this, msg);

        Toast.makeText(this, "预约成功！", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(BookingActivity.this, OrderActivity.class);
        intent.putExtra("tab_index", 2); // 待使用（待收货/代使用）
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
