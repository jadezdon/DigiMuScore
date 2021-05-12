package com.zhouppei.digimuscore.ui.home.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.utils.Constants
import kotlinx.android.synthetic.main.dialog_add_folder.*

class AddFolderDialog(
    context: Context,
    private var addFolderDialogListener: AddFolderDialogListener
) : AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_folder)

        val metrics = context.resources.displayMetrics
        window?.setLayout((metrics.widthPixels * 0.8).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(R.drawable.background_dialog)

        addFolder_button_save.setOnClickListener {
            val folderName = addFolder_editText_folderName.text.toString().trim()

            if (folderName.isEmpty()) {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (folderName == Constants.DEFAULT_FOLDER_NAME) {
                Toast.makeText(context, "Name cannot be named as Default", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addFolderDialogListener.onAddFolderButtonClicked(folderName)
            dismiss()
        }
    }
}

interface AddFolderDialogListener {
    fun onAddFolderButtonClicked(folderName: String)
}