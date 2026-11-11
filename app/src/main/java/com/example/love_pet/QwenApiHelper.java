package com.example.love_pet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
 *
 * 文本对话：qwen-turbo
 * 图片理解：qwen-vl-plus（多模态，messages.content 传数组）
 *
 * API Key 不再硬编码，改为从 local.properties 的 QWEN_API_KEY 读取，
 * 由 Gradle 注入到 BuildConfig。
 */
public class QwenApiHelper {

    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String API_KEY = BuildConfig.QWEN_API_KEY;
    private static final String MODEL = BuildConfig.QWEN_TEXT_MODEL;
    private static final String MODEL_VL = BuildConfig.QWEN_VL_MODEL;

    /** 上传前先降采样，避免 base64 体积过大导致请求超限 */
    private static final int MAX_IMAGE_EDGE = 1024;
    private static final int JPEG_QUALITY = 80;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
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
     * 消息数据结构（文本链路使用）
     */
    public static class Message {
        public final String role; // system / user / assistant
        public final String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    /** 是否已配置可用的 API Key */
    public boolean isConfigured() {
        return API_KEY != null && !API_KEY.trim().isEmpty() && !"sk-xxx".equals(API_KEY.trim());
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
     * 发送文本消息给千问模型，获取 AI 回复
     */
    public void sendMessage(String userMessage, OnResponseCallback callback) {
        if (!isConfigured()) {
            postError(callback, "未配置有效的 API Key，请在 local.properties 中设置 QWEN_API_KEY");
            return;
        }

        conversationHistory.add(new Message("user", userMessage));

        executor.execute(() -> {
            try {
                String responseText = doRequest(conversationHistory);
                if (responseText != null && !responseText.isEmpty()) {
                    conversationHistory.add(new Message("assistant", responseText));
                    mainHandler.post(() -> callback.onSuccess(responseText));
                } else {
                    mainHandler.post(() -> callback.onError("未获取到有效回复"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!conversationHistory.isEmpty()) {
                    conversationHistory.remove(conversationHistory.size() - 1);
                }
                mainHandler.post(() -> callback.onError("网络请求失败：" + e.getMessage()));
            }
        });
    }

    /**
     * 发送图片（可附带一句文字问题）给多模态模型，获取 AI 回复
     *
     * @param context  用于读取 content Uri
     * @param imageUri 图片 Uri（相册选择或拍照产生）
     * @param userText 附带的问题，可为空
     * @param callback 主线程回调
     */
    public void sendImageMessage(final Context context, final Uri imageUri, final String userText,
                                 final OnResponseCallback callback) {
        if (!isConfigured()) {
            postError(callback, "未配置有效的 API Key，请在 local.properties 中设置 QWEN_API_KEY");
            return;
        }

        final String historyText = (userText != null && !userText.trim().isEmpty())
                ? userText : "[用户上传了一张图片]";
        conversationHistory.add(new Message("user", historyText));

        executor.execute(() -> {
            try {
                String dataUrl = encodeImageAsDataUrl(context, imageUri);
                String responseText = doVisionRequest(dataUrl, userText);
                if (responseText != null && !responseText.isEmpty()) {
                    conversationHistory.add(new Message("assistant", responseText));
                    mainHandler.post(() -> callback.onSuccess(responseText));
                } else {
                    mainHandler.post(() -> callback.onError("未获取到有效回复"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!conversationHistory.isEmpty()) {
                    conversationHistory.remove(conversationHistory.size() - 1);
                }
                mainHandler.post(() -> callback.onError("图片识别失败：" + e.getMessage()));
            }
        });
    }

    private void postError(final OnResponseCallback callback, final String message) {
        mainHandler.post(() -> callback.onError(message));
    }

    /**
     * 执行文本对话请求
     */
    private String doRequest(List<Message> messages) throws IOException {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL);

            JSONArray messageArray = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messageArray.put(systemMsg);

            // 只带最近 20 条（10 轮），避免超 token 限制
            int startIndex = Math.max(0, messages.size() - 20);
            for (int i = startIndex; i < messages.size(); i++) {
                Message msg = messages.get(i);
                JSONObject msgObj = new JSONObject();
                msgObj.put("role", msg.role);
                msgObj.put("content", msg.content);
                messageArray.put(msgObj);
            }

            requestBody.put("messages", messageArray);

            return postAndParse(requestBody);
        } catch (Exception e) {
            throw new IOException("请求失败：" + e.getMessage(), e);
        }
    }

    /**
     * 执行图片理解请求：messages.content 使用多模态数组（text + image_url）
     */
    private String doVisionRequest(String imageDataUrl, String userText) throws IOException {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL_VL);

            JSONArray messageArray = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messageArray.put(systemMsg);

            JSONArray contentArray = new JSONArray();

            if (userText != null && !userText.trim().isEmpty()) {
                JSONObject textPart = new JSONObject();
                textPart.put("type", "text");
                textPart.put("text", userText);
                contentArray.put(textPart);
            }

            JSONObject imagePart = new JSONObject();
            imagePart.put("type", "image_url");
            JSONObject imageUrlObj = new JSONObject();
            imageUrlObj.put("url", imageDataUrl);
            imagePart.put("image_url", imageUrlObj);
            contentArray.put(imagePart);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", contentArray);
            messageArray.put(userMsg);

            requestBody.put("messages", messageArray);

            return postAndParse(requestBody);
        } catch (Exception e) {
            throw new IOException("图片请求失败：" + e.getMessage(), e);
        }
    }

    /**
     * 统一的 HTTP 发送与解析
     */
    private String postAndParse(JSONObject requestBody) throws IOException {
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
    }

    /**
     * 本地图片压缩后转 data:image/jpeg;base64
     */
    private String encodeImageAsDataUrl(Context context, Uri imageUri) throws IOException {
        if (context == null || imageUri == null) {
            throw new IOException("图片为空");
        }

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        InputStream is = context.getContentResolver().openInputStream(imageUri);
        if (is == null) throw new IOException("无法读取图片");
        try {
            BitmapFactory.decodeStream(is, null, bounds);
        } finally {
            is.close();
        }

        int sampleSize = 1;
        int maxEdge = Math.max(bounds.outWidth, bounds.outHeight);
        while (maxEdge > 0 && maxEdge / sampleSize > MAX_IMAGE_EDGE) {
            sampleSize *= 2;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sampleSize;
        Bitmap bitmap;
        InputStream is2 = context.getContentResolver().openInputStream(imageUri);
        if (is2 == null) throw new IOException("无法读取图片");
        try {
            bitmap = BitmapFactory.decodeStream(is2, null, opts);
        } finally {
            is2.close();
        }
        if (bitmap == null) {
            throw new IOException("图片解码失败");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, bos);
        bitmap.recycle();

        String base64 = Base64.encodeToString(bos.toByteArray(), Base64.NO_WRAP);
        return "data:image/jpeg;base64," + base64;
    }

    /**
     * 解析API响应，提取AI回复内容
     */
    private String parseResponseBody(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            JSONArray choices = json.optJSONArray("choices");
            if (choices != null && choices.length() > 0) {
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.optJSONObject("message");
                if (message != null) {
                    return message.optString("content", "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
