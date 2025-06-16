package com.vodovoz.app.feature.map.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.top_bar.BasicSearchField

@Composable
fun MapTopBar(
    modifier: Modifier = Modifier,
    query: String,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onFieldClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.icon_back),
            tint = MaterialTheme.colorScheme.onBackground,
            contentDescription = null,
            modifier = Modifier.size(24.dp).clip(CircleShape).clickable {
                onBackClick()
            }
        )
        BasicSearchField(
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth()
                .pointerInput(Unit){
                    awaitEachGesture {
                        awaitPointerEvent(PointerEventPass.Initial)
                        onFieldClick()
                    }
                },
            value = query,
            onValueChange = onQueryChange,
            onSearchClick = onSearchClick,
        ) { innerTextField ->
            Row(
                modifier = Modifier
                    .height(46.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surface),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.icon_search),
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onSearchClick)
                        .padding(8.dp)
                        .size(24.dp),
                    tint = MaterialTheme.colorScheme.surfaceTint
                )

                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .weight(1f)
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.search_address),
                            color = MaterialTheme.colorScheme.surfaceTint,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        innerTextField()
                    }
                }
            }
        }
    }
}