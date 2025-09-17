package com.m.vodovoz.feature.faq.model

sealed class FAQEvent {

    data object GoBack: FAQEvent()

}