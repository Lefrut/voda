package com.m.vodovoz.core.network

import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.core.network.interceptor.BaseUrlInterceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VodovozUrlManager @Inject constructor(
    private val baseUrlInterceptor: BaseUrlInterceptor,
) {

    fun setUrl(url: String) {
        baseUrlInterceptor.updateBaseUrl(url)
        VodovozWebConfig.setUrl(url)
    }

}
