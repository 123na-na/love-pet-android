package com.example.love_pet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.util.ArrayList;
import java.util.List;

public class LocationPickerActivity extends AppCompatActivity implements PoiSearch.OnPoiSearchListener, GeocodeSearch.OnGeocodeSearchListener {

    private MapView mapView;
    private AMap aMap;
    private Marker centerMarker;

    private ListView lvLocations;
    private Button btnConfirm;
    private EditText etSearch;
    private ImageView ivLocate;

    private List<LocationItem> locationList = new ArrayList<>();
    private int selectedPosition = -1;
    private LocationAdapter adapter;

    private AMapLocationClient locationClient;
    private double currentLat = 22.5431;
    private double currentLng = 114.0579;

    private static final int REQUEST_LOCATION_PERMISSION = 2001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        initMapView(savedInstanceState);
        initViews();
        initLocationList();
        setupListView();
        setupClickListeners();
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
            myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_gray));
            myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
            myLocationStyle.radiusFillColor(Color.argb(100, 0, 180, 255));
            aMap.setMyLocationStyle(myLocationStyle);

            LatLng defaultLatLng = new LatLng(currentLat, currentLng);
            aMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition(defaultLatLng, 15, 0, 0)));

            centerMarker = aMap.addMarker(new MarkerOptions()
                    .position(defaultLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_gray))
                    .anchor(0.5f, 1f));
        }
    }

    private void initViews() {
        lvLocations = findViewById(R.id.lv_locations);
        btnConfirm = findViewById(R.id.btn_confirm);
        etSearch = findViewById(R.id.et_search);
        ivLocate = findViewById(R.id.iv_locate);
        TextView tvClose = findViewById(R.id.tv_close);
        tvClose.setOnClickListener(v -> finish());
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
                updateMapLocation(currentLat, currentLng);
                searchNearbyPoi(currentLat, currentLng);
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
                updateMapLocation(currentLat, currentLng);
                searchNearbyPoi(currentLat, currentLng);

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

    private void updateMapLocation(double lat, double lng) {
        LatLng latLng = new LatLng(lat, lng);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        if (centerMarker != null) {
            centerMarker.setPosition(latLng);
        }
    }

    private void searchNearbyPoi(double lat, double lng) {
        LatLonPoint centerPoint = new LatLonPoint(lat, lng);
        PoiSearch.Query query = new PoiSearch.Query("", "", "");
        query.setPageSize(20);
        query.setPageNum(1);

        try {
            PoiSearch poiSearch = new PoiSearch(this, query);
            poiSearch.setBound(new PoiSearch.SearchBound(centerPoint, 1000));
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.searchPOIAsyn();
        } catch (AMapException e) {
            e.printStackTrace();
            Toast.makeText(this, "搜索初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchPoiByKeyword(String keyword) {
        PoiSearch.Query query = new PoiSearch.Query(keyword, "", "深圳");
        query.setPageSize(20);
        query.setPageNum(1);

        try {
            PoiSearch poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.searchPOIAsyn();
        } catch (AMapException e) {
            e.printStackTrace();
            Toast.makeText(this, "搜索初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPoiSearched(PoiResult result, int rCode) {
        if (rCode == 1000) {
            if (result != null && result.getPois() != null && !result.getPois().isEmpty()) {
                List<LocationItem> items = new ArrayList<>();
                for (PoiItem poi : result.getPois()) {
                    String name = poi.getTitle();
                    String address = poi.getSnippet();
                    double lat = poi.getLatLonPoint().getLatitude();
                    double lng = poi.getLatLonPoint().getLongitude();
                    int distance = poi.getDistance();
                    String distanceStr = distance > 1000
                            ? String.format("%.1fkm", distance / 1000.0)
                            : distance + "m";

                    items.add(new LocationItem(name, address, distanceStr, lat, lng));
                }

                locationList.clear();
                locationList.addAll(items);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "没有找到相关地点", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "搜索失败，错误码：" + rCode, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int rCode) {
    }

    private void getRegeocodeAddress(double lat, double lng, String name, String address) {
        pendingName = name;
        pendingAddress = address;
        pendingLat = lat;
        pendingLng = lng;
        hasReturnedResult = false;

        // 设置超时保护，3秒后如果还没返回结果就直接返回
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!hasReturnedResult) {
                hasReturnedResult = true;
                returnResultWithFallback(name, address);
            }
        }, 3000);

        try {
            GeocodeSearch geocodeSearch = new GeocodeSearch(this);
            geocodeSearch.setOnGeocodeSearchListener(this);
            RegeocodeQuery query = new RegeocodeQuery(
                    new LatLonPoint(lat, lng),
                    200,
                    GeocodeSearch.AMAP
            );
            geocodeSearch.getFromLocationAsyn(query);
        } catch (Exception e) {
            e.printStackTrace();
            if (!hasReturnedResult) {
                hasReturnedResult = true;
                returnResultWithFallback(name, address);
            }
        }
    }

    private String pendingName = "";
    private String pendingAddress = "";
    private double pendingLat = 0;
    private double pendingLng = 0;
    private boolean hasReturnedResult = false;

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (hasReturnedResult) {
            return;
        }
        hasReturnedResult = true;

        String province = "";
        String city = "";
        String district = "";
        if (rCode == 1000 && result != null && result.getRegeocodeAddress() != null) {
            province = result.getRegeocodeAddress().getProvince();
            city = result.getRegeocodeAddress().getCity();
            district = result.getRegeocodeAddress().getDistrict();
        }
        returnResult(pendingName, pendingAddress, province, city, district, pendingLat, pendingLng);
    }

    @Override
    public void onGeocodeSearched(GeocodeResult result, int rCode) {
    }

    private void returnResultWithFallback(String name, String address) {
        String province = "";
        String city = "";
        String district = "";

        // 从地址字符串中尝试解析省市区
        if (address != null && !address.isEmpty()) {
            if (address.contains("省")) {
                province = address.substring(0, address.indexOf("省") + 1);
                String rest = address.substring(address.indexOf("省") + 1);
                if (rest.contains("市")) {
                    city = rest.substring(0, rest.indexOf("市") + 1);
                    String rest2 = rest.substring(rest.indexOf("市") + 1);
                    if (rest2.contains("区") || rest2.contains("县")) {
                        int idx = rest2.contains("区") ? rest2.indexOf("区") : rest2.indexOf("县");
                        district = rest2.substring(0, idx + 1);
                    }
                }
            } else if (address.contains("市")) {
                city = address.substring(0, address.indexOf("市") + 1);
                String rest = address.substring(address.indexOf("市") + 1);
                if (rest.contains("区") || rest.contains("县")) {
                    int idx = rest.contains("区") ? rest.indexOf("区") : rest.indexOf("县");
                    district = rest.substring(0, idx + 1);
                }
            }
        }

        returnResult(name, address, province, city, district, pendingLat, pendingLng);
    }

    private void returnResult(String name, String address, String province, String city, String district) {
        returnResult(name, address, province, city, district, currentLat, currentLng);
    }

    private void returnResult(String name, String address, String province, String city, String district, double lat, double lng) {
        Intent result = new Intent();
        result.putExtra("location_name", name);
        result.putExtra("location_address", address);
        result.putExtra("province", province);
        result.putExtra("city", city);
        result.putExtra("district", district);
        result.putExtra("latitude", lat);
        result.putExtra("longitude", lng);
        setResult(RESULT_OK, result);
        finish();
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
                Toast.makeText(this, "需要定位权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initLocationList() {
        locationList.add(new LocationItem("正在定位中...", "", "", 0, 0));
    }

    private void setupListView() {
        adapter = new LocationAdapter(this, locationList);
        lvLocations.setAdapter(adapter);

        lvLocations.setOnItemClickListener((parent, view, position, id) -> {
            selectedPosition = position;
            adapter.notifyDataSetChanged();

            LocationItem item = locationList.get(position);
            if (item.getLat() != 0 && item.getLng() != 0) {
                updateMapLocation(item.getLat(), item.getLng());
            }
        });
    }

    private void setupClickListeners() {
        btnConfirm.setOnClickListener(v -> {
            if (selectedPosition >= 0) {
                LocationItem selected = locationList.get(selectedPosition);
                if (selected.getLat() != 0 && selected.getLng() != 0) {
                    getRegeocodeAddress(selected.getLat(), selected.getLng(), selected.getName(), selected.getAddress());
                } else {
                    returnResult(selected.getName(), selected.getAddress(), "", "", "");
                }
            } else {
                finish();
            }
        });

        ivLocate.setOnClickListener(v -> {
            startLocation();
            Toast.makeText(this, "正在定位...", Toast.LENGTH_SHORT).show();
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String keyword = etSearch.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    searchPoiByKeyword(keyword);
                }
                return true;
            }
            return false;
        });
    }

    private class LocationAdapter extends BaseAdapter {
        private Context context;
        private List<LocationItem> items;
        private LayoutInflater inflater;

        public LocationAdapter(Context context, List<LocationItem> items) {
            this.context = context;
            this.items = items;
            this.inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_location, parent, false);
                holder = new ViewHolder();
                holder.tvName = convertView.findViewById(R.id.tv_name);
                holder.tvAddress = convertView.findViewById(R.id.tv_address);
                holder.tvDistance = convertView.findViewById(R.id.tv_distance);
                holder.tvSelected = convertView.findViewById(R.id.tv_selected);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            LocationItem item = items.get(position);
            holder.tvName.setText(item.getName());
            holder.tvAddress.setText(item.getAddress());
            holder.tvDistance.setText(item.getDistance());
            holder.tvSelected.setVisibility(position == selectedPosition ? View.VISIBLE : View.INVISIBLE);

            return convertView;
        }
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvAddress;
        TextView tvDistance;
        TextView tvSelected;
    }

    public static class LocationItem {
        private String name;
        private String address;
        private String distance;
        private double lat;
        private double lng;

        public LocationItem(String name, String address, String distance, double lat, double lng) {
            this.name = name;
            this.address = address;
            this.distance = distance;
            this.lat = lat;
            this.lng = lng;
        }

        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getDistance() { return distance == null ? "" : distance; }
        public double getLat() { return lat; }
        public double getLng() { return lng; }
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
}
