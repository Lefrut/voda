package com.m.vodovoz.design_system.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.user.ForAdultsModel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class ForAdultsUi(
    val title: String,
    val description: String,
    val textBlur: String,
    val button: ColorfulButtonUi,
) : Parcelable {

    companion object {
        val Empty = ForAdultsUi("", "", "", ColorfulButtonUi.Empty)
    }
}

fun ForAdultsModel.toUi(): ForAdultsUi {
    return ForAdultsUi(
        title, description, textBlur, button.toUi()
    )
}

val ForAdultsUi?.textBlurOrEmpty: String
    get() = this?.textBlur ?: ""
