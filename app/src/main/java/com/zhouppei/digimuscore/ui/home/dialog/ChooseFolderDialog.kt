package com.zhouppei.digimuscore.ui.home.dialog

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialog
import com.zhouppei.digimuscore.R
import com.zhouppei.digimuscore.adapters.FolderClickListener
import com.zhouppei.digimuscore.adapters.FolderListAdapter
import com.zhouppei.digimuscore.data.models.Folder
import kotlinx.android.synthetic.main.dialog_choose_folder.*

class ChooseFolderDialog(
    context: Context,
    private val folderClickListener: FolderClickListener,
    private val folders: List<Folder>
) : AppCompatDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choose_folder)

        val metrics = context.resources.displayMetrics
        window?.setLayout((metrics.widthPixels * 0.8).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(R.drawable.background_dialog)

        val folderListAdapter = FolderListAdapter(object : FolderClickListener {
            override fun onFolderClick(folder: Folder) {
                folderClickListener.onFolderClick(folder)
                dismiss()
            }
        })
        folderListAdapter.submitList(folders)
        chooseFolder_recyclerview.adapter = folderListAdapter
    }
}