package com.zhouppei.digimuscore.ui.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zhouppei.digimuscore.data.models.Folder
import com.zhouppei.digimuscore.data.models.SheetMusic
import com.zhouppei.digimuscore.data.repositories.FolderRepository
import com.zhouppei.digimuscore.data.repositories.SheetMusicPageRepository
import com.zhouppei.digimuscore.data.repositories.SheetMusicRepository
import kotlinx.coroutines.launch

class HomeViewModel @ViewModelInject internal constructor(
    private val folderRepository: FolderRepository,
    private val sheetMusicRepository: SheetMusicRepository,
    private val sheetMusicPageRepository: SheetMusicPageRepository
) : ViewModel() {

    val folders = folderRepository.getAll()
    val sheetMusics = sheetMusicRepository.getAll()

    var currentFolder: Folder? = null

    fun addSheetMusic(sheetMusic: SheetMusic) {
        viewModelScope.launch {
            sheetMusicRepository.insert(sheetMusic)
        }
    }

    fun deleteSheetMusic(sheetMusic: SheetMusic) {
        viewModelScope.launch {
            sheetMusicRepository.delete(sheetMusic)
            sheetMusicPageRepository.deleteAllBySheetMusicId(sheetMusic.id)
        }
    }

    fun updateSheetMusic(sheetMusic: SheetMusic) {
        viewModelScope.launch {
            sheetMusicRepository.update(sheetMusic)
        }
    }

    fun updateAllSheetMusic(sheetMusics: List<SheetMusic>) {
        viewModelScope.launch {
            sheetMusicRepository.updateAll(sheetMusics)
        }
    }

    fun addFolder(folder: Folder) {
        viewModelScope.launch {
            folderRepository.insert(folder)
        }
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch {
            folderRepository.delete(folder)
        }
    }

    fun updateAllFolder(folders: List<Folder>) {
        viewModelScope.launch {
            folderRepository.updateAll(folders)
        }
    }

    companion object {
        private const val DEFAULT_FOLDER_ID = 0
    }
}