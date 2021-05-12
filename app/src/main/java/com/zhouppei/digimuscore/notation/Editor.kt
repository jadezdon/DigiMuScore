package com.zhouppei.digimuscore.notation


import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.zhouppei.digimuscore.view.DrawingMode
import com.zhouppei.digimuscore.view.EraserMode
import com.zhouppei.digimuscore.view.NotationView
import com.zhouppei.digimuscore.view.NotationViewListener

class Editor(private val context: Context?, private val notationView: NotationView) {

    fun setDrawingMode(drawingMode: DrawingMode) {
        notationView.setDrawingMode(drawingMode)
    }

    fun setOpacity(@IntRange(from = 0, to = 100) opacity: Int) {
        val opacityConverted = (opacity / 100.0 * 255.0).toInt()
        notationView.getPaintView().setOpacity(opacityConverted)
    }

    fun setBrushSize(size: Float) {
        notationView.getPaintView().setBrushSize(size)
    }

    fun setBrushColor(@ColorInt color: Int) {
        notationView.getPaintView().setBrushColor(color)
    }

    fun setEraserSize(size: Float) {
        notationView.getPaintView().setEraserSize(size)
    }

    fun setEraserMode(mode: EraserMode) {
        notationView.getPaintView().setEraserMode(mode)
    }

    fun clearAll() {
        notationView.getPaintView().clearAll()
    }

    fun undo(): Boolean {
        return notationView.undo()
    }

    fun changeTextSize(size: Int) {
        notationView.changeTextSize(size)
    }

    fun setDrawLayer(drawLayer: DrawLayer) {
        notationView.setDrawLayer(drawLayer)
    }

    fun getDrawLayer(drawLayer: DrawLayer) {
        notationView.getDrawLayer(drawLayer)
    }

    fun addMusicNote(value: DrawText) {
        notationView.addMusicNote(value)
    }

    fun setNotationViewListener(listener: NotationViewListener) {
        notationView.setNotationViewListener(listener)
    }

    fun setTextSize(size: Float) {
        notationView.setTextSize(size)
    }

    fun setTextColor(color: Int) {
        notationView.setTextColor(color)
    }

    fun setTextFontFamily(value: String) {
        notationView.setTextFontFamily(value)
    }

    fun setTextStyle(style: TextStyle) {
        notationView.setTextStyle(style)
    }
}