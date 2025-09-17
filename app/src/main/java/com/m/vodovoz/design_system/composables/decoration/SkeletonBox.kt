package com.m.vodovoz.design_system.composables.decoration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.shimmer


@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shimmerState: Shimmer,
    content: @Composable (BoxScope.() -> Unit)? = null,
) {
    val skeletonBoxModifier = Modifier
        .clip(MaterialTheme.shapes.large)
        .background(MaterialTheme.colorScheme.surface)
        .shimmer(shimmerState)
        .background(MaterialTheme.colorScheme.background)

    if (content == null) {
        Box(modifier = modifier.then(skeletonBoxModifier))
    } else {
        Box(modifier = modifier) {
            Box(modifier = skeletonBoxModifier.matchParentSize())
            content()
        }


    }
}


val LocalShimmer = compositionLocalOf<Shimmer> {
    throw IllegalArgumentException("Shimmer don't implemented")
}