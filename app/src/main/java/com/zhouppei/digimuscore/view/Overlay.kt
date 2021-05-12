package com.zhouppei.digimuscore.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class Overlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var previewWidth: Int = 0
    private var previewHeight: Int = 0
    private var scale = 1.0f
    private var offsetY = 1.0f
    private var rectPoints = mutableListOf<PointF>()
    private var touchX = 0f
    private var touchY = 0f
    private var touchedCircleIndex = -1
    private var isTouchedEnabled = false

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 5.0f
    }

    fun setRectPoints(points: FloatArray) {
        rectPoints.clear()
        for (index in points.indices step 2) {
            rectPoints.add(PointF(translateX(points[index]), translateY(points[index + 1])))
        }
        postInvalidate()
    }

    fun clearRectPoints() {
        rectPoints.clear()
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawRectBorder(canvas)
        drawRectCorners(canvas)
    }

    fun setIsTouchEnabled(isEnabled: Boolean) {
        isTouchedEnabled = isEnabled
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isTouchedEnabled) return false
        event?.let { ev ->
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchX = ev.x
                    touchY = ev.y
                    touchedCircleIndex = -1
                    for (i in rectPoints.indices) {
                        if (abs(rectPoints[i].x - touchX) < CIRCLE_RADIUS && abs(rectPoints[i].y - touchY) < CIRCLE_RADIUS) {
                            touchedCircleIndex = i
                            break
                        }
                    }

                    if (touchedCircleIndex == -1) return false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (touchedCircleIndex != -1) {
                        val dx = ev.x - touchX
                        val dy = ev.y - touchY
                        rectPoints[touchedCircleIndex].x += dx
                        rectPoints[touchedCircleIndex].y += dy
                        invalidate()
                        touchX = ev.x
                        touchY = ev.y
                    }
                }
                else -> {
                    return false
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    fun setPreviewSize(previewSize: Size, overlaySize: Size) {
        previewWidth = previewSize.width
        previewHeight = previewSize.height
        scale = overlaySize.width / previewWidth.toFloat()
        offsetY =
            (overlaySize.height - previewHeight.toFloat()) / (scale + overlaySize.height / previewHeight.toFloat() + 0.5f)
    }

    private fun drawRectCorners(canvas: Canvas) {
        if (rectPoints.isNotEmpty()) {
            for (i in 0 until 4) {
                canvas.drawCircle(rectPoints[i].x, rectPoints[i].y, CIRCLE_RADIUS, paint)
            }
        }
    }

    fun getRecPoints(): FloatArray {
        return rectPoints.toFloatArray()
    }

    private fun drawRectBorder(canvas: Canvas) {
        if (rectPoints.isNotEmpty()) {
            canvas.drawLine(
                rectPoints[0].x,
                rectPoints[0].y,
                rectPoints[1].x,
                rectPoints[1].y,
                paint
            )  // 0 1
            canvas.drawLine(
                rectPoints[0].x,
                rectPoints[0].y,
                rectPoints[2].x,
                rectPoints[2].y,
                paint
            )  // 0 2
            canvas.drawLine(
                rectPoints[1].x,
                rectPoints[1].y,
                rectPoints[3].x,
                rectPoints[3].y,
                paint
            )  // 1 3
            canvas.drawLine(
                rectPoints[2].x,
                rectPoints[2].y,
                rectPoints[3].x,
                rectPoints[3].y,
                paint
            )  // 2 3
        }
    }

    private fun translateX(x: Float): Float = x * scale
    private fun translateY(y: Float): Float = y * scale - offsetY

    companion object {
        private val TAG = Overlay::class.qualifiedName
        private const val CIRCLE_RADIUS = 30.0f
    }
}

fun MutableList<PointF>.toFloatArray(): FloatArray {
    val arr = FloatArray(size * 2)
    forEachIndexed { i, e ->
        arr[2 * i] = e.x
        arr[2 * i + 1] = e.y
    }
    return arr
}