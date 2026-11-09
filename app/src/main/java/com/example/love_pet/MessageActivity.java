package com.example.love_pet;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private LinearLayout llMessageList;
    private LinearLayout subTabLayout;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyText;
    private int currentTab = 0; // 0=商城客服, 1=预约提醒, 2=宠图互动, 3=系统通知
    private int currentSubTab = 0; // 0=赞和收藏, 1=新增关注, 2=评论

    public static class ShopMessage {
        public String shopName;
        public String lastMessage;
        public String time;
        public int shopLogo;
        public boolean hasUnread;
        public int unreadCount;

        public ShopMessage(String shopName, String lastMessage, String time, int shopLogo, boolean hasUnread, int unreadCount) {
            this.shopName = shopName;
            this.lastMessage = lastMessage;
            this.time = time;
            this.shopLogo = shopLogo;
            this.hasUnread = hasUnread;
            this.unreadCount = unreadCount;
        }
    }

    public static class BookingMessage {
        public String shopName;
        public String serviceName;
        public String bookingTime;
        public String status;
        public int shopLogo;

        public BookingMessage(String shopName, String serviceName, String bookingTime, String status, int shopLogo) {
            this.shopName = shopName;
            this.serviceName = serviceName;
            this.bookingTime = bookingTime;
            this.status = status;
            this.shopLogo = shopLogo;
        }
    }

    public static class InteractionMessage {
        public String userName;
        public String action; // 赞了、收藏了、关注了你、评论了
        public String content; // 帖子内容或评论内容
        public String time;
        public int userAvatar;

        public InteractionMessage(String userName, String action, String content, String time, int userAvatar) {
            this.userName = userName;
            this.action = action;
            this.content = content;
            this.time = time;
            this.userAvatar = userAvatar;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentTab == 0) {
            refreshShopMessages();
        } else if (currentTab == 3) {
            renderSystemMessages();
        }
    }

    private void refreshBookingMessages() {
        llMessageList.removeAllViews();
        renderBookingMessages();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        llMessageList = findViewById(R.id.ll_message_list);
        subTabLayout = findViewById(R.id.sub_tab_layout);
        layoutEmpty = findViewById(R.id.layout_empty);
        tvEmptyText = findViewById(R.id.tv_empty_text);

        ChatMessageManager.getInstance().loadFromPrefs(this);

        initTabs();
        renderMessages();
    }

    private void initTabs() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        findViewById(R.id.tab_shop).setOnClickListener(v -> switchTab(0));
        findViewById(R.id.tab_booking).setOnClickListener(v -> switchTab(1));
        findViewById(R.id.tab_petgram).setOnClickListener(v -> switchTab(2));
        findViewById(R.id.tab_system).setOnClickListener(v -> switchTab(3));

        findViewById(R.id.sub_tab_like).setOnClickListener(v -> switchSubTab(0));
        findViewById(R.id.sub_tab_follow).setOnClickListener(v -> switchSubTab(1));
        findViewById(R.id.sub_tab_comment).setOnClickListener(v -> switchSubTab(2));
    }

    private void switchTab(int index) {
        currentTab = index;
        updateTabUI();
        subTabLayout.setVisibility(index == 2 ? View.VISIBLE : View.GONE);
        renderMessages();
    }

    private void switchSubTab(int index) {
        currentSubTab = index;
        updateSubTabUI();
        renderMessages();
    }

    private void updateTabUI() {
        TextView tvShop = findViewById(R.id.tv_tab_shop);
        TextView tvBooking = findViewById(R.id.tv_tab_booking);
        TextView tvPetgram = findViewById(R.id.tv_tab_petgram);
        TextView tvSystem = findViewById(R.id.tv_tab_system);
        View vShop = findViewById(R.id.v_tab_shop);
        View vBooking = findViewById(R.id.v_tab_booking);
        View vPetgram = findViewById(R.id.v_tab_petgram);
        View vSystem = findViewById(R.id.v_tab_system);

        tvShop.setTextColor(currentTab == 0 ? 0xFFFFB74D : 0xFF999999);
        tvShop.setTypeface(currentTab == 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        vShop.setBackgroundColor(currentTab == 0 ? 0xFFFFB74D : 0x00000000);

        tvBooking.setTextColor(currentTab == 1 ? 0xFFFFB74D : 0xFF999999);
        tvBooking.setTypeface(currentTab == 1 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        vBooking.setBackgroundColor(currentTab == 1 ? 0xFFFFB74D : 0x00000000);

        tvPetgram.setTextColor(currentTab == 2 ? 0xFFFFB74D : 0xFF999999);
        tvPetgram.setTypeface(currentTab == 2 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        vPetgram.setBackgroundColor(currentTab == 2 ? 0xFFFFB74D : 0x00000000);

        tvSystem.setTextColor(currentTab == 3 ? 0xFFFFB74D : 0xFF999999);
        tvSystem.setTypeface(currentTab == 3 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        vSystem.setBackgroundColor(currentTab == 3 ? 0xFFFFB74D : 0x00000000);
    }

    private void updateSubTabUI() {
        TextView tvLike = findViewById(R.id.sub_tab_like);
        TextView tvFollow = findViewById(R.id.sub_tab_follow);
        TextView tvComment = findViewById(R.id.sub_tab_comment);

        tvLike.setTextColor(currentSubTab == 0 ? 0xFFFFB74D : 0xFF999999);
        tvLike.setTypeface(currentSubTab == 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

        tvFollow.setTextColor(currentSubTab == 1 ? 0xFFFFB74D : 0xFF999999);
        tvFollow.setTypeface(currentSubTab == 1 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

        tvComment.setTextColor(currentSubTab == 2 ? 0xFFFFB74D : 0xFF999999);
        tvComment.setTypeface(currentSubTab == 2 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
    }

    private void renderMessages() {
        llMessageList.removeAllViews();

        if (currentTab == 0) {
            renderShopMessages();
        } else if (currentTab == 1) {
            renderBookingMessages();
        } else if (currentTab == 2) {
            renderInteractionMessages();
        } else {
            renderSystemMessages();
        }
    }

    private List<ShopMessage> getShopMessages() {
        List<ShopMessage> list = new ArrayList<>();

        List<ChatMessageManager.ShopMessageInfo> realShops = ChatMessageManager.getInstance().getAllShopInfos();
        for (ChatMessageManager.ShopMessageInfo info : realShops) {
            if (info.lastMessage != null && !info.lastMessage.isEmpty()) {
                int avatarRes = info.avatarRes;
                if (avatarRes == 0) {
                    avatarRes = ShopActivity.Product.getShopAvatarRes(info.shopName);
                }
                list.add(new ShopMessage(info.shopName, info.lastMessage, info.lastTime, avatarRes, info.unreadCount > 0, info.unreadCount));
            }
        }

        return list;
    }

    private void refreshShopMessages() {
        llMessageList.removeAllViews();
        renderShopMessages();
    }

    private void renderShopMessages() {
        List<ShopMessage> messages = getShopMessages();
        if (messages.isEmpty()) {
            showEmptyState("暂无客服消息");
            return;
        }
        hideEmptyState();
        for (int i = 0; i < messages.size(); i++) {
            View card = createShopMessageCard(messages.get(i));
            llMessageList.addView(card);
            if (i < messages.size() - 1) {
                addSpace();
            }
        }
    }

    private View createShopMessageCard(final ShopMessage msg) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_dynamic_card);
        card.setPadding(dp2px(14), dp2px(14), dp2px(14), dp2px(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        card.setLayoutParams(lp);

        ImageView logoIv = new ImageView(this);
        logoIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logoIv.setImageResource(msg.shopLogo);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp2px(48), dp2px(48));
        logoIv.setLayoutParams(logoParams);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp2px(24));
        bg.setColor(0xFFF0F0F0);
        logoIv.setBackground(bg);
        card.addView(logoIv);

        LinearLayout rightLayout = new LinearLayout(this);
        rightLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        rightParams.setMargins(dp2px(12), 0, 0, 0);
        rightLayout.setLayoutParams(rightParams);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topRow.setLayoutParams(topParams);

        TextView nameTv = new TextView(this);
        nameTv.setText(msg.shopName);
        nameTv.setTextColor(0xFF333333);
        nameTv.setTextSize(15);
        nameTv.setTypeface(Typeface.DEFAULT_BOLD);
        nameTv.setMaxLines(1);
        nameTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        nameTv.setLayoutParams(nameParams);
        topRow.addView(nameTv);

        TextView timeTv = new TextView(this);
        timeTv.setText(msg.time);
        timeTv.setTextColor(0xFFBBBBBB);
        timeTv.setTextSize(12);
        topRow.addView(timeTv);

        rightLayout.addView(topRow);

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams bottomParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bottomParams.setMargins(0, dp2px(6), 0, 0);
        bottomRow.setLayoutParams(bottomParams);

        TextView contentTv = new TextView(this);
        contentTv.setText(msg.lastMessage);
        contentTv.setTextColor(0xFF999999);
        contentTv.setTextSize(13);
        contentTv.setMaxLines(1);
        contentTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        contentTv.setLayoutParams(contentParams);
        bottomRow.addView(contentTv);

        if (msg.hasUnread) {
            TextView badgeTv = new TextView(this);
            badgeTv.setText(String.valueOf(msg.unreadCount));
            badgeTv.setTextColor(0xFFFFFFFF);
            badgeTv.setTextSize(11);
            badgeTv.setTypeface(Typeface.DEFAULT_BOLD);
            badgeTv.setGravity(Gravity.CENTER);
            badgeTv.setBackgroundResource(R.drawable.bg_tag_orange);
            badgeTv.setPadding(dp2px(6), dp2px(1), dp2px(6), dp2px(1));
            bottomRow.addView(badgeTv);
        }

        rightLayout.addView(bottomRow);
        card.addView(rightLayout);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(MessageActivity.this, ChatDetailActivity.class);
            intent.putExtra(ChatDetailActivity.EXTRA_TITLE, msg.shopName);
            intent.putExtra(ChatDetailActivity.EXTRA_TYPE, ChatDetailActivity.TYPE_SHOP);
            intent.putExtra(ChatDetailActivity.EXTRA_AVATAR, msg.shopLogo);
            intent.putExtra(ChatDetailActivity.EXTRA_SHOP_NAME, msg.shopName);
            intent.putExtra(ChatDetailActivity.EXTRA_IS_AI_CHAT, true);
            startActivity(intent);
        });

        return card;
    }

    private List<ChatMessage> getShopChatMessages(ShopMessage shop) {
        List<ChatMessage> list = new ArrayList<>();
        if ("官方旗舰店".equals(shop.shopName)) {
            list.add(new ChatMessage("亲，欢迎光临官方旗舰店~全场满199减30哦！", "06-25 09:30", false));
            list.add(new ChatMessage("你好，这款全自动猫砂盆好用吗？", "06-25 09:35", true));
            list.add(new ChatMessage("非常好用的亲亲~全自动打包换袋，省心又省力，铲屎官必备神器哦😺", "06-25 09:36", false));
            list.add(new ChatMessage("那猫咪用着安全吗？会不会夹到？", "06-25 09:40", true));
            list.add(new ChatMessage("放心亲亲~有多重力感应+防夹设计，猫咪靠近会自动暂停，非常安全的~", "06-25 09:42", false));
            list.add(new ChatMessage("好的，我买一个", "06-25 09:43", true));
            list.add(new ChatMessage("感谢亲亲的支持~我们会尽快为您发货哒！", "06-25 09:45", false));
            list.add(new ChatMessage("【交易物流】您购买的全自动猫砂盆已发货，快递公司：顺丰速运，运单号：SF1234567890，请注意查收~", "昨天 14:30", false));
        } else if ("木屋旺旗舰店".equals(shop.shopName)) {
            list.add(new ChatMessage("您好，欢迎光临木屋旺旗舰店~", "前天 09:20", false));
            list.add(new ChatMessage("你好，这款狗屋中型犬能用吗？", "前天 09:25", true));
            list.add(new ChatMessage("可以的亲亲~中号适合30斤以内的狗狗哦，您家是什么品种呢？", "前天 09:26", false));
            list.add(new ChatMessage("柯基，大概25斤，中号可以吗？", "前天 09:30", true));
            list.add(new ChatMessage("完全没问题的亲亲~柯基住中号刚刚好，还能转身呢~", "前天 09:35", false));
            list.add(new ChatMessage("好的，那就中号吧，能帮忙快点发货吗？", "前天 09:40", true));
            list.add(new ChatMessage("好的，已为您安排明天发货", "前天 10:15", false));
        } else if ("帕特旗舰店".equals(shop.shopName)) {
            list.add(new ChatMessage("亲，欢迎光临帕特旗舰店~", "06-24 10:00", false));
            list.add(new ChatMessage("这款生命猫粮幼猫能吃吗？", "06-24 10:10", true));
            list.add(new ChatMessage("可以的亲亲~这款是全阶段猫粮，成猫幼猫都能吃，幼猫版还有羊奶粉配方哦", "06-24 10:12", false));
            list.add(new ChatMessage("好的，我买一袋幼猫款", "06-24 10:20", true));
            list.add(new ChatMessage("感谢亲亲的支持~我们会尽快发货哒😺", "06-24 10:21", false));
            list.add(new ChatMessage("您购买的猫粮已送达，请确认收货", "06-25 16:20", false));
        } else if ("超宠爱旗舰店".equals(shop.shopName)) {
            list.add(new ChatMessage("欢迎光临超宠爱旗舰店~", "06-23 14:00", false));
            list.add(new ChatMessage("你好，请问这款沐浴露泰迪能用吗？", "06-24 09:30", true));
            list.add(new ChatMessage("亲，这款沐浴露有货的哦", "06-24 09:40", false));
            list.add(new ChatMessage("泰迪用了会美毛吗？", "06-24 09:45", true));
            list.add(new ChatMessage("可以的亲亲~氨基酸配方柔顺亮毛，泰迪用了毛发更蓬松有光泽呢~", "06-24 09:48", false));
        }
        return list;
    }

    private List<BookingMessage> getBookingMessages() {
        List<BookingMessage> list = new ArrayList<>();
        List<BookingMessageManager.BookingMessage> realMessages = BookingMessageManager.getInstance().getMessages(this);
        for (BookingMessageManager.BookingMessage realMsg : realMessages) {
            list.add(new BookingMessage(realMsg.shopName, realMsg.serviceName,
                    realMsg.bookingTime, realMsg.status, realMsg.shopLogo));
        }
        return list;
    }

    private void renderBookingMessages() {
        List<BookingMessage> messages = getBookingMessages();
        if (messages.isEmpty()) {
            showEmptyState("暂无预约提醒");
            return;
        }
        hideEmptyState();
        for (int i = 0; i < messages.size(); i++) {
            View card = createBookingMessageCard(messages.get(i));
            llMessageList.addView(card);
            if (i < messages.size() - 1) {
                addSpace();
            }
        }
    }

    private View createBookingMessageCard(final BookingMessage msg) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_dynamic_card);
        card.setPadding(dp2px(14), dp2px(14), dp2px(14), dp2px(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        card.setLayoutParams(lp);

        ImageView logoIv = new ImageView(this);
        logoIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logoIv.setImageResource(msg.shopLogo);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp2px(48), dp2px(48));
        logoIv.setLayoutParams(logoParams);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp2px(24));
        bg.setColor(0xFFF0F0F0);
        logoIv.setBackground(bg);
        card.addView(logoIv);

        LinearLayout rightLayout = new LinearLayout(this);
        rightLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        rightParams.setMargins(dp2px(12), 0, 0, 0);
        rightLayout.setLayoutParams(rightParams);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topRow.setLayoutParams(topParams);

        TextView nameTv = new TextView(this);
        nameTv.setText(msg.shopName);
        nameTv.setTextColor(0xFF333333);
        nameTv.setTextSize(15);
        nameTv.setTypeface(Typeface.DEFAULT_BOLD);
        nameTv.setMaxLines(1);
        nameTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        nameTv.setLayoutParams(nameParams);
        topRow.addView(nameTv);

        TextView statusTv = new TextView(this);
        statusTv.setText(msg.status);
        statusTv.setTextColor("预约成功".equals(msg.status) ? 0xFFFF9800 : 0xFF4CAF50);
        statusTv.setTextSize(12);
        statusTv.setTypeface(Typeface.DEFAULT_BOLD);
        topRow.addView(statusTv);

        rightLayout.addView(topRow);

        TextView serviceTv = new TextView(this);
        serviceTv.setText("服务项目：" + msg.serviceName);
        serviceTv.setTextColor(0xFF666666);
        serviceTv.setTextSize(13);
        LinearLayout.LayoutParams serviceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        serviceParams.setMargins(0, dp2px(6), 0, 0);
        serviceTv.setLayoutParams(serviceParams);
        rightLayout.addView(serviceTv);

        TextView timeTv = new TextView(this);
        timeTv.setText("预约时间：" + msg.bookingTime);
        timeTv.setTextColor(0xFF999999);
        timeTv.setTextSize(12);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        timeParams.setMargins(0, dp2px(4), 0, 0);
        timeTv.setLayoutParams(timeParams);
        rightLayout.addView(timeTv);

        card.addView(rightLayout);

        card.setOnClickListener(v -> {
            List<ChatMessage> chatList = getBookingChatMessages(msg);
            ChatDataHolder.getInstance().setMessages(chatList);
            Intent intent = new Intent(MessageActivity.this, ChatDetailActivity.class);
            intent.putExtra(ChatDetailActivity.EXTRA_TITLE, msg.shopName);
            intent.putExtra(ChatDetailActivity.EXTRA_TYPE, ChatDetailActivity.TYPE_BOOKING);
            startActivity(intent);
        });

        return card;
    }

    private List<ChatMessage> getBookingChatMessages(BookingMessage booking) {
        List<ChatMessage> list = new ArrayList<>();
        list.add(new ChatMessage("预约成功",
                "服务项目：" + booking.serviceName + "\n预约时间：" + booking.bookingTime + "\n门店：" + booking.shopName + "\n状态：" + booking.status,
                booking.bookingTime, false, 0));
        return list;
    }

    private List<InteractionMessage> getInteractionMessages() {
        List<InteractionMessage> list = new ArrayList<>();
        String type = "";
        if (currentSubTab == 0) {
            type = "like";
        } else if (currentSubTab == 1) {
            type = "follow";
        } else {
            type = "comment";
        }
        List<InteractionMessageManager.InteractionMsg> realMsgs = InteractionMessageManager.getInstance().getMessages(this, type);
        for (InteractionMessageManager.InteractionMsg realMsg : realMsgs) {
            list.add(new InteractionMessage(realMsg.userName, realMsg.action, realMsg.content, realMsg.time, realMsg.avatarRes));
        }
        return list;
    }

    private void renderInteractionMessages() {
        List<InteractionMessage> messages = getInteractionMessages();
        if (messages.isEmpty()) {
            showEmptyState("暂无互动消息");
            return;
        }
        hideEmptyState();
        for (int i = 0; i < messages.size(); i++) {
            View card = createInteractionCard(messages.get(i));
            llMessageList.addView(card);
            if (i < messages.size() - 1) {
                addSpace();
            }
        }
    }

    private View createInteractionCard(final InteractionMessage msg) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_dynamic_card);
        card.setPadding(dp2px(14), dp2px(14), dp2px(14), dp2px(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        card.setLayoutParams(lp);

        ImageView avatarIv = new ImageView(this);
        avatarIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarIv.setImageResource(msg.userAvatar);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp2px(44), dp2px(44));
        avatarIv.setLayoutParams(avatarParams);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp2px(22));
        bg.setColor(0xFFF0F0F0);
        avatarIv.setBackground(bg);
        card.addView(avatarIv);

        LinearLayout rightLayout = new LinearLayout(this);
        rightLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        rightParams.setMargins(dp2px(12), 0, 0, 0);
        rightLayout.setLayoutParams(rightParams);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topRow.setLayoutParams(topParams);

        TextView nameTv = new TextView(this);
        nameTv.setText(msg.userName);
        nameTv.setTextColor(0xFF333333);
        nameTv.setTextSize(14);
        nameTv.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        nameTv.setLayoutParams(nameParams);
        topRow.addView(nameTv);

        TextView timeTv = new TextView(this);
        timeTv.setText(msg.time);
        timeTv.setTextColor(0xFFBBBBBB);
        timeTv.setTextSize(12);
        topRow.addView(timeTv);

        rightLayout.addView(topRow);

        TextView actionTv = new TextView(this);
        actionTv.setText(msg.action);
        actionTv.setTextColor(0xFFFF9800);
        actionTv.setTextSize(13);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        actionParams.setMargins(0, dp2px(4), 0, 0);
        actionTv.setLayoutParams(actionParams);
        rightLayout.addView(actionTv);

        if (!msg.content.isEmpty()) {
            TextView contentTv = new TextView(this);
            contentTv.setText(msg.content);
            contentTv.setTextColor(0xFF999999);
            contentTv.setTextSize(13);
            contentTv.setMaxLines(2);
            contentTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            contentParams.setMargins(0, dp2px(4), 0, 0);
            contentTv.setLayoutParams(contentParams);
            rightLayout.addView(contentTv);
        }

        card.addView(rightLayout);

        card.setOnClickListener(v -> {
            List<ChatMessage> chatList = getInteractionChatMessages();
            ChatDataHolder.getInstance().setMessages(chatList);
            Intent intent = new Intent(MessageActivity.this, ChatDetailActivity.class);
            String title = "";
            if (currentSubTab == 0) {
                title = "赞和收藏";
            } else if (currentSubTab == 1) {
                title = "新增关注";
            } else {
                title = "评论";
            }
            intent.putExtra(ChatDetailActivity.EXTRA_TITLE, title);
            intent.putExtra(ChatDetailActivity.EXTRA_TYPE, ChatDetailActivity.TYPE_INTERACTION);
            startActivity(intent);
        });

        return card;
    }

    private void renderSystemMessages() {
        List<SystemMessageManager.SystemMsg> messages = SystemMessageManager.getInstance().getMessages(this);
        if (messages.isEmpty()) {
            showEmptyState("暂无系统通知");
            return;
        }
        hideEmptyState();
        for (int i = 0; i < messages.size(); i++) {
            View card = createSystemMessageCard(messages.get(i));
            llMessageList.addView(card);
            if (i < messages.size() - 1) {
                addSpace();
            }
        }
    }

    private View createSystemMessageCard(final SystemMessageManager.SystemMsg msg) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_dynamic_card);
        card.setPadding(dp2px(14), dp2px(14), dp2px(14), dp2px(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        card.setLayoutParams(lp);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        topRow.setLayoutParams(topParams);

        ImageView iconIv = new ImageView(this);
        iconIv.setImageResource(R.drawable.ic_cat_logo);
        iconIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp2px(40), dp2px(40));
        iconIv.setLayoutParams(iconParams);
        topRow.addView(iconIv);

        LinearLayout titleLayout = new LinearLayout(this);
        titleLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        titleParams.setMargins(dp2px(10), 0, 0, 0);
        titleLayout.setLayoutParams(titleParams);

        TextView titleTv = new TextView(this);
        titleTv.setText(msg.title);
        titleTv.setTextColor(0xFF333333);
        titleTv.setTextSize(15);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleLayout.addView(titleTv);

        TextView timeTv = new TextView(this);
        timeTv.setText(msg.time);
        timeTv.setTextColor(0xFF999999);
        timeTv.setTextSize(12);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        timeParams.setMargins(0, dp2px(2), 0, 0);
        timeTv.setLayoutParams(timeParams);
        titleLayout.addView(timeTv);

        topRow.addView(titleLayout);
        card.addView(topRow);

        TextView contentTv = new TextView(this);
        contentTv.setText(msg.content);
        contentTv.setTextColor(0xFF666666);
        contentTv.setTextSize(13);
        contentTv.setLineSpacing(dp2px(4), 1);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        contentParams.setMargins(0, dp2px(10), 0, 0);
        contentTv.setLayoutParams(contentParams);
        card.addView(contentTv);

        return card;
    }

    private List<ChatMessage> getInteractionChatMessages() {
        List<InteractionMessage> interactions = getInteractionMessages();
        List<ChatMessage> list = new ArrayList<>();
        for (InteractionMessage im : interactions) {
            String content;
            String subTitle;
            if (currentSubTab == 1) {
                content = im.userName + " " + im.action;
                subTitle = im.userName;
            } else {
                content = im.userName + " " + im.action;
                subTitle = im.content;
            }
            list.add(new ChatMessage(content, subTitle, im.time, false, im.userAvatar));
        }
        return list;
    }

    private void showEmptyState(String text) {
        layoutEmpty.setVisibility(View.VISIBLE);
        tvEmptyText.setText(text);
        llMessageList.removeAllViews();
    }

    private void hideEmptyState() {
        layoutEmpty.setVisibility(View.GONE);
    }

    private void addSpace() {
        View space = new View(this);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp2px(10));
        llMessageList.addView(space, sp);
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
