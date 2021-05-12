package com.zhouppei.digimuscore.ui.pageturner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.zhouppei.digimuscore.data.models.SheetMusicPage
import com.zhouppei.digimuscore.data.repositories.SheetMusicPageRepository

class PageTurnerViewModel @AssistedInject constructor(
    private val sheetMusicPageRepository: SheetMusicPageRepository,
    @Assisted private val sheetMusicId: Int
) : ViewModel() {

    val sheetMusicPages = sheetMusicPageRepository.getAllBySheetMusicId(sheetMusicId)
    val currentPage = MutableLiveData<SheetMusicPage>()
    val isFaceDetected = MutableLiveData<Boolean>(false)

    @AssistedInject.Factory
    interface PageTurnerAssistedFactory {
        fun create(sheetMusicId: Int): PageTurnerViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: PageTurnerAssistedFactory,
            sheetMusicId: Int
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(sheetMusicId) as T
            }
        }
    }
}