// 该文件用于定义创作页专属模型，包括工具类型、菜单动作和折叠模式等基础枚举。
package com.example.kpappercutting.ui.features.creation

enum class EditTool {
    SCISSORS,
    PENCIL,
    ERASER
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

enum class FoldMode {
    NONE,
    FIVE_POINT,
    EIGHT_POINT
}
