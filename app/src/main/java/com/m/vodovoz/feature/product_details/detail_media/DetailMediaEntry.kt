package com.m.vodovoz.feature.product_details.detail_media

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.product_details.ProductDetailsFlowViewModel
import com.m.vodovoz.feature.product_details.detail_media.api.DetailMediaNavKey
import com.m.vodovoz.feature.product_details.detail_media.model.DetailMediaEvent
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.disableFullScreen
import com.m.vodovoz.util.extensions.enableFullScreen
import com.m.vodovoz.util.extensions.indexOfOrNull
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun DetailMediaEntry(navKey: DetailMediaNavKey? = null) =
    NavigationEntry<DetailMediaViewModel, DetailMediaViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val productDetailsViewModel = viewModel(modelClass = ProductDetailsFlowViewModel::class)
    val viewState by viewModel.collectAsState()
    val mediaList = viewState.mediaList
    val context = LocalContext.current

    LifecycleStartEffect(Unit) {
        viewModel.tabManager.setTabVisibility(false)
        onStopOrDispose {
            viewModel.insetsVisibilityState.consumeSystemBarInsets(false)
            viewModel.insetsVisibilityState.consumeSystemBarInsets(true)
            viewModel.tabManager.setTabVisibility(true)
        }
    }

    if (mediaList.isNotEmpty()) {
        val pagerState = rememberPagerState(
            initialPage = mediaList.indexOfOrNull(viewState.currentMedia) ?: 0,
            pageCount = { mediaList.size }
        )

        DetailMediaScreen(
            viewModel = viewModel,
            viewState = viewState,
            pagerState = pagerState
        )

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
                .distinctUntilChanged()
                .collectLatest { currentPage ->
                    viewModel.setMediaByIndex(currentPage)
                    productDetailsViewModel.setMediaPage(currentPage)
                }
        }
    }

    LifecycleEffect {
        viewModel.events.collect { event ->
            val activity = context as? Activity
            when (event) {
                DetailMediaEvent.GoBack -> {
                    navigator.goBack()
                }

                DetailMediaEvent.MakeLandscape -> {
                    activity?.enableFullScreen()
                    viewModel.insetsVisibilityState.consumeSystemBarInsets(false)
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }

                DetailMediaEvent.MakePortrait -> {
                    activity?.disableFullScreen()
                    viewModel.insetsVisibilityState.consumeSystemBarInsets(true)
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }
        }
    }
}
