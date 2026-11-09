package com.example.love_pet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearchInput;
    private LinearLayout layoutSearchTabs;
    private LinearLayout layoutSearchEmpty;
    private LinearLayout layoutSearchHistory;
    private ScrollView svSearchResults;
    private LinearLayout layoutSearchResults;
    private LinearLayout layoutHistoryTags;
    private LinearLayout layoutHotTags;

    private String[] tabNames = {"全部", "帖子", "医院", "服务", "商品"};
    private int currentTab = 0;

    private List<String> searchHistory = new ArrayList<>();
    private List<String> hotSearches = Arrays.asList("猫咪洗澡", "宠物医院", "疫苗接种", "狗粮推荐", "猫砂", "狗狗美容", "宠物寄养", "驱虫");

    private static final String PREFS_SEARCH = "search_history";
    private static final String KEY_HISTORY = "history";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        initTabs();
        loadSearchHistory();
        setupListeners();
        showHistoryPage();
    }

    private void initViews() {
        etSearchInput = findViewById(R.id.et_search_input);
        layoutSearchTabs = findViewById(R.id.layout_search_tabs);
        layoutSearchEmpty = findViewById(R.id.layout_search_empty);
        layoutSearchHistory = findViewById(R.id.layout_search_history);
        svSearchResults = findViewById(R.id.sv_search_results);
        layoutSearchResults = findViewById(R.id.layout_search_results);
        layoutHistoryTags = findViewById(R.id.layout_history_tags);
        layoutHotTags = findViewById(R.id.layout_hot_tags);
    }

    private void initTabs() {
        layoutSearchTabs.removeAllViews();
        for (int i = 0; i < tabNames.length; i++) {
            TextView tabView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.rightMargin = dpToPx(24);
            tabView.setLayoutParams(params);
            tabView.setText(tabNames[i]);
            tabView.setTextSize(15f);
            tabView.setTextColor(i == currentTab ? 0xFF333333 : 0xFF999999);
            tabView.getPaint().setFakeBoldText(i == currentTab);

            final int index = i;
            tabView.setOnClickListener(v -> {
                currentTab = index;
                updateTabs();
                String keyword = etSearchInput.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    performSearch(keyword);
                }
            });

            layoutSearchTabs.addView(tabView);
        }
    }

    private void updateTabs() {
        for (int i = 0; i < layoutSearchTabs.getChildCount(); i++) {
            View child = layoutSearchTabs.getChildAt(i);
            if (child instanceof TextView) {
                TextView tabView = (TextView) child;
                tabView.setTextColor(i == currentTab ? 0xFF333333 : 0xFF999999);
                tabView.getPaint().setFakeBoldText(i == currentTab);
            }
        }
    }

    private void setupListeners() {
        findViewById(R.id.iv_back_search).setOnClickListener(v -> finish());

        findViewById(R.id.tv_search_action).setOnClickListener(v -> {
            String keyword = etSearchInput.getText().toString().trim();
            if (!keyword.isEmpty()) {
                saveSearchHistory(keyword);
                performSearch(keyword);
            }
        });

        etSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String keyword = s.toString().trim();
                if (keyword.isEmpty()) {
                    showHistoryPage();
                }
            }
        });

        etSearchInput.setOnEditorActionListener((v, actionId, event) -> {
            String keyword = etSearchInput.getText().toString().trim();
            if (!keyword.isEmpty()) {
                saveSearchHistory(keyword);
                performSearch(keyword);
            }
            return true;
        });

        findViewById(R.id.tv_clear_history).setOnClickListener(v -> {
            clearSearchHistory();
            refreshHistoryTags();
        });
    }

    private void showHistoryPage() {
        layoutSearchEmpty.setVisibility(View.GONE);
        layoutSearchHistory.setVisibility(View.VISIBLE);
        svSearchResults.setVisibility(View.GONE);
        refreshHistoryTags();
        refreshHotTags();
    }

    private void showResultsPage() {
        layoutSearchEmpty.setVisibility(View.GONE);
        layoutSearchHistory.setVisibility(View.GONE);
        svSearchResults.setVisibility(View.VISIBLE);
    }

    private void showEmptyResults() {
        layoutSearchEmpty.setVisibility(View.VISIBLE);
        layoutSearchHistory.setVisibility(View.GONE);
        svSearchResults.setVisibility(View.GONE);
        TextView tvEmptyHint = findViewById(R.id.tv_empty_hint);
        if (tvEmptyHint != null) {
            tvEmptyHint.setText("暂无此内容");
        }
    }

    private void refreshHistoryTags() {
        layoutHistoryTags.removeAllViews();
        if (searchHistory.isEmpty()) {
            findViewById(R.id.tv_clear_history).setVisibility(View.GONE);
            return;
        }
        findViewById(R.id.tv_clear_history).setVisibility(View.VISIBLE);

        for (String keyword : searchHistory) {
            addTagView(layoutHistoryTags, keyword, v -> {
                etSearchInput.setText(keyword);
                etSearchInput.setSelection(keyword.length());
                saveSearchHistory(keyword);
                performSearch(keyword);
            });
        }
    }

    private void refreshHotTags() {
        layoutHotTags.removeAllViews();
        for (String keyword : hotSearches) {
            addTagView(layoutHotTags, keyword, v -> {
                etSearchInput.setText(keyword);
                etSearchInput.setSelection(keyword.length());
                saveSearchHistory(keyword);
                performSearch(keyword);
            });
        }
    }

    private void addTagView(LinearLayout container, String text, View.OnClickListener listener) {
        TextView tagView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = dpToPx(10);
        params.bottomMargin = dpToPx(8);
        tagView.setLayoutParams(params);
        tagView.setBackgroundResource(R.drawable.bg_tag_gray);
        tagView.setPadding(dpToPx(12), dpToPx(5), dpToPx(12), dpToPx(5));
        tagView.setText(text);
        tagView.setTextColor(0xFF666666);
        tagView.setTextSize(13f);
        tagView.setOnClickListener(listener);
        container.addView(tagView);
    }

    private void performSearch(String keyword) {
        showResultsPage();
        layoutSearchResults.removeAllViews();

        List<SearchResultItem> results = new ArrayList<>();

        if (currentTab == 0 || currentTab == 1) {
            results.addAll(searchPosts(keyword));
        }
        if (currentTab == 0 || currentTab == 2) {
            results.addAll(searchHospitals(keyword));
        }
        if (currentTab == 0 || currentTab == 3) {
            results.addAll(searchServices(keyword));
        }
        if (currentTab == 0 || currentTab == 4) {
            results.addAll(searchProducts(keyword));
        }

        if (results.isEmpty()) {
            showEmptyResults();
            return;
        }

        for (SearchResultItem result : results) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_search_result, layoutSearchResults, false);

            ImageView ivIcon = itemView.findViewById(R.id.iv_result_icon);
            TextView tvTitle = itemView.findViewById(R.id.tv_result_title);
            TextView tvDesc = itemView.findViewById(R.id.tv_result_desc);
            TextView tvType = itemView.findViewById(R.id.tv_result_type);

            if (result.iconRes != 0) {
                ivIcon.setImageResource(result.iconRes);
            }
            tvTitle.setText(result.title);
            tvDesc.setText(result.description);
            tvType.setText(result.type);

            itemView.setOnClickListener(v -> {
                result.onClick.run();
            });

            layoutSearchResults.addView(itemView);

            View divider = new View(this);
            LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
            dividerParams.leftMargin = dpToPx(68);
            divider.setLayoutParams(dividerParams);
            divider.setBackgroundColor(0xFFF5F5F5);
            layoutSearchResults.addView(divider);
        }
    }

    private List<SearchResultItem> searchPosts(String keyword) {
        List<SearchResultItem> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();

        PetDataManager dataManager = PetDataManager.getInstance(this);
        List<PetDataManager.PetNoteEntity> allNotes = dataManager.getAllNotes();

        for (PetDataManager.PetNoteEntity note : allNotes) {
            String title = note.getTitle() != null ? note.getTitle() : "";
            String author = note.getAuthorName() != null ? note.getAuthorName() : "";
            String content = note.getContent() != null ? note.getContent() : "";

            if (title.toLowerCase().contains(lowerKeyword) ||
                    author.toLowerCase().contains(lowerKeyword) ||
                    content.toLowerCase().contains(lowerKeyword)) {

                int imageRes = getImageResByName(note.getImageRes());
                final int noteId = note.getId();

                results.add(new SearchResultItem(
                        imageRes != 0 ? imageRes : R.drawable.img_cat_hua,
                        title.isEmpty() ? "(无标题)" : title,
                        author + " · " + note.getLikeCount() + " 赞",
                        "帖子",
                        () -> {
                            Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
                            intent.putExtra("note_id", noteId);
                            startActivity(intent);
                        }
                ));
            }
        }
        return results;
    }

    private int getImageResByName(String resName) {
        if (resName == null || resName.isEmpty()) return 0;
        try {
            return getResources().getIdentifier(resName, "drawable", getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }

    private List<SearchResultItem> searchHospitals(String keyword) {
        List<SearchResultItem> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();

        String[][] hospitals = {
                {"萌宠宠物医院", "朝阳区建国路88号", "三级甲等"},
                {"爱宠动物医院", "海淀区中关村大街1号", "三级乙等"},
                {"宠爱宠物诊所", "丰台区南三环西路5号", "一级医院"},
                {"博爱动物医院", "朝阳区东四环中路20号", "二级甲等"}
        };

        for (String[] hospital : hospitals) {
            if (hospital[0].toLowerCase().contains(lowerKeyword) ||
                    hospital[1].toLowerCase().contains(lowerKeyword) ||
                    keyword.equals("医院") || keyword.equals("宠物医院")) {
                results.add(new SearchResultItem(
                        R.drawable.animal_hospital,
                        hospital[0],
                        hospital[1] + " · " + hospital[2],
                        "医院",
                        () -> {
                            Intent intent = new Intent(SearchActivity.this, HospitalMapActivity.class);
                            startActivity(intent);
                        }
                ));
            }
        }
        return results;
    }

    private List<SearchResultItem> searchServices(String keyword) {
        List<SearchResultItem> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();

        String[][] services = {
                {"宠物洗澡美容", "专业洗护，猫狗通用", "美容"},
                {"疫苗接种服务", "进口疫苗，安全可靠", "医疗"},
                {"宠物寄养服务", "温馨环境，专人照顾", "寄养"},
                {"宠物体检套餐", "全面检查，健康保障", "体检"},
                {"宠物训练课程", "专业训犬师一对一", "训练"}
        };

        for (String[] service : services) {
            if (service[0].toLowerCase().contains(lowerKeyword) ||
                    service[1].toLowerCase().contains(lowerKeyword) ||
                    service[2].toLowerCase().contains(lowerKeyword) ||
                    keyword.equals("服务")) {
                results.add(new SearchResultItem(
                        R.drawable.scissors,
                        service[0],
                        service[1],
                        "服务",
                        () -> {
                            Toast.makeText(SearchActivity.this, "正在跳转到" + service[0] + "页面...", Toast.LENGTH_SHORT).show();
                        }
                ));
            }
        }
        return results;
    }

    private List<SearchResultItem> searchProducts(String keyword) {
        List<SearchResultItem> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();

        String[][] products = {
                {"皇家猫粮", "营养均衡，美毛护肤", "猫粮"},
                {"麦富迪狗粮", "天然配方，健康成长", "狗粮"},
                {"豆腐猫砂", "无尘除臭，可冲厕所", "猫砂"},
                {"宠物零食大礼包", "多种口味，训练奖励", "零食"},
                {"宠物牵引绳", "舒适耐用，安全出行", "用品"}
        };

        for (String[] product : products) {
            if (product[0].toLowerCase().contains(lowerKeyword) ||
                    product[1].toLowerCase().contains(lowerKeyword) ||
                    product[2].toLowerCase().contains(lowerKeyword) ||
                    keyword.equals("商品") || keyword.equals("商城")) {
                results.add(new SearchResultItem(
                        R.drawable.cat_food,
                        product[0],
                        product[1],
                        "商品",
                        () -> {
                            Toast.makeText(SearchActivity.this, "正在跳转到商品详情...", Toast.LENGTH_SHORT).show();
                        }
                ));
            }
        }
        return results;
    }

    private void loadSearchHistory() {
        SharedPreferences prefs = getSharedPreferences(PREFS_SEARCH, 0);
        String historyStr = prefs.getString(KEY_HISTORY, "");
        searchHistory.clear();
        if (!historyStr.isEmpty()) {
            String[] items = historyStr.split(",");
            for (String item : items) {
                if (!item.isEmpty()) {
                    searchHistory.add(item);
                }
            }
        }
    }

    private void saveSearchHistory(String keyword) {
        searchHistory.remove(keyword);
        searchHistory.add(0, keyword);
        if (searchHistory.size() > 10) {
            searchHistory = searchHistory.subList(0, 10);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < searchHistory.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(searchHistory.get(i));
        }

        SharedPreferences prefs = getSharedPreferences(PREFS_SEARCH, 0);
        prefs.edit().putString(KEY_HISTORY, sb.toString()).apply();
    }

    private void clearSearchHistory() {
        searchHistory.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS_SEARCH, 0);
        prefs.edit().remove(KEY_HISTORY).apply();
        Toast.makeText(this, "已清空搜索历史", Toast.LENGTH_SHORT).show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private static class SearchResultItem {
        int iconRes;
        String title;
        String description;
        String type;
        Runnable onClick;

        SearchResultItem(int iconRes, String title, String description, String type, Runnable onClick) {
            this.iconRes = iconRes;
            this.title = title;
            this.description = description;
            this.type = type;
            this.onClick = onClick;
        }
    }
}
