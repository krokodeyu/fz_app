package com.example.frauddetector.core.normalization

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class AppMetadataResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager

    open fun resolveLabel(packageName: String?): String? {
        if (packageName.isNullOrBlank()) return null
        return runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        }.getOrNull()
    }

    open fun resolveAppType(packageName: String?, label: String?): String {
        val seed = listOfNotNull(packageName, label).joinToString(separator = " ").lowercase()
        return when {
            seed.contains("alipay") || seed.contains("bank") || seed.contains("pay") || seed.contains("云闪付") || seed.contains("支付宝") -> "金融类app"
            seed.contains("shop") || seed.contains("mall") || seed.contains("taobao") || seed.contains("闲鱼") || seed.contains("jd") -> "电商类app"
            seed.contains("camera") || seed.contains("相机") -> "工具类app"
            seed.contains("chrome") || seed.contains("browser") || seed.contains("浏览器") -> "浏览器类app"
            seed.contains("chat") || seed.contains("wechat") || seed.contains("qq") || seed.contains("telegram") -> "社交类app"
            seed.contains("android") || seed.contains("system") -> "系统类app"
            else -> "未知类型app"
        }
    }

    open fun isLikelyOnlineApp(packageName: String?): Boolean {
        if (packageName.isNullOrBlank()) return false
        return runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            info.flags and ApplicationInfo.FLAG_SYSTEM == 0
        }.getOrDefault(true)
    }
}
