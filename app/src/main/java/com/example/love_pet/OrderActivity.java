package com.example.love_pet;

import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.ColorUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 订单页面 - 与设计稿完全一致
 */
public class OrderActivity extends AppCompatActivity {

    private LinearLayout orderListContainer;
    private int currentTab = 0; // 0=全部, 1=待付款, 2=待收货/代使用, 3=待评价, 4=退款/售后

    // Tab 标题
    private static final String[] TABS = { "全部", "待付款", "待收货/代使用", "待评价", "退款/售后" };

    // Tab IDs
    private final int[] tabIds = { R.id.tab_all, R.id.tab_unpaid, R.id.tab_pending, R.id.tab_review, R.id.tab_refund };

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        UserManager.getInstance().loadUsers(this);

        // 返回按钮
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        // 初始化控件
        orderListContainer = findViewById(R.id.order_list_container);

        // 读取传入的 Tab 索引（默认0=全部）
        currentTab = getIntent().getIntExtra("tab_index", 0);

        // 设置 Tab 点击事件
        setupTabs();

        // 加载订单数据
        loadOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    /**
     * 设置 Tab 切换
     */
    private void setupTabs() {
        for (int i = 0; i < tabIds.length; i++) {
            final int index = i;
            View tabView = findViewById(tabIds[i]);
            tabView.setOnClickListener(v -> selectTab(index));
        }
        selectTab(currentTab); // 使用传入的 tab_index，默认为0
    }

    /**
     * 切换 Tab
     */
    private void selectTab(int index) {
        currentTab = index;
        for (int i = 0; i < tabIds.length; i++) {
            View tabView = findViewById(tabIds[i]);
            FrameLayout frameLayout = (FrameLayout) tabView;
            TextView textView = (TextView) ((android.widget.FrameLayout) tabView).getChildAt(0);
            View underline = ((android.widget.FrameLayout) tabView).getChildAt(1);

            if (i == index) {
                textView.setTextColor(Color.parseColor("#333333"));
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                underline.setVisibility(View.VISIBLE);
            } else {
                textView.setTextColor(Color.parseColor("#666666"));
                textView.setTypeface(Typeface.DEFAULT);
                underline.setVisibility(View.INVISIBLE);
            }
        }
        loadOrders();
    }

    /**
     * 加载订单列表（模拟数据 + 真实订单）
     */
    private void loadOrders() {
        orderListContainer.removeAllViews();

        List<OrderItem> allOrders = new ArrayList<>();

        List<OrderManager.Order> realOrders = OrderManager.getOrders(this);
        for (OrderManager.Order realOrder : realOrders) {
            OrderItem order = new OrderItem();
            order.orderId = realOrder.orderId;
            order.shopName = realOrder.shopName;
            order.shopAvatar = ShopActivity.Product.getShopAvatarRes(realOrder.shopName);
            order.status = realOrder.status;
            order.statusType = realOrder.statusType;
            order.orderTime = realOrder.orderTime;
            order.totalPrice = (float) realOrder.totalPrice;
            order.itemCount = realOrder.itemCount;
            order.serviceDate = realOrder.serviceDate;
            order.serviceTime = realOrder.serviceTime;
            order.serviceAddress = realOrder.serviceAddress;

            if (realOrder.isServiceOrder) {
                order.isProductOrder = false;
                for (OrderManager.OrderProduct p : realOrder.products) {
                    ServiceItem svc = new ServiceItem();
                    svc.name = p.name;
                    try {
                        String priceStr = p.price.replace("¥", "").trim();
                        svc.price = Float.parseFloat(priceStr);
                    } catch (Exception e) {
                        svc.price = 0;
                    }
                    order.services.add(svc);
                }
                order.actions.add(new Action("取消预约", false));
                order.actions.add(new Action("修改预约", true));
            } else {
                order.isProductOrder = true;
                order.products = realOrder.products;
                order.actions.add(new Action("查看物流", false));
                order.actions.add(new Action("确认收货", true));
            }
            allOrders.add(order);
        }

        // 根据 Tab 筛选
        List<OrderItem> filtered = new ArrayList<>();
        for (OrderItem order : allOrders) {
            if (currentTab == 0) {
                // 全部
                filtered.add(order);
            } else if (currentTab == 1 && "unpaid".equals(order.statusType)) {
                // 待付款
                filtered.add(order);
            } else if (currentTab == 2 && "pending".equals(order.statusType)) {
                // 待收货/代使用
                filtered.add(order);
            } else if (currentTab == 3 && "review".equals(order.statusType)) {
                // 待评价
                filtered.add(order);
            } else if (currentTab == 4 && "refund".equals(order.statusType)) {
                // 退款/售后
                filtered.add(order);
            }
        }

        if (filtered.isEmpty()) {
            showEmptyState();
            return;
        }

        for (OrderItem order : filtered) {
            addOrderCard(order);
        }
    }

