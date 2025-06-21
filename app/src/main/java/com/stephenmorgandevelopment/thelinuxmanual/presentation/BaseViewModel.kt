package com.stephenmorgandevelopment.thelinuxmanual.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
abstract class BaseViewModel<State>(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    protected abstract val _state: MutableStateFlow<State>
    val state: StateFlow<State> = _state.asStateFlow()

    abstract fun onAction(action: Action): Unit

}