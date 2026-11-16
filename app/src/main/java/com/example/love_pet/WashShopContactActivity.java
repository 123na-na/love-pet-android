package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WashShopContactActivity extends AppCompatActivity {

    public static final String EXTRA_SHOP_NAME = "shop_name";
    public static final String EXTRA_SHOP_TYPE = "shop_type";

    private EditText etInput;
    private LinearLayout messageContainer;
    private ScrollView scrollViewMessage;
    private String shopName;
    private String shopType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wash_shop_contact);

        Intent intent = getIntent();
        shopName = intent.getStringExtra(EXTRA_SHOP_NAME);
        shopType = intent.getStringExtra(EXTRA_SHOP_TYPE);

        ImageView ivBack = findViewById(R.id.iv_back_wash_contact);
        ivBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tv_wash_contact_title);
        TextView tvType = findViewById(R.id.tv_wash_contact_type);
        ImageView ivShopAvatar = findViewById(R.id.iv_wash_shop_avatar);

        if (shopName != null) {
            tvTitle.setText(shopName);
        }
        if (shopType != null) {
            tvType.setText(shopType + "商家");
        }

        etInput = findViewById(R.id.et_input_wash_contact);
        messageContainer = findViewById(R.id.message_container_wash_contact);
        scrollViewMessage = findViewById(R.id.scroll_message_wash_contact);

        ImageView ivSend = findViewById(R.id.iv_send_wash_contact);
        ivSend.setOnClickListener(v -> sendMessage());

        LinearLayout layoutQuick1 = findViewById(R.id.layout_quick_1);
        LinearLayout layoutQuick2 = findViewById(R.id.layout_quick_2);
        LinearLayout layoutQuick3 = findViewById(R.id.layout_quick_3);
        LinearLayout layoutQuick4 = findViewById(R.id.layout_quick_4);

        layoutQuick1.setOnClickListener(v -> sendQuickMessage("请问现在可以预约洗澡吗？"));
        layoutQuick2.setOnClickListener(v -> sendQuickMessage("价格是多少呀？"));
        layoutQuick3.setOnClickListener(v -> sendQuickMessage("营业时间是什么时候？"));
        layoutQuick4.setOnClickListener(v -> sendQuickMessage("可以上门接送吗？"));

        scrollViewMessage.postDelayed(() -> {
            addShopMessage("您好！欢迎光临" + shopName + "，请问有什么可以帮您的？");
        }, 300);
    }

    private void sendQuickMessage(String text) {
        addUserMessage(text);
        scrollViewMessage.postDelayed(() -> {
            String reply;
            if (text.contains("预约")) {
                reply = "可以的呢！您可以点击下方的「立即预约」按钮进行预约，或者告诉我您的宠物品种和时间，我帮您安排~";
            } else if (text.contains("价格")) {
                reply = "我们家有多种套餐哦~基础普洗是68元起，精洗是128元起，具体要看宠物品种和体重呢！";
            } else if (text.contains("营业时间")) {
                reply = "我们的营业时间是每天9:00-21:00，节假日不休息哦~";
            } else if (text.contains("上门") || text.contains("接送")) {
                reply = "可以的呢！我们提供3公里内免费接送服务，超出范围会收取少量费用，您方便告诉我地址吗？";
            } else {
                reply = "好的，收到您的消息了，我们会尽快回复您~";
            }
            addShopMessage(reply);
        }, 500);
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        addUserMessage(text);
        etInput.setText("");

        scrollViewMessage.postDelayed(() -> {
            addShopMessage("好的，收到您的消息了，我们会尽快回复您~");
        }, 500);
    }

    private void addUserMessage(String text) {
        LinearLayout msgLayout = new LinearLayout(this);
        msgLayout.setOrientation(LinearLayout.HORIZONTAL);
        msgLayout.setGravity(android.view.Gravity.END);

        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextSize(14);
        bubble.setTextColor(0xFFFFFFFF);
        bubble.setBackgroundResource(R.drawable.bg_user_bubble);
        int dp6 = (int) (6 * getResources().getDisplayMetrics().density);
        int dp8 = (int) (8 * getResources().getDisplayMetrics().density);
        bubble.setPadding(dp6, dp6, dp6, dp6);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        bubble.setMaxWidth((int) (screenWidth * 0.7f));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bubble.setLayoutParams(params);
        msgLayout.addView(bubble);

        ImageView avatar = new ImageView(this);
        int sizeDp36 = (int) (36 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(sizeDp36, sizeDp36);
        avatarParams.setMarginStart(dp8);
        avatar.setLayoutParams(avatarParams);

        avatar.setClipToOutline(true);
        avatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });

        AvatarManager.loadAvatar(this, avatar);
        msgLayout.addView(avatar);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(0, 0, 0, dp6);
        messageContainer.addView(msgLayout, containerParams);
        scrollToBottom();
    }

    private void addShopMessage(String text) {
        LinearLayout msgLayout = new LinearLayout(this);
        msgLayout.setOrientation(LinearLayout.HORIZONTAL);
        msgLayout.setGravity(android.view.Gravity.START);

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(R.drawable.circle_11);
        int sizeDp44 = (int) (44 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(sizeDp44, sizeDp44);
        avatar.setLayoutParams(avatarParams);
        msgLayout.addView(avatar);

        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextSize(14);
        bubble.setTextColor(0xFF333333);
        bubble.setBackgroundResource(R.drawable.bg_shop_bubble_wash);
        int dp10 = (int) (10 * getResources().getDisplayMetrics().density);
        int dp6 = (int) (6 * getResources().getDisplayMetrics().density);
        bubble.setPadding(dp10, dp6, dp10, dp6);
        bubble.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.7f));
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bubbleParams.setMarginStart(dp10);
        bubble.setLayoutParams(bubbleParams);
        msgLayout.addView(bubble);

        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(0, 0, 0, dp6);
        messageContainer.addView(msgLayout, containerParams);
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollViewMessage.post(() -> scrollViewMessage.fullScroll(View.FOCUS_DOWN));
    }
}
