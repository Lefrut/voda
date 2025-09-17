package com.m.vodovoz.design_system.composables.decoration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R

@Composable
fun PasswordIcon(
    modifier: Modifier = Modifier,
    valueIsVisible: Boolean,
    onClick: (Boolean) -> Unit
) {
    Icon(
        painter = if (!valueIsVisible) painterResource(id = R.drawable.ic_closed_eye)
        else painterResource(id = R.drawable.ic_open_eye),
        contentDescription = null,
        modifier = modifier
            .padding(start = 16.dp)
            .size(24.dp)
            .clip(CircleShape)
            .clickable {
                onClick(!valueIsVisible)
            },
        tint = MaterialTheme.colorScheme.primary
    )
}