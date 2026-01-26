package com.m.vodovoz.ui.compose.player

import androidx.compose.runtime.Composable
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.TextureView
import android.widget.FrameLayout
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.ui.PlayerView

/**
 * "Один remember" для кадра из mp4 через ExoPlayer.
 * Возвращает State<ImageBitmap?> и сам внутри держит невидимый PlayerView (для PixelCopy).
 *
 * Видео НЕ проигрывается и БЕЗ звука (audio track disabled + volume=0).
 *
 * Требование: API 24+ (PixelCopy).
 */
@Composable
fun rememberExoVideoFrame(
    url: String,
    frameMs: Long,
    width: Dp,
): State<Bitmap?> {
    val context = LocalContext.current
    val density = LocalDensity.current

    // результат кадра
    val outState = remember(url, frameMs, width) { mutableStateOf<Bitmap?>(null) }

    // невидимый PlayerView референс
    var playerView by remember { mutableStateOf<PlayerView?>(null) }

    // ExoPlayer (не играет, без звука)
    val player = remember(url) {
        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(
                buildUponParameters()
                    .setTrackTypeDisabled(C.TRACK_TYPE_AUDIO, true) // ✅ вообще выключили аудио
            )
        }
        ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .build()
            .apply {
                playWhenReady = false
                repeatMode = Player.REPEAT_MODE_OFF
                volume = 0f // ✅ на всякий случай
                setSeekParameters(SeekParameters.CLOSEST_SYNC)
            }
    }

    // ВАЖНО: PlayerView должен быть в иерархии окна для PixelCopy — держим его скрытым
    AndroidView(
        modifier = Modifier
            .size(1.dp)
            .alpha(0f),
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                layoutParams = FrameLayout.LayoutParams(2, 2)
                this.player = player
                playerView = this
            }
        },
        update = { pv ->
            if (pv.player !== player) pv.player = player
            playerView = pv
        }
    )

    LaunchedEffect(url, frameMs, width, playerView) {
        runCatching {
            val pv = playerView ?: return@LaunchedEffect

            // prepare (не play!)
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()

            // ждём READY + videoSize
            val vs = awaitReadyAndVideoSize(player)

            // целевой размер bitmap'а (под аспект)
            val wPx = with(density) { width.roundToPx() }.coerceAtLeast(2)
            val hPx = ((wPx.toFloat() * vs.height) / vs.width).toInt().coerceAtLeast(2)

            // задаём PlayerView такой же размер, чтобы PixelCopy снял нормальный кадр
            pv.layoutParams = FrameLayout.LayoutParams(wPx, hPx)
            pv.requestLayout()

            // seek к кадру и ждём, пока он реально отрендерится
            player.seekTo(frameMs)
            delay(120)

            val bmp: Bitmap? = when (val v = pv.videoSurfaceView) {
                is SurfaceView -> {
                    val out = Bitmap.createBitmap(wPx, hPx, Bitmap.Config.ARGB_8888)
                    pixelCopy(v, out) // suspend, дождёмся результата
                    out
                }

                is TextureView -> {
                    // TextureView может вернуть bitmap нужного размера, но иногда null пока не отрендерилось
                    v.bitmap
                }

                else -> null
            }

            outState.value = bmp
        }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    return outState
}

private suspend fun awaitReadyAndVideoSize(player: ExoPlayer): VideoSize =
    suspendCancellableCoroutine { cont ->
        val l = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                val vs = player.videoSize
                if (state == Player.STATE_READY && vs.width > 0 && vs.height > 0) {
                    player.removeListener(this)
                    cont.resume(vs)
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                player.removeListener(this)
                cont.resumeWithException(error)
            }
        }
        player.addListener(l)
        cont.invokeOnCancellation { player.removeListener(l) }
    }

private suspend fun pixelCopy(surfaceView: SurfaceView, outBitmap: Bitmap) =
    suspendCancellableCoroutine { cont ->
        PixelCopy.request(
            surfaceView,
            outBitmap,
            { result ->
                if (result == PixelCopy.SUCCESS) cont.resume(Unit)
                else cont.resumeWithException(IllegalStateException("PixelCopy failed: $result"))
            },
            Handler(Looper.getMainLooper())
        )
    }
