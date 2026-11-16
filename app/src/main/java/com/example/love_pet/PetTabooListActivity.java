package com.example.love_pet;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PetTabooListActivity extends AppCompatActivity {

    private EditText etSearch;
    private LinearLayout listLayout;
    private String category;

    private List<TabooItem> allItems;
    private List<TabooItem> filteredItems;

    public static class TabooItem {
        String name;
        String harm;
        String solution;

        public TabooItem(String name, String harm, String solution) {
            this.name = name;
            this.harm = harm;
            this.solution = solution;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_taboo_list);

        category = getIntent().getStringExtra(PetTabooActivity.EXTRA_CATEGORY);
        String title = getIntent().getStringExtra(PetTabooActivity.EXTRA_TITLE);

        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(title);

        etSearch = findViewById(R.id.et_search);
        listLayout = findViewById(R.id.list_layout);

        allItems = generateData(category);
        filteredItems = new ArrayList<>(allItems);

        renderList();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private List<TabooItem> generateData(String category) {
        List<TabooItem> list = new ArrayList<>();

        if (PetTabooActivity.CATEGORY_FOOD.equals(category)) {
            list.add(new TabooItem("巧克力",
                    "可可碱和咖啡因会导致宠物呕吐、腹泻、心跳加速、痉挛，严重时可致命",
                    "立即催吐，喂食活性炭，尽快送往宠物医院"));
            list.add(new TabooItem("葡萄/葡萄干",
                    "会导致狗狗急性肾衰竭，出现呕吐、腹泻、无尿等症状",
                    "立即催吐，喂食活性炭，紧急送医进行输液治疗"));
            list.add(new TabooItem("洋葱/大蒜",
                    "含有硫化物，会破坏红细胞，导致溶血性贫血",
                    "停止喂食，观察宠物精神状态，出现异常立即就医"));
            list.add(new TabooItem("牛油果",
                    "含有 Persin 毒素，会导致宠物呕吐、腹泻、呼吸困难",
                    "停止喂食，多喝水，严重时就医"));
            list.add(new TabooItem("酒精",
                    "会导致宠物呕吐、腹泻、协调能力下降，严重时昏迷甚至死亡",
                    "立即催吐，紧急送医"));
            list.add(new TabooItem("咖啡因",
                    "会导致宠物心跳加速、呼吸急促、痉挛、体温升高",
                    "立即催吐，喂食活性炭，尽快送医"));
            list.add(new TabooItem("生鸡蛋",
                    "含有抗生物素蛋白，影响维生素B吸收；生蛋白可能含有沙门氏菌",
                    "煮熟后再喂食，如已喂食观察有无腹泻症状"));
            list.add(new TabooItem("生肉/生鱼",
                    "可能含有寄生虫和细菌，导致宠物感染疾病",
                    "煮熟后再喂食，定期做体内驱虫"));
            list.add(new TabooItem("牛奶",
                    "多数宠物乳糖不耐受，饮用后会出现腹泻、腹胀",
                    "停止喂食，腹泻严重时就医，可喂食宠物专用羊奶粉"));
            list.add(new TabooItem("坚果（夏威夷果）",
                    "会导致狗狗虚弱、抑郁、呕吐、共济失调、体温升高",
                    "催吐并喂食活性炭，送医观察"));
            list.add(new TabooItem("木糖醇",
                    "会导致狗狗胰岛素大量释放，引起低血糖、肝衰竭",
                    "立即催吐，紧急送医治疗"));
            list.add(new TabooItem("过咸的食物",
                    "长期食用会增加肾脏负担，导致高血压、心脏病",
                    "减少盐的摄入，喂食宠物专用食品"));
        } else if (PetTabooActivity.CATEGORY_HOME.equals(category)) {
            list.add(new TabooItem("樟脑丸（萘丸）",
                    "萘具有神经毒性，会导致宠物呕吐、抽搐、贫血",
                    "防止宠物接触，误食后立即催吐送医"));
            list.add(new TabooItem("洗衣液/柔顺剂",
                    "含有表面活性剂，会导致宠物呕吐、腹泻、口腔灼伤",
                    "用清水冲洗口腔，多喝水，严重时就医"));
            list.add(new TabooItem("漂白剂/消毒液",
                    "具有强腐蚀性，会灼伤消化道，导致呕吐、腹泻、休克",
                    "不要催吐，立即饮用大量牛奶或水，紧急送医"));
            list.add(new TabooItem("杀虫剂/灭鼠药",
                    "剧毒，会导致宠物抽搐、出血、呼吸困难、死亡",
                    "立即催吐，带上药物包装紧急送医"));
            list.add(new TabooItem("指甲油/洗甲水",
                    "含有丙酮等有机溶剂，会导致宠物呕吐、头晕、肝肾损伤",
                    "移到通风处，误食后立即就医"));
            list.add(new TabooItem("电池",
                    "电解液具有强腐蚀性，会灼伤消化道",
                    "不要催吐，立即送医，切勿挤压电池"));
            list.add(new TabooItem("打火机液/汽油",
                    "会导致宠物呕吐、腹泻、抽搐、呼吸衰竭",
                    "不要催吐，立即送医，保持呼吸道通畅"));
            list.add(new TabooItem("油漆/涂料",
                    "含有有机溶剂和重金属，会导致中毒",
                    "移至通风处，误食后立即就医"));
            list.add(new TabooItem("药品（人类）",
                    "人类药物对宠物剂量不同，可能导致严重中毒",
                    "药品放在宠物碰不到的地方，误食后带药盒立即送医"));
            list.add(new TabooItem("化妆品",
                    "含有化学物质，可能导致宠物过敏或中毒",
                    "放在宠物接触不到的地方，误食后观察症状，严重时就医"));
            list.add(new TabooItem("香烟/烟草",
                    "尼古丁会导致宠物呕吐、心跳加速、抽搐、昏迷",
                    "立即催吐，紧急送医"));
            list.add(new TabooItem("体温计（水银）",
                    "水银有毒，会导致宠物神经系统损伤",
                    "开窗通风，收集水银，宠物误食后立即送医"));
        } else if (PetTabooActivity.CATEGORY_PLANT.equals(category)) {
            list.add(new TabooItem("百合",
                    "对猫咪剧毒，微量即可导致急性肾衰竭",
                    "禁止养猫家庭摆放百合，误食后立即催吐紧急送医"));
            list.add(new TabooItem("绿萝",
                    "含有草酸钙，会刺激口腔和消化道，导致呕吐、吞咽困难",
                    "用清水漱口，多喝水，严重时就医"));
            list.add(new TabooItem("滴水观音",
                    "汁液有毒，接触皮肤会瘙痒，误食会导致口腔和胃部灼痛",
                    "用清水冲洗接触部位，误食后多喝水，严重时就医"));
            list.add(new TabooItem("万年青",
                    "含有草酸钙，会导致宠物口腔发炎、呕吐、腹泻",
                    "用清水漱口，误食后催吐，严重时就医"));
            list.add(new TabooItem("水仙花",
                    "鳞茎毒性最强，会导致宠物呕吐、腹泻、抽搐、心律失常",
                    "立即催吐，紧急送医"));
            list.add(new TabooItem("郁金香",
                    "球茎含有毒素，会导致宠物呕吐、腹泻、流口水",
                    "立即催吐，送医治疗"));
            list.add(new TabooItem("风信子",
                    "鳞茎有毒，会导致宠物胃部不适、呕吐、腹泻",
                    "催吐并喂食活性炭，送医观察"));
            list.add(new TabooItem("夹竹桃",
                    "全株有毒，会导致宠物心律失常、抽搐、死亡",
                    "紧急催吐，立即送医"));
            list.add(new TabooItem("杜鹃花",
                    "含有木藜芦毒素，会导致宠物呕吐、腹泻、血压下降、昏迷",
                    "立即催吐，紧急送医"));
            list.add(new TabooItem("仙客来",
                    "根茎毒性最强，会导致宠物呕吐、腹泻、抽搐",
                    "催吐并喂食活性炭，送医治疗"));
            list.add(new TabooItem("常春藤",
                    "含有皂苷，会导致宠物呕吐、腹泻、流口水、呼吸困难",
                    "催吐，多喝水，严重时就医"));
            list.add(new TabooItem("绿萝/龟背竹",
                    "含有草酸钙针晶，会刺激口腔，导致流口水、呕吐、吞咽困难",
                    "用清水冲洗口腔，多喝水，症状加重时就医"));
        }

        return list;
    }

    private void filterData(String keyword) {
        filteredItems.clear();
        if (keyword == null || keyword.isEmpty()) {
            filteredItems.addAll(allItems);
        } else {
            String lowerKeyword = keyword.toLowerCase();
            for (TabooItem item : allItems) {
                if (item.name.toLowerCase().contains(lowerKeyword) ||
                    item.harm.toLowerCase().contains(lowerKeyword) ||
                    item.solution.toLowerCase().contains(lowerKeyword)) {
                    filteredItems.add(item);
                }
            }
        }
        renderList();
    }

    private void renderList() {
        listLayout.removeAllViews();

        if (filteredItems.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            emptyView.setText("未找到相关物品");
            emptyView.setTextColor(0xFF999999);
            emptyView.setTextSize(14);
            emptyView.setGravity(android.view.Gravity.CENTER);
            emptyView.setPadding(0, 60, 0, 0);
            listLayout.addView(emptyView);
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < filteredItems.size(); i++) {
            TabooItem item = filteredItems.get(i);
            View itemView = inflater.inflate(R.layout.item_pet_taboo, listLayout, false);

            TextView tvName = itemView.findViewById(R.id.tv_name);
            TextView tvHarm = itemView.findViewById(R.id.tv_harm);
            TextView tvSolution = itemView.findViewById(R.id.tv_solution);

            tvName.setText(item.name);
            tvHarm.setText(item.harm);
            tvSolution.setText(item.solution);

            listLayout.addView(itemView);

            if (i < filteredItems.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                params.setMargins(0, 0, 0, 0);
                divider.setLayoutParams(params);
                divider.setBackgroundColor(0xFFF0F0F0);
                listLayout.addView(divider);
            }
        }

        View bottomSpace = new View(this);
        bottomSpace.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 20));
        listLayout.addView(bottomSpace);
    }
}
