package com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.stephenmorgandevelopment.thelinuxmanual.models.Command
import com.stephenmorgandevelopment.thelinuxmanual.models.TextSearchResult
import com.stephenmorgandevelopment.thelinuxmanual.presentation.JUMP_TO_RENDERING_OFFSET
import com.stephenmorgandevelopment.thelinuxmanual.presentation.JumpToData
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction.OnNextPressed
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction.OnPrevPressed
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction.OnScroll
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction.OnSearchPressed
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageAction.OnSearchTextUpdated
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageOptionsMenuAction.JumpTo
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageOptionsMenuAction.ToggleSearch
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageSearchState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageTabState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.OptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository
import com.stephenmorgandevelopment.thelinuxmanual.utils.isNull
import com.stephenmorgandevelopment.thelinuxmanual.utils.sanitizeInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
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

    // state
    private val restoredState = savedStateHandle.get<ManPageTabState>(FULL_STATE_KEY)

    private val _manPage: MutableStateFlow<Command?> = MutableStateFlow(restoredState?.command)
    private val _loading = MutableStateFlow<Boolean>(false)
    private val _currentSection = MutableStateFlow<String>(restoredState?.currentSection ?: "")

    override val state = combine(
        _manPage, _currentSection, _loading,
    ) { manPage, currentSection, loading ->
        ManPageTabState(
            command = manPage,
            loading = loading,
            currentSection = currentSection,
        ).also { persistState(it) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        restoredState ?: ManPageTabState(),
    )

    // search state
    private val restoredSearchState = savedStateHandle.get<ManPageSearchState>(SEARCH_STATE_KEY)

    private val _searchResults: MutableStateFlow<TextSearchResult?> =
        MutableStateFlow(restoredSearchState?.results)
    private val _searchVisible = MutableStateFlow<Boolean>(restoredSearchState?.visible ?: false)
    private val _searchText = MutableStateFlow<String>(restoredSearchState?.query ?: "")
    private val _searchIndex = MutableStateFlow<Int>(restoredSearchState?.index ?: 0)

    val searchState = combine(
        _searchVisible, _searchText, _searchResults, _searchIndex,
    ) { visible, query, results, index ->
        ManPageSearchState(manPageId, visible, query, results, index).also {
            savedStateHandle[SEARCH_STATE_KEY] = it
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        restoredSearchState ?: ManPageSearchState(manPageId),
    )


    private val _jumpToEvents = MutableSharedFlow<JumpToData>(2, 1, BufferOverflow.DROP_OLDEST)
    val jumpToEvents: SharedFlow<JumpToData> = _jumpToEvents.asSharedFlow()

    init {
        if (_manPage.value.isNull()) loadCommand().start()
    }

    override fun onAction(action: ManPageAction) {
        when (action) {
            OnSearchPressed -> {
                _loading.value = true
                searchFor().start()
            }

            OnPrevPressed -> gotoSearchIndex(prevSearchIndex)
            OnNextPressed -> gotoSearchIndex(nextSearchIndex)

            is OnSearchTextUpdated -> _searchText.value = sanitizeInput(action.text)
            is OnScroll -> _currentSection.value = action.sectionName
        }
    }

    override fun onOptionMenuAction(action: OptionsMenuAction) {
        when (action) {
            is JumpTo -> jumpTo(action.section, action.offset)
            ToggleSearch -> _searchVisible.value = !_searchVisible.value
            else -> {}
        }
    }

    fun restoreSearchState(state: ManPageSearchState) {
        if (state.id == manPageId) {
            _searchVisible.value = state.visible
            _searchText.value = state.query
            _searchResults.value = state.results
            _searchIndex.value = state.index
        }
    }

    private fun gotoSearchIndex(index: Int) {
        searchResultAt(index)?.section?.let {
            jumpTo(it, JUMP_TO_RENDERING_OFFSET)
        }

        _searchIndex.value = index
    }

    private val prevSearchIndex
        get() = if (_searchResults.value?.textMatches?.count() == 0) 0
        else if (_searchIndex.value <= 0) _searchResults.value?.textMatches?.lastIndex ?: 0
        else _searchIndex.value.minus(1)

    private val nextSearchIndex
        get() = if (_searchResults.value?.textMatches?.count() == 0) 0
        else if (_searchIndex.value >= (_searchResults.value?.textMatches?.lastIndex ?: 0)) 0
        else _searchIndex.value.plus(1)

    private fun searchFor() = viewModelScope.async {
        withContext(Dispatchers.IO) {
            if (_searchResults.value?.query != _searchText.value) {
                _searchIndex.value = 0

                _searchResults.value =
                    state.value.command
                        ?.searchDataForTextMatch(_searchText.value)
                        .also { if (it == null || it.count == 0) _loading.value = false }
                        ?.also { allResults ->
                            allResults.getMatch(_searchIndex.value)?.section
                                ?.let {
                                    jumpTo(it, JUMP_TO_RENDERING_OFFSET)
                                    _loading.value = false
                                }
                        }
            } else _loading.value = false
        }
    }

    private fun jumpTo(section: String, offset: Int) {
        _jumpToEvents.tryEmit(JumpToData(section, offset))
    }

    private fun loadCommand() = viewModelScope.async {
        withContext(Dispatchers.IO) {
            ubuntuRepository.getMatchingItemById(manPageId)
                ?.let { matchingItem ->
                    ubuntuRepository.getCommandData(matchingItem)
                        .takeIf { it.isNotEmpty() }
                        ?.let { data ->
                            _manPage.value = Command(manPageId, data)
                        }
                        ?: also {
                            _manPage.value = Command(-1L, emptyMap())
                        }
                }
        }
    }

    private val searchResults get() = _searchResults.value
    private fun searchResultAt(index: Int) = searchResults?.getMatch(index)

    companion object {
        private const val SEARCH_STATE_KEY = "SEARCH_STATE_KEY"
        const val TITLE_KEY = "TITLE_KEY"
        const val ITEM_ID_KEY = "ITEM_ID_KEY"
    }
}
