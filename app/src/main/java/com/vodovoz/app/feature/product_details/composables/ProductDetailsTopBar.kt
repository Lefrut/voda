package com.vodovoz.app.feature.product_details.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.vodovoz.app.R
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.ClickableIcon
import com.vodovoz.app.design_system.composables.decoration.SkeletonBox

@Composable
fun ProductDetailsTopBar(
    modifier: Modifier = Modifier,
    onNavigationClick: () -> Unit,
    onLikeClick: () -> Unit,
    onShareClick: () -> Unit,
    isFavoriteProduct: Boolean,
    isLoading: Boolean
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentColor = MaterialTheme.colorScheme.onBackground,
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClickableIcon(
                modifier = Modifier.clip(CircleShape),
                iconId = R.drawable.ic_arrow_left,
                tint = MaterialTheme.colorScheme.onBackground,
                onClick = onNavigationClick
            )
            Spacer(modifier = Modifier.weight(1f))

            val shimmer = rememberShimmer(ShimmerBounds.View)
            if (isLoading) {
                SkeletonBox(shimmerState = shimmer, modifier = Modifier.height(28.dp).width(124.dp))
            } else {
                Icon(
                    painter = painterResource(id = if (isFavoriteProduct) R.drawable.ic_filled_like else R.drawable.ic_like),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            onClick = { onLikeClick() }),
                    tint = if (isFavoriteProduct) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                )

                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .size(24.dp)
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            onClick = { onShareClick() }),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

        }
    }
}

@Preview
@Composable
private fun ProductDetailTopBarPreview() {
    VodovozTheme {
        ProductDetailsTopBar(
            onNavigationClick = { /*TODO*/ },
            onLikeClick = { /*TODO*/ },
            isFavoriteProduct = true,
            onShareClick = {},
            isLoading = true
        )
    }
}