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
                uiStateListener.updateData { s -> s.copy(userData = userData ?: s.userData) }
            }
        }

        launch {
            waterAppHelper.observeWaterAppRateData().collectLatest {
                uiStateListener.updateData { s ->
                    val rateData = it ?: s.rateData
                    s.copy(rateData = rateData)
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
        waterAppHelper.setGender(if (man) "man" else "gril")
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

                waterAppHelper.saveUserData()
                waterAppHelper.calculateAndSaveRate()
                waterAppHelper.setStart(true)
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
        waterAppHelper.setSport(activityLevel.value.toString())
    }

    fun selectReminderInterval(reminderInterval: ReminderIntervalUi) = viewModelScope.launch {
        waterAppHelper.setNotificationTime(reminderInterval.minutes.toString())
    }

    fun checkHaveNotifications() = viewModelScope.launch {
        eventListener.emit(WaterAppEvents.SwitchNotifications(!dataState.notificationData.switch))
    }

    fun changeHaveNotifications(haveNotifications: Boolean) = viewModelScope.launch {
        waterAppHelper.setNotificationSwitch(haveNotifications)
    }


    fun saveSettingsNotifications() = viewModelScope.launch {
        waterAppHelper.setNotificationFirstShow()
        waterAppHelper.saveWaterAppNotificationData()
        waterAppHelper.saveUserData()
        waterAppHelper.calculateAndSaveRate()

        uiStateListener.updateData { s -> s.copy(uiState = WaterAppUiState.Main) }


    }

    fun goToWaterApp() = viewModelScope.launch {
        waterAppHelper.setNotificationFirstShow()
        waterAppHelper.fetchWaterAppUserData()
        waterAppHelper.fetchWaterAppNotificationData()

        uiStateListener.updateData { s ->
            s.copy(uiState = WaterAppUiState.Main)
        }
    }

    fun goToSettings() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(uiState = WaterAppUiState.Settings)
        }
    }

    fun selectWeight(weight: Float) = viewModelScope.launch {
        waterAppHelper.setWeight(weight.toString())
    }

    fun selectHeight(height: Int) = viewModelScope.launch {
        waterAppHelper.setHeight(height.toString())
    }

    fun selectWakeUpTime(time: String) = viewModelScope.launch {
        waterAppHelper.setWakeUpTime(WaterAppHelper.parseTime(time))
    }

    fun selectSleepTime(time: String) = viewModelScope.launch {
        waterAppHelper.setSleepTime(WaterAppHelper.parseTime(time))
    }

    fun changeWaterLevel(progress: Float) = viewModelScope.launch {


        val rateData = dataState.rateData
        val currentLevel = (progress * rateData.rate).toInt()
        waterAppHelper.setWaterLevel(currentLevel)
        checkGoalCompleted()
    }

    fun addWater() = viewModelScope.launch {
        if (!dataState.rateData.canFill) return@launch

        waterAppHelper.tryToAddWater(dataState.changeWaterStep)

        checkGoalCompleted()
    }

    private fun checkGoalCompleted() = viewModelScope.launch {
        delay(2000L)
        if (waterAppHelper.observeWaterAppRateData().value?.canFill == false) {
            uiStateListener.updateData { s -> s.copy(uiState = WaterAppUiState.GoalCompleted) }
        }
    }

    fun goToUserDataStage(stage: WaterAppUiState.UserData) = viewModelScope.launch {
        uiStateListener.updateData { s -> s.copy(uiState = stage) }
    }

    fun addChangeWaterStep() = viewModelScope.launch {
        val levels = WaterAppHelper.waterCupLevels
        val currentValue = dataState.changeWaterStep

        val nextStep = when (val currentIndex = levels.indexOf(currentValue)) {
            -1 -> currentValue
            in 0 until levels.lastIndex -> levels.getOrNull(currentIndex + 1)
            else -> currentValue
        } ?: 250

        uiStateListener.updateData { state ->
            state.copy(changeWaterStep = nextStep)
        }
    }

    fun subtractChangeWaterStep() = viewModelScope.launch {
        val levels = WaterAppHelper.waterCupLevels
        val currentValue = dataState.changeWaterStep

        val prevStep = when (val currentIndex = levels.indexOf(currentValue)) {
            -1 -> currentValue
            in 1..levels.lastIndex -> levels.getOrNull(currentIndex - 1)
            else -> currentValue
        } ?: 250

        uiStateListener.updateData { it.copy(changeWaterStep = prevStep) }
    }
    fun showNotificationSettingsDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showNotificationSettingsDialog = true)
        }
    }

    fun closeNotificationSettingsDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showNotificationSettingsDialog = false)
        }
    }

    fun openNotificationSettings() = viewModelScope.launch {
        uiStateListener.updateData { s -> s.copy(showNotificationSettingsDialog = false) }
        eventListener.emit(WaterAppEvents.OpenNotificationSettings)
    }


    @Immutable
    data class WaterAppState(
        val userData: WaterAppHelper.WaterAppUserData = WaterAppHelper.WaterAppUserData(),
        val notificationData: WaterAppHelper.WaterAppNotificationData = WaterAppHelper.WaterAppNotificationData(),
        val rateData: WaterAppHelper.WaterAppRateData = WaterAppHelper.WaterAppRateData(),
        val uiState: WaterAppUiState = WaterAppUiState.Welcome,
        val reminderIntervals: List<ReminderIntervalUi> = emptyList(),
        val changeWaterStep: Int = 250,
        val showNotificationSettingsDialog: Boolean = false,
    ) : State

    @Immutable
    sealed class WaterAppEvents : Event {
        data class SwitchNotifications(val haveNotifications: Boolean) : WaterAppEvents()

        data object GoBack : WaterAppEvents()
        data object OpenNotificationSettings : WaterAppEvents()

    }


}