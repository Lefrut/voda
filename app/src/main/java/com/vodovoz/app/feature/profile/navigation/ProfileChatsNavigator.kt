package com.vodovoz.app.feature.profile.navigation

import android.content.Context
import androidx.navigation.NavController
import com.vodovoz.app.common.jivochat.JivoChatController
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.util.extensions.dialPhoneNumber
import com.vodovoz.app.util.extensions.startTelegram
import com.vodovoz.app.util.extensions.startViber
import com.vodovoz.app.util.extensions.startWhatsUp

object ProfileChatsNavigator {

    fun navigate(chatId: String, data: String, navController: NavController, context: Context) {
        when (chatId) {
            "chat" -> {
                navController.navigateToWebView(
                    JivoChatController.getLink(), ""
                )
            }

            "viber" -> {
                context.startViber(data)
            }

            "telega" -> {
                context.startTelegram(data)
            }

            "watsup" -> {
                context.startWhatsUp(data)
            }

            "telefon" -> {
                context.dialPhoneNumber(data)
            }

            else -> {
                //todo - navigate to writ message
                //navController.navi()
            }
        }
    }

}