package com.example.love_pet;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderManager {

    private static final String PREF_NAME = "order_prefs";
    private static final String KEY_ORDERS = "orders";

    private static String getPrefKey(Context context) {
        return PREF_NAME + "_" + UserManager.getInstance().getCurrentUserId();
    }

    public static class Order {
        public String orderId;
        public String shopName;
        public String status;
        public String statusType;
        public String orderTime;
        public double totalPrice;
        public int itemCount;
        public List<OrderProduct> products = new ArrayList<>();
        public boolean isServiceOrder;
        public String serviceType;
        public String serviceDate;
        public String serviceTime;
        public String serviceAddress;

        public Order() {
        }
    }

    public static class OrderProduct {
        public String productId;
        public String name;
        public String styleName;
        public String price;
        public String imageName;
        public int quantity;

        public OrderProduct() {
        }
    }

    public static void addOrder(Context context, List<CartItem> items, double totalAmount) {
        List<Order> orders = getOrders(context);

        Order order = new Order();
        order.orderId = "ORD" + System.currentTimeMillis();
        order.orderTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date());
        order.totalPrice = totalAmount;
        order.status = "待收货";
        order.statusType = "pending";
        order.itemCount = 0;
        order.isServiceOrder = false;

        if (!items.isEmpty()) {
            order.shopName = items.get(0).getShopName() != null ? items.get(0).getShopName() : "官方旗舰店";
        } else {
            order.shopName = "官方旗舰店";
        }

        for (CartItem item : items) {
            OrderProduct product = new OrderProduct();
            product.productId = String.valueOf(item.getProductId());
            product.name = item.getTitle();
            product.styleName = item.getStyleName();
            product.price = item.getPrice();
            product.imageName = item.getImageName();
            product.quantity = item.getQuantity();
            order.products.add(product);
            order.itemCount += item.getQuantity();
        }

        orders.add(0, order);
        saveOrders(context, orders);
    }

    public static String addServiceOrder(Context context, String shopName, String shopAddress,
                                       String serviceType, String serviceDate, String serviceTime, double price) {
        List<Order> orders = getOrders(context);

        Order order = new Order();
        order.orderId = "SVC" + System.currentTimeMillis();
        order.orderTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(new Date());
        order.totalPrice = price;
        order.status = "待使用";
        order.statusType = "pending";
        order.itemCount = 1;
        order.isServiceOrder = true;
        order.shopName = shopName;
        order.serviceType = serviceType;
        order.serviceDate = serviceDate;
        order.serviceTime = serviceTime;
        order.serviceAddress = shopAddress;

        OrderProduct product = new OrderProduct();
        product.productId = "service_" + serviceType;
        product.name = serviceType + "服务";
        product.styleName = serviceDate + " " + serviceTime;
        product.price = "¥" + String.format("%.2f", price);
        product.imageName = "";
        product.quantity = 1;
        order.products.add(product);

        orders.add(0, order);
        saveOrders(context, orders);
        return order.orderId;
    }

    public static boolean deleteOrder(Context context, String orderId) {
        List<Order> orders = getOrders(context);
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).orderId != null && orders.get(i).orderId.equals(orderId)) {
                orders.remove(i);
                saveOrders(context, orders);
                return true;
            }
        }
        return false;
    }

    public static List<Order> getOrders(Context context) {
        List<Order> orders = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_ORDERS, "");
        if (jsonStr.isEmpty()) return orders;
        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                Order order = new Order();
                order.orderId = obj.optString("orderId", "");
                order.shopName = obj.optString("shopName", "");
                order.status = obj.optString("status", "");
                order.statusType = obj.optString("statusType", "");
                order.orderTime = obj.optString("orderTime", "");
                order.totalPrice = obj.optDouble("totalPrice", 0);
                order.itemCount = obj.optInt("itemCount", 0);
                order.isServiceOrder = obj.optBoolean("isServiceOrder", false);
                order.serviceType = obj.optString("serviceType", "");
                order.serviceDate = obj.optString("serviceDate", "");
                order.serviceTime = obj.optString("serviceTime", "");
                order.serviceAddress = obj.optString("serviceAddress", "");

                JSONArray productsArr = obj.optJSONArray("products");
                if (productsArr != null) {
                    for (int j = 0; j < productsArr.length(); j++) {
                        JSONObject pObj = productsArr.getJSONObject(j);
                        OrderProduct product = new OrderProduct();
                        product.productId = pObj.optString("productId", "");
                        product.name = pObj.optString("name", "");
                        product.styleName = pObj.optString("styleName", "");
                        product.price = pObj.optString("price", "");
                        product.imageName = pObj.optString("imageName", "");
                        product.quantity = pObj.optInt("quantity", 0);
                        order.products.add(product);
                    }
                }
                orders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders;
    }

    static void saveOrders(Context context, List<Order> orders) {
        SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray arr = new JSONArray();
        try {
            for (Order order : orders) {
                JSONObject obj = new JSONObject();
                obj.put("orderId", order.orderId);
                obj.put("shopName", order.shopName);
                obj.put("status", order.status);
                obj.put("statusType", order.statusType);
                obj.put("orderTime", order.orderTime);
                obj.put("totalPrice", order.totalPrice);
                obj.put("itemCount", order.itemCount);
                obj.put("isServiceOrder", order.isServiceOrder);
                obj.put("serviceType", order.serviceType);
                obj.put("serviceDate", order.serviceDate);
                obj.put("serviceTime", order.serviceTime);
                obj.put("serviceAddress", order.serviceAddress);

                JSONArray productsArr = new JSONArray();
                for (OrderProduct product : order.products) {
                    JSONObject pObj = new JSONObject();
                    pObj.put("productId", product.productId);
                    pObj.put("name", product.name);
                    pObj.put("styleName", product.styleName);
                    pObj.put("price", product.price);
                    pObj.put("imageName", product.imageName);
                    pObj.put("quantity", product.quantity);
                    productsArr.put(pObj);
                }
                obj.put("products", productsArr);
                arr.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.putString(KEY_ORDERS, arr.toString());
        editor.commit();
    }
}
