package com.zhouppei.digimuscore.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Folder::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index("folder_id")]
)
data class SheetMusic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "folder_id") var folderId: Int?,
    @ColumnInfo(name = "author") var author: String,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "is_favorite") var isFavorite: Boolean = false
)