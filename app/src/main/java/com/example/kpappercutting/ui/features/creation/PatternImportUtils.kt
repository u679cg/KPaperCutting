package com.example.kpappercutting.ui.features.creation

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import kotlin.math.max

object PatternImportUtils {
    private const val MAX_BITMAP_SIDE = 160
    private const val ALPHA_THRESHOLD = 24

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
        val opaqueRunsPath = Path()
        for (y in 0 until bitmap.height) {
            var x = 0
            while (x < bitmap.width) {
                while (x < bitmap.width && bitmap.getPixel(x, y).ushr(24) <= ALPHA_THRESHOLD) {
                    x += 1
                }
                if (x >= bitmap.width) break
                val startX = x
                while (x < bitmap.width && bitmap.getPixel(x, y).ushr(24) > ALPHA_THRESHOLD) {
                    x += 1
                }
                opaqueRunsPath.addRect(
                    startX.toFloat(),
                    y.toFloat(),
                    x.toFloat(),
                    (y + 1).toFloat(),
                    Path.Direction.CW
                )
            }
        }

        val bounds = RectF()
        opaqueRunsPath.computeBounds(bounds, true)
        if (bounds.isEmpty) return null

        val longestSide = max(bounds.width(), bounds.height()).coerceAtLeast(1f)
        val normalized = Path(opaqueRunsPath)
        val matrix = Matrix().apply {
            postTranslate(-bounds.centerX(), -bounds.centerY())
            postScale(1f / longestSide, 1f / longestSide)
        }
        normalized.transform(matrix)
        return normalized
    }
}
