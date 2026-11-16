package com.example.love_pet;

import java.util.List;

public class ChatDataHolder {
    private static ChatDataHolder instance;
    private List<ChatMessage> messages;

    private ChatDataHolder() {}

    public static ChatDataHolder getInstance() {
        if (instance == null) {
            instance = new ChatDataHolder();
        }
        return instance;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void clear() {
        messages = null;
    }
}
