@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.stephenmorgandevelopment.thelinuxmanual.distros.ubuntu.AvailableReleases.Companion.releaseStrings
import com.stephenmorgandevelopment.thelinuxmanual.presentation.DialogEvent
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupAction.UpdateSearchText
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenOptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenOptionsMenuAction.ChangeVersion
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenOptionsMenuAction.ReSync
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageSearchState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.OptionsMenuHandler
import com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation.Lookup
import com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation.ManPage
import com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation.Offline
import com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation.PrivacyPolicy
import com.stephenmorgandevelopment.thelinuxmanual.presentation.toAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.ActivityViewModel
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.LookupViewModel
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.AppToolbar
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.CompletePager
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.SyncText
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus.LookupOptionsMenu
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.menus.ManPageOptionsMenu
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.navigation.PagerNavHost
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@Composable
fun BaseScreen(
    activityViewModel: ActivityViewModel = viewModel(),
    lookupViewModel: LookupViewModel = viewModel(),
    optionsMenuHandler: OptionsMenuHandler,
    searchStates: Map<Long, ManPageSearchState>,
    listStateMap: Map<Long, LazyListState>,
    updateSearchState: (ManPageSearchState) -> Unit,
    updateListMap: (Long, LazyListState) -> Unit,
) {

    val screenState by activityViewModel.state.collectAsStateWithLifecycle(lifecycleOwner = LocalActivity.current as ComponentActivity)
    val lookupState by lookupViewModel.state.collectAsStateWithLifecycle(lifecycleOwner = LocalActivity.current as ComponentActivity)
    val navController = rememberNavController()

    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    val coroutineScope = rememberCoroutineScope()
    val activity = LocalActivity.current as? ComponentActivity

    activity?.let {
        navController.setLifecycleOwner(it)
        navController.setViewModelStore(it.viewModelStore)
    }

    BackHandler(
        enabled = (screenState.selectedTabIndex == 0),
    ) {
        if (lookupState.searchText.isEmpty()) activity?.finish()
        else lookupViewModel.onAction(UpdateSearchText(""))
    }

    /**
     *  Event listener for dialogs
     */
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        activityViewModel.showDialogEvent
            .onEach {
                when (it) {
                    DialogEvent.PrivacyPolicy -> navController.navigate(PrivacyPolicy) {
                        launchSingleTop = true
                    }

                    DialogEvent.NoInternet -> navController.navigate(Offline) {
                        launchSingleTop = true
                    }
                }
            }.launchIn(coroutineScope)
    }

    /**
     *  Event listener for Option Menu events.
     */
    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        optionsMenuHandler.events
            .map { it.toAction() }
            .onEach {
                if (it is MainScreenOptionsMenuAction) activityViewModel.onOptionMenuAction(
                    it,
                )
            }.launchIn(coroutineScope)
    }

    /**
     *  State watcher for tab changes
     */
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        activityViewModel.state
            .distinctUntilChanged { one, two ->
                one.tabs.size <= two.tabs.size
                        && one.selectedTabIndex == two.selectedTabIndex
            }
            .onEach { screenState ->
                when {
                    screenState.selectedTabIndex == 0 -> {
                        navController.navigate(route = Lookup) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                                saveState = false
                            }

                            launchSingleTop = true
                            restoreState = true
                        }
                    }

                    else -> {
                        // Creating new instance every time we return to a tab.  This is happening
                        //  because the nav library can't differentiate routes, that have different
                        //  values in their path segments.  Therefore we cannot use restoreState, as
                        //  doing so will give us the same tab first navigated to.
                        with(screenState.tabs[screenState.selectedTabIndex]) {
                            navController.navigate(ManPage(title, manPageId)) {
                                popUpTo(ManPage(title, manPageId)) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    /**
     *  Scaffold
     */
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets =
            if (isPortrait) WindowInsets.navigationBars
            else WindowInsets.navigationBars.add(WindowInsets.displayCutout),
        topBar = {
            AppToolbar(screenState.title, screenState.subtitle) {
                if (screenState.selectedTabIndex == 0) {
                    LookupOptionsMenu(
                        releaseStrings,
                        screenState.tabsOnBottom,
                        screenState.searchOnBottom,
                    ) {
                        if (it == ReSync
                            || (it is ChangeVersion
                                    && it.version.lowercase() != screenState.subtitle.lowercase())
                        ) {
                            lookupViewModel.onAction(UpdateSearchText(""))
                        }
                        optionsMenuHandler.triggerEvent(it)
                    }
                } else {
                    with(screenState) {
                        ManPageOptionsMenu(
                            tabs[selectedTabIndex].title,
                            tabs[selectedTabIndex].manPageSections,
                        ) {
                            optionsMenuHandler.triggerEvent(it)
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        if (screenState.syncProgress != null) {
            screenState.syncProgress?.let { progress ->
                SyncText(
                    padding = paddingValues,
                    progressString = progress,
                )
            }
        } else {
            CompletePager(
                tabsOnBottom = screenState.tabsOnBottom,
                selectedTabIndex = screenState.selectedTabIndex,
                tabs = screenState.tabs,
                paddingValues = paddingValues,
                onAction = activityViewModel::onAction,
                tabContent = {
                    PagerNavHost(
                        searchStates = searchStates,
                        listStateMap = listStateMap,
                        updateSearchState = updateSearchState,
                        updateListMap = updateListMap,
                        optionsMenuHandler = optionsMenuHandler,
                        navController = navController,
                        coroutineScope = coroutineScope,
                        onActivityAction = activityViewModel::onAction,
                        lookupState = lookupState,
                        searchOnBottom = screenState.searchOnBottom,
                        onLookupAction = lookupViewModel::onAction,
                        closeTab = {
                            activityViewModel.onOptionMenuAction(
                                MainScreenOptionsMenuAction.CloseTab
                            )
                        }
                    )
                },
            )
        }
    }
}
