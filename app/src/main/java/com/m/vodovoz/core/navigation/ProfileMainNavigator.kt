package com.m.vodovoz.core.navigation

import android.content.Context
import com.m.vodovoz.common.model.GlobalAppLinks
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi
import com.m.vodovoz.feature.main.Navigator

data object ProfileMainNavigator {

    fun navigate(id: String, navigator: Navigator) {
        when (id) {
            CHANGE_PASSWORD_ROUTE -> {
                navigator.navigateToChangePassword()
            }

            CERTIFICATE_ACTIVATION_ROUTE -> {
                navigator.navigateToCertificateActivation()
            }

            ORDER_HISTORY_ROUTE -> {
                navigator.navigateToOrdersHistory()
            }

            PRODUCTS_HISTORY_ROUTE -> {
                navigator.navigateToPastPurchases()
            }

            ADDRESSES_ROUTE -> {
                navigator.navigateToAddresses(AddressScreenTypeUi.Add)
            }

            QUESTIONNAIRES_ROUTE -> {
                navigator.navigateToQuestionnaires()
            }

            ABOUT_DELIVERY_ROUTE -> {

                val aboutDeliveryLink = GlobalAppLinks.aboutDelivery
                navigator.navigateToWebView(
                    aboutDeliveryLink.url,
                    aboutDeliveryLink.title
                )
            }

            ABOUT_PAYMENT_ROUTE -> {
                val aboutPaymentLink = GlobalAppLinks.aboutPayment
                navigator.navigateToWebView(
                    aboutPaymentLink.url,
                    aboutPaymentLink.title
                )
            }

            SETTINGS_NOTIFICATIONS_ROUTE -> {
                navigator.navigateToNotificationSettings()
            }

            ABOUT_APP_ROUTE -> {
                navigator.navigateToAboutApp()
            }

            else -> {

            }
        }
    }


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
