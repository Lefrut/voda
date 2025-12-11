package com.m.vodovoz.feature.block_app

import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.model.BlockSiteInfo
import com.m.vodovoz.feature.block_app.model.BlockAppContactUi
import com.m.vodovoz.feature.block_app.model.BlockAppEvent
import com.m.vodovoz.feature.block_app.model.BlockAppState
import com.m.vodovoz.feature.block_app.model.mapToUi
import com.m.vodovoz.feature.sitestate.SiteStateManager
import com.m.vodovoz.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlockAppViewModel @Inject constructor(
    private val siteStateManager: SiteStateManager,
) : MviViewModel<BlockAppState, BlockAppEvent>(BlockAppState()) {

    init {
        siteStateManager.siteStateSnapshot.data?.let { data ->
            updateStateBySiteState(data)
        }
    }

    @OptIn(FlowPreview::class)
    suspend fun listenSiteState() =
        siteStateManager.siteStateFlow.mapNotNull { siteState ->
            siteState?.data
        }.debounce(350L).collect { data ->
            updateStateBySiteState(data)
        }

    private fun updateStateBySiteState(data: BlockSiteInfo) {
        updateState { s ->
            s.copy(
                title = data.title,
                image = data.logo,
                description = data.description,
                contacts = data.contacts.mapToUi()
            )
        }

    }

    fun showTime() {
        updateState { s ->
            s.copy(showTime = true)
        }
    }

    fun hideTime() {
        updateState { s ->
            s.copy(showTime = false)
        }
    }

    fun setTime(days: String, hours: String, minutes: String, seconds: String) =
        viewModelScope.launch {
            updateState { s ->
                s.copy(days = days, hours = hours, minutes = minutes, seconds = seconds)
            }
        }

    fun navigateByContact(contact: BlockAppContactUi) = viewModelScope.launch {
        when (contact.urlType) {
            "phone" -> {
                sendEvent(BlockAppEvent.DialPhoneNumber(contact.url))
            }

            "url" -> {
                sendEvent(BlockAppEvent.OpenUrl(contact.url))
            }

            else -> {
                sendEvent(BlockAppEvent.OpenUrl(contact.url))
            }
        }
    }

}