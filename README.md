# 爱宠 App（Love Pet）

Android 宠物服务 App，面向养宠用户提供**宠物商城、AI 客服、AI 问诊、危险食物识别、宠物地图**等一站式服务。

## 功能

- **AI 智能客服**：基于通义千问 `qwen-turbo` 的对话客服（助手"小咪"），回答宠物领养、健康护理、美容洗护、用品购买等问题（`ChatDetailActivity` + `QwenApiHelper`）
- **AI 问诊**：症状自查，基于规则打分引擎给出就医建议（`AiDiagnosisService`）
- **危险食物识别**：内置危险食物/物品清单 + 关键词匹配，快速提示毒性风险（`AiCheckActivity`）
- **宠物商城**：商品列表、详情、数据导入（`assets/products.json` + `ProductDataImporter`）
- **语音输入**：百度语音识别，支持语音提问（`BaiduSpeechRecognizer`）
- **宠物地图**：高德地图 H5 页面（`assets/amap.html` WebView）展示门店/医院位置
- **本地数据**：SQLite 数据库（SQLiteOpenHelper）持久化用户数据与浏览记录

## 技术栈

| 类别 | 技术 |
| --- | --- |
| 语言/构建 | Java + Kotlin、Gradle（compileSdk 32 / minSdk 21 / targetSdk 32） |
| 数据库 | SQLite（SQLiteOpenHelper 原生封装） |
| 网络 | OkHttp（30s 超时） |
| AI | 通义千问 `qwen-turbo`（DashScope OpenAI 兼容接口） |
| 其他 | Glide（图片加载）、uCrop（图片裁剪）、百度语音识别、高德地图 H5 |

## 目录结构

```
love_pet/
└── app/src/main/
    ├── java/com/example/love_pet/   # Activity 与工具类（客服/问诊/商城/识别等）
    ├── assets/                      # products.json、region_data.json、amap.html
    ├── res/                         # 布局、图片、动画等资源
    └── AndroidManifest.xml
```

## 启动方式

1. 使用 Android Studio 打开 `love_pet` 根目录（Gradle 同步）；
2. 配置 AI 服务：在 `app/src/main/java/com/example/love_pet/QwenApiHelper.java` 中填入阿里云百炼的 API Key（见下文"模型 Key 申请"）；
3. 连接模拟器或真机，编译运行。

> ⚠️ 安全提醒：`QwenApiHelper.java` 中当前硬编码了一个 API Key 并已进入公开仓库，建议到阿里云百炼控制台**重置/作废该 Key** 后替换，避免被他人盗用产生费用。

## 模型 Key 申请

阿里云百炼（通义千问）[bailian.console.aliyun.com](https://bailian.console.aliyun.com)：开通百炼服务 → 创建 API-KEY → 在 `QwenApiHelper.java` 中替换 `API_KEY`，模型为 `qwen-turbo`（DashScope 兼容模式 `https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions`）。

## 说明

- 危险食物识别当前采用**内置清单 + 关键词匹配**（快速、离线、零成本）；后续计划接入 RAG 知识库做语义召回，相关最小链路 Demo 见 [Project4/rag_demo](../Project4/rag_demo/README.md)。
- AI 问诊为规则引擎实现（非大模型），用于症状分级引导，最终以线下就医为准。

## 密钥配置（不写入源码）

源码中不再包含任何 API Key，全部在构建时注入 `BuildConfig`：

1. 复制 `local.properties.example` 为 `local.properties`（该文件已被 `.gitignore` 忽略）；
2. 填入自己的值：

```properties
QWEN_API_KEY=       # DashScope（通义千问）API Key
BAIDU_APP_ID=       # 百度语音识别 APP_ID
BAIDU_API_KEY=      # 百度语音识别 API_KEY
BAIDU_SECRET_KEY=   # 百度语音识别 SECRET_KEY
AGORA_APP_ID=       # 声网视频通话 App ID
```

3. 重新同步 Gradle 后，代码通过 `BuildConfig.QWEN_API_KEY` 等字段读取。未配置时为空字符串，涉及的功能不可用，其余功能不受影响。
