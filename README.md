# 爱宠 App（Love Pet）

Android 宠物服务 App，面向养宠用户提供**宠物商城、AI 客服（支持图片问答）、危险食物识别、报告解读、宠物地图**等一站式服务。

## 功能

- **AI 智能客服**：通义千问 `qwen-plus` 对话客服（助手"小咪"），按店铺/商品拼系统提示词（`CustomerServiceActivity` + `QwenApiHelper`）
- **AI 客服看图**：相册选图后压缩成 base64，按 `image_url` 内容块送进视觉模型 `qwen3-vl-plus`，回复沿用同一套店铺提示词（`QwenApiHelper.sendImageMessage`）
- **报告解读**：上传化验单/X 光片，视觉模型读图后输出"可辨认指标→偏离项→就医建议"，结尾固定免责语（`ReportAnalysisActivity`）
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
| 网络 | OkHttp（connect 30s / read 60s / write 30s） |
| AI（文本） | 通义千问 `qwen-plus`（DashScope OpenAI 兼容接口） |
| AI（视觉） | 通义千问 `qwen3-vl-plus`，`messages.content` 数组 + `image_url`（base64 data URL） |
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
2. 按下面"密钥与模型配置"一节填好 `local.properties`（API Key 与模型名都从这里注入 `BuildConfig`，源码里没有硬编码密钥）；
3. 连接模拟器或真机，编译运行。

> 若工程放在含中文的路径下，AGP 会报 `Your project path contains non-ASCII characters`，可在 `gradle.properties` 加 `android.overridePathCheck=true`，或把工程复制到纯英文路径编译。

## 密钥与模型配置（不写入源码）

源码中不含任何 API Key，全部在构建时注入 `BuildConfig`：

1. 复制 `local.properties.example` 为 `local.properties`（该文件已被 `.gitignore` 忽略）；
2. 填入自己的值：

```properties
QWEN_API_KEY=        # DashScope（通义千问）API Key
QWEN_TEXT_MODEL=qwen-plus          # 文本模型，可留空走默认
QWEN_VL_MODEL=qwen3-vl-plus        # 视觉模型，可留空走默认
BAIDU_APP_ID=        # 百度语音识别 APP_ID
BAIDU_API_KEY=       # 百度语音识别 API_KEY
BAIDU_SECRET_KEY=    # 百度语音识别 SECRET_KEY
AGORA_APP_ID=        # 声网视频通话 App ID
```

3. 重新同步 Gradle 后，代码通过 `BuildConfig.QWEN_API_KEY` 等字段读取。未配置时为空字符串，相关 AI 功能会提示"未配置"，其余功能不受影响。

> 模型名注意：`qwen-vl-plus` 已在百炼平台下架（调用会 404 `Model not exist.`），视觉模型需用 `qwen3-vl-plus`；不带日期的 `qwen-turbo` 也已不在模型列表，文本侧用 `qwen-plus`。

## 多模态实测记录（2026-11-11）

在 Pixel 6 API 36 模拟器上装包实测，三条链路均通：

| 链路 | 实测表现 |
| --- | --- |
| 客服页发图 | 上传一张血检报告截图，模型回复"图片为一只橘猫的血液检测报告…"，并附一句店铺语境建议 |
| 客服页文字 | 英文提问 → 英文作答，说明本店主营智能用品、不提供兽医预约，系统提示词生效 |
| 报告解读 | 读出一份报告单的 `WBC 18.5`、`RBC 6.1`、`PLT 520`、`ALT 85` 及参考区间，指出 ALT 超出上限、提示肝酶升高，结尾附固定免责语 |

服务端侧另有验证：`GET /v1/models` 返回 182 个模型；`qwen3-vl-plus` 带 `image_url` 请求返回 200，响应含 `prompt_tokens_details.image_tokens`，证明图片确实进入模型。

已知不足（不粉饰）：

- 报告里 PLT 520 也略高于参考上限，模型只点名了 ALT —— 目前只靠 Prompt 要求"指出明显偏离项"，没有对数值做程序化比对兜底；
- 无流式输出、无重试（429/网络抖动直接走兜底话术）；
- 无测试集，不具备任何准确率数据。

## 说明

- 危险食物识别当前采用**内置清单 + 关键词匹配**（快速、离线、零成本）；语义召回的 RAG 链路单独放在另一个仓库 `pet-feeding-rag`，**本 App 未接入 RAG，也未做过准确率评测**。
- AI 问诊为规则引擎实现（非大模型），用于症状分级引导。
- 报告解读与客服看图均为"图片信息整理"，不能作为诊断依据，最终以执业兽医判断为准。
