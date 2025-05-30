package com.vodovoz.app.domain.general.model.order

import com.vodovoz.app.domain.general.model.ImageAndTextModel
import com.vodovoz.app.domain.general.model.ImageButtonModel
import com.vodovoz.app.domain.general.model.MapPointModel

data class WhereOrderDetailsModel(
    val title: String,
    val secondTitle: String,
    val finishPoint: MapPointModel?,
    val driverPont: MapPointModel?,
    val buttons: List<ImageButtonModel>,
    val items: List<ImageAndTextModel>
)
