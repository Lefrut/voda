package com.vodovoz.app.feature.cart.ordering.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextField
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Composable
fun OrderingCommentColumn(
    modifier: Modifier = Modifier,
    comment: FieldUi,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        val title = comment.label

        if (title.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        VodovozTextField(
            field = comment.copy(label = ""),
            onFieldChange = onFieldChange,
            minLines = 2,
            maxLines = 3
        )
    }
}