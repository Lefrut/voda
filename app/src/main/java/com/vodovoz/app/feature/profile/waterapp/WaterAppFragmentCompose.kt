package com.vodovoz.app.feature.profile.waterapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppGoalScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppSettingsScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppUserDataScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppWelcomeScreen
import com.vodovoz.app.feature.profile.waterapp.model.WaterAppUiState
import com.vodovoz.app.util.extensions.addOnBackPressedCallback
import com.vodovoz.app.util.extensions.debugLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class WaterAppFragment : Fragment() {

    private val viewModel: WaterAppViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var waterAppHelper: WaterAppHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        waterAppHelper.fetchWaterAppUserData()
        waterAppHelper.fetchWaterAppNotificationData()
        waterAppHelper.fetchWaterAppRateData()
    }

    override fun onStart() {
        super.onStart()
        tabManager.changeTabVisibility(false)
    }

    override fun onStop() {
        super.onStop()
        debugLog { "save water rate" }
        waterAppHelper.saveWaterAppRateData()
        tabManager.changeTabVisibility(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                VodovozTheme {
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(newValue = pagingState.data)

                    AnimatedContent(
                        targetState = viewState.uiState,
                        label = "Animated Water app screens"
                    ) { uiState ->
                        when (uiState) {
                            WaterAppUiState.GoalCompleted -> {
                                Text("Goal completed")
                            }

                            WaterAppUiState.Main -> {
                                Text("Water app main")
                            }

                            WaterAppUiState.Settings -> {
                                WaterAppSettingsScreen(
                                    onCloseClick = {
                                        viewModel.goToWaterApp()
                                    },
                                    haveNotifications = viewState.notificationData.switch,
                                    intervals = viewState.reminderIntervals,
                                    showParameters = viewState.notificationData.firstShow,
                                    onReminderIntervalClick = { reminderInterval ->
                                        viewModel.selectReminderInterval(reminderInterval)
                                    },
                                    onHaveNotificationsChange = {
                                        viewModel.changeHaveNotification()
                                    },
                                    onNotificationsSaveClick = {
                                        viewModel.saveNotifications()
                                    }
                                )
                            }

                            is WaterAppUiState.UserData -> {
                                WaterAppUserDataScreen(
                                    userDataStage = uiState,
                                    userData = viewState.userData,
                                    onGenderSelect = { isMan ->
                                        viewModel.selectGender(isMan)
                                    },
                                    onWeightSelect = { weight ->
                                        viewModel.selectWeight(weight)
                                    },
                                    onHeightSelect = { height ->
                                        viewModel.selectHeight(height)
                                    },
                                    onWakeUpTimeChange = { time ->
                                        viewModel.selectWakeUpTime(time)
                                    },
                                    onSleepTimeChange = { time ->
                                        viewModel.selectSleepTime(time)
                                    },
                                    onActivityLevelSelect = { activityLevel ->
                                        viewModel.selectActivityLevel(activityLevel)
                                    },
                                    onBackClick = {
                                        viewModel.goToPreviousStage()
                                    },
                                    onCloseClick = {
                                        viewModel.navigateBack()
                                    },
                                    onNextClick = {
                                        viewModel.goToNextStage()
                                    },
                                )
                            }

                            WaterAppUiState.Welcome -> {
                                WaterAppWelcomeScreen(
                                    onCloseClick = {
                                        viewModel.navigateBack()
                                    },
                                    onStartClick = {
                                        viewModel.goToUserFields()
                                    }
                                )
                            }

                            WaterAppUiState.WaterGoal -> {
                                WaterAppGoalScreen(
                                    goal = viewState.rateData.rate,
                                    onCloseClick = {
                                        viewModel.navigateBack()
                                    },
                                    onStartClick = {
                                        viewModel.goToSettings()
                                    }
                                )
                            }
                        }

                    }

                    LifecycleEffect {
                        viewModel.observeEvent().collectLatest { event ->
                            when (event) {
                                WaterAppViewModel.WaterAppEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}