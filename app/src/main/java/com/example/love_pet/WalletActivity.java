package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WalletActivity extends AppCompatActivity {

    private static final int REQUEST_PAYMENT_PASSWORD = 1001;

    private ImageView ivBack;
    private TextView tvSettings;
    private ImageView ivHideBalance;
    private TextView tvBalance;
    private boolean isBalanceVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        initViews();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        tvSettings = findViewById(R.id.tv_settings);
        ivHideBalance = findViewById(R.id.iv_hide_balance);
        tvBalance = findViewById(R.id.tv_balance);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WalletActivity.this, PaymentSettingsActivity.class);
                startActivity(intent);
            }
        });

        ivHideBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBalanceVisibility();
            }
        });

        tvBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBalanceVisibility();
            }
        });

        findViewById(R.id.balance_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleBalanceCardClick();
            }
        });

        findViewById(R.id.item_coins).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WalletActivity.this, "薯币充值功能开发中", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.item_coupons).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WalletActivity.this, "优惠券详情功能开发中", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.item_red_packet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(WalletActivity.this, "购物红包功能开发中", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleBalanceCardClick() {
        Intent intent = new Intent(this, PaymentPasswordActivity.class);
        if (PaymentPasswordActivity.isPasswordSet(this)) {
            intent.putExtra(PaymentPasswordActivity.EXTRA_MODE, PaymentPasswordActivity.MODE_VERIFY);
        } else {
            intent.putExtra(PaymentPasswordActivity.EXTRA_MODE, PaymentPasswordActivity.MODE_SET);
        }
        startActivityForResult(intent, REQUEST_PAYMENT_PASSWORD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PAYMENT_PASSWORD && resultCode == RESULT_OK) {
            if (PaymentPasswordActivity.isPasswordSet(this)) {
                Toast.makeText(this, "支付密码验证成功", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void toggleBalanceVisibility() {
        isBalanceVisible = !isBalanceVisible;
        if (isBalanceVisible) {
            tvBalance.setText("¥0.00");
            ivHideBalance.setImageResource(R.drawable.ic_eye_open_dark);
        } else {
            tvBalance.setText("¥****");
            ivHideBalance.setImageResource(R.drawable.ic_eye_close_dark);
        }
    }
}
