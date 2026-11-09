package com.example.love_pet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AddPetActivity extends AppCompatActivity {

    private ImageView ivBack;
    private ImageView ivPetAvatar;
    private EditText etPetName;
    private LinearLayout btnTypeCat;
    private LinearLayout btnTypeDog;
    private Spinner spinnerBreed;
    private LinearLayout btnGenderMale;
    private LinearLayout btnGenderFemale;
    private EditText etDeviceName;
    private TextView btnSave;

    private String petType = "cat";
    private String gender = "male";
    private List<String> catBreeds = Arrays.asList("英国短毛猫", "美国短毛猫", "布偶猫", "暹罗猫", "波斯猫", "橘猫", "狸花猫", "其他");
    private List<String> dogBreeds = Arrays.asList("金毛", "拉布拉多", "泰迪", "柯基", "哈士奇", "柴犬", "边牧", "其他");

    private Uri avatarTempUri;
    private static final int REQUEST_CODE_PERMISSION = 2001;
    private static final int REQUEST_CODE_PICK_IMAGE = 2002;
    private static final int REQUEST_CODE_TAKE_PHOTO = 2003;
    private static final int REQUEST_CODE_CROP = 2004;
    private Uri cameraImageUri;

    public static final String EXTRA_PET_ID = "pet_id";
    private String editPetId;
    private boolean isEditMode = false;
    private PetManager.Pet existingPet;
    private String existingAvatarPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pet);

        editPetId = getIntent().getStringExtra(EXTRA_PET_ID);
        isEditMode = editPetId != null && !editPetId.isEmpty();

        initViews();
        setupListeners();
        updateBreedSpinner();

        if (isEditMode) {
            loadPetData();
        }
    }

    private void loadPetData() {
        existingPet = PetManager.getInstance().getPetById(this, editPetId);
        if (existingPet == null) {
            Toast.makeText(this, "宠物不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("编辑宠物");

        TextView btnDelete = findViewById(R.id.btn_delete_pet);
        if (btnDelete != null) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("确认删除")
                        .setMessage("确定要删除这只宠物吗？相关的记录也会被清除。")
                        .setPositiveButton("删除", (dialog, which) -> {
                            PetManager.getInstance().deletePet(this, existingPet.id);
                            Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .setNegativeButton("取消", null)
                        .show();
            });
        }

        petType = existingPet.type;
        gender = existingPet.gender;
        existingAvatarPath = existingPet.avatarPath;

        etPetName.setText(existingPet.name);
        etDeviceName.setText(existingPet.deviceName != null ? existingPet.deviceName : "");

        if (existingAvatarPath != null && !existingAvatarPath.isEmpty()) {
            java.io.File avatarFile = new java.io.File(existingAvatarPath);
            if (avatarFile.exists()) {
                Glide.with(this)
                        .load(avatarFile)
                        .circleCrop()
                        .into(ivPetAvatar);
            }
        }

        updateTypeButtons();
        updateGenderButtons();
        updateBreedSpinner();

        if (existingPet.breed != null && !existingPet.breed.isEmpty()) {
            List<String> breeds = "cat".equals(petType) ? catBreeds : dogBreeds;
            int index = breeds.indexOf(existingPet.breed);
            if (index >= 0) {
                spinnerBreed.setSelection(index);
            }
        }
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        ivPetAvatar = findViewById(R.id.iv_pet_avatar);
        etPetName = findViewById(R.id.et_pet_name);
        btnTypeCat = findViewById(R.id.btn_type_cat);
        btnTypeDog = findViewById(R.id.btn_type_dog);
        spinnerBreed = findViewById(R.id.spinner_breed);
        btnGenderMale = findViewById(R.id.btn_gender_male);
        btnGenderFemale = findViewById(R.id.btn_gender_female);
        etDeviceName = findViewById(R.id.et_device_name);
        btnSave = findViewById(R.id.btn_save);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivPetAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndPick();
            }
        });

        btnTypeCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                petType = "cat";
                updateTypeButtons();
                updateBreedSpinner();
            }
        });

        btnTypeDog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                petType = "dog";
                updateTypeButtons();
                updateBreedSpinner();
            }
        });

        btnGenderMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "male";
                updateGenderButtons();
            }
        });

        btnGenderFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "female";
                updateGenderButtons();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePet();
            }
        });
    }

    private void checkPermissionAndPick() {
        String[] permissions = PetAvatarManager.getRequiredPermissions();
        boolean allGranted = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (allGranted) {
            showAvatarPicker();
        } else {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION);
        }
    }

    private void showAvatarPicker() {
        String[] items = {"从相册选择", "拍摄照片"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("选择宠物头像")
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
            File photoFile = createTempFile();
            cameraImageUri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile
            );

            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, cameraImageUri);
            startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
        } catch (Exception e) {
            Toast.makeText(this, "无法创建图片文件", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempFile() throws Exception {
        String fileName = "pet_temp_" + UUID.randomUUID().toString() + ".jpg";
        File dir = new File(getExternalCacheDir(), "pet_avatars");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, fileName);
    }

    private void startCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(getExternalCacheDir(),
                "pet_crop_" + System.currentTimeMillis() + ".jpg"));

        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(512, 512)
                .start(this, REQUEST_CODE_CROP);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                showAvatarPicker();
            } else {
                Toast.makeText(this, "需要相机和存储权限才能选择头像", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CODE_PICK_IMAGE && data != null && data.getData() != null) {
            startCrop(data.getData());
        } else if (requestCode == REQUEST_CODE_TAKE_PHOTO && cameraImageUri != null) {
            startCrop(cameraImageUri);
        } else if (requestCode == REQUEST_CODE_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                avatarTempUri = resultUri;
                Glide.with(this)
                        .load(resultUri)
                        .circleCrop()
                        .into(ivPetAvatar);
            }
        }
    }

    private void updateTypeButtons() {
        if ("cat".equals(petType)) {
            btnTypeCat.setBackgroundResource(R.drawable.bg_type_selected);
            btnTypeDog.setBackgroundResource(R.drawable.bg_type_normal);
            TextView catText = (TextView) btnTypeCat.getChildAt(0);
            TextView dogText = (TextView) btnTypeDog.getChildAt(0);
            catText.setTextColor(0xFFFFFFFF);
            dogText.setTextColor(0xFF666666);
        } else {
            btnTypeDog.setBackgroundResource(R.drawable.bg_type_selected);
            btnTypeCat.setBackgroundResource(R.drawable.bg_type_normal);
            TextView catText = (TextView) btnTypeCat.getChildAt(0);
            TextView dogText = (TextView) btnTypeDog.getChildAt(0);
            dogText.setTextColor(0xFFFFFFFF);
            catText.setTextColor(0xFF666666);
        }
    }

    private void updateGenderButtons() {
        if ("male".equals(gender)) {
            btnGenderMale.setBackgroundResource(R.drawable.bg_type_selected);
            btnGenderFemale.setBackgroundResource(R.drawable.bg_type_normal);
            TextView maleText = (TextView) btnGenderMale.getChildAt(0);
            TextView femaleText = (TextView) btnGenderFemale.getChildAt(0);
            maleText.setTextColor(0xFFFFFFFF);
            femaleText.setTextColor(0xFF666666);
        } else {
            btnGenderFemale.setBackgroundResource(R.drawable.bg_type_selected);
            btnGenderMale.setBackgroundResource(R.drawable.bg_type_normal);
            TextView maleText = (TextView) btnGenderMale.getChildAt(0);
            TextView femaleText = (TextView) btnGenderFemale.getChildAt(0);
            femaleText.setTextColor(0xFFFFFFFF);
            maleText.setTextColor(0xFF666666);
        }
    }

    private void updateBreedSpinner() {
        List<String> breeds = "cat".equals(petType) ? catBreeds : dogBreeds;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, breeds);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBreed.setAdapter(adapter);
    }

    private void savePet() {
        String name = etPetName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "请输入宠物名称", Toast.LENGTH_SHORT).show();
            return;
        }

        String breed = spinnerBreed.getSelectedItem().toString();

        PetManager.Pet pet;
        if (isEditMode && existingPet != null) {
            pet = existingPet;
        } else {
            pet = new PetManager.Pet();
            pet.id = "pet_" + System.currentTimeMillis();
            pet.tags = new String[]{"可爱", "活泼"};
        }
        pet.name = name;
        pet.type = petType;
        pet.breed = breed;
        pet.gender = gender;
        pet.deviceName = etDeviceName.getText().toString().trim();

        if (avatarTempUri != null) {
            try {
                File sourceFile = getFileFromUri(avatarTempUri);
                if (sourceFile != null && sourceFile.exists()) {
                    File avatarDir = new File(getFilesDir(), "pet_avatars");
                    if (!avatarDir.exists()) avatarDir.mkdirs();
                    File destFile = new File(avatarDir, pet.id + ".jpg");
                    copyFile(sourceFile, destFile);
                    pet.avatarPath = destFile.getAbsolutePath();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (isEditMode) {
            PetManager.getInstance().updatePet(this, pet);
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        } else {
            PetManager.getInstance().addPet(this, pet);
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private File getFileFromUri(Uri uri) throws Exception {
        if ("content".equals(uri.getScheme())) {
            String path = getRealPathFromUri(uri);
            if (path != null && !path.isEmpty()) {
                return new File(path);
            }
            return null;
        } else {
            return new File(uri.getPath());
        }
    }

    private String getRealPathFromUri(Uri uri) {
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try {
                String[] proj = {android.provider.MediaStore.Images.Media.DATA};
                android.database.Cursor cursor = getContentResolver()
                        .query(uri, proj, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(column_index);
                    cursor.close();
                    return path;
                }
            } catch (Exception ignored) {}
            return uri.getPath();
        }
        return uri.getPath();
    }

    private void copyFile(File src, File dest) throws java.io.IOException {
        java.io.FileInputStream fis = new java.io.FileInputStream(src);
        java.io.FileOutputStream fos = new java.io.FileOutputStream(dest);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
        }
        fos.flush();
        fos.close();
        fis.close();
    }
}
