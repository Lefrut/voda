package com.m.vodovoz.feature.search.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchEmptyPlaceholder(
    modifier: Modifier = Modifier,
    imagePainter: Painter,
    description: String,
    title: String,
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

        Spacer(modifier = Modifier.weight(0.62f))

        Image(
            painter = imagePainter,
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
        )

        val textModifier = Modifier
            .padding(top = 24.dp)
            .padding(horizontal = 32.dp)

        if (title.isNotBlank()) {
            Text(
                modifier = textModifier,
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

        }


        if (description.isNotBlank()) {
            Text(
                modifier = textModifier,
                text = AnnotatedString.fromHtml(description),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 0.sp
                ),
                textAlign = TextAlign.Center
            )
        }


        Spacer(modifier = Modifier.weight(1.09f))

    }
}