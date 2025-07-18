package com.vodovoz.app.common.block_app_signal

import com.vodovoz.app.common.datastore.DataStorePrefsImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReloadAppSignalImpl @Inject constructor(
    private val dataStorePrefs: DataStorePrefsImpl,
) : ReloadAppSignal {

    companion object {
        const val BLOCK_APP_KEY = "block_app"
    }

    private val _channel = Channel<Boolean>(RENDEZVOUS)

    override suspend fun getChannel(scope: CoroutineScope): ReceiveChannel<Boolean> {
         scope.launch {
             dataStorePrefs.getBooleanFlow(BLOCK_APP_KEY).collect { update ->
                 _channel.trySend(update ?: false)
             }
         }

        return _channel
    }

}