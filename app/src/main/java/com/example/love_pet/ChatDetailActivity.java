package com.example.love_pet;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_AVATAR = "avatar";
    public static final String EXTRA_SHOP_NAME = "shop_name";
    public static final String EXTRA_PRODUCT_NAME = "product_name";
    public static final String EXTRA_IS_AI_CHAT = "is_ai_chat";

    public static final int TYPE_SHOP = 0;
    public static final int TYPE_BOOKING = 1;
    public static final int TYPE_INTERACTION = 2;

    private LinearLayout llMessageContainer;
    private ScrollView scrollMessage;
    private LinearLayout llInputBar;
    private EditText etInput;
    private TextView tvSend;

    private int currentType = TYPE_SHOP;
    private int avatarRes = R.drawable.avatar_color1;
    private List<ChatMessage> messageList;
    private boolean isAiChat = false;
    private String shopName = "";
    private String productName = "";
    private QwenApiHelper qwenApiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        llMessageContainer = findViewById(R.id.ll_message_container);
        scrollMessage = findViewById(R.id.scroll_message);
        llInputBar = findViewById(R.id.ll_input_bar);
        etInput = findViewById(R.id.et_input);
        tvSend = findViewById(R.id.tv_send);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        currentType = getIntent().getIntExtra(EXTRA_TYPE, TYPE_SHOP);
        avatarRes = getIntent().getIntExtra(EXTRA_AVATAR, R.drawable.avatar_color1);
        isAiChat = getIntent().getBooleanExtra(EXTRA_IS_AI_CHAT, false);
        shopName = getIntent().getStringExtra(EXTRA_SHOP_NAME);
        productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
        if (shopName == null)
            shopName = "";
        if (productName == null)
            productName = "";

        messageList = ChatDataHolder.getInstance().getMessages();

        TextView tvTitle = findViewById(R.id.tv_title);
        if (title != null && !title.isEmpty()) {
            tvTitle.setText(title);
        }

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        if (currentType == TYPE_SHOP) {
            llInputBar.setVisibility(View.VISIBLE);
            tvSend.setOnClickListener(v -> sendMessage());

            if (isAiChat) {
                qwenApiHelper = new QwenApiHelper();
                setupAiSystemPrompt();

                ChatMessageManager.getInstance().loadFromPrefs(this);
                List<ChatMessage> savedMessages = ChatMessageManager.getInstance().getShopMessages(shopName);
                if (savedMessages.isEmpty()) {
                    addAiWelcomeMessage();
                } else {
                    for (ChatMessage msg : savedMessages) {
                        llMessageContainer.addView(createShopMessageItem(msg));
                    }
                    scrollMessage.post(() -> scrollMessage.fullScroll(ScrollView.FOCUS_DOWN));
                    ChatMessage lastMsg = savedMessages.get(savedMessages.size() - 1);
                    ChatMessageManager.getInstance().setShopInfo(this, shopName, avatarRes, lastMsg.content,
                            lastMsg.time, 0);
                }
                ChatMessageManager.getInstance().clearUnread(this, shopName);
            }
        } else {
            llInputBar.setVisibility(View.GONE);
        }

        if (!isAiChat) {
            renderMessages();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ChatDataHolder.getInstance().clear();
    }

    private void renderMessages() {
        if (messageList == null)
            return;
        for (ChatMessage msg : messageList) {
            View item;
            if (currentType == TYPE_SHOP) {
                item = createShopMessageItem(msg);
            } else if (currentType == TYPE_BOOKING) {
                item = createBookingMessageItem(msg);
            } else {
                item = createInteractionMessageItem(msg);
            }
            llMessageContainer.addView(item);
        }
        scrollMessage.post(() -> scrollMessage.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private View createShopMessageItem(ChatMessage msg) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wp.setMargins(0, 0, 0, dp2px(12));
        wrapper.setLayoutParams(wp);

        TextView timeTv = new TextView(this);
        timeTv.setText(msg.time);
        timeTv.setTextColor(0xFFBBBBBB);
        timeTv.setTextSize(11);
        timeTv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tp.setMargins(0, 0, 0, dp2px(8));
        timeTv.setLayoutParams(tp);
        wrapper.addView(timeTv);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(msg.isSelf ? Gravity.END : Gravity.START);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (!msg.isSelf) {
            ImageView avatarIv = new ImageView(this);
            avatarIv.setImageResource(avatarRes);
            LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(dp2px(36), dp2px(36));
            avatarIv.setLayoutParams(ap);
            avatarIv.setClipToOutline(true);
            avatarIv.setOutlineProvider(new android.view.ViewOutlineProvider() {
                @Override
                public void getOutline(android.view.View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            row.addView(avatarIv);
        }

        android.widget.LinearLayout bubble = new android.widget.LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.65);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.setMargins(dp2px(8), 0, dp2px(8), 0);
        bubble.setLayoutParams(bp);
        bubble.setPadding(dp2px(12), dp2px(10), dp2px(12), dp2px(10));
        bubble.setBackgroundResource(msg.isSelf ? R.drawable.bubble_right : R.drawable.bubble_left);

        if (msg.content.contains("【交易物流】")) {
            String cleanContent = msg.content;
            if (cleanContent.startsWith("【交易物流】")) {
                cleanContent = cleanContent.substring(6);
            }
            android.widget.LinearLayout.LayoutParams titleParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            TextView labelTv = new TextView(this);
            labelTv.setText("交易物流");
            labelTv.setTextColor(0xFFFF9800);
            labelTv.setTextSize(12);
            labelTv.setMaxWidth(maxWidth - dp2px(24));
            labelTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            bubble.addView(labelTv, titleParams);

            android.widget.LinearLayout.LayoutParams contentParams = new android.widget.LinearLayout.LayoutParams(
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
            contentParams.setMargins(0, dp2px(4), 0, 0);
            TextView contentTv = new TextView(this);
            contentTv.setText(cleanContent);
            contentTv.setTextColor(0xFF333333);
            contentTv.setTextSize(14);
            contentTv.setMaxWidth(maxWidth - dp2px(24));
            contentTv.setLineSpacing(0, 1.2f);
            bubble.addView(contentTv, contentParams);
        } else {
            TextView contentTv = new TextView(this);
            contentTv.setText(msg.content);
            contentTv.setTextColor(msg.isSelf ? 0xFF333333 : 0xFF333333);
            contentTv.setTextSize(14);
            contentTv.setMaxWidth(maxWidth - dp2px(24));
            contentTv.setLineSpacing(0, 1.2f);
            bubble.addView(contentTv);
        }

        row.addView(bubble);

        if (msg.isSelf) {
            ImageView avatarIv = new ImageView(this);
            AvatarManager.loadAvatar(this, avatarIv);
            LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(dp2px(36), dp2px(36));
            avatarIv.setLayoutParams(ap);
            avatarIv.setClipToOutline(true);
            avatarIv.setOutlineProvider(new android.view.ViewOutlineProvider() {
                @Override
                public void getOutline(android.view.View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            row.addView(avatarIv);
        }

        wrapper.addView(row);
        return wrapper;
    }

    private View createBookingMessageItem(ChatMessage msg) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, 0, 0, dp2px(10));
        card.setLayoutParams(cp);
        card.setPadding(dp2px(14), dp2px(14), dp2px(14), dp2px(14));
        card.setBackgroundResource(R.drawable.bg_order_card);

        TextView titleTv = new TextView(this);
        titleTv.setText(msg.content);
        titleTv.setTextColor(0xFF333333);
        titleTv.setTextSize(15);
        titleTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        card.addView(titleTv);

        TextView contentTv = new TextView(this);
        contentTv.setText(msg.subTitle);
        contentTv.setTextColor(0xFF666666);
        contentTv.setTextSize(13);
        contentTv.setLineSpacing(0, 1.4f);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        contentParams.setMargins(0, dp2px(8), 0, 0);
        contentTv.setLayoutParams(contentParams);
        card.addView(contentTv);

        TextView timeTv = new TextView(this);
        timeTv.setText(msg.time);
        timeTv.setTextColor(0xFFBBBBBB);
        timeTv.setTextSize(11);
        timeTv.setGravity(Gravity.END);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        timeParams.setMargins(0, dp2px(8), 0, 0);
        timeTv.setLayoutParams(timeParams);
        card.addView(timeTv);

        return card;
    }

    private View createInteractionMessageItem(ChatMessage msg) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setBackgroundColor(0xFFFFFFFF);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ip.setMargins(0, 0, 0, dp2px(2));
        item.setLayoutParams(ip);
        item.setPadding(dp2px(14), dp2px(12), dp2px(14), dp2px(12));
        item.setGravity(Gravity.CENTER_VERTICAL);

        ImageView avatarIv = new ImageView(this);
        avatarIv.setImageResource(msg.avatar);
        LinearLayout.LayoutParams ap = new LinearLayout.LayoutParams(dp2px(40), dp2px(40));
        avatarIv.setLayoutParams(ap);
        item.addView(avatarIv);

        LinearLayout rightLayout = new LinearLayout(this);
        rightLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        rp.setMargins(dp2px(10), 0, 0, 0);
        rightLayout.setLayoutParams(rp);

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView titleTv = new TextView(this);
        titleTv.setText(msg.content);
        titleTv.setTextColor(0xFF333333);
        titleTv.setTextSize(14);
        titleTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        titleTv.setLayoutParams(titleParams);
        topRow.addView(titleTv);

        TextView timeTv = new TextView(this);
        timeTv.setText(msg.time);
        timeTv.setTextColor(0xFFBBBBBB);
        timeTv.setTextSize(11);
        topRow.addView(timeTv);

        rightLayout.addView(topRow);

        if (msg.subTitle != null && !msg.subTitle.isEmpty()) {
            TextView subTv = new TextView(this);
            subTv.setText(msg.subTitle);
            subTv.setTextColor(0xFF999999);
            subTv.setTextSize(13);
            subTv.setMaxLines(1);
            subTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            subParams.setMargins(0, dp2px(4), 0, 0);
            subTv.setLayoutParams(subParams);
            rightLayout.addView(subTv);
        }

        item.addView(rightLayout);
        return item;
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty())
            return;

        ChatMessage msg = new ChatMessage(text, "刚刚", true);
        llMessageContainer.addView(createShopMessageItem(msg));
        etInput.setText("");
        scrollMessage.post(() -> scrollMessage.fullScroll(ScrollView.FOCUS_DOWN));

        if (isAiChat && !shopName.isEmpty()) {
            ChatMessageManager.getInstance().addShopMessage(ChatDetailActivity.this, shopName, msg);
            ChatMessageManager.getInstance().setShopInfo(ChatDetailActivity.this, shopName, avatarRes, text, "刚刚", 0);
        }

        if (isAiChat && qwenApiHelper != null) {
            qwenApiHelper.sendMessage(text, new QwenApiHelper.OnResponseCallback() {
                @Override
                public void onSuccess(String reply) {
                    runOnUiThread(() -> {
                        ChatMessage replyMsg = new ChatMessage(reply, "刚刚", false);
                        llMessageContainer.addView(createShopMessageItem(replyMsg));
                        scrollMessage.post(() -> scrollMessage.fullScroll(ScrollView.FOCUS_DOWN));
                        if (!shopName.isEmpty()) {
                            ChatMessageManager.getInstance().addShopMessage(ChatDetailActivity.this, shopName,
                                    replyMsg);
                        }
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        ChatMessage replyMsg = new ChatMessage("抱歉，网络开小差了，请稍后再试~", "刚刚", false);
                        llMessageContainer.addView(createShopMessageItem(replyMsg));
                        scrollMessage.post(() -> scrollMessage.fullScroll(ScrollView.FOCUS_DOWN));
                        if (!shopName.isEmpty()) {
                            ChatMessageManager.getInstance().addShopMessage(ChatDetailActivity.this, shopName,
                                    replyMsg);
                        }
                    });
                }
            });
        } else {
            llMessageContainer.postDelayed(() -> {
                ChatMessage reply = new ChatMessage("收到~客服正在为您处理，请稍候哦😊", "刚刚", false);
                llMessageContainer.addView(createShopMessageItem(reply));
                scrollMessage.post(() -> scrollMessage.fullScroll(ScrollView.FOCUS_DOWN));
            }, 800);
        }
    }

    private void setupAiSystemPrompt() {
        StringBuilder prompt = new StringBuilder();
        String displayName = shopName.isEmpty() ? "店铺" : shopName;
        prompt.append("你是").append(displayName).append("的智能客服小咪，");
        prompt.append("请用友好、热情、专业的语气回答用户关于店铺商品的问题。");
        prompt.append("回答要简洁明了，控制在100字以内。");

        if (!productName.isEmpty()) {
            prompt.append("当前用户正在浏览的商品是：").append(productName).append("。");
            prompt.append("如果用户询问该商品，请给出积极的推荐和专业的解答。");
        }

        if ("官方旗舰店".equals(shopName)) {
            prompt.append("本店主营宠物智能用品，如全自动猫砂盆等，品质有保障，全场满199减30。");
        } else if ("木屋旺旗舰店".equals(shopName)) {
            prompt.append("本店主营实木宠物屋、狗笼狗窝等，防水防晒，四季通用，支持定制。");
        } else if ("帕特旗舰店".equals(shopName)) {
            prompt.append("本店主营帕特品牌宠物食品，生命系列猫粮生骨肉主食冻干，低敏营养，假一赔四。");
        } else if ("超宠爱旗舰店".equals(shopName)) {
            prompt.append("本店主营卡露诗品牌宠物沐浴露，氨基酸配方，美毛护肤，持久留香。");
        }

        prompt.append("如果遇到无法回答的问题，建议用户联系人工客服。");
        qwenApiHelper.setSystemPrompt(prompt.toString());
    }

    private void addAiWelcomeMessage() {
        String welcome = "亲，您好！我是" + (shopName.isEmpty() ? "店铺" : shopName) + "的智能客服小咪，很高兴为您服务~请问有什么可以帮您的吗？";
        ChatMessage msg = new ChatMessage(welcome, "刚刚", false);
        llMessageContainer.addView(createShopMessageItem(msg));
        scrollMessage.post(() -> scrollMessage.fullScroll(ScrollView.FOCUS_DOWN));
        if (!shopName.isEmpty()) {
            ChatMessageManager.getInstance().addShopMessage(this, shopName, msg);
            ChatMessageManager.getInstance().setShopInfo(this, shopName, avatarRes, welcome, "刚刚", 0);
        }
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
