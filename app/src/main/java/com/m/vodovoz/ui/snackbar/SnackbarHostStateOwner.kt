package com.m.vodovoz.ui.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.fragment.app.Fragment

interface SnackbarHostStateOwner {

    val snackbarHostState: SnackbarHostState

}

val Fragment.snackBarHostState: SnackbarHostState?
    get() = (this as? SnackbarHostStateOwner)?.snackbarHostState