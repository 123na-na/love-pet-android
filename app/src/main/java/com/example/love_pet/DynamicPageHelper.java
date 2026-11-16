package com.example.love_pet;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicPageHelper {

    private AppCompatActivity activity;
    private View rootView;
    private int selectedDay = -1;
    private int currentYear = 0;
    private int currentMonth = 0;
    private int weekOffset = 0;
    private List<TextView> dateViews = new ArrayList<>();

    private List<PetManager.Pet> petList = new ArrayList<>();
    private int currentPetIndex = 0;
    private ViewPager vpPetCards;
    private LinearLayout layoutIndicator;
    private PetCardPagerAdapter pagerAdapter;

    private Map<String, Map<Integer, List<RecordItem>>> allPetRecords = new HashMap<>();
    private static final String PREFS_NAME = "pet_records";

    public DynamicPageHelper(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void initDynamicPage(View rootView) {
        this.rootView = rootView;

        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH) + 1;
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        weekOffset = 0;

        loadPets();
        initPetCarousel(rootView);
        initHeader(rootView);
        initCalendar(rootView);
        initWeekButtons(rootView);
        initRecordList(rootView);
        initAddButton(rootView);
    }

    private void loadPets() {
        petList = PetManager.getInstance().getPets(activity);
        if (currentPetIndex >= petList.size()) {
            currentPetIndex = Math.max(0, petList.size() - 1);
        }
    }

    private void initPetCarousel(View rootView) {
        vpPetCards = rootView.findViewById(R.id.vp_pet_cards);
        layoutIndicator = rootView.findViewById(R.id.layout_pet_indicator);

        if (vpPetCards == null) return;

        pagerAdapter = new PetCardPagerAdapter();
        vpPetCards.setAdapter(pagerAdapter);

        vpPetCards.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                currentPetIndex = position;
                updateIndicator(position);
                if (position < petList.size()) {
                    refreshRecordList(rootView);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        if (currentPetIndex > 0 && currentPetIndex < pagerAdapter.getCount()) {
            vpPetCards.setCurrentItem(currentPetIndex, false);
        }
        updateIndicator(vpPetCards.getCurrentItem());
    }

    private void bindPetCard(View cardView, PetManager.Pet pet) {
        TextView tvPetName = cardView.findViewById(R.id.tv_pet_name);
        TextView tvPetInfo = cardView.findViewById(R.id.tv_pet_info);
        ImageView ivPetAvatar = cardView.findViewById(R.id.iv_pet_avatar);
        LinearLayout layoutTags = cardView.findViewById(R.id.layout_pet_tags);

        if (tvPetName != null) {
            tvPetName.setText(pet.name);
        }
        if (tvPetInfo != null) {
            String info = "";
            if (pet.breed != null && !pet.breed.isEmpty()) {
                info += pet.breed;
            }
            if (pet.weight != null && !pet.weight.isEmpty()) {
                if (!info.isEmpty()) info += " | ";
                info += pet.weight;
            }
            if (info.isEmpty()) {
                info = pet.type.equals("cat") ? "猫咪" : "狗狗";
            }
            tvPetInfo.setText(info);
        }
        if (ivPetAvatar != null) {
            if (pet.avatarPath != null && !pet.avatarPath.isEmpty()) {
                try {
                    java.io.File imgFile = new java.io.File(pet.avatarPath);
                    if (imgFile.exists()) {
                        com.bumptech.glide.Glide.with(activity)
                                .load(imgFile)
                                .circleCrop()
                                .into(ivPetAvatar);
                    } else {
                        ivPetAvatar.setImageResource(R.drawable.avatar_color6);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ivPetAvatar.setImageResource(R.drawable.avatar_color6);
                }
            } else {
                ivPetAvatar.setImageResource(R.drawable.avatar_color6);
            }
        }
        if (layoutTags != null) {
            layoutTags.removeAllViews();
            if (pet.tags != null && pet.tags.length > 0) {
                for (String tag : pet.tags) {
                    TextView tagView = new TextView(activity);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    if (layoutTags.getChildCount() > 0) {
                        params.leftMargin = dpToPx(8);
                    }
                    tagView.setLayoutParams(params);
                    tagView.setBackgroundResource(R.drawable.bg_tag_orange);
                    tagView.setPadding(dpToPx(12), dpToPx(3), dpToPx(12), dpToPx(3));
                    tagView.setText(tag);
                    tagView.setTextColor(0xFF8B5A00);
                    tagView.setTextSize(11f);
                    layoutTags.addView(tagView);
                }
            }
        }
    }

    private class PetCardPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return petList.isEmpty() ? 1 : petList.size() + 1;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView;
            if (petList.isEmpty() || position == petList.size()) {
                int layoutId = petList.isEmpty() ? R.layout.item_add_pet_card : R.layout.item_add_pet_card_small;
                itemView = LayoutInflater.from(activity).inflate(layoutId, container, false);
                itemView.setOnClickListener(v -> {
                    activity.startActivity(new Intent(activity, AddPetActivity.class));
                });
            } else {
                PetManager.Pet pet = petList.get(position);
                itemView = LayoutInflater.from(activity).inflate(R.layout.item_dynamic_pet_card, container, false);
                bindPetCard(itemView, pet);
                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(activity, AddPetActivity.class);
                    intent.putExtra(AddPetActivity.EXTRA_PET_ID, pet.id);
                    activity.startActivity(intent);
                });
            }
            container.addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    private void updateIndicator(int currentIndex) {
        if (layoutIndicator == null) return;
        layoutIndicator.removeAllViews();

        int totalDots = petList.isEmpty() ? 1 : petList.size() + 1;
        for (int i = 0; i < totalDots; i++) {
            View dot = new View(activity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(6), dpToPx(6));
            params.leftMargin = dpToPx(4);
            params.rightMargin = dpToPx(4);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == currentIndex ? R.drawable.bg_dot_selected : R.drawable.bg_dot_normal);
            layoutIndicator.addView(dot);
        }
    }

    private void initHeader(View rootView) {
        updateHeaderDisplay(rootView);
    }

    private void updateHeaderDisplay(View rootView) {
        TextView tvMonth = rootView.findViewById(R.id.tv_month);
        if (tvMonth != null) {
            tvMonth.setText(currentMonth + "月");
        }
    }

    private void initWeekButtons(View rootView) {
        ImageView ivPrevWeek = rootView.findViewById(R.id.iv_prev_week);
        ImageView ivNextWeek = rootView.findViewById(R.id.iv_next_week);

        if (ivPrevWeek != null) {
            ivPrevWeek.setOnClickListener(v -> changeWeek(rootView, -1));
        }
        if (ivNextWeek != null) {
            ivNextWeek.setOnClickListener(v -> changeWeek(rootView, 1));
        }
    }

    private void changeWeek(View rootView, int direction) {
        weekOffset += direction;

        Calendar cal = Calendar.getInstance();
        cal.set(currentYear, currentMonth - 1, selectedDay);
        cal.add(Calendar.DATE, direction * 7);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int diff = Calendar.MONDAY - dayOfWeek;
        cal.add(Calendar.DATE, diff);

        currentYear = cal.get(Calendar.YEAR);
        currentMonth = cal.get(Calendar.MONTH) + 1;
        selectedDay = cal.get(Calendar.DAY_OF_MONTH);

        updateHeaderDisplay(rootView);
        refreshCalendar(rootView);
        refreshRecordList(rootView);
    }

    private void refreshCalendar(View rootView) {
        dateViews.clear();
        LinearLayout layoutDates = rootView.findViewById(R.id.layout_dates);
        if (layoutDates == null) return;
        layoutDates.removeAllViews();

        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth - 1, selectedDay);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int diff = Calendar.MONDAY - dayOfWeek;
        calendar.add(Calendar.DATE, diff);

        List<int[]> dates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            int d = calendar.get(Calendar.DAY_OF_MONTH);
            int m = calendar.get(Calendar.MONTH) + 1;
            dates.add(new int[]{d, m});
            calendar.add(Calendar.DATE, 1);
        }

        for (int[] datePair : dates) {
            int day = datePair[0];
            int month = datePair[1];

            TextView dateView = new TextView(activity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            dateView.setLayoutParams(params);
            dateView.setGravity(Gravity.CENTER);
            dateView.setText(String.valueOf(day));
            dateView.setTextSize(14f);
            dateView.setTextColor(month != currentMonth ? 0xFFCCCCCC : 0xFF333333);
            dateView.setPadding(dpToPx(6), dpToPx(4), dpToPx(6), dpToPx(4));

            final int dayVal = day;
            final int monthVal = month;
            dateView.setOnClickListener(v -> selectDate(rootView, dayVal, monthVal));
            layoutDates.addView(dateView);
            dateViews.add(dateView);
        }

        updateDateSelection();
    }

    private void initCalendar(View rootView) {
        refreshCalendar(rootView);
    }

    private void selectDate(View rootView, int day, int month) {
        selectedDay = day;
        if (month != currentMonth) {
            currentMonth = month;
            Calendar cal = Calendar.getInstance();
            cal.set(currentYear, currentMonth - 1, day);
            currentYear = cal.get(Calendar.YEAR);
        }
        updateDateSelection();
        updateHeaderDisplay(rootView);
        refreshRecordList(rootView);
    }

    private void updateDateSelection() {
        for (TextView view : dateViews) {
            String dayTextStr = view.getText().toString();
            Integer dayText = null;
            try {
                dayText = Integer.parseInt(dayTextStr);
            } catch (NumberFormatException e) {
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

    private void initRecordList(View rootView) {
        refreshRecordList(rootView);
    }

    private Map<Integer, List<RecordItem>> getCurrentPetRecords() {
        if (petList.isEmpty() || currentPetIndex >= petList.size()) {
            return new HashMap<>();
        }
        String petId = petList.get(currentPetIndex).id;
        Map<Integer, List<RecordItem>> records = allPetRecords.get(petId);
        if (records == null) {
            records = loadRecordsFromPrefs(petId);
            allPetRecords.put(petId, records);
        }
        return records;
    }

    private void refreshRecordList(View rootView) {
        LinearLayout recordList = rootView.findViewById(R.id.layout_record_list);
        if (recordList == null) return;
        recordList.removeAllViews();

        if (petList.isEmpty() || currentPetIndex >= petList.size()) {
            TextView emptyView = new TextView(activity);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            emptyView.setLayoutParams(params);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setText("添加宠物后即可记录日常");
            emptyView.setTextSize(13f);
            emptyView.setTextColor(0xFF999999);
            emptyView.setPadding(0, dpToPx(30), 0, dpToPx(10));
            recordList.addView(emptyView);
            return;
        }

        Map<Integer, List<RecordItem>> recordsMap = getCurrentPetRecords();
        List<RecordItem> records = recordsMap.get(selectedDay);
        if (records == null) records = new ArrayList<>();

        if (records.isEmpty()) {
            TextView emptyView = new TextView(activity);
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
                View itemView = LayoutInflater.from(activity).inflate(R.layout.item_record, recordList, false);

                ImageView ivRecordIcon = itemView.findViewById(R.id.iv_record_icon);
                ivRecordIcon.setImageResource(record.getIconRes());

                TextView tvRecordTitle = itemView.findViewById(R.id.tv_record_title);
                tvRecordTitle.setText(record.getTitle());

                ImageView ivEdit = itemView.findViewById(R.id.iv_edit);
                ivEdit.setOnClickListener(v -> showAddDialog(record.getTitle()));

                recordList.addView(itemView);
            }
        }
    }

    private void initAddButton(View rootView) {
        View btnAddRecord = rootView.findViewById(R.id.btn_add_record);
        if (btnAddRecord != null) {
            btnAddRecord.setOnClickListener(v -> showAddDialog(""));
        }
    }

    private void showAddDialog(String existingTitle) {
        if (petList.isEmpty() || currentPetIndex >= petList.size()) {
            Toast.makeText(activity, "请先添加宠物", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(existingTitle.isEmpty() ? "添加提醒事项" : "编辑提醒事项");

        EditText input = new EditText(activity);
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
        if (petList.isEmpty() || currentPetIndex >= petList.size()) return;

        String petId = petList.get(currentPetIndex).id;
        Map<Integer, List<RecordItem>> recordsMap = getCurrentPetRecords();
        List<RecordItem> currentRecords = recordsMap.get(selectedDay);
        if (currentRecords == null) currentRecords = new ArrayList<>();

        boolean exists = false;
        for (RecordItem item : currentRecords) {
            if (item.getTitle().equals(title)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            int iconRes;
            if (title.contains("疫苗")) {
                iconRes = R.drawable.vaccine;
            } else if (title.contains("洗澡")) {
                iconRes = R.drawable.bathing;
            } else if (title.contains("罐头") || title.contains("零食") || title.contains("食物")) {
                iconRes = R.drawable.cat_food;
            } else if (title.contains("猫砂")) {
                iconRes = R.drawable.litter_box;
            } else if (title.contains("修剪") || title.contains("毛发") || title.contains("美容")) {
                iconRes = R.drawable.scissors;
            } else {
                iconRes = R.drawable.ic_paw_black;
            }
            currentRecords.add(new RecordItem(iconRes, title));
            recordsMap.put(selectedDay, currentRecords);
            allPetRecords.put(petId, recordsMap);
            saveRecordsToPrefs(petId, recordsMap);

            refreshRecordList(rootView);

            Toast.makeText(activity, "已添加到 " + currentMonth + "月" + selectedDay + "日", Toast.LENGTH_SHORT).show();
        }
    }

    private Map<Integer, List<RecordItem>> loadRecordsFromPrefs(String petId) {
        Map<Integer, List<RecordItem>> result = new HashMap<>();
        try {
            SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, 0);
            String jsonStr = prefs.getString(petId, "");
            if (jsonStr.isEmpty()) return result;

            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONArray daysArray = jsonObj.names();
            if (daysArray == null) return result;

            for (int i = 0; i < daysArray.length(); i++) {
                String dayKey = daysArray.getString(i);
                int day = Integer.parseInt(dayKey);
                JSONArray recordsArray = jsonObj.getJSONArray(dayKey);
                List<RecordItem> records = new ArrayList<>();
                for (int j = 0; j < recordsArray.length(); j++) {
                    JSONObject recordObj = recordsArray.getJSONObject(j);
                    int iconRes = recordObj.getInt("icon");
                    String title = recordObj.getString("title");
                    records.add(new RecordItem(iconRes, title));
                }
                result.put(day, records);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void saveRecordsToPrefs(String petId, Map<Integer, List<RecordItem>> recordsMap) {
        try {
            SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = prefs.edit();

            JSONObject jsonObj = new JSONObject();
            for (Map.Entry<Integer, List<RecordItem>> entry : recordsMap.entrySet()) {
                JSONArray recordsArray = new JSONArray();
                for (RecordItem record : entry.getValue()) {
                    JSONObject recordObj = new JSONObject();
                    recordObj.put("icon", record.getIconRes());
                    recordObj.put("title", record.getTitle());
                    recordsArray.put(recordObj);
                }
                jsonObj.put(String.valueOf(entry.getKey()), recordsArray);
            }

            editor.putString(petId, jsonObj.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshPetCard(View rootView) {
        this.rootView = rootView;
        loadPets();
        initPetCarousel(rootView);
        refreshRecordList(rootView);
    }

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
        return (int) (dp * activity.getResources().getDisplayMetrics().density);
    }
}
