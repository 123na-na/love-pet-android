package com.example.love_pet;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 千问（Qwen）大模型 API 调用工具类
 * 基于 DashScope OpenAI 兼容接口
 */
public class QwenApiHelper {

    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String API_KEY = BuildConfig.QWEN_API_KEY;
    private static final String MODEL = "qwen-turbo";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /** 对话历史记录 */
    private final List<Message> conversationHistory = new ArrayList<>();

    /** 系统提示词 */
    private String systemPrompt = "你是爱宠APP的智能客服助手小咪，请用友好、热情、专业的语气回答用户关于宠物的问题。" +
            "包括但不限于：宠物领养、宠物健康护理、宠物美容洗护、宠物用品购买、订单问题等。" +
            "回答要简洁明了，控制在100字以内。如果遇到无法回答的问题，建议用户联系人工客服。";

    /**
     * 消息数据结构
     */
    public static class Message {
        public final String role; // system / user / assistant
        public final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    /**
     * 设置系统提示词
     */
    public void setSystemPrompt(String prompt) {
        this.systemPrompt = prompt;
    }

    /**
     * 清空对话历史
     */
    public void clearHistory() {
        conversationHistory.clear();
    }

    /**
     * 发送消息给千问模型，获取 AI 回复
     *
     * @param userMessage 用户输入的消息
     * @param callback    回调接口，在主线程返回结果
     */
    public void sendMessage(String userMessage, OnResponseCallback callback) {
        // 添加用户消息到历史
        conversationHistory.add(new Message("user", userMessage));

        executor.execute(() -> {
            try {
                String responseText = doRequest(conversationHistory);
                if (responseText != null && !responseText.isEmpty()) {
                    // 添加AI回复到历史
                    conversationHistory.add(new Message("assistant", responseText));
                    mainHandler.post(() -> callback.onSuccess(responseText));
                } else {
                    mainHandler.post(() -> callback.onError("未获取到有效回复"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 移除失败的用户消息
                if (!conversationHistory.isEmpty()) {
                    conversationHistory.remove(conversationHistory.size() - 1);
                }
                mainHandler.post(() -> callback.onError("网络请求失败：" + e.getMessage()));
            }
        });
    }

    /**
     * 执行实际的HTTP请求
     */
    private String doRequest(List<Message> messages) throws IOException {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL);

            JSONArray messageArray = new JSONArray();

            // 添加系统提示
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messageArray.put(systemMsg);

            // 添加对话历史（限制最近10轮，避免超出token限制）
            int startIndex = Math.max(0, messages.size() - 20);
            for (int i = startIndex; i < messages.size(); i++) {
                Message msg = messages.get(i);
                JSONObject msgObj = new JSONObject();
                msgObj.put("role", msg.role);
                msgObj.put("content", msg.content);
                messageArray.put(msgObj);
            }

            requestBody.put("messages", messageArray);

            RequestBody body = RequestBody.create(requestBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new IOException("API返回错误：" + response.code() + " " + responseBody);
            }

            return parseResponseBody(responseBody);
        } catch (Exception e) {
            throw new IOException("请求失败：" + e.getMessage(), e);
        }
    }

    /**
     * 解析API响应，提取AI回复内容
     */
    private String parseResponseBody(String responseBody) throws Exception {
        JSONObject json = new JSONObject(responseBody);
        JSONArray choices = json.optJSONArray("choices");
        if (choices != null && choices.length() > 0) {
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.optJSONObject("message");
            if (message != null) {
                return message.optString("content", "");
            }
        }
        return null;
    }

    /**
     * 回调接口
     */
    public interface OnResponseCallback {
        void onSuccess(String reply);

        void onError(String error);
    }

    /**
     * 释放资源
     */
    public void release() {
        executor.shutdownNow();
    }
}
