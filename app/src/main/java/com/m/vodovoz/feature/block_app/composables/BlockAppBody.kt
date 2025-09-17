package com.m.vodovoz.feature.block_app.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.m.vodovoz.R
import com.m.vodovoz.design_system.robotoFontFamily
import com.m.vodovoz.feature.block_app.model.BlockAppContactUi

@Suppress("NonSkippableComposable")
@Composable
fun BlockAppBody(
    modifier: Modifier = Modifier,
    image: String,
    description: String,
    contacts: List<BlockAppContactUi>,
    showTime: Boolean,
    timeDays: String,
    timeHours: String,
    timeMinutes: String,
    timeSeconds: String,
    onContactClick: (BlockAppContactUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = image,
            contentDescription = null,
            modifier = Modifier
                .padding(
                    start = 32.dp,
                    end = 32.dp,
                    top = 16.dp,
                    bottom = 60.dp
                )
                .heightIn(max = 100.dp)
        )

        AnimatedVisibility(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 28.dp
            ),
            visible = showTime
        ) {
            TimeDataRow(
                days = timeDays,
                hours = timeHours,
                minutes = timeMinutes,
                seconds = timeSeconds
            )
        }

        if (description.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = description,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

        }

        FlowRow(
            modifier = Modifier
                .padding(top = 32.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            contacts.forEach { contact ->
                AsyncImage(
                    model = contact.image,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .pointerInput(Unit){
                            detectTapGestures {
                                onContactClick(contact)
                            }
                        },
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}


@Composable
fun TimeDataRow(
    modifier: Modifier = Modifier,
    days: String,
    hours: String,
    minutes: String,
    seconds: String,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val timeCardModifier = Modifier.weight(1f)

        TimeCard(
            modifier = timeCardModifier,
            label = stringResource(R.string.block_days),
            value = days
        )
        TimeCard(
            modifier = timeCardModifier,
            label = stringResource(R.string.block_hours),
            value = hours
        )
        TimeCard(
            modifier = timeCardModifier,
            label = stringResource(R.string.block_minutes),
            value = minutes
        )
        TimeCard(
            modifier = timeCardModifier,
            label = stringResource(R.string.block_seconds),
            value = seconds
        )
    }
}

@Composable
private fun TimeCard(
    modifier: Modifier,
    label: String,
    value: String,
) {
    Card(
        shape = MaterialTheme.shapes.small,
        modifier = modifier
            .padding(top = 4.dp, bottom = 4.dp, end = 2.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontFamily = robotoFontFamily,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                letterSpacing = 1.5.sp,
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 33.sp,
                fontFamily = robotoFontFamily,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.End
            )
        }
    }
}