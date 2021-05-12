package com.zhouppei.digimuscore.ui.home.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.zhouppei.digimuscore.R
import kotlinx.android.synthetic.main.dialog_add_sheet_music.*

class AddSheetMusicDialog(
    context: Context,
    private var addSheetMusicDialogListener: AddSheetMusicDialogListener
) : AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_sheet_music)

        val metrics = context.resources.displayMetrics
        window?.setLayout((metrics.widthPixels * 0.8).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(R.drawable.background_dialog)

        home_button_add_sheet_music.setOnClickListener {
            val author = home_editText_sheetMusicAuthor.text.toString()
            val title = home_editText_sheetMusicTitle.text.toString()

            if (author.isEmpty() || title.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addSheetMusicDialogListener.onAddButtonClicked(author, title)
            dismiss()
        }

        home_button_cancel.setOnClickListener {
            dismiss()
        }
    }
}