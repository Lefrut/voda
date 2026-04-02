package com.m.vodovoz.feature.profile.waterapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
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
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.openAppNotificationSettings
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WaterAppEntry() = NavigationEntry<WaterAppViewModel> {
    val context = LocalContext.current
    val activity = context as? Activity
    val viewState by viewModel.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.changeHaveNotifications(true)
        }
    }

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.setTabVisibility(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            viewModel.insetsVisibilityState.consumeSystemBarInsets(false)
        }

        onStopOrDispose {
            viewModel.insetsVisibilityState.consumeSystemBarInsets(true)
            viewModel.tabManager.setTabVisibility(true)
        }
    }

    AppearanceSystemBarsEffect()

    AnimatedContent(
        targetState = viewState.uiState,
        label = "Animated Water app screens",
        transitionSpec = {
            if (targetState == WaterAppUiState.GoalCompleted) {
                goalCompletedTransition()
            } else {
                waterAppTransition()
            }
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
                val dailyGoal = viewState.dailyGoal
                WaterAppBottleScreen(
                    dailyGoal = dailyGoal,
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
                    },
                    onClearClick = {
                        viewModel.showClearDialog()
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

                if (viewState.showClearDialog) {
                    VodovozDialog(
                        title = stringResource(R.string.reset_data),
                        description = stringResource(R.string.you_sure_delete_data),
                        acceptButtonText = stringResource(R.string.delete),
                        cancelButtonText = stringResource(R.string.notification_dialog_cancel),
                        onDismiss = {
                            viewModel.closeClearDialog()
                        },
                        onAccept = {
                            viewModel.clearWaterAppData()
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
                    navigator.goBack()
                }

                is WaterAppViewModel.WaterAppEvents.SwitchNotifications -> {
                    if (haveNotificationPermission(context) || !event.haveNotifications) {
                        viewModel.changeHaveNotifications(event.haveNotifications)
                    } else if (
                        shouldShowNotificationRationale(activity) &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    ) {
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

private fun haveNotificationPermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun shouldShowNotificationRationale(activity: Activity?): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == true
    } else {
        false
    }
}
