package com.zhouppei.digimuscore.ui.notation.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhouppei.digimuscore.databinding.BottomsheetNotationDrawBinding
import com.zhouppei.digimuscore.notation.PenType
import com.zhouppei.digimuscore.ui.notation.SharedViewModel
import com.zhouppei.digimuscore.ui.notation.colorpicker.ColorPickerDialog
import com.zhouppei.digimuscore.ui.notation.colorpicker.ColorPickerDialogListener
import kotlinx.android.synthetic.main.bottomsheet_notation_draw.*

class DrawBottomSheetFragment : BottomSheetDialogFragment() {

    private val mSharedViewModel by activityViewModels<SharedViewModel>()
    private lateinit var mColorPickerDialog: ColorPickerDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = BottomsheetNotationDrawBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            sharedViewModel = mSharedViewModel
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSharedViewModel.penType.observe(viewLifecycleOwner, Observer {
            drawBottomSheet_button_pen.isChecked = (it == PenType.PEN)
            drawBottomSheet_button_highlighter.isChecked = (it == PenType.HIGHLIGHTER)
        })

        mSharedViewModel.penSize.observe(viewLifecycleOwner, Observer {
            drawBottomSheet_spinner_penSize.setSelection(mSharedViewModel.penSizeList.indexOf(it))
        })

        drawBottomSheet_button_pen.setOnClickListener {
            mSharedViewModel.penType.postValue(PenType.PEN)
        }

        drawBottomSheet_button_highlighter.setOnClickListener {
            mSharedViewModel.penType.postValue(PenType.HIGHLIGHTER)
        }

        drawBottomSheet_spinner_penSize.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    mSharedViewModel.penSize.postValue(mSharedViewModel.penSizeList[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        drawBottomSheet_button_pickColor.setOnClickListener {
            mColorPickerDialog = ColorPickerDialog(
                requireContext(),
                mSharedViewModel.penColor.value!!,
                object : ColorPickerDialogListener {
                    override fun setSelectedColorString(colorString: String) {
                        mSharedViewModel.penColor.postValue(colorString)
                    }
                }
            )
            mColorPickerDialog.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mColorPickerDialog.isInitialized)
            mColorPickerDialog.dismiss()
    }
}