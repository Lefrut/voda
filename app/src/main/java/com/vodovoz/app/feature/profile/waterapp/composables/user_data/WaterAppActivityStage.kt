package com.vodovoz.app.feature.profile.waterapp.composables.user_data

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.design_system.composables.button.VodovozRadioButton
import com.vodovoz.app.design_system.composables.card.VodovozOutlinedCard
import com.vodovoz.app.feature.profile.waterapp.model.WaterAppActivityLevel

@Composable
fun WaterAppActivityStage(
    modifier: Modifier = Modifier,
    currentActivityLevel: WaterAppActivityLevel,
    onActivityClick: (WaterAppActivityLevel) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(0.5f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            WaterAppActivityLevel.entries.forEach { activityLevel ->
                ActivityLevelCard(
                    selected = currentActivityLevel == activityLevel,
                    activityLevel = activityLevel,
                    onClick = onActivityClick
                )
            }

        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun ActivityLevelCard(
    modifier: Modifier = Modifier,
    selected: Boolean,
    activityLevel: WaterAppActivityLevel,
    onClick: (WaterAppActivityLevel) -> Unit,
) {

    VodovozOutlinedCard(
        modifier = modifier.clickable { onClick(activityLevel) },
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            bottom = 8.dp,
            end = 16.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = activityLevel.imageId),
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(id = activityLevel.titleId),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        lineHeight = 26.sp,
                        letterSpacing = 0.sp
                    )
                )

                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = stringResource(id = activityLevel.descriptionId),
                    color = MaterialTheme.colorScheme.surfaceTint,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 26.sp,
                        letterSpacing = 0.sp
                    )
                )
            }

            VodovozRadioButton(
                selected = selected,
                onClick = { onClick(activityLevel) }
            )
        }
    }
}