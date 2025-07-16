package com.vodovoz.app.feature.about_product.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.model.ContentBlockUi
import com.vodovoz.app.design_system.utils.toAnnotatedString
import com.vodovoz.app.util.extensions.fromHtml

@Composable
fun DescriptionTabContent(modifier: Modifier = Modifier, description: ContentBlockUi<String>) {

    Text(
        modifier = modifier.padding(16.dp),
        text = description.content.fromHtml().toAnnotatedString(),
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyMedium
    )
}