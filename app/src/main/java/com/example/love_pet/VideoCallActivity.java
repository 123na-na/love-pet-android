package com.example.love_pet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc2.video.VideoEncoderConfiguration;

public class VideoCallActivity extends AppCompatActivity {

    public static final String EXTRA_DOCTOR_NAME = "doctor_name";
    public static final String EXTRA_DOCTOR_AVATAR = "doctor_avatar";
    public static final String EXTRA_PACKAGE_NAME = "package_name";

    private static final String AGORA_APP_ID = BuildConfig.AGORA_APP_ID;
    // TODO: 替换为你自己的Token（测试模式可暂时留空，生产环境必须从服务器获取）
    private static final String AGORA_TOKEN = "";
    // 测试频道名，实际应用中应该从服务器获取
    private static final String CHANNEL_NAME = "love_pet_consult";

    private static final int PERMISSION_REQ_ID = 1001;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };

    private RtcEngine mRtcEngine;
    private boolean mJoined = false;
    private boolean mMuted = false;
    private boolean mSpeakerOn = true;
    private int mCallSeconds = 0;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mTimerRunnable;

    private FrameLayout mFlLocalVideo;
    private FrameLayout mFlRemoteVideo;
    private TextView mTvDoctorName;
    private TextView mTvCallStatus;
    private TextView mTvCallDuration;
    private ImageView mIvMute;
    private ImageView mIvSpeaker;
    private View mLayoutWaiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        mFlLocalVideo = findViewById(R.id.fl_local_video);
        mFlRemoteVideo = findViewById(R.id.fl_remote_video);
        mTvDoctorName = findViewById(R.id.tv_doctor_name_video);
        mTvCallStatus = findViewById(R.id.tv_call_status);
        mTvCallDuration = findViewById(R.id.tv_call_duration);
        mIvMute = findViewById(R.id.iv_mute);
        mIvSpeaker = findViewById(R.id.iv_speaker);
        mLayoutWaiting = findViewById(R.id.layout_waiting);

        String doctorName = getIntent().getStringExtra(EXTRA_DOCTOR_NAME);
        if (doctorName != null) {
            mTvDoctorName.setText(doctorName);
        }

        ImageView ivDoctorAvatar = findViewById(R.id.iv_doctor_avatar_big);
        ivDoctorAvatar.setClipToOutline(true);
        ivDoctorAvatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(android.view.View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });

        findViewById(R.id.iv_back_video).setOnClickListener(v -> finish());
        findViewById(R.id.layout_hang_up).setOnClickListener(v -> finish());
        findViewById(R.id.layout_mute).setOnClickListener(v -> toggleMute());
        findViewById(R.id.layout_switch_camera).setOnClickListener(v -> switchCamera());
        findViewById(R.id.layout_speaker).setOnClickListener(v -> toggleSpeaker());

        if (checkSelfPermission()) {
            initRtcEngine();
        }
    }

    private boolean checkSelfPermission() {
        boolean granted = true;
        for (String permission : REQUESTED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        if (!granted) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_REQ_ID);
        }
        return granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_ID) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                initRtcEngine();
            } else {
                Toast.makeText(this, "需要摄像头和麦克风权限才能进行视频通话", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initRtcEngine() {
        if (AGORA_APP_ID == null || AGORA_APP_ID.isEmpty()) {
            mTvCallStatus.setText("请先配置声网AppId");
            return;
        }

        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getApplicationContext();
            config.mAppId = AGORA_APP_ID;
            config.mEventHandler = mRtcEventHandler;
            mRtcEngine = RtcEngine.create(config);

            mRtcEngine.enableVideo();
            mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
                    VideoEncoderConfiguration.VD_640x360,
                    VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                    VideoEncoderConfiguration.STANDARD_BITRATE,
                    VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            ));

            setupLocalVideo();
            joinChannel();
        } catch (Exception e) {
            e.printStackTrace();
            mTvCallStatus.setText("初始化失败: " + e.getMessage());
        }
    }

    private void setupLocalVideo() {
        SurfaceView localSurfaceView = new SurfaceView(getApplicationContext());
        localSurfaceView.setZOrderMediaOverlay(true);
        mFlLocalVideo.addView(localSurfaceView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        VideoCanvas localCanvas = new VideoCanvas(
                localSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                0
        );
        mRtcEngine.setupLocalVideo(localCanvas);
        mRtcEngine.startPreview();
    }

    private void joinChannel() {
        mTvCallStatus.setText("正在连接...");
        String token = AGORA_TOKEN == null || AGORA_TOKEN.isEmpty() ? null : AGORA_TOKEN;
        int ret = mRtcEngine.joinChannel(token, CHANNEL_NAME, "", 0);
        if (ret != 0) {
            mTvCallStatus.setText("加入频道失败");
        }
    }

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            mJoined = true;
            runOnUiThread(() -> {
                mTvCallStatus.setText("等待医生接听...");
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> {
                mTvCallStatus.setText("通话中");
                mTvCallDuration.setVisibility(View.VISIBLE);
                mLayoutWaiting.setVisibility(View.GONE);
                startCallTimer();
                setupRemoteVideo(uid);
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> {
                mTvCallStatus.setText("通话已结束");
                stopCallTimer();
            });
        }

        @Override
        public void onConnectionLost() {
            runOnUiThread(() -> {
                mTvCallStatus.setText("连接断开");
                stopCallTimer();
            });
        }

        @Override
        public void onError(int err) {
            runOnUiThread(() -> {
                Toast.makeText(VideoCallActivity.this, "错误: " + err, Toast.LENGTH_SHORT).show();
            });
        }
    };

    private void setupRemoteVideo(int uid) {
        if (mFlRemoteVideo.getChildCount() > 0) {
            mFlRemoteVideo.removeAllViews();
        }

        SurfaceView remoteSurfaceView = new SurfaceView(getApplicationContext());
        mFlRemoteVideo.addView(remoteSurfaceView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        VideoCanvas remoteCanvas = new VideoCanvas(
                remoteSurfaceView,
                VideoCanvas.RENDER_MODE_HIDDEN,
                uid
        );
        mRtcEngine.setupRemoteVideo(remoteCanvas);
    }

    private void toggleMute() {
        if (mRtcEngine == null) return;
        mMuted = !mMuted;
        mRtcEngine.muteLocalAudioStream(mMuted);
        if (mMuted) {
            mIvMute.setImageResource(R.drawable.ic_mic_off);
        } else {
            mIvMute.setImageResource(R.drawable.ic_mic_on);
        }
    }

    private void switchCamera() {
        if (mRtcEngine == null) return;
        mRtcEngine.switchCamera();
    }

    private void toggleSpeaker() {
        if (mRtcEngine == null) return;
        mSpeakerOn = !mSpeakerOn;
        mRtcEngine.setEnableSpeakerphone(mSpeakerOn);
        if (mSpeakerOn) {
            mIvSpeaker.setImageResource(R.drawable.ic_speaker);
        } else {
            mIvSpeaker.setImageResource(R.drawable.ic_speaker_off);
        }
    }

    private void startCallTimer() {
        mCallSeconds = 0;
        mTimerRunnable = new Runnable() {
            @Override
            public void run() {
                mCallSeconds++;
                int minutes = mCallSeconds / 60;
                int seconds = mCallSeconds % 60;
                mTvCallDuration.setText(String.format("%02d:%02d", minutes, seconds));
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(mTimerRunnable, 1000);
    }

    private void stopCallTimer() {
        if (mTimerRunnable != null) {
            mHandler.removeCallbacks(mTimerRunnable);
            mTimerRunnable = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCallTimer();
        if (mRtcEngine != null) {
            if (mJoined) {
                mRtcEngine.leaveChannel();
            }
            mRtcEngine.stopPreview();
            RtcEngine.destroy();
            mRtcEngine = null;
        }
    }
}
