package com.example.love_pet;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PetPageHelper {

    private MainActivity activity;
    private View petContainer;
    private GridLayout gridPets;
    private List<PetItem> petList = new ArrayList<>();
    private PetDataManager dataManager;

    // 当前选中的标签索引（0=关注, 1=发现, 2=附近）
    private int currentTabIndex = 1;

    // 本地图片资源数组
    public static final int[] LOCAL_IMAGE_RESOURCES = {
            R.drawable.post1,
            R.drawable.post2,
            R.drawable.post3,
            R.drawable.post4,
            R.drawable.post5,
            R.drawable.post6,
            R.drawable.post7
    };

    public PetPageHelper(MainActivity activity) {
        this.activity = activity;
        this.dataManager = PetDataManager.getInstance(activity);
    }

    public void initPetPage(View rootView) {
        petContainer = rootView;
        gridPets = rootView.findViewById(R.id.grid_pets);

        // 设置 ScrollView 底部 padding，避免被底部导航栏遮挡
        android.widget.ScrollView scrollView = rootView.findViewById(R.id.scroll_pet_content);
        if (scrollView != null) {
            scrollView.setPadding(0, 0, 0, dpToPx(140));
        }

        // 从数据库加载数据
        loadDataFromDatabase();

        // 设置标签点击事件
        setupTabClickListeners();

        // 搜索图标点击
        ImageView ivSearch = rootView.findViewById(R.id.iv_search);
        ivSearch.setOnClickListener(v -> {
            activity.startActivity(new Intent(activity, SearchActivity.class));
        });
    }

    /**
     * 刷新数据（从详情页返回时调用）
     */
    public void refreshData() {
        loadDataFromDatabase();
        // 如果当前在发现页，重新渲染卡片
        if (currentTabIndex == 1) {
            renderPetCards();
        }
    }

    private void loadDataFromDatabase() {
        try {
            List<PetDataManager.PetNoteEntity> notes = dataManager.initializeDataSync(LOCAL_IMAGE_RESOURCES.length);
            petList.clear();

            for (PetDataManager.PetNoteEntity note : notes) {
                String imageRes = note.getImageRes();
                
                boolean isFileImage = imageRes != null && imageRes.startsWith("/");
                
                int imageResId = 0;
                String imageUri = null;
                
                if (isFileImage) {
                    imageUri = imageRes;
                    // 只显示第一条用户上传的图片信息
                    if (petList.size() == 0) {
                        android.widget.Toast.makeText(activity, "发现用户上传的图片: " + imageUri.substring(0, Math.min(30, imageUri.length())) + "...", android.widget.Toast.LENGTH_LONG).show();
                    }
                } else {
                    imageResId = dataManager.getResourceIdByName(imageRes);
                }
                
                int avatarResId = dataManager.getResourceIdByName(note.getAvatarRes());

                PetItem item = new PetItem(
                        imageResId,
                        imageUri,
                        note.getTitle(),
                        avatarResId,
                        note.getAvatarRes(),
                        note.getAuthorName(),
                        note.getLikeCount(),
                        note.getCollectCount(),
                        note.getCommentCount(),
                        note.getContent(),
                        note.getLocation(),
                        note.getPublishTime(),
                        note.getId(),
                        note.isLiked(),
                        note.isCollected());
                petList.add(item);
            }
            android.widget.Toast.makeText(activity, "共加载 " + petList.size() + " 条笔记", android.widget.Toast.LENGTH_SHORT).show();
            renderPetCards();
        } catch (Exception e) {
            e.printStackTrace();
            initMockDataFallback();
        }
    }

    private void initMockDataFallback() {
        petList.clear();

        String[] nicknames = { "元气少女", "酷盖本盖", "小确幸", "人间理想", "星河滚烫", "温柔以待", "日常贩卖" };

        for (int i = 0; i < LOCAL_IMAGE_RESOURCES.length; i++) {
            PetItem item = new PetItem(
                    LOCAL_IMAGE_RESOURCES[i],
                    "可爱的小猫咪" + (i + 1),
                    R.drawable.ic_paw_black,
                    nicknames[i % nicknames.length],
                    2000 + i * 500,
                    1000 + i * 300,
                    5,
                    "今天天气真好，带着我家猫咪出来晒太阳，它玩得很开心！",
                    "北京",
                    "今天 10:30",
                    i + 1,
                    false);
            petList.add(item);
        }
        renderPetCards();
    }

    private void renderPetCards() {
        renderPetCards(petList);
    }

    private void renderPetCards(List<PetItem> items) {
        if (gridPets == null) {
            return;
        }

        gridPets.removeAllViews();

        // 计算卡片宽度（两列布局，减去间距）
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int cardWidth = (screenWidth - dpToPx(24) - dpToPx(8)) / 2;

        // 计算3:4比例的图片高度（长矩形，类似小红书风格）
        int imageHeight = (int) (cardWidth * 4.0 / 3.0);

        for (int index = 0; index < items.size(); index++) {
            PetItem pet = items.get(index);
            View cardView = LayoutInflater.from(activity).inflate(R.layout.item_pet_card, gridPets, false);

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

            // 使用PetItem中存储的正确图片资源（支持文件路径和资源ID）
            if (pet.hasUriImage()) {
                Glide.with(activity)
                        .load(new File(pet.getImageUri()))
                        .into(ivImage);
            } else {
                ivImage.setImageResource(pet.getImageRes());
            }

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

            // 设置头像（使用数据库中的头像，支持资源ID和文件路径）
            ImageView ivAvatar = cardView.findViewById(R.id.iv_avatar);
            if (pet.getAvatarUri() != null && !pet.getAvatarUri().isEmpty()) {
                java.io.File avatarFile = new java.io.File(pet.getAvatarUri());
                if (avatarFile.exists()) {
                    Glide.with(activity).load(avatarFile).into(ivAvatar);
                } else {
                    int resId = dataManager.getResourceIdByName(pet.getAvatarUri());
                    if (resId != 0) {
                        ivAvatar.setImageResource(resId);
                    } else {
                        ivAvatar.setImageResource(R.drawable.nav_profile_normal);
                    }
                }
            } else if (pet.getAvatarRes() != 0) {
                ivAvatar.setImageResource(pet.getAvatarRes());
            } else {
                ivAvatar.setImageResource(R.drawable.nav_profile_normal);
            }
            // 设置头像为圆形
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

            // 小爱心点击
            ImageView ivLikeSmall = cardView.findViewById(R.id.iv_like_small);
            ivLikeSmall.setOnClickListener(v -> {
                pet.setLiked(!pet.isLiked());
                pet.setLikeCount(pet.getLikeCount() + (pet.isLiked() ? 1 : -1));
                updateLikeStatus(cardView, pet);
                // 更新数据库
                dataManager.updateLikeStatus(pet.getNoteId(), pet.isLiked(), pet.getLikeCount());
            });

            // 点击卡片跳转详情页
            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(activity, DetailActivity.class);
                intent.putExtra("noteId", pet.getNoteId());
                intent.putExtra("title", pet.getTitle());
                intent.putExtra("username", pet.getUsername());
                intent.putExtra("avatarRes", pet.getAvatarRes());
                intent.putExtra("avatarUri", pet.getAvatarUri());
                intent.putExtra("content", pet.getContent());
                intent.putExtra("location", pet.getLocation());
                intent.putExtra("publishTime", pet.getPublishTime());
                intent.putExtra("likeCount", pet.getLikeCount());
                intent.putExtra("collectCount", pet.getCollectCount());
                intent.putExtra("commentCount", pet.getCommentCount());
                intent.putExtra("isLiked", pet.isLiked());
                intent.putExtra("isCollected", pet.isCollected());
                intent.putExtra("imageRes", pet.getImageRes());
                intent.putExtra("imageUri", pet.getImageUri());
                activity.startActivity(intent);
            });

            gridPets.addView(cardView);
        }
    }

    private void updateLikeStatus(View cardView, PetItem pet) {
        ImageView ivLikeSmall = cardView.findViewById(R.id.iv_like_small);
        TextView tvLikeCount = cardView.findViewById(R.id.tv_like_count);

        if (pet.isLiked()) {
            ivLikeSmall.setImageResource(R.drawable.ic_heart_red);
            tvLikeCount.setTextColor(Color.parseColor("#FF4444"));
        } else {
            ivLikeSmall.setImageResource(R.drawable.ic_like_normal);
            tvLikeCount.setTextColor(Color.parseColor("#999999"));
        }

        tvLikeCount.setText(formatLikeCount(pet.getLikeCount()));
    }

    private String formatLikeCount(int count) {
        if (count >= 10000) {
            return (count / 10000) + "w";
        } else {
            return String.valueOf(count);
        }
    }

    private void setupTabClickListeners() {
        TextView tvTabFollow = petContainer.findViewById(R.id.tv_tab_follow);
        TextView tvTabDiscover = petContainer.findViewById(R.id.tv_tab_discover);
        TextView tvTabNearby = petContainer.findViewById(R.id.tv_tab_nearby);

        View underlineFollow = petContainer.findViewById(R.id.underline_follow);
        View underlineDiscover = petContainer.findViewById(R.id.underline_discover);
        View underlineNearby = petContainer.findViewById(R.id.underline_nearby);

        List<TextView> tabs = new ArrayList<>();
        tabs.add(tvTabFollow);
        tabs.add(tvTabDiscover);
        tabs.add(tvTabNearby);

        List<View> underlines = new ArrayList<>();
        underlines.add(underlineFollow);
        underlines.add(underlineDiscover);
        underlines.add(underlineNearby);

        // 初始化样式：发现默认选中
        selectTab(tabs, underlines, 1);

        tvTabFollow.setOnClickListener(view -> {
            if (currentTabIndex != 0) {
                currentTabIndex = 0;
                selectTab(tabs, underlines, 0);
                switchContent(0);
            }
        });

        tvTabDiscover.setOnClickListener(view -> {
            if (currentTabIndex != 1) {
                currentTabIndex = 1;
                selectTab(tabs, underlines, 1);
                switchContent(1);
            }
        });

        tvTabNearby.setOnClickListener(view -> {
            if (currentTabIndex != 2) {
                currentTabIndex = 2;
                selectTab(tabs, underlines, 2);
                switchContent(2);
            }
        });
    }

    /**
     * 选中指定标签并显示对应下划线
     * 
     * @param tabIndex 标签索引（0=关注, 1=发现, 2=附近）
     */
    private void selectTab(List<TextView> tabs, List<View> underlines, int tabIndex) {
        for (int i = 0; i < tabs.size(); i++) {
            if (i == tabIndex) {
                // 选中状态：深色+加粗+显示下划线
                tabs.get(i).setTextColor(Color.parseColor("#333333"));
                tabs.get(i).setTypeface(null, Typeface.BOLD);
                underlines.get(i).setVisibility(View.VISIBLE);
            } else {
                // 未选中状态：灰色+普通+隐藏下划线
                tabs.get(i).setTextColor(Color.parseColor("#666666"));
                tabs.get(i).setTypeface(null, Typeface.NORMAL);
                underlines.get(i).setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 切换内容区域
     * 
     * @param tabIndex 标签索引（0=关注, 1=发现, 2=附近）
     */
    private void switchContent(int tabIndex) {
        if (gridPets == null)
            return;

        gridPets.removeAllViews();

        if (tabIndex == 1) {
            // 发现页：显示宠物卡片数据
            renderPetCards();
        } else if (tabIndex == 0) {
            // 关注页：加载并显示关注用户的帖子
            loadFollowingPosts();
        } else {
            // 附近页：显示空状态或提示
            showEmptyState("附近暂无内容", "开启定位后查看附近的萌宠");
        }
    }

    /**
     * 加载关注用户的帖子
     */
    private void loadFollowingPosts() {
        List<PetDataManager.PetNoteEntity> followingNotes = dataManager.getFollowingNotes();

        if (followingNotes == null || followingNotes.isEmpty()) {
            showEmptyState("还没有关注的用户", "去发现页面看看吧~");
            return;
        }

        List<PetItem> tempList = new ArrayList<>();
        for (PetDataManager.PetNoteEntity note : followingNotes) {
            if (note == null) {
                continue;
            }

            String imageRes = note.getImageRes();
            boolean isFileImage = imageRes != null && imageRes.startsWith("/");
            
            int imageResId = 0;
            String imageUri = null;
            
            if (isFileImage) {
                imageUri = imageRes;
            } else {
                imageResId = dataManager.getResourceIdByName(imageRes);
            }

            String avatarRes = note.getAvatarRes();
            boolean isAvatarFile = avatarRes != null && avatarRes.startsWith("/");
            
            int avatarResId = 0;
            String avatarUri = null;
            
            if (isAvatarFile) {
                avatarUri = avatarRes;
            } else {
                avatarResId = dataManager.getResourceIdByName(avatarRes);
            }

            PetItem item = new PetItem(
                    imageResId,
                    imageUri,
                    note.getTitle(),
                    avatarResId,
                    avatarUri,
                    note.getAuthorName(),
                    note.getLikeCount(),
                    note.getCollectCount(),
                    note.getCommentCount(),
                    note.getContent(),
                    note.getLocation(),
                    note.getPublishTime(),
                    note.getId(),
                    note.isLiked(),
                    note.isCollected());
            tempList.add(item);
        }

        if (tempList.isEmpty()) {
            showEmptyState("还没有关注的用户", "去发现页面看看吧~");
            return;
        }

        renderPetCards(tempList);
    }

    /**
     * 显示空状态页面
     */
    private void showEmptyState(String title, String subtitle) {
        View emptyView = LayoutInflater.from(activity).inflate(R.layout.item_empty_state, gridPets, false);

        TextView tvTitle = emptyView.findViewById(R.id.tv_empty_title);
        TextView tvSubtitle = emptyView.findViewById(R.id.tv_empty_subtitle);

        if (tvTitle != null)
            tvTitle.setText(title);
        if (tvSubtitle != null)
            tvSubtitle.setText(subtitle);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = GridLayout.LayoutParams.MATCH_PARENT;
        params.height = GridLayout.LayoutParams.MATCH_PARENT;
        params.columnSpec = GridLayout.spec(0, 2); // 占据两列
        emptyView.setLayoutParams(params);

        gridPets.addView(emptyView);
    }

    private int dpToPx(int dp) {
        return (int) (dp * activity.getResources().getDisplayMetrics().density);
    }

    // PetItem 内部类
    public static class PetItem {
        private final int imageRes;
        private final String imageUri;
        private final String title;
        private final int avatarRes;
        private final String avatarUri; // 头像的字符串表示（资源名或文件路径）
        private final String username;
        private int likeCount;
        private int collectCount;
        private int commentCount;
        private String content;
        private String location;
        private String publishTime;
        private int noteId;
        private boolean isLiked;
        private boolean isCollected;

        public PetItem(int imageRes, String title, int avatarRes, String username,
                int likeCount, int collectCount, int commentCount,
                String content, String location, String publishTime,
                int noteId, boolean isLiked) {
            this(imageRes, title, avatarRes, username, likeCount, collectCount, commentCount,
                    content, location, publishTime, noteId, isLiked, false);
        }

        public PetItem(int imageRes, String title, int avatarRes, String username,
                int likeCount, int collectCount, int commentCount,
                String content, String location, String publishTime,
                int noteId, boolean isLiked, boolean isCollected) {
            this(imageRes, null, title, avatarRes, null, username, likeCount, collectCount, commentCount,
                    content, location, publishTime, noteId, isLiked, isCollected);
        }

        public PetItem(int imageRes, String imageUri, String title, int avatarRes, String username,
                int likeCount, int collectCount, int commentCount,
                String content, String location, String publishTime,
                int noteId, boolean isLiked, boolean isCollected) {
            this(imageRes, imageUri, title, avatarRes, null, username, likeCount, collectCount, commentCount,
                    content, location, publishTime, noteId, isLiked, isCollected);
        }

        public PetItem(int imageRes, String imageUri, String title, int avatarRes, String avatarUri, String username,
                int likeCount, int collectCount, int commentCount,
                String content, String location, String publishTime,
                int noteId, boolean isLiked, boolean isCollected) {
            this.imageRes = imageRes;
            this.imageUri = imageUri;
            this.title = title;
            this.avatarRes = avatarRes;
            this.avatarUri = avatarUri;
            this.username = username;
            this.likeCount = likeCount;
            this.collectCount = collectCount;
            this.commentCount = commentCount;
            this.content = content;
            this.location = location;
            this.publishTime = publishTime;
            this.noteId = noteId;
            this.isLiked = isLiked;
            this.isCollected = isCollected;
        }

        public int getImageRes() {
            return imageRes;
        }

        public String getImageUri() {
            return imageUri;
        }

        public boolean hasUriImage() {
            return imageUri != null && !imageUri.isEmpty();
        }

        public String getTitle() {
            return title;
        }

        public int getAvatarRes() {
            return avatarRes;
        }

        public String getAvatarUri() {
            return avatarUri;
        }

        public String getUsername() {
            return username;
        }

        public int getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(int likeCount) {
            this.likeCount = likeCount;
        }

        public int getCollectCount() {
            return collectCount;
        }

        public int getCommentCount() {
            return commentCount;
        }

        public String getContent() {
            return content;
        }

        public String getLocation() {
            return location;
        }

        public String getPublishTime() {
            return publishTime;
        }

        public int getNoteId() {
            return noteId;
        }

        public boolean isLiked() {
            return isLiked;
        }

        public void setLiked(boolean liked) {
            this.isLiked = liked;
        }

        public boolean isCollected() {
            return isCollected;
        }

        public void setCollected(boolean collected) {
            this.isCollected = collected;
        }
    }
}