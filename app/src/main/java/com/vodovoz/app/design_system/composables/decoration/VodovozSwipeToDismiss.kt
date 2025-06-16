package com.vodovoz.app.design_system.composables.decoration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import kotlinx.coroutines.delay

@Composable
fun VodovozSwipeToDismiss(
    modifier: Modifier = Modifier,
    onRemove: () -> Unit,
    state: SwipeToDismissBoxState = rememberSwipeToDismissBoxState(
        positionalThreshold = with(LocalDensity.current) {
            { 120.dp.toPx() }
        },
        confirmValueChange = { boxValue ->
            if (boxValue == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
            }
            true
        }
    ),
    enableDismissFromEndToStart: Boolean = true,
    gesturesEnabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {


    SwipeToDismissBox(
        modifier = modifier,
        state = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = enableDismissFromEndToStart,
        gesturesEnabled = gesturesEnabled,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_delete),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.background
                )
            }
        },
        content = {
            content()
        }
    )

    LaunchedEffect(state.currentValue) {
        delay(300L)
        if (state.currentValue != SwipeToDismissBoxValue.Settled) {
            state.reset()
        }
    }
}