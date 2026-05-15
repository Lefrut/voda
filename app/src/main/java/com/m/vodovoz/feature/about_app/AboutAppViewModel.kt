package com.m.vodovoz.feature.about_app

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.model.GlobalAppLinks
import com.m.vodovoz.core.network.VodovozUrlManager
import com.m.vodovoz.core.network.VodovozWebConfig
import com.m.vodovoz.core.network.interceptor.BaseUrlInterceptor
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.about_app.composables.AppMode
import com.m.vodovoz.feature.about_app.model.AboutAppEvent
import com.m.vodovoz.feature.about_app.model.AboutAppOptionUi
import com.m.vodovoz.feature.about_app.model.AboutAppState
import com.m.vodovoz.feature.sitestate.SiteStateManager
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AboutAppViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val siteStateManager: SiteStateManager,
    private val urlManager: VodovozUrlManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<AboutAppState, AboutAppEvent>(AboutAppState()) {

    fun navigateBack() = viewModelScope.launch {
        sendEvent(AboutAppEvent.GoBack)
    }

    fun share() = viewModelScope.launch {
        sendEvent(AboutAppEvent.Share)
    }

    fun activateOption(aboutAppOption: AboutAppOptionUi) = viewModelScope.launch {
        when (aboutAppOption) {
            AboutAppOptionUi.ContactDevelopers -> {
                sendEvent(AboutAppEvent.WriteToDevelopers(accountManager.fetchAccountId() ?: -1))
            }

            AboutAppOptionUi.RateApp -> {
                sendEvent(AboutAppEvent.RateApp)
            }

            AboutAppOptionUi.PrivacyPolicy -> {
                with(GlobalAppLinks.policy) {
                    sendEvent(AboutAppEvent.GoToWebView(url, title))
                }
            }

            AboutAppOptionUi.TermsOfUse -> {
                with(GlobalAppLinks.termsOfUse) {
                    sendEvent(AboutAppEvent.GoToWebView(url, title))
                }
            }

            AboutAppOptionUi.PersonalData -> {
                with(GlobalAppLinks.personal) {
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
        val prodUrl = accountManager.getUserUrl()

        when (appMode) {
            AppMode.Test -> {
                VodovozWebConfig.isTestMode = true
                urlManager.setUrl(testUrl)
            }

            AppMode.Prod -> {
                VodovozWebConfig.isTestMode = false
                urlManager.setUrl(prodUrl)
            }
        }
        updateState { s ->
            s.copy(showDeveloperBS = false)
        }
        vodovozServiceRepository.relogin().singleResult()
        sendEvent(AboutAppEvent.RefreshApp)
    }
}