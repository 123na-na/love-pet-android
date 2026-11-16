package com.example.love_pet;

import java.io.Serializable;

/**
 * 评论实体类
 */
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 评论用户昵称 */
    private String username;

    /** 评论内容 */
    private String content;

    /** 评论时间（格式化后的字符串） */
    private String time;

    /** IP归属地 */
    private String ipLocation;

    /** 评论点赞数 */
    private int likeCount;

    /** 数据库中的评论ID（自增主键） */
    private long id = -1;

    /** 回复的目标用户名（null表示非回复） */
    private String replyTo;

    /** 回复的目标评论ID（用于嵌套排序，-1表示顶级评论） */
    private long parentCommentId = -1;

    /** 评论图片路径 */
    private String imagePath;

    public Comment() {
    }

    public Comment(String username, String content, String time) {
        this.username = username;
        this.content = content;
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getIpLocation() {
        return ipLocation;
    }

    public void setIpLocation(String ipLocation) {
        this.ipLocation = ipLocation;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public long getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
