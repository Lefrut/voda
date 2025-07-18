package com.vodovoz.app.core.network.interceptor

import com.vodovoz.app.common.datastore.DataStorePrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockAppInterceptor @Inject constructor(
    @ApplicationContext
    private val dataStorePrefs: DataStorePrefs
): Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        val response = chain.proceed(builder.build())

        if(response.code == 403){
            //todo
            dataStorePrefs.putBoolean("", true)
        }

        return response
    }


}