package com.m.vodovoz.feature.bottom.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.bottom.services.composables.AboutServicesBody

@Composable
fun AboutServicesScreen(
    viewModel: AboutServicesFlowViewModel,
    viewState: AboutServicesFlowViewModel.AboutServicesState,
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                viewModel.navigateToServiceDetails(service)
            }
        )
    }
}