package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.TabInfo
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors

@Composable
internal fun CompletePager(
    tabsOnBottom: Boolean,
    selectedTabIndex: Int,
    tabs: List<TabInfo>,
    paddingValues: PaddingValues,
    onAction: (MainScreenAction) -> Unit,
    tabContent: @Composable () -> Unit,
) {
    val tabScrollState: ScrollState = rememberScrollState()

    ConstraintLayout(
        modifier = Modifier
            .padding(paddingValues)
            .consumeWindowInsets(paddingValues)
            .fillMaxSize()
    ) {
        val (tabBar, pager) = createRefs()

        AppTabBar(
            modifier = Modifier
                .constrainAs(tabBar) {
                    start.linkTo(parent.start)
                    if (tabsOnBottom) bottom.linkTo(parent.bottom)
                    else top.linkTo(parent.top)
                }
                .padding(bottom = 3.dp)
                .wrapContentSize(),
            selectedIndex = selectedTabIndex,
            tabs = tabs,
            scrollState = tabScrollState,
            onTabSelected = onAction,
        )

        Box(
            modifier = Modifier
                .constrainAs(pager) {
                    top.linkTo(if (!tabsOnBottom) tabBar.bottom else parent.top)
                    bottom.linkTo(if (!tabsOnBottom) parent.bottom else tabBar.top)
                    height = Dimension.fillToConstraints
                    width = Dimension.matchParent
                }
                .background(color = Colors.transparent),
        ) {
            tabContent()
        }
    }
}