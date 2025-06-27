package com.vodovoz.app.feature.map.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.ExtendedTheme
import com.vodovoz.app.feature.home.composables.dropShadow

@Composable
fun DeliveryCard(modifier: Modifier = Modifier) {

    val deliveryCardShape = RoundedCornerShape(30.dp)

    Row(
        modifier = modifier
            .widthIn(max = 200.dp)
            .height(40.dp)
            .dropShadow(deliveryCardShape, Color.Black.copy(0.28f), 2.dp, 1.dp)
            .background(MaterialTheme.colorScheme.background, deliveryCardShape)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_truck),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            modifier = Modifier.padding(start = 10.dp),
            text = stringResource(id = R.string.delivery),
            color = MaterialTheme.colorScheme.primary,
            style = ExtendedTheme.typography.buttonSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}