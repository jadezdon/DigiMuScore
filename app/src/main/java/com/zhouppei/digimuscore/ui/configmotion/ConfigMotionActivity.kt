package com.zhouppei.digimuscore.ui.configmotion

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Size
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.utils.Constants
import com.zhouppei.digimuscore.ui.pageturner.MotionAnalyzer
import com.zhouppei.digimuscore.ui.pageturner.MotionConfig
import com.zhouppei.digimuscore.ui.pageturner.MotionListener
import com.zhouppei.digimuscore.ui.pageturner.MotionResult
import kotlinx.android.synthetic.main.activity_config_motion.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

class ConfigMotionActivity : AppCompatActivity() {

    private var mCameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var mCameraProvider: ProcessCameraProvider
    private lateinit var mImageAnalysis: ImageAnalysis
    private lateinit var mMotionAnalyzer: MotionAnalyzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_motion)

        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, Constants.REQUESTCODE_CAMERA_PERMISSION
            )
        }

        setupCamera()

        configMotion_button_back.setOnClickListener { finish() }
        configMotion_button_start.setOnClickListener { startCamera() }
        configMotion_button_stop.setOnClickListener { stopCamera() }

        setupLeftThreshold()
        setupRightThreshold()
    }

    private fun setupLeftThreshold() {
        configMotion_seekBar_leftThreshold.apply {
            min = abs(MotionConfig.MIN_HEAD_TILT_LEFT_THRESHOLD)
            max = abs(MotionConfig.MAX_HEAD_TILT_LEFT_THRESHOLD)
            progress = abs(
                PreferenceManager.getDefaultSharedPreferences(this@ConfigMotionActivity)
                    .getInt(Constants.SHARED_PREF_KEY_HEAD_TILT_LEFT_THRESHOLD, -23)
            )
            configMotion_leftThresholdValue.text = progress.toString()
            setOnSeekBarChangeListener(mSeekBarChangeListener)
        }

        configMotion_leftThreshold_help.setOnClickListener {
            Snackbar.make(it, "The max angle the head needs to tilt left to turn page.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupRightThreshold() {
        configMotion_seekBar_rightThreshold.apply {
            min = MotionConfig.MIN_HEAD_TILT_RIGHT_THRESHOLD
            max = MotionConfig.MAX_HEAD_TILT_RIGHT_THRESHOLD
            progress = PreferenceManager.getDefaultSharedPreferences(this@ConfigMotionActivity)
                .getInt(Constants.SHARED_PREF_KEY_HEAD_TILT_RIGHT_THRESHOLD, 23)
            configMotion_rightThresholdValue.text = progress.toString()
            setOnSeekBarChangeListener(mSeekBarChangeListener)
        }

        configMotion_rightThreshold_help.setOnClickListener {
            Snackbar.make(it, "The max angle the head needs to tilt right to turn page.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private val mSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            seekBar?.let {
                when (it) {
                    configMotion_seekBar_leftThreshold -> {
                        mMotionAnalyzer.setLeftThreshold(-progress)
                        configMotion_leftThresholdValue.text = progress.toString()
                    }
                    configMotion_seekBar_rightThreshold -> {
                        mMotionAnalyzer.setRightThreshold(progress)
                        configMotion_rightThresholdValue.text = progress.toString()
                    }
                }
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            seekBar?.let {
                when (it) {
                    configMotion_seekBar_leftThreshold -> {
                        with(PreferenceManager.getDefaultSharedPreferences(this@ConfigMotionActivity).edit()) {
                            putInt(Constants.SHARED_PREF_KEY_HEAD_TILT_LEFT_THRESHOLD, -it.progress)
                            commit()
                        }
                    }
                    configMotion_seekBar_rightThreshold -> {
                        with(PreferenceManager.getDefaultSharedPreferences(this@ConfigMotionActivity).edit()) {
                            putInt(Constants.SHARED_PREF_KEY_HEAD_TILT_RIGHT_THRESHOLD, it.progress)
                            commit()
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        if (requestCode == Constants.REQUESTCODE_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                setupCamera()
            } else {
                Toast.makeText(
                    this, "Without camera permission scan function cannot be used", Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("RestrictedApi")
    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            Runnable {
                mCameraProvider = cameraProviderFuture.get()
                mMotionAnalyzer = getMotionAnalyzer()
                mImageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setDefaultResolution(Size(1280, 960))
                    .build().also {
                        it.setAnalyzer(cameraExecutor, mMotionAnalyzer)
                    }
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun getMotionAnalyzer(): MotionAnalyzer {
        val motionConfig = MotionConfig()
        motionConfig.motionType =
            PreferenceManager.getDefaultSharedPreferences(this)
                .getString(Constants.SHARED_PREF_KEY_MOTIONTYPE, "head_tilt") ?: "head_tilt"
        motionConfig.leftThreshold = PreferenceManager.getDefaultSharedPreferences(this)
            .getInt(Constants.SHARED_PREF_KEY_HEAD_TILT_LEFT_THRESHOLD, -23)
        motionConfig.rightThreshold = PreferenceManager.getDefaultSharedPreferences(this)
            .getInt(Constants.SHARED_PREF_KEY_HEAD_TILT_LEFT_THRESHOLD, 23)

        return MotionAnalyzer(
            motionConfig,
            object : MotionListener {
                override fun onResult(motionResult: MotionResult, bitmap: Bitmap) {
                    runOnUiThread {
                        configMotion_face_not_detected.visibility =
                            if (motionResult == MotionResult.NOFACE) View.VISIBLE else View.GONE
                        configMotion_cameraView.setImageBitmap(bitmap)
                        when (motionResult) {
                            MotionResult.LEFT -> Toast.makeText(baseContext, "LEFT", Toast.LENGTH_SHORT).show()
                            MotionResult.RIGHT -> Toast.makeText(baseContext, "RIGHT", Toast.LENGTH_SHORT).show()
                            MotionResult.NONE -> {
                            }
                            MotionResult.NOFACE -> {
                            }
                        }
                    }
                }
            }
        )
    }

    private fun startCamera() {
        if (this::mCameraProvider.isInitialized) {
            mCameraProvider.unbindAll()
            mCameraProvider.bindToLifecycle(this, mCameraSelector, mImageAnalysis)
            configMotion_button_start.visibility = View.GONE
            configMotion_button_stop.visibility = View.VISIBLE
            configMotion_cameraView.visibility = View.VISIBLE
        }
    }

    private fun stopCamera() {
        if (this::mCameraProvider.isInitialized) {
            mCameraProvider.unbindAll()
            configMotion_button_start.visibility = View.VISIBLE
            configMotion_button_stop.visibility = View.GONE
            configMotion_cameraView.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

}