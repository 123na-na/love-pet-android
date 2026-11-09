package com.example.love_pet;

import java.io.Serializable;

public class ChatMessage implements Serializable {
    public String content;
    public String time;
    public boolean isSelf;
    public String subTitle;
    public int avatar;

    public ChatMessage(String content, String time, boolean isSelf) {
        this.content = content;
        this.time = time;
        this.isSelf = isSelf;
    }

    public ChatMessage(String content, String subTitle, String time, boolean isSelf, int avatar) {
        this.content = content;
        this.subTitle = subTitle;
        this.time = time;
        this.isSelf = isSelf;
        this.avatar = avatar;
    }
}
