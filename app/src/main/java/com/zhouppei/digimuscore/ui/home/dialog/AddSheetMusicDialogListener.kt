package com.zhouppei.digimuscore.ui.home.dialog

import com.zhouppei.digimuscore.data.models.SheetMusic

interface AddSheetMusicDialogListener {
    fun onAddButtonClicked(sheetmusic: SheetMusic)
}