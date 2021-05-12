package com.zhouppei.digimuscore.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.zhouppei.digimuscore.data.models.Folder

@Dao
abstract class FolderDao : BaseDao<Folder> {

    @Query("SELECT * FROM folder")
    abstract fun getAll(): LiveData<List<Folder>>

    @Query("SELECT * FROM folder WHERE name = \"Default\"")
    abstract fun getDefault(): LiveData<Folder>

    @Query("SELECT * FROM folder WHERE id = :folderId")
    abstract fun getById(folderId: Int): LiveData<Folder>
}