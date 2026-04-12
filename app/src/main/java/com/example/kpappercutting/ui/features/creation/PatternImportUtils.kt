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
import kotlin.math.roundToInt
import kotlin.math.sqrt

object PatternImportUtils {
    private const val MAX_BITMAP_SIDE = 160
    private const val ALPHA_THRESHOLD = 24
    private const val SIMPLIFY_EPSILON = 0.9f
    private const val MIN_LOOP_POINTS = 3
    private const val POINT_KEY_SCALE = 1000f

    fun importCustomPattern(
        context: Context,
        uri: Uri,
        displayName: String
    ): Result<CustomPattern> {
        return runCatching {
            val bitmap = decodeBitmap(context.contentResolver, uri)
            val normalizedPath = extractNormalizedPath(bitmap)
                ?: error("未识别到可切割的非透明轮廓，请尝试换一张主体清晰且带透明背景的 PNG。")
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
        val field = buildAlphaField(bitmap)
        val segments = marchingSquares(field)
        if (segments.isEmpty()) return null

        val rawLoops = assembleLoops(segments).filter { it.size >= MIN_LOOP_POINTS }
        if (rawLoops.isEmpty()) return null

        val contourLoops = classifyLoops(rawLoops)
        val contourPath = Path().apply {
            fillType = Path.FillType.EVEN_ODD
        }
        contourLoops.sortedByDescending { abs(it.signedArea) }.forEach { loop ->
            val simplified = simplifyClosedLoop(loop.points, SIMPLIFY_EPSILON)
            if (simplified.size >= MIN_LOOP_POINTS) {
                appendSmoothClosedLoop(
                    target = contourPath,
                    points = if (loop.isHole) simplified.reversed() else simplified
                )
            }
        }

        val bounds = RectF()
        contourPath.computeBounds(bounds, true)
        if (bounds.isEmpty) return null

        val longestSide = max(bounds.width(), bounds.height()).coerceAtLeast(1f)
        return Path(contourPath).apply {
            transform(
                Matrix().apply {
                    postTranslate(-bounds.centerX(), -bounds.centerY())
                    postScale(1f / longestSide, 1f / longestSide)
                }
            )
        }
    }

    private fun buildAlphaField(bitmap: Bitmap): Array<FloatArray> {
        val paddedHeight = bitmap.height + 2
        val paddedWidth = bitmap.width + 2
        return Array(paddedHeight) { y ->
            FloatArray(paddedWidth) { x ->
                if (x == 0 || y == 0 || x == paddedWidth - 1 || y == paddedHeight - 1) {
                    0f
                } else {
                    bitmap.getPixel(x - 1, y - 1).ushr(24) / 255f
                }
            }
        }
    }

    private fun marchingSquares(field: Array<FloatArray>): List<Segment> {
        val threshold = ALPHA_THRESHOLD / 255f
        val segments = mutableListOf<Segment>()
        for (y in 0 until field.lastIndex) {
            for (x in 0 until field[0].lastIndex) {
                val tl = field[y][x]
                val tr = field[y][x + 1]
                val br = field[y + 1][x + 1]
                val bl = field[y + 1][x]

                val top = interpolatePoint(x.toFloat(), y.toFloat(), x + 1f, y.toFloat(), tl, tr, threshold)
                val right = interpolatePoint(x + 1f, y.toFloat(), x + 1f, y + 1f, tr, br, threshold)
                val bottom = interpolatePoint(x + 1f, y + 1f, x.toFloat(), y + 1f, br, bl, threshold)
                val left = interpolatePoint(x.toFloat(), y + 1f, x.toFloat(), y.toFloat(), bl, tl, threshold)

                val edges = buildList {
                    if ((tl > threshold) != (tr > threshold)) add(Edge.TOP to top)
                    if ((tr > threshold) != (br > threshold)) add(Edge.RIGHT to right)
                    if ((br > threshold) != (bl > threshold)) add(Edge.BOTTOM to bottom)
                    if ((bl > threshold) != (tl > threshold)) add(Edge.LEFT to left)
                }

                when (edges.size) {
                    2 -> segments += Segment(edges[0].second, edges[1].second)
                    4 -> {
                        val cellCode = contourCode(tl, tr, br, bl, threshold)
                        val centerValue = (tl + tr + br + bl) / 4f
                        when (cellCode) {
                            5 -> {
                                if (centerValue > threshold) {
                                    segments += Segment(top, right)
                                    segments += Segment(bottom, left)
                                } else {
                                    segments += Segment(left, top)
                                    segments += Segment(right, bottom)
                                }
                            }

                            10 -> {
                                if (centerValue > threshold) {
                                    segments += Segment(left, top)
                                    segments += Segment(right, bottom)
                                } else {
                                    segments += Segment(top, right)
                                    segments += Segment(bottom, left)
                                }
                            }
                        }
                    }
                }
            }
        }
        return segments
    }

    private fun contourCode(
        tl: Float,
        tr: Float,
        br: Float,
        bl: Float,
        threshold: Float
    ): Int {
        var code = 0
        if (tl > threshold) code = code or 1
        if (tr > threshold) code = code or 2
        if (br > threshold) code = code or 4
        if (bl > threshold) code = code or 8
        return code
    }

    private fun assembleLoops(segments: List<Segment>): List<List<Vec2>> {
        val adjacency = linkedMapOf<PointKey, MutableList<Int>>()
        segments.forEachIndexed { index, segment ->
            adjacency.getOrPut(segment.start.key()) { mutableListOf() }.add(index)
            adjacency.getOrPut(segment.end.key()) { mutableListOf() }.add(index)
        }

        val used = BooleanArray(segments.size)
        val loops = mutableListOf<List<Vec2>>()

        segments.indices.forEach { segmentIndex ->
            if (used[segmentIndex]) return@forEach
            val startSegment = segments[segmentIndex]
            val loop = mutableListOf(startSegment.start)
            used[segmentIndex] = true

            var currentPoint = startSegment.end
            var previousPoint = startSegment.start
            var guard = 0

            while (guard < segments.size * 4) {
                guard += 1
                loop += currentPoint
                if (currentPoint.key() == loop.first().key()) {
                    break
                }
                val nextSegmentIndex = adjacency[currentPoint.key()]
                    ?.firstOrNull { candidate ->
                        !used[candidate] && !segments[candidate].otherEndpoint(currentPoint).key().equals(previousPoint.key())
                    }
                    ?: adjacency[currentPoint.key()]
                        ?.firstOrNull { candidate -> !used[candidate] }
                    ?: break

                used[nextSegmentIndex] = true
                val nextPoint = segments[nextSegmentIndex].otherEndpoint(currentPoint)
                previousPoint = currentPoint
                currentPoint = nextPoint
            }

            if (loop.size >= MIN_LOOP_POINTS && loop.last().key() == loop.first().key()) {
                loops += loop.dropLast(1)
            }
        }

        return loops
    }

    private fun classifyLoops(loops: List<List<Vec2>>): List<ContourLoop> {
        val contourLoops = loops.map { points ->
            ContourLoop(
                points = points,
                signedArea = signedArea(points)
            )
        }

        return contourLoops.map { loop ->
            val samplePoint = loop.points.first()
            val containCount = contourLoops.count { other ->
                other !== loop &&
                    abs(other.signedArea) > abs(loop.signedArea) &&
                    pointInPolygon(samplePoint, other.points)
            }
            loop.copy(isHole = containCount % 2 == 1)
        }
    }

    private fun simplifyClosedLoop(points: List<Vec2>, epsilon: Float): List<Vec2> {
        if (points.size <= 4) return points
        val polyline = points + points.first()
        val simplified = simplifyPolyline(polyline, epsilon)
        return simplified.dropLast(1)
            .distinctBy { it.key() }
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

    private fun appendSmoothClosedLoop(target: Path, points: List<Vec2>) {
        if (points.size < MIN_LOOP_POINTS) return
        val startMid = midpoint(points.last(), points.first())
        target.moveTo(startMid.x, startMid.y)
        points.forEachIndexed { index, point ->
            val next = points[(index + 1) % points.size]
            val mid = midpoint(point, next)
            target.quadTo(point.x, point.y, mid.x, mid.y)
        }
        target.close()
    }

    private fun interpolatePoint(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        v1: Float,
        v2: Float,
        threshold: Float
    ): Vec2 {
        val denominator = v2 - v1
        val t = if (abs(denominator) < 1e-6f) {
            0.5f
        } else {
            ((threshold - v1) / denominator).coerceIn(0f, 1f)
        }
        return Vec2(
            x = x1 + (x2 - x1) * t,
            y = y1 + (y2 - y1) * t
        )
    }

    private fun signedArea(points: List<Vec2>): Float {
        var area = 0f
        points.indices.forEach { index ->
            val current = points[index]
            val next = points[(index + 1) % points.size]
            area += (current.x * next.y) - (next.x * current.y)
        }
        return area / 2f
    }

    private fun pointInPolygon(point: Vec2, polygon: List<Vec2>): Boolean {
        var inside = false
        var previous = polygon.last()
        polygon.forEach { current ->
            val intersects = ((current.y > point.y) != (previous.y > point.y)) &&
                (point.x < (previous.x - current.x) * (point.y - current.y) / ((previous.y - current.y).takeIf { abs(it) > 1e-6f }
                    ?: 1e-6f) + current.x)
            if (intersects) {
                inside = !inside
            }
            previous = current
        }
        return inside
    }

    private fun perpendicularDistance(point: Vec2, lineStart: Vec2, lineEnd: Vec2): Float {
        val dx = lineEnd.x - lineStart.x
        val dy = lineEnd.y - lineStart.y
        if (abs(dx) < 1e-6f && abs(dy) < 1e-6f) {
            return distance(point, lineStart)
        }
        val numerator = abs(dy * point.x - dx * point.y + lineEnd.x * lineStart.y - lineEnd.y * lineStart.x)
        val denominator = sqrt(dx * dx + dy * dy)
        return numerator / denominator
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

    private enum class Edge {
        TOP, RIGHT, BOTTOM, LEFT
    }

    private data class Segment(
        val start: Vec2,
        val end: Vec2
    ) {
        fun otherEndpoint(point: Vec2): Vec2 {
            return if (point.key() == start.key()) end else start
        }
    }

    private data class ContourLoop(
        val points: List<Vec2>,
        val signedArea: Float,
        val isHole: Boolean = false
    )

    private data class Vec2(
        val x: Float,
        val y: Float
    ) {
        fun key(): PointKey {
            return PointKey(
                x = (x * POINT_KEY_SCALE).roundToInt(),
                y = (y * POINT_KEY_SCALE).roundToInt()
            )
        }
    }

    private data class PointKey(
        val x: Int,
        val y: Int
    )
}
