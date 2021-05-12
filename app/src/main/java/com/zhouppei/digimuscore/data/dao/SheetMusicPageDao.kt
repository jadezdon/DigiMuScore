package com.zhouppei.digimuscore.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.zhouppei.digimuscore.data.models.SheetMusicPage

@Dao
abstract class SheetMusicPageDao : BaseDao<SheetMusicPage> {

    @Query("SELECT * FROM sheetmusicpage WHERE sheet_music_id = :sheetMusicId ORDER BY page_number ASC")
    abstract fun getAllBySheetMusicId(sheetMusicId: Int): LiveData<List<SheetMusicPage>>

    @Query("SELECT * FROM sheetmusicpage WHERE id = :id")
    abstract fun getById(id: Int): LiveData<SheetMusicPage>

    @Query("DELETE FROM sheetmusicpage WHERE sheet_music_id = :sheetMusicId")
    abstract suspend fun deleteAllBySheetMusicId(sheetMusicId: Int)
}