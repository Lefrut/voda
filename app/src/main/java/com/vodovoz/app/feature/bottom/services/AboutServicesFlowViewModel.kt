package com.vodovoz.app.feature.bottom.services

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.content.ErrorState
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.toErrorState
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.data.model.common.ResponseEntity
import com.vodovoz.app.data.parser.response.service.AboutServicesResponseJsonParser.parseAboutServicesResponse
import com.vodovoz.app.data.parser.response.service.ServiceByIdResponseJsonParser.parseServiceByIdResponse
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.bottom.services.model.ServiceUi
import com.vodovoz.app.feature.bottom.services.model.mapToUi
import com.vodovoz.app.mapper.AboutServicesBundleMapper.mapToUI
import com.vodovoz.app.mapper.ServiceMapper.mapToUI
import com.vodovoz.app.ui.model.ServiceUI
import com.vodovoz.app.ui.model.custom.AboutServicesBundleUI
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AboutServicesFlowViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<AboutServicesFlowViewModel.AboutServicesState, AboutServicesFlowViewModel.AboutServicesEvents>(
    AboutServicesState()
) {


    init {
        viewModelScope.launch { delay(250L) }.invokeOnCompletion {
            fetchAboutServicesDetails()
        }
    }

    fun fetchAboutServicesDetails() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(uiState = AboutServicesUiState.Loading)
        }

        val allServicesDetailsResult =
            vodovozServiceRepository.getAllServicesDetails().singleResult()

        allServicesDetailsResult.onSuccess { allServicesDetails ->

            uiStateListener.updateData { s ->
                s.copy(
                    title = allServicesDetails.title,
                    descriptionHtml = allServicesDetails.description,
                    uiState = AboutServicesUiState.Success,
                    services = allServicesDetails.services.mapToUi()
                )
            }

        }.onFailure {
            uiStateListener.updateData { s ->
                s.copy(uiState = AboutServicesUiState.Error)
            }
        }
    }


    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(AboutServicesEvents.GoBack)
    }

    fun navigateToServiceDetails(service: ServiceUi) = viewModelScope.launch {
        eventListener.emit(AboutServicesEvents.GoToServiceDetails(service.id))
    }

    sealed class AboutServicesEvents : Event {
        data class GoToServiceDetails(val serviceId: Int): AboutServicesEvents()
        data object GoBack : AboutServicesEvents()
    }

    @Immutable
    data class AboutServicesState(
        val title: String = "",
        val descriptionHtml: String = "",
        val uiState: AboutServicesUiState = AboutServicesUiState.Loading,
        val services: List<ServiceUi> = emptyList(),
    ) : State

    sealed interface AboutServicesUiState {
        data object Loading : AboutServicesUiState
        data object Success : AboutServicesUiState
        data object Error : AboutServicesUiState
    }
}