package com.m.vodovoz.core.navigation

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.m.vodovoz.R
import com.m.vodovoz.common.model.GlobalAppLinks
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi

data object ProfileMainNavigator {

    fun navigate(id: String, navController: NavController) {
        when (id) {
            CHANGE_PASSWORD_ROUTE -> {
                navController.navigateToChangePassword()
            }

            CERTIFICATE_ACTIVATION_ROUTE -> {
                navController.navigateToCertificateActivation()
            }

            ORDER_HISTORY_ROUTE -> {
                navController.navigateToOrdersHistory()
            }

            PRODUCTS_HISTORY_ROUTE -> {
                navController.navigateToPastPurchases()
            }

            ADDRESSES_ROUTE -> {
                navController.navigateToAddresses(AddressScreenTypeUi.Add)
            }

            QUESTIONNAIRES_ROUTE -> {
                navController.navigateToQuestionnaires()
            }

            ABOUT_DELIVERY_ROUTE -> {

                val aboutDeliveryLink = GlobalAppLinks.aboutDelivery
                navController.navigateToWebView(
                    aboutDeliveryLink.url,
                    aboutDeliveryLink.title
                )
            }

            ABOUT_PAYMENT_ROUTE -> {
                navigateToAboutPayment(navController)
            }

            SETTINGS_NOTIFICATIONS_ROUTE -> {
                navController.navigateToNotificationSettings()
            }

            ABOUT_APP_ROUTE -> {
                navController.navigateToAboutApp()
            }

            else -> {

            }
        }
    }

    fun navigateToAboutPayment(navController: NavController) {
        with(GlobalAppLinks.aboutPayment) {
            navController.navigateToWebView(url, title)
        }
    }

    fun navigateToContacts(navController: NavController) {
        navController.navigate(R.id.profileFragment, bundleOf(OPEN_CONTACTS_ARG to true))
    }

    const val OPEN_CONTACTS_ARG = "openContacts"
    private const val CHANGE_PASSWORD_ROUTE = "parol"
    private const val CERTIFICATE_ACTIVATION_ROUTE = "kodslova"
    private const val ORDER_HISTORY_ROUTE = "historyzakaz"
    private const val PRODUCTS_HISTORY_ROUTE = "historytovar"
    private const val ADDRESSES_ROUTE = "adressa"
    private const val QUESTIONNAIRES_ROUTE = "anketa"
    private const val ABOUT_DELIVERY_ROUTE = "dostavka"
    private const val ABOUT_PAYMENT_ROUTE = "oplata"
    private const val SETTINGS_NOTIFICATIONS_ROUTE = "yvedomlenie"
    private const val ABOUT_APP_ROUTE = "oprile"


}
