package com.m.vodovoz.feature.profile.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.m.vodovoz.domain.general.model.user.ProfileWalletItemModel
import com.m.vodovoz.domain.general.model.user.ProfilePopupWindowModel
import com.m.vodovoz.ui.graphics.fromHexOrUnspecified

@Immutable
data class ProfileWalletItemUi(
    val backgroundColor: Color,
    val title: String,
    val titleColor: Color,
    val description: String,
    val descriptionColor: Color,
    val imageUrl: String,
    val id: String,
    val popupWindow: ProfilePopupWindowUi? = null,
)

fun List<ProfileWalletItemModel>.mapToUi(): List<ProfileWalletItemUi>{
    return map { it.toUi() }
}

fun ProfileWalletItemModel.toUi(): ProfileWalletItemUi {
    return ProfileWalletItemUi(
        backgroundColor = Color.fromHexOrUnspecified(background),
        title = title,
        titleColor = Color.fromHexOrUnspecified(titleColor),
        description = description,
        descriptionColor = Color.fromHexOrUnspecified(descriptionColor),
        imageUrl = imageUrl,
        id = id,
        popupWindow = popupWindow?.toUi()
    )
}

@Immutable
data class ProfilePopupWindowUi(
    val title: String,
    val text: String,
)

fun ProfilePopupWindowModel.toUi(): ProfilePopupWindowUi {
    return ProfilePopupWindowUi(
        title = title,
        text = text
    )
}