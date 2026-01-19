package com.m.vodovoz.design_system.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.m.vodovoz.common.model.VodovozAction
import com.m.vodovoz.design_system.utils.toHexString
import com.m.vodovoz.domain.general.model.promotion.ActionWithButtonModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.promotion.StoryModel
import com.m.vodovoz.ui.graphics.fromHexOrUnspecified
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class StoryUi(
    val id: Long,
    val image: String,
    val pages: List<StoryPage>,
    val viewed: Boolean,
) : Parcelable

@Immutable
@Parcelize
data class StoryPage(
    val image: String,
    val actionWithButton: ActionWithButtonUi,
    val durationMillis: Int,
) : Parcelable

@Stable
@Parcelize
data class ActionWithButtonUi(
    val action: VodovozAction,
    val colorfulButton: ColorfulButtonUi,
    val image: String,
) : Parcelable {
    companion object {
        val Empty = ActionWithButtonUi(VodovozAction.Unknown("", ""), ColorfulButtonUi.Empty, "")
    }
}

@Immutable
@Parcelize
data class ColorfulButtonUi(
    val name: String,
    val backgroundColorValue: ULong,
    val textColorValue: ULong,
    val id: String = "",
    val enabled: Boolean = true,
    val loading: Boolean = false,
    val browser: Boolean? = null,
    val url: String? = null,
) : Parcelable {


    val backgroundColor: Color get() = Color(backgroundColorValue)
    val textColor: Color get() = Color(textColorValue)

    companion object {
        val Empty = ColorfulButtonUi("", Color.Unspecified.value, Color.Unspecified.value)
    }
}

fun List<ColorfulButtonUi>.updateButton(
    buttonId: String,
    newButton: (ColorfulButtonUi) -> ColorfulButtonUi,
): List<ColorfulButtonUi> {
    return map { button ->
        if (button.id == buttonId) newButton(button)
        else button
    }
}

fun List<StoryModel>.mapToUi(): List<StoryUi> {
    return mapNotNull { storyModel -> storyModel.toUi() }
}

fun StoryModel.toUi(): StoryUi {
    return StoryUi(
        id = id,
        image = previewImage,
        pages = pages.map { action ->
            StoryPage(action.image, action.toUi(), 5_000)
        },
        viewed = viewed
    )
}

fun ActionWithButtonModel.toUi(): ActionWithButtonUi {
    return ActionWithButtonUi(
        action = action,
        colorfulButton = colorfulButton.toUi(),
        image = image
    )
}

fun ColorfulButtonUi.toDomain(): ColorfulButtonModel {
    return ColorfulButtonModel(
        name = name,
        backgroundColor = Color(backgroundColorValue).toHexString(),
        textColor = Color(textColorValue).toHexString(),
        id = id,
        browser = browser,
        url = url
    )
}

fun ColorfulButtonModel.toUi(): ColorfulButtonUi {
    return ColorfulButtonUi(
        name = name,
        backgroundColorValue = Color.fromHexOrUnspecified(backgroundColor).value,
        textColorValue = Color.fromHexOrUnspecified(textColor).value,
        id = id,
        browser = browser,
        url = url
    )
}

@JvmName("mapToColorfulButtonUiList")
fun List<ColorfulButtonModel>.mapToUi(): List<ColorfulButtonUi> {
    return map { it.toUi() }
}

