package com.m.vodovoz.feature.about_app.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.feature.about_app.model.AboutAppOption

@Composable
fun AboutAppBody(
    modifier: Modifier = Modifier,
    version: String,
    onOptionClick: (AboutAppOption) -> Unit,
    onTripleClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    var pointersDown: Int

                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Final)
                        pointersDown = event.changes.count { it.pressed }

                        if (pointersDown == 3) {
                            onTripleClick()
                        }

                        event.changes.forEach { it.consume() }
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Image(
            painter = painterResource(R.drawable.pic_app),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            color = MaterialTheme.colorScheme.surfaceTint,
            text = stringResource(R.string.app_version_text, version),
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.weight(1f))

        AboutAppOption.entries.forEach { option ->
            AboutAppOptionItem(option = option, onClick = onOptionClick)
        }
    }
}

@Composable
fun AboutAppOptionItem(
    modifier: Modifier = Modifier,
    option: AboutAppOption,
    onClick: (AboutAppOption) -> Unit,
) {
    val borderColor = MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = modifier
            .clickable { onClick(option) }
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
            }
            .padding(16.dp)
    ) {
        Icon(
            painter = painterResource(option.iconId),
            modifier = Modifier
                .padding(end = 16.dp)
                .size(24.dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.surfaceTint
        )

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(option.textId),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}