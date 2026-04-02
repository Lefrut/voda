package com.m.vodovoz.feature.auth.login_by_email.api

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey

@Immutable
data class LoginByEmailNavKey(
    val accountTypeId: String? = null,
    val source: Source = Source.None,
    override val parentContentKey: String? = null,
) : NavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "LoginByEmailNavKey(accountTypeId=$accountTypeId, source=$source)"

    enum class Source {
        None,
        Login,
    }

    companion object {
        const val NAV_NAME: String = "feature/auth/login_by_email/LoginByEmail"
    }
}
