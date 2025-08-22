package com.vodovoz.app.design_system.modifiers

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.detectTap(onClick: () -> Unit): Modifier {
    return pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(pass = PointerEventPass.Initial)
            waitForUpOrCancellation(pass = PointerEventPass.Initial)
            onClick()
        }
    }
}