package com.vodovoz.app.feature.auth.login_by_phone_code.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodovoz.app.R

@Composable
fun LoginByPhoneCodeBody(
    modifier: Modifier = Modifier,
    code: String,
    phone: String,
    secondsText: String,
    canRequestCode: Boolean,
    requestCodeLoading: Boolean,
    onCodeChange: (String) -> Unit,
    onCodeSend: () -> Unit,
    onCodeRequest: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.align(Alignment.Start),
            text = stringResource(id = R.string.enter_code_description, phone),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodySmall
        )

        val focusRequester = remember {
            FocusRequester()
        }

        OtpTextField(
            modifier = Modifier
                .padding(top = 58.dp)
                .focusable()
                .focusRequester(focusRequester),
            otpText = code,
            onOtpTextChange = onCodeChange,
            onDone = onCodeSend
        )

        LaunchedEffect(focusRequester) {
            focusRequester.requestFocus()
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                requestCodeLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        strokeWidth = 2.dp,
                        trackColor = Color.Transparent,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                canRequestCode -> {
                    Text(
                        modifier = Modifier.clickable(onClick = onCodeRequest),
                        text = stringResource(id = R.string.resend_code),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp)
                    )
                }
                else -> {
                    Text(
                        text = stringResource(id = R.string.resend_sms_hint),
                        color = MaterialTheme.colorScheme.surfaceTint,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = secondsText,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp)
                    )

                }
            }
        }
    }
}