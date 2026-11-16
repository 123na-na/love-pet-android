package com.example.love_pet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConsultChatActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;

    private LinearLayout layoutChatContent;
    private ScrollView scrollChat;
    private EditText etInput;
    private String doctorName;
    private String department;
    private int doctorAvatar;
    private List<String> messageHistory = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Uri cameraImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consult_chat);

        doctorName = getIntent().getStringExtra("doctor_name");
        department = getIntent().getStringExtra("department");
        doctorAvatar = getIntent().getIntExtra("doctor_avatar", R.drawable.doctor);

        if (doctorName == null) doctorName = "李医生";
        if (department == null) department = "内科";

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        TextView tvDoctorName = findViewById(R.id.tv_doctor_name);
        tvDoctorName.setText(doctorName);

        TextView tvDoctorStatus = findViewById(R.id.tv_doctor_status);
        tvDoctorStatus.setText(department + " · 在线中");

        layoutChatContent = findViewById(R.id.layout_chat_content);
        scrollChat = findViewById(R.id.scroll_chat);
        etInput = findViewById(R.id.et_input);

        addDoctorMessage("您好，我是" + department + "的" + doctorName + "，请问您的宠物有什么症状？");
        addSystemMessage("温馨提示：问诊过程中，请尽量详细描述宠物症状、持续时间、精神状态等，有助于医生快速判断。");

        findViewById(R.id.btn_send).setOnClickListener(v -> sendMessage());

        findViewById(R.id.btn_photo).setOnClickListener(v -> showImagePickerDialog());

        findViewById(R.id.btn_prescription).setOnClickListener(v -> {
            Intent intent = new Intent(this, PrescriptionActivity.class);
            intent.putExtra("doctor_name", doctorName);
            intent.putExtra("department", department);
            intent.putExtra("pet_name", "橘猫");
            startActivity(intent);
        });

        findViewById(R.id.btn_nursing).setOnClickListener(v -> showNursingPlan());

        findViewById(R.id.btn_end_consult).setOnClickListener(v -> showEndConsultDialog());

        findViewById(R.id.btn_voice).setOnClickListener(v -> {
            Toast.makeText(this, "语音功能开发中", Toast.LENGTH_SHORT).show();
        });
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    private void sendMessage() {
        String text = etInput.getText().toString().trim();
        if (text.isEmpty()) return;

        addUserMessage(text);
        etInput.setText("");
        messageHistory.add(text);

        handler.postDelayed(() -> {
            String reply = generateReply(text);
            addDoctorMessage(reply);
        }, 1000);
    }

    private String generateReply(String userMessage) {
        if (userMessage.contains("呕吐") || userMessage.contains("吐")) {
            return "呕吐是比较常见的症状。请问：\n1. 呕吐频率是怎样的？一天吐几次？\n2. 呕吐物是什么样的？有没有带血或黄色胆汁？\n3. 最近有没有换粮或吃了特殊的东西？\n4. 精神状态和食欲怎么样？";
        }
        if (userMessage.contains("拉稀") || userMessage.contains("拉肚子") || userMessage.contains("腹泻")) {
            return "了解了，请问：\n1. 拉稀几天了？一天几次？\n2. 大便是什么样的？水样还是软便？有没有带血或粘液？\n3. 最近饮食有没有变化？\n4. 有没有呕吐、发烧等其他症状？";
        }
        if (userMessage.contains("皮肤") || userMessage.contains("掉毛") || userMessage.contains("痒")) {
            return "皮肤问题需要了解更多信息：\n1. 症状出现多久了？\n2. 主要在哪些部位？\n3. 有没有红疹、结痂、皮屑？\n4. 最近有没有洗澡或接触过什么特殊的东西？";
        }
        if (userMessage.contains("谢谢") || userMessage.contains("感谢")) {
            return "不客气！如果后续有其他问题，随时可以再来咨询。祝您的宠物早日康复！";
        }
        return "好的，我了解了。为了更准确地判断病情，请您告诉我：\n1. 症状出现多久了？\n2. 宠物的精神状态和食欲怎么样？\n3. 有没有其他伴随症状？\n\n您也可以上传照片或视频，我来帮您看看。";
    }

    private void addSystemMessage(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12);
        tv.setTextColor(Color.parseColor("#999999"));
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setPadding(dp2px(12), dp2px(6), dp2px(12), dp2px(6));
        tv.setBackgroundColor(Color.parseColor("#F0F0F0"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        params.topMargin = dp2px(8);
        params.bottomMargin = dp2px(8);
        layoutChatContent.addView(tv, params);
        scrollToBottom();
    }

    private void addDoctorMessage(String text) {
        LinearLayout msgLayout = new LinearLayout(this);
        msgLayout.setOrientation(LinearLayout.HORIZONTAL);
        msgLayout.setPadding(0, dp2px(8), 0, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        msgLayout.setLayoutParams(params);

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(doctorAvatar);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setClipToOutline(true);
        avatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp2px(36), dp2px(36));
        msgLayout.addView(avatar, avatarParams);

        LinearLayout bubbleLayout = new LinearLayout(this);
        bubbleLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        bubbleParams.leftMargin = dp2px(8);
        bubbleParams.rightMargin = dp2px(48);
        msgLayout.addView(bubbleLayout, bubbleParams);

        TextView nameTv = new TextView(this);
        nameTv.setText(doctorName);
        nameTv.setTextSize(11);
        nameTv.setTextColor(Color.parseColor("#999999"));
        nameTv.setPadding(0, 0, 0, dp2px(4));
        bubbleLayout.addView(nameTv);

        TextView contentTv = new TextView(this);
        contentTv.setText(text);
        contentTv.setTextSize(14);
        contentTv.setTextColor(Color.parseColor("#333333"));
        contentTv.setLineSpacing(dp2px(4), 1);
        contentTv.setPadding(dp2px(12), dp2px(10), dp2px(12), dp2px(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(dp2px(12));
        contentTv.setBackground(bg);
        bubbleLayout.addView(contentTv);

        layoutChatContent.addView(msgLayout);
        scrollToBottom();
    }

    private void addUserMessage(String text) {
        LinearLayout msgLayout = new LinearLayout(this);
        msgLayout.setOrientation(LinearLayout.HORIZONTAL);
        msgLayout.setGravity(android.view.Gravity.END);
        msgLayout.setPadding(0, dp2px(8), 0, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        msgLayout.setLayoutParams(params);

        LinearLayout bubbleLayout = new LinearLayout(this);
        bubbleLayout.setOrientation(LinearLayout.VERTICAL);
        bubbleLayout.setGravity(android.view.Gravity.END);
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        bubbleParams.leftMargin = dp2px(48);
        bubbleParams.rightMargin = dp2px(8);
        msgLayout.addView(bubbleLayout, bubbleParams);

        TextView contentTv = new TextView(this);
        contentTv.setText(text);
        contentTv.setTextSize(14);
        contentTv.setTextColor(Color.WHITE);
        contentTv.setLineSpacing(dp2px(4), 1);
        contentTv.setPadding(dp2px(12), dp2px(10), dp2px(12), dp2px(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#FFD93D"));
        bg.setCornerRadius(dp2px(12));
        contentTv.setBackground(bg);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        contentParams.gravity = android.view.Gravity.END;
        bubbleLayout.addView(contentTv, contentParams);

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(R.drawable.ic_cat_logo);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setClipToOutline(true);
        avatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp2px(36), dp2px(36));
        msgLayout.addView(avatar, avatarParams);

        layoutChatContent.addView(msgLayout);
        scrollToBottom();
    }

    private void scrollToBottom() {
        handler.postDelayed(() -> scrollChat.fullScroll(ScrollView.FOCUS_DOWN), 100);
    }

    private void showNursingPlan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("居家护理方案");

        String nursingContent = "【饮食护理】\n" +
                "• 暂时禁食6-12小时，少量多次饮水\n" +
                "• 恢复饮食后，先喂少量易消化的食物\n" +
                "• 可适当添加益生菌调理肠胃\n\n" +
                "【环境护理】\n" +
                "• 保持环境安静、温暖、舒适\n" +
                "• 保证充足的清洁饮水\n" +
                "• 定期清洁食盆和水盆\n\n" +
                "【观察要点】\n" +
                "• 密切观察精神状态和食欲变化\n" +
                "• 记录呕吐/腹泻的次数和性状\n" +
                "• 如症状加重或持续超过24小时，请及时就医";

        TextView tv = new TextView(this);
        tv.setText(nursingContent);
        tv.setTextSize(14);
        tv.setTextColor(Color.parseColor("#333333"));
        tv.setLineSpacing(dp2px(4), 1);
        tv.setPadding(dp2px(20), dp2px(16), dp2px(20), dp2px(16));
        builder.setView(tv);

        builder.setPositiveButton("我知道了", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEndConsultDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("确认结束问诊");
        builder.setMessage("确定要结束本次问诊吗？结束后医生将为您开具电子处方和护理方案。");
        builder.setPositiveButton("确认结束", (dialog, which) -> {
            dialog.dismiss();
            addSystemMessage("问诊已结束，感谢您的咨询！");
            addDoctorMessage("问诊已结束。我已为您的宠物开具了电子处方和护理方案，请在「处方」中查看。\n\n如有其他问题，欢迎随时咨询。祝您的宠物早日康复！");
            Intent intent = new Intent(this, ConsultEvaluateActivity.class);
            intent.putExtra("doctor_name", doctorName);
            intent.putExtra("department", department);
            startActivity(intent);
        });
        builder.setNegativeButton("继续问诊", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

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

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

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

    private File createTempImageFile() throws IOException {
        String fileName = "consult_" + System.currentTimeMillis() + ".jpg";
        File dir = new File(getExternalCacheDir(), "consult_images");
        if (!dir.exists())
            dir.mkdirs();
        return new File(dir, fileName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PICK_IMAGE) {
            Uri imageUri;
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
            } else {
                imageUri = cameraImageUri;
            }
            if (imageUri != null) {
                sendImageMessage(imageUri);
            }
        }
    }

    private void sendImageMessage(Uri imageUri) {
        addUserImageMessage(imageUri);
        handler.postDelayed(() -> {
            addDoctorMessage("收到您发送的图片了。从图片来看，宠物的症状需要进一步了解：\n\n1. 这个症状出现多久了？\n2. 宠物有没有抓挠或舔舐？\n3. 最近有没有接触过其他宠物？\n\n您可以再详细描述一下，我来帮您分析。");
        }, 1500);
    }

    private void addUserImageMessage(Uri imageUri) {
        LinearLayout msgLayout = new LinearLayout(this);
        msgLayout.setOrientation(LinearLayout.HORIZONTAL);
        msgLayout.setGravity(android.view.Gravity.END);
        msgLayout.setPadding(0, dp2px(8), 0, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        msgLayout.setLayoutParams(params);

        LinearLayout bubbleLayout = new LinearLayout(this);
        bubbleLayout.setOrientation(LinearLayout.VERTICAL);
        bubbleLayout.setGravity(android.view.Gravity.END);
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        bubbleParams.leftMargin = dp2px(48);
        bubbleParams.rightMargin = dp2px(8);
        msgLayout.addView(bubbleLayout, bubbleParams);

        ImageView imageView = new ImageView(this);
        imageView.setImageURI(imageUri);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setAdjustViewBounds(true);
        imageView.setMaxWidth(dp2px(200));
        imageView.setMaxHeight(dp2px(200));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#FFD93D"));
        bg.setCornerRadius(dp2px(12));
        imageView.setBackground(bg);
        imageView.setPadding(dp2px(4), dp2px(4), dp2px(4), dp2px(4));
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        imageParams.gravity = android.view.Gravity.END;
        bubbleLayout.addView(imageView, imageParams);

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(R.drawable.ic_cat_logo);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setClipToOutline(true);
        avatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp2px(36), dp2px(36));
        msgLayout.addView(avatar, avatarParams);

        layoutChatContent.addView(msgLayout);
        scrollToBottom();
    }
}
