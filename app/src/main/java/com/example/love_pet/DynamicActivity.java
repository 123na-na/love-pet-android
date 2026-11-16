package com.example.love_pet;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicActivity extends AppCompatActivity {

    private int selectedDay = -1;
    private int currentYear = 0;
    private int currentMonth = 0;
    private int weekOffset = 0;
    private List<TextView> dateViews = new ArrayList<>();

    // 每日记录数据：日期 -> 记录列表
    private Map<Integer, List<RecordItem>> recordsByDay = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic);

        // 初始化记录数据
        initRecordsData();

        // 使用当前实际日期
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        weekOffset = 0;

        initHeader();
        initCalendar();
        initWeekButtons();
        initRecordList();
        initAddButton();
        loadPetInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPetInfo();
    }

    private void loadPetInfo() {
        List<PetManager.Pet> pets = PetManager.getInstance().getPets(this);
        if (pets != null && !pets.isEmpty()) {
            PetManager.Pet pet = pets.get(0);
            ImageView ivAvatar = findViewById(R.id.iv_pet_avatar);
            TextView tvName = findViewById(R.id.tv_pet_name);
            TextView tvInfo = findViewById(R.id.tv_pet_info);

            if (pet.avatarPath != null && !pet.avatarPath.isEmpty()) {
                java.io.File avatarFile = new java.io.File(pet.avatarPath);
                if (avatarFile.exists()) {
                    com.bumptech.glide.Glide.with(this)
                            .load(avatarFile)
                            .circleCrop()
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.img_cat_hua);
                }
            } else {
                ivAvatar.setImageResource(R.drawable.img_cat_hua);
            }

            if (pet.name != null && !pet.name.isEmpty()) {
                tvName.setText(pet.name);
            }

            String info = "";
            if (pet.breed != null && !pet.breed.isEmpty()) {
                info = pet.breed;
            }
            if (pet.gender != null && !pet.gender.isEmpty()) {
                if (!info.isEmpty()) info += " | ";
                info += pet.gender.equals("male") ? "公" : "母";
            }
            if (!info.isEmpty()) {
                tvInfo.setText(info);
            }
        }
    }

    private void initRecordsData() {
        recordsByDay.put(9, new ArrayList<>());
        
        List<RecordItem> day10Records = new ArrayList<>();
        day10Records.add(new RecordItem(R.drawable.vaccine, "给花花打疫苗"));
        day10Records.add(new RecordItem(R.drawable.litter_box, "清理猫砂盆"));
        day10Records.add(new RecordItem(R.drawable.cat_food, "购买花花的鱼罐头"));
        day10Records.add(new RecordItem(R.drawable.bathing, "给花花洗澡"));
        day10Records.add(new RecordItem(R.drawable.scissors, "给花花修剪毛发"));
        recordsByDay.put(10, day10Records);
        
        recordsByDay.put(11, new ArrayList<>());
        recordsByDay.put(12, new ArrayList<>());
        recordsByDay.put(13, new ArrayList<>());
        
        List<RecordItem> day15Records = new ArrayList<>();
        day15Records.add(new RecordItem(R.drawable.bathing, "给花花洗澡"));
        recordsByDay.put(15, day15Records);
        
        recordsByDay.put(16, new ArrayList<>());
    }

    private void initHeader() {
        updateHeaderDisplay();
    }

    private void updateHeaderDisplay() {
        TextView tvMonth = findViewById(R.id.tv_month);
        tvMonth.setText(currentMonth + "月");
    }

    private void initWeekButtons() {
        ImageView ivPrevWeek = findViewById(R.id.iv_prev_week);
        ImageView ivNextWeek = findViewById(R.id.iv_next_week);

        ivPrevWeek.setOnClickListener(v -> changeWeek(-1));
        ivNextWeek.setOnClickListener(v -> changeWeek(1));
    }

    private void changeWeek(int direction) {
        weekOffset += direction;

        // 计算新周的起始日期
        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth - 1, selectedDay);
        cal.add(Calendar.WEEK_OF_YEAR, direction * 7);

        // 调整到该周周一
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int diff = Calendar.MONDAY - dayOfWeek;
        cal.add(Calendar.DATE, diff);

        currentYear = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH) + 1;
        selectedDay = cal.get(Calendar.DAY_OF_MONTH);

        updateHeaderDisplay();
        refreshCalendar();
        refreshRecordList();
    }

    private void refreshCalendar() {
        dateViews.clear();
        LinearLayout layoutDates = findViewById(R.id.layout_dates);
        layoutDates.removeAllViews();

        // 根据当前日期生成本周的7天（周一到周日）
        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth - 1, selectedDay);

        // 调整到本周一
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int diff = Calendar.MONDAY - dayOfWeek;
        calendar.add(Calendar.DATE, diff);

        List<int[]> dates = new ArrayList<>(); // [day, month]
        for (int i = 0; i < 7; i++) {
            int d = calendar.get(Calendar.DAY_OF_MONTH);
            int m = calendar.get(Calendar.MONTH) + 1;
            dates.add(new int[]{d, m});
            calendar.add(Calendar.DATE, 1);
        }

        for (int[] datePair : dates) {
            int day = datePair[0];
            int month = datePair[1];

            if (month != currentMonth) {
                // 跨月显示为空
                TextView emptyView = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                emptyView.setLayoutParams(params);
                emptyView.setGravity(Gravity.CENTER);
                emptyView.setText("");
                emptyView.setTextSize(14f);
                layoutDates.addView(emptyView);
                dateViews.add(emptyView);
            } else {
                TextView dateView = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                dateView.setLayoutParams(params);
                dateView.setGravity(Gravity.CENTER);
                dateView.setText(String.valueOf(day));
                dateView.setTextSize(14f);
                dateView.setTextColor(0xFF333333);
                dateView.setPadding(dpToPx(6), dpToPx(4), dpToPx(6), dpToPx(4));

                final int dayVal = day;
                dateView.setOnClickListener(v -> selectDate(dayVal));
                layoutDates.addView(dateView);
                dateViews.add(dateView);
            }
        }

        updateDateSelection();
    }

    private void initCalendar() {
        refreshCalendar();
    }

    private void selectDate(int day) {
        selectedDay = day;
        updateDateSelection();
        updateHeaderDisplay();
        refreshRecordList();
    }

    private void updateDateSelection() {
        for (TextView view : dateViews) {
            String dayTextStr = view.getText().toString();
            Integer dayText = null;
            try {
                dayText = Integer.parseInt(dayTextStr);
            } catch (NumberFormatException e) {
                // 空字符串，忽略
            }
            if (dayText != null && dayText == selectedDay) {
                view.setTextColor(0xFFFFFFFF);
                view.setBackgroundResource(R.drawable.bg_date_selected);
            } else {
                view.setTextColor(0xFF333333);
                view.setBackground(null);
            }
        }
    }

    private void initRecordList() {
        refreshRecordList();
    }

    private void refreshRecordList() {
        LinearLayout recordList = findViewById(R.id.layout_record_list);
        recordList.removeAllViews();

        List<RecordItem> records = recordsByDay.get(selectedDay);
        if (records == null) records = new ArrayList<>();

        if (records.isEmpty()) {
            TextView emptyView = new TextView(this);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            emptyView.setLayoutParams(params);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setText("暂无记录，点击下方+添加");
            emptyView.setTextSize(13f);
            emptyView.setTextColor(0xFF999999);
            emptyView.setPadding(0, dpToPx(30), 0, dpToPx(10));
            recordList.addView(emptyView);
        } else {
            for (RecordItem record : records) {
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_record, recordList, false);

                ImageView ivRecordIcon = itemView.findViewById(R.id.iv_record_icon);
                ivRecordIcon.setImageResource(record.getIconRes());

                TextView tvRecordTitle = itemView.findViewById(R.id.tv_record_title);
                tvRecordTitle.setText(record.getTitle());

                // 编辑按钮点击
                ImageView ivEdit = itemView.findViewById(R.id.iv_edit);
                ivEdit.setOnClickListener(v -> showAddDialog(record.getTitle()));

                recordList.addView(itemView);
            }
        }
    }

    private void initAddButton() {
        View btnAddRecord = findViewById(R.id.btn_add_record);
        btnAddRecord.setOnClickListener(v -> showAddDialog(""));
    }

    private void showAddDialog(String existingTitle) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(existingTitle.isEmpty() ? "添加提醒事项" : "编辑提醒事项");

        EditText input = new EditText(this);
        input.setHint("输入提醒内容");
        if (!existingTitle.isEmpty()) input.setText(existingTitle);
        input.setSingleLine(true);
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String content = input.getText().toString().trim();
            if (!content.isEmpty()) {
                addOrUpdateRecord(content);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void addOrUpdateRecord(String title) {
        List<RecordItem> currentRecords = recordsByDay.get(selectedDay);
        if (currentRecords == null) currentRecords = new ArrayList<>();

        boolean exists = false;
        for (RecordItem item : currentRecords) {
            if (item.getTitle().equals(title)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            int iconRes = R.drawable.ic_paw_black;
            currentRecords.add(new RecordItem(iconRes, title));
            recordsByDay.put(selectedDay, currentRecords);
            refreshRecordList();

            Toast.makeText(this, "已添加到 " + currentMonth + "月" + selectedDay + "日", Toast.LENGTH_SHORT).show();
        }
    }

    // RecordItem 内部类
    private static class RecordItem {
        private final int iconRes;
        private final String title;

        public RecordItem(int iconRes, String title) {
            this.iconRes = iconRes;
            this.title = title;
        }

        public int getIconRes() {
            return iconRes;
        }

        public String getTitle() {
            return title;
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}