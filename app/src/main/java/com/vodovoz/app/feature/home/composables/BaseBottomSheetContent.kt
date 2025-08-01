package com.vodovoz.app.feature.home.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.vodovozTextLinkStyle

@Composable
fun BaseBottomSheetContent(
    modifier: Modifier = Modifier,
    name: String,
    picture: String,
    description: String,
    button: ColorfulButtonUi,
    onButtonClick: (ColorfulButtonUi) -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = name,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium
        )
        val painter = rememberAsyncImagePainter(picture)
        val painterState by painter.state.collectAsStateWithLifecycle()

        if (painterState is AsyncImagePainter.State.Success) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopStart
            )
        }

        if (description.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = AnnotatedString.fromHtml(
                    htmlString = description,
                    linkStyles = vodovozTextLinkStyle
                ),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall
            )
        }

        VodovozButtonsColumn(
            modifier = Modifier.padding(top = 20.dp, bottom = 18.dp),
            buttons = listOf(button)
        ) {
            onButtonClick(it)
        }
    }

}