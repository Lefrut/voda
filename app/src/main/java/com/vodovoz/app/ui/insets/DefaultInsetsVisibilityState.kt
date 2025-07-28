package com.vodovoz.app.ui.insets

import android.os.Build
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject


@Stable
class DefaultInsetsVisibilityState @Inject constructor() : InsetsVisibilityState {


    private val _statusBarInsets = MutableStateFlow(true)
    private val _navigationBarInsets = MutableStateFlow(true)
    private val _handleIme = MutableStateFlow(true)

    override val statusBarInsets: StateFlow<Boolean>
        get() = _statusBarInsets.asStateFlow()
    override val navigationBarInsets: StateFlow<Boolean>
        get() = _navigationBarInsets.asStateFlow()

    override fun insertStatusBarInsets(insert: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || insert) {
            _statusBarInsets.value = insert
        }
    }

    override fun insertNavigationBarInsets(insert: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R || insert) {
            _navigationBarInsets.value = insert
        }
    }

    override val handleIme: StateFlow<Boolean>
        get() = _handleIme.asStateFlow()

    override fun setHandleIme(handle: Boolean) {
        _handleIme.update { handle }
    }
}