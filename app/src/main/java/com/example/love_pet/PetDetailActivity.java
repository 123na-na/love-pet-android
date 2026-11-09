package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PetDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_IMAGE_RES = "image_res";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_AGE = "age";
    public static final String EXTRA_WEIGHT = "weight";
    public static final String EXTRA_GENDER = "gender";
    public static final String EXTRA_BREED = "breed";
    public static final String EXTRA_STERILIZED = "sterilized";
    public static final String EXTRA_DEWORMED = "dewormed";
    public static final String EXTRA_VACCINATED = "vaccinated";
    public static final String EXTRA_CHECKIN = "checkin";
    public static final String EXTRA_DESCRIPTION = "description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_detail);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        String name = getIntent().getStringExtra(EXTRA_NAME);
        int imageRes = getIntent().getIntExtra(EXTRA_IMAGE_RES, 0);
        String location = getIntent().getStringExtra(EXTRA_LOCATION);
        String age = getIntent().getStringExtra(EXTRA_AGE);
        String weight = getIntent().getStringExtra(EXTRA_WEIGHT);
        String gender = getIntent().getStringExtra(EXTRA_GENDER);
        String breed = getIntent().getStringExtra(EXTRA_BREED);
        String sterilized = getIntent().getStringExtra(EXTRA_STERILIZED);
        String dewormed = getIntent().getStringExtra(EXTRA_DEWORMED);
        String vaccinated = getIntent().getStringExtra(EXTRA_VACCINATED);
        String checkin = getIntent().getStringExtra(EXTRA_CHECKIN);
        String description = getIntent().getStringExtra(EXTRA_DESCRIPTION);

        ImageView ivPetPhoto = findViewById(R.id.iv_pet_photo);
        TextView tvName = findViewById(R.id.tv_name);
        TextView tvLocation = findViewById(R.id.tv_location);
        TextView tvBasicInfo = findViewById(R.id.tv_basic_info);
        TextView tvSterilized = findViewById(R.id.tv_sterilized);
        TextView tvDewormed = findViewById(R.id.tv_dewormed);
        TextView tvVaccinated = findViewById(R.id.tv_vaccinated);
        TextView tvBreed = findViewById(R.id.tv_breed);
        TextView tvCheckin = findViewById(R.id.tv_checkin);
        TextView tvDescription = findViewById(R.id.tv_description);
        TextView btnAdopt = findViewById(R.id.btn_adopt);

        if (imageRes != 0) {
            Glide.with(this)
                    .load(imageRes)
                    .centerCrop()
                    .into(ivPetPhoto);
        }

        tvName.setText(name != null ? name : "");
        tvLocation.setText(location != null ? location : "");
        tvBasicInfo.setText((age != null ? age : "") + " | " +
                (weight != null ? weight : "") + " | " +
                (gender != null ? gender : ""));
        tvSterilized.setText(sterilized != null ? sterilized : "是");
        tvDewormed.setText(dewormed != null ? dewormed : "1月内");
        tvVaccinated.setText(vaccinated != null ? vaccinated : "是");
        tvBreed.setText(breed != null ? breed : "");
        tvCheckin.setText(checkin != null ? checkin : "13周");
        tvDescription.setText(description != null ? description :
                "可能你想要领养一只宠物，但是可能实际和你想象的不太一样，所以请你一定要做好充足的准备。");

        btnAdopt.setOnClickListener(v -> {
            Intent intent = new Intent(PetDetailActivity.this, AdoptApplyActivity.class);
            intent.putExtra(AdoptApplyActivity.EXTRA_NAME, name);
            intent.putExtra(AdoptApplyActivity.EXTRA_IMAGE_RES, imageRes);
            intent.putExtra(AdoptApplyActivity.EXTRA_AGE, age);
            intent.putExtra(AdoptApplyActivity.EXTRA_WEIGHT, weight);
            intent.putExtra(AdoptApplyActivity.EXTRA_GENDER, gender);
            intent.putExtra(AdoptApplyActivity.EXTRA_LOCATION, location);
            startActivity(intent);
        });
    }
}
