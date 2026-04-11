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
import android.graphics.Region
import androidx.compose.ui.geometry.Offset
import com.example.kpappercutting.ui.features.creation.CreationPaperDefaults
import com.example.kpappercutting.data.model.PaperShape
import com.example.kpappercutting.ui.features.creation.EditTool
import com.example.kpappercutting.ui.features.creation.EraserSize
import com.example.kpappercutting.ui.features.creation.FoldCatalog
import com.example.kpappercutting.ui.features.creation.FoldGeometry
import com.example.kpappercutting.ui.features.creation.FoldMode
import com.example.kpappercutting.ui.features.creation.spec
import kotlin.math.hypot
import kotlin.math.roundToInt

class PaperCutEngine {
    companion object {
        private const val SCISSORS_SMOOTHING_THRESHOLD = 5f
        private const val BRUSH_SMOOTHING_THRESHOLD = 4f
    }

    data class SessionState(
        val canvasWidth: Int,
        val canvasHeight: Int,
        val selectedShape: PaperShape,
        val selectedTool: EditTool,
        val selectedEraserSize: EraserSize,
        val selectedPaperColor: Int,
        val foldMode: FoldMode,
        val isFolded: Boolean,
        val renderVersion: Int,
        val transformValues: FloatArray,
        val mainPath: Path,
        val sketchBitmap: Bitmap?,
        val undoStack: List<HistorySnapshot>,
        val redoStack: List<HistorySnapshot>,
        val preFoldPaperPath: Path?,
        val preFoldSketchBitmap: Bitmap?,
        val foldedBasePath: Path?,
        val foldedBaseSketchBitmap: Bitmap?
    )

    data class HistorySnapshot(
        val paperPath: Path,
        val sketchBitmap: Bitmap?,
        val paperColor: Int,
        val shape: PaperShape,
        val foldMode: FoldMode,
        val isFolded: Boolean,
        val preFoldPaperPath: Path?,
        val preFoldSketchBitmap: Bitmap?,
        val foldedBasePath: Path?,
        val foldedBaseSketchBitmap: Bitmap?
    )

