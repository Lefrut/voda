package com.m.vodovoz.domain.general.model.location

import com.m.vodovoz.domain.general.model.widgets.ImageAndTextModel

class MapPopupWindowModel(
    val title: String,
    val description: String,
    val items: List<ImageAndTextModel>
)