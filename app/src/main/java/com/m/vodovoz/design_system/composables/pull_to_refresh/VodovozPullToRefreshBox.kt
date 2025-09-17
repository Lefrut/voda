package com.m.vodovoz.design_system.composables.pull_to_refresh

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VodovozPullToRefreshBox(
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        state = state,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        indicator = {
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                state = state,
                containerColor = MaterialTheme.colorScheme.background,
                color = MaterialTheme.colorScheme.primary,
                isRefreshing = isRefreshing
            )
        },
        content = content
    )
}