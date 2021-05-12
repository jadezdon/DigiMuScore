package com.zhouppei.digimuscore.data.repositories

import androidx.lifecycle.LiveData
import com.zhouppei.digimuscore.data.dao.FolderDao
import com.zhouppei.digimuscore.data.models.Folder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderRepository @Inject constructor(
    private val folderDao: FolderDao
) {

    suspend fun insert(folder: Folder) {
        return folderDao.insert(folder)
    }

    fun getById(folderId: Int) : LiveData<Folder> {
        return folderDao.getById(folderId)
    }

    fun getAll(): LiveData<List<Folder>> {
        return folderDao.getAll()
    }

    suspend fun update(folder: Folder) {
        folderDao.update(folder)
    }

    suspend fun updateAll(folders: List<Folder>) {
        folderDao.updateAll(*folders.toTypedArray())
    }

    suspend fun delete(folder: Folder) {
        folderDao.delete(folder)
    }
}