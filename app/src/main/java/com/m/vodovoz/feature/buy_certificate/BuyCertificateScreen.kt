package com.m.vodovoz.feature.buy_certificate

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.m.vodovoz.design_system.composables.scaffold.VodovozScaffold
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackbarHost
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.buy_certificate.composables.BuyCertificateBody

@Composable
fun BuyCertificateScreen(
    viewModel: BuyCertificateViewModel,
    viewState: BuyCertificateViewModel.BuyCertificateState,
    snackbarHostState: SnackbarHostState,
) {

    VodovozScaffold(
        topBar = {
            VodovozTopBar(
                onBack = { viewModel.navigateBack() },
                title = viewState.title
            )
        },
        snackbarHost = {
            VodovozSnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        BuyCertificateBody(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            certificates = viewState.certificates,
            certificatesTitle = viewState.certificatesTitle,
            currentCertificate = viewState.currentCertificate,
            currentTab = viewState.currentTab,
            tabs = viewState.tabs,
            paymentTitle = viewState.paymentTitle,
            paymentTypes = viewState.paymentTypes,
            currentPaymentType = viewState.currentPaymentType,
            button = viewState.button,
            faq = viewState.faq,
            errors = viewState.errors,
            onTabClick = { tab ->
                viewModel.selectTab(tab)
            },
            onCertificateClick = { certificate ->
                viewModel.selectCertificate(certificate)
            },
            onFieldChange = { field, updatedField ->
                viewModel.changeField(field, updatedField)
            },
            onPaymentTypeClick = { paymentType ->
                viewModel.selectPaymentType(paymentType)
            },
            onButtonClick = { button ->
                viewModel.activateButton(button)
            },
            onFAQButtonClick = { faqUi ->
                viewModel.navigateToFAQ(faqUi)
            }
        )

    }
}