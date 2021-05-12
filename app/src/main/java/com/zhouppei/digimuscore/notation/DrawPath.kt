package com.zhouppei.digimuscore.notation

import android.graphics.*
import java.io.Serializable

class DrawPath() : Path(), Serializable {

    var pathPoints: ArrayList<FloatArray> = ArrayList()
    var pathColor: Int = Color.BLACK
    var pathWidth: Float = 25.0f
    var opacity: Int = 255
    var porterDuffXfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    fun addPathPoints(points: FloatArray) {
        pathPoints.add(points)
    }

    fun loadPathPointsAsQuadTo() {
        if (pathPoints.size > 0) {
            val initPoints = pathPoints[0]
            moveTo(initPoints[0], initPoints[1])
            for ((i, pointSet) in pathPoints.withIndex()) {
                if (i == 0) continue
                quadTo(pointSet[0], pointSet[1], pointSet[2], pointSet[3])
            }
        }
    }

    fun setupPaint(paint: Paint) {
        paint.apply {
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            xfermode = porterDuffXfermode
            color = pathColor
            strokeWidth = pathWidth
            alpha = opacity
        }
    }
}
