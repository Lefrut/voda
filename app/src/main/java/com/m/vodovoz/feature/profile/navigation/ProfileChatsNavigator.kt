package com.m.vodovoz.feature.profile.navigation

import android.content.Context
import androidx.navigation.NavController
import com.m.vodovoz.common.jivochat.JivoChatController
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.core.navigation.navigateToWriteMessage
import com.m.vodovoz.util.extensions.dialPhoneNumber
import com.m.vodovoz.util.extensions.openUrl
import com.m.vodovoz.util.extensions.startTelegram
import com.m.vodovoz.util.extensions.startViber
import com.m.vodovoz.util.extensions.startWhatsUp

object ProfileChatsNavigator {

    fun navigate(chatId: String, data: String, navController: NavController, context: Context) {
        with(context){
            when (chatId) {
                "chat" -> {
                    navController.navigateToWebView(
                        JivoChatController.getLink(), ""
                    )
                }

                "viber" -> {
                    startViber(data)
                }

                "telega" -> {
                    startTelegram(data)
                }

                "watsup" -> {
                    startWhatsUp(data)
                }

                "telefon" -> {
                    dialPhoneNumber(data)
                }

                "vk" -> {
                    openUrl(data)
                }

                else -> {
                    navController.navigateToWriteMessage()
                }
            }

        }
    }

}
