package com.zhouppei.digimuscore.data.repositories

import androidx.lifecycle.LiveData
import com.zhouppei.digimuscore.data.dao.SheetMusicDao
import com.zhouppei.digimuscore.data.models.SheetMusic
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SheetMusicRepository @Inject constructor(
    private val sheetMusicDao: SheetMusicDao
) {

    suspend fun insert(sheetMusic: SheetMusic) {
        return sheetMusicDao.insert(sheetMusic)
    }

    fun getAll(): LiveData<List<SheetMusic>> {
        return sheetMusicDao.getAll()
    }

    fun getAllFavorites(): LiveData<List<SheetMusic>> {
        return sheetMusicDao.getAllFavorites()
    }

    fun getAllInDefaultFolder(): LiveData<List<SheetMusic>> {
        return sheetMusicDao.getAllInDefaultFolder()
    }

    fun getAllByFolderId(folderId: Int): LiveData<List<SheetMusic>> {
        return sheetMusicDao.getAllByFolderId(folderId)
    }

    fun getById(id: Int): LiveData<SheetMusic> {
        return sheetMusicDao.getById(id)
    }

    suspend fun update(sheetMusic: SheetMusic) {
        sheetMusicDao.update(sheetMusic)
    }

    suspend fun updateAll(sheetMusics: List<SheetMusic>) {
        sheetMusicDao.updateAll(*sheetMusics.toTypedArray())
    }

    suspend fun delete(sheetMusic: SheetMusic) {
        sheetMusicDao.delete(sheetMusic)
    }
}