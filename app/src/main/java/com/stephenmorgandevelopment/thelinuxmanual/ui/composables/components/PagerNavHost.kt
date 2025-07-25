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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.stephenmorgandevelopment.thelinuxmanual.presentation.JUMP_TO_OPTIONS_MENU_OFFSET
import com.stephenmorgandevelopment.thelinuxmanual.presentation.JUMP_TO_RENDERING_OFFSET
import com.stephenmorgandevelopment.thelinuxmanual.presentation.JumpToData
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupAction.UpdateSearchText
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction.ShowOfflineDialog
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageOptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageOptionsMenuAction.JumpTo
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

    /**
     *      Event listener for dialogs
     */
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        activityViewModel.showDialogEvent
            .onEach {
                when (it) {
                    ShowDialogEvents.PrivacyPolicy -> navController.navigate(PrivacyPolicy)
                    ShowDialogEvents.NoInternet -> navController.navigate(Offline)
                }
            }.launchIn(coroutineScope)
    }

    /**
     *      State watcher for tab changes
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
                            navController.navigate(ManPage(title, manPageId).toRoutePath()) {
                                restoreState = false
                            }
                        }
                    }
                }
            }
            .launchIn(coroutineScope)
    }

    /**
     *      NavHost declaration
     */
    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Colors.transparent),
        navController = navController,
        startDestination = Lookup,
    ) {

        /**
         *  PrivacyPolicy dialog
         */
        dialog<PrivacyPolicy>(
            dialogProperties = DialogProperties(),
        ) {
            PrivacyPolicy()
        }

        /**
         *  Offline dialog
         */
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

        /**
         *  Lookup composable
         */
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


        /**
         *  ManPage composable
         */
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

            val title = backStackEntry.arguments?.getString(TITLE_KEY) ?: ""
            val id = backStackEntry.arguments?.getLong(ITEM_ID_KEY)
                ?: throw RuntimeException("Navigation api failed to populate arguments.")

            BackHandler(screenState.selectedTabIndex != 0) {
                if (screenState.selectedTabIndex != 0) {
                    activityViewModel.onAction(MainScreenAction.TabSelected(0))
                }
            }

            val vm = getManPageViewModel(title, id)
            val state by vm.state.collectAsStateWithLifecycle()
            val searchState by vm.searchState.collectAsStateWithLifecycle()

            val manPageScope = rememberCoroutineScope()

            /**
             *  Create/Restore list state to be preserved by PagerNavHost composable.
             *  This is needed because of the above mentioned limitation in the navigation library.
             *  We are actually creating a new composable when we go back to a previous tab, so
             *  we hoist the scroll state for each tab.
             */
            state.command?.let {
                listStateMap[id].let { savedListState ->
                    if (savedListState == null) {
                        listStateMap[id] = rememberLazyListState(
                            prefetchStrategy = ManPageSectionPrefetchStrategy(
                                id = id,
                                listSize = it.data.size,
                                onAction = vm::onAction,
                                tabLifecycle = backStackEntry.lifecycle,
                            )
                        )
                    } else {
                        listStateMap[id] = remember {
                            LazyListState(
                                firstVisibleItemIndex = savedListState.firstVisibleItemIndex,
                                firstVisibleItemScrollOffset = savedListState.firstVisibleItemScrollOffset,
                                prefetchStrategy = ManPageSectionPrefetchStrategy(
                                    id = id,
                                    listSize = it.data.size,
                                    onAction = vm::onAction,
                                    tabLifecycle = backStackEntry.lifecycle,
                                )
                            )
                        }
                    }
                }
            }
            val listState = listStateMap[id]


            /**
             * Create/Restore searchState held by composable at Activity level.
             * Holding at activity level so that search state's can survive config changes.
             */
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

            /**
             *  Listen for JumpTo events and scroll accordingly.
             */
            var launchedEffectKey by remember { mutableIntStateOf(0) }
            DisposableEffect(
                launchedEffectKey + (state.command?.id ?: -2L),
            ) {
                var lastJumpTo: JumpToData? = null
                val job = vm.jumpToEvents.onEach { jumpTo ->
                    if (listState == null) {
                        launchedEffectKey++
                        return@onEach
                    }

                    val isRenderJumpTo = jumpTo.offset == JUMP_TO_RENDERING_OFFSET
                    val onCorrectSection = state.currentSection == jumpTo.section

                    val isLastOffsetFromSearch = lastJumpTo?.offset?.let {
                        it != JUMP_TO_OPTIONS_MENU_OFFSET && it != JUMP_TO_RENDERING_OFFSET
                    } ?: false

                    val isJumpToFromSearch = jumpTo.offset != JUMP_TO_OPTIONS_MENU_OFFSET
                            && jumpTo.offset != JUMP_TO_RENDERING_OFFSET

                    state.getSectionIndexByName(jumpTo.section)?.let { sectionIndex ->
                        if (jumpTo.offset == JUMP_TO_OPTIONS_MENU_OFFSET) {
                            listState.animateScrollToItem(sectionIndex, jumpTo.offset)
                        } else if (isRenderJumpTo && !onCorrectSection) {
                            listState.scrollToItem(sectionIndex, jumpTo.offset)

                            if (isLastOffsetFromSearch) {
                                lastJumpTo?.offset?.let {
                                    listState.requestScrollToItem(sectionIndex, it)
                                }
                            }
                        } else if (isJumpToFromSearch) {
                            lastJumpTo = jumpTo
                            listState.requestScrollToItem(sectionIndex, jumpTo.offset)
                        }

                        vm.clearLoading()
                    }
                }.launchInCompletable(manPageScope)
                    .apply {
                        invokeOnCompletion {
                            // Relaunch job if canceled for any reason.
                            // We will complete the job onDispose.
                            if (it != null) launchedEffectKey++
                        }
                    }

                onDispose { job.complete() }
            }


            /**
             *  Composable Screen creation.
             */
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
                )
            }

            LifecycleEventEffect(
                Lifecycle.Event.ON_RESUME,
            ) {
                // Needed to trigger rendering of ManPageSection containing current search result,
                // when navigating back to the tab.
                searchState.results?.let { results ->
                    if (searchState.visible) {
                        results.getSectionAt(searchState.index)?.let { section ->
                            vm.onOptionMenuAction(JumpTo(section, JUMP_TO_RENDERING_OFFSET))
                        }
                    }
                }

                // Update searchState held at the activity level
                vm.searchState
                    .onEach { updateSearchState(it) }
                    .flowOn(Dispatchers.Default)
                    .launchIn(manPageScope)
            }

        }
    }
}
