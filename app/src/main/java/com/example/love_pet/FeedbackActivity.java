package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class FeedbackActivity extends AppCompatActivity {

    private LinearLayout feedbackListContainer;
    private LinearLayout contactContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        feedbackListContainer = findViewById(R.id.feedback_list_container);
        contactContainer = findViewById(R.id.contact_container);

        initFeedbackItems();
        initContactItems();
    }

    private void initFeedbackItems() {
        List<FeedbackItem> items = new ArrayList<>();
        items.add(new FeedbackItem("闪退/卡顿", "页面闪退、卡顿", "crash"));
        items.add(new FeedbackItem("画质/音质", "图片、视频画质和音质问题", "quality"));
        items.add(new FeedbackItem("拍摄发布", "图片、视频发布问题", "publish"));
        items.add(new FeedbackItem("商城/支付", "商品加载、支付失败", "shop"));
        items.add(new FeedbackItem("发现/附近/搜索", "无法刷新、不显示", "discover"));
        items.add(new FeedbackItem("关注/私信/评论", "关注、发送接收私信", "follow"));
        items.add(new FeedbackItem("个人中心", "账号注销、资料不显示", "profile"));
        items.add(new FeedbackItem("宠物相关", "宠物领养、宠物定位等", "pet"));
        items.add(new FeedbackItem("其他", "点赞、收藏、分享", "other"));

        for (int i = 0; i < items.size(); i++) {
            FeedbackItem item = items.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_feedback_option, feedbackListContainer, false);

            TextView tvTitle = itemView.findViewById(R.id.tv_option_title);
            TextView tvDesc = itemView.findViewById(R.id.tv_option_desc);

            tvTitle.setText(item.title);
            tvDesc.setText(item.description);

            final String category = item.category;
            final String title = item.title;
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(FeedbackActivity.this, FeedbackDetailActivity.class);
                intent.putExtra(FeedbackDetailActivity.EXTRA_CATEGORY, category);
                intent.putExtra(FeedbackDetailActivity.EXTRA_TITLE, title);
                startActivity(intent);
            });

            feedbackListContainer.addView(itemView);

            if (i < items.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                params.leftMargin = dpToPx(20);
                divider.setLayoutParams(params);
                divider.setBackgroundColor(0xFFF0F0F0);
                feedbackListContainer.addView(divider);
            }
        }
    }

    private void initContactItems() {
        List<ContactItem> items = new ArrayList<>();
        items.add(new ContactItem("未成年交易专线", "95013088"));

        for (int i = 0; i < items.size(); i++) {
            ContactItem item = items.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_feedback_contact, contactContainer, false);

            TextView tvTitle = itemView.findViewById(R.id.tv_contact_title);
            TextView tvValue = itemView.findViewById(R.id.tv_contact_value);

            tvTitle.setText(item.title);
            tvValue.setText(item.value);

            itemView.setOnClickListener(v -> {
                Toast.makeText(this, "联系：" + item.value, Toast.LENGTH_SHORT).show();
            });

            contactContainer.addView(itemView);

            if (i < items.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                params.leftMargin = dpToPx(20);
                divider.setLayoutParams(params);
                divider.setBackgroundColor(0xFFF0F0F0);
                contactContainer.addView(divider);
            }
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private static class FeedbackItem {
        String title;
        String description;
        String category;

        FeedbackItem(String title, String description, String category) {
            this.title = title;
            this.description = description;
            this.category = category;
        }
    }

    private static class ContactItem {
        String title;
        String value;

        ContactItem(String title, String value) {
            this.title = title;
            this.value = value;
        }
    }
}
