package com.vodovoz.app.common.media.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.ExtendedTheme

@Composable
private fun BottomPanelButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color.White,
        style = ExtendedTheme.typography.buttonMedium,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
            .clip(MaterialTheme.shapes.small)
    )

}

@Composable
fun PickerBottomPanel(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Row(
        modifier = modifier,
    ) {
        BottomPanelButton(
            text = stringResource(R.string.back),
            onClick = onBackClick
        )

        Spacer(modifier = Modifier.weight(1f))

        BottomPanelButton(
            text = stringResource(id = R.string.save),
            onClick = onSaveClick
        )
    }

}