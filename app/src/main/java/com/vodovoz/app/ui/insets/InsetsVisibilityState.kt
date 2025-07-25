package com.vodovoz.app.ui.insets

import kotlinx.coroutines.flow.StateFlow

interface InsetsVisibilityState {

    val statusBarInsets: StateFlow<Boolean>

    val navigationBarInsets: StateFlow<Boolean>

    fun insertStatusBarInsets(insert: Boolean)

    fun insertNavigationBarInsets(insert: Boolean)

    fun insertSystemBarInsets(
        insert: Boolean,
    ){
        insertStatusBarInsets(insert)
        insertNavigationBarInsets(insert)
    }

    val handleIme : StateFlow<Boolean>

    fun setHandleIme(handle: Boolean)


}