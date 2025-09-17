package com.m.vodovoz.design_system.modifiers

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

fun Modifier.detectTap(onClick: () -> Unit): Modifier {
    return pointerInput(Unit) {
        awaitPointerEventScope {
            while (true){
                awaitFirstDown(true, PointerEventPass.Initial)
                val up = waitForUpOrCancellation(PointerEventPass.Initial)


                if (up != null && !up.isConsumed) {
                    onClick()
                }
            }
        }
    }
}