package com.m.vodovoz.feature.profile.waterapp.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.vodovoz.R
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.swich.vodovozColors
import com.m.vodovoz.design_system.composables.top_bar.ClosingTopBar
import com.m.vodovoz.design_system.robotoFontFamily
import com.m.vodovoz.feature.profile.waterapp.WaterAppHelper
import com.m.vodovoz.feature.profile.waterapp.model.ReminderIntervalUi
import com.m.vodovoz.feature.profile.waterapp.model.WaterAppUiState
import com.m.vodovoz.util.toIntRoundOrNull

@NonRestartableComposable
@Composable
fun WaterAppSettingsScreen(
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit,
    intervals: List<ReminderIntervalUi>,
    userInfo: WaterApp.UserInfo,
    notificationSettings: WaterApp.NotificationSettings,
    showParameters: Boolean,
    onReminderIntervalClick: (ReminderIntervalUi) -> Unit,
    onHaveNotificationsChange: () -> Unit,
    onSettingsSaveClick: () -> Unit,
    onEditUserData: (WaterAppUiState.UserData) -> Unit,
) {
    BackHandler {
        onCloseClick()
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        topBar = {
            ClosingTopBar(
                title = stringResource(id = R.string.settings),
                onCloseClick = onCloseClick
            )
        },
        bottomBar = {
            VodovozButton(
                modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                text = stringResource(id = R.string.save),
                onClick = onSettingsSaveClick
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(bottom = paddingValues.calculateBottomPadding(), top = 16.dp)
                .fillMaxSize(),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.notifications),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
            Row(
                modifier = Modifier
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                    .clip(RectangleShape),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(id = R.string.water_reminder_message),
                    color = MaterialTheme.colorScheme.surfaceTint,
                    style = MaterialTheme.typography.bodySmall
                )
                Switch(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .requiredHeight(32.dp),
                    checked = notificationSettings.enableNotifications,
                    onCheckedChange = { onHaveNotificationsChange() },
                    colors = SwitchDefaults.vodovozColors()
                )
            }



            AnimatedVisibility(notificationSettings.enableNotifications) {
                Column(modifier = Modifier) {
                    Text(
                        modifier = Modifier.padding(top = 32.dp, start = 16.dp, end = 16.dp),
                        text = stringResource(R.string.reminder_interval),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.headlineSmall
                    )


                    BoxWithConstraints(
                        modifier = Modifier.padding(
                            top = 16.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                    ) {
                        val maxItemsInEachRow = 4
                        val cardWidth =
                            (maxWidth - 8.dp * (maxItemsInEachRow - 1)) / maxItemsInEachRow

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            maxItemsInEachRow = maxItemsInEachRow,
                            horizontalArrangement = Arrangement.spacedBy(
                                8.dp,
                                Alignment.CenterHorizontally
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            intervals.forEach { interval ->
                                key(interval.minutes) {
                                    ReminderCard(
                                        modifier = Modifier
                                            .width(cardWidth - 0.5.dp)
                                            .height(cardWidth * 0.85f),
                                        reminderIntervalUi = interval,
                                        onReminderIntervalClick = onReminderIntervalClick,
                                        selected = interval.minutes == notificationSettings.notificationsDelay.inWholeMinutes
                                    )
                                }
                            }
                        }

                    }
                }
            }

            if (showParameters) {
                Text(
                    modifier = Modifier.padding(
                        top = 32.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    ),
                    text = stringResource(R.string.setting_params),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall
                )

                val parameters =
                    WaterAppUiState.UserData.entries - WaterAppUiState.UserData.Gender - WaterAppUiState.UserData.ActivityLevel

                parameters.forEachIndexed { index, param ->
                    ParameterItem(
                        userInfo = userInfo,
                        wakeUpTimeText = notificationSettings.wakeUpTime.format(
                            WaterAppHelper.timeFormatter
                        ),
                        sleepTimeText = notificationSettings.sleepTime.format(
                            WaterAppHelper.timeFormatter
                        ),
                        uiState = param,
                        onEditClick = onEditUserData
                    )
                    if (index != parameters.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }

    }
}

@Composable
private fun ParameterItem(
    uiState: WaterAppUiState.UserData,
    userInfo: WaterApp.UserInfo,
    wakeUpTimeText: String,
    sleepTimeText: String,
    onEditClick: (WaterAppUiState.UserData) -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable { onEditClick(uiState) }
            .padding(vertical = 10.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = when (uiState) {
                WaterAppUiState.UserData.Height -> painterResource(id = R.drawable.pic_height)
                WaterAppUiState.UserData.Weight -> painterResource(id = R.drawable.pic_weight)
                WaterAppUiState.UserData.WakeUpTime -> painterResource(id = R.drawable.ic_sun)
                WaterAppUiState.UserData.SleepTime -> painterResource(id = R.drawable.pic_moon)
                else -> {
                    painterResource(id = R.drawable.ic_sun)
                }
            },
            contentDescription = null,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(40.dp),
            contentScale = ContentScale.FillBounds
        )
        Text(
            modifier = Modifier.weight(1f),
            text = when (uiState) {
                WaterAppUiState.UserData.Height -> stringResource(
                    R.string.quantity_cm,
                    userInfo.height
                )

                WaterAppUiState.UserData.Weight -> stringResource(
                    R.string.quantity_kg,
                    userInfo.weight
                )

                WaterAppUiState.UserData.WakeUpTime -> wakeUpTimeText
                WaterAppUiState.UserData.SleepTime -> sleepTimeText

                else -> ""
            },
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_edit),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable { onEditClick(uiState) },
            tint = MaterialTheme.colorScheme.surfaceTint
        )
    }
}


@Composable
private fun ReminderCard(
    modifier: Modifier = Modifier,
    reminderIntervalUi: ReminderIntervalUi,
    onReminderIntervalClick: (ReminderIntervalUi) -> Unit,
    selected: Boolean,
) {
    val showHours = reminderIntervalUi.shouldDisplayIntervalAsHours()
    val valueString = reminderIntervalUi.format()
    val valueInt = valueString.toIntRoundOrNull()

    val contentColor = if (!selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.background
    }
    val containerColor = if (!selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.primary
    }


    val text = when {
        valueInt == null && showHours -> stringResource(R.string.hours)
        valueInt == null && !showHours -> stringResource(R.string.minutes)
        valueInt != null && showHours -> pluralStringResource(
            R.plurals.hours_count,
            valueInt
        )

        valueInt != null && !showHours -> pluralStringResource(
            id = R.plurals.minutes_count,
            valueInt
        )

        else -> ""
    }


    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable {
                onReminderIntervalClick(reminderIntervalUi)
            }
            .background(color = containerColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = valueString.takeIf { valueInt == null } ?: valueInt.toString(),
            color = contentColor,
            style = ReminderCardDefaults.titleStyle
        )
        Text(
            text = text,
            color = contentColor,
            style = ReminderCardDefaults.textStyle
        )
    }
}

private data object ReminderCardDefaults {

    val titleStyle = TextStyle(
        fontSize = 18.sp,
        letterSpacing = 0.1.sp,
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Medium,
        lineHeightStyle = LineHeightStyle(
            LineHeightStyle.Alignment.Center,
            LineHeightStyle.Trim.None
        ),
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        ),
        lineHeight = 20.sp
    )

    val textStyle = TextStyle(
        fontSize = 14.sp,
        letterSpacing = 0.1.sp,
        fontFamily = robotoFontFamily,
        fontWeight = FontWeight.Medium,
        lineHeightStyle = LineHeightStyle(
            LineHeightStyle.Alignment.Center,
            LineHeightStyle.Trim.None
        ),
        platformStyle = PlatformTextStyle(
            includeFontPadding = false
        ),
        lineHeight = 17.sp
    )


}

