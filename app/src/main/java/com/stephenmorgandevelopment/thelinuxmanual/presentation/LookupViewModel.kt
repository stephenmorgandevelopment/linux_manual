package com.stephenmorgandevelopment.thelinuxmanual.presentation

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@HiltViewModel
class LookupViewModel(
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<LookupState>(savedStateHandle) {

    override val _state: MutableStateFlow<LookupState>
        get() = TODO("Not yet implemented")

    override fun onAction(action: Action) {
        TODO("Not yet implemented")
    }
}