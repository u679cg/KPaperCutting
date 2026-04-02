package com.example.kpappercutting.ui.features.culture

import com.example.kpappercutting.R

data class CultureEraUiModel(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val imageTitle: String,
    val description: String
)

data class CultureTechniqueUiModel(
    val emoji: String,
    val title: String
)

data class CulturePatternUiModel(
    val emoji: String,
    val title: String
)

data class CultureUiState(
    val bannerResId: Int = R.drawable.banner_1,
    val eras: List<CultureEraUiModel> = defaultCultureEras,
    val selectedEraIndex: Int = 2,
    val techniques: List<CultureTechniqueUiModel> = defaultCultureTechniques,
    val patterns: List<CulturePatternUiModel> = defaultCulturePatterns
) {
    val selectedEra: CultureEraUiModel
        get() = eras.getOrElse(selectedEraIndex) { eras.first() }
}

val defaultCultureEras = listOf(
    CultureEraUiModel(
        title = "春秋至",
        subtitle = "秦汉",
        emoji = "🏺",
        imageTitle = "早期纹样",
        description = "剪纸在这一时期更多以镂空饰片和礼俗装饰的形态出现，材料与工艺仍在发展，但已经能看到对对称、吉祥纹样和节庆用途的审美偏好。"
    ),
    CultureEraUiModel(
        title = "魏晋南",
        subtitle = "北朝",
        emoji = "🕊️",
        imageTitle = "佛影窗花",
        description = "随着宗教文化与民间装饰艺术交织，剪纸逐渐从礼仪附属走向更明确的观赏表达，纹样更轻逸，人物与禽鸟题材也开始丰富起来。"
    ),
    CultureEraUiModel(
        title = "隋唐至",
        subtitle = "五代",
        emoji = "🐉",
        imageTitle = "盛唐祥瑞",
        description = "如果把中华剪纸史比作一幅长卷，那隋唐五代几乎是最舒展的一段。社会繁荣、节俗兴盛，剪纸在婚嫁、节庆与祈福场景中广泛流行，图案也更饱满华丽。"
    ),
    CultureEraUiModel(
        title = "宋辽元",
        subtitle = "时代",
        emoji = "🏮",
        imageTitle = "市井雅趣",
        description = "宋元时期市民文化活跃，剪纸更贴近日常生活，窗花、灯彩和岁时装饰的应用明显增加，构图更加讲究层次与趣味，形成了细腻又生活化的面貌。"
    ),
    CultureEraUiModel(
        title = "明代至",
        subtitle = "清代",
        emoji = "🦋",
        imageTitle = "民俗高峰",
        description = "到了明清，剪纸真正进入民间普及与地方风格繁盛的高峰期。各地题材、刀法和构图语言逐渐分化成鲜明流派，喜庆吉祥与戏曲故事题材尤为常见。"
    )
)

val defaultCultureTechniques = listOf(
    CultureTechniqueUiModel("✂️", "剪刻技法"),
    CultureTechniqueUiModel("📐", "折剪技法"),
    CultureTechniqueUiModel("🧠", "创作方法")
)

val defaultCulturePatterns = listOf(
    CulturePatternUiModel("🪢", "盘长纹"),
    CulturePatternUiModel("🔶", "方胜纹"),
    CulturePatternUiModel("🌸", "连环纹"),
    CulturePatternUiModel("💮", "联珠纹")
)
