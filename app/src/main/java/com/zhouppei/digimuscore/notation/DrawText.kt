package com.zhouppei.digimuscore.notation


import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import com.zhouppei.digimuscore.utils.FontUtil
import java.io.Serializable

class DrawText(
    var content: String,
    var coordinateX: Float,
    var coordinateY: Float
): Serializable {

    var size = 20f
    var textColor = Color.BLACK
    var typefaceName: String = ""

    // convert sp to px
    var density: Float = 1f


    fun setX(value: Float) {
        coordinateX = value
    }

    fun setY(value: Float) {
        coordinateY = value
    }

    fun setupPaint(paint: Paint) {
        paint.apply {
            textSize = size
            color = textColor
            typeface = FontUtil.getTypeface(typefaceName)
        }
    }

    fun setupTextPaint(paint: TextPaint) {
        paint.apply {
            textSize = getTextSizeInPx()
            color = textColor
            typeface = FontUtil.getTypeface(typefaceName)
        }
    }

    fun getTextSizeInSp() = size
    fun getTextSizeInPx() = size * density
}