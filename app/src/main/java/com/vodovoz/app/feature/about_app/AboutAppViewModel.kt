package com.vodovoz.app.feature.about_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.agreement.AgreementController
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.feature.about_app.model.AboutAppEvent
import com.vodovoz.app.feature.about_app.model.AboutAppOption
import com.vodovoz.app.feature.about_app.model.AboutAppState
import com.vodovoz.app.feature.bottom.aboutapp.adapter.AboutApp
import com.vodovoz.app.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AboutAppViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val resourcesProvider: ResourcesProvider
) : MviViewModel<AboutAppState, AboutAppEvent>(AboutAppState()) {

    fun fetchUserId() = accountManager.fetchAccountId()

    private val aboutAppActionsList = listOf(
        AboutApp(
            id = 0,
            actionImgResId = R.drawable.png_share_app,
            actionNameResId = R.string.share_app_text
        ),
        AboutApp(
            id = 1,
            actionImgResId = R.drawable.png_ic_favorite,
            actionNameResId = R.string.rate_app_text
        ),
        AboutApp(
            id = 2,
            actionImgResId = R.drawable.png_letter,
            actionNameResId = R.string.write_to_developers
        )
    )

    fun observeAboutAppList() = MutableStateFlow(aboutAppActionsList).asStateFlow()

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