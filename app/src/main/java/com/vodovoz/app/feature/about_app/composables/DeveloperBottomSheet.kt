package com.vodovoz.app.feature.about_app.composables

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.bottom_sheet.VodovozDragHandle
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.design_system.composables.button.VodovozButtonDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperBottomSheet(
    modifier: Modifier = Modifier,
    onModeClick: (AppMode) -> Unit,
    onDismissClick: () -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(),
        onDismissRequest = onDismissClick,
        dragHandle = { VodovozDragHandle() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppMode.entries.forEach { mode ->
                VodovozButton(
                    text = stringResource(mode.titleId),
                    onClick = { onModeClick(mode) },
                    colors = if (mode == AppMode.Test) {
                        VodovozButtonDefaults.secondaryColors()
                    } else {
                        VodovozButtonDefaults.primaryColors()
                    }
                )
            }
        }
    }
}


enum class AppMode(
    @StringRes
    val titleId: Int,
) {
    Prod(R.string.prod),
    Test(R.string.test),
}