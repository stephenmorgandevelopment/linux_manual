package com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stephenmorgandevelopment.thelinuxmanual.presentation.Action
import com.stephenmorgandevelopment.thelinuxmanual.presentation.OptionsMenuAction
import com.stephenmorgandevelopment.thelinuxmanual.presentation.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel<StateType : State, ActionType : Action>(
    protected val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    abstract val state: StateFlow<StateType>

    abstract fun onAction(action: ActionType)
    open fun onOptionMenuAction(action: OptionsMenuAction) {}

    protected fun persistState(latestState: StateType) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { savedStateHandle[FULL_STATE_KEY] = latestState }
        }
    }

    companion object {
        const val FULL_STATE_KEY = "FULL_STATE_KEY"
    }
}
