package com.vodovoz.app.design_system.composables.snackbar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vodovoz.app.R
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.feature.home.composables.dropShadow
import kotlinx.coroutines.delay

@Composable
fun VodovozSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 16.dp,
        vertical = 8.dp
    ),
) {
    SnackbarHost(modifier = modifier, hostState = hostState) { data ->
        VodovozSnackbar(snackbarData = data, contentPadding = contentPadding)
    }
}



@Stable
class VodovozSnackBarVisuals(
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val message: String,
    override val withDismissAction: Boolean,
    val iconId: Int?,
) : SnackbarVisuals {

    companion object {

        fun create(
            message: String,
            @DrawableRes
            iconId: Int? = R.drawable.ic_success,
            actionLabel: String? = null,
            duration: SnackbarDuration = SnackbarDuration.Short,
            withDismissAction: Boolean = false,
        ): VodovozSnackBarVisuals {
            return VodovozSnackBarVisuals(actionLabel, duration, message, withDismissAction, iconId)
        }
    }
}

@Composable
fun VodovozSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
) {
    val iconId = (snackbarData.visuals as? VodovozSnackBarVisuals)?.iconId

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .dropShadow(shape = MaterialTheme.shapes.large, blur = 10.dp, offsetY = 4.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.large)
            .padding(contentPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            iconId?.let {
                Image(
                    imageVector = ImageVector.vectorResource(id = iconId),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(24.dp)
                )
            }

            Text(
                text = snackbarData.visuals.message,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview
@Composable
private fun VodovozSnackbarPreview() {
    val s = remember { SnackbarHostState() }
    VodovozTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter
        ) {
            VodovozSnackbarHost(
                hostState = s
            )
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            s.showSnackbar("Отключите VPN, если работа приложения замедляется")
        }
    }
}