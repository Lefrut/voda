package com.m.vodovoz.common.tab

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.main.BottomNavKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Stable
class TabManager @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) {

    private val bottomNavCartStateListener = MutableStateFlow<BottomNavCartState?>(null)
    fun observeBottomNavCartState() = bottomNavCartStateListener.asStateFlow()

    private val tabReselectListener = MutableStateFlow<BottomNavKey?>(null)
    fun observeTabReselect() = tabReselectListener.asStateFlow()
    fun notifyTabReselect(tab: BottomNavKey) {
        tabReselectListener.value = tab
    }

    fun resetTabReselect() {
        tabReselectListener.value = null
    }

    private val tabAuthRedirectListener = MutableStateFlow<BottomNavKey>(DEFAULT_AUTH_REDIRECT)
    fun fetchAuthRedirect() = tabAuthRedirectListener.value

    private val showBottomBar = MutableStateFlow(true)
    fun observeShowBottomBar() = showBottomBar.asStateFlow()

    fun setAuthRedirect(tab: BottomNavKey) {
        tabAuthRedirectListener.value = tab
    }

    fun setDefaultAuthRedirect() {
        tabAuthRedirectListener.value = DEFAULT_AUTH_REDIRECT
    }

    suspend fun updateBottomNavCartState() =
        vodovozServiceRepository.getBottomCart().onEach { result ->
            result.onSuccess { bottomCartModel ->
                bottomNavCartStateListener.update {
                    BottomNavCartState(
                        count = bottomCartModel.count,
                        total = bottomCartModel.total
                    )
                }
            }.onFailure {
                bottomNavCartStateListener.update { null }
            }
        }.collect {}


    fun setTabVisibility(visible: Boolean) {
        showBottomBar.value = visible
    }

    fun clearBottomNavCartState() {
        bottomNavCartStateListener.value = null
    }

    @Immutable
    data class BottomNavCartState(
        val count: Int,
        val total: Int,
    )

    companion object {
        @JvmField
        val DEFAULT_AUTH_REDIRECT: BottomNavKey = BottomNavKey.Profile
    }
}

fun TabManager.showTab() {
    setTabVisibility(true)
}

fun TabManager.hideTab() {
    setTabVisibility(false)
}
