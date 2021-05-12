package com.zhouppei.digimuscore.utils

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.pdf.PdfRenderer
import android.media.Image
import android.os.ParcelFileDescriptor
import android.text.StaticLayout
import android.text.TextPaint
import android.util.DisplayMetrics
import com.zhouppei.digimuscore.notation.DrawLayer
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer


object BitmapUtil {

    @JvmStatic
    fun getBitmapFromUri(uri: String, width: Int, height: Int): Bitmap {
        val image = File(uri)
        val bitmap: Bitmap

        if (image.exists()) {
            val bmOptions = BitmapFactory.Options()
            bmOptions.inScaled = false
            bitmap = BitmapFactory.decodeFile(image.absolutePath, bmOptions)
//            bitmap = Bitmap.createScaledBitmap(tempBitmap, width, height, false)
        } else {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.TRANSPARENT)
        }
        return bitmap
    }

    // https://stackoverflow.com/a/40511674/14246093
    @JvmStatic
    fun pdfToBitmap(pdfFile: File): ArrayList<Bitmap>? {
        val bitmaps: ArrayList<Bitmap> = ArrayList()
        try {
            val renderer =
                PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY))
            var bitmap: Bitmap
            val pageCount = renderer.pageCount
            for (i in 0 until pageCount) {
                val page = renderer.openPage(i)

                bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmaps.add(bitmap)

                page.close()
            }

            renderer.close()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return bitmaps
    }

    @JvmStatic
    fun saveBitmapToFile(bitmap: Bitmap, fileName: String) {
        try {
            val outputStream = FileOutputStream(fileName)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    @JvmStatic
    fun drawLayerToBitmap(layer: DrawLayer): Bitmap {
        val bitmap = Bitmap.createBitmap(layer.pageWidth, layer.pageHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val drawPaint = Paint()
        layer.drawPaths.forEach {
            it.loadPathPointsAsQuadTo()
            it.setupPaint(drawPaint)
            canvas.drawPath(it, drawPaint)
        }

        val textPaint = TextPaint()
        val textBounds = Rect()
        layer.drawTexts.forEach {
            it.setupTextPaint(textPaint)
            // convert sp to px
            textPaint.textSize = it.getTextSizeInPx()
            textPaint.getTextBounds(it.content, 0, it.content.length, textBounds)

            canvas.save()
            canvas.translate(it.coordinateX, it.coordinateY)
            StaticLayout.Builder.obtain(
                it.content, 0, it.content.length, textPaint, textBounds.width() + 20
            ).build().draw(canvas)
            canvas.restore()
        }
        return bitmap
    }

    @JvmStatic
    fun getPageSize(bitmap: Bitmap): Pair<Int, Int> {
        // A4 size paper width in px
        val pageWidth = (210 * DisplayMetrics.DENSITY_MEDIUM * (1f / 25.4)).toInt()
        val scale = pageWidth / bitmap.width.toFloat()
        val pageHeight = (bitmap.height * scale).toInt()
        return Pair(pageWidth, pageHeight)
    }

    @JvmStatic
    fun getBitmap(buffer: ByteBuffer, width: Int, height: Int): Bitmap {
        buffer.rewind()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.flipX(): Bitmap {
    val matrix = Matrix().apply { preScale(1.0f, -1.0f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.flipY(): Bitmap {
    val matrix = Matrix().apply { preScale(-1.0f, 1.0f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer // Y
    val vuBuffer = planes[2].buffer // VU

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)

    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(
        android.graphics.Rect(0, 0, yuvImage.width, yuvImage.height),
        50,
        out
    )
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}