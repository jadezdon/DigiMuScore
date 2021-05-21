package com.zhouppei.digimuscore.ui.sheetmusicpage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.zhouppei.digimuscore.data.models.SheetMusicPage
import com.zhouppei.digimuscore.data.repositories.SheetMusicPageRepository
import com.zhouppei.digimuscore.notation.DrawLayer
import dagger.assisted.AssistedFactory

class SheetMusicPageViewModel @AssistedInject constructor(
    private val sheetMusicPageRepository: SheetMusicPageRepository,
    @Assisted private val sheetMusicId: Int,
    @Assisted private val currentPageId: Int
) : ViewModel() {

    val sheetMusicPages = sheetMusicPageRepository.getAllBySheetMusicId(sheetMusicId)
    val currentPage = MutableLiveData<SheetMusicPage>()

    @AssistedInject.Factory
    interface SheetMusicPageAssistedFactory {
        fun create(sheetMusicId: Int, currentPageId: Int): SheetMusicPageViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: SheetMusicPageAssistedFactory,
            sheetMusicId: Int,
            currentPageId: Int
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(sheetMusicId, currentPageId) as T
            }
        }
    }
}