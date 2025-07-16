package com.vodovoz.app.feature.product_filters.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RangeSliderState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.slider.VodovozRangeSlider
import com.vodovoz.app.design_system.composables.text_fields.VodovozTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFilterSlider(
    modifier: Modifier = Modifier,
    sliderState: RangeSliderState,
    currentMax: Int,
    currentMin: Int,
    onFromChange: (String) -> Unit,
    onToChange: (String) -> Unit,
    onSliderRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)) {
            VodovozTextField(
                modifier = Modifier.weight(1f),
                value = currentMin.takeIf { it >= 0 }?.toString() ?: "",
                onValueChange = onFromChange,
                prefix = stringResource(R.string.from)
            )
            Spacer(modifier = Modifier.width(8.dp))
            VodovozTextField(
                modifier = Modifier.weight(1f),
                value = currentMax.takeIf { it >= 0 }?.toString() ?: "",
                onValueChange = onToChange,
                prefix = stringResource(R.string.to)
            )
        }

        VodovozRangeSlider(
            modifier = Modifier.padding(top = 10.dp, start = 2.dp, end = 2.dp),
            onValueChange = onSliderRangeChange,
            state = sliderState
        )
    }
}