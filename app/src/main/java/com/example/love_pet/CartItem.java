package com.example.love_pet;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CartItem {
    private int productId;
    private String title;
    private String styleName;
    private String price;
    private String originalPrice;
    private String imageName;
    private String shopName;
    private int quantity;
    private boolean selected;

    public CartItem() {
    }

    public CartItem(int productId, String title, String styleName, String price, String imageName, String shopName, int quantity) {
        this.productId = productId;
        this.title = title;
        this.styleName = styleName;
        this.price = price;
        this.imageName = imageName;
        this.shopName = shopName;
        this.quantity = quantity;
        this.selected = true;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStyleName() { return styleName; }
    public void setStyleName(String styleName) { this.styleName = styleName; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(String originalPrice) { this.originalPrice = originalPrice; }
    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    private static final String PREF_NAME = "cart_prefs";
    private static final String KEY_CART = "cart_items";
    private static final String KEY_BUY_NOW = "buy_now_items";

    private static String getPrefKey(Context context) {
        return PREF_NAME + "_" + UserManager.getInstance().getCurrentUserId();
    }

    public static void addToCart(Context context, CartItem item) {
        List<CartItem> items = getCartItems(context);
        boolean found = false;
        for (CartItem existing : items) {
            if (existing.getProductId() == item.getProductId() &&
                existing.getStyleName().equals(item.getStyleName())) {
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                found = true;
                break;
            }
        }
        if (!found) {
            items.add(item);
        }
        saveCartItems(context, items);
    }

    public static void updateQuantity(Context context, int productId, String styleName, int quantity) {
        List<CartItem> items = getCartItems(context);
        for (CartItem item : items) {
            if (item.getProductId() == productId && item.getStyleName().equals(styleName)) {
                item.setQuantity(quantity);
                break;
            }
        }
        saveCartItems(context, items);
    }

    public static void removeFromCart(Context context, int productId, String styleName) {
        List<CartItem> items = getCartItems(context);
        CartItem toRemove = null;
        for (CartItem item : items) {
            if (item.getProductId() == productId && item.getStyleName().equals(styleName)) {
                toRemove = item;
                break;
            }
        }
        if (toRemove != null) {
            items.remove(toRemove);
        }
        saveCartItems(context, items);
    }

    public static List<CartItem> getCartItems(Context context) {
        List<CartItem> items = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_CART, "");
        if (jsonStr.isEmpty()) return items;
        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                CartItem item = new CartItem();
                item.setProductId(obj.getInt("productId"));
                item.setTitle(obj.getString("title"));
                item.setStyleName(obj.optString("styleName", ""));
                item.setPrice(obj.getString("price"));
                item.setOriginalPrice(obj.optString("originalPrice", ""));
                item.setImageName(obj.optString("imageName", ""));
                item.setShopName(obj.optString("shopName", "官方旗舰店"));
                item.setQuantity(obj.getInt("quantity"));
                item.setSelected(obj.optBoolean("selected", true));
                items.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    public static void saveCartItems(Context context, List<CartItem> items) {
        SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray arr = new JSONArray();
        try {
            for (CartItem item : items) {
                JSONObject obj = new JSONObject();
                obj.put("productId", item.getProductId());
                obj.put("title", item.getTitle());
                obj.put("styleName", item.getStyleName());
                obj.put("price", item.getPrice());
                obj.put("originalPrice", item.getOriginalPrice() != null ? item.getOriginalPrice() : "");
                obj.put("imageName", item.getImageName());
                obj.put("shopName", item.getShopName());
                obj.put("quantity", item.getQuantity());
                obj.put("selected", item.isSelected());
                arr.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.putString(KEY_CART, arr.toString());
        editor.commit();
    }

    public static int getCartCount(Context context) {
        List<CartItem> items = getCartItems(context);
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public static void saveBuyNowItems(Context context, List<CartItem> items) {
        SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray arr = new JSONArray();
        try {
            for (CartItem item : items) {
                JSONObject obj = new JSONObject();
                obj.put("productId", item.getProductId());
                obj.put("title", item.getTitle());
                obj.put("styleName", item.getStyleName());
                obj.put("price", item.getPrice());
                obj.put("originalPrice", item.getOriginalPrice() != null ? item.getOriginalPrice() : "");
                obj.put("imageName", item.getImageName());
                obj.put("shopName", item.getShopName());
                obj.put("quantity", item.getQuantity());
                obj.put("selected", item.isSelected());
                arr.put(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.putString(KEY_BUY_NOW, arr.toString());
        editor.commit();
    }

    public static List<CartItem> getBuyNowItems(Context context) {
        List<CartItem> items = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_BUY_NOW, "");
        if (jsonStr.isEmpty()) return items;
        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                CartItem item = new CartItem();
                item.setProductId(obj.getInt("productId"));
                item.setTitle(obj.getString("title"));
                item.setStyleName(obj.optString("styleName", ""));
                item.setPrice(obj.getString("price"));
                item.setOriginalPrice(obj.optString("originalPrice", ""));
                item.setImageName(obj.optString("imageName", ""));
                item.setShopName(obj.optString("shopName", "官方旗舰店"));
                item.setQuantity(obj.getInt("quantity"));
                item.setSelected(obj.optBoolean("selected", true));
                items.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }
}
