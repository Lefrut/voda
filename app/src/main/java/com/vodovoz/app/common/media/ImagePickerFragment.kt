package com.vodovoz.app.common.media

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import coil3.compose.rememberAsyncImagePainter
import com.vodovoz.app.common.media.composables.PickerBottomPanel
import com.vodovoz.app.common.media.model.ImagePickerEvent
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.ui.insets.InsetsVisibilityState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ImagePickerFragment : Fragment() {

    @Inject
    internal lateinit var tabManager: TabManager

    @Inject
    internal lateinit var insetsVisibilityState: InsetsVisibilityState

    private val viewModel: ImagePickerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {

            setContent {
                VodovozTheme {
                    val viewState by viewModel.state.collectAsStateWithLifecycle()

                    val density = LocalDensity.current
                    var scale by rememberSaveable { mutableFloatStateOf(1f) }
                    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
                    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
                    var imageContainerSize by remember { mutableStateOf(IntSize.Zero) }
                    var lensSize by remember { mutableStateOf(0.dp) }
                    val lensSizePx by rememberUpdatedState(with(density) { lensSize.toPx() })
                    val asyncImagePainter = rememberAsyncImagePainter(viewState.imageUri)


                    val contentColor = Color.White
                    val containerColor = Color.Black

                    val pickImagesLauncher =
                        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                            if (uri != null) {
                                viewModel.setImageUri(uri.toString())
                            } else {
                                findNavController().popBackStack()
                            }
                        }

                    LaunchedEffect(Unit) {
                        pickImagesLauncher.launch(arrayOf("image/*"))
                    }

                    Column(
                        modifier = Modifier
                            .background(containerColor)
                            .windowInsetsPadding(WindowInsets.systemBars)
                    ) {

                        BoxWithConstraints(
                            modifier = Modifier
                                .weight(1f)
                                .pointerInput(lensSize) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 5f)

                                        val imageW = imageContainerSize.width.toFloat()
                                        val imageH = imageContainerSize.height.toFloat()

                                        val halfLens = lensSizePx / 2f

                                        val halfImageW = imageW * scale / 2f
                                        val halfImageH = imageH * scale / 2f

                                        val maxOffsetX =
                                            (halfImageW - halfLens).coerceAtLeast(0f)
                                        val maxOffsetY =
                                            (halfImageH - halfLens).coerceAtLeast(0f)

                                        offsetX =
                                            (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                        offsetY =
                                            (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {


                            lensSize = with(density) {
                                min(
                                    asyncImagePainter.intrinsicSize.height.toDp(),
                                    (constraints.maxWidth * 0.9f).toDp()
                                )
                            }

                            Image(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.65f)
                                    .onSizeChanged { size ->
                                        imageContainerSize = size
                                    }
                                    .clip(RectangleShape)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offsetX
                                        translationY = offsetY
                                    },
                                painter = asyncImagePainter,
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth
                            )

                            Box(
                                modifier = Modifier
                                    .size(lensSize)
                                    .border(2.dp, contentColor)
                                    .drawGrid(contentColor)
                            )
                        }


                        PickerBottomPanel(
                            modifier = Modifier.padding(
                                start = 32.dp, end = 32.dp,
                                top = 16.dp, bottom = 32.dp
                            ),
                            onBackClick = { viewModel.navigateBack() },
                            onSaveClick = {
                                viewModel.getBitmapFromImageUri()
                            }
                        )
                    }

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                ImagePickerEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is ImagePickerEvent.GetBitmap -> {
                                    val bitmap = MediaStore.Images.Media.getBitmap(
                                        context.contentResolver,
                                        Uri.parse(event.imageUri)
                                    )
                                    viewModel.saveBitmapToFile(
                                        sourceBitmap = bitmap,
                                        lensSize = lensSizePx,
                                        containerWidth = imageContainerSize.width.toFloat(),
                                        containerHeight = imageContainerSize.height.toFloat(),
                                        scale = scale,
                                        offsetX = offsetX,
                                        offsetY = offsetY
                                    )
                                }

                                is ImagePickerEvent.SaveInFile -> {
                                    createAvatarFile(event.bitmap).onSuccess { file ->
                                        viewModel.saveImageFile(file)
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private suspend fun createAvatarFile(bitmap: Bitmap) = withContext(Dispatchers.IO) {
        runCatching {
            val file = File.createTempFile("_avatar_", ".jpg", context?.cacheDir)
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            file
        }
    }

    private fun Modifier.drawGrid(
        color: Color = Color.White,
        countColumns: Int = 3,
        countRows: Int = 3,
    ) =
        drawBehind {
            val cellWidth = size.width / countColumns
            val cellHeight = size.height / countRows

            for (i in 1 until countColumns) {
                drawLine(
                    color = color,
                    start = Offset(x = cellWidth * i, y = 0f),
                    end = Offset(
                        x = cellWidth * i,
                        y = size.height
                    ),
                    strokeWidth = 1.dp.toPx()
                )
            }

            for (i in 1 until countRows) {
                drawLine(
                    color = color,
                    start = Offset(x = 0f, y = cellHeight * i),
                    end = Offset(
                        x = size.width,
                        y = cellHeight * i
                    ),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
}
