package com.zhouppei.digimuscore.ui.scanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Size
import android.view.Surface
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.utils.BitmapUtil
import com.zhouppei.digimuscore.utils.Constants
import com.zhouppei.digimuscore.utils.FileUtil
import com.zhouppei.digimuscore.utils.toBitmap
import kotlinx.android.synthetic.main.activity_scanner.*
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {

    private var mCameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var mCameraProvider: ProcessCameraProvider
    private lateinit var mImageAnalyzer: ImageAnalysis
    private lateinit var mPreview: Preview

    private lateinit var cameraExecutor: ExecutorService
    private var mImageMat: Mat = Mat()
    private var mOriginDocumentMat: Mat = Mat()
    private var mDocumentMat: Mat = Mat()
    private lateinit var mDocumentBitmap: Bitmap
    private var mDocumentPoints = FloatArray(8)
    private var mCurrentMode = MODE_CAPTURE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_scanner)

        if (allPermissionsGranted()) {
            setupCamera()
            setCurrentMode(MODE_CAPTURE)
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, Constants.REQUESTCODE_CAMERA_PERMISSION
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        scanner_button_back.setOnClickListener {
            finish()
        }
        scanner_fab_capture.setOnClickListener {
            setDocumentPoints()
        }

        scanner_button_cancel.setOnClickListener {
            scanner_preview_overlay.clearRectPoints()
            scanner_image_overlay.clearRectPoints()
            setCurrentMode(MODE_CAPTURE)
        }

        scanner_button_ok.setOnClickListener {
            if (mCurrentMode == MODE_EDIT_SCANNED_DOCUMENT) {
                saveAndExit()
            } else if (mCurrentMode == MODE_SET_DOCUMENT_POINTS) {
                setScannedDocument()
            }
        }

        setupImageTools()
    }

    private fun setupImageTools() {
        scanner_button_clear.setOnClickListener {
            mDocumentMat = mOriginDocumentMat.clone()
            displayImageFromMat()
        }
        scanner_button_rotate.setOnClickListener {
            rotateMat(mDocumentMat.nativeObjAddr)
            displayImageFromMat()
        }
        scanner_button_sharpen.setOnClickListener {
            sharpenMat(mDocumentMat.nativeObjAddr)
            displayImageFromMat()
        }
        scanner_button_whiteBlack.setOnClickListener {
            convertToBlackWhite(mDocumentMat.nativeObjAddr)
            // to fix when save bitmap becomes empty
            grayscale(mDocumentMat.nativeObjAddr)
            displayImageFromMat()
        }
        scanner_button_greyScale.setOnClickListener {
            grayscale(mDocumentMat.nativeObjAddr)
            displayImageFromMat()
        }
    }

    private fun displayImageFromMat() {
        mDocumentBitmap = Bitmap.createBitmap(mDocumentMat.cols(), mDocumentMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mDocumentMat, mDocumentBitmap)
        scanner_image.setImageBitmap(mDocumentBitmap)
    }

    private fun saveAndExit() {
        val intent = Intent().apply {
            val destinationFile = FileUtil.createOrGetFile(
                applicationContext,
                "Sheetmusics",
                "${FileUtil.getFileNameNoExt()}.png"
            )
            BitmapUtil.saveBitmapToFile(mDocumentBitmap, destinationFile.path.toString())
            putExtra("contentUri", destinationFile.path.toString())
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setCurrentMode(mode: Int) {
        mCurrentMode = mode

        scanner_camera_mode_layout.visibility = if (mode == MODE_CAPTURE) View.VISIBLE else View.GONE
        scanner_edit_mode_layout.visibility = if (mode != MODE_CAPTURE) View.VISIBLE else View.GONE
        scanner_edit_buttons_layout.visibility = if (mode == MODE_EDIT_SCANNED_DOCUMENT) View.VISIBLE else View.GONE

        if (mode == MODE_CAPTURE) startCamera() else stopCamera()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        if (requestCode == Constants.REQUESTCODE_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                setupCamera()
                setCurrentMode(MODE_CAPTURE)
            } else {
                Toast.makeText(
                    this, "Without camera permission scan function cannot be used", Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            Runnable {
                mCameraProvider = cameraProviderFuture.get()
                mPreview = Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .build().also {
                        it.setSurfaceProvider(scanner_previewView.surfaceProvider)
                    }
                mImageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().also {
                        it.setAnalyzer(cameraExecutor, DocumentAnalyzer())
                    }

                startCamera()
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun startCamera() {
        if (this::mCameraProvider.isInitialized) {
            mCameraProvider.unbindAll()
            mCameraProvider.bindToLifecycle(this, mCameraSelector, mImageAnalyzer, mPreview)
        }
    }

    private fun stopCamera() {
        if (this::mCameraProvider.isInitialized) {
            mCameraProvider.unbindAll()
        }
    }

    private fun setDocumentPoints() {
        scanner_previewView.bitmap?.let {
            Utils.bitmapToMat(it, mImageMat)
            setCurrentMode(MODE_SET_DOCUMENT_POINTS)
            scanner_image.setImageBitmap(it)
        }
        scanner_image_overlay.apply {
            setIsTouchEnabled(true)
            setPreviewSize(
                Size(480, 640),
                Size(mImageMat.cols(), mImageMat.rows())
            )
            setRectPoints(mDocumentPoints)
        }
    }

    private fun setScannedDocument() {
        mDocumentPoints = scanner_image_overlay.getRecPoints()
        scanner_image_overlay.clearRectPoints()
        setScannedDocumentMat(mImageMat.nativeObjAddr, mOriginDocumentMat.nativeObjAddr, mDocumentPoints)
        mDocumentMat = mOriginDocumentMat.clone()
        displayImageFromMat()
        setCurrentMode(MODE_EDIT_SCANNED_DOCUMENT)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        mDocumentMat.release()
    }

    inner class DocumentAnalyzer : ImageAnalysis.Analyzer {

        @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
        override fun analyze(image: ImageProxy) {
            Utils.bitmapToMat(image.image?.toBitmap(), mImageMat)

            mImageMat = fixMatRotation(mImageMat, image.imageInfo.rotationDegrees)

            val points = detectEdges(mImageMat.nativeObjAddr)
            if (arePointsValid(points)) {
                for (i in 0 until 8) {
                    mDocumentPoints[i] = points[i]
                }
            }
            scanner_preview_overlay.apply {
                setPreviewSize(
                    Size(mImageMat.cols(), mImageMat.rows()),
                    Size(scanner_previewView.width, scanner_previewView.height)
                )
                setRectPoints(points)
            }

            image.close()
        }

        private fun arePointsValid(points: FloatArray): Boolean {
            if (points.size < 8) return false
            for (point in points) {
                if (point.toInt() != 0) return true
            }
            return false
        }

        private fun fixMatRotation(matOrg: Mat, orientation: Int): Mat {
            val mat: Mat
            when (orientation) {
                Surface.ROTATION_0 -> {
                    mat = Mat(matOrg.cols(), matOrg.rows(), matOrg.type())
                    Core.transpose(matOrg, mat)
                    Core.flip(mat, mat, 1)
                }
                Surface.ROTATION_90 -> mat = matOrg
                Surface.ROTATION_270 -> {
                    mat = matOrg
                    Core.flip(mat, mat, -1)
                }
                else -> {
                    mat = Mat(matOrg.cols(), matOrg.rows(), matOrg.type())
                    Core.transpose(matOrg, mat)
                    Core.flip(mat, mat, 1)
                }
            }
            return mat
        }
    }

    private external fun detectEdges(matAddr: Long): FloatArray
    private external fun setScannedDocumentMat(matAddr: Long, matResultAddr: Long, points: FloatArray)
    private external fun sharpenMat(matAddr: Long)
    private external fun rotateMat(matAddr: Long)
    private external fun convertToBlackWhite(matAddr: Long)
    private external fun grayscale(matAddr: Long)

    companion object {
        private val TAG = ScannerActivity::class.qualifiedName
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        private const val MODE_CAPTURE = 1
        private const val MODE_SET_DOCUMENT_POINTS = 2
        private const val MODE_EDIT_SCANNED_DOCUMENT = 3

        init {
            System.loadLibrary("native-lib")
        }
    }
}