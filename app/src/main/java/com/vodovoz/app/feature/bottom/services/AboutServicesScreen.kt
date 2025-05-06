package com.vodovoz.app.feature.bottom.services

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.bottom.services.composables.AboutServicesBody

@Composable
fun AboutServicesScreen(
    viewModel: AboutServicesFlowViewModel,
    viewState: AboutServicesFlowViewModel.AboutServicesState,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        VodovozTopBar(
            title = viewState.title,
            onBack = {
                viewModel.navigateBack()
            }
        )
        AboutServicesBody(
            modifier = Modifier.weight(1f),
            descriptionHtml = viewState.descriptionHtml,
            services = viewState.services,
            onServiceClick = { service ->
                //todo - implement realization
            }
        )
    }
}