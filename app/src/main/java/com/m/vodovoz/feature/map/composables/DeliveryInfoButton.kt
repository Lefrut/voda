package com.m.vodovoz.feature.map.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.m.vodovoz.design_system.ExtendedTheme
import com.m.vodovoz.design_system.model.ImageButtonUi
import com.m.vodovoz.feature.home.composables.dropShadow

@Composable
fun DeliveryInfoButton(
    modifier: Modifier = Modifier,
    imageButton: ImageButtonUi,
    onClick: () -> Unit,
) {

    val deliveryCardShape = RoundedCornerShape(30.dp)

    Row(
        modifier = modifier
            .widthIn(max = 200.dp)
            .height(40.dp)
            .dropShadow(
                shape = deliveryCardShape,
                color = Color.Black.copy(0.28f),
                blur = 2.dp,
                offsetY = 1.dp
            )
            .background(
                color = imageButton.containerColor.takeOrElse {
                    MaterialTheme.colorScheme.background
                },
                shape = deliveryCardShape
            )
            .clip(deliveryCardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageButton.image.isNotEmpty()) {
            AsyncImage(
                model = imageButton.image,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(24.dp),
            )
        }

        Text(
            text = imageButton.name,
            color = imageButton.contentColor.takeOrElse {
                MaterialTheme.colorScheme.primary
            },
            style = ExtendedTheme.typography.buttonSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}