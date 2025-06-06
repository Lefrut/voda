package com.vodovoz.app.design_system.composables.decoration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.feature.home.composables.dropShadow

@Composable
fun MapIcon(
    modifier: Modifier = Modifier,
    iconId: Int,
    tint: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .dropShadow(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onBackground,
                blur = 2.dp,
                offsetY = 1.dp,
                offsetX = 0.dp
            )
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = iconId),
            contentDescription = null,
            tint = tint
        )
    }
}

@Composable
fun MapIconsColumn(
    modifier: Modifier,
    onZoomPlus: () -> Unit,
    onZoomMinus: () -> Unit,
    onGeoClick: () -> Unit
){
    Column(
        modifier = modifier
            .padding(top = 8.dp, end = 20.dp)
    ) {
        MapIcon(
            iconId = R.drawable.ic_plus,
            onClick = onZoomPlus
        )

        MapIcon(
            modifier = Modifier.padding(top = 8.dp),
            iconId = R.drawable.ic_minus,
            onClick = onZoomMinus
        )

        MapIcon(
            modifier = Modifier.padding(top = 32.dp),
            iconId = R.drawable.ic_geo,
            tint = MaterialTheme.colorScheme.primary,
            onClick = onGeoClick
        )

    }

}