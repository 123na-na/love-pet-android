package com.example.love_pet;

import android.graphics.Outline;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 电子宠物证页面 - 展示宠物品种信息及晒图
 */
public class PetCertificateActivity extends AppCompatActivity {

    // 宠物品种数据（与动态页面的 post 图片一一对应）
    private static final PetBreedInfo[] PET_BREEDS = {
            new PetBreedInfo("比熊犬", "法国/比利时", "小型", "23-30cm", "3-6kg",
                    "比熊、卷毛比熊犬、Bichon Frise",
                    "12-15年（相当于人类65-70岁）",
                    "性格活泼开朗、友善亲人、聪明易训",
                    "白色卷毛如棉花糖，圆黑眼睛，可爱甜美",
                    "每天梳毛防打结，定期美容修剪，注意泪痕清洁"),
            new PetBreedInfo("泰迪犬（贵宾）", "德国/法国", "中小型", "25-38cm", "3-10kg",
                    "贵宾、泰迪、Poodle、贵妇犬",
                    "12-15年（相当于人类65-70岁）",
                    "性格聪明活泼、粘人爱撒娇、学习能力强",
                    "毛发可塑性强，卷毛不掉毛，颜色丰富",
                    "需定期专业美容，毛发要勤梳理，智商高需多互动"),
            new PetBreedInfo("金渐层长毛猫", "英国", "中型", "28-35cm", "4-7kg",
                    "金渐层波斯、长毛金渐层",
                    "12-17年（相当于人类65-75岁）",
                    "性格温顺安静、粘人但不吵闹、适应力强",
                    "金色渐变长毛蓬松柔软，大眼睛圆润迷人",
                    "长毛需每日梳理，容易打结，注意肠胃健康"),
            new PetBreedInfo("比熊犬", "法国/比利时", "小型", "23-30cm", "3-6kg",
                    "比熊、棉花球犬、Bichon Frise",
                    "12-15年（相当于人类65-70岁）",
                    "性格乐观开朗、喜欢与人互动、很少吠叫",
                    "纯白卷毛，笑容甜美，像行走的棉花糖",
                    "每天梳毛防止打结，定期洗澡美容，控制饮食防肥胖"),
            new PetBreedInfo("英短金渐层", "英国", "中大型", "30-35cm", "4-8kg",
                    "英短金渐层、British Shorthair Golden Shaded",
                    "12-17年（相当于人类65-75岁）",
                    "性格沉稳温和、独立又粘人、适应力强",
                    "圆脸大眼绿眼睛，金色渐层被毛浓密",
                    "容易发胖需控制食量，定期清理耳朵和眼部分泌物"),
            new PetBreedInfo("金吉拉猫", "英国/美国", "中型", "26-32cm", "3.5-6kg",
                    "金吉拉、人造波斯猫、Chinchilla Persian",
                    "12-16年（相当于人类65-72岁）",
                    "性格文静优雅、温柔粘人、声音轻柔",
                    "长毛华丽如丝绸，银白或金色渐层，大圆眼",
                    "每天必须梳毛防止打结，肠胃敏感需注意饮食"),
            new PetBreedInfo("英短幼猫", "英国", "中型", "28-35cm", "4-8kg",
                    "英短幼崽、British Shorthair Kitten",
                    "12-17年（相当于人类65-75岁）",
                    "性格好奇好动、活泼可爱、逐渐变得稳重",
                    "圆脸圆腮帮子，铜色或蓝色大眼睛",
                    "幼猫期需要高蛋白饮食，定期接种疫苗和驱虫")
    };

