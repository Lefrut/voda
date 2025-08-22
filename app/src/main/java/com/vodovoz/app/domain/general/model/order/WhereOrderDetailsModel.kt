package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.widgets.ImageAndTextModel
import com.vodovoz.app.domain.general.model.widgets.ImageButtonModel
import com.vodovoz.app.domain.general.model.location.MapPointModel

data class WhereOrderDetailsModel(
    val title: String,
    val secondTitle: String,
    val description: String,
    val finishPoint: MapPointModel?,
    val driverPont: MapPointModel?,
    val buttons: List<ImageButtonModel>,
    val items: List<ImageAndTextModel>,
)
