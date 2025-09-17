package com.m.vodovoz.feature.profile.waterapp.composables.user_data

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.m.vodovoz.feature.profile.waterapp.composables.WaterAppButton

@Composable
fun WaterAppStageBox(
    modifier: Modifier = Modifier,
    title: String,
    onNextClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            content()
        }

        WaterAppButton(modifier = Modifier.padding(bottom = 20.dp), onClick = onNextClick)
    }
}