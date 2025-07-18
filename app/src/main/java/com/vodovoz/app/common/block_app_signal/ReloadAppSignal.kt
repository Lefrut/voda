package com.vodovoz.app.common.block_app_signal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel


interface ReloadAppSignal {

    suspend fun getChannel(scope: CoroutineScope): ReceiveChannel<Boolean>

}