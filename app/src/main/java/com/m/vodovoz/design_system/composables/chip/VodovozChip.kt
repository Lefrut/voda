package com.m.vodovoz.design_system.composables.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.ExtendedTheme
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.modifiers.vodovozSurface

@Composable
fun VodovozChip(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onSelect: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    containerColor: Color = if (selected) MaterialTheme.colorScheme.surfaceTint else MaterialTheme.colorScheme.surface,
    contentColor: Color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
    shape: Shape = MaterialTheme.shapes.small,
    borderStroke: BorderStroke? = BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    )
) {
    Box(
        modifier = modifier
            .vodovozSurface(
                border = if (selected) null else borderStroke,
                shape = shape,
                backgroundColor = containerColor,
                shadowElevation = 0f
            )
            .clickable(
                interactionSource = null,
                indication = ripple(),
                enabled = true,
                onClick = onSelect
            )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(contentPadding),
            style = MaterialTheme.typography.bodySmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }


}

@Composable
fun VodovozClosableChip(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onSelect: () -> Unit = {},
    onClose: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(
        end = 8.dp,
        start = 12.dp,
        top = 6.dp,
        bottom = 6.dp
    ),
) {
    Row(
        modifier = modifier
            .vodovozSurface(
                border = if (selected) null else BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = MaterialTheme.shapes.small,
                backgroundColor = if (selected) MaterialTheme.colorScheme.surfaceTint else MaterialTheme.colorScheme.surface,
                shadowElevation = 0f
            )
            .clickable(
                interactionSource = null,
                indication = ripple(),
                enabled = true,
                onClick = onSelect
            )
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f, false),
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_clean),
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .clickable {
                    onClose()
                },
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

}

@Composable
fun VodovozColorChip(modifier: Modifier = Modifier, backgroundColor: Color, text: String) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .widthIn(39.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.background,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VodovozColorChipSmall(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    text: String,
    textColor: Color = Color.Unspecified,
) {
    Box(
        modifier = modifier
            .widthIn(30.dp)
            .height(16.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {

        val textStyle = ExtendedTheme.typography.labelExtraSmall

        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 5.dp),
            style = textStyle.copy(lineHeight = textStyle.fontSize),
            color = textColor.takeOrElse { MaterialTheme.colorScheme.background }
        )
    }
}


@Preview
@Composable
private fun VodovozChipPreview() {
    VodovozTheme {
        VodovozChip(text = "Hello world!", selected = true)
    }
}