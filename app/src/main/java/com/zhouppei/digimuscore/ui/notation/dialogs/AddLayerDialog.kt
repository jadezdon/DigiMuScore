package com.zhouppei.digimuscore.ui.notation.dialogs

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.zhouppei.digimuscore.R
import kotlinx.android.synthetic.main.dialog_add_layer.*

class AddLayerDialog(
    context: Context,
    private var layerName: String,
    private var clickListener: AddLayerDialogListener
) : AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_layer)

        val metrics = context.resources.displayMetrics
        window?.setLayout((metrics.widthPixels * 0.8).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(R.drawable.background_dialog)

        if (layerName.isNotBlank()) addLayer_editText_layerName.setText(layerName)

        addLayer_button_save.setOnClickListener {
            val layerName = addLayer_editText_layerName.text.toString().trim()

            if (layerName.isEmpty()) {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            clickListener.onSaveButtonClicked(layerName)
            dismiss()
        }
    }
}

interface AddLayerDialogListener {
    fun onSaveButtonClicked(layerName: String)
}