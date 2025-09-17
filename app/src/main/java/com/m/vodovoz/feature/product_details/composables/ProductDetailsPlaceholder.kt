package com.m.vodovoz.feature.product_details.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.decoration.SkeletonBox
import kotlin.random.Random

@Composable
fun ProductDetailsPlaceholder(modifier: Modifier = Modifier) {
    val shimmer = rememberShimmer(shimmerBounds = ShimmerBounds.View)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SkeletonBox(
            shimmerState = shimmer,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) {
                SkeletonBox(
                    shimmerState = shimmer,
                    modifier = Modifier
                        .width(Random.nextInt(70, 110).dp)
                        .height(40.dp)
                )
            }
        }

        SkeletonBox(
            shimmerState = shimmer,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(36.dp)
        )

        SkeletonBox(
            shimmerState = shimmer,
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(26.dp)
        )

        SkeletonBox(
            shimmerState = shimmer,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
    }
}
@Preview(apiLevel = 34)
@Composable
private fun ProductDetailsPlaceholderPreview() {
    VodovozTheme {
        ProductDetailsPlaceholder()
    }
}