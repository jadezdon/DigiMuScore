package com.zhouppei.digimuscore.ui.notation.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zhouppei.digimuscore.databinding.BottomsheetNotationTextBinding
import com.zhouppei.digimuscore.notation.TextStyle
import com.zhouppei.digimuscore.ui.notation.SharedViewModel
import com.zhouppei.digimuscore.ui.notation.colorpicker.ColorPickerDialog
import com.zhouppei.digimuscore.ui.notation.colorpicker.ColorPickerDialogListener
import com.zhouppei.digimuscore.utils.FontUtil
import kotlinx.android.synthetic.main.bottomsheet_notation_text.*

class TextBottomSheetFragment : BottomSheetDialogFragment() {

    private val mSharedViewModel by activityViewModels<SharedViewModel>()

    private lateinit var mColorPickerDialog: ColorPickerDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = BottomsheetNotationTextBinding.inflate(inflater, container, false).apply {
            sharedViewModel = mSharedViewModel
            lifecycleOwner = viewLifecycleOwner
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fontSet = mutableSetOf<String>()
        for (f in FontUtil.typefaceMap.keys) {
            fontSet.add(f.substringBeforeLast("-"))
        }
        mSharedViewModel.textFontFamilyList = fontSet.toMutableList()

        mSharedViewModel.textSize.observe(viewLifecycleOwner, Observer {
            textBottomSheet_spinner_textSize.setSelection(mSharedViewModel.textSizeList.indexOf(it))
        })

        mSharedViewModel.textStyle.observe(viewLifecycleOwner, Observer {
            textBottomSheet_button_textNormal.isSelected = (it == TextStyle.NORMAL)
            textBottomSheet_button_textBold.isSelected = (it == TextStyle.BOLD)
            textBottomSheet_button_textItalic.isSelected = (it == TextStyle.ITALIC)
        })

        setupListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::mColorPickerDialog.isInitialized)
            mColorPickerDialog.dismiss()
    }

    private fun setupListeners() {
        textBottomSheet_spinner_fontFamily.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    mSharedViewModel.textFontFamily.postValue(mSharedViewModel.textFontFamilyList[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        textBottomSheet_spinner_textSize.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    mSharedViewModel.textSize.postValue(mSharedViewModel.textSizeList[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        textBottomSheet_button_pickColor.setOnClickListener {
            mColorPickerDialog = ColorPickerDialog(
                requireContext(),
                mSharedViewModel.textColor.value!!,
                object : ColorPickerDialogListener {
                    override fun setSelectedColorString(colorString: String) {
                        mSharedViewModel.textColor.postValue(colorString)
                    }
                }
            )
            mColorPickerDialog.show()
        }

        textBottomSheet_button_textNormal.setOnClickListener {
            mSharedViewModel.textStyle.postValue(TextStyle.NORMAL)
        }
        textBottomSheet_button_textBold.setOnClickListener {
            mSharedViewModel.textStyle.postValue(TextStyle.BOLD)
        }
        textBottomSheet_button_textItalic.setOnClickListener {
            mSharedViewModel.textStyle.postValue(TextStyle.ITALIC)
        }
    }
}