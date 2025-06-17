package com.vodovoz.app.feature.addresses.add

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.widgets.WidgetUi
import com.vodovoz.app.design_system.model.widgets.WidgetUpdaterHandler
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.addresses.add.model.AddAddressEvent
import com.vodovoz.app.feature.addresses.add.model.AddAddressState
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AddAddressViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<AddAddressState, AddAddressEvent>(AddAddressState()) {

    private val addressId = savedStateHandle.get<Long>("addressId") ?: (-1L).also {
        navigateBack()
    }

    fun navigateBack() = viewModelScope.launch {
        _events.emit(AddAddressEvent.GoBack)
    }

    fun showRemoveAddressDialog() {
        _state.update { s ->
            s.copy(showRemoveAddressDialog = true)
        }
    }

    fun hideRemoveAddressDialog() {
        _state.update { s ->
            s.copy(showRemoveAddressDialog = false)
        }
    }

    fun removeAddress() = viewModelScope.launch {
        vodovozServiceRepository.removeAddress(addressId.toInt()).singleResult()
        _state.update { s ->
            s.copy(showRemoveAddressDialog = false)
        }
        _events.emit(AddAddressEvent.GoBack)
    }

    fun updateAddress() = viewModelScope.launch {

    }

    private val widgetUpdaterHandler = WidgetUpdaterHandler(
        getString = { id ->
            resourcesProvider.getString(id)
        }
    )

    fun changeWidget(widget: WidgetUi, updatedWidget: WidgetUi) = viewModelScope.launch {
        widgetUpdaterHandler.updateWidget(
            currentList = stateSnapshot.widgets + stateSnapshot.fields,
            widget = widget,
            updatedWidget = updatedWidget
        )
    }

}