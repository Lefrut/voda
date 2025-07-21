package com.vodovoz.app.common.block_app_signal

import com.vodovoz.app.common.datastore.DataStorePrefsImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockAppSignalImpl @Inject constructor(
    private val dataStorePrefs: DataStorePrefsImpl,
) : BlockAppSignal {

    companion object {
        const val SIGNAL_KEY = "block_app"
    }


    override suspend fun getSignalFlow(): Flow<BlockAppSignal.Type> {
        return dataStorePrefs.getStringFlow(SIGNAL_KEY).distinctUntilChanged().mapNotNull { name ->
            kotlin.runCatching {
                BlockAppSignal.Type.valueOf(name ?: BlockAppSignal.Type.None.name)
            }.getOrElse { BlockAppSignal.Type.None }
        }
    }

    override suspend fun setSignal(signal: BlockAppSignal.Type): Result<Unit> {
        return runCatching { dataStorePrefs.putString(SIGNAL_KEY, signal.name) }
    }

    override suspend fun lastSignalOrNull(): BlockAppSignal.Type? {
        return runCatching {
            BlockAppSignal.Type.valueOf(dataStorePrefs.getString(SIGNAL_KEY) ?: "")
        }.getOrNull()

    }

}