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
import kotlin.math.roundToInt

class PaperCutEngine {
    private data class Snapshot(
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

    private var mainPath = Path()
    private var drawingPath = Path()
    private var sketchBitmap: Bitmap? = null
    private var sketchCanvas: Canvas? = null

    private val undoStack = mutableListOf<Snapshot>()
    private val redoStack = mutableListOf<Snapshot>()

    private var lastTouchX = 0f
    private var lastTouchY = 0f
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

    fun attachSize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        if (width == canvasWidth && height == canvasHeight) return

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
        selectedShape = PaperShape.SQUARE
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
        lastTouchX = mappedPoint.x
        lastTouchY = mappedPoint.y
        if (!strokePendingEntry) {
            beginStrokeAt(mappedPoint)
        }
        bumpRenderVersion()
    }

    fun appendStroke(point: Offset) {
        if (!strokeActive) return
        val mappedPoint = mapPointToCanvas(point)
        if (!isPointInsideCanvas(mappedPoint)) {
            lastTouchX = mappedPoint.x
            lastTouchY = mappedPoint.y
            return
        }

        if (strokePendingEntry) {
            if (!canStartStrokeOnPaper(mappedPoint)) {
                lastTouchX = mappedPoint.x
                lastTouchY = mappedPoint.y
                return
            }
            val entryPoint = findStrokeEntryPoint(
                from = Offset(lastTouchX, lastTouchY),
                to = mappedPoint
            )
            beginStrokeAt(entryPoint)
            if (entryPoint != mappedPoint) {
                drawingPath.lineTo(mappedPoint.x, mappedPoint.y)
            }
            lastTouchX = mappedPoint.x
            lastTouchY = mappedPoint.y
            bumpRenderVersion()
            return
        }

        val midX = (lastTouchX + mappedPoint.x) / 2f
        val midY = (lastTouchY + mappedPoint.y) / 2f
        drawingPath.quadTo(lastTouchX, lastTouchY, midX, midY)
        if (selectedTool == EditTool.PENCIL || selectedTool == EditTool.ERASER) {
            applySketch()
            drawingPath.reset()
            drawingPath.moveTo(midX, midY)
        }
        lastTouchX = mappedPoint.x
        lastTouchY = mappedPoint.y
        bumpRenderVersion()
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
                if (!drawingPath.isEmpty) {
                    drawingPath.lineTo(lastTouchX, lastTouchY)
                    drawingPath.close()
                    performCut()
                }
            }
            EditTool.PENCIL, EditTool.ERASER -> {
                applySketch()
                saveSnapshot()
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
                    canvas.drawPath(drawingPath, selectionPaint)
                    canvas.drawPath(drawingPath, strokePaint)
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
        if (saveSnapshotAfterRestore) {
            saveSnapshot()
        }
    }

    private fun saveSnapshot() {
        if (undoStack.size >= 20) {
            undoStack.removeAt(0)
        }
        undoStack += Snapshot(
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

    private fun restoreSnapshot(snapshot: Snapshot) {
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
        return Matrix(transformMatrix).apply {
            if (isFolded && foldMode != FoldMode.NONE) {
                // Only the folded display is rotated so the sector axis aligns with the
                // screen midline x = screenWidth / 2. The model geometry stays in the
                // original fold basis, which preserves correct expand/cut correspondence.
                postRotate(getFoldedDisplayRotation(), centerX, centerY)
            }
        }
    }

    private fun getFoldedDisplayRotation(): Float {
        if (!isFolded || foldMode == FoldMode.NONE) return 0f
        return -(getFoldSweepAngle() / 2f)
    }

    private fun resetTransform() {
        if (canvasWidth <= 0 || canvasHeight <= 0) return
        transformMatrix.reset()
        transformMatrix.postScale(0.65f, 0.65f, centerX, centerY)
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

    private fun applyPaperColor(color: Int) {
        paperPaint.color = color
        paperShadowPaint.color = color
    }

    private fun bumpRenderVersion() {
        renderVersionInternal += 1
    }
}

private fun Bitmap?.deepCopy(): Bitmap? {
    if (this == null) return null
    return copy(Bitmap.Config.ARGB_8888, true)
}
