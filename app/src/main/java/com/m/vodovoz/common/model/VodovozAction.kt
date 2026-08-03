package com.m.vodovoz.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


sealed interface BaseVodovozAction


@Parcelize
sealed interface VodovozAction : Parcelable, BaseVodovozAction {

    val actionName: String get() = ""

    @Parcelize
    data class Product(val id: Long) : VodovozAction

    @Parcelize
    data class Products(val blockId: Long, val bannerId: Long) : VodovozAction

    @Parcelize
    data class Category(val id: Long) : VodovozAction

    @Parcelize
    data class Promotion(val id: Long) : VodovozAction

    @Parcelize
    data class Promotions(val blockId: Long, val bannerId: Long) : VodovozAction

    @Parcelize
    data class Brand(val id: Long) : VodovozAction

    @Parcelize
    data class Url(val url: String) : VodovozAction

    @Parcelize
    data class UrlWithCookie(val url: String) : VodovozAction

    @Parcelize
    data class Unknown(val action: String, val id: String?) : VodovozAction {

        override val actionName: String get() = action.lowercase()

    }

    data object Name {

        const val CLOSE = "close"

    }

}

@Parcelize
enum class DataAllAction : VodovozAction, ButtonAction {
    AllServices,
    AllDiscount,
    AllNewProducts,
    AllPromotions,
    Delivery,
    Profile,
    WaterTracker,
    BuyCertificate,
    CoolerRental,
    FreeCoolerRental,
    CoolerRepair,
    SanitaryMaintenance,
    Unknown;

    override val actionName: String
        get() = name

}


sealed interface ButtonAction : BaseVodovozAction {

    data class Id(val id: Int) : ButtonAction
}

fun vodovozActionOf(action: String?, id: String?, blockId: Long): VodovozAction? {
    val actionName = action ?: return null
    val actionId = id.orEmpty()

    return when (actionName.uppercase()) {
        "TOVAR" -> VodovozAction.Product(actionId.toLongOrNull() ?: return null)
        "TOVARY" -> VodovozAction.Products(blockId, actionId.toLongOrNull() ?: return null)
        "RAZDEL" -> VodovozAction.Category(actionId.toLongOrNull() ?: return null)
        "AKCIYA" -> VodovozAction.Promotion(actionId.toLongOrNull() ?: return null)
        "AKCII" -> VodovozAction.Promotions(blockId, actionId.toLongOrNull() ?: return null)
        "BRAND" -> VodovozAction.Brand(actionId.toLongOrNull() ?: return null)
        "URL" -> VodovozAction.Url(actionId)
        "URLKYKI" -> VodovozAction.UrlWithCookie(actionId)
        "DANNYEVSE" -> dataAllActionOf(actionId)
        else -> VodovozAction.Unknown(actionName, actionId)
    }
}

fun dataAllActionOf(id: String): DataAllAction {
    return when (id) {
        "uslugi" -> DataAllAction.AllServices
        "vseskidki" -> DataAllAction.AllDiscount
        "vsenovinki" -> DataAllAction.AllNewProducts
        "vseakcii" -> DataAllAction.AllPromotions
        "dostavka" -> DataAllAction.Delivery
        "profil" -> DataAllAction.Profile
        "trekervodi" -> DataAllAction.WaterTracker
        "pokypkasertificat" -> DataAllAction.BuyCertificate
        "sanitarnaya_obrabotka" -> DataAllAction.SanitaryMaintenance
        "remont_kulerov" -> DataAllAction.CoolerRepair
        "arenda_kulera" -> DataAllAction.CoolerRental
        "besplatnaya_arenda_kulera" -> DataAllAction.FreeCoolerRental
        else -> DataAllAction.Unknown
    }
}
