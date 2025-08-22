package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.boolean
import com.vodovoz.app.common.model.from
import com.vodovoz.app.data.vodovoz_service.model.FIELD_DTO
import com.vodovoz.app.data.vodovoz_service.model.FormDTO
import com.vodovoz.app.domain.general.model.widgets.FieldModel
import com.vodovoz.app.domain.general.model.order.FormModel

fun FormDTO.toDomain(): FormModel {
    return FormModel(
        title = TITLE ?: "",
        description = INFORMIROVANIE ?: "",
        fields = (POLYA ?: DANNYE)?.mapNotNull { it.toDomain() } ?: emptyList(),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("Colorful button can't be null in PreOrder:$this")
    )
}

fun FIELD_DTO.toDomain(): FieldModel? {
    return FieldModel(
        id = SID ?: ID ?: return null,
        label = TITLE ?: NAME ?: "",
        value = VALUE ?: "",
        valueType = TITLE_TYPE ?: TYPE ?: "text",
        isRequired = VodovozBoolean.from(REQUIRED ?: OBYZATELEN).boolean,
        readOnly = VodovozBoolean.from(ZAPRETREDAKTOR).boolean,
        supportingText = COMMENTS ?: "",
        hint = TEXT_V_POLE ?: ""
    )
}

