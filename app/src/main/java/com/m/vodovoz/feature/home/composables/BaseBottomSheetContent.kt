package com.m.vodovoz.feature.home.composables

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.decoration.SkeletonBox
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.vodovozTextLinkStyle
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer

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

        val asyncPainter = rememberAsyncImagePainter(model = picture)
        val asyncPainterState by asyncPainter.state.collectAsStateWithLifecycle()

        if (picture.isNotBlank()) {

            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .animateContentSize(),
            ) {
                when (asyncPainterState) {
                    is AsyncImagePainter.State.Loading, is AsyncImagePainter.State.Empty -> {
                        SkeletonBox(
                            modifier = Modifier
                                .height(220.dp)
                                .fillMaxWidth(),
                            shimmerState = rememberShimmer(ShimmerBounds.View)
                        )
                    }

                    is AsyncImagePainter.State.Success -> {
                        Image(
                            painter = asyncPainter,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .clip(MaterialTheme.shapes.large),
                            contentScale = ContentScale.FillWidth
                        )
                    }

                    is AsyncImagePainter.State.Error -> {

                    }
                }

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