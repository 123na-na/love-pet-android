# Debug Session: shop-crash

## Status: FIXED

## Root Cause

`ProductDataImporter.getResourceIdByName()` did not handle the case when `getIdentifier()` returns 0 for non-existent resources (like `img_cat_bai`, `img_cat_orange`, `img_dog_brown`). This caused `ImageView.setImageResource(0)` to crash.

## Fix Applied

Added fallback in `getResourceIdByName()` to return `R.drawable.ic_paw_black` when `getIdentifier()` returns 0.

```java
int resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
// getIdentifier returns 0 if resource not found
return resId != 0 ? resId : R.drawable.ic_paw_black;
```

## Verification

User needs to rebuild and test the shop functionality.