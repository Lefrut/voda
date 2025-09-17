package com.m.vodovoz.feature.bottom.services.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil3.compose.AsyncImage
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.utils.toAnnotatedString
import com.m.vodovoz.design_system.vodovozTextLinkStyle
import com.m.vodovoz.feature.bottom.services.model.ServiceUi
import com.m.vodovoz.feature.home.composables.dropShadow

@Suppress("NonSkippableComposable")
@Composable
fun AboutServicesBody(
    modifier: Modifier = Modifier,
    descriptionHtml: String,
    services: List<ServiceUi>,
    onServiceClick: (ServiceUi) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val linkStyle = vodovozTextLinkStyle
        val annotatedString = remember(descriptionHtml) {
            HtmlCompat.fromHtml(
                descriptionHtml,
                HtmlCompat.FROM_HTML_MODE_LEGACY,
            ).toAnnotatedString(linkStyle)
        }



        Text(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
            text = annotatedString,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium
        )

        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            services.forEach { service ->
                ServiceCard(
                    service = service,
                    onClick = onServiceClick
                )
            }
        }
    }
}

@Composable
private fun ServiceCard(
    modifier: Modifier = Modifier,
    service: ServiceUi,
    onClick: (ServiceUi) -> Unit,
) {
    Box(
        Modifier.dropShadow(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.onBackground.copy(0.2f),
            offsetY = 1.dp,
            blur = 5.dp
        )
    ) {
        Column(
            modifier = modifier
                .clip(MaterialTheme.shapes.medium)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                )
                .clickable { onClick(service) }
        ) {
            AsyncImage(
                model = service.image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.8f),
                contentScale = ContentScale.Crop
            )

            Text(
                modifier = Modifier.padding(
                    start = 12.dp,
                    top = 10.dp,
                    end = 16.dp,
                    bottom = 8.dp
                ),
                text = service.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Preview
@Composable
private fun ServiceCardPreview() {
    VodovozTheme {
        ServiceCard(service = ServiceUi("Rent water", "", -1)) {}
    }
}