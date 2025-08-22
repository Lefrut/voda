package com.vodovoz.app.domain.general.model.location

import com.vodovoz.app.domain.general.model.widgets.ImageAndTextModel

class MapPopupWindowModel(
    val title: String,
    val description: String,
    val items: List<ImageAndTextModel>
)