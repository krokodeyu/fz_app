# fz_app

## Android Studio Sync 报错（`gradle-9.0.0-src.zip` 超时）排查

如果你看到类似错误：

- `Could not resolve gradle:gradle:9.0.0`
- `Could not get resource 'https://services.gradle.org/distributions/gradle-9.0.0-src.zip'`
- `Connect to github.com:443 ... timed out`

通常是 **网络 + IDE 下载源设置** 问题，而不是业务代码问题。

## 推荐做法（中国大陆网络）

### 1) 使用项目内镜像仓库配置（已提交）

本仓库 `settings.gradle.kts` 已加入阿里云镜像，并保留官方仓库兜底。

- 插件仓库：`maven.aliyun.com/repository/gradle-plugin` 等
- 依赖仓库：`maven.aliyun.com/repository/google|public|central` 等

### 2) 使用 Gradle Wrapper + 国内分发镜像（已提交）

`gradle/wrapper/gradle-wrapper.properties` 已设置为腾讯云 Gradle 镜像：

- `distributionUrl=https://mirrors.cloud.tencent.com/gradle/gradle-8.7-bin.zip`

> 如果你的网络对该镜像仍不稳定，可切回官方：
> `https://services.gradle.org/distributions/gradle-8.7-bin.zip`

### 3) Android Studio 里必须检查

- `Settings -> Build, Execution, Deployment -> Build Tools -> Gradle`
- `Use Gradle from`: **Gradle wrapper (recommended)**
- `Gradle JDK`: **17**

### 4) 关闭 IDE 自动下载 Gradle 源码（重点）

你的错误里有 `gradle-9.0.0-src.zip`，这是 IDE 为“源码附加/导航”触发的下载，不是必须。

请在 Android Studio 中关闭 **Download sources**（不同版本位置文案略有差异）。

### 5) 如果仍失败，设置代理与超时

已在项目 `gradle.properties` 提升超时时间。你还可在以下位置补充代理：

- Android Studio -> HTTP Proxy
- `~/.gradle/gradle.properties`

示例（按你自己的代理地址替换）：

```properties
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=7890
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=7890
```

## 快速复现后的修复步骤

1. 关闭 Android Studio。
2. 删除本机缓存目录中的失败下载（可选）：`~/.gradle/caches` 与 `~/.gradle/wrapper/dists`。
3. 重新打开项目，确认 Gradle 使用 Wrapper + JDK17。
4. 执行 `File -> Sync Project with Gradle Files`。

