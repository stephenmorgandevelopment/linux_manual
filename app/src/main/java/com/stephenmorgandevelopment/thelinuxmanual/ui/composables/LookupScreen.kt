package com.stephenmorgandevelopment.thelinuxmanual.ui.composables

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupViewModel
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.MatchingListItem
import com.stephenmorgandevelopment.thelinuxmanual.ui.composables.components.SearchBar
import com.stephenmorgandevelopment.thelinuxmanual.utils.MockObjects

@Composable
fun LookupScreen(
    searchOnBottom: Boolean,
    viewModel: LookupViewModel = viewModel(
        viewModelStoreOwner = LocalViewModelStoreOwner.current
            ?: (LocalActivity.current as ComponentActivity),
    ),
    onItemClick: (Long, String) -> Unit,
) {
    val activity = LocalActivity.current as ComponentActivity?
        ?: throw IllegalStateException("We explicitly set this in our activity, so this shouldn't happen.")

    val state by viewModel.state.collectAsStateWithLifecycle(lifecycleOwner = activity)

    LookupScreenContent(searchOnBottom, state, viewModel::onAction, onItemClick)
}

@Composable
private fun LookupScreenContent(
    searchOnBottom: Boolean,
    state: LookupState,
    onAction: (LookupAction) -> Unit,
    onItemClick: (Long, String) -> Unit,
) {
    Log.i("LookupScreenCompose", "recomposing lookup screen with ${state.matches.size} items.")

    ConstraintLayout(
        modifier = Modifier.fillMaxSize(),
    ) {
        val (searchBar, matchList) = createRefs()

        SearchBar(
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 6.dp),
        ) {
            items(
                items = state.matches.toList(),
                contentType = { null },
                key = { it.id },
            ) {
                onAction(LookupAction.AddDescription(it.id))

                MatchingListItem(it.name, it.description) {
                    onItemClick(it.id, it.url)
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
        MockObjects.matchItems,
    )

    LookupScreenContent(true, lookupState, {}) { _, _ -> }
}

@Preview
@Composable
private fun PreviewLookupScreenSearchBottom() {
    val lookupState = LookupState(
        "Random",
        MockObjects.lookupItemsLongList,
    )

    LookupScreenContent(true, lookupState, {}) { _, _ -> }
}
