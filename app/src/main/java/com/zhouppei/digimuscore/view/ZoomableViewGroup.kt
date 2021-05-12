package com.zhouppei.digimuscore.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.math.abs
import kotlin.math.sqrt


class ZoomableViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    companion object {
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2

        private const val MIN_ZOOM = 0.8f
        private const val MAX_ZOOM = 6f

        private val TAG = ZoomableViewGroup::class.qualifiedName
    }

    private var mMatrix = Matrix()
    private var mMatrixInverse = Matrix()
    private var mMatrixSaved = Matrix()

    private var mMatrixValues = FloatArray(9)

    private var mMode = NONE

    private var mDragStartPoint = PointF()
    private var mMidPoint = PointF()
    private var mOldDistance = 1f

    private var mDispatchEventLocationPoints = FloatArray(2)
    private var mEventLocationPoints = FloatArray(2)

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let { motionEvent ->
            mDispatchEventLocationPoints[0] = motionEvent.x
            mDispatchEventLocationPoints[1] = motionEvent.y
            mDispatchEventLocationPoints = screenPointsToScaledPoints(mDispatchEventLocationPoints)
            motionEvent.setLocation(
                mDispatchEventLocationPoints[0],
                mDispatchEventLocationPoints[1]
            )
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mMatrix.getValues(mMatrixValues)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        canvas?.let {
            it.save()
            it.setMatrix(mMatrix)
            super.dispatchDraw(canvas)
            it.restore()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { motionEvent ->
            mEventLocationPoints[0] = motionEvent.x
            mEventLocationPoints[1] = motionEvent.y
            mEventLocationPoints = scaledPointsToScreenPoints(mEventLocationPoints)

            motionEvent.setLocation(mEventLocationPoints[0], mEventLocationPoints[1])

            when (motionEvent.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (motionEvent.pointerCount != 2) return false

                    mOldDistance = getSpacing(motionEvent)
                    mMatrixSaved.set(mMatrix)
                    setMidPoint(mMidPoint, motionEvent)
                    mDragStartPoint.set(motionEvent.x, motionEvent.y)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (motionEvent.pointerCount != 2) return false

                    val newDistance = getSpacing(motionEvent)
                    setMidPoint(mMidPoint, motionEvent)
                    when {
                        abs(newDistance - mOldDistance) < 100f -> {
                            mMatrix.set(mMatrixSaved)
                            val dx = motionEvent.x - mDragStartPoint.x
                            val dy = motionEvent.y - mDragStartPoint.y
                            mMatrix.postTranslate(dx, dy)
                            mMatrix.invert(mMatrixInverse)
                        }
                        mOldDistance > 10f && newDistance > 10f -> {
                            mMatrix.set(mMatrixSaved)
                            var scale = newDistance / mOldDistance
                            val values = FloatArray(9)
                            mMatrix.getValues(values)
                            if (scale * values[Matrix.MSCALE_X] >= MAX_ZOOM) {
                                scale = MAX_ZOOM / values[Matrix.MSCALE_X]
                            }
                            if (scale * values[Matrix.MSCALE_X] <= MIN_ZOOM) {
                                scale = MIN_ZOOM / values[Matrix.MSCALE_X]
                            }
                            mMatrix.postScale(scale, scale, mMidPoint.x, mMidPoint.y)
                            mMatrix.invert(mMatrixInverse)
                        }
                        else -> {
                        }
                    }
                }
                else -> {
                }
            }
        }
        invalidate()
        return true
    }

    private fun scaledPointsToScreenPoints(array: FloatArray): FloatArray {
        mMatrix.mapPoints(array)
        return array
    }

    private fun screenPointsToScaledPoints(array: FloatArray): FloatArray {
        mMatrixInverse.mapPoints(array)
        return array
    }

    private fun getSpacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return (sqrt(x * x + y * y))
    }

    private fun setMidPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2f, y / 2f)
    }
}