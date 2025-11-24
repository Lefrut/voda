package com.m.vodovoz.design_system.composables.blur

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.m.vodovoz.R
import dev.chrisbanes.haze.hazeEffect


@Composable
inline fun BlurBox(
    modifier: Modifier = Modifier,
    showBlur: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .then(
                if (showBlur) Modifier.hazeEffect {
                    blurEnabled = true
                    blurRadius = 28.dp
                }
                else Modifier
            )
    ) {
        content()
    }
}

@Composable
inline fun AsyncImageBlur(
    model: String,
    showBlur: Boolean,
    placeholderText: String,
    modifier: Modifier = Modifier,
    image: @Composable (AsyncImagePainter) -> Unit,
) {

    val imagePainter = rememberAsyncImagePainter(model = model)
    val imageState by imagePainter.state.collectAsStateWithLifecycle()

    VodovozBlur(
        modifier = modifier.clip(MaterialTheme.shapes.small),
        showBlur = showBlur && imageState !is AsyncImagePainter.State.Loading,
        placeholderText = placeholderText,
        content = {
            image(imagePainter)
        }
    )
}

@Composable
inline fun VodovozBlur(
    modifier: Modifier = Modifier,
    showBlur: Boolean = true,
    placeholderImage: ImageVector? = ImageVector.vectorResource(R.drawable.ic_no_visibility),
    placeholderText: String = "",
    textStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        BlurBox(showBlur = showBlur) {
            content()
        }
        if (showBlur) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.matchParentSize()
            ) {
                if (placeholderImage != null) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_no_visibility),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                if (placeholderText.isNotBlank()) {
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = placeholderText,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = textStyle,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
