package com.zhouppei.digimuscore.ui.pageturner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.databinding.FragmentPageTurnerBinding
import com.zhouppei.digimuscore.utils.Constants
import kotlinx.android.synthetic.main.fragment_page_turner.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class PageTurnerFragment : Fragment() {

    private val mArgs: PageTurnerFragmentArgs by navArgs()

    @Inject
    lateinit var mViewModelFactory: PageTurnerViewModel.PageTurnerAssistedFactory
    private val mViewModel: PageTurnerViewModel by viewModels {
        PageTurnerViewModel.provideFactory(
            mViewModelFactory,
            mArgs.sheetMusicId
        )
    }

    private var mCameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var mCameraProvider: ProcessCameraProvider
    private lateinit var mImageAnalyzer: ImageAnalysis

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (allPermissionsGranted()) {
            setupAndStartCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, Constants.REQUESTCODE_CAMERA_PERMISSION
            )
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        if (requestCode == Constants.REQUESTCODE_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                setupAndStartCamera()
            } else {
                Toast.makeText(
                    context, "Without camera permission scan function cannot be used", Toast.LENGTH_SHORT
                ).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentPageTurnerBinding>(
            inflater,
            R.layout.fragment_page_turner,
            container,
            false
        ).apply {
            viewmodel = mViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        mViewModel.sheetMusicPages.observe(viewLifecycleOwner, Observer {
            mViewModel.currentPage.postValue(it.first())
        })

        return binding.root
    }

    @SuppressLint("RestrictedApi")
    private fun setupAndStartCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            Runnable {
                mCameraProvider = cameraProviderFuture.get()
                mImageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setDefaultResolution(Size(1280, 960))
                    .build().also {
                        it.setAnalyzer(cameraExecutor, getMotionAnalyzer())
                    }
                startCamera()
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun getMotionAnalyzer(): MotionAnalyzer {
        val motionConfig = MotionConfig()
        motionConfig.motionType =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Constants.SHARED_PREF_KEY_MOTIONTYPE, "head_tilt") ?: "head_tilt"
        motionConfig.leftThreshold = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(Constants.SHARED_PREF_KEY_HEAD_TILT_LEFT_THRESHOLD, -23)
        motionConfig.rightThreshold = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(Constants.SHARED_PREF_KEY_HEAD_TILT_RIGHT_THRESHOLD, 23)

        return MotionAnalyzer(
            motionConfig,
            object : MotionListener {
                override fun onResult(motionResult: MotionResult, bitmap: Bitmap) {
                    mViewModel.isFaceDetected.postValue(motionResult == MotionResult.NOFACE)
                    when (motionResult) {
                        MotionResult.LEFT -> showPrevPage()
                        MotionResult.RIGHT -> showNextPage()
                        MotionResult.NONE -> {
                        }
                        MotionResult.NOFACE -> {
                        }
                    }
                }
            }
        )
    }

    private fun startCamera() {
        if (this::mCameraProvider.isInitialized) {
            mCameraProvider.unbindAll()
            mCameraProvider.bindToLifecycle(viewLifecycleOwner, mCameraSelector, mImageAnalyzer)
        }
    }

    private fun stopCamera() {
        if (this::mCameraProvider.isInitialized) {
            mCameraProvider.unbindAll()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        pageTurner_button_back.setOnClickListener {
            stopCamera()
            findNavController().navigateUp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun showPrevPage() {
        mViewModel.sheetMusicPages.value?.let { pageList ->
            mViewModel.currentPage.value?.let { page ->
                pageList.find {
                    it.pageNumber == (page.pageNumber - 1)
                }?.let {
                    mViewModel.currentPage.postValue(it)
                }
            }
        }
    }

    private fun showNextPage() {
        mViewModel.sheetMusicPages.value?.let { pageList ->
            mViewModel.currentPage.value?.let { page ->
                pageList.find {
                    it.pageNumber == (page.pageNumber + 1)
                }?.let {
                    mViewModel.currentPage.postValue(it)
                }
            }
        }
    }

    companion object {
        private val TAG = PageTurnerFragment::class.qualifiedName
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}