    /**
     * 获取所有模拟订单数据
     */
    private List<OrderItem> getAllMockOrderData() {
        List<OrderItem> orders = new ArrayList<>();

        // 订单1：已完成 - 毛发护理 + 洗澡
        OrderItem order1 = new OrderItem();
        order1.shopName = "宜家宠物洗护";
        order1.status = "已完成";
        order1.statusType = "completed";
        order1.orderTime = "2025-11-11 11:11";

        ServiceItem s1 = new ServiceItem();
        s1.name = "毛发护理";
        s1.price = 46.00f;

        ServiceItem s2 = new ServiceItem();
        s2.name = "洗澡";
        s2.price = 38.00f;

        order1.services.add(s1);
        order1.services.add(s2);
        order1.totalPrice = 84.00f;
        order1.itemCount = 2;
        order1.actions.add(new Action("评价", false));
        order1.actions.add(new Action("再来一单", true));
        orders.add(order1);

        // 订单2：待付款 - 宠物美容套餐
        OrderItem order2 = new OrderItem();
        order2.shopName = "萌宠乐园";
        order2.status = "待付款";
        order2.statusType = "unpaid";
        order2.orderTime = "2025-12-01 15:30";

        ServiceItem s3 = new ServiceItem();
        s3.name = "美容套餐";
        s3.price = 128.00f;

        order2.services.add(s3);
        order2.totalPrice = 128.00f;
        order2.itemCount = 1;
        order2.actions.add(new Action("去支付", true));
        order2.actions.add(new Action("取消", false));
        orders.add(order2);

        // 订单3：待收货/代使用 - 宠物零食大礼包
        OrderItem order3 = new OrderItem();
        order3.shopName = "爱宠商城";
        order3.status = "待收货";
        order3.statusType = "pending";
        order3.orderTime = "2025-11-28 09:20";

        ServiceItem s4 = new ServiceItem();
        s4.name = "宠物零食礼包";
        s4.price = 59.90f;

        ServiceItem s5 = new ServiceItem();
        s5.name = "营养膏";
        s5.price = 35.00f;

        order3.services.add(s4);
        order3.services.add(s5);
        order3.totalPrice = 94.90f;
        order3.itemCount = 2;
        order3.actions.add(new Action("查看物流", false));
        order3.actions.add(new Action("确认收货", true));
        orders.add(order3);

        // 订单4：待评价 - 宠物洗澡服务
        OrderItem order4 = new OrderItem();
        order4.shopName = "汪星人洗护中心";
        order4.status = "待评价";
        order4.statusType = "review";
        order4.orderTime = "2025-11-25 14:10";

        ServiceItem s6 = new ServiceItem();
        s6.name = "精洗+吹干";
        s6.price = 68.00f;

        order4.services.add(s6);
        order4.totalPrice = 68.00f;
        order4.itemCount = 1;
        order4.actions.add(new Action("去评价", true));
        order4.actions.add(new Action("再次购买", false));
        orders.add(order4);

        // 订单5：退款/售后 - 宠物玩具
        OrderItem order5 = new OrderItem();
        order5.shopName = "爱宠商城";
        order5.status = "退款中";
        order5.statusType = "refund";
        order5.orderTime = "2025-11-20 16:45";

        ServiceItem s7 = new ServiceItem();
        s7.name = "互动飞盘";
        s7.price = 29.90f;

        order5.services.add(s7);
        order5.totalPrice = 29.90f;
        order5.itemCount = 1;
        order5.actions.add(new Action("查看详情", false));
        order5.actions.add(new Action("撤销申请", true));
        orders.add(order5);

        return orders;
    }

