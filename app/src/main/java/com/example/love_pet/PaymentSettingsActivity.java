package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentSettingsActivity extends AppCompatActivity {

    private static final int REQUEST_PAYMENT_PASSWORD = 1001;

    private ImageView ivBack;
    private TextView tvHuabeiStatus;
    private TextView tvAlipayStatus;
    private TextView tvPaymentPasswordStatus;
    private boolean isHuabeiOpened = false;
    private boolean isAlipayBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_settings);

        initViews();
        setupListeners();
        updatePasswordStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePasswordStatus();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvHuabeiStatus = findViewById(R.id.tv_huabei_status);
        tvAlipayStatus = findViewById(R.id.tv_alipay_status);
        tvPaymentPasswordStatus = findViewById(R.id.tv_payment_password_status);
    }

    private void updatePasswordStatus() {
        if (PaymentPasswordActivity.isPasswordSet(this)) {
            tvPaymentPasswordStatus.setText("已设置");
            tvPaymentPasswordStatus.setTextColor(0xFF666666);
        } else {
            tvPaymentPasswordStatus.setText("未设置");
            tvPaymentPasswordStatus.setTextColor(0xFF999999);
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.item_payment_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PaymentSettingsActivity.this, PaymentPasswordActivity.class);
                if (PaymentPasswordActivity.isPasswordSet(PaymentSettingsActivity.this)) {
                    intent.putExtra(PaymentPasswordActivity.EXTRA_MODE, PaymentPasswordActivity.MODE_VERIFY);
                    Toast.makeText(PaymentSettingsActivity.this, "请先验证原密码", Toast.LENGTH_SHORT).show();
                } else {
                    intent.putExtra(PaymentPasswordActivity.EXTRA_MODE, PaymentPasswordActivity.MODE_SET);
                }
                startActivityForResult(intent, REQUEST_PAYMENT_PASSWORD);
            }
        });

        findViewById(R.id.item_huabei).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isHuabeiOpened) {
                    Toast.makeText(PaymentSettingsActivity.this, "正在跳转到支付宝开通先用后付...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.item_alipay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isAlipayBound) {
                    Toast.makeText(PaymentSettingsActivity.this, "正在跳转到支付宝绑定账户...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.item_add_bank).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PaymentSettingsActivity.this, "添加银行卡功能开发中", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PAYMENT_PASSWORD && resultCode == RESULT_OK) {
            updatePasswordStatus();
            if (PaymentPasswordActivity.isPasswordSet(this)) {
                Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
