package com.example.kpappercutting.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GalleryExporter {
    fun exportBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        displayNamePrefix: String = "paper_cut"
    ): Result<String> {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val displayName = "${displayNamePrefix}_$timestamp.png"
        val resolver = context.contentResolver
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/KPappercutting"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(collection, contentValues)
            ?: return Result.failure(IOException("无法创建相册文件"))

        return runCatching {
            resolver.openOutputStream(uri)?.use { stream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                    throw IOException("图片写入失败")
                }
            } ?: throw IOException("无法打开输出流")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            displayName
        }.onFailure {
            resolver.delete(uri, null, null)
        }
    }
}
