package com.example.love_pet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库管理器
 */
public class PetDataManager {

    private static PetDataManager instance;
    private PetDatabaseHelper dbHelper;
    private Context context;

    // 现代风格昵称池（与头像一一对应，共19组，固定配对不变）
    public static final String[] MODERN_NICKNAMES = {
            "元气少女", // avatar[0] - feature1
            "酷盖本盖", // avatar[1] - feature2
            "小确幸", // avatar[2] - feature4
            "人间理想", // avatar[3] - feature5
            "星河滚烫", // avatar[4] - feature6
            "温柔以待", // avatar[5] - feature7
            "日常贩卖", // avatar[6] - feature8
            "可盐可甜", // avatar[7] - feature9
            "限定可爱", // avatar[8] - feature10
            "今日份甜", // avatar[9] - feature11
            "生活碎片", // avatar[10] - feature12
            "午后阳光", // avatar[11] - feature13
            "心动预警", // avatar[12] - feature14
            "猫咪控", // avatar[13] - color1 (粉橙)
            "铲屎官", // avatar[14] - color2 (蓝青)
            "毛孩子妈", // avatar[15] - color3 (绿青)
            "宠物达人", // avatar[16] - color4 (紫粉)
            "萌宠日记", // avatar[17] - color5 (黄橙)
            "爱宠一族" // avatar[18] - color6 (青蓝)
    };

    // 按数字顺序排列的头像资源名（13张图片 + 6个彩色XML）
    private static final String[] AVATAR_FIELD_NAMES = {
            "feature1", "feature2", "feature4",
            "feature5", "feature6", "feature7",
            "feature8", "feature9", "feature10",
            "feature11", "feature12", "feature13",
            "feature14",
            "avatar_color1", "avatar_color2", "avatar_color3",
            "avatar_color4", "avatar_color5", "avatar_color6"
    };

    // 缓存解析后的头像资源ID
    private int[] cachedAvatarResIds;

    /**
     * 通过资源名称获取资源ID（动态获取当前编译的资源ID）
     */
    public int getResourceIdByName(String resourceName) {
        if (resourceName == null || resourceName.isEmpty()) {
            return R.drawable.ic_paw_black;
        }
        try {
            return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
        } catch (Exception e) {
            return R.drawable.ic_paw_black;
        }
    }

    /**
     * 通过反射从 R.drawable 获取头像资源ID（带缓存）
     */
    private int getAvatarResourceId(int index) {
        if (cachedAvatarResIds == null) {
            cachedAvatarResIds = new int[AVATAR_FIELD_NAMES.length];
            try {
                Field[] fields = R.drawable.class.getDeclaredFields();
                for (int i = 0; i < AVATAR_FIELD_NAMES.length; i++) {
                    String targetName = AVATAR_FIELD_NAMES[i];
                    int foundId = 0;
                    for (Field f : fields) {
                        if (f.getName().equals(targetName)) {
                            foundId = f.getInt(null);
                            break;
                        }
                    }
                    cachedAvatarResIds[i] = (foundId != 0) ? foundId : R.drawable.ic_paw_black;
                }
            } catch (Exception e) {
                e.printStackTrace();
                for (int i = 0; i < cachedAvatarResIds.length; i++) {
                    cachedAvatarResIds[i] = R.drawable.ic_paw_black;
                }
            }
        }
        return cachedAvatarResIds[index % cachedAvatarResIds.length];
    }

    /**
     * 公开方法：根据索引获取头像资源ID（供DetailActivity使用）
     */
    public int getAvatarResourceByIndex(int index) {
        return getAvatarResourceId(index);
    }

    private PetDataManager(Context context) {
        this.context = context;
        this.dbHelper = PetDatabaseHelper.getInstance(context);
    }

