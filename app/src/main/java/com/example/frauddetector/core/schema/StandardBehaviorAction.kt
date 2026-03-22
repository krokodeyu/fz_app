package com.example.frauddetector.core.schema

enum class StandardBehaviorAction(
    val schemaAction: String,
    val observable: Boolean
) {
    OPEN_APP("打开应用", true),
    SWITCH_APP("切换应用", true),
    CLOSE_APP("关闭应用", true),
    INSTALL_APP("安装应用", true),
    UNINSTALL_APP("卸载应用", true),
    UPDATE_APP("更新应用", true),
    OPEN_CAMERA("打开相机", true),
    CAMERA_ACTIVE("调用相机", true),
    OPEN_BROWSER("打开浏览器", true),
    OTHER_OBSERVABLE("可观测行为", true),
    TEXT_CHAT("文本聊天", false),
    PURCHASE_ITEM("购买商品", false);

    companion object {
        fun fromActionText(action: String): StandardBehaviorAction? = entries.firstOrNull {
            it.schemaAction == action
        }
    }
}
