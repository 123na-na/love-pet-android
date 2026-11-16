package com.example.love_pet;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfirmOrderActivity extends AppCompatActivity {

    public static final String EXTRA_FROM_BUY_NOW = "from_buy_now";
    private static final int REQ_PICK_ADDRESS = 1001;
    private static final int REQ_PAYMENT_PASSWORD = 1002;

    private List<CartItem> selectedItems;
    private double totalAmount = 0;
    private int totalCount = 0;
    private double totalOriginalAmount = 0;

    private TextView tvRecipientInfo;
    private TextView tvFullAddress;
    private LinearLayout llProductsContainer;
    private TextView tvItemCount;
    private TextView tvDiscountInfo;
    private TextView tvTotalAmount;
    private TextView btnPayNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);

        initViews();
        loadSelectedItems();
        loadAddress();
        renderProducts();
        updateTotals();
    }

    private void initViews() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        tvRecipientInfo = findViewById(R.id.tv_recipient_info);
        tvFullAddress = findViewById(R.id.tv_full_address);
        llProductsContainer = findViewById(R.id.ll_products_container);
        tvItemCount = findViewById(R.id.tv_item_count);
        tvDiscountInfo = findViewById(R.id.tv_discount_info);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        btnPayNow = findViewById(R.id.btn_pay_now);

        findViewById(R.id.ll_address_card).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddressPickerActivity.class);
            startActivityForResult(intent, REQ_PICK_ADDRESS);
        });

        btnPayNow.setOnClickListener(v -> {
            if (selectedItems == null || selectedItems.isEmpty()) {
                Toast.makeText(this, "请选择商品", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, PaymentPasswordActivity.class);
            if (PaymentPasswordActivity.isPasswordSet(this)) {
                intent.putExtra(PaymentPasswordActivity.EXTRA_MODE, PaymentPasswordActivity.MODE_VERIFY);
            } else {
                intent.putExtra(PaymentPasswordActivity.EXTRA_MODE, PaymentPasswordActivity.MODE_SET);
                Toast.makeText(this, "请先设置支付密码", Toast.LENGTH_SHORT).show();
            }
            startActivityForResult(intent, REQ_PAYMENT_PASSWORD);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_ADDRESS && resultCode == RESULT_OK) {
            loadAddress();
        } else if (requestCode == REQ_PAYMENT_PASSWORD && resultCode == RESULT_OK) {
            OrderManager.addOrder(this, selectedItems, totalAmount);

            boolean fromBuyNow = getIntent().getBooleanExtra(EXTRA_FROM_BUY_NOW, false);
            if (!fromBuyNow) {
                for (CartItem item : selectedItems) {
                    CartItem.removeFromCart(this, item.getProductId(), item.getStyleName());
                }
            }

            Toast.makeText(this, "支付成功", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, OrderActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void loadSelectedItems() {
        selectedItems = new ArrayList<>();
        boolean fromBuyNow = getIntent().getBooleanExtra(EXTRA_FROM_BUY_NOW, false);
        if (fromBuyNow) {
            selectedItems = CartItem.getBuyNowItems(this);
        } else {
            List<CartItem> allItems = CartItem.getCartItems(this);
            for (CartItem item : allItems) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }
        }
    }

    private void loadAddress() {
        Address addr = getSelectedAddressItem();
        if (addr != null) {
            tvRecipientInfo.setText(addr.getName() + "  " + addr.getPhone());
            tvFullAddress.setText(addr.getFullAddress());
        } else {
            tvRecipientInfo.setText("请选择收货地址");
            tvFullAddress.setText("");
        }
    }

    private Address getSelectedAddressItem() {
        String prefKey = "address_prefs_" + UserManager.getInstance().getCurrentUserId();
        android.content.SharedPreferences prefs = getSharedPreferences(prefKey, MODE_PRIVATE);
        String selectedId = prefs.getString("selected_address_id", "");
        if (selectedId.isEmpty()) return null;
        List<Address> addresses = Address.getAddresses(this);
        for (Address addr : addresses) {
            if (addr.getId().equals(selectedId)) {
                return addr;
            }
        }
        return null;
    }

    private void renderProducts() {
        llProductsContainer.removeAllViews();

        Map<String, List<CartItem>> shopMap = new LinkedHashMap<>();
        for (CartItem item : selectedItems) {
            String shopName = item.getShopName() != null ? item.getShopName() : "官方旗舰店";
            if (!shopMap.containsKey(shopName)) {
                shopMap.put(shopName, new ArrayList<>());
            }
            shopMap.get(shopName).add(item);
        }

        boolean isFirstShop = true;
        for (Map.Entry<String, List<CartItem>> entry : shopMap.entrySet()) {
            String shopName = entry.getKey();
            List<CartItem> shopItems = entry.getValue();

            LinearLayout shopCard = new LinearLayout(this);
            shopCard.setOrientation(LinearLayout.VERTICAL);
            shopCard.setBackgroundResource(R.drawable.bg_order_card);
            shopCard.setPadding(dp2px(14), dp2px(12), dp2px(14), dp2px(12));

            if (!isFirstShop) {
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                cardParams.topMargin = dp2px(12);
                shopCard.setLayoutParams(cardParams);
            }
            isFirstShop = false;

            LinearLayout shopHeader = new LinearLayout(this);
            shopHeader.setOrientation(LinearLayout.HORIZONTAL);
            shopHeader.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            headerParams.bottomMargin = dp2px(10);
            shopCard.addView(shopHeader, headerParams);

            TextView shopNameTv = new TextView(this);
            shopNameTv.setText(shopName);
            shopNameTv.setTextColor(0xFF333333);
            shopNameTv.setTextSize(14);
            shopNameTv.setTypeface(null, Typeface.BOLD);
            LinearLayout.LayoutParams shopNameParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            shopHeader.addView(shopNameTv, shopNameParams);

            ImageView arrowIv = new ImageView(this);
            arrowIv.setImageResource(R.drawable.ic_arrow_right_gray);
            LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(dp2px(16), dp2px(16));
            shopHeader.addView(arrowIv, arrowParams);

            for (int i = 0; i < shopItems.size(); i++) {
                CartItem item = shopItems.get(i);
                View itemView = createProductItemView(item);
                shopCard.addView(itemView);

                if (i < shopItems.size() - 1) {
                    View divider = new View(this);
                    LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, dp2px(10));
                    shopCard.addView(divider, dividerParams);
                }
            }

            View infoDivider = new View(this);
            infoDivider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp2px(1)));
            infoDivider.setBackgroundColor(0xFFF0F0F0);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp2px(1));
            dividerParams.topMargin = dp2px(12);
            shopCard.addView(infoDivider, dividerParams);

            LinearLayout deliveryRow = createInfoRow("配送", "快递 包邮", "预计3小时内发货", true);
            LinearLayout.LayoutParams deliveryParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            deliveryParams.topMargin = dp2px(12);
            shopCard.addView(deliveryRow, deliveryParams);

            LinearLayout returnRow = createInfoRow("退货权益", "退换货免首重运费", "", false);
            LinearLayout.LayoutParams returnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            returnParams.topMargin = dp2px(10);
            shopCard.addView(returnRow, returnParams);

            llProductsContainer.addView(shopCard);
        }
    }

    private View createProductItemView(CartItem item) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setGravity(Gravity.TOP);

        ImageView productIv = new ImageView(this);
        int imageResId = getResources().getIdentifier(item.getImageName(), "drawable", getPackageName());
        if (imageResId == 0) imageResId = R.drawable.ic_paw_black;
        productIv.setImageResource(imageResId);
        productIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dp2px(90), dp2px(90));
        GradientDrawable imgBg = new GradientDrawable();
        imgBg.setColor(0xFFF5F5F5);
        imgBg.setCornerRadius(dp2px(6));
        productIv.setBackground(imgBg);
        itemLayout.addView(productIv, imgParams);

        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        infoParams.leftMargin = dp2px(10);
        itemLayout.addView(infoLayout, infoParams);

        TextView titleTv = new TextView(this);
        titleTv.setText(item.getTitle());
        titleTv.setTextColor(0xFF333333);
        titleTv.setTextSize(13);
        titleTv.setMaxLines(2);
        titleTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        infoLayout.addView(titleTv);

        if (item.getStyleName() != null && !item.getStyleName().isEmpty()) {
            TextView styleTv = new TextView(this);
            styleTv.setText("颜色分类:" + item.getStyleName());
            styleTv.setTextColor(0xFF999999);
            styleTv.setTextSize(11);
            LinearLayout.LayoutParams styleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            styleParams.topMargin = dp2px(4);
            infoLayout.addView(styleTv, styleParams);
        }

        TextView serviceTv = new TextView(this);
        serviceTv.setText("可享先用后付 假一赔四");
        serviceTv.setTextColor(0xFF999999);
        serviceTv.setTextSize(11);
        LinearLayout.LayoutParams serviceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        serviceParams.topMargin = dp2px(6);
        infoLayout.addView(serviceTv, serviceParams);

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams bottomParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomParams.topMargin = dp2px(8);
        infoLayout.addView(bottomRow, bottomParams);

        TextView priceTv = new TextView(this);
        priceTv.setText("¥" + item.getPrice());
        priceTv.setTextColor(0xFFFF3B30);
        priceTv.setTextSize(15);
        priceTv.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        bottomRow.addView(priceTv, priceParams);

        LinearLayout qtyLayout = new LinearLayout(this);
        qtyLayout.setOrientation(LinearLayout.HORIZONTAL);
        qtyLayout.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable qtyBg = new GradientDrawable();
        qtyBg.setColor(0xFFF8F8F8);
        qtyBg.setCornerRadius(dp2px(4));
        qtyLayout.setBackground(qtyBg);

        TextView minusTv = new TextView(this);
        minusTv.setText("-");
        minusTv.setTextSize(14);
        minusTv.setTextColor(0xFF666666);
        minusTv.setGravity(Gravity.CENTER);
        minusTv.setEnabled(false);
        LinearLayout.LayoutParams minusParams = new LinearLayout.LayoutParams(dp2px(28), dp2px(26));
        qtyLayout.addView(minusTv, minusParams);

        TextView qtyTv = new TextView(this);
        qtyTv.setText(String.valueOf(item.getQuantity()));
        qtyTv.setTextSize(13);
        qtyTv.setTextColor(0xFF333333);
        qtyTv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams qtyParams = new LinearLayout.LayoutParams(dp2px(32), dp2px(26));
        qtyLayout.addView(qtyTv, qtyParams);

        TextView plusTv = new TextView(this);
        plusTv.setText("+");
        plusTv.setTextSize(14);
        plusTv.setTextColor(0xFF666666);
        plusTv.setGravity(Gravity.CENTER);
        plusTv.setEnabled(false);
        LinearLayout.LayoutParams plusParams = new LinearLayout.LayoutParams(dp2px(28), dp2px(26));
        qtyLayout.addView(plusTv, plusParams);

        bottomRow.addView(qtyLayout);

        return itemLayout;
    }

    private LinearLayout createInfoRow(String label, String value, String rightValue, boolean rightIsGreen) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView labelTv = new TextView(this);
        labelTv.setText(label);
        labelTv.setTextColor(0xFF999999);
        labelTv.setTextSize(13);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        row.addView(labelTv, labelParams);

        TextView valueTv = new TextView(this);
        valueTv.setText(value);
        valueTv.setTextColor(0xFF333333);
        valueTv.setTextSize(13);
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        valueParams.leftMargin = dp2px(20);
        row.addView(valueTv, valueParams);

        if (!TextUtils.isEmpty(rightValue)) {
            TextView rightTv = new TextView(this);
            rightTv.setText(rightValue);
            rightTv.setTextColor(rightIsGreen ? 0xFF4CAF50 : 0xFF333333);
            rightTv.setTextSize(13);
            row.addView(rightTv);
        }

        return row;
    }

    private void updateTotals() {
        totalAmount = 0;
        totalCount = 0;
        totalOriginalAmount = 0;

        for (CartItem item : selectedItems) {
            try {
                double price = Double.parseDouble(item.getPrice());
                totalAmount += price * item.getQuantity();
                totalCount += item.getQuantity();

                if (item.getOriginalPrice() != null && !item.getOriginalPrice().isEmpty()) {
                    double origPrice = Double.parseDouble(item.getOriginalPrice());
                    totalOriginalAmount += origPrice * item.getQuantity();
                } else {
                    totalOriginalAmount += price * item.getQuantity();
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        tvItemCount.setText("共" + totalCount + "件");
        tvTotalAmount.setText("¥" + String.format("%.2f", totalAmount));
        btnPayNow.setText("立即支付 ¥" + String.format("%.2f", totalAmount));

        double discount = totalOriginalAmount - totalAmount;
        if (discount > 0.01) {
            tvDiscountInfo.setText("共减 ¥" + String.format("%.2f", discount));
            tvDiscountInfo.setVisibility(View.VISIBLE);
        } else {
            tvDiscountInfo.setVisibility(View.GONE);
        }
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
