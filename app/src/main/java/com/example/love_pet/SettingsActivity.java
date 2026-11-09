package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 返回按钮
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // 账号安全
        findViewById(R.id.layout_account_security).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        // 通用设置
        findViewById(R.id.layout_general_settings).setOnClickListener(v -> {
            showToast("通用设置");
        });

        // 通知设置
        findViewById(R.id.layout_notification_settings).setOnClickListener(v -> {
            showToast("通知设置");
        });

        // 隐私设置
        findViewById(R.id.layout_privacy_settings).setOnClickListener(v -> {
            showToast("隐私设置");
        });

        // 存储空间（原清除缓存）
        findViewById(R.id.layout_storage).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定要清除缓存吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        clearCache();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        // 字体大小调节
        findViewById(R.id.layout_font_size).setOnClickListener(v -> {
            showToast("字体大小调节");
        });

        // 收货地址
        findViewById(R.id.layout_address).setOnClickListener(v -> {
            showToast("收货地址");
        });

        // 帮助与客服
        findViewById(R.id.layout_help).setOnClickListener(v -> {
            showToast("帮助与客服");
        });

        // 意见反馈
        findViewById(R.id.layout_feedback).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });

        // 关于萌宠园
        findViewById(R.id.layout_about).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("关于萌宠园")
                    .setMessage("萌宠园 v1.0.0\n\n一款专注于宠物服务的综合应用平台，为您提供宠物领养、美容洗护、商城购物等一站式服务。")
                    .setPositiveButton("确定", null)
                    .show();
        });

        // 新功能体验
        findViewById(R.id.layout_new_features).setOnClickListener(v -> {
            showToast("新功能体验");
        });

        // 退出登录
        TextView btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("确定要退出登录吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        logout();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    /**
     * 显示Toast提示
     */
    private void showToast(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    /**
     * 清除缓存
     */
    private void clearCache() {
        try {
            // 清除应用缓存
            deleteDir(getCacheDir());
            if (getExternalCacheDir() != null) {
                deleteDir(getExternalCacheDir());
            }
            showToast("缓存已清除");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 递归删除目录
     */
    private boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new java.io.File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }
        return dir != null && dir.delete();
    }

    /**
     * 退出登录，返回登录页
     */
    private void logout() {
        UserManager.getInstance().logout(this);
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
