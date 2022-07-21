package com.smarttoolfactory.colordetector

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isFinite
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.util.calculateColorInPixel
import com.smarttoolfactory.extendedcolors.parser.ColorNameParser
import com.smarttoolfactory.extendedcolors.parser.rememberColorParser
import com.smarttoolfactory.image.ImageWithThumbnail
import com.smarttoolfactory.image.rememberThumbnailState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest


/**
 * Composable that detects color at touch position on [imageBitmap].
 * @param imageBitmap image is being drawn and colors to be detected
 * @param contentScale how image should be layout inside the Canvas that is drawn
 * @param alignment Optional alignment parameter used to place the ImageBitmap in the
 * given bounds defined by the width and height
 * @param thumbnailSize size of the thumbnail that displays touch position with zoom
 * @param thumbnailZoom zoom scale between 100% and 500%
 * @param onColorChange callback to notify that user moved and picked a color
 */
@Composable
fun ImageColorDetector(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap,
    contentScale: ContentScale = ContentScale.FillBounds,
    alignment: Alignment = Alignment.Center,
    colorNameParser: ColorNameParser = rememberColorParser(),
    thumbnailSize: Dp = 70.dp,
    @IntRange(from = 100, to = 500) thumbnailZoom: Int = 200,
    onColorChange: (ColorData) -> Unit
) {

    var offset by remember(imageBitmap, contentScale) {
        mutableStateOf(Offset.Unspecified)
    }

    var center by remember(imageBitmap, contentScale) {
        mutableStateOf(Offset.Unspecified)
    }

    var color by remember { mutableStateOf(Color.Unspecified) }

    LaunchedEffect(key1 = colorNameParser) {

        snapshotFlow { color }
            .distinctUntilChanged()
            .mapLatest { color: Color ->
                colorNameParser.parseColorName(color)
            }
            .flowOn(Dispatchers.Default)
            .collect { name: String ->
                onColorChange(ColorData(color, name))
            }
    }

    ImageWithThumbnail(
        imageBitmap = imageBitmap,
        modifier = modifier,
        contentDescription = "Image Color Detector",
        contentScale = contentScale,
        alignment = alignment,
        thumbnailState = rememberThumbnailState(
            size = DpSize(thumbnailSize, thumbnailSize),
            thumbnailZoom = thumbnailZoom,
        ),
        onThumbnailCenterChange = {
            center = it
        },
        onDown = {
            offset = it
        },
        onMove = {
            offset = it
        }
    ) {

        val density = LocalDensity.current.density

        if (offset.isSpecified && offset.isFinite) {
            color = calculateColorInPixel(
                offsetX = offset.x,
                offsetY = offset.y,
                startImageX = 0f,
                startImageY = 0f,
                rect = rect,
                width = imageWidth.value * density,
                height = imageHeight.value * density,
                bitmap = imageBitmap.asAndroidBitmap()
            )
        }

        ColorSelectionDrawing(
            modifier = Modifier.size(imageWidth, imageHeight),
            offset = offset,
            thumbnailCenter = center,
            color = color
        )
    }
}