package com.example.love_pet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class AdoptActivity extends AppCompatActivity {

    private ViewPager2 viewPagerBanner;
    private LinearLayout indicatorContainer;
    private LinearLayout recommendPetsContainer;
    private LinearLayout starsContainer;

    private List<BannerItem> bannerList = new ArrayList<>();
    private List<PetItem> recommendPets = new ArrayList<>();
    private List<PetStar> starList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adopt);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        View tvAllPets = findViewById(R.id.tv_all_pets);
        if (tvAllPets != null) {
            tvAllPets.setOnClickListener(v -> {
                Intent intent = new Intent(AdoptActivity.this, AllPetsActivity.class);
                startActivity(intent);
            });
        }

        viewPagerBanner = findViewById(R.id.view_pager_banner);
        indicatorContainer = findViewById(R.id.indicator_container);
        recommendPetsContainer = findViewById(R.id.recommend_pets_container);
        starsContainer = findViewById(R.id.stars_container);

        initBannerData();
        initRecommendPets();
        initPetStars();

        setupBanner();
        setupRecommendPets();
        setupPetStars();
    }

    private void initBannerData() {
        BannerItem banner1 = new BannerItem();
        banner1.title = "我的宠物\n搭子";
        banner1.subtitle = "全世界最好的搭子\n是我的小狗/猫咪/小龟/兔兔......";
        banner1.bgColor = "#FFE680";
        banner1.petResId = R.drawable.pet_yuyu;
        bannerList.add(banner1);

        BannerItem banner2 = new BannerItem();
        banner2.title = "领养代替\n购买";
        banner2.subtitle = "给流浪的毛孩子一个温暖的家";
        banner2.bgColor = "#FFB6C1";
        banner2.petResId = R.drawable.pet_tuantuan;
        bannerList.add(banner2);

        BannerItem banner3 = new BannerItem();
        banner3.title = "爱心\n公益";
        banner3.subtitle = "每一次领养都是一次爱的传递";
        banner3.bgColor = "#98D8C8";
        banner3.petResId = R.drawable.pet_buding;
        bannerList.add(banner3);
    }

    private void initRecommendPets() {
        PetItem pet1 = new PetItem();
        pet1.name = "雨雨";
        pet1.info = "2岁 | 2.2kg";
        pet1.bgColor = "#FFE8E8";
        pet1.imageResId = R.drawable.pet_yuyu;
        pet1.location = "湛江市霞山区";
        pet1.age = "2岁";
        pet1.weight = "2.2kg";
        pet1.gender = "女孩子";
        pet1.breed = "金渐层";
        pet1.sterilized = "是";
        pet1.dewormed = "1月内";
        pet1.vaccinated = "是";
        pet1.checkin = "13周";
        pet1.description = "可能你想要领养一只宠物，但是可能实际和你想象的不太一样，所以请你一定要做好充足的准备。";
        recommendPets.add(pet1);

        PetItem pet2 = new PetItem();
        pet2.name = "团团";
        pet2.info = "3岁 | 8.6kg";
        pet2.bgColor = "#E8F5FF";
        pet2.imageResId = R.drawable.pet_tuantuan;
        pet2.location = "广州市天河区";
        pet2.age = "3岁";
        pet2.weight = "8.6kg";
        pet2.gender = "男孩子";
        pet2.breed = "英短银渐层";
        pet2.sterilized = "是";
        pet2.dewormed = "2周前";
        pet2.vaccinated = "是";
        pet2.checkin = "8周";
        pet2.description = "团团是一只非常温顺的猫咪，喜欢安静的环境，适合上班族领养。已绝育，已驱虫，已打疫苗。";
        recommendPets.add(pet2);
    }

    private void initPetStars() {
        PetStar star1 = new PetStar();
        star1.name = "小糊涂";
        star1.info = "1岁3个月  广东 | 茂名";
        star1.desc = "很活泼，非常非常乖，很听话，等待一个有爱的家，不离不弃";
        star1.isMale = false;
        star1.avatarResId = R.drawable.xiaohutu;
        starList.add(star1);

        PetStar star2 = new PetStar();
        star2.name = "布丁";
        star2.info = "2岁  北京 | 朝阳";
        star2.desc = "温柔粘人，喜欢撒娇，会用猫砂，已绝育已驱虫，希望找个靠谱的主人";
        star2.isMale = false;
        star2.avatarResId = R.drawable.pet_buding;
        starList.add(star2);

        PetStar star3 = new PetStar();
        star3.name = "豆豆";
        star3.info = "1岁  上海 | 浦东";
        star3.desc = "活泼好动，会各种技能，非常聪明，对人友好，适合有小孩的家庭";
        star3.isMale = true;
        star3.avatarResId = R.drawable.doudou;
        starList.add(star3);
    }

    private void setupBanner() {
        BannerAdapter adapter = new BannerAdapter(bannerList);
        viewPagerBanner.setAdapter(adapter);

        for (int i = 0; i < bannerList.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(6), dpToPx(6));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.bg_dot_unselected);
            indicatorContainer.addView(dot);
        }
        updateIndicator(0);

        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicator(position);
            }
        });
    }

    private void updateIndicator(int currentIndex) {
        for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
            View dot = indicatorContainer.getChildAt(i);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
            if (i == currentIndex) {
                params.width = dpToPx(16);
                dot.setBackgroundResource(R.drawable.bg_dot_selected);
            } else {
                params.width = dpToPx(6);
                dot.setBackgroundResource(R.drawable.bg_dot_unselected);
            }
            dot.setLayoutParams(params);
        }
    }

    private void setupRecommendPets() {
        for (int i = 0; i < recommendPets.size(); i++) {
            PetItem pet = recommendPets.get(i);
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_recommend_pet, recommendPetsContainer, false);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            if (i == 0) {
                params.rightMargin = dpToPx(10);
            } else {
                params.leftMargin = dpToPx(10);
            }
            itemView.setLayoutParams(params);

            ImageView ivPhoto = itemView.findViewById(R.id.iv_pet_photo);
            TextView tvName = itemView.findViewById(R.id.tv_pet_name);
            TextView tvInfo = itemView.findViewById(R.id.tv_pet_info);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.parseColor(pet.bgColor));
            ivPhoto.setBackground(bg);
            Glide.with(this)
                    .load(pet.imageResId)
                    .centerCrop()
                    .into(ivPhoto);
            tvName.setText(pet.name);
            tvInfo.setText(pet.info);

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(AdoptActivity.this, PetDetailActivity.class);
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

            recommendPetsContainer.addView(itemView);
        }
    }

    private void setupPetStars() {
        for (PetStar star : starList) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_pet_star, starsContainer, false);

            ImageView ivAvatar = itemView.findViewById(R.id.iv_star_avatar);
            TextView tvName = itemView.findViewById(R.id.tv_star_name);
            ImageView ivGender = itemView.findViewById(R.id.iv_gender);
            TextView tvInfo = itemView.findViewById(R.id.tv_star_info);
            TextView tvDesc = itemView.findViewById(R.id.tv_star_desc);

            ivAvatar.setImageResource(star.avatarResId);
            tvName.setText(star.name);
            tvInfo.setText(star.info);
            tvDesc.setText(star.desc);

            if (star.isMale) {
                ivGender.setImageResource(R.drawable.ic_male);
            } else {
                ivGender.setImageResource(R.drawable.ic_female);
            }

            itemView.setOnClickListener(v -> {
                Toast.makeText(this, "查看 " + star.name + " 的详情", Toast.LENGTH_SHORT).show();
            });

            starsContainer.addView(itemView);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private static class BannerItem {
        String title;
        String subtitle;
        String bgColor;
        int petResId;
    }

    private static class PetItem {
        String name;
        String info;
        String bgColor;
        int imageResId;
        String location;
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

    private static class PetStar {
        String name;
        String info;
        String desc;
        boolean isMale;
        int avatarResId;
    }

    private class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

        private List<BannerItem> list;

        BannerAdapter(List<BannerItem> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_adopt_banner, parent, false);
            return new BannerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
            BannerItem item = list.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvSubtitle.setText(item.subtitle);
            holder.ivPet.setImageResource(item.petResId);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.parseColor(item.bgColor));
            bg.setCornerRadius(dpToPx(16));
            holder.bannerLayout.setBackground(bg);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class BannerViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            TextView tvSubtitle;
            ImageView ivPet;
            View bannerLayout;

            BannerViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tv_banner_title);
                tvSubtitle = itemView.findViewById(R.id.tv_banner_subtitle);
                ivPet = itemView.findViewById(R.id.iv_banner_pet);
                bannerLayout = itemView.findViewById(R.id.banner_root);
            }
        }
    }
}
