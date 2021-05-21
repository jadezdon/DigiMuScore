package com.zhouppei.digimuscore.ui.home.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.data.models.SheetMusic
import kotlinx.android.synthetic.main.dialog_add_sheet_music.*

class AddSheetMusicDialog(
    context: Context,
    private var addSheetMusicDialogListener: AddSheetMusicDialogListener,
    private val sheetmusic: SheetMusic? = null
) : AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_sheet_music)

        val metrics = context.resources.displayMetrics
        window?.setLayout((metrics.widthPixels * 0.8).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(R.drawable.background_dialog)

        sheetmusic?.let {
            addSheetMusic_editText_title.setText(it.title)
            addSheetMusic_editText_author.setText(it.author)
            addSheetMusic_button_add.text = "SAVE"
            addSheetMusic_dialogTitle.text = "Edit sheet music"
        }

        addSheetMusic_button_add.setOnClickListener {
            val title = addSheetMusic_editText_title.text.toString()
            val author = addSheetMusic_editText_author.text.toString()

            if (author.isBlank() || title.isBlank()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (sheetmusic == null) {
                addSheetMusicDialogListener.onAddButtonClicked(SheetMusic(author = author, title = title, folderId = null))
            } else {
                sheetmusic.let {
                    it.author = author
                    it.title = title
                    addSheetMusicDialogListener.onAddButtonClicked(it)
                }
            }
            dismiss()
        }
    }
}