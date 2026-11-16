package com.example.love_pet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库辅助类
 */
public class PetDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pet_notes.db";
    private static final int DATABASE_VERSION = 12;

    // 表名
    public static final String TABLE_PET_NOTES = "pet_notes";
    public static final String TABLE_COMMENTS = "comments";
    public static final String TABLE_FOLLOWING = "following";  // 关注表

    // 宠物笔记表字段
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AUTHOR_NAME = "author_name";
    public static final String COLUMN_AVATAR_RES = "avatar_res";
    public static final String COLUMN_IMAGE_RES = "image_res";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_PUBLISH_TIME = "publish_time";
    public static final String COLUMN_LIKE_COUNT = "like_count";
    public static final String COLUMN_COLLECT_COUNT = "collect_count";
    public static final String COLUMN_COMMENT_COUNT = "comment_count";
    public static final String COLUMN_IS_LIKED = "is_liked";
    public static final String COLUMN_IS_COLLECTED = "is_collected";

    // 评论表字段
    public static final String COLUMN_COMMENT_ID = "id";
    public static final String COLUMN_NOTE_ID = "note_id";
    public static final String COLUMN_COMMENT_USERNAME = "username";
    public static final String COLUMN_COMMENT_CONTENT = "content";
    public static final String COLUMN_COMMENT_TIME = "time";
    public static final String COLUMN_COMMENT_IP = "ip_location";
    public static final String COLUMN_COMMENT_LIKE_COUNT = "comment_like_count";
    public static final String COLUMN_COMMENT_REPLY_TO = "reply_to";
    public static final String COLUMN_COMMENT_PARENT_ID = "parent_comment_id";
    public static final String COLUMN_COMMENT_IS_USER = "is_user_comment"; // 是否为用户发表的评论
    public static final String COLUMN_COMMENT_IMAGE_PATH = "image_path"; // 评论图片路径

    // 关注表字段
    public static final String COLUMN_FOLLOWING_ID = "id";
    public static final String COLUMN_FOLLOWING_USERNAME = "username";
    public static final String COLUMN_FOLLOWING_AVATAR = "avatar_res";

    private static PetDatabaseHelper instance;  // 单例实例

    // 创建宠物笔记表SQL
    private static final String CREATE_TABLE_PET_NOTES =
            "CREATE TABLE " + TABLE_PET_NOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_AUTHOR_NAME + " TEXT, " +
                    COLUMN_AVATAR_RES + " TEXT, " +           // 存储资源名称（字符串）
                    COLUMN_IMAGE_RES + " TEXT, " +           // 存储资源名称（字符串）
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_PUBLISH_TIME + " TEXT, " +
                    COLUMN_LIKE_COUNT + " INTEGER, " +
                    COLUMN_COLLECT_COUNT + " INTEGER, " +
                    COLUMN_COMMENT_COUNT + " INTEGER, " +
                    COLUMN_IS_LIKED + " INTEGER DEFAULT 0, " +
                    COLUMN_IS_COLLECTED + " INTEGER DEFAULT 0" +
                    ")";

    // 创建评论表SQL
    private static final String CREATE_TABLE_COMMENTS =
            "CREATE TABLE " + TABLE_COMMENTS + " (" +
                    COLUMN_COMMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOTE_ID + " INTEGER, " +
                    COLUMN_COMMENT_USERNAME + " TEXT, " +
                    COLUMN_COMMENT_CONTENT + " TEXT, " +
                    COLUMN_COMMENT_TIME + " TEXT, " +
                    COLUMN_COMMENT_IP + " TEXT, " +
                    COLUMN_COMMENT_LIKE_COUNT + " INTEGER, " +
                    COLUMN_COMMENT_REPLY_TO + " TEXT, " +
                    COLUMN_COMMENT_PARENT_ID + " INTEGER DEFAULT -1, " +
                    COLUMN_COMMENT_IS_USER + " INTEGER DEFAULT 0, " +
                    COLUMN_COMMENT_IMAGE_PATH + " TEXT" +
                    ")";

    // 创建关注表SQL
    private static final String CREATE_TABLE_FOLLOWING =
            "CREATE TABLE " + TABLE_FOLLOWING + " (" +
                    COLUMN_FOLLOWING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FOLLOWING_USERNAME + " TEXT UNIQUE, " +
                    COLUMN_FOLLOWING_AVATAR + " TEXT" +
                    ")";

    // 商品表
    public static final String TABLE_PRODUCTS = "products";
    
    // 商品表字段
    public static final String COLUMN_PRODUCT_ID = "id";
    public static final String COLUMN_PRODUCT_TITLE = "title";
    public static final String COLUMN_PRODUCT_SPEC = "spec";
    public static final String COLUMN_PRODUCT_PRICE = "price";
    public static final String COLUMN_PRODUCT_ORIGINAL_PRICE = "original_price";
    public static final String COLUMN_PRODUCT_LOCATION = "location";
    public static final String COLUMN_PRODUCT_TAGS = "tags";
    public static final String COLUMN_PRODUCT_SHOP_NAME = "shop_name";
    public static final String COLUMN_PRODUCT_BRAND = "brand";
    public static final String COLUMN_PRODUCT_IMAGE_RES = "image_res";
    public static final String COLUMN_PRODUCT_CATEGORY = "category";
    // 详情页扩展字段
    public static final String COLUMN_PRODUCT_DETAIL_TEXT = "detail_text";
    // 图片列表（JSON数组，存储多个图片资源名）
    public static final String COLUMN_PRODUCT_IMAGES = "images";
    // 款式预览图（JSON数组）
    public static final String COLUMN_PRODUCT_STYLE_IMAGES = "style_images";
    // 物流信息（JSON对象：company, time, location, shipping）
    public static final String COLUMN_PRODUCT_LOGISTICS = "logistics";
    // 服务保障（JSON数组）
    public static final String COLUMN_PRODUCT_SERVICES = "services";
    // 规格参数（JSON数组，每项含label和value）
    public static final String COLUMN_PRODUCT_SPECS = "specs";
    // 款式选项（JSON数组，每项含name, imageRes, price, originalPrice）
    public static final String COLUMN_PRODUCT_STYLES = "styles";

    // 创建商品表SQL
    private static final String CREATE_TABLE_PRODUCTS =
            "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                    COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PRODUCT_TITLE + " TEXT, " +
                    COLUMN_PRODUCT_SPEC + " TEXT, " +
                    COLUMN_PRODUCT_PRICE + " TEXT, " +
                    COLUMN_PRODUCT_ORIGINAL_PRICE + " TEXT, " +
                    COLUMN_PRODUCT_LOCATION + " TEXT, " +
                    COLUMN_PRODUCT_TAGS + " TEXT, " +
                    COLUMN_PRODUCT_SHOP_NAME + " TEXT, " +
                    COLUMN_PRODUCT_BRAND + " TEXT, " +
                    COLUMN_PRODUCT_IMAGE_RES + " TEXT, " +
                    COLUMN_PRODUCT_CATEGORY + " TEXT, " +
                    COLUMN_PRODUCT_DETAIL_TEXT + " TEXT, " +
                    COLUMN_PRODUCT_IMAGES + " TEXT, " +
                    COLUMN_PRODUCT_STYLE_IMAGES + " TEXT, " +
                    COLUMN_PRODUCT_LOGISTICS + " TEXT, " +
                    COLUMN_PRODUCT_SERVICES + " TEXT, " +
                    COLUMN_PRODUCT_SPECS + " TEXT, " +
                    COLUMN_PRODUCT_STYLES + " TEXT" +
                    ")";

    // 收藏表
    public static final String TABLE_FAVORITES = "favorites";
    
    // 收藏表字段
    public static final String COLUMN_FAVORITE_ID = "id";
    public static final String COLUMN_FAVORITE_PRODUCT_ID = "product_id";
    public static final String COLUMN_FAVORITE_TITLE = "title";
    public static final String COLUMN_FAVORITE_PRICE = "price";
    public static final String COLUMN_FAVORITE_IMAGE_RES = "image_res";
    public static final String COLUMN_FAVORITE_ADDED_TIME = "added_time";

    // 创建收藏表SQL
    private static final String CREATE_TABLE_FAVORITES =
            "CREATE TABLE " + TABLE_FAVORITES + " (" +
                    COLUMN_FAVORITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FAVORITE_PRODUCT_ID + " INTEGER UNIQUE, " +
                    COLUMN_FAVORITE_TITLE + " TEXT, " +
                    COLUMN_FAVORITE_PRICE + " REAL, " +
                    COLUMN_FAVORITE_IMAGE_RES + " INTEGER, " +
                    COLUMN_FAVORITE_ADDED_TIME + " TEXT" +
                    ")";

    // 购物车表
    public static final String TABLE_CART = "cart";
    
    // 购物车表字段
    public static final String COLUMN_CART_ID = "id";
    public static final String COLUMN_CART_PRODUCT_ID = "product_id";
    public static final String COLUMN_CART_TITLE = "title";
    public static final String COLUMN_CART_PRICE = "price";
    public static final String COLUMN_CART_IMAGE_RES = "image_res";
    public static final String COLUMN_CART_QUANTITY = "quantity";

    // 创建购物车表SQL
    private static final String CREATE_TABLE_CART =
            "CREATE TABLE " + TABLE_CART + " (" +
                    COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CART_PRODUCT_ID + " INTEGER UNIQUE, " +
                    COLUMN_CART_TITLE + " TEXT, " +
                    COLUMN_CART_PRICE + " REAL, " +
                    COLUMN_CART_IMAGE_RES + " INTEGER, " +
                    COLUMN_CART_QUANTITY + " INTEGER DEFAULT 1" +
                    ")";

    public static synchronized PetDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PetDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private PetDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PET_NOTES);
        db.execSQL(CREATE_TABLE_COMMENTS);
        db.execSQL(CREATE_TABLE_FOLLOWING);
        db.execSQL(CREATE_TABLE_PRODUCTS);
        db.execSQL(CREATE_TABLE_FAVORITES);
        db.execSQL(CREATE_TABLE_CART);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PET_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLLOWING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        onCreate(db);
    }
}