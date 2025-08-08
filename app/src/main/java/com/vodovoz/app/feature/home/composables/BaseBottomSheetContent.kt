package com.vodovoz.app.feature.home.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.decoration.SkeletonBox
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.vodovozTextLinkStyle

@Composable
fun BaseBottomSheetContent(
    modifier: Modifier = Modifier,
    name: String,
    picture: String,
    description: String,
    button: ColorfulButtonUi?,
    onButtonClick: (ColorfulButtonUi) -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
            text = name,
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium
        )

        if (picture.isNotBlank()) {
            SkeletonBox(
                modifier = Modifier
                    .padding(top = 8.dp),
                shimmerState = rememberShimmer(ShimmerBounds.Window)
            ) {
                AsyncImage(
                    model = picture,
                    contentDescription = null,
                    modifier = Modifier
                        .height(220.dp)
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large),
                    contentScale = ContentScale.FillBounds
                )
            }
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

        button?.let {
            VodovozButtonsColumn(
                modifier = Modifier.padding(top = 20.dp, bottom = 18.dp),
                buttons = listOf(button)
            ) {
                onButtonClick(it)
            }
        }

    }

}