package com.m.vodovoz.feature.sitestate

import com.m.vodovoz.common.agreement.AgreementController
import com.m.vodovoz.common.jivochat.JivoChatController
import com.m.vodovoz.common.model.AppConfig
import com.m.vodovoz.common.model.GlobalAppExtraAgreement
import com.m.vodovoz.common.model.GlobalAppLinks
import com.m.vodovoz.core.network.VodovozWebConfig
import com.m.vodovoz.data.parser.common.safeString
import com.m.vodovoz.data.vodovoz_service.model.ACTION_DTO
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.util.extensions.debugLog
import com.m.vodovoz.util.extensions.singleResult
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

    val siteStateSnapshot get() = siteStateFlow.value ?: AppConfig.Blocked

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


        val path = json.safeString("ACTION")
        val id = json.safeString("ID")
        val subsections = json.safeString("SUBSECTIONS")
        val orderId = json.safeString("ID")
        val section = json.safeString("NAME_RAZDEL")
        val action = json.safeString("ACTION")
        val blockId = json.safeString("IBLOCK_ID")


        if (path.isNotEmpty()) {
            pushListener.value = PushData(
                id = id,
                section = section,
                orderId = orderId,
                subsections = subsections,
                action = action,
                path = path,
                blockId = blockId
            )
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
        val blockId: String? = null
    )
}