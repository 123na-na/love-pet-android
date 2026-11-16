package com.example.love_pet;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class AddAddressDialogHelper {

    public interface OnAddressSavedListener {
        void onAddressSaved(Address address);
    }

    private Activity activity;
    private PopupWindow currentPopupWindow;
    private View currentDimView;
    private com.amap.api.maps.MapView mapPreviewMapView;
    private com.amap.api.maps.AMap previewAMap;
    private double selectedLatitude = 0;
    private double selectedLongitude = 0;
    private boolean isEditMode = false;
    private Address editingAddress = null;
    private OnAddressSavedListener listener;
    private View addAddressPopupView;
    private static final int REQUEST_PICK_LOCATION = 1001;

    public AddAddressDialogHelper(Activity activity) {
        this.activity = activity;
    }

    public void showAddAddressDialog(Address editAddress, OnAddressSavedListener listener) {
        this.listener = listener;
        this.isEditMode = editAddress != null;
        this.editingAddress = editAddress;

        View dimView = new View(activity);
        dimView.setBackgroundColor(0x99000000);
        dimView.setAlpha(0f);
        dimView.setTag("dim_view");

        ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
        ViewGroup.LayoutParams dimParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.addView(dimView, dimParams);

        currentDimView = dimView;

        View popupView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_address, null);

        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = activity.getResources().getDisplayMetrics().heightPixels;
        int popupHeight = (int) (screenHeight * 0.85);

        FrameLayout container = new FrameLayout(activity);
        container.setBackgroundResource(R.drawable.bg_dialog_top_rounded);

        if (popupView.getParent() != null) {
            ((ViewGroup) popupView.getParent()).removeView(popupView);
        }
        container.addView(popupView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        PopupWindow popupWindow = new PopupWindow(container,
                screenWidth,
                popupHeight,
                true);

        popupWindow.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        currentPopupWindow = popupWindow;

        addAddressPopupView = popupView;

        EditText etName = popupView.findViewById(R.id.et_name);
        EditText etPhone = popupView.findViewById(R.id.et_phone);
        EditText etDetail = popupView.findViewById(R.id.et_address_detail);
        TextView tvRegion = popupView.findViewById(R.id.tv_region_value);
        TextView tvLocationResult = popupView.findViewById(R.id.tv_location_result);
        LinearLayout llLocation = popupView.findViewById(R.id.ll_location_section);
        RadioButton rbDefault = popupView.findViewById(R.id.rb_default);
        Button btnSave = popupView.findViewById(R.id.btn_save_address);
        TextView tvTitle = popupView.findViewById(R.id.tv_title);
        LinearLayout llLocationInfo = popupView.findViewById(R.id.ll_location_info);
        ImageView ivClose = popupView.findViewById(R.id.iv_close_add_address);

        final String[] selectedTag = { "" };
        int[] tagIds = { R.id.tv_tag_home, R.id.tv_tag_company, R.id.tv_tag_school,
                R.id.tv_tag_parents, R.id.tv_tag_friend, R.id.tv_tag_custom };
        String[] tagNames = { "家", "公司", "学校", "父母", "朋友", "自定义" };

        for (int i = 0; i < tagIds.length; i++) {
            final int idx = i;
            TextView tagTv = popupView.findViewById(tagIds[i]);
            if (tagTv != null) {
                tagTv.setOnClickListener(v -> {
                    selectedTag[0] = tagNames[idx];
                    for (int j = 0; j < tagIds.length; j++) {
                        TextView t = popupView.findViewById(tagIds[j]);
                        if (t != null) {
                            t.setBackgroundResource(
                                    j == idx ? R.drawable.bg_tag_selected : R.drawable.bg_tag_normal);
                            t.setTextColor(j == idx ? 0xFFFF9800 : 0xFF666666);
                        }
                    }
                });
            }
        }

        if (isEditMode && editingAddress != null) {
            if (tvTitle != null) {
                tvTitle.setText("编辑地址");
            }
            etName.setText(editingAddress.getName());
            etPhone.setText(editingAddress.getPhone());
            etDetail.setText(editingAddress.getDetail());
            tvRegion.setText(editingAddress.getProvince() + editingAddress.getCity() + editingAddress.getDistrict());
            selectedTag[0] = editingAddress.getTag();
            rbDefault.setChecked(editingAddress.isDefault());

            for (int i = 0; i < tagNames.length; i++) {
                if (tagNames[i].equals(editingAddress.getTag())) {
                    TextView tagTv = popupView.findViewById(tagIds[i]);
                    if (tagTv != null) {
                        tagTv.setBackgroundResource(R.drawable.bg_tag_selected);
                        tagTv.setTextColor(0xFFFF9800);
                    }
                    break;
                }
            }

            if (llLocationInfo != null) {
                llLocationInfo.setVisibility(View.VISIBLE);
            }
            if (tvLocationResult != null) {
                tvLocationResult.setText(editingAddress.getDetail());
            }
            selectedLatitude = editingAddress.getLatitude();
            selectedLongitude = editingAddress.getLongitude();
        } else {
            selectedLatitude = 0;
            selectedLongitude = 0;
        }

        com.amap.api.maps.MapView mapView = popupView.findViewById(R.id.map_preview);
        if (mapView != null) {
            mapPreviewMapView = mapView;
            mapView.onCreate(null);
            previewAMap = mapView.getMap();
            if (previewAMap != null) {
                previewAMap.getUiSettings().setZoomControlsEnabled(false);
                previewAMap.getUiSettings().setCompassEnabled(false);
                previewAMap.getUiSettings().setMyLocationButtonEnabled(false);
                previewAMap.getUiSettings().setZoomGesturesEnabled(true);
                previewAMap.getUiSettings().setScrollGesturesEnabled(true);
                com.amap.api.maps.model.LatLng defaultLatLng;
                if (isEditMode && editingAddress != null && editingAddress.getLatitude() != 0) {
                    defaultLatLng = new com.amap.api.maps.model.LatLng(editingAddress.getLatitude(), editingAddress.getLongitude());
                } else {
                    defaultLatLng = new com.amap.api.maps.model.LatLng(22.5431, 114.0579);
                }
                previewAMap.moveCamera(com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(defaultLatLng, 15));
                previewAMap.addMarker(new com.amap.api.maps.model.MarkerOptions()
                        .position(defaultLatLng)
                        .icon(com.amap.api.maps.model.BitmapDescriptorFactory.defaultMarker(com.amap.api.maps.model.BitmapDescriptorFactory.HUE_AZURE)));
                previewAMap.setOnMapClickListener(latLng -> {
                    Intent intent = new Intent(activity, LocationPickerActivity.class);
                    activity.startActivityForResult(intent, REQUEST_PICK_LOCATION);
                });
            }
        }

        LinearLayout llRegionPicker = popupView.findViewById(R.id.ll_region_picker);
        if (llRegionPicker != null) {
            llRegionPicker.setOnClickListener(v -> {
                RegionPicker regionPicker = new RegionPicker(activity);
                regionPicker.setOnRegionSelectedListener((province, city, district) -> {
                    tvRegion.setText(province + city + district);
                });
                regionPicker.show();
            });
        }

        Runnable dismissRunnable = () -> {
            dimView.animate().alpha(0f).setDuration(200).start();
            popupWindow.dismiss();
            rootView.removeView(dimView);
            destroyMapPreview();
        };

        if (ivClose != null) {
            ivClose.setOnClickListener(v -> dismissRunnable.run());
        }

        dimView.setOnClickListener(v -> dismissRunnable.run());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String detail = etDetail.getText().toString().trim();
            String region = tvRegion.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(activity, "请输入收货人姓名", Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.isEmpty()) {
                Toast.makeText(activity, "请输入手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (region.equals("请选择省市区") || region.isEmpty()) {
                Toast.makeText(activity, "请选择所在地区", Toast.LENGTH_SHORT).show();
                return;
            }
            if (detail.isEmpty()) {
                Toast.makeText(activity, "请输入详细地址", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = new Address(
                    isEditMode && editingAddress != null ? editingAddress.getId() : String.valueOf(System.currentTimeMillis()),
                    name,
                    phone,
                    region.contains("省") ? region.substring(0, region.indexOf("省") + 1) : "",
                    region.contains("市") && region.contains("省")
                            ? region.substring(region.indexOf("省") + 1, region.indexOf("市") + 1)
                            : "",
                    region.contains("区") ? region.substring(region.indexOf("市") + 1) : "",
                    detail,
                    selectedTag[0],
                    rbDefault.isChecked());
            address.setLatitude(selectedLatitude);
            address.setLongitude(selectedLongitude);

            if (isEditMode && editingAddress != null) {
                Address.updateAddress(activity, address);
            } else {
                Address.addAddress(activity, address);
            }

            Toast.makeText(activity, "地址保存成功", Toast.LENGTH_SHORT).show();

            dismissRunnable.run();

            if (listener != null) {
                listener.onAddressSaved(address);
            }
        });

        dimView.animate().alpha(1f).setDuration(200).start();
        popupWindow.showAtLocation(rootView, android.view.Gravity.BOTTOM, 0, 0);
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_LOCATION && resultCode == Activity.RESULT_OK && data != null) {
            String locationName = data.getStringExtra("location_name");
            String locationAddress = data.getStringExtra("location_address");
            String province = data.getStringExtra("province");
            String city = data.getStringExtra("city");
            String district = data.getStringExtra("district");
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);

            if (latitude != 0 && longitude != 0) {
                selectedLatitude = latitude;
                selectedLongitude = longitude;
            }

            if (addAddressPopupView != null && locationAddress != null && !locationAddress.isEmpty()) {
                TextView tvLocationResult = addAddressPopupView.findViewById(R.id.tv_location_result);
                LinearLayout llLocationInfo = addAddressPopupView.findViewById(R.id.ll_location_info);
                TextView tvRegion = addAddressPopupView.findViewById(R.id.tv_region_value);

                if (previewAMap != null && latitude != 0 && longitude != 0) {
                    com.amap.api.maps.model.LatLng newLatLng = new com.amap.api.maps.model.LatLng(latitude, longitude);
                    previewAMap.moveCamera(com.amap.api.maps.CameraUpdateFactory.newLatLngZoom(newLatLng, 16));
                    previewAMap.clear();
                    previewAMap.addMarker(new com.amap.api.maps.model.MarkerOptions()
                            .position(newLatLng)
                            .icon(com.amap.api.maps.model.BitmapDescriptorFactory.defaultMarker(com.amap.api.maps.model.BitmapDescriptorFactory.HUE_AZURE)));
                }

                if (tvLocationResult != null && llLocationInfo != null) {
                    llLocationInfo.setVisibility(View.VISIBLE);
                    tvLocationResult.setText(locationName != null ? locationName : locationAddress);

                    TextView tvAddressDetail = addAddressPopupView.findViewById(R.id.tv_location_address_detail);
                    if (tvAddressDetail != null && locationName != null && !locationName.equals(locationAddress)) {
                        tvAddressDetail.setText(locationAddress);
                        tvAddressDetail.setVisibility(View.VISIBLE);
                    } else if (tvAddressDetail != null) {
                        tvAddressDetail.setVisibility(View.GONE);
                    }
                }

                if (tvRegion != null) {
                    province = province != null ? province : "";
                    city = city != null ? city : "";
                    district = district != null ? district : "";

                    if (province.isEmpty() && city.isEmpty() && district.isEmpty()) {
                        if (locationAddress.contains("省")) {
                            province = locationAddress.substring(0, locationAddress.indexOf("省") + 1);
                            String rest = locationAddress.substring(locationAddress.indexOf("省") + 1);
                            if (rest.contains("市")) {
                                city = rest.substring(0, rest.indexOf("市") + 1);
                                String rest2 = rest.substring(rest.indexOf("市") + 1);
                                if (rest2.contains("区") || rest2.contains("县")) {
                                    int idx = rest2.contains("区") ? rest2.indexOf("区") : rest2.indexOf("县");
                                    district = rest2.substring(0, idx + 1);
                                }
                            }
                        } else if (locationAddress.contains("市")) {
                            city = locationAddress.substring(0, locationAddress.indexOf("市") + 1);
                            String rest = locationAddress.substring(locationAddress.indexOf("市") + 1);
                            if (rest.contains("区") || rest.contains("县")) {
                                int idx = rest.contains("区") ? rest.indexOf("区") : rest.indexOf("县");
                                district = rest.substring(0, idx + 1);
                            }
                        }
                    }

                    tvRegion.setText(province + city + district);

                    EditText etDetail = addAddressPopupView.findViewById(R.id.et_address_detail);
                    if (etDetail != null) {
                        String detail = locationAddress;
                        if (!province.isEmpty()) {
                            detail = detail.replace(province, "");
                        }
                        if (!city.isEmpty()) {
                            detail = detail.replace(city, "");
                        }
                        if (!district.isEmpty()) {
                            detail = detail.replace(district, "");
                        }
                        if (detail != null && !detail.isEmpty()) {
                            etDetail.setText(detail);
                        }
                    }
                }
            }
        }
    }

    public void destroyMapPreview() {
        if (mapPreviewMapView != null) {
            try {
                mapPreviewMapView.onDestroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mapPreviewMapView = null;
        }
        previewAMap = null;
        addAddressPopupView = null;
    }

    public void onDestroy() {
        destroyMapPreview();
        currentPopupWindow = null;
        currentDimView = null;
    }

    public boolean isShowing() {
        return currentPopupWindow != null && currentPopupWindow.isShowing();
    }

    public void dismiss() {
        if (currentPopupWindow != null) {
            currentPopupWindow.dismiss();
        }
    }
}
