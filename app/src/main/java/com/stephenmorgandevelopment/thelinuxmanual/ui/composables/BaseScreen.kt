@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stephenmorgandevelopment.thelinuxmanual.distros.AvailableReleases
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ActivityViewModel
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.PagerTab
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ScreenState
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.AppTabBar
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.AppToolbar
import com.stephenmorgandevelopment.thelinuxmanual.utils.MockObjects
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BaseScreen(viewModel: ActivityViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState { state.tabs.size }
    val tabScrollState = rememberScrollState()

    LaunchedEffect(1) {
        viewModel.state
            .distinctUntilChanged { one, two -> one.selectedTabIndex == two.selectedTabIndex }
            .onEach { screenState -> pagerState.scrollToPage(screenState.selectedTabIndex) }
            .launchIn(this)
    }

    BaseScreenContent(
        state = state,
        pagerState = pagerState,
        tabScrollState = tabScrollState,
        onAction = viewModel::onAction
    )
}

@Composable
private fun BaseScreenContent(
    state: ScreenState,
    pagerState: PagerState,
    tabScrollState: ScrollState,
    onAction: (MainScreenAction) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBarsIgnoringVisibility,
        topBar = {
            AppToolbar(
                AvailableReleases.releaseStrings,
                state.tabsOnBottom,
                state.searchOnBottom,
                onAction,
            )
        },
    ) { paddingValues ->
        val syncProgressScrollState = rememberScrollState()

        if (state.syncProgress != null) {
            Text(
                text = state.syncProgress,
                modifier = Modifier
                    .padding(paddingValues)
                    .verticalScroll(syncProgressScrollState),
                style = TextStyle(fontSize = 18.sp),
            )
        } else {
            CompletePager(
                state = state,
                paddingValues = paddingValues,
                pagerState = pagerState,
                tabScrollState = tabScrollState,
                onAction = onAction,
            )
        }
    }
}

@Composable
private fun CompletePager(
    state: ScreenState,
    paddingValues: PaddingValues,
    pagerState: PagerState,
    tabScrollState: ScrollState,
    onAction: (MainScreenAction) -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding(),
            )
            .fillMaxSize()
    ) {
        val (tabBar, pager) = createRefs()

        if (!state.tabsOnBottom) {
            AppTabBar(
                modifier = Modifier
                    .constrainAs(tabBar) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
                    .wrapContentSize(),
                selectedIndex = state.selectedTabIndex,
                tabs = state.tabs,
                scrollState = tabScrollState,
                onTabSelected = onAction,
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .constrainAs(pager) {
                    top.linkTo(if (!state.tabsOnBottom) tabBar.bottom else parent.top)
                    bottom.linkTo(if (!state.tabsOnBottom) parent.bottom else tabBar.top)
                    height = Dimension.fillToConstraints
                    width = Dimension.matchParent
                }
                .background(color = Colors.transparent),
            verticalAlignment = Alignment.Top,
        ) {
            if (state.selectedTabIndex == 0) {
                LookupScreen(
                    searchOnBottom = state.searchOnBottom,
                    onItemClick = { id, url -> onAction(MainScreenAction.ItemClick(id, url)) },
                )
            } else {
                Text(
                    text = "Placeholder ${state.selectedTabIndex}",
                    style = TextStyle(fontSize = 40.sp),
                    color = Colors.offWhite,
                )
            }
        }

        if (state.tabsOnBottom) {
            AppTabBar(
                Modifier
                    .constrainAs(tabBar) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                    }
                    .padding(vertical = 3.dp)
                    .wrapContentSize(),
                state.selectedTabIndex,
                state.tabs,
                tabScrollState,
                onAction,
            )
        }
    }
}


private val previewState = ScreenState(
    toolbarTitle = "Test Preview Pages",
    selectedTabIndex = 0,
    tabs = listOf(PagerTab("Search"), *MockObjects.pagerTabs.toTypedArray()),
    tabsOnBottom = true,
    searchOnBottom = true,
    syncProgress = null,
)

@Preview
@Composable
private fun PreviewBaseScreenBottomSearchAndPager() {
    BaseScreenContent(
        previewState,
        rememberPagerState { MockObjects.pagerTabs.size },
        rememberScrollState(),
    ) { }
}

@Preview
@Composable
private fun PreviewBaseScreenBottomSearchAndPagerShort() {
    BaseScreenContent(
        previewState,
        rememberPagerState { MockObjects.pagerTabs.size },
        rememberScrollState(),
    ) { }
}

