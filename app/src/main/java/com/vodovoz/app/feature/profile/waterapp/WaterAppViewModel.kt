package com.vodovoz.app.feature.profile.waterapp

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.feature.profile.waterapp.model.ReminderIntervalUi
import com.vodovoz.app.feature.profile.waterapp.model.WaterAppActivityLevel
import com.vodovoz.app.feature.profile.waterapp.model.WaterAppUiState
import com.vodovoz.app.feature.profile.waterapp.model.mapToReminderIntervalUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class WaterAppViewModel @Inject constructor(
    private val waterAppHelper: WaterAppHelper,
) : PagingContractViewModel<WaterAppViewModel.WaterAppState, WaterAppViewModel.WaterAppEvents>(
    WaterAppState()
) {


    init {
        setupScreen()
    }

    private fun setupScreen() = viewModelScope.launch {
        waterAppHelper.fetchWaterAppRateData()
        waterAppHelper.fetchWaterAppUserData()
        waterAppHelper.fetchWaterAppNotificationData()

        val (userStarted, firstShow) = waterAppHelper.observeWaterAppNotificationData().value?.run {
            started to firstShow
        } ?: (null to null)

        if (userStarted == true) {
            uiStateListener.updateData { s ->
                s.copy(uiState = if (firstShow == true) WaterAppUiState.Main else WaterAppUiState.Settings)
            }
        }

        launch {
            waterAppHelper.observeWaterAppUserData().collectLatest { userData ->
                uiStateListener.updateData { s ->
                    s.copy(userData = userData ?: s.userData)
                }
            }
        }

        launch {
            waterAppHelper.observeWaterAppRateData().collectLatest {
                uiStateListener.updateData { s ->
                    val rateData = it ?: s.rateData
                    s.copy(
                        rateData = rateData,
                    )
                }
            }
        }

        launch {
            waterAppHelper.observeWaterAppNotificationData().collectLatest { notificationData ->
                uiStateListener.updateData { s ->
                    val uiNotificationData = notificationData ?: s.notificationData
                    s.copy(
                        notificationData = uiNotificationData,
                        reminderIntervals = WaterAppHelper.reminderIntervals
                            .mapToReminderIntervalUi()
                            .map { option ->
                                if (option.minutes == uiNotificationData.time.toLongOrNull()) {
                                    option.copy(
                                        selected = true
                                    )
                                } else option
                            }
                    )
                }
            }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(WaterAppEvents.GoBack)
    }

    fun goToUserFields() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(uiState = WaterAppUiState.UserData.Gender)
        }
    }

    fun selectGender(man: Boolean) = viewModelScope.launch {
        waterAppHelper.saveGender(if (man) "man" else "gril")
    }

    fun goToPreviousStage() = viewModelScope.launch {
        val prevUiState = when (val currentUiState = dataState.uiState) {
            is WaterAppUiState.UserData -> currentUiState.previous() ?: WaterAppUiState.Welcome
            else -> currentUiState
        }
        uiStateListener.updateData { s ->
            s.copy(
                uiState = if (dataState.notificationData.started) WaterAppUiState.Settings
                else prevUiState
            )
        }
    }

    fun goToNextStage() = viewModelScope.launch {
        val nextUiState = when (val currentUiState = dataState.uiState) {
            is WaterAppUiState.UserData -> currentUiState.next() ?: run {

                waterAppHelper.saveWaterAppUserData()
                waterAppHelper.saveRate()
                waterAppHelper.saveStart(true)
                waterAppHelper.saveWaterAppNotificationData()

                WaterAppUiState.WaterGoal
            }

            else -> currentUiState
        }




        uiStateListener.updateData { s ->
            s.copy(
                uiState = if (dataState.notificationData.firstShow) {
                    WaterAppUiState.Settings
                } else {
                    nextUiState
                }
            )
        }
    }

    fun selectActivityLevel(activityLevel: WaterAppActivityLevel) {
        waterAppHelper.saveSport(activityLevel.value.toString())
    }

    fun selectReminderInterval(reminderInterval: ReminderIntervalUi) = viewModelScope.launch {
        waterAppHelper.saveNotificationTime(reminderInterval.minutes.toString())
    }

    fun changeHaveNotification() = viewModelScope.launch {
        waterAppHelper.saveNotificationSwitch(!dataState.notificationData.switch)
    }

    fun saveSettingsNotifications() = viewModelScope.launch {
        waterAppHelper.saveNotificationFirstShow()
        waterAppHelper.saveWaterAppNotificationData()
        waterAppHelper.saveWaterAppUserData()
        waterAppHelper.saveRate()
        uiStateListener.updateData { s -> s.copy(uiState = WaterAppUiState.Main) }
    }

    fun goToWaterApp() = viewModelScope.launch {
        waterAppHelper.fetchWaterAppUserData()
        uiStateListener.updateData { s ->
            s.copy(uiState = WaterAppUiState.Main)
        }

        if (!dataState.notificationData.firstShow) {
            waterAppHelper.saveNotificationFirstShow()
            waterAppHelper.saveWaterAppNotificationData()
        }

    }

    fun goToSettings() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(uiState = WaterAppUiState.Settings)
        }
    }

    fun selectWeight(weight: Float) = viewModelScope.launch {
        waterAppHelper.saveWeight(weight.toString())
    }

    fun selectHeight(height: Int) = viewModelScope.launch {
        waterAppHelper.saveHeight(height.toString())
    }

    fun selectWakeUpTime(time: String) = viewModelScope.launch {
        waterAppHelper.saveWakeUpTime(WaterAppHelper.parseTime(time))
    }

    fun selectSleepTime(time: String) = viewModelScope.launch {
        waterAppHelper.saveSleepTime(WaterAppHelper.parseTime(time))
    }

    fun changeWaterLevel(progress: Float) = viewModelScope.launch {


        val rateData = dataState.rateData
        val currentLevel = (progress * rateData.rate).toInt()
        waterAppHelper.setWaterLevel(currentLevel)

        if (!rateData.canFill) return@launch

        goToGoalCompleted()
    }

    fun addWater() = viewModelScope.launch {
        if (!dataState.rateData.canFill) return@launch

        waterAppHelper.tryToChangeWaterLevel(dataState.changeWaterStep)

        goToGoalCompleted()
    }

    private fun goToGoalCompleted() = viewModelScope.launch {
        delay(1000)
        if (waterAppHelper.observeWaterAppRateData().value?.canFill == false) {
            uiStateListener.updateData { s ->
                s.copy(uiState = WaterAppUiState.GoalCompleted)
            }
        }
    }

    fun goToUserDataStage(stage: WaterAppUiState.UserData) = viewModelScope.launch {
        uiStateListener.updateData { s -> s.copy(uiState = stage) }
    }

    fun addChangeWaterStep() = viewModelScope.launch {
        val newStep = (dataState.changeWaterStep + 100).coerceAtMost(750)
        uiStateListener.updateData { it.copy(changeWaterStep = newStep) }
    }

    fun subtractChangeWaterStep() = viewModelScope.launch {
        val newStep = (dataState.changeWaterStep - 100).coerceAtLeast(100)
        uiStateListener.updateData { it.copy(changeWaterStep = newStep) }
    }


    @Immutable
    data class WaterAppState(
        val userData: WaterAppHelper.WaterAppUserData = WaterAppHelper.WaterAppUserData(),
        val notificationData: WaterAppHelper.WaterAppNotificationData = WaterAppHelper.WaterAppNotificationData(),
        val rateData: WaterAppHelper.WaterAppRateData = WaterAppHelper.WaterAppRateData(),
        val uiState: WaterAppUiState = WaterAppUiState.Welcome,
        val reminderIntervals: List<ReminderIntervalUi> = emptyList(),
        val changeWaterStep: Int = 250,
    ) : State

    @Immutable
    sealed class WaterAppEvents : Event {

        data object GoBack : WaterAppEvents()

    }


}