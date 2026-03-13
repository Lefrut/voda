package com.m.vodovoz.feature.auth.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.SwitchUi

@Immutable
interface AuthContentOperations {

    suspend fun listenAuthDetailsChanges()

    fun onBackClick() = Unit

    fun changeField(field: FieldUi, updatedField: FieldUi)

    fun changeCheckbox(checkbox: CheckboxUi, updatedCheckbox: CheckboxUi)

    fun clickButton(button: ColorfulButtonUi)

    fun clickHyperlink(url: String, title: String)

    fun changeSwitch(switch: SwitchUi, updatedSwitch: SwitchUi)

    fun clickForgotPassword() = Unit

    fun onDispose()

}
