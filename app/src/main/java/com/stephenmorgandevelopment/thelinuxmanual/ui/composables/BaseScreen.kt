@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.AvailableReleases.Companion.releaseStrings
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenOptionsMenuAction.ChangeVersion
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenOptionsMenuAction.ReSync
import com.stephenmorgandevelopment.thelinuxmanual.presentation.OptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.TabInfo
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.AppTabBar
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.AppToolbar
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus.LookupOptionsMenu
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus.ManPageOptionsMenu
import com.stephenmorgandevelopment.thelinuxmanual.utils.showSyncText

@Composable
fun BaseScreen(
    title: String,
    subtitle: String,
    tabsOnBottom: Boolean,
    searchOnBottom: Boolean,
    selectedTabIndex: Int,
    tabs: List<TabInfo>,
    syncProgress: String? = null,
    clearLookupQuery: () -> Unit = {},
    onActivityAction: (MainScreenAction) -> Unit,
    onOptionsMenuAction: (OptionsMenuAction) -> Unit,
    content: @Composable () -> Unit,
) {
    val orientation = LocalConfiguration.current.orientation

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets =
            if (orientation == Configuration.ORIENTATION_PORTRAIT)  WindowInsets.navigationBars
            else WindowInsets.navigationBars.add(WindowInsets.displayCutout),
        topBar = {
            AppToolbar(title, subtitle) {
                if (selectedTabIndex == 0) {
                    LookupOptionsMenu(
                        releaseStrings,
                        tabsOnBottom,
                        searchOnBottom,
                    ) {
                        if (it == ReSync
                            || (it is ChangeVersion && it.version.lowercase()
                                    != subtitle.lowercase())
                        ) {
                            clearLookupQuery()
                        }
                        onOptionsMenuAction(it)
                    }
                } else {
                    ManPageOptionsMenu(
                        tabs[selectedTabIndex].title,
                        tabs[selectedTabIndex].manPageSections,
                    ) { onOptionsMenuAction(it) }
                }
            }
        },
    ) { paddingValues ->
        if (syncProgress != null) {
            val syncProgressScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                Text(
                    text = showSyncText(title, syncProgress),
                    modifier = Modifier
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                        .verticalScroll(syncProgressScrollState),
                    style = TextStyle(fontSize = 18.sp),
                )
            }
        } else {
            CompletePager(
                tabsOnBottom = tabsOnBottom,
                selectedTabIndex = selectedTabIndex,
                tabs = tabs,
                paddingValues = paddingValues,
                onAction = onActivityAction,
                content = content,
            )
        }
    }
}

@Composable
private fun CompletePager(
    tabsOnBottom: Boolean,
    selectedTabIndex: Int,
    tabs: List<TabInfo>,
    paddingValues: PaddingValues,
    onAction: (MainScreenAction) -> Unit,
    content: @Composable () -> Unit,
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
            content()
        }
    }
}
