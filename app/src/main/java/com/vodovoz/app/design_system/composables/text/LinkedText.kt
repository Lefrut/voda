package com.vodovoz.app.design_system.composables.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.fromHtml
import com.vodovoz.app.design_system.vodovozTextLinkStyle

@Composable
fun LinkedText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onBackground,
    style: TextStyle = MaterialTheme.typography.labelSmall,
    onUrlClick: (String, Int) -> Unit,

    ) {
    val links = remember {
        mutableStateOf(listOf<String>())
    }

    val annotatedString = AnnotatedString.fromHtml(
        htmlString = text,
        linkStyles = vodovozTextLinkStyle,
        linkInteractionListener = { link ->
            val linkUrl = link as? LinkAnnotation.Url
            linkUrl.toString()
            runCatching {
                val url = linkUrl!!.url
                onUrlClick(linkUrl.url, links.value.indexOf(url))
            }
        }
    )

    Text(
        modifier = modifier,
        text = annotatedString,
        style = style,
        color = color,
    )

    LaunchedEffect(annotatedString) {
        links.value = annotatedString.getLinkAnnotations(0, Int.MAX_VALUE)
            .mapNotNull { (it.item as? LinkAnnotation.Url)?.url }
    }
}
