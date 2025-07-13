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
    val tabs: List<TabInfo>,
    val tabsOnBottom: Boolean = false,
    val searchOnBottom: Boolean = false,
    val syncProgress: String? = null,
) : State

@Parcelize
@Immutable
data class LookupState(
    val searchText: String,
    val matches: List<MatchingItem>,
) : State

@Parcelize
@Immutable
data class ManPageTabState(
    val command: Command? = null,
//    val searchResults: TextSearchResult? = null,
//    val searchVisible: Boolean = false,
//    val searchText: String = "",
    val jumpTo: JumpToData? = null,
) : State {
    fun getSectionIndexByName(sectionName: String?): Int? {
        return sectionName?.let { command?.data?.keys?.indexOf(it) }
    }
}

@Parcelize
@Immutable
data class ManPageSearchState(
    val id: Long,
    val visible: Boolean = false,
    val query: String = "",
    val results: TextSearchResult? = null,
    val index: Int = 0,
) : State

@Parcelize
@Immutable
data class TabInfo(
    val title: String,
    val manPageId: Long = -1L,
    val manPageSections: List<String> = emptyList(),
) : Parcelable

@Parcelize
@Immutable
data class TabInfoExp(
    val title: String,
    val manPageId: Long = -1L,
    val manPageSections: List<String> = emptyList(),
    val command: Command? = null,

    ) : Parcelable

@Parcelize
@Immutable
data class JumpToData(
    val section: String,
    val offset: Int = 0,
    val quickJump: Boolean = false,
) : Parcelable