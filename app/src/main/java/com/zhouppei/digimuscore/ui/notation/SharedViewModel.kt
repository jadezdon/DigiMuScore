package com.zhouppei.digimuscore.ui.notation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zhouppei.digimuscore.notation.PenType
import com.zhouppei.digimuscore.notation.TextStyle
import com.zhouppei.digimuscore.view.DrawingMode
import com.zhouppei.digimuscore.view.EraserMode

class SharedViewModel : ViewModel() {

    var pageNumber = MutableLiveData(1)

    var currentAction = MutableLiveData(DrawingMode.SELECT)

    var textSizeList = listOf(10..100).flatten().toMutableList()
    var textFontFamilyList = mutableListOf<String>()

    var textFontFamily = MutableLiveData<String>("OpenSans")
    var textSize = MutableLiveData(14)
    var textColor = MutableLiveData<String>("#000000")
    var textStyle = MutableLiveData<TextStyle>(TextStyle.NORMAL)

    var penSizeList = listOf(5..20).flatten().toMutableList()

    var penSize = MutableLiveData(5)
    var penType = MutableLiveData<PenType>(PenType.PEN)
    var penColor = MutableLiveData<String>("#000000")

    var eraserMode = MutableLiveData<EraserMode>(EraserMode.NORMAL)
    var eraserSize = MutableLiveData<Float>(15f)

    var musicNoteString = MutableLiveData<String>("")

    companion object {
        private val TAG = SharedViewModel::class.qualifiedName
    }
}