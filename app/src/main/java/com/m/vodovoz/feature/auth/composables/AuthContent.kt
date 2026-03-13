package com.m.vodovoz.feature.auth.composables

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.composables.checkbox.VodovozCheckbox
import com.m.vodovoz.design_system.composables.swich.VodovozSwitch
import com.m.vodovoz.design_system.composables.text.LinkedText
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextFieldsColumn
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.auth.model.AuthContentOperations
import com.m.vodovoz.feature.auth.model.AuthDetailsUi
import com.m.vodovoz.ui.units.minus

@Composable
fun AuthContent(
    modifier: Modifier = Modifier,
    operations: AuthContentOperations,
    authDetails: AuthDetailsUi,
) {
    LifecycleEffect { operations.listenAuthDetailsChanges() }

    DisposableEffect(Unit) {
        onDispose {
            operations.onDispose()
        }
    }

    BackHandler {
        operations.onBackClick()
    }



    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        VodovozTopBar(
            onBack = operations::onBackClick,
            title = authDetails.title
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            val accountTypeSwitches = authDetails.accountTypeSwitches
            val headlineSmall = MaterialTheme.typography.headlineSmall

            if (accountTypeSwitches.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                    text = stringResource(R.string.choose_account),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = headlineSmall.copy(fontSize = headlineSmall.fontSize - 1.sp)
                )

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                )

                accountTypeSwitches.forEach { switchUi ->
                    key(switchUi.id) {
                        VodovozSwitch(
                            switch = switchUi,
                            onSwitchChange = operations::changeSwitch
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = accountTypeSwitches.isEmpty() || accountTypeSwitches.any { it.value },
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 220)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 180)
                )
            ) {
                key("auth_content") {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                        Spacer(modifier = Modifier.height(8.dp))

                        if (authDetails.description.isNotEmpty()) {
                            Text(
                                modifier = Modifier.padding(bottom = 24.dp),
                                text = authDetails.description,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (authDetails.fields.isNotEmpty()) {
                            VodovozTextFieldsColumn(
                                fields = authDetails.fields,
                                onFieldChange = operations::changeField,
                            )
                        }

                        if (authDetails.showForgotPassword) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .padding(top = 8.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable(onClick = operations::clickForgotPassword),
                                text = stringResource(id = R.string.forgot_password),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }


                        val checkboxes = authDetails.checkboxes

                        if (checkboxes.isNotEmpty()) {
                            Column(
                                modifier = Modifier.padding(top = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                checkboxes.forEach { checkbox ->
                                    key(checkbox.id) {
                                        VodovozCheckbox(
                                            checkbox = checkbox,
                                            onCheckboxClick = operations::changeCheckbox,
                                            onUrlClick = operations::clickHyperlink
                                        )
                                    }
                                }
                            }
                        }

                        val warning = authDetails.warning
                        val spaceText = stringResource(id = R.string.space)

                        if (warning.isNotEmpty()) {
                            LinkedText(
                                modifier = Modifier.padding(top = 16.dp),
                                text = warning,
                                onUrlClick = { url, index ->
                                    val title = authDetails.waringTitles.getOrElse(index) {
                                        spaceText
                                    }
                                    operations.clickHyperlink(url, title)
                                }
                            )
                        }

                        VodovozButtonsColumn(
                            modifier = Modifier.padding(vertical = 24.dp),
                            buttons = authDetails.buttons,
                            onButtonClick = operations::clickButton
                        )
                    }
                }
            }
        }
    }

}
