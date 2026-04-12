// 该文件用于承接创作页的 ViewModel 与导航入口，把状态驱动的 Route 与纯渲染 Screen 分离开。
package com.example.kpappercutting.ui.features.creation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kpappercutting.util.GalleryExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class CreateSubRoute {
    MAIN,
    ADD_PATTERN
}

@Composable
fun CreateRoute(
    onBack: () -> Unit = {},
    onMenuAction: (CreationMenuAction) -> Unit = {},
    viewModel: CreateViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var currentSubRoute by rememberSaveable { mutableStateOf(CreateSubRoute.MAIN) }

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

    when (currentSubRoute) {
        CreateSubRoute.MAIN -> {
            CreateScreen(
                uiState = viewModel.uiState,
                engine = viewModel.engine,
                onAction = viewModel::onAction,
                onOpenAddPattern = { currentSubRoute = CreateSubRoute.ADD_PATTERN },
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

        CreateSubRoute.ADD_PATTERN -> {
            AddPatternScreen(
                customPatterns = viewModel.uiState.customPatterns,
                onBack = { currentSubRoute = CreateSubRoute.MAIN },
                onPatternImported = { uri ->
                    coroutineScope.launch {
                        val importResult = withContext(Dispatchers.IO) {
                            PatternImportUtils.importCustomPattern(
                                context = context,
                                uri = uri,
                                displayName = "自定义图案 ${viewModel.uiState.customPatterns.size + 1}"
                            )
                        }
                        importResult.fold(
                            onSuccess = { pattern ->
                                viewModel.onAction(CreateUiAction.AddCustomPattern(pattern))
                                Toast.makeText(context, "图案已导入并提取轮廓", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { throwable ->
                                Toast.makeText(
                                    context,
                                    throwable.message ?: "图案导入失败",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            )
        }
    }
}
