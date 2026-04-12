package com.example.kpappercutting.ui.features.creation

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sqrt

object PatternImportUtils {
    private const val MAX_BITMAP_SIDE = 160
    private const val ALPHA_THRESHOLD = 24
    private const val SIMPLIFY_EPSILON = 1.15f
    private const val MIN_LOOP_POINTS = 3

    fun importCustomPattern(
        context: Context,
        uri: Uri,
        displayName: String
    ): Result<CustomPattern> {
        return runCatching {
            val bitmap = decodeBitmap(context.contentResolver, uri)
            val normalizedPath = extractNormalizedPath(bitmap)
                ?: error("未识别到可切割的非透明轮廓，请尝试换一张主体更清晰的 PNG。")
            CustomPattern(
                displayName = displayName,
                uriString = uri.toString(),
                normalizedPath = normalizedPath
            )
        }
    }

    private fun decodeBitmap(
        contentResolver: ContentResolver,
        uri: Uri
    ): Bitmap {
        val source = ImageDecoder.createSource(contentResolver, uri)
        val decodedBitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = false
            val size = info.size
            val longestSide = max(size.width, size.height).coerceAtLeast(1)
            if (longestSide > MAX_BITMAP_SIDE) {
                val scale = MAX_BITMAP_SIDE.toFloat() / longestSide.toFloat()
                decoder.setTargetSize(
                    (size.width * scale).toInt().coerceAtLeast(1),
                    (size.height * scale).toInt().coerceAtLeast(1)
                )
            }
        }
        return if (decodedBitmap.config == Bitmap.Config.HARDWARE) {
            decodedBitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            decodedBitmap
        }
    }

    private fun extractNormalizedPath(bitmap: Bitmap): Path? {
        val mask = Array(bitmap.height) { y ->
            BooleanArray(bitmap.width) { x ->
                bitmap.getPixel(x, y).ushr(24) > ALPHA_THRESHOLD
            }
        }
        val loops = traceMaskContours(mask)
        if (loops.isEmpty()) return null

        val contourPath = Path().apply {
            fillType = Path.FillType.EVEN_ODD
        }
        loops.forEach { loop ->
            if (loop.size >= MIN_LOOP_POINTS) {
                val simplified = simplifyClosedLoop(loop, SIMPLIFY_EPSILON)
                contourPath.op(buildSmoothClosedPath(simplified), Path.Op.UNION)
            }
        }

        val bounds = RectF()
        contourPath.computeBounds(bounds, true)
        if (bounds.isEmpty) return null

        val longestSide = max(bounds.width(), bounds.height()).coerceAtLeast(1f)
        val normalized = Path(contourPath)
        val matrix = Matrix().apply {
            postTranslate(-bounds.centerX(), -bounds.centerY())
            postScale(1f / longestSide, 1f / longestSide)
        }
        normalized.transform(matrix)
        return normalized
    }

    private fun traceMaskContours(mask: Array<BooleanArray>): List<List<Vec2>> {
        if (mask.isEmpty() || mask.first().isEmpty()) return emptyList()
        val height = mask.size
        val width = mask.first().size
        val adjacency = linkedMapOf<Vec2, MutableList<Vec2>>()

        fun addEdge(start: Vec2, end: Vec2) {
            adjacency.getOrPut(start) { mutableListOf() }.add(end)
        }

        fun isFilled(x: Int, y: Int): Boolean {
            return y in 0 until height && x in 0 until width && mask[y][x]
        }

        for (y in 0 until height) {
            for (x in 0 until width) {
                if (!mask[y][x]) continue
                if (!isFilled(x, y - 1)) addEdge(Vec2(x.toFloat(), y.toFloat()), Vec2((x + 1).toFloat(), y.toFloat()))
                if (!isFilled(x + 1, y)) addEdge(Vec2((x + 1).toFloat(), y.toFloat()), Vec2((x + 1).toFloat(), (y + 1).toFloat()))
                if (!isFilled(x, y + 1)) addEdge(Vec2((x + 1).toFloat(), (y + 1).toFloat()), Vec2(x.toFloat(), (y + 1).toFloat()))
                if (!isFilled(x - 1, y)) addEdge(Vec2(x.toFloat(), (y + 1).toFloat()), Vec2(x.toFloat(), y.toFloat()))
            }
        }

        val loops = mutableListOf<List<Vec2>>()
        while (true) {
            val start = adjacency.entries.firstOrNull { it.value.isNotEmpty() }?.key ?: break
            val loop = mutableListOf<Vec2>()
            var current = start
            var guard = 0

            while (guard < width * height * 8) {
                guard += 1
                loop += current
                val nextList = adjacency[current] ?: break
                if (nextList.isEmpty()) break
                val next = nextList.removeAt(0)
                if (nextList.isEmpty()) {
                    adjacency.remove(current)
                }
                current = next
                if (current == start) {
                    break
                }
            }

            if (loop.size >= MIN_LOOP_POINTS) {
                loops += loop
            }
        }
        return loops
    }

    private fun simplifyClosedLoop(points: List<Vec2>, epsilon: Float): List<Vec2> {
        if (points.size <= 4) return points
        val openPoints = points + points.first()
        val simplified = simplifyPolyline(openPoints, epsilon)
        return simplified.dropLast(1).distinct()
            .takeIf { it.size >= MIN_LOOP_POINTS }
            ?: points
    }

    private fun simplifyPolyline(points: List<Vec2>, epsilon: Float): List<Vec2> {
        if (points.size < 3) return points

        var maxDistance = 0f
        var maxIndex = -1
        for (index in 1 until points.lastIndex) {
            val distance = perpendicularDistance(points[index], points.first(), points.last())
            if (distance > maxDistance) {
                maxDistance = distance
                maxIndex = index
            }
        }

        return if (maxDistance > epsilon && maxIndex != -1) {
            val left = simplifyPolyline(points.subList(0, maxIndex + 1), epsilon)
            val right = simplifyPolyline(points.subList(maxIndex, points.size), epsilon)
            left.dropLast(1) + right
        } else {
            listOf(points.first(), points.last())
        }
    }

    private fun perpendicularDistance(point: Vec2, lineStart: Vec2, lineEnd: Vec2): Float {
        val dx = lineEnd.x - lineStart.x
        val dy = lineEnd.y - lineStart.y
        if (abs(dx) < 1e-4f && abs(dy) < 1e-4f) {
            return distance(point, lineStart)
        }

        val numerator = abs(dy * point.x - dx * point.y + lineEnd.x * lineStart.y - lineEnd.y * lineStart.x)
        val denominator = sqrt(dx * dx + dy * dy)
        return numerator / denominator
    }

    private fun buildSmoothClosedPath(points: List<Vec2>): Path {
        if (points.size < MIN_LOOP_POINTS) return Path()
        return Path().apply {
            val startMid = midpoint(points.last(), points.first())
            moveTo(startMid.x, startMid.y)
            points.forEachIndexed { index, point ->
                val next = points[(index + 1) % points.size]
                val mid = midpoint(point, next)
                quadTo(point.x, point.y, mid.x, mid.y)
            }
            close()
        }
    }

    private fun midpoint(a: Vec2, b: Vec2): Vec2 {
        return Vec2(
            x = (a.x + b.x) / 2f,
            y = (a.y + b.y) / 2f
        )
    }

    private fun distance(a: Vec2, b: Vec2): Float {
        return hypot((a.x - b.x).toDouble(), (a.y - b.y).toDouble()).toFloat()
    }

    private data class Vec2(
        val x: Float,
        val y: Float
    )
}
