package com.vodovoz.app.design_system.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.vodovoz.app.common.content.Event
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