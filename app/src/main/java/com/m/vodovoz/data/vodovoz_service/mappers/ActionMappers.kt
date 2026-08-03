package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.model.ACTION_DTO
import com.m.vodovoz.common.model.DataAllAction
import com.m.vodovoz.common.model.VodovozAction
import com.m.vodovoz.common.model.dataAllActionOf
import com.m.vodovoz.common.model.vodovozActionOf


fun ACTION_DTO.toAction(blockId: Long): VodovozAction? {
    return vodovozActionOf(action = ACTION, id = ID, blockId = blockId)
}

fun String.toDataAllAction(): DataAllAction = dataAllActionOf(this)
