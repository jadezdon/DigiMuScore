package com.zhouppei.digimuscore.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.zhouppei.digimuscore.data.models.SheetMusic

@Dao
abstract class SheetMusicDao : BaseDao<SheetMusic> {

    @Query("SELECT * FROM sheetmusic")
    abstract fun getAll(): LiveData<List<SheetMusic>>

    @Query("SELECT * FROM sheetmusic WHERE folder_id = :folderId")
    abstract fun getAllByFolderId(folderId: Int): LiveData<List<SheetMusic>>

    @Query("SELECT * FROM sheetmusic WHERE is_favorite = 1")
    abstract fun getAllFavorites(): LiveData<List<SheetMusic>>

    @Query("SELECT * FROM sheetmusic WHERE folder_id is null")
    abstract fun getAllInDefaultFolder(): LiveData<List<SheetMusic>>

    @Query("SELECT * FROM sheetmusic WHERE id = :id")
    abstract fun getById(id: Int): LiveData<SheetMusic>
}