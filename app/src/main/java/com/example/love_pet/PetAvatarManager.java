package com.example.love_pet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PetAvatarManager {

    public static final int REQUEST_CODE_PICK_IMAGE = 1001;
    public static final int REQUEST_CODE_TAKE_PHOTO = 1002;
    public static final int REQUEST_CODE_CROP = 1003;

    private static final String PET_AVATAR_DIR = "pet_avatars";

    private final Activity activity;
    private Uri cameraImageUri;
    private OnPetAvatarListener listener;

    public interface OnPetAvatarListener {
        void onAvatarSelected(String avatarPath);
    }

    public PetAvatarManager(Activity activity) {
        this.activity = activity;
    }

    public void setOnPetAvatarListener(OnPetAvatarListener listener) {
        this.listener = listener;
    }

    public Uri getCameraImageUri() {
        return cameraImageUri;
    }

    public void showPickerDialog() {
        String[] items = {"从相册选择", "拍摄照片"};
        new AlertDialog.Builder(activity)
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
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void takePhoto() {
        try {
            File photoFile = createTempFile();
            cameraImageUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".fileprovider",
                    photoFile
            );

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            activity.startActivityForResult(intent, REQUEST_CODE_TAKE_PHOTO);
        } catch (IOException e) {
            Toast.makeText(activity, "无法创建图片文件", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempFile() throws IOException {
        String fileName = "pet_temp_" + UUID.randomUUID().toString() + ".jpg";
        File dir = new File(activity.getExternalCacheDir(), PET_AVATAR_DIR);
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, fileName);
    }

    public void startCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(activity.getExternalCacheDir(),
                "pet_crop_" + System.currentTimeMillis() + ".jpg"));

        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(512, 512)
                .withOptions(getCropOptions())
                .start(activity, REQUEST_CODE_CROP);
    }

    private UCrop.Options getCropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(90);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(false);
        return options;
    }

    public String savePetAvatar(Uri imageUri, int petId) {
        try {
            File sourceFile = getFileFromUri(imageUri);
            File destFile = getPetAvatarFile(activity, petId);
            File parentDir = destFile.getParentFile();
            if (!parentDir.exists()) parentDir.mkdirs();

            copyFile(sourceFile, destFile);
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getPetAvatarFile(Context context, int petId) {
        File dir = new File(context.getFilesDir(), PET_AVATAR_DIR);
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "pet_" + petId + ".jpg");
    }

    public static void loadPetAvatar(Context context, ImageView imageView, String avatarPath, int defaultRes) {
        if (avatarPath != null && !avatarPath.isEmpty()) {
            File file = new File(avatarPath);
            if (file.exists()) {
                Glide.with(context)
                        .load(file)
                        .circleCrop()
                        .into(imageView);
                return;
            }
        }
        imageView.setImageResource(defaultRes);
    }

    private void copyFile(File src, File dest) throws IOException {
        FileInputStream fis = new FileInputStream(src);
        FileOutputStream fos = new FileOutputStream(dest);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
        }
        fos.flush();
        fos.close();
        fis.close();
    }

    private File getFileFromUri(Uri uri) throws Exception {
        if ("content".equals(uri.getScheme())) {
            String path = getRealPathFromUri(uri);
            return new File(path);
        } else {
            return new File(uri.getPath());
        }
    }

    private String getRealPathFromUri(Uri uri) {
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try {
                String[] proj = {MediaStore.Images.Media.DATA};
                android.database.Cursor cursor = activity.getContentResolver()
                        .query(uri, proj, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
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

    public static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            return new String[]{android.Manifest.permission.CAMERA};
        } else {
            return new String[]{
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
    }
}
