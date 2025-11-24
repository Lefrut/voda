package com.m.vodovoz.core.network.interceptor

import com.m.vodovoz.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VersionQueryInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val newUrl = original.url.newBuilder()
            .addEncodedQueryParameter("versiyaan", BuildConfig.VERSION_NAME)
            .build()

        val newRequest = original.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}

/**
 * ViewModel содержит Products. ViewModel агрегирует список объектов с интерфейсом ProductsChanger, что возвращает
 * измененный Products.
 * ViewModel содержит observeProductUpdates(), который обходит список ProductsChanger и вызывает его метод: change()
 * чей результат используется для изменения поля Products во ViewModel.
 * Обход в observeProductUpdates() случается когда любой из потоков уведомляет о новом значении, список потоков
 * ProductsFlows, что передается параметром во ViewModel.
 *
 *
 * */