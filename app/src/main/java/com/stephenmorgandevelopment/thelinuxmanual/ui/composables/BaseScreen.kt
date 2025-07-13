@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.AvailableReleases.Companion.releaseStrings
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.OptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ScreenState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.TabInfo
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.AppTabBar
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.AppToolbar
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus.LookupOptionsMenu
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus.ManPageOptionsMenu
import com.stephenmorgandevelopment.thelinuxmanual.utils.MockObjects
import com.stephenmorgandevelopment.thelinuxmanual.utils.showSyncText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BaseScreen(
    content: @Composable () -> Unit,
    screenState: ScreenState,
    tabScrollState: ScrollState,
    onActivityAction: (MainScreenAction) -> Unit,
    onOptionsMenuAction: (OptionsMenuAction) -> Unit,
) {
    BackHandler(
        enabled = false
    ) {}

    BaseScreenContent(
        state = screenState,
        tabScrollState = tabScrollState,
        onActivityAction = onActivityAction,
        onOptionsMenuAction = onOptionsMenuAction,
        content = content,
    )
}

@Composable
private fun BaseScreenContent(
    state: ScreenState,
    tabScrollState: ScrollState,
    onActivityAction: (MainScreenAction) -> Unit,
    onOptionsMenuAction: (OptionsMenuAction) -> Unit,
    content: @Composable () -> Unit,
) {
    BackHandler(enabled = false) {}

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            with(state) {
                AppToolbar(state.toolbarTitle) {
                    if (selectedTabIndex == 0) {
                        LookupOptionsMenu(
                            releaseStrings,
                            tabsOnBottom,
                            searchOnBottom,
                        ) {
                            onOptionsMenuAction(it)
                        }
                    } else {
                        ManPageOptionsMenu(
                            tabs[selectedTabIndex].title,
                            tabs[selectedTabIndex].manPageSections,
                        ) { onOptionsMenuAction(it) }
                    }
                }
            }
        },
    ) { paddingValues ->
        val syncProgressScrollState = rememberScrollState()
        BackHandler(enabled = false) {}

        if (state.syncProgress != null) {
            Text(
                text = showSyncText(state.toolbarTitle, state.syncProgress),
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(syncProgressScrollState),
                style = TextStyle(fontSize = 18.sp),
            )
        } else {
            CompletePager(
                state = state,
                paddingValues = paddingValues,
                tabScrollState = tabScrollState,
                onAction = onActivityAction,
                content = content,
            )
        }
    }
}

@Composable
private fun CompletePager(
    state: ScreenState,
    paddingValues: PaddingValues,
    tabScrollState: ScrollState,
    onAction: (MainScreenAction) -> Unit,
    content: @Composable () -> Unit,
) {
    BackHandler(enabled = false) {}

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
                    if (state.tabsOnBottom) bottom.linkTo(parent.bottom)
                    else top.linkTo(parent.top)
                }
                .padding(bottom = 3.dp)
                .wrapContentSize(),
            selectedIndex = state.selectedTabIndex,
            tabs = state.tabs,
            scrollState = tabScrollState,
            onTabSelected = onAction,
        )

        Box(
            modifier = Modifier
                .constrainAs(pager) {
                    top.linkTo(if (!state.tabsOnBottom) tabBar.bottom else parent.top)
                    bottom.linkTo(if (!state.tabsOnBottom) parent.bottom else tabBar.top)
                    height = Dimension.fillToConstraints
                    width = Dimension.matchParent
                }
                .background(color = Colors.transparent),
        ) {
            content()
        }
    }
}


/**
 * Preview code
 */


private val previewState = ScreenState(
    toolbarTitle = "Test Preview Pages",
    selectedTabIndex = 0,
    tabs = listOf(TabInfo("Search"), *MockObjects.tabInfos.toTypedArray()),
    tabsOnBottom = true,
    searchOnBottom = true,
    syncProgress = null,
)

@Preview
@Composable
private fun PreviewBaseScreenTopSearchAndPager() {
    val state = ScreenState(
        toolbarTitle = "Test Preview Pages",
        selectedTabIndex = 0,
        tabs = MockObjects.tabInfos,
        tabsOnBottom = false,
        searchOnBottom = false,
        syncProgress = null,
    )

    BaseScreenContent(
        state,
        rememberScrollState(),
        {},
        {},
        {}
    )
}

@Preview
@Composable
private fun PreviewBaseScreenBottomSearchAndPager() {
    BaseScreenContent(
        previewState,
        rememberScrollState(),
        {},
        {},
        {},
    )
}

@Preview
@Composable
private fun PreviewBaseScreenBottomSearchAndPagerShort() {
    BaseScreenContent(
        previewState,
        rememberScrollState(),
        {},
        {},
        {},
    )
}

