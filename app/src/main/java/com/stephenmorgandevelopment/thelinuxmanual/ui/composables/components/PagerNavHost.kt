@file:OptIn(ExperimentalFoundationApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupAction.UpdateSearchText
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.ShowOfflineDialog
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageOptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageSearchState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ShowDialogEvents
import com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation.Lookup
import com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation.ManPage
import com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation.Offline
import com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation.PrivacyPolicy
import com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation.routeDef
import com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation.toRoutePath
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.ActivityViewModel
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.LookupViewModel
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.ManPageViewModel.Companion.ITEM_ID_KEY
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.ManPageViewModel.Companion.TITLE_KEY
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.BaseScreen
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.LookupScreen
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.ManPageScreen
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.getManPageViewModel
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.strategies.ManPageSectionPrefetchStrategy
import com.stephenmorgandevelopment.thelinuxmanual.ui.lifecycle.ManPageTab
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNull
import com.stephenmorgandevelopment.thelinuxmanual.utils.launchInCompletable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@SuppressLint("RememberReturnType")
@Composable
fun PagerNavHost(
    activityViewModel: ActivityViewModel = viewModel(),
    lookupViewModel: LookupViewModel = viewModel(),
    navController: NavHostController,
    searchStates: Map<Long, ManPageSearchState>,
    onFinish: () -> Unit,
    updateSearchState: (ManPageSearchState) -> Unit,
) {
    val screenState by activityViewModel.state.collectAsStateWithLifecycle(lifecycleOwner = LocalActivity.current as ComponentActivity)
    val lookupState by lookupViewModel.state.collectAsStateWithLifecycle(lifecycleOwner = LocalActivity.current as ComponentActivity)
    val tabScrollState = rememberScrollState()
    val listStateMap: MutableMap<Long, LazyListState> = remember(1) { mutableStateMapOf() }
    val tabLifecycleOwners = remember(1) { mutableMapOf<Long, ManPageTab>() }

    val coroutineScope = rememberCoroutineScope()

    (LocalActivity.current as ComponentActivity?)?.let {
        navController.setLifecycleOwner(it)
        navController.setViewModelStore(it.viewModelStore)
    }

    BackHandler(
        enabled = (screenState.selectedTabIndex == 0),
    ) {
        if (lookupState.searchText.isEmpty()) onFinish()
        else lookupViewModel.onAction(UpdateSearchText(""))
    }

    // Event listener for dialogs
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        activityViewModel.showDialogEvent
            .onEach {
                when (it) {
                    ShowDialogEvents.PrivacyPolicy -> navController.navigate(PrivacyPolicy)
                    ShowDialogEvents.NoInternet -> navController.navigate(Offline)
                }
            }.launchIn(coroutineScope)
    }

    // State watcher for tab changes
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
                            navController.navigate(ManPage(title, manPageId).toRoutePath()) {
                                restoreState = false
                            }
                        }
                    }
                }

                // Control the custom lifecycles for tabs.
                tabLifecycleOwners.forEach { tabEntry ->
                    if (tabEntry.key == screenState.currentTab.manPageId) {
                        tabEntry.value.lifecycle.moveTo(Lifecycle.State.RESUMED)
                    } else if (!screenState.tabs.map { it.manPageId }.contains(tabEntry.key)) {
                        tabEntry.value.lifecycle.moveTo(Lifecycle.State.DESTROYED)
                    } else {
                        tabEntry.value.lifecycle.moveTo(Lifecycle.State.STARTED)
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Colors.transparent),
        navController = navController,
        startDestination = Lookup,
    ) {

        dialog<PrivacyPolicy>(
            dialogProperties = DialogProperties(),
        ) {
            PrivacyPolicy()
        }

        dialog<Offline>(
            dialogProperties = DialogProperties(),
        ) {
            LifecycleStartEffect(1) {
                onStopOrDispose {
                    activityViewModel.onAction(MainScreenAction.CloseTab)
                }
            }

            OfflineDialog()
        }

        composable<Lookup>(
            enterTransition = {
                fadeIn(
                    initialAlpha = .7f,
                    animationSpec = tween(durationMillis = 100)
                )
            },
            exitTransition = { null },
        ) {
            BackHandler(enabled = false) {}
            if (listStateMap[-700L].isNull()) listStateMap[-700L] = rememberLazyListState()

            listStateMap[-700L]?.let { listState ->
                BaseScreen(
                    title = screenState.title,
                    subtitle = screenState.subtitle,
                    tabsOnBottom = screenState.tabsOnBottom,
                    searchOnBottom = screenState.searchOnBottom,
                    selectedTabIndex = screenState.selectedTabIndex,
                    tabs = screenState.tabs,
                    tabScrollState = tabScrollState,
                    syncProgress = screenState.syncProgress,
                    clearLookupQuery = { lookupViewModel.onAction(UpdateSearchText("")) },
                    onActivityAction = activityViewModel::onAction,
                    onOptionsMenuAction = activityViewModel::onOptionMenuAction,
                ) {
                    LookupScreen(
                        searchOnBottom = screenState.searchOnBottom,
                        state = lookupState,
                        onAction = lookupViewModel::onAction,
                        lazyListState = listState
                    ) { title, manPageId ->
                        activityViewModel.onAction(
                            MainScreenAction.AddTab(
                                title,
                                manPageId
                            )
                        )
                    }
                }
            }
        }

        composable(
            route = ManPage.Companion.routeDef,
            arguments = listOf(
                navArgument(TITLE_KEY) { type = NavType.StringType },
                navArgument(ITEM_ID_KEY) { type = NavType.LongType },
            ),
            enterTransition = {
                fadeIn(
                    initialAlpha = .4f,
                    animationSpec = tween(durationMillis = 70)
                )
            },
            exitTransition = { null }
        ) { backStackEntry ->
            if (backStackEntry.arguments == null) throw RuntimeException("Navigation api failed to populate arguments.")
            backStackEntry.arguments?.let { args ->
                val title = args.getString(TITLE_KEY) ?: ""
                val id = args.getLong(ITEM_ID_KEY)
                val vm = getManPageViewModel(title, id)

                val manPageScope = rememberCoroutineScope()

                val state by vm.state.collectAsStateWithLifecycle()
                val searchState by vm.searchState.collectAsStateWithLifecycle()

                val tabLifecycleOwner = tabLifecycleOwners[id]
                    ?: ManPageTab(id, LocalActivity.current as ComponentActivity)
                        .also {
                            tabLifecycleOwners[id] = it
                            it.lifecycle.moveTo(Lifecycle.State.RESUMED)
                        }

                state.command?.let {
                    listStateMap[id].let { savedListState ->
                        if (savedListState == null) {
                            listStateMap[id] = rememberLazyListState(
                                prefetchStrategy = ManPageSectionPrefetchStrategy(
                                    it.data.size,
                                    vm::onAction,
                                    tabLifecycleOwner.lifecycle,
                                )
                            )
                        } else {
                            listStateMap[id] = remember {
                                LazyListState(
                                    firstVisibleItemIndex = savedListState.firstVisibleItemIndex,
                                    firstVisibleItemScrollOffset = savedListState.firstVisibleItemScrollOffset,
                                    prefetchStrategy = ManPageSectionPrefetchStrategy(
                                        it.data.size,
                                        vm::onAction,
                                        tabLifecycleOwner.lifecycle,
                                    )
                                )
                            }
                        }
                    }
                }
                val listState = listStateMap[id]

                // Create/Restore searchState held by composable at Activity level.
                LifecycleEventEffect(
                    Lifecycle.Event.ON_CREATE,
                ) {
                    searchStates[id].let {
                        if (it == null) {
                            updateSearchState(ManPageSearchState(id))
                        } else {
                            vm.restoreSearchState(it)
                        }
                    }
                }

                // Listen for JumpTo events and scroll accordingly.
                var launchedEffectKey by remember { mutableIntStateOf(0) }
                DisposableEffect(
                    launchedEffectKey + (state.command?.id ?: -2L),
                ) {
                    val job = vm.jumpToEvents.onEach { jumpTo ->
                        state.getSectionIndexByName(jumpTo.section)
                            ?.let { sectionIndex ->
                                listState?.animateScrollToItem(sectionIndex, jumpTo.offset)
                                    ?: also {
                                        launchedEffectKey++
                                        return@let
                                    }
                                vm.clearLoading()
                            }
                    }.launchInCompletable(manPageScope)
                        .apply {
                            invokeOnCompletion {
                                // Relaunch job if canceled for any reason.  We will complete
                                //  the job onDispose.
                                if (it != null) launchedEffectKey++
                            }
                        }

                    onDispose { job.complete() }
                }

                CompositionLocalProvider(
                    LocalLifecycleOwner provides tabLifecycleOwner.lifecycle.owner,
                ) {
                    BaseScreen(
                        title = title,
                        subtitle = state.currentSection,
                        tabScrollState = tabScrollState,
                        tabsOnBottom = screenState.tabsOnBottom,
                        searchOnBottom = screenState.searchOnBottom,
                        selectedTabIndex = screenState.selectedTabIndex,
                        syncProgress = screenState.syncProgress,
                        tabs = screenState.tabs,
                        onActivityAction = activityViewModel::onAction,
                        onOptionsMenuAction = { action ->
                            if (action is ManPageOptionsMenuAction.Close) {
                                activityViewModel.onAction(MainScreenAction.CloseTab)
                            } else {
                                vm.onOptionMenuAction(action)
                            }
                        },
                    ) {
                        ManPageScreen(
                            title = title,
                            listState = listState,
                            searchState = searchState,
                            command = state.command,
                            loading = state.loading,
                            onJumpTo = vm::onOptionMenuAction,
                            onAction = vm::onAction,
                            showOfflineDialog = {
                                activityViewModel.onAction(ShowOfflineDialog(id))
                            },
                            onBackPressed = {
                                if (screenState.selectedTabIndex != 0) {
                                    activityViewModel.onAction(MainScreenAction.TabSelected(0))
                                }
                            },
                            backCallbackEnabled = { screenState.selectedTabIndex != 0 }
                        )
                    }
                }

                // Update searchState held at the activity level
                LifecycleEventEffect(
                    Lifecycle.Event.ON_RESUME,
                ) {
                    vm.searchState
                        .onEach { updateSearchState(it) }
                        .flowOn(Dispatchers.Default)
                        .launchIn(manPageScope)
                }
            }
        }
    }
}
