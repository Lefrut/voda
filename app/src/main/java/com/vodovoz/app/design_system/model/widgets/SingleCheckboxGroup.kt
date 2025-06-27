package com.vodovoz.app.design_system.model.widgets

import androidx.compose.runtime.Immutable
import com.vodovoz.app.R
import com.vodovoz.app.common.model.VodovozAddressType

@Immutable
data class SingleCheckboxGroup<out T : Any>(
    override val id: String,
    val checkbox: SingleCheckbox<T>?,
    val checkboxes: List<SingleCheckbox<T>>
): WidgetUi(id) {

    override fun value(): String {
        return checkbox?.value?.toString() ?: ""
    }
}

@Immutable
data class SingleCheckbox<out T : Any>(
    val name: String,
    val value: T
)

fun List<VodovozAddressType>.mapToSingleCheckboxes(getString: (Int) -> String): List<SingleCheckbox<Int>>{
    return mapNotNull { addressType ->
        when(addressType){
            VodovozAddressType.Personal -> { null }
            VodovozAddressType.Company -> {
                SingleCheckbox(
                    name = getString(
                        R.string.company_office_delivery_text,
                    ),
                    value = VodovozAddressType.Company.value
                )
            }
        }
    }

}





