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

public class SystemMessageManager {

    private static SystemMessageManager instance;
    private static final String PREF_NAME = "system_messages";
    private static final String KEY_MESSAGES = "messages";

    public static class SystemMsg {
        public String title;
        public String content;
        public String time;
        public int type; // 0=系统通知, 1=举报反馈, 2=活动通知

        public SystemMsg(String title, String content, String time, int type) {
            this.title = title;
            this.content = content;
            this.time = time;
            this.type = type;
        }
    }

    private SystemMessageManager() {
    }

    public static SystemMessageManager getInstance() {
        if (instance == null) {
            instance = new SystemMessageManager();
        }
        return instance;
    }

    private static String getPrefKey(Context context) {
        return PREF_NAME + "_" + UserManager.getInstance().getCurrentUserId();
    }

    public List<SystemMsg> getMessages(Context context) {
        List<SystemMsg> list = new ArrayList<>();
        if (context == null) return list;

        SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_MESSAGES, "");
        if (jsonStr.isEmpty()) return list;

        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                SystemMsg msg = new SystemMsg(
                        obj.optString("title", ""),
                        obj.optString("content", ""),
                        obj.optString("time", ""),
                        obj.optInt("type", 0)
                );
                list.add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addMessage(Context context, SystemMsg message) {
        if (context == null || message == null) return;

        List<SystemMsg> allMessages = getMessages(context);
        allMessages.add(0, message);
        saveMessages(context, allMessages);
    }

    public void addSystemMessage(Context context, String title, String content, long timestamp) {
        String time = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new Date(timestamp));
        SystemMsg msg = new SystemMsg(title, content, time, 1);
        addMessage(context, msg);
    }

    private void saveMessages(Context context, List<SystemMsg> messages) {
        try {
            JSONArray arr = new JSONArray();
            for (SystemMsg msg : messages) {
                JSONObject obj = new JSONObject();
                obj.put("title", msg.title);
                obj.put("content", msg.content);
                obj.put("time", msg.time);
                obj.put("type", msg.type);
                arr.put(obj);
            }
            SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_MESSAGES, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
