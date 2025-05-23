package com.vodovoz.app.feature.profile.waterapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppBottleScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppGoalCompletedScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppGoalScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppSettingsScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppUserDataScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppWelcomeScreen
import com.vodovoz.app.feature.profile.waterapp.model.WaterAppUiState
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
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(newValue = pagingState.data)

                    AnimatedContent(
                        targetState = viewState.uiState,
                        label = "Animated Water app screens",
                        contentKey = { targetState ->
                            if (targetState is WaterAppUiState.UserData) {
                                "UserData"
                            } else targetState.toString()
                        },
                        transitionSpec = {
                            val bouncySpring = spring<Float>(
                                dampingRatio = 0.2f,
                                stiffness = 50f
                            )

                            val enter = scaleIn(
                                initialScale = 0.7f,
                                animationSpec = bouncySpring
                            ) + slideInVertically(
                                initialOffsetY = { it / 2 },
                            ) + fadeIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessVeryLow
                                ),
                                initialAlpha = 0.3f
                            )

                            val exit = scaleOut(
                                targetScale = 1.2f,
                                animationSpec = bouncySpring
                            ) + slideOutVertically(
                                targetOffsetY = { -it / 3 },
                            ) + fadeOut(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessVeryLow
                                ),
                                targetAlpha = 0.0f
                            )

                            enter togetherWith  exit using SizeTransform(clip = false)

                        }) { uiState ->
                        when (uiState) {
                            WaterAppUiState.GoalCompleted -> {
                                WaterAppGoalCompletedScreen(
                                    goal = viewState.rateData.rate,
                                    onCloseClick = {
                                        viewModel.navigateBack()
                                    }
                                )
                            }

                            WaterAppUiState.Main -> {
                                WaterAppBottleScreen(
                                    maxLevel = viewState.rateData.rate,
                                    currentLevel = viewState.rateData.currentLevel,
                                    changeWaterStep = viewState.changeWaterStep,
                                    onBackClick = {
                                        viewModel.navigateBack()
                                    },
                                    onSettingsClick = {
                                        viewModel.goToSettings()
                                    },
                                    onProgressChanged = { progress ->
                                        viewModel.changeWaterLevel(progress)
                                    },
                                    onMinusClick = {

                                    },
                                    onPlusClick = {

                                    },
                                    onBottleClick = {
                                        viewModel.addWater()
                                    }
                                )
                            }

                            WaterAppUiState.Settings -> {
                                WaterAppSettingsScreen(
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
                                    },
                                    onCloseClick = {
                                        viewModel.goToWaterApp()
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