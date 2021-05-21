package com.zhouppei.digimuscore.ui.sheetmusic

import android.util.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.zhouppei.digimuscore.data.models.SheetMusic
import com.zhouppei.digimuscore.data.models.SheetMusicPage
import com.zhouppei.digimuscore.data.repositories.SheetMusicPageRepository
import com.zhouppei.digimuscore.data.repositories.SheetMusicRepository
import com.zhouppei.digimuscore.notation.DrawLayer
import com.zhouppei.digimuscore.utils.FileUtil
import kotlinx.coroutines.launch

class SheetMusicViewModel @AssistedInject constructor(
    private val sheetMusicRepository: SheetMusicRepository,
    private val sheetMusicPageRepository: SheetMusicPageRepository,
    @Assisted private val sheetMusicId: Int
) : ViewModel() {

    val sheetMusic = sheetMusicRepository.getById(sheetMusicId)
    val sheetMusicPages = sheetMusicPageRepository.getAllBySheetMusicId(sheetMusicId)

    fun updateSheetMusic(sheetMusic: SheetMusic) {
        viewModelScope.launch {
            sheetMusicRepository.update(sheetMusic)
        }
    }

    fun addPage(contentUri: String, imageSize: Size) {
        viewModelScope.launch {
            val defaultLayer = DrawLayer("Default", 0).apply {
                pageWidth = imageSize.width
                pageHeight = imageSize.height
            }
            val page = SheetMusicPage(
                sheetMusicId = sheetMusicId,
                contentUri = contentUri,
                pageNumber = if (sheetMusicPages.value == null) 1 else sheetMusicPages.value!!.size + 1
            )
            page.drawLayers.add(defaultLayer)
            sheetMusicPageRepository.insert(page)
        }
    }

    fun addPages(contentUris: List<String>, imageSizes: List<Size>) {
        viewModelScope.launch {
            var currentPageNumber = if (sheetMusicPages.value == null) 1 else sheetMusicPages.value!!.size + 1
            contentUris.forEachIndexed { index, contentUri ->
                val defaultLayer = DrawLayer("Default", 0).apply {
                    pageWidth = imageSizes[index].width
                    pageHeight = imageSizes[index].height
                }
                val page = SheetMusicPage(
                    sheetMusicId = sheetMusicId,
                    contentUri = contentUri,
                    pageNumber = currentPageNumber
                )
                page.drawLayers.add(defaultLayer)
                sheetMusicPageRepository.insert(page)
                currentPageNumber += 1
            }
        }
    }

    fun deletePage(sheetMusicPage: SheetMusicPage) {
        viewModelScope.launch {
            FileUtil.deleteAllRelatedFiles(sheetMusicPage.contentUri)
            sheetMusicPageRepository.delete(sheetMusicPage)
        }
    }

    fun updatePage(sheetMusicPage: SheetMusicPage) {
        viewModelScope.launch {
            sheetMusicPageRepository.update(sheetMusicPage)
        }
    }

    fun updateAllPage(sheetMusicPages: List<SheetMusicPage>) {
        viewModelScope.launch {
            sheetMusicPageRepository.updateAll(sheetMusicPages)
        }
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(sheetMusicId: Int): SheetMusicViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            sheetMusicId: Int
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(sheetMusicId) as T
            }
        }

        private val TAG = SheetMusicViewModel::class.qualifiedName
    }
}