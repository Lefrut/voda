package com.m.vodovoz.feature.delivery_date.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.order.DeliveryDateOptionModel
import com.m.vodovoz.util.formatters.VodovozDateFormatters
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Immutable
@Parcelize
data class DeliveryDateOptionUi(
    val code: String,
    val name: String,
    val value: String,
) : Parcelable {
    companion object {
        val Empty = DeliveryDateOptionUi("", "", "")


    }
}

fun DeliveryDateOptionUi.displayDate(
    today: String,
    tomorrow: String,
    currentDate: LocalDate = LocalDate.now(),
): String {
    val localDate = try {
        LocalDate.parse(value, VodovozDateFormatters.DMY)
    } catch (_: Exception) {
        null
    }
    return when (localDate) {
        currentDate -> today
        currentDate.plusDays(1) -> tomorrow
        else -> value
    }
}


fun List<DeliveryDateOptionModel>.mapToUi(): List<DeliveryDateOptionUi> {
    return mapNotNull { it.toUi() }
}

fun DeliveryDateOptionModel.toUi(): DeliveryDateOptionUi {
    return DeliveryDateOptionUi(
        code = code,
        name = name,
        value = value
    )
}
