package com.m.vodovoz.feature.auth.login_by_phone_code.model

sealed interface LoginByPhoneCodeEvent {

    data object GoBack: LoginByPhoneCodeEvent
    data object RefreshProfile : LoginByPhoneCodeEvent

}