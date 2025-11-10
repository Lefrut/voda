package com.m.vodovoz.feature.profile.waterapp

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.water_app.NotificationSettings
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.domain.general.respository.WaterAppRepository
import com.m.vodovoz.domain.general.respository.clearAll
import com.m.vodovoz.feature.profile.waterapp.model.ReminderIntervalUi
import com.m.vodovoz.feature.profile.waterapp.model.WaterAppActivityLevelUi
import com.m.vodovoz.feature.profile.waterapp.model.WaterAppUiState
import com.m.vodovoz.feature.profile.waterapp.model.WaterStepUi
import com.m.vodovoz.feature.profile.waterapp.model.toStage
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
@Stable
class WaterAppViewModel @Inject constructor(
    private val waterAppRepository: WaterAppRepository,
    private val waterAppHelper: WaterAppHelper,
) : MviViewModel<WaterAppViewModel.WaterAppState, WaterAppViewModel.WaterAppEvents>(
    WaterAppState()
) {


    @OptIn(FlowPreview::class)
    private fun setInitialStage() =
        waterAppRepository.stageFlow.take(1).onEach { stageResult ->
            stageResult.onSuccess { stage ->
                val currentUiState = WaterAppUiState.checkpoints.firstOrNull { waterAppUiState ->
                    waterAppUiState.toStage() == stage
                } ?: WaterAppUiState.Welcome

                updateState { s ->
                    s.copy(
                        uiState = currentUiState,
                        completeSettings = currentUiState is WaterAppUiState.Main || currentUiState is WaterAppUiState.Settings
                    )
                }
            }.onFailure {
                updateState { s ->
                    s.copy(uiState = WaterAppUiState.Welcome)
                }
            }
        }.launchIn(viewModelScope)


    private inline fun updateUserInfo(
        crossinline block: WaterApp.UserInfo.() -> WaterApp.UserInfo,
    ) {
        updateState { s ->
            s.copy(userInfo = block(s.userInfo))
        }
    }


    private inline fun updateNotificationSettings(
        crossinline block: NotificationSettings.() -> NotificationSettings,
    ) {
        updateState { s ->
            s.copy(notificationSettings = block(s.notificationSettings))
        }
    }

    private inline fun updateDailyGoal(
        crossinline block: WaterApp.DailyGoal.() -> WaterApp.DailyGoal,
    ) {
        updateState { s ->
            s.copy(dailyGoal = block(s.dailyGoal))
        }
    }

    val userInfoJob = waterAppRepository.userInfoFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Result.success(WaterApp.UserInfo.ManDefault)
    ).onEach {
        updateUserInfo { it.getOrNull() ?: this }
    }.launchIn(viewModelScope)


    val notificationSettingsJob = waterAppRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Result.success(WaterApp.DefaultNotificationSettings)
    ).onEach { result ->
        updateNotificationSettings {
            result.getOrNull() ?: this
        }
    }.launchIn(viewModelScope)

    init {
        setInitialStage()
        userInfoJob
        notificationSettingsJob
    }


    @OptIn(FlowPreview::class)
    suspend fun listenDailyGoal(): Nothing = coroutineScope {
        val currentDate = LocalDate.now()
        waterAppRepository.dailyGoalFlow.debounce(100L).stateIn(this).collect { result ->
            val updatedDailyGoal = result.getOrNull()
            val dailyGoal = WaterApp.calculateDailyGoal(stateSnapshot.userInfo)

            if (updatedDailyGoal?.date != currentDate) {
                waterAppRepository.saveDailyGoal(dailyGoal)
            }

            updateDailyGoal { updatedDailyGoal ?: dailyGoal }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(WaterAppEvents.GoBack)
    }

    fun goToUserStage() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = WaterAppUiState.UserData.Gender)
        }
    }

    fun selectGender(man: Boolean) = viewModelScope.launch {
        updateUserInfo {
            val newGender = WaterApp.Gender.from(man)
            val defaultUserInfo = when (newGender) {
                WaterApp.Gender.Man -> WaterApp.UserInfo.ManDefault
                WaterApp.Gender.Girl -> WaterApp.UserInfo.GirlDefault
            }

            val oldDefault = when (gender) {
                WaterApp.Gender.Man -> WaterApp.UserInfo.ManDefault
                WaterApp.Gender.Girl -> WaterApp.UserInfo.GirlDefault
            }

            copy(
                gender = newGender,
                height = if (height == oldDefault.height) defaultUserInfo.height else height,
                weight = if (weight == oldDefault.weight) defaultUserInfo.weight else weight,
                activityLevel = if (activityLevel == oldDefault.activityLevel) {
                    defaultUserInfo.activityLevel
                } else {
                    activityLevel
                }
            )
        }
    }

    fun goToPreviousStage(userDataStage: WaterAppUiState.UserData) = viewModelScope.launch {
        val prevUiState = userDataStage.previous() ?: WaterAppUiState.Welcome

        updateState { s ->
            s.copy(
                uiState = if (stateSnapshot.completeSettings) {
                    WaterAppUiState.Settings
                } else {
                    prevUiState
                }
            )
        }
    }


    fun goToNextStage(currentStage: WaterAppUiState.UserData) = viewModelScope.launch {
        val wasCompleteSettings = stateSnapshot.completeSettings
        val userInfo = stateSnapshot.userInfo
        val waterGoalUiState = WaterAppUiState.WaterGoal

        val nextUiState = currentStage.next() ?: waterGoalUiState

        if (nextUiState == waterGoalUiState) with(waterAppRepository) {
            saveUserInfo(userInfo)
            saveDailyGoal(WaterApp.calculateDailyGoal(userInfo))
            saveStage(WaterAppUiState.Settings.toStage())
        }

        updateState { s ->
            s.copy(
                uiState = if (wasCompleteSettings) {
                    WaterAppUiState.Settings
                } else nextUiState
            )
        }
    }

    fun selectActivityLevel(activityLevel: WaterAppActivityLevelUi) {
        updateUserInfo {
            copy(
                activityLevel = WaterApp.ActivityLevel.entries.firstOrNull { level ->
                    level.sport == activityLevel.value
                } ?: WaterApp.ActivityLevel.Low
            )
        }
    }

    fun selectReminderInterval(reminderInterval: ReminderIntervalUi) = viewModelScope.launch {
        updateNotificationSettings {
            copy(notificationsDelay = reminderInterval.minutes.minutes)
        }
    }

    fun checkHaveNotifications() = viewModelScope.launch {
        sendEvent(WaterAppEvents.SwitchNotifications(!stateSnapshot.notificationSettings.enableNotifications))
    }

    fun changeHaveNotifications(haveNotifications: Boolean) = viewModelScope.launch {
        updateNotificationSettings {
            copy(enableNotifications = haveNotifications)
        }
    }


    fun saveNotificationSettings() = viewModelScope.launch {
        val mainUiState = WaterAppUiState.Main
        val userInfo = stateSnapshot.userInfo
        val notificationSettings = stateSnapshot.notificationSettings
        val updatedDailyGoal = stateSnapshot.dailyGoal.copy(
            totalMl = WaterApp.calculateWaterNorm(userInfo)
        )

        updateState { s ->
            s.copy(
                uiState = mainUiState,
                completeSettings = true,
                dailyGoal = updatedDailyGoal
            )
        }

        waterAppHelper.runOrCancelWorkManager(notificationSettings)
        waterAppRepository.saveUserInfo(userInfo)
        waterAppRepository.saveDailyGoal(updatedDailyGoal)
        waterAppRepository.saveNotificationSettings(notificationSettings)
        waterAppRepository.saveStage(mainUiState.toStage())
    }

    fun goToMainStage() = viewModelScope.launch {
        val mainUiState = WaterAppUiState.Main

        updateState { s -> s.copy(uiState = mainUiState) }

        val notificationSettings =
            waterAppRepository.getNotificationSettings().getOrNull()
        waterAppRepository.saveStage(mainUiState.toStage())

        delay(250)

        updateState { s ->
            s.copy(
                completeSettings = true,
                notificationSettings = notificationSettings ?: s.notificationSettings
            )
        }

    }

    fun goToSettings() = viewModelScope.launch {
        updateState { s -> s.copy(uiState = WaterAppUiState.Settings) }
    }

    fun selectWeight(weight: Float) = viewModelScope.launch {
        updateUserInfo { copy(weight = weight) }
    }

    fun selectHeight(height: Int) = viewModelScope.launch {
        updateUserInfo { copy(height = height.toFloat()) }
    }

    fun selectWakeUpTime(time: String) = viewModelScope.launch {
        updateNotificationSettings {
            copy(
                wakeUpTime = LocalTime.parse(time, WaterAppHelper.timeFormatter)
            )
        }
    }

    fun selectSleepTime(time: String) = viewModelScope.launch {
        updateNotificationSettings {
            copy(
                sleepTime = LocalTime.parse(time, WaterAppHelper.timeFormatter)
            )
        }
    }

    fun setWaterLevel(progress: Float) = viewModelScope.launch {
        changeWaterInBottle {
            val currentMl = (progress * totalMl).toInt()
            withMl(currentMl)
        }
    }

    fun addWater() = viewModelScope.launch {
        changeWaterInBottle { plusMl(stateSnapshot.changeWaterStep.ml) }
    }

    private suspend fun changeWaterInBottle(block: WaterApp.DailyGoal.() -> WaterApp.DailyGoal) {
        val dailyGoal = stateSnapshot.dailyGoal
        val currentDailyGoal = block(dailyGoal)

        updateDailyGoal { currentDailyGoal }
        waterAppRepository.saveDailyGoal(currentDailyGoal)

        if (!dailyGoal.wasCompleted && currentDailyGoal.currentMl == currentDailyGoal.totalMl) {
            delay(300L)
            updateState { s -> s.copy(uiState = WaterAppUiState.GoalCompleted) }
        }
    }

    fun goToUserDataStage(stage: WaterAppUiState.UserData) = viewModelScope.launch {
        updateState { s -> s.copy(uiState = stage) }
    }

    fun addChangeWaterStep() = viewModelScope.launch {
        updateWaterStep { index -> index + 1 }
    }

    fun subtractChangeWaterStep() = viewModelScope.launch {
        updateWaterStep { index -> index - 1 }
    }

    private fun updateWaterStep(newIndex: (index: Int) -> Int) {
        val waterStepLevels = WaterAppUiState.Main.waterSteps
        val currentWaterStep = stateSnapshot.changeWaterStep

        val newWaterStep = when (val currentIndex = waterStepLevels.indexOf(currentWaterStep)) {
            -1 -> currentWaterStep
            in 1..waterStepLevels.lastIndex -> waterStepLevels.getOrNull(newIndex(currentIndex))
            else -> currentWaterStep
        } ?: WaterStepUi.Default250

        updateState { s -> s.copy(changeWaterStep = newWaterStep) }
    }

    fun showClearDialog() {
        updateState { s ->
            s.copy(showClearDialog = true)
        }
    }

    fun closeClearDialog() {
        updateState { s ->
            s.copy(showClearDialog = false)
        }
    }

    fun clearWaterAppData() = viewModelScope.launch {
        updateState { WaterAppState(uiState = WaterAppUiState.Welcome) }
        waterAppRepository.clearAll()
    }

    fun showNotificationSettingsDialog() = viewModelScope.launch {
        updateNotificationSettingsDialog(true)
    }

    fun closeNotificationSettingsDialog() = viewModelScope.launch {
        updateNotificationSettingsDialog(false)
    }

    private fun updateNotificationSettingsDialog(showDialog: Boolean) = updateState { s ->
        s.copy(showNotificationSettingsDialog = showDialog)
    }

    fun openNotificationSettings() = viewModelScope.launch {
        updateState { s -> s.copy(showNotificationSettingsDialog = false) }
        sendEvent(WaterAppEvents.OpenNotificationSettings)
    }


    @Immutable
    data class WaterAppState(
        val userInfo: WaterApp.UserInfo = WaterApp.UserInfo.ManDefault,
        val dailyGoal: WaterApp.DailyGoal = WaterApp.DailyGoal.create(3500),
        val notificationSettings: NotificationSettings = WaterApp.DefaultNotificationSettings,
        val uiState: WaterAppUiState = WaterAppUiState.Loading,
        val completeSettings: Boolean = false,
        val reminderIntervals: List<ReminderIntervalUi> = WaterAppUiState.Settings.reminderIntervals,
        val changeWaterStep: WaterStepUi = WaterStepUi.Default250,
        val showNotificationSettingsDialog: Boolean = false,
        val showClearDialog: Boolean = false,
    ) : State

    sealed class WaterAppEvents : Event {
        data class SwitchNotifications(val haveNotifications: Boolean) : WaterAppEvents()
        data object GoBack : WaterAppEvents()
        data object OpenNotificationSettings : WaterAppEvents()
    }


}