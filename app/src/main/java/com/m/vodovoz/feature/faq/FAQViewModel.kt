package com.m.vodovoz.feature.faq

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.feature.buy_certificate.model.FAQItemUi
import com.m.vodovoz.feature.buy_certificate.model.FAQUi
import com.m.vodovoz.feature.faq.model.FAQEvent
import com.m.vodovoz.feature.faq.model.FAQState
import com.m.vodovoz.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FAQViewModel @Inject constructor(
    val tabManager: TabManager,
    savedStateHandle: SavedStateHandle,
) :
    MviViewModel<FAQState, FAQEvent>(FAQState()) {

    private val faq = savedStateHandle.get<FAQUi>("faq") ?: FAQUi.Empty.also {
        navigateBack()
    }

    init {
        initFAQ(faq)
    }

    private fun initFAQ(faq: FAQUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                name = faq.name,
                items = faq.items
            )
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(FAQEvent.GoBack)
    }

    fun changeExpand(faqItem: FAQItemUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                items = s.items.map {
                    if (faqItem.name == it.name) it.copy(expanded = !it.expanded) else it
                }
            )
        }
    }


}
