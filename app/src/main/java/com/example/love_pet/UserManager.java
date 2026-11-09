package com.example.love_pet;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private static UserManager instance;
    private static final String PREF_NAME = "user_manager";
    private static final String KEY_USERS = "users";
    private static final String KEY_CURRENT_PHONE = "current_phone";
    private static final String KEY_HAS_INIT_DEFAULT = "has_init_default";
    private static final String DEFAULT_PHONE = "13800000000";
    private static final String DEFAULT_PASSWORD = "123456";
    private static final String DEFAULT_NICKNAME = "萌宠1号";
    private static final String DEFAULT_USER_ID = "user_default";

    public static class User {
        public String phone;
        public String password;
        public String nickname;
        public String avatarPath;
        public String registerTime;

        public User(String phone, String password, String nickname, String avatarPath, String registerTime) {
            this.phone = phone;
            this.password = password;
            this.nickname = nickname;
            this.avatarPath = avatarPath;
            this.registerTime = registerTime;
        }
    }

    private List<User> users = new ArrayList<>();
    private User currentUser;
    private boolean isLoaded = false;

    private UserManager() {
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    private void ensureLoaded(Context context) {
        if (!isLoaded && context != null) {
            loadUsers(context);
        }
    }

    public void loadUsers(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String jsonStr = prefs.getString(KEY_USERS, "");
        users.clear();
        if (!jsonStr.isEmpty()) {
            try {
                JSONArray arr = new JSONArray(jsonStr);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    User user = new User(
                            obj.optString("phone", ""),
                            obj.optString("password", ""),
                            obj.optString("nickname", ""),
                            obj.optString("avatarPath", ""),
                            obj.optString("registerTime", "")
                    );
                    users.add(user);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (findUserByPhoneInternal(DEFAULT_PHONE) == null) {
            User defaultUser = new User(
                    DEFAULT_PHONE,
                    DEFAULT_PASSWORD,
                    DEFAULT_NICKNAME,
                    "",
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.CHINA)
                            .format(new java.util.Date())
            );
            users.add(defaultUser);
            saveUsers(context);
            prefs.edit().putBoolean(KEY_HAS_INIT_DEFAULT, true).apply();
            
            User oldCurrent = currentUser;
            currentUser = defaultUser;
            initDefaultUserData(context);
            currentUser = oldCurrent;
        }

        String currentPhone = prefs.getString(KEY_CURRENT_PHONE, "");
        if (!currentPhone.isEmpty()) {
            currentUser = findUserByPhoneInternal(currentPhone);
        }
        isLoaded = true;
    }

    private void saveUsers(Context context) {
        if (context == null) return;
        try {
            JSONArray arr = new JSONArray();
            for (User user : users) {
                JSONObject obj = new JSONObject();
                obj.put("phone", user.phone);
                obj.put("password", user.password);
                obj.put("nickname", user.nickname);
                obj.put("avatarPath", user.avatarPath);
                obj.put("registerTime", user.registerTime);
                arr.put(obj);
            }
            SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(KEY_USERS, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveCurrentUser(Context context) {
        if (context == null) return;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_CURRENT_PHONE, currentUser != null ? currentUser.phone : "").apply();
    }

    private User findUserByPhoneInternal(String phone) {
        for (User user : users) {
            if (user.phone.equals(phone)) {
                return user;
            }
        }
        return null;
    }

    public User findUserByPhone(String phone) {
        return findUserByPhoneInternal(phone);
    }

    public boolean register(Context context, String phone, String password, String nickname, String avatarPath) {
        if (findUserByPhone(phone) != null) {
            return false;
        }
        String registerTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.CHINA)
                .format(new java.util.Date());
        User user = new User(phone, password, nickname, avatarPath, registerTime);
        users.add(user);
        saveUsers(context);
        return true;
    }

    public boolean login(Context context, String phone, String password) {
        User user = findUserByPhone(phone);
        if (user != null && user.password.equals(password)) {
            currentUser = user;
            saveCurrentUser(context);
            if (DEFAULT_PHONE.equals(phone)) {
                initDefaultUserData(context);
            }
            return true;
        }
        return false;
    }

    public void setCurrentUser(Context context, User user) {
        this.currentUser = user;
        saveCurrentUser(context);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentUserId() {
        if (currentUser != null) {
            if (DEFAULT_PHONE.equals(currentUser.phone)) {
                return DEFAULT_USER_ID;
            }
            return "user_" + currentUser.phone;
        }
        return "user_default";
    }

    public String getUserIdByNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return null;
        }
        for (User user : users) {
            if (nickname.equals(user.nickname)) {
                if (DEFAULT_PHONE.equals(user.phone)) {
                    return DEFAULT_USER_ID;
                }
                return "user_" + user.phone;
            }
        }
        return null;
    }

    public String getCurrentNickname() {
        if (currentUser != null && currentUser.nickname != null && !currentUser.nickname.isEmpty()) {
            return currentUser.nickname;
        }
        return "宠友";
    }

    public String getCurrentAvatarPath() {
        if (currentUser != null) {
            return currentUser.avatarPath;
        }
        return null;
    }

    public void updateCurrentUserAvatar(Context context, String avatarPath) {
        if (currentUser != null) {
            currentUser.avatarPath = avatarPath;
            saveUsers(context);
        }
    }

    public void updateCurrentUserNickname(Context context, String nickname) {
        if (currentUser != null) {
            currentUser.nickname = nickname;
            saveUsers(context);
        }
    }

    public void logout(Context context) {
        currentUser = null;
        saveCurrentUser(context);
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    private void initDefaultUserData(Context context) {
        if (context == null) return;
        
        List<PetManager.Pet> pets = PetManager.getInstance().getPets(context);
        if (pets.isEmpty()) {
            PetManager.Pet pet = new PetManager.Pet();
            pet.name = "花花";
            pet.type = "cat";
            pet.breed = "橘猫";
            pet.gender = "female";
            pet.age = "9个月";
            pet.weight = "6.4kg";
            pet.avatarPath = copyResourceToFile(context, R.drawable.img_cat_hua, "default_pet_hua");
            pet.tags = new String[]{"安静", "可爱", "粘人"};
            PetManager.getInstance().addPet(context, pet);
        }

        List<OrderManager.Order> orders = OrderManager.getOrders(context);
        if (orders.isEmpty()) {
            initDefaultOrders(context);
        }

        List<ChatMessageManager.ShopMessageInfo> shops = ChatMessageManager.getInstance().getAllShopInfos();
        if (shops.isEmpty()) {
            initDefaultChatMessages(context);
        }
    }

    private void initDefaultOrders(Context context) {
        OrderManager.Order order1 = new OrderManager.Order();
        order1.orderId = "ORD" + (System.currentTimeMillis() - 86400000 * 10);
        order1.shopName = "宜家宠物洗护";
        order1.status = "已完成";
        order1.statusType = "completed";
        order1.orderTime = "2025-11-11 11:11";
        order1.totalPrice = 84.0;
        order1.itemCount = 2;
        order1.isServiceOrder = true;
        order1.serviceType = "洗护套餐";
        order1.serviceDate = "2025-11-10";
        order1.serviceTime = "14:00";
        order1.serviceAddress = "广州市天河区";
        OrderManager.OrderProduct p1 = new OrderManager.OrderProduct();
        p1.productId = "service_wash";
        p1.name = "毛发护理";
        p1.styleName = "2025-11-10 14:00";
        p1.price = "¥46.00";
        p1.imageName = "";
        p1.quantity = 1;
        order1.products.add(p1);
        OrderManager.OrderProduct p2 = new OrderManager.OrderProduct();
        p2.productId = "service_bath";
        p2.name = "洗澡";
        p2.styleName = "2025-11-10 14:00";
        p2.price = "¥38.00";
        p2.imageName = "";
        p2.quantity = 1;
        order1.products.add(p2);
        saveOrderDirect(context, order1);

        OrderManager.Order order2 = new OrderManager.Order();
        order2.orderId = "ORD" + (System.currentTimeMillis() - 86400000 * 5);
        order2.shopName = "爱宠商城";
        order2.status = "待收货";
        order2.statusType = "pending";
        order2.orderTime = "2025-11-28 09:20";
        order2.totalPrice = 94.9;
        order2.itemCount = 2;
        order2.isServiceOrder = false;
        OrderManager.OrderProduct p3 = new OrderManager.OrderProduct();
        p3.productId = "1001";
        p3.name = "宠物零食大礼包";
        p3.styleName = "混合口味";
        p3.price = "¥59.90";
        p3.imageName = "";
        p3.quantity = 1;
        order2.products.add(p3);
        OrderManager.OrderProduct p4 = new OrderManager.OrderProduct();
        p4.productId = "1002";
        p4.name = "营养膏";
        p4.styleName = "120g";
        p4.price = "¥35.00";
        p4.imageName = "";
        p4.quantity = 1;
        order2.products.add(p4);
        saveOrderDirect(context, order2);
    }

    private void saveOrderDirect(Context context, OrderManager.Order order) {
        List<OrderManager.Order> orders = OrderManager.getOrders(context);
        orders.add(0, order);
        OrderManager.saveOrders(context, orders);
    }

    private String copyResourceToFile(Context context, int resId, String fileName) {
        try {
            java.io.File dir = new java.io.File(context.getFilesDir(), "pet_avatars");
            if (!dir.exists()) dir.mkdirs();
            java.io.File destFile = new java.io.File(dir, fileName + ".jpg");
            if (destFile.exists()) return destFile.getAbsolutePath();
            
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeResource(
                    context.getResources(), resId);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(destFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void initDefaultChatMessages(Context context) {
        ChatMessageManager chatMgr = ChatMessageManager.getInstance();
        
        chatMgr.setShopInfo(context, "官方旗舰店", 0,
                "【交易物流】您购买的全自动猫砂盆已发货，快递公司：顺丰速运，运单号：SF1234567890，请注意查收~",
                "昨天 14:30", 0);
        chatMgr.addShopMessage(context, "官方旗舰店",
                new ChatMessage("亲，欢迎光临官方旗舰店~全场满199减30哦！", "06-25 09:30", false));
        chatMgr.addShopMessage(context, "官方旗舰店",
                new ChatMessage("你好，这款全自动猫砂盆好用吗？", "06-25 09:35", true));
        chatMgr.addShopMessage(context, "官方旗舰店",
                new ChatMessage("非常好用的亲亲~全自动打包换袋，省心又省力，铲屎官必备神器哦😺", "06-25 09:36", false));
        chatMgr.addShopMessage(context, "官方旗舰店",
                new ChatMessage("那猫咪用着安全吗？会不会夹到？", "06-25 09:40", true));
        chatMgr.addShopMessage(context, "官方旗舰店",
                new ChatMessage("放心亲亲~有多重力感应+防夹设计，猫咪靠近会自动暂停，非常安全的~", "06-25 09:42", false));
        chatMgr.addShopMessage(context, "官方旗舰店",
                new ChatMessage("好的，我买一个", "06-25 09:43", true));
        chatMgr.addShopMessage(context, "官方旗舰店",
                new ChatMessage("感谢亲亲的支持~我们会尽快为您发货哒！", "06-25 09:45", false));
        chatMgr.addShopMessage(context, "官方旗舰店",
                new ChatMessage("【交易物流】您购买的全自动猫砂盆已发货，快递公司：顺丰速运，运单号：SF1234567890，请注意查收~", "昨天 14:30", false));

        chatMgr.setShopInfo(context, "木屋旺旗舰店", 0,
                "好的，已为您安排明天发货", "前天 10:15", 0);
        chatMgr.addShopMessage(context, "木屋旺旗舰店",
                new ChatMessage("您好，欢迎光临木屋旺旗舰店~", "前天 09:20", false));
        chatMgr.addShopMessage(context, "木屋旺旗舰店",
                new ChatMessage("你好，这款狗屋中型犬能用吗？", "前天 09:25", true));
        chatMgr.addShopMessage(context, "木屋旺旗舰店",
                new ChatMessage("可以的亲亲~中号适合30斤以内的狗狗哦，您家是什么品种呢？", "前天 09:26", false));
        chatMgr.addShopMessage(context, "木屋旺旗舰店",
                new ChatMessage("柯基，大概25斤，中号可以吗？", "前天 09:30", true));
        chatMgr.addShopMessage(context, "木屋旺旗舰店",
                new ChatMessage("完全没问题的亲亲~柯基住中号刚刚好，还能转身呢~", "前天 09:35", false));
        chatMgr.addShopMessage(context, "木屋旺旗舰店",
                new ChatMessage("好的，那就中号吧，能帮忙快点发货吗？", "前天 09:40", true));
        chatMgr.addShopMessage(context, "木屋旺旗舰店",
                new ChatMessage("好的，已为您安排明天发货", "前天 10:15", false));

        chatMgr.setShopInfo(context, "帕特旗舰店", 0,
                "您购买的猫粮已送达，请确认收货", "06-25 16:20", 0);
        chatMgr.addShopMessage(context, "帕特旗舰店",
                new ChatMessage("亲，欢迎光临帕特旗舰店~", "06-24 10:00", false));
        chatMgr.addShopMessage(context, "帕特旗舰店",
                new ChatMessage("这款生命猫粮幼猫能吃吗？", "06-24 10:10", true));
        chatMgr.addShopMessage(context, "帕特旗舰店",
                new ChatMessage("可以的亲亲~这款是全阶段猫粮，成猫幼猫都能吃，幼猫版还有羊奶粉配方哦", "06-24 10:12", false));
        chatMgr.addShopMessage(context, "帕特旗舰店",
                new ChatMessage("好的，我买一袋幼猫款", "06-24 10:20", true));
        chatMgr.addShopMessage(context, "帕特旗舰店",
                new ChatMessage("感谢亲亲的支持~我们会尽快发货哒😺", "06-24 10:21", false));
        chatMgr.addShopMessage(context, "帕特旗舰店",
                new ChatMessage("您购买的猫粮已送达，请确认收货", "06-25 16:20", false));
    }
}
