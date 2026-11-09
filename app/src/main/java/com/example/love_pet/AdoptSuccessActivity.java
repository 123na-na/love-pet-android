package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AdoptSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adopt_success);

        findViewById(R.id.iv_back).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        TextView btnBackHome = findViewById(R.id.btn_back_home);
        TextView btnViewMore = findViewById(R.id.btn_view_more);

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnViewMore.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllPetsActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
