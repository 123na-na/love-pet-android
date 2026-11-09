package com.example.love_pet;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ReportActivity extends AppCompatActivity {

    private String selectedReason = "";
    private String postTitle = "";
    private int noteId = 0;

    private ImageView ivCheckSpam;
    private ImageView ivCheckPorn;
    private ImageView ivCheckViolence;
    private ImageView ivCheckFake;
    private ImageView ivCheckSteal;
    private ImageView ivCheckOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        postTitle = getIntent().getStringExtra("post_title");
        noteId = getIntent().getIntExtra("note_id", 0);
        if (postTitle == null) postTitle = "";

        TextView tvPostTitle = findViewById(R.id.tv_post_title);
        tvPostTitle.setText("举报内容：" + postTitle);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> finish());

        ivCheckSpam = findViewById(R.id.iv_check_spam);
        ivCheckPorn = findViewById(R.id.iv_check_porn);
        ivCheckViolence = findViewById(R.id.iv_check_violence);
        ivCheckFake = findViewById(R.id.iv_check_fake);
        ivCheckSteal = findViewById(R.id.iv_check_steal);
        ivCheckOther = findViewById(R.id.iv_check_other);

        findViewById(R.id.layout_reason_spam).setOnClickListener(v -> selectReason("垃圾广告/诈骗信息", ivCheckSpam));
        findViewById(R.id.layout_reason_porn).setOnClickListener(v -> selectReason("色情低俗内容", ivCheckPorn));
        findViewById(R.id.layout_reason_violence).setOnClickListener(v -> selectReason("暴力血腥内容", ivCheckViolence));
        findViewById(R.id.layout_reason_fake).setOnClickListener(v -> selectReason("虚假信息/造谣", ivCheckFake));
        findViewById(R.id.layout_reason_steal).setOnClickListener(v -> selectReason("盗用他人作品", ivCheckSteal));
        findViewById(R.id.layout_reason_other).setOnClickListener(v -> selectReason("其他原因", ivCheckOther));

        findViewById(R.id.btn_submit_report).setOnClickListener(v -> submitReport());
    }

    private void selectReason(String reason, ImageView checkView) {
        selectedReason = reason;
        resetAllChecks();
        checkView.setImageResource(R.drawable.ic_check_blue);
    }

    private void resetAllChecks() {
        ivCheckSpam.setImageResource(R.drawable.ic_uncheck);
        ivCheckPorn.setImageResource(R.drawable.ic_uncheck);
        ivCheckViolence.setImageResource(R.drawable.ic_uncheck);
        ivCheckFake.setImageResource(R.drawable.ic_uncheck);
        ivCheckSteal.setImageResource(R.drawable.ic_uncheck);
        ivCheckOther.setImageResource(R.drawable.ic_uncheck);
    }

    private void submitReport() {
        if (selectedReason.isEmpty()) {
            Toast.makeText(this, "请选择举报原因", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText etDetail = findViewById(R.id.et_report_detail);
        String detail = etDetail.getText().toString().trim();

        SystemMessageManager msgManager = SystemMessageManager.getInstance();
        String content = "您举报的「" + postTitle + "」已收到，我们将在24小时内处理。举报原因：" + selectedReason;
        if (!detail.isEmpty()) {
            content += "\n补充说明：" + detail;
        }
        msgManager.addSystemMessage(this, "举报受理通知", content, System.currentTimeMillis());

        Toast.makeText(this, "举报已提交，我们会尽快处理", Toast.LENGTH_SHORT).show();
        finish();
    }
}
