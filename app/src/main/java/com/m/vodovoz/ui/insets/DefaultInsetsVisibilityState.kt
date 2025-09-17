package com.m.vodovoz.ui.insets

import android.os.Build
import androidx.compose.runtime.Stable
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@Stable
class DefaultInsetsVisibilityState @Inject constructor() : InsetsVisibilityState {


    private val _statusBarInsets = MutableStateFlow(
        InsetsState(
            consume = true,
            type = WindowInsetsCompat.Type.statusBars()
        )
    )
    private val _navigationBarInsets = MutableStateFlow(
        InsetsState(
            consume = true,
            type = WindowInsetsCompat.Type.navigationBars()
        )
    )

    private val _imeInsets = MutableStateFlow(
        InsetsState(
            consume = true,
            type = WindowInsetsCompat.Type.ime()
        )
    )

    override val statusBarInsets: StateFlow<InsetsState>
        get() = _statusBarInsets.asStateFlow()
    override val navigationBarInsets: StateFlow<InsetsState>
        get() = _navigationBarInsets.asStateFlow()
    override val imeInsets: StateFlow<InsetsState>
        get() = _imeInsets.asStateFlow()

    override fun consumeStatusBarInsets(consume: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || consume) {
            _statusBarInsets.update { s -> s.copy(consume = consume) }
        }
    }

    override fun consumeNavigationBarInsets(consume: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || consume) {
            _navigationBarInsets.update { s -> s.copy(consume = consume) }
        }
    }

    override fun consumeIme(consume: Boolean) {
        _imeInsets.update { s ->
            s.copy(consume = consume)
        }
    }
}