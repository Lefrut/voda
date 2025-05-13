package com.vodovoz.app.feature.profile.notificationsettings

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.toErrorState
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.core.network.ApiConfig
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.preorder.model.FieldUi
import com.vodovoz.app.feature.preorder.model.toUi
import com.vodovoz.app.feature.profile.notificationsettings.model.NotSettingsItem
import com.vodovoz.app.feature.profile.notificationsettings.model.NotificationSettingsModel
import com.vodovoz.app.feature.profile.notificationsettings.model.SwitchSectionUi
import com.vodovoz.app.feature.profile.notificationsettings.model.SwitchUi
import com.vodovoz.app.feature.profile.notificationsettings.model.mapToUi
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

    fun firstLoad() {
        if (!state.isFirstLoad) {
            uiStateListener.value = state.copy(isFirstLoad = true, loadingPage = true)
            fetchNotificationSettingsData()
        }
    }

    fun refresh() {
        uiStateListener.value =
            state.copy(loadingPage = true)
        fetchNotificationSettingsData()
    }

    fun saveItem(item: NotSettingsItem) {
        val mappedItems = state.data.item?.notSettingsData?.settingsList?.map {
            if (it.id == item.id) {
                item
            } else {
                it
            }
        } ?: state.data.item?.notSettingsData?.settingsList
        uiStateListener.value = state.copy(
            data = state.data.copy(
                item = state.data.item?.copy(
                    notSettingsData = state.data.item?.notSettingsData?.copy(
                        settingsList = mappedItems
                    )
                )
            )
        )
    }

    fun saveChanges(phoneNumber: String?) {
        viewModelScope.launch {
            val userId = accountManager.fetchAccountId() ?: return@launch

            runCatching {
                val uri = ApiConfig.VODOVOZ_URL
                    .toUri()
                    .buildUpon()
                    .encodedPath("newmobile/uvedomleniya_new.php")
                    .appendQueryParameter("action", "sms")
                    .appendQueryParameter("userid", userId.toString())
                    .apply {
                        if (state.data.item?.notSettingsData?.myPhone?.id != null && phoneNumber != null) {
                            appendQueryParameter(
                                state.data.item?.notSettingsData?.myPhone?.id,
                                phoneNumber
                            )
                        }
                    }
                    .apply {
                        if (!state.data.item?.notSettingsData?.settingsList.isNullOrEmpty()) {
                            state.data.item?.notSettingsData?.settingsList?.forEach {
                                appendQueryParameter(it.id, it.active)
                            }
                        }
                    }
                    .build()
                    .toString()

                repository.fetchNotificationSettingsData(uri)
            }
                .onSuccess {
                    uiStateListener.value = state.copy(
                        error = null,
                        loadingPage = false
                    )
                    eventListener.emit(NotSettingsEvents.Success("Сохранение выполнено"))
                }
                .onFailure {
                    uiStateListener.value = state.copy(
                        loadingPage = false,
                        error = it.toErrorState()
                    )
                    eventListener.emit(NotSettingsEvents.Failure("Данные не были изменены"))
                }
        }
    }

    private fun fetchNotificationSettingsData() {
        viewModelScope.launch {
            val userId = accountManager.fetchAccountId() ?: return@launch

            runCatching {
                val uri = ApiConfig.VODOVOZ_URL
                    .toUri()
                    .buildUpon()
                    .encodedPath("newmobile/uvedomleniya_new.php")
                    .appendQueryParameter("action", "detail")
                    .appendQueryParameter("userid", userId.toString())
                    .build()
                    .toString()
                repository.fetchNotificationSettingsData(uri)
            }
                .onSuccess {
                    uiStateListener.value = state.copy(
                        data = state.data.copy(item = it),
                        error = null,
                        loadingPage = false
                    )
                }
                .onFailure {
                    uiStateListener.value = state.copy(
                        loadingPage = false,
                        error = it.toErrorState()
                    )
                }
        }
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
        val item: NotificationSettingsModel? = null,
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