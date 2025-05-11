package com.vodovoz.app.design_system.composables.swich

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


@Composable
fun SwitchDefaults.vodovozColors() = colors(
        uncheckedTrackColor = Color.Transparent,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor = MaterialTheme.colorScheme.primary,
        checkedThumbColor = MaterialTheme.colorScheme.background,
        checkedBorderColor = MaterialTheme.colorScheme.primary,
        uncheckedBorderColor = MaterialTheme.colorScheme.primary
    )
