package com.example.love_pet;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class AdoptApplyActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_IMAGE_RES = "extra_image_res";
    public static final String EXTRA_AGE = "extra_age";
    public static final String EXTRA_WEIGHT = "extra_weight";
    public static final String EXTRA_GENDER = "extra_gender";
    public static final String EXTRA_LOCATION = "extra_location";

    private EditText etName;
    private EditText etPhone;
    private EditText etAddress;
    private EditText etRemark;
    private TextView btnSubmit;

    private TextView tvHomeOwn;
    private TextView tvHomeRent;
    private TextView tvHomeFamily;
    private String selectedHome = "";

    private TextView tvExpNew;
    private TextView tvExpSome;
    private TextView tvExpPro;
    private String selectedExp = "";

    private TextView tvWorkStable;
    private TextView tvWorkFreelance;
    private TextView tvWorkStudent;
    private String selectedWork = "";

    private ImageView ivAgree;
    private boolean isAgreed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adopt_apply);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etRemark = findViewById(R.id.et_remark);
        btnSubmit = findViewById(R.id.btn_submit);

        tvHomeOwn = findViewById(R.id.tv_home_own);
        tvHomeRent = findViewById(R.id.tv_home_rent);
        tvHomeFamily = findViewById(R.id.tv_home_family);

        tvExpNew = findViewById(R.id.tv_exp_new);
        tvExpSome = findViewById(R.id.tv_exp_some);
        tvExpPro = findViewById(R.id.tv_exp_pro);

        tvWorkStable = findViewById(R.id.tv_work_stable);
        tvWorkFreelance = findViewById(R.id.tv_work_freelance);
        tvWorkStudent = findViewById(R.id.tv_work_student);

        ivAgree = findViewById(R.id.iv_agree);
        TextView tvAgreement = findViewById(R.id.tv_agreement);

        initPetInfo();
        setupTagClick();
        setupListeners();
        updateSubmitButton();
    }

    private void initPetInfo() {
        Intent intent = getIntent();
        String name = intent.getStringExtra(EXTRA_NAME);
        int imageRes = intent.getIntExtra(EXTRA_IMAGE_RES, 0);
        String age = intent.getStringExtra(EXTRA_AGE);
        String weight = intent.getStringExtra(EXTRA_WEIGHT);
        String gender = intent.getStringExtra(EXTRA_GENDER);
        String location = intent.getStringExtra(EXTRA_LOCATION);

        ImageView ivPet = findViewById(R.id.iv_pet);
        TextView tvPetName = findViewById(R.id.tv_pet_name);
        TextView tvPetInfo = findViewById(R.id.tv_pet_info);
        TextView tvPetLocation = findViewById(R.id.tv_pet_location);

        Glide.with(this).load(imageRes).centerCrop().into(ivPet);
        tvPetName.setText(name);
        tvPetInfo.setText(age + " | " + weight + " | " + gender);
        tvPetLocation.setText(location);
    }

    private void setupTagClick() {
        tvHomeOwn.setOnClickListener(v -> selectHome("own", tvHomeOwn));
        tvHomeRent.setOnClickListener(v -> selectHome("rent", tvHomeRent));
        tvHomeFamily.setOnClickListener(v -> selectHome("family", tvHomeFamily));

        tvExpNew.setOnClickListener(v -> selectExp("new", tvExpNew));
        tvExpSome.setOnClickListener(v -> selectExp("some", tvExpSome));
        tvExpPro.setOnClickListener(v -> selectExp("pro", tvExpPro));

        tvWorkStable.setOnClickListener(v -> selectWork("stable", tvWorkStable));
        tvWorkFreelance.setOnClickListener(v -> selectWork("freelance", tvWorkFreelance));
        tvWorkStudent.setOnClickListener(v -> selectWork("student", tvWorkStudent));
    }

    private void selectHome(String type, TextView tv) {
        selectedHome = type;
        resetHomeTags();
        tv.setBackgroundResource(R.drawable.bg_tag_selected);
        tv.setTextColor(getResources().getColor(R.color.white));
        updateSubmitButton();
    }

    private void resetHomeTags() {
        tvHomeOwn.setBackgroundResource(R.drawable.bg_tag_normal);
        tvHomeOwn.setTextColor(getResources().getColor(R.color.gray_66));
        tvHomeRent.setBackgroundResource(R.drawable.bg_tag_normal);
        tvHomeRent.setTextColor(getResources().getColor(R.color.gray_66));
        tvHomeFamily.setBackgroundResource(R.drawable.bg_tag_normal);
        tvHomeFamily.setTextColor(getResources().getColor(R.color.gray_66));
    }

    private void selectExp(String type, TextView tv) {
        selectedExp = type;
        resetExpTags();
        tv.setBackgroundResource(R.drawable.bg_tag_selected);
        tv.setTextColor(getResources().getColor(R.color.white));
        updateSubmitButton();
    }

    private void resetExpTags() {
        tvExpNew.setBackgroundResource(R.drawable.bg_tag_normal);
        tvExpNew.setTextColor(getResources().getColor(R.color.gray_66));
        tvExpSome.setBackgroundResource(R.drawable.bg_tag_normal);
        tvExpSome.setTextColor(getResources().getColor(R.color.gray_66));
        tvExpPro.setBackgroundResource(R.drawable.bg_tag_normal);
        tvExpPro.setTextColor(getResources().getColor(R.color.gray_66));
    }

    private void selectWork(String type, TextView tv) {
        selectedWork = type;
        resetWorkTags();
        tv.setBackgroundResource(R.drawable.bg_tag_selected);
        tv.setTextColor(getResources().getColor(R.color.white));
        updateSubmitButton();
    }

    private void resetWorkTags() {
        tvWorkStable.setBackgroundResource(R.drawable.bg_tag_normal);
        tvWorkStable.setTextColor(getResources().getColor(R.color.gray_66));
        tvWorkFreelance.setBackgroundResource(R.drawable.bg_tag_normal);
        tvWorkFreelance.setTextColor(getResources().getColor(R.color.gray_66));
        tvWorkStudent.setBackgroundResource(R.drawable.bg_tag_normal);
        tvWorkStudent.setTextColor(getResources().getColor(R.color.gray_66));
    }

    private void setupListeners() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etName.addTextChangedListener(watcher);
        etPhone.addTextChangedListener(watcher);
        etAddress.addTextChangedListener(watcher);

        ivAgree.setOnClickListener(v -> {
            isAgreed = !isAgreed;
            ivAgree.setImageResource(isAgreed ? R.drawable.ic_check_blue : R.drawable.ic_close_gray);
            updateSubmitButton();
        });

        btnSubmit.setOnClickListener(v -> {
            if (!isAgreed) {
                Toast.makeText(this, "请先同意领养协议", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedHome.isEmpty()) {
                Toast.makeText(this, "请选择居住情况", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedExp.isEmpty()) {
                Toast.makeText(this, "请选择养宠经验", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedWork.isEmpty()) {
                Toast.makeText(this, "请选择工作情况", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(AdoptApplyActivity.this, AdoptSuccessActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void updateSubmitButton() {
        boolean enabled = !etName.getText().toString().trim().isEmpty()
                && !etPhone.getText().toString().trim().isEmpty()
                && !etAddress.getText().toString().trim().isEmpty()
                && !selectedHome.isEmpty()
                && !selectedExp.isEmpty()
                && !selectedWork.isEmpty()
                && isAgreed;

        btnSubmit.setEnabled(enabled);
        btnSubmit.setAlpha(enabled ? 1.0f : 0.5f);
    }
}
