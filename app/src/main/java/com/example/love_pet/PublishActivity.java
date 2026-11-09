package com.example.love_pet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.bumptech.glide.Glide;

public class PublishActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1002;
    private static final int REQUEST_CODE_LOCATION_PICKER = 1003;
    private static final int MAX_IMAGES = 9;

    private List<Uri> selectedImages = new ArrayList<>();
    private ImageAdapter imageAdapter;
    private EditText etTitle;
    private EditText etContent;
    private TextView tvCurrentLocation;
    private List<String> selectedTopics = new ArrayList<>();

    // 定位相关
    private LocationManager locationManager;
    private String currentLocationName = "";
    private double currentLat = 0;
    private double currentLng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        initViews();
        initImageList();
        initClickListeners();

        // 初始化定位
        initLocationManager();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_title);
        etContent = findViewById(R.id.et_content);
        tvCurrentLocation = findViewById(R.id.tv_current_location);

        setupTopicHighlighter();
        initHotLocationTags();
    }

    private void setupTopicHighlighter() {
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                highlightTopics(s);
            }
        });
    }

    private void highlightTopics(Editable editable) {
        String text = editable.toString();
        ForegroundColorSpan[] spans = editable.getSpans(0, editable.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan span : spans) {
            editable.removeSpan(span);
        }

        int topicColor = 0xFF4A90E2;
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '#') {
                int start = i;
                i++;
                while (i < text.length() && text.charAt(i) != ' ' && text.charAt(i) != '\n') {
                    i++;
                }
                int end = i;
                if (end - start > 1) {
                    editable.setSpan(new ForegroundColorSpan(topicColor),
                            start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            } else {
                i++;
            }
        }
    }
    
    private void initHotLocationTags() {
        TextView tvLocation1 = findViewById(R.id.tv_location_1);
        TextView tvLocation2 = findViewById(R.id.tv_location_2);
        TextView tvLocation3 = findViewById(R.id.tv_location_3);
        TextView tvLocation4 = findViewById(R.id.tv_location_4);
        
        View.OnClickListener locationTagClickListener = v -> {
            TextView tagView = (TextView) v;
            String locationName = tagView.getText().toString();
            currentLocationName = locationName;
            tvCurrentLocation.setText(locationName);
            tvCurrentLocation.setVisibility(View.VISIBLE);
            showToast("已选择地点: " + locationName);
        };
        
        tvLocation1.setOnClickListener(locationTagClickListener);
        tvLocation2.setOnClickListener(locationTagClickListener);
        tvLocation3.setOnClickListener(locationTagClickListener);
        tvLocation4.setOnClickListener(locationTagClickListener);
    }

    private void initImageList() {
        RecyclerView rvImages = findViewById(R.id.rv_images);
        rvImages.setLayoutManager(new GridLayoutManager(this, 3));
        imageAdapter = new ImageAdapter(selectedImages, this);
        imageAdapter.setOnAddImageListener(this::pickImage);
        imageAdapter.setOnImageRemoveListener(position -> {
            selectedImages.remove(position);
            imageAdapter.notifyDataSetChanged();
        });
        rvImages.setAdapter(imageAdapter);
    }

    /**
     * 初始化定位管理器
     */
    private void initLocationManager() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // 尝试获取上次已知位置
        if (checkLocationPermission()) {
            getLastKnownLocation();
        }
    }

    /**
     * 检查定位权限
     */
    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 获取最后一次已知位置
     */
    private void getLastKnownLocation() {
        try {
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location bestLocation = null;
            if (gpsLocation != null && networkLocation != null) {
                bestLocation = gpsLocation.getTime() > networkLocation.getTime() ? gpsLocation : networkLocation;
            } else if (gpsLocation != null) {
                bestLocation = gpsLocation;
            } else {
                bestLocation = networkLocation;
            }

            if (bestLocation != null) {
                currentLat = bestLocation.getLatitude();
                currentLng = bestLocation.getLongitude();
                resolveAddress(currentLat, currentLng);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始实时定位
     */
    private void startRealTimeLocation() {
        if (!checkLocationPermission()) {
            // 显示权限说明对话框
            showLocationPermissionRationale();
            return;
        }

        try {
            // 先尝试GPS定位
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,       // 最小更新间隔5秒
                        10,         // 最小移动距离10米
                        locationListener
                );
            }

            // 同时使用网络定位（更快）
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        3000,       // 最小更新间隔3秒
                        10,         // 最小移动距离10米
                        locationListener
                );
            }

            showToast("正在定位中...");
        } catch (SecurityException e) {
            e.printStackTrace();
            showToast("定位权限不足");
        }
    }

    /**
     * 显示权限说明对话框
     */
    private void showLocationPermissionRationale() {
        new AlertDialog.Builder(this)
                .setTitle("需要定位权限")
                .setMessage("为了标记您发布内容的位置信息，需要获取您的定位权限。")
                .setPositiveButton("允许", (dialog, which) -> {
                    // 用户点击允许，请求系统权限
                    ActivityCompat.requestPermissions(PublishActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 定位监听器
     */
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLat = location.getLatitude();
            currentLng = location.getLongitude();
            resolveAddress(currentLat, currentLng);

            // 获取到位置后停止更新，节省电量
            stopLocationUpdates();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    /**
     * 停止定位更新
     */
    private void stopLocationUpdates() {
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(locationListener);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * 经纬度转地址名称（地理编码）
     */
    private void resolveAddress(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                StringBuilder sb = new StringBuilder();

                // 获取地址信息
                String thoroughfare = address.getThoroughfare();   // 街道
                String subLocality = address.getSubLocality();     // 区域/区县
                String locality = address.getLocality();           // 城市

                if (subLocality != null && !subLocality.isEmpty()) {
                    sb.append(subLocality);
                }
                if (thoroughfare != null && !thoroughfare.isEmpty()) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(thoroughfare);
                }
                if (sb.length() == 0 && locality != null) {
                    sb.append(locality);
                }

                currentLocationName = sb.toString().trim();
                if (currentLocationName.isEmpty()) {
                    currentLocationName = address.getAddressLine(0);
                }

                runOnUiThread(() -> {
                    tvCurrentLocation.setText(currentLocationName);
                    tvCurrentLocation.setVisibility(View.VISIBLE);
                });
            } else {
                runOnUiThread(() -> {
                    tvCurrentLocation.setText(String.format("%.4f, %.4f", lat, lng));
                    tvCurrentLocation.setVisibility(View.VISIBLE);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                tvCurrentLocation.setText(String.format("%.4f, %.4f", lat, lng));
                tvCurrentLocation.setVisibility(View.VISIBLE);
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限被授予，开始定位
                startRealTimeLocationWithRetry();
            } else {
                // 权限被拒绝
                showToast("已拒绝定位权限，将使用默认地点");
            }
        }
    }

    /**
     * 带重试机制的定位方法
     */
    private void startRealTimeLocationWithRetry() {
        // 检查定位服务是否开启
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!gpsEnabled && !networkEnabled) {
            // 定位服务未开启，提示用户
            showLocationServiceDialog();
            return;
        }

        showToast("正在定位中...");

        try {
            // 先尝试获取上次已知位置（更快）
            Location lastKnownLocation = getLastKnownLocationSafely();
            if (lastKnownLocation != null) {
                currentLat = lastKnownLocation.getLatitude();
                currentLng = lastKnownLocation.getLongitude();
                resolveAddress(currentLat, currentLng);
                // 同时继续监听实时位置更新
            }

            // 请求实时位置更新
            if (gpsEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        5000,
                        10,
                        locationListenerWithTimeout
                );
            }

            if (networkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        3000,
                        10,
                        locationListenerWithTimeout
                );
            }

            // 设置超时机制
            locationTimeoutHandler.postDelayed(() -> {
                stopLocationUpdates();
                if (currentLocationName.isEmpty()) {
                    showToast("定位超时，将使用默认地点");
                }
            }, 10000); // 10秒超时

        } catch (SecurityException e) {
            e.printStackTrace();
            showToast("定位权限不足");
        }
    }

    /**
     * 安全获取上次已知位置
     */
    private Location getLastKnownLocationSafely() {
        try {
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (gpsLocation != null && networkLocation != null) {
                return gpsLocation.getTime() > networkLocation.getTime() ? gpsLocation : networkLocation;
            }
            return gpsLocation != null ? gpsLocation : networkLocation;
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 显示定位服务未开启的提示对话框
     */
    private void showLocationServiceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("定位服务未开启")
                .setMessage("请先在系统设置中开启定位服务")
                .setPositiveButton("去设置", (dialog, which) -> {
                    // 打开系统定位设置
                    Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 带超时处理的定位监听器
    private final LocationListener locationListenerWithTimeout = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // 取消超时任务
            locationTimeoutHandler.removeCallbacksAndMessages(null);

            currentLat = location.getLatitude();
            currentLng = location.getLongitude();
            resolveAddress(currentLat, currentLng);

            stopLocationUpdates();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    // 超时处理器
    private final android.os.Handler locationTimeoutHandler = new android.os.Handler();

    private void initClickListeners() {
        // 返回按钮
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        // 话题标签
        TextView tvTopic = findViewById(R.id.tv_topic);
        tvTopic.setOnClickListener(v -> showTopicDialog());

        // @用户
        TextView tvMention = findViewById(R.id.tv_mention);
        tvMention.setOnClickListener(v -> showToast("选择@用户"));

        // 投票
        TextView tvVote = findViewById(R.id.tv_vote);
        tvVote.setOnClickListener(v -> showToast("创建投票"));

        // 标记地点 - 跳转到地点选择页面
        LinearLayout layoutLocation = findViewById(R.id.layout_location);
        layoutLocation.setOnClickListener(v -> openLocationPicker());

        // 公开可见
        LinearLayout layoutVisibility = findViewById(R.id.layout_visibility);
        layoutVisibility.setOnClickListener(v -> showVisibilityDialog());

        // 添加组件
        LinearLayout layoutAddComponent = findViewById(R.id.layout_add_component);
        layoutAddComponent.setOnClickListener(v -> showToast("添加组件"));

        // 高级选项
        LinearLayout layoutAdvanced = findViewById(R.id.layout_advanced);
        layoutAdvanced.setOnClickListener(v -> showToast("高级选项"));

        // 笔记内容声明
        TextView tvDisclaimer = findViewById(R.id.tv_disclaimer);
        tvDisclaimer.setOnClickListener(v -> showDisclaimerDialog());

        // 存草稿
        TextView btnSaveDraft = findViewById(R.id.btn_save_draft);
        btnSaveDraft.setOnClickListener(v -> saveDraft());

        // 发布笔记
        TextView btnPublish = findViewById(R.id.btn_publish);
        btnPublish.setOnClickListener(v -> publishNote());
    }

    /**
     * 选择图片
     */
    private void pickImage() {
        if (selectedImages.size() >= MAX_IMAGES) {
            showToast("最多只能上传" + MAX_IMAGES + "张图片");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // 处理图片选择
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null && selectedImages.size() < MAX_IMAGES) {
                selectedImages.add(uri);
                imageAdapter.notifyDataSetChanged();
                updateAddButtonVisibility();
            }
        }
        
        // 处理地点选择
        if (requestCode == REQUEST_CODE_LOCATION_PICKER && resultCode == RESULT_OK && data != null) {
            String locationName = data.getStringExtra("location_name");
            if (locationName != null && !locationName.isEmpty()) {
                currentLocationName = locationName;
                tvCurrentLocation.setText(locationName);
                tvCurrentLocation.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 更新添加按钮可见性
     */
    private void updateAddButtonVisibility() {
        imageAdapter.notifyDataSetChanged();
    }

    /**
     * 显示话题选择对话框
     */
    private void showTopicDialog() {
        String[] topics = {"宠物日常", "萌宠穿搭", "宠物美食", "养宠经验", "宠物健康"};
        
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<String>(this, android.R.layout.select_dialog_item, topics) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                if (selectedTopics.contains(topics[position])) {
                    textView.setTextColor(0xFF4A90E2);
                } else {
                    textView.setTextColor(getResources().getColor(android.R.color.black));
                }
                return textView;
            }
        };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加话题")
                .setAdapter(adapter, (dialog, which) -> {
                    String topic = "#" + topics[which];
                    
                    if (selectedTopics.contains(topics[which])) {
                        selectedTopics.remove(topics[which]);
                        String currentText = etContent.getText().toString();
                        etContent.setText(currentText.replace(topic + " ", ""));
                        showToast("已取消话题: " + topics[which]);
                    } else {
                        selectedTopics.add(topics[which]);
                        String currentText = etContent.getText().toString();
                        etContent.setText(currentText + topic + " ");
                        etContent.setSelection(etContent.getText().length());
                        showToast("已添加话题: " + topics[which]);
                    }
                    adapter.notifyDataSetChanged();
                })
                .show();
    }

    /**
     * 显示公开范围对话框
     */
    private void showVisibilityDialog() {
        String[] options = {"公开（所有人可见）", "仅粉丝可见", "私密（仅自己可见）"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置可见范围")
                .setItems(options, (dialog, which) -> {
                    showToast("已设置为：" + options[which]);
                })
                .show();
    }

    /**
     * 显示内容声明对话框
     */
    private void showDisclaimerDialog() {
        new AlertDialog.Builder(this)
                .setTitle("笔记内容声明")
                .setMessage("请确保您发布的内容符合社区规范，不包含违法违规、侵权、虚假信息等内容。平台有权对违规内容进行处理。")
                .setPositiveButton("我知道了", null)
                .show();
    }

    /**
     * 打开地点选择页面
     */
    private void openLocationPicker() {
        Intent intent = new Intent(this, LocationPickerActivity.class);
        startActivityForResult(intent, REQUEST_CODE_LOCATION_PICKER);
    }

    /**
     * 保存草稿
     */
    private void saveDraft() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty() && selectedImages.isEmpty()) {
            showToast("没有内容可保存");
            return;
        }

        showToast("草稿已保存");
    }

    /**
     * 发布笔记
     */
    private void publishNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        showToast("选中图片数量: " + selectedImages.size());

        if (title.isEmpty() && content.isEmpty() && selectedImages.isEmpty()) {
            showToast("请输入标题、内容或添加图片");
            return;
        }

        String imagePath = "post1";
        if (!selectedImages.isEmpty()) {
            showToast("正在保存图片...");
            imagePath = saveImageToLocal(selectedImages.get(0));
            if (imagePath == null) {
                showToast("图片保存失败");
                return;
            }
            showToast("图片保存成功: " + imagePath);
        } else {
            showToast("未选择图片，使用默认图片");
        }
        
        showToast("即将保存图片路径: " + imagePath);

        String currentNickname = UserManager.getInstance().getCurrentNickname();
        String currentAvatarPath = UserManager.getInstance().getCurrentAvatarPath();
        String avatarRes = currentAvatarPath != null ? currentAvatarPath : "feature1";

        PetDataManager dataManager = PetDataManager.getInstance(this);
        long noteId = dataManager.insertNote(
                title.isEmpty() ? "未命名笔记" : title,
                content,
                currentNickname,
                imagePath,
                avatarRes,
                currentLocationName
        );

        if (noteId > 0) {
            showToast("发布成功！");
            setResult(RESULT_OK);
        } else {
            showToast("发布失败，请重试");
        }

        finish();
    }

    /**
     * 将图片Uri保存到应用私有目录
     *
     * @param uri 图片Uri
     * @return 保存后的文件路径，失败返回null
     */
    private String saveImageToLocal(Uri uri) {
        try {
            android.util.Log.d("PublishActivity", "尝试保存图片: " + uri);
            
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                android.util.Log.e("PublishActivity", "无法打开输入流");
                return null;
            }

            File filesDir = getFilesDir();
            android.util.Log.d("PublishActivity", "文件目录: " + filesDir.getAbsolutePath());
            
            String fileName = UUID.randomUUID().toString() + ".jpg";
            File outputFile = new File(filesDir, fileName);
            android.util.Log.d("PublishActivity", "输出文件: " + outputFile.getAbsolutePath());

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            long totalBytes = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            outputStream.close();
            inputStream.close();

            android.util.Log.d("PublishActivity", "图片保存成功，大小: " + totalBytes + " bytes");
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            android.util.Log.e("PublishActivity", "图片保存失败", e);
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
