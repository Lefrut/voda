package com.m.vodovoz.feature.product_details.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.m.vodovoz.design_system.composables.bottom_sheet.VodovozDragHandle
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.chip.VodovozColorChip
import com.m.vodovoz.design_system.model.BlockPromoDataUi
import com.m.vodovoz.design_system.model.BuyButtonUi
import com.m.vodovoz.design_system.model.PromoProductUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresentBottomSheet(
    state: SheetState = rememberModalBottomSheetState(true),
    data: BlockPromoDataUi,
    button: BuyButtonUi?,
    onDismissRequest: () -> Unit,
    onBuyButtonClick: (BuyButtonUi) -> Unit,
) {


    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = onDismissRequest,
        dragHandle = {
            VodovozDragHandle()
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.padding(top = 20.dp),
                text = data.title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = data.description,
                color = MaterialTheme.colorScheme.surfaceTint,
                style = MaterialTheme.typography.bodyMedium
            )


            Column(modifier = Modifier.padding(vertical = 20.dp)) {
                data.products.forEachIndexed { index, product ->
                    PromoProductCard(product = product)
                    if (data.products.lastIndex != index) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }



            if (button != null) {
                VodovozButton(
                    modifier = Modifier,
                    text = button.title,
                    onClick = {
                        onBuyButtonClick(button)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = button.backgroundColor.takeOrElse { MaterialTheme.colorScheme.primary },
                        contentColor = button.textColor.takeOrElse { MaterialTheme.colorScheme.background }
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PromoProductCard(product: PromoProductUi) {
    Row(
        modifier = Modifier
            .height(78.dp)
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier.size(76.dp)) {
            AsyncImage(
                modifier = Modifier.matchParentSize(),
                model = product.image,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                alignment = Alignment.Center
            )
//            if (product.quantity != 0) {
//                VodovozColorChip(
//                    modifier = Modifier.align(Alignment.BottomEnd),
//                    color = MaterialTheme.colorScheme.secondary,
//                    text = product.quantity.toString()
//                )
//            }
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = product.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(
                modifier = Modifier
                    .wrapContentSize()
                    .height(4.dp)
            )
            Row(verticalAlignment = Alignment.Bottom) {

                Text(
                    text = product.price.new,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val oldPrice = product.price.old
                if (oldPrice.isNotEmpty()) {
                    Text(
                        modifier = Modifier.padding(start = 2.dp),
                        text = product.price.old,
                        color = MaterialTheme.colorScheme.surfaceTint,
                        style = MaterialTheme.typography.bodySmall.copy(
                            textDecoration = TextDecoration.LineThrough
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

}