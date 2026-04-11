// 该文件用于承接创作页的 ViewModel 与导航入口，把状态驱动的 Route 与纯渲染 Screen 分离开。
package com.example.kpappercutting.ui.features.creation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kpappercutting.util.GalleryExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CreateRoute(
    onBack: () -> Unit = {},
    onMenuAction: (CreationMenuAction) -> Unit = {},
    viewModel: CreateViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.persistSession()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.persistSession()
        }
    }

    CreateScreen(
        uiState = viewModel.uiState,
        engine = viewModel.engine,
        onAction = viewModel::onAction,
        onMenuAction = { action ->
            if (action == CreationMenuAction.INITIALIZE_CANVAS) {
                viewModel.resetCanvas()
            } else if (action == CreationMenuAction.EXPORT_TO_GALLERY) {
                coroutineScope.launch {
                    val exportResult = withContext(Dispatchers.IO) {
                        val bitmap = viewModel.engine.getExpandedBitmap()
                            ?: return@withContext Result.failure<String>(
                                IllegalStateException("当前没有可导出的图形")
                            )
                        GalleryExporter.exportBitmapToGallery(
                            context = context,
                            bitmap = bitmap,
                            displayNamePrefix = "kpappercutting"
                        )
                    }

                    val message = exportResult.fold(
                        onSuccess = { fileName -> "已导出到相册：$fileName" },
                        onFailure = { throwable ->
                            throwable.message ?: "导出失败，请稍后重试"
                        }
                    )
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                onMenuAction(action)
            }
        },
        onBack = onBack
    )
}
