package com.m.vodovoz.feature.home.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter

import coil3.request.crossfade
import com.m.vodovoz.design_system.composables.bottom_sheet.VodovozDragHandle
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.model.SpecialPromotionUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialPromotionBottomSheet(
    specialPromotionUi: SpecialPromotionUi,
    state: SheetState = rememberModalBottomSheetState(true),
    onDismissRequest: () -> Unit,
    onButtonClick: (SpecialPromotionUi) -> Unit,
) {
    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = onDismissRequest,
        dragHandle = {
            VodovozDragHandle()
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
    ) {
        BaseBottomSheetContent(
            name = specialPromotionUi.name,
            picture = specialPromotionUi.picture,
            description = specialPromotionUi.text,
            button = specialPromotionUi.actionWithButton?.colorfulButton,
            onButtonClick = { onButtonClick(specialPromotionUi) }
        )
    }
}