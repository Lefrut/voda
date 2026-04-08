package com.m.vodovoz.feature.faq

import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.feature.buy_certificate.model.FAQItemUi
import com.m.vodovoz.feature.buy_certificate.model.FAQUi
import com.m.vodovoz.feature.faq.api.FAQNavKey
import com.m.vodovoz.feature.faq.model.FAQEvent
import com.m.vodovoz.feature.faq.model.FAQState
import com.m.vodovoz.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = FAQViewModel.Factory::class)
class FAQViewModel @AssistedInject constructor(
    val tabManager: TabManager,
    @Assisted private val navKey: FAQNavKey,
) :
    MviViewModel<FAQState, FAQEvent>(FAQState()) {

    private val faq = navKey.faq

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

    @AssistedFactory
    interface Factory {
        fun create(navKey: FAQNavKey): FAQViewModel
    }


}
