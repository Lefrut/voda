package com.vodovoz.app.core.network.interceptor

import com.vodovoz.app.common.block_app_signal.BlockAppSignal
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockAppInterceptor @Inject constructor(
    private val blockAppSignal: BlockAppSignal,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        val response = chain.proceed(builder.build())



        if (response.code == 403) {
            runBlocking {
                blockAppSignal.setSignal(BlockAppSignal.Type.Block)
            }
        }

        return response
    }

}