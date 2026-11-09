package com.example.love_pet;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity基类，提供通用功能
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * 获取布局资源ID
     */
    protected abstract int getLayoutId();

    /**
     * 初始化视图
     */
    protected abstract void initViews();

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initViews();
    }

    /**
     * 显示Toast提示
     */
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示Toast提示（长时）
     */
    public void showToastLong(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 获取Context
     */
    public Context getContext() {
        return this;
    }
}
