package com.m.vodovoz.ui.compose.player

import android.annotation.SuppressLint
import android.media.AudioAttributes.CONTENT_TYPE_MOVIE
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.m.vodovoz.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.USAGE_MEDIA
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util.getStringForTime
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.buttons.PlayPauseButton
import androidx.media3.ui.compose.indicators.TimeText

import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.max

@SuppressLint("UnusedBoxWithConstraintsScope")
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaComposePlayer(
    url: String,
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit
) {
    val context = LocalContext.current

    // Создаём плеер один раз на url (и корректно освобождаем)
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            // Более “правильное” поведение звука/наушников
            setHandleAudioBecomingNoisy(true)
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(CONTENT_TYPE_MOVIE)
                    .setUsage(USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus= */ true
            )
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = true
        }
    }
    DisposableEffect(player) { onDispose { player.release() } }

    // Устанавливаем медиа при изменении url
    LaunchedEffect(url) {
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        // playWhenReady уже true
    }

    // --- duration/position
    var durationMs by remember { mutableLongStateOf(0L) }
    var positionMs by remember { mutableLongStateOf(0L) }

    // --- aspect ratio: по умолчанию 16:9, затем обновим из VideoSize
    var videoAspectRatio by remember { mutableFloatStateOf(16f / 9f) }

    // --- scrubbing
    var isScrubbing by remember { mutableStateOf(false) }
    var scrubFraction by remember { mutableFloatStateOf(0f) }

    fun safeDuration(p: Player): Long = (p.duration).takeIf { it > 0L } ?: 0L

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onVideoSizeChanged(videoSize: VideoSize) {

                val w = max(1, videoSize.width)
                val h = max(1, videoSize.height)
                val ratio = (w.toFloat() * videoSize.pixelWidthHeightRatio) / h.toFloat()
                if (ratio.isFinite() && ratio > 0f) {
                    videoAspectRatio = ratio
                }
            }

            override fun onEvents(p: Player, events: Player.Events) {
                durationMs = safeDuration(p)
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(player) {
        while (isActive) {
            if (!isScrubbing) {
                positionMs = player.currentPosition.coerceAtLeast(0L)
                durationMs = safeDuration(player)
            }
            delay(250)
        }
    }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                @Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val isPortraitVideo = videoAspectRatio < 1f

                    val videoModifier = if (isPortraitVideo) {

                        Modifier
                            .fillMaxHeight(1f)
                            .aspectRatio(videoAspectRatio)
                    } else {

                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(videoAspectRatio)
                    }

                    Box(videoModifier) {
                        TappablePlayerSurface(
                            player = player,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            // Контролы снизу
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TimeText(player) {
                        Text(
                            text = getStringForTime(this.currentPositionMs),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodySmall
                        )

                    }

                    TimeText(player) {
                        Text(
                            text = getStringForTime(this.durationMs),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                }

                val safeDur = durationMs.coerceAtLeast(1L)
                val fraction =
                    if (isScrubbing) scrubFraction
                    else (positionMs.toFloat() / safeDur.toFloat()).coerceIn(0f, 1f)

                val colors = SliderDefaults.colors(
                    thumbColor = Color.Transparent,
                    activeTrackColor = MaterialTheme.colorScheme.onBackground,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent,
                )

                Slider(
                    value = fraction,
                    onValueChange = { v ->
                        isScrubbing = true
                        scrubFraction = v
                    },
                    onValueChangeFinished = {
                        player.seekTo((scrubFraction * safeDur).toLong())
                        isScrubbing = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(48.dp),
                    thumb = {

                    },
                    colors = colors,
                    track = { sliderState ->
                        SliderDefaults.Track(
                            modifier = Modifier.requiredHeight(12.dp),
                            colors = colors,
                            enabled = true,
                            sliderState = sliderState,
                            drawStopIndicator = {

                            },
                            drawTick = { _, _ ->

                            },
                            thumbTrackGapSize = 0.dp
                        )
                    }
                )

            }
        }


        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_close),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    end = 16.dp,
                    top = 32.dp
                )
                .clip(CircleShape)
                .clickable {
                    onCloseClick()
                }
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp)
                .size(32.dp)
                .zIndex(Float.MAX_VALUE),
            tint = MaterialTheme.colorScheme.onBackground
        )


    }
}

@Composable
private fun TappablePlayerSurface(
    player: Player,
    modifier: Modifier = Modifier,
) {
    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var playbackState by remember { mutableIntStateOf(player.playbackState) }

    // ВАЖНО: это главный флаг
    var userPaused by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }

            override fun onPlaybackStateChanged(state: Int) {
                playbackState = state
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    fun toggleByUser() {
        if (player.isPlaying) {
            userPaused = true
            player.pause()
        } else {
            userPaused = false
            if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
            player.play()
        }
    }

    val isBuffering = playbackState == Player.STATE_BUFFERING
    val isEnded = playbackState == Player.STATE_ENDED

    // Показываем спиннер только если буферимся и пользователь НЕ ставил паузу
    val showSpinner = isBuffering && !userPaused

    // Показываем play-иконку только если пауза от пользователя ИЛИ видео закончилось
    val showPlayOverlay = (userPaused && !isPlaying) || isEnded

    Box(
        modifier = modifier.clickable { toggleByUser() },
        contentAlignment = Alignment.Center
    ) {
        PlayerSurface(
            player = player,
            modifier = Modifier.fillMaxSize()
        )


        if (showSpinner) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onBackground,
                trackColor = Color.Transparent,
                modifier = Modifier.size(36.dp),
                strokeWidth = 4.dp
            )
        }

        if (showPlayOverlay) {
            Image(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable { toggleByUser() },
                painter = painterResource(id = R.drawable.svg_play_video),
                contentDescription = null
            )
        }
    }
}