    public static synchronized PetDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new PetDataManager(context);
        }
        return instance;
    }

    /**
     * 帖子图片资源名称数组
     */
    public static final String[] POST_IMAGE_NAMES = {
            "post1", "post2", "post3", "post4", "post5", "post6", "post7"
    };

    /**
     * 同步初始化数据（只在首次运行时生成，数据永久存储）
     */
    public List<PetNoteEntity> initializeDataSync(int noteCount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 检查是否已有数据，有则直接返回，不再重新生成
        int existingCount = getNoteCount();
        if (existingCount > 0) {
            return getAllNotes();
        }

        // 首次运行：生成初始数据（只执行一次，永久存储）
        db.beginTransaction();
        try {
            List<PetNoteEntity> notes = new ArrayList<>();

            // 生成笔记
            for (int i = 0; i < noteCount; i++) {
                // 固定选择头像和昵称（一一对应，每次运行结果相同）
                int avatarIndex = i % MODERN_NICKNAMES.length;
                String avatarName = AVATAR_FIELD_NAMES[avatarIndex]; // 存储资源名称
                String nickname = MODERN_NICKNAMES[avatarIndex];

                // 固定选择帖子图片（存储资源名称）
                String imageName = POST_IMAGE_NAMES[i % POST_IMAGE_NAMES.length];

                // 基于索引的伪随机数据（同一帖子每次结果相同）
                java.util.Random fixedRand = new java.util.Random(i * 1000 + 42);
                int likeCount = 500 + fixedRand.nextInt(9501);
                int collectCount = 1000 + fixedRand.nextInt(7001);
                int commentCount = 5 + fixedRand.nextInt(2); // 5-6条评论
                String location = MockPetData.CITY_POOL[fixedRand.nextInt(MockPetData.CITY_POOL.length)];
                // 时间也用固定种子，确保每次相同
                String publishTime = getFixedPublishTime(i);
                String title = MockPetData.TITLE_POOL[fixedRand.nextInt(MockPetData.TITLE_POOL.length)] + (i * 137 + 1);
                String content = MockPetData.CONTENT_POOL[fixedRand.nextInt(MockPetData.CONTENT_POOL.length)];

                // 插入笔记（存储资源名称）
                ContentValues noteValues = new ContentValues();
                noteValues.put(PetDatabaseHelper.COLUMN_TITLE, title);
                noteValues.put(PetDatabaseHelper.COLUMN_AUTHOR_NAME, nickname);
                noteValues.put(PetDatabaseHelper.COLUMN_AVATAR_RES, avatarName); // 存储名称
                noteValues.put(PetDatabaseHelper.COLUMN_IMAGE_RES, imageName); // 存储名称
                noteValues.put(PetDatabaseHelper.COLUMN_CONTENT, content);
                noteValues.put(PetDatabaseHelper.COLUMN_LOCATION, location);
                noteValues.put(PetDatabaseHelper.COLUMN_PUBLISH_TIME, publishTime);
                noteValues.put(PetDatabaseHelper.COLUMN_LIKE_COUNT, likeCount);
                noteValues.put(PetDatabaseHelper.COLUMN_COLLECT_COUNT, collectCount);
                noteValues.put(PetDatabaseHelper.COLUMN_COMMENT_COUNT, commentCount);
                noteValues.put(PetDatabaseHelper.COLUMN_IS_LIKED, 0);
                long noteId = db.insert(PetDatabaseHelper.TABLE_PET_NOTES, null, noteValues);

                // 生成评论（基于帖子索引+评论序号的固定种子）
                for (int j = 0; j < commentCount; j++) {
                    java.util.Random commentRand = new java.util.Random(i * 10000 + j * 77 + 13);
                    ContentValues commentValues = new ContentValues();
                    commentValues.put(PetDatabaseHelper.COLUMN_NOTE_ID, noteId);
                    // 评论用户也使用同一昵称池
                    int commenterIndex = commentRand.nextInt(MODERN_NICKNAMES.length);
                    commentValues.put(PetDatabaseHelper.COLUMN_COMMENT_USERNAME,
                            MODERN_NICKNAMES[commenterIndex]);
                    commentValues.put(PetDatabaseHelper.COLUMN_COMMENT_CONTENT,
                            MockPetData.COMMENT_CONTENT_POOL[commentRand
                                    .nextInt(MockPetData.COMMENT_CONTENT_POOL.length)]);
                    commentValues.put(PetDatabaseHelper.COLUMN_COMMENT_TIME, getFixedCommentTime(i, j));
                    commentValues.put(PetDatabaseHelper.COLUMN_COMMENT_IP,
                            MockPetData.CITY_POOL[commentRand.nextInt(MockPetData.CITY_POOL.length)]);
                    commentValues.put(PetDatabaseHelper.COLUMN_COMMENT_LIKE_COUNT, commentRand.nextInt(4));
                    db.insert(PetDatabaseHelper.TABLE_COMMENTS, null, commentValues);
                }

                // 创建实体对象
                PetNoteEntity note = new PetNoteEntity();
                note.setId((int) noteId);
                note.setTitle(title);
                note.setAuthorName(nickname);
                note.setAvatarRes(avatarName); // 使用资源名称
                note.setImageRes(imageName); // 使用资源名称
                note.setContent(content);
                note.setLocation(location);
                note.setPublishTime(publishTime);
                note.setLikeCount(likeCount);
                note.setCollectCount(collectCount);
                note.setCommentCount(commentCount);
                note.setLiked(false);
                notes.add(note);
            }

            db.setTransactionSuccessful();
            return notes;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * 获取笔记总数
     */
    public int getNoteCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + PetDatabaseHelper.TABLE_PET_NOTES, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * 获取所有笔记（从数据库读取）
     */
    public List<PetNoteEntity> getAllNotes() {
        List<PetNoteEntity> notes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                PetDatabaseHelper.TABLE_PET_NOTES,
                null, null, null, null, null,
                PetDatabaseHelper.COLUMN_ID + " DESC");
        while (cursor.moveToNext()) {
            PetNoteEntity note = new PetNoteEntity();
            note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_ID)));
            note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_TITLE)));
            note.setAuthorName(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_AUTHOR_NAME)));
            note.setAvatarRes(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_AVATAR_RES))); // 字符串
            note.setImageRes(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_IMAGE_RES))); // 字符串
            note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_CONTENT)));
            note.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_LOCATION)));
            note.setPublishTime(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PUBLISH_TIME)));
            note.setLikeCount(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_LIKE_COUNT)));
            note.setCollectCount(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_COLLECT_COUNT)));
            note.setCommentCount(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_COMMENT_COUNT)));
            note.setLiked(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_IS_LIKED)) == 1);
            note.setCollected(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_IS_COLLECTED)) == 1);
            notes.add(note);
        }
        cursor.close();
        return notes;
    }

    /**
     * 获取某笔记的所有评论
     */
    public List<Comment> getCommentsByNoteId(int noteId) {
        List<Comment> comments = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                PetDatabaseHelper.TABLE_COMMENTS,
                null,
                PetDatabaseHelper.COLUMN_NOTE_ID + " = ?",
                new String[] { String.valueOf(noteId) },
                null, null, null);

        while (cursor.moveToNext()) {
            Comment comment = new Comment();
            // 读取评论ID
            comment.setId(cursor.getLong(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_COMMENT_ID)));
            comment.setUsername(
                    cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_COMMENT_USERNAME)));
            comment.setContent(
                    cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_COMMENT_CONTENT)));
            comment.setTime(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_COMMENT_TIME)));
            // IP归属地
            int ipIndex = cursor.getColumnIndex(PetDatabaseHelper.COLUMN_COMMENT_IP);
            if (ipIndex >= 0) {
                comment.setIpLocation(cursor.getString(ipIndex));
            } else {
                comment.setIpLocation("未知");
            }
            // 评论点赞数
            int likeIndex = cursor.getColumnIndex(PetDatabaseHelper.COLUMN_COMMENT_LIKE_COUNT);
            if (likeIndex >= 0) {
                comment.setLikeCount(cursor.getInt(likeIndex));
            } else {
                comment.setLikeCount(0);
            }
            // 回复目标用户名
            int replyToIndex = cursor.getColumnIndex(PetDatabaseHelper.COLUMN_COMMENT_REPLY_TO);
            if (replyToIndex >= 0) {
                String replyTo = cursor.getString(replyToIndex);
                if (replyTo != null && !replyTo.isEmpty()) {
                    comment.setReplyTo(replyTo);
                }
            }
            // 父评论ID
            int parentIdIndex = cursor.getColumnIndex(PetDatabaseHelper.COLUMN_COMMENT_PARENT_ID);
            if (parentIdIndex >= 0) {
                comment.setParentCommentId(cursor.getLong(parentIdIndex));
            }
            // 评论图片路径
            int imagePathIndex = cursor.getColumnIndex(PetDatabaseHelper.COLUMN_COMMENT_IMAGE_PATH);
            if (imagePathIndex >= 0) {
                String imagePath = cursor.getString(imagePathIndex);
                if (imagePath != null && !imagePath.isEmpty()) {
                    comment.setImagePath(imagePath);
                }
            }
            comments.add(comment);
        }
        cursor.close();
        return comments;
    }

    /**
     * 更新笔记点赞状态
     */
    public void updateLikeStatus(int noteId, boolean isLiked, int likeCount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PetDatabaseHelper.COLUMN_IS_LIKED, isLiked ? 1 : 0);
        values.put(PetDatabaseHelper.COLUMN_LIKE_COUNT, likeCount);
        db.update(PetDatabaseHelper.TABLE_PET_NOTES, values,
                PetDatabaseHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(noteId) });
    }

    public void updateCollectStatus(int noteId, boolean isCollected, int collectCount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PetDatabaseHelper.COLUMN_IS_COLLECTED, isCollected ? 1 : 0);
        values.put(PetDatabaseHelper.COLUMN_COLLECT_COUNT, collectCount);
        db.update(PetDatabaseHelper.TABLE_PET_NOTES, values,
                PetDatabaseHelper.COLUMN_ID + " = ?", new String[] { String.valueOf(noteId) });
    }

    /**
     * 关注用户
     */
    public void followUser(String username, String avatarRes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PetDatabaseHelper.COLUMN_FOLLOWING_USERNAME, username);
        values.put(PetDatabaseHelper.COLUMN_FOLLOWING_AVATAR, avatarRes);
        try {
            db.insertOrThrow(PetDatabaseHelper.TABLE_FOLLOWING, null, values);
        } catch (Exception e) {
            // 用户已存在，忽略
        }
    }

    /**
     * 取消关注用户
     */
    public void unfollowUser(String username) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(PetDatabaseHelper.TABLE_FOLLOWING,
                PetDatabaseHelper.COLUMN_FOLLOWING_USERNAME + " = ?",
                new String[] { username });
    }

    /**
     * 判断是否已关注某用户
     */
    public boolean isFollowing(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                PetDatabaseHelper.TABLE_FOLLOWING,
                null,
                PetDatabaseHelper.COLUMN_FOLLOWING_USERNAME + " = ?",
                new String[] { username },
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * 获取关注的用户列表
     */
    public List<String> getFollowingUsernames() {
        List<String> usernames = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                PetDatabaseHelper.TABLE_FOLLOWING,
                new String[] { PetDatabaseHelper.COLUMN_FOLLOWING_USERNAME },
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            usernames.add(cursor.getString(0));
        }
        cursor.close();
        return usernames;
    }

    /**
     * 获取关注用户的帖子
     */
    public List<PetNoteEntity> getFollowingNotes() {
        List<String> followingUsernames = getFollowingUsernames();
        if (followingUsernames.isEmpty()) {
            return new ArrayList<>();
        }

        StringBuilder selection = new StringBuilder();
        selection.append(PetDatabaseHelper.COLUMN_AUTHOR_NAME).append(" IN (");
        for (int i = 0; i < followingUsernames.size(); i++) {
            if (i > 0) {
                selection.append(",");
            }
            selection.append("?");
        }
        selection.append(")");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                PetDatabaseHelper.TABLE_PET_NOTES,
                null,
                selection.toString(),
                followingUsernames.toArray(new String[0]),
                null, null,
                PetDatabaseHelper.COLUMN_PUBLISH_TIME + " DESC");

        List<PetNoteEntity> notes = new ArrayList<>();
        while (cursor.moveToNext()) {
            PetNoteEntity note = new PetNoteEntity();
            note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_ID)));
            note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_TITLE)));
            note.setAuthorName(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_AUTHOR_NAME)));
            note.setAvatarRes(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_AVATAR_RES)));
            note.setImageRes(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_IMAGE_RES)));
            note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_CONTENT)));
            note.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_LOCATION)));
            note.setPublishTime(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PUBLISH_TIME)));
            note.setLikeCount(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_LIKE_COUNT)));
            note.setCollectCount(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_COLLECT_COUNT)));
            note.setCommentCount(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_COMMENT_COUNT)));
            note.setLiked(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_IS_LIKED)) == 1);
            notes.add(note);
        }
        cursor.close();
        return notes;
    }

    /**
     * 清除所有数据（用于数据损坏时重新生成）
     */
    public void clearAllData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(PetDatabaseHelper.TABLE_COMMENTS, null, null);
        db.delete(PetDatabaseHelper.TABLE_PET_NOTES, null, null);
    }

    /**
     * 插入一条新评论到数据库（用户发送的评论）
     * 
     * @param noteId          帖子ID
     * @param username        评论者昵称
     * @param content         评论内容
     * @param ipLocation      IP归属地
     * @param replyTo         回复的目标用户名，null表示直接评论
     * @param parentCommentId 父评论ID（-1表示顶级评论）
     * @param imagePath       评论图片路径，null表示无图片
     */
    public void insertComment(int noteId, String username, String content, String ipLocation,
            String replyTo, long parentCommentId, String imagePath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PetDatabaseHelper.COLUMN_NOTE_ID, noteId);
        values.put(PetDatabaseHelper.COLUMN_COMMENT_USERNAME, username);
        values.put(PetDatabaseHelper.COLUMN_COMMENT_CONTENT, content);
        values.put(PetDatabaseHelper.COLUMN_COMMENT_TIME, "刚刚");
        values.put(PetDatabaseHelper.COLUMN_COMMENT_IP, ipLocation);
        values.put(PetDatabaseHelper.COLUMN_COMMENT_LIKE_COUNT, 0); // 新评论0点赞
        values.put(PetDatabaseHelper.COLUMN_COMMENT_IS_USER, 1); // 标记为用户发表的评论
        if (replyTo != null) {
            values.put(PetDatabaseHelper.COLUMN_COMMENT_REPLY_TO, replyTo);
        }
        if (parentCommentId >= 0) {
            values.put(PetDatabaseHelper.COLUMN_COMMENT_PARENT_ID, parentCommentId);
        } else {
            values.put(PetDatabaseHelper.COLUMN_COMMENT_PARENT_ID, -1);
        }
        if (imagePath != null && !imagePath.isEmpty()) {
            values.put(PetDatabaseHelper.COLUMN_COMMENT_IMAGE_PATH, imagePath);
        }
        db.insert(PetDatabaseHelper.TABLE_COMMENTS, null, values);

        // 同时更新帖子的评论数
        db.execSQL("UPDATE " + PetDatabaseHelper.TABLE_PET_NOTES +
                " SET " + PetDatabaseHelper.COLUMN_COMMENT_COUNT +
                " = " + PetDatabaseHelper.COLUMN_COMMENT_COUNT + " + 1" +
                " WHERE " + PetDatabaseHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(noteId) });
    }

    /**
     * 插入一条新评论到数据库（无回复参数的便捷方法）
     */
    public void insertComment(int noteId, String username, String content, String ipLocation) {
        insertComment(noteId, username, content, ipLocation, null, -1, null);
    }

    /**
     * 插入一条新笔记到数据库
     *
     * @param title      标题
     * @param content    内容
     * @param authorName 作者名
     * @param imageRes   图片资源名称
     * @param avatarRes  头像资源名称
     * @param location   位置
     * @return 新插入笔记的ID
     */
    public long insertNote(String title, String content, String authorName,
                           String imageRes, String avatarRes, String location) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PetDatabaseHelper.COLUMN_TITLE, title);
        values.put(PetDatabaseHelper.COLUMN_CONTENT, content);
        values.put(PetDatabaseHelper.COLUMN_AUTHOR_NAME, authorName);
        values.put(PetDatabaseHelper.COLUMN_IMAGE_RES, imageRes);
        values.put(PetDatabaseHelper.COLUMN_AVATAR_RES, avatarRes);
        values.put(PetDatabaseHelper.COLUMN_LOCATION, location != null ? location : "");
        values.put(PetDatabaseHelper.COLUMN_PUBLISH_TIME, "刚刚");
        values.put(PetDatabaseHelper.COLUMN_LIKE_COUNT, 0);
        values.put(PetDatabaseHelper.COLUMN_COLLECT_COUNT, 0);
        values.put(PetDatabaseHelper.COLUMN_COMMENT_COUNT, 0);
        values.put(PetDatabaseHelper.COLUMN_IS_LIKED, 0);
        values.put(PetDatabaseHelper.COLUMN_IS_COLLECTED, 0);
        return db.insert(PetDatabaseHelper.TABLE_PET_NOTES, null, values);
    }

    /**
     * 获取当前用户的所有笔记
     *
     * @param authorName 作者名
     * @return 当前用户的笔记列表
     */
    public List<PetNoteEntity> getNotesByAuthor(String authorName) {
        List<PetNoteEntity> notes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                PetDatabaseHelper.TABLE_PET_NOTES,
                null,
                PetDatabaseHelper.COLUMN_AUTHOR_NAME + " = ?",
                new String[]{authorName},
                null, null,
                PetDatabaseHelper.COLUMN_ID + " DESC");
        while (cursor.moveToNext()) {
            PetNoteEntity note = new PetNoteEntity();
            note.setId(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_ID)));
            note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_TITLE)));
            note.setAuthorName(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_AUTHOR_NAME)));
            note.setAvatarRes(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_AVATAR_RES)));
            note.setImageRes(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_IMAGE_RES)));
            note.setContent(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_CONTENT)));
            note.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_LOCATION)));
            note.setPublishTime(cursor.getString(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_PUBLISH_TIME)));
            note.setLikeCount(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_LIKE_COUNT)));
            note.setCollectCount(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_COLLECT_COUNT)));
            note.setCommentCount(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_COMMENT_COUNT)));
            note.setLiked(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_IS_LIKED)) == 1);
            note.setCollected(cursor.getInt(cursor.getColumnIndexOrThrow(PetDatabaseHelper.COLUMN_IS_COLLECTED)) == 1);
            notes.add(note);
        }
        cursor.close();
        return notes;
    }

    /**
     * 删除一条笔记
     *
     * @param noteId 笔记ID
     * @return 是否删除成功
     */
    public boolean deleteNote(long noteId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleted = db.delete(PetDatabaseHelper.TABLE_PET_NOTES,
                PetDatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(noteId)});
        return deleted > 0;
    }

    /**
     * 更新评论的点赞数
     * 
     * @param commentId    评论ID
     * @param newLikeCount 新的点赞数
     */
    public void updateCommentLikeCount(long commentId, int newLikeCount) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PetDatabaseHelper.COLUMN_COMMENT_LIKE_COUNT, newLikeCount);
        db.update(PetDatabaseHelper.TABLE_COMMENTS, values,
                PetDatabaseHelper.COLUMN_COMMENT_ID + " = ?",
                new String[] { String.valueOf(commentId) });
    }

    private int randomInt(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    private String getRandomPublishTime() {
        int dayOffset = randomInt(0, 6);
        String dayStr;
        if (dayOffset == 0) {
            dayStr = "今天";
        } else if (dayOffset == 1) {
            dayStr = "昨天";
        } else {
            dayStr = dayOffset + "天前";
        }
        int hour = randomInt(0, 23);
        int minute = randomInt(0, 59);
        return String.format("%s %02d:%02d", dayStr, hour, minute);
    }

    private String getRandomCommentTime() {
        String[] units = { "分钟前", "小时前", "天前" };
        int unitIndex = randomInt(0, 2);
        int value;
        switch (unitIndex) {
            case 0:
                value = randomInt(1, 59);
                break;
            case 1:
                value = randomInt(1, 23);
                break;
            default:
                value = randomInt(1, 30);
                break;
        }
        return value + units[unitIndex];
    }

    /**
     * 基于帖子索引的固定发布时间（每次运行结果相同）
     */
    private String getFixedPublishTime(int postIndex) {
        java.util.Random r = new java.util.Random(postIndex * 5555 + 99);
        int dayOffset = r.nextInt(7);
        String dayStr;
        if (dayOffset == 0) {
            dayStr = "今天";
        } else if (dayOffset == 1) {
            dayStr = "昨天";
        } else {
            dayStr = dayOffset + "天前";
        }
        int hour = r.nextInt(24);
        int minute = r.nextInt(60);
        return String.format("%s %02d:%02d", dayStr, hour, minute);
    }

    /**
     * 基于帖子索引+评论序号的固定评论时间（每次运行结果相同）
     */
    private String getFixedCommentTime(int postIndex, int commentIndex) {
        java.util.Random r = new java.util.Random(postIndex * 7777 + commentIndex * 111 + 33);
        String[] units = { "分钟前", "小时前", "天前" };
        int unitIndex = r.nextInt(3);
        int value;
        switch (unitIndex) {
            case 0:
                value = 1 + r.nextInt(59);
                break;
            case 1:
                value = 1 + r.nextInt(23);
                break;
            default:
                value = 1 + r.nextInt(30);
                break;
        }
        return value + units[unitIndex];
    }

    /**
     * 数据初始化回调接口
     */
    public interface OnDataInitListener {
        void onDataInitComplete(List<PetNoteEntity> notes);

        void onDataInitError(String error);
    }

    /**
     * 宠物笔记实体类（用于数据库）
     */
    public static class PetNoteEntity {
        private int id;
        private String title;
        private String authorName;
        private String avatarRes; // 存储资源名称（字符串）
        private String imageRes; // 存储资源名称（字符串）
        private String content;
        private String location;
        private String publishTime;
        private int likeCount;
        private int collectCount;
        private int commentCount;
        private boolean isLiked;
        private boolean isCollected;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthorName() {
            return authorName;
        }

        public void setAuthorName(String authorName) {
            this.authorName = authorName;
        }

        public String getAvatarRes() {
            return avatarRes;
        }

        public void setAvatarRes(String avatarRes) {
            this.avatarRes = avatarRes;
        }

        public String getImageRes() {
            return imageRes;
        }

        public void setImageRes(String imageRes) {
            this.imageRes = imageRes;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getPublishTime() {
            return publishTime;
        }

        public void setPublishTime(String publishTime) {
            this.publishTime = publishTime;
        }

        public int getLikeCount() {
            return likeCount;
        }

        public void setLikeCount(int likeCount) {
            this.likeCount = likeCount;
            ;
        }

        public int getCollectCount() {
            return collectCount;
        }

        public void setCollectCount(int collectCount) {
            this.collectCount = collectCount;
        }

        public int getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(int commentCount) {
            this.commentCount = commentCount;
        }

        public boolean isLiked() {
            return isLiked;
        }

        public void setLiked(boolean liked) {
            isLiked = liked;
        }

        public boolean isCollected() {
            return isCollected;
        }

        public void setCollected(boolean collected) {
            isCollected = collected;
        }
    }

    // ==================== 商品收藏相关方法 ====================

    /**
     * 添加商品收藏
     */
    public void addFavorite(int productId, String title, double price, int imageRes) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PetDatabaseHelper.COLUMN_FAVORITE_PRODUCT_ID, productId);
        values.put(PetDatabaseHelper.COLUMN_FAVORITE_TITLE, title);
        values.put(PetDatabaseHelper.COLUMN_FAVORITE_PRICE, price);
        values.put(PetDatabaseHelper.COLUMN_FAVORITE_IMAGE_RES, imageRes);
        values.put(PetDatabaseHelper.COLUMN_FAVORITE_ADDED_TIME, java.time.LocalDateTime.now().toString());
        try {
            db.insertOrThrow(PetDatabaseHelper.TABLE_FAVORITES, null, values);
        } catch (Exception e) {
            // 已存在，忽略
        }
    }

    /**
     * 添加商品收藏（简化版）
     */
    public void addFavorite(int productId) {
        // 使用默认值，实际应用中应该从商品数据获取详情
        addFavorite(productId, "", 0.0, 0);
    }

    /**
     * 移除商品收藏
     */
    public void removeFavorite(int productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(PetDatabaseHelper.TABLE_FAVORITES,
                PetDatabaseHelper.COLUMN_FAVORITE_PRODUCT_ID + " = ?",
                new String[] { String.valueOf(productId) });
    }

    /**
     * 判断商品是否已收藏
     */
    public boolean isProductFavorite(int productId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                PetDatabaseHelper.TABLE_FAVORITES,
                null,
                PetDatabaseHelper.COLUMN_FAVORITE_PRODUCT_ID + " = ?",
                new String[] { String.valueOf(productId) },
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    /**
     * 获取收藏数量
     */
    public int getFavoriteCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + PetDatabaseHelper.TABLE_FAVORITES, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    // ==================== 购物车相关方法 ====================

    /**
     * 添加商品到购物车
     */
    public void addToCart(int productId, String title, double price, int imageRes, int quantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 先检查是否已存在
        Cursor cursor = db.query(
                PetDatabaseHelper.TABLE_CART,
                new String[] { PetDatabaseHelper.COLUMN_CART_QUANTITY },
                PetDatabaseHelper.COLUMN_CART_PRODUCT_ID + " = ?",
                new String[] { String.valueOf(productId) },
                null, null, null);

        if (cursor.moveToFirst()) {
            // 已存在，更新数量
            int currentQuantity = cursor.getInt(0);
            ContentValues values = new ContentValues();
            values.put(PetDatabaseHelper.COLUMN_CART_QUANTITY, currentQuantity + quantity);
            db.update(PetDatabaseHelper.TABLE_CART, values,
                    PetDatabaseHelper.COLUMN_CART_PRODUCT_ID + " = ?",
                    new String[] { String.valueOf(productId) });
        } else {
            // 不存在，插入新记录
            ContentValues values = new ContentValues();
            values.put(PetDatabaseHelper.COLUMN_CART_PRODUCT_ID, productId);
            values.put(PetDatabaseHelper.COLUMN_CART_TITLE, title);
            values.put(PetDatabaseHelper.COLUMN_CART_PRICE, price);
            values.put(PetDatabaseHelper.COLUMN_CART_IMAGE_RES, imageRes);
            values.put(PetDatabaseHelper.COLUMN_CART_QUANTITY, quantity);
            db.insert(PetDatabaseHelper.TABLE_CART, null, values);
        }
        cursor.close();
    }

    /**
     * 从购物车移除商品
     */
    public void removeFromCart(int productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(PetDatabaseHelper.TABLE_CART,
                PetDatabaseHelper.COLUMN_CART_PRODUCT_ID + " = ?",
                new String[] { String.valueOf(productId) });
    }

    /**
     * 更新购物车商品数量
     */
    public void updateCartQuantity(int productId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(productId);
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PetDatabaseHelper.COLUMN_CART_QUANTITY, quantity);
        db.update(PetDatabaseHelper.TABLE_CART, values,
                PetDatabaseHelper.COLUMN_CART_PRODUCT_ID + " = ?",
                new String[] { String.valueOf(productId) });
    }

    /**
     * 获取购物车商品总数（所有商品数量之和）
     */
    public int getCartCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + PetDatabaseHelper.COLUMN_CART_QUANTITY + ") FROM " + PetDatabaseHelper.TABLE_CART,
                null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * 清空购物车
     */
    public void clearCart() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(PetDatabaseHelper.TABLE_CART, null, null);
    }
}