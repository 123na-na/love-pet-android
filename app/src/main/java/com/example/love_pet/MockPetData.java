package com.example.love_pet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 模拟数据工具类
 * 纯本地内存生成宠物笔记数据
 */
public class MockPetData {

    private static final Random random = new Random();

    // 宠物标题池
    public static final String[] TITLE_POOL = {
        "可爱的小猫咪",
        "萌萌哒狗狗",
        "宠物日常",
        "萌宠成长记",
        "治愈系宠物",
        "宠物表情包",
        "宠物写真",
        "宠物趣事",
        "铲屎官的日常",
        "宠物萌瞬间",
        "宠物睡姿大赏",
        "宠物搞怪时刻",
        "宠物聚会",
        "宠物美食",
        "宠物穿搭"
    };

    // 用户昵称池（多样化，不局限于宠物）
    public static final String[] NICKNAME_POOL = {
        // 2字昵称
        "小明", "小红", "阿强", "小美", "大壮",
        "小雪", "小雨", "阿华", "阿梅", "阿杰",
        "静静", "晓晓", "娜娜", "豆豆", "糖糖",
        "阿龙", "阿虎", "阿豹", "阿狼", "阿熊",
        // 3字昵称
        "王小二", "李小花", "张大伟", "刘小芳", "赵铁柱",
        "小可爱", "萌萌哒", "帅帅哒", "美美哒", "酷酷哒",
        "小幸运", "小确幸", "小确丧", "小太阳", "小月亮",
        // 4字昵称
        "春风十里", "阳光明媚", "岁月静好", "不忘初心", "方得始终",
        "梦想成真", "幸福美满", "快乐无忧", "自由自在", "心满意足",
        // 5字昵称
        "温柔的风", "暖暖的阳光", "甜甜的微笑", "轻轻的问候", "美好的时光",
        // 6字昵称
        "面朝大海春暖花开", "生活不止眼前的苟且", "诗和远方", "星辰大海", "人间值得",
        // 创意昵称
        "追风少年", "元气少女", "文艺青年", "佛系青年", "斜杠青年",
        "夜猫子", "早起鸟", "独行侠", "背包客", "美食家",
        "旅行者", "摄影师", "设计师", "程序员", "梦想家"
    };

    // 作者名称池
    public static final String[] AUTHOR_POOL = {
        "萌宠小柯", "奶茶是弟弟", "矮子爱光脑子", "卷耳的橘猫米米",
        "铲屎官阿杰", "宠物达人", "喵星人日记", "汪星人日常",
        "宠物摄影师", "爱宠之家", "毛孩子乐园", "宠物爱好者",
        "猫咪控", "狗狗控", "宠物博主", "萌宠日记"
    };

    // 正文文案池（长内容）
    public static final String[] CONTENT_POOL = {
        "今天带小宝贝出门散步，阳光正好，微风不燥，小家伙开心得不得了，一直蹦蹦跳跳的，真是太可爱了！",
        "家有萌宠真的太治愈了，每天回家看到它摇着尾巴迎接我，所有的疲惫都烟消云散了，感谢有你陪伴。",
        "记录一下毛孩子的日常，从一只小小的奶猫/奶狗，到现在活泼可爱的小家伙，看着它一天天长大，心里满满的成就感。",
        "周末带宠物去公园玩耍，遇到了好多小伙伴，大家一起玩得很开心，真是美好的一天！",
        "给毛孩子新买了玩具，它爱不释手，玩了一整天都不觉得累，看着它开心的样子，我也跟着开心起来。",
        "宠物真的是人类最好的朋友，它们不会说话，但总能在你需要的时候给你温暖和安慰，感谢这份缘分让我们相遇。",
        "今天给小家伙拍了好多美美的照片，每一张都舍不得删，分享出来给大家看看，希望能给你们带来一点治愈。",
        "带宠物去做了一次体检，医生说它很健康，心里的石头终于落地了，以后要更加细心地照顾它。",
        "天气变冷了，给毛孩子穿上了新买的小衣服，萌萌的样子真是太可爱了，忍不住想多拍几张照片。",
        "早上起来看到小家伙在窗边晒太阳，阳光洒在它身上，画面太美了，赶紧用手机记录下来这美好的瞬间。",
        "宠物的世界很简单，有吃的、有玩的、有人爱它就足够了，希望每个毛孩子都能被温柔以待。",
        "今天教小家伙学会了一个新技能，它学得很快，真是聪明又可爱，忍不住奖励了它好多零食。",
        "晚上给毛孩子准备了丰盛的晚餐，它吃得津津有味，看着它满足的样子，我也觉得很幸福。",
        "带宠物去了一趟宠物乐园，它玩得非常开心，跑了一整天，晚上回家累得直接睡着了，真是充实的一天。",
        "周末在家陪毛孩子玩耍，虽然什么都没做，但感觉特别放松，有宠物陪伴的时光总是过得特别快。"
    };

    // 话题标签池
    private static final String[] TAG_POOL = {
        "#萌宠", "#宠物日常", "#猫咪", "#狗狗", "#铲屎官",
        "#萌宠成长记", "#治愈系", "#宠物写真", "#宠物表情包",
        "#可爱猫咪", "#可爱狗狗", "#宠物美食", "#宠物穿搭",
        "#喵星人", "#汪星人", "#毛孩子", "#宠物日记", "#萌宠视频"
    };

