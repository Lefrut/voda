package com.vodovoz.app.feature.block_app

import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.model.VodovozSiteStateData
import com.vodovoz.app.feature.block_app.model.BlockAppContactUi
import com.vodovoz.app.feature.block_app.model.BlockAppEvent
import com.vodovoz.app.feature.block_app.model.BlockAppState
import com.vodovoz.app.feature.block_app.model.mapToUi
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.ui.mvi.MviViewModel
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
        siteStateManager.siteStateSnapshot?.data?.let { data ->
            updateStateBySiteState(data)
        }
    }

    @OptIn(FlowPreview::class)
    suspend fun listenSiteState() =
        siteStateManager.siteStateFlow.mapNotNull { siteState ->
            siteState?.data
        }.debounce(500L).collect { data ->
            updateStateBySiteState(data)
        }

    private fun updateStateBySiteState(data: VodovozSiteStateData) {
        _state.update { s ->
            s.copy(
                title = data.title,
                image = data.logo,
                description = data.description,
                contacts = data.contacts.mapToUi()
            )
        }

    }

    fun showTime() {
        _state.update { s ->
            s.copy(showTime = true)
        }
    }

    fun hideTime() {
        _state.update { s ->
            s.copy(showTime = false)
        }
    }

    fun setTime(days: String, hours: String, minutes: String, seconds: String) =
        viewModelScope.launch {
            _state.update { s ->
                s.copy(days = days, hours = hours, minutes = minutes, seconds = seconds)
            }
        }

    fun navigateByContact(contact: BlockAppContactUi) = viewModelScope.launch {
        when (contact.urlType) {
            "phone" -> {
                _events.emit(BlockAppEvent.DialPhoneNumber(contact.url))
            }
            "url" -> {
                _events.emit(BlockAppEvent.OpenUrl(contact.url))
            }
            else -> {
                _events.emit(BlockAppEvent.OpenUrl(contact.url))
            }
        }
    }

}