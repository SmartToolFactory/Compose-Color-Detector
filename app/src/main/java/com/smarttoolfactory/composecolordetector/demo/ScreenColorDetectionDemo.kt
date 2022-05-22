@file:OptIn(ExperimentalMaterial3Api::class)

package com.smarttoolfactory.composecolordetector.demo

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.ColorData
import com.smarttoolfactory.colordetector.ScreenColorDetector

@SuppressLint("UnsafeOptInUsageError")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenColorDetectionDemo() {

    var enabled by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { enabled = !enabled }) {
                Text(text = if (enabled) "Disable" else "Enable")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        content = { paddingValues: PaddingValues ->
            ScreenColorDetection(paddingValues, enabled)
        }
    )

}

@Composable
private fun ScreenColorDetection(paddingValues: PaddingValues, enabled: Boolean) {

    var currentColor by remember { mutableStateOf(Color.Unspecified) }
    var colorName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {

        ScreenColorDetector(
            enabled = enabled,
            content = {
                Column(

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(modifier = Modifier.height(10.dp))
                    var value by remember { mutableStateOf(0f) }

                    Slider(value = value, onValueChange = {
                        value = it
                    })

                    HorizontalChipList(modifier = Modifier.fillMaxWidth())

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ElevatedButton(onClick = { /*TODO*/ }) {
                            Text(text = "ElevatedButton")
                        }

                        FilledTonalButton(onClick = { /*TODO*/ }) {
                            Text("FilledTonalButton")
                        }
                    }

                    LazyVerticalGrid(
                        contentPadding = PaddingValues(12.dp),
                        modifier = Modifier.fillMaxSize(),
                        columns = GridCells.Fixed(3),
                        content = {
                            items(snacks) { snack: Snack ->
                                GridSnackCard(snack = snack)
                            }
                        }
                    )
                }
            }
        ) { colorData: ColorData ->
            currentColor = colorData.color
            colorName = colorData.name
        }

    }
}

@Composable
private fun HorizontalChipList(modifier: Modifier = Modifier) {

    val fruits =
        listOf("Apple", "Orange", "Strawberry", "Pineapple", "Pear", "Banana", "Kiwi", "Cherry")
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(items = fruits) { fruit: String ->
            var selected by remember { mutableStateOf(true) }
            FilterChip(
                selected = selected,
                onClick = { selected = !selected },
                label = {
                    Text(text = fruit, modifier = Modifier.padding(4.dp))
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null)

                }
            )
        }
    }
}