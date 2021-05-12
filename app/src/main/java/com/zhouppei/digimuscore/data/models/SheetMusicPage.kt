package com.zhouppei.digimuscore.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.zhouppei.digimuscore.notation.DrawLayer

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = SheetMusic::class,
            parentColumns = ["id"],
            childColumns = ["sheet_music_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index("sheet_music_id")]
)
data class SheetMusicPage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "sheet_music_id") val sheetMusicId: Int,
    @ColumnInfo(name = "content_uri") var contentUri: String,
    @ColumnInfo(name = "page_number") var pageNumber: Int,
    @ColumnInfo(name = "draw_layer_list") var drawLayers: MutableList<DrawLayer> = mutableListOf()
)