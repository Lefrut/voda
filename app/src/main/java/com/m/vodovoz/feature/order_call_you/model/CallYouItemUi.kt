package com.m.vodovoz.feature.order_call_you.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.order.CallYouItemModel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class CallYouItemUi(
    val code: String,
    val name: String,
    val description: String,
    val value: String,
    val enabled: Boolean
): Parcelable {
    companion object {
        val Empty = CallYouItemUi("", "", "", "", false)
    }
}


fun CallYouItemModel.toUi(): CallYouItemUi {
    return CallYouItemUi(
        code, name, description, value, enabled
    )
}

fun List<CallYouItemModel>.mapToUi(): List<CallYouItemUi> {
    return map { it.toUi() }
}

