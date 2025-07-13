package com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ActivityViewModel
import com.stephenmorgandevelopment.thelinuxmanual.presentation.Lookup
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupViewModel
import com.stephenmorgandevelopment.thelinuxmanual.presentation.MainScreenAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPage
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction.RestoreSearchState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageOptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageSearchState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageViewModel.Companion.ITEM_ID_KEY
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageViewModel.Companion.TITLE_KEY
import com.stephenmorgandevelopment.thelinuxmanual.presentation.PrivacyPolicy
import com.stephenmorgandevelopment.thelinuxmanual.presentation.routeDef
import com.stephenmorgandevelopment.thelinuxmanual.presentation.toRoutePath
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.BaseScreen
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.Colors
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.LookupScreen
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.ManPageScreen
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.getManPageViewModel
import com.stephenmorgandevelopment.thelinuxmanual.utils.getString
import com.stephenmorgandevelopment.thelinuxmanual.utils.ilog
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNull
import com.stephenmorgandevelopment.thelinuxmanual.utils.vlog
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

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
        else lookupViewModel.onAction(LookupAction.UpdateSearchText(""))
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        activityViewModel.showPrivacyPolicyEvent
            .onEach {
                navController.navigate(PrivacyPolicy)
            }.launchIn(coroutineScope)
    }

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
                        //  values in their path segments.....
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

    NavHost(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Colors.transparent),
        navController = navController,
        startDestination = Lookup,
    ) {

        composable<Lookup> {

            if (listStateMap[-700L].isNull()) listStateMap[-700L] = rememberLazyListState()

            listStateMap[-700L]?.let { listState ->
                BaseScreen(
                    content = {
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
                    },
                    screenState = screenState,
                    tabScrollState = tabScrollState,
                    onActivityAction = activityViewModel::onAction,
                    onOptionsMenuAction = activityViewModel::onOptionMenuAction,
                )
            }
        }

        dialog<PrivacyPolicy>(
            dialogProperties = DialogProperties(),
        ) {
            Box(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                ManPageSection(
                    getString(R.string.privacy_policy_button),
                    getString(R.string.privacy_policy)
                )
            }
        }

        composable(
            route = ManPage.Companion.routeDef,
            arguments = listOf(
                navArgument(TITLE_KEY) { type = NavType.StringType },
                navArgument(ITEM_ID_KEY) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            if (backStackEntry.arguments == null) throw RuntimeException("Navigation api failed to populate arguments.")
            backStackEntry.arguments?.let { args ->
                val title = args.getString(TITLE_KEY) ?: ""
                val id = args.getLong(ITEM_ID_KEY)
                val vm = getManPageViewModel(title, id)

                if (listStateMap[id].isNull()) listStateMap[id] = rememberLazyListState()
                val listState = listStateMap[id]
                    ?: throw IllegalStateException("Error initializing rememberLazyListState().")

                if (searchStates[id] == null) updateSearchState(ManPageSearchState(id))
                else {
                    searchStates[id]?.let {
                        remember {
                            vm.onAction(RestoreSearchState(it))
                            true
                        }
                    }
                }

                vm.searchState
                    .onEach {
                        javaClass.ilog("nagGraph received emission from searchState flow with $it")
                        updateSearchState(it).also { _ ->
                            javaClass.ilog("nagGraph calling updateSearchState for ${it.id}")
                            javaClass.vlog("nagGraph calling updateSearchState for ${it}")
                        }
                    }.launchIn(coroutineScope)

                val searchState by vm.searchState.collectAsStateWithLifecycle(lifecycleOwner = backStackEntry)

                BaseScreen(
                    content = {
                        ManPageScreen(
                            title = title,
                            listState = listState,
                            searchState = searchState,
                            viewModel = vm,
                            backPressedEnabled = { screenState.selectedTabIndex != 0 },
                        ) {
                            activityViewModel.onAction(MainScreenAction.TabSelected(0))
                        }
                    },
                    screenState = screenState,
                    tabScrollState = tabScrollState,
                    onActivityAction = activityViewModel::onAction,
                    onOptionsMenuAction = { action ->
                        if (action is ManPageOptionsMenuAction.Close) {
                            activityViewModel.onAction(MainScreenAction.CloseTab)
                        } else {
                            vm.onOptionMenuAction(action)
                        }
                    },
                )
            }
        }
    }
}