package com.zhouppei.digimuscore.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhouppei.digimuscore.notation.DrawLayer

class DrawLayerConverter {

    private val gson = Gson()

    @TypeConverter
    fun drawLayerListToString(value: List<DrawLayer>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun stringToDrawLayerList(value: String): List<DrawLayer> {
        val itemType = object : TypeToken<List<DrawLayer>>() {}.type
        return gson.fromJson<List<DrawLayer>>(value, itemType)
    }
}