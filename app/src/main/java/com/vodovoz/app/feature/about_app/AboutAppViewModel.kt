package com.vodovoz.app.feature.about_app

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.model.GlobalAppLinks
import com.vodovoz.app.core.network.VodovozWebConfig
import com.vodovoz.app.core.network.interceptor.BaseUrlInterceptor
import com.vodovoz.app.feature.about_app.composables.AppMode
import com.vodovoz.app.feature.about_app.model.AboutAppEvent
import com.vodovoz.app.feature.about_app.model.AboutAppOption
import com.vodovoz.app.feature.about_app.model.AboutAppState
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AboutAppViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val siteStateManager: SiteStateManager,
    private val baseUrlInterceptor: BaseUrlInterceptor,
) : MviViewModel<AboutAppState, AboutAppEvent>(AboutAppState()) {

    fun navigateBack() = viewModelScope.launch {
        sendEvent(AboutAppEvent.GoBack)
    }

    fun share() = viewModelScope.launch {
        sendEvent(AboutAppEvent.Share)
    }

    fun activateOption(aboutAppOption: AboutAppOption) = viewModelScope.launch {
        when (aboutAppOption) {
            AboutAppOption.ContactDevelopers -> {
                sendEvent(AboutAppEvent.WriteToDevelopers(accountManager.fetchAccountId() ?: -1))
            }

            AboutAppOption.RateApp -> {
                sendEvent(AboutAppEvent.RateApp)
            }

            AboutAppOption.PrivacyPolicy -> {
                with(GlobalAppLinks.policy) {
                    sendEvent(AboutAppEvent.GoToWebView(url, title))
                }
            }

            AboutAppOption.TermsOfUse -> {
                with(GlobalAppLinks.termsOfUse) {
                    sendEvent(AboutAppEvent.GoToWebView(url, title))
                }
            }
        }
    }

    fun showDeveloperBS() = viewModelScope.launch {
        updateState { s ->
            s.copy(showDeveloperBS = true)
        }
    }

    fun hideDeveloperBottomSheet() = viewModelScope.launch {
        updateState { s ->
            s.copy(showDeveloperBS = false)
        }
    }

    fun changeMode(appMode: AppMode) = viewModelScope.launch {
        val testUrl = siteStateManager.siteStateSnapshot.testUrl
        when (appMode) {
            AppMode.Test -> {
                baseUrlInterceptor.updateBaseUrl(testUrl)
            }

            AppMode.Prod -> {
                baseUrlInterceptor.updateBaseUrl(VodovozWebConfig.VODOVOZ_URL)
            }
        }
        updateState { s ->
            s.copy(showDeveloperBS = false)
        }
        sendEvent(AboutAppEvent.RefreshApp)
    }
}