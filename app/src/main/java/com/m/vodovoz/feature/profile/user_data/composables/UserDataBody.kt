package com.m.vodovoz.feature.profile.user_data.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.m.vodovoz.design_system.model.widgets.FieldUi

@Suppress("NonSkippableComposable")
@Composable
fun UserDataBody(
    modifier: Modifier = Modifier,
    photo: String,
    photoTitle: String,
    photoDescription: String,
    fields: List<FieldUi>,
    buttonEnabled: Boolean,
    buttonLoading: Boolean,
    deleteText: String,
    deleteTextPrefix: String,
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
            onFieldClick = onFieldClick,
        )

        VodovozButton(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp),
            text = stringResource(id = R.string.save),
            onClick = onSaveDataClick,
            enabled = buttonEnabled,
            isLoading = buttonLoading
        )

        DeleteText(
            modifier = Modifier.padding(
                top = 16.dp,
                bottom = 48.dp,
                start = 16.dp,
                end = 16.dp
            ),
            prefix = deleteTextPrefix,
            deleteText = deleteText,
            onDeleteAccountClick = onDeleteAccountClick
        )
    }
}

@Composable
private fun DeleteText(
    modifier: Modifier = Modifier,
    prefix: String,
    deleteText: String,
    onDeleteAccountClick: () -> Unit,
) {
    val labelSmall = MaterialTheme.typography.labelSmall.copy(
        letterSpacing = 0.sp, lineHeight = 0.sp
    )

    val deleteAnnotatedString = buildAnnotatedString {
        withStyle(
            style = labelSmall.copy(
                color = MaterialTheme.colorScheme.surfaceTint
            ).toSpanStyle()
        ) {
            append(prefix)
            append(stringResource(id = R.string.space))
        }

        pushLink(LinkAnnotation.Clickable("delete", null) { onDeleteAccountClick() })
        withStyle(
            style = labelSmall.copy(
                color = MaterialTheme.colorScheme.error
            ).toSpanStyle()
        ) {
            append(deleteText)
        }
        pop()
    }


    Text(
        modifier = modifier,
        text = deleteAnnotatedString,
        textAlign = TextAlign.Start
    )
}
