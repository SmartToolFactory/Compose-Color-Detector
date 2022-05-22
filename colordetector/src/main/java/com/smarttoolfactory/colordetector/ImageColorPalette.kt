package com.smarttoolfactory.colordetector

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.smarttoolfactory.extendedcolors.parser.ColorNameParser
import com.smarttoolfactory.extendedcolors.parser.rememberColorParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ImageColorPalette(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap,
    selectedIndex: Int,
    colorNameParser: ColorNameParser = rememberColorParser(),
    maximumColorCount: Int = 16,
    onColorChange: (ColorData) -> Unit
) {
    var colorList by remember {
        mutableStateOf(listOf<ColorData>())
    }

    var colorProfileMap by remember {
        mutableStateOf(LinkedHashMap<String, ColorData>())
    }

    LaunchedEffect(key1 = imageBitmap) {
        snapshotFlow {
            imageBitmap
        }.map {
            val palette = Palette
                .from(imageBitmap.asAndroidBitmap())
                .maximumColorCount(maximumColorCount)
                .generate()

            val list = mutableListOf<ColorData>()

            val colorMap = linkedMapOf<String, ColorData>()

            palette.lightVibrantSwatch?.rgb.let {
                if (it != null) {
                    val color = Color(it)
                    val name = colorNameParser.parseColorName(color)
                    val colorData = ColorData(color, name)
                    colorMap["Light Vibrant"] = colorData
                }
            }

            palette.vibrantSwatch?.rgb.let {
                if (it != null) {
                    val color = Color(it)
                    val name = colorNameParser.parseColorName(color)
                    val colorData = ColorData(color, name)
                    colorMap["Vibrant"] = colorData
                }
            }

            palette.darkVibrantSwatch?.rgb.let {
                if (it != null) {
                    val color = Color(it)
                    val name = colorNameParser.parseColorName(color)
                    val colorData = ColorData(color, name)
                    colorMap["Light Vibrant"] = colorData
                }
            }

            palette.lightMutedSwatch?.rgb.let {
                if (it != null) {
                    val color = Color(it)
                    val name = colorNameParser.parseColorName(color)
                    val colorData = ColorData(color, name)
                    colorMap["Light Muted"] = colorData
                }
            }

            palette.mutedSwatch?.rgb.let {
                if (it != null) {
                    val color = Color(it)
                    val name = colorNameParser.parseColorName(color)
                    val colorData = ColorData(color, name)
                    colorMap["Muted"] = colorData
                }
            }

            palette.darkMutedSwatch?.rgb.let {
                if (it != null) {
                    val color = Color(it)
                    val name = colorNameParser.parseColorName(color)
                    val colorData = ColorData(color, name)
                    colorMap["Dark Muted"] = colorData
                }
            }

            colorProfileMap = colorMap

            palette.swatches.forEach { swatch: Palette.Swatch? ->
                swatch?.let {
                    val color = Color(it.rgb)
                    val name = colorNameParser.parseColorName(color)
                    val colorData = ColorData(color, name)
                    list.add(colorData)
                }
            }

            list.toList()
        }
            .flowOn(Dispatchers.Default)
            .onEach {
                colorList = it
            }
            .launchIn(this)
    }

    ColorProfileList(
        modifier = modifier,
        index = selectedIndex,
        colorProfileMap = colorProfileMap,
        colorList = colorList,
        onColorChange = onColorChange
    )
}

@Composable
private fun ColorProfileList(
    modifier: Modifier,
    index: Int = 0,
    colorProfileMap: Map<String, ColorData>,
    colorList: List<ColorData>,
    onColorChange: (ColorData) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (index == 0) {
            colorProfileMap.forEach {
                val profile = it.key
                val colorData = ColorData(it.value.color, it.value.name)

                item {
                    Column {
                        ColorItemRow(
                            modifier = Modifier
                                .shadow(1.dp, RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colors.background)
                                .fillMaxWidth(),
                            profile = "($profile) ",
                            colorData = colorData,
                            onClick = onColorChange
                        )
                    }
                }
            }
        } else {
            items(colorList) { colorData: ColorData ->
                ColorItemRow(
                    modifier = Modifier
                        .shadow(1.dp, RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colors.background)
                        .fillMaxWidth(),
                    colorData = colorData,
                    onClick = onColorChange
                )
            }
        }
    }
}

