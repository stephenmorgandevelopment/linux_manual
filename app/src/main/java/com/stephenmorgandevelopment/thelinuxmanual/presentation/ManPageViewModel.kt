package com.stephenmorgandevelopment.thelinuxmanual.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.stephenmorgandevelopment.thelinuxmanual.models.Command
import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction.Search
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction.UpdateSearchText
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository
import com.stephenmorgandevelopment.thelinuxmanual.utils.ilog
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNotNull
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNull
import com.stephenmorgandevelopment.thelinuxmanual.utils.sanitizeInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManPageViewModel @Inject constructor(
    private val ubuntuRepository: UbuntuRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ManPageTabState, ManPageAction>(savedStateHandle) {

    private val manPageId: Long = savedStateHandle[ITEM_ID_KEY]
        ?: throw IllegalStateException("Must have manPageId.")

    private val restoredState = savedStateHandle.get<ManPageTabState>(FULL_STATE_KEY)
    private val restoredSearchState = savedStateHandle.get<ManPageSearchState>(SEARCH_STATE_KEY)

    private val _manPage: MutableStateFlow<Command?> =
        MutableStateFlow(restoredState?.command)

    private val _searchResults: MutableStateFlow<TextSearchResult?> =
        MutableStateFlow(restoredSearchState?.results)

    private val _searchVisible = MutableStateFlow<Boolean>(restoredSearchState?.visible ?: false)
    private val _searchText = MutableStateFlow<String>(restoredSearchState?.query ?: "")
    private val _searchIndex = MutableStateFlow<Int>(restoredSearchState?.index ?: 0)

    private val _jumpTo = MutableStateFlow<JumpToData?>(null)
    private val _quickJump = MutableStateFlow<Boolean>(false)

    private val _jumpToQuickly = _jumpTo.combine(_quickJump) { jumpData, quickData ->
        jumpData?.copy(quickJump = quickData)
    }

    override val state = combine(
        _manPage, _jumpToQuickly
    ) { manPage, jumpTo ->
        ManPageTabState(
            command = manPage,
            jumpTo = jumpTo,
        ).also { persistState(it) }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(15_000),
        restoredState ?: ManPageTabState(),
    ).also {
        // The UI will only jump to when the value of jumpTo changes.  If we restore the
        //  jumpTo as part of the initial state, then we won't actually jump to.
        //  Setting the initial value to null, and then changing it here, should trigger the
        //  the UI to scroll to the restored jumpTo value.
        if (restoredState?.jumpTo.isNotNull()) _jumpTo.value = restoredState?.jumpTo
    }

    val searchState = combine(
        _searchVisible, _searchText, _searchResults, _searchIndex,
    ) { visible, query, results, index ->
        ManPageSearchState(manPageId, visible, query, results, index).also {
            savedStateHandle[SEARCH_STATE_KEY] = it
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(15_000),
        restoredSearchState ?: ManPageSearchState(manPageId),
    )

    init {
        if (_manPage.value.isNull()) loadCommand().start()
    }

    override fun onAction(action: ManPageAction) {
        when (action) {
            Search -> searchFor().start()
            is UpdateSearchText -> _searchText.value = sanitizeInput(action.text)

            ManPageAction.PrevSearchMatch -> {
                _searchIndex.value =
                    if (_searchResults.value?.textMatches?.count() == 0) 0
                    else if (_searchIndex.value <= 0) _searchResults.value?.textMatches?.lastIndex
                        ?: 0
                    else _searchIndex.value.minus(1)
            }

            ManPageAction.NextSearchMatch -> {
                _searchIndex.value =
                    if (_searchResults.value?.textMatches?.count() == 0) 0
                    else if (_searchIndex.value >= (_searchResults.value?.textMatches?.lastIndex
                            ?: 0)
                    ) 0
                    else _searchIndex.value.plus(1)
            }

            is ManPageAction.RestoreSearchState -> restoreSearchState(action.searchState)
        }
    }

    override fun onOptionMenuAction(action: OptionsMenuAction) {
        when (action) {
            is ManPageOptionsMenuAction.JumpTo -> jumpTo(action.section, action.offset)
            ManPageOptionsMenuAction.ToggleSearch -> _searchVisible.value = !_searchVisible.value

            else -> {}
        }
    }

    private fun restoreSearchState(state: ManPageSearchState) {
        if (state.id == manPageId) {
            _searchVisible.value = state.visible
            _searchText.value = state.query
            _searchResults.value = state.results
            _searchIndex.value = state.index
        }
    }

    private fun searchFor() = viewModelScope.async {
        withContext(Dispatchers.IO) {
            _searchResults.value = state.value.command?.searchDataForTextMatch(_searchText.value)
        }
    }

    private fun jumpTo(section: String, offset: Int) {
        _jumpTo.value = JumpToData(section, offset)
    }

    private fun loadCommand() = viewModelScope.async {
        withContext(Dispatchers.IO) {
            ubuntuRepository.getMatchingItemById(manPageId)
                ?.let { matchingItem ->
                    ubuntuRepository.getCommandData(matchingItem).let { manPage ->
                        _manPage.value = Command(manPageId, manPage)
                        calculateQuickJump().start()
                    }
                }
        }
    }

    private fun calculateQuickJump() = viewModelScope.async {
        withContext(Dispatchers.Default) {
            _manPage.value?.data?.map { entry ->
                entry.value.length
            }?.sumOf { it.toLong() }
                ?.also { charCount ->
                    javaClass.ilog("totalDataTextSize: $charCount")
                    _quickJump.value = charCount > CHAR_COUNT_BREAKPOINT
                }
        }
    }

    companion object {
        private const val CHAR_COUNT_BREAKPOINT = 70_000L
        private const val SEARCH_STATE_KEY = "SEARCH_STATE_KEY"
        const val TITLE_KEY = "TITLE_KEY"
        const val ITEM_ID_KEY = "ITEM_ID_KEY"
    }
}