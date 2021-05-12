package com.zhouppei.digimuscore.ui.pageturner

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import com.zhouppei.digimuscore.utils.flipX
import com.zhouppei.digimuscore.utils.rotate
import com.zhouppei.digimuscore.utils.toBitmap
import java.util.*
import kotlin.math.abs

class MotionAnalyzer(
    private val motionConfig: MotionConfig,
    private val listener: MotionListener
) : ImageAnalysis.Analyzer {
    private val detector: FaceDetector
    private val lastFrames: Queue<Face> = LinkedList<Face>()
    private var faceId: Int = -1
    private var lastDetectedTime: Long = -1

    private val DEBUGGING = true

    companion object {
        private const val MAX_TOTAL_FRAMES_SIZE = 10
        private const val MIN_DETECTED_ELAPSED_SECONDS = 3
    }

    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setMinFaceSize(0.15f)
            .enableTracking()
            .build()

        detector = FaceDetection.getClient(options)
    }

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let {
            val inputImage = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    val bitmap = it.toBitmap().copy(Bitmap.Config.ARGB_8888, true).rotate(90f)
                    processFaceList(faces, bitmap)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        strokeWidth = 1.0f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
    }

    private fun processFaceList(faces: List<Face>, bitmap: Bitmap) {
        val bitmapWithPicture = bitmap.flipX()
        val bitmapOnlyContour =
            Bitmap.createBitmap(bitmapWithPicture.width, bitmapWithPicture.height, Bitmap.Config.ARGB_8888)

        if (faces.isEmpty()) {
            listener.onResult(MotionResult.NOFACE, bitmapWithPicture)
            return
        }

        var largestFace = faces.first()
        faces.forEach {
            if (it.boundingBox.width() * it.boundingBox.height() > largestFace.boundingBox.width() * largestFace.boundingBox.height()) {
                largestFace = it
            }
        }

        largestFace.trackingId?.let {
            if (it != faceId) lastFrames.clear()
            faceId = it
        }

        lastFrames.add(largestFace)
        if (lastFrames.size > MAX_TOTAL_FRAMES_SIZE) lastFrames.poll()

        if (DEBUGGING) {
//            https://developers.google.com/ml-kit/vision/face-detection/images/face_contours.svg

            val canvas = Canvas(bitmapWithPicture)
            lastFrames.forEach { face ->
                face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points?.first()?.let { point ->
                    canvas.drawCircle(bitmap.width - point.x, point.y, 2.0f, dotPaint)
                }
                face.getContour(FaceContour.UPPER_LIP_BOTTOM)?.points?.last()?.let { point ->
                    canvas.drawCircle(bitmap.width - point.x, point.y, 2.0f, dotPaint)
                }
                face.getContour(FaceContour.FACE)?.points?.first()?.let { point ->
                    canvas.drawCircle(bitmap.width - point.x, point.y, 2.0f, dotPaint)
                }
            }

            canvas.drawText(
                "x = ${largestFace.headEulerAngleX}, y = ${largestFace.headEulerAngleY}, z = ${largestFace.headEulerAngleZ}",
                10f,
                100f,
                textPaint
            )

            largestFace.getContour(FaceContour.FACE)?.points?.let {
                canvas.drawLine(bitmap.width - it[0].x, it[0].y, bitmap.width - it[18].x, it[18].y, dotPaint)
            }
//            largestFace.allContours.forEach {
//                it.points.forEachIndexed { index, point ->
//                    if (index + 1 < it.points.size) {
//                        canvas.drawLine(
//                            bitmap.width - it.points[index].x,
//                            it.points[index].y,
//                            bitmap.width - it.points[index + 1].x,
//                            it.points[index + 1].y,
//                            dotPaint
//                        )
//                    }
//                }
//            }

        }

        // headleft z < 0 (-25...-30), headright z > 0 (25...30)
        // down x < 0 (-18...-20), up x > 0 (18...20)
        // faceleft y < 0 (20...40), faceright y > 0 (-20...-40)
        if (lastDetectedTime > 0 && (System.nanoTime() - lastDetectedTime) / 1_000_000_000.0 < MIN_DETECTED_ELAPSED_SECONDS) {
            listener.onResult(MotionResult.NONE, bitmapWithPicture)
            return
        }

        val motionResult = when (motionConfig.motionType) {
            "head_tilt" -> detectHeadMotion(largestFace)
            "mouth_movement" -> detectMouthMotion(largestFace)
            else -> MotionResult.NONE
        }
        if (motionResult == MotionResult.RIGHT || motionResult == MotionResult.LEFT) {
            lastDetectedTime = System.nanoTime()
        }

        listener.onResult(motionResult, bitmapWithPicture)
    }

    private fun detectHeadMotion(largestFace: Face): MotionResult {
        if (abs(largestFace.headEulerAngleY) > 10f || abs(largestFace.headEulerAngleX) > 10f) return MotionResult.NONE

        if (largestFace.headEulerAngleZ > motionConfig.rightThreshold) {
            return MotionResult.RIGHT
        }
        if (largestFace.headEulerAngleZ < motionConfig.leftThreshold) {
            return MotionResult.LEFT
        }
        return MotionResult.NONE
    }

    private fun detectMouthMotion(largestFace: Face): MotionResult {


        return MotionResult.NONE
    }

    fun setLeftThreshold(value: Int) {
        motionConfig.leftThreshold = value
    }

    fun setRightThreshold(value: Int) {
        motionConfig.rightThreshold = value
    }
}

enum class MotionResult {
    LEFT, RIGHT, NONE, NOFACE
}

interface MotionListener {
    fun onResult(motionResult: MotionResult, bitmap: Bitmap)
}

class MotionConfig {
    companion object {
        const val MIN_HEAD_TILT_LEFT_THRESHOLD = -13
        const val MAX_HEAD_TILT_LEFT_THRESHOLD = -30
        const val MIN_HEAD_TILT_RIGHT_THRESHOLD = 13
        const val MAX_HEAD_TILT_RIGHT_THRESHOLD = 30
    }

    var motionType: String = ""
    var leftThreshold = 0
    var rightThreshold = 0
}