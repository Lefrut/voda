package com.m.vodovoz.feature.profile.navigation

import android.content.Context
import androidx.navigation.NavController
import com.m.vodovoz.common.jivochat.JivoChatController
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.core.navigation.navigateToWriteMessage
import com.m.vodovoz.util.extensions.dialPhoneNumber
import com.m.vodovoz.util.extensions.startTelegram
import com.m.vodovoz.util.extensions.startViber
import com.m.vodovoz.util.extensions.startWhatsUp

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
                navController.navigateToWriteMessage()
            }
        }
    }

}