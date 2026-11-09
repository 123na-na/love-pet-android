package com.example.love_pet;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

public class HospitalMapActivity extends AppCompatActivity {

    private MapView mapView;
    private AMap aMap;

    private double currentLat = 39.9087;
    private double currentLng = 116.3975;

    private static final int REQUEST_LOCATION_PERMISSION = 2001;

    private HospitalInfo[] hospitals = {
            new HospitalInfo("萌宠宠物医院", 39.9150, 116.4100, "朝阳区建国路88号", "三级甲等"),
            new HospitalInfo("爱宠动物医院", 39.9850, 116.3150, "海淀区中关村大街1号", "三级乙等"),
            new HospitalInfo("宠爱宠物诊所", 39.8700, 116.3600, "丰台区南三环西路5号", "一级医院"),
            new HospitalInfo("博爱动物医院", 39.9400, 116.4500, "朝阳区东四环中路20号", "二级甲等")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_map);

        initMapView(savedInstanceState);
        initViews();
        startLocation();
    }

    private void initMapView(Bundle savedInstanceState) {
        mapView = findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.getUiSettings().setZoomControlsEnabled(false);
            aMap.getUiSettings().setCompassEnabled(false);
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);

            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.strokeColor(0x00000000);
            myLocationStyle.radiusFillColor(0x6600B4FF);
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.setMyLocationEnabled(true);

            LatLng defaultLatLng = new LatLng(currentLat, currentLng);
            aMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(defaultLatLng, 13, 0, 0)));

            addHospitalMarkers();
        }
    }

    private void addHospitalMarkers() {
        for (int i = 0; i < hospitals.length; i++) {
            HospitalInfo hospital = hospitals[i];
            LatLng latLng = new LatLng(hospital.lat, hospital.lng);
            Marker marker = aMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(hospital.name)
                    .snippet(hospital.address)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_red))
                    .anchor(0.5f, 1f));
            marker.setObject(i);
        }

        aMap.setOnMarkerClickListener(marker -> {
            Object obj = marker.getObject();
            if (obj instanceof Integer) {
                int index = (int) obj;
                if (index >= 0 && index < hospitals.length) {
                    highlightHospital(index == 0 ? 1 : 2);
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(hospitals[index].lat, hospitals[index].lng), 15));
                }
            }
            return false;
        });
    }

    private void initViews() {
        findViewById(R.id.iv_back_hospital_map).setOnClickListener(v -> finish());

        findViewById(R.id.tv_refresh_hospital).setOnClickListener(v -> {
            Toast.makeText(this, "正在刷新位置...", Toast.LENGTH_SHORT).show();
            startLocation();
        });

        findViewById(R.id.iv_locate_me_hospital).setOnClickListener(v -> {
            moveToCurrentLocation();
            Toast.makeText(this, "已定位到当前位置", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_zoom_in).setOnClickListener(v -> {
            if (aMap != null) {
                aMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });

        findViewById(R.id.btn_zoom_out).setOnClickListener(v -> {
            if (aMap != null) {
                aMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        findViewById(R.id.btn_nav_hospital1).setOnClickListener(v -> {
            Toast.makeText(this, "正在导航到萌宠宠物医院...", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_appointment_hospital1).setOnClickListener(v -> {
            Intent intent = new Intent(this, AppointmentBookActivity.class);
            intent.putExtra(AppointmentBookActivity.EXTRA_HOSPITAL_NAME, "萌宠宠物医院");
            intent.putExtra(AppointmentBookActivity.EXTRA_HOSPITAL_LEVEL, "三级甲等");
            intent.putExtra(AppointmentBookActivity.EXTRA_HOSPITAL_ADDRESS, "朝阳区建国路88号");
            startActivity(intent);
        });

        findViewById(R.id.btn_nav_hospital2).setOnClickListener(v -> {
            Toast.makeText(this, "正在导航到爱宠动物医院...", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_appointment_hospital2).setOnClickListener(v -> {
            Intent intent = new Intent(this, AppointmentBookActivity.class);
            intent.putExtra(AppointmentBookActivity.EXTRA_HOSPITAL_NAME, "爱宠动物医院");
            intent.putExtra(AppointmentBookActivity.EXTRA_HOSPITAL_LEVEL, "三级乙等");
            intent.putExtra(AppointmentBookActivity.EXTRA_HOSPITAL_ADDRESS, "海淀区中关村大街1号");
            startActivity(intent);
        });
    }

    private void startLocation() {
        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            return;
        }

        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastLocation == null) {
                lastLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastLocation != null) {
                currentLat = lastLocation.getLatitude();
                currentLng = lastLocation.getLongitude();
                moveToCurrentLocation();
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener, Looper.getMainLooper());
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener, Looper.getMainLooper());

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (locationManager != null) {
                    locationManager.removeUpdates(locationListener);
                }
            }, 10000);

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                moveToCurrentLocation();

                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (lm != null) {
                    lm.removeUpdates(this);
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    private void moveToCurrentLocation() {
        if (aMap != null) {
            LatLng latLng = new LatLng(currentLat, currentLng);
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        }
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocation();
            } else {
                Toast.makeText(this, "定位权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void highlightHospital(int index) {
        View layout1 = findViewById(R.id.layout_hospital1);
        View layout2 = findViewById(R.id.layout_hospital2);
        if (index == 1) {
            layout1.setBackgroundResource(R.drawable.bg_hospital_card_map);
            layout2.setBackgroundResource(R.drawable.bg_white_stroke);
        } else {
            layout1.setBackgroundResource(R.drawable.bg_white_stroke);
            layout2.setBackgroundResource(R.drawable.bg_hospital_card_map);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    private static class HospitalInfo {
        String name;
        double lat;
        double lng;
        String address;
        String level;

        HospitalInfo(String name, double lat, double lng, String address, String level) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
            this.address = address;
            this.level = level;
        }
    }
}
