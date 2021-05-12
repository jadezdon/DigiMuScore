package com.zhouppei.digimuscore.utils

import android.content.res.AssetManager
import android.graphics.Typeface

object FontUtil {
    val typefaceMap: MutableMap<String, Typeface> = mutableMapOf()
    lateinit var musicalTypeface: Typeface
    private const val MUSICAL_TYPEFACE_NAME = "musical.ttf"

    fun loadTypefaces(assets: AssetManager?) {
        if (typefaceMap.isEmpty()) {
            val fontList = assets?.list("fonts")
            fontList?.let {
                for (f in it) {
                    typefaceMap[f.toString()] = Typeface.createFromAsset(assets, "fonts/${f}")
                }
            }
        }
        if (!this::musicalTypeface.isInitialized)
            musicalTypeface = Typeface.createFromAsset(assets, "musical-fonts/musical.ttf")
    }

    fun getTypefaceName(targetTypeface: Typeface): String {
        for ((name, typeface) in typefaceMap) {
            if (typeface == targetTypeface) return name
        }

        if (targetTypeface == musicalTypeface) return MUSICAL_TYPEFACE_NAME
        return ""
    }

    fun getTypeface(typefaceName: String?): Typeface {

        if (typefaceName == MUSICAL_TYPEFACE_NAME) return musicalTypeface

        for ((name, typeface) in typefaceMap) {
            if (name == typefaceName) return typeface
        }

        return typefaceMap.values.first()
    }
}