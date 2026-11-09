package com.example.love_pet;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Address {
    private static final String PREF_NAME = "addresses";

    private static String getPrefKey(Context context) {
        return PREF_NAME + "_" + UserManager.getInstance().getCurrentUserId();
    }
    private String id;
    private String name;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String tag;
    private boolean isDefault;
    private double latitude;
    private double longitude;

    public Address() {}

    public Address(String id, String name, String phone, String province, String city, String district, String detail, String tag, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detail = detail;
        this.tag = tag;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (province != null && !province.isEmpty()) sb.append(province);
        if (city != null && !city.isEmpty()) sb.append(city);
        if (district != null && !district.isEmpty()) sb.append(district);
        if (detail != null && !detail.isEmpty()) sb.append(detail);
        return sb.toString();
    }

    public static void saveAddresses(Context context, List<Address> addresses) {
        SharedPreferences sp = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        JSONArray jsonArray = new JSONArray();
        for (Address addr : addresses) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("id", addr.getId());
                obj.put("name", addr.getName());
                obj.put("phone", addr.getPhone());
                obj.put("province", addr.getProvince());
                obj.put("city", addr.getCity());
                obj.put("district", addr.getDistrict());
                obj.put("detail", addr.getDetail());
                obj.put("tag", addr.getTag());
                obj.put("isDefault", addr.isDefault());
                obj.put("latitude", addr.getLatitude());
                obj.put("longitude", addr.getLongitude());
                jsonArray.put(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        editor.putString("address_list", jsonArray.toString());
        editor.commit();
    }

    public static List<Address> getAddresses(Context context) {
        SharedPreferences sp = context.getSharedPreferences(getPrefKey(context), Context.MODE_PRIVATE);
        String json = sp.getString("address_list", "");
        if (json.isEmpty()) {
            return getDefaultAddresses();
        }
        List<Address> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Address addr = new Address(
                        obj.optString("id"),
                        obj.optString("name"),
                        obj.optString("phone"),
                        obj.optString("province"),
                        obj.optString("city"),
                        obj.optString("district"),
                        obj.optString("detail"),
                        obj.optString("tag"),
                        obj.optBoolean("isDefault")
                );
                addr.setLatitude(obj.optDouble("latitude", 0));
                addr.setLongitude(obj.optDouble("longitude", 0));
                list.add(addr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultAddresses();
        }
        return list;
    }

    private static List<Address> getDefaultAddresses() {
        List<Address> list = new ArrayList<>();
        list.add(new Address("1", "张三", "138****8888", "广东省", "深圳市", "南山区", "科技园路88号", "家", true));
        return list;
    }

    public static void addAddress(Context context, Address address) {
        List<Address> addresses = getAddresses(context);
        if (address.isDefault()) {
            for (Address addr : addresses) {
                addr.setDefault(false);
            }
        }
        addresses.add(address);
        saveAddresses(context, addresses);
    }

    public static void updateAddress(Context context, Address address) {
        List<Address> addresses = getAddresses(context);
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i).getId().equals(address.getId())) {
                addresses.set(i, address);
                break;
            }
        }
        saveAddresses(context, addresses);
    }

    public static void deleteAddress(Context context, String id) {
        List<Address> addresses = getAddresses(context);
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i).getId().equals(id)) {
                addresses.remove(i);
                break;
            }
        }
        saveAddresses(context, addresses);
    }
}
