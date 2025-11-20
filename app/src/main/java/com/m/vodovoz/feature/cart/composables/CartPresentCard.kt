package com.m.vodovoz.feature.cart.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.m.vodovoz.R
import com.m.vodovoz.design_system.ExtendedTheme
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.button.VodovozButtonSmall
import com.m.vodovoz.feature.cart.model.CartPresentUi

@Composable
fun CartPresentCard(
    modifier: Modifier = Modifier,
    present: CartPresentUi,
    onChoosePresentClick: () -> Unit,
) {
    val density = LocalDensity.current

    var contentHeight by remember {
        mutableStateOf(0.dp)
    }

    BoxWithConstraints(
        modifier = modifier,
        propagateMinConstraints = true
    ) {
        val maxWidth = maxWidth
        val horizontalPadding = 16.dp
        val verticalPadding = 10.dp

        val button = present.button

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .zIndex(1f)
                    .onSizeChanged { intSize ->
                        contentHeight = with(density) {
                            intSize.height.toDp()
                        }
                    }
                    .padding(
                        top = verticalPadding,
                        start = horizontalPadding,
                        bottom = verticalPadding
                    )
            ) {
                Text(
                    modifier = Modifier.padding(end = 20.dp),
                    text = present.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = 20.sp
                    ),
                )
                Text(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .wrapContentWidth(Alignment.Start, true)
                        .fillMaxWidth(),
                    text = AnnotatedString.fromHtml(present.description),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodySmall
                )

                if (button != null) {
                    VodovozButtonSmall(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .wrapContentWidth(Alignment.Start, true),
                        onClick = onChoosePresentClick,
                        colors = ButtonDefaults.buttonColors(
                            contentColor = button.textColor,
                            containerColor = button.backgroundColor,
                            disabledContainerColor = button.backgroundColor.copy(0.45f),
                            disabledContentColor = button.textColor.copy(0.75f)
                        ),
                        enabled = button.enabled
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                modifier = Modifier,
                                text = button.name,
                                style = ExtendedTheme.typography.buttonSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_right),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                } else {
                    val animatedProgress by animateFloatAsState(
                        targetValue = present.currentPresentPrice.toFloat() / present.maxPresentPrice,
                        label = "animated present progress",
                        animationSpec = tween(160)
                    )

                    Row(
                        modifier = Modifier
                            .wrapContentWidth(Alignment.Start, true)
                            .width(maxWidth - (horizontalPadding + horizontalPadding)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .padding(vertical = 6.dp)
                                .weight(1f)
                                .height(8.dp)
                                .clip(MaterialTheme.shapes.small),
                            strokeCap = StrokeCap.Square,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.background,
                            progress = { animatedProgress },
                            drawStopIndicator = {}
                        )


                        Icon(
                            painter = painterResource(id = R.drawable.ic_info),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(20.dp)
                        )
                    }

                }
            }

            val imageTopPaddingDp = 10.dp

            AsyncImage(
                modifier = Modifier
                    .align(
                        if (button == null) Alignment.Top
                        else Alignment.Bottom
                    )
                    .then(
                        if (button == null) Modifier.padding(end = 24.dp, top = 10.dp)
                        else Modifier.padding(end = 10.dp, top = imageTopPaddingDp)
                    )
                    .then(
                        if (button == null) Modifier.size(width = 70.dp, height = 64.dp)
                        else Modifier
                            .width(82.dp)
                            .height((contentHeight - imageTopPaddingDp).coerceAtMost(110.dp))

                    )
                    .zIndex(0f),
                model = present.image,
                contentDescription = null,
                contentScale = ContentScale.FillBounds
            )
        }
    }
}

@Preview
@Composable
private fun CartPresentCardPreview() {
    val cartPresent = CartPresentUi(
        id = 1,
        title = "123".repeat(10) ,
        description = "123",
        image = "",
        currentPresentPrice = 10,
        maxPresentPrice = 100,
        button = null,
        popupWindow = null
    )
    VodovozTheme {
        CartPresentCard(present = cartPresent) { }
    }
}