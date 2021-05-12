package com.zhouppei.digimuscore.ui.notation.colorpicker

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.adapters.ColorClickListener
import com.zhouppei.digimuscore.adapters.ColorListAdapter
import com.zhouppei.digimuscore.utils.Constants
import kotlinx.android.synthetic.main.dialog_color_picker.*
import java.util.*

class ColorPickerDialog(
    context: Context,
    private var colorSelected: String,
    private val colorPickerDialogListener: ColorPickerDialogListener
) : AppCompatDialog(context) {

    private var mColorPresets = MutableList(10) { "" }
    private var mSharedPreferences: SharedPreferences? = null
    private lateinit var mColorListAdapter: ColorListAdapter
    private val gson = Gson()

    companion object {
        private val TAG = ColorPickerDialog::class.qualifiedName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_color_picker)

        val metrics = context.resources.displayMetrics
        window?.setLayout((metrics.widthPixels * 0.8).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(R.drawable.background_dialog)

        setProgress(colorSelected.replace("#", "").toUpperCase(Locale.getDefault()))
        colorPicker_editText_colorString.setText(
            colorSelected.replace("#", "").toUpperCase(Locale.getDefault())
        )
        colorPicker_button_colorPreview.setBackgroundColor(Color.parseColor(colorSelected))

        colorPicker_button_cancel.setOnClickListener {
            dismiss()
        }

        colorPicker_button_apply.setOnClickListener {
            colorPickerDialogListener.setSelectedColorString(colorSelected)
            saveColorPresets(colorSelected)
            dismiss()
        }

        colorPicker_editText_colorString.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let { setProgress(it.toString()) }
            }
        })

        setupSeekBars()
        setupColorPresets()
    }

    private fun setupSeekBars() {
        colorPicker_seekbar_colorA.setOnSeekBarChangeListener(ColorSeekBarChangeListener())
        colorPicker_seekbar_colorR.setOnSeekBarChangeListener(ColorSeekBarChangeListener())
        colorPicker_seekbar_colorG.setOnSeekBarChangeListener(ColorSeekBarChangeListener())
        colorPicker_seekbar_colorB.setOnSeekBarChangeListener(ColorSeekBarChangeListener())
    }

    private fun setupColorPresets() {
        mSharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE)

        mSharedPreferences?.let {
            if (it.contains(Constants.SHARED_PREF_COLOR_PRESET)) {
                val str = it.getString(Constants.SHARED_PREF_COLOR_PRESET, "")
                val itemType = object : TypeToken<List<String>>() {}.type
                mColorPresets = gson.fromJson(str, itemType)
            }
        }

        mColorListAdapter = ColorListAdapter(
            object : ColorClickListener {
                override fun onColorClick(color: String) {
                    colorPickerDialogListener.setSelectedColorString(color)
                    saveColorPresets(color)
                    dismiss()
                }
            }
        )

        colorPicker_recyclerView_colorPresets.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = mColorListAdapter
        }

        mColorListAdapter.submitList(mColorPresets)
    }

    private fun saveColorPresets(lastColor: String) {
        if (mColorPresets.remove(lastColor)) {
            mColorPresets.add(0, lastColor)
        } else {
            for (index in mColorPresets.size - 1 downTo 1) {
                mColorPresets[index] = mColorPresets[index - 1]
            }
            mColorPresets[0] = lastColor
        }

        mSharedPreferences?.let {
            it.edit().apply {
                putString(Constants.SHARED_PREF_COLOR_PRESET, gson.toJson(mColorPresets))
                apply()
            }
        }
    }

    inner class ColorSeekBarChangeListener : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            colorSelected = getColorString()
            colorPicker_editText_colorString.setText(
                colorSelected.replace("#", "").toUpperCase(Locale.getDefault())
            )
            colorPicker_button_colorPreview.setBackgroundColor(Color.parseColor(colorSelected))
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun setProgress(s: String) {
        if (s.length == 6) {
            colorPicker_seekbar_colorA.progress = 255
            colorPicker_seekbar_colorR.progress = Integer.parseInt(s.substring(0..1), 16)
            colorPicker_seekbar_colorG.progress = Integer.parseInt(s.substring(2..3), 16)
            colorPicker_seekbar_colorB.progress = Integer.parseInt(s.substring(4..5), 16)
        } else if (s.length == 8) {
            colorPicker_seekbar_colorA.progress = Integer.parseInt(s.substring(0..1), 16)
            colorPicker_seekbar_colorR.progress = Integer.parseInt(s.substring(2..3), 16)
            colorPicker_seekbar_colorG.progress = Integer.parseInt(s.substring(4..5), 16)
            colorPicker_seekbar_colorB.progress = Integer.parseInt(s.substring(6..7), 16)
        }
    }

    private fun getColorString(): String {
        var colorA =
            Integer.toHexString(((255 * colorPicker_seekbar_colorA.progress) / colorPicker_seekbar_colorA.max))
        if (colorA.length == 1) colorA = "0$colorA"

        var colorR =
            Integer.toHexString(((255 * colorPicker_seekbar_colorR.progress) / colorPicker_seekbar_colorR.max))
        if (colorR.length == 1) colorR = "0$colorR"

        var colorG =
            Integer.toHexString(((255 * colorPicker_seekbar_colorG.progress) / colorPicker_seekbar_colorG.max))
        if (colorG.length == 1) colorG = "0$colorG"

        var colorB =
            Integer.toHexString(((255 * colorPicker_seekbar_colorB.progress) / colorPicker_seekbar_colorB.max))
        if (colorB.length == 1) colorB = "0$colorB"

        return "#$colorA$colorR$colorG$colorB"
    }
}