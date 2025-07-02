package com.stephenmorgandevelopment.thelinuxmanual.presentation

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.stephenmorgandevelopment.thelinuxmanual.models.Command
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult
import kotlinx.parcelize.Parcelize

interface State : Parcelable

@Parcelize
@Immutable
data class ScreenState(
    val toolbarTitle: String,
    val selectedTabIndex: Int,
    val tabs: List<PagerTab>,
    val tabsOnBottom: Boolean = false,
    val searchOnBottom: Boolean = false,
    val syncProgress: String? = null,
): State

@Parcelize
@Immutable
data class LookupState(
    val searchText: String,
    val matches: List<MatchingItem>,
): State

@Parcelize
@Immutable
data class PagerTab(
    val title: String,
    val command: Command? = null,
    val searchResults: TextSearchResult? = null,
) : Parcelable
