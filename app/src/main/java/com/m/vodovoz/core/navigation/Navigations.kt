package com.m.vodovoz.core.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavOptionsBuilder
import com.m.vodovoz.R

fun NavOptionsBuilder.slideAnim() {
    anim {
        exit = R.anim.fade_out
        enter = R.anim.slide_in_right
        popEnter = R.anim.slide_in_left
        popExit = R.anim.slide_out_right
    }
}

fun NavOptionsBuilder.expandAnim() {
    anim {
        exit = R.anim.fade_out
        enter = R.anim.slide_in_bottom
        popExit = R.anim.slide_out_bottom
    }
}

data object CommonArgs {
    const val QUERY_PARAMS: String = "queryParams"
}

data object AuthArgs {
    const val ACCOUNT_TYPE_ID: String = "accountTypeId"
}

data object LoginByPhoneCodeArgs {
    const val USER_URL = "user_url"
    const val PHONE = "phoneNumber"
    const val WAIT_SECONDS = "waitRequestCodeSeconds"
}

fun CommonArgs.queryParamsTo(second: Map<String, String>): Pair<String, Map<String, String>> {
    return QUERY_PARAMS to second
}

fun SavedStateHandle.getQueryParams(): Map<String, String> {
    return get<Map<String, String>>(CommonArgs.QUERY_PARAMS).orEmpty()
}
