package com.example.love_pet;

import java.io.Serializable;
import java.util.List;

/**
 * 笔记实体类
 */
public class PetNote implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 封面图链接 */
    private String coverUrl;

    /** 1-9张图集链接集合 */
    private List<String> imageUrls;

    /** 标题 */
    private String title;

    /** 作者名称 */
    private String authorName;

    /** 点赞数 */
    private int likeCount;

    /** 收藏数 */
    private int collectCount;

    /** 评论总数 */
    private int commentCount;

    /** 正文内容 */
    private String content;

    /** 话题标签数组 */
    private String[] tags;

    /** 定位城市 */
    private String location;

    /** 发布时间 */
    private String publishTime;

    /** 评论集合 */
    private List<Comment> comments;

    public PetNote() {
    }

    // Getters and Setters

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
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

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
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

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}