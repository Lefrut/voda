package com.m.vodovoz.common.cache

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.m.vodovoz.core.network.serialization.fromJson
import com.m.vodovoz.data.vodovoz_service.model.COLORFUL_KNOPKA_DTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozButtonDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class VodovozHttpErrorCache @Inject constructor(
    mappers: HttpErrorCacheMappers<VodovozHttpError>,
) : HttpErrorCache(mappers)


@Singleton
class VodovozHttpErrorCacheMappers @Inject constructor(
    private val moshi: Moshi,
) : HttpErrorCacheMappers<VodovozHttpError> {

    override fun String.toHttpError(): VodovozHttpError {
        return moshi.fromJson<VodovozHttpError>(this)
    }

}

@Keep
data class VodovozHttpError(
    @Json(name = "title")
    val title: String?,
    @Json(name = "message")
    val message: String?,
) : HttpError()

