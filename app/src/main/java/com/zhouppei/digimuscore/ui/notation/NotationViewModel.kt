package com.zhouppei.digimuscore.ui.notation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.zhouppei.digimuscore.data.repositories.SheetMusicPageRepository
import com.zhouppei.digimuscore.notation.DrawLayer
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.launch

class NotationViewModel @AssistedInject constructor(
    private val sheetMusicPageRepository: SheetMusicPageRepository,
    @Assisted private val id: Int
) : ViewModel() {

    val sheetMusicPage = sheetMusicPageRepository.getById(id)
    val currentLayer = MutableLiveData<DrawLayer>()
    var currentLayerIndex = -1

    fun updateSheetMusicPage() {
        viewModelScope.launch {
            sheetMusicPage.value?.let {
                sheetMusicPageRepository.update(it)
            }
        }
    }

    fun addNewLayer(layerName: String) {
        viewModelScope.launch {
            sheetMusicPage.value?.let {
                val layer = DrawLayer(layerName, it.drawLayers.size).apply {
                    pageWidth = it.drawLayers.first().pageWidth
                    pageHeight = it.drawLayers.first().pageHeight
                }
                it.drawLayers.add(layer)
                sheetMusicPageRepository.update(it)
            }
        }
    }

    @AssistedInject.Factory
    interface NotationAssistedFactory {
        fun create(id: Int): NotationViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: NotationAssistedFactory,
            id: Int
        ) : ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(id) as T
            }
        }
    }
}