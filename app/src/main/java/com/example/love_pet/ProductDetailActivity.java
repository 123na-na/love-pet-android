package com.example.love_pet;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.EditText;
import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";

    // 第一个商品的真实图片资源
    private static final int[] PRODUCT_1_IMAGES = {
            R.drawable.litter_box_main, // 轮播图1
            R.drawable.litter_box_main2, // 轮播图2
            R.drawable.litter_box_main3, // 轮播图3
            R.drawable.litter_box_main4 // 轮播图4
    };

    // 商品规格预览图片
    private static final int[] PRODUCT_1_PREVIEW = {
            R.drawable.litter_box_detail, // 规格预览1
            R.drawable.litter_box_detail2 // 规格预览2
    };

    private ViewPager vpProductImages;
    private LinearLayout llIndicator;
    private LinearLayout llStyles;

    // 价格信息
    private TextView tvProductName;
    private TextView tvPrice;
    private TextView tvOriginalPrice;
    private TextView tvProductDetail;

    // 底部信息行
    private TextView tvLogisticsTitle;
    private TextView tvLogisticsDetail;
    private LinearLayout llServiceTags;
    private LinearLayout llSpecTags;
    private TextView tvStoreName;
    private ImageView ivStoreAvatar;

    // 底部按钮
    private ImageView ivFavorite;
    private TextView tvCartCount;

    private List<Integer> currentImages = new ArrayList<>();
    private List<Integer> currentPreviewImages = new ArrayList<>();
    private boolean isFavorite = false;
    private int cartCount = 0;

    // 当前商品完整数据（从数据库加载）
    private ProductDataImporter.ProductDetail currentProductDetail;

    // 地址管理相关
    private List<Address> addressList = new ArrayList<>();
    private String selectedAddressId = null;

    // 定位相关
    private AMapLocationClient locationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 2002;

    // 当前弹窗相关
    private PopupWindow currentPopupWindow;
    private View currentDimView;

    // 添加地址相关
    private AddAddressDialogHelper addAddressHelper;
    private boolean isEditMode = false;
    private Address editingAddress;
    private boolean isManageMode = false;
    private LinearLayout currentAddressListContainer;
    private boolean isFromOrderDialog = false;
    private int orderSelectedStyleIndex = 0;
    private int orderQuantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail_full);
        Log.d(TAG, "onCreate: 布局加载成功");

        addAddressHelper = new AddAddressDialogHelper(this);

        Intent intent = getIntent();
        int productId = intent.getIntExtra("productId", 1);
        Log.d(TAG, "onCreate: productId = " + productId);

        initViews();
        setupData(productId);

        Toast.makeText(this, "商品详情页加载成功", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCreate: 初始化完成");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        addAddressHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void initViews() {
        vpProductImages = findViewById(R.id.vp_product_images);
        llIndicator = findViewById(R.id.ll_indicator);
        llStyles = findViewById(R.id.ll_image_preview);

        tvProductName = findViewById(R.id.tv_product_name);
        tvPrice = findViewById(R.id.tv_price);
        tvOriginalPrice = findViewById(R.id.tv_original_price);
        tvProductDetail = findViewById(R.id.tv_product_detail);

        // 底部信息行
        tvLogisticsTitle = findViewById(R.id.tv_logistics_title);
        tvLogisticsDetail = findViewById(R.id.tv_logistics_detail);
        llServiceTags = findViewById(R.id.ll_service_tags);
        llSpecTags = findViewById(R.id.ll_spec_tags);
        tvStoreName = findViewById(R.id.tv_store_name);
        ivStoreAvatar = findViewById(R.id.iv_store_avatar);

        ivFavorite = findViewById(R.id.iv_favorite);
        tvCartCount = findViewById(R.id.tv_cart_count);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        findViewById(R.id.iv_share).setOnClickListener(v -> openCustomerService());
        findViewById(R.id.btn_favorite).setOnClickListener(v -> toggleFavorite());
        findViewById(R.id.btn_cart).setOnClickListener(v -> goToCart());
        findViewById(R.id.btn_add_to_cart).setOnClickListener(v -> addToCart());
        findViewById(R.id.btn_buy_now).setOnClickListener(v -> buyNow());

        // 三个可点击区域的点击事件
        findViewById(R.id.layout_logistics).setOnClickListener(v -> {
            isFromOrderDialog = false;
            showShippingDetail();
        });
        findViewById(R.id.layout_services).setOnClickListener(v -> showServiceDetail());
        findViewById(R.id.layout_specs).setOnClickListener(v -> showSpecsDetail());
    }

    private void setupData(int productId) {
        // 从数据库加载完整商品数据
        ProductDataImporter importer = new ProductDataImporter(this);
        currentProductDetail = importer.getProductDetailById(productId);

        if (currentProductDetail != null) {
            Log.d(TAG, "加载商品: id=" + currentProductDetail.id
                    + " title=" + currentProductDetail.title
                    + " imageResId=" + currentProductDetail.mainImageResId
                    + " imagesCount=" + currentProductDetail.imageResIds.length);
        }

        if (currentProductDetail == null) {
            // 数据库无数据时显示默认提示
            tvProductName.setText("商品信息加载失败");
            tvPrice.setText("--");
            tvOriginalPrice.setText("");
            tvProductDetail.setText("无法获取商品详情，请稍后重试");
            return;
        }

        // 设置商品基本信息
        tvProductName.setText(currentProductDetail.title);
        tvPrice.setText(currentProductDetail.price);

        String originalPriceText = currentProductDetail.originalPrice;
        tvOriginalPrice.setText(originalPriceText.isEmpty() ? "" : "原价 ¥" + originalPriceText);

        String detailText = currentProductDetail.detailText;
        tvProductDetail.setText(detailText.isEmpty() ? "暂无详情描述" : detailText);

        // 设置轮播图
        currentImages.clear();
        for (int imageRes : currentProductDetail.imageResIds) {
            currentImages.add(imageRes);
        }
        if (currentImages.isEmpty()) {
            currentImages.add(currentProductDetail.mainImageResId);
        }

        // 设置款式预览图
        currentPreviewImages.clear();
        for (int previewRes : currentProductDetail.styleImageResIds) {
            currentPreviewImages.add(previewRes);
        }

        setupImagePager();
        // 隐藏款式预览条
        llStyles.setVisibility(View.GONE);

        // 动态填充底部4行信息
        setupBottomInfoRows();
    }

    /**
     * 动态填充底部4行：物流、服务保障、规格参数、店铺名
     */
    private void setupBottomInfoRows() {
        if (currentProductDetail == null)
            return;

        // 1. 物流行
        tvLogisticsTitle.setText(currentProductDetail.logisticsCompany + " " + currentProductDetail.logisticsTime);
        tvLogisticsDetail
                .setText(currentProductDetail.logisticsLocation + " · " + currentProductDetail.logisticsShipping);

        // 2. 服务保障行（紧凑单行，分隔线隔开）
        llServiceTags.removeAllViews();
        List<String> serviceTitles = currentProductDetail.serviceTitles;
        for (int i = 0; i < serviceTitles.size(); i++) {
            TextView tag = new TextView(this);
            tag.setText(serviceTitles.get(i));
            tag.setTextColor(0xFF666666);
            tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tag.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            llServiceTags.addView(tag);

            if (i < serviceTitles.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(1, 12);
                dlp.setMargins(dpToPx(8), 0, dpToPx(8), 0);
                divider.setLayoutParams(dlp);
                divider.setBackgroundColor(0xFFDDDDDD);
                llServiceTags.addView(divider);
            }
        }

        // 3. 规格参数行（紧凑单行，最多显示4个值，超长截断）
        llSpecTags.removeAllViews();
        List<String> specValues = currentProductDetail.specValues;
        int specCount = Math.min(specValues.size(), 4);
        for (int i = 0; i < specCount; i++) {
            String val = specValues.get(i);
            // 超过6个字符截断
            if (val.length() > 6) {
                val = val.substring(0, 6) + "...";
            }
            TextView valueTv = new TextView(this);
            valueTv.setText(val);
            valueTv.setTextColor(i == specCount - 1 ? 0xFFFF3322 : 0xFF333333);
            valueTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            valueTv.setTypeface(null, Typeface.BOLD);
            valueTv.setSingleLine(true);
            valueTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            valueTv.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            llSpecTags.addView(valueTv);

            if (i < specCount - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(1, 14);
                dlp.setMargins(dpToPx(10), 0, dpToPx(10), 0);
                divider.setLayoutParams(dlp);
                divider.setBackgroundColor(0xFFDDDDDD);
                llSpecTags.addView(divider);
            }
        }

        // 4. 店铺名
        String shopName = currentProductDetail.shopName.isEmpty() ? "官方旗舰店" : currentProductDetail.shopName;
        tvStoreName.setText(shopName);
        if (ivStoreAvatar != null) {
            ivStoreAvatar.setImageResource(ShopActivity.Product.getShopAvatarRes(shopName));
        }

        Log.d(TAG, "底部信息: 服务=" + serviceTitles.size() + "项, 规格=" + specValues.size() + "项, 店铺="
                + currentProductDetail.shopName);
    }

    private void setupImagePager() {
        vpProductImages.setAdapter(new ImagePagerAdapter());
        vpProductImages.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateIndicator(position);
                updateImagePreview(position);
            }

            @Override
            public void onPageScrolled(int position, float offset, int pixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // 初始化指示器
        llIndicator.removeAllViews();
        for (int i = 0; i < currentImages.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(8, 8);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0 ? R.drawable.dot_selected : R.drawable.dot_unselected);
            llIndicator.addView(dot);
        }
    }

    private void setupImagePreview() {
        llStyles.removeAllViews();

        for (int i = 0; i < currentPreviewImages.size(); i++) {
            // 创建缩略图容器
            FrameLayout previewContainer = new FrameLayout(this);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            containerParams.setMargins(0, 0, 12, 0);
            previewContainer.setLayoutParams(containerParams);

            // 缩略图ImageView
            ImageView thumbnail = new ImageView(this);
            int thumbWidth = 55;
            int thumbHeight = 45;
            FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(thumbWidth, thumbHeight);
            thumbnail.setLayoutParams(thumbParams);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumbnail.setImageResource(currentPreviewImages.get(i));

            // 选中边框效果
            if (i == 0) {
                Drawable border = getResources().getDrawable(R.drawable.bg_style_selected);
                if (border != null) {
                    thumbnail.setBackground(border);
                }
                thumbnail.setAlpha(1.0f);
            } else {
                thumbnail.setAlpha(0.6f);
            }

            final int position = i;
            previewContainer.setOnClickListener(v -> {
                // 点击缩略图切换到对应的大图
                if (position < currentImages.size()) {
                    vpProductImages.setCurrentItem(position);
                }
            });

            previewContainer.addView(thumbnail);
            llStyles.addView(previewContainer);
        }
    }

    private void updateIndicator(int position) {
        for (int i = 0; i < llIndicator.getChildCount(); i++) {
            llIndicator.getChildAt(i).setBackgroundResource(
                    i == position ? R.drawable.dot_selected : R.drawable.dot_unselected);
        }
    }

    private void updateImagePreview(int position) {
        for (int i = 0; i < llStyles.getChildCount(); i++) {
            View container = llStyles.getChildAt(i);
            if (container instanceof ViewGroup && ((ViewGroup) container).getChildCount() > 0) {
                ImageView thumbnail = (ImageView) ((ViewGroup) container).getChildAt(0);
                if (i == position || (position >= currentPreviewImages.size() && i == 0)) {
                    // 选中状态
                    Drawable border = getResources().getDrawable(R.drawable.bg_style_selected);
                    if (border != null) {
                        thumbnail.setBackground(border);
                    }
                    thumbnail.setAlpha(1.0f);
                } else {
                    // 未选中状态
                    thumbnail.setBackground(null);
                    thumbnail.setAlpha(0.6f);
                }
            }
        }
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        ivFavorite.setImageResource(isFavorite ? R.drawable.ic_heart_red : R.drawable.ic_heart_outline);
        Toast.makeText(this, isFavorite ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
    }

    private void addToCart() {
        orderSelectedStyleIndex = 0;
        orderQuantity = 1;
        isFromOrderDialog = false;
        showOrderDialog(false);
    }

    private void goToCart() {
        Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
    }

    private void buyNow() {
        orderSelectedStyleIndex = 0;
        orderQuantity = 1;
        isFromOrderDialog = false;
        showOrderDialog(true);
    }

    private void openCustomerService() {
        String shopName = currentProductDetail != null && !currentProductDetail.shopName.isEmpty()
                ? currentProductDetail.shopName : "官方旗舰店";
        int shopAvatar = ShopActivity.Product.getShopAvatarRes(shopName);
        Intent intent = new Intent(ProductDetailActivity.this, ChatDetailActivity.class);
        intent.putExtra(ChatDetailActivity.EXTRA_TITLE, shopName);
        intent.putExtra(ChatDetailActivity.EXTRA_TYPE, ChatDetailActivity.TYPE_SHOP);
        intent.putExtra(ChatDetailActivity.EXTRA_AVATAR, shopAvatar);
        intent.putExtra(ChatDetailActivity.EXTRA_SHOP_NAME, shopName);
        intent.putExtra(ChatDetailActivity.EXTRA_PRODUCT_NAME, currentProductDetail != null ? currentProductDetail.title : "");
        intent.putExtra(ChatDetailActivity.EXTRA_IS_AI_CHAT, true);
        startActivity(intent);
    }

    private void showSpecsDetail() {
        showPopupDialog(R.layout.dialog_product_specs, popupView -> {
            if (currentProductDetail == null || currentProductDetail.specsJson.isEmpty())
                return;
            try {
                org.json.JSONArray specsArr = new org.json.JSONArray(currentProductDetail.specsJson);
                // 找到TableLayout容器
                TableLayout tableLayout = popupView.findViewById(R.id.table_specs);
                if (tableLayout == null) {
                    ScrollView sv = popupView.findViewById(R.id.scroll_specs);
                    if (sv != null)
                        tableLayout = (TableLayout) sv.getChildAt(0);
                }
                // 清除旧的静态行
                tableLayout.removeAllViews();

                for (int i = 0; i < specsArr.length(); i++) {
                    org.json.JSONObject specObj = specsArr.getJSONObject(i);
                    String label = specObj.optString("label", "");
                    String value = specObj.optString("value", "");

                    // 构建一行
                    TableRow row = new TableRow(this);
                    row.setLayoutParams(new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));

                    // 左列：label + value
                    LinearLayout leftCol = createSpecCell(label, value, false);
                    leftCol.setPadding(0, dpToPx(12), dpToPx(16), dpToPx(12));

                    // 右列：下一个label+value（如果有）
                    LinearLayout rightCol;
                    if (i + 1 < specsArr.length()) {
                        org.json.JSONObject nextObj = specsArr.getJSONObject(i + 1);
                        String nextLabel = nextObj.optString("label", "");
                        String nextValue = nextObj.optString("value", "");
                        rightCol = createSpecCell(nextLabel, nextValue, false);
                        rightCol.setPadding(0, dpToPx(12), 0, dpToPx(12));
                        i++; // 跳过下一个
                    } else {
                        rightCol = new LinearLayout(this);
                        rightCol.setLayoutParams(new TableRow.LayoutParams(
                                0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                    }

                    row.addView(leftCol);
                    row.addView(rightCol);
                    tableLayout.addView(row);

                    // 分割线
                    View divider = new View(this);
                    divider.setLayoutParams(new TableLayout.LayoutParams(
                            TableLayout.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(0xFFF5F5F5);
                    tableLayout.addView(divider);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private LinearLayout createSpecCell(String label, String value, boolean isLastRed) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setLayoutParams(new TableRow.LayoutParams(
                0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

        TextView labelTv = new TextView(this);
        labelTv.setText(label.isEmpty() ? "-" : label);
        labelTv.setTextColor(0xFF999999);
        labelTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        cell.addView(labelTv);

        TextView valueTv = new TextView(this);
        valueTv.setText(value.isEmpty() ? "-" : value);
        valueTv.setTextColor(isLastRed ? 0xFFFF3322 : 0xFF333333);
        valueTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        valueTv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        vlp.topMargin = dpToPx(6);
        valueTv.setLayoutParams(vlp);
        cell.addView(valueTv);

        return cell;
    }

    private void showServiceDetail() {
        showPopupDialog(R.layout.dialog_service, popupView -> {
            if (currentProductDetail == null || currentProductDetail.servicesJson.isEmpty())
                return;
            try {
                org.json.JSONArray servicesArr = new org.json.JSONArray(currentProductDetail.servicesJson);
                LinearLayout container = popupView.findViewById(R.id.ll_service_items);
                if (container == null)
                    return;

                for (int i = 0; i < servicesArr.length(); i++) {
                    org.json.JSONObject svcObj = servicesArr.getJSONObject(i);
                    String title = svcObj.optString("title", "");
                    String subtitle = svcObj.optString("subtitle", "");
                    String desc = svcObj.optString("desc", "");

                    // 构建单条服务项
                    LinearLayout itemLayout = new LinearLayout(this);
                    itemLayout.setOrientation(LinearLayout.VERTICAL);
                    itemLayout.setPadding(0, dpToPx(12), 0, dpToPx(12));

                    // 标题行：图标 + 标题 + 副标题
                    LinearLayout titleRow = new LinearLayout(this);
                    titleRow.setOrientation(LinearLayout.HORIZONTAL);
                    titleRow.setGravity(Gravity.CENTER_VERTICAL);

                    ImageView iconIv = new ImageView(this);
                    iconIv.setImageResource(R.drawable.ic_shield);
                    int iconSize = dpToPx(24);
                    iconIv.setLayoutParams(new ViewGroup.LayoutParams(iconSize, iconSize));
                    titleRow.addView(iconIv);

                    TextView titleTv = new TextView(this);
                    titleTv.setText(title.isEmpty() ? "-" : title);
                    titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                    titleTv.setTypeface(null, Typeface.BOLD);
                    titleTv.setTextColor(0xFF333333);
                    LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    tlp.leftMargin = dpToPx(8);
                    titleTv.setLayoutParams(tlp);
                    titleRow.addView(titleTv);

                    if (!subtitle.isEmpty()) {
                        TextView subTv = new TextView(this);
                        subTv.setText(subtitle);
                        subTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                        subTv.setTextColor(0xFF999999);
                        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        slp.leftMargin = dpToPx(8);
                        subTv.setLayoutParams(slp);
                        titleRow.addView(subTv);
                    }

                    itemLayout.addView(titleRow);

                    // 描述文字
                    if (!desc.isEmpty()) {
                        TextView descTv = new TextView(this);
                        descTv.setText(desc);
                        descTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        descTv.setTextColor(0xFF666666);
                        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        dlp.topMargin = dpToPx(8);
                        dlp.leftMargin = dpToPx(32);
                        descTv.setLayoutParams(dlp);
                        itemLayout.addView(descTv);
                    }

                    container.addView(itemLayout);

                    // 分割线（最后一个不加）
                    if (i < servicesArr.length() - 1) {
                        View divider = new View(this);
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, 1));
                        divider.setBackgroundColor(0xFFEEEEEE);
                        container.addView(divider);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 显示订单弹窗（加入购物车 / 立即购买）
     * 
     * @param isBuyNow true=立即购买, false=加入购物车
     */
    private void showOrderDialog(boolean isBuyNow) {
        if (currentProductDetail == null)
            return;

        showPopupDialog(R.layout.dialog_order, popupView -> {
            // ---- 第一部分：收件人信息 ----
            TextView tvName = popupView.findViewById(R.id.tv_recipient_name);
            TextView tvAddress = popupView.findViewById(R.id.tv_recipient_address);

            // 确保地址列表已加载
            if (addressList == null || addressList.isEmpty()) {
                addressList = Address.getAddresses(this);
            }

            // 获取选中的地址或默认地址
            Address selectedAddr = null;
            if (selectedAddressId != null) {
                for (Address addr : addressList) {
                    if (addr.getId().equals(selectedAddressId)) {
                        selectedAddr = addr;
                        break;
                    }
                }
            }
            if (selectedAddr == null && !addressList.isEmpty()) {
                for (Address addr : addressList) {
                    if (addr.isDefault()) {
                        selectedAddr = addr;
                        selectedAddressId = addr.getId();
                        break;
                    }
                }
            }
            if (selectedAddr == null && !addressList.isEmpty()) {
                selectedAddr = addressList.get(0);
                selectedAddressId = selectedAddr.getId();
            }

            if (selectedAddr != null) {
                if (tvName != null)
                    tvName.setText(selectedAddr.getName() + " " + selectedAddr.getPhone());
                if (tvAddress != null)
                    tvAddress.setText(selectedAddr.getFullAddress());
            } else {
                if (tvName != null)
                    tvName.setText("请添加收货地址");
                if (tvAddress != null)
                    tvAddress.setText("");
            }

            // 收件人区域点击可打开地址选择
            View addrSection = popupView.findViewById(R.id.ll_address_section);
            if (addrSection != null) {
                addrSection.setOnClickListener(v -> {
                    // 标记是从订单弹窗进入的
                    isFromOrderDialog = true;
                    // 关闭当前订单弹窗
                    if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
                        currentPopupWindow.dismiss();
                        currentPopupWindow = null;
                    }
                    if (currentDimView != null) {
                        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
                        rootView.removeView(currentDimView);
                        currentDimView = null;
                    }
                    // 打开物流/地址弹窗
                    showShippingDetail();
                });
            }

            // ---- 第二部分：商品图片+价格+数量 ----
            ImageView ivProductImg = popupView.findViewById(R.id.iv_order_product_image);
            TextView tvPriceLabel = popupView.findViewById(R.id.tv_order_price_label);
            TextView tvDiscountHint = popupView.findViewById(R.id.tv_order_discount_hint);
            TextView tvQty = popupView.findViewById(R.id.tv_order_quantity);
            Button btnSubmit = popupView.findViewById(R.id.btn_submit_order);

            // 初始化商品图
            if (ivProductImg != null && currentProductDetail.imageResIds.length > 0) {
                ivProductImg.setImageResource(currentProductDetail.imageResIds[0]);
                ivProductImg.setOnClickListener(v -> {
                    int currentResId = 0;
                    try {
                        if (!currentProductDetail.stylesJson.isEmpty()) {
                            org.json.JSONArray stylesArr = new org.json.JSONArray(currentProductDetail.stylesJson);
                            if (orderSelectedStyleIndex < stylesArr.length()) {
                                org.json.JSONObject styleObj = stylesArr.getJSONObject(orderSelectedStyleIndex);
                                String imgResName = styleObj.optString("imageRes",
                                        currentProductDetail.imageNames != null && currentProductDetail.imageNames.length > 0
                                                ? currentProductDetail.imageNames[0]
                                                : "");
                                currentResId = getResourceIdByName(imgResName);
                            }
                        }
                        if (currentResId == 0 && currentProductDetail.imageResIds.length > 0) {
                            currentResId = currentProductDetail.imageResIds[0];
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (currentResId != 0) {
                        showImageZoomDialog(currentResId);
                    }
                });
            }

            // 初始化数量显示
            if (tvQty != null)
                tvQty.setText(String.valueOf(orderQuantity));

            // 更新价格和提交按钮的辅助方法
            Runnable updatePrice = () -> {
                try {
                    if (currentProductDetail.stylesJson.isEmpty())
                        return;
                    org.json.JSONArray stylesArr = new org.json.JSONArray(currentProductDetail.stylesJson);
                    if (orderSelectedStyleIndex >= stylesArr.length())
                        return;
                    org.json.JSONObject styleObj = stylesArr.getJSONObject(orderSelectedStyleIndex);
                    double stylePrice = styleObj.optDouble("price", Double.parseDouble(currentProductDetail.price));
                    double styleOrigPrice = styleObj.optDouble("originalPrice",
                            Double.parseDouble(currentProductDetail.originalPrice));
                    double total = stylePrice * orderQuantity;
                    double discount = styleOrigPrice - stylePrice;
                    String priceStr = String.format("%.2f", stylePrice);
                    String origStr = String.format("%.2f", styleOrigPrice);
                    String discStr = String.format("%.2f", discount);

                    if (tvPriceLabel != null)
                        tvPriceLabel.setText("实付 ¥" + priceStr + " | 优惠前 ¥" + origStr);
                    if (tvDiscountHint != null)
                        tvDiscountHint.setText("共减 ¥" + discStr + " | 关注店铺最高再减10元 >");
                    if (btnSubmit != null)
                        btnSubmit.setText(isBuyNow ? "立即支付 ¥" + String.format("%.2f", total) : "确认加入购物车");

                    // 更新商品图片
                    if (ivProductImg != null) {
                        String imgResName = styleObj.optString("imageRes",
                                currentProductDetail.imageNames != null && currentProductDetail.imageNames.length > 0
                                        ? currentProductDetail.imageNames[0]
                                        : "");
                        int resId = getResourceIdByName(imgResName);
                        if (resId != 0)
                            ivProductImg.setImageResource(resId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            // 数量控制
            ImageButton btnMinus = popupView.findViewById(R.id.btn_qty_minus);
            ImageButton btnPlus = popupView.findViewById(R.id.btn_qty_plus);
            if (btnMinus != null) {
                btnMinus.setOnClickListener(v -> {
                    if (orderQuantity > 1) {
                        orderQuantity--;
                        if (tvQty != null)
                            tvQty.setText(String.valueOf(orderQuantity));
                        updatePrice.run();
                    }
                });
            }
            if (btnPlus != null) {
                btnPlus.setOnClickListener(v -> {
                    if (orderQuantity < 99) {
                        orderQuantity++;
                        if (tvQty != null)
                            tvQty.setText(String.valueOf(orderQuantity));
                        updatePrice.run();
                    }
                });
            }

            // ---- 第三部分：款式选项（动态构建圆角矩形列表） ----
            LinearLayout llStyles = popupView.findViewById(R.id.ll_style_options);
            TextView tvStyleTitle = popupView.findViewById(R.id.tv_style_category_title);
            java.util.List<View> styleItemViews = new java.util.ArrayList<>(); // 保存款式项View引用

            if (llStyles != null && !currentProductDetail.stylesJson.isEmpty()) {
                try {
                    org.json.JSONArray stylesArr = new org.json.JSONArray(currentProductDetail.stylesJson);
                    if (tvStyleTitle != null)
                        tvStyleTitle.setText("颜色分类 (" + stylesArr.length() + ")");

                    for (int i = 0; i < stylesArr.length(); i++) {
                        final int idx = i;
                        org.json.JSONObject styleObj = stylesArr.getJSONObject(i);
                        String name = styleObj.optString("name", "");
                        double sp = styleObj.optDouble("price", 0);
                        String imgResName = styleObj.optString("imageRes", "");

                        // 每个款式项：圆角矩形
                        LinearLayout styleItem = new LinearLayout(this);
                        styleItem.setOrientation(LinearLayout.HORIZONTAL);
                        styleItem.setGravity(Gravity.CENTER_VERTICAL);
                        styleItem.setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10));
                        styleItem.setClickable(true);
                        styleItem.setFocusable(true);

                        // 圆角背景：选中=橙色边框，未选=灰色边框
                        GradientDrawable bg = new GradientDrawable();
                        bg.setCornerRadius(dpToPx(8));
                        bg.setStroke(dpToPx(1), idx == orderSelectedStyleIndex ? 0xFFFF5722 : 0xFFDDDDDD);
                        bg.setColor(idx == orderSelectedStyleIndex ? 0xFFFFF8F0 : 0xFFFFFFFF);
                        styleItem.setBackground(bg);

                        // 小图标
                        ImageView thumbIv = new ImageView(this);
                        int thumbSize = dpToPx(36);
                        thumbIv.setLayoutParams(new ViewGroup.LayoutParams(thumbSize, thumbSize));
                        thumbIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        int thumbResId = getResourceIdByName(imgResName);
                        thumbIv.setImageResource(thumbResId != 0 ? thumbResId : R.drawable.ic_paw_black);
                        styleItem.addView(thumbIv);

                        // 文字信息
                        LinearLayout textCol = new LinearLayout(this);
                        textCol.setOrientation(LinearLayout.VERTICAL);
                        textCol.setLayoutParams(new LinearLayout.LayoutParams(
                                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                        ((LinearLayout.LayoutParams) textCol.getLayoutParams()).leftMargin = dpToPx(10);

                        TextView nameTv = new TextView(this);
                        nameTv.setText(name.isEmpty() ? "-" : name);
                        nameTv.setTextColor(0xFF333333);
                        nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                        textCol.addView(nameTv);

                        TextView priceTv = new TextView(this);
                        priceTv.setText("¥" + String.format("%.2f", sp));
                        priceTv.setTextColor(0xFFFF5722);
                        priceTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                        priceTv.setTypeface(null, Typeface.BOLD);
                        LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        plp.topMargin = dpToPx(4);
                        priceTv.setLayoutParams(plp);
                        textCol.addView(priceTv);

                        // 店长推荐标签（最后一个加）
                        if (i == stylesArr.length() - 1) {
                            TextView tagTv = new TextView(this);
                            tagTv.setText("店长主推");
                            tagTv.setTextColor(0xFFFFFFFF);
                            tagTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                            tagTv.setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2));
                            tagTv.setBackgroundResource(R.drawable.bg_tag_orange);
                            textCol.addView(tagTv);
                        }

                        styleItem.addView(textCol);
                        llStyles.addView(styleItem);
                        styleItemViews.add(styleItem); // 记录款式项

                        // 点击切换款式
                        styleItem.setOnClickListener(v -> {
                            orderSelectedStyleIndex = idx;
                            // 只遍历真正的款式项（跳过间隙View）
                            for (int j = 0; j < styleItemViews.size(); j++) {
                                View item = styleItemViews.get(j);
                                GradientDrawable d = new GradientDrawable();
                                d.setCornerRadius(dpToPx(8));
                                if (j == idx) {
                                    d.setStroke(dpToPx(1), 0xFFFF5722);
                                    d.setColor(0xFFFFF8F0);
                                } else {
                                    d.setStroke(dpToPx(1), 0xFFDDDDDD);
                                    d.setColor(0xFFFFFFFF);
                                }
                                item.setBackground(d);
                            }
                            updatePrice.run();
                        });

                        // 间距
                        if (i < stylesArr.length() - 1) {
                            View gap = new View(this);
                            gap.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(6)));
                            llStyles.addView(gap);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 提交按钮
            if (btnSubmit != null) {
                btnSubmit.setOnClickListener(v -> {
                    try {
                        String styleName = "";
                        String stylePrice = currentProductDetail.price;
                        String styleOrigPrice = currentProductDetail.originalPrice;
                        String imageName = "";
                        if (!currentProductDetail.stylesJson.isEmpty()) {
                            org.json.JSONArray stylesArr = new org.json.JSONArray(currentProductDetail.stylesJson);
                            if (orderSelectedStyleIndex < stylesArr.length()) {
                                org.json.JSONObject styleObj = stylesArr.getJSONObject(orderSelectedStyleIndex);
                                styleName = styleObj.optString("name", "");
                                stylePrice = String.valueOf(styleObj.optDouble("price", Double.parseDouble(currentProductDetail.price)));
                                styleOrigPrice = String.valueOf(styleObj.optDouble("originalPrice", Double.parseDouble(currentProductDetail.originalPrice)));
                                imageName = styleObj.optString("imageRes", "");
                            }
                        }
                        if (imageName.isEmpty() && currentProductDetail.imageNames != null && currentProductDetail.imageNames.length > 0) {
                            imageName = currentProductDetail.imageNames[0];
                        }
                        String shopName = currentProductDetail.shopName.isEmpty() ? "官方旗舰店" : currentProductDetail.shopName;

                        CartItem item = new CartItem(
                                currentProductDetail.id,
                                currentProductDetail.title,
                                styleName,
                                stylePrice,
                                imageName,
                                shopName,
                                orderQuantity
                        );
                        item.setOriginalPrice(styleOrigPrice);
                        item.setSelected(true);

                        if (isBuyNow) {
                            List<CartItem> singleItemList = new ArrayList<>();
                            singleItemList.add(item);
                            CartItem.saveBuyNowItems(ProductDetailActivity.this, singleItemList);

                            if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
                                currentPopupWindow.dismiss();
                                currentPopupWindow = null;
                            }
                            if (currentDimView != null) {
                                ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
                                rootView.removeView(currentDimView);
                                currentDimView = null;
                            }

                            Intent intent = new Intent(ProductDetailActivity.this, ConfirmOrderActivity.class);
                            intent.putExtra(ConfirmOrderActivity.EXTRA_FROM_BUY_NOW, true);
                            startActivity(intent);
                        } else {
                            CartItem.addToCart(ProductDetailActivity.this, item);

                            if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
                                currentPopupWindow.dismiss();
                                currentPopupWindow = null;
                            }
                            if (currentDimView != null) {
                                ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
                                rootView.removeView(currentDimView);
                                currentDimView = null;
                            }
                            Toast.makeText(this, "已加入购物车", Toast.LENGTH_SHORT).show();
                            Intent cartIntent = new Intent(ProductDetailActivity.this, CartActivity.class);
                            startActivity(cartIntent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, isBuyNow ? "操作失败" : "加入购物车失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // 初始化价格显示
            updatePrice.run();
        });
    }

    private void showShippingDetail() {
        isManageMode = false;
        showPopupDialog(R.layout.dialog_shipping, popupView -> {
            if (currentProductDetail == null)
                return;
            TextView tvTime = popupView.findViewById(R.id.tv_shipping_time);
            TextView tvFrom = popupView.findViewById(R.id.tv_shipping_from);
            TextView tvCost = popupView.findViewById(R.id.tv_shipping_cost);
            if (tvTime != null && !currentProductDetail.logisticsTime.isEmpty())
                tvTime.setText(currentProductDetail.logisticsTime);
            if (tvFrom != null && !currentProductDetail.logisticsLocation.isEmpty())
                tvFrom.setText(currentProductDetail.logisticsLocation);
            if (tvCost != null && !currentProductDetail.logisticsShipping.isEmpty())
                tvCost.setText(currentProductDetail.logisticsShipping);

            // 加载地址列表
            addressList = Address.getAddresses(this);
            LinearLayout llAddressList = popupView.findViewById(R.id.ll_address_list);
            if (llAddressList != null) {
                currentAddressListContainer = llAddressList;
                llAddressList.removeAllViews();
                renderAddressList(llAddressList);
            }

            // 管理按钮
            TextView tvManage = popupView.findViewById(R.id.tv_manage_address);
            if (tvManage != null) {
                tvManage.setText("管理");
                tvManage.setOnClickListener(v -> {
                    isManageMode = !isManageMode;
                    tvManage.setText(isManageMode ? "完成" : "管理");
                    if (currentAddressListContainer != null) {
                        renderAddressList(currentAddressListContainer);
                    }
                });
            }

            // 新增地址按钮
            TextView tvAddNew = popupView.findViewById(R.id.tv_add_new_address);
            if (tvAddNew != null) {
                tvAddNew.setOnClickListener(v -> {
                    isEditMode = false;
                    editingAddress = null;
                    // 先关闭物流弹窗，再打开新增地址弹窗
                    if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
                        currentPopupWindow.dismiss();
                        currentPopupWindow = null;
                    }
                    if (currentDimView != null) {
                        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
                        rootView.removeView(currentDimView);
                        currentDimView = null;
                    }
                    showAddAddressDialog();
                });
            }

            // 确定按钮 - 关闭物流弹窗
            Button btnConfirm = popupView.findViewById(R.id.btn_confirm_shipping);
            if (btnConfirm != null) {
                btnConfirm.setOnClickListener(v -> {
                    if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
                        currentPopupWindow.dismiss();
                        currentPopupWindow = null;
                    }
                    if (currentDimView != null) {
                        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
                        rootView.removeView(currentDimView);
                        currentDimView = null;
                    }
                    // 根据来源决定跳回哪个页面
                    if (isFromOrderDialog) {
                        // 从订单弹窗来的，回到订单弹窗
                        showOrderDialog(true);
                    }
                });
            }
        });
    }

    private void renderAddressList(LinearLayout container) {
        container.removeAllViews();

        if (addressList.isEmpty()) {
            TextView emptyTv = new TextView(this);
            emptyTv.setText("暂无收货地址");
            emptyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            emptyTv.setTextColor(0xFF999999);
            emptyTv.setPadding(0, dpToPx(16), 0, dpToPx(16));
            emptyTv.setGravity(Gravity.CENTER);
            container.addView(emptyTv);
            return;
        }

        for (int i = 0; i < addressList.size(); i++) {
            Address addr = addressList.get(i);
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), 0);
            itemLayout.setBackgroundColor(0xFFFFFFFF);

            LinearLayout contentRow = new LinearLayout(this);
            contentRow.setOrientation(LinearLayout.HORIZONTAL);
            contentRow.setGravity(Gravity.CENTER_VERTICAL);

            RadioButton radioBtn = new RadioButton(this);
            radioBtn.setId(View.generateViewId());
            radioBtn.setChecked(addr.getId().equals(selectedAddressId));
            radioBtn.setButtonTintList(ContextCompat.getColorStateList(this, R.color.orange));
            radioBtn.setOnClickListener(v -> {
                if (!isManageMode) {
                    selectedAddressId = addr.getId();
                    renderAddressList(container);
                }
            });
            contentRow.addView(radioBtn);

            LinearLayout infoLayout = new LinearLayout(this);
            infoLayout.setOrientation(LinearLayout.VERTICAL);
            infoLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            ((LinearLayout.LayoutParams) infoLayout.getLayoutParams()).leftMargin = dpToPx(12);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView nameTv = new TextView(this);
            nameTv.setText(addr.getName());
            nameTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            nameTv.setTypeface(null, Typeface.BOLD);
            nameTv.setTextColor(0xFF333333);
            topRow.addView(nameTv);

            TextView phoneTv = new TextView(this);
            phoneTv.setText(addr.getPhone());
            phoneTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            phoneTv.setTextColor(0xFF666666);
            LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            plp.leftMargin = dpToPx(12);
            phoneTv.setLayoutParams(plp);
            topRow.addView(phoneTv);

            if (!addr.getTag().isEmpty()) {
                TextView tagTv = new TextView(this);
                tagTv.setText(addr.getTag());
                tagTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                tagTv.setTextColor(0xFFFF9800);
                tagTv.setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2));
                GradientDrawable tagBg = new GradientDrawable();
                tagBg.setCornerRadius(dpToPx(4));
                tagBg.setColor(0xFFFFF0E6);
                tagTv.setBackground(tagBg);
                LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                tlp.leftMargin = dpToPx(8);
                tagTv.setLayoutParams(tlp);
                topRow.addView(tagTv);
            }

            infoLayout.addView(topRow);

            TextView addrTv = new TextView(this);
            addrTv.setText(addr.getFullAddress());
            addrTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            addrTv.setTextColor(0xFF666666);
            addrTv.setMaxLines(2);
            LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            alp.topMargin = dpToPx(4);
            addrTv.setLayoutParams(alp);
            infoLayout.addView(addrTv);

            contentRow.addView(infoLayout);

            ImageView editIv = new ImageView(this);
            editIv.setImageResource(R.drawable.revise);
            editIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(dpToPx(24), dpToPx(24));
            editIv.setLayoutParams(elp);
            editIv.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            editIv.setOnClickListener(v -> {
                isEditMode = true;
                editingAddress = addr;
                // 先关闭物流弹窗，再打开编辑地址弹窗
                if (currentPopupWindow != null && currentPopupWindow.isShowing()) {
                    currentPopupWindow.dismiss();
                    currentPopupWindow = null;
                }
                if (currentDimView != null) {
                    ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
                    rootView.removeView(currentDimView);
                    currentDimView = null;
                }
                showAddAddressDialog();
            });
            contentRow.addView(editIv);

            itemLayout.addView(contentRow);

            if (isManageMode) {
                LinearLayout actionRow = new LinearLayout(this);
                actionRow.setOrientation(LinearLayout.HORIZONTAL);
                actionRow.setGravity(Gravity.END);
                actionRow.setPadding(0, dpToPx(8), 0, dpToPx(12));

                TextView deleteTv = new TextView(this);
                deleteTv.setText("删除");
                deleteTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                deleteTv.setTextColor(0xFFFF6B35);
                deleteTv.setPadding(dpToPx(16), dpToPx(6), dpToPx(16), dpToPx(6));
                GradientDrawable deleteBg = new GradientDrawable();
                deleteBg.setCornerRadius(dpToPx(4));
                deleteBg.setColor(0xFFF5F5F5);
                deleteTv.setBackground(deleteBg);
                deleteTv.setOnClickListener(v -> {
                    Address.deleteAddress(this, addr.getId());
                    addressList = Address.getAddresses(this);
                    if (addr.getId().equals(selectedAddressId)) {
                        selectedAddressId = addressList.isEmpty() ? "" : addressList.get(0).getId();
                    }
                    renderAddressList(container);
                });
                actionRow.addView(deleteTv);

                itemLayout.addView(actionRow);
            } else {
                View bottomPadding = new View(this);
                bottomPadding.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(12)));
                itemLayout.addView(bottomPadding);
            }

            container.addView(itemLayout);

            if (i < addressList.size() - 1) {
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(0xFFEEEEEE);
                container.addView(divider);
            }
        }
    }

    private void updateAddressSelection(LinearLayout container) {
        int addrIndex = 0;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout itemLayout = (LinearLayout) child;
                for (int j = 0; j < itemLayout.getChildCount(); j++) {
                    View inner = itemLayout.getChildAt(j);
                    if (inner instanceof RadioButton) {
                        RadioButton rb = (RadioButton) inner;
                        if (addrIndex < addressList.size()) {
                            String addrId = addressList.get(addrIndex).getId();
                            rb.setChecked(addrId.equals(selectedAddressId));
                        }
                        addrIndex++;
                        break;
                    }
                }
            }
        }
    }

    private void showPopupDialog(int layoutResId) {
        showPopupDialog(layoutResId, null);
    }

    private void showAddAddressDialog() {
        Address editAddr = isEditMode ? editingAddress : null;
        addAddressHelper.showAddAddressDialog(editAddr, address -> {
            addressList = Address.getAddresses(ProductDetailActivity.this);
            selectedAddressId = address.getId();
            if (isFromOrderDialog) {
                showOrderDialog(true);
            } else {
                showShippingDetail();
            }
        });
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocation(LocationCallback callback) {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(
                    android.content.Context.LOCATION_SERVICE);
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation == null) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastLocation != null) {
                convertLocationToAddress(lastLocation.getLatitude(), lastLocation.getLongitude(), callback);
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10,
                    new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            if (location != null) {
                                convertLocationToAddress(location.getLatitude(), location.getLongitude(), callback);
                                locationManager.removeUpdates(this);
                            }
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                        }
                    }, Looper.getMainLooper());

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (locationManager != null) {
                    locationManager.removeUpdates((LocationListener) this);
                }
            }, 10000);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void convertLocationToAddress(double lat, double lng, LocationCallback callback) {
        try {
            locationClient = new AMapLocationClient(getApplicationContext());
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setOnceLocation(true);

            locationClient.setLocationOption(option);
            locationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation aMapLocation) {
                    if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                        String province = aMapLocation.getProvince();
                        String city = aMapLocation.getCity();
                        String district = aMapLocation.getDistrict();
                        String address = aMapLocation.getAddress();
                        callback.onLocationResult(address, province, city, district);
                    } else {
                        Toast.makeText(ProductDetailActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
                    }
                    if (locationClient != null) {
                        locationClient.stopLocation();
                    }
                }
            });

            locationClient.startLocation();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "定位服务初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已获取", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要定位权限才能获取地址", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        addAddressHelper.handleActivityResult(requestCode, resultCode, data);
    }

    private void loadMapPreview(ImageView imageView, double lat, double lng) {
        String apiKey = "7ad8f760b5fe418fe9b8134b497354aa";
        String url = "https://restapi.amap.com/v3/staticmap?location=" + lng + "," + lat
                + "&zoom=15&size=800*400&markers=mid,,A:" + lng + "," + lat
                + "&key=" + apiKey;
        Glide.with(this)
                .load(url)
                .centerCrop()
                .placeholder(R.drawable.bg_map_placeholder)
                .error(R.drawable.bg_map_placeholder)
                .into(imageView);
    }

    private interface LocationCallback {
        void onLocationResult(String location, String province, String city, String district);
    }

    /**
     * 通用弹窗显示方法 - 使用FrameLayout+背景drawable实现顶部圆角
     * 
     * @param layoutResId 布局资源ID
     * @param onPopulate  弹窗视图填充回调（可在显示前动态设置内容），可为null
     */
    private void showPopupDialog(int layoutResId, java.util.function.Consumer<View> onPopulate) {
        // 创建半透明遮罩背景（全屏覆盖）
        View dimView = new View(this);
        dimView.setBackgroundColor(0x99000000); // 60%黑色透明度
        dimView.setAlpha(0f);
        dimView.setTag("dim_view");

        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        ViewGroup.LayoutParams dimParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.addView(dimView, dimParams);

        // 保存当前dimView引用
        currentDimView = dimView;

        // 加载弹窗布局
        View popupView = LayoutInflater.from(this).inflate(layoutResId, null);

        // 计算弹窗尺寸
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        // 弹窗最大高度为屏幕的80%，确保按钮能完整显示
        int popupHeight = (int) (screenHeight * 0.8);

        // 使用FrameLayout作为弹窗容器，设置顶部圆角背景
        FrameLayout container = new FrameLayout(this);
        container.setBackgroundResource(R.drawable.bg_dialog_top_rounded);

        // 将popupView添加到容器
        if (popupView.getParent() != null) {
            ((ViewGroup) popupView.getParent()).removeView(popupView);
        }
        container.addView(popupView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        PopupWindow popupWindow = new PopupWindow(container,
                screenWidth,
                popupHeight,
                true);

        popupWindow
                .setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        // 保存当前popupWindow引用
        currentPopupWindow = popupWindow;

        // 设置关闭按钮点击事件（根据不同弹窗设置不同的关闭按钮ID）
        ImageView ivClose = null;
        Button btnConfirm = null;

        if (layoutResId == R.layout.dialog_product_specs) {
            ivClose = popupView.findViewById(R.id.iv_close_specs);
        } else if (layoutResId == R.layout.dialog_service) {
            ivClose = popupView.findViewById(R.id.iv_close_service);
            btnConfirm = popupView.findViewById(R.id.btn_confirm_service);
        } else if (layoutResId == R.layout.dialog_shipping) {
            ivClose = popupView.findViewById(R.id.iv_close_shipping);
            btnConfirm = popupView.findViewById(R.id.btn_confirm_shipping);
        } else if (layoutResId == R.layout.dialog_order) {
            ivClose = popupView.findViewById(R.id.iv_close_order);
            btnConfirm = popupView.findViewById(R.id.btn_submit_order);
        } else if (layoutResId == R.layout.dialog_add_address) {
            ivClose = popupView.findViewById(R.id.iv_close_add_address);
        }

        // 关闭弹窗的方法
        final int finalLayoutResId = layoutResId;
        Runnable dismissRunnable = () -> {
            dimView.animate().alpha(0f).setDuration(200).start();
            popupWindow.dismiss();
            rootView.removeView(dimView);
        };

        // 设置关闭按钮
        if (ivClose != null) {
            ivClose.setOnClickListener(v -> dismissRunnable.run());
        }

        // 设置确定按钮
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> dismissRunnable.run());
        }

        // 点击遮罩层关闭
        dimView.setOnClickListener(v -> dismissRunnable.run());

        // 回调：动态填充弹窗内容（放在通用按钮设置之后，以便覆盖默认行为）
        if (onPopulate != null) {
            onPopulate.accept(popupView);
        }

        // 显示弹窗（从底部弹出）
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);

        // 显示遮罩淡入动画
        dimView.animate().alpha(1f).setDuration(300).start();
    }

    private void showImageZoomDialog(int imageResId) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageResource(imageResId);
        imageView.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(imageView);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.black);
        dialog.getWindow().getDecorView().setBackgroundColor(0xFF000000);
        dialog.show();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int getResourceIdByName(String name) {
        if (name == null || name.isEmpty())
            return R.drawable.ic_paw_black;
        try {
            int resId = getResources().getIdentifier(name, "drawable", getPackageName());
            return resId != 0 ? resId : R.drawable.ic_paw_black;
        } catch (Exception e) {
            return R.drawable.ic_paw_black;
        }
    }

    private class ImagePagerAdapter extends PagerAdapter {
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView imageView = new ImageView(ProductDetailActivity.this);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageResource(currentImages.get(position));
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return currentImages.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }
}