package com.m.vodovoz.feature.home.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.m.vodovoz.design_system.composables.bottom_sheet.VodovozDragHandle
import com.m.vodovoz.design_system.model.SpecialPromotionUi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialPromotionBottomSheet(
    specialPromotionUi: SpecialPromotionUi,
    state: SheetState = rememberModalBottomSheetState(true),
    onDismissRequest: () -> Unit,
    onButtonClick: (SpecialPromotionUi) -> Unit,
) {
    ModalBottomSheet(
        modifier = Modifier,
        sheetState = state,
        onDismissRequest = onDismissRequest,
        dragHandle = {
            VodovozDragHandle()
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
    ) {
        BaseBottomSheetContent(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            name = specialPromotionUi.name,
            picture = specialPromotionUi.picture,
            description = specialPromotionUi.text,
            button = specialPromotionUi.actionWithButton?.colorfulButton,
            onButtonClick = { onButtonClick(specialPromotionUi) }
        )
    }
}