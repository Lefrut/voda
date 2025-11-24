package com.m.vodovoz.feature.profile.waterapp.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.modifiers.radialGradientBackground
import com.m.vodovoz.design_system.robotoFontFamily
import com.m.vodovoz.feature.profile.waterapp.WaterAppHelper

@Composable
fun WaterAppGoalCompletedScreen(goal: Int, onCloseClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .radialGradientBackground(
                0.6f,
                0.34f,
                0.66f,
                0.66f,
                0f to WaterAppHelper.Colors.lightBlue,
                1f to WaterAppHelper.Colors.darkBlue
            )
            .systemBarsPadding()
            .clickable(onClick = onCloseClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.weight(0.5f).fillMaxWidth()
        ) {

            IconButton(
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
                onClick = onCloseClick
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_close),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.background
                )

            }
        }
        Text(
            modifier = Modifier,
            text = stringResource(R.string.congratulations),
            color = MaterialTheme.colorScheme.background,
            style = TextStyle(
                textAlign = TextAlign.Center,
                letterSpacing = 0.15.sp,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = robotoFontFamily,
                lineHeightStyle = LineHeightStyle(
                    LineHeightStyle.Alignment.Center,
                    LineHeightStyle.Trim.None
                )
            )
        )

        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = stringResource(R.string.you_have_drunk_water_goal),
            color = MaterialTheme.colorScheme.background,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(0.24f))

        Image(
            contentScale = ContentScale.FillBounds,
            painter = painterResource(R.drawable.pic_goal_completed),
            contentDescription = null,
            modifier = Modifier
                .width(300.dp)
                .height(233.dp)
        )

        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = goal.toString(),
            color = MaterialTheme.colorScheme.background,
            style = MaterialTheme.typography.displayLarge
        )

        Text(
            modifier = Modifier.size(40.dp, 25.dp),
            text = stringResource(R.string.ml),
            color = MaterialTheme.colorScheme.background.copy(0.8f),
            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(0.65f))

        Text(
            modifier = Modifier
                .padding(bottom = 44.dp)
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onCloseClick)
                .padding(10.dp),
            text = stringResource(R.string.see_you_tomorrow),
            color = MaterialTheme.colorScheme.background.copy(0.8f),
            style = MaterialTheme.typography.bodySmall.copy(
                lineHeight = 26.sp,
                letterSpacing = 0.15.sp
            )

        )

    }
}