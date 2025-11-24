package com.m.vodovoz.domain.general.model.promotion

data class SpecialPromotionModel(
    val id: Int,
    val name: String,
    val text: String,
    val picture: String,
    val actionWithButton: ActionWithButtonModel?,
    val aboutAdvertising: AboutAdvertisingModel?
)

data class AppUpdateInfoModel(
    val id: Int,
    val title: String,
    val text: String,
    val playMarketUrl: String,
    val androidVersion: String,
    val picture: String,
    val colorfulButton: ColorfulButtonModel?,
)


data class PopupWindowInfoModel(
    val specialPromotion: SpecialPromotionModel?,
    val appUpdateInfo: AppUpdateInfoModel,
) {

    companion object {
        val Empty = PopupWindowInfoModel(
            SpecialPromotionModel(-1, "", "", "", null, null),
            AppUpdateInfoModel(-1, "", "", "", "0.0.0", "", null)
        )

    }
}