package com.m.vodovoz.feature.product_filters.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RangeSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.slider.VodovozRangeSlider
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextField
import com.m.vodovoz.util.formatNumber
import com.m.vodovoz.util.isTrailingDotOnly

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun ProductFilterSlider(
    modifier: Modifier = Modifier,
    sliderState: RangeSliderState,
    currentMaxText: String,
    currentMinText: String,
    noinline onFromChange: (Float) -> Unit,
    noinline onToChange: (Float) -> Unit,
    noinline onSliderRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
) {
    var minFieldValue by remember(currentMinText) {
        mutableStateOf(currentMinText.formatNumber())
    }

    var maxFieldValue by remember(currentMaxText) {
        mutableStateOf(currentMaxText.formatNumber())
    }

    Column(modifier = modifier) {
        Row(modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)) {
            VodovozTextField(
                modifier = Modifier.weight(1f),
                value = minFieldValue,
                onValueChange = { str ->
                    val formattedStr = str.formatNumber()
                    minFieldValue = formattedStr

                    val float = formattedStr.toFloatOrNull()
                    if (!formattedStr.isTrailingDotOnly() && float != null) {
                        onFromChange(float)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                prefix = stringResource(R.string.from)
            )
            Spacer(modifier = Modifier.width(8.dp))

            VodovozTextField(
                modifier = Modifier.weight(1f),
                value = maxFieldValue,
                onValueChange = { str ->
                    val formattedStr = str.formatNumber()
                    maxFieldValue = formattedStr

                    val float = formattedStr.toFloatOrNull()
                    if (!formattedStr.isTrailingDotOnly() && float != null) {
                        onToChange(float)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                prefix = stringResource(R.string.to),
            )
        }

        VodovozRangeSlider(
            modifier = Modifier.padding(top = 10.dp, start = 2.dp, end = 2.dp),
            onValueChange = onSliderRangeChange,
            state = sliderState
        )
    }

}