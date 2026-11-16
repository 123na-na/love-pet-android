package com.example.love_pet;

import android.app.Application;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.MapsInitializer;

/**
 * 应用Application类
 * 用于初始化高德地图SDK隐私合规
 */
public class LovePetApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        UserManager.getInstance().loadUsers(this);

        // 高德地图SDK隐私合规设置（必须在调用任何SDK接口前设置）
        // 1. 更新隐私弹窗显示
        AMapLocationClient.updatePrivacyShow(this, true, true);
        // 2. 更新隐私合规声明
        AMapLocationClient.updatePrivacyAgree(this, true);

        // 提高地图SDK性能
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);
    }
}
