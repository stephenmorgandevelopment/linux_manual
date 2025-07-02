@file:OptIn(DelicateCoroutinesApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.stephenmorgandevelopment.thelinuxmanual.data.SimpleCommandsDatabase
import com.stephenmorgandevelopment.thelinuxmanual.models.MatchingItem
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupAction.AddDescription
import com.stephenmorgandevelopment.thelinuxmanual.presentation.LookupAction.UpdateSearchText
import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository
import com.stephenmorgandevelopment.thelinuxmanual.utils.ilog
import com.stephenmorgandevelopment.thelinuxmanual.utils.queryAdjusted
import com.stephenmorgandevelopment.thelinuxmanual.utils.sanitizeInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LookupViewModel @Inject constructor(
    private val ubuntuRepository: UbuntuRepository,
    private val roomDatabase: SimpleCommandsDatabase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LookupState, LookupAction>(savedStateHandle) {
    override val _state: MutableStateFlow<LookupState> = MutableStateFlow(
        LookupState(
            "",
            emptyList(),
        )
    )
    val state: StateFlow<LookupState> = _state.asStateFlow()

    override fun onAction(action: LookupAction) {
        when (action) {
            is UpdateSearchText -> processTextInput(sanitizeInput(action.text))
            is AddDescription -> matchBy(action.itemId)
                ?.takeIf { it.needsDescription }
                ?.let { addDescription(it) }
        }
    }

    private fun processTextInput(text: String) {
        _state.update { it.copy(searchText = text) }
        findMatches(text)
    }

    private fun addDescription(matchingItem: MatchingItem) = viewModelScope.launch(Dispatchers.IO) {
        ubuntuRepository.addDescription(matchingItem)?.let { updatedItem ->
            _state.update {
                it.copy(
                    matches = it.matches.map { item ->
                        if (item.id == matchingItem.id) updatedItem else item
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun findMatches(searchText: String) {
        when {
            searchText.length < 2 || searchText.isBlank() ->
                _state.update { it.copy(matches = emptyList()) }

            else -> viewModelScope.launch(Dispatchers.IO) {
                roomDatabase.dao().partialMatches(searchText.queryAdjusted)
                    .filterNotNull()
                    .flowOn(Dispatchers.Main)
                    .collectLatest { matchingItems ->
                        javaClass.ilog("collecting ${matchingItems.size} items.")
                        _state.update { it.copy(matches = matchingItems) }.also {
                            javaClass.ilog("updating state")
                        }
                    }
            }.apply {
                invokeOnCompletion { e ->
                    javaClass.ilog(" completed: $e")
                }
            }
        }
    }

    private val matches get() = state.value.matches
    private fun matchBy(id: Long) = matches.firstOrNull { it.id == id }
}
