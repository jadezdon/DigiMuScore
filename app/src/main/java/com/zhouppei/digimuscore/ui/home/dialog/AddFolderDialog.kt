package com.zhouppei.digimuscore.ui.home.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.data.models.Folder
import com.zhouppei.digimuscore.utils.Constants
import kotlinx.android.synthetic.main.dialog_add_folder.*

class AddFolderDialog(
    context: Context,
    private var addFolderDialogListener: AddFolderDialogListener,
    private val folder: Folder? = null
) : AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_folder)

        val metrics = context.resources.displayMetrics
        window?.setLayout((metrics.widthPixels * 0.8).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(R.drawable.background_dialog)

        folder?.let {
            addFolder_editText_folderName.setText(it.name)
            addFolder_button_save.text = "SAVE"
            addFolder_dialogTitle.text = "Edit folder"
        }

        addFolder_button_save.setOnClickListener {
            val folderName = addFolder_editText_folderName.text.toString().trim()

            if (folderName.isBlank()) {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (folderName == Constants.DEFAULT_FOLDER_NAME) {
                Toast.makeText(context, "Name cannot be named as Default", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (folder == null) {
                addFolderDialogListener.onAddFolderButtonClicked(Folder(name = folderName))
            } else {
                folder.let {
                    it.name = folderName
                    addFolderDialogListener.onAddFolderButtonClicked(it)
                }
            }
            dismiss()
        }
    }
}

interface AddFolderDialogListener {
    fun onAddFolderButtonClicked(folder: Folder)
}