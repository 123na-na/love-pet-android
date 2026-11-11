package com.example.love_pet;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportAnalysisActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_GALLERY = 1002;
    private static final int REQUEST_CAMERA_PERMISSION = 1003;

    private LinearLayout layoutHistory;
    private LinearLayout layoutUpload;
    private LinearLayout layoutEmpty;
    private List<ReportRecord> reportList = new ArrayList<>();
    private Uri photoUri;
    private QwenApiHelper qwenApiHelper;

    public static class ReportRecord {
        public String type;
        public String petName;
        public String date;
        public String result;
        public String suggestion;
        public int status;

        public ReportRecord(String type, String petName, String date, String result, String suggestion, int status) {
            this.type = type;
            this.petName = petName;
            this.date = date;
            this.result = result;
            this.suggestion = suggestion;
            this.status = status;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_analysis);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        layoutHistory = findViewById(R.id.layout_history);
        layoutUpload = findViewById(R.id.layout_upload);
        layoutEmpty = findViewById(R.id.layout_empty);

        layoutUpload.setOnClickListener(v -> showUploadOptions());

        qwenApiHelper = new QwenApiHelper();

        loadHistory();
    }

    private void showUploadOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_choose_image, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btn_take_photo).setOnClickListener(v -> {
            dialog.dismiss();
            checkCameraPermission();
        });

        dialogView.findViewById(R.id.btn_choose_gallery).setOnClickListener(v -> {
            dialog.dismiss();
            openGallery();
        });

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            takePhoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        } else {
            Toast.makeText(this, "需要相机权限", Toast.LENGTH_SHORT).show();
        }
    }

    private void takePhoto() {
        try {
            File photoFile = createImageFile();
            photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, REQUEST_CAMERA);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "拍照失败", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir("Pictures");
        return File.createTempFile("report_" + timeStamp, ".jpg", storageDir);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA && photoUri != null) {
                analyzeReport(photoUri);
            } else if (requestCode == REQUEST_GALLERY && data != null && data.getData() != null) {
                analyzeReport(data.getData());
            }
        }
    }

    private void analyzeReport(Uri imageUri) {
        showPetSelectDialog(imageUri);
    }

    private void showPetSelectDialog(final Uri imageUri) {
        final String[] petNames = {"橘猫", "金毛", "布偶", "其他宠物"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择患宠");
        builder.setItems(petNames, (dialog, which) -> {
            String petName = petNames[which];
            if (which == petNames.length - 1) {
                petName = "未知";
            }
            doAnalyzeReport(imageUri, petName);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void doAnalyzeReport(Uri imageUri, final String petName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_analyzing, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.show();

        String prompt = "这是一张宠物检查报告或化验单的照片。请用中文回答："
                + "1）先说清图片上能辨认的检查项目和数值；"
                + "2）指出明显偏离常见参考范围的项；"
                + "3）给一句就医建议；"
                + "4）结尾固定写“以上仅为图片信息整理，不能作为诊断，请以执业兽医的判断为准”。"
                + "如果图片看不清或不是报告单，请直接说明无法识别，不要编造任何数据。";

        qwenApiHelper.sendImageMessage(this, imageUri, prompt, new QwenApiHelper.OnResponseCallback() {
            @Override
            public void onSuccess(String reply) {
                dialog.dismiss();
                String cleaned = reply == null ? "" : reply.trim();
                if (cleaned.isEmpty()) {
                    cleaned = "图片信息读取完成，但未识别到明确数值。";
                }
                String result = cleaned;
                String suggestion = "以上仅为图片信息整理，不能作为诊断，请以执业兽医的判断为准。";
                int cut = cleaned.indexOf("以上仅为图片信息整理");
                if (cut > 0) {
                    result = cleaned.substring(0, cut).trim();
                }
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                addReportRecord(new ReportRecord("报告解读", petName, date, result, suggestion, 1));
                Toast.makeText(ReportAnalysisActivity.this, "解读完成", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                dialog.dismiss();
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                addReportRecord(new ReportRecord("报告解读", petName, date,
                        "暂时无法完成图片识别，未生成解读结果。",
                        "请携带原件到宠物医院，由执业兽医当面解读。", 2));
            }
        });
    }

    private List<ReportRecord> getReports() {
        if (reportList.isEmpty()) {
            reportList.add(new ReportRecord("血常规检查", "橘猫", "2024-01-12", "基本正常，白细胞略高", "建议观察，一周后复查", 1));
            reportList.add(new ReportRecord("生化全套", "金毛", "2024-01-05", "肝功能指标正常", "继续保持，定期体检", 1));
            reportList.add(new ReportRecord("X光片", "布偶", "2023-12-28", "骨折愈合良好", "继续康复训练，定期复查", 1));
        }
        return reportList;
    }

    private void loadHistory() {
        List<ReportRecord> reports = getReports();

        if (reports.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            layoutHistory.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        layoutHistory.setVisibility(View.VISIBLE);

        for (ReportRecord report : reports) {
            View item = createReportItem(report);
            layoutHistory.addView(item);
        }
    }

    private void addReportRecord(ReportRecord record) {
        reportList.add(0, record);
        layoutHistory.removeAllViews();
        for (ReportRecord r : reportList) {
            layoutHistory.addView(createReportItem(r));
        }
        layoutEmpty.setVisibility(View.GONE);
        layoutHistory.setVisibility(View.VISIBLE);
    }

    private View createReportItem(final ReportRecord report) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setBackgroundColor(Color.WHITE);
        item.setPadding(dp2px(16), dp2px(16), dp2px(16), dp2px(16));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        item.addView(topRow);

        TextView typeTv = new TextView(this);
        typeTv.setText(report.type);
        typeTv.setTextSize(15);
        typeTv.setTextColor(Color.parseColor("#333333"));
        typeTv.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams typeParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        topRow.addView(typeTv, typeParams);

        TextView statusTv = new TextView(this);
        statusTv.setText("已解读");
        statusTv.setTextSize(12);
        statusTv.setTextColor(Color.parseColor("#34C759"));
        statusTv.setBackgroundColor(Color.parseColor("#E8F8ED"));
        statusTv.setPadding(dp2px(8), dp2px(3), dp2px(8), dp2px(3));
        topRow.addView(statusTv);

        TextView petTv = new TextView(this);
        petTv.setText("患宠：" + report.petName);
        petTv.setTextSize(13);
        petTv.setTextColor(Color.parseColor("#666666"));
        petTv.setPadding(0, dp2px(8), 0, 0);
        item.addView(petTv);

        TextView dateTv = new TextView(this);
        dateTv.setText("检查日期：" + report.date);
        dateTv.setTextSize(13);
        dateTv.setTextColor(Color.parseColor("#666666"));
        dateTv.setPadding(0, dp2px(4), 0, 0);
        item.addView(dateTv);

        TextView resultTv = new TextView(this);
        resultTv.setText("检查结果：" + report.result);
        resultTv.setTextSize(13);
        resultTv.setTextColor(Color.parseColor("#333333"));
        resultTv.setPadding(0, dp2px(8), 0, 0);
        item.addView(resultTv);

        TextView sugTv = new TextView(this);
        sugTv.setText("医生建议：" + report.suggestion);
        sugTv.setTextSize(13);
        sugTv.setTextColor(Color.parseColor("#FF6B35"));
        sugTv.setPadding(0, dp2px(4), 0, 0);
        item.addView(sugTv);

        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#F0F0F0"));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp2px(1));
        dividerParams.topMargin = dp2px(16);
        item.addView(divider, dividerParams);

        return item;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (qwenApiHelper != null) {
            qwenApiHelper.release();
        }
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
