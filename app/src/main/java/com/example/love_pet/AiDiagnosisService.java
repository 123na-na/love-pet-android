package com.example.love_pet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiDiagnosisService {

    public static class DiagnosisResult {
        public String level;
        public String levelDesc;
        public int levelColor;
        public List<DiseaseInfo> diseases;
        public List<String> adviceList;
        public String department;
        public boolean needEmergency;
    }

    public static class DiseaseInfo {
        public String name;
        public float probability;
        public String description;
        public String department;

        public DiseaseInfo(String name, float probability, String description, String department) {
            this.name = name;
            this.probability = probability;
            this.description = description;
            this.department = department;
        }
    }

    public static DiagnosisResult analyze(String petType, String age, List<String> symptoms, String description) {
        DiagnosisResult result = new DiagnosisResult();
        result.diseases = new ArrayList<>();
        result.adviceList = new ArrayList<>();

        Map<String, Float> diseaseScores = new HashMap<>();
        Map<String, String> diseaseDesc = new HashMap<>();
        Map<String, String> diseaseDept = new HashMap<>();

        addDigestiveDiseases(symptoms, diseaseScores, diseaseDesc, diseaseDept);
        addSkinDiseases(symptoms, diseaseScores, diseaseDesc, diseaseDept);
        addRespiratoryDiseases(symptoms, diseaseScores, diseaseDesc, diseaseDept);
        addUrinaryDiseases(symptoms, diseaseScores, diseaseDesc, diseaseDept);
        addNeurologicalDiseases(symptoms, diseaseScores, diseaseDesc, diseaseDept);
        addGeneralDiseases(symptoms, diseaseScores, diseaseDesc, diseaseDept);

        float ageFactor = getAgeFactor(age);
        float petTypeFactor = getPetTypeFactor(petType);
        float descFactor = getDescFactor(description);

        List<Map.Entry<String, Float>> sortedDiseases = new ArrayList<>(diseaseScores.entrySet());
        sortedDiseases.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

        for (Map.Entry<String, Float> entry : sortedDiseases) {
            float prob = Math.min(0.95f, entry.getValue() * ageFactor * petTypeFactor * descFactor);
            String dept = diseaseDept.get(entry.getKey());
            String desc = diseaseDesc.get(entry.getKey());
            result.diseases.add(new DiseaseInfo(entry.getKey(), prob, desc, dept));
        }

        determineLevel(result, symptoms);
        generateAdvice(result, symptoms);
        determineDepartment(result);

        return result;
    }

    private static void addDigestiveDiseases(List<String> symptoms, Map<String, Float> scores, Map<String, String> desc, Map<String, String> dept) {
        float score = 0;
        if (symptoms.contains("呕吐")) score += 0.35f;
        if (symptoms.contains("拉稀")) score += 0.35f;
        if (symptoms.contains("食欲不振")) score += 0.15f;
        if (symptoms.contains("精神萎靡")) score += 0.1f;
        if (symptoms.contains("发烧")) score += 0.1f;
        if (score > 0) {
            scores.put("急性肠胃炎", score);
            desc.put("急性肠胃炎", "多由饮食不当、细菌感染引起，表现为呕吐、腹泻、食欲下降");
            dept.put("急性肠胃炎", "内科");
        }

        score = 0;
        if (symptoms.contains("呕吐")) score += 0.25f;
        if (symptoms.contains("食欲不振")) score += 0.2f;
        if (symptoms.contains("精神萎靡")) score += 0.15f;
        if (score > 0) {
            scores.put("消化不良", score);
            desc.put("消化不良", "肠胃功能紊乱，食物消化吸收不良");
            dept.put("消化不良", "内科");
        }

        score = 0;
        if (symptoms.contains("拉稀")) score += 0.3f;
        if (symptoms.contains("呕吐")) score += 0.15f;
        if (symptoms.contains("食欲不振")) score += 0.15f;
        if (symptoms.contains("精神萎靡")) score += 0.1f;
        if (score > 0) {
            scores.put("寄生虫感染", score);
            desc.put("寄生虫感染", "体内寄生虫引起的消化道症状，建议定期驱虫");
            dept.put("寄生虫感染", "内科");
        }

        score = 0;
        if (symptoms.contains("呕吐")) score += 0.3f;
        if (symptoms.contains("食欲不振")) score += 0.1f;
        if (score > 0) {
            scores.put("毛球症", score);
            desc.put("毛球症", "猫咪舔毛导致毛发在胃内堆积形成毛球");
            dept.put("毛球症", "内科");
        }
    }

    private static void addSkinDiseases(List<String> symptoms, Map<String, Float> scores, Map<String, String> desc, Map<String, String> dept) {
        float score = 0;
        if (symptoms.contains("皮肤病")) score += 0.3f;
        if (symptoms.contains("掉毛")) score += 0.25f;
        if (symptoms.contains("瘙痒")) score += 0.25f;
        if (score > 0) {
            scores.put("真菌感染（猫癣）", score);
            desc.put("真菌感染（猫癣）", "由皮肤癣菌引起，具有传染性，表现为圆形脱毛、皮屑");
            dept.put("真菌感染（猫癣）", "皮肤科");
        }

        score = 0;
        if (symptoms.contains("皮肤病")) score += 0.25f;
        if (symptoms.contains("瘙痒")) score += 0.3f;
        if (symptoms.contains("掉毛")) score += 0.15f;
        if (score > 0) {
            scores.put("过敏性皮炎", score);
            desc.put("过敏性皮炎", "由食物、环境、跳蚤等过敏原引起的皮肤炎症");
            dept.put("过敏性皮炎", "皮肤科");
        }

        score = 0;
        if (symptoms.contains("皮肤病")) score += 0.25f;
        if (symptoms.contains("瘙痒")) score += 0.25f;
        if (symptoms.contains("掉毛")) score += 0.2f;
        if (score > 0) {
            scores.put("寄生虫性皮肤病", score);
            desc.put("寄生虫性皮肤病", "跳蚤、疥螨等寄生虫引起的皮肤问题");
            dept.put("寄生虫性皮肤病", "皮肤科");
        }
    }

    private static void addRespiratoryDiseases(List<String> symptoms, Map<String, Float> scores, Map<String, String> desc, Map<String, String> dept) {
        float score = 0;
        if (symptoms.contains("咳嗽")) score += 0.3f;
        if (symptoms.contains("打喷嚏")) score += 0.25f;
        if (symptoms.contains("发烧")) score += 0.2f;
        if (symptoms.contains("精神萎靡")) score += 0.1f;
        if (symptoms.contains("食欲不振")) score += 0.1f;
        if (score > 0) {
            scores.put("上呼吸道感染", score);
            desc.put("上呼吸道感染", "病毒或细菌引起的呼吸道感染，俗称感冒");
            dept.put("上呼吸道感染", "内科");
        }

        score = 0;
        if (symptoms.contains("打喷嚏")) score += 0.35f;
        if (symptoms.contains("咳嗽")) score += 0.25f;
        if (symptoms.contains("发烧")) score += 0.15f;
        if (symptoms.contains("精神萎靡")) score += 0.1f;
        if (score > 0) {
            scores.put("猫鼻支（猫疱疹病毒）", score);
            desc.put("猫鼻支（猫疱疹病毒）", "由猫疱疹病毒引起的传染性呼吸道疾病");
            dept.put("猫鼻支（猫疱疹病毒）", "内科");
        }
    }

    private static void addUrinaryDiseases(List<String> symptoms, Map<String, Float> scores, Map<String, String> desc, Map<String, String> dept) {
        float score = 0;
        if (symptoms.contains("尿闭")) score += 0.5f;
        if (symptoms.contains("食欲不振")) score += 0.15f;
        if (symptoms.contains("精神萎靡")) score += 0.15f;
        if (score > 0) {
            scores.put("尿路结石/尿闭", score);
            desc.put("尿路结石/尿闭", "尿路结石堵塞尿道，导致排尿困难或无法排尿，属急症");
            dept.put("尿路结石/尿闭", "外科");
        }

        score = 0;
        if (symptoms.contains("尿闭")) score += 0.3f;
        if (score > 0) {
            scores.put("泌尿系统感染", score);
            desc.put("泌尿系统感染", "细菌感染引起的膀胱或尿道炎症");
            dept.put("泌尿系统感染", "内科");
        }
    }

    private static void addNeurologicalDiseases(List<String> symptoms, Map<String, Float> scores, Map<String, String> desc, Map<String, String> dept) {
        float score = 0;
        if (symptoms.contains("抽搐")) score += 0.5f;
        if (symptoms.contains("精神萎靡")) score += 0.15f;
        if (symptoms.contains("食欲不振")) score += 0.1f;
        if (score > 0) {
            scores.put("癫痫", score);
            desc.put("癫痫", "大脑神经元异常放电导致的短暂性脑功能障碍");
            dept.put("癫痫", "内科");
        }

        score = 0;
        if (symptoms.contains("抽搐")) score += 0.45f;
        if (symptoms.contains("呕吐")) score += 0.2f;
        if (symptoms.contains("精神萎靡")) score += 0.15f;
        if (score > 0) {
            scores.put("中毒", score);
            desc.put("中毒", "误食有毒物质引起，属急症，需立即就医");
            dept.put("中毒", "急诊24h");
        }
    }

    private static void addGeneralDiseases(List<String> symptoms, Map<String, Float> scores, Map<String, String> desc, Map<String, String> dept) {
        float score = 0;
        if (symptoms.contains("发烧")) score += 0.35f;
        if (symptoms.contains("精神萎靡")) score += 0.25f;
        if (symptoms.contains("食欲不振")) score += 0.2f;
        if (score > 0) {
            scores.put("细菌/病毒感染", score);
            desc.put("细菌/病毒感染", "全身性感染引起的发热、精神不振等症状");
            dept.put("细菌/病毒感染", "内科");
        }
    }

    private static float getAgeFactor(String age) {
        if ("幼年".equals(age)) return 1.1f;
        if ("老年".equals(age)) return 1.15f;
        return 1.0f;
    }

    private static float getPetTypeFactor(String petType) {
        return 1.0f;
    }

    private static float getDescFactor(String description) {
        if (description == null || description.isEmpty()) return 1.0f;
        if (description.length() > 50) return 1.1f;
        if (description.length() > 20) return 1.05f;
        return 1.0f;
    }

    private static void determineLevel(DiagnosisResult result, List<String> symptoms) {
        boolean isEmergency = symptoms.contains("抽搐") || symptoms.contains("尿闭");
        boolean hasSevereSymptoms = symptoms.contains("呕吐") && symptoms.contains("拉稀") && symptoms.contains("发烧");
        boolean hasMultipleSymptoms = symptoms.size() >= 4;

        if (isEmergency) {
            result.level = "紧急";
            result.levelColor = android.graphics.Color.parseColor("#FF3B30");
            result.levelDesc = "请立即就医，必要时拨打急诊电话";
            result.needEmergency = true;
        } else if (hasSevereSymptoms || hasMultipleSymptoms) {
            result.level = "中症";
            result.levelColor = android.graphics.Color.parseColor("#FF9500");
            result.levelDesc = "建议尽快到院检查或在线问诊";
            result.needEmergency = false;
        } else {
            result.level = "轻症";
            result.levelColor = android.graphics.Color.parseColor("#34C759");
            result.levelDesc = "建议居家护理观察，如症状加重请及时就医";
            result.needEmergency = false;
        }
    }

    private static void generateAdvice(DiagnosisResult result, List<String> symptoms) {
        List<String> generalAdvice = Arrays.asList(
            "密切观察宠物精神状态和食欲变化",
            "保证充足的清洁饮水",
            "保持环境安静、温暖、舒适"
        );
        result.adviceList.addAll(generalAdvice);

        if (symptoms.contains("呕吐") || symptoms.contains("拉稀")) {
            result.adviceList.add(0, "暂时禁食6-12小时，少量多次饮水，防止脱水");
            result.adviceList.add("可适当喂食益生菌调理肠胃");
            result.adviceList.add("观察呕吐物/粪便的颜色、性状和次数");
        }
        if (symptoms.contains("皮肤病") || symptoms.contains("掉毛") || symptoms.contains("瘙痒")) {
            result.adviceList.add(0, "佩戴伊丽莎白圈，防止抓挠加重感染");
            result.adviceList.add("保持皮肤清洁干燥，定期药浴");
            result.adviceList.add("环境消毒，避免交叉感染");
        }
        if (symptoms.contains("咳嗽") || symptoms.contains("打喷嚏")) {
            result.adviceList.add(0, "注意保暖，避免受凉和吹风");
            result.adviceList.add("保持室内空气流通，湿度适宜");
            result.adviceList.add("避免接触其他宠物，防止传染");
        }
        if (symptoms.contains("抽搐")) {
            result.adviceList.clear();
            result.adviceList.add("立即就医，切勿自行用药！抽搐可能危及生命");
            result.adviceList.add("记录抽搐持续时间、频率和症状表现");
            result.adviceList.add("抽搐时让宠物侧卧，保持呼吸道通畅");
            result.adviceList.add("避免强光和噪音刺激");
        }
        if (symptoms.contains("尿闭")) {
            result.adviceList.clear();
            result.adviceList.add("尿闭为急症，请立即就医！超过24小时无尿可能导致急性肾衰竭");
            result.adviceList.add("尝试诱导饮水，不要强行灌水");
            result.adviceList.add("记录最后一次排尿时间");
        }
        if (symptoms.contains("食欲不振") || symptoms.contains("精神萎靡")) {
            result.adviceList.add("可尝试喂食适口性好的食物，观察进食情况");
            result.adviceList.add("如超过24小时不进食，请及时就医");
        }
        if (symptoms.contains("发烧")) {
            result.adviceList.add("物理降温：用湿毛巾擦拭脚垫、腹股沟");
            result.adviceList.add("定期测量体温，记录变化");
        }
    }

    private static void determineDepartment(DiagnosisResult result) {
        if (result.diseases.isEmpty()) {
            result.department = "综合科";
            return;
        }
        result.department = result.diseases.get(0).department;
    }
}
