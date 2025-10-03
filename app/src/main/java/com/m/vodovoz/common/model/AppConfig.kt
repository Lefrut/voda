package com.m.vodovoz.common.model

import kotlin.reflect.KProperty

data class AppConfig(
    val isActive: Boolean,
    val testUrl: String,
    val smsUrl: String,
    val isSmsEnabled: Boolean,
    val smsCodeCount: Int,
    val takePhotos: Boolean,
    val jivoChat: JivoChat,
    val tracking: TrackingInfo,
    val agreement: Agreement,
    val extraAgreement: Agreement,
    val data: BlockSiteInfo?,
    val callPhoneNumber: String,
    val geocoderKey: String,
    val mapkitKey: String,
    val appLinks: AppLinks,
) {
    companion object {
        val Empty = AppConfig(
            isActive = true,
            testUrl = "",
            smsUrl = "",
            isSmsEnabled = false,
            smsCodeCount = 4,
            takePhotos = false,
            jivoChat = JivoChat(false, ""),
            tracking = TrackingInfo(false, 30),
            agreement = Agreement.Empty,
            extraAgreement = Agreement.Empty,
            callPhoneNumber = "",
            data = null,
            geocoderKey = "",
            mapkitKey = "",
            appLinks = AppLinks.Empty
        )

        val Blocked = Empty.copy(isActive = false)
    }
}

data class JivoChat(
    val isActive: Boolean,
    val url: String,
)

data class Agreement(
    val html: String,
    val titles: List<String>,
) {
    companion object {
        val Empty = Agreement("", emptyList())
    }
}

data class TrackingInfo(
    val trackingIsEnabled: Boolean,
    val time: Int,
)

data class BlockSiteInfo(
    val title: String,
    val logo: String,
    val description: String,
    val email: String,
    val time: String,
    val phone: String,
    val contacts: List<BlockSiteContact>,
)

data class BlockSiteContact(
    val url: String,
    val urlType: String,
    val image: String,
)

var GlobalAppLinks: AppLinks by SingletonDelegate(AppLinks.Empty)

var GlobalAppExtraAgreement: Agreement by SingletonDelegate(Agreement.Empty)

class SingletonDelegate<T : Any>(
    private val empty: T,
) {

    var instance = empty
        private set

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun getValue(
        nothing: Nothing?,
        property: KProperty<*>,
    ): T {
        return instance
    }

    operator fun setValue(
        nothing: Nothing?,
        property: KProperty<*>,
        appLinks: T,
    ) {
        if (instance == empty) {
            instance = appLinks
        }
    }
}

data class AppLinks(
    val policy: AppLink,
    val termsOfUse: AppLink,
    val aboutDelivery: AppLink,
    val aboutPayment: AppLink,
    val personal: AppLink,
) {
    companion object {
        val Empty = AppLinks(
            AppLink.Empty,
            AppLink.Empty,
            AppLink.Empty,
            AppLink.Empty,
            AppLink.Empty
        )
    }
}

data class AppLink(
    val title: String,
    val url: String,
) {
    companion object {
        val Empty = AppLink("", "")
    }
}