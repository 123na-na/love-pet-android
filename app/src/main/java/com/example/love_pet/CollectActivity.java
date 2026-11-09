package com.example.love_pet;

import android.content.Intent;
import android.graphics.Outline;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 收藏页面 - 布局与宠图页面一致（GridLayout + 卡片瀑布流）
 */
public class CollectActivity extends AppCompatActivity {

    private GridLayout gridCollect;
    private View layoutEmpty;
    private PetDataManager dataManager;
    private List<PetPageHelper.PetItem> collectList = new ArrayList<>();

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);

        // 返回按钮
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // 初始化控件
        gridCollect = findViewById(R.id.grid_collect);
        layoutEmpty = findViewById(R.id.layout_empty);

        // 初始化数据管理器
        dataManager = PetDataManager.getInstance(this);

        // 加载收藏数据
        loadCollectData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从详情页返回时刷新数据
        loadCollectData();
    }

    /**
     * 加载收藏数据
     */
    private void loadCollectData() {
        try {
            // 获取所有笔记，筛选出已收藏的
            List<PetDataManager.PetNoteEntity> allNotes = dataManager.getAllNotes();
            collectList.clear();

            for (PetDataManager.PetNoteEntity note : allNotes) {
                if (note.isCollected()) {
                    int imageResId = dataManager.getResourceIdByName(note.getImageRes());
                    int avatarResId = dataManager.getResourceIdByName(note.getAvatarRes());

                    PetPageHelper.PetItem item = new PetPageHelper.PetItem(
                            imageResId,
                            note.getTitle(),
                            avatarResId,
                            note.getAuthorName(),
                            note.getLikeCount(),
                            note.getCollectCount(),
                            note.getCommentCount(),
                            note.getContent(),
                            note.getLocation(),
                            note.getPublishTime(),
                            note.getId(),
                            note.isLiked());
                    collectList.add(item);
                }
            }

            renderCollectCards();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 渲染收藏卡片（与宠图布局一致）
     */
    private void renderCollectCards() {
        if (gridCollect == null)
            return;

        gridCollect.removeAllViews();

        if (collectList.isEmpty()) {
            // 显示空状态
            layoutEmpty.setVisibility(View.VISIBLE);
            gridCollect.setVisibility(View.GONE);
            return;
        } else {
            layoutEmpty.setVisibility(View.GONE);
            gridCollect.setVisibility(View.VISIBLE);
        }

        // 计算卡片宽度（两列布局，减去间距）
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cardWidth = (screenWidth - dpToPx(24) - dpToPx(8)) / 2;

        // 计算3:4比例的图片高度（长矩形，类似小红书风格）
        int imageHeight = (int) (cardWidth * 4.0 / 3.0);

        for (int index = 0; index < collectList.size(); index++) {
            PetPageHelper.PetItem pet = collectList.get(index);
            View cardView = LayoutInflater.from(this).inflate(R.layout.item_pet_card, gridCollect, false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = cardWidth;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.setMarginStart(index % 2 == 0 ? 0 : dpToPx(8));
            params.setMarginEnd(index % 2 == 0 ? dpToPx(8) : 0);
            params.topMargin = dpToPx(8);

            cardView.setLayoutParams(params);

            // 设置图片高度为3:4比例
            ImageView ivImage = cardView.findViewById(R.id.iv_pet_image);
            ivImage.getLayoutParams().height = imageHeight;
            ivImage.setImageResource(pet.getImageRes());

            // 设置图片圆角
            ivImage.setClipToOutline(true);
            ivImage.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dpToPx(12));
                }
            });

            // 设置标题
            TextView tvTitle = cardView.findViewById(R.id.tv_title);
            tvTitle.setText(pet.getTitle());

            // 设置头像
            ImageView ivAvatar = cardView.findViewById(R.id.iv_avatar);
            if (pet.getAvatarRes() != 0) {
                ivAvatar.setImageResource(pet.getAvatarRes());
            }
            ivAvatar.setClipToOutline(true);
            ivAvatar.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });

            // 设置用户名
            TextView tvUsername = cardView.findViewById(R.id.tv_username);
            tvUsername.setText(pet.getUsername());

            // 设置点赞状态
            updateLikeStatus(cardView, pet);

            // 点赞点击
            ImageView ivLikeSmall = cardView.findViewById(R.id.iv_like_small);
            ivLikeSmall.setOnClickListener(v -> {
                pet.setLiked(!pet.isLiked());
                pet.setLikeCount(pet.getLikeCount() + (pet.isLiked() ? 1 : -1));
                updateLikeStatus(cardView, pet);
                dataManager.updateLikeStatus(pet.getNoteId(), pet.isLiked(), pet.getLikeCount());
            });

            // 点击卡片跳转详情页
            final int position = index;
            cardView.setOnClickListener(v -> {
                PetPageHelper.PetItem item = collectList.get(position);
                Intent intent = new Intent(CollectActivity.this, DetailActivity.class);
                intent.putExtra("noteId", item.getNoteId());
                intent.putExtra("title", item.getTitle());
                intent.putExtra("username", item.getUsername());
                intent.putExtra("avatarRes", item.getAvatarRes());
                intent.putExtra("content", item.getContent());
                intent.putExtra("location", item.getLocation());
                intent.putExtra("publishTime", item.getPublishTime());
                intent.putExtra("likeCount", item.getLikeCount());
                intent.putExtra("collectCount", item.getCollectCount());
                intent.putExtra("commentCount", item.getCommentCount());
                intent.putExtra("isLiked", item.isLiked());
                intent.putExtra("isCollected", item.isCollected());
                intent.putExtra("imageRes", item.getImageRes());
                startActivity(intent);
            });

            gridCollect.addView(cardView);
        }
    }

    /**
     * 更新点赞状态显示
     */
    private void updateLikeStatus(View cardView, PetPageHelper.PetItem pet) {
        ImageView ivLikeSmall = cardView.findViewById(R.id.iv_like_small);
        TextView tvLikeCount = cardView.findViewById(R.id.tv_like_count);

        if (pet.isLiked()) {
            ivLikeSmall.setImageResource(R.drawable.ic_heart_red);
            tvLikeCount.setTextColor(ColorUtils.blendARGB(0xFF000000, 0xFFFF4444, 0));
        } else {
            ivLikeSmall.setImageResource(R.drawable.ic_like_normal);
            tvLikeCount.setTextColor(ColorUtils.blendARGB(0xFF000000, 0xFF999999, 0));
        }
        tvLikeCount.setText(String.valueOf(pet.getLikeCount()));
    }

    /**
     * dp 转 px
     */
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
