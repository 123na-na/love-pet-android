package com.example.love_pet;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        initViews();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageView ivTogglePwd = findViewById(R.id.iv_toggle_pwd);
        EditText etPassword = findViewById(R.id.et_new_password);
        TextView btnGetSmsCode = findViewById(R.id.btn_get_sms_code);

        btnBack.setOnClickListener(v -> finish());

        ivTogglePwd.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePwd.setImageResource(R.drawable.ic_eye_open);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePwd.setImageResource(R.drawable.ic_eye_close);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        btnGetSmsCode.setOnClickListener(v -> {
            // 获取短信验证码逻辑
        });
    }
}