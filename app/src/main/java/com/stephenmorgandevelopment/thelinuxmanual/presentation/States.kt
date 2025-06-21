package com.stephenmorgandevelopment.thelinuxmanual.presentation

import android.graphics.drawable.Icon
import androidx.compose.runtime.Immutable
import com.stephenmorgandevelopment.thelinuxmanual.models.Command
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult

interface State

@Immutable
data class ScreenState(
    val toolbarTitle: String,
    val tabs: List<PagerTab>,
    val optionMenuItems: List<OptionsMenuItem>,
): State

@Immutable
data class LookupState(
    val searchText: String,
    val matches: List<MatchingItem>,
): State

@Immutable
data class PagerTab(
    val title: String,
    val command: Command,
    val searchResults: TextSearchResult,
)

@Immutable
data class OptionsMenuItem(
    val title: String,
    val icon: Icon,
)

