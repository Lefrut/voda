package com.vodovoz.app.feature.profile.notification_settings

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.preorder.model.FieldUi
import com.vodovoz.app.feature.preorder.model.toUi
import com.vodovoz.app.feature.profile.notification_settings.model.SwitchSectionUi
import com.vodovoz.app.feature.profile.notification_settings.model.SwitchUi
import com.vodovoz.app.feature.profile.notification_settings.model.mapToUi
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class NotificationSettingsViewModel @Inject constructor(
    private val repository: MainRepository,
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
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
                    phoneTitle = notificationSettingDetails.phoneTitle,
                    phoneField = notificationSettingDetails.phoneField.toUi(),
                    switchSections = notificationSettingDetails.switchSections.mapToUi(),
                    uiState = NotSettingsUiState.Success
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

    private fun updateNotificationSettings() = viewModelScope.launch {
        val queries = listOf(
            with(dataState.phoneField) { id to value }
        ) + dataState.switchSections.map { switchSection ->
            switchSection.switches.map { switch -> switch.id to (if (switch.checked) "Y" else "N") }
        }.flatten()

        val queriesMap = queries.associate { it.first to it.second }

        vodovozServiceRepository.updateNotificationSettings(queriesMap).singleResult()
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(NotSettingsEvents.GoBack)
    }


    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(phoneField = updatedField)
        }
    }

    fun changeSwitch(switch: SwitchUi, checked: Boolean) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                switchSections = s.switchSections.map { section ->
                    section.copy(
                        switches = section.switches.map {
                            if (it.id == switch.id) it.copy(checked = checked) else it
                        }
                    )
                }
            )
        }

        updateNotificationSettings().join()
        fetchNotificationSettingsDetails()
    }


    @Immutable
    data class NotSettingsState(
        val title: String = "",
        val phoneTitle: String = "",
        val phoneField: FieldUi = FieldUi.Empty,
        val switchSections: List<SwitchSectionUi> = emptyList(),
        val uiState: NotSettingsUiState = NotSettingsUiState.Loading,
    ) : State

    sealed class NotSettingsEvents : Event {
        data class Success(val message: String) : NotSettingsEvents()
        data class Failure(val message: String) : NotSettingsEvents()

        data object GoBack : NotSettingsEvents()
    }

    sealed interface NotSettingsUiState {
        data object Loading : NotSettingsUiState
        data object Success : NotSettingsUiState
        data object Error : NotSettingsUiState

    }
}