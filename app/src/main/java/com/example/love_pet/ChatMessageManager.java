package com.example.love_pet;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatMessageManager {

    private static ChatMessageManager instance;
    private static final String PREF_NAME = "chat_messages";
    private static final String KEY_SHOPS = "shops";

    private String getPrefKey(Context context) {
        return PREF_NAME + "_" + UserManager.getInstance().getCurrentUserId();
    }

    private Map<String, List<ChatMessage>> shopMessagesMap = new HashMap<>();
    private Map<String, ShopMessageInfo> shopInfoMap = new HashMap<>();

    public static class ShopMessageInfo {
        public String shopName;
        public int avatarRes;
        public String lastMessage;
        public String lastTime;
        public int unreadCount;

        public ShopMessageInfo(String shopName, int avatarRes, String lastMessage, String lastTime, int unreadCount) {
            this.shopName = shopName;
            this.avatarRes = avatarRes;
            this.lastMessage = lastMessage;
            this.lastTime = lastTime;
            this.unreadCount = unreadCount;
        }
    }

    private ChatMessageManager() {
    }

    public static ChatMessageManager getInstance() {
        if (instance == null) {
            instance = new ChatMessageManager();
        }
        return instance;
    }

    private SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
    }

    public void loadFromPrefs(Context context) {
        if (context == null) return;
        shopMessagesMap.clear();
        shopInfoMap.clear();
        SharedPreferences prefs = getPrefs(context);
        String shopsJson = prefs.getString(KEY_SHOPS, "");
        if (shopsJson.isEmpty()) return;

        try {
            JSONArray shopsArr = new JSONArray(shopsJson);
            for (int i = 0; i < shopsArr.length(); i++) {
                JSONObject shopObj = shopsArr.getJSONObject(i);
                String shopName = shopObj.optString("shopName", "");
                int avatarRes = shopObj.optInt("avatarRes", 0);
                String lastMessage = shopObj.optString("lastMessage", "");
                String lastTime = shopObj.optString("lastTime", "");
                int unreadCount = shopObj.optInt("unreadCount", 0);

                if (shopName.isEmpty()) continue;

                ShopMessageInfo info = new ShopMessageInfo(shopName, avatarRes, lastMessage, lastTime, unreadCount);
                shopInfoMap.put(shopName, info);

                List<ChatMessage> messages = new ArrayList<>();
                JSONArray msgsArr = shopObj.optJSONArray("messages");
                if (msgsArr != null) {
                    for (int j = 0; j < msgsArr.length(); j++) {
                        JSONObject msgObj = msgsArr.getJSONObject(j);
                        String content = msgObj.optString("content", "");
                        String subTitle = msgObj.optString("subTitle", "");
                        String time = msgObj.optString("time", "");
                        boolean isSelf = msgObj.optBoolean("isSelf", false);
                        int avatar = msgObj.optInt("avatar", 0);
                        ChatMessage msg = new ChatMessage(content, subTitle, time, isSelf, avatar);
                        messages.add(msg);
                    }
                }
                shopMessagesMap.put(shopName, messages);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveToPrefs(Context context) {
        if (context == null) return;
        try {
            JSONArray shopsArr = new JSONArray();
            for (Map.Entry<String, ShopMessageInfo> entry : shopInfoMap.entrySet()) {
                String shopName = entry.getKey();
                ShopMessageInfo info = entry.getValue();
                JSONObject shopObj = new JSONObject();
                shopObj.put("shopName", shopName);
                shopObj.put("avatarRes", info.avatarRes);
                shopObj.put("lastMessage", info.lastMessage);
                shopObj.put("lastTime", info.lastTime);
                shopObj.put("unreadCount", info.unreadCount);

                List<ChatMessage> messages = shopMessagesMap.get(shopName);
                if (messages != null) {
                    JSONArray msgsArr = new JSONArray();
                    for (ChatMessage msg : messages) {
                        JSONObject msgObj = new JSONObject();
                        msgObj.put("content", msg.content);
                        msgObj.put("subTitle", msg.subTitle);
                        msgObj.put("time", msg.time);
                        msgObj.put("isSelf", msg.isSelf);
                        msgObj.put("avatar", msg.avatar);
                        msgsArr.put(msgObj);
                    }
                    shopObj.put("messages", msgsArr);
                }
                shopsArr.put(shopObj);
            }
            SharedPreferences prefs = getPrefs(context);
            prefs.edit().putString(KEY_SHOPS, shopsArr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<ChatMessage> getShopMessages(String shopName) {
        List<ChatMessage> messages = shopMessagesMap.get(shopName);
        if (messages == null) {
            messages = new ArrayList<>();
            shopMessagesMap.put(shopName, messages);
        }
        return messages;
    }

    public void addShopMessage(Context context, String shopName, ChatMessage message) {
        List<ChatMessage> messages = getShopMessages(shopName);
        messages.add(message);

        ShopMessageInfo info = shopInfoMap.get(shopName);
        if (info != null) {
            info.lastMessage = message.content;
            info.lastTime = message.time;
            if (!message.isSelf) {
                info.unreadCount++;
            }
        }

        saveToPrefs(context);
    }

    public void setShopInfo(Context context, String shopName, int avatarRes, String lastMessage, String lastTime, int unreadCount) {
        ShopMessageInfo info = shopInfoMap.get(shopName);
        if (info == null) {
            info = new ShopMessageInfo(shopName, avatarRes, lastMessage, lastTime, unreadCount);
            shopInfoMap.put(shopName, info);
        } else {
            info.avatarRes = avatarRes;
            info.lastMessage = lastMessage;
            info.lastTime = lastTime;
            info.unreadCount = unreadCount;
        }
        saveToPrefs(context);
    }

    public ShopMessageInfo getShopInfo(String shopName) {
        return shopInfoMap.get(shopName);
    }

    public List<ShopMessageInfo> getAllShopInfos() {
        return new ArrayList<>(shopInfoMap.values());
    }

    public void clearUnread(Context context, String shopName) {
        ShopMessageInfo info = shopInfoMap.get(shopName);
        if (info != null) {
            info.unreadCount = 0;
            saveToPrefs(context);
        }
    }
}
