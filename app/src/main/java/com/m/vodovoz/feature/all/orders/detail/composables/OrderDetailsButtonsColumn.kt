package com.m.vodovoz.feature.all.orders.detail.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.m.vodovoz.design_system.ExtendedTheme
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.button.VodovozButtonDefaults
import com.m.vodovoz.design_system.composables.button.VodovozButtonSmall
import com.m.vodovoz.design_system.composables.button.VodovozOutlineButton
import com.m.vodovoz.feature.all.orders.detail.model.OrderDetailsButtonUi

@Suppress("NonSkippableComposable")
@Composable
fun OrderDetailsButtonsColumn(
    modifier: Modifier = Modifier,
    topButtons: List<OrderDetailsButtonUi>,
    onButtonClick: (OrderDetailsButtonUi) -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        topButtons.forEach { button ->
            when (button) {
                is OrderDetailsButtonUi.OutlineButton -> {
                    VodovozOutlineButton(
                        imagePainter = rememberAsyncImagePainter(model = button.image),
                        name = button.name,
                        description = button.description,
                        onClick = { onButtonClick(button) }
                    )
                }

                is OrderDetailsButtonUi.ImageButton -> {
                    ImageOrderButton(button = button) {
                        onButtonClick(button)
                    }
                }

                is OrderDetailsButtonUi.Button -> {
                    val buttonColors = VodovozButtonDefaults.colors(
                        contentColor = button.textColor,
                        containerColor = button.backgroundColor
                    )

                    if (button.isSmall) {
                        VodovozButtonSmall(
                            text = button.name,
                            colors = buttonColors,
                            onClick = { onButtonClick(button) }
                        )
                    } else {
                        VodovozButton(
                            text = button.name,
                            colors = buttonColors,
                            onClick = { onButtonClick(button) }
                        )
                    }
                }
            }
        }

    }
}

@Composable
private fun ImageOrderButton(
    modifier: Modifier = Modifier,
    button: OrderDetailsButtonUi.ImageButton,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp),
        shape = MaterialTheme.shapes.large,
        colors = VodovozButtonDefaults.colors(
            containerColor = button.backgroundColor,
            contentColor = button.textColor
        )
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = button.image),
            contentDescription = null,
            modifier = Modifier
                .padding(end = 8.dp)
                .size(24.dp),
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(button.textColor)
        )
        Text(
            text = button.name,
            color = button.textColor,
            style = ExtendedTheme.typography.buttonMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
