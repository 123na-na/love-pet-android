# Debug Session: product-detail-crash

## Session Info
- **Session ID**: product-detail-crash
- **Start Time**: 2026-06-21
- **Status**: [CLOSED - FIXED]

## Symptom
- 点击商品进入详情页后页面空白，然后app强制退出(ANR/Crash)

## Root Cause
**ProductDetailActivity未在AndroidManifest.xml中注册**

## Fix Applied
在AndroidManifest.xml中添加了ProductDetailActivity的注册：
```xml
<activity
    android:name=".ProductDetailActivity"
    android:exported="false" />
```

## Verification
- 编译成功: `./gradlew assembleDebug` 通过

## Conclusion
问题已修复。当Activity未在AndroidManifest.xml中注册时，Android系统无法找到该Activity，导致启动时抛出ActivityNotFoundException崩溃。
