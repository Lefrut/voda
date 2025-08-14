package com.vodovoz.app.design_system.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.LifecycleStartEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Suppress("NonSkippableComposable")
@Composable
fun LifecycleEffect(
    vararg keys: Any?,
    block: suspend CoroutineScope.() -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    LifecycleStartEffect(*keys) {
        val job = coroutineScope.launch {
            block()
        }
        onStopOrDispose {
            job.cancel()
        }
    }
}