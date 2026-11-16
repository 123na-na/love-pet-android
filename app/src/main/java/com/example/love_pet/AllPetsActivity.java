package com.example.love_pet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AllPetsActivity extends AppCompatActivity {

    private LinearLayout columnLeft;
    private LinearLayout columnRight;
    private List<PetItem> petList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_pets);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        columnLeft = findViewById(R.id.column_left);
        columnRight = findViewById(R.id.column_right);

        initPetData();
        renderPetGrid();
    }

    private void initPetData() {
        PetItem pet1 = new PetItem();
        pet1.name = "雨雨";
        pet1.info = "2岁 | 2.2kg | 女孩子";
        pet1.location = "湛江市霞山区";
        pet1.bgColor = "#FFE8E8";
        pet1.imageResId = R.drawable.pet_yuyu;
        pet1.age = "2岁";
        pet1.weight = "2.2kg";
        pet1.gender = "女孩子";
        pet1.breed = "金渐层";
        pet1.sterilized = "已绝育";
        pet1.dewormed = "内外驱虫已做";
        pet1.vaccinated = "已免疫";
        pet1.checkin = "需要打卡";
        pet1.description = "1. 有稳定工作和收入，生活稳定\n2. 有固定住所，家里窗户已封窗\n3. 了解养猫常识，科学喂养\n4. 接受定期回访和线上打卡\n5. 不弃养，不虐待，生病及时医治";
        petList.add(pet1);

        PetItem pet2 = new PetItem();
        pet2.name = "团团";
        pet2.info = "3岁 | 8.6kg | 男孩子";
        pet2.location = "广州市天河区";
        pet2.bgColor = "#E8F4FF";
        pet2.imageResId = R.drawable.pet_tuantuan;
        pet2.age = "3岁";
        pet2.weight = "8.6kg";
        pet2.gender = "男孩子";
        pet2.breed = "英短银渐层";
        pet2.sterilized = "已绝育";
        pet2.dewormed = "内外驱虫已做";
        pet2.vaccinated = "已免疫";
        pet2.checkin = "需要打卡";
        pet2.description = "1. 有稳定工作和收入，生活稳定\n2. 有固定住所，家里窗户已封窗\n3. 了解养猫常识，科学喂养\n4. 接受定期回访和线上打卡\n5. 不弃养，不虐待，生病及时医治";
        petList.add(pet2);

        PetItem pet3 = new PetItem();
        pet3.name = "豆豆";
        pet3.info = "1岁 | 3.5kg | 男孩子";
        pet3.location = "深圳市南山区";
        pet3.bgColor = "#FFF5E8";
        pet3.imageResId = R.drawable.doudou;
        pet3.age = "1岁";
        pet3.weight = "3.5kg";
        pet3.gender = "男孩子";
        pet3.breed = "美短虎斑";
        pet3.sterilized = "已绝育";
        pet3.dewormed = "内外驱虫已做";
        pet3.vaccinated = "已免疫";
        pet3.checkin = "需要打卡";
        pet3.description = "1. 有稳定工作和收入，生活稳定\n2. 有固定住所，家里窗户已封窗\n3. 了解养猫常识，科学喂养\n4. 接受定期回访和线上打卡\n5. 不弃养，不虐待，生病及时医治";
        petList.add(pet3);

        PetItem pet4 = new PetItem();
        pet4.name = "小糊涂";
        pet4.info = "6个月 | 1.8kg | 女孩子";
        pet4.location = "珠海市香洲区";
        pet4.bgColor = "#F0E8FF";
        pet4.imageResId = R.drawable.xiaohutu;
        pet4.age = "6个月";
        pet4.weight = "1.8kg";
        pet4.gender = "女孩子";
        pet4.breed = "布偶猫";
        pet4.sterilized = "未绝育";
        pet4.dewormed = "内外驱虫已做";
        pet4.vaccinated = "已免疫";
        pet4.checkin = "需要打卡";
        pet4.description = "1. 有稳定工作和收入，生活稳定\n2. 有固定住所，家里窗户已封窗\n3. 了解养猫常识，科学喂养\n4. 接受定期回访和线上打卡\n5. 不弃养，不虐待，生病及时医治";
        petList.add(pet4);

        PetItem pet5 = new PetItem();
        pet5.name = "布丁";
        pet5.info = "2岁 | 5kg | 男孩子";
        pet5.location = "佛山市顺德区";
        pet5.bgColor = "#E8FFEF";
        pet5.imageResId = R.drawable.pet_buding;
        pet5.age = "2岁";
        pet5.weight = "5kg";
        pet5.gender = "男孩子";
        pet5.breed = "橘猫";
        pet5.sterilized = "已绝育";
        pet5.dewormed = "内外驱虫已做";
        pet5.vaccinated = "已免疫";
        pet5.checkin = "需要打卡";
        pet5.description = "1. 有稳定工作和收入，生活稳定\n2. 有固定住所，家里窗户已封窗\n3. 了解养猫常识，科学喂养\n4. 接受定期回访和线上打卡\n5. 不弃养，不虐待，生病及时医治";
        petList.add(pet5);
    }

    private void renderPetGrid() {
        columnLeft.removeAllViews();
        columnRight.removeAllViews();
        int spacing = dpToPx(10);

        for (int i = 0; i < petList.size(); i++) {
            PetItem pet = petList.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_all_pet, null, false);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i >= 2) {
                params.topMargin = spacing;
            }
            itemView.setLayoutParams(params);

            ImageView ivPhoto = itemView.findViewById(R.id.iv_pet_photo);
            TextView tvName = itemView.findViewById(R.id.tv_pet_name);
            TextView tvInfo = itemView.findViewById(R.id.tv_pet_info);
            TextView tvLocation = itemView.findViewById(R.id.tv_pet_location);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.parseColor(pet.bgColor));
            ivPhoto.setBackground(bg);
            Glide.with(this)
                    .load(pet.imageResId)
                    .centerCrop()
                    .into(ivPhoto);
            tvName.setText(pet.name);
            tvInfo.setText(pet.info);
            tvLocation.setText(pet.location);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(AllPetsActivity.this, PetDetailActivity.class);
                intent.putExtra(PetDetailActivity.EXTRA_NAME, pet.name);
                intent.putExtra(PetDetailActivity.EXTRA_IMAGE_RES, pet.imageResId);
                intent.putExtra(PetDetailActivity.EXTRA_LOCATION, pet.location);
                intent.putExtra(PetDetailActivity.EXTRA_AGE, pet.age);
                intent.putExtra(PetDetailActivity.EXTRA_WEIGHT, pet.weight);
                intent.putExtra(PetDetailActivity.EXTRA_GENDER, pet.gender);
                intent.putExtra(PetDetailActivity.EXTRA_BREED, pet.breed);
                intent.putExtra(PetDetailActivity.EXTRA_STERILIZED, pet.sterilized);
                intent.putExtra(PetDetailActivity.EXTRA_DEWORMED, pet.dewormed);
                intent.putExtra(PetDetailActivity.EXTRA_VACCINATED, pet.vaccinated);
                intent.putExtra(PetDetailActivity.EXTRA_CHECKIN, pet.checkin);
                intent.putExtra(PetDetailActivity.EXTRA_DESCRIPTION, pet.description);
                startActivity(intent);
            });

            if (i % 2 == 0) {
                columnLeft.addView(itemView);
            } else {
                columnRight.addView(itemView);
            }
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    private static class PetItem {
        String name;
        String info;
        String location;
        String bgColor;
        int imageResId;
        String age;
        String weight;
        String gender;
        String breed;
        String sterilized;
        String dewormed;
        String vaccinated;
        String checkin;
        String description;
    }
}
