package com.example.love_pet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentPasswordActivity extends AppCompatActivity {

    public static final String MODE_SET = "set";
    public static final String MODE_VERIFY = "verify";
    public static final String EXTRA_MODE = "mode";
    private static final String PREF_NAME = "payment";

    private String mode;
    private StringBuilder currentPassword = new StringBuilder();
    private String firstPassword = null;

    private TextView tvTitle;
    private TextView tvHint;
    private TextView tvError;
    private View[] dotViews = new View[6];

    private static String getPrefKey(Context context) {
        return PREF_NAME + "_" + UserManager.getInstance().getCurrentUserId();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_password);

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) {
            mode = MODE_SET;
        }

        tvTitle = findViewById(R.id.tv_title);
        tvHint = findViewById(R.id.tv_hint);
        tvError = findViewById(R.id.tv_error);

        dotViews[0] = findViewById(R.id.dot1);
        dotViews[1] = findViewById(R.id.dot2);
        dotViews[2] = findViewById(R.id.dot3);
        dotViews[3] = findViewById(R.id.dot4);
        dotViews[4] = findViewById(R.id.dot5);
        dotViews[5] = findViewById(R.id.dot6);

        if (MODE_SET.equals(mode)) {
            tvTitle.setText("设置支付密码");
            tvHint.setText("请输入6位支付密码");
        } else {
            tvTitle.setText("验证支付密码");
            tvHint.setText("请输入6位支付密码");
        }

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        setupKeyboard();
    }

    private void setupKeyboard() {
        int[] keyIds = {
                R.id.key_0, R.id.key_1, R.id.key_2, R.id.key_3, R.id.key_4,
                R.id.key_5, R.id.key_6, R.id.key_7, R.id.key_8, R.id.key_9
        };
        String[] keyValues = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

        for (int i = 0; i < keyIds.length; i++) {
            final String value = keyValues[i];
            findViewById(keyIds[i]).setOnClickListener(v -> onKeyPressed(value));
        }

        findViewById(R.id.key_delete).setOnClickListener(v -> onDeletePressed());
    }

    private void onKeyPressed(String digit) {
        if (currentPassword.length() >= 6) {
            return;
        }

        hideError();
        currentPassword.append(digit);
        updateDots();

        if (currentPassword.length() == 6) {
            handlePasswordComplete();
        }
    }

    private void onDeletePressed() {
        if (currentPassword.length() > 0) {
            currentPassword.deleteCharAt(currentPassword.length() - 1);
            updateDots();
            hideError();
        }
    }

    private void updateDots() {
        int length = currentPassword.length();
        for (int i = 0; i < 6; i++) {
            if (i < length) {
                dotViews[i].setBackgroundResource(R.drawable.bg_password_dot_filled);
            } else {
                dotViews[i].setBackgroundResource(R.drawable.bg_password_dot_empty);
            }
        }
    }

    private void handlePasswordComplete() {
        String password = currentPassword.toString();

        if (MODE_SET.equals(mode)) {
            if (firstPassword == null) {
                firstPassword = password;
                currentPassword.setLength(0);
                updateDots();
                tvHint.setText("请再次确认支付密码");
            } else {
                if (firstPassword.equals(password)) {
                    savePassword(password);
                    Toast.makeText(this, "支付密码设置成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    showError("两次输入密码不一致，请重新输入");
                    currentPassword.setLength(0);
                    updateDots();
                    firstPassword = null;
                    tvHint.setText("请输入6位支付密码");
                }
            }
        } else {
            SharedPreferences sp = getSharedPreferences(getPrefKey(this), MODE_PRIVATE);
            String savedPassword = sp.getString("password", "");

            if (savedPassword.equals(password)) {
                Toast.makeText(this, "验证成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            } else {
                showError("密码错误，请重新输入");
                currentPassword.setLength(0);
                updateDots();
            }
        }
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.INVISIBLE);
    }

    private void savePassword(String password) {
        SharedPreferences sp = getSharedPreferences(getPrefKey(this), MODE_PRIVATE);
        sp.edit().putString("password", password).apply();
        sp.edit().putBoolean("password_set", true).apply();
    }

    public static boolean isPasswordSet(AppCompatActivity activity) {
        String prefKey = PREF_NAME + "_" + UserManager.getInstance().getCurrentUserId();
        SharedPreferences sp = activity.getSharedPreferences(prefKey, MODE_PRIVATE);
        return sp.getBoolean("password_set", false);
    }
}
