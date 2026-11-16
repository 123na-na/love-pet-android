package com.example.love_pet;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RegionPicker {

    public interface OnRegionSelectedListener {
        void onSelected(String province, String city, String district);
    }

    private Context context;
    private PopupWindow popupWindow;
    private View dimView;
    private OnRegionSelectedListener listener;

    private List<String> provinces = new ArrayList<>();
    private List<List<String>> cities = new ArrayList<>();
    private List<List<List<String>>> districts = new ArrayList<>();

    private int selectedProvinceIndex = 0;
    private int selectedCityIndex = 0;
    private int selectedDistrictIndex = 0;

    private TextView tvProvinceTab;
    private TextView tvCityTab;
    private TextView tvDistrictTab;
    private LinearLayout llOptionsContainer;

    private int currentLevel = 0;

    public RegionPicker(Context context) {
        this.context = context;
        initRegionData();
    }

    public void setOnRegionSelectedListener(OnRegionSelectedListener listener) {
        this.listener = listener;
    }

    private void initRegionData() {
        try {
            InputStream is = context.getAssets().open("region_data.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            is.close();

            JSONArray provinceArray = new JSONArray(sb.toString());
            for (int i = 0; i < provinceArray.length(); i++) {
                JSONObject provinceObj = provinceArray.getJSONObject(i);
                String provinceName = provinceObj.getString("name");
                provinces.add(provinceName);

                List<String> cityList = new ArrayList<>();
                List<List<String>> districtList = new ArrayList<>();

                JSONArray cityArray = provinceObj.getJSONArray("cities");
                for (int j = 0; j < cityArray.length(); j++) {
                    JSONObject cityObj = cityArray.getJSONObject(j);
                    String cityName = cityObj.getString("name");
                    cityList.add(cityName);

                    List<String> distList = new ArrayList<>();
                    JSONArray districtArray = cityObj.getJSONArray("districts");
                    for (int k = 0; k < districtArray.length(); k++) {
                        distList.add(districtArray.getString(k));
                    }
                    districtList.add(distList);
                }
                cities.add(cityList);
                districts.add(districtList);
            }
        } catch (Exception e) {
            e.printStackTrace();
            provinces.add("北京市");
            List<String> cityList = new ArrayList<>();
            cityList.add("北京市");
            List<List<String>> districtList = new ArrayList<>();
            List<String> distList = new ArrayList<>();
            distList.add("东城区");
            distList.add("西城区");
            distList.add("朝阳区");
            distList.add("海淀区");
            distList.add("丰台区");
            distList.add("石景山区");
            distList.add("通州区");
            distList.add("昌平区");
            districtList.add(distList);
            cities.add(cityList);
            districts.add(districtList);
        }
    }

    public void show() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.dialog_region_picker, null);

        tvProvinceTab = popupView.findViewById(R.id.tv_province_tab);
        tvCityTab = popupView.findViewById(R.id.tv_city_tab);
        tvDistrictTab = popupView.findViewById(R.id.tv_district_tab);
        llOptionsContainer = popupView.findViewById(R.id.ll_options_container);
        TextView tvCancel = popupView.findViewById(R.id.tv_cancel);
        TextView tvConfirm = popupView.findViewById(R.id.tv_confirm);

        tvProvinceTab.setOnClickListener(v -> {
            currentLevel = 0;
            updateTabs();
            showProvinceOptions();
        });

        tvCityTab.setOnClickListener(v -> {
            currentLevel = 1;
            updateTabs();
            showCityOptions();
        });

        tvDistrictTab.setOnClickListener(v -> {
            currentLevel = 2;
            updateTabs();
            showDistrictOptions();
        });

        tvCancel.setOnClickListener(v -> dismiss());

        tvConfirm.setOnClickListener(v -> {
            if (listener != null) {
                String province = provinces.get(selectedProvinceIndex);
                String city = cities.get(selectedProvinceIndex).get(selectedCityIndex);
                String district = districts.get(selectedProvinceIndex).get(selectedCityIndex).get(selectedDistrictIndex);
                listener.onSelected(province, city, district);
            }
            dismiss();
        });

        ViewGroup rootView = (ViewGroup) ((android.app.Activity) context).findViewById(android.R.id.content);

        dimView = new View(context);
        dimView.setBackgroundColor(0x99000000);
        dimView.setAlpha(0f);
        rootView.addView(dimView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
        popupWindow.setOnDismissListener(() -> {
            if (dimView != null) {
                dimView.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                    ViewGroup parent = (ViewGroup) dimView.getParent();
                    if (parent != null) {
                        parent.removeView(dimView);
                    }
                }).start();
            }
        });

        dimView.animate().alpha(1f).setDuration(200).start();
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);

        currentLevel = 0;
        updateTabs();
        showProvinceOptions();
    }

    private void updateTabs() {
        tvProvinceTab.setTextColor(currentLevel == 0 ? 0xFFFF9800 : 0xFF666666);
        tvProvinceTab.setTextSize(currentLevel == 0 ? 16 : 14);
        tvCityTab.setTextColor(currentLevel == 1 ? 0xFFFF9800 : 0xFF666666);
        tvCityTab.setTextSize(currentLevel == 1 ? 16 : 14);
        tvDistrictTab.setTextColor(currentLevel == 2 ? 0xFFFF9800 : 0xFF666666);
        tvDistrictTab.setTextSize(currentLevel == 2 ? 16 : 14);
    }

    private void showProvinceOptions() {
        llOptionsContainer.removeAllViews();
        for (int i = 0; i < provinces.size(); i++) {
            final int index = i;
            TextView tv = new TextView(context);
            tv.setText(provinces.get(i));
            tv.setTextSize(15);
            tv.setTextColor(i == selectedProvinceIndex ? 0xFFFF9800 : 0xFF333333);
            tv.setPadding(40, 30, 40, 30);
            tv.setOnClickListener(v -> {
                selectedProvinceIndex = index;
                selectedCityIndex = 0;
                selectedDistrictIndex = 0;
                currentLevel = 1;
                updateTabs();
                showCityOptions();
            });
            llOptionsContainer.addView(tv);
        }
    }

    private void showCityOptions() {
        llOptionsContainer.removeAllViews();
        List<String> cityList = cities.get(selectedProvinceIndex);
        for (int i = 0; i < cityList.size(); i++) {
            final int index = i;
            TextView tv = new TextView(context);
            tv.setText(cityList.get(i));
            tv.setTextSize(15);
            tv.setTextColor(i == selectedCityIndex ? 0xFFFF9800 : 0xFF333333);
            tv.setPadding(40, 30, 40, 30);
            tv.setOnClickListener(v -> {
                selectedCityIndex = index;
                selectedDistrictIndex = 0;
                currentLevel = 2;
                updateTabs();
                showDistrictOptions();
            });
            llOptionsContainer.addView(tv);
        }
    }

    private void showDistrictOptions() {
        llOptionsContainer.removeAllViews();
        List<String> districtList = districts.get(selectedProvinceIndex).get(selectedCityIndex);
        for (int i = 0; i < districtList.size(); i++) {
            final int index = i;
            TextView tv = new TextView(context);
            tv.setText(districtList.get(i));
            tv.setTextSize(15);
            tv.setTextColor(i == selectedDistrictIndex ? 0xFFFF9800 : 0xFF333333);
            tv.setPadding(40, 30, 40, 30);
            tv.setOnClickListener(v -> {
                selectedDistrictIndex = index;
                showDistrictOptions();
            });
            llOptionsContainer.addView(tv);
        }
    }

    public void dismiss() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }
}
