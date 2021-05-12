package com.zhouppei.digimuscore.ui.notation.bottomsheets

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhouppei.digimuscore.databinding.BottomsheetNotationEraserBinding
import com.zhouppei.digimuscore.ui.notation.SharedViewModel
import com.zhouppei.digimuscore.view.EraserMode
import kotlinx.android.synthetic.main.bottomsheet_notation_eraser.*

class EraserBottomSheetFragment : BottomSheetDialogFragment() {
    private val mSharedViewModel by activityViewModels<SharedViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = BottomsheetNotationEraserBinding.inflate(inflater, container, false)
        context ?: return binding.root

        binding.sharedViewModel = mSharedViewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSharedViewModel.eraserMode.observe(viewLifecycleOwner, Observer {
            if (it == EraserMode.NORMAL) {
                eraserBottomSheet_button_normalEraser.isChecked = true
            } else {
                eraserBottomSheet_button_strokeEraser.isChecked = true
            }
        })

        eraserBottomSheet_button_normalEraser.setOnClickListener {
            mSharedViewModel.eraserMode.postValue(EraserMode.NORMAL)
        }
        eraserBottomSheet_button_strokeEraser.setOnClickListener {
            mSharedViewModel.eraserMode.postValue(EraserMode.STROKE)
        }

        eraserBottomSheet_slider_eraserSize.setLabelFormatter {
            it.toString()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mSharedViewModel.eraserSize.postValue(eraserBottomSheet_slider_eraserSize.value)
    }
}