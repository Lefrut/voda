package com.m.vodovoz.common.tab

import androidx.compose.runtime.Immutable
import com.google.android.material.tabs.TabLayout
import com.m.vodovoz.R
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TabManager @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) {

    private val tabStateListener = MutableSharedFlow<Int>()
    fun observeTabState() = tabStateListener.asSharedFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val bottomNavCartStateListener = MutableStateFlow<BottomNavCartState?>(null)
    fun observeBottomNavCartState() = bottomNavCartStateListener.asStateFlow()

    private val tabReselectListener = MutableStateFlow(DEFAULT_STATE)
    fun observeTabReselect() = tabReselectListener.asStateFlow()
    fun setDefaultState() {
        tabReselectListener.value = DEFAULT_STATE
    }

    private val tabAuthRedirectListener = MutableStateFlow<Int>(DEFAULT_AUTH_REDIRECT)
    fun fetchAuthRedirect() = tabAuthRedirectListener.value

    private val tabVisibilityListener = MutableStateFlow(true)
    fun observeTabVisibility() = tabVisibilityListener.asStateFlow()

    fun setAuthRedirect(graphId: Int) {
        tabAuthRedirectListener.value = graphId
    }

    fun setDefaultAuthRedirect() {
        tabAuthRedirectListener.value = DEFAULT_AUTH_REDIRECT
    }

    fun selectTab(id: Int) {
        scope.launch { tabStateListener.emit(id) }
    }

    fun reselect(id: Int) {
        tabReselectListener.value = id
    }

    suspend fun updateBottomNavCartState() =
        vodovozServiceRepository.getBottomCart().onEach { result ->
            result.onSuccess { bottomCartModel ->
                bottomNavCartStateListener.value = BottomNavCartState(
                    count = bottomCartModel.count,
                    total = bottomCartModel.total
                )
            }.onFailure {
                bottomNavCartStateListener.value = null
            }
        }.collect {}


    fun changeTabVisibility(vis: Boolean) {
        tabVisibilityListener.value = vis
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
        const val DEFAULT_STATE = -1

        @JvmField
        val DEFAULT_AUTH_REDIRECT = R.id.graph_profile
    }
}

fun TabManager.showTab(){
    changeTabVisibility(true)
}

fun TabManager.hideTab(){
    changeTabVisibility(false)
}