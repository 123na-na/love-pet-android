package com.example.love_pet;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 宠物公益活动页面（公益专区）
 */
public class WelfareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welfare);

        initViews();
    }

    private void initViews() {
        // 返回按钮
        ImageView ivBack = findViewById(R.id.iv_back);
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> finish());
        }

        // 投喂按钮
        View btnFeed = findViewById(R.id.btn_feed);
        if (btnFeed != null) {
            btnFeed.setOnClickListener(v -> onFeedClicked());
        }
    }

    /**
     * 投喂按钮点击事件
     * TODO: 根据实际业务需求实现投喂逻辑
     */
    private void onFeedClicked() {
        // 示例：显示提示
        Toast.makeText(this, "感谢您的爱心！投喂功能开发中...", Toast.LENGTH_SHORT).show();

        // TODO: 实际功能可包括：
        // 1. 跳转到投喂详情页
        // 2. 弹出选择投喂金额/类型
        // 3. 调用接口记录投喂行为
        // 4. 显示投喂成功动画/提示
    }
}
