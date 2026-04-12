package com.example.kpappercutting.ui.features.creation

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import java.util.UUID

sealed interface PatternSource {
    val id: String
    val displayName: String
}

enum class BuiltinPattern(
    override val id: String,
    override val displayName: String
) : PatternSource {
    CRESCENT(id = "builtin_crescent", displayName = "月牙"),
    STAR(id = "builtin_star", displayName = "五角星"),
    HEART(id = "builtin_heart", displayName = "爱心"),
    SQUARE(id = "builtin_square", displayName = "正方形")
}

data class CustomPattern(
    override val id: String = UUID.randomUUID().toString(),
    override val displayName: String,
    val uriString: String,
    val thumbnailUriString: String = uriString
) : PatternSource

data class EditablePatternState(
    val source: PatternSource,
    val center: Offset,
    val width: Float,
    val height: Float,
    val rotationDegrees: Float,
    val previewColor: Int = PatternDefaults.PREVIEW_COLOR,
    val isEditing: Boolean = true
)

object PatternDefaults {
    const val PREVIEW_COLOR: Int = 0xFFFFD700.toInt()
    const val MIN_PATTERN_SIZE: Float = 36f
    const val DEFAULT_PATTERN_SIZE_RATIO: Float = 0.28f
}

object PatternCatalog {
    val builtinPatterns: List<BuiltinPattern> = BuiltinPattern.entries

    fun unitPathFor(source: PatternSource): Path? {
        return when (source) {
            is BuiltinPattern -> when (source) {
                BuiltinPattern.CRESCENT -> buildCrescentPath()
                BuiltinPattern.STAR -> buildStarPath()
                BuiltinPattern.HEART -> buildHeartPath()
                BuiltinPattern.SQUARE -> buildSquarePath()
            }

            is CustomPattern -> null
        }
    }

    fun transformedPathFor(pattern: EditablePatternState): Path? {
        val unitPath = unitPathFor(pattern.source) ?: return null
        val matrix = Matrix().apply {
            postScale(pattern.width, pattern.height)
            postRotate(pattern.rotationDegrees)
            postTranslate(pattern.center.x, pattern.center.y)
        }
        return Path(unitPath).apply { transform(matrix) }
    }

    fun createEditablePattern(
        source: PatternSource,
        canvasCenter: Offset,
        maxSize: Float,
        previewColor: Int = PatternDefaults.PREVIEW_COLOR
    ): EditablePatternState {
        val baseSize = max(maxSize * PatternDefaults.DEFAULT_PATTERN_SIZE_RATIO, 88f)
        return EditablePatternState(
            source = source,
            center = canvasCenter,
            width = baseSize,
            height = baseSize,
            rotationDegrees = 0f,
            previewColor = previewColor
        )
    }

    fun boundsOf(pattern: EditablePatternState): RectF? {
        val path = transformedPathFor(pattern) ?: return null
        return RectF().also { path.computeBounds(it, true) }
    }

    private fun buildCrescentPath(): Path {
        val outer = Path().apply {
            addOval(RectF(-0.5f, -0.5f, 0.5f, 0.5f), Path.Direction.CW)
        }
        val inner = Path().apply {
            addOval(RectF(-0.12f, -0.42f, 0.72f, 0.42f), Path.Direction.CW)
        }
        return outer.apply {
            op(inner, Path.Op.DIFFERENCE)
        }
    }

    private fun buildStarPath(): Path {
        val path = Path()
        val outerRadius = 0.5f
        val innerRadius = 0.22f
        repeat(10) { index ->
            val angle = -PI / 2 + index * PI / 5
            val radius = if (index % 2 == 0) outerRadius else innerRadius
            val x = (cos(angle) * radius).toFloat()
            val y = (sin(angle) * radius).toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return path
    }

    private fun buildHeartPath(): Path {
        return Path().apply {
            moveTo(0f, 0.38f)
            cubicTo(-0.56f, 0.02f, -0.62f, -0.42f, -0.18f, -0.46f)
            cubicTo(0f, -0.48f, 0.08f, -0.3f, 0f, -0.18f)
            cubicTo(-0.08f, -0.3f, 0f, -0.48f, 0.18f, -0.46f)
            cubicTo(0.62f, -0.42f, 0.56f, 0.02f, 0f, 0.38f)
            close()
        }
    }

    private fun buildSquarePath(): Path {
        return Path().apply {
            addRect(RectF(-0.5f, -0.5f, 0.5f, 0.5f), Path.Direction.CW)
        }
    }
}

fun EditablePatternState.withCenter(center: Offset): EditablePatternState = copy(center = center)

fun EditablePatternState.withSize(width: Float, height: Float): EditablePatternState = copy(
    width = max(width, PatternDefaults.MIN_PATTERN_SIZE),
    height = max(height, PatternDefaults.MIN_PATTERN_SIZE)
)

fun EditablePatternState.scaleUniform(factor: Float): EditablePatternState {
    val clampedFactor = max(factor, 0.1f)
    return withSize(width * clampedFactor, height * clampedFactor)
}

fun EditablePatternState.canvasBounds(): RectF {
    return RectF(
        center.x - width / 2f,
        center.y - height / 2f,
        center.x + width / 2f,
        center.y + height / 2f
    )
}

fun EditablePatternState.fitsWithin(minSize: Float = PatternDefaults.MIN_PATTERN_SIZE): EditablePatternState {
    return copy(
        width = max(width, minSize),
        height = max(height, minSize)
    )
}

fun Offset.rotateAround(origin: Offset, degrees: Float): Offset {
    val radians = Math.toRadians(degrees.toDouble())
    val dx = x - origin.x
    val dy = y - origin.y
    val rotatedX = dx * cos(radians) - dy * sin(radians)
    val rotatedY = dx * sin(radians) + dy * cos(radians)
    return Offset(
        x = origin.x + rotatedX.toFloat(),
        y = origin.y + rotatedY.toFloat()
    )
}

fun RectF.centerOffset(): Offset = Offset(centerX(), centerY())

fun RectF.maxDimension(): Float = max(width(), height())

fun RectF.minDimension(): Float = min(width(), height())
