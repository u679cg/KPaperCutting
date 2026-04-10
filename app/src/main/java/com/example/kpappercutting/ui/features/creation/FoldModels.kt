package com.example.kpappercutting.ui.features.creation

enum class FoldMode {
    NONE,
    TWO_PART,
    THREE_PART,
    FOUR_PART,
    FIVE_POINT,
    SIX_PART,
    EIGHT_PART,
    TEN_PART,
    TWELVE_PART,
    EIGHT_POINT
}

data class FoldGeometry(
    val wedgeSweepAngle: Float,
    val rotationCopies: Int,
    val mirrorEnabled: Boolean = true,
    val rotationStepAngle: Float = 360f / rotationCopies.coerceAtLeast(1)
)

data class FoldSpec(
    val mode: FoldMode,
    val displayName: String,
    val legacyNames: List<String>,
    val segmentCount: Int,
    val baseUnitAngle: Float,
    val layerCount: Int,
    val derivedFrom: FoldMode? = null,
    val derivedByHalving: Boolean = false,
    val description: String,
    val geometry: FoldGeometry
)

object FoldCatalog {
    private val specs = linkedMapOf(
        FoldMode.NONE to FoldSpec(
            mode = FoldMode.NONE,
            displayName = "不折叠",
            legacyNames = emptyList(),
            segmentCount = 1,
            baseUnitAngle = 360f,
            layerCount = 1,
            description = "完整纸面自由创作",
            geometry = FoldGeometry(
                wedgeSweepAngle = 360f,
                rotationCopies = 1,
                mirrorEnabled = false,
                rotationStepAngle = 360f
            )
        ),
        FoldMode.TWO_PART to FoldSpec(
            mode = FoldMode.TWO_PART,
            displayName = "二分法",
            legacyNames = emptyList(),
            segmentCount = 2,
            baseUnitAngle = 180f,
            layerCount = 1,
            description = "半幅对折，适合轴对称纹样",
            geometry = FoldGeometry(wedgeSweepAngle = 180f, rotationCopies = 1)
        ),
        FoldMode.THREE_PART to FoldSpec(
            mode = FoldMode.THREE_PART,
            displayName = "三分法",
            legacyNames = emptyList(),
            segmentCount = 3,
            baseUnitAngle = 60f,
            layerCount = 3,
            description = "180° 三等分，每份 60°",
            geometry = FoldGeometry(wedgeSweepAngle = 60f, rotationCopies = 3)
        ),
        FoldMode.FOUR_PART to FoldSpec(
            mode = FoldMode.FOUR_PART,
            displayName = "四分法",
            legacyNames = emptyList(),
            segmentCount = 4,
            baseUnitAngle = 90f,
            layerCount = 2,
            description = "4 分区，90° 基础单元",
            geometry = FoldGeometry(wedgeSweepAngle = 90f, rotationCopies = 2)
        ),
        FoldMode.FIVE_POINT to FoldSpec(
            mode = FoldMode.FIVE_POINT,
            displayName = "五分法",
            legacyNames = listOf("五角折法"),
            segmentCount = 5,
            baseUnitAngle = 36f,
            layerCount = 5,
            description = "180° 五等分，每份 36°",
            geometry = FoldGeometry(wedgeSweepAngle = 36f, rotationCopies = 5)
        ),
        FoldMode.SIX_PART to FoldSpec(
            mode = FoldMode.SIX_PART,
            displayName = "六分法",
            legacyNames = emptyList(),
            segmentCount = 6,
            baseUnitAngle = 30f,
            layerCount = 6,
            derivedFrom = FoldMode.THREE_PART,
            derivedByHalving = true,
            description = "三分法再对折，30°",
            geometry = FoldGeometry(wedgeSweepAngle = 30f, rotationCopies = 6)
        ),
        FoldMode.EIGHT_PART to FoldSpec(
            mode = FoldMode.EIGHT_PART,
            displayName = "八分法",
            legacyNames = emptyList(),
            segmentCount = 8,
            baseUnitAngle = 45f,
            layerCount = 4,
            derivedFrom = FoldMode.FOUR_PART,
            derivedByHalving = true,
            description = "四分法再对折，45°",
            geometry = FoldGeometry(wedgeSweepAngle = 45f, rotationCopies = 4)
        ),
        FoldMode.TEN_PART to FoldSpec(
            mode = FoldMode.TEN_PART,
            displayName = "十分法",
            legacyNames = emptyList(),
            segmentCount = 10,
            baseUnitAngle = 18f,
            layerCount = 10,
            derivedFrom = FoldMode.FIVE_POINT,
            derivedByHalving = true,
            description = "五分法再对折，18°",
            geometry = FoldGeometry(wedgeSweepAngle = 18f, rotationCopies = 10)
        ),
        FoldMode.TWELVE_PART to FoldSpec(
            mode = FoldMode.TWELVE_PART,
            displayName = "十二分法",
            legacyNames = emptyList(),
            segmentCount = 12,
            baseUnitAngle = 15f,
            layerCount = 12,
            derivedFrom = FoldMode.SIX_PART,
            derivedByHalving = true,
            description = "六分法再对折，15°",
            geometry = FoldGeometry(wedgeSweepAngle = 15f, rotationCopies = 12)
        ),
        FoldMode.EIGHT_POINT to FoldSpec(
            mode = FoldMode.EIGHT_POINT,
            displayName = "十六分法",
            legacyNames = listOf("八角折法"),
            segmentCount = 16,
            baseUnitAngle = 22.5f,
            layerCount = 8,
            derivedFrom = FoldMode.EIGHT_PART,
            derivedByHalving = true,
            description = "八分法再对折，22.5°",
            geometry = FoldGeometry(wedgeSweepAngle = 22.5f, rotationCopies = 8)
        )
    )

    val selectableModes: List<FoldMode> = specs.keys.filterNot { it == FoldMode.NONE }

    fun specOf(mode: FoldMode): FoldSpec = specs.getValue(mode)

    fun specsForSelection(): List<FoldSpec> = selectableModes.map(::specOf)

    fun matchesLegacyName(mode: FoldMode, name: String): Boolean {
        val spec = specOf(mode)
        return name == spec.displayName || name in spec.legacyNames
    }

    fun fromDisplayName(name: String): FoldMode? {
        return specs.values.firstOrNull { spec ->
            name == spec.displayName || name in spec.legacyNames
        }?.mode
    }
}

val FoldMode.spec: FoldSpec
    get() = FoldCatalog.specOf(this)
