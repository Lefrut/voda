package com.vodovoz.app.feature.profile.waterapp.composables.user_data

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper
import com.vodovoz.app.feature.profile.waterapp.composables.VodovozWheelPicker
import com.vodovoz.app.util.extensions.indexOfOrNull

@Composable
fun WaterAppTimeStage(
    modifier: Modifier = Modifier,
    time: String,
    isSleepTime: Boolean,
    onWakeUpTimeChange: (String) -> Unit,
    onSleepTimeChange: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

        Image(
            painter = painterResource(id = if (isSleepTime) R.drawable.pic_night else R.drawable.pic_morning),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 24.dp)
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )

        Text(
            modifier = Modifier.padding(top = 24.dp, bottom = 44.dp),
            text = time,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displayLarge
        )

        if (isSleepTime) {
            VodovozWheelPicker(
                modifier = Modifier.padding(horizontal = 16.dp),
                items = WaterAppHelper.times,
                markedNumber = 4,
                initialIndex = WaterAppHelper.times.indexOfOrNull(time) ?: 0,
                onMiddleItemChange = onSleepTimeChange
            )

        } else {
            VodovozWheelPicker(
                modifier = Modifier.padding(horizontal = 16.dp),
                items = WaterAppHelper.times,
                markedNumber = 4,
                initialIndex = WaterAppHelper.times.indexOfOrNull(time) ?: 0,
                onMiddleItemChange = onWakeUpTimeChange
            )

        }

        Spacer(Modifier.weight(1f))
    }
}