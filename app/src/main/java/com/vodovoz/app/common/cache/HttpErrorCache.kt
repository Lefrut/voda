package com.vodovoz.app.common.cache

import androidx.annotation.Keep
import com.squareup.moshi.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VodovozHttpErrorCache @Inject constructor() : HttpErrorCache()

abstract class HttpErrorCache {

    private val _lastErrorData: MutableStateFlow<String?> = MutableStateFlow(null)
    val lastErrorData: StateFlow<String?> = _lastErrorData.asStateFlow()

    fun setLastError(errorData: String?) {
        _lastErrorData.update { errorData }
    }
}


@Keep
data class VodovozHttpError(
    @Json(name = "title")
    val title: String?,
    @Json(name = "message")
    val message: String?,
) : HttpError()

abstract class HttpError


interface HttpErrorCacheProvider {

    val httpErrorCache: HttpErrorCache

}

