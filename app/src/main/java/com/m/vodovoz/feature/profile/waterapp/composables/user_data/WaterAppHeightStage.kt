package com.m.vodovoz.feature.profile.waterapp.composables.user_data

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.vodovoz.R
import com.m.vodovoz.feature.profile.waterapp.composables.VodovozWheelPickerVertical
import com.m.vodovoz.feature.profile.waterapp.model.WaterAppUiState
import com.m.vodovoz.util.extensions.indexOfOrNull

@Composable
fun WaterAppHeightStage(
    modifier: Modifier = Modifier,
    height: Int,
    onHeightSelect: (Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 52.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.weight(0.67f))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

            Spacer(Modifier.weight(0.52f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.widthIn(86.dp),
                    text = height.toString(),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.displayLarge,
                    textAlign = TextAlign.Center
                )

                val bodyMedium = MaterialTheme.typography.bodyMedium

                Text(
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .height(25.dp)
                        .wrapContentSize(Alignment.Center),
                    text = stringResource(R.string.cm),
                    color = MaterialTheme.colorScheme.surfaceTint,
                    style = bodyMedium.copy(lineHeight = bodyMedium.fontSize, letterSpacing = 0.sp)
                )
            }

            Spacer(Modifier.weight(0.32f))


            val heights = WaterAppUiState.UserData.heights.reversed()

            VodovozWheelPickerVertical(
                modifier = Modifier,
                items = heights,
                initialIndex = heights.indexOfOrNull(height) ?: 0,
                onMiddleItemChange = onHeightSelect
            )

            Spacer(Modifier.weight(0.32f))

        }

        Spacer(modifier = Modifier.weight(0.67f))

    }
}