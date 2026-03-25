// 该文件用于实现创作页的核心剪纸引擎，负责纸张路径、草稿层、折叠逻辑和历史记录。
package com.example.kpappercutting.ui.features.creation.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.compose.ui.geometry.Offset
import com.example.kpappercutting.data.model.PaperShape
import com.example.kpappercutting.ui.features.creation.EditTool
import com.example.kpappercutting.ui.features.creation.FoldMode

class PaperCutEngine {
    private data class Snapshot(
        val paperPath: Path,
        val sketchBitmap: Bitmap?,
        val shape: PaperShape,
        val foldMode: FoldMode,
        val isFolded: Boolean
    )

    private val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B02621")
        style = Paint.Style.FILL
    }
    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80FFD700")
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val brushPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val eraserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = Paint.Style.STROKE
        strokeWidth = 28f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val eraserPreviewPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
        alpha = 150
        pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)
    }
    private val clearFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.TRANSPARENT
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = Paint.Style.FILL
    }

    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f
    private val paperBounds = RectF()

    private var mainPath = Path()
    private var drawingPath = Path()
    private var sketchBitmap: Bitmap? = null
    private var sketchCanvas: Canvas? = null

    private val undoStack = mutableListOf<Snapshot>()
    private val redoStack = mutableListOf<Snapshot>()

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var renderVersionInternal = 0

    var selectedShape: PaperShape = PaperShape.CIRCLE
        private set
    var selectedTool: EditTool = EditTool.SCISSORS
        private set
    var foldMode: FoldMode = FoldMode.NONE
        private set
    var isFolded: Boolean = false
        private set

    val renderVersion: Int
        get() = renderVersionInternal

    val canUndo: Boolean
        get() = undoStack.size > 1

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    val canExpand: Boolean
        get() = foldMode != FoldMode.NONE && isFolded

    val canSelectFiveFold: Boolean
        get() = foldMode == FoldMode.NONE || foldMode == FoldMode.FIVE_POINT

    val canSelectEightFold: Boolean
        get() = foldMode == FoldMode.NONE || foldMode == FoldMode.EIGHT_POINT

    fun attachSize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (width == canvasWidth && height == canvasHeight) return

        canvasWidth = width
        canvasHeight = height
        centerX = width / 2f
        centerY = height / 2f
        radius = minOf(width, height) / 2f * 0.85f
        paperBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        sketchBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        sketchCanvas = Canvas(sketchBitmap!!)
        rebuildPaperPath(clearSketch = true, resetHistory = true)
    }

    fun setTool(tool: EditTool) {
        selectedTool = tool
        bumpRenderVersion()
    }

    fun setShape(shape: PaperShape) {
        if (selectedShape == shape) return
        selectedShape = shape
        rebuildPaperPath(clearSketch = true, resetHistory = true)
    }

    fun clearCanvas() {
        rebuildPaperPath(clearSketch = true, resetHistory = true)
    }

    fun resetAll() {
        selectedTool = EditTool.SCISSORS
        selectedShape = PaperShape.CIRCLE
        foldMode = FoldMode.NONE
        isFolded = false
        rebuildPaperPath(clearSketch = true, resetHistory = true)
    }

    fun selectFoldMode(mode: FoldMode) {
        if (canvasWidth == 0 || canvasHeight == 0) return
        when {
            foldMode == FoldMode.NONE -> {
                foldMode = mode
                isFolded = true
                rebuildPaperPath(clearSketch = true, resetHistory = true)
            }
            foldMode == mode && !isFolded -> {
                foldLogic()
                bumpRenderVersion()
            }
        }
    }

    fun toggleFold() {
        if (foldMode == FoldMode.NONE) return
        if (isFolded) {
            unfoldLogic()
        } else {
            foldLogic()
        }
        bumpRenderVersion()
    }

    fun startStroke(point: Offset) {
        if (canvasWidth == 0 || canvasHeight == 0) return
        if (selectedTool == EditTool.PENCIL || selectedTool == EditTool.ERASER) {
            saveSnapshot()
        }
        drawingPath.reset()
        drawingPath.moveTo(point.x, point.y)
        lastTouchX = point.x
        lastTouchY = point.y
        bumpRenderVersion()
    }

    fun appendStroke(point: Offset) {
        val midX = (lastTouchX + point.x) / 2f
        val midY = (lastTouchY + point.y) / 2f
        drawingPath.quadTo(lastTouchX, lastTouchY, midX, midY)
        if (selectedTool == EditTool.PENCIL || selectedTool == EditTool.ERASER) {
            applySketch()
            drawingPath.reset()
            drawingPath.moveTo(midX, midY)
        }
        lastTouchX = point.x
        lastTouchY = point.y
        bumpRenderVersion()
    }

    fun endStroke() {
        if (drawingPath.isEmpty) return
        when (selectedTool) {
            EditTool.SCISSORS -> {
                if (!drawingPath.isEmpty) {
                    drawingPath.lineTo(lastTouchX, lastTouchY)
                    drawingPath.close()
                    performCut()
                }
            }
            EditTool.PENCIL, EditTool.ERASER -> applySketch()
        }
        drawingPath.reset()
        bumpRenderVersion()
    }

    fun undo() {
        if (undoStack.size <= 1) return
//        val current = undoStack.removeLast()
        val current = undoStack.removeAt(undoStack.lastIndex)
//        val current =undoStack.removeLastOrNull()
        redoStack += current
        restoreSnapshot(undoStack.last())
        bumpRenderVersion()
    }

    fun redo() {
        val snapshot = redoStack.removeLastOrNull() ?: return
        undoStack += snapshot.copy(
            paperPath = Path(snapshot.paperPath),
            sketchBitmap = snapshot.sketchBitmap.deepCopy()
        )
        restoreSnapshot(snapshot)
        bumpRenderVersion()
    }

    fun render(canvas: Canvas) {
        if (canvasWidth == 0 || canvasHeight == 0) return
        canvas.drawPath(mainPath, paperPaint)
        sketchBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        if (!drawingPath.isEmpty) {
            when (selectedTool) {
                EditTool.SCISSORS -> {
                    canvas.drawPath(drawingPath, selectionPaint)
                    canvas.drawPath(drawingPath, strokePaint)
                }
                EditTool.PENCIL -> canvas.drawPath(drawingPath, brushPaint)
                EditTool.ERASER -> canvas.drawPath(drawingPath, eraserPreviewPaint)
            }
        }
    }

    private fun rebuildPaperPath(clearSketch: Boolean, resetHistory: Boolean) {
        if (canvasWidth == 0 || canvasHeight == 0) return
        mainPath.reset()
        drawingPath.reset()

        if (clearSketch) {
            sketchBitmap?.eraseColor(Color.TRANSPARENT)
        }

        if (foldMode != FoldMode.NONE && isFolded) {
            val wedgeAngle = getFoldSweepAngle()
            mainPath.moveTo(centerX, centerY)
            mainPath.arcTo(paperBounds, -90f, wedgeAngle)
            mainPath.close()
        } else {
            if (selectedShape == PaperShape.CIRCLE) {
                mainPath.addCircle(centerX, centerY, radius, Path.Direction.CW)
            } else {
                mainPath.addRect(paperBounds, Path.Direction.CW)
            }
        }

        if (resetHistory) {
            undoStack.clear()
            redoStack.clear()
            saveSnapshot()
        }
        bumpRenderVersion()
    }

    private fun performCut() {
        saveSnapshot()
        mainPath.op(drawingPath, Path.Op.DIFFERENCE)
        sketchCanvas?.drawPath(drawingPath, clearFillPaint)
    }

    private fun applySketch() {
        val targetCanvas = sketchCanvas ?: return
        val targetPaint = if (selectedTool == EditTool.ERASER) eraserPaint else brushPaint
        targetCanvas.drawPath(drawingPath, targetPaint)
        if (selectedTool == EditTool.PENCIL) {
            clipSketchToPaper()
        }
    }

    private fun clipSketchToPaper() {
        val targetCanvas = sketchCanvas ?: return
        val outside = Path().apply {
            addRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), Path.Direction.CW)
            op(mainPath, Path.Op.DIFFERENCE)
        }
        targetCanvas.drawPath(outside, clearFillPaint)
    }

    private fun foldLogic() {
        val wedge = Path().apply {
            moveTo(centerX, centerY)
            arcTo(paperBounds, -90f, getFoldSweepAngle())
            close()
        }
        mainPath.op(wedge, Path.Op.INTERSECT)

        val outside = Path().apply {
            addRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), Path.Direction.CW)
            op(wedge, Path.Op.DIFFERENCE)
        }
        sketchCanvas?.drawPath(outside, clearFillPaint)
        isFolded = true
        saveSnapshot()
    }

    private fun unfoldLogic() {
        val segments = when (foldMode) {
            FoldMode.FIVE_POINT -> 5
            FoldMode.EIGHT_POINT -> 8
            FoldMode.NONE -> return
        }

        val mirroredPetal = Path().apply {
            addPath(mainPath)
            val mirrorMatrix = Matrix().apply { postScale(-1f, 1f, centerX, centerY) }
            addPath(mainPath, mirrorMatrix)
        }
        val fullPath = Path()
        val rotateMatrix = Matrix()
        val angleStep = 360f / segments
        repeat(segments) { index ->
            rotateMatrix.reset()
            rotateMatrix.postRotate(index * angleStep, centerX, centerY)
            fullPath.addPath(mirroredPetal, rotateMatrix)
        }
        mainPath = fullPath

        val sourceBitmap = sketchBitmap
        if (sourceBitmap == null) {
            isFolded = false
            return
        }
        val expandedBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val expandedCanvas = Canvas(expandedBitmap)
        repeat(segments) { index ->
            expandedCanvas.save()
            expandedCanvas.rotate(index * angleStep, centerX, centerY)
            expandedCanvas.drawBitmap(sourceBitmap, 0f, 0f, null)
            expandedCanvas.save()
            expandedCanvas.scale(-1f, 1f, centerX, centerY)
            expandedCanvas.drawBitmap(sourceBitmap, 0f, 0f, null)
            expandedCanvas.restore()
            expandedCanvas.restore()
        }
        sketchBitmap = expandedBitmap
        sketchCanvas = Canvas(expandedBitmap)
        isFolded = false
        clipSketchToPaper()
        saveSnapshot()
    }

    private fun saveSnapshot() {
        if (undoStack.size >= 20) {
            undoStack.removeAt(0)
        }
        undoStack += Snapshot(
            paperPath = Path(mainPath),
            sketchBitmap = sketchBitmap.deepCopy(),
            shape = selectedShape,
            foldMode = foldMode,
            isFolded = isFolded
        )
        redoStack.clear()
    }

    private fun restoreSnapshot(snapshot: Snapshot) {
        selectedShape = snapshot.shape
        foldMode = snapshot.foldMode
        isFolded = snapshot.isFolded
        mainPath = Path(snapshot.paperPath)
        sketchBitmap = snapshot.sketchBitmap.deepCopy() ?: Bitmap.createBitmap(
            canvasWidth,
            canvasHeight,
            Bitmap.Config.ARGB_8888
        )
        sketchCanvas = Canvas(sketchBitmap!!)
        drawingPath.reset()
    }

    private fun getFoldSweepAngle(): Float = when (foldMode) {
        FoldMode.FIVE_POINT -> 360f / 10f
        FoldMode.EIGHT_POINT -> 360f / 16f
        FoldMode.NONE -> 360f
    }

    private fun bumpRenderVersion() {
        renderVersionInternal += 1
    }
}

private fun Bitmap?.deepCopy(): Bitmap? {
    if (this == null) return null
    return copy(Bitmap.Config.ARGB_8888, true)
}
