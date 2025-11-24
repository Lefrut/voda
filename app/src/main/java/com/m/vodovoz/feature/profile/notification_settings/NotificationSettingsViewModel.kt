package com.m.vodovoz.feature.profile.notification_settings

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.design_system.model.widgets.WidgetUi
import com.m.vodovoz.design_system.model.widgets.WidgetUpdaterKeeperFactory
import com.m.vodovoz.design_system.model.widgets.toQueryMap
import com.m.vodovoz.design_system.model.widgets.toUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class NotificationSettingsViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<NotificationSettingsViewModel.NotSettingsState, NotificationSettingsViewModel.NotSettingsEvents>(
    NotSettingsState()
) {

    private val widgetsChangesHandlerJob = state.map { s ->
        s.sections.flatMap { sectionUi -> sectionUi.items }
    }.distinctUntilChanged().drop(2).onEach {
        changeScreenLock(true)
        saveNotificationSettings()
        changeScreenLock(false)
    }.launchIn(viewModelScope)

    init {
        viewModelScope.launch { delay(200) }.invokeOnCompletion {
            fetchNotificationSettingsDetails()
        }
    }

    private fun changeScreenLock(lockScreen: Boolean) {
        updateState { s ->
            s.copy(lockScreen = lockScreen)
        }
    }

    fun fetchNotificationSettingsDetails() = viewModelScope.launch {
        if (stateSnapshot.uiState !is NotSettingsUiState.Success) {
            updateState { s ->
                s.copy(uiState = NotSettingsUiState.Loading)
            }
        }

        val notificationSettingDetailsResult =
            vodovozServiceRepository.getNotificationSettingsDetails().singleResult()

        notificationSettingDetailsResult.onSuccess { notificationSettingDetails ->

            updateState { s ->
                s.copy(
                    title = notificationSettingDetails.title,
                    uiState = NotSettingsUiState.Success,
                    sections = notificationSettingDetails.sections.map { section ->
                        section.toUi()
                    },
                )
            }

        }.onFailure {
            if (stateSnapshot.uiState !is NotSettingsUiState.Success) {
                updateState { s ->
                    s.copy(uiState = NotSettingsUiState.Error)
                }
            }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(NotSettingsEvents.GoBack)
    }


    private val widgetUpdaterKeeper = WidgetUpdaterKeeperFactory.createWidgetUpdateKeeper { id ->
        resourcesProvider.getString(id)
    }

    fun changeWidget(widget: WidgetUi, updatedWidget: WidgetUi) = viewModelScope.launch {

        val currentSection =
            stateSnapshot.sections.firstOrNull { section ->
                section.items.firstOrNull { widgetUi -> widgetUi.id == widget.id } != null
            } ?: return@launch

        val updatedWidgets = widgetUpdaterKeeper.updateWidget(
            widgets = currentSection.items,
            widget = widget,
            updatedWidget = updatedWidget
        )

        updateState { s ->
            s.copy(
                sections = s.sections.map { section ->
                    if (section == currentSection) section.copy(items = updatedWidgets)
                    else section
                }
            )
        }
    }


    private suspend fun saveNotificationSettings() {
        val widgets = stateSnapshot.sections.map { sectionUi ->
            sectionUi.items
        }.flatten()
        val queriesMap = widgets.toQueryMap()
        val result = vodovozServiceRepository.updateNotificationSettings(
            queriesMap
        ).singleResult()

        result.onFailure {
            fetchNotificationSettingsDetails().join()
            sendEvent(
                NotSettingsEvents.ShowToast(
                    resourcesProvider.getString(R.string.notification_settings_save_error)
                )
            )
        }.onSuccess { message ->
            sendEvent(
                NotSettingsEvents.ShowToast(message)
            )
        }
    }


    @Immutable
    data class NotSettingsState(
        val title: String = "",
        val uiState: NotSettingsUiState = NotSettingsUiState.Loading,
        val sections: List<SectionUi<WidgetUi>> = emptyList(),
        val lockScreen: Boolean = false
    ) : State

    sealed class NotSettingsEvents : Event {
        data class ShowToast(val message: String) : NotSettingsEvents()
        data object GoBack : NotSettingsEvents()
    }

    @Stable
    sealed interface NotSettingsUiState {
        data object Loading : NotSettingsUiState
        data object Success : NotSettingsUiState
        data object Error : NotSettingsUiState
    }
}