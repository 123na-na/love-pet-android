package com.example.love_pet;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookingMessageManager {

    private static BookingMessageManager instance;
    private static final String PREF_NAME = "booking_messages";
    private static final String KEY_MESSAGES = "messages";

    private static String getPrefKey(Context context) {
        return PREF_NAME + "_" + UserManager.getInstance().getCurrentUserId();
    }

    public static class BookingMessage {
        public String shopName;
        public String serviceName;
        public String bookingTime;
        public String status;
        public int shopLogo;
        public String orderId;

        public BookingMessage(String shopName, String serviceName, String bookingTime, String status, int shopLogo, String orderId) {
            this.shopName = shopName;
            this.serviceName = serviceName;
            this.bookingTime = bookingTime;
            this.status = status;
            this.shopLogo = shopLogo;
            this.orderId = orderId;
        }
    }

    private BookingMessageManager() {
    }

    public static BookingMessageManager getInstance() {
        if (instance == null) {
            instance = new BookingMessageManager();
        }
        return instance;
    }

    public List<BookingMessage> getMessages(Context context) {
        List<BookingMessage> list = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_MESSAGES, "");
        if (jsonStr.isEmpty()) return list;
        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String shopName = obj.optString("shopName", "");
                String serviceName = obj.optString("serviceName", "");
                String bookingTime = obj.optString("bookingTime", "");
                String status = obj.optString("status", "");
                int shopLogo = obj.optInt("shopLogo", 0);
                String orderId = obj.optString("orderId", "");
                list.add(new BookingMessage(shopName, serviceName, bookingTime, status, shopLogo, orderId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addMessage(Context context, BookingMessage message) {
        List<BookingMessage> messages = getMessages(context);
        messages.add(0, message);
        saveMessages(context, messages);
    }

    public void removeMessage(Context context, String orderId) {
        List<BookingMessage> messages = getMessages(context);
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).orderId != null && messages.get(i).orderId.equals(orderId)) {
                messages.remove(i);
                break;
            }
        }
        saveMessages(context, messages);
    }

    private void saveMessages(Context context, List<BookingMessage> messages) {
        try {
            JSONArray arr = new JSONArray();
            for (BookingMessage msg : messages) {
                JSONObject obj = new JSONObject();
                obj.put("shopName", msg.shopName);
                obj.put("serviceName", msg.serviceName);
                obj.put("bookingTime", msg.bookingTime);
                obj.put("status", msg.status);
                obj.put("shopLogo", msg.shopLogo);
                obj.put("orderId", msg.orderId);
                arr.put(obj);
            }
            SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_MESSAGES, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
