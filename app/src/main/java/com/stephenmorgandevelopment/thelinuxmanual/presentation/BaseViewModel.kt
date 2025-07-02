package com.stephenmorgandevelopment.thelinuxmanual.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel<StateType : State, ActionType : Action>(
    protected val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // TODO:  Implement something on the _state or state, that will trigger updates to saveStateHAndle.
    protected abstract val _state: MutableStateFlow<StateType>

    abstract fun onAction(action: ActionType): Unit

    init {
//         TODO: Verify that we can background this parcelization to bundle.
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                _state.collect { savedStateHandle[FULL_STATE_KEY] = it }
//            }
//        }
//        state.onEach { savedStateHandle[FULL_STATE_KEY] = it }.launchIn(viewModelScope)
    }

    companion object {
        const val FULL_STATE_KEY = "FULL_STATE_KEY"
    }
}