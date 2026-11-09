package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        UserManager.getInstance().loadUsers(this);

        if (UserManager.getInstance().isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
    }

    private void initViews() {
        TextView btnLogin = findViewById(R.id.btn_login);
        TextView tvRegister = findViewById(R.id.tv_register);
        TextView tvForgetPwd = findViewById(R.id.tv_forget_pwd);
        ImageView ivTogglePwd = findViewById(R.id.iv_toggle_pwd);
        EditText etPhone = findViewById(R.id.et_phone);
        EditText etPassword = findViewById(R.id.et_password);

        btnLogin.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (phone.isEmpty()) {
                Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = UserManager.getInstance().login(this, phone, password);
            if (success) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "手机号或密码错误", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        tvForgetPwd.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
        });

        TextView tvGuestLogin = findViewById(R.id.tv_guest_login);
        if (tvGuestLogin != null) {
            tvGuestLogin.setOnClickListener(v -> {
                boolean success = UserManager.getInstance().login(LoginActivity.this,
                        "13800000000", "123456");
                if (success) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "游客登录失败", Toast.LENGTH_SHORT).show();
                }
            });
        }

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
    }
}