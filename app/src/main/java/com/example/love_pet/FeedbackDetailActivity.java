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

public class FeedbackDetailActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_TITLE = "title";

    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_detail);

        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title != null) {
            TextView tvTitle = findViewById(R.id.tv_title);
            tvTitle.setText(title);
        }

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        container = findViewById(R.id.container);

        buildPage(category);
    }

    private void buildPage(String category) {
        addSuggestionItem();
        addSectionTitle("请选择你遇到的问题");

        List<String> problems = getProblemList(category);
        addOptionList(problems);

        List<String> helpList = getHelpList(category);
        if (helpList != null && !helpList.isEmpty()) {
            addSectionTitle("你可能还需要");
            addOptionList(helpList);
        }
    }

    private void addSuggestionItem() {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_feedback_option, container, false);
        TextView tvTitle = itemView.findViewById(R.id.tv_option_title);
        TextView tvDesc = itemView.findViewById(R.id.tv_option_desc);

        tvTitle.setText("我要提建议");
        tvDesc.setVisibility(View.GONE);

        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(FeedbackDetailActivity.this, FeedbackSuggestionActivity.class);
            startActivity(intent);
        });

        container.addView(itemView);
        addDivider();
    }

    private void addSectionTitle(String title) {
        TextView tv = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dpToPx(20);
        params.bottomMargin = dpToPx(12);
        params.leftMargin = dpToPx(20);
        params.rightMargin = dpToPx(20);
        tv.setLayoutParams(params);
        tv.setText(title);
        tv.setTextColor(0xFF333333);
        tv.setTextSize(15);
        container.addView(tv);
    }

    private void addOptionList(List<String> options) {
        LinearLayout listLayout = new LinearLayout(this);
        listLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        listLayout.setBackgroundColor(0xFFFFFFFF);
        listLayout.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < options.size(); i++) {
            final String option = options.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_feedback_option, listLayout, false);
            TextView tvTitle = itemView.findViewById(R.id.tv_option_title);
            TextView tvDesc = itemView.findViewById(R.id.tv_option_desc);

            tvTitle.setText(option);
            tvDesc.setVisibility(View.GONE);

            itemView.setOnClickListener(v -> {
                if ("前往帮助与客服".equals(option)) {
                    Intent intent = new Intent(FeedbackDetailActivity.this, CustomerServiceActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(FeedbackDetailActivity.this, FeedbackSuggestionActivity.class);
                    startActivity(intent);
                }
            });

            listLayout.addView(itemView);

            if (i < options.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                params.leftMargin = dpToPx(20);
                divider.setLayoutParams(params);
                divider.setBackgroundColor(0xFFF0F0F0);
                listLayout.addView(divider);
            }
        }

        container.addView(listLayout);
    }

    private void addDivider() {
        View divider = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        params.leftMargin = dpToPx(20);
        divider.setLayoutParams(params);
        divider.setBackgroundColor(0xFFF0F0F0);
        container.addView(divider);
    }

    private List<String> getProblemList(String category) {
        List<String> list = new ArrayList<>();
        if (category == null) return list;

        switch (category) {
            case "crash":
                list.add("首页闪退");
                list.add("搜索时闪退");
                list.add("浏览笔记闪退");
                list.add("发布笔记闪退");
                list.add("首页卡顿");
                list.add("其他闪退/卡顿");
                break;
            case "quality":
                list.add("图片模糊");
                list.add("视频卡顿");
                list.add("声音异常");
                list.add("视频画质差");
                list.add("其他画质/音质问题");
                break;
            case "publish":
                list.add("审核时间过长");
                list.add("笔记无法搜到、别人看不到");
                list.add("笔记丢失");
                list.add("上传失败");
                list.add("相册页问题");
                list.add("图片编辑问题");
                list.add("视频编辑问题");
                list.add("草稿内容丢失");
                list.add("其他发布问题");
                break;
            case "shop":
                list.add("支付报错");
                list.add("订单加载失败");
                list.add("商品详情页加载失败");
                list.add("购物车加载失败");
                list.add("心愿单加载失败");
                list.add("订单记录消失");
                list.add("小红卡购买失败");
                list.add("商品无法加入购物车");
                list.add("其他商城问题");
                break;
            case "discover":
                list.add("发现页无法刷新");
                list.add("附近不显示");
                list.add("搜索无结果");
                list.add("其他发现/附近/搜索问题");
                break;
            case "follow":
                list.add("关注失败");
                list.add("私信发送失败");
                list.add("私信接收失败");
                list.add("评论发布失败");
                list.add("其他关注/私信/评论问题");
                break;
            case "profile":
                list.add("账号注销问题");
                list.add("资料不显示");
                list.add("资料修改失败");
                list.add("头像上传失败");
                list.add("其他个人中心问题");
                break;
            case "pet":
                list.add("宠物定位失败");
                list.add("宠物信息修改失败");
                list.add("领养申请问题");
                list.add("其他宠物相关问题");
                break;
            case "other":
                list.add("无法点赞");
                list.add("无法收藏");
                list.add("无法分享");
                list.add("其他问题");
                break;
            default:
                list.add("其他问题");
                break;
        }
        return list;
    }

    private List<String> getHelpList(String category) {
        List<String> list = new ArrayList<>();
        if (category == null) return list;

        switch (category) {
            case "publish":
                list.add("如何发布1分钟以上视频");
                list.add("我的笔记违规了怎么办");
                list.add("为什么搜不到我的笔记");
                list.add("前往帮助与客服");
                break;
            case "shop":
                list.add("如何取消小红卡自动续费");
                list.add("如何修改订单地址/电话");
                list.add("退换货政策");
                list.add("前往帮助与客服");
                break;
            case "other":
                list.add("如何取消小红卡自动续费");
                list.add("退换货政策");
                list.add("我的笔记违规了怎么办");
                list.add("前往帮助与客服");
                break;
            case "profile":
                list.add("如何修改个人资料");
                list.add("如何注销账号");
                list.add("前往帮助与客服");
                break;
            default:
                list.add("前往帮助与客服");
                break;
        }
        return list;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
