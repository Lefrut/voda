package com.m.vodovoz.core.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import androidx.navigation.NavController
import com.m.vodovoz.R
import com.m.vodovoz.common.model.BaseVodovozAction
import com.m.vodovoz.common.model.ButtonAction
import com.m.vodovoz.common.model.DataAllAction
import com.m.vodovoz.common.model.GlobalAppLinks
import com.m.vodovoz.common.model.VodovozAction
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.analytics.Analytics
import com.m.vodovoz.core.network.VodovozWebConfig
import com.m.vodovoz.util.extensions.openUrl


fun BaseVodovozAction.activate(
    navController: NavController,
    context: Context? = null,
    cookie: String = "",
    tabManager: TabManager? = null,
    onUnknownAction: (VodovozAction.Unknown) -> Unit = {},
) {

    when (this) {
        DataAllAction.AllDiscount -> {
            navController.navigateToHurryBuyUpProducts()
        }

        DataAllAction.AllNewProducts -> {
            navController.navigateToNewProducts()
        }

        DataAllAction.AllPromotions -> {
            navController.navigateToPromotions()
        }

        DataAllAction.Delivery -> {
            with(GlobalAppLinks.aboutDelivery) {
                navController.navigateToWebView(url, title)
            }
        }

        DataAllAction.Profile -> {
            tabManager?.apply {
                setAuthRedirect(navController.graph.id)
                selectTab(R.id.graph_profile)
            }

        }

        DataAllAction.WaterTracker -> {
            Analytics.reportEvent("trekervodi_catalog")
            navController.navigateToWaterApp()
        }

        DataAllAction.BuyCertificate -> {
            navController.navigateToBuyCertificate()
        }

        DataAllAction.AllServices -> {
            navController.navigateToAllServices()
        }

        DataAllAction.CoolerRental -> {
            navController.navigateToServiceDetails(98121)
        }

        DataAllAction.FreeCoolerRental -> {
            navController.navigateToServiceDetails(98123)
        }

        DataAllAction.CoolerRepair -> {
            navController.navigateToServiceDetails(98886)

        }

        DataAllAction.SanitaryMaintenance -> {
            navController.navigateToServiceDetails(98887)
        }

        DataAllAction.Unknown -> {

        }

        is ButtonAction.Id -> {
            navController.navigateToButtonProductList(id)
        }

        is VodovozAction.Brand -> {
            navController.navigateToBrandProductList(id)
        }

        is VodovozAction.Category -> {
            navController.navigateToCategoryProductList(id)
        }

        is VodovozAction.Product -> {
            navController.navigateToProductDetails(id)
        }

        is VodovozAction.Products -> {
            navController.navigateToBannerProductList(bannerId, blockId)
        }

        is VodovozAction.Promotion -> {
            navController.navigateToPromotionDetails(id)
        }

        is VodovozAction.Promotions -> {
            navController.navigateToPromotions(blockId, bannerId)
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


