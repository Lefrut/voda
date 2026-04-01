package com.m.vodovoz.design_system.composables.placeholders

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.m.vodovoz.R
import com.m.vodovoz.common.block_app_signal.BlockAppSignal
import com.m.vodovoz.common.cache.VodovozHttpError
import com.m.vodovoz.core.navigation.slideAnim
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.button.VodovozButtonsColumn
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.ui.base.blockAppSignal
import com.m.vodovoz.ui.base.httpErrorCache
import com.m.vodovoz.util.extensions.isInternetAvailable
import kotlinx.coroutines.flow.map
import kotlinx.parcelize.Parcelize

@Stable
sealed class PlaceholderType {

    data class Http(val error: VodovozHttpErrorUi) : PlaceholderType()
    data object NetworkError : PlaceholderType()

}

@Immutable
@Parcelize
data class VodovozHttpErrorUi(
    val title: String,
    val message: String,
    val button: ColorfulButtonUi? = null,
) : Parcelable {

    companion object {
        val Unspecified = VodovozHttpErrorUi("Unspecified", "")
        val Empty = VodovozHttpErrorUi("", "")

        val Saver: Saver<VodovozHttpErrorUi, List<String>> = Saver(
            save = { listOf(it.title, it.message) },
            restore = { restoredList ->
                restoredList.let { VodovozHttpErrorUi(it[0], it[1]) }
            }
        )
    }
}

fun VodovozHttpError.toUi(): VodovozHttpErrorUi {
    return VodovozHttpErrorUi(
        title = title ?: "",
        message = message ?: "",
    )
}

@Stable
sealed interface ErrorPlaceholderMode {

    data object Automatic : ErrorPlaceholderMode
    data class Fixed(val type: PlaceholderType) : ErrorPlaceholderMode

}


@Composable
private fun rememberHttpError(): VodovozHttpErrorUi {
    val activity = LocalActivity.current

    val httpErrorCache = remember { activity.httpErrorCache }
    val httpError by httpErrorCache.lastHttpError.map {
        (it as? VodovozHttpError)?.toUi()
    }.collectAsStateWithLifecycle(VodovozHttpErrorUi.Empty)

    return httpError ?: VodovozHttpErrorUi.Empty
}

@Composable
private fun rememberAutoPlaceholderType(): PlaceholderType {
    val httpError = rememberHttpError()
    val context = LocalContext.current

    return remember(httpError) {
        return@remember when {
            context.isInternetAvailable() == false -> PlaceholderType.NetworkError
            httpError != VodovozHttpErrorUi.Empty -> PlaceholderType.Http(httpError)
            else -> PlaceholderType.NetworkError
        }
    }
}

private suspend fun BlockAppSignal.Type.handleAppSignal(
    onReload: suspend () -> Unit,
    onBlock: suspend () -> Unit,
    onNone: suspend () -> Unit = {},
) {
    when (this) {
        BlockAppSignal.Type.Reload -> onReload()

        BlockAppSignal.Type.Block -> onBlock()

        BlockAppSignal.Type.None -> onNone()
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

        blockAppSignal.getSignalFlow().collect { signalType ->
            signalType.handleAppSignal(
                onReload = {
                    blockAppSignal.setSignal(BlockAppSignal.Type.None)
                    onTryAgainClick()
                },
                onBlock = {
                    activity.findNavController(view.id).navigate(
                        R.id.blockAppFragment,
                        Bundle.EMPTY,
                        navOptions { slideAnim() }
                    )

                }
            )
        }
    }

    val expandedState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    AnimatedVisibility(visibleState = expandedState, enter = fadeIn(), exit = fadeOut()) {
        when (placeholderType) {
            is PlaceholderType.Http -> {
                HttpError(
                    modifier = modifier,
                    httpError = placeholderType.error,
                    onButtonClick = onTryAgainClick
                )
            }

            PlaceholderType.NetworkError -> {
                NetworkError(
                    modifier = modifier,
                    onTryAgainClick = onTryAgainClick
                )
            }
        }
    }
}

@Composable
private fun HttpError(
    modifier: Modifier = Modifier,
    httpError: VodovozHttpErrorUi,
    onButtonClick: () -> Unit,
) {
    ErrorBasePlaceholder(
        modifier = modifier,
        title = httpError.title,
        message = httpError.message,
        imageId = R.drawable.pic_search,
        button = httpError.button,
        onButtonClick = onButtonClick
    )
}

@Composable
fun ErrorBasePlaceholder(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    @DrawableRes
    imageId: Int,
    button: ColorfulButtonUi?,
    onButtonClick: () -> Unit = {},
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

        val buttonModifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 24.dp)

        button?.let {
            VodovozButtonsColumn(
                modifier = buttonModifier,
                buttons = listOf(button)
            ) { onButtonClick() }
        } ?: VodovozButton(
            modifier = buttonModifier,
            text = stringResource(R.string.try_again),
            onClick = {
                onButtonClick()
            }
        )

    }

}


@Composable
private fun NetworkError(modifier: Modifier = Modifier, onTryAgainClick: () -> Unit) {
    ErrorBasePlaceholder(
        modifier = modifier,
        title = stringResource(R.string.connection_error),
        message = stringResource(R.string.check_intenet_connection),
        imageId = R.drawable.pic_wifi_error,
        button = ColorfulButtonUi(
            name = stringResource(R.string.try_again),
            backgroundColorValue = MaterialTheme.colorScheme.primary.value,
            textColorValue = MaterialTheme.colorScheme.background.value
        ),
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
