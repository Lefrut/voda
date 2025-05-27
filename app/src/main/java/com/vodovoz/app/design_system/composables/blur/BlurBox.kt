package com.vodovoz.app.design_system.composables.blur

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect


private val blurStyle: HazeStyle
    @Composable
    get() {
        val containerColor = MaterialTheme.colorScheme.surface
        return HazeStyle(
            blurRadius = 30.dp,
            backgroundColor = containerColor,
            tint = HazeTint(
                containerColor.copy(alpha = 0.15f),
            ),
        )
    }

@Composable
fun BlurBox(
    modifier: Modifier = Modifier,
    showBlur: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.then(
            if (showBlur) Modifier.hazeEffect(blurStyle)
            else Modifier
        ),
        content = content
    )
}


@Composable
fun VodovozBlur(
    modifier: Modifier = Modifier,
    showBlur: Boolean = true,
    text: String = "",
    textStyle: TextStyle = MaterialTheme.typography.labelSmall,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        BlurBox(showBlur = showBlur) {
            content()
        }
        if (showBlur) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_no_visibility),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = text,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = textStyle,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

