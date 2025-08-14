package com.stephenmorgandevelopment.thelinuxmanual.ui.composables

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupState
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.MatchingListItem
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.SearchBar
import com.stephenmorgandevelopment.thelinuxmanual.utils.PreviewObjects

@Composable
fun LookupScreen(
    searchOnBottom: Boolean,
    state: LookupState,
    onAction: (LookupAction) -> Unit,
    lazyListState: LazyListState,
    onMatchingItemClick: (String, Long) -> Unit,
) {
    BackHandler(enabled = false) {}

    LookupScreenContent(
        searchOnBottom,
        state,
        lazyListState,
        onAction,
        onMatchingItemClick
    )
}

@Composable
private fun LookupScreenContent(
    searchOnBottom: Boolean,
    state: LookupState,
    lazyListState: LazyListState,
    onAction: (LookupAction) -> Unit,
    onItemClick: (String, Long) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val imeManager = LocalSoftwareKeyboardController.current

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
    ) {
        val (searchBar, matchList) = createRefs()

        SearchBar(
            text = state.searchText,
            modifier = Modifier.constrainAs(searchBar) {
                top.linkTo(if (searchOnBottom) matchList.bottom else parent.top)
                bottom.linkTo(if (searchOnBottom) parent.bottom else matchList.top)
                height = Dimension.wrapContent
                width = Dimension.matchParent
            }
        ) { onAction(LookupAction.UpdateSearchText(it)) }

        LazyColumn(
            modifier = Modifier.constrainAs(matchList) {
                top.linkTo(if (searchOnBottom) parent.top else searchBar.bottom)
                bottom.linkTo(if (searchOnBottom) searchBar.top else parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.matchParent
            },
            state = lazyListState,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 5.dp),
        ) {
            items(
                items = state.matches,
                contentType = { null },
                key = { it.id },
            ) {

                MatchingListItem(name = it.name, description = it.descriptionPreview) {
                    focusManager.clearFocus(true)
                    imeManager?.hide()
                    onItemClick(it.name, it.id)
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                )
            }
        }

    }
}

@Preview
@Composable
private fun PreviewLookupScreen() {
    val lookupState = LookupState(
        "Not Random",
        PreviewObjects.matchItems,
    )

    LookupScreenContent(true, lookupState, rememberLazyListState(), {}) { _, _ -> }
}

@Preview
@Composable
private fun PreviewLookupScreenSearchBottom() {
    val lookupState = LookupState(
        "Random",
        PreviewObjects.lookupItemsLongList,
    )

    LookupScreenContent(true, lookupState, rememberLazyListState(), {}) { _, _ -> }
}
