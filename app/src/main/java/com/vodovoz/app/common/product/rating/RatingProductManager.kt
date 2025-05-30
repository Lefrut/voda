package com.vodovoz.app.common.product.rating

import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.data.MainRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RatingProductManager @Inject constructor(
    private val repository: MainRepository,
    private val accountManager: AccountManager,
) {

    private val ratings = ConcurrentHashMap<Long, Float>()
    private val ratingsStateListener = MutableSharedFlow<Map<Long, Float>>(replay = 1)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun observeRatings() = ratingsStateListener.asSharedFlow().filter { it.isNotEmpty() }

    private val showRatingSnackbarListener = MutableSharedFlow<String>()
    fun observeRatingSnackbar() = showRatingSnackbarListener.asSharedFlow()

    suspend fun rate(id: Long, oldRating: Float, rating: Float) {

    }



}

