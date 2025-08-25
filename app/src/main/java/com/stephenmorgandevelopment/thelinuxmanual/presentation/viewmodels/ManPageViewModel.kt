package com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.stephenmorgandevelopment.thelinuxmanual.models.Command
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
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageScreenState
import com.stephenmorgandevelopment.thelinuxmanual.presentation.OptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation.ManPage
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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ManPageViewModel @Inject constructor(
    private val ubuntuRepository: UbuntuRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<ManPageScreenState, ManPageAction>(savedStateHandle) {
    private val route = savedStateHandle.toRoute<ManPage>()
    private val manPageId: Long = route.manPageId

    private val initialSearchState = savedStateHandle.get<ManPageSearchState>(SEARCH_STATE_KEY)
        ?: ManPageSearchState(id = manPageId)

    // state
    private val initialState = savedStateHandle.get<ManPageScreenState>(FULL_STATE_KEY)
        ?: ManPageScreenState(searchState = initialSearchState)

   private val _state = MutableStateFlow(initialState)
    override val state = _state.asStateFlow()

    private val _jumpToEvents = MutableSharedFlow<JumpToData>(2, 1, BufferOverflow.DROP_OLDEST)
    val jumpToEvents: SharedFlow<JumpToData> = _jumpToEvents.asSharedFlow()

    init {
        if (st8.command.isNull()) loadCommand().start()
    }

    override fun onAction(action: ManPageAction) {
        when (action) {
            OnSearchPressed -> {
                _state.update { it.copy(loading = true) }
                searchFor().start()
            }

            OnPrevPressed -> {
                val index = if (st8.searchState.results?.textMatches?.count() == 0) 0
                else if (search.index <= 0) search.results?.textMatches?.lastIndex ?: 0
                else search.index.minus(1)

                searchResultAt(index)?.section?.let {
                    jumpTo(it, JUMP_TO_RENDERING_OFFSET)
                }

                _state.update { it.copy(searchState = it.searchState.copy(index = index)) }
            }

            OnNextPressed -> {
                val index = if (search.results?.textMatches?.count() == 0) 0
                else if (search.index >= (search.results?.textMatches?.lastIndex ?: 0)) 0
                else search.index.plus(1)

                searchResultAt(index)?.section?.let {
                    jumpTo(it, JUMP_TO_RENDERING_OFFSET)
                }

                _state.update { it.copy(searchState = it.searchState.copy(index = index)) }
            }

            is OnSearchTextUpdated -> _state.update {
                it.copy(searchState = it.searchState.copy(query = sanitizeInput(action.text)))
            }

            is OnScroll -> _state.update { it.copy(currentSection = action.sectionName) }
        }
    }

    override fun onOptionMenuAction(action: OptionsMenuAction) {
        when (action) {
            is JumpTo -> {
                _state.update { it.copy(loading = true) }
                jumpTo(action.section, action.offset)
            }

            ToggleSearch -> _state.update {
                it.copy(searchState = it.searchState.copy(visible = !it.searchState.visible))
            }
            else -> {}
        }
    }

    fun restoreSearchState(state: ManPageSearchState) {
        _state.update { it.copy(searchState = state) }
    }

    fun clearLoading() {
        _state.update { it.copy(loading = false) }
    }

    private fun searchFor() = viewModelScope.async {
        withContext(Dispatchers.IO) {
            if (search.results?.query != search.query) {
                _state.update {
                    it.copy(
                        searchState = it.searchState.copy(
                            index = 0,
                            results = state.value.command?.searchDataForTextMatch(search.query).also {
                                if (it.isNull() || it?.count == 0) clearLoading()
                            }?.also { allResults ->
                                allResults.getMatch(search.index)?.section?.let {
                                    _jumpToEvents.tryEmit(
                                        JumpToData(section = it, JUMP_TO_RENDERING_OFFSET)
                                    )
                                }
                            },
                        )
                    )
                }
            } else clearLoading()
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
                            _state.update { it.copy(command =  Command(manPageId, data)) }
                        }
                        ?: also {
                            _state.update { it.copy(command = Command(-1L, emptyMap())) }
                        }
                }
        }
    }

    private fun searchResultAt(index: Int) = search.results?.getMatch(index)

    private val st8 get() = state.value
    private val search get() = state.value.searchState

    companion object {
        private const val SEARCH_STATE_KEY = "SEARCH_STATE_KEY"
        const val TITLE_KEY = "TITLE_KEY"
        const val ITEM_ID_KEY = "ITEM_ID_KEY"
    }
}