    // 定位城市池
    public static final String[] CITY_POOL = {
        "北京", "上海", "广州", "深圳", "杭州",
        "成都", "重庆", "西安", "武汉", "南京",
        "苏州", "郑州", "长沙", "青岛", "沈阳",
        "天津", "合肥", "昆明", "福州", "厦门"
    };

    // 评论内容池
    public static final String[] COMMENT_CONTENT_POOL = {
        "太可爱了！",
        "萌化了",
        "哈哈哈",
        "好治愈",
        "想养一只",
        "颜值爆表",
        "可爱到犯规",
        "吸一口",
        "萌宠上线",
        "心都化了",
        "太萌了吧",
        "可爱可爱",
        "治愈系",
        "毛茸茸的",
        "好想抱抱",
        "太可爱了吧！！",
        "萌宠日常",
        "记录美好生活",
        "爱了爱了",
        "转发了"
    };

    /**
     * 生成随机宠物笔记列表
     * @param count 生成数量
     * @return 宠物笔记列表
     */
    public static List<PetNote> getRandomPetList(int count) {
        List<PetNote> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(createRandomPetNote(i));
        }
        return result;
    }

    /**
     * 创建随机宠物笔记
     */
    private static PetNote createRandomPetNote(int seed) {
        PetNote note = new PetNote();

        // 生成1-9张图片
        int imageCount = random.nextInt(9) + 1;
        List<String> imageUrls = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
            // 使用不同的seed保证图片不重复
            int randomSeed = seed * 100 + i;
            imageUrls.add("https://picsum.photos/400/500?random=" + randomSeed);
        }

        // 封面图使用第一张
        String coverUrl = imageUrls.get(0);

        // 标题：从标题池随机选取 + 随机数字
        String title = TITLE_POOL[random.nextInt(TITLE_POOL.length)] + (random.nextInt(1000));

        // 作者名称：从作者池随机选取
        String authorName = AUTHOR_POOL[random.nextInt(AUTHOR_POOL.length)];

        // 点赞数：500~10000
        int likeCount = random.nextInt(9501) + 500;

        // 收藏数：1000~8000
        int collectCount = random.nextInt(7001) + 1000;

        // 正文内容：从文案池随机选取
        String content = CONTENT_POOL[random.nextInt(CONTENT_POOL.length)];

        // 话题标签：随机2-5个
        int tagCount = random.nextInt(4) + 2;
        String[] tags = new String[tagCount];
        for (int i = 0; i < tagCount; i++) {
            tags[i] = TAG_POOL[random.nextInt(TAG_POOL.length)];
        }

        // 定位城市：随机选取
        String location = CITY_POOL[random.nextInt(CITY_POOL.length)];

        // 发布时间：随机生成
        String publishTime = getRandomPublishTime();

        // 评论：3~10条
        int commentCount = random.nextInt(8) + 3;
        List<Comment> comments = createRandomComments(commentCount, seed);

        // 设置字段
        note.setCoverUrl(coverUrl);
        note.setImageUrls(imageUrls);
        note.setTitle(title);
        note.setAuthorName(authorName);
        note.setLikeCount(likeCount);
        note.setCollectCount(collectCount);
        note.setCommentCount(commentCount);
        note.setContent(content);
        note.setTags(tags);
        note.setLocation(location);
        note.setPublishTime(publishTime);
        note.setComments(comments);

        return note;
    }

    /**
     * 创建随机评论列表
     */
    private static List<Comment> createRandomComments(int count, int seed) {
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Comment comment = new Comment();

            // 用户名：直接从昵称池选取，不添加数字
            String username = NICKNAME_POOL[random.nextInt(NICKNAME_POOL.length)];

            // 评论内容：从评论池随机选取
            String content = COMMENT_CONTENT_POOL[random.nextInt(COMMENT_CONTENT_POOL.length)];

            // 评论时间：随机时间
            String time = getRandomTime();

            comment.setUsername(username);
            comment.setContent(content);
            comment.setTime(time);

            comments.add(comment);
        }
        return comments;
    }

    /**
     * 获取随机时间（评论用）
     */
    private static String getRandomTime() {
        String[] timeUnits = {"分钟前", "小时前", "天前"};
        int unitIndex = random.nextInt(timeUnits.length);
        int value;
        switch (unitIndex) {
            case 0: // 分钟前
                value = random.nextInt(59) + 1;
                break;
            case 1: // 小时前
                value = random.nextInt(23) + 1;
                break;
            default: // 天前
                value = random.nextInt(30) + 1;
                break;
        }
        return value + timeUnits[unitIndex];
    }

    /**
     * 获取随机发布时间
     */
    private static String getRandomPublishTime() {
        int dayOffset = random.nextInt(7); // 0-6天前
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        
        String dayStr;
        if (dayOffset == 0) {
            dayStr = "今天";
        } else if (dayOffset == 1) {
            dayStr = "昨天";
        } else {
            dayStr = dayOffset + "天前";
        }
        
        return String.format("%s %02d:%02d", dayStr, hour, minute);
    }
}