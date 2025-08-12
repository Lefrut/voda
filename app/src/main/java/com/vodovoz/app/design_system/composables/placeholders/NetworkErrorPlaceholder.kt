package com.vodovoz.app.design_system.composables.placeholders

import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.navOptions
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vodovoz.app.R
import com.vodovoz.app.common.block_app_signal.BlockAppSignal
import com.vodovoz.app.common.cache.VodovozHttpError
import com.vodovoz.app.core.navigation.findRootNavController
import com.vodovoz.app.core.navigation.slideAnim
import com.vodovoz.app.core.network.serialization.fromJson
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.button.VodovozButton
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.ui.base.blockAppSignal
import com.vodovoz.app.ui.base.httpErrorCache
import com.vodovoz.app.util.extensions.isInternetAvailable


private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()


@Stable
sealed class PlaceholderType {

    data class Http(val error: VodovozHttpErrorUi) : PlaceholderType()
    data object NetworkError : PlaceholderType()
    data object Unknown : PlaceholderType()

}

@Immutable
data class VodovozHttpErrorUi(
    val title: String,
    val message: String,
)

fun VodovozHttpError.toUi(): VodovozHttpErrorUi {
    return VodovozHttpErrorUi(
        title ?: "", message ?: ""
    )
}

@Stable
sealed interface ErrorPlaceholderMode {

    data object Automatic : ErrorPlaceholderMode
    data class Fixed(val type: PlaceholderType) : ErrorPlaceholderMode


}

@Composable
private fun rememberAutoPlaceholderType(): PlaceholderType {
    val activity = LocalActivity.current
    val context = LocalContext.current
    return remember {
        val httpErrorCache = activity?.httpErrorCache
        val lastErrorData = httpErrorCache?.lastErrorData?.value ?: ""

        httpErrorCache?.setLastError(null)
        val error = runCatching {
            moshi.fromJson<VodovozHttpError>(lastErrorData)
        }.getOrNull()
        val errorUi = error?.toUi()

        return@remember when {
            context.isInternetAvailable() == false -> {
                PlaceholderType.NetworkError
            }

            errorUi != null -> {
                PlaceholderType.Http(errorUi)
            }

            else -> PlaceholderType.Unknown
        }
    }
}

private suspend fun BlockAppSignal.Type.handleAppSignal(
    onReload: suspend () -> Unit,
    onBlock: suspend () -> Unit,
    onNone: suspend () -> Unit = {},
) {
    when (this) {
        BlockAppSignal.Type.Reload -> {

        }

        BlockAppSignal.Type.Block -> {

        }

        BlockAppSignal.Type.None -> {}
    }

}

@Composable
fun NetworkErrorPlaceholder(
    modifier: Modifier = Modifier,
    mode: ErrorPlaceholderMode = ErrorPlaceholderMode.Automatic,
    onTryAgainClick: () -> Unit,
) {
    val activity = LocalActivity.current
    val view = LocalView.current

    val placeholderType = when (mode) {
        ErrorPlaceholderMode.Automatic -> {
            rememberAutoPlaceholderType()
        }

        is ErrorPlaceholderMode.Fixed -> {
            mode.type
        }
    }

    LifecycleEffect {
        val blockAppSignal = activity?.blockAppSignal ?: return@LifecycleEffect
        val rootNavController = view.findRootNavController()

        blockAppSignal.getSignalFlow().collect { signalType ->
            signalType.handleAppSignal(
                onReload = {
                    blockAppSignal.setSignal(BlockAppSignal.Type.None)
                    onTryAgainClick()
                },
                onBlock = {
                    rootNavController?.navigate(
                        R.id.blockAppFragment,
                        null,
                        navOptions { slideAnim() }
                    )
                }
            )
        }
    }

    when (placeholderType) {
        is PlaceholderType.Http -> {
            HttpError(
                modifier = modifier,
                httpError = placeholderType.error
            )
        }

        PlaceholderType.NetworkError -> {
            NetworkError(modifier = modifier) {
                onTryAgainClick()
            }
        }

        PlaceholderType.Unknown -> {
            BaseError(
                title = stringResource(R.string.unknown_error),
                message = "",
                imageId = R.drawable.pic_search
            )
        }
    }
}

@Composable
private fun HttpError(modifier: Modifier = Modifier, httpError: VodovozHttpErrorUi) {
    BaseError(
        modifier = modifier,
        title = httpError.title,
        message = httpError.message,
        imageId = R.drawable.pic_search
    )
}

@Composable
fun BaseError(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    @DrawableRes
    imageId: Int,
    onButtonClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.weight(1.6f))


        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Image(
                painter = painterResource(id = imageId),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )


            Text(
                modifier = Modifier.padding(top = 24.dp),
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            if (message.isNotBlank()) {
                Text(
                    modifier = Modifier.padding(top = 24.dp),
                    text = message,
                    color = MaterialTheme.colorScheme.surfaceTint,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }


        Spacer(modifier = Modifier.weight(1.9f))

        onButtonClick?.let {
            VodovozButton(
                text = stringResource(R.string.try_again),
                onClick = onButtonClick,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            )
        }
    }

}


@Composable
private fun NetworkError(modifier: Modifier = Modifier, onTryAgainClick: () -> Unit) {
    BaseError(
        modifier = modifier,
        title = stringResource(R.string.connection_error),
        message = stringResource(R.string.check_intenet_connection),
        imageId = R.drawable.pic_wifi_error,
        onButtonClick = onTryAgainClick
    )
}

@Preview
@Composable
private fun NetworkErrorPlaceholderPreview() {
    VodovozTheme {
        NetworkErrorPlaceholder { }
    }
}
