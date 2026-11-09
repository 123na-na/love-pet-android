package com.example.love_pet;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

import com.yalantis.ucrop.UCrop;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int currentTab = 0;

    private List<View> contentViews;
    private List<NavTab> navTabs;
    private PetPageHelper petPageHelper;
    private DynamicPageHelper dynamicPageHelper;

    // 附近门店轮播图
    private ViewFlipper vfNearbyStores;
    private LinearLayout llStoreDots;
    private int currentStoreBanner = 0;
    private static final int STORE_BANNER_COUNT = 3;
    private Handler storeBannerHandler = new Handler();
    private Runnable storeBannerRunnable;

    // 头像管理器
    private AvatarManager avatarManager;
    private ImageView ivAvatar; // 个人页头像

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserManager.getInstance().loadUsers(this);
        ChatMessageManager.getInstance().loadFromPrefs(this);

        initViews();
        petPageHelper = new PetPageHelper(this);
        petPageHelper.initPetPage(findViewById(R.id.content_pet));
        dynamicPageHelper = new DynamicPageHelper(this);
        dynamicPageHelper.initDynamicPage(findViewById(R.id.content_news));

        // 初始化头像管理器
        initAvatarManager();
    }

    /**
     * 初始化头像上传功能
     */
    private void initAvatarManager() {
        avatarManager = new AvatarManager(this);

        // 获取个人页头像控件
        View profileView = findViewById(R.id.content_profile);
        if (profileView != null) {
            ivAvatar = profileView.findViewById(R.id.iv_avatar);

            // 加载已保存的头像
            if (ivAvatar != null) {
                AvatarManager.loadAvatar(this, ivAvatar);

                // 设置点击事件
                ivAvatar.setOnClickListener(v -> {
                    // 检查权限后显示选择弹窗
                    checkPermissionAndShowPicker();
                });
            }
        }

        // 设置上传监听
        avatarManager.setOnAvatarUploadListener(new AvatarManager.OnAvatarUploadListener() {
            @Override
            public void onUploadStart() {
                // 显示加载提示（可自定义）
            }

            @Override
            public void onUploadSuccess(String avatarUrl) {
                // 上传成功，刷新所有头像显示
                refreshAllAvatars();
            }

            @Override
            public void onUploadFailed(String error) {
                // 错误处理已在 AvatarManager 内部 Toast 提示
            }
        });
    }

    /**
     * 检查权限并显示选择器
     */
    private void checkPermissionAndShowPicker() {
        String[] permissions = AvatarManager.getRequiredPermissions();
        boolean allGranted = true;

        for (String perm : permissions) {
            if (checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            avatarManager.showPickerDialog();
        } else {
            requestPermissions(permissions, AvatarManager.REQUEST_CODE_PERMISSION);
        }
    }

    /**
     * 刷新所有位置的头像显示（同步更新）
     */
    private void refreshAllAvatars() {
        // 1. 刷新个人页头像
        if (ivAvatar != null) {
            AvatarManager.loadAvatar(this, ivAvatar);
        }

        // TODO: 2. 如果侧边栏有头像，也刷新
        // ImageView sidebarAvatar = findViewById(R.id.iv_sidebar_avatar);
        // if (sidebarAvatar != null) {
        // AvatarManager.loadAvatar(this, sidebarAvatar);
        // }

        // TODO: 3. 其他需要显示头像的地方...
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == AvatarManager.REQUEST_CODE_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                avatarManager.showPickerDialog();
            } else {
                // 可选：引导用户去设置页面开启权限
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case AvatarManager.REQUEST_CODE_PICK_IMAGE:
                // 从相册选择 → 开始裁剪
                if (data != null && data.getData() != null) {
                    avatarManager.startCrop(data.getData());
                }
                break;

            case AvatarManager.REQUEST_CODE_TAKE_PHOTO:
                // 拍照完成 → 开始裁剪
                avatarManager.startCrop(avatarManager.getCameraImageUri());
                break;

            case AvatarManager.REQUEST_CODE_CROP:
                // 裁剪完成 → 保存到本地
                Uri croppedUri = UCrop.getOutput(data);
                if (croppedUri != null) {
                    avatarManager.saveAvatarToLocal(croppedUri);
                }
                break;

            case 1000:
                // 发布笔记成功 → 刷新宠图页面
                if (petPageHelper != null) {
                    petPageHelper.refreshData();
                }
                // 自动切换到宠图tab
                selectTab(1);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (petPageHelper != null) {
            petPageHelper.refreshData();
        }
        if (dynamicPageHelper != null) {
            dynamicPageHelper.refreshPetCard(findViewById(R.id.content_news));
        }
        refreshProfileInfo();
        if (storeBannerRunnable == null && vfNearbyStores != null) {
            startStoreBannerAutoPlay();
        }
    }

    private void initViews() {
        // 内容区域
        contentViews = new ArrayList<>();
        contentViews.add(findViewById(R.id.content_home));
        contentViews.add(findViewById(R.id.content_pet));
        contentViews.add(findViewById(R.id.content_scan));
        contentViews.add(findViewById(R.id.content_news));
        contentViews.add(findViewById(R.id.content_profile));

        // 导航栏 tab
        navTabs = new ArrayList<>();
        navTabs.add(new NavTab(
                findViewById(R.id.nav_home),
                findViewById(R.id.iv_nav_home),
                findViewById(R.id.tv_nav_home),
                R.drawable.nav_home_normal,
                R.drawable.nav_home_selected));
        navTabs.add(new NavTab(
                findViewById(R.id.nav_pet),
                findViewById(R.id.iv_nav_pet),
                findViewById(R.id.tv_nav_pet),
                R.drawable.nav_pet_normal,
                R.drawable.nav_pet_selected));
        navTabs.add(new NavTab(
                findViewById(R.id.nav_scan),
                findViewById(R.id.iv_nav_scan),
                findViewById(R.id.tv_nav_scan),
                R.drawable.nav_post,
                R.drawable.nav_post));
        navTabs.add(new NavTab(
                findViewById(R.id.nav_news),
                findViewById(R.id.iv_nav_news),
                findViewById(R.id.tv_nav_news),
                R.drawable.ic_dynamic,
                R.drawable.nav_news_selected2));
        navTabs.add(new NavTab(
                findViewById(R.id.nav_profile),
                findViewById(R.id.iv_nav_profile),
                findViewById(R.id.tv_nav_profile),
                R.drawable.nav_news_normal,
                R.drawable.nav_scan));

        navTabs.get(0).getContainer().setOnClickListener(v -> selectTab(0));
        navTabs.get(1).getContainer().setOnClickListener(v -> selectTab(1));
        navTabs.get(2).getContainer().setOnClickListener(v -> {
            Intent publishIntent = new Intent(MainActivity.this, PublishActivity.class);
            startActivityForResult(publishIntent, 1000);
        });
        navTabs.get(3).getContainer().setOnClickListener(v -> selectTab(3));
        navTabs.get(4).getContainer().setOnClickListener(v -> selectTab(4));

        // 默认选中首页
        selectTab(0);

        // 初始化附近门店轮播图
        initNearbyStoreBanner();

        // 初始化搜索框
        initSearchBar();

        // 联系客服点击跳转
        View profileView = findViewById(R.id.content_profile);
        if (profileView != null) {
            View customerService = profileView.findViewById(R.id.customer_service);
            if (customerService != null) {
                customerService.setOnClickListener(
                        v -> startActivity(new Intent(MainActivity.this, CustomerServiceActivity.class)));
            }

            View feedbackArea = profileView.findViewById(R.id.feedback_area);
            if (feedbackArea != null) {
                feedbackArea.setOnClickListener(
                        v -> startActivity(new Intent(MainActivity.this, FeedbackActivity.class)));
            }

            View tabooArea = profileView.findViewById(R.id.taboo_area);
            if (tabooArea != null) {
                tabooArea.setOnClickListener(
                        v -> startActivity(new Intent(MainActivity.this, PetTabooActivity.class)));
            }

            TextView tvNickname = profileView.findViewById(R.id.tv_nickname);
            TextView tvUserId = profileView.findViewById(R.id.tv_user_id);
            if (tvNickname != null) {
                tvNickname.setText(UserManager.getInstance().getCurrentNickname());
            }
            if (tvUserId != null) {
                String phone = "";
                if (UserManager.getInstance().getCurrentUser() != null) {
                    phone = UserManager.getInstance().getCurrentUser().phone;
                }
                tvUserId.setText("ID：" + (phone.isEmpty() ? "123456" : phone) + "    广东");
            }
        }

        // 宠物医生点击跳转
        View btnServiceShower = findViewById(R.id.btn_service_shower);
        if (btnServiceShower != null) {
            btnServiceShower.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PetDoctorActivity.class)));
        }

        // 美容服务点击跳转
        View btnServiceGrooming = findViewById(R.id.btn_service_grooming);
        if (btnServiceGrooming != null) {
            btnServiceGrooming.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BeautyActivity.class)));
        }

        // 商城服务点击跳转
        View btnServiceShop = findViewById(R.id.btn_service_shop);
        if (btnServiceShop != null) {
            btnServiceShop.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ShopActivity.class)));
        }

        // 领养服务点击跳转
        View btnServiceAdopt = findViewById(R.id.btn_service_adopt);
        if (btnServiceAdopt != null) {
            btnServiceAdopt.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdoptActivity.class)));
        }

        // 公益专区点击跳转
        View welfareArea = findViewById(R.id.welfare_area);
        if (welfareArea != null) {
            welfareArea.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WelfareActivity.class)));
        }

        // 收藏点击跳转
        if (profileView != null) {
            View collectArea = profileView.findViewById(R.id.collect_area);
            if (collectArea != null) {
                collectArea
                        .setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CollectActivity.class)));
            }

            // 钱包点击跳转
            View walletArea = profileView.findViewById(R.id.wallet_area);
            if (walletArea != null) {
                walletArea.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WalletActivity.class)));
            }

            // 设置按钮点击跳转
            View ivSettings = profileView.findViewById(R.id.iv_settings);
            if (ivSettings != null) {
                ivSettings
                        .setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
            }

            // 订单点击跳转
            View orderArea = profileView.findViewById(R.id.order_area);
            if (orderArea != null) {
                orderArea.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OrderActivity.class)));
            }

            // 电子宠物证点击跳转
            View certArea = profileView.findViewById(R.id.layout_pet_certificate);
            if (certArea != null) {
                certArea.setOnClickListener(
                        v -> startActivity(new Intent(MainActivity.this, PetCertificateActivity.class)));
            }

            // 消息点击跳转
            View messageArea = profileView.findViewById(R.id.message_area);
            if (messageArea != null) {
                messageArea.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MessageActivity.class)));
            }

            // 我的宠物点击 → 跳转到动态页面（底部导航第4个tab）
            View myPetArea = profileView.findViewById(R.id.my_pet_area);
            if (myPetArea != null) {
                myPetArea.setOnClickListener(v -> selectTab(3));
            }

            View myNotesArea = profileView.findViewById(R.id.my_notes_area);
            if (myNotesArea != null) {
                myNotesArea.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MyNotesActivity.class)));
            }

            View petLocationArea = profileView.findViewById(R.id.pet_location_area);
            if (petLocationArea != null) {
                petLocationArea.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PetLocationActivity.class)));
            }
        }
    }

    private void initSearchBar() {
        View homeView = findViewById(R.id.content_home);
        if (homeView == null) return;

        android.widget.EditText searchBar = homeView.findViewById(R.id.et_search);
        View searchIcon = homeView.findViewById(R.id.iv_search_icon);

        View.OnClickListener searchListener = v -> {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
        };

        if (searchBar != null) {
            searchBar.setOnClickListener(searchListener);
            searchBar.setFocusable(false);
            searchBar.setCursorVisible(false);
        }
        if (searchIcon != null) {
            searchIcon.setOnClickListener(searchListener);
        }
    }

    private void initNearbyStoreBanner() {
        vfNearbyStores = findViewById(R.id.vf_nearby_stores);
        llStoreDots = findViewById(R.id.ll_store_dots);

        if (vfNearbyStores == null || llStoreDots == null) return;

        for (int i = 0; i < vfNearbyStores.getChildCount(); i++) {
            final int index = i;
            View child = vfNearbyStores.getChildAt(i);
            child.setOnClickListener(v -> openStoreDetail(index));
        }

        initStoreDots();
        startStoreBannerAutoPlay();
    }

    private void openStoreDetail(int index) {
        Intent intent = new Intent(this, WashActivity.class);
        intent.putExtra("store_index", index);
        startActivity(intent);
    }

    private void initStoreDots() {
        llStoreDots.removeAllViews();
        for (int i = 0; i < STORE_BANNER_COUNT; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageResource(i == 0 ? R.drawable.dot_selected : R.drawable.dot_unselected);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (i > 0) params.leftMargin = dp2px(6);
            dot.setLayoutParams(params);
            llStoreDots.addView(dot);
        }
    }

    private void updateStoreDots(int index) {
        if (llStoreDots == null) return;
        for (int i = 0; i < llStoreDots.getChildCount(); i++) {
            ImageView dot = (ImageView) llStoreDots.getChildAt(i);
            dot.setImageResource(i == index ? R.drawable.dot_selected : R.drawable.dot_unselected);
        }
    }

    private void startStoreBannerAutoPlay() {
        storeBannerRunnable = new Runnable() {
            @Override
            public void run() {
                currentStoreBanner = (currentStoreBanner + 1) % STORE_BANNER_COUNT;
                if (vfNearbyStores != null) {
                    vfNearbyStores.showNext();
                }
                updateStoreDots(currentStoreBanner);
                storeBannerHandler.postDelayed(this, 3000);
            }
        };
        storeBannerHandler.postDelayed(storeBannerRunnable, 3000);
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (storeBannerRunnable != null) {
            storeBannerHandler.removeCallbacks(storeBannerRunnable);
            storeBannerRunnable = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (storeBannerRunnable != null) {
            storeBannerHandler.removeCallbacks(storeBannerRunnable);
            storeBannerRunnable = null;
        }
    }

    private void selectTab(int index) {
        if (currentTab == index)
            return;
        currentTab = index;

        // 切换内容显示
        for (int i = 0; i < contentViews.size(); i++) {
            contentViews.get(i).setVisibility(i == index ? View.VISIBLE : View.GONE);
        }

        // 切换导航栏选中态
        for (int i = 0; i < navTabs.size(); i++) {
            NavTab tab = navTabs.get(i);
            if (i == index) {
                tab.getIcon().setImageResource(tab.getSelectedIcon());
                tab.getText().setTextColor(Color.parseColor("#FFD93D"));
            } else {
                tab.getIcon().setImageResource(tab.getNormalIcon());
                tab.getText().setTextColor(Color.parseColor("#999999"));
            }
        }

        if (index == 4) {
            refreshProfileInfo();
        }
        if (index == 3) {
            dynamicPageHelper.refreshPetCard(findViewById(R.id.content_news));
        }
    }

    private void refreshProfileInfo() {
        View profileView = findViewById(R.id.content_profile);
        if (profileView == null) return;

        TextView tvNickname = profileView.findViewById(R.id.tv_nickname);
        TextView tvUserId = profileView.findViewById(R.id.tv_user_id);
        ImageView ivAvatar = profileView.findViewById(R.id.iv_avatar);
        TextView tvMsgUnread = profileView.findViewById(R.id.tv_msg_unread);

        if (tvNickname != null) {
            tvNickname.setText(UserManager.getInstance().getCurrentNickname());
        }
        if (tvUserId != null) {
            String phone = "";
            if (UserManager.getInstance().getCurrentUser() != null) {
                phone = UserManager.getInstance().getCurrentUser().phone;
            }
            tvUserId.setText("ID：" + (phone.isEmpty() ? "123456" : phone) + "    广东");
        }
        if (ivAvatar != null) {
            AvatarManager.loadAvatar(this, ivAvatar);
        }
        if (tvMsgUnread != null) {
            int totalUnread = 0;
            List<ChatMessageManager.ShopMessageInfo> shopInfos = ChatMessageManager.getInstance().getAllShopInfos();
            for (ChatMessageManager.ShopMessageInfo info : shopInfos) {
                totalUnread += info.unreadCount;
            }
            List<InteractionMessageManager.InteractionMsg> interactionMsgs = InteractionMessageManager.getInstance().getMessages(this, null);
            totalUnread += interactionMsgs.size();
            if (totalUnread > 0) {
                tvMsgUnread.setVisibility(View.VISIBLE);
                tvMsgUnread.setText(totalUnread > 99 ? "99+" : String.valueOf(totalUnread));
            } else {
                tvMsgUnread.setVisibility(View.GONE);
            }
        }
    }

    // NavTab 内部类
    private static class NavTab {
        private final View container;
        private final ImageView icon;
        private final TextView text;
        private final int normalIcon;
        private final int selectedIcon;

        public NavTab(View container, ImageView icon, TextView text, int normalIcon, int selectedIcon) {
            this.container = container;
            this.icon = icon;
            this.text = text;
            this.normalIcon = normalIcon;
            this.selectedIcon = selectedIcon;
        }

        public View getContainer() {
            return container;
        }

        public ImageView getIcon() {
            return icon;
        }

        public TextView getText() {
            return text;
        }

        public int getNormalIcon() {
            return normalIcon;
        }

        public int getSelectedIcon() {
            return selectedIcon;
        }
    }
}