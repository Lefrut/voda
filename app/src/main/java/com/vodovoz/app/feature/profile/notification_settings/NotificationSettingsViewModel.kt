package com.vodovoz.app.feature.profile.notification_settings

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.from
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.SwitchUi
import com.vodovoz.app.design_system.model.widgets.WidgetUi
import com.vodovoz.app.design_system.model.widgets.WidgetUpdaterHandler
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class NotificationSettingsViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : PagingContractViewModel<NotificationSettingsViewModel.NotSettingsState, NotificationSettingsViewModel.NotSettingsEvents>(
    NotSettingsState()
) {

    init {
        viewModelScope.launch { delay(200) }.invokeOnCompletion {
            fetchNotificationSettingsDetails()
        }
    }

    fun fetchNotificationSettingsDetails() = viewModelScope.launch {
        if (dataState.uiState !is NotSettingsUiState.Success) {
            uiStateListener.updateData { s ->
                s.copy(uiState = NotSettingsUiState.Loading)
            }
        }

        val notificationSettingDetailsResult =
            vodovozServiceRepository.getNotificationSettingsDetails().singleResult()

        notificationSettingDetailsResult.onSuccess { notificationSettingDetails ->

            uiStateListener.updateData { s ->
                s.copy(
                    title = notificationSettingDetails.title,
                    uiState = NotSettingsUiState.Success,
                    sections = notificationSettingDetails.sections.map { section ->
                        section.toUi()
                    },
                    button = notificationSettingDetails.button.toUi()
                )
            }

        }.onFailure {
            if (dataState.uiState !is NotSettingsUiState.Success) {
                uiStateListener.updateData { s ->
                    s.copy(uiState = NotSettingsUiState.Error)
                }
            }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(NotSettingsEvents.GoBack)
    }


    private val widgetUpdater = WidgetUpdaterHandler(
        getString = { id ->
            resourcesProvider.getString(id)
        }
    )

    fun changeWidget(widget: WidgetUi, updatedWidget: WidgetUi) = viewModelScope.launch {

        val currentSection =
            dataState.sections.firstOrNull { section ->
                section.items.firstOrNull { widgetUi -> widgetUi.id == widget.id } != null
            } ?: return@launch

        val updatedWidgets = WidgetUpdaterHandler(
            getString = { id -> resourcesProvider.getString(id) }
        ).updateWidget(currentSection.items, widget, updatedWidget)

        uiStateListener.updateData { s ->
            s.copy(
                sections = s.sections.map { section ->
                    if (section == currentSection) section.copy(items = updatedWidgets)
                    else section
                }
            )
        }
    }

    fun saveNotificationSettings() {
        //todo - need review when backend's fixed
        viewModelScope.launch {

            uiStateListener.updateData { s ->
                s.copy(button = s.button.copy(loading = true))
            }

            val widgets = dataState.sections.map { sectionUi ->
                sectionUi.items
            }.flatten()

            if (!widgets.mapNotNull { it as? FieldUi }.checkFields(true)) {
                eventListener.emit(
                    NotSettingsEvents.ShowToast(
                        resourcesProvider.getString(R.string.notification_settings_validation_error)
                    )
                )
                uiStateListener.updateData { s ->
                    s.copy(button = s.button.copy(loading = false))
                }
            }


            val queriesMap = widgets.mapNotNull { widget ->
                when (widget) {
                    is FieldUi -> widget.id to widget.value


                    is SwitchUi -> widget.id to VodovozBoolean.from(widget.value).value

                    else -> null
                }
            }.associate { it.first to it.second }


            val result =
                vodovozServiceRepository.updateNotificationSettings(queriesMap).singleResult()

            result.onFailure {
                eventListener.emit(
                    NotSettingsEvents.ShowToast(
                        resourcesProvider.getString(R.string.notification_settings_save_error)
                    )
                )
                fetchNotificationSettingsDetails()
            }

            uiStateListener.updateData { s ->
                s.copy(button = s.button.copy(loading = false))
            }
        }

    }

    @Immutable
    data class NotSettingsState(
        val title: String = "",
        val uiState: NotSettingsUiState = NotSettingsUiState.Loading,
        val sections: List<SectionUi<WidgetUi>> = emptyList(),
        val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    ) : State

    sealed class NotSettingsEvents : Event {
        data class ShowToast(val message: String) : NotSettingsEvents()
        data object GoBack : NotSettingsEvents()
    }

    sealed interface NotSettingsUiState {
        data object Loading : NotSettingsUiState
        data object Success : NotSettingsUiState
        data object Error : NotSettingsUiState
    }
}