    private ImageView ivPetPhoto;
    private TextView tvBreed, tvOrigin, tvBodyType, tvHeight, tvWeight;
    private TextView tvAlias, tvLifespan, tvPersonality, tvFeatures, tvCareTips;
    private LinearLayout galleryContainer;
    private HorizontalScrollView galleryScroll;
    private int currentPetIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_certificate);

        // 返回按钮
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // 初始化控件
        ivPetPhoto = findViewById(R.id.iv_pet_photo);
        tvBreed = findViewById(R.id.tv_breed);
        tvOrigin = findViewById(R.id.tv_origin);
        tvBodyType = findViewById(R.id.tv_body_type);
        tvHeight = findViewById(R.id.tv_height);
        tvWeight = findViewById(R.id.tv_weight);
        tvAlias = findViewById(R.id.tv_alias);
        tvLifespan = findViewById(R.id.tv_lifespan);
        tvPersonality = findViewById(R.id.tv_personality);
        tvFeatures = findViewById(R.id.tv_features);
        tvCareTips = findViewById(R.id.tv_care_tips);
        galleryContainer = findViewById(R.id.gallery_container);

        // 宠物图片点击切换
        ivPetPhoto.setOnClickListener(v -> {
            currentPetIndex = (currentPetIndex + 1) % PET_BREEDS.length;
            updatePetInfo();
        });

        // 加载数据
        loadPetData();
    }

    /**
     * 加载宠物数据并显示
     */
    private void loadPetData() {
        // 从数据库获取动态页的笔记数据，取第一只宠物的信息
        try {
            PetDataManager dataManager = PetDataManager.getInstance(this);
            java.util.List<PetDataManager.PetNoteEntity> notes = dataManager.getAllNotes();

            if (!notes.isEmpty()) {
                // 根据第一条笔记的图片资源确定显示哪个品种
                String imageName = notes.get(0).getImageRes();
                int index = getImageIndex(imageName);
                currentPetIndex = index;
                updatePetInfo();

                // 构建晒图列表
                buildGallery(notes);
            } else {
                updatePetInfo();
                buildEmptyGallery();
            }
        } catch (Exception e) {
            e.printStackTrace();
            updatePetInfo();
            buildEmptyGallery();
        }
    }

    /**
     * 根据图片名称获取对应品种索引
     */
    private int getImageIndex(String imageName) {
        if (imageName == null)
            return 0;
        for (int i = 0; i < PetPageHelper.LOCAL_IMAGE_RESOURCES.length; i++) {
            String name = "post" + (i + 1);
            if (imageName.equals(name))
                return i;
        }
        return 0;
    }

    /**
     * 更新宠物信息显示
     */
    private void updatePetInfo() {
        PetBreedInfo info = PET_BREEDS[currentPetIndex];

        // 设置图片圆角
        ivPetPhoto.setClipToOutline(true);
        ivPetPhoto.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dpToPx(16));
            }
        });
        ivPetPhoto.setImageResource(PetPageHelper.LOCAL_IMAGE_RESOURCES[currentPetIndex]);

        tvBreed.setText("品种：" + info.breed);
        tvOrigin.setText("原产地：" + info.origin);
        tvBodyType.setText("体型：" + info.bodyType);
        tvHeight.setText("预计身高：" + info.height);
        tvWeight.setText("预计体重：" + info.weight);
        tvAlias.setText("别名：" + info.alias);
        tvLifespan.setText("寿命：" + info.lifespan);
        tvPersonality.setText("性格：" + info.personality);
        tvFeatures.setText("特点：" + info.features);
        tvCareTips.setText("打磨：" + info.careTips);
    }

    /**
     * 构建晒图画廊（使用动态中的笔记数据）
     */
    private void buildGallery(java.util.List<PetDataManager.PetNoteEntity> notes) {
        galleryContainer.removeAllViews();
        PetDataManager dataManager = PetDataManager.getInstance(this);

        for (int i = 0; i < notes.size(); i++) {
            PetDataManager.PetNoteEntity note = notes.get(i);
            View cardView = LayoutInflater.from(this).inflate(R.layout.item_cert_gallery, galleryContainer, false);

            // 用户头像
            ImageView ivAvatar = cardView.findViewById(R.id.iv_gallery_avatar);
            int avatarResId = dataManager.getResourceIdByName(note.getAvatarRes());
            if (avatarResId != 0)
                ivAvatar.setImageResource(avatarResId);

            // 用户名
            TextView tvName = cardView.findViewById(R.id.tv_gallery_name);
            tvName.setText(note.getAuthorName());

            // 晒图
            ImageView ivPhoto = cardView.findViewById(R.id.iv_gallery_photo);
            int imageResId = dataManager.getResourceIdByName(note.getImageRes());
            if (imageResId != 0)
                ivPhoto.setImageResource(imageResId);

            // 点击切换主图（根据该笔记的图片精确匹配品种）
            final int breedIndex = getImageIndex(note.getImageRes());
            cardView.setOnClickListener(v -> {
                currentPetIndex = breedIndex;
                updatePetInfo();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(140), LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(dpToPx(10));
            cardView.setLayoutParams(params);
            galleryContainer.addView(cardView);
        }
    }

    /**
     * 空状态画廊
     */
    private void buildEmptyGallery() {
        galleryContainer.removeAllViews();
        for (int i = 0; i < PET_BREEDS.length; i++) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.item_cert_gallery, galleryContainer, false);

            ImageView ivAvatar = cardView.findViewById(R.id.iv_gallery_avatar);
            ivAvatar.setImageResource(R.drawable.ic_cat_logo);

            TextView tvName = cardView.findViewById(R.id.tv_gallery_name);
            tvName.setText(PET_BREEDS[i].breed);

            ImageView ivPhoto = cardView.findViewById(R.id.iv_gallery_photo);
            ivPhoto.setImageResource(PetPageHelper.LOCAL_IMAGE_RESOURCES[i]);

            final int index = i;
            cardView.setOnClickListener(v -> {
                currentPetIndex = index;
                updatePetInfo();
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(140), LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(dpToPx(10));
            cardView.setLayoutParams(params);
            galleryContainer.addView(cardView);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    /**
     * 宠物品种信息数据类
     */
    private static class PetBreedInfo {
        String breed; // 品种
        String origin; // 原产地
        String bodyType; // 体型
        String height; // 身高
        String weight; // 体重
        String alias; // 别名
        String lifespan; // 寿命
        String personality;// 性格
        String features; // 特点
        String careTips; // 养护提示

        PetBreedInfo(String breed, String origin, String bodyType, String height,
                String weight, String alias, String lifespan,
                String personality, String features, String careTips) {
            this.breed = breed;
            this.origin = origin;
            this.bodyType = bodyType;
            this.height = height;
            this.weight = weight;
            this.alias = alias;
            this.lifespan = lifespan;
            this.personality = personality;
            this.features = features;
            this.careTips = careTips;
        }
    }
}
