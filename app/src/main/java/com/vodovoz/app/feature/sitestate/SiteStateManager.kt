package com.vodovoz.app.feature.sitestate

import com.vodovoz.app.common.agreement.AgreementController
import com.vodovoz.app.common.jivochat.JivoChatController
import com.vodovoz.app.common.model.AppConfig
import com.vodovoz.app.common.model.GlobalAppExtraAgreement
import com.vodovoz.app.common.model.GlobalAppLinks
import com.vodovoz.app.data.parser.common.safeString
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.singleResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SiteStateManager @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) {
    private val _siteStateFlow = MutableStateFlow<AppConfig?>(null)
    val siteStateFlow = _siteStateFlow.asStateFlow()

    val siteStateSnapshot get() = siteStateFlow.value ?: AppConfig.Empty

    private val deepLinkPathListener = MutableStateFlow<String?>(null)
    fun observeDeepLinkPath() = deepLinkPathListener.asStateFlow()

    private val pushListener = MutableStateFlow<PushData?>(null)
    fun observePush() = pushListener.asStateFlow()

    suspend fun requestSiteState(): AppConfig? {
        val siteStateResult = vodovozServiceRepository.getSiteState().singleResult()

        siteStateResult.onSuccess { siteState ->
            GlobalAppLinks = siteState.appLinks
            GlobalAppExtraAgreement = siteState.extraAgreement
            val siteAgreement = siteState.agreement
            val jivoChat = siteState.jivoChat

            _siteStateFlow.update { siteState }

            AgreementController.setAgreement(
                text = siteAgreement.html,
                titles = siteAgreement.titles,
            )
            JivoChatController.setParams(
                active = jivoChat.isActive,
                link = jivoChat.url,
            )
        }.onFailure {
            _siteStateFlow.update { null }
        }

        return siteStateResult.getOrNull()
    }


    fun smsEnabled(): Boolean = siteStateSnapshot.isSmsEnabled

    fun saveDeepLinkPath(path: String?) {
        if (path == null) return
        deepLinkPathListener.value = path
    }

    fun savePushData(json: JSONObject) {
        val path = json.safeString("Secreen")
        val id = json.safeString("ID")
        val subsections = json.safeString("SUBSECTIONS")
        val orderId = json.safeString("NumberZakaz")
        val section = json.safeString("NAME_RAZDEL")
        val action = json.safeString("ACTION")

        if (path.isNotEmpty()) {
            pushListener.value = PushData(id, section, orderId, subsections, action, path)
        }
    }

    fun clearDeepLinkListener() {
        deepLinkPathListener.value = null
    }

    fun clearPushListener() {
        debugLog { "clear push data" }
        pushListener.value = null
    }

    data class PushData(
        val id: String? = null,
        val section: String? = null,
        val orderId: String? = null,
        val subsections: String? = null,
        val action: String? = null,
        val path: String,
    )
}