package com.m.vodovoz.feature.auth.login_by_phone_code

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.feature.auth.login_by_phone_code.composables.LoginByPhoneCodeBody
import com.m.vodovoz.feature.auth.login_by_phone_code.model.LoginByPhoneCodeState

@Composable
fun LoginByPhoneCodeScreen(viewState: LoginByPhoneCodeState, viewModel: LoginByPhoneCodeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        VodovozTopBar(
            onBack = { viewModel.navigateBack() },
            title = stringResource(id = R.string.enter_code_title)
        )
        LoginByPhoneCodeBody(
            modifier = Modifier.weight(1f),
            code = viewState.code,
            phone = viewState.phone,
            secondsText = viewState.waitSecondsText,
            canRequestCode = viewState.canRequestCode,
            requestCodeLoading = viewState.requestCodeLoading,
            otpCount = viewModel.smsCodeCount,
            onCodeChange = { code ->
                viewModel.changeCode(code)
            },
            onCodeSend = {
                viewModel.sendCode()
            },
            onCodeRequest = {
                viewModel.requestCode()
            },
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (viewState.blockScreen) {
        LoadingPlaceholder(
            modifier = Modifier
                .systemBarsPadding()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        event.changes.forEach { change ->
                            change.consume()
                        }
                    }
                },
            containerColor = MaterialTheme.colorScheme.background.copy(0.5f)
        )
    }
}