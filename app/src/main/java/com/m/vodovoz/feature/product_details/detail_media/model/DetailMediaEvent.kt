package com.m.vodovoz.feature.product_details.detail_media.model

sealed interface DetailMediaEvent {

    data object GoBack: DetailMediaEvent
    data object MakeLandscape : DetailMediaEvent
    data object MakePortrait : DetailMediaEvent

}