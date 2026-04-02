package com.m.vodovoz.core.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import com.m.vodovoz.common.model.BaseVodovozAction
import com.m.vodovoz.common.model.ButtonAction
import com.m.vodovoz.common.model.DataAllAction
import com.m.vodovoz.common.model.GlobalAppLinks
import com.m.vodovoz.common.model.VodovozAction
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.network.VodovozWebConfig
import com.m.vodovoz.feature.main.Navigator
import com.m.vodovoz.util.extensions.openUrl


fun BaseVodovozAction.activate(
    navigator: Navigator,
    context: Context? = null,
    cookie: String = "",
    tabManager: TabManager? = null,
    onUnknownAction: (VodovozAction.Unknown) -> Unit = {},
) {

    when (this) {
        DataAllAction.AllDiscount -> {
            navigator.navigateToHurryBuyUpProducts()
        }

        DataAllAction.AllNewProducts -> {
            navigator.navigateToNewProducts()
        }

        DataAllAction.AllPromotions -> {
            navigator.navigateToPromotions()
        }

        DataAllAction.Delivery -> {
            with(GlobalAppLinks.aboutDelivery) {
                navigator.navigateToWebView(url, title)
            }
        }

        DataAllAction.Profile -> {
            navigator.navigateToProfile(tabManager)
        }

        DataAllAction.WaterTracker -> {
            navigator.navigateToWaterApp()
        }

        DataAllAction.BuyCertificate -> {
            navigator.navigateToBuyCertificate()
        }

        DataAllAction.AllServices -> {
            navigator.navigateToAllServices()
        }

        DataAllAction.CoolerRental -> {
            navigator.navigateToServiceDetails(98121)
        }

        DataAllAction.FreeCoolerRental -> {
            navigator.navigateToServiceDetails(98123)
        }

        DataAllAction.CoolerRepair -> {
            navigator.navigateToServiceDetails(98886)

        }

        DataAllAction.SanitaryMaintenance -> {
            navigator.navigateToServiceDetails(98887)
        }

        DataAllAction.Unknown -> {

        }

        is ButtonAction.Id -> {
            navigator.navigateToButtonProductList(id)
        }

        is VodovozAction.Brand -> {
            navigator.navigateToBrandProductList(id)
        }

        is VodovozAction.Category -> {
            navigator.navigateToCategoryProductList(id)
        }

        is VodovozAction.Product -> {
            navigator.navigateToProductDetails(id)
        }

        is VodovozAction.Products -> {
            navigator.navigateToBannerProductList(bannerId, blockId)
        }

        is VodovozAction.Promotion -> {
            navigator.navigateToPromotionDetails(id)
        }

        is VodovozAction.Promotions -> {
            navigator.navigateToPromotions(blockId, bannerId)
        }

        is VodovozAction.Unknown -> {
            onUnknownAction(this)
        }

        is VodovozAction.Url -> runCatching {
            context?.openUrl(url)
        }

        is VodovozAction.UrlWithCookie -> {
            context?.openUrl(url)
        }
    }
}


open class Activator<out T : BaseVodovozAction>(
    val action: T,
    private val activate: (T) -> Unit,
) {

    fun activate() = activate(action)

    override fun equals(other: Any?): Boolean {
        return action == other
    }

    override fun hashCode(): Int {
        return action.hashCode()
    }

}

inline fun <reified T : VodovozAction> createActivator(
    action: T,
    noinline activate: (T) -> Unit,
): Activator<T> {
    return Activator(action, activate)
}

open class VodovozActionActivator(
    action: VodovozAction,
    activate: (VodovozAction) -> Unit,
) : Activator<VodovozAction>(action, activate)

inline fun <reified T : VodovozAction> createVodovozActivator(
    action: T,
    noinline activate: (T) -> Unit,
): VodovozActionActivator {
    return VodovozActionActivator(action = action, activate = { activate(action) })
}

class DataAllActionActivator(
    action: DataAllAction,
    activate: (DataAllAction) -> Unit,
) : VodovozActionActivator(action, activate = { activate(action) })

inline fun <reified T : DataAllAction> createDataAllActivator(
    action: T,
    noinline activate: (T) -> Unit,
): DataAllActionActivator {
    return DataAllActionActivator(action = action, activate = { activate(action) })
}