    /**
     * 添加订单卡片
     */
    private void addOrderCard(OrderItem order) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_order, orderListContainer, false);

        // 店铺头像
        ImageView ivAvatar = cardView.findViewById(R.id.iv_shop_avatar);
        ivAvatar.setImageResource(order.shopAvatar != 0 ? order.shopAvatar : R.drawable.ic_paw_black);
        ivAvatar.setClipToOutline(true);
        ivAvatar.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });

        // 店铺名
        TextView tvShopName = cardView.findViewById(R.id.tv_shop_name);
        tvShopName.setText(order.shopName + " >");

        // 状态（根据状态类型显示不同颜色）
        TextView tvStatus = cardView.findViewById(R.id.tv_order_status);
        tvStatus.setText(order.status);
        switch (order.statusType) {
            case "unpaid":
                tvStatus.setTextColor(Color.parseColor("#FF9800")); // 橙色-待付款
                break;
            case "pending":
                tvStatus.setTextColor(Color.parseColor("#2196F3")); // 蓝色-待收货
                break;
            case "review":
                tvStatus.setTextColor(Color.parseColor("#4CAF50")); // 绿色-待评价
                break;
            case "refund":
                tvStatus.setTextColor(Color.parseColor("#F44336")); // 红色-退款
                break;
            default:
                tvStatus.setTextColor(Color.parseColor("#999999")); // 灰色-已完成
        }

        // 商品/服务项容器
        LinearLayout serviceContainer = cardView.findViewById(R.id.service_items_container);
        if (order.isProductOrder && order.products != null && !order.products.isEmpty()) {
            // 商品订单：显示商品图片+名称
            for (int i = 0; i < order.products.size(); i++) {
                OrderManager.OrderProduct product = order.products.get(i);

                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));
                itemLayout.setGravity(Gravity.CENTER_VERTICAL);

                ImageView ivProduct = new ImageView(this);
                int imgResId = getResources().getIdentifier(product.imageName, "drawable", getPackageName());
                if (imgResId == 0) imgResId = R.drawable.ic_paw_black;
                ivProduct.setImageResource(imgResId);
                ivProduct.setScaleType(ImageView.ScaleType.CENTER_CROP);
                LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dpToPx(60), dpToPx(60));
                GradientDrawable imgBg = new GradientDrawable();
                imgBg.setColor(Color.parseColor("#F5F5F5"));
                imgBg.setCornerRadius(dpToPx(4));
                ivProduct.setBackground(imgBg);
                itemLayout.addView(ivProduct, imgParams);

                LinearLayout infoLayout = new LinearLayout(this);
                infoLayout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                infoParams.leftMargin = dpToPx(10);
                itemLayout.addView(infoLayout, infoParams);

                TextView tvName = new TextView(this);
                tvName.setText(product.name);
                tvName.setTextSize(12);
                tvName.setTextColor(Color.parseColor("#333333"));
                tvName.setMaxLines(2);
                tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);
                infoLayout.addView(tvName);

                if (product.styleName != null && !product.styleName.isEmpty()) {
                    TextView tvStyle = new TextView(this);
                    tvStyle.setText("规格:" + product.styleName);
                    tvStyle.setTextSize(10);
                    tvStyle.setTextColor(Color.parseColor("#999999"));
                    tvStyle.setPadding(0, dpToPx(2), 0, 0);
                    infoLayout.addView(tvStyle);
                }

                TextView tvPrice = new TextView(this);
                tvPrice.setText(String.format(Locale.CHINA, "¥%s x%d", product.price, product.quantity));
                tvPrice.setTextSize(13);
                tvPrice.setTextColor(Color.parseColor("#333333"));
                tvPrice.setTypeface(Typeface.DEFAULT_BOLD);
                tvPrice.setPadding(0, dpToPx(4), 0, 0);
                infoLayout.addView(tvPrice);

                serviceContainer.addView(itemLayout);

                if (i < order.products.size() - 1) {
                    TextView plusSign = new TextView(this);
                    plusSign.setText("+");
                    plusSign.setTextSize(18);
                    plusSign.setTextColor(Color.parseColor("#333333"));
                    plusSign.setPadding(dpToPx(4), 0, dpToPx(4), 0);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = Gravity.CENTER_VERTICAL;
                    plusSign.setLayoutParams(params);
                    serviceContainer.addView(plusSign);
                }
            }
        } else {
            // 服务订单：原有显示方式
            for (int i = 0; i < order.services.size(); i++) {
                ServiceItem svc = order.services.get(i);

                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setPadding(dpToPx(10), dpToPx(8), dpToPx(10), dpToPx(8));

                TextView tvName = new TextView(this);
                tvName.setText(svc.name);
                tvName.setTextSize(12);
                tvName.setTextColor(Color.parseColor("#333333"));
                itemLayout.addView(tvName);

                if (order.serviceDate != null && !order.serviceDate.isEmpty()) {
                    TextView tvTime = new TextView(this);
                    tvTime.setText("预约：" + order.serviceDate + " " + order.serviceTime);
                    tvTime.setTextSize(10);
                    tvTime.setTextColor(Color.parseColor("#FF9800"));
                    tvTime.setPadding(0, dpToPx(2), 0, 0);
                    itemLayout.addView(tvTime);
                }

                if (order.serviceAddress != null && !order.serviceAddress.isEmpty()) {
                    TextView tvAddr = new TextView(this);
                    tvAddr.setText("地址：" + order.serviceAddress);
                    tvAddr.setTextSize(10);
                    tvAddr.setTextColor(Color.parseColor("#999999"));
                    tvAddr.setPadding(0, dpToPx(2), 0, 0);
                    itemLayout.addView(tvAddr);
                }

                serviceContainer.addView(itemLayout);

                // 添加 "+" 号（非最后一项）
                if (i < order.services.size() - 1) {
                    TextView plusSign = new TextView(this);
                    plusSign.setText("+");
                    plusSign.setTextSize(18);
                    plusSign.setTextColor(Color.parseColor("#333333"));
                    plusSign.setPadding(dpToPx(4), 0, dpToPx(4), 0);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.gravity = android.view.Gravity.CENTER_VERTICAL;
                    plusSign.setLayoutParams(params);
                    serviceContainer.addView(plusSign);
                }
            }
        }

        // 总价
        TextView tvTotal = cardView.findViewById(R.id.tv_total_price);
        tvTotal.setText(String.format(Locale.CHINA, "¥%.2f", order.totalPrice));

        // 件数
        TextView tvCount = cardView.findViewById(R.id.tv_item_count);
        if (!order.isProductOrder && order.services.size() > 0) {
            tvCount.setText("共" + order.services.size() + "项服务");
        } else {
            String[] countText = {"", "共一件", "共两件", "共三件", "共四件", "共五件"};
            tvCount.setText(order.itemCount > 0 && order.itemCount <= countText.length ? countText[order.itemCount]
                    : "共" + order.itemCount + "件");
        }

        // 下单时间
        TextView tvTime = cardView.findViewById(R.id.tv_order_time);
        tvTime.setText("下单时间：" + order.orderTime);

        // 操作按钮
        LinearLayout actionContainer = cardView.findViewById(R.id.action_buttons_container);
        for (Action action : order.actions) {
            TextView btn = new TextView(this);
            btn.setText(action.name);
            btn.setTextSize(12);
            btn.setGravity(android.view.Gravity.CENTER);
            btn.setPadding(dpToPx(14), dpToPx(7), dpToPx(14), dpToPx(7));

            GradientDrawable btnBg = new GradientDrawable();
            btnBg.setCornerRadius(dpToPx(15));

            if (action.isPrimary) {
                btnBg.setColor(Color.parseColor("#FFD93D"));
                btn.setTextColor(Color.parseColor("#333333"));
            } else {
                btnBg.setColor(Color.parseColor("#FFFFFF"));
                btnBg.setStroke((int) (1 * getResources().getDisplayMetrics().density), Color.parseColor("#FFD93D"));
                btn.setTextColor(Color.parseColor("#FF9800"));
            }
            btn.setBackground(btnBg);

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            btnParams.setMarginStart(dpToPx(8));
            btn.setLayoutParams(btnParams);

            final String actionName = action.name;
            final OrderItem orderRef = order;
            btn.setOnClickListener(v -> {
                if ("取消预约".equals(actionName)) {
                    if (orderRef.orderId != null && !orderRef.orderId.isEmpty()) {
                        boolean deleted = OrderManager.deleteOrder(OrderActivity.this, orderRef.orderId);
                        if (deleted) {
                            BookingMessageManager.getInstance().removeMessage(OrderActivity.this, orderRef.orderId);
                            Toast.makeText(OrderActivity.this, "取消预约成功", Toast.LENGTH_SHORT).show();
                            loadOrders();
                        } else {
                            Toast.makeText(OrderActivity.this, "取消失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if ("修改预约".equals(actionName)) {
                    Toast.makeText(OrderActivity.this, "修改预约功能开发中", Toast.LENGTH_SHORT).show();
                } else if ("确认收货".equals(actionName)) {
                    Toast.makeText(OrderActivity.this, "确认收货功能开发中", Toast.LENGTH_SHORT).show();
                } else if ("查看物流".equals(actionName)) {
                    Toast.makeText(OrderActivity.this, "查看物流功能开发中", Toast.LENGTH_SHORT).show();
                }
            });

            actionContainer.addView(btn);
        }

        // 卡片间距
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, dpToPx(10));
        cardView.setLayoutParams(cardParams);

        orderListContainer.addView(cardView);
    }

    /**
     * 显示空状态
     */
    private void showEmptyState() {
        LinearLayout emptyLayout = new LinearLayout(this);
        emptyLayout.setOrientation(LinearLayout.VERTICAL);
        emptyLayout.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        emptyLayout.setLayoutParams(lp);

        ImageView ivEmpty = new ImageView(this);
        ivEmpty.setImageResource(R.drawable.ic_orders);
        LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(dpToPx(80), dpToPx(80));
        ivParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        ivEmpty.setLayoutParams(ivParams);
        ivEmpty.setAlpha(0.3f);
        emptyLayout.addView(ivEmpty);

        TextView tvEmpty = new TextView(this);
        tvEmpty.setText("暂无订单");
        tvEmpty.setTextSize(14);
        tvEmpty.setTextColor(Color.parseColor("#999999"));
        tvEmpty.setGravity(android.view.Gravity.CENTER);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        tvParams.topMargin = dpToPx(16);
        tvEmpty.setLayoutParams(tvParams);
        emptyLayout.addView(tvEmpty);

        orderListContainer.addView(emptyLayout);
    }

    /**
     * dp 转 px
     */
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // ==================== 数据模型 ====================

    /** 订单项 */
    private static class OrderItem {
        String orderId;
        String shopName;
        int shopAvatar;
        String status;
        String statusType; // completed/pending/unpaid/review/refund
        String orderTime;
        float totalPrice;
        int itemCount;
        boolean isProductOrder;
        String serviceDate;
        String serviceTime;
        String serviceAddress;
        List<ServiceItem> services = new ArrayList<>();
        List<OrderManager.OrderProduct> products = new ArrayList<>();
        List<Action> actions = new ArrayList<>();
    }

    /** 服务项 */
    private static class ServiceItem {
        String name;
        float price;
    }

    /** 操作按钮 */
    private static class Action {
        String name;
        boolean isPrimary; // true=橙色填充, false=白色描边

        Action(String name, boolean isPrimary) {
            this.name = name;
            this.isPrimary = isPrimary;
        }
    }
}
