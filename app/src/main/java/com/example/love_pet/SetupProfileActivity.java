package com.example.love_pet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SetupProfileActivity extends AppCompatActivity {

    public static final String EXTRA_PHONE = "extra_phone";
    public static final String EXTRA_PASSWORD = "extra_password";

    private ImageView ivAvatar;
    private EditText etNickname;
    private TextView tvChangeAvatar;
    private TextView btnComplete;

    private String phone;
    private String password;
    private String avatarPath;

    private AvatarManager avatarManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_profile);

        phone = getIntent().getStringExtra(EXTRA_PHONE);
        password = getIntent().getStringExtra(EXTRA_PASSWORD);

        ivAvatar = findViewById(R.id.iv_avatar);
        etNickname = findViewById(R.id.et_nickname);
        tvChangeAvatar = findViewById(R.id.tv_change_avatar);
        btnComplete = findViewById(R.id.btn_complete);
        ImageView ivBack = findViewById(R.id.iv_back);

        avatarManager = new AvatarManager(this);
        avatarManager.setOnAvatarUploadListener(new AvatarManager.OnAvatarUploadListener() {
            @Override
            public void onUploadStart() {
            }

            @Override
            public void onUploadSuccess(String path) {
                avatarPath = path;
                AvatarManager.loadAvatar(SetupProfileActivity.this, ivAvatar);
            }

            @Override
            public void onUploadFailed(String error) {
                Toast.makeText(SetupProfileActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        ivBack.setOnClickListener(v -> finish());

        tvChangeAvatar.setOnClickListener(v -> avatarManager.showPickerDialog());

        btnComplete.setOnClickListener(v -> completeSetup());
    }

    private void completeSetup() {
        String nickname = etNickname.getText().toString().trim();
        if (nickname.isEmpty()) {
            Toast.makeText(this, "请输入昵称", Toast.LENGTH_SHORT).show();
            return;
        }
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "注册信息异常，请重新注册", Toast.LENGTH_SHORT).show();
            return;
        }

        UserManager.getInstance().loadUsers(this);
        boolean success = UserManager.getInstance().register(this, phone, password, nickname, "");
        if (success) {
            UserManager.User user = UserManager.getInstance().findUserByPhone(phone);
            UserManager.getInstance().setCurrentUser(this, user);

            if (avatarPath != null && !avatarPath.isEmpty()) {
                UserManager.getInstance().updateCurrentUserAvatar(this, avatarPath);
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "该手机号已注册", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        if (requestCode == AvatarManager.REQUEST_CODE_PICK_IMAGE && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                avatarManager.startCrop(imageUri);
            }
        } else if (requestCode == AvatarManager.REQUEST_CODE_TAKE_PHOTO) {
            if (avatarManager.getCameraImageUri() != null) {
                avatarManager.startCrop(avatarManager.getCameraImageUri());
            }
        } else if (requestCode == AvatarManager.REQUEST_CODE_CROP && data != null) {
            Uri resultUri = com.yalantis.ucrop.UCrop.getOutput(data);
            if (resultUri != null) {
                avatarManager.saveAvatarToLocal(resultUri);
            }
        }
    }
}
