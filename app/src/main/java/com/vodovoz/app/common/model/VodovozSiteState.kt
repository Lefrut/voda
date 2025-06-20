package com.vodovoz.app.common.model

data class VodovozSiteState(
    val isActive: Boolean,
    val testUrl: String,
    val smsUrl: String,
    val isSmsEnabled: Boolean,
    val showComments: Boolean,
    val jivoChat: JivoChatModel,
    val tracking: TrackingConfig,
    val agreement: AgreementModel,
    val data: SiteStateData? = null,
    val callPhoneNumber: String
){
    companion object{
        val Blocked = VodovozSiteState(
            isActive = false,
            testUrl = "",
            smsUrl = "",
            isSmsEnabled = false,
            showComments = false,
            jivoChat = JivoChatModel(false, ""),
            tracking = TrackingConfig(false, 30),
            agreement = AgreementModel("", emptyList()),
            callPhoneNumber = ""
        )
    }
}

data class JivoChatModel(
    val isActive: Boolean,
    val url: String,
)

data class AgreementModel(
    val html: String,
    val titles: List<String>,
)

data class TrackingConfig(
    val trackingIsEnabled: Boolean,
    val time: Int,
)

data class SiteStateData(
    val title: String?,
    val logo: String?,
    val desc: String?,
    val email: String?,
    val whatsUp: SiteStateContact?,
    val viber: SiteStateContact?,
    val telegram: SiteStateContact?,
    val chat: SiteStateContact?,
    val phone: SiteStateContact?,
    val time: String?,
)

data class SiteStateContact(
    val url: String,
    val image: String,
)
