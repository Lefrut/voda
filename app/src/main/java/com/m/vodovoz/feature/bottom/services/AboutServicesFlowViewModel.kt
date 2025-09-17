package com.m.vodovoz.feature.bottom.services

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.bottom.services.model.ServiceUi
import com.m.vodovoz.feature.bottom.services.model.mapToUi
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AboutServicesFlowViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<AboutServicesFlowViewModel.AboutServicesState, AboutServicesFlowViewModel.AboutServicesEvents>(
    AboutServicesState()
) {


    init {
        viewModelScope.launch { delay(250L) }.invokeOnCompletion {
            fetchAboutServicesDetails()
        }
    }

    fun fetchAboutServicesDetails() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = AboutServicesUiState.Loading)
        }

        val allServicesDetailsResult =
            vodovozServiceRepository.getAllServicesDetails().singleResult()

        allServicesDetailsResult.onSuccess { allServicesDetails ->

            updateState { s ->
                s.copy(
                    title = allServicesDetails.title,
                    descriptionHtml = allServicesDetails.description,
                    uiState = AboutServicesUiState.Success,
                    services = allServicesDetails.services.mapToUi()
                )
            }

        }.onFailure {
            updateState { s ->
                s.copy(uiState = AboutServicesUiState.Error)
            }
        }
    }


    fun navigateBack() = viewModelScope.launch {
        sendEvent(AboutServicesEvents.GoBack)
    }

    fun navigateToServiceDetails(service: ServiceUi) = viewModelScope.launch {
        sendEvent(AboutServicesEvents.GoToServiceDetails(service.id))
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

    @Stable
    sealed interface AboutServicesUiState {
        data object Loading : AboutServicesUiState
        data object Success : AboutServicesUiState
        data object Error : AboutServicesUiState
    }
}