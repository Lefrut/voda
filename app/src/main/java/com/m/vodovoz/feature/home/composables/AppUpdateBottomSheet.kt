package com.m.vodovoz.feature.home.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.bottom_sheet.VodovozDragHandle
import com.m.vodovoz.feature.home.model.AppUpdateInfoUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUpdateBottomSheet(
    appUpdateInfoUi: AppUpdateInfoUi,
    state: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { false }
    ),
    onDismissRequest: () -> Unit,
    onButtonClick: (AppUpdateInfoUi) -> Unit,
) {
    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = onDismissRequest,
        dragHandle = {
            VodovozDragHandle()
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
        properties = ModalBottomSheetProperties(false)
    ) {
        BaseBottomSheetContent(
            name = appUpdateInfoUi.title,
            picture = appUpdateInfoUi.picture,
            description = appUpdateInfoUi.text,
            button = appUpdateInfoUi.colorfulButton,
            onButtonClick = { onButtonClick(appUpdateInfoUi) }
        )
    }
}