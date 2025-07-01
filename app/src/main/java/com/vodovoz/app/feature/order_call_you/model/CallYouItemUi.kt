package com.vodovoz.app.feature.order_call_you.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.order.CallYouItemModel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class CallYouItemUi(
    val code: String,
    val name: String,
    val description: String,
    val value: String,
): Parcelable {
    companion object {
        val Empty = CallYouItemUi("", "", "", "")
    }
}


fun CallYouItemModel.toUi(): CallYouItemUi {
    return CallYouItemUi(
        code, name, description, value
    )
}

fun List<CallYouItemModel>.mapToUi(): List<CallYouItemUi> {
    return map { it.toUi() }
}

