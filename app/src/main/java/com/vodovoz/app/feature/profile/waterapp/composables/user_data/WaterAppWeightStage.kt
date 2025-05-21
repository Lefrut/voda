package com.vodovoz.app.feature.profile.waterapp.composables.user_data

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.R
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper
import com.vodovoz.app.feature.profile.waterapp.composables.VodovozWheelPicker
import com.vodovoz.app.util.extensions.indexOfOrNull
import kotlin.math.roundToInt

@Composable
fun WaterAppWeightStage(
    modifier: Modifier = Modifier,
    weight: Float,
    onWeightChange: (Float) -> Unit,
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(Modifier.weight(0.82f))

        Text(
            modifier = Modifier,
            text = weight.toString(),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displayLarge
        )

        val bodyMedium = MaterialTheme.typography.bodyMedium

        Text(
            modifier = Modifier
                .padding(top = 1.dp)
                .height(25.dp)
                .wrapContentSize(Alignment.Center),
            text = stringResource(R.string.kg),
            color = MaterialTheme.colorScheme.surfaceTint,
            style = bodyMedium.copy(lineHeight = bodyMedium.fontSize, letterSpacing = 0.sp)
        )


        Spacer(modifier = Modifier.weight(0.71f))

        val weights = remember {
            WaterAppHelper.weights
        }

        VodovozWheelPicker(
            modifier = Modifier.padding(horizontal = 20.dp),
            items = weights,
            initialIndex = weights.indexOfOrNull(weight) ?: 0,
            itemText = { item ->
                item.roundToInt().toString()
            },
            onMiddleItemChange = onWeightChange
        )

        Spacer(modifier = Modifier.weight(0.84f))
    }
}