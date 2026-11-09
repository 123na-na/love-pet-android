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

public class ShopContactActivity extends AppCompatActivity {

    public static final String EXTRA_SHOP_NAME = "shop_name";
    public static final String EXTRA_SHOP_TYPE = "shop_type";

    private EditText etInput;
    private LinearLayout messageContainer;
    private ScrollView scrollViewMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_contact);

        Intent intent = getIntent();
        String shopName = intent.getStringExtra(EXTRA_SHOP_NAME);
        int shopType = intent.getIntExtra(EXTRA_SHOP_TYPE, 0);

        ImageView ivBack = findViewById(R.id.iv_back_contact);
        ivBack.setOnClickListener(v -> finish());

        TextView tvTitle = findViewById(R.id.tv_contact_title);
        ImageView ivShopAvatar = findViewById(R.id.iv_shop_avatar_contact);

        if (shopName != null) {
            tvTitle.setText(shopName);
        }

        etInput = findViewById(R.id.et_input_contact);
        messageContainer = findViewById(R.id.message_container_contact);
        scrollViewMessage = findViewById(R.id.scroll_message_contact);

        ImageView ivSend = findViewById(R.id.iv_send_contact);
        ivSend.setOnClickListener(v -> sendMessage());
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
        bubble.setBackgroundResource(R.drawable.bg_dynamic_card);
        int dp10 = (int) (10 * getResources().getDisplayMetrics().density);
        int dp6 = (int) (6 * getResources().getDisplayMetrics().density);
        bubble.setPadding(dp6, dp6, dp6, dp6);
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
