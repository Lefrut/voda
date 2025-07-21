package com.vodovoz.app.common.block_app_signal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow


interface BlockAppSignal {

    suspend fun getSignalFlow(): Flow<Type>

    suspend fun setSignal(signal: Type): Result<Unit>

    suspend fun lastSignalOrNull(): Type?

    enum class Type {
        Reload, Block, None
    }

}