@file:OptIn(ExperimentalSerializationApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LocalPinnableContainer
import androidx.compose.ui.layout.PinnableContainer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.stephenmorgandevelopment.thelinuxmanual.R
import com.stephenmorgandevelopment.thelinuxmanual.models.Command
import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageOptionsMenuAction.JumpTo
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageSearchState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.ManPageViewModel
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.LoadingIndicator
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.ManPageSection
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.SearchBarWithButton
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.TextMatchControlBar
import com.stephenmorgandevelopment.thelinuxmanual.utils.MockObjects
import com.stephenmorgandevelopment.thelinuxmanual.utils.getString
import com.stephenmorgandevelopment.thelinuxmanual.utils.ilog
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNotNull
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun ManPageScreen(
    title: String,
    listState: LazyListState?,
    searchState: ManPageSearchState,
    command: Command?,
    loading: Boolean,
    onJumpTo: (JumpTo) -> Unit,
    onAction: (ManPageAction) -> Unit,
    showOfflineDialog: () -> Unit,
) {

    when {
        command?.id == -1L -> {
            LaunchedEffect(1) {
                delay(125)
                showOfflineDialog()
            }
        }

        command == null -> LoadingIndicator()

        else -> listState?.let {
            ManPageScreenContent(
                title = title,
                searchText = searchState.query,
                searchVisible = searchState.visible,
                searchResults = searchState.results,
                searchIndex = searchState.index,
                command = command,
                loading = loading,
                listState = it,
                onJumpTo = onJumpTo,
                onAction = onAction,
            )
        }
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
    loading: Boolean,
    listState: LazyListState,
    onJumpTo: (JumpTo) -> Unit,
    onAction: (ManPageAction) -> Unit,
) {
    var forceShowSearch by remember(searchResults) { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val imeManager = LocalSoftwareKeyboardController.current
    val commandDataList = remember(command.id) {
        command.data.toList()
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
    ) {
        val (searchBar, pageData, textMatchBar) = createRefs()

        if (loading) {
            LoadingIndicator()
        }

        if (forceShowSearch || (searchVisible && searchResults.isNull())) {
            SearchBarWithButton(
                modifier = Modifier
                    .constrainAs(searchBar) {
                        top.linkTo(parent.top)
                        width = Dimension.matchParent
                        height = Dimension.wrapContent
                    },
                iconContentDescription =
                    getString(R.string.search_for_text_in_page, searchText, title),
                searchText = searchText,
                searchResults = searchResults,
                onAction = {
                    if (it is ManPageAction.OnSearchPressed) {
                        focusManager.clearFocus(true)
                        imeManager?.hide()
                        forceShowSearch = false
                    }
                    onAction(it)
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
                query = searchResults?.query ?: "",
                index = searchIndex,
                count = searchResults?.count ?: 0,
                showSearchBar = { forceShowSearch = true },
                onPrev = {
                    onAction(ManPageAction.OnPrevPressed)
                },
                onNext = {
                    onAction(ManPageAction.OnNextPressed)
                }
            )
        }

        val pinHandles = remember(command.id) {
            mutableStateListOf<PinnableContainer.PinnedHandle>()
        }

        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
            pinHandles.clear()
        }

        LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
            pinHandles.onEach {
                it.release()
            }.clear()
        }

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
            contentPadding = PaddingValues(
                horizontal = 4.dp,
                vertical = 8.dp,
            ),
        ) {
            items(
                count = commandDataList.size,
                key = { commandDataList[it].first },
                contentType = { it },
            ) { index ->
                commandDataList[index].let { (header, data) ->
                    // Rendering large char counts (~40,000+) in Text composables,
                    // in a lazy list is not scalable.
                    // Encounters major freezing when scrolling to an item with ~40,000+ characters.
                    // Using a custom Prefetch strategy on the list state, as well as pinning all
                    // ManPageSection items, has significantly reduced "hang time".
                    // On substantially large pages, there may be some slight jank or hang time
                    // encountered, briefly upon initial tab load.  Once all items have been pinned,
                    // it is smooth sailing.  This is Vastly preferred to several seconds of app
                    // freezing, literally Every time you scroll a large item off the screen
                    // and then back on.
                    val pinContainer = LocalPinnableContainer.current
                    if (pinHandles.getOrNull(index) == null) {
                        pinContainer?.pin()?.also {
                            pinHandles.add(it)
                            javaClass.ilog("Pinning container handle: $it for $header")
                        }
                    }

                    val singleTextMatch = remember(
                        searchVisible,
                        searchIndex,
                        command.id,
                        searchResults,
                    ) {
                        if (searchVisible) {
                            searchResults?.getMatch(searchIndex)
                                ?.takeIf { it.section == header }
                        } else null
                    }

                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(color = Colors.transparent)
                    )

                    ManPageSection(
                        name = header,
                        data = data,
                        singleTextMatch = singleTextMatch,
                        onTextMatchedOffset = onJumpTo.takeIf { singleTextMatch.isNotNull() }
                    )
                }

                // Used to trigger onScroll in ManPageSectionPrefetchStrategy, so that all
                //  sections can be queued for prefetching.
                LaunchedEffect(command.id) {
                    if (index == 0 && listState.canScrollForward) listState.scrollBy(1f)
                }
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(color = Colors.transparent)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewManPageScreen() {
    ManPageScreen(
        title = "Anything",
        searchState = MockObjects.mockSearchState,
        command = Command(27L, MockObjects.commandData),
        listState = rememberLazyListState(),
        loading = false,
        onAction = {},
        showOfflineDialog = {},
        onJumpTo = {},
    )
}

@Preview
@Composable
private fun PreviewManPageNoSearch() {
    ManPageScreen(
        title = "Something",
        searchState = MockObjects.mockSearchState.copy(
            visible = false,
            results = null,
        ),
        command = Command(27L, MockObjects.commandData),
        listState = rememberLazyListState(),
        loading = true,
        onAction = {},
        showOfflineDialog = {},
        onJumpTo = {},
    )
}

@Preview
@Composable
private fun PreviewManPageLoadingScreen() {
    ManPageScreen(
        title = "Nothing",
        searchState = MockObjects.mockSearchState.copy(
            visible = true,
            results = null,
        ),
        command = Command(27L, MockObjects.commandData),
        listState = rememberLazyListState(),
        loading = false,
        onAction = {},
        showOfflineDialog = {},
        onJumpTo = {},
    )
}
