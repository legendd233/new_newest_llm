# new_newest_llm —— Android 客户端

面向 LLM / AI 资讯的聚合阅读器 Android 客户端，配合后端服务 `new_newest_llm_server` 使用。后端聚合 arXiv、Hacker News、Reddit、GitHub Releases、HuggingFace 等来源，本客户端负责登录鉴权、信息流浏览、收藏管理、内容翻译等功能。

## 功能特性

- 账号体系：注册 / 登录 / 修改密码 / 找回密码（密保问答）
- 信息流：首页拉取最新条目，下拉刷新，按 ID 增量加载
- 收藏：一键收藏 / 取消收藏，单独的收藏列表页
- 翻译：对条目标题或摘要请求服务端翻译
- 离线缓存：通过 Room 持久化条目数据
- 后台刷新：使用 WorkManager 定时同步信息流
- 多语言：中文 / 英文（`values/` + `values-en/`）
- 主题：日间 / 夜间（`values-night/`）

## 技术栈

- 语言：Java + Kotlin 混合
- 最低 SDK：24（Android 7.0），目标 SDK：36
- 架构：Activity + Fragment + ViewModel + Repository
- 网络：Retrofit 2 + OkHttp + Gson
- 持久化：Room、EncryptedSharedPreferences（Token 存储）
- 异步：Kotlin Coroutines + LiveData
- 后台任务：WorkManager
- UI：ViewBinding、Material Components、RecyclerView、SwipeRefreshLayout、Navigation Component

## 目录结构

```
app/src/main/java/com/example/new_newest_llm/
├── data/
│   ├── local/         Room（AppDatabase / ItemDao / ItemEntity）
│   ├── model/         数据模型（FeedItem、AuthModels、Translate*…）
│   ├── remote/        Retrofit Client、Auth/Feed Api、AuthInterceptor
│   └── repository/    AuthRepository、FeedRepository
├── ui/
│   ├── auth/          登录 / 注册 / 找回密码 Fragment + ViewModel
│   └── feed/          信息流相关 UI
├── utils/             TokenManager、LocaleHelper、SecurityAnswerHelper
├── worker/            FeedWorker（WorkManager 后台同步）
├── MainActivity.java       主页（信息流）
├── FavoritesActivity.java  收藏页
├── ProfileActivity.java    个人中心
├── SettingsActivity.java   设置
├── ChangePasswordActivity.java
├── LoginActivity.java
├── NewsAdapter.java / NewsItem.java
└── ...
```

## 后端接口

服务端基址在 `data/remote/RetrofitClient.kt` 中：

```kotlin
private const val BASE_URL = "http://47.245.107.172:31415/"
```

主要端点（详见 `FeedApi.java` / `AuthApi.kt`）：

| 方法   | 路径                  | 说明              |
| ------ | --------------------- | ----------------- |
| GET    | `/feed?since&limit`   | 拉取信息流        |
| GET    | `/favorites`          | 获取收藏列表      |
| POST   | `/favorites/{id}`     | 添加收藏          |
| DELETE | `/favorites/{id}`     | 取消收藏          |
| POST   | `/translate`          | 翻译标题 / 摘要   |
| ...    | `/auth/*`             | 注册 / 登录 / 重置密码等 |

请求会经 `AuthInterceptor` 自动注入登录后保存的 Token。

## 构建与运行

### 环境要求

- Android Studio（建议 Hedgehog 或更新版本）
- JDK 11
- Android SDK 36（compileSdk = 36，含 minor API level 1）
- 一台 Android 7.0+ 的真机或模拟器

### 步骤

1. 用 Android Studio 打开 `new_newest_llm/` 根目录。
2. 等待 Gradle Sync 完成。
3. 确认/修改后端地址：编辑 `app/src/main/java/com/example/new_newest_llm/data/remote/RetrofitClient.kt` 中的 `BASE_URL`。
4. 选择 `app` 模块运行：

   ```bash
   ./gradlew :app:installDebug
   ```

   或在 Android Studio 中点击 Run。

### 本地后端

如需连接本地启动的 `new_newest_llm_server`，把 `BASE_URL` 改为 `http://10.0.2.2:<port>/`（模拟器）或 `http://<电脑局域网 IP>:<port>/`（真机）。明文 HTTP 已在 `res/xml/network_security_config.xml` 中允许。

## 权限

`AndroidManifest.xml` 中声明：

- `INTERNET` —— 访问后端
- `ACCESS_NETWORK_STATE` —— 判断网络可用性

## 启动入口

应用启动 Activity 为 `ui.auth.AuthActivity`（容器，承载登录 / 注册 / 找回密码 Fragment）。登录成功后跳转到 `MainActivity`。

## 与服务端的对应关系

| 客户端模块                | 服务端模块                       |
| ------------------------- | -------------------------------- |
| `FeedApi.getFeed`         | `app/main.py` `/feed`            |
| `FeedApi.translate`       | `app/services/translate.py`      |
| `FeedApi.{add,remove}Favorite` | `/favorites/*`              |
| `AuthApi`                 | `/auth/*`                        |
| `worker/FeedWorker`       | 周期性拉取 `/feed` 并写入 Room   |

## 常见问题

- **登录后立刻被踢回登录页**：检查 `BASE_URL` 与服务端是否互通；`TokenManager` 使用 EncryptedSharedPreferences，卸载重装会清空。
- **拉取信息流为空**：服务端的 `enrich`/`translate` 是异步任务，新条目可能还未补全；下拉刷新或稍候再试。
- **真机连不上后端**：明文 HTTP 需要 `network_security_config.xml` 中允许的域名/IP；自定义 `BASE_URL` 时记得同步。
