package com.zhouppei.digimuscore.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.zhouppei.digimuscore.notation.DrawPath
import kotlin.math.abs


class PaintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var mBrushSize = DEFAULT_BRUSH_SIZE
    private var mEraserSize = DEFAULT_ERASER_SIZE
    private var mOpacity = DEFAULT_OPACITY

    private var mDrawPaths: MutableList<DrawPath> = mutableListOf()

    // variables for undo action
    private var mUndoActions: MutableList<UndoAction> = mutableListOf()
    private var mAddedPaths: MutableList<DrawPath> = mutableListOf()
    private var mDeletedPaths: MutableList<DrawPath> = mutableListOf()

    private var mCurrentPaint = Paint()
    private var mDrawPaint = Paint()
    private lateinit var mPath: DrawPath

    private lateinit var mDrawCanvas: Canvas
    private var mDrawingMode = DrawingMode.SELECT
    private var mEraserMode = EraserMode.NORMAL
    private var mTouchX = 0f
    private var mTouchY = 0f


    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        mCurrentPaint.color = Color.BLACK
        initPathAndPaint()
        visibility = GONE
    }

    private fun initPathAndPaint() {
        mPath = DrawPath()
        mCurrentPaint.apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = mBrushSize
            alpha = mOpacity
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
        }
    }

    private fun refreshDrawing() {
        mDrawingMode = DrawingMode.DRAW
        initPathAndPaint()
    }

    private fun eraser() {
        mDrawingMode = DrawingMode.ERASER
        mCurrentPaint.apply {
            strokeWidth = mEraserSize
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
    }

    fun addLastAction(action: UndoAction) {
        mUndoActions.add(action)
    }

    fun setDrawingMode(drawingMode: DrawingMode) {
        mDrawingMode = drawingMode
        if (drawingMode == DrawingMode.DRAW || drawingMode == DrawingMode.ERASER) {
            visibility = VISIBLE
            refreshDrawing()
            if (drawingMode == DrawingMode.ERASER) eraser()
        }
    }

    fun setOpacity(@IntRange(from = 0, to = 255) opacity: Int) {
        mOpacity = opacity
        setDrawingMode(DrawingMode.DRAW)
    }

    fun setBrushSize(size: Float) {
        mBrushSize = size
        setDrawingMode(DrawingMode.DRAW)
    }

    fun setBrushColor(@ColorInt color: Int) {
        mCurrentPaint.color = color
        setDrawingMode(DrawingMode.DRAW)
    }

    fun setEraserSize(size: Float) {
        mEraserSize = size
        eraser()
    }

    fun setEraserMode(mode: EraserMode) {
        mEraserMode = mode
        eraser()
    }

    fun clearAll() {
        mDrawPaths.clear()
        mDrawCanvas.drawColor(0, PorterDuff.Mode.CLEAR)
        invalidate()
    }

    /**
     *  @return null if there is no action to undo or already handled
     *          else undoaction is returned which need to be handled
     */
    fun undo(): UndoAction? {
        if (mUndoActions.isNotEmpty()) {
            return when (val lastAction = mUndoActions.removeAt(mUndoActions.size - 1)) {
                UndoAction.ADD_PATH -> {
                    mDrawPaths.remove(mAddedPaths.removeAt(mAddedPaths.size - 1))
                    invalidate()
                    null
                }
                UndoAction.REMOVE_PATH -> {
                    mDrawPaths.add(mDeletedPaths.removeAt(mDeletedPaths.size - 1))
                    invalidate()
                    null
                }
                UndoAction.ADD_TEXT, UndoAction.REMOVE_TEXT -> lastAction
            }
        }
        return null
    }

    fun setDraws(draws: MutableList<DrawPath>) {
        // reset variables
        mUndoActions.clear()
        mAddedPaths.clear()
        mDeletedPaths.clear()

        mDrawPaths.clear()
        draws.forEach {
            mDrawPaths.add(it)
            it.loadPathPointsAsQuadTo()
        }
        invalidate()
    }

    fun getDrawPaths(): MutableList<DrawPath> {
        return mDrawPaths
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (h > 0) {
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            mDrawCanvas = Canvas(bitmap)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        mDrawPaths.forEach {
            it.setupPaint(mDrawPaint)
            canvas?.drawPath(it, mDrawPaint)
        }
        canvas?.drawPath(mPath, mCurrentPaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if (it.pointerCount == 2) return false

            val touchX = it.x
            val touchY = it.y

            if (mDrawingMode == DrawingMode.DRAW || (mDrawingMode == DrawingMode.ERASER && mEraserMode == EraserMode.NORMAL)) {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> drawTouchStart(touchX, touchY)
                    MotionEvent.ACTION_MOVE -> drawTouchMove(touchX, touchY)
                    MotionEvent.ACTION_UP -> drawTouchUp()
                }
                invalidate()
                return true
            } else if (mDrawingMode == DrawingMode.ERASER && mEraserMode == EraserMode.STROKE) {
                when (it.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        mTouchX = touchX
                        mTouchY = touchY

                        getTouchedDrawPath(mTouchX, mTouchY)?.let { drawPath ->
                            mDrawPaths.remove(drawPath)

                            mDeletedPaths.add(drawPath)
                            addLastAction(UndoAction.REMOVE_PATH)
                        }
                    }
                }
                invalidate()
                return true
            }
        }
        return false
    }

    private fun drawTouchStart(x: Float, y: Float) {
        mPath.apply {
            reset()
            moveTo(x, y)
            addPathPoints(floatArrayOf(x, y))
        }
        mTouchX = x
        mTouchY = y
    }

    private fun drawTouchMove(x: Float, y: Float) {
        val dx = abs(x - mTouchX)
        val dy = abs(y - mTouchY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mTouchX, mTouchY, (x + mTouchX) / 2, (y + mTouchY) / 2)
            mPath.addPathPoints(
                floatArrayOf(
                    mTouchX,
                    mTouchY,
                    (x + mTouchX) / 2,
                    (y + mTouchY) / 2
                )
            )
            mTouchX = x
            mTouchY = y
        }
    }

    private fun drawTouchUp() {
        mPath.apply {
            lineTo(mTouchX, mTouchY)
            mDrawCanvas.drawPath(this, mCurrentPaint)
            pathColor = mCurrentPaint.color
            pathWidth = mCurrentPaint.strokeWidth
            opacity = mCurrentPaint.alpha
            porterDuffXfermode = mCurrentPaint.xfermode
            mDrawPaths.add(this)

            mAddedPaths.add(this)
            this@PaintView.addLastAction(UndoAction.ADD_PATH)
        }
        mPath = DrawPath()
    }

    private fun getTouchedDrawPath(x: Float, y: Float): DrawPath? {
        for (draw in mDrawPaths) {
            for (points in draw.pathPoints) {
                if (points.size > 2 && abs(points[0] - x) <= mCurrentPaint.strokeWidth && abs(points[1] - y) <= mCurrentPaint.strokeWidth) {
                    return draw
                }
            }
        }
        return null
    }

    companion object {
        const val DEFAULT_BRUSH_SIZE = 25f
        const val DEFAULT_ERASER_SIZE = 25f
        const val DEFAULT_OPACITY = 255

        const val TOUCH_TOLERANCE = 4

        private val TAG = PaintView::class.qualifiedName
    }
}

enum class DrawingMode {
    DRAW, TEXT, SELECT, ERASER
}

enum class EraserMode {
    NORMAL, STROKE
}

enum class UndoAction {
    ADD_TEXT, REMOVE_TEXT, ADD_PATH, REMOVE_PATH
}