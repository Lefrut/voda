package com.vodovoz.app.feature.profile.waterapp

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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.dialogs.VodovozDialog
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.design_system.effects.SystemBarsEffect
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppBottleScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppGoalCompletedScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppGoalScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppSettingsScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppUserDataScreen
import com.vodovoz.app.feature.profile.waterapp.composables.WaterAppWelcomeScreen
import com.vodovoz.app.feature.profile.waterapp.model.WaterAppUiState
import com.vodovoz.app.util.extensions.openAppNotificationSettings
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

    private fun haveNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            context?.let { context ->
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            } ?: false
        } else {
            true
        }
    }

    private fun shouldShowNotificationRationale(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == true
        else false

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

                    SystemBarsEffect(
                        statusBarColor = Color.Transparent,
                        navigationBarColor = Color.Transparent,
                        navigationBarContrastEnforced = false,
                        handleDecorFitsSystemWindows = true
                    )

                    val context = LocalContext.current
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(newValue = pagingState.data)
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
                            if (targetState == WaterAppUiState.GoalCompleted) {
                                fadeIn(
                                    tween(300, 0, LinearEasing),
                                    0.6f
                                ) togetherWith fadeOut(tween(300, 0, LinearEasing), 0f)
                            } else (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                    scaleIn(
                                        initialScale = 0.92f,
                                        animationSpec = tween(220, delayMillis = 90)
                                    ))
                                .togetherWith(fadeOut(animationSpec = tween(90)))
                        },
                        contentKey = { targetState ->
                            if (targetState is WaterAppUiState.UserData) {
                                "UserData"
                            } else targetState.toString()
                        }
                    ) { uiState ->
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
                                    haveNotifications = viewState.notificationData.switch,
                                    intervals = viewState.reminderIntervals,
                                    userData = viewState.userData,
                                    showParameters = viewState.notificationData.firstShow,
                                    onReminderIntervalClick = { reminderInterval ->
                                        viewModel.selectReminderInterval(reminderInterval)
                                    },
                                    onHaveNotificationsChange = {
                                        viewModel.checkHaveNotifications()
                                    },
                                    onSettingsSaveClick = {
                                        viewModel.saveSettingsNotifications()
                                    },
                                    onCloseClick = {
                                        viewModel.goToWaterApp()
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
                                    userData = viewState.userData,
                                    showParameters = viewState.notificationData.started && viewState.notificationData.firstShow,
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

                    LaunchedEffect(Unit) {
                        if (!haveNotificationPermission()) {
                            viewModel.changeHaveNotifications(false)
                        }
                    }

                    LifecycleEffect {
                        viewModel.observeEvent().collectLatest { event ->
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