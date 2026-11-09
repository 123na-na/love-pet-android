package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
    }

    private void initViews() {
        TextView btnRegister = findViewById(R.id.btn_register);
        TextView tvLogin = findViewById(R.id.tv_login);
        ImageView ivTogglePwd = findViewById(R.id.iv_toggle_pwd);
        ImageView ivToggleConfirmPwd = findViewById(R.id.iv_toggle_confirm_pwd);
        EditText etPhone = findViewById(R.id.et_phone);
        EditText etPassword = findViewById(R.id.et_password);
        EditText etConfirmPassword = findViewById(R.id.et_confirm_password);

        btnRegister.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (phone.isEmpty()) {
                Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.length() != 11) {
                Toast.makeText(this, "请输入正确的手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            UserManager.getInstance().loadUsers(this);
            if (UserManager.getInstance().findUserByPhone(phone) != null) {
                Toast.makeText(this, "该手机号已注册", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, SetupProfileActivity.class);
            intent.putExtra(SetupProfileActivity.EXTRA_PHONE, phone);
            intent.putExtra(SetupProfileActivity.EXTRA_PASSWORD, password);
            startActivity(intent);
        });

        tvLogin.setOnClickListener(v -> finish());

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

        ivToggleConfirmPwd.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            if (isConfirmPasswordVisible) {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivToggleConfirmPwd.setImageResource(R.drawable.ic_eye_open);
            } else {
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivToggleConfirmPwd.setImageResource(R.drawable.ic_eye_close);
            }
            etConfirmPassword.setSelection(etConfirmPassword.getText().length());
        });
    }
}