package com.example.love_pet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FeedbackSuggestionActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_CODE_TAKE_PHOTO = 1002;
    private static final int REQUEST_CODE_PERMISSION = 1003;

    private EditText etContent;
    private EditText etContact;
    private TextView tvWordCount;
    private TextView btnSubmit;
    private LinearLayout layoutAddImage;
    private ImageView ivPreview;

    private Uri cameraImageUri;
    private String selectedImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_suggestion);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        etContent = findViewById(R.id.et_content);
        etContact = findViewById(R.id.et_contact);
        tvWordCount = findViewById(R.id.tv_word_count);
        btnSubmit = findViewById(R.id.btn_submit);
        layoutAddImage = findViewById(R.id.layout_add_image);
        ivPreview = findViewById(R.id.iv_preview);

        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                tvWordCount.setText(length + "/100");
                updateSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        layoutAddImage.setOnClickListener(v -> {
            checkPermissionAndShowPicker();
        });

        ivPreview.setOnClickListener(v -> {
            showImageOptions();
        });

        btnSubmit.setOnClickListener(v -> {
            if (etContent.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "请填写问题和意见", Toast.LENGTH_SHORT).show();
                return;
            }
            submitFeedback();
        });
    }

    private void checkPermissionAndShowPicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSION);
            return;
        }
        showPickerDialog();
    }

    private void showPickerDialog() {
        String[] items = {"从相册选择", "拍摄照片"};
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

    private void showImageOptions() {
        String[] items = {"查看大图", "删除图片"};
        new AlertDialog.Builder(this)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        Toast.makeText(this, "查看大图", Toast.LENGTH_SHORT).show();
                    } else {
                        selectedImagePath = null;
                        cameraImageUri = null;
                        ivPreview.setVisibility(View.GONE);
                        layoutAddImage.setVisibility(View.VISIBLE);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_PERMISSION);
            return;
        }
        try {
            File imageFile = createImageFile();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cameraImageUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", imageFile);
            } else {
                cameraImageUri = Uri.fromFile(imageFile);
            }
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "无法启动相机", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = new File(getFilesDir(), "feedback_images");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        return File.createTempFile("feedback_" + timeStamp, ".jpg", storageDir);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showPickerDialog();
            } else {
                Toast.makeText(this, "需要权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_IMAGE && data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    selectedImagePath = imageUri.toString();
                    showImagePreview(imageUri);
                }
            } else if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
                if (cameraImageUri != null) {
                    selectedImagePath = cameraImageUri.toString();
                    showImagePreview(cameraImageUri);
                }
            }
        }
    }

    private void showImagePreview(Uri imageUri) {
        layoutAddImage.setVisibility(View.GONE);
        ivPreview.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .into(ivPreview);
    }

    private void updateSubmitButton() {
        String content = etContent.getText().toString().trim();
        if (!content.isEmpty()) {
            btnSubmit.setBackgroundColor(0xFFFFD93D);
            btnSubmit.setTextColor(0xFF333333);
            btnSubmit.setEnabled(true);
        } else {
            btnSubmit.setBackgroundColor(0xFFE0E0E0);
            btnSubmit.setTextColor(0xFF999999);
            btnSubmit.setEnabled(false);
        }
    }

    private void submitFeedback() {
        Toast.makeText(this, "提交成功，感谢您的反馈！", Toast.LENGTH_SHORT).show();
        finish();
    }
}
