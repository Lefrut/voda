package com.m.vodovoz.feature.profile.waterapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.dialogs.VodovozDialog
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.effects.AppearanceSystemBarsEffect
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.profile.waterapp.composables.WaterAppBottleScreen
import com.m.vodovoz.feature.profile.waterapp.composables.WaterAppGoalCompletedScreen
import com.m.vodovoz.feature.profile.waterapp.composables.WaterAppGoalScreen
import com.m.vodovoz.feature.profile.waterapp.composables.WaterAppSettingsScreen
import com.m.vodovoz.feature.profile.waterapp.composables.WaterAppUserDataScreen
import com.m.vodovoz.feature.profile.waterapp.composables.WaterAppWelcomeScreen
import com.m.vodovoz.feature.profile.waterapp.composables.goalCompletedTransition
import com.m.vodovoz.feature.profile.waterapp.composables.waterAppTransition
import com.m.vodovoz.feature.profile.waterapp.model.WaterAppUiState
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.openAppNotificationSettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class WaterAppFragment : Fragment() {

    private val viewModel: WaterAppViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var insertVisibilityState: InsetsVisibilityState

    @Inject
    lateinit var waterAppHelper: WaterAppHelper

    private fun haveNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            context?.let { context ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            } == true
        } else {
            true
        }
    }

    private fun shouldShowNotificationRationale(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == true
        else false


    override fun onStart() {
        super.onStart()

        tabManager.changeTabVisibility(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            insertVisibilityState.consumeSystemBarInsets(false)
        }
    }

    override fun onStop() {
        super.onStop()
        insertVisibilityState.consumeSystemBarInsets(true)
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
                    AppearanceSystemBarsEffect()

                    val context = LocalContext.current
                    val viewState by viewModel.collectAsState()

                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            viewModel.changeHaveNotifications(true)
                        }
                    }

                    AnimatedContent(
                        targetState = viewState.uiState,
                        label = "Animated Water app screens",
                        transitionSpec = {
                            if (targetState == WaterAppUiState.GoalCompleted){
                                goalCompletedTransition()
                            }
                            else waterAppTransition()
                        },
                        contentKey = { targetState ->
                            val key: Any = if (targetState is WaterAppUiState.UserData) {
                                WaterAppUiState.UserData
                            } else targetState
                            key.toString()
                        }
                    ) { uiState ->
                        when (uiState) {
                            WaterAppUiState.GoalCompleted -> {
                                WaterAppGoalCompletedScreen(
                                    goal = viewState.dailyGoal.totalMl,
                                    onCloseClick = {
                                        viewModel.navigateBack()
                                    }
                                )
                            }

                            WaterAppUiState.Main -> {
                                WaterAppBottleScreen(
                                    maxLevel = viewState.dailyGoal.totalMl,
                                    currentLevel = viewState.dailyGoal.currentMl,
                                    changeWaterStep = viewState.changeWaterStep,
                                    onBackClick = {
                                        viewModel.navigateBack()
                                    },
                                    onSettingsClick = {
                                        viewModel.goToSettings()
                                    },
                                    onProgressChanged = { progress ->
                                        viewModel.setWaterLevel(progress)
                                    },
                                    onMinusClick = {
                                        viewModel.subtractChangeWaterStep()
                                    },
                                    onPlusClick = {
                                        viewModel.addChangeWaterStep()
                                    },
                                    onBottleClick = {
                                        viewModel.addWater()
                                    }
                                )
                            }

                            WaterAppUiState.Settings -> {
                                WaterAppSettingsScreen(
                                    intervals = viewState.reminderIntervals,
                                    userInfo = viewState.userInfo,
                                    notificationSettings = viewState.notificationSettings,
                                    showParameters = viewState.completeSettings,
                                    onReminderIntervalClick = { reminderInterval ->
                                        viewModel.selectReminderInterval(reminderInterval)
                                    },
                                    onHaveNotificationsChange = {
                                        viewModel.checkHaveNotifications()
                                    },
                                    onSettingsSaveClick = {
                                        viewModel.saveNotificationSettings()
                                    },
                                    onCloseClick = {
                                        viewModel.goToMainStage()
                                    },
                                    onEditUserData = { stage ->
                                        viewModel.goToUserDataStage(stage)
                                    }
                                )

                                if (viewState.showNotificationSettingsDialog) {
                                    VodovozDialog(
                                        title = stringResource(R.string.notification_dialog_title),
                                        description = stringResource(R.string.notification_dialog_description),
                                        acceptButtonText = stringResource(R.string.notification_dialog_accept),
                                        cancelButtonText = stringResource(R.string.notification_dialog_cancel),
                                        onDismiss = {
                                            viewModel.closeNotificationSettingsDialog()
                                        },
                                        onAccept = {
                                            viewModel.openNotificationSettings()
                                        }
                                    )
                                }
                            }

                            is WaterAppUiState.UserData -> {
                                WaterAppUserDataScreen(
                                    userDataStage = uiState,
                                    userInfo = viewState.userInfo,
                                    notificationSettings = viewState.notificationSettings,
                                    hideTopBar = viewState.completeSettings,
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
                                    onBackClick = { userDataStage ->
                                        viewModel.goToPreviousStage(userDataStage)
                                    },
                                    onCloseClick = {
                                        viewModel.navigateBack()
                                    },
                                    onNextClick = { userDataStage ->
                                        viewModel.goToNextStage(userDataStage)
                                    },
                                )
                            }

                            WaterAppUiState.Welcome -> {
                                WaterAppWelcomeScreen(
                                    onCloseClick = {
                                        viewModel.navigateBack()
                                    },
                                    onStartClick = {
                                        viewModel.goToUserStage()
                                    }
                                )
                            }

                            WaterAppUiState.WaterGoal -> {
                                WaterAppGoalScreen(
                                    goal = viewState.dailyGoal.totalMl,
                                    onCloseClick = {
                                        viewModel.navigateBack()
                                    },
                                    onStartClick = {
                                        viewModel.goToSettings()
                                    }
                                )
                            }

                            WaterAppUiState.Loading -> {
                                LoadingPlaceholder()
                            }
                        }

                    }

                    LifecycleEffect {
                        viewModel.listenDailyGoal()
                    }


                    LifecycleEffect {
                        viewModel.events.collectLatest { event ->
                            when (event) {
                                WaterAppViewModel.WaterAppEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is WaterAppViewModel.WaterAppEvents.SwitchNotifications -> {
                                    if (haveNotificationPermission() || !event.haveNotifications) {
                                        viewModel.changeHaveNotifications(event.haveNotifications)
                                    } else if (shouldShowNotificationRationale() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.showNotificationSettingsDialog()
                                    }
                                }

                                WaterAppViewModel.WaterAppEvents.OpenNotificationSettings -> {
                                    context.openAppNotificationSettings()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}