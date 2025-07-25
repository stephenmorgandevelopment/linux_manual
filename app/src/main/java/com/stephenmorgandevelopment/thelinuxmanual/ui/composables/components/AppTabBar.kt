@file:OptIn(ExperimentalMaterial3Api::class)

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.TabInfo
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors

@Composable
fun AppTabBar(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    tabs: List<TabInfo>,
    scrollState: ScrollState,
    onTabSelected: (MainScreenAction.TabSelected) -> Unit,
) {
    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        edgePadding = 0.dp,
        indicator = {
            TabRowDefaults.PrimaryIndicator(
                Modifier.tabIndicatorOffset(
                    selectedTabIndex = selectedIndex,
                    matchContentSize = false
                ),
                width = Dp.Unspecified,
                color = Colors.indicatorColor,
            )
        },
        scrollState = scrollState,
    ) {
        tabs.forEachIndexed { idx, pagerTab ->
            Tab(
                selected = idx == selectedIndex,
                onClick = { onTabSelected(MainScreenAction.TabSelected(idx)) },
                text = @Composable { Text(pagerTab.title) },
            )
        }
    }
}
