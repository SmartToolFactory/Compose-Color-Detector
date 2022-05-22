package com.smarttoolfactory.colordetector

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.ScreenRefreshPolicy.*
import com.smarttoolfactory.colordetector.util.calculateColorInPixel
import com.smarttoolfactory.extendedcolors.parser.rememberColorParser
import com.smarttoolfactory.imagecropper.ImageWithThumbnail
import com.smarttoolfactory.screenshot.ImageResult
import com.smarttoolfactory.screenshot.ScreenshotBox
import com.smarttoolfactory.screenshot.rememberScreenshotState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

/**
 * A Composable that detect color at pixel that user touches when [enabled].
 * @param enabled when enabled detect color at user's point of touch
 * @param thumbnailSize size of the thumbnail that displays touch position with zoom
 * @param content is screen/Composable is displayed to user to get color from. [ScreenshotBox]
 * gets [Bitmap] from screen when users first down and stores it.
 * @param onColorChange callback to notify that user moved and picked a color
 */
@Composable
fun ScreenColorDetector(
    modifier: Modifier = Modifier,
    enabled: Boolean = false,
    thumbnailSize: Dp = 80.dp,
    screenRefreshPolicy: ScreenRefreshPolicy = ScreenRefreshPolicy.OnEnable,
    content: @Composable () -> Unit,
    onColorChange: (ColorData) -> Unit
) {
    var offset by remember {
        mutableStateOf(Offset.Unspecified)
    }

    var center by remember {
        mutableStateOf(Offset.Unspecified)
    }

    var labelSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    val screenshotState = rememberScreenshotState()

    LaunchedEffect(key1 = enabled) {
        if (enabled) {
            screenshotState.capture()
        } else {
            screenshotState.imageState.value = ImageResult.Initial
            offset = Offset.Unspecified
            center = Offset.Unspecified
        }
    }

    val colorNameParser = rememberColorParser()
    var color by remember { mutableStateOf(Color.Unspecified) }
    var colorName by remember { mutableStateOf("") }

    LaunchedEffect(key1 = colorNameParser) {

        snapshotFlow { color }
            .distinctUntilChanged()
            .mapLatest { color: Color ->
                colorNameParser.parseColorName(color)
            }
            .flowOn(Dispatchers.Default)
            .collect { name: String ->
                colorName = name
                onColorChange(ColorData(color, name))
            }
    }

    Box {

        ScreenshotBox(
            modifier = modifier,
            screenshotState = screenshotState
        ) {
            content()
        }

        if (enabled) {

            val imageResult = screenshotState.imageState.value
            if (imageResult is ImageResult.Success) {

                val bitmap = imageResult.data
                val density = LocalDensity.current.density
                val imageWidth = (bitmap.width / density).dp
                val imageHeight = (bitmap.height / density).dp

                ScreenColorDetectorImpl(
                    modifier = Modifier.size(imageWidth, imageHeight),
                    enabled = enabled,
                    bitmap = bitmap,
                    thumbnailSize = thumbnailSize,
                    colorData = ColorData(color, colorName),
                    center = center,
                    offset = offset,
                    onColorChange = {
                        color = it
                    },
                    onTouchEvent = {
                        offset = it
                    },
                    onThumbnailCenterChange = {
                        center = it
                    }
                )

                if (offset.isSpecified && offset.isFinite) {

                    val labelOffsetX = if (offset.x < bitmap.width - labelSize.width) {
                        offset.x + 16.dp.value * density
                    } else {
                        offset.x - labelSize.width - 16.dp.value * density
                    }.toInt()

                    val labelOffsetY = (offset.y - labelSize.height / 2).toInt()

                    ColorDisplayWithClipboard(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    labelOffsetX,
                                    labelOffsetY
                                )
                            }
                            .onSizeChanged {
                                labelSize = it
                            },
                        ColorData(color, colorName)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScreenColorDetectorImpl(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    bitmap: Bitmap,
    thumbnailSize: Dp,
    colorData: ColorData,
    offset: Offset,
    center: Offset,
    onColorChange: (Color) -> Unit,
    onTouchEvent: (Offset) -> Unit,
    onThumbnailCenterChange: (Offset) -> Unit
) {
    val density = LocalDensity.current.density
    val imageBitmap by remember {
        mutableStateOf(bitmap.asImageBitmap())
    }
    ImageWithThumbnail(
        imageBitmap = imageBitmap,
        modifier = modifier,
        contentDescription = "Image Color Detector",
        contentScale = ContentScale.FillBounds,
        thumbnailSize = thumbnailSize,
        onThumbnailCenterChange = {
            onThumbnailCenterChange(it)
        },
        onDown = {
            onTouchEvent(it)
        },
        onMove = {
            onTouchEvent(it)
        }
    ) {

        if (enabled && offset.isSpecified && offset.isFinite) {

            onColorChange(
                calculateColorInPixel(
                    offsetX = offset.x,
                    offsetY = offset.y,
                    startImageX = 0f,
                    startImageY = 0f,
                    rect = rect,
                    width = imageWidth.value * density,
                    height = imageHeight.value * density,
                    bitmap = bitmap
                )
            )

            ColorSelectionDrawing(
                modifier = Modifier.size(imageWidth, imageHeight),
                offset = offset,
                thumbnailCenter = center,
                color = colorData.color
            )
        }
    }
}

/**
 * Enum class fo screen refresh policy
 * * When [OnEnable] is selected screenshot is taken on each on [ScreenColorDetector] is enabled
 * after specified delay
 * * When [OnDown] is selected screenshot is taken if
 * [ScreenColorDetector] is disabled after specified delay
 * * When [OnUp] is selected screenshot is taken after last pointer on screen is up if
 * [ScreenColorDetector] is disabled after specified delay
 */
enum class ScreenRefreshPolicy {
    OnEnable, OnDown, OnUp
}