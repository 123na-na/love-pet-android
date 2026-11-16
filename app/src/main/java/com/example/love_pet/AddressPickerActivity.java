package com.example.love_pet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AddressPickerActivity extends AppCompatActivity {

    private static final String PREF_ADDRESS = "address_prefs";
    private static final String KEY_SELECTED_ID = "selected_address_id";

    private String getPrefKey() {
        return PREF_ADDRESS + "_" + UserManager.getInstance().getCurrentUserId();
    }

    private List<Address> addressList;
    private String selectedId = "";
    private boolean isManageMode = false;

    private AddAddressDialogHelper addAddressHelper;

    private LinearLayout llAddressList;
    private TextView tvManage;
    private Button btnConfirm;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_picker);

        addAddressHelper = new AddAddressDialogHelper(this);

        initViews();
        loadAddresses();
        renderAddressList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        addAddressHelper.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        addAddressHelper.handleActivityResult(requestCode, resultCode, data);
    }

    private void initViews() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());

        llAddressList = findViewById(R.id.ll_address_list);
        tvManage = findViewById(R.id.tv_manage);
        btnConfirm = findViewById(R.id.btn_confirm);
        layoutEmpty = findViewById(R.id.layout_empty);

        findViewById(R.id.tv_add_address).setOnClickListener(v -> {
            showAddAddressDialog(null);
        });

        tvManage.setOnClickListener(v -> {
            isManageMode = !isManageMode;
            tvManage.setText(isManageMode ? "完成" : "管理");
            renderAddressList();
        });

        btnConfirm.setOnClickListener(v -> {
            if (TextUtils.isEmpty(selectedId) && !addressList.isEmpty()) {
                selectedId = addressList.get(0).getId();
            }
            if (TextUtils.isEmpty(selectedId)) {
                Toast.makeText(this, "请选择收货地址", Toast.LENGTH_SHORT).show();
                return;
            }
            saveSelectedAddress();
            setResult(RESULT_OK);
            finish();
        });
    }

    private void loadAddresses() {
        addressList = Address.getAddresses(this);
        SharedPreferences prefs = getSharedPreferences(getPrefKey(), MODE_PRIVATE);
        selectedId = prefs.getString(KEY_SELECTED_ID, "");

        if (TextUtils.isEmpty(selectedId) && !addressList.isEmpty()) {
            for (Address addr : addressList) {
                if (addr.isDefault()) {
                    selectedId = addr.getId();
                    break;
                }
            }
            if (TextUtils.isEmpty(selectedId)) {
                selectedId = addressList.get(0).getId();
            }
        }
    }

    private void saveSelectedAddress() {
        SharedPreferences prefs = getSharedPreferences(getPrefKey(), MODE_PRIVATE);
        prefs.edit().putString(KEY_SELECTED_ID, selectedId).apply();
    }

    private void renderAddressList() {
        llAddressList.removeAllViews();

        if (addressList.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            llAddressList.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        llAddressList.setVisibility(View.VISIBLE);

        for (int i = 0; i < addressList.size(); i++) {
            Address item = addressList.get(i);
            View itemView = createAddressItemView(item);
            llAddressList.addView(itemView);

            if (i < addressList.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp2px(1));
                dividerParams.leftMargin = dp2px(44);
                divider.setBackgroundColor(0xFFF0F0F0);
                llAddressList.addView(divider, dividerParams);
            }
        }
    }

    private View createAddressItemView(final Address item) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setGravity(Gravity.CENTER_VERTICAL);
        itemLayout.setPadding(dp2px(0), dp2px(14), dp2px(0), dp2px(14));
        itemLayout.setBackgroundColor(0xFFFFFFFF);

        RadioButton radioBtn = new RadioButton(this);
        radioBtn.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
        radioBtn.setChecked(item.getId().equals(selectedId));
        radioBtn.setClickable(false);
        radioBtn.setFocusable(false);
        LinearLayout.LayoutParams radioParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        radioParams.rightMargin = dp2px(10);
        itemLayout.addView(radioBtn, radioParams);

        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        itemLayout.addView(infoLayout, infoParams);

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);
        infoLayout.addView(nameRow);

        TextView nameTv = new TextView(this);
        nameTv.setText(item.getName());
        nameTv.setTextColor(0xFF333333);
        nameTv.setTextSize(15);
        nameTv.setTypeface(null, Typeface.BOLD);
        nameRow.addView(nameTv);

        TextView phoneTv = new TextView(this);
        phoneTv.setText("  " + item.getPhone());
        phoneTv.setTextColor(0xFF333333);
        phoneTv.setTextSize(15);
        nameRow.addView(phoneTv);

        if (!TextUtils.isEmpty(item.getTag())) {
            TextView labelTv = new TextView(this);
            labelTv.setText(item.getTag());
            labelTv.setTextColor(0xFFFF6B35);
            labelTv.setTextSize(12);
            labelTv.setGravity(Gravity.CENTER);
            GradientDrawable labelBg = new GradientDrawable();
            labelBg.setColor(0xFFFFF0E6);
            labelBg.setCornerRadius(dp2px(4));
            labelTv.setBackground(labelBg);
            labelTv.setPadding(dp2px(8), dp2px(2), dp2px(8), dp2px(2));
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.leftMargin = dp2px(10);
            nameRow.addView(labelTv, labelParams);
        }

        TextView addrTv = new TextView(this);
        addrTv.setText(item.getFullAddress());
        addrTv.setTextColor(0xFF666666);
        addrTv.setTextSize(13);
        addrTv.setMaxLines(2);
        addrTv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams addrParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        addrParams.topMargin = dp2px(6);
        infoLayout.addView(addrTv, addrParams);

        if (isManageMode) {
            ImageView editIv = new ImageView(this);
            editIv.setImageResource(android.R.drawable.ic_menu_edit);
            editIv.setColorFilter(0xFF999999);
            editIv.setPadding(dp2px(8), dp2px(8), dp2px(8), dp2px(8));
            editIv.setOnClickListener(v -> {
                showAddAddressDialog(item);
            });
            itemLayout.addView(editIv);

            ImageView deleteIv = new ImageView(this);
            deleteIv.setImageResource(android.R.drawable.ic_menu_delete);
            deleteIv.setColorFilter(0xFF999999);
            deleteIv.setPadding(dp2px(8), dp2px(8), dp2px(8), dp2px(8));
            deleteIv.setOnClickListener(v -> {
                Address.deleteAddress(this, item.getId());
                addressList = Address.getAddresses(this);
                if (selectedId.equals(item.getId())) {
                    selectedId = addressList.isEmpty() ? "" : addressList.get(0).getId();
                }
                saveSelectedAddress();
                renderAddressList();
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
            });
            itemLayout.addView(deleteIv);
        } else {
            itemLayout.setOnClickListener(v -> {
                selectedId = item.getId();
                renderAddressList();
            });
        }

        return itemLayout;
    }

    private void showAddAddressDialog(Address editAddress) {
        addAddressHelper.showAddAddressDialog(editAddress, address -> {
            addressList = Address.getAddresses(this);
            selectedId = address.getId();
            saveSelectedAddress();
            renderAddressList();
        });
    }

    private int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
