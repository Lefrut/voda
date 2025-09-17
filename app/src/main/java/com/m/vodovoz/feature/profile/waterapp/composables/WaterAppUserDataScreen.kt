package com.m.vodovoz.feature.profile.waterapp.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.feature.profile.waterapp.WaterAppHelper
import com.m.vodovoz.feature.profile.waterapp.composables.user_data.WaterAppActivityStage
import com.m.vodovoz.feature.profile.waterapp.composables.user_data.WaterAppGenderStage
import com.m.vodovoz.feature.profile.waterapp.composables.user_data.WaterAppHeightStage
import com.m.vodovoz.feature.profile.waterapp.composables.user_data.WaterAppStageBox
import com.m.vodovoz.feature.profile.waterapp.composables.user_data.WaterAppTimeStage
import com.m.vodovoz.feature.profile.waterapp.composables.user_data.WaterAppWeightStage
import com.m.vodovoz.feature.profile.waterapp.model.WaterAppActivityLevelUi
import com.m.vodovoz.feature.profile.waterapp.model.WaterAppUiState

@Composable
fun WaterAppUserDataScreen(
    modifier: Modifier = Modifier,
    userDataStage: WaterAppUiState.UserData,
    userInfo: WaterApp.UserInfo,
    notificationSettings: WaterApp.NotificationSettings,
    hideTopBar: Boolean,
    onGenderSelect: (isMan: Boolean) -> Unit,
    onActivityLevelSelect: (WaterAppActivityLevelUi) -> Unit,
    onWeightSelect: (Float) -> Unit,
    onHeightSelect: (Int) -> Unit,
    onWakeUpTimeChange: (String) -> Unit,
    onSleepTimeChange: (String) -> Unit,
    onBackClick: (WaterAppUiState.UserData) -> Unit,
    onCloseClick: () -> Unit,
    onNextClick: (WaterAppUiState.UserData) -> Unit,
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        if (!hideTopBar) {
            WaterAppUserDataTopBar(
                currentStage = userDataStage,
                onBackClick = { onBackClick(userDataStage) },
                onCloseClick = onCloseClick
            )
        }
        AnimatedContent(
            targetState = userDataStage,
            label = "Animated UserDataStages",
            transitionSpec = {
                val animationSpec = tween<IntOffset>(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )

                if (targetState > initialState) {
                    (slideInHorizontally(animationSpec) { it } +
                            scaleIn(initialScale = 0.35f)) togetherWith (slideOutHorizontally(
                        animationSpec
                    ) { -it } + scaleOut(
                        targetScale = 1.00f
                    ))
                } else {
                    (slideInHorizontally(animationSpec) { -it } +
                            scaleIn(initialScale = 0.35f)) togetherWith
                            (slideOutHorizontally(animationSpec) { it } +
                                    scaleOut(targetScale = 1.0f))
                }.using(SizeTransform(clip = false))
            }
        ) { state ->
            when (state) {
                WaterAppUiState.UserData.Gender -> {
                    WaterAppStageBox(
                        title = stringResource(R.string.you_sex),
                        onNextClick = { onNextClick(state) },
                        content = {
                            WaterAppGenderStage(
                                onGenderSelect = { isMan -> onGenderSelect(isMan) },
                                isMan = userInfo.gender == WaterApp.Gender.Man
                            )
                        }
                    )
                }

                WaterAppUiState.UserData.Height -> {
                    WaterAppStageBox(
                        title = stringResource(R.string.you_height),
                        onNextClick = { onNextClick(state) },
                        content = {
                            WaterAppHeightStage(
                                height = userInfo.height.toInt(),
                                onHeightSelect = onHeightSelect
                            )
                        }
                    )
                }

                WaterAppUiState.UserData.Weight -> {
                    WaterAppStageBox(
                        title = stringResource(R.string.you_weight),
                        onNextClick = { onNextClick(state) },
                        content = {
                            WaterAppWeightStage(
                                weight = userInfo.weight,
                                onWeightChange = onWeightSelect
                            )
                        }
                    )
                }

                WaterAppUiState.UserData.WakeUpTime, WaterAppUiState.UserData.SleepTime -> {
                    val isSleepTime = state == WaterAppUiState.UserData.SleepTime
                    WaterAppStageBox(
                        title = if (isSleepTime) stringResource(R.string.sleep_time) else stringResource(
                            R.string.wake_up_time
                        ),
                        onNextClick = { onNextClick(state) },
                        content = {
                            val time =
                                (if (isSleepTime) notificationSettings.sleepTime else notificationSettings.wakeUpTime)
                            WaterAppTimeStage(
                                time = time.format(WaterAppHelper.timeFormatter),
                                isSleepTime = isSleepTime,
                                onWakeUpTimeChange = onWakeUpTimeChange,
                                onSleepTimeChange = onSleepTimeChange
                            )
                        }
                    )
                }

                WaterAppUiState.UserData.ActivityLevel -> {
                    WaterAppStageBox(
                        title = stringResource(R.string.activity_level),
                        onNextClick = { onNextClick(state) },
                        content = {
                            WaterAppActivityStage(
                                currentActivityLevel = WaterAppActivityLevelUi.getByValue(userInfo.activityLevel.sport.toString()),
                                onActivityClick = onActivityLevelSelect
                            )
                        }
                    )
                }
            }
        }
    }

    BackHandler {
        onBackClick(userDataStage)
    }
}

@Composable
fun WaterAppUserDataTopBar(
    modifier: Modifier = Modifier,
    currentStage: WaterAppUiState.UserData,
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    val stages = remember { WaterAppUiState.UserData.entries }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_left),
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(onClick = onBackClick),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
        ) {

            stages.forEachIndexed { index, stage ->
                val stageIndicatorColor by
                animateColorAsState(
                    targetValue = if (index <= currentStage.ordinal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    label = "IndicatorColorAnimation"
                )

                key(stage) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(stageIndicatorColor),
                    )
                }
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.ic_close),
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(onClick = onCloseClick),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}