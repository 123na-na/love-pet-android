package com.example.love_pet;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
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

/**
 * 头像管理器（纯本地存储）
 * 功能：选择图片 → 裁剪 → 保存本地 → 同步更新显示
 */
public class AvatarManager {

    private static final String PREFS_NAME = "avatar_prefs";
    private static final String KEY_AVATAR_PATH = "avatar_path";

    private static String getPrefKey(Context context) {
        return PREFS_NAME + "_" + UserManager.getInstance().getCurrentUserId();
    }

    private static String getAvatarFileName() {
        String userId = UserManager.getInstance().getCurrentUserId();
        return "avatar_" + userId + ".jpg";
    }

    // 请求码
    public static final int REQUEST_CODE_PICK_IMAGE = 1001;
    public static final int REQUEST_CODE_TAKE_PHOTO = 1002;
    public static final int REQUEST_CODE_CROP = 1003;
    public static final int REQUEST_CODE_PERMISSION = 1004;

    // 头像保存目录名
    private static final String AVATAR_DIR = "avatars";
    private static final String AVATAR_FILE = "my_avatar.jpg";

    private final Activity activity;
    private Uri cameraImageUri;  // 拍照临时文件URI
    private OnAvatarUploadListener listener;

    /**
     * 获取拍照图片的 URI（供外部 onActivityResult 使用）
     */
    public Uri getCameraImageUri() {
        return cameraImageUri;
    }

    public interface OnAvatarUploadListener {
        void onUploadStart();
        void onUploadSuccess(String avatarPath);
        void onUploadFailed(String error);
    }

    public AvatarManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * 设置监听器
     */
    public void setOnAvatarUploadListener(OnAvatarUploadListener listener) {
        this.listener = listener;
    }

    /**
     * 显示选择头像的弹窗（相册/拍照）
     */
    public void showPickerDialog() {
        String[] items = {"从相册选择", "拍摄照片"};
        new AlertDialog.Builder(activity)
                .setTitle("选择头像")
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
        activity.startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    /**
     * 拍照获取图片
     */
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

    /**
     * 创建临时文件
     */
    private File createTempFile() throws IOException {
        String fileName = "temp_" + UUID.randomUUID().toString() + ".jpg";
        File dir = new File(activity.getExternalCacheDir(), AVATAR_DIR);
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, fileName);
    }

    /**
     * 获取头像最终保存路径
     */
    private static File getAvatarFile(Context context) {
        File dir = new File(context.getFilesDir(), AVATAR_DIR);  // 应用私有目录
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, getAvatarFileName());
    }

    /**
     * 启动裁剪界面
     */
    public void startCrop(Uri sourceUri) {
        Uri destinationUri = Uri.fromFile(new File(activity.getExternalCacheDir(),
                "avatar_crop_" + System.currentTimeMillis() + ".jpg"));

        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)           // 1:1 正方形
                .withMaxResultSize(512, 512)      // 最大尺寸 512x512
                .withOptions(getCropOptions())
                .start(activity, REQUEST_CODE_CROP);
    }

    /**
     * 裁剪配置选项
     */
    private UCrop.Options getCropOptions() {
        UCrop.Options options = new UCrop.Options();
        options.setCompressionQuality(90);
        options.setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG);
        options.setHideBottomControls(false);
        options.setFreeStyleCropEnabled(false);
        return options;
    }

    /**
     * 保存裁剪后的头像到本地（核心方法：不依赖服务器）
     * @param imageUri 裁剪后的图片URI
     */
    public void saveAvatarToLocal(Uri imageUri) {
        if (listener != null) {
            listener.onUploadStart();
        }

        try {
            // 获取源文件
            File sourceFile = getFileFromUri(imageUri);

            // 目标文件（应用私有目录）
            File destFile = getAvatarFile(activity);
            File parentDir = destFile.getParentFile();
            if (!parentDir.exists()) parentDir.mkdirs();

            // 复制文件
            copyFile(sourceFile, destFile);

            // 保存路径到 SharedPreferences
            saveAvatarPath(destFile.getAbsolutePath());

            // 更新 UserManager 中当前用户的头像路径
            UserManager.getInstance().updateCurrentUserAvatar(activity, destFile.getAbsolutePath());

            // 回调主线程更新UI
            activity.runOnUiThread(() -> {
                // 清除 Glide 内存缓存，确保显示新头像（必须在主线程调用）
                Glide.get(activity).clearMemory();
                if (listener != null) {
                    listener.onUploadSuccess(destFile.getAbsolutePath());
                }
                Toast.makeText(activity, "头像设置成功", Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            e.printStackTrace();
            activity.runOnUiThread(() -> {
                if (listener != null) {
                    listener.onUploadFailed("保存失败：" + e.getMessage());
                }
                Toast.makeText(activity, "头像保存失败", Toast.LENGTH_SHORT).show();
            });
        }
    }

    /**
     * 复制文件
     */
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

    /**
     * 从 URI 获取 File 对象
     */
    private File getFileFromUri(Uri uri) throws Exception {
        if ("content".equals(uri.getScheme())) {
            // content URI → 转换为真实路径
            String path = getRealPathFromUri(uri);
            return new File(path);
        } else {
            // file URI
            return new File(uri.getPath());
        }
    }

    /**
     * 获取 URI 的真实文件路径
     */
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

    /**
     * 保存头像路径到 SharedPreferences
     */
    private void saveAvatarPath(String path) {
        activity.getSharedPreferences(getPrefKey(activity), Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_AVATAR_PATH, path)
                .apply();
    }

    /**
     * 获取已保存的头像路径
     */
    public static String getSavedAvatarPath(Context context) {
        if (UserManager.getInstance().isLoggedIn() && UserManager.getInstance().getCurrentAvatarPath() != null) {
            return UserManager.getInstance().getCurrentAvatarPath();
        }
        return context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE)
                .getString(KEY_AVATAR_PATH, null);
    }

    /**
     * 加载并显示头像（使用 Glide 加载本地文件）
     * @param imageView 头像 ImageView
     */
    public static void loadAvatar(Context context, ImageView imageView) {
        String avatarPath = getSavedAvatarPath(context);
        if (avatarPath != null && !avatarPath.isEmpty()) {
            File file = new File(avatarPath);
            if (file.exists()) {
                Glide.with(context)
                        .load(file)
                        .circleCrop()
                        .placeholder(R.drawable.nav_profile_normal)
                        .error(R.drawable.nav_profile_normal)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.nav_profile_normal);
            }
        } else {
            imageView.setImageResource(R.drawable.nav_profile_normal);
        }
    }

    /**
     * 清除本地头像
     */
    public static void clearAvatarCache(Context context) {
        // 删除文件
        File file = getAvatarFile(context);
        if (file.exists()) file.delete();

        // 清除 SharedPreferences
        context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_AVATAR_PATH)
                .apply();

        // 清除 Glide 内存缓存
        Glide.get(context).clearMemory();
    }

    /**
     * 需要申请的权限数组
     */
    public static String[] getRequiredPermissions() {
        // Android 13 (API 33) 以上只需要相机权限
        if (Build.VERSION.SDK_INT >= 33) {
            return new String[]{Manifest.permission.CAMERA};
        } else {
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
    }
}
