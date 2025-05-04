package com.vodovoz.app.feature.about_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.about_app.composables.AboutAppBody
import com.vodovoz.app.feature.about_app.model.AboutAppState

@Composable
fun AboutAppScreen(viewModel: AboutAppViewModel, viewState: AboutAppState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        VodovozTopBar(
            onBack = {
                viewModel.navigateBack()
            },
            title = stringResource(R.string.about_app),
            actionPainter = painterResource(
                R.drawable.ic_share
            )
        )
        AboutAppBody(
            modifier = Modifier.weight(1f),
            version = viewState.version,
            onOptionClick = { aboutAppOption ->
                viewModel.activateOption(aboutAppOption)
            }
        )
    }
}