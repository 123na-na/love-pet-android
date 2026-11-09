package com.example.love_pet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PetLocationActivity extends AppCompatActivity {

    private MapView mapView;
    private AMap aMap;
    private List<Marker> petMarkers = new ArrayList<>();
    private Marker userMarker;

    private RecyclerView rvPets;
    private PetLocationAdapter adapter;
    private List<PetLocationItem> petList = new ArrayList<>();

    private AMapLocationClient locationClient;
    private double userLat = 21.1535;
    private double userLng = 110.3085;

    private Ringtone ringtone;
    private int ringingPetId = -1;

    private static final int REQUEST_LOCATION_PERMISSION = 2001;
    private static final int REQUEST_ADD_PET = 2002;

    private int nextPetId = 3;
    private int[] avatarResIds = {R.drawable.post1, R.drawable.post2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_location);

        initMapView(savedInstanceState);
        initViews();
        initPetData();
        initLocation();
        setupClickListeners();
    }

    private void initMapView(Bundle savedInstanceState) {
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.getUiSettings().setZoomControlsEnabled(false);
            aMap.getUiSettings().setCompassEnabled(false);
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        }
    }

    private void initViews() {
        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        ImageView ivLocate = findViewById(R.id.iv_locate);
        ivLocate.setOnClickListener(v -> moveCameraToUser());

        ImageView ivAddPet = findViewById(R.id.iv_add_pet);
        ivAddPet.setOnClickListener(v -> {
            Intent intent = new Intent(PetLocationActivity.this, AddPetActivity.class);
            startActivityForResult(intent, REQUEST_ADD_PET);
        });

        rvPets = findViewById(R.id.rv_pets);
        rvPets.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PetLocationAdapter();
        rvPets.setAdapter(adapter);

        Uri notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(this, notificationUri);
    }

    private void initPetData() {
        petList.add(new PetLocationItem(1, "花花", "你的宠物",
                R.drawable.post1, 21.1555, 110.3105));
        petList.add(new PetLocationItem(2, "豆豆", "你的宠物",
                R.drawable.post2, 21.1520, 110.3060));

        adapter.notifyDataSetChanged();
    }

    private void initLocation() {
        try {
            locationClient = new AMapLocationClient(getApplicationContext());
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setOnceLocation(true);
            locationClient.setLocationOption(option);

            locationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation aMapLocation) {
                    if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                        userLat = aMapLocation.getLatitude();
                        userLng = aMapLocation.getLongitude();

                        Random random = new Random();
                        for (int i = 0; i < petList.size(); i++) {
                            PetLocationItem pet = petList.get(i);
                            double latOffset = (random.nextDouble() - 0.3) * 0.006;
                            double lngOffset = (random.nextDouble() - 0.3) * 0.006;
                            pet.latitude = userLat + 0.003 + latOffset * (i + 1);
                            pet.longitude = userLng + 0.003 + lngOffset * (i + 1);
                        }

                        updateUserMarker();
                        updateAllPetMarkers();
                        moveCameraToPets();
                        adapter.notifyDataSetChanged();
                    } else {
                        updateAllPetMarkers();
                        moveCameraToPets();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        updateAllPetMarkers();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }

    private void startLocation() {
        if (locationClient != null) {
            locationClient.startLocation();
        }
    }

    private void updateUserMarker() {
        LatLng latLng = new LatLng(userLat, userLng);
        if (userMarker == null) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_blue_dot))
                    .draggable(false);
            userMarker = aMap.addMarker(options);
        } else {
            userMarker.setPosition(latLng);
        }
    }

    private void updateAllPetMarkers() {
        for (Marker marker : petMarkers) {
            marker.remove();
        }
        petMarkers.clear();

        for (PetLocationItem pet : petList) {
            LatLng latLng = new LatLng(pet.latitude, pet.longitude);
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .icon(createPetMarkerIcon(pet))
                    .draggable(false);
            Marker marker = aMap.addMarker(options);
            marker.setObject(pet);
            petMarkers.add(marker);
        }
    }

    private BitmapDescriptor createPetMarkerIcon(PetLocationItem pet) {
        int size = dpToPx(60);
        int borderWidth = dpToPx(4);

        Bitmap output = Bitmap.createBitmap(size, size + dpToPx(16), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.FILL);
        borderPaint.setAntiAlias(true);

        float centerX = size / 2f;
        float centerY = size / 2f;
        float radius = size / 2f - borderWidth / 2f;

        canvas.drawCircle(centerX, centerY, radius + borderWidth / 2f, borderPaint);

        Bitmap avatarBitmap;
        if (pet.avatarPath != null && !pet.avatarPath.isEmpty()) {
            avatarBitmap = BitmapFactory.decodeFile(pet.avatarPath);
        } else {
            avatarBitmap = BitmapFactory.decodeResource(getResources(), pet.avatarRes);
        }

        if (avatarBitmap == null) {
            avatarBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_paw_black);
        }

        Bitmap scaledAvatar = Bitmap.createScaledBitmap(avatarBitmap, size - borderWidth * 2, size - borderWidth * 2, true);

        BitmapShader shader = new BitmapShader(scaledAvatar, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint avatarPaint = new Paint();
        avatarPaint.setShader(shader);
        avatarPaint.setAntiAlias(true);

        canvas.drawCircle(centerX, centerY, radius, avatarPaint);

        Paint pinPaint = new Paint();
        pinPaint.setColor(Color.parseColor("#FF3B30"));
        pinPaint.setStyle(Paint.Style.FILL);
        pinPaint.setAntiAlias(true);

        Path pinPath = new Path();
        pinPath.moveTo(centerX - dpToPx(6), size - dpToPx(2));
        pinPath.lineTo(centerX + dpToPx(6), size - dpToPx(2));
        pinPath.lineTo(centerX, size + dpToPx(10));
        pinPath.close();
        canvas.drawPath(pinPath, pinPaint);

        canvas.drawCircle(centerX, size + dpToPx(12), dpToPx(3), pinPaint);

        return BitmapDescriptorFactory.fromBitmap(output);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void moveCameraToPets() {
        if (petList.isEmpty()) return;

        double minLat = userLat, maxLat = userLat;
        double minLng = userLng, maxLng = userLng;

        for (PetLocationItem pet : petList) {
            minLat = Math.min(minLat, pet.latitude);
            maxLat = Math.max(maxLat, pet.latitude);
            minLng = Math.min(minLng, pet.longitude);
            maxLng = Math.max(maxLng, pet.longitude);
        }

        double centerLat = (minLat + maxLat) / 2;
        double centerLng = (minLng + maxLng) / 2;

        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(centerLat, centerLng), 15f));
    }

    private void moveCameraToUser() {
        LatLng latLng = new LatLng(userLat, userLng);
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
    }

    private String calculateDistance(double lat, double lng) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(userLat, userLng, lat, lng, results);
        float distance = results[0];

        if (distance >= 1000) {
            DecimalFormat df = new DecimalFormat("#.0");
            return "离你" + df.format(distance / 1000) + "公里";
        } else {
            return "离你" + (int) distance + "米";
        }
    }

    private void setupClickListeners() {
        aMap.setOnMarkerClickListener(marker -> {
            if (marker.getObject() instanceof PetLocationItem) {
                PetLocationItem pet = (PetLocationItem) marker.getObject();
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16f));
                Toast.makeText(this, pet.name + "在这里！", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void playRingtone(int petId) {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
        ringingPetId = petId;
        if (ringtone != null) {
            ringtone.play();
            adapter.notifyDataSetChanged();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (ringtone != null && ringtone.isPlaying()) {
                    ringtone.stop();
                }
                ringingPetId = -1;
                adapter.notifyDataSetChanged();
            }, 5000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocation();
            } else {
                Toast.makeText(this, "未获得定位权限，使用默认位置", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_PET && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("pet_name");
            String breed = data.getStringExtra("pet_breed");
            String type = data.getStringExtra("pet_type");
            String avatarTempPath = data.getStringExtra("avatar_temp_path");

            Random random = new Random();
            double latOffset = (random.nextDouble() - 0.5) * 0.006;
            double lngOffset = (random.nextDouble() - 0.5) * 0.006;

            int avatarRes = avatarResIds[petList.size() % avatarResIds.length];
            int newPetId = nextPetId++;
            String savedAvatarPath = null;

            if (avatarTempPath != null && !avatarTempPath.isEmpty()) {
                PetAvatarManager avatarManager = new PetAvatarManager(this);
                savedAvatarPath = avatarManager.savePetAvatar(
                        android.net.Uri.fromFile(new java.io.File(avatarTempPath)), newPetId);
            }

            PetLocationItem newPet = new PetLocationItem(
                    newPetId,
                    name,
                    breed,
                    avatarRes,
                    savedAvatarPath,
                    userLat + 0.003 + latOffset,
                    userLng + 0.003 + lngOffset
            );

            petList.add(newPet);
            adapter.notifyItemInserted(petList.size() - 1);
            updateAllPetMarkers();
            moveCameraToPets();

            Toast.makeText(this, "添加成功：" + name, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (locationClient != null) {
            locationClient.onDestroy();
        }
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }

    private static class PetLocationItem {
        int id;
        String name;
        String desc;
        int avatarRes;
        String avatarPath;
        double latitude;
        double longitude;

        PetLocationItem(int id, String name, String desc, int avatarRes,
                        double latitude, double longitude) {
            this(id, name, desc, avatarRes, null, latitude, longitude);
        }

        PetLocationItem(int id, String name, String desc, int avatarRes, String avatarPath,
                        double latitude, double longitude) {
            this.id = id;
            this.name = name;
            this.desc = desc;
            this.avatarRes = avatarRes;
            this.avatarPath = avatarPath;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private class PetLocationAdapter extends RecyclerView.Adapter<PetLocationAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pet_location, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PetLocationItem pet = petList.get(position);
            holder.tvName.setText(pet.name);
            holder.tvDesc.setText(pet.desc);
            PetAvatarManager.loadPetAvatar(PetLocationActivity.this,
                    holder.ivAvatar, pet.avatarPath, pet.avatarRes);
            holder.tvDistance.setText(calculateDistance(pet.latitude, pet.longitude));

            if (ringingPetId == pet.id) {
                holder.btnRing.setText("响铃中...");
                holder.btnRing.setBackgroundColor(Color.parseColor("#FF9500"));
            } else {
                holder.btnRing.setText("响铃");
                holder.btnRing.setBackgroundResource(R.drawable.bg_ring_button);
            }

            holder.btnRing.setOnClickListener(v -> {
                playRingtone(pet.id);
                Toast.makeText(PetLocationActivity.this,
                        "正在呼叫 " + pet.name + "，请注意听铃声~", Toast.LENGTH_SHORT).show();
            });

            holder.itemView.setOnClickListener(v -> {
                LatLng latLng = new LatLng(pet.latitude, pet.longitude);
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
            });
        }

        @Override
        public int getItemCount() {
            return petList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivAvatar;
            TextView tvName;
            TextView tvDesc;
            TextView btnRing;
            TextView tvDistance;

            ViewHolder(View itemView) {
                super(itemView);
                ivAvatar = itemView.findViewById(R.id.iv_pet_avatar);
                tvName = itemView.findViewById(R.id.tv_pet_name);
                tvDesc = itemView.findViewById(R.id.tv_pet_desc);
                btnRing = itemView.findViewById(R.id.btn_ring);
                tvDistance = itemView.findViewById(R.id.tv_distance);

                ivAvatar.setClipToOutline(true);
                ivAvatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
                    @Override
                    public void getOutline(android.view.View view, android.graphics.Outline outline) {
                        outline.setOval(0, 0, view.getWidth(), view.getHeight());
                    }
                });
            }
        }
    }
}
