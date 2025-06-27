package com.vodovoz.app.feature.profile.user_data.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Suppress("NonSkippableComposable")
@Composable
fun UserDataBody(
    modifier: Modifier = Modifier,
    photo: String,
    photoTitle: String,
    photoDescription: String,
    fields: List<FieldUi>,
    buttonEnabled: Boolean,
    onFieldChange: (FieldUi, FieldUi) -> Unit,
    onFieldClick: (FieldUi) -> Unit,
    onSaveDataClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onAvatarClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        UserDataPhotoColumn(
            modifier = Modifier.padding(top = 8.dp),
            photo = photo,
            photoTitle = photoTitle,
            photoDescription = photoDescription,
            onPhotoClick = onAvatarClick
        )


        VodovozTextFieldsColumn(
            modifier = Modifier.padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            fields = fields,
            onFieldChange = onFieldChange,
            onFieldClick = onFieldClick
        )

        VodovozButton(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp),
            text = stringResource(id = R.string.save),
            onClick = onSaveDataClick,
            enabled = buttonEnabled
        )

        Text(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 48.dp)
                .align(Alignment.Start)
                .clip(MaterialTheme.shapes.small)
                .clickable(onClick = onDeleteAccountClick)
                .padding(16.dp),
            text = stringResource(id = R.string.delete_account),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}