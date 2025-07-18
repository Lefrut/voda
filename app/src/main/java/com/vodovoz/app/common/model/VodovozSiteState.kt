package com.vodovoz.app.common.model

data class VodovozSiteState(
    val isActive: Boolean,
    val testUrl: String,
    val smsUrl: String,
    val isSmsEnabled: Boolean,
    val takePhotos: Boolean,
    val jivoChat: JivoChatModel,
    val tracking: TrackingConfig,
    val agreement: AgreementModel,
    val data: VodovozSiteStateData?,
    val callPhoneNumber: String,
) {
    companion object {
        val Blocked = VodovozSiteState(
            isActive = false,
            testUrl = "",
            smsUrl = "",
            isSmsEnabled = false,
            takePhotos = false,
            jivoChat = JivoChatModel(false, ""),
            tracking = TrackingConfig(false, 30),
            agreement = AgreementModel("", emptyList()),
            callPhoneNumber = "",
            data = null
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

data class VodovozSiteStateData(
    val title: String,
    val logo: String,
    val description: String,
    val email: String,
    val time: String,
    val phone: String,
    val contacts: List<VodovozSiteStateContact>,
)

data class VodovozSiteStateContact(
    val url: String,
    val urlType: String,
    val image: String,
)
