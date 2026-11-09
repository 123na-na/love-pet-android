package com.example.love_pet;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 百度语音识别工具类（REST API）
 * 流程：录音 → 获取token → 发送音频 → 返回识别文字
 */
public class BaiduSpeechRecognizer {

    private static final String TAG = "BaiduSpeech";

    // 百度语音API配置
    private static final String APP_ID = BuildConfig.BAIDU_APP_ID;
    private static final String API_KEY = BuildConfig.BAIDU_API_KEY;
    private static final String SECRET_KEY = BuildConfig.BAIDU_SECRET_KEY;

    // API地址
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String RECOGNIZE_URL = "https://vop.baidu.com/server_api";

    // 录音参数：16kHz, 16bit, 单声道 (百度要求)
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private final Context context;
    private final OkHttpClient httpClient;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String accessToken;
    private long tokenExpireTime = 0;

    // 录音相关
    private boolean isRecording = false;
    private AudioRecord audioRecord;
    private Thread recordThread;
    private ByteArrayOutputStream audioBuffer;

    // 回调接口
    public interface OnRecognitionListener {
        void onRecordingStart();

        void onRecordingStop();

        void onResult(String text);

        void onError(String error);
    }

    public BaiduSpeechRecognizer(Context context) {
        this.context = context.getApplicationContext();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // 连接超时30秒
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // 读取超时60秒
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS) // 写入超时60秒
                // 网络重试机制：失败后自动重试一次
                .retryOnConnectionFailure(true)
                .build();
        this.audioBuffer = new ByteArrayOutputStream();
    }

    /**
     * 开始录音识别
     */
    public void startRecognition(OnRecognitionListener listener) {
        if (isRecording) {
            listener.onError("正在录音中，请先停止");
            return;
        }

        try {
            // 初始化AudioRecord
            int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
            if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                listener.onError("设备不支持当前录音参数");
                return;
            }
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION, // 使用语音识别专用音频源（自动降噪/回声消除）
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize * 2);

            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                listener.onError("麦克风初始化失败，请检查权限");
                return;
            }

            isRecording = true;
            synchronized (audioBuffer) {
                audioBuffer.reset();
            }
            audioRecord.startRecording();
            listener.onRecordingStart();

            // 录音线程：最多录制60秒，或用户手动停止
            final int maxDurationMs = 60000;
            final int bytesPerSample = 2; // 16bit = 2字节
            final int sampleCountPerRead = bufferSize / bytesPerSample;
            final long totalSamples = (long) SAMPLE_RATE * maxDurationMs / 1000;

            recordThread = new Thread(() -> {
                byte[] buffer = new byte[bufferSize];
                long recordedSamples = 0;
                int readCount = 0; // 调试：记录读取次数
                int errorCount = 0; // 调试：记录错误次数

                Log.d(TAG, "录音线程启动, bufferSize=" + buffer.length);

                while (isRecording && recordedSamples < totalSamples) {
                    int read = audioRecord.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        synchronized (audioBuffer) {
                            audioBuffer.write(buffer, 0, read);
                        }
                        recordedSamples += read / bytesPerSample;
                        readCount++;
                        // 每100次读取输出一次日志
                        if (readCount % 100 == 0) {
                            Log.d(TAG, "录音中: " + recordedSamples + " samples, buffer=" + audioBuffer.size() + " bytes");
                        }
                    } else if (read == AudioRecord.ERROR_INVALID_OPERATION) {
                        errorCount++;
                        if (errorCount <= 3) { // 只打印前3次错误
                            Log.w(TAG, "AudioRecord无效操作(read=" + read + "), 状态=" + audioRecord.getRecordingState());
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ignored) {
                            break; // 中断则退出
                        }
                    } else if (read == AudioRecord.ERROR_BAD_VALUE) {
                        Log.e(TAG, "AudioRecord参数错误(read=" + read + ")");
                        break;
                    } else {
                        // read == 0
                        errorCount++;
                        if (errorCount <= 3) {
                            Log.w(TAG, "AudioRecord返回0, 状态=" + audioRecord.getRecordingState());
                        }
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException ignored) {
                            break;
                        }
                    }
                }

                Log.d(TAG, "录音线程结束: recordedSamples=" + recordedSamples
                        + ", readCount=" + readCount + ", errorCount=" + errorCount
                        + ", bufferSize=" + audioBuffer.size());
            });
            recordThread.start();

        } catch (Exception e) {
            Log.e(TAG, "启动录音失败", e);
            isRecording = false;
            listener.onError("启动录音失败：" + e.getMessage());
        }
    }

    /**
     * 停止录音并发送识别请求
     */
    public void stopRecognition(OnRecognitionListener listener) {
        Log.d(TAG, "=== stopRecognition 开始 ===");
        if (!isRecording) {
            Log.w(TAG, "未在录音状态");
            listener.onError("未在录音状态");
            return;
        }

        isRecording = false;

        // 等待录音线程结束
        try {
            if (recordThread != null && recordThread.isAlive()) {
                recordThread.join(1000);
            }
        } catch (InterruptedException e) {
            Log.w(TAG, "等待录音线程中断", e);
        }

        // 停止录音
        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                Log.e(TAG, "停止AudioRecord异常", e);
            }
            audioRecord = null;
        }
        Log.d(TAG, "录音已停止");

        listener.onRecordingStop();

        // 获取录音数据并处理（整体try-catch防止任何异常被吞掉）
        try {
            byte[] audioData;
            synchronized (audioBuffer) {
                audioData = audioBuffer.toByteArray();
            }
            Log.d(TAG, "[步骤1] 获取录音数据: " + (audioData != null ? audioData.length : 0) + " 字节");

            if (audioData == null || audioData.length < 400) {
                listener.onError("录音数据太短（" + (audioData != null ? audioData.length : 0) + "字节），请按住时间长一些");
                return;
            }

            // 直接使用PCM格式发送（百度推荐，不做任何转换）
            Log.d(TAG, "[步骤2] 使用原始PCM格式，大小: " + audioData.length + " 字节");

            // 获取Token并发送识别请求
            final byte[] finalAudioData = audioData;
            Log.d(TAG, "[步骤3] 开始获取Token...");
            fetchToken(new OnTokenCallback() {
                @Override
                public void onSuccess(String token) {
                    Log.d(TAG, "[步骤4] Token获取成功，发送识别请求...");
                    sendRecognizeRequest(finalAudioData, token, listener);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "[步骤4] Token获取失败: " + error);
                    mainHandler.post(() -> listener.onError("获取Token失败：" + error));
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "=== stopRecognition 异常 ===", e);
            mainHandler.post(() -> listener.onError("处理异常：" + e.getMessage()));
        }
    }

    /**
     * 获取百度access_token
     */
    private void fetchToken(OnTokenCallback callback) {
        // 检查缓存token是否有效（提前5分钟刷新）
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime - 300000) {
            callback.onSuccess(accessToken);
            return;
        }

        String url = TOKEN_URL + "?grant_type=client_credentials"
                + "&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY;

        Request request = new Request.Builder().url(url).get().build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                try {
                    JSONObject json = new JSONObject(body);
                    if (json.has("access_token")) {
                        accessToken = json.getString("access_token");
                        int expiresIn = json.optInt("expires_in", 2592000); // 默认30天
                        tokenExpireTime = System.currentTimeMillis() + expiresIn * 1000L;
                        callback.onSuccess(accessToken);
                    } else {
                        callback.onError(json.optString("error_description", "未知错误"));
                    }
                } catch (JSONException e) {
                    callback.onError("解析Token响应失败");
                }
            }
        });
    }

    /**
     * 检查录音数据是否有效（非全零/静音）
     * 通过计算音频能量来判断是否有声音
     */
    private boolean isAudioDataValid(byte[] audioData) {
        long energy = 0;
        int sampleCount = 0;
        int maxSample = 0;
        int nonZeroCount = 0;

        // 遍历所有样本计算能量
        for (int i = 0; i < audioData.length - 1; i += 2) {
            // 16bit PCM，小端序
            int sample = (audioData[i] & 0xFF) | ((audioData[i + 1] & 0xFF) << 8);
            // 处理有符号数
            if (sample > 32767)
                sample -= 65536;
            int absSample = Math.abs(sample);
            energy += (long) absSample * absSample;
            if (absSample > maxSample)
                maxSample = absSample;
            if (absSample > 10)
                nonZeroCount++;
            sampleCount++;
        }

        double avgEnergy = sampleCount > 0 ? (double) energy / sampleCount : 0;
        double zeroRatio = sampleCount > 0 ? 1.0 - (double) nonZeroCount / sampleCount : 1.0;

        Log.d(TAG, "===== 音频数据分析 =====");
        Log.d(TAG, "  总大小: " + audioData.length + " 字节");
        Log.d(TAG, "  样本数: " + sampleCount);
        Log.d(TAG, "  最大振幅: " + maxSample);
        Log.d(TAG, "  平均能量: " + (long) avgEnergy);
        Log.d(TAG, "  静音比例: " + String.format("%.1f%%", zeroRatio * 100));
        Log.d(TAG, "  前20字节: " + bytesToHex(audioData, 20));
        Log.d(TAG, "========================");

        // 放宽判断条件：只要不是全零就认为有效，让百度API来判断是否能识别
        // 条件：最大振幅 > 50 或 非零样本占比 > 1%
        boolean valid = maxSample > 50 || zeroRatio < 0.99;
        Log.d(TAG, "音频有效性判定: " + (valid ? "通过" : "不通过"));
        return valid;
    }

    /**
     * 去除音频首尾的静音段
     * 3307错误常因音频包含大量静音数据导致，去除后可大幅提高识别率
     */
    private byte[] removeSilence(byte[] pcmData) {
        final int bytesPerSample = 2; // 16bit = 2字节
        final int silenceThreshold = 200; // 静音阈值（振幅小于此值视为静音）
        int startSample = 0;
        int endSample = (pcmData.length / bytesPerSample) - 1;

        // 从前往后找第一个非静音样本
        for (int i = 0; i < pcmData.length - 1; i += bytesPerSample) {
            int sample = Math.abs((pcmData[i] & 0xFF) | ((pcmData[i + 1] & 0xFF) << 8));
            if (sample > silenceThreshold) {
                startSample = i / bytesPerSample;
                break;
            }
        }

        // 从后往前找最后一个非静音样本
        for (int i = pcmData.length - 2; i >= 0; i -= bytesPerSample) {
            int sample = Math.abs((pcmData[i] & 0xFF) | ((pcmData[i + 1] & 0xFF) << 8));
            if (sample > silenceThreshold) {
                endSample = i / bytesPerSample;
                break;
            }
        }

        if (startSample >= endSample) {
            Log.w(TAG, "去静音：整段都是静音");
            return pcmData; // 全是静音，返回原数据
        }

        // 提取有效音频段（前后各保留少量缓冲）
        int bufferSamples = SAMPLE_RATE / 100; // 前后各留10ms缓冲
        startSample = Math.max(0, startSample - bufferSamples);
        endSample = Math.min(pcmData.length / bytesPerSample - 1, endSample + bufferSamples);

        int newLen = (endSample - startSample + 1) * bytesPerSample;
        byte[] trimmed = new byte[newLen];
        System.arraycopy(pcmData, startSample * bytesPerSample, trimmed, 0, newLen);
        return trimmed;
    }

    /**
     * 将PCM数据转换为标准WAV格式
     * WAV = 44字节RIFF头 + PCM原始数据
     * 头信息包含采样率、声道数、位深度等元数据，消除格式歧义
     */
    private byte[] pcmToWav(byte[] pcmData) {
        int dataLength = pcmData.length;
        int totalLength = dataLength + 36; // 文件总长度 - 8
        byte[] wavHeader = new byte[44];

        // RIFF chunk descriptor
        wavHeader[0] = 'R';
        wavHeader[1] = 'I';
        wavHeader[2] = 'F';
        wavHeader[3] = 'F';
        wavHeader[4] = (byte) (totalLength & 0xFF);
        wavHeader[5] = (byte) ((totalLength >> 8) & 0xFF);
        wavHeader[6] = (byte) ((totalLength >> 16) & 0xFF);
        wavHeader[7] = (byte) ((totalLength >> 24) & 0xFF);

        // WAVE标识
        wavHeader[8] = 'W';
        wavHeader[9] = 'A';
        wavHeader[10] = 'V';
        wavHeader[11] = 'E';

        // fmt sub-chunk
        wavHeader[12] = 'f';
        wavHeader[13] = 'm';
        wavHeader[14] = 't';
        wavHeader[15] = ' ';
        wavHeader[16] = 16; // fmt chunk大小(固定16)
        wavHeader[17] = 0;
        wavHeader[18] = 0;
        wavHeader[19] = 0;
        wavHeader[20] = 1; // 音频格式: 1=PCM
        wavHeader[21] = 0;
        wavHeader[22] = 1; // 声道数: 单声道
        wavHeader[23] = 0;
        // 采样率 (16000 Hz)
        wavHeader[24] = (byte) (SAMPLE_RATE & 0xFF);
        wavHeader[25] = (byte) ((SAMPLE_RATE >> 8) & 0xFF);
        wavHeader[26] = (byte) ((SAMPLE_RATE >> 16) & 0xFF);
        wavHeader[27] = (byte) ((SAMPLE_RATE >> 24) & 0xFF);
        // 字节率 = 采样率 × 声道数 × 位深度/8
        int byteRate = SAMPLE_RATE * 1 * 16 / 8;
        wavHeader[28] = (byte) (byteRate & 0xFF);
        wavHeader[29] = (byte) ((byteRate >> 8) & 0xFF);
        wavHeader[30] = (byte) ((byteRate >> 16) & 0xFF);
        wavHeader[31] = (byte) ((byteRate >> 24) & 0xFF);
        // 块对齐 = 声道数 × 位深度/8
        wavHeader[32] = 2; // 1 * 16/8 = 2
        wavHeader[33] = 0;
        // 位深度
        wavHeader[34] = 16;
        wavHeader[35] = 0;

        // data sub-chunk
        wavHeader[36] = 'd';
        wavHeader[37] = 'a';
        wavHeader[38] = 't';
        wavHeader[39] = 'a';
        wavHeader[40] = (byte) (dataLength & 0xFF);
        wavHeader[41] = (byte) ((dataLength >> 8) & 0xFF);
        wavHeader[42] = (byte) ((dataLength >> 16) & 0xFF);
        wavHeader[43] = (byte) ((dataLength >> 24) & 0xFF);

        // 组合: WAV头 + PCM数据
        ByteArrayOutputStream baos = new ByteArrayOutputStream(44 + dataLength);
        try {
            baos.write(wavHeader);
            baos.write(pcmData);
        } catch (IOException e) {
            Log.e(TAG, "PCM转WAV失败", e);
            return pcmData;
        }
        return baos.toByteArray();
    }

    /**
     * 字节数组转十六进制字符串（用于调试）
     */
    private String bytesToHex(byte[] bytes, int maxLen) {
        StringBuilder sb = new StringBuilder();
        int len = Math.min(bytes.length, maxLen);
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }

    /**
     * 发送语音识别请求到百度API
     * 使用PCM原始格式（百度推荐，最简单可靠）
     */
    private void sendRecognizeRequest(byte[] pcmData, String token, OnRecognitionListener listener) {
        try {
            JSONObject params = new JSONObject();
            params.put("format", "pcm"); // PCM原始格式
            params.put("rate", 16000); // 固定16kHz采样率
            params.put("channel", 1); // 单声道
            params.put("cuid", android.os.Build.MODEL); // 设备唯一标识
            params.put("token", token);
            params.put("dev_pid", 1537); // 普通话输入法模型
            params.put("len", pcmData.length); // 原始PCM数据长度
            params.put("speech", android.util.Base64.encodeToString(pcmData, android.util.Base64.NO_WRAP));

            Log.d(TAG, "发送请求: format=pcm, rate=16000, len=" + pcmData.length + ", dev_pid=1537");

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"),
                    params.toString());

            Request request = new Request.Builder()
                    .url(RECOGNIZE_URL)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "网络请求失败", e);
                    mainHandler.post(() -> listener.onError("网络请求失败：" + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String resultBody = response.body().string();
                    Log.d(TAG, "API响应: " + resultBody);

                    try {
                        JSONObject resultJson = new JSONObject(resultBody);
                        int errNo = resultJson.optInt("err_no", -1);

                        if (errNo == 0) {
                            // 识别成功
                            JSONArray results = resultJson.optJSONArray("result");
                            StringBuilder text = new StringBuilder();
                            if (results != null) {
                                for (int i = 0; i < results.length(); i++) {
                                    text.append(results.getString(i));
                                }
                            }
                            String recognizedText = text.toString().trim();
                            Log.d(TAG, "识别结果: '" + recognizedText + "' (长度=" + recognizedText.length() + ")");
                            if (recognizedText.isEmpty()) {
                                Log.w(TAG, "百度返回空结果，可能未识别到有效语音");
                                mainHandler.post(() -> listener.onError("未识别到有效内容，请重试"));
                            } else {
                                mainHandler.post(() -> listener.onResult(recognizedText));
                            }
                        } else {
                            // 识别失败 - 输出详细错误信息用于排查
                            String errMsg = resultJson.optString("err_msg", "识别失败");
                            String sn = resultJson.optString("sn", "");
                            Log.e(TAG, "识别错误详情:");
                            Log.e(TAG, "  err_no=" + errNo);
                            Log.e(TAG, "  err_msg=" + errMsg);
                            Log.e(TAG, "  sn=" + sn);
                            mainHandler.post(() -> listener.onError("识别错误[" + errNo + "]: " + errMsg));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "解析响应失败, 原始内容: " + resultBody, e);
                        mainHandler.post(() -> listener.onError("解析识别结果失败"));
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "构建请求参数失败", e);
            listener.onError("构建请求参数失败");
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        isRecording = false;
        if (audioRecord != null) {
            try {
                if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    audioRecord.stop();
                }
                audioRecord.release();
            } catch (Exception ignored) {
            }
            audioRecord = null;
        }
    }

    /** Token回调接口 */
    private interface OnTokenCallback {
        void onSuccess(String token);

        void onError(String error);
    }
}
