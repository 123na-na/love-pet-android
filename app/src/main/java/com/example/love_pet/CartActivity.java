package com.example.love_pet;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private List<CartItem> cartItems;
    private List<CartItem> filteredItems;
    private boolean isManageMode = false;
    private boolean isSearchMode = false;
    private String searchKeyword = "";

    private PopupWindow currentPopupWindow;
    private View currentDimView;

    private LinearLayout llCartList;
    private RadioButton rbSelectAll;
    private TextView tvTotalPrice;
    private TextView tvManage;
    private TextView tvTitle;
    private LinearLayout layoutSearchBar;
    private EditText etSearch;
    private LinearLayout layoutEmpty;
    private ScrollView scrollCart;
    private Button btnCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        loadCartData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartData();
    }

    private void initViews() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tv_title);
        tvManage = findViewById(R.id.tv_manage);
        llCartList = findViewById(R.id.ll_cart_list);
        rbSelectAll = findViewById(R.id.rb_select_all);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        layoutSearchBar = findViewById(R.id.layout_search_bar);
        etSearch = findViewById(R.id.et_search);
        layoutEmpty = findViewById(R.id.layout_empty);
        scrollCart = findViewById(R.id.scroll_cart);
        btnCheckout = findViewById(R.id.btn_checkout);

        findViewById(R.id.iv_search).setOnClickListener(v -> toggleSearchMode());
        findViewById(R.id.tv_cancel_search).setOnClickListener(v -> toggleSearchMode());

        tvManage.setOnClickListener(v -> toggleManageMode());

        rbSelectAll.setOnClickListener(v -> {
            boolean isChecked = rbSelectAll.isChecked();
            for (CartItem item : cartItems) {
                item.setSelected(isChecked);
            }
            CartItem.saveCartItems(this, cartItems);
            renderCartList();
            updateTotalPrice();
        });

        btnCheckout.setOnClickListener(v -> {
            if (isManageMode) {
                List<CartItem> selectedItems = getSelectedItems();
                if (selectedItems.isEmpty()) {
                    Toast.makeText(this, "请选择要删除的商品", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (CartItem item : selectedItems) {
                    CartItem.removeFromCart(this, item.getProductId(), item.getStyleName());
                }
                loadCartData();
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
            } else {
                List<CartItem> selectedItems = getSelectedItems();
                if (selectedItems.isEmpty()) {
                    Toast.makeText(this, "请选择要结算的商品", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(CartActivity.this, ConfirmOrderActivity.class);
                startActivity(intent);
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchKeyword = s.toString().trim();
                filterItems();
                renderCartList();
                updateTotalPrice();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchKeyword = etSearch.getText().toString().trim();
                filterItems();
                renderCartList();
                updateTotalPrice();
                return true;
            }
            return false;
        });
    }

    private void loadCartData() {
        cartItems = CartItem.getCartItems(this);
        filterItems();
        updateTitle();
        renderCartList();
        updateTotalPrice();
        updateSelectAllState();
    }

    private void updateTitle() {
        int count = CartItem.getCartCount(this);
        tvTitle.setText("购物车(" + count + ")");
    }

    private void filterItems() {
        filteredItems = new ArrayList<>();
        if (searchKeyword.isEmpty()) {
            filteredItems.addAll(cartItems);
        } else {
            for (CartItem item : cartItems) {
                if (item.getTitle().toLowerCase().contains(searchKeyword.toLowerCase()) ||
                    item.getStyleName().toLowerCase().contains(searchKeyword.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
        }
    }

    private void toggleSearchMode() {
        isSearchMode = !isSearchMode;
        layoutSearchBar.setVisibility(isSearchMode ? View.VISIBLE : View.GONE);
        if (isSearchMode) {
            etSearch.requestFocus();
        } else {
            etSearch.setText("");
            searchKeyword = "";
            filterItems();
            renderCartList();
            updateTotalPrice();
        }
    }

    private void toggleManageMode() {
        isManageMode = !isManageMode;
        tvManage.setText(isManageMode ? "完成" : "管理");
        btnCheckout.setText(isManageMode ? "删除" : "结算");
        renderCartList();
    }

    private List<CartItem> getSelectedItems() {
        List<CartItem> selected = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selected.add(item);
            }
        }
        return selected;
    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                try {
                    double price = Double.parseDouble(item.getPrice());
                    total += price * item.getQuantity();
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        tvTotalPrice.setText(String.format("%.2f", total));
    }

    private void updateSelectAllState() {
        if (cartItems.isEmpty()) {
            rbSelectAll.setChecked(false);
            return;
        }
        boolean allSelected = true;
        for (CartItem item : cartItems) {
            if (!item.isSelected()) {
                allSelected = false;
                break;
            }
        }
        rbSelectAll.setChecked(allSelected);
    }

    private void renderCartList() {
        llCartList.removeAllViews();

        List<CartItem> displayItems = isSearchMode ? filteredItems : cartItems;

        if (displayItems.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            scrollCart.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        scrollCart.setVisibility(View.VISIBLE);

        for (int i = 0; i < displayItems.size(); i++) {
            CartItem item = displayItems.get(i);
            View itemView = createCartItemView(item);
            llCartList.addView(itemView);

            if (i < displayItems.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 12);
                llCartList.addView(divider, dividerParams);
            }
        }
    }

    private View createCartItemView(CartItem item) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setBackgroundColor(0xFFFFFFFF);

        int padding = dp2px(16);
        itemLayout.setPadding(padding, padding, padding, padding);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0xFFFFFFFF);
        bg.setCornerRadius(dp2px(12));
        itemLayout.setBackground(bg);

        LinearLayout shopRow = new LinearLayout(this);
        shopRow.setOrientation(LinearLayout.HORIZONTAL);
        shopRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams shopParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        shopParams.bottomMargin = dp2px(10);
        itemLayout.addView(shopRow, shopParams);

        ImageView shopIcon = new ImageView(this);
        shopIcon.setImageResource(android.R.drawable.ic_menu_myplaces);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp2px(16), dp2px(16));
        shopIcon.setColorFilter(0xFFFF6B35);
        shopRow.addView(shopIcon, iconParams);

        TextView shopNameTv = new TextView(this);
        shopNameTv.setText(item.getShopName());
        shopNameTv.setTextSize(14);
        shopNameTv.setTextColor(0xFF333333);
        shopNameTv.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams shopNameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        shopNameParams.leftMargin = dp2px(6);
        shopRow.addView(shopNameTv, shopNameParams);

        ImageView arrowIv = new ImageView(this);
        arrowIv.setImageResource(android.R.drawable.ic_menu_more);
        arrowIv.setColorFilter(0xFF999999);
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(
                dp2px(16), dp2px(16));
        arrowParams.leftMargin = dp2px(6);
        shopRow.addView(arrowIv, arrowParams);

        LinearLayout contentRow = new LinearLayout(this);
        contentRow.setOrientation(LinearLayout.HORIZONTAL);
        contentRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        itemLayout.addView(contentRow);

        RadioButton radioBtn = new RadioButton(this);
        radioBtn.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
        radioBtn.setChecked(item.isSelected());
        LinearLayout.LayoutParams radioParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        radioParams.rightMargin = dp2px(8);
        contentRow.addView(radioBtn, radioParams);

        radioBtn.setOnClickListener(v -> {
            boolean newSelected = !item.isSelected();
            item.setSelected(newSelected);
            radioBtn.setChecked(newSelected);
            CartItem.saveCartItems(this, cartItems);
            updateTotalPrice();
            updateSelectAllState();
        });

        ImageView productIv = new ImageView(this);
        int imageResId = getResources().getIdentifier(item.getImageName(), "drawable", getPackageName());
        if (imageResId == 0) {
            imageResId = R.drawable.ic_paw_black;
        }
        productIv.setImageResource(imageResId);
        productIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dp2px(110), dp2px(110));
        GradientDrawable imgBg = new GradientDrawable();
        imgBg.setColor(0xFFF5F5F5);
        imgBg.setCornerRadius(dp2px(6));
        productIv.setBackground(imgBg);
        contentRow.addView(productIv, imgParams);

        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.leftMargin = dp2px(10);
        contentRow.addView(infoLayout, infoParams);

        TextView titleTv = new TextView(this);
        titleTv.setText(item.getTitle());
        titleTv.setTextSize(14);
        titleTv.setTextColor(0xFF333333);
        titleTv.setMaxLines(2);
        titleTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        infoLayout.addView(titleTv);

        TextView styleTv = null;
        if (item.getStyleName() != null && !item.getStyleName().isEmpty()) {
            styleTv = new TextView(this);
            styleTv.setText(item.getStyleName() + " >");
            styleTv.setTextSize(12);
            styleTv.setTextColor(0xFF666666);
            GradientDrawable styleBg = new GradientDrawable();
            styleBg.setColor(0xFFF5F5F5);
            styleBg.setCornerRadius(dp2px(4));
            styleTv.setBackground(styleBg);
            styleTv.setPadding(dp2px(10), dp2px(4), dp2px(10), dp2px(4));
            LinearLayout.LayoutParams styleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            styleParams.topMargin = dp2px(8);
            infoLayout.addView(styleTv, styleParams);

            final CartItem finalItem = item;
            styleTv.setOnClickListener(v -> {
                showStylePickerDialog(finalItem);
            });
        }

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams bottomParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomParams.topMargin = dp2px(10);
        infoLayout.addView(bottomRow, bottomParams);

        TextView priceTv = new TextView(this);
        priceTv.setText("¥" + item.getPrice());
        priceTv.setTextSize(16);
        priceTv.setTextColor(0xFFFF3B30);
        priceTv.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        bottomRow.addView(priceTv, priceParams);

        View.OnClickListener toggleSelectListener = v -> {
            item.setSelected(!item.isSelected());
            CartItem.saveCartItems(this, cartItems);
            renderCartList();
            updateTotalPrice();
            updateSelectAllState();
        };

        View.OnClickListener goToDetailListener = v -> {
            Intent intent = new Intent(CartActivity.this, ProductDetailActivity.class);
            intent.putExtra("product_id", item.getProductId());
            startActivity(intent);
        };

        productIv.setOnClickListener(goToDetailListener);
        infoLayout.setOnClickListener(goToDetailListener);
        titleTv.setOnClickListener(goToDetailListener);

        bottomRow.setOnClickListener(goToDetailListener);
        priceTv.setOnClickListener(goToDetailListener);

        if (isManageMode) {
            TextView deleteTv = new TextView(this);
            deleteTv.setText("删除");
            deleteTv.setTextSize(13);
            deleteTv.setTextColor(0xFFFF6B35);
            GradientDrawable deleteBg = new GradientDrawable();
            deleteBg.setColor(0xFFF5F5F5);
            deleteBg.setCornerRadius(dp2px(4));
            deleteTv.setBackground(deleteBg);
            deleteTv.setPadding(dp2px(10), dp2px(4), dp2px(10), dp2px(4));
            deleteTv.setOnClickListener(v -> {
                CartItem.removeFromCart(this, item.getProductId(), item.getStyleName());
                loadCartData();
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
            });
            bottomRow.addView(deleteTv);
        } else {
            LinearLayout qtyLayout = new LinearLayout(this);
            qtyLayout.setOrientation(LinearLayout.HORIZONTAL);
            qtyLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);
            GradientDrawable qtyBg = new GradientDrawable();
            qtyBg.setStroke(1, 0xFFE0E0E0);
            qtyBg.setCornerRadius(dp2px(4));
            qtyLayout.setBackground(qtyBg);

            TextView minusBtn = new TextView(this);
            minusBtn.setText("-");
            minusBtn.setTextSize(14);
            minusBtn.setTextColor(0xFF666666);
            minusBtn.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams minusParams = new LinearLayout.LayoutParams(
                    dp2px(24), dp2px(24));
            minusBtn.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    CartItem.updateQuantity(this, item.getProductId(), item.getStyleName(), item.getQuantity());
                    loadCartData();
                }
            });
            qtyLayout.addView(minusBtn, minusParams);

            TextView qtyTv = new TextView(this);
            qtyTv.setText(String.valueOf(item.getQuantity()));
            qtyTv.setTextSize(12);
            qtyTv.setTextColor(0xFF333333);
            qtyTv.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams qtyParams = new LinearLayout.LayoutParams(
                    dp2px(28), dp2px(24));
            qtyLayout.addView(qtyTv, qtyParams);

            TextView plusBtn = new TextView(this);
            plusBtn.setText("+");
            plusBtn.setTextSize(14);
            plusBtn.setTextColor(0xFF666666);
            plusBtn.setGravity(android.view.Gravity.CENTER);
            LinearLayout.LayoutParams plusParams = new LinearLayout.LayoutParams(
                    dp2px(24), dp2px(24));
            plusBtn.setOnClickListener(v -> {
                item.setQuantity(item.getQuantity() + 1);
                CartItem.updateQuantity(this, item.getProductId(), item.getStyleName(), item.getQuantity());
                loadCartData();
            });
            qtyLayout.addView(plusBtn, plusParams);

            bottomRow.addView(qtyLayout);
        }

        return itemLayout;
    }

    private void showStylePickerDialog(CartItem cartItem) {
        ProductDataImporter importer = new ProductDataImporter(this);
        ProductDataImporter.ProductDetail productDetail = importer.getProductDetailById(cartItem.getProductId());

        if (productDetail == null || productDetail.stylesJson.isEmpty()) {
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_order, null);
        GradientDrawable dialogBg = new GradientDrawable();
        dialogBg.setColor(0xFFFFFFFF);
        dialogBg.setCornerRadii(new float[]{dp2px(16), dp2px(16), dp2px(16), dp2px(16), 0, 0, 0, 0});
        dialogView.setBackground(dialogBg);

        LinearLayout llAddressSection = dialogView.findViewById(R.id.ll_address_section);
        if (llAddressSection != null) {
            llAddressSection.setVisibility(View.GONE);
        }

        ImageView ivProductImg = dialogView.findViewById(R.id.iv_order_product_image);
        TextView tvPriceLabel = dialogView.findViewById(R.id.tv_order_price_label);
        TextView tvDiscountHint = dialogView.findViewById(R.id.tv_order_discount_hint);
        ImageButton btnMinus = dialogView.findViewById(R.id.btn_qty_minus);
        ImageButton btnPlus = dialogView.findViewById(R.id.btn_qty_plus);
        TextView tvQty = dialogView.findViewById(R.id.tv_order_quantity);
        LinearLayout llStyles = dialogView.findViewById(R.id.ll_style_options);
        TextView tvStyleTitle = dialogView.findViewById(R.id.tv_style_category_title);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit_order);
        RadioGroup rgPayment = dialogView.findViewById(R.id.rg_payment_method);
        if (rgPayment != null) {
            View paymentParent = (View) rgPayment.getParent();
            if (paymentParent != null) {
                paymentParent.setVisibility(View.GONE);
            }
        }

        final int[] selectedStyleIndex = {0};
        final int[] quantity = {cartItem.getQuantity()};

        try {
            org.json.JSONArray stylesArr = new org.json.JSONArray(productDetail.stylesJson);
            for (int i = 0; i < stylesArr.length(); i++) {
                org.json.JSONObject styleObj = stylesArr.getJSONObject(i);
                String name = styleObj.optString("name", "");
                if (name.equals(cartItem.getStyleName())) {
                    selectedStyleIndex[0] = i;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ivProductImg != null) {
            int resId = getResources().getIdentifier(cartItem.getImageName(), "drawable", getPackageName());
            if (resId != 0) {
                ivProductImg.setImageResource(resId);
            }
        }

        if (tvQty != null) {
            tvQty.setText(String.valueOf(quantity[0]));
        }

        if (btnSubmit != null) {
            btnSubmit.setText("确认");
        }

        Runnable updatePrice = () -> {
            try {
                if (productDetail.stylesJson.isEmpty()) return;
                org.json.JSONArray stylesArr = new org.json.JSONArray(productDetail.stylesJson);
                if (selectedStyleIndex[0] >= stylesArr.length()) return;
                org.json.JSONObject styleObj = stylesArr.getJSONObject(selectedStyleIndex[0]);
                double stylePrice = styleObj.optDouble("price", Double.parseDouble(productDetail.price));
                double styleOrigPrice = styleObj.optDouble("originalPrice",
                        Double.parseDouble(productDetail.originalPrice));
                double total = stylePrice * quantity[0];
                String priceStr = String.format("%.2f", stylePrice);
                String origStr = String.format("%.2f", styleOrigPrice);
                if (tvPriceLabel != null)
                    tvPriceLabel.setText("实付 ¥" + priceStr + " | 优惠前 ¥" + origStr);
                if (tvDiscountHint != null)
                    tvDiscountHint.setVisibility(View.GONE);
                if (btnSubmit != null)
                    btnSubmit.setText("确认");

                if (ivProductImg != null) {
                    String imgResName = styleObj.optString("imageRes",
                            productDetail.imageNames != null && productDetail.imageNames.length > 0
                                    ? productDetail.imageNames[0]
                                    : "");
                    int resId = getResources().getIdentifier(imgResName, "drawable", getPackageName());
                    if (resId != 0)
                        ivProductImg.setImageResource(resId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        updatePrice.run();

        if (btnMinus != null) {
            btnMinus.setOnClickListener(v -> {
                if (quantity[0] > 1) {
                    quantity[0]--;
                    if (tvQty != null)
                        tvQty.setText(String.valueOf(quantity[0]));
                    updatePrice.run();
                }
            });
        }
        if (btnPlus != null) {
            btnPlus.setOnClickListener(v -> {
                if (quantity[0] < 99) {
                    quantity[0]++;
                    if (tvQty != null)
                        tvQty.setText(String.valueOf(quantity[0]));
                    updatePrice.run();
                }
            });
        }

        if (llStyles != null && !productDetail.stylesJson.isEmpty()) {
            try {
                org.json.JSONArray stylesArr = new org.json.JSONArray(productDetail.stylesJson);
                if (tvStyleTitle != null)
                    tvStyleTitle.setText("颜色分类 (" + stylesArr.length() + ")");

                java.util.List<View> styleItemViews = new java.util.ArrayList<>();

                for (int i = 0; i < stylesArr.length(); i++) {
                    final int idx = i;
                    org.json.JSONObject styleObj = stylesArr.getJSONObject(i);
                    String name = styleObj.optString("name", "");
                    double price = styleObj.optDouble("price", Double.parseDouble(productDetail.price));
                    String imageRes = styleObj.optString("imageRes", "");

                    LinearLayout item = new LinearLayout(this);
                    item.setOrientation(LinearLayout.HORIZONTAL);
                    item.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    GradientDrawable d = new GradientDrawable();
                    d.setStroke(dp2px(1), idx == selectedStyleIndex[0] ? 0xFFFF5722 : 0xFFDDDDDD);
                    d.setColor(idx == selectedStyleIndex[0] ? 0xFFFFF8F0 : 0xFFFFFFFF);
                    d.setCornerRadius(dp2px(8));
                    item.setBackground(d);
                    item.setPadding(dp2px(10), dp2px(8), dp2px(10), dp2px(8));

                    int imgResId = getResources().getIdentifier(imageRes, "drawable", getPackageName());
                    if (imgResId != 0) {
                        ImageView styleImg = new ImageView(this);
                        styleImg.setImageResource(imgResId);
                        styleImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dp2px(60), dp2px(60));
                        imgParams.rightMargin = dp2px(10);
                        styleImg.setBackgroundColor(0xFFF5F5F5);
                        item.addView(styleImg, imgParams);
                    }

                    LinearLayout textLayout = new LinearLayout(this);
                    textLayout.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                    item.addView(textLayout, textParams);

                    TextView nameTv = new TextView(this);
                    nameTv.setText(name);
                    nameTv.setTextSize(14);
                    nameTv.setTextColor(0xFF333333);
                    nameTv.setMaxLines(2);
                    nameTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    textLayout.addView(nameTv);

                    TextView priceTv = new TextView(this);
                    priceTv.setText("¥" + String.format("%.2f", price));
                    priceTv.setTextSize(14);
                    priceTv.setTextColor(0xFFFF3B30);
                    priceTv.setTypeface(null, android.graphics.Typeface.BOLD);
                    LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    priceParams.topMargin = dp2px(4);
                    textLayout.addView(priceTv, priceParams);

                    item.setOnClickListener(v -> {
                        selectedStyleIndex[0] = idx;
                        for (int j = 0; j < styleItemViews.size(); j++) {
                            View view = styleItemViews.get(j);
                            GradientDrawable d1 = new GradientDrawable();
                            d1.setStroke(dp2px(1), j == idx ? 0xFFFF5722 : 0xFFDDDDDD);
                            d1.setColor(j == idx ? 0xFFFFF8F0 : 0xFFFFFFFF);
                            d1.setCornerRadius(dp2px(8));
                            view.setBackground(d1);
                        }
                        updatePrice.run();
                    });

                    styleItemViews.add(item);

                    LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    llStyles.addView(item, itemParams);

                    if (i < stylesArr.length() - 1) {
                        View gap = new View(this);
                        gap.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT, dp2px(8)));
                        llStyles.addView(gap);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
        View dimView = new View(this);
        dimView.setBackgroundColor(0x80000000);
        FrameLayout.LayoutParams dimParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.addView(dimView, dimParams);

        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int popupHeight = (int) (screenHeight * 0.7);
        PopupWindow popupWindow = new PopupWindow(dialogView,
                ViewGroup.LayoutParams.MATCH_PARENT, popupHeight);
        popupWindow.setFocusable(true);
        popupWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
        popupWindow.setOnDismissListener(() -> {
            rootView.removeView(dimView);
        });

        currentPopupWindow = popupWindow;
        currentDimView = dimView;

        dimView.setOnClickListener(v -> popupWindow.dismiss());

        ImageView ivClose = dialogView.findViewById(R.id.iv_close_order);
        if (ivClose != null) {
            ivClose.setOnClickListener(v -> popupWindow.dismiss());
        }

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                try {
                    org.json.JSONArray stylesArr = new org.json.JSONArray(productDetail.stylesJson);
                    if (selectedStyleIndex[0] < stylesArr.length()) {
                        org.json.JSONObject styleObj = stylesArr.getJSONObject(selectedStyleIndex[0]);
                        String newStyleName = styleObj.optString("name", "");
                        double newPrice = styleObj.optDouble("price", Double.parseDouble(productDetail.price));
                        double newOrigPrice = styleObj.optDouble("originalPrice", Double.parseDouble(productDetail.originalPrice));
                        String newImageName = styleObj.optString("imageRes", "");

                        String oldStyleName = cartItem.getStyleName();
                        cartItem.setStyleName(newStyleName);
                        cartItem.setPrice(String.valueOf(newPrice));
                        cartItem.setOriginalPrice(String.valueOf(newOrigPrice));
                        cartItem.setImageName(newImageName);
                        cartItem.setQuantity(quantity[0]);

                        List<CartItem> items = CartItem.getCartItems(this);
                        for (CartItem item : items) {
                            if (item.getProductId() == cartItem.getProductId() &&
                                item.getStyleName().equals(oldStyleName)) {
                                item.setStyleName(newStyleName);
                                item.setPrice(String.valueOf(newPrice));
                                item.setOriginalPrice(String.valueOf(newOrigPrice));
                                item.setImageName(newImageName);
                                item.setQuantity(quantity[0]);
                                break;
                            }
                        }
                        CartItem.saveCartItems(this, items);
                        loadCartData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                popupWindow.dismiss();
            });
        }

        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
