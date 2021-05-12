package com.zhouppei.digimuscore.data.repositories

import androidx.lifecycle.LiveData
import com.zhouppei.digimuscore.data.dao.SheetMusicPageDao
import com.zhouppei.digimuscore.data.models.SheetMusicPage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SheetMusicPageRepository @Inject constructor(
    private val sheetMusicPageDao: SheetMusicPageDao
) {

    suspend fun insert(page: SheetMusicPage) {
        sheetMusicPageDao.insert(page)
    }

    fun getAllBySheetMusicId(sheetMusicId: Int): LiveData<List<SheetMusicPage>> {
        return sheetMusicPageDao.getAllBySheetMusicId(sheetMusicId)
    }

    fun getById(id: Int): LiveData<SheetMusicPage> {
        return sheetMusicPageDao.getById(id)
    }

    suspend fun update(page: SheetMusicPage) {
        sheetMusicPageDao.update(page)
    }

    suspend fun updateAll(pages: List<SheetMusicPage>) {
        sheetMusicPageDao.updateAll(*pages.toTypedArray())
    }

    suspend fun delete(page: SheetMusicPage) {
        sheetMusicPageDao.delete(page)
    }

    suspend fun deleteAllBySheetMusicId(sheetMusicId: Int) {
        sheetMusicPageDao.deleteAllBySheetMusicId(sheetMusicId)
    }
}