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
    val title: String,
    val subtitle: String,
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
    val loading: Boolean = false,
    val currentSection: String = "",
) : State {
    fun getSectionIndexByName(sectionName: String?): Int? {
        if (sectionName.isNullOrBlank()) return null

        command?.data?.toList()?.let { list ->
            for (idx in list.indices) {
                if (list[idx].first == sectionName) return idx
            }
        }

        return null
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

const val JUMP_TO_OPTIONS_MENU_OFFSET = 40
const val JUMP_TO_RENDERING_OFFSET = 60

@Parcelize
@Immutable
data class JumpToData(
    val section: String,
    val offset: Int,
) : Parcelable
