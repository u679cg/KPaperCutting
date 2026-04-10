// 该文件用于定义创作页专属模型，包括工具类型、菜单动作和折叠模式等基础枚举。
package com.example.kpappercutting.ui.features.creation

enum class EditTool {
    SCISSORS,
    PENCIL,
    ERASER
}

enum class EraserSize(
    val strokeWidth: Float,
    val previewWidth: Float,
    val label: String
) {
    X_SMALL(strokeWidth = 12f, previewWidth = 1.2f, label = "极细"),
    SMALL(strokeWidth = 18f, previewWidth = 1.5f, label = "细"),
    MEDIUM(strokeWidth = 28f, previewWidth = 2f, label = "中"),
    LARGE(strokeWidth = 40f, previewWidth = 2.6f, label = "粗"),
    X_LARGE(strokeWidth = 54f, previewWidth = 3.2f, label = "极粗")
}

object CreationPaperDefaults {
    const val DEFAULT_PAPER_COLOR: Int = 0xFFB02621.toInt()
}

enum class CreationMenuAction {
    INITIALIZE_CANVAS,
    SAVE_DRAFT,
    EXPORT_TO_GALLERY,
    PUBLISH_TO_COMMUNITY
}
