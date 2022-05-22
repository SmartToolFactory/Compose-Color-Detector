package com.smarttoolfactory.composecolordetector

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ColorProfileTab(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onTabChange: (Int) -> Unit
) {

    val list = listOf("Primary", "Available")

    TabRow(selectedTabIndex = selectedIndex,
//        containerColor = MaterialTheme.colorScheme.primary,
        modifier = modifier.clip(RoundedCornerShape(26.dp)),
        divider = {},
        indicator = { tabPositions: List<TabPosition> ->
//            TabRowDefaults.Indicator(
//                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
//                height = 0.dp
//            )
        }
    ) {
        list.forEachIndexed { index, text ->
            val selected = selectedIndex == index
            Tab(
                modifier = if (selected) Modifier
                    .clip(RoundedCornerShape(26.dp))
                    .background(MaterialTheme.colorScheme.primary)
                else Modifier
                    .clip(RoundedCornerShape(26.dp))
//                    .background(MaterialTheme.colorScheme.primary)
                ,
                selected = selected,
                onClick = {
                    onTabChange(index)
                },
                text = {
                    Text(
                        text = text,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }
}
