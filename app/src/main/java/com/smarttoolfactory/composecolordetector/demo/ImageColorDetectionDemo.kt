@file:OptIn(ExperimentalMaterialApi::class)

package com.smarttoolfactory.composecolordetector.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.ColorData
import com.smarttoolfactory.colordetector.ColorDisplayWithClipboard
import com.smarttoolfactory.colordetector.ImageColorDetector
import com.smarttoolfactory.colordetector.ImageColorPalette
import com.smarttoolfactory.composecolordetector.ColorProfileTab
import com.smarttoolfactory.composecolordetector.ContentScaleSelectionMenu
import com.smarttoolfactory.composecolordetector.ImageSelectionButton
import com.smarttoolfactory.composecolordetector.R
import com.smarttoolfactory.extendedcolors.parser.ColorNameParser
import com.smarttoolfactory.extendedcolors.parser.rememberColorParser

@Composable
fun ImageColorDetectionDemo() {

    val imageBitmapLarge = ImageBitmap.imageResource(
        LocalContext.current.resources,
        R.drawable.landscape1
    )

    var imageBitmap by remember { mutableStateOf(imageBitmapLarge) }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(BottomSheetValue.Collapsed)
    )

    var index by remember { mutableStateOf(0) }
    val colorNameParser = rememberColorParser()

    var currentColor by remember { mutableStateOf(Color.Unspecified) }
    var colorName by remember { mutableStateOf("") }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetElevation = 8.dp,
        sheetShape = RoundedCornerShape(
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
            topStart = 16.dp,
            topEnd = 16.dp
        ),
        floatingActionButton = {
            ImageSelectionButton(
                onImageSelected = { bitmap: ImageBitmap ->
                    imageBitmap = bitmap
                }
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        sheetGesturesEnabled = true,
        sheetContent = {
            SheetContent(
                index,
                imageBitmap,
                colorNameParser,
                onIndexChange = {
                    index = it
                },
                onColorChange = { colorData: ColorData ->
                    currentColor = colorData.color
                    colorName = colorData.name
                }
            )
        },
        drawerElevation = 16.dp,
        drawerGesturesEnabled = true,
        // This is the height in collapsed state
        sheetPeekHeight = 70.dp
    ) {
        MainContent(
            colorNameParser,
            imageBitmap,
            currentColor,
            colorName
        ) { colorData: ColorData ->
            currentColor = colorData.color
            colorName = colorData.name
        }
    }

}

@Composable
private fun SheetContent(
    index: Int,
    imageBitmap: ImageBitmap,
    colorNameParser: ColorNameParser,
    onIndexChange: (Int) -> Unit,
    onColorChange: (ColorData) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 340.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ColorProfileTab(
            modifier = Modifier.width(300.dp),
            selectedIndex = index,
            onTabChange = onIndexChange
        )
        Spacer(modifier = Modifier.height(10.dp))
        ImageColorPalette(
            modifier = Modifier
                .fillMaxWidth(),
            imageBitmap = imageBitmap,
            colorNameParser = colorNameParser,
            selectedIndex = index,
            onColorChange = onColorChange
        )
    }
}

@Composable
private fun MainContent(
    colorNameParser: ColorNameParser,
    imageBitmap: ImageBitmap,
    currentColor: Color,
    colorName: String,
    onColorChange: (ColorData) -> Unit
) {

    val modifier = Modifier
        .background(Color.LightGray)
        .fillMaxWidth()
        .aspectRatio(4 / 3f)

    var contentScale by remember { mutableStateOf(ContentScale.Fit) }

    Column(modifier = Modifier.fillMaxSize()) {

        ContentScaleSelectionMenu(contentScale) {
            contentScale = it
        }
        ImageColorDetector(
            modifier = modifier,
            contentScale = contentScale,
            colorNameParser = colorNameParser,
            imageBitmap = imageBitmap,
            thumbnailSize = 70.dp,
            onColorChange = onColorChange
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (currentColor != Color.Unspecified) {
            ColorDisplayWithClipboard(
                colorData = ColorData(
                    color = currentColor,
                    name = colorName
                )
            )
        }
    }
}