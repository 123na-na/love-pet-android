package com.example.love_pet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class CustomerServiceActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_CODE_PERMISSION_AUDIO = 2001;

    private EditText etInput;
    private ImageView ivVoice;
    private LinearLayout messageContainer;
    private android.widget.ScrollView scrollViewMessage;
    private Uri cameraImageUri;

    private BaiduSpeechRecognizer speechRecognizer;
    private boolean isRecording = false;
    private QwenApiHelper qwenApiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_service);

        // 返回按钮
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // 动态设置标题
        String shopName = getIntent().getStringExtra("shop_name");
        if (shopName != null && !shopName.isEmpty()) {
            TextView tvTitle = findViewById(R.id.tv_title);
            if (tvTitle != null) {
                tvTitle.setText(shopName);
            }
        }

        // 初始化控件
        etInput = findViewById(R.id.et_input);
        ivVoice = findViewById(R.id.iv_voice);
        ImageView ivAddImage = findViewById(R.id.iv_add_image);
        ImageView ivSend = findViewById(R.id.iv_send);
        messageContainer = findViewById(R.id.message_container);
        scrollViewMessage = findViewById(R.id.scroll_message);

        // 设置客服头像为圆形
        ImageView ivCsAvatar = findViewById(R.id.iv_cs_avatar);
        if (ivCsAvatar != null) {
            ivCsAvatar.setClipToOutline(true);
            ivCsAvatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
                @Override
                public void getOutline(android.view.View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
        }

        // 初始化百度语音识别
        speechRecognizer = new BaiduSpeechRecognizer(this);

        // 初始化千问大模型
        qwenApiHelper = new QwenApiHelper();

        // 根据店铺和商品信息设置AI系统提示词
        String productName = getIntent().getStringExtra("product_name");
        setupAiSystemPrompt(shopName, productName);

        // 语音识别按钮（按住录音，松开识别）
        setupVoiceButton();

        // 图片上传按钮（加号）
        ivAddImage.setOnClickListener(v -> showImagePickerDialog());

        // 发送按钮
        ivSend.setOnClickListener(v -> sendMessage());
    }

    private String currentShopName = "";
    private int currentShopAvatar = R.drawable.ic_cat_logo;

    /**
     * 根据店铺和商品信息设置AI系统提示词
     */
    private void setupAiSystemPrompt(String shopName, String productName) {
        currentShopName = shopName != null ? shopName : "官方旗舰店";
        if (shopName != null && !shopName.isEmpty()) {
            currentShopAvatar = ShopActivity.Product.getShopAvatarRes(currentShopName);
        } else {
            currentShopAvatar = R.drawable.ic_cat_logo;
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是").append(currentShopName).append("的智能客服小咪，");
        prompt.append("请用友好、热情、专业的语气回答用户关于店铺商品的问题。");
        prompt.append("回答要简洁明了，控制在100字以内。");

        if (productName != null && !productName.isEmpty()) {
            prompt.append("当前用户正在浏览的商品是：").append(productName).append("。");
            prompt.append("如果用户询问该商品，请给出积极的推荐和专业的解答。");
        }

        if ("官方旗舰店".equals(currentShopName)) {
            prompt.append("本店主营宠物智能用品，如全自动猫砂盆等，品质有保障，全场满199减30。");
        } else if ("木屋旺旗舰店".equals(currentShopName)) {
            prompt.append("本店主营实木宠物屋、狗笼狗窝等，防水防晒，四季通用，支持定制。");
        } else if ("帕特旗舰店".equals(currentShopName)) {
            prompt.append("本店主营帕特品牌宠物食品，生命系列猫粮生骨肉主食冻干，低敏营养，假一赔四。");
        } else if ("超宠爱旗舰店".equals(currentShopName)) {
            prompt.append("本店主营卡露诗品牌宠物沐浴露，氨基酸配方，美毛护肤，持久留香。");
        }

        prompt.append("如果遇到无法回答的问题，建议用户联系人工客服。");
        qwenApiHelper.setSystemPrompt(prompt.toString());
    }

    /**
     * 设置语音按钮：按住录音，松开停止并识别
     */
    private void setupVoiceButton() {
        ivVoice.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startVoiceRecording();
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    stopVoiceRecording();
                    return true;
            }
            return false;
        });
    }

    /**
     * 开始录音
     */
    private void startVoiceRecording() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.RECORD_AUDIO },
                    REQUEST_CODE_PERMISSION_AUDIO);
            return;
        }

        isRecording = true;
        // 按下时改变图标样式（放大+高亮）
        ivVoice.setScaleX(1.15f);
        ivVoice.setScaleY(1.15f);
        ivVoice.setAlpha(0.7f);

        speechRecognizer.startRecognition(new BaiduSpeechRecognizer.OnRecognitionListener() {
            @Override
            public void onRecordingStart() {
                Toast.makeText(CustomerServiceActivity.this, "开始录音...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRecordingStop() {
            }

            @Override
            public void onResult(String text) {
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CustomerServiceActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 停止录音并发起识别
     */
    private void stopVoiceRecording() {
        if (!isRecording)
            return;
        isRecording = false;

        // 松开恢复图标样式
        ivVoice.setScaleX(1.0f);
        ivVoice.setScaleY(1.0f);
        ivVoice.setAlpha(1.0f);

        Toast.makeText(this, "正在识别...", Toast.LENGTH_SHORT).show();

        speechRecognizer.stopRecognition(new BaiduSpeechRecognizer.OnRecognitionListener() {
            @Override
            public void onRecordingStart() {
            }

            @Override
            public void onRecordingStop() {
            }

            @Override
            public void onResult(String text) {
                if (text != null && !text.isEmpty()) {
                    // 将识别结果追加到输入框
                    String currentText = etInput.getText().toString();
                    etInput.setText(currentText + (currentText.isEmpty() ? "" : "") + text);
                    etInput.setSelection(etInput.getText().length());
                    Toast.makeText(CustomerServiceActivity.this, "识别成功", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CustomerServiceActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 显示图片选择弹窗（相册/拍照）
     */
    private void showImagePickerDialog() {
        String[] items = { "从相册选择", "拍摄照片" };
        new AlertDialog.Builder(this)
                .setTitle("选择图片")
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        pickFromGallery();
                    } else {
                        takePhoto();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 从相册选择图片
     */
    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    /**
     * 拍照获取图片
     */
    private void takePhoto() {
        try {
            File photoFile = createTempImageFile();
            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        } catch (IOException e) {
            Toast.makeText(this, "无法创建图片文件", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 创建临时图片文件
     */
    private File createTempImageFile() throws IOException {
        String fileName = "cs_image_" + System.currentTimeMillis() + ".jpg";
        File dir = new File(getExternalCacheDir(), "customer_service");
        if (!dir.exists())
            dir.mkdirs();
        return new File(dir, fileName);
    }

    /**
     * 发送消息
     */
    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        addUserMessage(text);
        etInput.setText("");
        requestAiReply(text);
    }

    /**
     * 添加用户消息气泡
     */
    private void addUserMessage(String text) {
        LinearLayout msgLayout = new LinearLayout(this);
        msgLayout.setOrientation(LinearLayout.HORIZONTAL);
        msgLayout.setGravity(android.view.Gravity.END);

        // 气泡
        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextSize(14);
        bubble.setTextColor(0xFF333333);
        bubble.setBackgroundResource(R.drawable.bg_user_bubble);
        int dp6 = (int) (6 * getResources().getDisplayMetrics().density);
        int dp8 = (int) (8 * getResources().getDisplayMetrics().density);
        bubble.setPadding(dp6, dp6, dp6, dp6);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        bubble.setMaxWidth((int) (screenWidth * 0.7f));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bubble.setLayoutParams(params);
        msgLayout.addView(bubble);

        // 用户头像（圆形，在气泡右侧）
        ImageView avatar = new ImageView(this);
        int sizeDp36 = (int) (36 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(sizeDp36, sizeDp36);
        avatarParams.setMarginStart(dp8);
        avatar.setLayoutParams(avatarParams);

        avatar.setClipToOutline(true);
        avatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });

        // 使用 AvatarManager 加载用户头像（与"我的"页面一致）
        AvatarManager.loadAvatar(this, avatar);
        msgLayout.addView(avatar);

        // 消息间距
        android.widget.LinearLayout.LayoutParams containerParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(0, 0, 0, dp6);
        messageContainer.addView(msgLayout, containerParams);
        scrollToBottom();
    }

    /**
     * 滚动到消息区域底部
     */
    private void scrollToBottom() {
        scrollViewMessage.post(() -> scrollViewMessage.fullScroll(android.view.View.FOCUS_DOWN));
    }

    /**
     * 调用千问大模型获取AI回复
     */
    private void requestAiReply(String userMessage) {
        // 先显示"正在思考"提示
        addCustomerServiceMessage("正在思考中...");

        qwenApiHelper.sendMessage(userMessage, new QwenApiHelper.OnResponseCallback() {
            @Override
            public void onSuccess(String reply) {
                // 移除"正在思考"消息，显示真实回复
                removeLastMessage();
                addCustomerServiceMessage(reply);
            }

            @Override
            public void onError(String error) {
                removeLastMessage();
                addCustomerServiceMessage("抱歉，暂时无法回复，请稍后再试~");
                Toast.makeText(CustomerServiceActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 移除最后一条客服消息
     */
    private void removeLastMessage() {
        if (messageContainer.getChildCount() > 0) {
            messageContainer.removeViewAt(messageContainer.getChildCount() - 1);
        }
    }

    /**
     * 添加客服消息气泡
     */
    private void addCustomerServiceMessage(String text) {
        LinearLayout container = findViewById(R.id.message_container);

        LinearLayout msgLayout = new LinearLayout(this);
        msgLayout.setOrientation(LinearLayout.HORIZONTAL);
        msgLayout.setGravity(android.view.Gravity.START);

        // 客服头像
        ImageView avatar = new ImageView(this);
        avatar.setImageResource(currentShopAvatar);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int sizeDp44 = (int) (44 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(sizeDp44, sizeDp44);
        avatar.setLayoutParams(avatarParams);
        avatar.setClipToOutline(true);
        avatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        msgLayout.addView(avatar);

        // 消息气泡
        TextView bubble = new TextView(this);
        bubble.setText(text);
        bubble.setTextSize(14);
        bubble.setTextColor(0xFF333333);
        bubble.setBackgroundResource(R.drawable.bg_dynamic_card);
        int dp10 = (int) (10 * getResources().getDisplayMetrics().density);
        int dp6 = (int) (6 * getResources().getDisplayMetrics().density);
        bubble.setPadding(dp6, dp6, dp6, dp6);
        bubble.setMaxWidth((int) (getResources().getDisplayMetrics().widthPixels * 0.7f));
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bubbleParams.setMarginStart(dp10);
        bubble.setLayoutParams(bubbleParams);
        msgLayout.addView(bubble);

        // 消息间距
        android.widget.LinearLayout.LayoutParams containerParams = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        containerParams.setMargins(0, 0, 0, dp6);
        container.addView(msgLayout, containerParams);
        scrollToBottom();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri;
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
            } else {
                imageUri = cameraImageUri;
            }
            if (imageUri != null) {
                Toast.makeText(this, "图片已选择", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予，用户可以再次点击录音
                Toast.makeText(this, "权限已授权，请按住麦克风录音", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要录音权限才能使用语音识别", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.release();
        }
        if (qwenApiHelper != null) {
            qwenApiHelper.release();
        }
    }
}
