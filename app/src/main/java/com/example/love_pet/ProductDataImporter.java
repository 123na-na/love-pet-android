package com.example.love_pet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品数据导入器
 * 从 assets/products.json 文件导入商品数据
 */
public class ProductDataImporter {

    private Context context;
    private PetDatabaseHelper dbHelper;

    public ProductDataImporter(Context context) {
        this.context = context;
        this.dbHelper = PetDatabaseHelper.getInstance(context);
    }

    /**
     * 检查是否需要导入数据（始终返回true，确保JSON数据始终是最新的）
     */
    public boolean needImport() {
        return true;
    }

    /**
     * 强制重新导入（删除旧数据后重新从JSON导入）
     */
    public int forceReimport() {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.delete(PetDatabaseHelper.TABLE_PRODUCTS, null, null);
            return importFromJson();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 从 JSON 文件导入商品数据（先清空再导入，ID固定为1/2/3/4）
     */
    public int importFromJson() {
        try {
            // 读取 JSON 文件
            String jsonContent = loadJsonFromAssets("products.json");
            JSONObject jsonObject = new JSONObject(jsonContent);
            JSONArray productsArray = jsonObject.getJSONArray("products");

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            // 先清空旧数据，确保每次导入都是干净的
            db.delete(PetDatabaseHelper.TABLE_PRODUCTS, null, null);

            int importedCount = 0;
            for (int i = 0; i < productsArray.length(); i++) {
                JSONObject productObj = productsArray.getJSONObject(i);

                ContentValues values = new ContentValues();
                // 使用固定 ID：1, 2, 3, 4 ...
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_ID, i + 1);
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_TITLE, getStringValue(productObj, "title"));
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_SPEC, getStringValue(productObj, "spec"));
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_PRICE, getStringValue(productObj, "price"));
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_ORIGINAL_PRICE, getStringValue(productObj, "originalPrice"));
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_LOCATION, getStringValue(productObj, "location"));
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_TAGS, getStringValue(productObj, "tags"));
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_SHOP_NAME, getStringValue(productObj, "shopName"));
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_BRAND, getStringValue(productObj, "brand"));
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_IMAGE_RES, getStringValue(productObj, "imageRes"));
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_CATEGORY, getStringValue(productObj, "category"));
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_DETAIL_TEXT, getStringValue(productObj, "detailText"));
                // JSON字段直接存字符串
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_IMAGES,
                        productObj.has("images") ? productObj.getJSONArray("images").toString() : "");
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_STYLE_IMAGES,
                        productObj.has("styleImages") ? productObj.getJSONArray("styleImages").toString() : "");
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_LOGISTICS,
                        productObj.has("logistics") ? productObj.getJSONObject("logistics").toString() : "");
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_SERVICES,
                        productObj.has("services") ? productObj.getJSONArray("services").toString() : "");
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_SPECS,
                        productObj.has("specs") ? productObj.getJSONArray("specs").toString() : "");
                values.put(PetDatabaseHelper.COLUMN_PRODUCT_STYLES,
                        productObj.has("styles") ? productObj.getJSONArray("styles").toString() : "");

                db.insertWithOnConflict(PetDatabaseHelper.TABLE_PRODUCTS, null, values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                importedCount++;
            }

            db.setTransactionSuccessful();
            db.endTransaction();

            return importedCount;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 加载 assets 中的 JSON 文件
     */
    private String loadJsonFromAssets(String fileName) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        is.close();
        return sb.toString();
    }

    /**
     * 安全获取 JSON 字符串值
     */
    private String getStringValue(JSONObject obj, String key) {
        try {
            return obj.getString(key);
        } catch (JSONException e) {
            return "";
        }
    }

    /**
     * 通过ID获取单个商品
     */
    public ShopActivity.Product getProductById(int productId) {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(
                    PetDatabaseHelper.TABLE_PRODUCTS,
                    null,
                    PetDatabaseHelper.COLUMN_PRODUCT_ID + " = ?",
                    new String[] { String.valueOf(productId) },
                    null, null, null);

            ShopActivity.Product product = null;
            if (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_TITLE));
                String spec = cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_SPEC));
                String price = cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_PRICE));
                String location = cursor
                        .getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_LOCATION));
                String tags = cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_TAGS));
                String shopName = cursor
                        .getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_SHOP_NAME));
                String brand = cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_BRAND));
                String imageRes = cursor
                        .getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_IMAGE_RES));

                // 获取图片资源ID
                int imageResId = getResourceIdByName(imageRes);

                product = new ShopActivity.Product(id, title, spec, price, location, tags, shopName, brand, imageResId);
            }
            cursor.close();
            return product;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取所有商品
     */
    public List<ShopActivity.Product> getAllProducts() {
        List<ShopActivity.Product> products = new ArrayList<>();
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(
                    PetDatabaseHelper.TABLE_PRODUCTS,
                    null,
                    null, null, null, null, null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_TITLE));
                String spec = cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_SPEC));
                String price = cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_PRICE));
                String location = cursor
                        .getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_LOCATION));
                String tags = cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_TAGS));
                String shopName = cursor
                        .getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_SHOP_NAME));
                String brand = cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_BRAND));
                String imageRes = cursor
                        .getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_IMAGE_RES));

                // 获取图片资源ID
                int imageResId = getResourceIdByName(imageRes);

                products.add(
                        new ShopActivity.Product(id, title, spec, price, location, tags, shopName, brand, imageResId));
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * 通过资源名称获取资源ID
     */
    private int getResourceIdByName(String resourceName) {
        if (resourceName == null || resourceName.isEmpty()) {
            return R.drawable.ic_paw_black;
        }
        try {
            int resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
            return resId != 0 ? resId : R.drawable.ic_paw_black;
        } catch (Exception e) {
            return R.drawable.ic_paw_black;
        }
    }

    /**
     * 商品详情完整数据（供ProductDetailActivity使用）
     */
    public static class ProductDetail {
        public int id;
        public String title, spec, price, originalPrice, location, tags, shopName, brand, detailText;
        public int mainImageResId;          // 主图资源ID
        public String[] imageNames;         // 轮播图资源名称数组
        public int[] imageResIds;           // 轮播图资源ID数组
        public int[] styleImageResIds;      // 款式预览图资源ID数组
        // 物流信息
        public String logisticsCompany, logisticsTime, logisticsLocation, logisticsShipping;
        // 服务保障（JSON字符串 + 解析后的标题列表）
        public String servicesJson;
        public List<String> serviceTitles = new ArrayList<>();
        // 规格参数（JSON字符串 + 解析后的标签和值列表）
        public String specsJson;
        public List<String> specLabels = new ArrayList<>();
        public List<String> specValues = new ArrayList<>();
        // 款式选项（JSON字符串）
        public String stylesJson;

        /** 解析图片JSON数组为资源ID数组 */
        public static int[] parseImageIds(Context ctx, String jsonStr) {
            if (jsonStr == null || jsonStr.isEmpty()) return new int[0];
            try {
                JSONArray arr = new JSONArray(jsonStr);
                int[] ids = new int[arr.length()];
                for (int i = 0; i < arr.length(); i++) {
                    String name = arr.getString(i);
                    ids[i] = getResourceIdByNameStatic(ctx, name);
                }
                return ids;
            } catch (Exception e) {
                return new int[0];
            }
        }

        private static int getResourceIdByNameStatic(Context ctx, String name) {
            if (name == null || name.isEmpty()) return R.drawable.ic_paw_black;
            try {
                int resId = ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
                return resId != 0 ? resId : R.drawable.ic_paw_black;
            } catch (Exception e) {
                return R.drawable.ic_paw_black;
            }
        }
    }

    /**
     * 通过ID获取商品完整详情数据
     */
    public ProductDetail getProductDetailById(int productId) {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.query(PetDatabaseHelper.TABLE_PRODUCTS,
                    null,
                    PetDatabaseHelper.COLUMN_PRODUCT_ID + " = ?",
                    new String[]{String.valueOf(productId)},
                    null, null, null);

            ProductDetail detail = null;
            if (cursor.moveToNext()) {
                detail = new ProductDetail();
                detail.id = cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_ID));
                detail.title = cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_TITLE));
                detail.spec = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_SPEC);
                detail.price = cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PRODUCT_PRICE));
                detail.originalPrice = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_ORIGINAL_PRICE);
                detail.location = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_LOCATION);
                detail.tags = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_TAGS);
                detail.shopName = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_SHOP_NAME);
                detail.brand = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_BRAND);
                detail.detailText = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_DETAIL_TEXT);

                // 主图
                String imageRes = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_IMAGE_RES);
                detail.mainImageResId = getResourceIdByName(imageRes);

                // 轮播图列表
                String imagesJson = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_IMAGES);
                detail.imageResIds = ProductDetail.parseImageIds(context, imagesJson);
                // 解析图片名称
                if (!imagesJson.isEmpty()) {
                    try {
                        JSONArray imgArr = new JSONArray(imagesJson);
                        detail.imageNames = new String[imgArr.length()];
                        for (int i = 0; i < imgArr.length(); i++)
                            detail.imageNames[i] = imgArr.getString(i);
                    } catch (Exception e) { detail.imageNames = new String[0]; }
                }
                if (detail.imageResIds.length == 0 && detail.mainImageResId != 0) {
                    detail.imageResIds = new int[]{detail.mainImageResId};
                    if (detail.imageNames == null || detail.imageNames.length == 0)
                        detail.imageNames = new String[]{imageRes};
                }

                // 款式预览图
                String styleImagesJson = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_STYLE_IMAGES);
                detail.styleImageResIds = ProductDetail.parseImageIds(context, styleImagesJson);

                // 物流信息
                String logisticsJson = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_LOGISTICS);
                if (!logisticsJson.isEmpty()) {
                    JSONObject logObj = new JSONObject(logisticsJson);
                    detail.logisticsCompany = logObj.optString("company", "");
                    detail.logisticsTime = logObj.optString("time", "");
                    detail.logisticsLocation = logObj.optString("location", "");
                    detail.logisticsShipping = logObj.optString("shipping", "");
                }

                // 服务保障和规格参数（存为JSON字符串 + 解析为列表）
                detail.servicesJson = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_SERVICES);
                if (!detail.servicesJson.isEmpty()) {
                    try {
                        JSONArray svcArr = new JSONArray(detail.servicesJson);
                        for (int i = 0; i < svcArr.length(); i++) {
                            detail.serviceTitles.add(svcArr.getJSONObject(i).optString("title", ""));
                        }
                    } catch (Exception ignored) {}
                }

                detail.specsJson = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_SPECS);
                if (!detail.specsJson.isEmpty()) {
                    try {
                        JSONArray specArr = new JSONArray(detail.specsJson);
                        for (int i = 0; i < specArr.length(); i++) {
                            JSONObject sp = specArr.getJSONObject(i);
                            detail.specLabels.add(sp.optString("label", ""));
                            detail.specValues.add(sp.optString("value", ""));
                        }
                    } catch (Exception ignored) {}
                }

                detail.stylesJson = getStringSafe(cursor, PetDatabaseHelper.COLUMN_PRODUCT_STYLES);
            }
            cursor.close();
            return detail;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getStringSafe(Cursor cursor, String column) {
        int idx = cursor.getColumnIndex(column);
        if (idx < 0) return "";
        return cursor.getString(idx);
    }
}