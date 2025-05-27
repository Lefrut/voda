package com.vodovoz.app.design_system.composables.placeholders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.design_system.model.ForAdultsUi

@Composable
fun ForAdultsPlaceholder(
    modifier: Modifier = Modifier,
    forAdults: ForAdultsUi,
    onBackClick: () -> Unit,
    onApplyClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VodovozTopBar(onBack = onBackClick, title = forAdults.title)
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = forAdults.description,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp)
        )
        Spacer(modifier = Modifier.weight(1f))
        val button = forAdults.button

        VodovozButton(
            modifier = Modifier.padding(vertical = 24.dp),
            text = button.name,
            onClick = onApplyClick,
            colors = ButtonDefaults.buttonColors(
                contentColor = button.textColor,
                containerColor = button.backgroundColor
            )
        )
    }
}