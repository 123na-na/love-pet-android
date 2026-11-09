package com.example.love_pet;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private boolean isLiked = false;
    private boolean isCollected = false;
    private int likeCount = 0;
    private int collectCount = 0;
    private int commentCount = 0;
    private int noteId = 0;
    private int avatarRes = 0;
    private String avatarUri = null; // 头像的字符串表示（资源名或文件路径）

    // 帖子数据（通过 Intent 传入）
    private String postTitle = "可爱的小猫咪";
    private String postContent = "今天又是元气满满的一天~";
    private String username = "萌宠小柯";
    private String authorName = "萌宠小柯"; // 博主昵称
    private String location = "北京";
    private String publishTime = "昨天 15:20";
    private int imageRes = 0;
    private String imageUri = null;

    // 评论数据
    private List<Comment> comments = new ArrayList<>();

    // 当前登录用户信息（从UserManager获取）
    private String myNickname;
    private static final String MY_IP = "广东";
    private int myAvatarRes = 0;
    private String myCustomAvatarPath = null; // 用户上传的自定义头像本地路径

    // 评论对话框相关
    private android.app.Dialog commentDialog;
    private long replyingToCommentId = -1; // 正在回复的评论ID，-1表示直接评论
    private static final int REQUEST_CODE_PICK_COMMENT_IMAGE = 2001;
    private Uri selectedCommentImageUri = null;
    private String replyToUsername = null;

    private WeChatShareManager weChatShareManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 接收帖子数据
        if (getIntent() != null) {
            noteId = getIntent().getIntExtra("noteId", 0);

            postTitle = getIntent().getStringExtra("title");
            if (postTitle == null)
                postTitle = "可爱的小猫咪";

            postContent = getIntent().getStringExtra("content");
            if (postContent == null)
                postContent = "今天又是元气满满的一天~";

            username = getIntent().getStringExtra("username");
            if (username == null)
                username = "萌宠小柯";

            authorName = username; // 博主昵称等于用户名

            location = getIntent().getStringExtra("location");
            if (location == null)
                location = "北京";

            publishTime = getIntent().getStringExtra("publishTime");
            if (publishTime == null)
                publishTime = "昨天 15:20";

            likeCount = getIntent().getIntExtra("likeCount", 2000);
            collectCount = getIntent().getIntExtra("collectCount", 1000);
            commentCount = getIntent().getIntExtra("commentCount", 300);
            isLiked = getIntent().getBooleanExtra("isLiked", false);
            isCollected = getIntent().getBooleanExtra("isCollected", false);
            imageRes = getIntent().getIntExtra("imageRes", 0);
            imageUri = getIntent().getStringExtra("imageUri");
            avatarRes = getIntent().getIntExtra("avatarRes", 0);
            avatarUri = getIntent().getStringExtra("avatarUri");
        }

        // 从数据库加载评论
        loadCommentsFromDatabase();

        // 初始化当前用户头像（萌宠1号使用固定头像）
        initMyAvatar();

        initViews();
        setupViewPager();
        setupClickListeners();

        weChatShareManager = new WeChatShareManager(this);
    }

    private void loadCommentsFromDatabase() {
        PetDataManager dataManager = PetDataManager.getInstance(this);
        comments = dataManager.getCommentsByNoteId(noteId);
        setupCommentList();
    }

    private void initViews() {
        TextView tvUsername = findViewById(R.id.tv_username_detail);
        TextView tvTitle = findViewById(R.id.tv_title_detail);
        TextView tvContent = findViewById(R.id.tv_content_detail);
        TextView tvLocation = findViewById(R.id.tv_location_detail);
        TextView tvTime = findViewById(R.id.tv_time_detail);

        tvUsername.setText(username);
        tvTitle.setText(postTitle);
        tvContent.setText(postContent);
        tvLocation.setText(location);
        tvTime.setText(publishTime);

        // 设置用户头像
        ImageView ivAvatar = findViewById(R.id.iv_avatar_detail);
        if (avatarUri != null && !avatarUri.isEmpty()) {
            java.io.File avatarFile = new java.io.File(avatarUri);
            if (avatarFile.exists()) {
                Glide.with(this).load(avatarFile).into(ivAvatar);
            } else {
                int resId = getResources().getIdentifier(avatarUri, "drawable", getPackageName());
                if (resId != 0) {
                    ivAvatar.setImageResource(resId);
                } else {
                    ivAvatar.setImageResource(R.drawable.nav_profile_normal);
                }
            }
        } else if (avatarRes != 0) {
            ivAvatar.setImageResource(avatarRes);
        } else {
            ivAvatar.setImageResource(R.drawable.nav_profile_normal);
        }
        // 设置详情页头像为圆形
        ivAvatar.setClipToOutline(true);
        ivAvatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });

        updateLikeUI();
        updateCollectUI();
        ((TextView) findViewById(R.id.tv_like_count)).setText(String.valueOf(likeCount));
        ((TextView) findViewById(R.id.tv_collect_count)).setText(String.valueOf(collectCount));
        ((TextView) findViewById(R.id.tv_comment_count)).setText(String.valueOf(commentCount));

        // 初始化关注按钮状态（从数据库读取）
        initFollowButton();

        // 返回按钮
        ImageView ivBack = findViewById(R.id.iv_back_detail);
        ivBack.setOnClickListener(v -> finish());
    }

    /**
     * 初始化关注按钮状态
     */
    private void initFollowButton() {
        TextView btnFollow = findViewById(R.id.btn_follow_detail);
        boolean isFollowing = PetDataManager.getInstance(this).isFollowing(authorName);
        btnFollow.setSelected(isFollowing);

        if (isFollowing) {
            btnFollow.setText("已关注");
            btnFollow.setTextColor(0xFF999999);
            btnFollow.setBackgroundResource(R.drawable.bg_follow_button_followed);
        } else {
            btnFollow.setText("关注");
            btnFollow.setTextColor(0xFF8B5A00);
            btnFollow.setBackgroundResource(R.drawable.bg_follow_button);
        }
    }

    private void setupViewPager() {
        ViewPager viewPager = findViewById(R.id.vp_images);

        // 使用帖子实际图片（支持文件路径和资源ID）
        List<Object> images = new ArrayList<>();
        if (imageUri != null && !imageUri.isEmpty()) {
            images.add(imageUri);
        } else if (imageRes != 0) {
            images.add(imageRes);
        } else {
            // 默认示例图片列表
            images.add(R.drawable.img_cat_hua);
            images.add(R.drawable.vaccine);
            images.add(R.drawable.bathing);
            images.add(R.drawable.cat_food);
            images.add(R.drawable.litter_box);
            images.add(R.drawable.scissors);
            images.add(R.drawable.ic_paw_black);
            images.add(R.drawable.editing);
            images.add(R.drawable.shoucang);
        }

        PagerAdapter adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return images.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(View container, int position) {
                ImageView imageView = new ImageView(DetailActivity.this);
                imageView.setLayoutParams(new ViewPager.LayoutParams());
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Object imageData = images.get(position);
                if (imageData instanceof String) {
                    Glide.with(DetailActivity.this)
                            .load(new File((String) imageData))
                            .into(imageView);
                } else {
                    imageView.setImageResource((Integer) imageData);
                }

                ((ViewPager) container).addView(imageView);
                return imageView;
            }

            @Override
            public void destroyItem(View container, int position, Object object) {
                ((ViewPager) container).removeView((View) object);
            }
        };

        viewPager.setAdapter(adapter);

        // 解决 ScrollView 与 ViewPager 滑动冲突
        viewPager.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    if (v.getParent() != null) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    break;
            }
            return false;
        });

        // 分页指示器
        TextView tvIndicator = findViewById(R.id.tv_page_indicator);
        LinearLayout layoutDots = findViewById(R.id.layout_dots);

        // 创建分页点
        for (int i = 0; i < images.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
            dotParams.setMarginEnd(dpToPx(4));
            dot.setLayoutParams(dotParams);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            if (i == 0) {
                drawable.setColor(0xFF333333);
            } else {
                drawable.setColor(0xFFCCCCCC);
            }
            dot.setBackground(drawable);
            layoutDots.addView(dot);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                tvIndicator.setText((position + 1) + "/" + images.size());
                for (int i = 0; i < layoutDots.getChildCount(); i++) {
                    View dot = layoutDots.getChildAt(i);
                    GradientDrawable drawable = new GradientDrawable();
                    drawable.setShape(GradientDrawable.OVAL);
                    if (i == position) {
                        drawable.setColor(0xFF333333);
                    } else {
                        drawable.setColor(0xFFCCCCCC);
                    }
                    dot.setBackground(drawable);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void setupClickListeners() {
        // 分享按钮 - 弹出分享菜单
        ImageView ivShare = findViewById(R.id.iv_share_detail);
        ivShare.setOnClickListener(v -> showShareMenu());

        // 关注按钮
        TextView btnFollow = findViewById(R.id.btn_follow_detail);
        btnFollow.setOnClickListener(v -> {
            btnFollow.setSelected(!btnFollow.isSelected());
            if (btnFollow.isSelected()) {
                btnFollow.setText("已关注");
                btnFollow.setTextColor(0xFF999999);
                btnFollow.setBackgroundResource(R.drawable.bg_follow_button_followed);
                // 将该用户加入关注列表
                addToFollowingList(authorName);
            } else {
                btnFollow.setText("关注");
                btnFollow.setTextColor(0xFF8B5A00);
                btnFollow.setBackgroundResource(R.drawable.bg_follow_button);
                // 从关注列表移除
                removeFromFollowingList(authorName);
            }
        });

        // 喜欢/取消喜欢（同步到数据库）
        View btnLike = findViewById(R.id.btn_like_detail);
        btnLike.setOnClickListener(v -> {
            isLiked = !isLiked;
            if (isLiked) {
                likeCount++;
                sendLikeNotificationToAuthor();
            } else {
                likeCount--;
            }
            updateLikeUI();
            ((TextView) findViewById(R.id.tv_like_count)).setText(String.valueOf(likeCount));
            // 同步到数据库，确保返回列表页时状态一致
            PetDataManager.getInstance(this).updateLikeStatus(noteId, isLiked, likeCount);
        });

        // 收藏/取消收藏（同步到数据库）
        View btnCollect = findViewById(R.id.btn_collect_detail);
        btnCollect.setOnClickListener(v -> {
            isCollected = !isCollected;
            if (isCollected)
                collectCount++;
            else
                collectCount--;
            updateCollectUI();
            ((TextView) findViewById(R.id.tv_collect_count)).setText(String.valueOf(collectCount));
            // 同步到数据库
            PetDataManager.getInstance(this).updateCollectStatus(noteId, isCollected, collectCount);
        });

        // 评论输入框
        View btnCommentInput = findViewById(R.id.btn_comment_input);
        btnCommentInput.setOnClickListener(v -> showCommentDialog(null, -1));

        // 评论按钮
        View btnComment = findViewById(R.id.btn_comment_detail);
        btnComment.setOnClickListener(v -> showCommentDialog(null, -1));
    }

    private void updateLikeUI() {
        ImageView ivLike = findViewById(R.id.iv_like_detail);
        if (isLiked) {
            ivLike.setImageResource(R.drawable.ic_heart_red);
        } else {
            ivLike.setImageResource(R.drawable.ic_heart_outline);
        }
    }

    private void updateCollectUI() {
        ImageView ivCollect = findViewById(R.id.iv_collect_detail);
        if (isCollected) {
            ivCollect.setImageResource(R.drawable.shoucang_filled);
        } else {
            ivCollect.setImageResource(R.drawable.shoucang);
        }
    }

    /**
     * 显示分享菜单（在分享图标右下方弹出）
     */
    private void showShareMenu() {
        ImageView ivShare = findViewById(R.id.iv_share_detail);

        PopupMenu popupMenu = new PopupMenu(this, ivShare);
        popupMenu.getMenuInflater().inflate(R.menu.menu_share, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_save_image:
                    saveShareImage();
                    return true;
                case R.id.menu_copy_link:
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(
                            CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("分享链接",
                            "https://example.com/pet/note/" + noteId);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(DetailActivity.this, "链接已复制", Toast.LENGTH_SHORT).show();
                    return true;
                case R.id.menu_report:
                    Intent reportIntent = new Intent(DetailActivity.this, ReportActivity.class);
                    reportIntent.putExtra("post_title", postTitle);
                    reportIntent.putExtra("note_id", noteId);
                    startActivity(reportIntent);
                    return true;
                case R.id.menu_forward:
                    showShareToWeChat();
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void showShareToWeChat() {
        try {
            Bitmap shareBitmap = createShareCard();
            if (shareBitmap != null) {
                File shareFile = saveBitmapToFile(shareBitmap);
                if (shareFile != null) {
                    Uri shareUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", shareFile);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.setPackage("com.tencent.mm");
                    intent.putExtra(Intent.EXTRA_STREAM, shareUri);
                    intent.putExtra(Intent.EXTRA_TEXT, postTitle + "\n" + "@" + username + " 的笔记\n\n来自：爱宠APP");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "分享到"));
                } else {
                    shareTextOnly();
                }
            } else {
                shareTextOnly();
            }
        } catch (Exception e) {
            shareTextOnly();
        }
    }

    private void shareTextOnly() {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setPackage("com.tencent.mm");
            intent.putExtra(Intent.EXTRA_TEXT, postTitle + "\n@" + username + " 的笔记\n" + likeCount + "人点赞\n\n来自：爱宠APP");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "请先安装微信", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap createShareCard() {
        try {
            View cardView = LayoutInflater.from(this).inflate(R.layout.share_card_xhs, null);

            ImageView ivCover = cardView.findViewById(R.id.iv_share_cover);
            TextView tvTitle = cardView.findViewById(R.id.tv_share_title);
            TextView tvAuthor = cardView.findViewById(R.id.tv_share_author);
            TextView tvCount = cardView.findViewById(R.id.tv_share_count);

            tvTitle.setText(postTitle);
            tvAuthor.setText("@" + username + " 的笔记");
            tvCount.setText(likeCount + "人点赞");

            if (imageRes != 0) {
                ivCover.setImageResource(imageRes);
            } else if (imageUri != null && !imageUri.isEmpty()) {
                File imgFile = new File(imageUri);
                if (imgFile.exists()) {
                    Glide.with(this).load(imgFile).into(ivCover);
                } else {
                    ivCover.setImageResource(R.drawable.ic_paw_black);
                }
            } else {
                ivCover.setImageResource(R.drawable.ic_paw_black);
            }

            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(dpToPx(320), View.MeasureSpec.EXACTLY);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            cardView.measure(widthMeasureSpec, heightMeasureSpec);
            cardView.layout(0, 0, cardView.getMeasuredWidth(), cardView.getMeasuredHeight());

            Bitmap bitmap = Bitmap.createBitmap(cardView.getMeasuredWidth(), cardView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            cardView.draw(canvas);

            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private File saveBitmapToFile(Bitmap bitmap) {
        try {
            File cacheDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File shareFile = new File(cacheDir, "share_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(shareFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return shareFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveShareImage() {
        try {
            Bitmap shareBitmap = createShareCard();
            if (shareBitmap != null) {
                String fileName = "share_" + System.currentTimeMillis() + ".png";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                }
                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    OutputStream out = getContentResolver().openOutputStream(uri);
                    shareBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                    Toast.makeText(DetailActivity.this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Toast.makeText(DetailActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(DetailActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 将用户加入关注列表
     */
    private void addToFollowingList(String username) {
        PetDataManager.getInstance(this).followUser(username, "");
        Toast.makeText(this, "已关注 " + username, Toast.LENGTH_SHORT).show();
    }

    /**
     * 从关注列表移除用户
     */
    private void removeFromFollowingList(String username) {
        PetDataManager.getInstance(this).unfollowUser(username);
        Toast.makeText(this, "已取消关注 " + username, Toast.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // 设置评论列表（支持回复嵌套 + 展开/收起）
    private void setupCommentList() {
        LinearLayout layoutComments = findViewById(R.id.layout_comments);
        layoutComments.removeAllViews();

        // 设置"共 X 条评论 ≡"标题
        TextView tvCommentsTitle = findViewById(R.id.tv_comments_title);
        tvCommentsTitle.setText("共 " + comments.size() + " 条评论 ≡");

        // 分离顶级评论和回复评论
        List<Comment> topComments = new ArrayList<>();
        java.util.Map<Long, List<Comment>> replyMap = new java.util.HashMap<>();

        for (Comment comment : comments) {
            if (comment.getParentCommentId() < 0) {
                topComments.add(comment);
            } else {
                long parentId = comment.getParentCommentId();
                if (!replyMap.containsKey(parentId)) {
                    replyMap.put(parentId, new ArrayList<>());
                }
                replyMap.get(parentId).add(comment);
            }
        }

        // 渲染顶级评论及其回复（带展开/收起）
        for (Comment comment : topComments) {
            // 渲染顶级评论
            addCommentView(layoutComments, comment, false);

            // 如果有回复，创建回复容器（默认隐藏）+ 展开/收起按钮
            List<Comment> replies = replyMap.get(comment.getId());
            if (replies != null && !replies.isEmpty()) {
                // 回复容器
                LinearLayout replyContainer = new LinearLayout(this);
                replyContainer.setOrientation(LinearLayout.VERTICAL);
                replyContainer.setVisibility(View.GONE); // 默认隐藏

                for (Comment reply : replies) {
                    addCommentView(replyContainer, reply, true);
                }
                layoutComments.addView(replyContainer);

                // 展开/收起按钮
                TextView tvExpandCollapse = new TextView(this);
                int replyCount = replies.size();
                tvExpandCollapse.setText("展开 " + replyCount + " 条回复");
                tvExpandCollapse.setTextColor(0xFF999999);
                tvExpandCollapse.setTextSize(13f);
                tvExpandCollapse.setPadding(56, 6, 16, 6);
                final LinearLayout finalReplyContainer = replyContainer;
                final TextView finalTvExpandCollapse = tvExpandCollapse;
                final int finalReplyCount = replyCount;

                tvExpandCollapse.setOnClickListener(v -> {
                    if (finalReplyContainer.getVisibility() == View.GONE) {
                        finalReplyContainer.setVisibility(View.VISIBLE);
                        finalTvExpandCollapse.setText("收起");
                        finalTvExpandCollapse.setTextColor(0xFF666666);
                    } else {
                        finalReplyContainer.setVisibility(View.GONE);
                        finalTvExpandCollapse.setText("展开 " + finalReplyCount + " 条回复");
                        finalTvExpandCollapse.setTextColor(0xFF999999);
                    }
                });
                layoutComments.addView(tvExpandCollapse);
            }
        }

        // 处理孤儿回复（找不到父评论的，直接显示）
        for (Comment comment : comments) {
            if (comment.getParentCommentId() >= 0) {
                boolean hasParentInTop = false;
                for (Comment top : topComments) {
                    if (top.getId() == comment.getParentCommentId()) {
                        hasParentInTop = true;
                        break;
                    }
                }
                if (!hasParentInTop) {
                    addCommentView(layoutComments, comment, true);
                }
            }
        }
    }

    /**
     * 添加单条评论视图到容器
     */
    private void addCommentView(LinearLayout container, Comment comment, boolean isReply) {
        View commentView = LayoutInflater.from(this).inflate(R.layout.item_comment, container, false);

        // 回复评论增加左边距（缩进效果）
        if (isReply) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) commentView.getLayoutParams();
            params.setMarginStart(56); // 缩进到头像右侧
            commentView.setLayoutParams(params);
            // 回复评论背景略灰
            commentView.setBackgroundColor(0x0A000000);
        }

        TextView tvCommentUsername = commentView.findViewById(R.id.tv_comment_username);
        TextView tvCommentContent = commentView.findViewById(R.id.tv_comment_content);
        TextView tvCommentTime = commentView.findViewById(R.id.tv_comment_time);
        TextView tvCommentIp = commentView.findViewById(R.id.tv_comment_ip);
        TextView tvCommentLikeCount = commentView.findViewById(R.id.tv_comment_like_count);
        ImageView ivCommentAvatar = commentView.findViewById(R.id.iv_comment_avatar);
        ImageView ivCommentImage = commentView.findViewById(R.id.iv_comment_image);

        tvCommentUsername.setText(comment.getUsername());
        tvCommentContent.setText(comment.getContent());
        tvCommentTime.setText(comment.getTime());

        // 显示评论图片
        if (comment.getImagePath() != null && !comment.getImagePath().isEmpty()) {
            ivCommentImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(new java.io.File(comment.getImagePath()))
                    .into(ivCommentImage);
        } else {
            ivCommentImage.setVisibility(View.GONE);
        }

        if (comment.getIpLocation() != null) {
            tvCommentIp.setText(comment.getIpLocation());
        }
        tvCommentLikeCount.setText(String.valueOf(comment.getLikeCount()));

        // 点赞图标点击事件
        ImageView ivCommentLikeIcon = commentView.findViewById(R.id.iv_comment_like_icon);
        final boolean[] isCommentLiked = { false }; // 使用数组以便在匿名内部类中修改
        ivCommentLikeIcon.setOnClickListener(v -> {
            isCommentLiked[0] = !isCommentLiked[0];
            if (isCommentLiked[0]) {
                ivCommentLikeIcon.setImageResource(R.drawable.ic_like_selected);
                comment.setLikeCount(comment.getLikeCount() + 1);
            } else {
                ivCommentLikeIcon.setImageResource(R.drawable.ic_like_normal);
                comment.setLikeCount(comment.getLikeCount() - 1);
            }
            tvCommentLikeCount.setText(String.valueOf(comment.getLikeCount()));
            // 保存到数据库
            if (comment.getId() > 0) {
                PetDataManager.getInstance(DetailActivity.this).updateCommentLikeCount(comment.getId(),
                        comment.getLikeCount());
            }
        });

        // 回复按钮点击事件
        android.widget.LinearLayout replyArea = (android.widget.LinearLayout) commentView
                .findViewById(R.id.tv_comment_time).getParent();
        final String targetUsername = comment.getUsername();
        final long targetCommentId = comment.getId(); // 需要在Comment中添加id字段
        for (int i = 0; i < replyArea.getChildCount(); i++) {
            View child = replyArea.getChildAt(i);
            if (child instanceof TextView && "回复".equals(((TextView) child).getText().toString())) {
                child.setOnClickListener(v -> showCommentDialog(targetUsername, targetCommentId));
                break;
            }
        }

        // 根据评论用户名设置对应头像（当前用户优先使用自定义头像）
        if (comment.getUsername() != null && comment.getUsername().equals(myNickname)) {
            // 当前用户优先使用自定义头像
            if (myCustomAvatarPath != null && new java.io.File(myCustomAvatarPath).exists()) {
                AvatarManager.loadAvatar(this, ivCommentAvatar);
            } else {
                ivCommentAvatar.setImageResource(R.drawable.nav_profile_normal);
            }
        } else {
            int commentAvatarRes = getAvatarResByUsername(comment.getUsername());
            if (commentAvatarRes != 0) {
                ivCommentAvatar.setImageResource(commentAvatarRes);
            } else {
                // 确保有默认头像
                ivCommentAvatar.setImageResource(R.drawable.ic_paw_black);
            }
        }
        ivCommentAvatar.setClipToOutline(true);
        ivCommentAvatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });

        container.addView(commentView);
    }

    /**
     * 初始化当前用户头像和昵称（优先使用用户上传的自定义头像）
     */
    private void initMyAvatar() {
        myNickname = UserManager.getInstance().getCurrentNickname();

        // 优先检查用户是否上传了自定义头像
        myCustomAvatarPath = AvatarManager.getSavedAvatarPath(this);
        if (myCustomAvatarPath != null && new java.io.File(myCustomAvatarPath).exists()) {
            // 有自定义头像，myAvatarRes 留 0 表示走文件路径加载
            myAvatarRes = 0;
        } else {
            // 无自定义头像，使用默认资源
            myAvatarRes = R.drawable.nav_profile_normal;
            myCustomAvatarPath = null;
        }
    }

    /**
     * 根据用户名从 MODERN_NICKNAMES 查找对应的头像资源ID
     */
    private int getAvatarResByUsername(String username) {
        if (username == null || username.isEmpty()) {
            return R.drawable.ic_paw_black;
        }
        if (username.equals(myNickname)) {
            if (myCustomAvatarPath != null && new java.io.File(myCustomAvatarPath).exists()) {
                return 0;
            }
            return myAvatarRes != 0 ? myAvatarRes : R.drawable.nav_profile_normal;
        }
        String[] nicknames = PetDataManager.MODERN_NICKNAMES;
        for (int i = 0; i < nicknames.length; i++) {
            if (nicknames[i].equals(username)) {
                return PetDataManager.getInstance(this).getAvatarResourceByIndex(i);
            }
        }
        return R.drawable.ic_paw_black;
    }

    /**
     * 显示自定义评论输入对话框（仿小红书底部栏样式）
     * 
     * @param replyTo         被回复的用户名，null表示直接评论
     * @param parentCommentId 被回复的评论ID，-1表示直接评论
     */
    private void showCommentDialog(String replyTo, long parentCommentId) {
        replyingToCommentId = parentCommentId;
        replyToUsername = replyTo;
        selectedCommentImageUri = null;

        // 使用普通Dialog主题（非透明），确保正确浮层
        commentDialog = new android.app.Dialog(this, android.R.style.Theme_Material_Light_Dialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_comment_input, null);

        ImageView ivUserAvatar = dialogView.findViewById(R.id.iv_dialog_user_avatar);
        TextView tvUserName = dialogView.findViewById(R.id.tv_dialog_user_name);
        EditText etInput = dialogView.findViewById(R.id.et_comment_input);
        View btnClose = dialogView.findViewById(R.id.btn_dialog_close);
        TextView btnSend = dialogView.findViewById(R.id.btn_send_comment);
        TextView btnEmoji = dialogView.findViewById(R.id.btn_emoji);
        TextView btnImage = dialogView.findViewById(R.id.btn_image);

        // 显示当前登录用户信息
        tvUserName.setText(myNickname);
        if (myCustomAvatarPath != null) {
            AvatarManager.loadAvatar(this, ivUserAvatar);
        } else if (myAvatarRes != 0) {
            ivUserAvatar.setImageResource(myAvatarRes);
        }
        ivUserAvatar.setClipToOutline(true);
        ivUserAvatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });

        // 输入框提示文字
        if (replyTo != null) {
            etInput.setHint("回复 @" + replyTo);
        } else {
            etInput.setHint("说点什么...");
        }

        // emoji按钮点击
        btnEmoji.setOnClickListener(v -> showEmojiPicker(etInput));

        // 图片按钮点击
        btnImage.setOnClickListener(v -> pickCommentImage());

        // 发送按钮点击
        btnSend.setOnClickListener(v -> {
            String content = etInput.getText().toString().trim();
            if (!TextUtils.isEmpty(content) || selectedCommentImageUri != null) {
                submitComment(content, replyTo, selectedCommentImageUri);
                dismissDialog();
            } else {
                Toast.makeText(this, "请输入评论内容或选择图片", Toast.LENGTH_SHORT).show();
            }
        });

        // 关闭按钮
        btnClose.setOnClickListener(v -> dismissDialog());

        // 点击对话框外部区域关闭
        dialogView.setOnClickListener(v -> {
        }); // 防止穿透
        commentDialog.setCanceledOnTouchOutside(true);

        commentDialog.setContentView(dialogView);

        // 设置窗口属性
        android.view.Window window = commentDialog.getWindow();
        // 底部弹出
        window.setGravity(android.view.Gravity.BOTTOM);
        window.setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        // 圆角矩形背景
        window.setBackgroundDrawableResource(R.drawable.bg_dialog_rounded);
        // 动画：从底部滑入
        window.setWindowAnimations(android.R.style.Animation_InputMethod);
        // 关键：对话框显示时自动弹出键盘
        window.setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE |
                        android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        commentDialog.show();

        // 聚焦输入框（键盘由窗口属性自动触发）
        etInput.requestFocus();
    }

    /**
     * 显示emoji选择器
     */
    private void showEmojiPicker(EditText etInput) {
        String[] emojis = {
                "😀", "😂", "🥰", "😎", "🤔", "😭", "😡", "👍",
                "👎", "❤️", "🎉", "🔥", "✨", "💯", "🙏", "👏",
                "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择表情");

        View emojiView = LayoutInflater.from(this).inflate(R.layout.emoji_picker_grid, null);
        android.widget.GridView gridView = emojiView.findViewById(R.id.gv_emoji);
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, emojis) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextSize(24f);
                tv.setGravity(android.view.Gravity.CENTER);
                tv.setPadding(0, 20, 0, 20);
                return tv;
            }
        };
        gridView.setAdapter(adapter);
        gridView.setNumColumns(8);

        builder.setView(emojiView);
        AlertDialog dialog = builder.create();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            String emoji = emojis[position];
            int start = etInput.getSelectionStart();
            etInput.getText().insert(start, emoji);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * 选择评论图片
     */
    private void pickCommentImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_COMMENT_IMAGE);
    }

    private void dismissDialog() {
        if (commentDialog != null && commentDialog.isShowing()) {
            // 关闭键盘
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(commentDialog.getCurrentFocus().getWindowToken(), 0);
            commentDialog.dismiss();
            commentDialog = null;
        }
    }

    /**
     * 提交评论到数据库并刷新列表
     * 
     * @param content  评论内容
     * @param replyTo  被回复的用户名，null表示直接评论
     * @param imageUri 评论图片Uri，null表示无图片
     */
    private void submitComment(String content, String replyTo, Uri imageUri) {
        PetDataManager dataManager = PetDataManager.getInstance(this);
        String imagePath = null;
        if (imageUri != null) {
            imagePath = saveImageToLocal(imageUri);
        }
        dataManager.insertComment(noteId, myNickname, content, MY_IP, replyTo, replyingToCommentId, imagePath);

        // 刷新评论列表
        loadCommentsFromDatabase();

        // 更新底部评论数显示
        commentCount++;
        ((TextView) findViewById(R.id.tv_comment_count)).setText(String.valueOf(commentCount));

        sendCommentNotificationToAuthor(content);

        Toast.makeText(this, "评论成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 将图片Uri保存到应用私有目录
     */
    private String saveImageToLocal(Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null)
                return null;

            java.io.File filesDir = getFilesDir();
            String fileName = "comment_" + System.currentTimeMillis() + ".jpg";
            java.io.File outputFile = new java.io.File(filesDir, fileName);

            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();
            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_COMMENT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                selectedCommentImageUri = uri;
                Toast.makeText(this, "已选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendLikeNotificationToAuthor() {
        if (authorName == null || authorName.isEmpty() || authorName.equals(myNickname)) {
            return;
        }
        String authorUserId = UserManager.getInstance().getUserIdByNickname(authorName);
        if (authorUserId == null) {
            return;
        }
        String time = new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.CHINA)
                .format(new java.util.Date());
        InteractionMessageManager.InteractionMsg msg = new InteractionMessageManager.InteractionMsg(
                myNickname,
                "赞了你的帖子",
                postContent,
                time,
                myAvatarRes,
                "like");
        InteractionMessageManager.getInstance().addMessageToUser(this, authorUserId, msg);
    }

    private void sendCommentNotificationToAuthor(String commentContent) {
        if (authorName == null || authorName.isEmpty() || authorName.equals(myNickname)) {
            return;
        }
        String authorUserId = UserManager.getInstance().getUserIdByNickname(authorName);
        if (authorUserId == null) {
            return;
        }
        String time = new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.CHINA)
                .format(new java.util.Date());
        InteractionMessageManager.InteractionMsg msg = new InteractionMessageManager.InteractionMsg(
                myNickname,
                "评论了你的帖子",
                commentContent,
                time,
                myAvatarRes,
                "comment");
        InteractionMessageManager.getInstance().addMessageToUser(this, authorUserId, msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissDialog();
    }
}