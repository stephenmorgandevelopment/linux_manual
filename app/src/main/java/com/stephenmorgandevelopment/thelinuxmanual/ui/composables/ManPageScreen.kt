@file:OptIn(ExperimentalSerializationApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.models.Command
import com.stephenmorgandevelopment.thelinuxmanual.models.SingleTextMatch
import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageOptionsMenuAction.JumpTo
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageSearchState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageViewModel
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.ManPageSection
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.SearchBarWithButton
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.TextMatchControlBar
import com.stephenmorgandevelopment.thelinuxmanual.utils.getString
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNotNull
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNull
import com.stephenmorgandevelopment.thelinuxmanual.utils.launchInCompletable
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.ExperimentalSerializationApi

@Composable
fun getManPageViewModel(
    title: String,
    manPageId: Long,
    viewModel: ManPageViewModel = hiltViewModel<ManPageViewModel>(
        key = "$title-$manPageId",
    ),
): ManPageViewModel {
    return remember("$title-$manPageId") { viewModel }
}

@Composable
fun ManPageScreen(
    title: String,
    listState: LazyListState,
    searchState: ManPageSearchState,
    viewModel: ManPageViewModel,
    backPressedEnabled: () -> Boolean = { false },
    onBackPressed: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle(
        lifecycleOwner = LocalLifecycleOwner.current,
    )

    BackHandler(enabled = backPressedEnabled()) {
        onBackPressed()
    }

    val recomposerCoroutineScope = rememberCoroutineScope()

    var launchedEffectKey by remember { mutableIntStateOf(0) }
    LifecycleStartEffect(title + launchedEffectKey) {
        val lifecycleScopedJob: CompletableJob = viewModel.state
            .distinctUntilChanged { one, two -> one.jumpTo == two.jumpTo }
            .onEach { state ->
                with(state) {
                    if (jumpTo != null) {
                        getSectionIndexByName(jumpTo.section)
                            ?.let { sectionIndex ->
                                if (jumpTo.quickJump) {
                                    listState.scrollToItem(sectionIndex, jumpTo.offset)
                                } else {
                                    listState.animateScrollToItem(sectionIndex, jumpTo.offset)
                                }
                            }
                    }
                }
            }.launchInCompletable(recomposerCoroutineScope).apply {
                invokeOnCompletion {
                    if (it != null) launchedEffectKey++
                }
            }

        onStopOrDispose {
            lifecycleScopedJob.complete()
        }
    }

//    LifecycleEventEffect(Lifecycle.Event.ON_START) {
//        // Persist search state
//        viewModel.state.distinctUntilChanged { one, two ->
//            one.searchVisible == two.searchVisible
//                    && one.searchResults == two.searchResults
//                    && one.searchText == two.searchText
//        }.onEach {
//            searchManager.visible = it.searchVisible
//            searchManager.query = it.searchText
//            searchManager.results = it.searchResults
//        }.flowOn(Dispatchers.IO)
//            .launchIn(recomposerCoroutineScope)
//    }

    state.command?.let {
        ManPageScreenContent(
            title = title,
            searchText = searchState.query,
            searchVisible = searchState.visible,
            searchResults = searchState.results,
            searchIndex = searchState.index,
            command = it,
            listState = listState,
            onJumpTo = viewModel::onOptionMenuAction,
            onAction = viewModel::onAction,
        )
    }
}

@Composable
private fun ManPageScreenContent(
    title: String,
    searchText: String,
    searchVisible: Boolean,
    searchResults: TextSearchResult?,
    searchIndex: Int,
    command: Command,
    listState: LazyListState,
    onJumpTo: (JumpTo) -> Unit,
    onAction: (ManPageAction) -> Unit,
) {
//    var matchIndex by remember { mutableIntStateOf(0) }
    val focusManager = LocalFocusManager.current

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        val (searchBar, pageData, textMatchBar) = createRefs()

        if (searchVisible) {
            SearchBarWithButton(
                modifier = Modifier.constrainAs(searchBar) {
                    top.linkTo(parent.top)
                    width = Dimension.matchParent
                    height = Dimension.wrapContent
                },
                iconContentDescription =
                    getString(R.string.search_for_text_in_page, searchText, title),
                searchText = searchText,
                searchResults = searchResults,
                onAction = {
                    onAction(it)
                    if (it is ManPageAction.Search) {
//                        matchIndex = 0
                        focusManager.clearFocus(true)
                    }
                },
            )
        }

        if (searchVisible && searchResults.isNotNull()) {
            TextMatchControlBar(
                modifier = Modifier
                    .constrainAs(textMatchBar) {
                        top.linkTo(searchBar.bottom)
                        width = Dimension.matchParent
                        height = Dimension.wrapContent
                    }
                    .padding(start = 12.dp, end = 12.dp, bottom = 4.dp),
                query = searchText,
                index = searchIndex,
                count = searchResults?.count ?: 0,
                onPrev = {
                    onAction(ManPageAction.PrevSearchMatch)
                },
                onNext = {
                    onAction(ManPageAction.NextSearchMatch)
                }
            )
        }

        // TODO: Make implementation using Column and compare for performance.
        //  Ensure sections added to column as ready.
        LazyColumn(
            modifier = Modifier
                .constrainAs(pageData) {
                    if (searchVisible && searchResults.isNull()) top.linkTo(searchBar.bottom)
                    else if (searchVisible && searchResults.isNotNull()) top.linkTo(
                        textMatchBar.bottom
                    )
                    else top.linkTo(parent.top)

                    bottom.linkTo(parent.bottom)
                    width = Dimension.matchParent
                    height = Dimension.fillToConstraints
                }
                .padding(vertical = 0.dp, horizontal = 8.dp),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            flingBehavior = ScrollableDefaults.flingBehavior(),
            contentPadding = PaddingValues(
                horizontal = 4.dp,
                vertical = 8.dp,
            ),
        ) {
            items(
                count = command.data.size,
                key = { command.data.toList()[it].first },
            ) { index ->
                // TODO: Optimize this.  Most likely causing minor slow downs.
                val singleTextMatch = searchResults?.getMatch(searchIndex)?.takeIf {
                    it.section == command.data.toList()[index].first
                }

                command.data.toList()[index].let { section ->
                    ManPageSection(
                        name = section.first,
                        data = section.second,
                        singleTextMatch = singleTextMatch,
                        onTextMatchedOffset =
                            if (singleTextMatch.isNull()) {
                                {}
                            } else {
                                {
                                    if (it != SingleTextMatch.NO_TEXT_MATCH) {
                                        onJumpTo(JumpTo(section.first, it))
                                    }
                                }
                            }
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(color = Colors.transparent)
                    )
                }
            }

        }

        searchResults?.let {
            if (searchVisible) {
                it.getSectionAt(searchIndex)?.let { section -> onJumpTo(JumpTo(section)) }
            }
        }
    }
}

//@Preview
//@Composable
//private fun PreviewManPageScreen() {
//    val state = ManPageTabState(
//        Command(17L, MockObjects.commandData),
//        TextSearchResult("Winning?", emptyList()),
//        true,
//        "Winning?"
//    )
//
//    ManPageScreenContent(
//        "Pina Call'alot-us",
//        state.searchText,
//        state.searchVisible,
//        state.searchResults,
//        state.command!!,
//        rememberLazyListState(),
//        {},
//        {},
//    )
//}
