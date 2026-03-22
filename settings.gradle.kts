pluginManagement {
    repositories {
        // Mainland mirrors first, then official repositories as fallback
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
        maven(url = "https://maven.aliyun.com/repository/google")
        maven(url = "https://maven.aliyun.com/repository/public")
        maven(url = "https://repo.huaweicloud.com/repository/maven/google/")
        maven(url = "https://repo.huaweicloud.com/repository/maven/")

        google()
        mavenCentral()
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.application" -> useModule("com.android.tools.build:gradle:${requested.version}")
                "org.jetbrains.kotlin.android" -> useModule("org.jetbrains.kotlin.android:org.jetbrains.kotlin.android.gradle.plugin:${requested.version}")
                "org.jetbrains.kotlin.plugin.compose" -> useModule("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:${requested.version}")
                "com.google.devtools.ksp" -> useModule("com.google.devtools.ksp:symbol-processing-gradle-plugin:${requested.version}")
                "com.google.dagger.hilt.android" -> useModule("com.google.dagger:hilt-android-gradle-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Mainland mirrors first, then official repositories as fallback
        maven(url = "https://maven.aliyun.com/repository/google")
        maven(url = "https://maven.aliyun.com/repository/public")
        maven(url = "https://maven.aliyun.com/repository/central")
        maven(url = "https://repo.huaweicloud.com/repository/maven/google/")
        maven(url = "https://repo.huaweicloud.com/repository/maven/")

        google()
        mavenCentral()
    }
}

rootProject.name = "FraudDetector"
include(":app")
