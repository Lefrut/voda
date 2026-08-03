package com.m.vodovoz.feature.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.image.VodovozAsyncImage
import com.m.vodovoz.domain.general.model.promotion.FloatingPromoButtonModel

private const val FLOATING_PROMO_IMAGE_ASPECT_RATIO = 2134f / 1711f

@Composable
fun FloatingPromoButton(
    button: FloatingPromoButtonModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    VodovozAsyncImage(
        model = button.imageUrl,
        contentDescription = button.name,
        modifier = modifier
            .width(104.dp)
            .aspectRatio(FLOATING_PROMO_IMAGE_ASPECT_RATIO)
            .clickable(
                role = Role.Button,
                onClick = onClick,
                indication = null,
                interactionSource = null
            ),
    )
}
