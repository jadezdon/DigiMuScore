package com.zhouppei.digimuscore.notation

class DrawLayer(
    var name: String,
    var layerOrder: Int
) {
    var drawTexts: MutableList<DrawText> = mutableListOf()
    var drawPaths: MutableList<DrawPath> = mutableListOf()

    var pageWidth: Int = 0
    var pageHeight: Int = 0
}