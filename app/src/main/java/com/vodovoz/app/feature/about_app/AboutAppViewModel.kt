package com.vodovoz.app.feature.about_app

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.agreement.AgreementController
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.feature.about_app.model.AboutAppEvent
import com.vodovoz.app.feature.about_app.model.AboutAppOption
import com.vodovoz.app.feature.about_app.model.AboutAppState
import com.vodovoz.app.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AboutAppViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val resourcesProvider: ResourcesProvider
) : MviViewModel<AboutAppState, AboutAppEvent>(AboutAppState()) {

    fun fetchUserId() = accountManager.fetchAccountId()

    fun navigateBack() = viewModelScope.launch {
        _events.emit(AboutAppEvent.GoBack)
    }

    fun share() = viewModelScope.launch {
        _events.emit(AboutAppEvent.Share)
    }

    fun activateOption(aboutAppOption: AboutAppOption) = viewModelScope.launch {
        when (aboutAppOption) {
            AboutAppOption.ContactDevelopers -> {
                _events.emit(AboutAppEvent.WriteToDevelopers(accountManager.fetchAccountId() ?: -1))
            }

            AboutAppOption.RateApp -> {
                _events.emit(AboutAppEvent.RateApp)
            }

            AboutAppOption.PrivacyPolicy -> {
                val links = AgreementController.extractLinks()
                val link = links.getOrElse(0) { "" }
                val title =
                    AgreementController.getTitle(0) ?: resourcesProvider.getString(R.string.space)
                _events.emit(AboutAppEvent.GoToWebView(link, title))

            }

            AboutAppOption.TermsOfUse -> {
                val links = AgreementController.extractLinks()
                val link = links.getOrElse(1) { "" }
                val title =
                    AgreementController.getTitle(1) ?: resourcesProvider.getString(R.string.space)
                _events.emit(AboutAppEvent.GoToWebView(link, title))
            }
        }
    }
}