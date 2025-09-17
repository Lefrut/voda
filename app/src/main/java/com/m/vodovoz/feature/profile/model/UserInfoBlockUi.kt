package com.m.vodovoz.feature.profile.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.TextButtonUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.user.UserInfoBlockModel

@Immutable
data class UserInfoBlockUi(
    val username: String,
    val imageUrl: String,
    val textButton: TextButtonUi,
) {
    companion object {
        val Empty = UserInfoBlockUi("", "", TextButtonUi.Empty)
    }
}

fun UserInfoBlockModel.toUi(): UserInfoBlockUi{
    return UserInfoBlockUi(
        username =fullName.ifEmpty { phoneNumber } ,
        imageUrl = imageUrl,
        textButton = textButton.toUi()
    )
}