    private val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B02621")
        style = Paint.Style.FILL
    }
    private val paperShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B02621")
        style = Paint.Style.FILL
        setShadowLayer(28f, 0f, 14f, Color.argb(80, 54, 18, 16))
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
    private val clearBitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f
    private val paperBounds = RectF()
    private var paperRegion = Region()
    private val transformMatrix = Matrix()
    private val inverseMatrix = Matrix()
    private val matrixValues = FloatArray(9)
    private val foldedDisplayBaseMatrix = Matrix()
    private val currentDisplayMatrixCache = Matrix()
    private var foldedDisplayBaseDirty = true
    private var currentDisplayMatrixDirty = true

    private var mainPath = Path()
    private var drawingPath = Path()
    private var sketchBitmap: Bitmap? = null
    private var sketchCanvas: Canvas? = null

    private val undoStack = mutableListOf<HistorySnapshot>()
    private val redoStack = mutableListOf<HistorySnapshot>()

    private var lastPointerX = 0f
    private var lastPointerY = 0f
    private var lastPathX = 0f
    private var lastPathY = 0f
    private var strokeActive = false
    private var strokePendingEntry = false
    private var renderVersionInternal = 0
    private var loadedBitmap: Bitmap? = null
    private var preFoldPaperPath: Path? = null
    private var preFoldSketchBitmap: Bitmap? = null
    private var foldedBasePath: Path? = null
    private var foldedBaseSketchBitmap: Bitmap? = null

    var selectedShape: PaperShape = PaperShape.SQUARE
        private set
    var selectedTool: EditTool = EditTool.SCISSORS
        private set
    var selectedEraserSize: EraserSize = EraserSize.MEDIUM
        private set
    var selectedPaperColor: Int = CreationPaperDefaults.DEFAULT_PAPER_COLOR
        private set
    var foldMode: FoldMode = FoldMode.SIX_PART
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

    val availableFoldModes: List<FoldMode>
        get() = FoldCatalog.selectableModes

    fun saveSessionState(): SessionState {
        transformMatrix.getValues(matrixValues)
        return SessionState(
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight,
            selectedShape = selectedShape,
            selectedTool = selectedTool,
            selectedEraserSize = selectedEraserSize,
            selectedPaperColor = selectedPaperColor,
            foldMode = foldMode,
            isFolded = isFolded,
            renderVersion = renderVersionInternal,
            transformValues = matrixValues.copyOf(),
            mainPath = Path(mainPath),
            sketchBitmap = sketchBitmap.deepCopy(),
            undoStack = undoStack.map(::copyHistorySnapshot),
            redoStack = redoStack.map(::copyHistorySnapshot),
            preFoldPaperPath = preFoldPaperPath?.let(::Path),
            preFoldSketchBitmap = preFoldSketchBitmap.deepCopy(),
            foldedBasePath = foldedBasePath?.let(::Path),
            foldedBaseSketchBitmap = foldedBaseSketchBitmap.deepCopy()
        )
    }

    fun restoreSessionState(sessionState: SessionState) {
        canvasWidth = sessionState.canvasWidth
        canvasHeight = sessionState.canvasHeight
        selectedShape = sessionState.selectedShape
        selectedTool = sessionState.selectedTool
        selectedEraserSize = sessionState.selectedEraserSize
        selectedPaperColor = sessionState.selectedPaperColor
        foldMode = sessionState.foldMode
        isFolded = sessionState.isFolded
        renderVersionInternal = sessionState.renderVersion
        preFoldPaperPath = sessionState.preFoldPaperPath?.let(::Path)
        preFoldSketchBitmap = sessionState.preFoldSketchBitmap.deepCopy()
        foldedBasePath = sessionState.foldedBasePath?.let(::Path)
        foldedBaseSketchBitmap = sessionState.foldedBaseSketchBitmap.deepCopy()

        eraserPaint.strokeWidth = selectedEraserSize.strokeWidth
        eraserPreviewPaint.strokeWidth = selectedEraserSize.previewWidth
        applyPaperColor(selectedPaperColor)

        if (canvasWidth <= 0 || canvasHeight <= 0) {
            mainPath = Path()
            sketchBitmap = null
            sketchCanvas = null
            undoStack.clear()
            redoStack.clear()
            resetTransform()
            invalidateDisplayMatrices(baseMatrixChanged = true)
            bumpRenderVersion()
            return
        }

        centerX = canvasWidth / 2f
        centerY = canvasHeight / 2f
        radius = minOf(canvasWidth, canvasHeight) / 2f * 0.85f
        paperBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        mainPath = Path(sessionState.mainPath)
        sketchBitmap = sessionState.sketchBitmap.deepCopy()
            ?: Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        sketchCanvas = Canvas(sketchBitmap!!)
        transformMatrix.setValues(sessionState.transformValues)

        undoStack.clear()
        undoStack += sessionState.undoStack.map(::copyHistorySnapshot)
        redoStack.clear()
        redoStack += sessionState.redoStack.map(::copyHistorySnapshot)

        strokeActive = false
        strokePendingEntry = false
        drawingPath.reset()
        updatePaperRegion()
        invalidateDisplayMatrices(baseMatrixChanged = true)
        bumpRenderVersion()
    }

    fun attachSize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (width == canvasWidth && height == canvasHeight) return

        if (canvasWidth > 0 && canvasHeight > 0) {
            resizeCanvas(width, height)
            return
        }

        canvasWidth = width
        canvasHeight = height
        centerX = width / 2f
        centerY = height / 2f
        radius = minOf(width, height) / 2f * 0.85f
        paperBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        paperRegion = Region(0, 0, width, height)
        resetTransform()

        sketchBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        sketchCanvas = Canvas(sketchBitmap!!)
        rebuildPaperPath(clearSketch = true, resetHistory = true)
    }

    fun setTool(tool: EditTool) {
        selectedTool = tool
        bumpRenderVersion()
    }

    fun setEraserSize(size: EraserSize) {
        if (selectedEraserSize == size) return
        selectedEraserSize = size
        eraserPaint.strokeWidth = size.strokeWidth
        eraserPreviewPaint.strokeWidth = size.previewWidth
        bumpRenderVersion()
    }

    fun setPaperColor(color: Int) {
        if (selectedPaperColor == color) return
        selectedPaperColor = color
        applyPaperColor(color)
        if (canvasWidth > 0 && canvasHeight > 0) {
            saveSnapshot()
        }
        bumpRenderVersion()
    }

    fun setShape(shape: PaperShape) {
        if (selectedShape == shape) return
        selectedShape = shape
        rebuildPaperPath(clearSketch = true, resetHistory = true)
    }

    fun clearCanvas() {
        resetAll()
    }

    fun resetAll() {
        selectedTool = EditTool.SCISSORS
//        selectedShape = PaperShape.SQUARE
        selectedEraserSize = EraserSize.MEDIUM
        eraserPaint.strokeWidth = selectedEraserSize.strokeWidth
        eraserPreviewPaint.strokeWidth = selectedEraserSize.previewWidth
        selectedPaperColor = CreationPaperDefaults.DEFAULT_PAPER_COLOR
        applyPaperColor(selectedPaperColor)
        foldMode = FoldMode.SIX_PART
        isFolded = false
        preFoldPaperPath = null
        preFoldSketchBitmap = null
        foldedBasePath = null
        foldedBaseSketchBitmap = null
        loadedBitmap = null
        rebuildPaperPath(clearSketch = true, resetHistory = true)
        resetTransform()
    }

    fun loadBitmap(bitmap: Bitmap?) {
        loadedBitmap = bitmap
        bumpRenderVersion()
    }

    fun getBitmap(): Bitmap? {
        if (canvasWidth <= 0 || canvasHeight <= 0) return null
        val output = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val outputCanvas = Canvas(output)
        drawPaperContent(outputCanvas, mainPath)
        sketchBitmap?.let { outputCanvas.drawBitmap(it, 0f, 0f, null) }
        return output
    }

    fun getExpandedBitmap(): Bitmap? {
        if (canvasWidth <= 0 || canvasHeight <= 0) return null

        val exportPaperPath = if (isFolded) buildExpandedPaperPath() else Path(mainPath)
        val exportSketchBitmap = if (isFolded) {
            preFoldSketchBitmap.deepCopy()
        } else {
            sketchBitmap.deepCopy()
        }

        val output = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val outputCanvas = Canvas(output)
        drawPaperContent(outputCanvas, exportPaperPath)
        exportSketchBitmap?.let { bitmap ->
            outputCanvas.drawBitmap(bitmap, 0f, 0f, null)
        }
        return output
    }

    fun selectFoldMode(mode: FoldMode) {
        if (canvasWidth == 0 || canvasHeight == 0 || mode == FoldMode.NONE) return

        when {
            foldMode == FoldMode.NONE -> {
                foldMode = mode
                enterFoldedState()
                resetTransform()
                saveSnapshot()
                bumpRenderVersion()
            }

            foldMode == mode && !isFolded -> {
                foldLogic()
                resetTransform()
                bumpRenderVersion()
            }

            foldMode != mode -> {
                if (isFolded) {
                    unfoldLogic(saveSnapshotAfterRestore = false)
                }
                foldMode = mode
                enterFoldedState()
                resetTransform()
                saveSnapshot()
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
        resetTransform()
        bumpRenderVersion()
    }

    fun startStroke(point: Offset) {
        if (canvasWidth == 0 || canvasHeight == 0) return
        val mappedPoint = mapPointToCanvas(point)
        if (!isPointInsideCanvas(mappedPoint)) {
            strokeActive = false
            strokePendingEntry = false
            return
        }
        strokeActive = true
        strokePendingEntry = !canStartStrokeOnPaper(mappedPoint)
        drawingPath.reset()
        lastPointerX = mappedPoint.x
        lastPointerY = mappedPoint.y
        lastPathX = mappedPoint.x
        lastPathY = mappedPoint.y
        if (!strokePendingEntry) {
            beginStrokeAt(mappedPoint)
        }
        bumpRenderVersion()
    }

    fun appendStroke(point: Offset) {
        if (!strokeActive) return
        val mappedPoint = mapPointToCanvas(point)
        if (!isPointInsideCanvas(mappedPoint)) {
            lastPointerX = mappedPoint.x
            lastPointerY = mappedPoint.y
            return
        }

        if (strokePendingEntry) {
            if (!canStartStrokeOnPaper(mappedPoint)) {
                lastPointerX = mappedPoint.x
                lastPointerY = mappedPoint.y
                return
            }
            val entryPoint = findStrokeEntryPoint(
                from = Offset(lastPointerX, lastPointerY),
                to = mappedPoint
            )
            beginStrokeAt(entryPoint)
            appendSmoothedPoint(mappedPoint)
            lastPointerX = mappedPoint.x
            lastPointerY = mappedPoint.y
            bumpRenderVersion()
            return
        }

        val pathChanged = appendSmoothedPoint(mappedPoint)
        lastPointerX = mappedPoint.x
        lastPointerY = mappedPoint.y
        if (pathChanged) {
            bumpRenderVersion()
        }
    }

    fun endStroke() {
        if (!strokeActive) return
        if (strokePendingEntry) {
            strokeActive = false
            strokePendingEntry = false
            drawingPath.reset()
            bumpRenderVersion()
            return
        }
        if (drawingPath.isEmpty) {
            strokeActive = false
            strokePendingEntry = false
            drawingPath.reset()
            bumpRenderVersion()
            return
        }
        when (selectedTool) {
            EditTool.SCISSORS -> {
                if (finalizeStrokePath()) {
                    drawingPath.close()
                    performCut()
                }
            }
            EditTool.PENCIL, EditTool.ERASER -> {
                if (finalizeStrokePath()) {
                    applySketch()
                    saveSnapshot()
                }
            }
        }
        strokeActive = false
        strokePendingEntry = false
        drawingPath.reset()
        bumpRenderVersion()
    }

    fun transform(centroid: Offset, pan: Offset, zoom: Float) {
        if (canvasWidth == 0 || canvasHeight == 0) return
        if (strokeActive) {
            endStroke()
        }

        transformMatrix.getValues(matrixValues)
        val currentScale = matrixValues[Matrix.MSCALE_X]
        var clampedZoom = zoom
        if (currentScale * clampedZoom < 0.5f) {
            clampedZoom = 0.5f / currentScale
        }
        if (currentScale * clampedZoom > 5f) {
            clampedZoom = 5f / currentScale
        }

        transformMatrix.postTranslate(pan.x, pan.y)
        transformMatrix.postScale(clampedZoom, clampedZoom, centroid.x, centroid.y)
        invalidateDisplayMatrices()
        bumpRenderVersion()
    }

    fun undo() {
        if (undoStack.size <= 1) return
        val current = undoStack.removeAt(undoStack.lastIndex)
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
        canvas.save()
        canvas.concat(currentDisplayMatrix())
        drawPaperContent(canvas, mainPath)
        sketchBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        if (!drawingPath.isEmpty) {
            when (selectedTool) {
                EditTool.SCISSORS -> {
                    val previewPath = currentPreviewPath()
                    canvas.drawPath(previewPath, selectionPaint)
                    canvas.drawPath(previewPath, strokePaint)
                }
                EditTool.PENCIL -> canvas.drawPath(drawingPath, brushPaint)
                EditTool.ERASER -> canvas.drawPath(drawingPath, eraserPreviewPaint)
            }
        }
        canvas.restore()
    }

    private fun rebuildPaperPath(clearSketch: Boolean, resetHistory: Boolean) {
        if (canvasWidth == 0 || canvasHeight == 0) return
        mainPath.reset()
        drawingPath.reset()

        if (clearSketch) {
            sketchBitmap?.eraseColor(Color.TRANSPARENT)
        }
        strokeActive = false
        strokePendingEntry = false

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
        updatePaperRegion()
        invalidateDisplayMatrices(baseMatrixChanged = true)
        bumpRenderVersion()
    }

    private fun performCut() {
        mainPath.op(drawingPath, Path.Op.DIFFERENCE)
        sketchCanvas?.drawPath(drawingPath, clearFillPaint)
        saveSnapshot()
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
        enterFoldedState()
        saveSnapshot()
    }

    private fun buildExpandedPaperPath(): Path {
        val geometry = currentFoldGeometry() ?: return Path(mainPath)
        val basePath = preFoldPaperPath ?: return Path(mainPath)
        val baseFoldedPath = foldedBasePath ?: return Path(mainPath)

        val foldedRemovedArea = Path(baseFoldedPath).apply {
            op(mainPath, Path.Op.DIFFERENCE)
        }
        val expandedRemovedArea = expandPathByFoldSymmetry(
            path = foldedRemovedArea,
            geometry = geometry
        )
        return Path(basePath).apply {
            op(expandedRemovedArea, Path.Op.DIFFERENCE)
        }
    }

    private fun unfoldLogic(saveSnapshotAfterRestore: Boolean = true) {
        val geometry = currentFoldGeometry() ?: return
        val basePath = preFoldPaperPath
        val baseFoldedPath = foldedBasePath
        if (basePath == null || baseFoldedPath == null) {
            isFolded = false
            return
        }

        val foldedRemovedArea = Path(baseFoldedPath).apply {
            op(mainPath, Path.Op.DIFFERENCE)
        }
        val expandedRemovedArea = expandPathByFoldSymmetry(
            path = foldedRemovedArea,
            geometry = geometry
        )
        mainPath = Path(basePath).apply {
            op(expandedRemovedArea, Path.Op.DIFFERENCE)
        }

        val restoredSketch = preFoldSketchBitmap.deepCopy()
            ?: Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        sketchBitmap = restoredSketch
        sketchCanvas = Canvas(restoredSketch)
        isFolded = false
        preFoldPaperPath = null
        preFoldSketchBitmap = null
        foldedBasePath = null
        foldedBaseSketchBitmap = null
        updatePaperRegion()
        clipSketchToPaper()
        invalidateDisplayMatrices(baseMatrixChanged = true)
        if (saveSnapshotAfterRestore) {
            saveSnapshot()
        }
    }

    private fun saveSnapshot() {
        if (undoStack.size >= 20) {
            undoStack.removeAt(0)
        }
        undoStack += HistorySnapshot(
            paperPath = Path(mainPath),
            sketchBitmap = sketchBitmap.deepCopy(),
            paperColor = selectedPaperColor,
            shape = selectedShape,
            foldMode = foldMode,
            isFolded = isFolded,
            preFoldPaperPath = preFoldPaperPath?.let(::Path),
            preFoldSketchBitmap = preFoldSketchBitmap.deepCopy(),
            foldedBasePath = foldedBasePath?.let(::Path),
            foldedBaseSketchBitmap = foldedBaseSketchBitmap.deepCopy()
        )
        redoStack.clear()
    }

    private fun restoreSnapshot(snapshot: HistorySnapshot) {
        selectedPaperColor = snapshot.paperColor
        applyPaperColor(selectedPaperColor)
        selectedShape = snapshot.shape
        foldMode = snapshot.foldMode
        isFolded = snapshot.isFolded
        preFoldPaperPath = snapshot.preFoldPaperPath?.let(::Path)
        preFoldSketchBitmap = snapshot.preFoldSketchBitmap.deepCopy()
        foldedBasePath = snapshot.foldedBasePath?.let(::Path)
        foldedBaseSketchBitmap = snapshot.foldedBaseSketchBitmap.deepCopy()
        mainPath = Path(snapshot.paperPath)
        sketchBitmap = snapshot.sketchBitmap.deepCopy() ?: Bitmap.createBitmap(
            canvasWidth,
            canvasHeight,
            Bitmap.Config.ARGB_8888
        )
        sketchCanvas = Canvas(sketchBitmap!!)
        strokeActive = false
        strokePendingEntry = false
        drawingPath.reset()
        updatePaperRegion()
        invalidateDisplayMatrices(baseMatrixChanged = true)
    }

    private fun drawPaperContent(canvas: Canvas, path: Path) {
        canvas.save()
        canvas.drawPath(path, paperShadowPaint)
        canvas.drawPath(path, paperPaint)
        loadedBitmap?.let { bitmap ->
            canvas.clipPath(path)
            canvas.drawBitmap(bitmap, null, paperBounds, null)
        }
        canvas.restore()
    }

    private fun isPointInsideCanvas(point: Offset): Boolean {
        if (canvasWidth <= 0 || canvasHeight <= 0) return false
        return point.x in 0f..canvasWidth.toFloat() && point.y in 0f..canvasHeight.toFloat()
    }

    private fun beginStrokeAt(point: Offset) {
        strokePendingEntry = false
        drawingPath.reset()
        drawingPath.moveTo(point.x, point.y)
        lastPathX = point.x
        lastPathY = point.y
    }

    private fun appendSmoothedPoint(point: Offset): Boolean {
        val dx = point.x - lastPathX
        val dy = point.y - lastPathY
        if (hypot(dx.toDouble(), dy.toDouble()).toFloat() < currentSmoothingThreshold()) {
            return false
        }

        val midX = (lastPathX + point.x) / 2f
        val midY = (lastPathY + point.y) / 2f
        drawingPath.quadTo(lastPathX, lastPathY, midX, midY)
        if (selectedTool == EditTool.PENCIL || selectedTool == EditTool.ERASER) {
            applySketch()
            drawingPath.reset()
            drawingPath.moveTo(midX, midY)
        }
        lastPathX = point.x
        lastPathY = point.y
        return true
    }

    private fun finalizeStrokePath(): Boolean {
        if (drawingPath.isEmpty) {
            return false
        }
        drawingPath.quadTo(lastPathX, lastPathY, lastPathX, lastPathY)
        return true
    }

    private fun currentPreviewPath(): Path {
        if (selectedTool != EditTool.SCISSORS || !strokeActive) {
            return drawingPath
        }

        val dx = lastPointerX - lastPathX
        val dy = lastPointerY - lastPathY
        if (hypot(dx.toDouble(), dy.toDouble()).toFloat() <= 0.5f) {
            return drawingPath
        }

        val previewPath = Path(drawingPath)
        val midX = (lastPathX + lastPointerX) / 2f
        val midY = (lastPathY + lastPointerY) / 2f
        previewPath.quadTo(lastPathX, lastPathY, midX, midY)
        return previewPath
    }

    private fun currentSmoothingThreshold(): Float {
        return when (selectedTool) {
            EditTool.SCISSORS -> SCISSORS_SMOOTHING_THRESHOLD
            EditTool.PENCIL, EditTool.ERASER -> BRUSH_SMOOTHING_THRESHOLD
        }
    }

    private fun canStartStrokeOnPaper(point: Offset): Boolean {
        return isPointOnPaper(point, tolerance = 14f)
    }

    private fun findStrokeEntryPoint(from: Offset, to: Offset): Offset {
        if (isPointOnPaper(from, tolerance = 0f)) {
            return from
        }
        if (!isPointOnPaper(to, tolerance = 0f)) {
            return to
        }

        var low = 0f
        var high = 1f
        repeat(16) {
            val mid = (low + high) / 2f
            val candidate = lerp(from, to, mid)
            if (isPointOnPaper(candidate, tolerance = 0f)) {
                high = mid
            } else {
                low = mid
            }
        }
        return lerp(from, to, high)
    }

    private fun isPointOnPaper(point: Offset, tolerance: Float = 0f): Boolean {
        val roundedX = point.x.roundToInt()
        val roundedY = point.y.roundToInt()
        if (paperRegion.contains(roundedX, roundedY)) {
            return true
        }
        if (tolerance <= 0f) {
            return false
        }

        val toleranceInt = tolerance.roundToInt()
        for (dx in -toleranceInt..toleranceInt) {
            for (dy in -toleranceInt..toleranceInt) {
                if (dx * dx + dy * dy > toleranceInt * toleranceInt) continue
                if (paperRegion.contains(roundedX + dx, roundedY + dy)) {
                    return true
                }
            }
        }
        return false
    }

    private fun lerp(start: Offset, end: Offset, fraction: Float): Offset {
        return Offset(
            x = start.x + (end.x - start.x) * fraction,
            y = start.y + (end.y - start.y) * fraction
        )
    }

    private fun mapPointToCanvas(point: Offset): Offset {
        val values = floatArrayOf(point.x, point.y)
        currentDisplayMatrix().invert(inverseMatrix)
        inverseMatrix.mapPoints(values)
        return Offset(values[0], values[1])
    }

    private fun currentDisplayMatrix(): Matrix {
        if (currentDisplayMatrixDirty) {
            if (isFolded && foldMode != FoldMode.NONE) {
                updateFoldedDisplayBaseMatrixIfNeeded()
                currentDisplayMatrixCache.set(foldedDisplayBaseMatrix)
                // User gestures are concatenated after the folded display transform so
                // pinch-pan sensitivity stays consistent with the pre-enlargement behavior.
                currentDisplayMatrixCache.postConcat(transformMatrix)
            } else {
                currentDisplayMatrixCache.set(transformMatrix)
            }
            currentDisplayMatrixDirty = false
        }
        return currentDisplayMatrixCache
    }

    private fun getFoldedDisplayRotation(): Float {
        if (!isFolded || foldMode == FoldMode.NONE) return 0f
        return -(getFoldSweepAngle() / 2f)
    }

    private fun updateFoldedDisplayBaseMatrixIfNeeded() {
        if (!foldedDisplayBaseDirty) return

        foldedDisplayBaseMatrix.reset()
        // Two-part and four-part folds use a smaller default folded display size;
        // all other fold modes keep the current enlarged presentation.
        foldedDisplayBaseMatrix.postScale(getFoldedDisplayScale(), getFoldedDisplayScale(), centerX, centerY)
        // Align the folded sector axis with the screen vertical midline.
        foldedDisplayBaseMatrix.postRotate(getFoldedDisplayRotation(), centerX, centerY)
        val displayBounds = RectF()
        Path(mainPath).apply {
            transform(foldedDisplayBaseMatrix)
            computeBounds(displayBounds, true)
        }

        // Keep the enlarged folded sector centered inside the canvas area, which is the
        // region directly below the toolbar on every device size.
        foldedDisplayBaseMatrix.postTranslate(
            centerX - displayBounds.centerX(),
            centerY - displayBounds.centerY()
        )
        foldedDisplayBaseDirty = false
    }

    private fun getFoldedDisplayScale(): Float {
        return when (foldMode) {
            FoldMode.TWO_PART -> 1.5f
            FoldMode.FOUR_PART -> 2f
            else -> 3f
        }
    }

    private fun resetTransform() {
        if (canvasWidth <= 0 || canvasHeight <= 0) return
        transformMatrix.reset()
        transformMatrix.postScale(0.65f, 0.65f, centerX, centerY)
        invalidateDisplayMatrices()
    }

    private fun updatePaperRegion() {
        if (canvasWidth <= 0 || canvasHeight <= 0) {
            paperRegion = Region()
            return
        }
        val clipRegion = Region(0, 0, canvasWidth, canvasHeight)
        paperRegion = Region().apply {
            setPath(mainPath, clipRegion)
        }
    }

    private fun getFoldSweepAngle(): Float {
        return currentFoldGeometry()?.wedgeSweepAngle ?: 360f
    }

    private fun enterFoldedState() {
        if (canvasWidth == 0 || canvasHeight == 0) return
        val wedge = createFoldWedgePath()
        preFoldPaperPath = Path(mainPath)
        preFoldSketchBitmap = sketchBitmap.deepCopy()
        mainPath = Path(mainPath).apply {
            op(wedge, Path.Op.INTERSECT)
        }
        sketchBitmap = clipBitmapToPath(preFoldSketchBitmap, wedge)
            ?: Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        sketchCanvas = Canvas(sketchBitmap!!)
        foldedBasePath = Path(mainPath)
        foldedBaseSketchBitmap = sketchBitmap.deepCopy()
        isFolded = true
        updatePaperRegion()
        invalidateDisplayMatrices(baseMatrixChanged = true)
    }

    private fun createFoldWedgePath(): Path {
        return Path().apply {
            moveTo(centerX, centerY)
            arcTo(paperBounds, -90f, getFoldSweepAngle())
            close()
        }
    }

    private fun expandPathByFoldSymmetry(path: Path, geometry: FoldGeometry): Path {
        val foldedUnit = Path().apply {
            addPath(path)
            if (geometry.mirrorEnabled) {
                val mirrorMatrix = Matrix().apply { postScale(-1f, 1f, centerX, centerY) }
                addPath(path, mirrorMatrix)
            }
        }
        val fullPath = Path()
        val rotateMatrix = Matrix()
        repeat(geometry.rotationCopies) { index ->
            rotateMatrix.reset()
            rotateMatrix.postRotate(index * geometry.rotationStepAngle, centerX, centerY)
            fullPath.addPath(foldedUnit, rotateMatrix)
        }
        return fullPath
    }

    private fun currentFoldGeometry(): FoldGeometry? {
        if (foldMode == FoldMode.NONE) return null
        return foldMode.spec.geometry
    }

    private fun clipBitmapToPath(sourceBitmap: Bitmap?, clipPath: Path): Bitmap? {
        if (sourceBitmap == null) return null
        val clippedBitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val clippedCanvas = Canvas(clippedBitmap)
        val outside = Path().apply {
            addRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), Path.Direction.CW)
            op(clipPath, Path.Op.DIFFERENCE)
        }
        clippedCanvas.drawPath(outside, clearFillPaint)
        return clippedBitmap
    }

    private fun resizeCanvas(newWidth: Int, newHeight: Int) {
        val oldWidth = canvasWidth
        val oldHeight = canvasHeight
        val scaleX = newWidth.toFloat() / oldWidth.toFloat()
        val scaleY = newHeight.toFloat() / oldHeight.toFloat()
        val scaleMatrix = Matrix().apply { setScale(scaleX, scaleY) }

        canvasWidth = newWidth
        canvasHeight = newHeight
        centerX = newWidth / 2f
        centerY = newHeight / 2f
        radius = minOf(newWidth, newHeight) / 2f * 0.85f
        paperBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        paperRegion = Region(0, 0, newWidth, newHeight)

        mainPath.transform(scaleMatrix)
        drawingPath.transform(scaleMatrix)
        preFoldPaperPath = preFoldPaperPath?.scaled(scaleMatrix)
        foldedBasePath = foldedBasePath?.scaled(scaleMatrix)
        preFoldSketchBitmap = preFoldSketchBitmap.scaled(newWidth, newHeight)
        foldedBaseSketchBitmap = foldedBaseSketchBitmap.scaled(newWidth, newHeight)
        sketchBitmap = sketchBitmap.scaled(newWidth, newHeight)
            ?: Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        sketchCanvas = Canvas(sketchBitmap!!)

        resizeHistoryStack(undoStack, scaleMatrix, newWidth, newHeight)
        resizeHistoryStack(redoStack, scaleMatrix, newWidth, newHeight)

        strokeActive = false
        strokePendingEntry = false
        resetTransform()
        updatePaperRegion()
        invalidateDisplayMatrices(baseMatrixChanged = true)
        bumpRenderVersion()
    }

    private fun resizeHistoryStack(
        stack: MutableList<HistorySnapshot>,
        scaleMatrix: Matrix,
        newWidth: Int,
        newHeight: Int
    ) {
        if (stack.isEmpty()) return

        val resizedSnapshots = stack.map { snapshot ->
            snapshot.copy(
                paperPath = snapshot.paperPath.scaled(scaleMatrix),
                sketchBitmap = snapshot.sketchBitmap.scaled(newWidth, newHeight),
                preFoldPaperPath = snapshot.preFoldPaperPath?.scaled(scaleMatrix),
                preFoldSketchBitmap = snapshot.preFoldSketchBitmap.scaled(newWidth, newHeight),
                foldedBasePath = snapshot.foldedBasePath?.scaled(scaleMatrix),
                foldedBaseSketchBitmap = snapshot.foldedBaseSketchBitmap.scaled(newWidth, newHeight)
            )
        }

        stack.clear()
        stack += resizedSnapshots
    }

    private fun applyPaperColor(color: Int) {
        paperPaint.color = color
        paperShadowPaint.color = color
    }

    private fun copyHistorySnapshot(snapshot: HistorySnapshot): HistorySnapshot {
        return HistorySnapshot(
            paperPath = Path(snapshot.paperPath),
            sketchBitmap = snapshot.sketchBitmap.deepCopy(),
            paperColor = snapshot.paperColor,
            shape = snapshot.shape,
            foldMode = snapshot.foldMode,
            isFolded = snapshot.isFolded,
            preFoldPaperPath = snapshot.preFoldPaperPath?.let(::Path),
            preFoldSketchBitmap = snapshot.preFoldSketchBitmap.deepCopy(),
            foldedBasePath = snapshot.foldedBasePath?.let(::Path),
            foldedBaseSketchBitmap = snapshot.foldedBaseSketchBitmap.deepCopy()
        )
    }

    private fun invalidateDisplayMatrices(baseMatrixChanged: Boolean = false) {
        if (baseMatrixChanged) {
            foldedDisplayBaseDirty = true
        }
        currentDisplayMatrixDirty = true
    }

    private fun bumpRenderVersion() {
        renderVersionInternal += 1
    }
}

private fun Bitmap?.deepCopy(): Bitmap? {
    if (this == null) return null
    return copy(Bitmap.Config.ARGB_8888, true)
}

private fun Bitmap?.scaled(width: Int, height: Int): Bitmap? {
    if (this == null) return null
    return Bitmap.createScaledBitmap(this, width, height, true)
}

private fun Path.scaled(matrix: Matrix): Path {
    return Path(this).apply { transform(matrix) }
}
