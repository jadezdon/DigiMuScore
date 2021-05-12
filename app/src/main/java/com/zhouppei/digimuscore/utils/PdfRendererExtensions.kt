package com.zhouppei.digimuscore.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer

fun PdfRenderer.Page.renderAndClose(width: Int) = use {
    val bitmap = createBitmap(width)
    render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
    bitmap
}

fun PdfRenderer.Page.createBitmap(bitmapWidth: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(
        bitmapWidth, (bitmapWidth.toFloat() / width * height).toInt(), Bitmap.Config.ARGB_8888
    )

    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.WHITE)
    canvas.drawBitmap(bitmap, 0f, 0f, null)

    return bitmap
}

inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            close()
        } catch (closeException: Exception) {
            e.addSuppressed(closeException)
        }
        throw e
    } finally {
        if (!closed) {
            close()
        }
    }
}

fun getPageHeight(renderer: PdfRenderer) : Int {
    val page = renderer.openPage(0)
    val height = page.height
    page.close()
    return height
}