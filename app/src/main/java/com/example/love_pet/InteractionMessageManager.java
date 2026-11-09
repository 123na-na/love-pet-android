package com.example.love_pet;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InteractionMessageManager {

    private static InteractionMessageManager instance;
    private static final String PREF_NAME = "interaction_messages";
    private static final String KEY_MESSAGES = "messages";

    public static class InteractionMsg {
        public String userName;
        public String action;
        public String content;
        public String time;
        public int avatarRes;
        public String type; // like, follow, comment

        public InteractionMsg(String userName, String action, String content, String time, int avatarRes, String type) {
            this.userName = userName;
            this.action = action;
            this.content = content;
            this.time = time;
            this.avatarRes = avatarRes;
            this.type = type;
        }
    }

    private InteractionMessageManager() {
    }

    public static InteractionMessageManager getInstance() {
        if (instance == null) {
            instance = new InteractionMessageManager();
        }
        return instance;
    }

    private static String getPrefKey(Context context) {
        return PREF_NAME + "_" + UserManager.getInstance().getCurrentUserId();
    }

    private static String getPrefKeyByUserId(String userId) {
        return PREF_NAME + "_" + userId;
    }

    public List<InteractionMsg> getMessages(Context context, String type) {
        List<InteractionMsg> list = new ArrayList<>();
        if (context == null)
            return list;

        SharedPreferences prefs = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_MESSAGES, "");
        if (jsonStr.isEmpty())
            return list;

        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String msgType = obj.optString("type", "");
                if (type != null && !type.isEmpty() && !type.equals(msgType)) {
                    continue;
                }
                InteractionMsg msg = new InteractionMsg(
                        obj.optString("userName", ""),
                        obj.optString("action", ""),
                        obj.optString("content", ""),
                        obj.optString("time", ""),
                        obj.optInt("avatarRes", 0),
                        msgType);
                list.add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addMessage(Context context, InteractionMsg message) {
        if (context == null || message == null)
            return;

        List<InteractionMsg> allMessages = getMessages(context, null);
        allMessages.add(0, message);
        saveMessages(context, allMessages);
    }

    public void addMessageToUser(Context context, String targetUserId, InteractionMsg message) {
        if (context == null || targetUserId == null || message == null)
            return;

        List<InteractionMsg> allMessages = getMessagesByUserId(context, targetUserId);
        allMessages.add(0, message);
        saveMessagesByUserId(context, targetUserId, allMessages);
    }

    private List<InteractionMsg> getMessagesByUserId(Context context, String userId) {
        List<InteractionMsg> list = new ArrayList<>();
        if (context == null || userId == null)
            return list;

        SharedPreferences prefs = context.getSharedPreferences(getPrefKeyByUserId(userId), Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_MESSAGES, "");
        if (jsonStr.isEmpty())
            return list;

        try {
            JSONArray arr = new JSONArray(jsonStr);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                InteractionMsg msg = new InteractionMsg(
                        obj.optString("userName", ""),
                        obj.optString("action", ""),
                        obj.optString("content", ""),
                        obj.optString("time", ""),
                        obj.optInt("avatarRes", 0),
                        obj.optString("type", ""));
                list.add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void saveMessagesByUserId(Context context, String userId, List<InteractionMsg> messages) {
        if (context == null || userId == null)
            return;
        try {
            JSONArray arr = new JSONArray();
            for (InteractionMsg msg : messages) {
                JSONObject obj = new JSONObject();
                obj.put("userName", msg.userName);
                obj.put("action", msg.action);
                obj.put("content", msg.content);
                obj.put("time", msg.time);
                obj.put("avatarRes", msg.avatarRes);
                obj.put("type", msg.type);
                arr.put(obj);
            }
            SharedPreferences prefs = context.getSharedPreferences(getPrefKeyByUserId(userId), Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_MESSAGES, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveMessages(Context context, List<InteractionMsg> messages) {
        if (context == null)
            return;
        try {
            JSONArray arr = new JSONArray();
            for (InteractionMsg msg : messages) {
                JSONObject obj = new JSONObject();
                obj.put("userName", msg.userName);
                obj.put("action", msg.action);
                obj.put("content", msg.content);
                obj.put("time", msg.time);
                obj.put("avatarRes", msg.avatarRes);
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
